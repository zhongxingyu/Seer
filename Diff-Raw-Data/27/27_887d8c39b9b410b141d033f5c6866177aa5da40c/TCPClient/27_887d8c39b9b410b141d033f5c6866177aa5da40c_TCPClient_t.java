 package fi.hwo.manuel;
 
 import android.nfc.Tag;
 import android.util.Log;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
import java.net.SocketTimeoutException;
 
 
 public class TCPClient implements Runnable {
 
   private String serverMessage;
   private String server;
   private int port;
   //  public static final String SERVERIP = "boris.helloworldopen.fi"; //your computer IP address
 //  public static final int SERVERPORT = 9090;
   private OnMessageReceived mMessageListener = null;
   private boolean mRun = false;
 
   PrintWriter out;
   BufferedReader in;
 
   /**
    *  Constructor of the class. OnMessagedReceived listens for the messages received from server
    */
   public TCPClient(String server, int port, OnMessageReceived listener) {
     this.server = server;
     this.port = port;
     mMessageListener = listener;
   }
 
   public void sendMessage(String message) {
     if (out != null && !out.checkError()) {
       out.println(message);
       out.flush();
     } else {
       Log.e("TCP", "error");
     }
   }
 
   public void stopClient() {
     mRun = false;
   }
 
   public void run() {
     mRun = true;
 
     Log.d("TCP", "Connecting " + server + ":" + port);
 
     try {
       Socket socket = new Socket(InetAddress.getByName(server), port);
      socket.setSoTimeout(5000);
 
       try {
         out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
         while (mRun) {
          try {
            serverMessage = in.readLine();
            Log.v("TCP", "Received message " + serverMessage);
            if (serverMessage != null && mMessageListener != null) {
              mMessageListener.messageReceived(serverMessage);
            }
            serverMessage = null;
          } catch (SocketTimeoutException ste) {}
         }
       } catch (Exception e) {
         Log.e("TCP", "S: Error", e);
       } finally {
        Log.e("TCP", "Disconnecting from server");
         socket.close();
        mMessageListener.disconnected();
        }
     } catch (Exception e) {
       Log.e("TCP", "C: Error", e);
     }
   }
 
   public interface OnMessageReceived {
     public void messageReceived(String message);
    void disconnected();
   }
 }
