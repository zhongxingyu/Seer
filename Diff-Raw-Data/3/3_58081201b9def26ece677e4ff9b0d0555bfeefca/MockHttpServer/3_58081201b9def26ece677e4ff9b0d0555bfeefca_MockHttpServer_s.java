 package com.artcom.y60.http;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import com.artcom.y60.Logger;
 
 public class MockHttpServer extends NanoHTTPD {
 
     private static String                 LOG_TAG        = "MockHttpServer";
     private static int                    PORT           = 4000;
     private final HashMap<String, String> mSubResources  = new HashMap<String, String>();
     private int                           mResponseDelay = 0;
     private ClientRequest                 mLastRequest;
     private ClientRequest                 mLastPost;
     private Response                      mLastResponse;
 
     public MockHttpServer() throws IOException {
         super(PORT);
     }
 
     public void setResponseDelay(int pMilliseconds) {
         mResponseDelay = pMilliseconds;
     }
 
     ClientRequest getLastRequest() {
         return mLastRequest;
     }
 
     ClientRequest getLastPost() {
         return mLastPost;
     }
 
     Response getLastResponse() {
         return mLastResponse;
     }
 
     @Override
     public Response serve(ClientRequest request) {
         Logger.v(LOG_TAG, request);
         mLastRequest = request;
         mLastResponse = handleRequest(request);
         return mLastResponse;
     }
 
     private Response handleRequest(ClientRequest request) {
         try {
             Thread.sleep(mResponseDelay);
         } catch (InterruptedException e) {
             Logger.e(LOG_TAG, e);
         }
 
         String acceptedMimeType = request.header.getProperty("accept", NanoHTTPD.MIME_PLAINTEXT);
         if (!acceptedMimeType.equals(NanoHTTPD.MIME_PLAINTEXT)) {
             return new NanoHTTPD.Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, "accept mime-type '"
                     + acceptedMimeType + "' is not implemented ");
         }
 
         String contentType = request.header.getProperty("content-type", NanoHTTPD.MIME_PLAINTEXT);
         if (!contentType.equals(NanoHTTPD.MIME_PLAINTEXT)
                && !contentType.equals(NanoHTTPD.MIME_XML)) {
             return new NanoHTTPD.Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, "content-type '"
                     + contentType + "' is not implemented ");
         }
 
         if (request.method.equals("PUT")) {
             return handlePutRequest(request);
         }
 
         if (!request.uri.equals("/") && !mSubResources.containsKey(request.uri)) {
             return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Not Found");
         }
 
         if (request.method.equals("GET")) {
             return handleGetRequest(request);
         }
 
         if (request.method.equals("POST")) {
             return handlePostRequest(request);
         }
 
         return new NanoHTTPD.Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, "not implemented");
     }
 
     private NanoHTTPD.Response handlePostRequest(ClientRequest request) {
 
         mLastPost = request;
 
         String newResource = "/" + UUID.randomUUID();
         String data = "no data posted";
         String contentType = request.header.getProperty("content-type", "");
         if (contentType.equals(NanoHTTPD.MIME_FORM_URLENCODED)) {
             data = request.parameters.getProperty("message",
                     "no message property given (use http//...?message=<your message>");
         } else if (contentType.equals(NanoHTTPD.MIME_PLAINTEXT)) {
             data = request.body;
         } else if (!contentType.equals("")) {
             return new NanoHTTPD.Response(HTTP_NOTIMPLEMENTED, MIME_PLAINTEXT, "content-type "
                     + contentType + " is not implemented");
         }
 
         mSubResources.put(newResource, data);
 
         NanoHTTPD.Response response;
         response = new NanoHTTPD.Response(HTTP_REDIRECT, MIME_PLAINTEXT, "see other: "
                 + "http://localhost:" + PORT + newResource);
         response.addHeader("Location", "http://localhost:" + PORT + newResource);
         return response;
     }
 
     private Response handleGetRequest(ClientRequest request) {
         String msg = null;
         if (mSubResources.containsKey(request.uri)) {
             msg = mSubResources.get(request.uri);
         } else {
             msg = "I'm a mock server for test purposes";
         }
         return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, msg);
     }
 
     private Response handlePutRequest(ClientRequest request) {
         if (request.uri.equals("/")) {
             return new NanoHTTPD.Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
                     "to create a resource at '/' you need to use HTTP POST");
         }
 
         mSubResources.put(request.uri, request.body);
         return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, request.body);
     }
 
     /**
      * @return basic location and port of the server
      */
     public String getUri() {
         return "http://localhost:" + PORT;
     }
 }
