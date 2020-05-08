 package monnef.core.utils;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 public class PlayerHelper {
     public static void damageCurrentItem(EntityPlayer player) {
         if (player.worldObj.isRemote) return;
         ItemStack handItem = player.getCurrentEquippedItem();
         if (handItem == null) return;
         handItem.damageItem(1, player);
     }
 
     public static void giveItemToPlayer(EntityPlayer player, ItemStack item) {
         World world = player.worldObj;
         if (item == null || item.stackSize <= 0 || world.isRemote) return;
         Entity entity = new EntityItem(world, player.posX, player.posY + 0.5, player.posZ, item.copy());
         world.spawnEntityInWorld(entity);
     }
 
     public static boolean PlayerHasEquipped(EntityPlayer player, int itemId) {
         if (player == null) return false;
         ItemStack equippedItem = player.getCurrentEquippedItem();
         if (equippedItem == null) return false;
         return equippedItem.itemID == itemId;
     }
 
     public static MovingObjectPosition rayTrace(EntityLiving entity, double distance) {
 /**
  Vec3 vec3 = this.getPosition(par3);
  Vec3 vec31 = this.getLook(par3);
  Vec3 vec32 = vec3.addVector(vec31.xCoord * par1, vec31.yCoord * par1, vec31.zCoord * par1);
  */
 
        Vec3 pos = entity.worldObj.getWorldVec3Pool().getVecFromPool(entity.posX, entity.posY + 1.62D - entity.yOffset, entity.posZ);
         Vec3 look = entity.getLookVec();
         Vec3 target = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
         return entity.worldObj.rayTraceBlocks(pos, target);
     }
 }
