 package network;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Server{
 
 	private ExecutorService threads;
 	private ArrayList<Socket> sockets = new ArrayList<Socket>();
 	private ArrayList<ConnectInform> cast = new ArrayList<ConnectInform>();
 	private NetCallable pushTo;
 	private int roomSize = 0;
 	public boolean exitFlag = false;
 	
 	public Server (NetCallable pushTo, int roomSize) {
 		this.pushTo = pushTo;
 		this.roomSize = roomSize;
 		threads = Executors.newFixedThreadPool(roomSize-1);
 		run ();
 	}
 	public void run() {
 		try {
 			ServerSocket ss = new ServerSocket(10001);
 
 			System.out.println("Waiting for Connection...");
 
 			for (int i = 0; i < roomSize-1; i++) {
 				Socket so = ss.accept();
 				System.out.println((i+1)+"th connect from " + so.getInetAddress());
 				addSocket(so);
 				addThread(so, i+1);
 			}
 			
 			System.out.println("Connection all complete");			
 		} catch (Exception e) {
 			System.out.println(e);
 		} finally {
 			exit();
 		}
 	}
 
 	private void addSocket(Socket client) {
 		sockets.add(client);
 		cast.add(new ConnectInform(client));
 	}
 
 	private void addThread(Socket client, int id) {
 		try {
 			ServerThread st = new ServerThread(client, this, pushTo, id);
 			threads.execute(st);
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 
 	void broadcast(Socket from, Serializable msg, int id) {
 //		System.out.println("broadcast(from client) called");
 		pushTo.pushMessage(msg, id);
 		try {
 			for (ConnectInform cell : cast) {
 				if (cell.getSocket() == from) {
 					continue;
 				}
 				cell.getOut().writeObject(msg);
 			}
 		} catch (IOException e) {
 			System.out.println("exception on Server Broadcast method : "+e);
 		}
 	}
 	
 	void broadcast(Object msg) {
 //		System.out.println("echo on server "+ (String)msg);
 		try {
 			for (ConnectInform cell : cast) {
 				cell.getOut().writeObject(msg);
 			}
 		} catch (IOException e) {
 			System.out.println("exception on Server Broadcast method : "+e);
 		}
 	}
 	void bye (Socket closed) {
 		pushTo.disconnect(sockets.indexOf(closed)+1);
 		sockets.remove(closed);
 	}
 	public void exit() {
 		threads.shutdown();
 		exitFlag = true;
 	}
 	public void send (Serializable message) {
 		broadcast(message);
 	}
 	public void send (Serializable message, int... target) {
 		try {
 			for(int cell : target){
 				cast.get(cell-1).getOut().writeObject(message);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void send (Serializable message, int exclude) {
 		try {
 			for(int cell = 0; cell < cast.size(); cell++){
 				if(cell == exclude-1) continue;
				cast.get(cell).getOut().writeObject(message);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void sendExcept (Serializable message, int exclude) {
 		send(message, exclude);
 	}
 }
