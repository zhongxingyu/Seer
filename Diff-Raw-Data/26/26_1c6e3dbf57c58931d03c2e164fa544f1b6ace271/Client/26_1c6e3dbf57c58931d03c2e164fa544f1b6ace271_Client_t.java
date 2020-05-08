 
 import java.net.*;
 import java.util.Scanner;
 import java.io.*;
 
 
 public class Client {
 
 	  public static void main(String[] args) {
 	    String host = "localhost";
 	    int port = 11907;
 	    
 	    StringBuilder sb = new StringBuilder();
 	    
 	    System.out.println("Client started listen...");
 	    try {
 	    	  Socket connection = new Socket(InetAddress.getByName(host), port);	      
 	          OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()), "UTF-8");
 	          Scanner con = new Scanner(System.in);
 	          System.out.println("Waiting message to server: ");   
 	          writer.write("Message: "+con.nextLine());
 	          writer.write(" has been sending to the server on "+ host + " port " + port +" at " + (new java.util.Date().toString()) +  "\r\n");
 	          
 	          writer.flush();
 	          con.close();
 	          
 	          InputStreamReader inputStream = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "UTF-8");
 
 	          int c;
 	          while ( (c = inputStream.read()) != 10)// 10 is unix style newline
	             sb.append( (char) c);          
 	          System.out.println("Response from server: "+sb);
	          connection.close();
 	    }catch (Exception g) {
 	          System.out.println("Exception: " + g);
 	        
 	    }
 	  }
 	  
 }
