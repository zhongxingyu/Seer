 package com.adamki11s.ai;
 
import java.util.ArrayList;

 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
import com.adamki11s.questx.QuestX;
 
 public class MovementController {
 	
 	final NPCHandler handle;
 	
 	public MovementController(NPCHandler handle){
 		this.handle = handle;
 	}
 	
	public void run(){
		ArrayList<SimpleNPC> process = new ArrayList<SimpleNPC>();
 		for(SimpleNPC npc : handle.getNPCs()){
			
 			if(!npc.isMovementScheduled() && npc.isMoveable() && npc.isNPCSpawned() && !npc.isUnderAttack() && !npc.isConversing()){
				process.add(npc);
 			}
 		}
		
		for(SimpleNPC npc : process){
			npc.moveTick();
		}
 	}
 
 }
