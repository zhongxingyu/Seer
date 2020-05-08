 
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Enumeration;
 
 import Communications.*;
 import Server.ServerThread;
 import Utilities.InputReader;
 
 public class ServerMainLoop {
 	static TCP tcp=null;
 	static boolean error=false;
 	static boolean skip=false;
 	static boolean done=false;
 	static InputReader ir = new InputReader();
 	public static InetAddress ip=null;
 	static ServerSocket serverSocket=null;
 	static Socket socket=null;
 	static Thread t=null;
 	
 	
 	public static void main(String[] args){
 		try {
 			InetAddress address = null;
 			Enumeration<NetworkInterface> interfaces = NetworkInterface
 					.getNetworkInterfaces();
 			while (interfaces.hasMoreElements()) {
 				NetworkInterface networkInterface = interfaces.nextElement();
 				if (networkInterface.isLoopback())
 					continue; // Don't want to broadcast to the loopback
 								// interface
 				Enumeration<InetAddress> addresses = networkInterface
 						.getInetAddresses();
 				while (addresses.hasMoreElements()) {
 					address = addresses.nextElement();
 					if (!address.getHostAddress().contains("192.168.224"))
 						continue;
 					break;
 				}
 				if (address != null && address.toString().contains("192.168.224")) {
 					break;
 				}
 			}
 			ip=address;
 		}catch(Exception e){
 			System.out.println("Unable to launch server");
 			error=true;
 		}
 		if(!error){
 			try{
 				serverSocket=new ServerSocket(12345, 0, ip);
 				serverSocket.setSoTimeout(500);
 			}
 			catch(Exception e){}
 		}
 		
 		Thread irThread=new Thread(ir);
 		irThread.start();
 		while(!done && !error){
 			String input=ir.getSubmitted();
 			if(input.startsWith(":quit")){
 				done=true;
 				System.out.println("Quitting");
 				continue;
 			}
 			try {
 				socket = serverSocket.accept();
 				skip = false;
 			} catch (Exception e) {
 				skip = true;
 			}
			if (!error) {
 				ServerThread s=new ServerThread();
 				tcp=new TCP();
 				tcp.initiator = false;
 				tcp.socket = socket;
 				socket=null;
 				tcp.active = true;
 				tcp.tr.setSocket(socket);
 				t = new Thread(tcp.tr);
 				t.start();
 				s.setTCP(tcp);
 				new Thread(s).start();
 			}
 			try {
 				Thread.sleep(10);
 			} catch (Exception e) {}
 		}
 		System.out.println("Hit enter to finish quitting");
 		tcp.stop();
 		ir.stop();
 		System.out.println("If quitting does not occur please make sure there are no connections to the server");
 	}
 }
