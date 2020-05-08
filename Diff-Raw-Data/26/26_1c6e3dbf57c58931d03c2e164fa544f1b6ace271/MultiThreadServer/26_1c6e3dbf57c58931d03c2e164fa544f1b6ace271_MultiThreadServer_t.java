 import java.net.*;
 import java.io.*;
 
 public class MultiThreadServer implements Runnable {
 
   private Socket connection;
   private int ID;
   
   MultiThreadServer(Socket s, int i) {
 	  this.connection = s;
 	  this.ID = i;
    }
   
   public static void main(String[] args) {
 	  int port = 11907;
 	  int count = 0;
 	  try{
 		  System.out.println("MultipleThreadServer started...");
		  ServerSocket socket = new ServerSocket(port);
 		  while (true) {
	    		Socket connection = socket.accept();
 	    		new Thread(new MultiThreadServer(connection, ++count)).start();	    		
     	  }
      }
      catch (Exception e) {}
   }
 
   public void run() {
 	    try {
 		    InputStreamReader inputStream = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
 		      		        
 	        StringBuilder sb = new StringBuilder();
 	        int c;
 	        while((c = inputStream.read()) != 10) {
 	            if(c==13) continue;
 	            sb.append((char)c);
 	        }
 	        
 	        System.out.println("Got message from a client: "+sb);
 	        
 	        try {
 	          Thread.sleep(5000); //wait 5 seconds
 	        }
 	        catch (Exception e){}
    
 	       OutputStreamWriter writer= new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()), "UTF-8");
 	       writer.write("MultiThreadServer repsonded to Thread: "+this.ID+" at "+ new java.util.Date().toString() + " from connection: "+connection+(char) 10);
 	       writer.flush();
 	    }
 	    catch (Exception e) {}
 	    finally {
 	       try {
 	          connection.close();
 	       }
 	       catch (IOException e){}
 	    }
   }
 }
