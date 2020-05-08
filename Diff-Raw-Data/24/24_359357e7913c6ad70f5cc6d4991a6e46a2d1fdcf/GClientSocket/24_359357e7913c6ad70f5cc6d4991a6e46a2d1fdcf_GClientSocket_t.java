 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Game.Network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  *
  * @author michal
  */
 public class GClientSocket {
 
     String host;
     int port;
     Socket sock;
     ObjectOutputStream objOut;
     ObjectInputStream objIn;
     ClientSocketListener listener;
 
     public GClientSocket(String host, int port, GameClient client) {
         this.host = host;
         this.port = port;
         initSock();
         initListener(client);
     }
 
     private void initSock() {
         try {
             sock = new Socket(host, port);
             objOut = new ObjectOutputStream(sock.getOutputStream());
             objIn = new ObjectInputStream(sock.getInputStream());
             System.out.println("Socket successfully initialised.");
         } catch (UnknownHostException ex) {
             System.out.println("Error while initialising the client socket.");
             ex.printStackTrace();
         } catch (IOException ex) {
             System.out.println("Error while initialising the client socket.");
             ex.printStackTrace();
         }
     }
 
     private void initListener(GameClient client) {
         listener = new ClientSocketListener(this, client);
         Thread t = new Thread(listener);
         t.start();
     }
 
     public void disconnect(boolean sendMessage) {
         try {
             if (sendMessage) {
                 sendObject("disconnect");
             }
             objOut.close();
             objIn.close();
             sock.close();
             System.out.println("Disconnected from server.");
         } catch (IOException ex) {
             System.out.println("Error while disconnecting socket.");
             ex.printStackTrace();
         }
     }
 
     public void sendObject(Object o) throws IOException {
         objOut.writeObject(o);
         objOut.flush();
        objOut.reset();
     }
 
     public Object readObject() throws IOException, ClassNotFoundException {
         Object in = objIn.readObject();
         System.out.println("%%%%%%%%%%%%%%%%%%% RECEIVED FROM SERVER %%%%%%%%%%");
         System.out.println("Read: ");
         System.out.println(in);
         System.out.println("%%%%%%%%%%%%%%%%%");
         return in;
     }
 }
