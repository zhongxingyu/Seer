 package client;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import utils.Constants;
 import utils.GameAddress;
 import LobbyServer.LobbyServer;
 
 /**
  * Class for handling the connection between clients and server.
  * 
  * @author BigFarmor
  * 
  */
 public class ClientConnectionHandler {
 	private static ClientConnectionHandler myCCH = null;
 	private Socket socket;
 	private ClientInputThread iThread;
 
 	private ClientOutputThread oThread;
 	private int nbrOfPlayers;
 	ClientMonitor cm;
 	LobbyServer ls;
 	private int hubPort;
 	
 	public static ClientConnectionHandler getCCH(InetAddress hubHost, int hubPort){
 		if(myCCH==null){
 			myCCH = new ClientConnectionHandler(hubHost, hubPort);
 		}
 		return myCCH;
 	}
 	private ClientConnectionHandler(InetAddress hubHost, int hubPort) {
 		try {
 			socket = new Socket(hubHost, hubPort);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		this.hubPort = hubPort;
 		cm = new ClientMonitor();
 		iThread = new ClientInputThread(socket,cm);
 		iThread.start();
 		oThread =  new ClientOutputThread(socket,cm);
 		oThread.start();
 	}
 
 	/**
 	 * Sends a message via the protocol SENDMESSAGE,N(LENGTH OF MESSAGE),MESSAGE
 	 * 
 	 * @param message
 	 */
 
 
 	public void sendMessage(String message) {
 		cm.addMessage(message);
 	}
 
 
 	/**
 	 * Retrieves the messages from the client.InputThread
 	 * 
 	 * @return
 	 */
 	public ArrayList<String> getMessage() {
 		ArrayList<String> list = cm.getRecievedChatMessages();
 		return list;
 	}
 
 	/**Sends movement from client to server using UDP
 	 * @param acc
 	 */
 	public void sendMovement(int[] acc) {
 		byte[] command = {getCommandFromMovementKey(acc)};
 		//System.out.println("SendMovements: " + misc.printByte(command));
 		cm.addMovementToSend(command);
 	}
 
 	/**Translates key pressings to 1-byte commands
 	 * @param acc
 	 * @return
 	 */
 	private byte getCommandFromMovementKey(int[] acc) {
 		if(acc[0]==1){
 			if(acc[2]==1){
 				return Constants.NORTHWEST;
 			}
 			else if(acc[3]==1){
 				return Constants.NORTHEAST;
 			}else{
 				return Constants.NORTH;
 			}
 		}
 		if(acc[1]==1){
 			if(acc[2]==1){
 				return Constants.SOUTHWEST;
 			}
 			else if(acc[3]==1){
 				return Constants.SOUTHEAST;
 			}else{
 				return Constants.SOUTH;
 			}
 		}
 		if(acc[2]==1){
 			return Constants.WEST;
 		}
 		if(acc[3]==1){
 			return Constants.EAST;
 		}
 		return -1;
 	}
 	
 	//TODO
 	public byte[] receiveInit() {
 		return new byte[1];
 	}
 
 	public byte[] receiveMovements() {
 		try {
 			nbrOfPlayers = cm.getNbrOfPlayers();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		byte[] buff = new byte[1*nbrOfPlayers + 8];
 		buff = cm.recieveMovements(buff);
 		return buff;
 	}
 	/**
 	 * creates a new lobby and binds its own socket an output/input to this socket (which in turn is bound to the lobby)
 	 * @param gameName
 	 * @param port
 	 */
 	public void createNewLobby(String gameName, int port) {
 		//System.out.println("Going to create new lobby");
 		if(port != -1){
 			try {
 				ls = new LobbyServer(gameName, port, socket.getInetAddress(), hubPort); //send hubInetAddress aswell to enable lobby to "deregister" himself after game is created
 				ls.start();
 				oThread.startNewGame(gameName, port);
 				socket.close();
 				socket = new Socket(InetAddress.getLocalHost(),port);
 				iThread = new ClientInputThread(socket,cm);
 				iThread.start();
 				oThread = new ClientOutputThread(socket,cm);
 				oThread.start();
 				System.out.println("New lobby created @: " + gameName + ":" + port);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	public ArrayList<GameAddress> getGames() {
 		ArrayList<GameAddress> list = cm.getGames();
 		System.out.println("GamesSizeInnCCh: " + list.size());
 		return list;
 	}
 
 	public void refreshGames() {
 		oThread.refresh();
 
 	}
 	public String getGameName() {
 		//System.out.println("CCH:GETGAMENAME");
 		if(ls!=null){			
 			String re = ls.getGameName();
 			return re;
 		}else{
 			return cm.getGameName();
 		}
 	}
 	
 	public void updateGameName(){
 		oThread.getGameName();
 	}
 	
 	public ArrayList<String> getNames() {	
 		return cm.getNames();
 	}
 
 	public boolean joinLobby(String host, int port) {
 		InetAddress ia = null;
 		try {
 			ia = InetAddress.getByName(host);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 			try {
				
				Socket newSocket = new Socket(ia, port);				
 				iThread = new ClientInputThread(socket,cm);
 				iThread.start();
 				oThread = new ClientOutputThread(socket,cm);
 				oThread.start();
 				updateGameName();
				socket.close();
				socket = newSocket;
 				
 			} catch (IOException e) {
 				System.out.println("IOEXCEPTIONS");
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 	}
 	
 	public void closeConnection() {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setReady() {
 		cm.setReady(true);
 		
 	}
 	
 	public long getSeed() {
 		long seed = -1;
 		try {
 			seed =  cm.getSeed();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return seed;
 	}
 	public int getIndex() {
 		int mindex = -1;
 		try {
 			mindex =  cm.getMyIndex();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return mindex;
 	}
 	public int getNbrOfPlayers() {
 		int nplayers = -1;
 		try {
 			nplayers =  cm.getNbrOfPlayers();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return nplayers;
 	}
 }
