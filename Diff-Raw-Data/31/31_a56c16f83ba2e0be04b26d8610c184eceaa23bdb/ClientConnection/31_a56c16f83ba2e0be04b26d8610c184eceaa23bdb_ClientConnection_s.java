 package ru.sgu.itcourses.chat.model;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.sgu.itcourses.chat.server.ServerCore;
 import ru.sgu.itcourses.chat.utils.SynchronizedDataInputStream;
 import ru.sgu.itcourses.chat.utils.SynchronizedDataOutputStream;
 
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 /**
  * @author Konovalov_Nik
  */
 public class ClientConnection extends Thread {
     private static final Logger LOG = LoggerFactory.getLogger(ClientConnection.class);
     private Socket socket;
     private SynchronizedDataInputStream in;
     private SynchronizedDataOutputStream out;
     private User user;
 
     public ClientConnection(Socket socket) {
         this.socket = socket;
         try {
             in = new SynchronizedDataInputStream(new DataInputStream(socket.getInputStream()));
         } catch (IOException e) {
             LOG.error("Cant get input stream", e);
             throw new RuntimeException(e);
         }
         try {
             out = new SynchronizedDataOutputStream(new DataOutputStream(socket.getOutputStream()));
         } catch (IOException e) {
             LOG.error("Cant get output stream", e);
             throw new RuntimeException(e);
         }
     }
 
     public User getUser() {
         return user;
     }

    public void setUser(User user) {
        this.user = user;
    }

     @Override
     public void run() {
         while (true) {
             try {
                 String s = in.readUTF();
                 processCommand(s.trim());
             } catch (IOException e) {
                 LOG.warn("Connection dropped.");
                 break;
             }
         }
     }
 
     private void processCommand(String s) {
         String[] splitted = s.split(" ");
         if ("connect".equals(splitted[0])) {
             String password;
             if (splitted.length < 3) {
                 password = "";
             } else {
                 password = splitted[2];
             }
             processConnect(splitted[1], password);
         } else {
             ServerCore.getInstance().addMessage(new Message(user.getLogin(), splitted));
         }
     }
 
     private void processConnect(String login, String password) {
         if (ServerCore.getInstance().isRegistered(login)) {
             User user = ServerCore.getInstance().checkPasswordAndGet(login, password);
             if (user != null) {
                 this.user = user;
             } else {
                 try {
                     this.user = null;
                     out.writeUTF("Wrong password");
                     socket.close();
 
                 } catch (IOException e) {
                     LOG.error("Cant send error message", e);
                 }
                 return;
             }
         } else {
             user = ServerCore.getInstance().register(login, password);
         }
         send("Send 'help' to get list of supported commands.");
     }
 
     public void send(String text) {
         try {
             out.writeUTF(text);
         } catch (IOException e) {
             LOG.error("Cant send message " + text);
         }
     }
 
     public void send(String[] text) {
         try {
             out.writeUTF(text);
         } catch (IOException e) {
             LOG.error("Cant send message " + text[0] + ", " + text[1]);
         }
     }
 
     public void close() {
         try {
             socket.close();
         } catch (IOException e) {
             //
         }
     }
 
     public boolean isConnected() {
        return ! socket.isClosed();
     }
 }
