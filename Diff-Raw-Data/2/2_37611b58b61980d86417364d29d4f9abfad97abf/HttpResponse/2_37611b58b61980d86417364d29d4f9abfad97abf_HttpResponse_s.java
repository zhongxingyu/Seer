 package com.janx57.aweb.server.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 public class HttpResponse extends HttpMessage {
   private int statusCode;
   private String reasonPhrase;
 
   public void setStatusCode(final int statusCode) {
     this.statusCode = statusCode;
   }
 
   public void setReasonPhrase(final String reasonPhrase) {
     this.reasonPhrase = reasonPhrase;
   }
 
   public byte[] toByteArray() {
     StringBuilder responseText = new StringBuilder();
    responseText.append(version + sp + statusCode + reasonPhrase);
     responseText.append(crlf);
     for (String key : headers.keySet()) {
       responseText.append(key + ":" + sp + headers.get(key) + crlf);
     }
     responseText.append(crlf);
 
     ByteArrayOutputStream response = new ByteArrayOutputStream();
     try {
       response.write(responseText.toString().getBytes());
       response.write(body);
     } catch (IOException e) {
       throw new IllegalStateException(e.getMessage());
     }
     return response.toByteArray();
   }
 }
