 package assets.fyresmodjam;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityTrap extends TileEntity {
 	
 	//public int type = -1;
 	public String placedBy = null;
 	
     public TileEntityTrap() {}
 
     /*public void updateEntity() {
     	super.updateEntity();
     }*/
     
     public void updateEntity() {
     	super.updateEntity();
     	
     	if(worldObj.isRemote) {spawnParticles();}
     }
     
     public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
     	super.writeToNBT(par1NBTTagCompound);
     	
     	//if(type == -1) {type = ModjamMod.r.nextInt(4);}
     	//par1NBTTagCompound.setInteger("TrapType", type);
     	
     	if(placedBy != null) {par1NBTTagCompound.setString("PlacedBy", placedBy);}
     }
 
     public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
     	super.readFromNBT(par1NBTTagCompound);
     	//type = par1NBTTagCompound.getInteger("TrapType");
     	
     	if(par1NBTTagCompound.hasKey("PlacedBy")) {placedBy = par1NBTTagCompound.getString("PlacedBy");}
     }
 
     public Packet getDescriptionPacket() {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
     }
 
     public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {this.readFromNBT(packet.customParam1);}
     
     @SideOnly(Side.CLIENT)
     public AxisAlignedBB getRenderBoundingBox() {
     	//fixes rendering bug, but on downside, always renders, even off screen
         return INFINITE_EXTENT_AABB;
     }
     
     @SideOnly(Side.CLIENT)
     public double getMaxRenderDistanceSquared() {
     	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
     	//EntityPlayer player = Minecraft.getMinecraft().thePlayer;
         return (player != null && player.getEntityName().equals(placedBy)) ? 4096.0F: 36.0F; //(player != null && player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals("Scout")) ? 16.0D : 36.0D;
     }
     
     @SideOnly(Side.CLIENT)
     public void spawnParticles() {
     	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
 		int type = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
 		
		if(player != null && !PacketHandler.trapsDisabled && (player.getEntityName().equals(placedBy) || player.isSneaking() || (player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals("Scout"))) && this.getDistanceFrom(TileEntityRenderer.staticPlayerX, TileEntityRenderer.staticPlayerY, TileEntityRenderer.staticPlayerZ) < (player.getEntityName().equals(placedBy) ? 4096 : 36.0F)) {
 			if(type == 1) {
 				if(ModjamMod.r.nextInt(5) == 0) {this.worldObj.spawnParticle("smoke", this.xCoord + 0.5F, this.yCoord + 0.175F, this.zCoord + 0.5F, (ModjamMod.r.nextFloat() - 0.5F)/16, ModjamMod.r.nextFloat()/16, (ModjamMod.r.nextFloat() - 0.5F)/16);}
 				this.worldObj.spawnParticle("flame", this.xCoord + 0.5F, this.yCoord + 0.175F, this.zCoord + 0.5F, 0.0F, 0.0F, 0.0F);
 			} else if(type == 2) {
 				for(int i = 0; i < 3; i++) {this.worldObj.spawnParticle("smoke", this.xCoord + 0.5F, this.yCoord + 0.175F, this.zCoord + 0.5F, (ModjamMod.r.nextFloat() - 0.5F)/16, ModjamMod.r.nextFloat()/16, (ModjamMod.r.nextFloat() - 0.5F)/16);}
 			}
 		}
     }
 }
