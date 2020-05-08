 import java.net.*;
 import java.io.*;
 
 public class SingleThreadServer {
 
 
  public static void main(String[] args) {
  		Socket connection=null;
  		StringBuilder sb;
  		int port = 11907;
 	  
 	    try{
 	      System.out.println("SingleThreadServer Initialized");
 	      int c;
		  ServerSocket socket = new ServerSocket(port);
 	      while (true) {
	        connection =  socket.accept();
 
 	        InputStreamReader inputStream = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
 	        
 	        sb = new StringBuilder();
 	        while((c = inputStream.read()) != 10) {
 	            if(c==13) continue;
 	            sb.append((char)c);
 	        }
 	        
 	        System.out.println("Got message from a client: "+sb);
 	        
 	        try {
 	          Thread.sleep(5000); //wait 5 seconds
 	        }
 	        catch (Exception e){}
 
 	        
 	        OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()), "UTF-8");
 	        writer.write("SingleThreadServer repsonded at "+ (new java.util.Date().toString()) + (char) 10);
 	        writer.flush();
 	     }
 	    }
 	    catch (IOException e) {}
 	      try {
 	    	if(connection!=null)
 	           connection.close();
 	      }
 	      catch (IOException e) {}
 	  }
 	}
