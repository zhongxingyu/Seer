 package ch.rollis.emma.response;
 
 import ch.rollis.emma.HttpRequest;
 import ch.rollis.emma.HttpServerConfig;
 
 public class HttpResponseFactory {
     public HttpResponse getResponse() {
         return getDefaultResponse();
     }
 
     public HttpResponse getResponse(HttpRequest request) {
         HttpResponse response = getDefaultResponse();
         response.setProtocol(request.getProtocol());
         return response;
     }
 
     public HttpResponse getResponse(HttpResponseStatus status) {
         HttpResponse response = getDefaultResponse();
         response.setStatus(status);
         response.setEntity(String.format("<h1>%s - %s</h1>", status.getCode(),
                 status.getReasonPhrase()));
         return response;
     }
 
     public HttpResponse getResponse(HttpRequest request, HttpResponseStatus status) {
         HttpResponse response = getDefaultResponse();
         response.setProtocol(request.getProtocol());
         response.setStatus(status);
         response.setEntity(String.format("<h1>%s - %s</h1>", status.getCode(),
                 status.getReasonPhrase()));
         return response;
     }
 
     private HttpResponse getDefaultResponse() {
         HttpResponse response = new HttpResponse();
         response.addHeader("Server", HttpServerConfig.SERVER_TOKEN);
         return response;
     }
 }
