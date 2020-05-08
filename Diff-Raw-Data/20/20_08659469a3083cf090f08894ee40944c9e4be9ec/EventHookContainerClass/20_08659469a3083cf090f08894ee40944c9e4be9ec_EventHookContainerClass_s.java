 package mods.learncraft.common;
 
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.player.EnumStatus;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.DamageSource;
 import net.minecraftforge.event.CommandEvent;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingSpawnEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
 
 public class EventHookContainerClass {
 	/**
 	* The key is the @ForgeSubscribe annotation and the cast of the Event you put in as argument.
 	* The method name you pick does not matter. Method signature is public void, always.
 	*/
 	@ForgeSubscribe
 	public void entityAttacked(LivingAttackEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 		}
 	}
 	
 	@ForgeSubscribe
 	public void livingDeathEvent(LivingDeathEvent event)
 	{
 		if(event.entityLiving instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			event.setCanceled(true);
 			player.setEntityHealth(20);
 			
 			// Drops all the glowstone dust, but nothing else!
 			if(player.inventory.hasItem(348)) {
 				for(int a=0; a<player.inventory.getSizeInventory(); a++) {
 					ItemStack slotitemstack = player.inventory.getStackInSlot(a);
 					if(slotitemstack != null) {
 						if(slotitemstack.itemID == 348) {
 							player.inventory.setInventorySlotContents(a, null);
 							player.dropItem(slotitemstack.itemID, slotitemstack.stackSize);
 						}
 					}
 				}
 			}
 			
 			Common.announce("Player "+player.username+" has died and was sent to the maze!");
 			Common.teleportPlayerTo(player, "maze_spawn");
 		}
 	}
 	
 	@ForgeSubscribe
 	public void playerInteractEvent(PlayerInteractEvent event) {
 
 		if(event.entityLiving instanceof EntityPlayer) {
 			
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			int blockid = player.worldObj.getBlockId(event.x, event.y, event.z);
 			int metadata = player.worldObj.getBlockMetadata(event.x, event.y, event.z);
 			
 			// Teleport to appropriate team area if touch mob head
 			if(blockid==144 && event.action.compareTo(event.action.RIGHT_CLICK_BLOCK)==0) {
 				if(Common.blueteam.hasPlayer(player)) {
 					Common.teleportPlayerTo(player, "blue_arena");
 				} else if(Common.goldteam.hasPlayer(player)) {
 					Common.teleportPlayerTo(player, "gold_arena");
 				}
 			}
 			
 			// Teleport and transfer to the gold team
 			if(blockid==519 && event.action.compareTo(event.action.RIGHT_CLICK_BLOCK)==0) {
 
 				if(Common.blueteam.hasPlayer(player)) {
 					// Don't do anything, this player shouldn't be touching this block
 				} else if(Common.goldteam.hasPlayer(player)) {
 					// Don't transfer to team, but just teleport to gold team
 					Common.teleportPlayerTo(player, "gold_spawn");
 				} else {
 					Common.goldteam.addPlayer(player);
 					Common.teleportPlayerTo(player, "gold_spawn");
 				}
 			}
 			
 			// Teleport and transfer to the blue team
 			if(blockid==520 && event.action.compareTo(event.action.RIGHT_CLICK_BLOCK)==0) {
 				if(Common.goldteam.hasPlayer(player)) {
 					// Don't do anything, this player shouldn't be touching this block
 				} else if(Common.blueteam.hasPlayer(player)) {
 					// Don't transfer to team, but just teleport to blue team
 					Common.teleportPlayerTo(player, "blue_spawn");
 				} else {
 					Common.blueteam.addPlayer(player);
 					Common.teleportPlayerTo(player, "blue_spawn");
 				}
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void livingSpecialSpawnEvent(LivingSpawnEvent event) 
 	{
 		if(event.entityLiving instanceof EntityPlayer) {
 		}
 	}
 
 	@ForgeSubscribe
 	public void entityJoinWorldEvent(EntityJoinWorldEvent event)
 	{
 		if(event.entity instanceof EntityPlayer) {
 			//System.out.println("Join World Event!");
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onSleepyTime(PlayerSleepInBedEvent event)
 	{
 		event.result = EnumStatus.NOT_POSSIBLE_NOW;
 	}
 }
