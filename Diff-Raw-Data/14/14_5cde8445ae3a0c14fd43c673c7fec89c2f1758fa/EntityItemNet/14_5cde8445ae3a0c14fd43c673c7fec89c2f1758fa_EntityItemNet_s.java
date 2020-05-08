 package craiggowing.mod.netmod;
 
 import java.util.Random;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityThrowable;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class EntityItemNet extends EntityThrowable
 {
     public EntityItemNet(World par1World)
     {
         super(par1World);
     }
 
     public EntityItemNet(World par1World, EntityLiving par2EntityLiving)
     {
         super(par1World, par2EntityLiving);
     }
 
     public EntityItemNet(World par1World, double par2, double par4, double par6)
     {
         super(par1World);
         this.setPosition(par2, par4, par6);
         this.yOffset = 0.0F;
     }
 
     protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
     {
         int capturedAnimal = -1;
         EntityItem dropped = null;
         Random random = new Random();
 
         if (!this.worldObj.isRemote)
         {
             if (par1MovingObjectPosition.entityHit != null)
             {
                 par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0);
                 for (int x = 0; x < mod_NetMod.mobTotal; ++x)
                 {
                     if (par1MovingObjectPosition.entityHit.getClass() == mod_NetMod.itemClasses[x])
                     {
                        capturedAnimal = x;
                        break;
                     }
                 }
                 if (capturedAnimal > -1)
                 {
                     if (random.nextFloat() <= mod_NetMod.itemProbs[capturedAnimal][0])
                     {
                         dropped = this.entityDropItem(new ItemStack(mod_NetMod.itemNetFull.itemID, 1, capturedAnimal), 0.0F);
                     }
                 }
 
                 if (capturedAnimal > -1 && dropped != null)
                 {
                     NBTTagList tl = new NBTTagList();
                     NBTTagCompound nbt_el = new NBTTagCompound();
                     NBTTagCompound nbt_is;
                     if (dropped.getEntityItem().getTagCompound() != null)
                     {
                         nbt_is = (NBTTagCompound)dropped.getEntityItem().getTagCompound().copy();
                     }
                     else
                     {
                         nbt_is = new NBTTagCompound();
                     }
                     par1MovingObjectPosition.entityHit.writeToNBT(nbt_el);
                     tl.appendTag(nbt_el);
                     nbt_is.setTag("Creature", tl);
                     dropped.getEntityItem().setTagCompound(nbt_is);
                     par1MovingObjectPosition.entityHit.setDead();
                 }
                else if (capturedAnimal == -1 && par1MovingObjectPosition.entityHit instanceof EntityLiving)
                 {
                     EntityLiving eh = (EntityLiving)par1MovingObjectPosition.entityHit;
 
                     int var13 = MathHelper.floor_double((eh.posX * 2.0 + this.posX) / 3.0);
                     int var14 = MathHelper.floor_double((eh.posY + (eh.height / 2.0) + this.posY * 2.0) / 3.0);
                     int var15 = MathHelper.floor_double((eh.posZ * 2.0 + this.posZ) / 3.0);
 
                     if (this.worldObj.getBlockId(var13, var14, var15) == 0) // air
                     {
                         if (!this.worldObj.isRemote)
                         {
                             this.worldObj.setBlockAndMetadataWithNotify(var13, var14, var15, mod_NetMod.blockNet.blockID, 1);
                             capturedAnimal = 0;
                         }
                     }
                 }
             }
 
             if (capturedAnimal == -1)
             {
                 this.dropItem(mod_NetMod.itemNet.itemID, 1);
             }
             else if (capturedAnimal > -1 && dropped == null)
             {
                 if (random.nextFloat() >= mod_NetMod.itemProbs[capturedAnimal][1])
                 {
                     this.dropItem(mod_NetMod.itemNet.itemID, 1);
                 }
             }
         }
 
         for (int var5 = 0; var5 < 8; ++var5)
         {
             this.worldObj.spawnParticle("snowballpoof", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
         }
 
         if (!this.worldObj.isRemote)
         {
             this.setDead();
         }
     }
 
     public String getTexture()
     {
         return CommonProxy.ITEMS_PNG;
     }
 }
