 package net.slipcor.pvparena.modules.blockrestore;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.neworder.ArenaRegion;
 import net.slipcor.pvparena.neworder.ArenaRegion.RegionShape;
 
 public class RestoreContainer {
 	private Arena arena;
 	private ArenaRegion bfRegion;
 
 	private HashMap<Location, ItemStack[]> chests = new HashMap<Location, ItemStack[]>();
 	private HashMap<Location, ItemStack[]> furnaces = new HashMap<Location, ItemStack[]>();
 	private HashMap<Location, ItemStack[]> dispensers = new HashMap<Location, ItemStack[]>();
 
 	public RestoreContainer(Arena a, ArenaRegion r) {
 		arena = a;
 		bfRegion = r;
 	}
 
 	private Debug db = new Debug(55);
 
 	protected void restoreChests() {
 		World world = Bukkit.getWorld(arena.getWorld());
 		db.i("restoring chests");
 		for (Location loc : chests.keySet()) {
 			try {
 				db.i("trying to restore chest: " + loc.toString());
 				Inventory inv = ((Chest) world.getBlockAt(loc).getState())
 						.getInventory();
 				inv.clear();
 				inv.setContents(chests.get(loc));
 				db.i("success!");
 			} catch (Exception e) {
 				//
 			}
 		}
 		for (Location loc : dispensers.keySet()) {
 			try {
 				db.i("trying to restore dispenser: " + loc.toString());
 
 				Inventory inv = ((Dispenser) world.getBlockAt(loc).getState())
 						.getInventory();
 				inv.clear();
 				for (ItemStack is : dispensers.get(loc)) {
 					if (is != null) {
 						inv.addItem(cloneIS(is));
 					}
 				}
 				db.i("success!");
 			} catch (Exception e) {
				//
 			}
 		}
 		for (Location loc : furnaces.keySet()) {
 			try {
 				db.i("trying to restore furnace: " + loc.toString());
 				((Furnace) world.getBlockAt(loc).getState()).getInventory()
 						.setContents(cloneIS(furnaces.get(loc)));
 				db.i("success!");
 			} catch (Exception e) {
 				//
 			}
 		}
 	}
 
 	private ItemStack[] cloneIS(ItemStack[] contents) {
 		ItemStack[] result = new ItemStack[contents.length];
 
 		for (int i = 0; i < result.length; i++) {
 			if (contents[i] == null) {
 				continue;
 			}
 			ItemStack is = contents[i];
 			result[i] = new ItemStack(is.getType(), is.getAmount(),
 					is.getDurability(), is.getData().getData());
 
 			for (Enchantment ench : is.getEnchantments().keySet()) {
 				result[i].addUnsafeEnchantment(ench,
 						is.getEnchantments().get(ench));
 			}
 		}
 
 		return result;
 	}
 
 	private ItemStack cloneIS(ItemStack is) {
 		return is.clone();
 	}
 
 	public void saveChests() {
 
 		if (arena.cfg.get("inventories") != null) {
 
 			List<String> tempList = arena.cfg.getStringList("inventories", null);
 
 			db.i("reading inventories");
 			
 			for (String s : tempList) {
 				Location loc = parseStringToLocation(s);
 				
 				saveBlock(loc.getWorld(),loc.getBlockX(),loc.getBlockY(), loc.getBlockZ());
 			}
 			
 			return;
 		}
 		db.i("NO inventories");
 
 		chests.clear();
 		furnaces.clear();
 		dispensers.clear();
 		int x;
 		int y;
 		int z;
 
 		Location min = bfRegion.getAbsoluteMinimum();
 		Location max = bfRegion.getAbsoluteMaximum();
 
 		World world = bfRegion.getAbsoluteMaximum().getWorld();
 		
 		List<String> result = new ArrayList<String>();
 		
 		if (bfRegion.getShape().equals(RegionShape.CUBOID)) {
 
 			for (x = min.getBlockX(); x <= max.getBlockX(); x++) {
 				for (y = min.getBlockY(); y <= max.getBlockY(); y++) {
 					for (z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
 						Location loc = saveBlock(world,x,y,z);
 						if (loc == null) {
 							continue;
 						}
 						db.i("loc not null: " + loc.toString());
 						result.add(parseLocationToString(loc));
 					}
 				}
 			}
 		} else if (bfRegion.getShape().equals(RegionShape.SPHERIC)) {
 			for (x = min.getBlockX(); x <= max.getBlockX(); x++) {
 				for (y = min.getBlockY(); y <= max.getBlockY(); y++) {
 					for (z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
 						Location loc = saveBlock(world,x,y,z);
 						if (loc == null) {
 							continue;
 						}
 						db.i("loc not null: " + loc.toString());
 						result.add(parseLocationToString(loc));
 					}
 				}
 			}
 		}
 		arena.cfg.set("inventories", result);
 		arena.cfg.save();
 	}
 
 	private Location saveBlock(World world, int x, int y, int z) {
 		Block b = world.getBlockAt(x, y, z);
 		if (b.getType() == Material.CHEST) {
 			Chest c = (Chest) b.getState();
 
 			chests.put(b.getLocation(), cloneIS(c
 					.getInventory().getContents()));
 			return b.getLocation();
 		} else if (b.getType() == Material.FURNACE) {
 			Furnace c = (Furnace) b.getState();
 
 			furnaces.put(b.getLocation(), cloneIS(c
 					.getInventory().getContents()));
 			return b.getLocation();
 		} else if (b.getType() == Material.DISPENSER) {
 			Dispenser c = (Dispenser) b.getState();
 
 			dispensers.put(b.getLocation(), cloneIS(c
 					.getInventory().getContents()));
 			return b.getLocation();
 		}
 		return null;
 	}
 
 	private Location parseStringToLocation(String loc) {
 		// world,x,y,z
 		String[] args = loc.split(",");
 
 		World world = Bukkit.getWorld(args[0]);
 		int x = Integer.parseInt(args[1]);
 		int y = Integer.parseInt(args[2]);
 		int z = Integer.parseInt(args[3]);
 
 		return new Location(world, x, y, z);
 	}
 
 	private String parseLocationToString(Location loc) {
 		return loc.getWorld().getName() + "," + loc.getBlockX() + ","
 				+ loc.getBlockY() + "," + loc.getBlockZ();
 	}
 }
