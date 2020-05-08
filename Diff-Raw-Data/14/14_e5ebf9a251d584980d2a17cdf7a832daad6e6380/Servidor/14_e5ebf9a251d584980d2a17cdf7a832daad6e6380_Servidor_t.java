 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tareaiiiredes;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 /**
  *
  * @author Rodrigo
  */
 public class Servidor {
     
     ServerSocket SS;
     Socket NewCon = null;
     int port = 8003;
     int id=-1;
     
     public void run() throws IOException
     {
         // open server socket
         
         try {
             SS = new ServerSocket(port); 
         } catch (IOException e) {
             System.err.println("Could not start server: " + e);
             System.exit(-1);
         }
         System.out.println("FileServer accepting connections on port " + port);
         
          // request handler loop
         while (true) {
             
             try {
                 // wait for request
                 NewCon = SS.accept();
                 new Thread(new NewConHandler(NewCon, id++)).start();
                     
             }
            finally{}   
         }
     }
     
     public static void main(String args[]) throws IOException {
 		Servidor server = new Servidor();
 		server.run();
 	}
 }
