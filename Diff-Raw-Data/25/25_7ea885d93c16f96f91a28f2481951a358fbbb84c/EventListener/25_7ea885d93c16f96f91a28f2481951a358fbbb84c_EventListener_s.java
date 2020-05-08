 package net.runfast.frangiblebuttons;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItemFrame;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.EntityInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
 import net.runfast.frangiblebuttons.block.BlockSafetyButton;
 import net.runfast.frangiblebuttons.entity.EntityEmergencyItemFrame;
 
 public class EventListener {
 
     /**
      * This allows the player to create a BlockSafetyButton in-place by using a
      * vanilla glass pane on a vanilla button.
      */
     @ForgeSubscribe()
     public void onPlayerBlockInteract(PlayerInteractEvent event) {
         EntityPlayer player = event.entityPlayer;
         ItemStack hand = player.getHeldItem();
         if (hand != null && hand.itemID == Block.thinGlass.blockID
                 && event.action == Action.RIGHT_CLICK_BLOCK
                 && !player.isSneaking()) {
             World world = player.worldObj;
             if (world.getBlockId(event.x, event.y, event.z) == Block.stoneButton.blockID) {
                 // Trigger the button's update early, if one is pending.
                 // This is to avoid any headaches resulting from a pressed
                 // button inside the cover.
                 Block.stoneButton.updateTick(world, event.x, event.y, event.z,
                         world.rand);
                 // Replace the vanilla button with our block.
                 world.setBlock(event.x, event.y, event.z,
                         BlockSafetyButton.instance.blockID,
                         world.getBlockMetadata(event.x, event.y, event.z), 0x02);
                 EffectsManager.broadcastGlassReplaceEffect(world, event.x,
                         event.y, event.z);
                 if (!player.capabilities.isCreativeMode) {
                     hand.stackSize -= 1;
                 }
                 event.useBlock = Result.DENY;
             }
         }
     }
 
     /**
      * This allows the player to create an EmergencyItemFrame by using a vanilla
      * glass pane on a vanilla item frame.
      */
     @ForgeSubscribe()
     public void onPlayerEntityInteract(EntityInteractEvent event) {
         EntityPlayer player = event.entityPlayer;
         ItemStack hand = player.getHeldItem();
         if (hand != null && hand.itemID == Block.thinGlass.blockID
                 && !player.isSneaking()
                 && event.target instanceof EntityItemFrame
                 && !(event.target instanceof EntityEmergencyItemFrame)) {
             World world = player.worldObj;
             EntityItemFrame frame = (EntityItemFrame) event.target;
             if (!world.isRemote) {
                 world.spawnEntityInWorld(new EntityEmergencyItemFrame(frame));
                 world.removeEntity(frame);
                 world.playSoundEffect(frame.xPosition + 0.5,
                         frame.yPosition + 0.5, frame.zPosition + 0.5,
                         "step.stone", 1.0F, 0.8F);
             }
             if (!player.capabilities.isCreativeMode) {
                 hand.stackSize -= 1;
             }
             event.setCanceled(true);
         }
     }
 }
