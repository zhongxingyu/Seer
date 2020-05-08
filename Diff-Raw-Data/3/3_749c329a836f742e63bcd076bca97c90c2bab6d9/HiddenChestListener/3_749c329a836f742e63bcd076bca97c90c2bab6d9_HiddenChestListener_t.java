 package de.bananaco.hidden;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class HiddenChestListener {
 	
 	private final HiddenChestData data;
 	
 	private final BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
 	
 	public HiddenChestListener(HiddenChestData data) {
 		this.data = data;
 	}
 	
 	/**
 	 * Checks if the block is being placed next to a HiddenChest
 	 * @param block
 	 * @param player
 	 * @return boolean allowed
 	 */
 	public boolean blockPlace(Block block, Player player) {
 		// No point checking if it's not a chest being placed
 		if(block.getType() != Material.CHEST)
 			return true;
 		// Iterate over the faces
 		for(BlockFace face : faces) {
 			// If this block is a HiddenChest you can't place a chest next to it!
 			if(data.isBlockHiddenChest(block.getRelative(face)))
 				return false;
 		}
 		// Allow it if not
 		return true;
 	}
 	
 	/**
 	 * Checks if the block being broken is a HiddenChest
 	 * @param block
 	 * @param player
 	 * @return boolean allowed
 	 */
 	public boolean blockBreak(Block block, Player player) {
 		// If the block we're breaking is a chest... what do you think?
 		if(block.getType() == Material.CHEST)
 			return true;
 		// What world are we in?
 		World world = block.getWorld();
 		// Grab a reference to the HiddenChest
 		HiddenChest chest = data.getHiddenChest(block);
 		// Obviously we don't do anything if it's null
 		if(chest == null)
 			return true;
 		// Does the player have permission to break hidden chests?
 		if(!player.hasPermission("hiddenchest.break"))
 			return false;
 		// Get the blocks of the chest
 		Block[] blocks = chest.getBlocks();
 		// Need this for when we drop the items
 		Location location = block.getLocation();
 		// And set them to air
 		for(Block b : blocks)
 			b.setType(Material.AIR);
 		// Then add the itemstacks to the location
 		for(ItemStack item : chest.getContents()) {
 			// Don't drop null items!
 			if(item != null)
 				world.dropItemNaturally(location, item);
 		}
 		// Drop the chest
 		world.dropItem(location, new ItemStack(Material.CHEST, 1));
 		// Remove the HiddenChest reference
 		data.remove(chest);
 		// And cancel the event, just 'cos it's neater
 		return false;
 	}
 	
 	/**
 	 * Transforms a chest when it is left clicked
 	 * @param block
 	 * @param player
 	 * @return allowed
 	 */
 	public boolean chestLeftClick(Block block, Player player) {
 		// If the block we're clicking isn't a chest, no point doing anything
 		if(block.getType() != Material.CHEST)
 			return true;
 		// Get the material in the hand
 		Material inHand = player.getItemInHand().getType();
 		// They're not interacting with a block
 		if(inHand == Material.AIR || inHand.getId() > 127)
 			return true;
 		// If the player doesn't have permission to transform the Chest into a HiddenChest, don't do anything
 		if(!player.hasPermission("hiddenchest.transform."+inHand.getId()))
 			return true;
 		// This shouldn't ever happen, but if it does we'll be safe
 		if(!(block.getState() instanceof Chest))
 			return true;
 		// Now define the Chest
 		Chest chest = (Chest) block.getState();
 		// Add the Chest
 		HiddenChest hidden = data.add(chest);
 		// Clear the contents of the chest
 		chest.getInventory().clear();
 		// Get the Block[] array
 		Block[] blocks = hidden.getBlocks();
 		// And change to the setting Material
 		for(Block b : blocks)
 			b.setType(inHand);
 		// And cancel the event so (hopefully) nothing else happens
 		return false;
 	}
 	
 	/**
 	 * Turns a transformed Block back into a Chest
 	 * @param block
 	 * @param player
 	 * @return allowed
 	 */
 	public boolean blockLeftClick(Block block, Player player) {
 		// If the block we're clicking is a chest, no point doing anything
 		if(block.getType() == Material.CHEST)
 			return true;
 		// Get the material in the hand
 		Material inHand = player.getItemInHand().getType();
 		// They're not interacting with a block
 		if(inHand == Material.AIR || inHand.getId() > 127)
 			return true;
		// Make sure the chest is the same as the one in the hand
		if(block.getType() != inHand)
			return true;
 		// What world are we in?
 		World world = block.getWorld();
 		// Get the HiddenChest (or null)
 		HiddenChest hidden = data.getHiddenChest(block);
 		// If it's null, no point doing anything
 		if(hidden == null)
 			return true;
 		// If the player doesn't have permission to transform the Chest into a HiddenChest, don't do anything
 		if(!player.hasPermission("hiddenchest.transform."+inHand.getId()))
 			return true;
 		// Get the Block[] array
 		Block[] blocks = hidden.getBlocks();
 		// And set them to chests
 		for(Block b : blocks)
 			b.setType(Material.CHEST);
 		// Now that it's a chest we change the reference to the Block object
 		block = world.getBlockAt(block.getLocation());
 		if(!(block.getState() instanceof Chest))
 			return true;
 		// Now define the Chest
 		Chest chest = (Chest) block.getState();
 		// And set the contents
 		chest.getInventory().setContents(hidden.getContents());
 		// Update the chest
 		chest.update();
 		// And remove the reference to the HiddenChest
 		data.remove(hidden);
 		// And cancel the event, for neatness
 		return false;
 	}
 
 }
