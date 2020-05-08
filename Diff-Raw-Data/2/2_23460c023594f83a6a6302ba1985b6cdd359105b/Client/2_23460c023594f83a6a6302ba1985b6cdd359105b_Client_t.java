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
       String grades_host = args[0];
      String stats_host = Server.STAT_SERVERS.get(0);
       int port = 3000;
       String command = args[1];
       
       ServerMessage grade_msg = new ServerMessage();
       ServerMessage stat_msg = new ServerMessage();
       ServerMessage prepare = new ServerMessage();
       prepare.setType(ServerMessage.CLIENT_GET_LEADER);
       
       if(command.equals("APPEND")){
     	  grade_msg.setType(ServerMessage.CLIENT_APPEND);
     	  stat_msg.setType(ServerMessage.CLIENT_APPEND);
       } else if (command.equals("READ")){
     	  grade_msg.setType(ServerMessage.CLIENT_READ);
     	  stat_msg.setType(ServerMessage.CLIENT_READ);
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
         String statsString = calc(grades, stats);
         grade_msg.setMessage(gradeString);
         stat_msg.setMessage(statsString);
       }
       
     //initiate contact with a grade server and get back the grade paxos leader
       ServerMessage line = sendMessage(grades_host,port,prepare);
       if(line != null) {
           grades_host = line.getMessage();
           System.out.println("GRADES PAXOS LEADER IS: " + grades_host);     
         }
       
       //initiate contact with the stat server and get back the stat paxos leader
       line = sendMessage(stats_host,port,prepare);
       if(line != null) {
           stats_host = line.getMessage();
           System.out.println("STATS PAXOS LEADER IS: " + stats_host);     
         }
       
       //send the grades and stats to their appropriate paxos leaders
       sendMessage(grades_host,port,grade_msg);
       sendMessage(stats_host,port,stat_msg);
       
        //listen on port 3003 for results of read or append.
     try {
         ServerSocket socket = new ServerSocket(3003);
         for(int i = 0; i < 2; i++) {
                 
             Socket connected_socket;
             connected_socket = socket.accept();
             ObjectInputStream from_server = new ObjectInputStream(connected_socket.getInputStream());
 
             line = (ServerMessage) from_server.readObject();
             if(line != null) {
                 System.out.print("Message: " + line.getMessage() + " Type: " + line.getTypeName() + " from Server");     
             }
 
             connected_socket.close();                   
         }
     }
     catch (Exception e) {    // report any exceptions
       System.err.println(e);
     }
 
   }
   
   public static ServerMessage sendMessage(String host_addr, int port, ServerMessage myMsg) {
 	  try {
 	      InetAddress address = InetAddress.getByName(host_addr);
 	      System.out.print("Connecting to " + host_addr + "...");
 	      
 	      // open socket, then input and output streams to it
 	      Socket socket = new Socket(address, port);
 
 	      ObjectInputStream from_server = new ObjectInputStream(socket.getInputStream());
 	      ObjectOutputStream to_server = new ObjectOutputStream(socket.getOutputStream());
 	      System.out.println("Connected");
 	      
 	      // send command to server, then read and print lines until
 	      // the server closes the connection
 	      myMsg.setSourceAddress("CLIENT");
 	      System.out.print("SENDING " + myMsg + " to Server:" + host_addr + "...");
 	      to_server.writeObject(myMsg); to_server.flush();
 	      System.out.println("SENT");
 	      
 	      ServerMessage retVal = (ServerMessage) from_server.readObject();
 	      socket.close();
 	      return retVal;
 	     
 	      
 	    }
 	    catch (Exception e) {    // report any exceptions
 	      System.err.println(e);
 	      return null;
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
