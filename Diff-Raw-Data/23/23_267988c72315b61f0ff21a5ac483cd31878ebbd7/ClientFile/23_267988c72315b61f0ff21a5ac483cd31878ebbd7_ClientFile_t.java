 import java.io.*;
import java.net.*;
 import javax.net.ssl.*;
 
 public class ClientFile {
 	
 	public static BufferedInputStream readFile(String path) throws Exception {
 		
 		return new BufferedInputStream(new FileInputStream(new File(path)));
 	}
 
 	/**
 	 * @param args
 	 */
	public static void main(String[] args) throws Exception {
 		File file = new File("../cirrus/Client.java");
 		long length = file.length();
 		
 		BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(file));
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
 		SSLSocket clientSocket = (SSLSocket)factory.createSocket("localhost", 6789);
 //			Socket clientSocket = new Socket("192.168.1.138", 6789);
 		
 		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
 		
 		outToServer.writeLong(length);
 		
 		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 		
 		byte[] buffer = new byte[65536];
 		int offset = 0;
 		int read = 0;
 		
 		while(offset < length) {
 			read = inFile.read(buffer, 0, 65536);
 			outToServer.write(buffer, 0, read);
 			
 			offset += read;
 		}
 		
 		String transmission = inFromServer.readLine();
 		
 		System.out.println("FROM SERVER: " + transmission);
 		
 		clientSocket.close();
 	}
 
 }
