 import java.io.*;
import java.net.*;
 import javax.net.ssl.*;
 
 public class ServerFile {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		FileOutputStream output = new FileOutputStream("./output.txt");
		
		SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
 		SSLServerSocket welcomeSocket = (SSLServerSocket)factory.createServerSocket(6789);
 //		ServerSocket welcomeSocket = new ServerSocket(6789);
 		
 		while(true) {
			SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
 //			Socket connectionSocket = welcomeSocket.accept();
 			
 			DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
 			
 			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
 			
 			long length = inFromClient.readLong();
 			
 			byte[] buffer = new byte[65536];
 			int read = 0;
 			int offset = 0;
 			
 			while (offset < length) {
 				read = inFromClient.read(buffer, 0, 65536);
 				output.write(buffer, 0, read);
 				
 				offset += read;
 			}
 			
 			outToClient.writeBytes("Read " + offset + " bytes.\n");
 			
 			output.close();
 		}
 	}
 
 }
