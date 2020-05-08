 package dk.au.cs.EagleEye2Server;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class PositionServer {
   public static void main(String[] args) throws IOException {
     String location;
     String capitalizedSentence;
     ServerSocket welcomeSocket = new ServerSocket(57005);
 
     while(true) {
       Socket connectionSocket = welcomeSocket.accept();
       BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
       DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
       location = inFromClient.readLine();
 
       saveToFile(location, "locations");
 
       // For testing purposes, perhaps this configurable
      System.out.println("Received: " + location);
       capitalizedSentence = location.toUpperCase() + '\n';
       outToClient.writeBytes(capitalizedSentence);
     }
   }
 
   private static void saveToFile(String location, String fileName) {
     FileWriter fileWriter;
     BufferedWriter bufferedWriter;
     File locationsFile = new File("data/" + fileName + ".out");
 
     try {
       locationsFile.createNewFile();
       fileWriter = new FileWriter(locationsFile, true);
       bufferedWriter = new BufferedWriter(fileWriter);
 
       bufferedWriter.write(location);
       bufferedWriter.newLine();
       bufferedWriter.flush();
 
       bufferedWriter.close();
       fileWriter.close();
     } catch (IOException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
   }
 }
