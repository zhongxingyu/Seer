 package com.madavan.bridge.server;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 public class BridgePlayer extends Thread {
 
   private DataOutputStream _dataOut;
   private DataInputStream  _dataIn;
   private Socket _socket;
   
   public BridgePlayer(Socket socket) throws IOException {
     _socket = socket;
     
     _dataIn  = new DataInputStream(_socket.getInputStream());
     _dataOut = new DataOutputStream(_socket.getOutputStream());
   }
   
   /**
    * Sends a message string via a DataOutputStream.
    * 
    * @param str String to be sent
    */
   public void send(String str) throws IOException {
     _dataOut.writeUTF(str);
   }
   
   /**
    * Reads the entire contents of the InputStream. Note, this method
    * will return regardless of whether or not there is data to be read.
    * 
    * @return message
    */
   public String readAll() throws IOException {
     String message = "";
     while(_dataIn.available() > 0)
       message += _dataIn.readUTF();
     return message;
   }
   
   /**
    * Read the next message from the InputStream. Note, this method
    * will block until a message is available to be read.
    * 
    * @return message
    */
   public String readNext() throws IOException {
     return _dataIn.readUTF();
   }
 }
