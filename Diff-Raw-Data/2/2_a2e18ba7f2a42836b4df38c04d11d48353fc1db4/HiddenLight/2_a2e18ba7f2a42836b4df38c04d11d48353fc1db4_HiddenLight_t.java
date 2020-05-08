 package com.thvortex.bukkit.hiddenlight;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 import java.lang.reflect.Field;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.Event;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.bukkit.craftbukkit.CraftChunk;
 
 import net.minecraft.server.EnumSkyBlock;
 import net.minecraft.server.WorldServer;
 
 
 public class HiddenLight extends JavaPlugin {
 	public static int DISTANCE = 1000;
 	public static Material ITEM = Material.STICK;
 	public static HashSet<Byte> SKIPBLOCKS = new HashSet<Byte>();
 	
 	public static Logger log = Logger.getLogger("Minecraft");
 
 	// EnumSkyBlock.Block is obfuscated in CraftBukkit so we use it by value
 	private static final EnumSkyBlock BLOCK_LIGHT = EnumSkyBlock.values()[1];
 	
 	// Queue used for a breadth first traversal of blocks when propagating light changes
 	private Queue<UpdatePos> updateQueue = new LinkedList<UpdatePos>();
 
 	// Secondary queue use to back-propagate other existing light sources after another
 	// light source was removed or reduced.
 	private Queue<UpdatePos> backUpdateQueue = new LinkedList<UpdatePos>();
 	
 	private Map<String, Integer> activePlayers = new HashMap<String, Integer>();
 	private int blocksUpdatedCount;
 	private int lightLevelDelta;
 	
 	static {
 		// These blocks will get skipped by LivingEntity.getLastTwoTargetBlocks()
 		SKIPBLOCKS.add((byte) 0); // Air
 		SKIPBLOCKS.add((byte) 8); // Water
 		SKIPBLOCKS.add((byte) 9); // Stationary Water
 		SKIPBLOCKS.add((byte) 10); // Lava
 		SKIPBLOCKS.add((byte) 11); // Stationary Lava
 	}
 	
 	private class UpdatePos {
 		public int x, y, z, level;
 		public UpdatePos(int _x, int _y, int _z, int _level) {
 			x = _x; y = _y; z = _z; level = _level;
 		}
 	}
 	
 	@Override
 	public void onEnable() {
 		PlayerListener listener = new PlayerListener() {
 			@Override
 			public void onPlayerInteract(PlayerInteractEvent event) {
 				HiddenLight.this.onPlayerInteract(event);
 			};
 			@Override
 			public void onPlayerQuit(PlayerQuitEvent event) {
 				HiddenLight.this.onPlayerQuit(event);
 			};
 		};
 	
 		PluginManager manager = getServer().getPluginManager();
 		manager.registerEvent(Event.Type.PLAYER_INTERACT, listener, Event.Priority.Normal, this);
 		manager.registerEvent(Event.Type.PLAYER_QUIT, listener, Event.Priority.Normal, this);
 		
 		log.info("[HiddenLight] v0.2 Enabled");
 	}
 
 	@Override
 	public void onDisable() {
 		log.info("[HiddenLight] v0.2 Disabled");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("hiddenlight")) {
 			if(sender instanceof ConsoleCommandSender) {
 				sender.sendMessage("[HiddenLight] You cannot use this command from the console.");
 				return true;
 			}
 
 			Player player = (Player) sender;
 			if(!player.hasPermission("hiddenlight.*")) {
 				player.sendMessage("[HiddenLight] You are not authorized to use this command.");
 				// TODO: remove from activePlayers here
 				return true;
 			}
 
 			if(args.length != 1) {
 				return false;
 			}
 			
 			if(args[0].equalsIgnoreCase("off")) {
 				if(activePlayers.containsKey(player.getName())) {
 					activePlayers.remove(player.getName());
 					sender.sendMessage("[HiddenLight] Deactivated.");
 				} else {
 					sender.sendMessage("[HiddenLight] Was not previously active.");					
 				}
 			} else {
 				int lightLevel;
 			
 				try {
 					lightLevel = Integer.parseInt(args[0]);
 				} catch (NumberFormatException e) {
 					return false;
 				}
 				if(lightLevel < 1 || lightLevel > 15) {
 					return false;
 				}
 				
 				if(activePlayers.containsKey(player.getName())) {
 					sender.sendMessage("[HiddenLight] Using light level " + lightLevel + ".");
 				} else {
 					sender.sendMessage("[HiddenLight] Activated. Using light level " + lightLevel +
 						". Right click with stick to place hidden light source.");
 				}
 				activePlayers.put(player.getName(), lightLevel);
 			}
 			
 			return true;
 		}
 				
 		return false;
 	}
 	
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		// Note that LEFT/RIGHT_CLICK_AIR events are already in cancelled state so we can't really
 		// check for cancel state to avoid conflicts with other plugins using the same item ID
 		// (e.g. Big Brother uses a stick for example).
 		Action action = event.getAction();
 		
 		// Bukkit JavaDoc has this to say about Action.PHYSICAL: "Ass-pressure". What does that even mean?
 		if(action == Action.PHYSICAL) {
 			return;
 		}
 		
 		if(!event.hasItem() || event.getItem().getType() != ITEM) {
 			return;
 		}
 
 		Player player = event.getPlayer();
 		if(!activePlayers.containsKey(player.getName())) {
 			return;
 		}
 		// TODO: Verify permission has not been subsequently revoked
 		int lightLevel = activePlayers.get(player.getName());
 
 		// TODO: getLastTwoTargetBlocks() has a shorter distance than Far rendering. May need to
 		// implement our own line of sight routine here.
 		List<Block> lineOfSight = player.getLastTwoTargetBlocks(SKIPBLOCKS, DISTANCE);
 		if(lineOfSight.size() != 2) {
 			return;
 		}
 		
 		// The second block in the list is the one actually targeted. The first is the air block
 		// next to the targeted face that will get the full light level. If the targeted block is
 		// transparent, then player was aiming at the sky or at least past the end of any currently
 		// loaded chunks.
 		Block block = lineOfSight.get(1);
 		if(SKIPBLOCKS.contains((byte) block.getTypeId())) {
 			return;
 		}
 		block = lineOfSight.get(0);
 
 		int x = block.getX(), y = block.getY(), z = block.getZ();
 		WorldServer world = (WorldServer) ((CraftChunk)block.getChunk()).getHandle().world;
 		
 		// Clicking while crouching will "sample" the existing total light level (block + sky) at
 		// recticle location (similar to the eyedroper tool in paint programs).
 		if(player.isSneaking()) {
 			lightLevel = world.getLightLevel(x, y, z); // World.getBlockLightValue()
 			player.sendMessage("[HiddenLight] Using light level " + lightLevel + ".");
 			activePlayers.put(player.getName(), lightLevel);
 			return;
 		}		
 		
 		// A left click (without crouch) will erase existing light source with light level zero
 		if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
 			lightLevel = 0;
 		}
 		
 		blocksUpdatedCount = 0;
 		lightLevelDelta = lightLevel - world.a(BLOCK_LIGHT, x, y, z); // World.getSavedLightValue()
 		
 		// Propagate light changes outwards starting from the initial click location
 		enqueueUpdate(world, x, y, z, lightLevel, false);
 		while(!updateQueue.isEmpty()) {
 			UpdatePos p = updateQueue.remove();
 			enqueueNeighbors(world, p.x, p.y, p.z, p.level, false);
 		}
 		
 		// Swap the backUpdateQueue with updateQueue so it can run if anything was added to it in the first pass
 		Queue<UpdatePos> temp = updateQueue;
 		updateQueue = backUpdateQueue;
 		backUpdateQueue = temp;
 
 		// After reducing/removing one light, we may have other nearby light sources that can no propagate their
 		// light out to where the removed light was. These additional changes would be previously queued up in
 		// backUpdateQueue.
 		lightLevelDelta = 0;
 		while(!updateQueue.isEmpty()) {
 			UpdatePos p = updateQueue.remove();
 			enqueueNeighbors(world, p.x, p.y, p.z, p.level, false);
 		}
 		
 		// Make sure that a 0x33 Map Chunk packet with light levels is always sent
 		if(blocksUpdatedCount < 10) {
 			forceMapChunkPacket(world, block.getX(), block.getY(), block.getZ());
 		}		
 
 		// Cancel event to avoid potential conflict with other plugins using the same item ID
 		event.setCancelled(true);
 	}
 	
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		activePlayers.remove(event.getPlayer().getName());
 	}
 	
 	private void enqueueUpdate(WorldServer world, int x, int y, int z, int level, boolean backQueue) {	
 		if(y < 0 || y > 127) {
 			return;
 		}
 
 		int opacity = net.minecraft.server.Block.q[world.getTypeId(x, y, z)]; // Block.lightOpacity[]
 		if(opacity > 0) {
 			level -= opacity - 1;
 		}
 		
 		int oldLevel = world.a(BLOCK_LIGHT, x, y, z); // World.getSavedLightValue()		
 		
 		// When removing light, updates must follow along an already existing gradient of decreasing
 		// by 1 light values. The lightLevelDelta is used to detect that gradient and it's calculated
 		// at the original click position as the difference between the newly requested light level
 		// and the original light level at the click location. When a brighter light value is found
 		// at the edge of the gradient, then there must be another light source nearby; queue up
 		// an update for the 2nd pass in backUpdateQueue so the light from the other light source
 		// can be propagated into the blocks where we just removed light in the 1st pass.
 		if(lightLevelDelta < 0 && !backQueue) {
 			if(oldLevel != (level - lightLevelDelta)) {
 				enqueueNeighbors(world, x, y, z, oldLevel, true);
 				return;
 			}
 			
 		// When adding light, only update (and propagate changes) to blocks with less light than we're
 		// currently adding. This propagates the light changes outwards from the initial click location.
 		} else {
 			if(oldLevel >= level) {
 				return;
 			}
 		}
 		
 		// Note that level can be negative when removing light. This is needed to properly detect
 		// the decreasing light gradient that we follow.
 		blocksUpdatedCount++;
		world.a(BLOCK_LIGHT, x, y, z, level < 0 ? 0 : level); // World.setLightValue()
 		
 		if(backQueue) {
 			backUpdateQueue.offer(new UpdatePos(x, y, z, level));
 		} else {
 			updateQueue.offer(new UpdatePos(x, y, z, level));
 		}
 	}
 
 	private void enqueueNeighbors(WorldServer world, int x, int y, int z, int level, boolean backQueue) {	
 		enqueueUpdate(world, x - 1, y, z, level - 1, backQueue);
 		enqueueUpdate(world, x + 1, y, z, level - 1, backQueue);
 		enqueueUpdate(world, x, y - 1, z, level - 1, backQueue);
 		enqueueUpdate(world, x, y + 1, z, level - 1, backQueue);
 		enqueueUpdate(world, x, y, z - 1, level - 1, backQueue);
 		enqueueUpdate(world, x, y, z + 1, level - 1, backQueue);	
 	}
 			
 	// If less than 10 blocks changed, the server will send a 0x35 or 0x34 Block Change or
 	// Multi Block Change packet which does not carry light level updates. We have to mark
 	// additional blocks for update to force sending a 0x33 Map Chunk packet. This function
 	// marks a 3x3x3 block cuboid for updates centered around the initial click location.
 	private void forceMapChunkPacket(WorldServer world, int centerX, int centerY, int centerZ) {
 		if(centerY < 1) {
 			centerY = 1;
 		}
 		if(centerY > 126) {
 			centerY = 126;
 		}
 	
 		for(int x = centerX - 1; x <= centerX + 1; x++) {
 			for(int y = centerY - 1; y <= centerY + 1; y++) {
 				for(int z = centerZ - 1; z <= centerZ + 1; z++) {
 					world.notify(x, y, z);
 				}
 			}
 		}
 	}
 }
