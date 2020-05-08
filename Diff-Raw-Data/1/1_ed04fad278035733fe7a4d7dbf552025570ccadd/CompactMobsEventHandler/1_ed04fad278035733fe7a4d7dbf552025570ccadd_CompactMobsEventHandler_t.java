 package compactMobs;
 
 import net.minecraft.entity.EntityAgeable;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntitySheep;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.EntityInteractEvent;
 
 import compactMobs.Items.CompactMobsItems;
 
 public class CompactMobsEventHandler {
 	@ForgeSubscribe
 	public void entityInteract(EntityInteractEvent event)
 	{
 		if (event.entityPlayer != null && event.target != null)
 		{
 			//EntityInteractEvent event = (EntityInteractEvent)eevent;
 			EntityPlayer player = event.entityPlayer;
 			if (event.target instanceof EntityLiving)
 			{
 				EntityLiving entity = (EntityLiving) event.target;
 				if (player.getCurrentEquippedItem() != null)
 				{
 					if (player.getCurrentEquippedItem().getItem() == CompactMobsItems.handCompactor)
 					{
 						NBTTagCompound nbttag;
 				    	int charge = 0;
 				    	if (player.getCurrentEquippedItem().hasTagCompound())
 				    	{
 				    		nbttag = player.getCurrentEquippedItem().stackTagCompound;
 				    	}
 				    	else
 				    	{
 				    		nbttag = new NBTTagCompound();
 				    	}
 				    	if (nbttag.hasKey("charge"))
 				    	{
 				    		charge = nbttag.getInteger("charge");
 				    	}
 						
 				    	if (charge == 0)
 				    	{
 				    		charge = player.getCurrentEquippedItem().getItem().getMaxDamage()-10;
 				    	}
 				    	else if (charge > 1)
 				    	{
 				    		if (charge - 10 <= 1)
 				    		{
 				    			charge = 1;
 				    	}
 				    		else
 				    		{
 				    			charge -= 10;
 				    		}
 				    	}
 				    	ItemStack stack = player.getCurrentEquippedItem();
 						if (stack.getItemDamage() <= stack.getItem().getMaxDamage()*.05 && stack.getItemDamage() != 0 )
 						{
 							charge = 0;
 							if (player.inventory.hasItem(Item.coal.itemID)&&player.inventory.hasItem(CompactMobsItems.mobHolder.itemID))
 							{
 								boolean space = false;
 								boolean useS1 = false;
 								boolean useS2 = false;
 								int spot = 0;
 								for (int i = 0; i< player.inventory.getSizeInventory(); i++)
 								{
 									if (player.inventory.getStackInSlot(i)==null)
 									{
 										space = true;
 										spot = i;
 										break;
 									}
 									else if(player.inventory.getStackInSlot(i).getItem()==Item.coal && player.inventory.getStackInSlot(i).stackSize == 1)
 									{
 										space = true;
 										spot = i;
 										useS1 = true;
 										break;
 									}
 									else if(player.inventory.getStackInSlot(i).getItem()==CompactMobsItems.mobHolder && player.inventory.getStackInSlot(i).stackSize == 1)
 									{
 										space = true;
 										spot = i;
 										useS2 = true;
 										break;
 									}
 								}
 								if (space)
 								{
 									spawnParticles(player.worldObj, entity, player);
 									if (!useS1)
 									{
 										player.inventory.consumeInventoryItem(Item.coal.itemID);
 									}
 									if (!useS2)
 									{
 										player.inventory.consumeInventoryItem(CompactMobsItems.mobHolder.itemID);
 									}
 									player.inventory.setInventorySlotContents(spot, compact(entity));
 								}
 								else
 								{
 									CompactMobsCore.instance.proxy.spawnParticle("flame", entity.posX+.5D, entity.posY + .5D, entity.posZ + .5D, 0, 0, 0, 10);
 								}
 							}
 							else
 							{
 								CompactMobsCore.instance.proxy.spawnParticle("flame", entity.posX+.5D, entity.posY + .5D, entity.posZ + .5D, 0, 0, 0, 10);
 							}
 						}
 				    	nbttag.setInteger("charge", charge);
 				    	player.getCurrentEquippedItem().setTagCompound(nbttag);
 				    	
 				    	
 				    	
 				    	
 				    	//spawnParticles(player.worldObj, entity, player);
				    	event.setCanceled(true);
 						
 					}
 				}
 			}
 		}
 
 	}
 	
 	public ItemStack compact(EntityLiving entity)
 	{
 		int id = EntityList.getEntityID(entity);
 		ItemStack holder = new ItemStack(CompactMobsItems.fullMobHolder, 1);
         NBTTagCompound nbttag = holder.stackTagCompound;
         if (nbttag == null) {
             nbttag = new NBTTagCompound();
         }
         nbttag.setInteger("entityId", id);
         
         if (entity instanceof EntityAgeable) {
             EntityAgeable entityAge = (EntityAgeable) entity;
             nbttag.setInteger("entityGrowingAge", entityAge.getGrowingAge());
         }
 
         if (entity instanceof EntitySheep) {
             EntitySheep entitySheep = (EntitySheep) entity;
             nbttag.setBoolean("entitySheared", entitySheep.getSheared());
             nbttag.setInteger("entityColor", entitySheep.getFleeceColor());
             
         }
      
        
         if (CompactMobsCore.instance.useFullTagCompound) {
         	NBTTagCompound entityTags = new NBTTagCompound();
         	
         	NBTTagCompound var2 = new NBTTagCompound();
             entity.writeToNBT(var2);
             
         	nbttag.setCompoundTag("entityTags", var2);
         	CompactMobsCore.instance.cmLog.info(var2.toString());
         }
     
         String name = entity.getEntityName();
         nbttag.setString("name", name);
         holder.setItemDamage(id);
 
        
 
         holder.setTagCompound(nbttag);
         entity.worldObj.removeEntity(entity);
 		return holder;
 	}
 	
 	public void spawnParticles(World world, EntityLiving entity, EntityPlayer player) {
 	    //CompactMobsCore.instance.cmLog.info("Got");
 	    double xv, yv, zv;
 	    double py = player.posY, px = player.posX, pz = player.posZ;
 	    double x = entity.posX, y = entity.posY, z = entity.posZ;
 	    if (py - y > 0) {
 	        yv = (py - y) / 10;
 	    } else {
 	        yv = 0;
 	    }
 	    if (px - x > 0) {
 	        xv = (px - x) / 10;
 	    } else if (px - x < 0) {
 	        xv = (px - x) / 10;
 	    } else {
 	        xv = 0;
 	    }
 	    if (pz - z > 0) {
 	        zv = (pz - z) / 10;
 	    } else if (pz - z < 0) {
 	        zv = (pz - z) / 10;
 	    } else {
 	        zv = 0;
 	    }
 	    CompactMobsCore.instance.proxy.spawnParticle("explode", x + .5D, y + .5D, z + .5D, xv, yv, zv, 10);
 	    //this.worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 	    //world.spawnParticle("smoke", x+.5D, y+.5D, z+.5D, xv, yv, zv);
 	}
 	
 
 }
