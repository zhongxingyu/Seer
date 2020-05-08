 package javachallenge.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import javachallenge.common.ClientMessage;
 import javachallenge.common.InitMessage;
 import javachallenge.common.ServerMessage;
 
 public class TeamConnection {
 	private Team team;
 	private Socket socket;
 	private ObjectOutputStream out;
 	public Team getTeam() {
 		return team;
 	}
 
 	private ObjectInputStream in;
 	private boolean listening = true;
 	private ClientMessage clientMessage;
 	
 	public TeamConnection(Team team, Socket socket) throws IOException{
 		this.team = team;
 		this.socket = socket;
 		out = new ObjectOutputStream(socket.getOutputStream());
 		in = new ObjectInputStream(socket.getInputStream());
 		
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					while (listening) {
						
 						ClientMessage tmp = (ClientMessage)in.readObject();
 						synchronized (TeamConnection.this) {
 							clientMessage = tmp;
 						}
 					}
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassCastException e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public synchronized ClientMessage getClientMessage() {
 		return clientMessage;
 	}
 	
 	public synchronized void clearClientMessage() {
 		clientMessage = null;
 	}
 
 	public void sendInitialMessage(InitMessage msg) throws IOException {
 		out.writeObject(msg);
 		out.flush();
 	}
 	
 	public void sendStepMessage(ServerMessage msg) throws IOException {
 		out.writeObject(msg);
 		out.flush();
 	}
 }
