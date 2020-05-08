 package de.htwg.wzzrd.control.network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import de.htwg.wzzrd.model.GameNetwork;
 import de.htwg.wzzrd.model.IPacket;
 import de.htwg.wzzrd.model.PacketQueue;
 import de.htwg.wzzrd.model.network.packet.JoinGamePacket;
 
 public class ConnectionListener extends Thread {
     private ServerSocket listener;
     private String password;
     private PacketQueue pq;
 
    // set to 0 for no timeout
    public static final int CONNECTION_TIMEOUT = 0;
 
     public ConnectionListener(int port, String password, PacketQueue pq) throws IOException {
         this.listener = new ServerSocket(port);
         this.password = password;
         this.pq = pq;
         this.setName("connectionListener");
         this.start();
     }
 
     /**
      * Checks for client connection requests and answers those.
      */
     @Override
     public void run() {
         try {
             System.out.println("Starting ConnectionListener on port: " + listener.getLocalPort());
             while (true) {
                 Socket client = listener.accept();
                 GameNetwork gnet = new GameNetwork(client);
 
                 IPacket inpack = gnet.receive(CONNECTION_TIMEOUT);
                 System.out.println(inpack.getClass().getSimpleName());
                 if (inpack instanceof JoinGamePacket) {
                     JoinGamePacket jg = (JoinGamePacket) inpack;
                     if (jg.getPassword().equals(password)) {
                         jg.setGameNetwork(gnet);
                         new ClientThread(pq, jg);
                         pq.addPacket(jg);
                         System.out.println("Accepted connection from " + jg.getName());
                     } else {
                         System.out.println(jg.getName() + " tried to enter with wrong password.");
                     }
                 } else {
                     System.out.println("First packet recieved was no JoinGamePacket");
                 }
             }
         } catch (SocketException e) {
             //
         } catch (IOException e) {
             //
         } catch (ClassNotFoundException e) {
             // untestable, needs a missing class file
             System.err.println("Missing Class File: " + e.getCause().getMessage());
         }
     }
 
     @Override
     public void interrupt() {
         try {
             listener.close();
         } catch (IOException e) {
             //
         }
         super.interrupt();
     }
 }
