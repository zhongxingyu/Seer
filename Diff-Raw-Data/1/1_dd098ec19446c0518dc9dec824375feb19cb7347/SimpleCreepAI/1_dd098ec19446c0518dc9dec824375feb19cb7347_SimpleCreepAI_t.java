 package ais;
 
 import model.AI;
 import model.Entity;
 
 public class SimpleCreepAI implements AI {
 	State last;
 	
 	@Override
 	public void next(Entity e) {
 		last = State.MOVE;
 		e.getLocation().moveLeft(10);		
 	}
 }
