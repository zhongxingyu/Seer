 package craiggowing.mod.netmod;
 
 import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
 import net.minecraft.dispenser.IBlockSource;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.EnumFacing;
 import net.minecraft.util.MathHelper;
 
 public class BehaviorNetFullDispense extends BehaviorDefaultDispenseItem
 {
     final MinecraftServer mcServer;
 
     public BehaviorNetFullDispense(MinecraftServer par1)
     {
         this.mcServer = par1;
     }
 
     public ItemStack dispenseStack(IBlockSource par1IBlockSource, ItemStack par2ItemStack)
     {
         EnumFacing var3 = EnumFacing.getFront(par1IBlockSource.func_82620_h());
         double var4 = par1IBlockSource.getX() + (double)var3.getFrontOffsetX();
         double var6 = (double)((float)par1IBlockSource.getYInt() + 0.2F);
         double var8 = par1IBlockSource.getZ() + (double)var3.getFrontOffsetZ();
         ItemNetFull inf = (ItemNetFull)par2ItemStack.getItem();
         EntityLiving spawnEntity = inf.getEntityToSpawn(par2ItemStack, par1IBlockSource.getWorld());
         float yaw = 0.0f;
         if (var3.getFrontOffsetX() > 0)
         {
             yaw = 270.0f;
         }
         else if (var3.getFrontOffsetX() < 0)
         {
             yaw = 90.0f;
         }
         else if (var3.getFrontOffsetZ() > 0)
         {
             yaw = 0.0f;
         }
         else
         {
             yaw = 180.0f;
         }
         spawnEntity.rotationYaw = yaw;
         spawnEntity.setPositionAndRotation(var4, var6, var8, yaw, 0.0f);
         spawnEntity.posX -= (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * 0.16F);
         spawnEntity.posY -= 0.10000000149011612D;
         spawnEntity.posZ -= (double)(MathHelper.sin(yaw / 180.0F * (float)Math.PI) * 0.16F);
         spawnEntity.setPosition(spawnEntity.posX, spawnEntity.posY, spawnEntity.posZ);
         spawnEntity.yOffset = 0.0F;
         float var10 = 0.4F;
         spawnEntity.motionX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(0.0f / 180.0F * (float)Math.PI) * var10) * 2.0;
         spawnEntity.motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(0.0f / 180.0F * (float)Math.PI) * var10) * 2.0;
         spawnEntity.motionY = (double)(-MathHelper.sin((0.0f) / 180.0F * (float)Math.PI) * var10) * 2.0;
         if (par2ItemStack.getTagCompound() != null)
         {
             spawnEntity.readEntityFromNBT(par2ItemStack.getTagCompound());
         }
        else
        {
            spawnEntity.initCreature();
        }
         par1IBlockSource.getWorld().spawnEntityInWorld(spawnEntity);
         par2ItemStack.splitStack(1);
         return par2ItemStack;
     }
 }
