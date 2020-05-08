 package com.ninja_squad.jb.codestory;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Throwables;
 import com.google.common.io.Closeables;
 
 import javax.annotation.Nonnull;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.charset.StandardCharsets;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * The main loop, which listens for new HTTP connections.
  * @author JB
  */
 class ListenLoop implements Runnable {
 
     private final ServerSocket serverSocket;
     private final ActionFactory actionFactory;
 
     private final ExecutorService requestExecutor;
 
     /**
      * Indicates that the thread should stop serving requests. We can't use standard thread interrupts because
      * the server socket doesn't react to them.
      */
     private final AtomicBoolean stopped = new AtomicBoolean(false);
 
     public ListenLoop(@Nonnull ServerSocket serverSocket,
                       @Nonnull ActionFactory actionFactory) {
         this.serverSocket = serverSocket;
         this.actionFactory = actionFactory;
         this.requestExecutor = Executors.newCachedThreadPool();
     }
 
     /**
      * Stops the listen loop by closing the server socket.
      */
     public void stop() {
         stopped.set(true);
         this.requestExecutor.shutdownNow();
         try {
             serverSocket.close();
         }
         catch (IOException e) {
             // too bad, but we don't care.
         }
     }
 
     @Override
     public void run() {
         while (!stopped.get()) {
             try {
                 Socket socket = serverSocket.accept();
                 answerTo(socket);
             }
             catch (IOException e) {
                 // too bad : either the request couldn't be served, and we don't care, or
                 // the server socket can't accept a connection because it has been closed. If that's the
                 // case, the stopped flag will be set to true and the loop will end.
             }
         }
     }
 
     /**
      * Answers to a request.
      */
     private void answerTo(final Socket socket) {
         requestExecutor.execute(new Runnable() {
             @Override
             public void run() {
                 try (InputStream in = new BufferedInputStream(socket.getInputStream());
                      OutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                     HttpRequest request = HttpRequest.parse(in);
                     HttpResponse response;
                     try {
                         response = actionFactory.getAction(request).execute(request);
                     }
                     catch (Exception e) {
                         response = createErrorResponse(e);
                     }
                     response.send(out);
                    out.flush();
                 }
                 catch (IOException e) {
                     // too bad
                 }
                 finally {
                     try {
                         Closeables.close(socket, true);
                     }
                     catch (IOException e) {
                         // impossible
                         throw Throwables.propagate(e);
                     }
                 }
             }
 
 
         });
     }
 
     @VisibleForTesting
     protected HttpResponse createErrorResponse(Exception e) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         PrintWriter out = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
         out.println("Internal error:");
         e.printStackTrace(out);
         out.flush();
         out.close();
         return new HttpResponse(HttpResponse.Status._500_INTERNAL_ERROR,
                                 HttpHeaders.builder()
                                            .setContentType("text/plain", StandardCharsets.UTF_8)
                                            .build(),
                                 baos.toByteArray());
     }
 }
