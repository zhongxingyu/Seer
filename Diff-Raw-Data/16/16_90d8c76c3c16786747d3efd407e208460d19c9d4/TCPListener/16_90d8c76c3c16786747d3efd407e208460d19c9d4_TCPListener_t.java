 package com.github.etsai.kfsxtrackingserver;
 
 import static com.github.etsai.kfsxtrackingserver.Common.logger;
 import static com.github.etsai.kfsxtrackingserver.Common.threadPool;
 import com.github.etsai.kfsxtrackingserver.web.HTTPHandler;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.file.Path;
 import java.util.logging.Level;
 
 public class TCPListener implements Runnable {
     private int id;
     private final int port;
     private final Path httpRootDir;
 
     public TCPListener(int port, Path httpRootDir) {
         this.port= port;
         this.httpRootDir= httpRootDir;
         this.id= 0;
     }
 
     @Override
     public void run() {
         logger.log(Level.CONFIG, "Listening for http requests on port: {0}", port);
         
         try {
             ServerSocket httpSocket= new ServerSocket(port);
             while(true) {
                Socket connection= httpSocket.accept();
                logger.info(String.format("Received TCP connection from %s:%d", 
                        connection.getInetAddress().getHostAddress(), connection.getPort()));
                threadPool.submit(new HTTPHandler(connection, httpRootDir, id));
                id++;
             }
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
     }
 }
