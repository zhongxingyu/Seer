 package net.slipcor.pvparena.modules.chestfiller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.loadables.ArenaRegion;
 import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
 import net.slipcor.pvparena.regions.CuboidRegion;
 import net.slipcor.pvparena.regions.SphericRegion;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.CommandSender;
 import org.bukkit.inventory.ItemStack;
 
 public class ChestFiller extends ArenaModule {
 	public ChestFiller() {
 		super("ChestFiller");
 	}
 	boolean setup = false;
 	
 	@Override
 	public String version() {
		return "v1.1.0.333";
 	}
 
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!cf") || s.startsWith("chestfiller");
 	}
 
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !cf clear | clear inventory definitions
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 
 		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 1 })) {
 			return;
 		}
 		
 		if (!args[0].equals("clear")) {
 			return;
 		}
 		
 		arena.getArenaConfig().setManually("inventories", null);
 		arena.getArenaConfig().save();
 		
 		sender.sendMessage(Language.parse(MSG.MODULE_CHESTFILLER_CLEAR));
 	}
 	
 	@Override
 	public void displayInfo(CommandSender sender) {
 		sender.sendMessage("items: " + (String) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems"));
 		sender.sendMessage("max: " + (Integer) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")
 				+ " | " +
 				"min: " + (Integer) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems"));
 		
 	}
 	
 	@Override
 	public boolean needsBattleRegion() {
 		return true;
 	}
 	
 	@Override
 	public void parseStart() {
 		if (!setup) {
 			if (arena.getArenaConfig().getUnsafe("modules.chestfiller") == null) {
 				arena.getArenaConfig().setManually("modules.chestfiller.cfitems", "1");
 				arena.getArenaConfig().setManually("modules.chestfiller.cfmaxitems", 5);
 				arena.getArenaConfig().setManually("modules.chestfiller.cfminitems", 0);
 				arena.getArenaConfig().save();
 			}
 			if (arena.getArenaConfig().getUnsafe("modules.chestfiller.clear") == null) {
 				arena.getArenaConfig().setManually("modules.chestfiller.clear", false);
 				arena.getArenaConfig().save();
 			}
 			setup = true;
 		}
 		
 		String items = null;
 		try {
 			items = (String) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems");
 		} catch (Exception e) {
 			return;
 		}
 		
 		boolean clear = false;
 		try {
 			clear = (Boolean) arena.getArenaConfig().getUnsafe("modules.chestfiller.clear");
 		} catch (Exception e) {
 			return;
 		}
 		
 		final int cmax = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")));
 		final int cmin = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems")));
 		
 		ItemStack[] stacks = StringParser.getItemStacksFromString(items);
 		
 		if (stacks.length < 1) {
 			return;
 		}
 		
 
 		// ----------------------------------------
 		
 		if (arena.getArenaConfig().getStringList("inventories", new ArrayList<String>()).size() > 0) {
 
 			List<String> tempList = arena.getArenaConfig()
 					.getStringList("inventories", null);
 
 			debug.i("reading inventories");
 
 			for (String s : tempList) {
 				Location loc = parseStringToLocation(s);
 
 				fill(loc, clear, cmin, cmax, stacks);
 			}
 
 			return;
 		}
 		debug.i("NO inventories");
 		
 		int x;
 		int y;
 		int z;
 		List<String> result = new ArrayList<String>();
 		
 		for (ArenaRegion bfRegion : arena.getRegionsByType(RegionType.BATTLE)) {
 			PABlockLocation min = bfRegion.getShape().getMinimumLocation();
 			PABlockLocation max = bfRegion.getShape().getMaximumLocation();
 
 			debug.i("min: "+min.toString());
 			debug.i("max: "+max.toString());
 
 			World world = Bukkit.getWorld(max.getWorldName());
 
 
 			if (bfRegion.getShape() instanceof CuboidRegion) {
 				debug.i("cube!");
 
 				for (x = min.getX(); x <= max.getX(); x++) {
 					for (y = min.getY(); y <= max.getY(); y++) {
 						for (z = min.getZ(); z <= max.getZ(); z++) {
 							Location loc = saveBlock(world, x, y, z);
 							if (loc == null) {
 								continue;
 							}
 							debug.i("loc not null: " + loc.toString());
 							result.add(parseLocationToString(loc));
 						}
 					}
 				}
 			} else if (bfRegion.getShape() instanceof SphericRegion) {
 				debug.i("sphere!");
 				for (x = min.getX(); x <= max.getX(); x++) {
 					for (y = min.getY(); y <= max.getY(); y++) {
 						for (z = min.getZ(); z <= max.getZ(); z++) {
 							Location loc = saveBlock(world, x, y, z);
 							if (loc == null) {
 								continue;
 							}
 							debug.i("loc not null: " + loc.toString());
 							result.add(parseLocationToString(loc));
 						}
 					}
 				}
 			}
 		}
 		
 		
 		arena.getArenaConfig().setManually("inventories", result);
 		arena.getArenaConfig().save();
 
 		// ----------------------------------------
 		
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
 	
 	private void fill(Location loc, boolean clear, int min, int max, ItemStack[] stacks) {
		Chest c;
		
		try {
			c = (Chest) loc.getBlock().getState();
		} catch (ClassCastException cce) {
			return;
		}
 		
 		if (clear) {
 			c.getBlockInventory().clear();
 		}
 		
 		List<ItemStack> adding = new ArrayList<ItemStack>();
 		
 		Random r = (new Random());
 		
 		int count = r.nextInt(max-min)+min;
 		
 		
 		int i = 0;
 		
 		while (i++ < count) {
 			int d = r.nextInt(stacks.length);
 			adding.add(stacks[d].clone());
 		}
 		
 		for (ItemStack it : adding) {
 			c.getInventory().addItem(it);
 		}
 		c.update();
 	}
 
 	private Location saveBlock(World world, int x, int y, int z) {
 		Block b = world.getBlockAt(x, y, z);
 		if (b.getType() == Material.CHEST) {
 			return b.getLocation();
 		}
 		return null;
 	}
 
 	private String parseLocationToString(Location loc) {
 		return loc.getWorld().getName() + "," + loc.getBlockX() + ","
 				+ loc.getBlockY() + "," + loc.getBlockZ();
 	}
 }
