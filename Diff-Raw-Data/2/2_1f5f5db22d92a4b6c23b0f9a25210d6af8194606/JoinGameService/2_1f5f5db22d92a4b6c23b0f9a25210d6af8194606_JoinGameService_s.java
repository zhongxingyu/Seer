 package org.tcgframework;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
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
 	HashMap<String, Object> users = new HashMap<String, Object>();
 	HashSet<String> usernames = new HashSet<String>();
 	HashMap<String, DominionGameState> games = new HashMap<String, DominionGameState>();
 	int gameID = 1;
 	
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
 		
 		//add user to map -- assume username not in set
 		if(usernames.add(message.getData().toString())){
 			users.put("USERS", usernames);	
 		}
 		//broadcast list to all users who are waiting
 		this.bayeux.createIfAbsent("/broadcast/waiting");		  
     	ServerChannel broadcastChannel = this.bayeux.getChannel("/broadcast/waiting");
     	broadcastChannel.publish(this.session, users , null);
     	
     	System.out.println("Broadcasted List of Users");
 	}
 	
 	//whenever owner starts a game
 	@Listener("/broadcast/startgame")
 	public void startGame(ServerSession session, ServerMessage message){
 		//add a new game to games
 		games.put("/game/"+ this.gameID, new DominionGameState(this.gameID, (HashSet<String>) usernames.clone()));
 		
 		//broadcast to all users that a new game started
 		this.bayeux.createIfAbsent("/broadcast/waiting");
 		ServerChannel broadcastChannel = this.bayeux.getChannel("/broadcast/waiting");
 		broadcastChannel.publish(this.session, this.gameID, "start");
 		
 		//empty out the users list
 		users.clear();
 		usernames.clear();
 		this.gameID++;
 		System.out.println("Game has started, Users have been cleared");
 	}
 	
 	//GAME LISTENERS
 	
 	@Listener("/game/*")
 	public void gameHandler(ServerSession sender, ServerMessage message){
 		System.out.println("caught a message");
 		DominionGameState state = games.get(message.getChannel());
 		
 		
 		if (message.getData().toString().equals("iam_ready")){
 			System.out.println("Channel: " + message.getChannel());
			sender.deliver(this.session, message.getChannel(), state.players, null);
 		} else if (message.getData().toString().equals("phase_change")){
 			//progress phase
 			state.nextPhase();
 			
 			//send gamestate
 			ServerChannel broadcastChannel = this.bayeux.getChannel(message.getChannel());
 			broadcastChannel.publish(this.session, state, "gamestate");
 		} else if (message.getData().toString().equals("do_card")){
 			
 		}
 	}
 	
 	
 	
 	
 }
