 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serverghome;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author anisbenyoub
  */
 public class ComClient extends Thread{
     
     public int threadID;
     boolean myClientConnected;
     ServerSocket myServer;
     Socket myClient;
     private PrintWriter out;
     ServerGHome myHost;
     
     public ComClient(ServerSocket server, Socket client,ServerGHome host)
     {
         myClientConnected=true;
         myServer=server;
         myClient=client;
         myHost= host;
     }
     @Override
     public void run ()
     {
         BufferedReader in;
         try {
             System.out.println("Associated thread launched");
             System.out.println("Un client s'est connecté");
             out = new PrintWriter(myClient.getOutputStream());
             out.println("C1");
             out.flush();
             while(myClientConnected)
             {
                 System.out.println("Attente d'un message");
                 in = new BufferedReader (new InputStreamReader (myClient.getInputStream()));
                 String message = in.readLine();            
                 System.out.println("message recu"+message);
                out.println("J'ai recu ton message");
                out.flush();
                 System.out.println(message.compareTo("D"));
                if(message.equals("D"))            
                 {
                     myClientConnected=false;
                     myClient.close();
                     myHost.removeClient(myClient.getInetAddress());
                 }
             }
 
 
         } catch (IOException ex) {
             Logger.getLogger(ComClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
