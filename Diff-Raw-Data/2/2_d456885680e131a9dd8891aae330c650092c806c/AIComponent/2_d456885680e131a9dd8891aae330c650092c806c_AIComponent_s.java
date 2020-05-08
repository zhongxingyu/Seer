 /**************************************************************
  *	file:		AIComponent.java
  *	author:		Andrew King, Anthony Mendez, Ghislain Muberwa
  *	class:		CS499 - Game Programming
  *
  *	assignment:	Class Project
  *	date last modified:	
  *
  *	purpose: Abstract class for AI
 **************************************************************/
 package edu.csupomona.kyra.component.ai;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.geom.Line;
 import org.newdawn.slick.tiled.TiledMap;
 
 import edu.csupomona.kyra.Kyra;
 import edu.csupomona.kyra.component.Component;
 import edu.csupomona.kyra.component.physics.objects.Block;
 import edu.csupomona.kyra.component.physics.objects.BlockMap;
 import edu.csupomona.kyra.entity.Entity;
 
 public abstract class AIComponent extends Component {
 	Entity player1;
 	Entity player2;
 	BlockMap map;
 	ArrayList<String> actions;
 
 	public AIComponent(String id, Entity player1, Entity player2, TiledMap map) {
 		super(id);
 		this.player1 = player1;
 		this.player2 = player2;
 		this.map = new BlockMap(map);
 	}
 	
 	//Draws line to player
 	protected Line getLineToPlayer(Entity player) {
 		if (player.getHealthComponent().isDead())
 			return null;
 		return new Line(owner.getPosition(), player.getPosition());
 	}
 	
 	//Returns whether there is a clear path to the player
 	protected boolean clearPathToPlayer(Line lineToPlayer) {
 		for (Block block : map.getBlocks()) {
 			if (lineToPlayer.intersects(block.getPolygon()))
 				return false;
 		}
 		return true;
 	}
 	
 	//Returns the line to player
 	public Line getLineToTarget() {
 		Line p1Line = getLineToPlayer(player1);
 		if (Kyra.vs) {
 			Line p2Line = getLineToPlayer(player2);
 			//If a player is dead, return the living player's line if it is clear
 			if (p1Line != null && p2Line == null) {
 				if (clearPathToPlayer(p1Line))
 					return p1Line;
 				return null;
 			}
 			else if (p2Line != null && p1Line == null) {
 				if (clearPathToPlayer(p2Line))
 					return p2Line;
 				return null;
 			}
 			//If both players are alive return the one with the clear path
 			if (clearPathToPlayer(p1Line) && !clearPathToPlayer(p2Line))
 				return p1Line;
 			else if (!clearPathToPlayer(p1Line) && clearPathToPlayer(p2Line))
 				return p2Line;
 			//If both are clear return the shorter line
 			else if (clearPathToPlayer(p1Line) && clearPathToPlayer(p2Line)) {
 				if (p1Line.length() < p2Line.length())
 					return p1Line;
 				else
 					return p2Line;
 			}
 			//If all of the above fail, then there is no path
 			else
 				return null;
 			
 		}
		if (clearPathToPlayer(p1Line))
 			return p1Line;
 		else
 			return null;
 	}
 	
 	//Returns a set of actions
 	public ArrayList<String> getActions() {
 		return actions;
 	}
 }
