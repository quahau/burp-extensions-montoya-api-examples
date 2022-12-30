package example.httphandler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;

import static burp.api.montoya.http.handler.RequestToSendAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;
import static burp.api.montoya.http.message.params.HttpParameter.urlParameter;

class MyHttpHandler implements HttpHandler {
    private final Logging logging;

    public MyHttpHandler(MontoyaApi api) {
        this.logging = api.logging();
    }


    @Override
    public RequestToSendAction handleHttpRequestToSend(HttpRequestToSend requestToSend) {
        Annotations annotations = requestToSend.annotations();

        // If the request is a post, log the body and add a comment annotation.
        if (isPost(requestToSend)) {
            annotations = annotations.withComment("Request was a post");
            logging.logToOutput(requestToSend.bodyToString());
        }

        //Modify the request by adding url param.
        HttpRequest modifiedRequest = requestToSend.withAddedParameters(urlParameter("foo", "bar"));

        //Return the modified request to burp with updated annotations.
        return continueWith(modifiedRequest, annotations);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        Annotations annotations = responseReceived.annotations();
        //Highlight all responses where the request had a Content-Length header.
        if (responseHasContentLengthHeader(responseReceived)) {
            annotations = annotations.withHighlightColor(HighlightColor.BLUE);
        }

        return continueWith(responseReceived, annotations);
    }

    private static boolean isPost(HttpRequestToSend httpRequestToSend) {
        return httpRequestToSend.method().equalsIgnoreCase("POST");
    }

    private static boolean responseHasContentLengthHeader(HttpResponseReceived httpResponseReceived) {
        return httpResponseReceived.initiatingRequest().headers().stream().anyMatch(header -> header.name().equalsIgnoreCase("Content-Length"));
    }
}
