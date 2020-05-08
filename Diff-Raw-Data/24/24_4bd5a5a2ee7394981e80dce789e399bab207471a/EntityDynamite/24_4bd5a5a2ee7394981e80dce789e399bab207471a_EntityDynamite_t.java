 package flaxbeard.dynamite.entity;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.util.List;
 
 import flaxbeard.dynamite.DynamiteMod;
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.IProjectile;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityBlaze;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityThrowable;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 public class EntityDynamite extends EntityThrowable
 {
 	boolean stopped = false;
 	boolean bounce = false;
 	public int type = 0;
 	int fuse;
 	
 	public EntityDynamite(World par1World) {
 		super(par1World);
 		
 	}
 	public EntityDynamite(World par1World, double x, double y, double z, float yaw, float pitch, double force, int fuseLength, int ty) {
 		super(par1World);
 		this.setRotation(yaw, 0);
 		System.out.println(yaw);
 		System.out.println(pitch);
         double xHeading = -MathHelper.sin((yaw * 3.141593F) / 180F);
         double zHeading = MathHelper.cos((yaw * 3.141593F) / 180F);
         motionX = force*xHeading*MathHelper.cos((pitch / 180F) * 3.141593F);
         motionY = -force*MathHelper.sin((pitch / 180F) * 3.141593F);
         motionZ = force*zHeading*MathHelper.cos((pitch / 180F) * 3.141593F);
 
         setPosition(x+xHeading*0.8, y+1.5, z+zHeading*0.8);
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
  
         fuse = fuseLength;
         type = ty;
         System.out.println(type);
 	}
 	
 	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
 	{
 		return false;
 	}
 	
     public EntityDynamite(World world, Entity entity, double force, int ty)
     {
         this(world, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch, force, 50, ty);
     }
     
     public void onUpdate()
     {
         if(fuse-- <= 0)
         {
         	if (!worldObj.isRemote) {
         	
         	worldObj.createExplosion(null, posX, posY, posZ, 2.0F, true);
             this.setDead();
         	}
             
         }
        if(stopped && !worldObj.isRemote) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(7);
            DataOutputStream outputStream = new DataOutputStream(bos);
            try
            {
                outputStream.writeByte(1);
                outputStream.writeInt(this.entityId);
                outputStream.writeInt((int) posX);
                outputStream.writeInt((int) posY);
                outputStream.writeInt((int) posZ);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "DynamiteMod";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
            PacketDispatcher.sendPacketToAllAround(posX, posY, posZ, 50, worldObj.provider.dimensionId, packet);
        }
         if(!(stopped))
         {
                 double prevVelX = motionX;
                 double prevVelY = motionY;
                 double prevVelZ = motionZ;
                 prevPosX = posX;
                 prevPosY = posY;
                 prevPosZ = posZ;
                 moveEntity(motionX, motionY, motionZ);
          
                 boolean collided = false;
                         
                 if(motionX!=prevVelX)
                 {
                 		if (type == 1) {
                 			stopped = true;
                             motionX = 0;
                             motionY = 0;
                             motionZ = 0;
                 		}
                 		else
                 		{
                         motionX = -0.5*prevVelX;
                         collided = true;
                 		}
                 }
                 if(motionZ!=prevVelZ)
                 {
 	            		if (type == 1) {
 	            			stopped = true;
 	                        motionX = 0;
 	                        motionY = 0;
 	                        motionZ = 0;
 	            		}
 	            		else
 	            		{
                         motionZ = -0.5*prevVelZ;
 	            		}
                 }
          
                 if(motionY!=prevVelY)
                 {
 	            		if (type == 1) {
 	            			stopped = true;
 	                        motionX = 0;
 	                        motionY = 0;
 	                        motionZ = 0;
 	            		}
 	            		else
 	            		{
                         motionY = -prevVelY;
                         collided = true;   
 	            		}
                 }
                 else
                 {
                         motionY -= 0.04;
                 }
          
                 if(collided)
                 {
 						motionX *= 0.5;
                         motionY *= 0.1;
                         motionZ *= 0.5;
                 }
          
                 motionX *= 0.99;
                 motionY *= 0.99;
                 motionZ *= 0.99;
                 System.out.println(Boolean.toString(worldObj.isRemote) + Integer.toString(entityId));
                 if(onGround && (motionX*motionX+motionY*motionY+motionZ*motionZ)<0.02)
                 {
                         stopped = true;
                         motionX = 0;
                         motionY = 0;
                         motionZ = 0;
                 }
         }
     }
 
 	@Override
 	protected void onImpact(MovingObjectPosition movingobjectposition) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void stop(int x, int y, int z) {
 		stopped = true;
 		this.posX = x;
 		this.posY = y;
 		this.posZ = z;
 		System.out.println("i trai to stop but i fal.");
 	}
 
 	
   
 }
