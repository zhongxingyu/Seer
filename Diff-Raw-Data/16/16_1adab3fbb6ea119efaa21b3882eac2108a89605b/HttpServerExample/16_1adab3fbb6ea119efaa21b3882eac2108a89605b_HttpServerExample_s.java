 package com.github.tachesimazzoca.java.example;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.net.InetSocketAddress;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 public final class HttpServerExample {
     private HttpServerExample() {
     }
 
     public static void main(String[] args) throws IOException {
         HttpServer server = HttpServer.create(
                 new InetSocketAddress("localhost", 8000), 8000);
         server.createContext("/hello", new HelloHandler());
         server.setExecutor(null); // creates a default executor
         server.start();
     }
 
     private static class HelloHandler implements HttpHandler {
         public void handle(HttpExchange t) throws IOException {
             StringBuilder response = new StringBuilder();
             Headers req = t.getRequestHeaders();
             for (String key : req.keySet()) {
                 for (String value : req.get(key)) {
                   response.append(key);
                   response.append(": ");
                   response.append(value);
                   response.append("\n");
                 }
             }
             String body = response.toString();
             t.sendResponseHeaders(200, body.length());
             Headers res = t.getResponseHeaders();
             res.set("Content-Type", "text/plain");
             OutputStream os = t.getResponseBody();
             os.write(body.getBytes());
             os.close();
         }
     }
 }
