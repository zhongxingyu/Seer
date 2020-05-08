 /* 
  * This is the main class, at the moment you are able to create basic NPC's.
  * The only problem I'm facing now is that an npc will not be shown to a player that dies/just joined.
  * (because the npc-packet doesn't get transmitted, now I need to find a good way to do this without eating
  * all the server resources)
  */
 package common.captainbern.npclib;
 
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 
 import common.captainbern.npclib.entity.NPC;
 
 public class NPCManager {
 	
 	/**
 	 * We will use a LinkedList instead of an ArrayList since a LinkedList is faster and can't contain 2 times the
 	 * same value. (May change to arraylist in the future, who knows?)
 	 */
 	private LinkedList<NPC> npcs = new LinkedList<NPC>();
 	
 	private ConcurrentHashMap<Integer, NPC> npcIDS = new ConcurrentHashMap<Integer, NPC>();
 	private ConcurrentHashMap<String, NPC> npcNAMES = new ConcurrentHashMap<String, NPC>();
 	
 	public NPCManager(){
 		
 	}
 
 	public NPC createNpc(String name, Location location){
 		if(npcNAMES.containsKey(name)){
 			Bukkit.getLogger().warning("There already exists an NPC with the name: " + name + "!");
 			return null;
 		}
 		
 		int id = getNextID();
 		
 		NPC npc = new NPC(name, location);
 		npc.setId(id);
 		npc.update();
 		
 		npcs.add(npc);
 		
 		npcNAMES.put(npc.getName(), npc);
 		npcIDS.put(id, npc);
 		
 		return npc;
 	}
 	
 	/**
 	 * Returns a fancy (unique) ID for the NPC.
 	 */
 	protected int nextID = Integer.MIN_VALUE;
 	public int getNextID(){
 		return nextID++;
 	}
 	
 	/**
 	 * Returns a npc by it's name.
 	 */
 	public NPC getNpcByName(String name){
 		if(npcNAMES.containsKey(name)){
 			return npcNAMES.get(name);
 		}else{
 			return null;
 		}
 	}
 	
 	/**
	 * Returns a npc by it's id.
 	 */
 	public NPC getNpcById(int id){
 		if(npcIDS.containsKey(id)){
 			return npcIDS.get(id);
 		}else{
 			return null;
 		}
 	}
 }
