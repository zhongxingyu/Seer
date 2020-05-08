 package distclient;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Arrays;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distfilelisting.FileObject;
 import distnodelisting.NodeSearchTable;
 
 public class ClntUploadFile implements Runnable {
 	private String host;
 	private Client client;
 	private FileObject file;
 	private String username;
 	private String fullpath;
 
 	public ClntUploadFile(String host, Client client, FileObject file, String username){
 		DistConfig distConfig = DistConfig.get_Instance();
 		this.host = host;
 		this.client = client;
 		this.file = file;
 		this.fullpath = distConfig.get_rootPath() + System.getProperty("file.separator") + file.getName();
 		this.username = username;
 	}
 	
 	public ClntUploadFile (Client client, FileObject file, String fullPath, String username) {
 		this.client = client;
 		this.file = file;
 		this.fullpath = fullPath;
 		this.username = username;
 		
 		// Get the first host to check
 		NodeSearchTable nst = NodeSearchTable.get_Instance();
 		this.host = null;
 		for (int index = 0; index < nst.size()-1; index++) {
 			if (NodeSearchTable.is_between(
 					this.file.get_hash(),
 					Integer.parseInt(nst.get_IDAt(index)), 
 					Integer.parseInt(nst.get_IDAt(index + 1)))) {
 				this.host = nst.get_IPAt(index);
 				break;
 			}
 			else {
 				this.host = nst.get_IPAt(index+1);
 			}
 		}
 	}
 	
 	public ClntUploadFile (String host, Client client, FileObject file, String fullPath, String username) {
 		this.client = client;
 		this.file = file;
 		this.fullpath = fullPath;
 		this.username = username;
 		this.host = host;
 	}
 
 
 	@Override
 	public void run() {
 		try {
 	
 	    	DistConfig distConfig = DistConfig.get_Instance();
 
 			System.out.printf("Connecting to %s\n", this.host);
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
 	        
 	        
 	        System.out.println("Sending Code");
 	        outStream.println(ConnectionCodes.UPLOADFILE);
 	        outStream.flush();
 	        
 	        ObjectOutputStream oos = new ObjectOutputStream(bos);
 	        
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 	        System.out.println("Sending filename as " + this.file.getName());
 	        outStream.println(this.file.getName());
 	        outStream.flush();
 	        
 	        String response = in.readLine();
 	        System.out.println("Received " + response);
 	        
 	        if (Integer.parseInt(response) == ConnectionCodes.CORRECTPOSITION) {
	        //if (response.equals(ConnectionCodes.CORRECTPOSITION)) {
 	        	System.out.println("Sending username as " + this.username);
 		        outStream.println(this.username);
 		        outStream.flush();
 		        
 		        oos.writeObject(this.file);
 		        oos.flush();
 		        
 		        response = in.readLine();
 		        System.out.println("Received " + response);
 		        
 		        if (Integer.parseInt(response) == ConnectionCodes.AUTHORIZED) {
		        //if (response.equals(ConnectionCodes.AUTHORIZED)) {
 
 		        	//String fullPathName = distConfig.get_rootPath() + this.file.getName();
 		        	String fullPathName = this.fullpath;
 		        	
 		        	File toTransfer = new File (fullPathName);
 		        	
                     FileInputStream fis = new FileInputStream(toTransfer);
                     byte[] buffer = new byte[distConfig.getBufferSize()];
                     
                     // Send each set of bytes over
                     Integer bytesRead = 0;
                     while ((bytesRead = fis.read(buffer)) > 0) {
                     	oos.writeObject(bytesRead);
                     	oos.writeObject(Arrays.copyOf(buffer, buffer.length));
                     	oos.flush();
                     }
 		        	
 		        	System.out.println("Received " + in.readLine());
 		        	sock.close();
 		        	return;
 			        	
 			    } 
 		        else if (Integer.parseInt(response) == ConnectionCodes.NOTAUTHORIZED) {
		        //else if (response.equals(ConnectionCodes.NOTAUTHORIZED)) {
 			    	sock.close();
 			    	return;
 			    }
 	        	
 	        } 
 	        else if (Integer.parseInt(response) == ConnectionCodes.WRONGPOSITION) {
	        //else if (response.equals(ConnectionCodes.WRONGPOSITION)) {
 	        	int nextId = Integer.parseInt(in.readLine());
 		        System.out.println("Received next ID: " + nextId);
 		        
 		        String nextHost = in.readLine();
 		        System.out.println("Received next Server IP: " + nextHost);
 		        
 		        sock.close();
 		        //client.addTask(new ClntUploadFile(nextHost, this.client, this.file, this.username));
 		        ClntUploadFile cuf = new ClntUploadFile (nextHost, this.client, this.file, this.fullpath, this.username);
 		        cuf.run();
 		        return;
 	        	
 	        }
 	        
 	        if (!sock.isClosed())
 	        	sock.close();
 	        return;
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
