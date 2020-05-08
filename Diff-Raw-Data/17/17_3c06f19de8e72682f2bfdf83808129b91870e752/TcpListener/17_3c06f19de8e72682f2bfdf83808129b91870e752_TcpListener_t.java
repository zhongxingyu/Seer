 //This class listens for incoming client connections for the server
 
 import java.io.*;
 import java.net.*;
 
 public class TcpListener implements Runnable{
 	
 	ServerSocket servSock;
 	
 	Socket[] clients;
 	ObjectOutputStream[] streams;
 	ServerDataListener dataHandler;
 	
 	boolean running = true;
 
 	public TcpListener(ServerDataListener dataListen) {
 		clients = new Socket[10];
 		streams = new ObjectOutputStream[10];
 		dataHandler = dataListen;
 		
 		Thread t = new Thread(this, "tcp listener");
 		
 		try{
 			servSock = new ServerSocket(13337);
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		t.start();
 	}
 
 	@Override
 	public void run() {
 		
 		Socket sock;
 		
 		while(running){//tcp listener main loop
 			try {
 				sock = servSock.accept();
 				System.out.println("client has connected");
 				
 				addClientToList(sock);
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		for(int i=0;i<clients.length;i++){//close all connections
 			try {
 				clients[i].close();
 			} catch(Exception e){}
 		}
 		
 		try { //close the server
 			servSock.close();
 		}catch(Exception e){}
 	}
 	
 	void addClientToList(Socket sock) {
 		for(int i=0;i<clients.length;i++){
 			if(clients[i] == null){
 				clients[i] = sock;
 				
 				try {
 					//set up buffer size
 					sock.setSendBufferSize(512);
 					
 					streams[i] = new ObjectOutputStream(sock.getOutputStream());
 					streams[i].writeInt(i);
 
 					dataHandler.streams[i] = new ObjectInputStream(sock.getInputStream());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
				try{
					sock.close();
				} catch (Exception e){
					e.printStackTrace();
				}
				
 				return;
 			}
 		}
		
		System.out.println("connection refused; max clients reached");
 	}
 }
