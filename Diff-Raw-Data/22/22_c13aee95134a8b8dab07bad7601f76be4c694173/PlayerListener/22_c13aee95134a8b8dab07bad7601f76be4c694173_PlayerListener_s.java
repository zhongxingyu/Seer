 package uk.codingbadgers.bsign.listener;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Button;
 import org.bukkit.material.Diode;
 import org.bukkit.material.Lever;
 
 import uk.codingbadgers.bsign.bSignModule;
 import uk.codingbadgers.bsign.sign.Sign;
 
 /**
  * The listener interface for receiving player events.
  * The class that is interested in processing a player
  * event implements this interface. When
  * the player event occurs, that object's appropriate
  * method is invoked.
  */
 public class PlayerListener implements Listener {
 	
 	/**
 	 * Handles when a player interacts clicks a bSign
 	 * @param event The interact event to handle with a sign interaction
 	 */
 	private void handleSignInteract(final Block block, final Player player)
 	{		
 		// try to find the bsign from all our stored signs
 		Sign contextSign = null;
 		for (Sign sign : bSignModule.SIGNS) {
 					
 			if (sign == null)
 				continue;
 					
 			if (sign.getLocation().equals(block.getLocation())) {
 				contextSign = sign;
 				break;
 			}
 					
 		}
 				
 		// its a sign, but not a bsign
 		if (contextSign == null)
 			return;
 				
 		contextSign.interact(player);
 	}
 	
 	/**
 	 * Handles when a player interacts clicks a bSign
 	 * @param event The interact event to handle with a sign interaction
 	 */
 	private void handleSignInteract(final Block block, final Player player, final int delay)
 	{		
 		// try to find the bsign from all our stored signs
 		Sign contextSign = null;
 		for (Sign sign : bSignModule.SIGNS) {
 			
 			if (sign == null)
 				continue;
 			
 			if (sign.getLocation().equals(block.getLocation())) {
 				contextSign = sign;
 				break;
 			}
 			
 		}
 		
 		// its a sign, but not a bsign
 		if (contextSign == null)
 			return;
 		
 		final Sign bsign = contextSign;
 		
 		bSignModule.MODULE.debugConsole("Running sign at (" + bsign.getLocation().getBlockX() + ", " 
 															+ bsign.getLocation().getBlockY() + ", "
 															+ bsign.getLocation().getBlockZ() + ") with delay of " + delay);
 		
 		Bukkit.getScheduler().scheduleSyncDelayedTask(bSignModule.PLUGIN, new Runnable() {
 			@Override
 			public void run() {
 				bsign.interact(player);
 			}
 		}, (long)delay);
 	}
 	
 	/**
 	 * See if a block is valid restone wire
 	 * @param block THe block to validate
 	 * @return true if the block is redstone, false otherwise
 	 */
 	private boolean isRedStone(Block block) {
 		if (block.getType() == Material.REDSTONE_WIRE)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * See if a block is a sign that we havn't already called
 	 * @param block The block to validate
 	 * @param usedSigns 
 	 * @return true if the block is a sign, false otherwise
 	 */
 	private boolean findSignBlock(Block block, ArrayList<Location> usedSigns) {
 		if ((block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) && !usedSigns.contains(block.getLocation()))
 			return true;		
 		return false;
 	}
 	
 	/**
 	 * See if a block is a sign
 	 * @param block The block to validate
 	 * @param usedSigns 
 	 * @return true if the block is a sign, false otherwise
 	 */
 	private boolean findSignBlock(Block block) {
 		if (block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)
 			return true;		
 		return false;
 	}
 	
 	/**
 	 * See if a block is a repeater
 	 * @param block The block to validate
 	 * @return true if the block is a repeater, false otherwise
 	 */
 	private boolean findRepeaterBlock(Block block) {
 		if (block.getType() == Material.DIODE_BLOCK_ON || block.getType() == Material.DIODE_BLOCK_OFF)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Find connected sign to our redstone trail.
 	 *
 	 * @param startBlock the start block
 	 * @param usedSigns 
 	 * @return the block
 	 */
 	private Block findConnectedSign(Block startBlock, ArrayList<Location> usedSigns)
 	{
 		Location right = startBlock.getLocation();
 		Location left = startBlock.getLocation();
 		Location forward = startBlock.getLocation();
 		Location back = startBlock.getLocation();
 		Location up = startBlock.getLocation();
 		Location down = startBlock.getLocation();
 		
 		if (findSignBlock(right.add(1.0, 0.0, 0.0).getBlock(), usedSigns))
 			return right.getBlock();
 		
 		if (findSignBlock(left.add(-1.0, 0.0, 0.0).getBlock(), usedSigns))
 			return left.getBlock();
 		
 		if (findSignBlock(forward.add(0.0, 0.0, 1.0).getBlock(), usedSigns))
 			return forward.getBlock();
 		
 		if (findSignBlock(back.add(0.0, 0.0, -1.0).getBlock(), usedSigns))
 			return back.getBlock();
 		
 		if (findSignBlock(up.add(0.0, 1.0, 0.0).getBlock(), usedSigns))
 			return up.getBlock();
 		
 		if (findSignBlock(down.add(0.0, -2.0, 0.0).getBlock(), usedSigns))
 			return down.getBlock();
 		
 		return null;
 	}
 	
 	/**
 	 * Find connected a repeater to our current redstone block.
 	 *
 	 * @param startBlock the start block
 	 * @return the block
 	 */
 	private Block findConnectedRepeater(Block startBlock, ArrayList<Location> visitedLocations)
 	{
 		Block north = startBlock.getRelative(BlockFace.NORTH);
 		Block south = startBlock.getRelative(BlockFace.SOUTH);
 		Block east = startBlock.getRelative(BlockFace.EAST);
 		Block west = startBlock.getRelative(BlockFace.WEST);
 		
 		if (findRepeaterBlock(north)) {
 			if (!visitedLocations.contains(north.getLocation())) {
 				
 				Diode data = (Diode)north.getState().getData();
 				BlockFace facing = data.getFacing();
 				
 				if (facing == BlockFace.NORTH) {
 					return north;
 				}
 			}
 		}
 		
 		if (findRepeaterBlock(south)) {
 			if (!visitedLocations.contains(south.getLocation())) {
 
 				Diode data = (Diode)south.getState().getData();
 				BlockFace facing = data.getFacing();
 				
 				if (facing == BlockFace.SOUTH) {
 					return south;
 				}
 			}
 		}
 		
 		if (findRepeaterBlock(east)) {
 			if (!visitedLocations.contains(east.getLocation())) {
 
 				Diode data = (Diode)east.getState().getData();
 				BlockFace facing = data.getFacing();
 				
 				if (facing == BlockFace.EAST) {
 					return east;
 				}
 			}
 		}
 		
 		if (findRepeaterBlock(west)) {
 			if (!visitedLocations.contains(west.getLocation())) {
 
 				Diode data = (Diode)west.getState().getData();
 				BlockFace facing = data.getFacing();
 				
 				if (facing == BlockFace.WEST) {
 					return west;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * find connected redstone to the last piece of redstone
 	 * 
 	 * @param the lastBlock
 	 * @return the new block of redstone
 	 */
 	private Block findConnectedRedstone(Block lastBlock, ArrayList<Location> visitedLocations) {
 				
 		Location up = lastBlock.getLocation();
 		Location down = lastBlock.getLocation();
 		
 		if (isRedStone(up.add(1.0, 0.0, 0.0).getBlock())) {
 			if (!visitedLocations.contains(up.getBlock().getLocation())) {
 				return up.getBlock();
 			}
 		}
 		
 		if (isRedStone(down.add(-1.0, 0.0, 0.0).getBlock())) {
 			if (!visitedLocations.contains(down.getBlock().getLocation())) {
 				return down.getBlock();
 			}
 		}
 		for (int y = -1; y <= 1; ++y) {
 			Location north = lastBlock.getLocation();
 			Location south = lastBlock.getLocation();
 			Location east = lastBlock.getLocation();
 			Location west = lastBlock.getLocation();
 			
 			if (isRedStone(north.add(1.0, y, 0.0).getBlock())) {
 				if (!visitedLocations.contains(north.getBlock().getLocation())) {
 					return north.getBlock();
 				}
 			}
 			
 			if (isRedStone(south.add(-1.0, y, 0.0).getBlock())) {
 				if (!visitedLocations.contains(south.getBlock().getLocation())) {
 					return south.getBlock();
 				}
 			}
 			
 			if (isRedStone(east.add(0.0, y, 1.0).getBlock())) {
 				if (!visitedLocations.contains(east.getBlock().getLocation())) {
 					return east.getBlock();
 				}
 			}
 			
 			if (isRedStone(west.add(0.0, y, -1.0).getBlock())) {
 				if (!visitedLocations.contains(west.getBlock().getLocation())) {
 					return west.getBlock();
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Find connected redstone to a repeater.
 	 *
 	 * @param repeater the repeater
 	 * @param vistedLocations the visted locations
 	 * @return the new redstone block
 	 */
 	private Block findConnectedRedstoneToRepeater(Block repeater, ArrayList<Location> vistedLocations) {
 		Diode data = (Diode)repeater.getState().getData();
 		BlockFace facing = data.getFacing();
 		Block newRedstone = repeater.getRelative(facing);
 		
 		// if the block is not solid or redstone leave because it cannot carry the current
 		if (!(isRedStone(newRedstone) || newRedstone.getType().isTransparent())) {
 			return null;
 		}
 		
 		if (!vistedLocations.contains(newRedstone.getLocation())) {
 			return newRedstone;
 		}
 		return null;
 	}
 	
 	/**
 	 * Handle redstone sign interaction
 	 * @param event The interact event to handle with a sign interaction
 	 */
 	private void handleRedStoneInteract(PlayerInteractEvent event) 
 	{
 		if (!(event.getPlayer() instanceof Player))
 			return;
 		
 		Block block = event.getClickedBlock();
 		if (!isRedstonePowerer(block.getType(), event.getAction()))
 			return;
 		
 		if (block.getType() == Material.STONE_BUTTON || block.getType() == Material.WOOD_BUTTON) {
 			Button data = (Button) block.getState().getData();
 			BlockFace attached = data.getAttachedFace();
 			block = block.getRelative(attached);
 		}
 		
 		if (block.getType() == Material.LEVER) {
 			Lever data = (Lever) block.getState().getData();
 			
 			if (!data.isPowered()) {
 				return;
 			}
 
 			BlockFace attached = data.getAttachedFace();
 			block = block.getRelative(attached);
 		}
 			
 		ArrayList<Location> visitedLocations = new ArrayList<Location>();
 		ArrayList<Location> usedSigns = new ArrayList<Location>();
 		visitedLocations.clear();
 		usedSigns.clear();
 		
 		int powerLevel = 15;
 		int delay = 0;
 		Block sign = null;
 		
 		// check for signs
 		if ((sign = findConnectedSign(block, usedSigns)) != null) {
 			handleSignInteract(sign, event.getPlayer());
 			usedSigns.add(sign.getLocation());
 		}
 		
 		do {
 			
 			visitedLocations.add(block.getLocation());
 			if ((block = findConnectedRedstone(block, visitedLocations)) == null) {
 				bSignModule.MODULE.debugConsole("Could not find next redstone block");
 				return;
 			}
 		
 			if ((sign = findConnectedSign(block, usedSigns)) != null) {
 				handleSignInteract(sign, event.getPlayer(), delay);
 				usedSigns.add(sign.getLocation());
 			}
 			
 			
 			bSignModule.MODULE.debugConsole("Found redstone at (" + block.getLocation().getX() + ", " + block.getLocation().getY() + ", " + block.getLocation().getZ() + ") with power level " + powerLevel);
 			powerLevel = powerLevel - 1;
 		
 			Block repeater = null;
 			if ((repeater = findConnectedRepeater(block, visitedLocations)) != null){
 				bSignModule.MODULE.debugConsole("Found repeater at (" + repeater.getLocation().getX() + ", " + repeater.getLocation().getY() + ", " + repeater.getLocation().getZ() + ")");
 				powerLevel = 15;
 				visitedLocations.add(repeater.getLocation());
 				
 				Block nextRedstone = findConnectedRedstoneToRepeater(repeater, visitedLocations);
 				
 				if (nextRedstone == null) {
 					bSignModule.MODULE.debugConsole("Could not find next redstone block");
 					return;
 				}
 				
 				Diode data = (Diode)repeater.getState().getData();
 				delay += data.getDelay() * 2;
 				
 				block = nextRedstone;
 			
 				if ((sign = findConnectedSign(block, usedSigns)) != null) {
 					handleSignInteract(sign, event.getPlayer(), delay);
 					usedSigns.add(sign.getLocation());
 				}			
 				
 			}
 		} while (powerLevel != 0);
 		
 	}
 	
 	/**
 	 * On player interact. Used to call the interact command on a bSign
 	 *
 	 * @param event the event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 
 		if (event.isCancelled())
 			return;
 		
 		if (bSignModule.SIGNS == null)
 			return;
 				
 		// If we right clicked, then handle the sign interaction
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			// see if we are dealing with a sign
 			final Block block = event.getClickedBlock();
 			if (findSignBlock(block)) {
 				handleSignInteract(block, event.getPlayer());
 				return;
 			}
 		}
 		
 		// Handle redstone events
 		if (isRedstonePowerer(event.getClickedBlock().getType(), event.getAction())) {
 			handleRedStoneInteract(event);
 			return;
 		}
 			
 	}
 	
 	/**
 	 * Checks if is redstone input.
 	 *
 	 * @param type the type
 	 * @param action the action
 	 * @return true, if is redstone input
 	 */
 	private boolean isRedstonePowerer(Material type, Action action ) {
 		if (type == Material.STONE_BUTTON && action.equals(Action.RIGHT_CLICK_BLOCK)) 
 			return true;
 		
 		if (type == Material.WOOD_BUTTON && action.equals(Action.RIGHT_CLICK_BLOCK))
 			return true;
 		
 		if (type == Material.LEVER && action.equals(Action.RIGHT_CLICK_BLOCK))
 			return true;
 		
 		if (type == Material.WOOD_PLATE && action.equals(Action.PHYSICAL))
 			return true;
 		
 		if (type == Material.STONE_PLATE && action.equals(Action.PHYSICAL))
 			return true;
 		
 		return false;
 	}
 
 	/**
 	 * On chat interact. Used to intercept the next chat message a bSign creator
 	 * says, so that we can set the context of a bSign.
 	 *
 	 * @param event the event
 	 */
 	@EventHandler(priority = EventPriority.LOW)
 	public void onChatInteract(AsyncPlayerChatEvent event) {
 		
 		if (bSignModule.SIGNS == null)
 			return;
 		
 		// get the person that is speaking
 		Player chatter = (Player)event.getPlayer();
 		if (chatter == null)
 			return;
 		
 		// spin through all signs, to find a sign that does not have a context
 		// and the creator is the person who is talking
 		Sign contextSign = null;
 		for (Sign sign : bSignModule.SIGNS) {
 			if (sign == null)
 				continue;
 				
 			if (sign.getContext() != null)
 				continue;
 			
 			if (sign.getCreator() == null)
 				continue;
 			
 			if (!sign.getCreator().getName().equalsIgnoreCase(chatter.getName()))
 				continue;
 			
 			contextSign = sign;
 			break;
 		}
 		
 		// if we didn't find a sign, leave now, and let the chat event continue
 		if (contextSign == null)
 			return;
 		
 		// found a sign, so get the message, and cancel the event.
 		String context = event.getMessage();
 		event.setCancelled(true);
 		
 		// if it is a cancel command, destroy the bsign and break the actual sign
 		if (context.equalsIgnoreCase("bsigncancel"))
 		{
 			contextSign.getLocation().getBlock().breakNaturally();
 			bSignModule.SIGNS.remove(contextSign);
 			return;
 		}
 		
 		// Attempt to initialize the sign based upon the message.
 		if (contextSign.init(event.getMessage())) {
 			bSignModule.sendMessage("bSign", chatter, bSignModule.MODULE.getLanguageValue("SIGN-CONTEXT-SET"));
 			
 			String type = contextSign.getType(); 
 			String location = contextSign.getLocation().getX() + "," + contextSign.getLocation().getY() + "," + contextSign.getLocation().getZ() + "," + contextSign.getLocation().getWorld().getName();
 			
 			String addSign = "INSERT INTO bSign " +
 					"VALUES ('" +
 					type + "', '" +
 					contextSign.getContext() + "', '" +
 					contextSign.getCreator().getName() + "', '" +
 					location +
 					"')";
			bSignModule.DATABASE.Query(addSign);
 			return;
 		}
 		
 		bSignModule.sendMessage("bSign", chatter, bSignModule.MODULE.getLanguageValue("INVALID-SIGN-CONTEXT"));
 		
 	}
 }
