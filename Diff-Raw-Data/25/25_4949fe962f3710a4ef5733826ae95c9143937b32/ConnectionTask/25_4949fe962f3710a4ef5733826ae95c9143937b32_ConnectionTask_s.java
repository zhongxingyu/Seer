 package pps.et.server.tasks;
 
 import pps.et.logic.Player;
 
 public class ConnectionTask extends Task {
 	public ConnectionTask(Player p) {
 		super(p);
 	}
 
 	@Override
 	public void consume() {
 		server.sendToPlayer(player, "you id " + player.getID()); // update player id.
 		
 		String text = "player connected " + player.getID() + " " + player.getNick();
 //		server.sendToPlayer(player, "connected " + player.getID());
 		
 		server.send(text);
 	}
 }
