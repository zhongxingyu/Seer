 package distclient;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distfilelisting.FileObject;
 
 public class ClntGetFile implements Runnable {
 	private String host;
 	private Client client;
 	private FileObject file;
 	private boolean backup;
 	private String username;
 
 	public ClntGetFile(String host, Client client, FileObject file, String username){
 		this.host = host;
 		this.client = client;
 		this.file = file;
 		this.backup = false;
 		this.username = username;
 	}
 	
 	private ClntGetFile(String host, Client client, FileObject file, String username, boolean backup) {
 		this.host = host;
 		this.client = client;
 		this.file = file;
 		this.backup = backup;
 		this.username = username;
 	}
 
 	@Override
 	public void run() {
 		try {
 	
 	    	DistConfig distConfig = DistConfig.get_Instance();
 	    	
 			System.out.println("Connecting");
 			Socket sock = new Socket(host, distConfig.get_servPortNumber());
 	
 	        sock.setSoTimeout(5000);
 	        System.out.println("Connected");
 	        
 	        BufferedOutputStream bos = new BufferedOutputStream (
 	                                sock.getOutputStream());
 	        System.out.println("Got OutputStream");
 	        PrintWriter outStream = new PrintWriter(bos, false);
 	        System.out.println("Got PrintWriter");
 	
 	        BufferedReader in = new BufferedReader (
 	                    new InputStreamReader (
 	                            sock.getInputStream()));
 	        System.out.println("Got InputStream");
 	        
             // Create object input stream
             ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
 	        
 	
 	        
 	        System.out.println("Sending Code");
 	        outStream.println(ConnectionCodes.GETFILE);
 	        outStream.flush();
 	        
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 	        System.out.println("Sending filename as " + this.file.getName());
 	        outStream.println(this.file.getName());
 	        outStream.flush();
 	        
 	        String back = this.backup ? "1" : "0";
 	        System.out.println("Sending backup as " + back);
 	        outStream.println(back);
 	        outStream.flush();
 	        
 	        String response = in.readLine();
 	        System.out.println("Received " + response);
 	        
	        if (response.equals(ConnectionCodes.CORRECTPOSITION)) {
 	        	response = in.readLine();
 		        System.out.println("Received " + response);
 		        
		        if (response.equals(ConnectionCodes.FILEEXISTS)) {
 		        	System.out.println("Sending username as " + username);
 			        outStream.println(username);
 			        outStream.flush();
 			        
 			        response = in.readLine();
 			        System.out.println("Received " + response);
 			        
			        if (response.equals(ConnectionCodes.AUTHORIZED)) {
 			        	FileOutputStream fos = new FileOutputStream (distConfig.get_rootPath() + file.getName());
 	            		
 	            		// Upload the file
 			        	int bytesRead = 0;
 	            		byte [] buffer = new byte[distConfig.getBufferSize()];
 	            		do {
 	            			bytesRead = (Integer)ois.readObject();
 	            			buffer = (byte[])ois.readObject();
 	            			fos.write(buffer, 0, bytesRead);
 	            		} while (bytesRead == distConfig.getBufferSize());
 	            		
 	            		fos.close();
 			        	
 			        	System.out.println("Sending confirm");
 			        	outStream.println(ConnectionCodes.GETFILE);
 			        	
			        } else if (response.equals(ConnectionCodes.NOTAUTHORIZED)) {
 			        	return;
 			        }
 		        	
		        } else if (response.equals(ConnectionCodes.FILEDOESNTEXIST)) {
 		        	response = in.readLine();
 			        System.out.println("Received backup IP: " + response);
 			        
 			        if (this.backup){
 			        	response = in.readLine();
 				        System.out.println("Received backup IP: " + response);
 				        
 				        sock.close();
 				        client.addTask(new ClntGetFile(response, this.client, this.file, this.username, true));
 			        	
 			        } else {
 			        	response = in.readLine();
 				        System.out.println("Received ack: " + response);
 			        	
 			        }
 			        
 			        if (!sock.isClosed())
 			        	sock.close();
 
 		        	return;
 		        }
 	        	
	        } else if (response.equals(ConnectionCodes.WRONGPOSITION)) {
 		        String nextHost = in.readLine();
 		        System.out.println("Received next Server IP: " + nextHost);
 		        
 		        sock.close();
 		        client.addTask(new ClntGetFile(nextHost, this.client, this.file, this.username));
 		        return;
 	        	
 	        }
 	        
 	        if (!sock.isClosed()) 
 	        	sock.close();
 	        return;
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
