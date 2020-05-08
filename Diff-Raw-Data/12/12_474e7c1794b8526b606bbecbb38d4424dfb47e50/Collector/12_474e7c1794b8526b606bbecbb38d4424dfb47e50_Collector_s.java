 package DistGrep;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kyle
  * Date: 9/15/13
  * Time: 8:54 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Collector implements Runnable {
 
     private String output = "";
     private Socket conn;
 
     public Collector(Socket conn) {
         this.conn = conn;
     }
 
     public String getOutput() { return output; }
 
     public void run() {
         if(conn == null)
             return;
 
         try {
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             InputStream inputStream = conn.getInputStream();
             String header = "\nResults from " + conn.getInetAddress().toString() + ":\n";
             byteArrayOutputStream.write(header.getBytes());
 
             while(true) {
                 byte[] buffer = new byte[4096];
                 int n =  inputStream.read(buffer,0,4096);
 
                 if(n < 0)
                     break;
                byteArrayOutputStream.write(buffer);
             }
             conn.close();
 
             this.output = byteArrayOutputStream.toString();
         }
         catch (IOException e) {
             System.err.println("Failed to collect output from " + conn.getInetAddress() + " " + e);
         }
     }
 }
