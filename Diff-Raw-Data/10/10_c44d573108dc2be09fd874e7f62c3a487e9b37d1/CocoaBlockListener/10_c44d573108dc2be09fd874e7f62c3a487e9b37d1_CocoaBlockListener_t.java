 package net.mabako.minecraft.Cocoa;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Refills Items on block placement
  * @author mabako (mabako@gmail.com)
  */
 public class CocoaBlockListener extends BlockListener
 {
 	/**
 	 * Refill Item stack on block placement
 	 */
 	@Override
 	public void onBlockPlace( BlockPlaceEvent event )
 	{
 		// Has the player cocoa?
 		Player player = event.getPlayer( );
 		if( Util.hasCocoa( player ) )
 		{
 			// Refill the material
 			Block block = event.getBlock( );
 			Material material = Util.findItemFromBlockMaterial( block.getType( ) );

			if( material != null )
				player.setItemInHand( new ItemStack( material, material.getMaxStackSize( ), block.getData( ), block.getData( ) ) );
 		}
 	}
 }
