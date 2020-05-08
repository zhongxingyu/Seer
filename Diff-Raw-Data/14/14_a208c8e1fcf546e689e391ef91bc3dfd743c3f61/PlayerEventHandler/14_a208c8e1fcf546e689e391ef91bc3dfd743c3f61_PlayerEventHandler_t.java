 package sidben.villagertweaks;
 
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.EntityInteractEvent;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 
 
 public class PlayerEventHandler {
 
 
 	@SideOnly(Side.CLIENT)
 	@ForgeSubscribe
 	public void onEntityInteractEvent(EntityInteractEvent event)
 	{
 		/*
 		 * OBS: This method is called whenever a player interacts with something (right-click)
 		 */
 
 		// check if the player right-clicked a villager
         if (event.target instanceof EntityVillager)
         {
 
         	// Check if the player is holding a Name Tag
             ItemStack item = event.entityPlayer.inventory.getCurrentItem();
             if (item != null && item.itemID == Item.field_111212_ci.itemID)			// Name Tag
             {
 
            	// Check if the tag have a name
             	if (item.hasDisplayName()) 
             	{
            		// Set the name to that villager
             		EntityLiving villager = (EntityLiving)event.target;
             		villager.setCustomNameTag(item.getDisplayName());
            		
            		// Consumes the item if not in Creative
                    if (!event.entityPlayer.capabilities.isCreativeMode)
                    {
                    	--item.stackSize;
                    }

                    // Cancel the regular event (trade GUI)
             		event.setCanceled(true);
             	}
 
             }
 
             
         }
 
 
        
 	}
 
 	
 }
