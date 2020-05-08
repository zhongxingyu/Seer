 package Server;
 import java.net.*;
 import java.util.*;
 import java.io.*;
 import Common.Finals;
 
 public class AudioThread extends Thread{
 
     /* This is the server socket to accept connections */
     private ServerSocket serverSocket;
 
     private boolean isRunning; //is the server up?
 
     //I/O
     private InputStream in1, in2;
     private OutputStream out1, out2;
     private Socket clientSocket1;
     private Socket clientSocket2;
     private byte[] client1Data;
     private byte[] client2Data;
 
     public static void main (String[] args){
         AudioThread main = new AudioThread(9001); //9001 is audio port by default
         main.start();
     }
 
     public AudioThread(int port) {
         try{
             serverSocket = new ServerSocket(port);
         } catch (Exception e){ e.printStackTrace(); }
     }
 
     public void run(){
         isRunning = true;
         try {
             //connect clients...
             clientSocket1 = serverSocket.accept();
             System.out.println("New audio connection from "+clientSocket1.getInetAddress().toString());
             clientSocket2 = serverSocket.accept();
             System.out.println("New audio connection from "+clientSocket2.getInetAddress().toString());
 
             //set up I/O
             in1 = clientSocket1.getInputStream(); in2 = clientSocket2.getInputStream();
             out1 = clientSocket1.getOutputStream(); out2 = clientSocket2.getOutputStream();
             client1Data = new byte[Finals.BUFFER_SIZE];
             client2Data = new byte[Finals.BUFFER_SIZE];
 
             int client1BytesRead;
             int client2BytesRead;
             //then facilitate data transfer
             while (isRunning){
                 client1BytesRead = in1.read(client1Data, 0, client1Data.length);
                 client2BytesRead = in2.read(client2Data, 0, client2Data.length);
 
                 out1.write(client2Data, 0, client2BytesRead); //sending to client 1
                 out2.write(client1Data, 0, client1BytesRead); //sending to client 2
             }
         } catch (IOException e) {
             System.out.println("Audio chat server closed due to client disconnecting.");
         }
     }
 
     public void stopRunning(){
         try{
         isRunning = false; clientSocket1.close(); clientSocket2.close();
         } catch (Exception e){ e.printStackTrace(); }
        System.out.println("Audio chat server closing on command.");
     }
 }
