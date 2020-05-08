 package org.tcgframework;
 
 import java.util.ArrayList;
import java.util.HashMap;
 
 import javax.inject.Inject;
 
 import org.cometd.annotation.Listener;
 import org.cometd.annotation.Service;
 import org.cometd.annotation.Session;
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ServerChannel;
 import org.cometd.bayeux.server.ServerMessage;
 import org.cometd.bayeux.server.ServerSession;
 import org.tcgframework.resource.DominionGameState;
 import org.tcgframework.resource.GameState;
 
 @Service
 public class JoinGameService {
 	
 	//Instance Variables
	HashMap<String, String> users = new HashMap<String, String>();
 	ArrayList<GameState> games = new ArrayList<GameState>();
 	
 	@Session
 	private ServerSession session;
 	
 	@Inject
 	private BayeuxServer bayeux;
 	
 	//whenever a user joins the room
 	@Listener("/broadcast/waiting")
 	public void addUser(ServerSession session, ServerMessage message){
		//add the owner
		if (users.isEmpty()){
			users.put("OWNER", message.getData().toString());
		}
		
		//add user to map
		if (users.containsKey(message.getClientId())){
			users.put(message.getClientId(), message.getData().toString());
			System.out.println("Added User: " + message.getData().toString() + " to the users list");
		}
 		
 		//broadcast list to all users who are waiting
		this.bayeux.createIfAbsent("/broadcast/waiting");		  
    	ServerChannel broadcastChannel = this.bayeux.getChannel("/broadcast/waiting");
    	broadcastChannel.publish(this.session, users , null);
 	}
 	
 	//whenever owner starts a game
 	@Listener("/service/startgame")
 	public void startGame(ServerSession session, ServerMessage message){
 		//add a new game to games
 		games.add(new DominionGameState(message.getClientId()));
 		
		//broadcast to all users that a new game started
		
		//empty out the users list
 	}
 	
 	
 	
 	
 }
