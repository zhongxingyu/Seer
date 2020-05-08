 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dht;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tcc10a
  */
 public class DHTClient {
 
     private static final int port = 1138;
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException {
         // TODO code application logic here
         
         if (args.length != 1) {
             System.err.println("Usage: <host>");
             System.exit(1);
         } 
 
         String serverHostname = args[0];
         
         System.out.println("Attemping to connect to host " + serverHostname + " on port "+port);
 
         Socket echoSocket = null;
         PrintWriter out = null;
         BufferedReader in = null;
 
         try {
             echoSocket = new Socket(serverHostname, port);
             out = new PrintWriter(echoSocket.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
         } catch (UnknownHostException e) {
             System.err.println("Don't know about host: " + serverHostname);
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for " + "the connection to: " + serverHostname);
             System.exit(1);
         }
 
         BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
         String userInput;
 
         System.out.println("Type Message (\"quit\" to quit, \"shutdown\" to shutdown both the client and the server)");
         while ((userInput = stdIn.readLine()) != null) {
             // send message to server
             out.println(userInput);
 
             // end loop
             if (userInput.equals("quit") || userInput.equals("shutdown")) {
                 break;
             }
 
             String output = in.readLine();
             if (output == null) {
                 break;
             }
             if (userInput.contains("article")) {
                 if (output.contains("FAIL:")) {
                     String node = output.substring(7);
                     System.out.println("Error: Try node: "+node);
                 } else {
                     System.out.println(output);
                 }
             } else {
                 System.out.println("Response: " + output);
             }
         }
 
         out.close();
         in.close();
         stdIn.close();
         echoSocket.close();
     }
 }
