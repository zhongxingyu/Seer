 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GraphDB;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.net.Socket;
 
 /**
  *
  * @author michal
  */
 public abstract class DBSocket {
 
     Socket sock;
 
     BufferedReader in;
     PrintStream out;
     ObjectInputStream objIn;
     ObjectOutputStream objOut;
     
     int port;
     boolean secure;
     
 
     public DBSocket() {
     };
 
     public DBSocket(int port, boolean secure) {
         this.port = port;
         this.secure = secure;
         createSocket();
     }
 
     public abstract void createSocket();
 
     public abstract void disconnect();
 
     public void sendString(String s) {
         out.println(s + "\n");
     }
 
     public void sendObject(Object o) {
     }
 
     public String getStringMessage() throws IOException {
         StringBuilder build = new StringBuilder();
         String inLine = in.readLine();
         System.out.println(inLine);
        while (!inLine.equals("")) {
             build.append(inLine);
             inLine = in.readLine();
         }
         return build.toString();
     }
 
     public Object getObjectMessage() throws IOException, ClassNotFoundException {
         return objIn.readObject();
     }
 }
