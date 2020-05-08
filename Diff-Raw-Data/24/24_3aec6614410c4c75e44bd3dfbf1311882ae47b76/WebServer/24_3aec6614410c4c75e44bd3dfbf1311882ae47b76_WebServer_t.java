 /*My Web Server 
  *Bhojan Anand
  */
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 public class WebServer {
 	
     public static void main (String args[]) throws Exception 
 	{
 		// throws Exception here because don't want to deal
 		// with errors in the rest of the code for simplicity.
 		
 		// Create a new TCP WELCOME SOCKET that waits for connection at port
 		// number 7000.
 		ServerSocket serverSock = new ServerSocket(7000);
 		System.out.println("SERVER IS WAITING FOR HTTP REQUEST at PORT 7000...");
 		
 		while (true) 
 		{
 			//Listen & Accept Connection and Create new CONNECTION SOCKET
 			Socket s = serverSock.accept();
			//System.out.println("connection established from " + s.getInetAddress());
			//System.out.println("Connection Definition " + s.toString());
 
 			
 			// The next 3 lines create a buffer reader that
 			// reads from the socket s.
 			InputStream is = s.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 
 			// The next 2 lines create a output stream we can
 			// write to.
 			OutputStream os = s.getOutputStream();
 			DataOutputStream dos = new DataOutputStream(os);
 			
 			// Read HTTP request (empty line signal end of request)
 			String input = br.readLine();
 			String filename = "";
			String parameters = "";
			if(input == null || input.length() <= 0)
				continue;
 			StringTokenizer st = new StringTokenizer(input);
 		
 			if (st.nextToken().equals("GET"))
 			{
 				// This is a GET request.  Parse filename.
 				filename = st.nextToken();
				System.out.println("Got request - " + filename);
 				if (filename.startsWith("/")) 
 				{
 					filename = filename.substring(1);
				
					if(filename.indexOf("?") != -1)
					{
						parameters = filename.substring(filename.indexOf("?"));
						filename = filename.substring(0, filename.indexOf("?"));
					}
					System.out.println("Filename is - " + filename);
					System.out.println("Request is - " + parameters);
				}
 			    filename = "root/" + filename; //my web folder
 			}
 			// read and throw away the rest of the HTTP request
 			while (input.compareTo("") != 0) 
 			{
 				input = br.readLine();  //Just read and ignore
 			}
 			
 			try{
 			
 			// Open and read the file into buffer
 			File f = new File(filename);
 		      
 			if (f.canRead())
 			{
 				int size = (int)f.length();
 				
 				//Create a File InputStrem to read the File
 				FileInputStream fis = new FileInputStream(filename);
 				byte[] buffer = new byte[size];
 				fis.read(buffer);
 			
 				// Now, write buffer to client
 				// (but, send HTTP response header first)
 				
 				dos.writeBytes("HTTP/1.0 200 Okie \r\n");
 				dos.writeBytes("Content-type: text/html\r\n");
 				dos.writeBytes("\r\n");
 				dos.write(buffer,  0, size);
 			}
 			else
 			{
 				// File cannot be read.  Reply with 404 error.
 				dos.writeBytes("HTTP/1.0 404 Not Found\r\n");
 				dos.writeBytes("\r\n");
 				dos.writeBytes("Cannot find " + filename + " leh");
 			}
 			}
 			catch (Exception ex){
				System.out.println("Connection over");
 			}
 
 			// Close connection (using HTTP 1.0 which is non-persistent).
 			s.close();
 		}
 	}
 }
