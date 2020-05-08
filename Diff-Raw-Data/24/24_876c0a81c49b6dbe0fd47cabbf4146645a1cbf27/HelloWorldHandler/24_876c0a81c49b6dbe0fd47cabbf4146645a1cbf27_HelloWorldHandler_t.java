 package cukepresentation;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 import java.io.IOException;
 
 /**
  * @author: Stephen Abrams
  */
 public class HelloWorldHandler implements HttpHandler {
 
    private static final String helloWorldStr = "Hello, World!";
 
     @Override
     public void handle(HttpExchange httpExchange) throws IOException {
 
         httpExchange.sendResponseHeaders(200, helloWorldStr.length());
         httpExchange.getResponseBody().write(helloWorldStr.getBytes());
     }
 }
