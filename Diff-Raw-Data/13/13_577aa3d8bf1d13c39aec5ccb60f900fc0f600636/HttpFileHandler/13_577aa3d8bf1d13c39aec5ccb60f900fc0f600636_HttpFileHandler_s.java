 package com.interview.web.http;
 
 import com.interview.web.SocketHandler;
 import com.interview.web.utils.Constants;
 import com.interview.web.utils.RequestBuilder;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.nio.channels.SocketChannel;
 
 public class HttpFileHandler implements SocketHandler, HttpHandler {
     public static final int HTTP_OK = 200;
     public static final int HTTP_SERVER_ERROR = 500;
     public static final String TEXT_HTML = "text/html";
     public static final String GET = "GET";
     public static final String POST = "POST";
 
     private static Logger logger = Logger.getLogger(HttpFileHandler.class);
     private String root;
     private HttpHandler successor;
 
     @Override
     public void handle(SocketChannel socketChannel) {
         PrintStream out = null;
         try {
             out = new PrintStream(socketChannel.socket().getOutputStream(), true);
             InputStream inputStream = socketChannel.socket().getInputStream();
             HttpRequest request = RequestBuilder.getRequest(inputStream);
 
             String method = request.getHeader(Constants.METHOD);
             handle(method, request, out);
         } catch (IOException e) {
             logger.error("Unable to parse request headers", e);
             sendError(out, HTTP_SERVER_ERROR, e.getMessage());
         } finally {
             closeSocket(socketChannel);
         }
     }
 
     protected void printHeaders(PrintStream out, int code, String contentType) {
         out.println("HTTP/1.1 " + code);
         out.println("Content-Type: " + contentType);
         out.println();
     }
 
     protected void sendError(PrintStream out, int code, String message) {
         message = "Error\r\n " + code + ": " + message;
         printHeaders(out, code, "text/plain; charset=utf-8");
         out.println(message);
     }
 
     private void closeSocket(SocketChannel socketChannel) {
         try {
             socketChannel.close();
         } catch (IOException e) {
             logger.error("Error closing socket", e);
         }
     }
 
     @Override
     public void handle(String method, HttpRequest request, PrintStream out) throws IOException {
         getSuccessor().handle(method, request, out);
     }
 
     public String getRoot() {
        if(root == null) {
            return ".";
        }

         return root;
     }
 
     public void setRoot(String root) {
         this.root = root;
     }
 
     public Logger getLogger() {
         return logger;
     }
 
     public HttpHandler getSuccessor() {
         return successor;
     }
 
     public void setSuccessor(HttpHandler successor) {
         this.successor = successor;
     }
 }
