 import java.io.IOException;
 import java.net.*;
 import java.util.*;
 
 public class Server extends Thread {
 	
 	private ServerSocket d_server;
 	
 	private ArrayList<ServerThread> d_threads;
 	
 	private String d_ip;
 	
 	public Server(int port)
 	{
 		d_ip = null;
 		try {
 			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 	        while(interfaces.hasMoreElements())
 	        {
 	            NetworkInterface netInterface = interfaces.nextElement();
 	            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
 	            while(addresses.hasMoreElements())
 	            {
 	                InetAddress address = addresses.nextElement();
 	                
 	                if(address.getHostAddress().contains(":") || address.getHostAddress().startsWith("127"))
 	                	continue;
 	                else 
 	                {
 	                	d_ip = address.getHostAddress();
 	                	break;
 	                }
 	            }
 	            if(d_ip != null)
 	            	break;
 	        }
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		d_threads = new ArrayList<ServerThread>();
 		
 		d_server = null;
 		try {
 			d_server = new ServerSocket(port); 
 			System.out.println("Server up and running!");
 		}
 		catch(Exception e) {
 			System.out.println(e.getMessage() + ", java... :|");
 			System.exit(1);
 		}
 	}
 	
 	public void send(byte[] message) {
 		Iterator<ServerThread> iter = d_threads.iterator();
 		while(iter.hasNext()) {
 			if(iter.next().send(message) == false) {
 				iter.remove();
 				System.out.println("Connection thrown away");
 				
 				// if no more players pause game
 				if(d_threads.size() == 0)
 					SpectroPolaris.setPaused(true);
			}				
 		}
 	}
 	
     @Override
     public void run() {
     	while(true)
     	{
     		if(d_server.isClosed())
     			return;
     		
     		try {
 				Socket socket = d_server.accept();
 				ServerThread thread = new ServerThread(socket);
 				thread.start();
 				
 				d_threads.add(thread);
 				
 				// if game is paused unpause it
 				SpectroPolaris.setPaused(false);
 			} catch (Exception e) {
 				System.out.println(e.getMessage() + ", java... :|");
 			}
     	}
     }
     
     public String ip() {
     	return d_ip;
     }
     
     public int port() {
     	return d_server.getLocalPort();
     }
 
 	public void shutdown() {		
 		try {
 			d_server.close();
 		} catch (Exception e) {
 			System.out.println(e.getMessage() + ", java... :|");
 		}
 	}
 	
 }
