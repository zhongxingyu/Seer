 package ch.rollis.emma.response;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.HashMap;
 
 import ch.rollis.emma.HttpMethod;
 import ch.rollis.emma.HttpRequest;
 import ch.rollis.emma.util.DateFormatter;
 
 public class HttpResponse {
     private String protocol;
     private HttpRequest request;
     private HttpResponseStatus status;
     private final HashMap<String, String> headers;
     private byte[] entityBody;
 
     private static final String CRLF = "\r\n";
     private static final String SP = " ";
 
     HttpResponse() {
         this("HTTP/1.1");
     }
 
     HttpResponse(String protocol) {
         this.protocol = protocol;
         headers = new HashMap<String, String>();
     }
 
     public void setProtocol(String protocol) {
         this.protocol = protocol;
     }
 
     public String getProtocol() {
         if (request != null) {
             return request.getProtocol();
         }
         return protocol;
     }
 
     public HttpRequest getRequest() {
         return request;
     }
 
     public void setRequest(HttpRequest request) {
         this.request = request;
     }
 
     /**
      * @return the status
      */
     public HttpResponseStatus getStatus() {
         return status;
     }
 
     public void setStatus(HttpResponseStatus status) {
         this.status = status;
     }
 
     public String getContentType() {
         return getHeader("Content-type");
     }
 
     public void setContentType(String value) {
         addHeader("Content-type", value);
     }
 
     public String getContentLength() {
         return getHeader("Content-length");
     }
 
     public void setContentLength(String value) {
         addHeader("Content-length", value);
     }
 
     public String getLastModified() {
         return getHeader("Last-modified");
     }
 
     public void setLastModified(String value) {
         addHeader("Last-modified", value);
     }
 
     private String getHeader(String key) {
         if (headers.containsKey(key)) {
             return headers.get(key);
         }
         return null;
     }
 
     void addHeader(String key, String value) {
         headers.put(key, value);
     }
 
     public void setEntity(String entity) {
         setEntity(entity.getBytes());
     }
 
     public void setEntity(byte[] entity) {
         this.entityBody = entity;
     }
 
     public void send(OutputStream output) throws IOException {
         BufferedOutputStream out = new BufferedOutputStream(output);
 
         if (getHeader("Content-length") == null) {
             addHeader("Cotnent-length", String.valueOf(entityBody.length));
         }
 
        if (!"HTTP/0.9".equals(getProtocol())) {
            sendStatusLine(out);
            sendHeaders(out);
        }
 
         if (request == null || request.getMethod() != HttpMethod.HEAD) {
             out.write(entityBody);
         }
         out.flush();
     }
 
     private void sendStatusLine(BufferedOutputStream out) throws IOException {
         StringBuilder sb = new StringBuilder();
         sb.append(protocol).append(SP).append(status.getCode()).append(SP)
         .append(status.getReasonPhrase()).append(CRLF);
         out.write(sb.toString().getBytes());
     }
 
     private void sendHeaders(BufferedOutputStream out) throws IOException {
         if (!headers.containsKey("Date")) {
             headers.put("Date", DateFormatter.formatRfc1123(new Date()));
         }
         for (String field : headers.keySet()) {
             out.write(String.format("%s: %s", field, headers.get(field)).getBytes());
             out.write(CRLF.getBytes());
         }
         out.write(CRLF.getBytes());
     }
 }
