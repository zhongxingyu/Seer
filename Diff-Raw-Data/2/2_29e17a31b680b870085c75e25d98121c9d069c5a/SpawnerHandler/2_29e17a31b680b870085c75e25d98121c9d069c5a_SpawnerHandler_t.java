 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.runetooncraft.plugins.EasyMobArmory.EMA;
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.Eggs;
 
 public class SpawnerHandler {
 	public static Eggs eggs = EMA.eggs;
 	public static SpawnerConfig Spawners = EMA.Spawners;
 	public static HashMap<Location, SpawnerCache> SpawnerCache = new HashMap<Location, SpawnerCache>();
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
 		if(i.contains(Material.REDSTONE)) {
 			sc.TimerEnabled = true;
 			HashMap<Integer, ? extends ItemStack> hm = i.all(Material.REDSTONE);
 			int Size = hm.size();
 			int TimerTick = 0;
 			for(int a = 1; a<Size;) {
 				TimerTick = TimerTick + hm.get(a).getAmount();
 			}
 			sc.TimerTick = TimerTick;
 		}
 		SaveSpawnerCache(sc);
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
 		World world = SpawnerLocation.getWorld();
 		Block b = world.getBlockAt(SpawnerLocation);
 		String LocString = Spawners.LocString(SpawnerLocation);
 		Inventory inv = InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory"));
 		return new SpawnerCache(b,SpawnerLocation,inv);
 	}
 	public static void SaveSpawnerCache(SpawnerCache sc) {
 		String LocString = Spawners.LocString(sc.getLocation());
 		Inventory i = sc.getInventory();
 		Spawners.SetString("Spawners." + LocString + ".Inventory", InventorySerializer.tobase64(i));
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
 }
