 package com.qzx.au.extras;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBasePressurePlate;
 import net.minecraft.block.BlockButton;
 import net.minecraft.block.BlockLever;
 import net.minecraft.block.BlockRailBase;
 import net.minecraft.block.BlockSign;
 import net.minecraft.block.BlockTorch;
 import net.minecraft.block.BlockTripWire;
 #ifdef MC152
 import net.minecraft.entity.EntityLiving;
 #else
 import net.minecraft.entity.EntityLivingBase;
 #endif
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeDirection;
 
 import java.util.List;
 import java.util.Random;
 
 import com.qzx.au.core.PacketUtils;
 import com.qzx.au.core.RenderUtils;
 import com.qzx.au.core.SidedBlockInfo;
 import com.qzx.au.core.SidedSlotInfo;
 import com.qzx.au.core.TileEntityAU;
 
 public class TileEntityEnderCube extends TileEntityAU {
 	private byte playerDirection; // NBT
 	private byte teleportDirection; // NBT
 	private boolean playerControl; // NBT
 	private boolean playerRedstoneControl; // NBT
 	private boolean redstoneControl; // NBT
 	private boolean isPowered; // NBT
 	private String playerControlWhitelist; // NBT
 
 	public TileEntityEnderCube(){
 		super();
 
 		this.playerDirection = 0;
 		this.teleportDirection = 0;
 		this.playerControl = false;
 		this.playerRedstoneControl = false;
 		this.redstoneControl = false;
 		this.isPowered = false;
 		this.playerControlWhitelist = null;
 	}
 
 	//////////
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt){
 		super.readFromNBT(nbt);
 
 		short cfg = nbt.getShort("_cfg");
 
 		this.playerDirection = (byte)(cfg & 0x3);			// 0000 0000 0011
 		this.teleportDirection = (byte)((cfg>>2) & 0x7);	// 0000 0001 1100
 		this.playerControl = (cfg & 0x20) != 0;				// 0000 0010 0000
 		this.playerRedstoneControl = (cfg & 0x40) != 0;		// 0000 0100 0000
 		this.redstoneControl = (cfg & 0x80) != 0;			// 0000 1000 0000
 		this.isPowered = (cfg & 0x100) != 0;				// 0001 0000 0000
 
 		if(this.redstoneControl) this.playerControl = false; // can't use both
 		if(!this.playerControl) this.playerRedstoneControl = false;
 
 		String pcl = nbt.getString("_pcl");
 		if(pcl != null){
 			pcl = pcl.trim();
 			if(pcl.equals("")) pcl = null;
 		}
 
 		if(pcl != null)
 			this.playerControlWhitelist = pcl;
 		else
 			this.playerControlWhitelist = null;
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt){
 		super.writeToNBT(nbt);
 
 		short cfg = (short)(this.playerDirection | (this.teleportDirection<<2));
 		if(this.playerControl) cfg |= 0x20;
 		if(this.playerRedstoneControl) cfg |= 0x40;
 		if(this.redstoneControl) cfg |= 0x80;
 		if(this.isPowered) cfg |= 0x100;
 
 		nbt.setShort("_cfg", cfg);
 
 		if(this.playerControlWhitelist != null)
 			nbt.setString("_pcl", this.playerControlWhitelist);
 	}
 
 	//////////
 
 	@Override
 	public ContainerEnderCube getContainer(InventoryPlayer inventory){
 		return new ContainerEnderCube(inventory, this);
 	}
 
 	//////////
 
 	@Override
 	public boolean canRotate(){
 		return false;
 	}
 
 	@Override
 	public boolean canCamo(){
 		return true;
 	}
 
 	@Override
 	public boolean canOwn(){
 		return false;
 	}
 
 	//////////
 
 	public EnderButton getPlayerDirection(){
 		return EnderButton.values()[EnderButton.BUTTON_PLAYER_UD.ordinal() + this.playerDirection];
 	}
 	public void setPlayerDirection(EnderButton button){
 		this.playerDirection = (byte)(button.ordinal() - EnderButton.BUTTON_PLAYER_UD.ordinal());
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_PLAYER_DIRECTION,
 									this.xCoord, this.yCoord, this.zCoord, this.playerDirection);
 		this.markChunkModified();
 	}
 	public void setPlayerDirection(byte button){
 		// client
 		this.playerDirection = button;
 	}
 
 	public EnderButton getTeleportDirection(){
 		return EnderButton.values()[EnderButton.BUTTON_DOWN.ordinal() + this.teleportDirection];
 	}
 	public void setTeleportDirection(EnderButton button){
 		this.teleportDirection = (byte)(button.ordinal() - EnderButton.BUTTON_DOWN.ordinal());
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_TELEPORT_DIRECTION,
 									this.xCoord, this.yCoord, this.zCoord, this.teleportDirection);
 		this.markChunkModified();
 	}
 	public void setTeleportDirection(byte button){
 		// client
 		this.teleportDirection = button;
 	}
 
 	public boolean getPlayerControl(){
 		return this.playerControl;
 	}
 	public void togglePlayerControl(){
 		this.playerControl = this.playerControl ? false : true;
 		if(this.playerControl) this.redstoneControl = false; // can't use both
 		else this.playerRedstoneControl = false;
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_PLAYER_CONTROL,
 									this.xCoord, this.yCoord, this.zCoord, this.playerControl);
 		this.markChunkModified();
 	}
 	public void setPlayerControl(boolean enable){
 		// client
 		this.playerControl = enable;
 		if(this.playerControl) this.redstoneControl = false; // can't use both
 		else this.playerRedstoneControl = false;
 	}
 
 	public boolean getPlayerRedstoneControl(){
 		return this.playerRedstoneControl;
 	}
 	public void togglePlayerRedstoneControl(){
 		this.playerRedstoneControl = this.playerRedstoneControl ? false : true;
 		if(!this.playerControl) this.playerRedstoneControl = false;
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_PLAYER_RS_CONTROL,
 									this.xCoord, this.yCoord, this.zCoord, this.playerRedstoneControl);
 		this.markChunkModified();
 	}
 	public void setPlayerRedstoneControl(boolean enable){
 		// client
 		this.playerRedstoneControl = enable;
 		if(!this.playerControl) this.playerRedstoneControl = false;
 	}
 
 	public boolean getRedstoneControl(){
 		return this.redstoneControl;
 	}
 	public void toggleRedstoneControl(){
 		this.redstoneControl = this.redstoneControl ? false : true;
 		if(this.redstoneControl){
 			this.playerControl = false; // can't use both
 			this.playerRedstoneControl = false;
 		}
 
 		PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_REDSTONE_CONTROL,
 									this.xCoord, this.yCoord, this.zCoord, this.redstoneControl);
 		this.markChunkModified();
 	}
 	public void setRedstoneControl(boolean enable){
 		// client
 		this.redstoneControl = enable;
 		if(this.redstoneControl){
 			this.playerControl = false; // can't use both
 			this.playerRedstoneControl = false;
 		}
 	}
 
 	public boolean isPowered(){
 		return this.isPowered;
 	}
 	public void setPowered(boolean isPowered){
 		// not sent to client
 		this.isPowered = isPowered;
 
 		this.markChunkModified();
 	}
 
 	public String getPlayerControlWhitelist(){
 		return this.playerControlWhitelist == null ? "" : this.playerControlWhitelist;
 	}
 	public void setPlayerControlWhitelist(String pcl, boolean server){
 		if(pcl != null){
 			pcl = pcl.trim();
 			if(pcl.equals("")) pcl = null;
 		}
 		if(pcl == null && this.playerControlWhitelist == null) return;
 		if(pcl != null && this.playerControlWhitelist != null && pcl.equals(this.playerControlWhitelist)) return;
 
 		this.playerControlWhitelist = pcl;
 
 		if(server){
 			PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SET_PCL,
 										this.xCoord, this.yCoord, this.zCoord, this.playerControlWhitelist == null ? "" : this.playerControlWhitelist);
 			this.markChunkModified();
 		} else
 			GuiEnderCube.update_pcl = true;
 	}
 
 	public void spawnParticles(int direction_coord){
 		// display particles above both ender cubes
 		Random random = new Random();
 		RenderUtils.spawnParticles(this.worldObj, (float)this.xCoord + 0.5F, (float)this.yCoord + 2.0F, (float)this.zCoord + 0.5F,
 									random, BlockEnderCube.nrPortalParticles, "portal", 1.0F, 2.0F, 1.0F);
 		if((this.playerControl && this.playerDirection == 0) || (this.redstoneControl && (this.teleportDirection == 0 || this.teleportDirection == 1)))
 			// up/down
 			RenderUtils.spawnParticles(this.worldObj, (float)this.xCoord + 0.5F, (float)direction_coord + 2.0F, (float)this.zCoord + 0.5F,
 										random, BlockEnderCube.nrPortalParticles, "portal", 1.0F, 2.0F, 1.0F);
 		else if((this.playerControl && this.playerDirection == 1) || (this.redstoneControl && (this.teleportDirection == 2 || this.teleportDirection == 3)))
 			// north/south
 			RenderUtils.spawnParticles(this.worldObj, (float)this.xCoord + 0.5F, (float)this.yCoord + 2.0F, (float)direction_coord + 0.5F,
 										random, BlockEnderCube.nrPortalParticles, "portal", 1.0F, 2.0F, 1.0F);
 		else
 			// east/west
 			RenderUtils.spawnParticles(this.worldObj, (float)direction_coord + 0.5F, (float)this.yCoord + 2.0F, (float)this.zCoord + 0.5F,
 										random, BlockEnderCube.nrPortalParticles, "portal", 1.0F, 2.0F, 1.0F);
 	}
 
 	@Override
 	public boolean canUpdate(){
 		return false;
 	}
 
 	//////////
 
 	private class AABB extends AxisAlignedBB {
 		public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
 			super(minX, minY, minZ, maxX, maxY, maxZ);
 		}
 	}
 
 	public void teleportAll(){
 		if(this.redstoneControl == false) return;
 
 		int x = this.xCoord, y = this.yCoord, z = this.zCoord, direction_coord = 0;
 		boolean found_match = false;
 
 		// scan for nearby ender cube
 		int delta = (this.teleportDirection % 2 == 1 ? 1 : -1);
 		if(this.teleportDirection == 0 || this.teleportDirection == 1){
 			// scan up/down
 			y += delta;
 			int limit = y + (Cfg.enderCubeDistance * delta);
 			if(limit < 1) limit = 1; else if(limit > 255) limit = 255;
 			for(; y != limit; y += delta){
 				if(this.worldObj.getBlockId(x, y, z) == AUExtras.blockEnderCube.blockID){
 					direction_coord = y;
 					found_match = true;
 					break;
 				}
 			}
 		} else if(this.teleportDirection == 2 || this.teleportDirection == 3){
 			// scan north/south
 			z += delta;
 			int limit = z + (Cfg.enderCubeDistance * delta);
 			for(; z != limit; z += delta){
 				if(this.worldObj.getBlockId(x, y, z) == AUExtras.blockEnderCube.blockID){
 					direction_coord = z;
 					found_match = true;
 					break;
 				}
 			}
 		} else {
 			// scan east/west
 			x += delta;
 			int limit = x + (Cfg.enderCubeDistance * delta);
 			for(; x != limit; x += delta){
 				if(this.worldObj.getBlockId(x, y, z) == AUExtras.blockEnderCube.blockID){
 					direction_coord = x;
 					found_match = true;
 					break;
 				}
 			}
 		}
 
 		if(found_match && !this.isObstructed(this.worldObj, x, y, z)){
 			// get all entities above source cube
 			#ifdef MC152
 			List<EntityLiving> entities = this.worldObj.getEntitiesWithinAABB(EntityLiving.class,
 			#else
 			List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
 			#endif
 					(AxisAlignedBB)(new AABB((double)this.xCoord, (double)this.yCoord + 1.0D, (double)this.zCoord,
 									(double)this.xCoord + 1.0D, (double)this.yCoord + 3.0D, (double)this.zCoord + 1.0D)));
 
 			// teleport all entities to destination cube
 			int nr_entities = entities.size();
 			for(int i = 0; i < nr_entities; i++)
 				this._teleportEntity(this.worldObj, x, y, z, entities.get(i), direction_coord, (i == 0));
 		}
 	}
 
 	public boolean canPlayerControl(String playerName){
 		if(this.playerControlWhitelist == null) return true;
 
 		String[] players = this.playerControlWhitelist.split(",");
 		for(int i = 0; i < players.length; i++)
 			if(players[i].trim().equals(playerName)) return true;
 		return false;
 	}
 
 	public void teleportPlayer(EntityPlayer player, boolean teleport_up){
 		if(this.playerControl == false || (this.playerRedstoneControl == true && this.isPowered == false)) return;
 
 		if(!canPlayerControl(player.getEntityName())) return;
 
 		// scan for nearby ender cube
 		int delta = (teleport_up ? 1 : -1);
 		if(this.playerDirection == 0){
 			// scan up/down
 			int y = this.yCoord + delta;
 			int limit = y + (Cfg.enderCubeDistance * delta);
			if(limit < 0) limit = 0; else if(limit > 255) limit = 255;
 			for(; y != limit; y += delta){
 				if(this.worldObj.getBlockId(this.xCoord, y, this.zCoord) == AUExtras.blockEnderCube.blockID){
 					if(!this.isObstructed(this.worldObj, this.xCoord, y, this.zCoord))
 						this._teleportEntity(this.worldObj, this.xCoord, y, this.zCoord, player, y, true);
 					break;
 				}
 			}
 		} else if(this.playerDirection == 1){
 			// scan north/south
 			int z = this.zCoord + delta;
 			int limit = z + (Cfg.enderCubeDistance * delta);
 			for(; z != limit; z += delta){
 				if(this.worldObj.getBlockId(this.xCoord, this.yCoord, z) == AUExtras.blockEnderCube.blockID){
 					if(!this.isObstructed(this.worldObj, this.xCoord, this.yCoord, z))
 						this._teleportEntity(this.worldObj, this.xCoord, this.yCoord, z, player, z, true);
 					break;
 				}
 			}
 		} else {
 			// scan east/west
 			int x = this.xCoord + delta;
 			int limit = x + (Cfg.enderCubeDistance * delta);
 			for(; x != limit; x += delta){
 				if(this.worldObj.getBlockId(x, this.yCoord, this.zCoord) == AUExtras.blockEnderCube.blockID){
 					if(!this.isObstructed(this.worldObj, x, this.yCoord, this.zCoord))
 						this._teleportEntity(this.worldObj, x, this.yCoord, this.zCoord, player, x, true);
 					break;
 				}
 			}
 		}
 	}
 
 	#ifdef MC152
 	private void _teleportEntity(World world, int x, int y, int z, EntityLiving entity, int direction_coord, boolean spawn_particles){
 	#else
 	private void _teleportEntity(World world, int x, int y, int z, EntityLivingBase entity, int direction_coord, boolean spawn_particles){
 	#endif
 		world.playSoundAtEntity(entity, "mob.endermen.portal", 1.0F, 1.0F);
 		entity.setPositionAndUpdate(x + 0.5F, y + 1.1F, z + 0.5F);
 		world.playSoundAtEntity(entity, "mob.endermen.portal", 1.0F, 1.0F);
 
 		if(spawn_particles)
 			PacketUtils.sendToAllAround(this.worldObj, PacketUtils.MAX_RANGE, AUExtras.packetChannel, Packets.CLIENT_ENDER_SPAWN_PARTICLES,
 										this.xCoord, this.yCoord, this.zCoord, direction_coord);
 	}
 
 	public boolean isObstructed(World world, int x, int y, int z){
 		if(world.isAirBlock(x, y+1, z) && world.isAirBlock(x, y+2, z)) return false; // 2 air blocks
 		Block block = Block.blocksList[world.getBlockId(x, y+1, z)];
 		if(!this.checkBlockForObstructions(block)){
 			block = Block.blocksList[world.getBlockId(x, y+2, z)];
 			return this.checkBlockForObstructions(block);
 		}
 		return true;
 	}
 	private boolean checkBlockForObstructions(Block block){
 		if(block == null) return false;
 		if(block instanceof BlockBasePressurePlate) return false;
 		if(block instanceof BlockButton) return false;
 		if(block instanceof BlockLever) return false;
 		if(block instanceof BlockRailBase) return false;
 		if(block instanceof BlockSign) return false;
 		if(block instanceof BlockTorch) return false;
 		if(block instanceof BlockTripWire) return false;
 		return true;
 	}
 
 	//////////
 
 	@Override
 	public String getInvName(){
 		return "au.tileentity.EnderCube";
 	}
 }
