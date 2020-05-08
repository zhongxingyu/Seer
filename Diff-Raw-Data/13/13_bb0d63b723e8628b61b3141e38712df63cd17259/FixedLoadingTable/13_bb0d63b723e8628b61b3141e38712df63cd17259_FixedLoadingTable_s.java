 package com.adamki11s.npcs.loading;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.adamki11s.display.FixedSpawnsDisplay;
 import com.adamki11s.exceptions.MissingDeathTriggerPropertyException;
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
 				} catch (MissingDeathTriggerPropertyException e) {
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
 		} catch (MissingDeathTriggerPropertyException e) {
 			e.printErrorReason();
 		}
 
 	}
	
	public static boolean doesNPCHaveFixedSpawn(String npc){
 		return fixedSpawns.containsKey(npc);
 	}
 
 	public static final void deleteAllFixedSpawns(Player p, NPCHandler handle) {
 		File spawn = FileLocator.getNPCFixedSpawnsFile();
 		if (spawn.canRead() && spawn.canWrite()) {
 			if (spawn.exists()) {
 				spawn.delete();
 				try {
 					spawn.createNewFile();
 					QuestX.logChat(p, "All fixed spawns for NPCs were deleted");
 				} catch (IOException e) {
 					QuestX.logChat(p, "There was an error deleting the file");
 					e.printStackTrace();
 				}
 			} else {
 				QuestX.logChat(p, "The file does not exist!");
 			}
 		} else {
 			QuestX.logChat(p, "The file cannot be accessed, it is either missing or being used. Please try again later.");
 		}
 	}
 
 	public static boolean editFixedNPCSpawn(Player p, String npcName, NPCHandler handle) {
 		if (!FileLocator.doesNPCNameExist(npcName)) {
 			if (p != null) {
 				QuestX.logChat(p, ChatColor.RED + "There is no NPC created with this name!");
 			}
 			return false;
 		} else {
 			if (!fixedSpawns.containsKey(npcName)) {
 				if (p != null) {
 					QuestX.logChat(p, "A fixed spawn location for this NPC does not exist");
 				}
 				return false;
 			} else {
 
 				SimpleNPC rem = handle.getSimpleNPCByName(npcName);
 				if (rem != null) {
 					rem.destroyNPCObject();
 				}
 
 				loader.read();
 				loader.clearWriteArray();
 				for (SyncWrapper wrap : loader.getReadableData()) {
 					// copy all the data read, except the npc to remove, set
 					// this to be edited
 					if (!wrap.getTag().equalsIgnoreCase(npcName)) {
 						loader.add(wrap);
 					} else {
 						if (p != null) {
 							loader.add(wrap.getTag(), new SyncLocation(p.getLocation()));
 						}
 					}
 				}
 
 				loader.write();
 				loader.clearReadArray();
 				loader.clearWriteArray();
 
 				if (p != null) {
 					QuestX.logChat(p, "The fixed spawn for NPC '" + npcName + "' was changed to your current location.");
 				}
 
 				return true;
 			}
 		}
 	}
 
 	public static boolean removeFixedNPCSpawn(Player p, String npcName, NPCHandler handle) {
 		if (!FileLocator.doesNPCNameExist(npcName)) {
 			if (p != null) {
 				QuestX.logChat(p, ChatColor.RED + "There is no NPC created with this name!");
 			}
 			return false;
 		} else {
 			if (!fixedSpawns.containsKey(npcName)) {
 				if (p != null) {
 					QuestX.logChat(p, "A fixed spawn location for this NPC does not exist");
 				}
 				return false;
 			} else {
 				SimpleNPC rem = handle.getSimpleNPCByName(npcName);
 				if (rem != null) {
 					rem.destroyNPCObject();
 				}
 
 				loader.read();
 				loader.clearWriteArray();
 				for (SyncWrapper wrap : loader.getReadableData()) {
 					// copy all the data read, except the npc to remove
 					if (!wrap.getTag().equalsIgnoreCase(npcName)) {
 						loader.add(wrap);
 					}
 				}
 				loader.write();
 				loader.clearReadArray();
 				loader.clearWriteArray();
 
 				if (p != null) {
 					QuestX.logChat(p, "The fixed spawn for NPC '" + npcName + "' was removed.");
 				}
 
 				return true;
 			}
 		}
 	}
 
 	public static boolean addFixedNPCSpawn(Player p, String npcName, Location l, NPCHandler handle) {
 		if (!FileLocator.doesNPCNameExist(npcName)) {
 			if (p != null) {
 				QuestX.logChat(p, ChatColor.RED + "There is no NPC created with this name!");
 			}
 			return false;
 		} else {
 			if (fixedSpawns.containsKey(npcName)) {
 				if (p != null) {
 					QuestX.logChat(p, "A fixed spawn location for this NPC already exists");
 				}
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
 			} catch (MissingDeathTriggerPropertyException e) {
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
 
 			if (p != null) {
 				QuestX.logChat(p, "Fixed spawn created successfully for NPC '" + npcName + "'.");
 			}
 			return true;
 		}
 	}
 
 }
