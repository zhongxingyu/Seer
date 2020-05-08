 package net.mabako.minecraft.Cocoa;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Helper functions
  * 
  * @author mabako (mabako@gmail.com)
  * @version 201107142356
  */
 public class Util
 {
 	/**
 	 * Gives the player an item based on the block data
 	 * 
 	 * @param player
 	 *            player to give the item to
 	 * @param block
 	 *            block to copy the item info from
 	 */
 	@SuppressWarnings( "deprecation" )
 	public static void giveItem( Player player, Block block )
 	{
 		Inventory inventory = player.getInventory( );
 		ItemStack[ ] contents = inventory.getContents( );
 		Material material = findItemFromBlockMaterial( block.getType( ) );
 		if( material == null )
 			return;
 
 		// If it is the most recently used item already, do nothing
 		if( same( contents[1], block ) )
 			return;
 
 		// If it's in the last 4 open inventory slots, keep it there
 		for( short i = 5; i <= 8; ++i )
 			if( same( contents[i], block ) )
 				return;
 
 		for( short i = 2; i <= 4; ++i )
 		{
 			if( same( contents[i], block ) )
 			{
 				// Shuffle all lower slots up until the first
 				ItemStack item = contents[i];
 				for( int j = i; j >= 2; --j )
 					inventory.setItem( j, contents[j - 1] );
 				inventory.setItem( 1, item );
 				player.updateInventory( );
 				return;
 			}
 		}
 
 		// Possibly somewhere else in his inventory
 		inventory.remove( material );
 
 		// Only relevant if there is a first item
 		ItemStack lastHistoryItem = null;
 		if( contents[1] != null )
 		{
 			// Save the last history item temporarily
 			lastHistoryItem = contents[4];
 			if( lastHistoryItem != null )
 				inventory.remove( lastHistoryItem );
 
 			// Shuffle history entries
 			for( short i = 4; i >= 2; i-- )
 				inventory.setItem( i, contents[i - 1] );
 		}
 
 		inventory.setItem( 1, new ItemStack( material, material.getMaxStackSize( ), (short) 0,
 				block.getData( ) ) );
 
 		// Give the last history item again (if any)
 		if( lastHistoryItem != null )
 			inventory.addItem( lastHistoryItem );
 
 		player.updateInventory( );
 	}
 
 	/**
 	 * Returns the Item Material that Blocks should be in inventory
 	 * 
 	 * @param material
 	 *            block's material
 	 * @return item's material
 	 */
 	public static Material findItemFromBlockMaterial( Material material )
 	{
 		switch( material )
 		{
 			case WATER:
 				return Material.STATIONARY_WATER;
 			case LAVA:
 				return Material.STATIONARY_LAVA;
 			case DOUBLE_STEP:
 				return Material.STEP;
 			case REDSTONE_WIRE:
 				return Material.REDSTONE;
 			case BURNING_FURNACE:
 				return Material.FURNACE;
 			case SIGN_POST:
 			case WALL_SIGN:
 				return Material.SIGN;
 			case WOODEN_DOOR:
 				return Material.WOOD_DOOR;
 			case IRON_DOOR_BLOCK:
 				return Material.IRON_DOOR;
 			case GLOWING_REDSTONE_ORE:
 				return Material.REDSTONE_ORE;
 			case REDSTONE_TORCH_OFF:
 				return Material.REDSTONE_TORCH_ON;
 			case CAKE_BLOCK:
 				return Material.CAKE;
 			case DIODE_BLOCK_OFF:
 			case DIODE_BLOCK_ON:
 				return Material.DIODE;
 			case LOCKED_CHEST:
 				return Material.CHEST;
 			case AIR:
 			case PISTON_EXTENSION:
 			case PISTON_MOVING_PIECE:
 			case PORTAL:
 			case BEDROCK:
 				return null;
 
 		}
 		return material;
 	}
 
 	/**
 	 * Checks whether an item and a block are identical, meaning they would
 	 * represent the exact same item (keeping colored wool in mind)
 	 * 
 	 * @param item
 	 *            the inventory item to compare with
 	 * @param block
 	 *            the block to compare to
 	 * @return <code>true</code> if the item and block are equal,
 	 *         <code>false</code> otherwise
 	 */
 	private static boolean same( ItemStack item, Block block )
 	{
 		if( item == null )
 			return block == null;
 		else
 			return item.getType( ) == findItemFromBlockMaterial( block.getType( ) )
 					&& item.getDurability( ) == block.getData( );
 	}
 
 	public static boolean usesCocoa( Player player )
 	{
 		ItemStack inHand = player.getItemInHand( );
 		return inHand.getType( ) == Material.INK_SACK && inHand.getDurability( ) == 3;
 	}
 
 	/**
 	 * This checks whether the player has Cocoa as his first item in hand. This
 	 * does <i>not</i> mean the player has no Cocoa at all
 	 * 
 	 * @param player
 	 *            player to check
 	 * @return <code>true</code> if the player has Cocoa, <code>false</code>
 	 *         otherwise
 	 */
 	public static boolean hasCocoa( Player player )
 	{
 		Inventory inventory = player.getInventory( );
 		return inventory.getItem( 0 ).getType( ) == Material.INK_SACK
 				&& inventory.getItem( 0 ).getDurability( ) == 3;
 	}
 
 	/**
 	 * Checks if the player has any of this specific item in his inventory
 	 * 
 	 * @param player
 	 *            player to check
 	 * @param material
 	 *            material to check for
 	 * @param durability
 	 *            damage value (colored wool, etc) to check
 	 * @return <code>true</code> if found, <code>false</code> otherwise
 	 */
 	public static boolean hasItem( Player player, Material material, short durability )
 	{
 		Inventory inventory = player.getInventory( );
 		ItemStack[ ] contents = inventory.getContents( );
 		for( ItemStack i : contents )
			if( i != null && i.getType( ) == material && i.getDurability( ) == durability )
 				return true;
 		return false;
 	}
 }
