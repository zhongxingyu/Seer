 package DistGrep;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.*;
 import java.util.Enumeration;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kyle
  * Date: 9/14/13
  * Time: 8:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ConnectionHandler implements Runnable {
 
     private LinkedBlockingQueue<Socket> connectionQueue;
     private String searchPath;
     private boolean shouldRun = true;
     private Config config;
 
     public ConnectionHandler(LinkedBlockingQueue<Socket> connectionQueue, Config conf) {
         this.connectionQueue = connectionQueue;
         this.searchPath = conf.valueFor("searchPath");
 	this.config = conf;
     }
 
     public void kill() {
         this.shouldRun = false;
     }
 
     public void run() {
 
         System.out.println("[" + this.getClass().toString() + "]: Waiting to handle accepted connections: Started");
 
         while(shouldRun) {
 
             // Poll the connection queue for an accepted connection.
             Socket clientSocket = null;
             InetAddress clientAddress = null;
 
             try {
                 clientSocket = connectionQueue.poll(1, TimeUnit.MINUTES);
             }
             catch (InterruptedException e) {
                 break;
             }
 
             //If we timed out or our thread was interrupted, continue.
             if(clientSocket == null)
                 continue;
 
             clientAddress = clientSocket.getInetAddress();
             System.out.println("[" + this.getClass().toString() + "]: Got connection from: " + clientAddress);
 
             String clientMessage;
 
 
             //Attempt to get the message from the client.
             try {
                 clientMessage = readStringFromConnection(clientSocket);
             }
             catch (IOException e) {
                 System.out.println("Failed to get message from client. " + e);
                 try {
                     clientSocket.close();
                 } catch (IOException ex) {}
                 continue;
             }
 
             System.out.println("[" + this.getClass().toString() + "]: Got message from: " + clientAddress);
 
             String[] parsedMessage;
             try {
                 parsedMessage = parseMessage(clientMessage);
             }
             catch (IllegalStateException e) {
                 try {
                     clientSocket.close();
                 } catch (IOException ex) {}
                 continue;
             }
             String header = parsedMessage[0];
             String body = parsedMessage[1];
 
 
             //If a request was sent to this machine, it will execute a grep and sends the results back to the initiator
 
             System.out.println("[" + this.getClass().toString() + "]: Running search for: " + clientAddress);
 
             CommandExecutor grepExecutor = null;
             try {
                 grepExecutor = Search.runSearch(searchPath, body, config.valueFor("delimiter"));
             }
             catch (IOException e) {
                 System.out.println("Failed to generate search results. " + e);
             }
             catch (InterruptedException e) {
                 break;
             }
 
             try {
                 System.out.println("[" + this.getClass().toString() + "]: Delivering results to: " + clientAddress);
                 deliverResults(clientSocket, grepExecutor);
                 clientSocket.close();
             }
             catch (SocketException e) {
                 System.out.println("[" + this.getClass().toString() + "]: Failed to enumerate network devices. " + e);
                 continue;
             }
             catch (IOException e) {
                 System.out.println("[" + this.getClass().toString() + "]: Failed to deliver results to client. " + e);
                 continue;
             }
         }
 
         System.out.println("[" + this.getClass().toString() + "] is dying.");
     }
 
     //Reads a string message from a client.
     private String readStringFromConnection(Socket clientSocket) throws IOException {
         String clientMessage = null;
 
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         char[] buffer = new char[3000];
         int numberOfChars = bufferedReader.read(buffer, 0, 3000);
         clientMessage = new String(buffer, 0, numberOfChars);
 
         return clientMessage;
     }
 
     // Parse the received XML-message and extract header and body information
     // Returns a string array of size 2. The first member is the header,
     // The second is the body.
     private String[] parseMessage(String clientMessage) throws NullPointerException, IllegalStateException {
 
         String header = null;
         String body = null;
 
         clientMessage = clientMessage.replace("\n", "<br>");
 
         final Pattern headerpattern = Pattern.compile("<header>(.+?)</header>");
         final Matcher headermatcher = headerpattern.matcher(clientMessage);
         headermatcher.find();
         header = headermatcher.group(1);
 
         final Pattern bodypattern = Pattern.compile("<body>(.+?)</body>");
         final Matcher bodymatcher = bodypattern.matcher(clientMessage);
         bodymatcher.find();
         body = bodymatcher.group(1);
 
         String[] parsedMessage = new String[2];
         parsedMessage[0] = header;
         parsedMessage[1] = body;
 
         return parsedMessage;
     }
 
     private void deliverResults(Socket clientSocket, CommandExecutor grepExecutor) throws SocketException, IOException {
 
         OutputStream clientOutputStream = clientSocket.getOutputStream();
         BufferedReader processOutput = grepExecutor.getProcessReader();
 
         String line;
         while(!grepExecutor.processIsTerminated()) {
            line = processOutput.readLine(); //.replace("\0","");
             if(line != null)
                 clientOutputStream.write((line + "\n").getBytes());
         }
 
         while((line = processOutput.readLine()) != null) {
            clientOutputStream.write((line + "\n").getBytes()); //.replace("\0","")
         }
     }
 
     //Check, if an address is local.
     private boolean isLocalInetAddress(InetAddress addr) throws SocketException {
 
         Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
         while(n.hasMoreElements())
         {
             NetworkInterface e =(NetworkInterface) n.nextElement();
             Enumeration ee = e.getInetAddresses();
             while(ee.hasMoreElements())
             {
                 InetAddress i= (InetAddress) ee.nextElement();
                 if(addr.toString().substring(1).equalsIgnoreCase(i.getHostAddress().toString())) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
