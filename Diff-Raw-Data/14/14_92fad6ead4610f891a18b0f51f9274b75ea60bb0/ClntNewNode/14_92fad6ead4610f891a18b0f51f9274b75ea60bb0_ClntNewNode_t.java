 package distclient;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 
 public class ClntNewNode implements Runnable {
 	private String host;
 	private int id;
 	
 	public ClntNewNode(String host){
 		this.host = host;
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
 	        
 	        System.out.println("Sending Code");
 	        outStream.println(ConnectionCodes.NEWNODE);
 	        outStream.flush();
 	        
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 	        id = Integer.parseInt(NodeSearchTable.get_Instance().get_ownID());
 	        System.out.println("Sending my ID as " + id);
 	        outStream.println(Integer.toString(id));
 	        
	        String ip = NodeSearchTable.get_Instance().get_ownIPAddress();
 	        System.out.println("Sending my IP as " + ip);
 	        outStream.println(ip);
 	        outStream.flush();
 	        
 	        System.out.println("Sending my username as" + UserManagement.get_Instance().get_ownUserName());
 	        outStream.println(UserManagement.get_Instance().get_ownUserName());
	        outStream.flush();
 	        
 	        System.out.println("Getting Ack");
 	        System.out.println(in.readLine());
 	        
 		} catch (IOException e) {
 			System.out.println("Inside ClntNewNode");
 			e.printStackTrace();
 		}
 	}
 
 }
