 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Game.Network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 /**
  *
  * @author michal
  */
 public class GServerSocket {
 
     Socket sock;
     ObjectOutputStream objOut;
     ObjectInputStream objIn;
     ServerSocketListener listener;
     boolean killed;
     String sockName;
 
     public GServerSocket(Socket sock, GameServer server, String sockName) {
         this.sock = sock;
         this.sockName = sockName;
         killed = false;
         getStreams();
         initListener(server);
         
     }
 
     private void initListener(GameServer server){
         listener = new ServerSocketListener(this, server);
         Thread t = new Thread(listener);
         t.start();
     }
 
     private void getStreams() {
         try {
             objIn = new ObjectInputStream(sock.getInputStream());
             objOut = new ObjectOutputStream(sock.getOutputStream());
         } catch (IOException ex) {
             System.err.println("Server socket unable to get streams.");
             ex.printStackTrace();
         }
     }
 
     public void disconnect(boolean sendMessage) {
         try {
             if (sendMessage) {
                 sendObject("disconnect");
             }
             objOut.close();
             objIn.close();
             sock.close();
             killed = true;
             Thread.currentThread().interrupt();
             System.out.println("Client disconnected.");
         } catch (IOException ex) {
             System.out.println("Error while disconnecting socket." + this.hashCode());
             ex.printStackTrace();
         }
     }
 
     public void printString(String s) {
         System.out.println(s + this.hashCode());
     }
 
     public void sendObject(Object o) throws IOException {
         System.out.println("{{{{{{{{{{{{{{{{{{{{{sending object}}}}}}}}}}}}}");
         System.out.println("Sending :");
         System.out.println(o);
         System.out.println("{{{{{{{{{{{{{{{{{{}}}}}}}}}}}}}}}}");
         objOut.writeObject(o);
         objOut.flush();
        objOut.reset();
     }
 
     public Object readObject() throws IOException, ClassNotFoundException {
         return objIn.readObject();
     }
 
 }
