 package org.tcgframework;
 
 import java.util.ArrayList;
 
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
	ArrayList<String> users = new ArrayList<String>();
 	ArrayList<GameState> games = new ArrayList<GameState>();
 	
 	@Session
 	private ServerSession session;
 	
 	@Inject
 	private BayeuxServer bayeux;
 	
 	//whenever a user joins the room
 	@Listener("/broadcast/waiting")
 	public void addUser(ServerSession session, ServerMessage message){
		//add user to list
		users.add(message.getData().toString());
 		
 		//broadcast list to all users who are waiting
		bayeux.createIfAbsent("/broadcast/waiting");		  
    	ServerChannel broadcastChannel = this.bayeux.getChannel("/broadcast/waiting");   	
    	broadcastChannel.publish(this.session, users, null);
 	}
 	
 	//whenever owner starts a game
 	@Listener("/service/startgame")
 	public void startGame(ServerSession session, ServerMessage message){
 		//add a new game to games
 		games.add(new DominionGameState(message.getClientId()));
 		
 	}
 	
 	
 	
 	
 }
