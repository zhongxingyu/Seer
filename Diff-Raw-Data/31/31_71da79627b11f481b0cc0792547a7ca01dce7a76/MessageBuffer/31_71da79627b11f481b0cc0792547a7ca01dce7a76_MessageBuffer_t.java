 package chatosaur.client;
 
 import java.net.*;
 import java.io.*;
 
 import chatosaur.common.ConnectedServer;
 
 public class MessageBuffer implements Runnable {
 
     private Client client;
     private Thread thread;
 
     public MessageBuffer(Client client) {
 
         this.client = client;
 
         this.thread = new Thread(this);
         thread.start();
     }
 
     public void reconnectNow() {
         boolean reconnected = false;
         for (int i=0; i<client.serverList.size(); i++) {
             ConnectedServer shot = client.serverList.get(i);
 
             if (client.connectToServer(shot.host, shot.port)) {
                System.out.println("You were reconnected to a new server.\n");
                 reconnected = true;
                 break;
             }
         }
 
         if (!reconnected) {
            System.out.println("You were disconnected and could not re-connect.\n");
             System.exit(0);
         }
     }
 
     public void run() {
         try {
             while (true) {
 
                 String message = client.in.readLine();
 
                 if (message == null) {
                    thread.sleep(1000);
                     reconnectNow();
                 } else {
                     System.out.println(message);
                 }
 
                 thread.sleep(1000);
             }
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
