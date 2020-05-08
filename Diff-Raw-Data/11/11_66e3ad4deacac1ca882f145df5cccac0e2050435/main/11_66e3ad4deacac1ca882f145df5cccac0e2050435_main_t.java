 /**
  * 
  */
 package AP2DX.coordinator;
 
import java.net.*;
import java.io.*;

 import AP2DX.AP2DXBase;
 
 
 /**
  * @author jjwt
  *
  */
 public class main extends AP2DXBase {
 	
 
 	/**
 	 * entrypoint of coordinator 
 	 */
 	public static void main (String[] args){
 		AP2DXBase instance = new main();
 		
 		System.exit(0);
 	}
 	
 	/**
 	 * constructor
 	 */
 	public main () {
 		System.out.println(" Running... ");
 		/* Connection data. */
 		String addr = "127.0.0.1";
 		int port = 3000;
 		
 		/* Objects to start the connection. */
 		InetAddress address = null;
 		Socket connect = null;
 	
 		/* Input and output stream. */
 	    PrintWriter out = null;
 	    BufferedReader in = null;
 		
         try {
     		address = InetAddress.getByName(addr);
     		connect = new Socket(address, port);
 
     		
             out = new PrintWriter(connect.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
         } catch (Exception e) {
         	e.getMessage();
             System.exit(1);
         }
         
         /* Sending and receiving a message. */
 	    try {
 	        String bericht = "INIT {ClassName USARBot.P2DX} {Location 4.5,1.9,1.8} {Name R1}\r\n";
 		    out.print(bericht);
 			System.out.println("Ontvangen bericht: " + in.readLine());
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 		        /* Closing input, output stream and socket. */
 	        	out.close();
 	        	in.close();
 	        	connect.close();
 	        } catch (Exception e) {
 	        	e.getMessage();
 	        	System.exit(1);
 	        }
         	System.out.println(" Connection closed. Exiting... ");
 		}
 	}
 }
