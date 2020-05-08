 package com.github.wsrv.nio;
 
 import com.github.wsrv.nio.message.request.HttpRequest;
 import com.github.wsrv.nio.message.request.HttpRequestParser;
 import com.github.wsrv.nio.message.response.HttpResponse;
 import com.github.wsrv.nio.message.response.HttpResponseFactory;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.util.concurrent.Callable;
 
 /**
  * @author tommaso
  */
 class SocketHandler implements Callable<Long> {
   private final Logger log = LoggerFactory.getLogger(SocketHandler.class);
 
   private final Socket socket;
 
   private Boolean persistentConnection;
 
   public SocketHandler(Socket socket) {
     this.socket = socket;
     persistentConnection = true;
   }
 
   @Override
   public Long call() throws Exception {
     try {
       long s = System.nanoTime();
       run();
       long e = System.nanoTime();
       return e - s;
     } finally {
       log.info("finished running");
     }
   }
 
   void run() {
     try {
       // read from the socket inpustream
       while (persistentConnection) {
         InputStream socketInputStream = socket.getInputStream();
         byte[] b = new byte[2048];
         socketInputStream.read(b);
         String requestString = new String(b);
         if (requestString.length() > 20) {
           HttpRequestParser httpRequestParser = new HttpRequestParser();
           // parse the request
           HttpRequest httpRequest = httpRequestParser.parse(requestString);
           if (log.isDebugEnabled())
             log.debug("parsed HTTP request :\n{}", httpRequest);
 
           // create the response
           HttpResponseFactory httpResponseFactory = new HttpResponseFactory();
           HttpResponse httpResponse = httpResponseFactory.createResponse(httpRequest);
           if (log.isDebugEnabled())
             log.debug("parsed HTTP response :\n{}", httpResponse);
 
           // handle connection persistence
           persistentConnection = httpResponse.isKeepAlive();
 
          // write response to socket output stream
           IOUtils.write(httpResponse.toString(), socket.getOutputStream());
         }
       }
     } catch (Exception e) {
       throw new RuntimeException(e);
     } finally {
       try {
         socket.close();
       } catch (IOException e) {
         // do nothing
       }
     }
   }
 
 }
