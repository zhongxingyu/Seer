 package grygrflzr.mods.glowstonewire;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 
 public class GlowstoneWireEventHook {
 	@ForgeSubscribe
 	public void playerInteract(PlayerInteractEvent event) {
 		if(event.action == event.action.RIGHT_CLICK_BLOCK) {
			if(event.entityPlayer.inventory.getCurrentItem() != null &&
				event.entityPlayer.inventory.getCurrentItem().itemID == Item.glowstone.itemID) {
 				int x = event.x;
 				int y = event.y;
 				int z = event.z;
 				int face = event.face;
 				if(event.entity.worldObj.getBlockId(x, y, z) != Block.snow.blockID) {
 					if (face == 0)
 		                --y;
 		            if (face == 1)
 		                ++y;
 		            if (face == 2)
 		                --z;
 		            if (face == 3)
 		                ++z;
 		            if (face == 4)
 		                --x;
 		            if (face == 5)
 		                ++x;
 				}
 				if(!event.entity.worldObj.isAirBlock(x, y, z))
 					if(event.entity.worldObj.getBlockId(x, y, z) != Block.snow.blockID)
 						return;
 				if(!event.entityPlayer.canPlayerEdit(x, y, z, face, event.entityPlayer.inventory.getCurrentItem()))
 					return;
 				else
 					if(GlowstoneWireMod.glowstoneWire.canPlaceBlockAt(event.entity.worldObj, x, y, z)) {
 						if(!event.entityPlayer.capabilities.isCreativeMode)
 							--event.entityPlayer.inventory.getCurrentItem().stackSize;
 						event.entity.worldObj.setBlock(x, y, z, GlowstoneWireMod.glowstoneWire.blockID);
 					}
 			}
 		}
 	}
 }
