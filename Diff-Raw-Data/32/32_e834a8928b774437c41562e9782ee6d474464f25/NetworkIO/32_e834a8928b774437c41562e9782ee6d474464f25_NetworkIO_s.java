 package edu.stuy.util;
 
 import java.io.*;
 import javax.microedition.io.*;
 
public class NetworkIO{
     private static final int PORT = 6940;
     private static final String HOST = "10.6.95.242";
     private SocketConnection requestSocket;
     private DataOutputStream out;
     private DataInputStream in;
     private String message;
     private double mostRecentOut;
    public NetworkIO(){
         mostRecentOut = 694; // return a huge value by default, the caller better know this
         try {
             SocketConnection requestSocket = (SocketConnection) Connector.open("socket://" + HOST + ":" + PORT);
             requestSocket.setSocketOption(SocketConnection.LINGER, 5);
             in = new DataInputStream(requestSocket.openInputStream());
             out = new DataOutputStream(requestSocket.openOutputStream());
             out.flush();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
    public void run()
    {
         try {
             double output;
             if (in.available() > 0) {
                 output  = in.readDouble();
             }
             else {
                 output = mostRecentOut;
             }
             message = "" + output;
             mostRecentOut = Double.parseDouble(message);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
    void sendMessage(String msg)
    {
        try{
             out.write(msg.getBytes());
             out.flush();
        }
        catch(IOException ioException){
             ioException.printStackTrace();
         }
     }
    public double getMostRecent () {
         return mostRecentOut;
     }
    public double getCurrent()
    {
         run();
         return getMostRecent();
     }
 }
