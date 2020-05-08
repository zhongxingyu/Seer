 package npc;
 
import gameCharacter.GameCharacter;
 import app.RPGame;
 import utils.Location;
 
 public class NPCFactory {
 	
	public static NPC createNPC(String npcName, GameCharacter gameChar){
 		if (npcName.equals("NPC"))
			return new NPC(gameChar);
 		if (npcName.equals("NPCTest1"))
			return new NPCTest1(gameChar);
 		else{
 			System.out.println("didn't recognize type name for the NPC, created generic NPC");
			return new NPC(gameChar);
 		}
 	}
 
 }
