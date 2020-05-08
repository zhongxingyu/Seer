 //  usage:
 //    javac Client.java
 //    java Client <host> <APPEND (or READ)>
 
 import java.io.*;
 import java.net.*;
 import java.util.Scanner;
 
 
 
 public class Client {
   public static void main(String[] args) {
       if (args.length != 2) {
         System.out.println("need 2 arguments");
         System.exit(1);
       }
       String host = args[0];
       int port = 3000;
       String command = args[1];
       
       ServerMessage msg = new ServerMessage();
       
       if(command.equals("APPEND")){
     	  msg.setType(ServerMessage.CLIENT_APPEND);
       } else if (command.equals("READ")){
     	  msg.setType(ServerMessage.CLIENT_READ);
       }
       
       
       if (!(command.equals("APPEND") || command.equals("READ"))) {
         System.out.println("command should be 'APPEND' or 'READ'");
         System.exit(1);
       }
       int[] grades = new int[10];
       double[] stats = new double[3];
       
       if(command.equals("APPEND")) {
         System.out.println("Reading in numbers");
         String gradeString = num(grades);
         calc(grades, stats);
         msg.setMessage(gradeString);
       }
       
       
      
       
     try {
       InetAddress address = InetAddress.getByName(host);
       System.out.print("Connecting to Server...");
       
       // open socket, then input and output streams to it
       Socket socket = new Socket(address,port);

       ObjectInputStream from_server = new ObjectInputStream(socket.getInputStream());
       ObjectOutputStream to_server = new ObjectOutputStream(socket.getOutputStream());
       System.out.println("Connected");
       
       // send command to server, then read and print lines until
       // the server closes the connection
       System.out.print("Sending Message to Server...");
       to_server.writeObject(msg); to_server.flush();
       System.out.println("Sent: " + msg);
       ServerMessage line;
       
       while ((line = (ServerMessage) from_server.readObject()) != null) {
         System.out.println(line.getMessage());
       }
     }
     catch (Exception e) {    // report any exceptions
       System.err.println(e);
     }
   }
   public static String num(int g[]) {
 	String retVal = "";
     Scanner input = new Scanner( System.in ); 
     for(int i = 0; i < 10; i++) {
         g[i] = input.nextInt();
         retVal += Integer.toString(g[i]) + " ";
     }
     
     return retVal;
   }
   
   public static String calc(int g[], double s[]) {
 	
     int min = g[0];
     int max = g[0];
     double sum = g[0]; 
     for(int i = 1; i < 10; i++) {
         if(g[i] < min)
             min = g[i];
         if(g[i] > max)
             max = g[i];
         sum += g[i];
     }
     s[0] = min;
     s[1] = max;
     s[2] = sum/10;
     return new String ("" + min + " " + max + " " + s[2]);
   }
 }
