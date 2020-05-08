 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.runetooncraft.plugins.EasyMobArmory.EMA;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.Eggs;
 
 public class SpawnerHandler {
 	public static Eggs eggs = EMA.eggs;
 	public static SpawnerConfig Spawners = EMA.Spawners;
 	public static HashMap<Location, SpawnerCache> SpawnerCache = new HashMap<Location, SpawnerCache>();
 	public static HashMap<SpawnerCache, BukkitTask> SpawnerCacheTimers = new HashMap<SpawnerCache, BukkitTask>();
 	public static Boolean IsEMASpawner(Location loc) {
 		if(Spawners.getList("Spawners.List").contains(Spawners.LocString(loc))) {
 			return true;
 		}else{
 			return false;
 		}
 	}
 	public static void NewEMASpawner(Block b,Player p) {
 		Inventory inv = Bukkit.createInventory(p, 54, "Spawnerinv");
 		SpawnerCache.put(b.getLocation(), new SpawnerCache(b,b.getLocation(),inv));
 		String LocString = Spawners.LocString(b.getLocation());
 		Spawners.addtolist("Spawners.List", LocString);
 		Spawners.SetString("Spawners." + LocString + ".Inventory", InventorySerializer.tobase64(inv));
 		ArrayList<String> EggList = new ArrayList<String>();
 		Spawners.SetList("Spawners." + LocString + ".EggList",EggList);
 		Spawners.SetBoolean("Spawners." + LocString + ".TimerEnabled", false);
 		Spawners.setInt("Spawners." + LocString + ".TimerTick", 64);
 	}
 	public static void OpenSpawnerInventory(Block b,Player p) {
 		String LocString = Spawners.LocString(b.getLocation());
 		Inventory inv = Bukkit.createInventory(p, 54, "Spawnerinv");
 		inv.setContents(InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory")).getContents());
 		p.openInventory(inv);
 	}
 	public static void SetSpawnerInventory(Inventory i, SpawnerCache sc) {
 		sc.getInventory().setContents(i.getContents());
 		SaveSpawnerCache(sc);
 	}
 	private static boolean ReloadSCTimer(SpawnerCache sc) {
 		if(SpawnerCache.containsKey(sc) && SpawnerCacheTimers.containsKey(SpawnerCache.get(sc))) {
 			int TimerTick = sc.TimerTick * 20;
 			SpawnerCacheTimers.get(sc).cancel();
 			SpawnerCacheTimers.put(sc, new MonsterSpawnTimer(sc).runTaskTimer(Bukkit.getPluginManager().getPlugin("EasyMobArmory"), TimerTick, TimerTick));
 			return true;
 		}else{
 			int TimerTick = sc.TimerTick * 20;
 			SpawnerCacheTimers.put(sc, new MonsterSpawnTimer(sc).runTaskTimer(Bukkit.getPluginManager().getPlugin("EasyMobArmory"), TimerTick, TimerTick));
 			return false;
 		}
 	}
 	public static void StartTimer(SpawnerCache sc) {
 		int TimerTick = sc.TimerTick * 20;
 		SpawnerCacheTimers.put(sc, new MonsterSpawnTimer(sc).runTaskTimer(Bukkit.getPluginManager().getPlugin("EasyMobArmory"), TimerTick, TimerTick));
 	}
 	private static void LoadSpawner(Location SpawnerLocation) {
 		World world = SpawnerLocation.getWorld();
 		Block b = world.getBlockAt(SpawnerLocation);
 		String LocString = Spawners.LocString(SpawnerLocation);
 		Inventory inv = InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory"));
 		Boolean TimerEnabled = Spawners.getBoolean("Spawners." + LocString + ".TimerEnabled");
 		SpawnerCache sc = new SpawnerCache(b,SpawnerLocation,inv);
 		sc.TimerEnabled = TimerEnabled;
 		if(TimerEnabled) sc.TimerTick = Spawners.getInt("Spawners." + LocString + ".TimerTick");
 		SpawnerCache.put(SpawnerLocation, sc);
 	}
 	public static SpawnerCache getSpawner(Location SpawnerLocation) {
 		if(SpawnerCache.containsKey(SpawnerLocation)) {
 			return SpawnerCache.get(SpawnerLocation);
 		}else{
 			World world = SpawnerLocation.getWorld();
 			Block b = world.getBlockAt(SpawnerLocation);
 			String LocString = Spawners.LocString(SpawnerLocation);
 			Inventory inv = InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory"));
 			SpawnerCache sc = new SpawnerCache(b,SpawnerLocation,inv);
 			sc.TimerEnabled = Spawners.getBoolean("Spawners." + LocString + ".TimerEnabled");
 			sc.TimerTick = Spawners.getInt("Spawners." + LocString + ".TimerTick");
 			return new SpawnerCache(b,SpawnerLocation,inv);
 		}
 	}
 	public static void SaveSpawnerCache(SpawnerCache sc) {
		if(SpawnerCache.containsKey(sc.getLocation())) SpawnerCache.remove(sc.getLocation());
 		SpawnerCache.put(sc.getLocation(), sc);
 		String LocString = Spawners.LocString(sc.getLocation());
 		Inventory i = sc.getInventory();
 		Spawners.SetString("Spawners." + LocString + ".Inventory", InventorySerializer.tobase64(i));
 		Spawners.setInt("Spawners." + LocString + ".TimerTick", sc.TimerTick);
 		Spawners.SetBoolean("Spawners." + LocString + ".TimerEnabled", sc.TimerEnabled);
 		ItemStack[] EggsStack = i.getContents();
 		for(ItemStack is : EggsStack) {
 			String id = EggHandler.getEggID(is);
 			if(id != null) {
 				if(!Spawners.getList("Spawners." + LocString + ".EggList").contains(id)) {
 					Spawners.addtolist("Spawners." + LocString + ".EggList", id);
 				}
 			}
 		}
 	}
 	public static void SetSpawnTick(Player p,String spawntick) {
 		if(IsInteger(spawntick)) {
 			Block b = p.getTargetBlock(null, 200);
 			if(b.getTypeId() == 52) {
 				if(IsEMASpawner(b.getLocation())) {
 					if(SpawnerCache.containsKey(b.getLocation())) {
 						SpawnerCache sc = SpawnerCache.get(b.getLocation());
 						if(SpawnerCacheTimers.containsKey(sc)) {
 							SpawnerCacheTimers.get(sc).cancel();
 						}
 						sc.TimerTick = Integer.parseInt(spawntick);
 						StartTimer(sc);
 					}else{
 						SpawnerCache sc = getSpawner(b.getLocation());
 						sc.TimerTick = Integer.parseInt(spawntick);
 						SpawnerCache.put(b.getLocation(), sc);
 						StartTimer(sc);
 					}
 					Messenger.playermessage("Set spawner tick to the spawner you are looking at to " + spawntick + ".", p);
 				}else{
 					Messenger.playermessage("The block is a Spawner, but not a EMA-Spawner.", p);
 					Messenger.playermessage("Select the block with a bone and with EMA enabled and add some EMA eggs to make it an EMA spawner.", p);
 				}
 			}else{
 				Messenger.playermessage("Please look at a EMA-Spawner before typing this command.", p);
 			}
 		}else{
 			Messenger.playermessage("The second argument must be an integer.", p);
 		}
 	}
 	public static void SetSpawnTick(SpawnerCache sc) {
 					if(SpawnerCache.containsKey(sc.getLocation())) {
 						if(SpawnerCacheTimers.containsKey(sc)) {
 							SpawnerCacheTimers.get(sc).cancel();
 						}
 						StartTimer(sc);
 					}else{
 						SpawnerCache.put(sc.getLocation(), sc);
 						StartTimer(sc);
 					}
 	} 
 	public static void StartAlreadyExistingSpawnerTimer(String location) {
 		LoadSpawner(Spawners.ParseLocation(location));
 		SpawnerCache sc = getSpawner(Spawners.ParseLocation(location));
 		SetSpawnTick(sc);
 	}
 	private static boolean IsInteger(String s) {
 	    try { 
 	        Integer.parseInt(s); 
 	    } catch(NumberFormatException e) { 
 	        return false; 
 	    }
 	    return true;
 	}
 	public static void CancelSpawnTimer(Player p) {
 		Block b = p.getTargetBlock(null, 200);
 		if(b.getTypeId() == 52) {
 			if(IsEMASpawner(b.getLocation())) {
 				if (SpawnerCache.containsKey(b.getLocation())) {
 					boolean ReturnMessage = true;
 					SpawnerCache sc = SpawnerCache.get(b.getLocation());	
 					Spawners.RemoveFromList("Spawners.Running.List", Spawners.LocString(sc.getLocation()));
 					sc.TimerEnabled = false;
 					try{
 						SpawnerCacheTimers.get(sc).cancel();
 						SpawnerCacheTimers.remove(sc);
 					}catch(NullPointerException e) {
 						Messenger.playermessage("Could not stop this spawners timer.", p);
 						ReturnMessage = false;
 					}
 					SaveSpawnerCache(sc);
 						if(ReturnMessage) Messenger.playermessage("This spawner had it's spawn timer disabled", p);
 					}
 			}else{
 				Messenger.playermessage("The block is a Spawner, but not a EMA-Spawner.", p);
 				Messenger.playermessage("Select the block with a bone and with EMA enabled and add some EMA eggs to make it an EMA spawner.", p);
 			}
 		}else{
 			Messenger.playermessage("Please look at a EMA-Spawner before typing this command.", p);
 		}
 	}
 	public static void CancelSpawnTimer(Block b) {
 		if(b.getTypeId() == 52) {
 			if(IsEMASpawner(b.getLocation())) {
 				if (SpawnerCache.containsKey(b.getLocation())) {
 					SpawnerCache sc = SpawnerCache.get(b.getLocation());	
 					Spawners.RemoveFromList("Spawners.Running.List", Spawners.LocString(sc.getLocation()));
 					sc.TimerEnabled = false;
 					try{
 						SpawnerCacheTimers.get(sc).cancel();
 						SpawnerCacheTimers.remove(sc);
 					}catch(NullPointerException e) {
 					}
 					SaveSpawnerCache(sc);
 				}
 			}
 			}
 		}
 	}
