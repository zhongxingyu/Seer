 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc3946.UltimateAscent;
 
 import com.sun.squawk.util.StringTokenizer;
 import edu.wpi.first.wpilibj.Timer;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import javax.microedition.io.Connector;
 import javax.microedition.io.SocketConnection;
 
 /**
  *
  * @author Gustave Michel
  */
 public class ThreadedPi {
     
     private String url = "Socket://10.39.46.12:10000";
     private int bufferSize = 64;
     private char delimiter = ',';
     
     private SocketConnection m_socket;
     private InputStream m_is;
     private OutputStream m_os;
     
     String m_rawData;
     byte[] m_receivedData;
     
     private boolean m_connected = false;
     
     Thread m_thread;
     private boolean m_enabled;
     private boolean m_run = true;
     
     public static class DataKeeper {
         private static int m_distance;
         private static int m_offset;
         private static double m_time;
         
         public static synchronized void setDistance(int distance) {
             m_distance = distance;
         }
         public static synchronized void setOffset(int offset) {
             m_offset = offset;
         }
         public static synchronized void setTime(double time) {
             m_time = time;
         }
         
         public static synchronized int getDistance() {
             return m_distance;
         }
         public static synchronized int getOffset() {
             return m_offset;
         }
         public static synchronized double getTime() {
             return m_time;
         }
     }
     
     private class RaspberryPiThread extends Thread {
         ThreadedPi m_raspberryPi;
         public int distance;
         public int offset;
         public double time;
         private boolean report;
     
         public RaspberryPiThread(ThreadedPi raspberryPi) {
             m_raspberryPi = raspberryPi;
         }
         
         public void run() {
             while(m_run) {
                 if(m_raspberryPi.isEnabled()) { //Checks for Thread to run
                     if(m_raspberryPi.isConnected()) {
                         report = true;
                         try {
                             String[] data = m_raspberryPi.tokenizeData(m_raspberryPi.getRawData()); //Get and examine Data
                             time = Timer.getFPGATimestamp();
                             if(data.length < 4) { //Error Check
                                 report = false;
                             } else {
                                 try {
                                     distance = Integer.parseInt(data[1]); //Get data
                                     offset = Integer.parseInt(data[0]);
                                 } catch(NumberFormatException ex) {
                                     report = false;
                                 }
                             }
                            System.out.println("Pi Data Get!!!!");
                         } catch (IOException ex) {
                             report = false;
                         }
                         if(report) { //Store Data in DataKeeper
                             DataKeeper.setDistance(distance);
                             DataKeeper.setOffset(offset);
                             DataKeeper.setTime(time);
                         }
                     } else {
                         try {
                             m_raspberryPi.connect();
                         } catch (IOException ex) {}
                     }
                 }
                 try {
                     Thread.sleep(500); //Wait half second before getting Data again
                 } catch(InterruptedException ex) {}
             }
         }
     }
     
     public ThreadedPi() {
         m_enabled = false;
         m_thread = new RaspberryPiThread(this);
     }
     
     public synchronized void connect() throws IOException {
         m_socket = (SocketConnection) Connector.open(url, Connector.READ_WRITE, false);
         m_is = m_socket.openInputStream();
         m_os = m_socket.openOutputStream();
         m_connected = true;
     }
     
     public synchronized void disconnect() throws IOException {
         m_socket.close();
         m_is.close();
         m_os.close();
         m_connected = false;
     }
     
     public synchronized boolean isConnected() {
         //need to actually test the connection 
         //to figure out if we're connected or not
         try{
             m_os.write('\n'); //request Data
         }
         catch(IOException ex){
             m_connected = false;
         }
         
         return m_connected;
     }
     
     public synchronized boolean isEnabled() {
         return m_enabled;
     }
     
     public int getOffset() {
         return DataKeeper.getOffset();
     }
     
     public int getDistance() {
         return DataKeeper.getDistance();
     }
     
     public double getTime() {
         return DataKeeper.getTime();
     }
     public synchronized void start() {
         m_enabled = true;
     }
     
     public synchronized void stop() {
         m_enabled = false;
     }
     
     /**
      * Requests data from RaspberryPi
      * @return String returned from RaspberryPi
      * @throws IOException 
      */
     public synchronized String getRawData() throws IOException {
         byte[] input;
         
         if (m_connected) {
             m_os.write('G'); //request Data
             System.out.println("Requested Data");
             
             if(m_is.available() <= bufferSize) {
                 input = new byte[m_is.available()]; //storage space sized to fit!
                 m_receivedData = new byte[m_is.available()];
                 m_is.read(input);
                 for(int i = 0; (i < input.length) && (input != null); i++) {
                     m_receivedData[i] = input[i]; //transfer input to full size storage
                 }
             } else {
                 System.out.println("PI OVERFLOW");
                 m_is.skip(m_is.available()); //reset if more is stored than buffer
                 return null;
             }
             
             m_rawData = ""; //String to transfer received data to
             System.out.println("Raw Data: "+m_receivedData.length);
             for (int i = 0; i < m_receivedData.length; i++) {
                 m_rawData += (char) m_receivedData[i]; //Cast bytes to chars and concatinate them to the String
             }
             System.out.println(m_rawData);
             return m_rawData;
         } else {
             connect();
             return null;
         }
     }
     
     /**
      * Separates input String into many Strings based on the delimiter given
      * @param input String to be tokenized
      * @return String Array of Tokenized Input String
      */
     public synchronized String[] tokenizeData(String input) {
         StringTokenizer tokenizer = new StringTokenizer(input, String.valueOf(delimiter));
         String output[] = new String[tokenizer.countTokens()];
         
         for(int i = 0; i < output.length; i++) {
             output[i] = tokenizer.nextToken();
         }
         return output;
     }
 }
