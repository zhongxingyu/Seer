 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package distclient;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  *
  * @author paul
  */
 public class TestClient {
     
     public TestClient () {}
     
     public void DistTestServEnterNetwork () {
         DistConfig distConfig = DistConfig.get_Instance();
         try {
             System.out.println("Connecting");
             Socket sock = new Socket("127.0.0.1", distConfig.get_servPortNumber());
 
             sock.setSoTimeout(5000);
             System.out.println("Connected");
             
             BufferedOutputStream bos = new BufferedOutputStream (
                                     sock.getOutputStream());
             System.out.println("Got OutputStream");
             PrintWriter outStream = new PrintWriter(bos, false);
             System.out.println("Got PrintWriter");
 //            ObjectOutputStream oos = new ObjectOutputStream (bos);
 //            System.out.println("Got ObjectOutputStream");
 
 
             BufferedReader in = new BufferedReader (
                         new InputStreamReader (
                                 sock.getInputStream()));
             System.out.println("Got InputStream");
             
 
             
             System.out.println("Sending Code");
             outStream.println(ConnectionCodes.ENTERNETWORK);
             outStream.flush();
             
             ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
             System.out.println("Got Object InputStream");
             
             DistConfig sdc = (DistConfig) ois.readObject();
             DistConfig mdc = DistConfig.get_Instance();
             mdc.increment_CurrNodes();
             System.out.println(sdc.get_CurrNodes());
             System.out.println(mdc.get_CurrNodes());
             
             outStream.println(ConnectionCodes.ENTERNETWORK);
             outStream.flush();
             
             int id = Integer.parseInt(in.readLine());
             System.out.println(id);
             
             outStream.println(ConnectionCodes.ENTERNETWORK);
             outStream.flush();
             
             int servID = Integer.parseInt(in.readLine());
             String servIP = in.readLine();
             System.out.println(servID);
             System.out.println(servIP);
             
             outStream.println(ConnectionCodes.ENTERNETWORK);
             outStream.flush();
         }
         
         catch (UnknownHostException uhe) {
             uhe.printStackTrace(System.err);
         }
         catch (IOException ioe) {
             ioe.printStackTrace(System.err);
         }
         catch (ClassNotFoundException cnfe) {
             cnfe.printStackTrace(System.err);
         }
         
         
     }
     
     
     public void DistTestServCheckPosition (int myID) {
    	DistConfig distConfig = DistConfig.get_Instance();
    	try {
             System.out.println("Connecting");
             Socket sock = new Socket("127.0.0.1", distConfig.get_servPortNumber());
 
             sock.setSoTimeout(5000);
             System.out.println("Connected");
             
             BufferedOutputStream bos = new BufferedOutputStream (
                                     sock.getOutputStream());
             System.out.println("Got OutputStream");
             PrintWriter outStream = new PrintWriter(bos, false);
             System.out.println("Got PrintWriter");
 
             BufferedReader in = new BufferedReader (
                         new InputStreamReader (
                                 sock.getInputStream()));
             System.out.println("Got InputStream");
             
 
             
             System.out.println("Sending Code");
             outStream.println(ConnectionCodes.CHECKPOSITION);
             outStream.flush();
             
             ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
             System.out.println("Got Object InputStream");
             
             System.out.println("Getting Ack");
             System.out.println(in.readLine());
             
             System.out.println("Sending my ID as " + myID);
             outStream.println(Integer.toString(myID));
             outStream.flush();
             
             String tmpline = in.readLine();
             
             if (Integer.parseInt(tmpline) == ConnectionCodes.NEWID) {
                 myID = Integer.parseInt(in.readLine());
                 System.out.println("New ID = " + myID);
                 tmpline = in.readLine();
             }
             
             if (Integer.parseInt(tmpline) == ConnectionCodes.CORRECTPOSITION) {
                 String[] predicessor = (String[])ois.readObject();
                 System.out.println("Correct Position");
                 System.out.println("Pred ID = " + predicessor[0]);
                 System.out.println("Pred IP = " + predicessor[1]);
                 String[] successor = (String[])ois.readObject();
                 System.out.println("Next ID = " + successor[0]);
                 System.out.println("Next IP = " + successor[1]);
             }
             else {
                 String nextTestID = in.readLine();
                 String nextTestIP = in.readLine();
                 System.out.println("Wrong Position");
                 System.out.println("next ID = " + nextTestID);
                 System.out.println("next IP = " + nextTestIP);
             }
         }
         
         catch (UnknownHostException uhe) {
             uhe.printStackTrace(System.err);
         }
         catch (IOException ioe) {
             ioe.printStackTrace(System.err);
         }
         catch (ClassNotFoundException cnfe) {
             cnfe.printStackTrace(System.err);
         }
         
         
     }
     
     public static void main (String[] args) {
    	System.out.println("Hello");
         TestClient dtc = new TestClient();
        dtc.DistTestServCheckPosition(5);
     }
     
 }
