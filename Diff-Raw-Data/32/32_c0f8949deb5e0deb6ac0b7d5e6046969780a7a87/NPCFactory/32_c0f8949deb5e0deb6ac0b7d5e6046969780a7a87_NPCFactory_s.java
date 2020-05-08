 package npc;
 
 import app.RPGame;
 import utils.Location;
 
 public class NPCFactory {
 	
	public static NPC createNPC(String npcName, RPGame game, Location loc, String configURL){
 		if (npcName.equals("NPC"))
			return new NPC(game, loc, configURL);
 		if (npcName.equals("NPCTest1"))
			return new NPCTest1(game, loc, configURL);
 		else{
 			System.out.println("didn't recognize type name for the NPC, created generic NPC");
			return new NPC(game, loc, configURL);
 		}
 	}
 
 }
