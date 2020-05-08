 package com.adamki11s.npcs.loading;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
import com.adamki11s.display.FixedSpawnsDisplay;
 import com.adamki11s.exceptions.MissingPropertyException;
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
 import com.adamki11s.npcs.io.LoadNPCTemplate;
 import com.adamki11s.npcs.io.NPCTemplate;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.io.objects.SyncObjectIO;
 import com.adamki11s.sync.io.objects.SyncWrapper;
 import com.adamki11s.sync.io.serializable.SyncLocation;
 
 public class FixedLoadingTable {
 
 	static volatile HashMap<String, Location> fixedSpawns = new HashMap<String, Location>();
 
 	final static SyncObjectIO loader = new SyncObjectIO(FileLocator.getNPCFixedSpawnsFile());
 
 	
 	public static String[] getFixedSpawns() {
 		HashSet<String> ret = new HashSet<String>(fixedSpawns.size());
 		for (Entry<String, Location> e : fixedSpawns.entrySet()) {
 			ret.add(e.getKey());
 		}
 		String[] toSort = ret.toArray(new String[ret.size()]);
 		Arrays.sort(toSort, String.CASE_INSENSITIVE_ORDER);
 		return toSort;
 	}
 
 	public static void spawnFixedNPCS(NPCHandler handle) {
 		QuestX.logDebug("registering npc spawns");
 		loader.read();
 		QuestX.logDebug("wrapper length = " + loader.getReadableData().size());
 		for (SyncWrapper wrapper : loader.getReadableData()) {
 			if (wrapper.getTag().equalsIgnoreCase("NPC_COUNT")) {
 				continue;
 			}
 			String npcName = wrapper.getTag();
 			if (FileLocator.doesNPCNameExist(npcName)) {
 				SyncLocation sl = (SyncLocation) wrapper.getObject();
 				Location spawnLocation = sl.getBukkitLocation();
 				LoadNPCTemplate tempLoader = new LoadNPCTemplate(npcName);
 				try {
 					tempLoader.loadProperties();
 					NPCTemplate template = tempLoader.getLoadedNPCTemplate();
 					template.registerSimpleNPCFixedSpawn(handle, spawnLocation);
 					fixedSpawns.put(npcName, spawnLocation);
 				} catch (MissingPropertyException e) {
 					e.printErrorReason();
 				}
 
 			} else {
 				QuestX.logError("Tried to load NPC '" + npcName + "' but no NPC file was found.");
 			}
 		}
		FixedSpawnsDisplay.updateSoftReference();
 	}
 
 	public static void spawnFixedNPC(NPCHandler handle, String name) {
 		loader.read();
 		SyncLocation sl = (SyncLocation) loader.getObject(name);
 		Location spawnLocation = sl.getBukkitLocation();
 		LoadNPCTemplate tempLoader = new LoadNPCTemplate(name);
 		try {
 			tempLoader.loadProperties();
 			NPCTemplate template = tempLoader.getLoadedNPCTemplate();
 			template.registerSimpleNPCFixedSpawn(handle, spawnLocation);
 		} catch (MissingPropertyException e) {
 			e.printErrorReason();
 		}
 
 	}
 
 	public static boolean addFixedNPCSpawn(Player p, String npcName, Location l, NPCHandler handle) {
 		if (!FileLocator.doesNPCNameExist(npcName)) {
 			QuestX.logChat(p, ChatColor.RED + "There is no NPC created with this name!");
 			return false;
 		} else {
 			if (fixedSpawns.containsKey(npcName)) {
 				QuestX.logChat(p, "A fixed spawn location for this NPC already exists");
 				return false;
 			}
 			SimpleNPC remove = handle.getSimpleNPCByName(npcName);
 			if (remove != null) {
 				remove.destroyNPCObject();
 			}
 
 			LoadNPCTemplate tmp = new LoadNPCTemplate(npcName);
 			try {
 				tmp.loadProperties();
 				tmp.getLoadedNPCTemplate().registerSimpleNPCFixedSpawn(handle, l);
 			} catch (MissingPropertyException e) {
 				e.printErrorReason();
 			}
 
 			loader.read();
 			for (SyncWrapper wrap : loader.getReadableData()) {
 				loader.add(wrap.getTag(), wrap.getObject());
 			}
 			loader.add(npcName, new SyncLocation(l));
 			loader.write();
 			loader.clearReadArray();
 			loader.clearWriteArray();
 
 			fixedSpawns.put(npcName, l);
 
 			QuestX.logChat(p, "Fixed spawn created successfully for NPC '" + npcName + "'.");
 			return true;
 		}
 	}
 
 }
