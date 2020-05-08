 package io;
 import gnu.io.SerialPortEvent;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import clientcommands.Commands;
 import clientcommands.Commands.Command;
 
 import com.google.protobuf.*;
 
 /**
  * This class handles a single Client connection
  * @author danielwilbers
  *
  */
 class Connection implements Runnable{
 
 	Socket socket;
 	
 	public Connection(Socket socket){
 		this.socket = socket;
 	}
 	
 	@Override
 	public void run() {
		BufferedReader  fromClient;
 		
 		try {
 	
 			while(socket.isConnected()){
				if(socket.getInputStream().available()>0){
					Command in = Command.parseFrom(socket.getInputStream()); 
 					System.out.println(in.getAction() + " " + in.getModule());
 				}
 			}
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		};
 	
 		
 	}
 	
 }
 
 /**
  * 
  * @author danielwilbers
  *
  */
 public class ClientInput implements Runnable{
 
 	private int maximumConnections = 3;
 	private int currentConnections = 0;
 	private ServerSocket server;
 	
 	
 	public  ClientInput(){
 		try {
 			server = new ServerSocket(1811);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void run() {
 		
 		//Command c = Command.newBuilder().setModule(Command.Module.FADE).setAction(Command.Action.START).build();
 		//System.out.println(c.toByteArray());
 		
 		while(true){
 			if(currentConnections <= maximumConnections){
 				try {
 					Socket socket = server.accept();
 					
 					Connection singleCon = new Connection(socket);
 					Thread t = new Thread(singleCon);
 					t.start();
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		}
 		
 		
 	}
 
 	
 	
 }
