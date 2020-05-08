 package network;
 
 import java.io.*;
 import java.net.*;
 import java.util.Collection;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import actor.Actor;
 
 public class DedicatedServer {
     private static final long FRAME_RATE = 1000 / 60; // Frame rate in ms
     
     private ServerSocket socket;
     private boolean running;
     private Timer timer;
 
 
     public static void main(String[] args) {
         DedicatedServer server = new DedicatedServer();
         server.run();
     }
     
     private void startup() {
         setRunning(true);
         try {
             socket = new ServerSocket(NetworkProtocol.SERVER_PORT);
         } catch (IOException e) {
             System.out.println("Could not listen on port: " + NetworkProtocol.SERVER_PORT);
             System.exit(-1);
         }
         
         /* Start a timer to handle our per frame updates */
         timer = new Timer();
         timer.scheduleAtFixedRate(new UpdateTask(), 0, FRAME_RATE);
         
         new ListenerThread(this).start();
     }
 
     private void shutdown() {
        /* Clean up */
         timer.cancel();
         
         try {
             /* Close the socket so the listener thread will stop blocking */
             socket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void run() {
         startup();
         
         new ServerCli(this, System.out, System.in).run();
         
         shutdown();
     }
 
     // Main server update code
     private void update() {
        for(Actor a: Actor.actors)
            a.update();
     }
     
    
     public void setRunning(boolean running) {
         this.running = running;
     }
 

     public boolean isRunning() {
         return running;
     }
 

     public Collection<Object> getPlayers() {
         return new Vector<Object>();
     }
 
     private class ListenerThread extends Thread {
         private DedicatedServer server;
         
         public ListenerThread(DedicatedServer server) {
             // We have to pass this through so we can then pass it to ServerClientThread
             this.server = server;
         }
 
         public void run() {
             while (isRunning()) {
                 try {
                     Socket client = socket.accept();
                     new ServerClientThread(client, server).start();
                 } catch (SocketException e) {
                     if (e.getMessage().equals("Socket closed") && isRunning() == false) {
                         System.err.println("Server socket closed, shutting down.");
                     } else {
                         e.printStackTrace();                        
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
     private class UpdateTask extends TimerTask {
         public void run() {
             update();
         }
     }
 }
