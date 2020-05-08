 package pixlepix.missioncontrol.common.helper;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.item.ItemExpireEvent;
 
 public class PacketEntityItem extends EntityItem {
 
 	public PacketEntityItem(World par1World, double par2, double par4,
 			double par6, ItemStack par8ItemStack) {
 		super(par1World, par2, par4, par6, par8ItemStack);
 	}
 	@Override
 	protected void doBlockCollisions(){
 		
 	}
 	public void onUpdate()
     {
 
 	ItemStack stack = this.getDataWatcher().getWatchableObjectItemStack(10);
     if (stack != null && stack.getItem() != null)
     {
         if (stack.getItem().onEntityItemUpdate(this))
         {
             return;
         }
     }
 
     this.worldObj.theProfiler.startSection("entityBaseTick");
 
     if (this.ridingEntity != null && this.ridingEntity.isDead)
     {
         this.ridingEntity = null;
     }
 
     this.prevDistanceWalkedModified = this.distanceWalkedModified;
     this.prevPosX = this.posX;
     this.prevPosY = this.posY;
     this.prevPosZ = this.posZ;
     this.prevRotationPitch = this.rotationPitch;
     this.prevRotationYaw = this.rotationYaw;
     int i;
 
     if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer)
     {
         this.worldObj.theProfiler.startSection("portal");
         MinecraftServer minecraftserver = ((WorldServer)this.worldObj).getMinecraftServer();
         
 
         this.worldObj.theProfiler.endSection();
     }
 
     if (this.isSprinting() && !this.isInWater())
     {
         int j = MathHelper.floor_double(this.posX);
         i = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double)this.yOffset);
         int k = MathHelper.floor_double(this.posZ);
         int l = this.worldObj.getBlockId(j, i, k);
 
         if (l > 0)
         {
             this.worldObj.spawnParticle("tilecrack_" + l + "_" + this.worldObj.getBlockMetadata(j, i, k), this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.boundingBox.minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D);
         }
     }
 
     this.handleWaterMovement();
 
    
     if (this.handleLavaMovement())
     {
         this.setOnFireFromLava();
         this.fallDistance *= 0.5F;
     }
 
     if (this.posY < -64.0D)
     {
         this.kill();
     }
 
    
     this.worldObj.theProfiler.endSection();
 
 
     
 
     this.prevPosX = this.posX;
     this.prevPosY = this.posY;
     this.prevPosZ = this.posZ;
     //this.noClip = this.pushOutOfBlocks(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
     this.moveEntity(this.motionX, this.motionY, this.motionZ);
     boolean flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;
 
     if (flag || this.ticksExisted % 25 == 0)
     {
         if (this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) == Material.lava)
         {
             this.motionY = 0.20000000298023224D;
             this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
             this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
             this.playSound("random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
         }
 
        
     }
 
     
 
     
     ++this.age;
 
     ItemStack item = getDataWatcher().getWatchableObjectItemStack(10);
 
     if (!this.worldObj.isRemote && this.age >= lifespan)
     {
         if (item != null)
         {   
             ItemExpireEvent event = new ItemExpireEvent(this, (item.getItem() == null ? 6000 : item.getItem().getEntityLifespan(item, worldObj)));
             if (MinecraftForge.EVENT_BUS.post(event))
             {
                 lifespan += event.extraLife;
             }
             else
             {
                 this.setDead();
             }
         }
         else
         {
             this.setDead();
         }
     }
 
     if (item != null && item.stackSize <= 0)
     {
         this.setDead();
     }
 }
 
 	
 }
