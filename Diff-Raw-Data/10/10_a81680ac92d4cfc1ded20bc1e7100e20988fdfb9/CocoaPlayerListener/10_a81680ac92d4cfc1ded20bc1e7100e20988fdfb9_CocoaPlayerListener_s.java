 package net.mabako.minecraft.Cocoa;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 /**
  * Player Listener for Cocoa that enables its world-editing properties
  * @author mabako (mabako@gmail.com)
  */
 public class CocoaPlayerListener extends PlayerListener
 {
 	private static final int VIEW_DISTANCE = 600;
 
 	/**
 	 * Allows us to process world clicks
 	 * 
 	 * Left Click - replace block with Air
 	 * Right Click - give you the block's material
 	 */
 	@Override
 	public void onPlayerInteract( PlayerInteractEvent event )
 	{
 		Player player = event.getPlayer( );
 		
 		// Has the player cocoa?
 		if( Util.usesCocoa( player ) )
 		{
 			Block block = event.getClickedBlock( );
 			Action action = event.getAction( );
 
 			// Clicking into air = far away
 			if( action == Action.LEFT_CLICK_AIR )
 			{
 				block = player.getTargetBlock( null, VIEW_DISTANCE );
 				action = Action.LEFT_CLICK_BLOCK;
 			}
 			else if( action == Action.RIGHT_CLICK_AIR )
 			{
 				block = player.getTargetBlock( null, VIEW_DISTANCE );
 				action = Action.RIGHT_CLICK_BLOCK;
 			}
 
 			// Can't modify air or bedrock
			if( block == null || block.getType( ) == Material.AIR
					|| block.getType( ) == Material.BEDROCK )
 				return;
 
 			if( action == Action.LEFT_CLICK_BLOCK )
 			{
 				event.setCancelled( true );
 				
 				// Replace block with air
 				block.setType( Material.AIR );
 			}
 			else if( action == Action.RIGHT_CLICK_BLOCK )
 			{
 				event.setCancelled( true );
 				
 				// Give the player the matching item
 				Util.giveItem( player, block );
 			}
 		}
 	}
 
 	/**
 	 * Stops the player from dropping the item
 	 */
 	@Override
 	public void onPlayerDropItem( PlayerDropItemEvent event )
 	{
 		// Has the player cocoa?
 		Player player = event.getPlayer( );
 		if( Util.hasCocoa( player ) )
 		{
 			// Remove all of the player's items with this type
 			player.getInventory( ).remove( event.getItemDrop( ).getItemStack( ).getType( ) );
 
 			// Remove the dropped item
 			// event.getItemDrop( ).remove( );
 		}
 	}
 }
