 package com.em.allocator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.material.Directional;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 
 public class Allocator extends JavaPlugin {
 
 	AllocatorListener thelistener = new AllocatorListener(this);
 
 	// Properties
 	Map<Location, AllocatorBlock> allocatorMap = new HashMap<Location, AllocatorBlock>();
 	public boolean allowFiltering = true;
 	public int quantityDropped = 2;
 	public boolean quantityIsStack = true;
 
 	// Constant
 	Material BLOCK_TYPE = Material.PUMPKIN;
 
 	public void onDisable() {
 		// reload the config
 		reloadConfig();
 
 		// Contruct the datas
 		Map<String, String> allocatorMapS = new HashMap<String, String>();
 		for (Location l : this.allocatorMap.keySet()) {
 			allocatorMapS.put(convertLocation(l), this.allocatorMap.get(l).paramToText());
 		}
 		String allocators = new Yaml().dump(allocatorMapS);
 
 		// if data change, update and save
 		if (!allocators.equals(getConfig().getString("data", "{}"))) {
 			getConfig().set("data", allocators);
 
 			saveConfig();
 		}
 	}
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this.thelistener, this);
 
 		@SuppressWarnings("unchecked")
 		HashMap<String, String> allocatorMapS = (HashMap<String, String>) new Yaml().loadAs(getConfig().getString("data", "{}"), HashMap.class);
 		for (String s : allocatorMapS.keySet()) {
 			Location l = convertString(s);
 			Block b = l.getBlock();
 			this.allocatorMap.put(l, AllocatorBlock.fromBlockAndParamString(b, allocatorMapS.get(s)));
 		}
 
 		allowFiltering = getConfig().getBoolean("allowFiltering", true);
 		quantityDropped = getConfig().getInt("quantityDropped", 1);
 		quantityIsStack = getConfig().getBoolean("quantityIsStack", true);
 
 		getLogger().info(allocatorMap.size() + " allocators");
 		getLogger().info("allowFiltering  = " + allowFiltering);
 		getLogger().info("quantityDropped = " + quantityDropped);
 		getLogger().info("quantityIsStack = " + quantityIsStack);
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (!sender.hasPermission("allocator.create")) {
 			sender.sendMessage(ChatColor.RED + "You have no permission to use this command !!");
 			return true;
 		}
 
 		try {
 			Player player = (Player) sender;
 			Block block = getPlayerTargetBlock(player);
 
 			if (args.length != 0) {
 				// Get info on an Allocator
 				if (args[0].equalsIgnoreCase("info")) {
 					if (this.allocatorMap.containsKey(block.getLocation())) {
 						player.sendMessage(ChatColor.GREEN + "Allocator (" + this.allocatorMap.get(block.getLocation()) + ")");
 						return true;
 					} else {
 						player.sendMessage(ChatColor.RED + "This " + block.getType().toString().toLowerCase() + " is not an allocator");
 						return true;
 					}
 				}
 				// Add an Allocator
 				if (args[0].equalsIgnoreCase("add")) {
 					if (this.allocatorMap.containsKey(block.getLocation())) {
 						player.sendMessage(ChatColor.RED + "This " + block.getType().toString().toLowerCase() + " is allready an allocator (" + this.allocatorMap.get(block.getLocation()) + ")");
 						return true;
 					}
 					if (!block.getType().equals(BLOCK_TYPE)) {
 						player.sendMessage(ChatColor.RED + "Either that's not a " + BLOCK_TYPE.toString().toLowerCase());
 						return true;
 					}
 
 					// Create a new Allocator
 					AllocatorBlock al = setNewAllocator(block, args, player);
 					player.sendMessage(ChatColor.GREEN + "Allocator added ! (" + al + ")");
 
 					return true;
 				}
 				// Set an Allocator
 				if (args[0].equalsIgnoreCase("set")) {
 					if (!this.allocatorMap.containsKey(block.getLocation())) {
 						player.sendMessage(ChatColor.RED + "This " + block.getType().toString().toLowerCase() + " is not an allocator");
 						return true;
 					}
 
 					// Change the Allocator
 					AllocatorBlock al = setNewAllocator(block, args, player);
 					player.sendMessage(ChatColor.GREEN + "Allocator modified ! (" + al + ")");
 
 					return true;
 				}
 			}
 		} catch (ClassCastException e) {
 			sender.sendMessage("You can only use this command as a player!");
 			return true;
 		}
 
 		usage(sender);
 		return true;
 	}
 
 	/**
 	 * Create a new Allocator and put it into the table
 	 * 
 	 * @param block
 	 *          the targeted block
 	 * @param args
 	 *          the command args (the first one is the sub command set, add, ..)
 	 * @param player
 	 *          the player
 	 * @return the new created Allocator
 	 */
 	private AllocatorBlock setNewAllocator(Block block, String[] args, Player player) {
 		List<Material> filters = new ArrayList<Material>();
 		filters.add(player.getItemInHand().getType());
 		BlockFace face = BlockFace.NORTH;
 
 		// get the block facing
 		if (block.getState().getData() instanceof Directional) {
 			face = AllocatorBlock.getFace(block);
 		}
 
 		// get the parameters
 		boolean noFilterFoundYet = true;
 		for (int i = 1; i < args.length; i++) {
 			try {
 				face = BlockFace.valueOf(args[i].toUpperCase());
 			} catch (IllegalArgumentException e1) {
 				try {
 					Material filter = Material.valueOf(args[i].toUpperCase());
 					if (noFilterFoundYet) {
 						filters = new ArrayList<Material>();
 						noFilterFoundYet = false;
 					}
 					filters.add(filter);
 				} catch (IllegalArgumentException e2) {
 					player.sendMessage(ChatColor.RED + "Unknown parameter : " + args[i].toUpperCase());
 				}
 			}
 		}
 
 		AllocatorBlock al = new AllocatorBlock(block, filters, face);
 		this.allocatorMap.put(block.getLocation(), al);
 		return al;
 	}
 
 	void usage(CommandSender sender) {
 		// |----------------------------------------------------------------|
 		// |Command usage :
 		// | /allocator add [filters] [direction] :
 		// | Add an allocator on the pointed pumpkins
 		// | ex : /allocator add Cobblestone Dirt Up
 		// | /allocator set [filters] [direction] :
 		// | Change settings on the pointed allocator
 		// | ex : /allocator set Cobblestone Dirt Up
 		// | /allocator info :
 		// | Get infos (settings) on the pointed allocator
 		// |----------------------------------------------------------------|
 		sender.sendMessage(ChatColor.WHITE + "Command usage :");
 		sender.sendMessage(ChatColor.GOLD + " /allocator add [filters] [direction]" + ChatColor.WHITE + " : ");
 		sender.sendMessage(ChatColor.WHITE + "          Add an allocator on the pointed " + BLOCK_TYPE.toString().toLowerCase());
 		sender.sendMessage(ChatColor.WHITE + "          ex : " + ChatColor.ITALIC + "/allocator add Cobblestone Dirt Up");
 		sender.sendMessage(ChatColor.GOLD + " /allocator set [filters] [direction]" + ChatColor.WHITE + " :");
 		sender.sendMessage(ChatColor.WHITE + "          Change settings on the pointed allocator");
 		sender.sendMessage(ChatColor.WHITE + "          ex : " + ChatColor.ITALIC + "/allocator set Cobblestone Dirt Up");
 		sender.sendMessage(ChatColor.GOLD + " /allocator info" + ChatColor.WHITE + " :");
 		sender.sendMessage(ChatColor.WHITE + "          Get infos (settings) on the pointed allocator");
 	}
 
 	String convertLocation(Location l) {
 		String out = "";
 		out = out + l.getWorld().getName() + ",";
 		out = out + l.getBlockX() + ",";
 		out = out + l.getBlockY() + ",";
 		out = out + l.getBlockZ();
 		return out;
 	}
 
 	Location convertString(String s) {
 		String[] parts = s.split(",");
 		return new Location(getServer().getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 	}
 
 	/**
 	 * Utilities to get Dropped entities at a Location
 	 * 
 	 * @param inputLocation
 	 * @param world
 	 * @return
 	 */
 	public static List<Entity> getEntitiesAtLocation(Location inputLocation) {
 
 		List<Entity> entities = new ArrayList<Entity>();
 		Chunk chunk = inputLocation.getBlock().getChunk();
 		if (chunk.isLoaded()) {
 			Entity[] cEntities = chunk.getEntities();
 
 			for (Entity ent : cEntities) {
 				if (ent.getLocation().getBlock().equals(inputLocation.getBlock())) {
 					entities.add(ent);
 				}
 			}
 		}
 
 		return entities;
 	}
 
 	/**
 	 * Utilities to get Dropped entities at a Location
 	 * 
 	 * @param inputLocation
 	 * @param world
 	 * @return
 	 */
 	public static StorageMinecart getMinecartAtLocation(Location inputLocation) {
 		
 		// First look at the right location
 		List<Entity> entities = getEntitiesAtLocation(inputLocation);
 		
 		for (Entity e : entities) {
			if (e instanceof StorageMinecart) {
 				return (StorageMinecart) e;
 			}
 		}
 		
 		// not found
 		Block b = inputLocation.getBlock();
 		if (b.getType().equals(Material.DETECTOR_RAIL) && b.isBlockPowered()) {
 			StorageMinecart s = getMinecartAtLocation(b.getRelative(BlockFace.NORTH).getLocation());
 			if (s != null) {
 				return s;
 			}
 			s = getMinecartAtLocation(b.getRelative(BlockFace.SOUTH).getLocation());
 			if (s != null) {
 				return s;
 			}
 			s = getMinecartAtLocation(b.getRelative(BlockFace.EAST).getLocation());
 			if (s != null) {
 				return s;
 			}
 			s = getMinecartAtLocation(b.getRelative(BlockFace.WEST).getLocation());
 			if (s != null) {
 				return s;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the targeted Block
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private Block getPlayerTargetBlock(Player player) {
 		Block block = player.getTargetBlock(TRANSPARENT, 5);
 		return block;
 	}
 
 	// define transparent blocks id
 	private static final HashSet<Byte> TRANSPARENT = new HashSet<Byte>();
 	static {
 		TRANSPARENT.add((byte) Material.AIR.getId());
 		TRANSPARENT.add((byte) Material.FENCE.getId());
 		TRANSPARENT.add((byte) Material.FENCE_GATE.getId());
 		TRANSPARENT.add((byte) Material.DETECTOR_RAIL.getId());
 		TRANSPARENT.add((byte) Material.POWERED_RAIL.getId());
 		TRANSPARENT.add((byte) Material.RAILS.getId());
 		TRANSPARENT.add((byte) Material.REDSTONE_WIRE.getId());
 		TRANSPARENT.add((byte) Material.TORCH.getId());
 		TRANSPARENT.add((byte) Material.WOOD_PLATE.getId());
 		TRANSPARENT.add((byte) Material.STONE_PLATE.getId());
 	}
 
 }
