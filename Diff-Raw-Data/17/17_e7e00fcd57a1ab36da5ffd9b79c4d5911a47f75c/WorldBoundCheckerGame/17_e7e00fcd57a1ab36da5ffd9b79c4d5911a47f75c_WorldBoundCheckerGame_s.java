 package com.github.sashman.MC_tehboyz_survival;
 
 import java.util.Date;
 
 import org.bukkit.Server;
 
 /**
  * The class also reduces the bounds size over time and provides warnings when bounds have changed.
  * @author HugeCannon
  *
  */
 
 public class WorldBoundCheckerGame extends WorldBoundCheckerLobby{
 
 	private int bounds_change_time; // In ms
 	private int bounds_change_wait; // In ms
 	private int bounds_change_amount;
 	private int min_world_size;
 	
 	private long last_change_time;
 	private boolean warning_sent;
 	
 	public WorldBoundCheckerGame(Server server, int height, int width, int boundsChangeTime, int boundsChangeAmount, int minimumWorldSize) {
 		super(server, height, width);
 		bounds_change_time = boundsChangeTime;
		bounds_change_wait = (boundsChangeTime/5);
 		bounds_change_amount = boundsChangeAmount;
 		min_world_size = minimumWorldSize;
 		
 		last_change_time = getMsTime();
 		warning_sent = false;
 	}
 	
 	@Override
 	public void run(){
 		if(server == null)
 			return;
 		
 		checkAndMovePlayers();
 		checkAndReduceBounds();
 	}
 	
 	private void checkAndReduceBounds(){
 		
 		if(top_right.getX() > min_world_size && top_right.getZ() > min_world_size){
 			if(getMsTime() - last_change_time > bounds_change_time){
 				if(!warning_sent){
					server.broadcastMessage("Bounds changing soon. Move to within (x,z): " + top_right.toString());
 					warning_sent = true;
 				}
 				if(getMsTime() - last_change_time > (bounds_change_time + bounds_change_wait)){ // Waiting period over
					server.broadcastMessage("Bounds have changed to:" + top_right.toString() );
 					server.broadcastMessage("You will be teleported inside the bounds if you were outside.");
 					bottom_left.setX(bottom_left.getX() + bounds_change_amount);
 					bottom_left.setZ(bottom_left.getZ() + bounds_change_amount);
 					top_right.setX(top_right.getX() - bounds_change_amount);
 					top_right.setZ(top_right.getZ() - bounds_change_amount);
 					last_change_time = getMsTime();
 					warning_sent = false;
 				}
 			}
 		}
 	}
 	
 	
 	private long getMsTime(){
 		return new Date().getTime();
 	}
 
 }
