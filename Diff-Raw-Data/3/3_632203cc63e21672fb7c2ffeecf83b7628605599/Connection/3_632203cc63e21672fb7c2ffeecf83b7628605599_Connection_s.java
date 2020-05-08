 package serverTCP;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 /**
  * Created with IntelliJ IDEA.
  * User: joaosimoes
  * Date: 10/15/13
  * Time: 4:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Connection extends Thread {
 
     private boolean shutDown;
     private Socket clientSocket;
     private ObjectInputStream inStream;
     private ObjectOutputStream outStream;
 
 
     public Connection(Socket cSocket) {
         clientSocket = cSocket;
         shutDown = false;
         try {
             inStream = new ObjectInputStream(cSocket.getInputStream());
            outStream = new ObjectOutputStream(cSocket.getOutputStream());
 
         } catch (IOException ie) {
 
         }
     }
 
     @Override
     public void run() {
 
     }
 }
