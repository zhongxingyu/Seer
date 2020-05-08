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
     private Config conf;
 
     public ConnectionHandler(LinkedBlockingQueue<Socket> connectionQueue, Config conf) {
         this.connectionQueue = connectionQueue;
         this.conf = conf;
         this.searchPath = conf.valueFor("searchPath");
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
                 //clientSocket.close();
             }
             catch (IOException e) {
                 System.err.println("Failed to get message from client. " + e);
                 continue;
             }
 
             System.out.println("[" + this.getClass().toString() + "]: Got message from: " + clientAddress);
 
             String[] parsedMessage;
             try {
                 parsedMessage = parseMessage(clientMessage);
             }
             catch (IllegalStateException e) {
                 continue;
             }
             String header = parsedMessage[0];
             String body = parsedMessage[1];
 
 
             //If a request was sent to this machine, it will execute a grep and sends the results back to the initiator
             // if(header.equalsIgnoreCase("searchrequest")) {
 
             System.out.println("[" + this.getClass().toString() + "]: Running search for: " + clientAddress);
 
             CommandExecutor grepExecutor = null;
             try {
                 grepExecutor = Search.runSearch(searchPath, body);
             }
             catch (IOException e) {
                 System.err.println("Failed to generate search results. " + e);
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
                 System.err.println("[" + this.getClass().toString() + "]: Failed to enumerate network devices. " + e);
                 continue;
             }
             catch (IOException e) {
                 System.err.println("[" + this.getClass().toString() + "]: Failed to deliver results to client. " + e);
                 continue;
             }
 
             /*
             } else if(header.equalsIgnoreCase("searchresult")) {
                 body = body.replace("<br>", "\n");
                 System.out.println("Search results from " + clientAddress + ":\n----------\n" + body);
             }
             */
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
             line = processOutput.readLine();
             if(line != null)
                 clientOutputStream.write((line + "\n").getBytes());
         }
 
         while((line = processOutput.readLine()) != null) {
             clientOutputStream.write((line + "\n").getBytes());
         }
 
         /*
 		while ((line = b.readLine()) != null) {
 		    text += line + "\n";
             System.out.println(text.length());
             System.out.println(p.exitValue());
             p.
 		}
 
         clientSocket.getOutputStream().write(searchResults.getBytes());
         /*
         if(isLocalInetAddress(clientSocket)) {
             System.out.println("Search results from localhost:\n"+searchResults+"---\n");
         } else {
 
             Connection backcon = new Connection(conf);
             String[] receiver = new String[1];
             receiver[0] = clientSocket.toString().substring(1);
             backcon.sendMessage(searchResults, "searchresult", receiver);
 
         }
         */
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
