 package xolova.blued00r.divinerpg.entities.vethea;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityThrowable;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class EntityDisk extends EntityThrowable
 {
     public int damage;
     public int counter;
     public boolean rebound;
     public Item item;
     public int icon;
     public int collideCounter;
     public EntityLiving thrower;
 
     public EntityDisk(World var1)
     {
         super(var1);
     }
 
     public EntityDisk(World var1, EntityLiving var2, int var3, Item var4)
     {
         super(var1, var2);
         this.damage = var3;
         this.counter = 30;
         this.thrower = var2;
         this.collideCounter = 0;
         this.rebound = false;
         this.item = var4;
     }
 
     public EntityDisk(World var1, double var2, double var4, double var6)
     {
         super(var1, var2, var4, var6);
     }
 
     /**
      * Called to update the entity's position/logic.
      */
     public void onUpdate()
     {
         super.onUpdate();
        setVelocity(this.motionX / 0.99D, this.motionY / 0.99D, this.motionZ / 0.99D);
 
         if (this.counter == 0 && !this.rebound && this.thrower != null && this.collideCounter == 0)
         {
            setVelocity(-this.motionX, -this.motionY, -this.motionZ);
             this.rebound = true;
             this.collideCounter = 5;
         }
         else if (this.counter > 0)
         {
             --this.counter;
         }
         else if (this.collideCounter > 0)
         {
             --this.collideCounter;
         }
     }
    
    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double par1, double par3, double par5)
    {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)var7) * 180.0D / Math.PI);
        }
    }
 
     /**
      * Called when this EntityThrowable hits a block or entity.
      */
     public void onImpact(MovingObjectPosition var1)
     {
         if (this.thrower != null)
         {
             if (var1.entityHit != null && var1.entityHit != this.thrower)
             {
                 var1.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.thrower), this.damage);
             }
             else if (var1.entityHit == this.thrower && this.thrower instanceof EntityPlayer && this.rebound)
             {
                 if (!((EntityPlayer)this.thrower).capabilities.isCreativeMode)
                 {
                     ((EntityPlayer)this.thrower).inventory.addItemStackToInventory(new ItemStack(this.item));
                 }
 
                 if (!this.worldObj.isRemote)
                 {
                     this.setDead();
                 }
             }
 
             if (this.collideCounter == 0)
             {
                 this.rebound = false;
                 this.counter = 0;
             }
         }
         else if (!this.worldObj.isRemote)
         {
             this.setDead();
         }
     }
 
     /**
      * Gets the amount of gravity to apply to the thrown entity with each tick.
      */
     public float getGravityVelocity()
     {
         return 0.0F;
     }
 }
