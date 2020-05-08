 package seg.project.checkers;
 
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 import ocsf.server.AbstractServer;
 import ocsf.server.ConnectionToClient;
 /**
  *	 This is the server the checkers game will use 
  *
  */
 public class CheckersServer extends AbstractServer {
 	public static final int DEFAULT_PORT = 24563; // Because it is final, there is no reason not to make it public
 	private boolean gameMode;
 	/**
 	 * The constructor for creating a server and it will automatically start listening
 	 * @param port the port to host on
 	 * @throws IOException If the start up failed, this will be thrown
 	 */
 	public CheckersServer(int port) throws IOException {
 		super(port);
 		this.listen();
 		gameMode = false;
 	}
 	/**
 	 * Default constructor, this is just CheckersServer(DEFAULT_PORT)
 	 * @throws IOException  If the start up failed, this will be thrown
 	 */
 	public CheckersServer() throws IOException{
 		super(DEFAULT_PORT);
 		this.listen();
 	}
 	
 	@Override
 	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
 		// TODO Auto-generated method stub
 		if(msg instanceof String)
 			CheckerGame.getInstance().handleCommand((String)msg);
 		
 	}
     protected void clientConnected(ConnectionToClient client) { // When a client connects it will notify the listeners who connected
    	if(!isGameMode()) // if it is in game mode I don't want to notify
    	{
     		CheckerGame.getInstance().notifyObservers(client);
    	}
 	}
   
     synchronized protected void clientException(
     	    ConnectionToClient client, Throwable exception) {
     	if(gameMode) // if it is still waiting to accept a player I do not need to end the program
     	{
     		JOptionPane.showMessageDialog(null, "Connection to other player lost, exiting");
 			System.exit(0);
     	}
     }
     /**
      * If the server has stopped listening and is in a game it will return true
      * @return If a game has started
      */
 	public boolean isGameMode() {
 		return gameMode;
 	}
 	/**
 	 * One time function to set the server to game mode, it drops all the connections except for the supplied client 
 	 * @param client The client to keep
 	 * @return If it was successfully changed
 	 */
 	public boolean setToGameMode(String client) {
 		if(gameMode)
 			return false;
 		Thread [] connections = this.getClientConnections();
 		int index = -1;
 		// loop for finding the client we want to keep
 		for(int i = 0; i < connections.length; i++){
 			if(client.equals(((ConnectionToClient)connections[i]).getInetAddress().toString())){
 				index =i;
 			}
 		}
 		if(index == -1){ // it was not found
 			return false;
 		}
 		// Close all the other clients
 		for(int i = 0; i < connections.length; i++){
 			if(i != index){// if it is not the client I want to keep
 				try {
 					((ConnectionToClient)connections[i]).close();
 				} catch (IOException e1) {
 					// I don't care if the closing fails, because the connection will be dead anyway
 				}
 			}
 		}
 		// Stop the server from accepting more clients
 		this.stopListening();
 		this.gameMode = true;
 		return true;
 	}
 	/**
 	 * Function for ending the server
 	 */
 	public void killServer(){
 		try {
 			this.close();
 		} catch (IOException e) {
 			// don't care if there is an issue
 		}
 		// null out the checker's Game server
 		CheckerGame.getInstance().setServer(null);
 	}
 
 }
