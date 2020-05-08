 package enosphorous.chateau_romani.handlers;
 
 import enosphorous.chateau_romani.common.Items;
 import net.minecraft.entity.monster.EntityGhast;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.EntityInteractEvent;
 
 public class GhastHandler
 {
 @ForgeSubscribe
 public void onEntityInteract(EntityInteractEvent event)
 {
 if (event.entity instanceof EntityPlayer)
 {
 EntityPlayer player = (EntityPlayer) event.entity;
 if (event.target != null && event.target instanceof EntityGhast && player.getHeldItem() != null && player.getHeldItem().itemID == Item.glassBottle.itemID)
 {
        player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Items.bottled_ghast));
        event.target.setInvisible(true);
        event.target.setPosition(event.target.posX, event.target.posY - 300, event.target.posZ);
        event.target.attackEntityFrom(DamageSource.generic, 800F);
        event.target.setDead();
        player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Items.bottled_ghast));
        
        /**
         * VERY buggy, still.
         * Teleports Ghast far away, and then kills it, so as it destroy the drops.
         * Issues with out-of-bounds errors may occur. . .
         * Bottled Ghast still iffy - sometimes kills Ghast and still gives empty bottle.
         */
 }
 }
 }
 }
