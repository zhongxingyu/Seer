 package com.arlandis;
 
 import com.arlandis.interfaces.Request;
 import com.arlandis.interfaces.ResponseBuilder;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 public class HttpResponseBuilder implements ResponseBuilder {
 
     @Override
     public String generateResponse(Request request) {
         String statusHeader = "HTTP/1.0 200 OK";
         String contentTypeHeader = "Content-type: text/html";
         String body;
 
         if (request.headers().startsWith("GET /form")) {
             body = formBody();
         } else if (request.headers().startsWith("POST ")) {
             body = formParams(request);
         } else if (request.headers().startsWith("GET /ping?sleep")) {
             ThreadSleeper sleeper = new ThreadSleeper();
             request.sleep(sleeper);
             body = pongBody();
         } else {
             body = pongBody();
         }
 
         return statusHeader + "\r\n" + contentTypeHeader + "\r\n\r\n" + body;
 
     }
     private String formBody() {
         return "<html><body>" +
                "<form method='post', action='/form'>" +
                "<label>foo<input name='foo'></label>" +
                "<br /><label>bar<input name='bar'></label>" +
                "<br /><input value='submit' type='submit'></form>";
     }
 
     private String pongBody() {
         return "<html><body>pong</body></html>";
     }
 
     private String formParams(Request request) {
         String LINE_BREAK = "<br />";
         return "foo = " + decodeValue(request.fooValue()) + " " + LINE_BREAK + " bar = " + decodeValue(request.barValue());
     }
 
     private String decodeValue(String value) {
         try {
             return URLDecoder.decode(value, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return "";
         }
     }
 
 
 }
