 package com.adamki11s.ai;
 
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
 
 public class MovementController {
 	
 	final NPCHandler handle;
 	
 	public MovementController(NPCHandler handle){
 		this.handle = handle;
 	}
 	
	public synchronized void run(){
 		for(SimpleNPC npc : handle.getNPCs()){
 			if(!npc.isMovementScheduled() && npc.isMoveable() && npc.isNPCSpawned() && !npc.isUnderAttack() && !npc.isConversing()){
				npc.moveTick();
 			}
 		}
 	}
 
 }
