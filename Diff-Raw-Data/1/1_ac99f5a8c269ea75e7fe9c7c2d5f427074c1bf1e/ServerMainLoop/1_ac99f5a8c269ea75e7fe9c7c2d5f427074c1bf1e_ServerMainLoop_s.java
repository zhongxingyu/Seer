 
 
 import Communications.*;
 import Server.ServerThread;
 import Utilities.InputReader;
 
 public class ServerMainLoop {
 	static TCP tcp=null;
 	static boolean error=false;
 	static boolean done=false;
 	static InputReader ir = new InputReader();
 	
 	public static void main(String[] args){
 		tcp=new TCP();
 		Thread tcpListen=new Thread(tcp);
 		tcpListen.start();
 		Thread irThread=new Thread(ir);
 		irThread.start();
 		while(!done && !error){
 			String input=ir.getSubmitted();
 			if(input.startsWith(":quit")){
 				done=true;
 				System.out.println("Quitting");
 				continue;
 			}
 			if(tcp.getActive()){
 				ServerThread st=new ServerThread();
 				st.setTCP(tcp);
 				(new Thread(st)).start();
 				tcp=new TCP();
 			}
 		}
 		System.out.println("Hit enter to finish quitting");
 		tcp.stop();
 		ir.stop();
 		System.out.println("If quitting does not occur please make sure there are no connections to the server");
 	}
 }
