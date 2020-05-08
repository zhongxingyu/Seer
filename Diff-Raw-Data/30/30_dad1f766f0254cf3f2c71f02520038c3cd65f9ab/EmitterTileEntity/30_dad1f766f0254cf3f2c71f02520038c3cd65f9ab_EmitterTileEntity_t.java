 package pixlepix.particlephysics.common.tile;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.ForgeDirection;
 import pixlepix.particlephysics.common.api.BaseParticle;
 import pixlepix.particlephysics.common.entity.BlazepowderParticle;
 import pixlepix.particlephysics.common.entity.CharcoalParticle;
 import pixlepix.particlephysics.common.entity.ClayParticle;
 import pixlepix.particlephysics.common.entity.CoalParticle;
 import pixlepix.particlephysics.common.entity.GlassParticle;
 import pixlepix.particlephysics.common.entity.GunpowderParticle;
 import pixlepix.particlephysics.common.entity.LeafParticle;
 import pixlepix.particlephysics.common.entity.PaperParticle;
 import pixlepix.particlephysics.common.entity.SandParticle;
 import pixlepix.particlephysics.common.entity.SeedParticle;
 import pixlepix.particlephysics.common.helper.BasicComplexTileEntity;
 
 public class EmitterTileEntity extends BasicComplexTileEntity implements IInventory {
 
 	
 	public ItemStack inventory;
 	public static int[] validFuel={Item.coal.itemID,Item.paper.itemID,Item.clay.itemID, Item.seeds.itemID,Block.sand.blockID,Item.gunpowder.itemID,Block.glass.blockID, Item.blazePowder.itemID,Block.leaves.blockID};
 	@Override
 	public float getRequest(ForgeDirection direction) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public float getProvide(ForgeDirection direction) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public float getMaxEnergyStored() {
 		// TODO Auto-generated method stubnbt
 		return 0;
 	}
 	public int fuelType;
 
 	public int fuelMeta;
 	public int fuelStored=0;
 	public void updateEntity(){
 		if(!worldObj.isRemote&&worldObj.getTotalWorldTime()%40==0){
 			if(fuelStored<1){
 				if(this.inventory!=null){
 					if(isValidFuel(this.inventory.itemID)){
 						this.fuelStored=100;
 						this.fuelType=this.inventory.itemID;
 						this.fuelMeta=this.inventory.getItemDamage();
 						this.decrStackSize(0, 1);
 					}
 				}
 			}
 			if(fuelStored>0){
 				this.fuelStored--;
 				
 				ForgeDirection[] outputDirections={ForgeDirection.SOUTH,ForgeDirection.NORTH,ForgeDirection.WEST,ForgeDirection.EAST};
 				for(ForgeDirection dir:outputDirections){
 					
 					BaseParticle particle=getParticleFromFuel(fuelType,fuelMeta);
 					if(particle==null){
 						return;
 					}
 					particle.addVelocity(dir.offsetX, dir.offsetY, dir.offsetZ);
 					particle.setPosition(xCoord+dir.offsetX+0.375,yCoord+dir.offsetY+0.375,zCoord+dir.offsetZ+0.375);
 					worldObj.spawnEntityInWorld(particle);
 					
 				}
 			}
 		}
 	}
 	public BaseParticle getParticleFromFuel(int fuel,int meta){
 		if(fuel==Item.coal.itemID){
 			if(meta==1){
 				return new CharcoalParticle(worldObj);
 			}
 			return new CoalParticle(worldObj);
 		}
 		
 		if(fuel==Item.clay.itemID){
 			return new ClayParticle(worldObj);
 		}
 		if(fuel==Item.paper.itemID){
 			return new PaperParticle(worldObj);
 		}
 		if(fuel==Item.seeds.itemID){
 			return new SeedParticle(worldObj);
 		}
 		if(fuel==Block.sand.blockID){
 			return new SandParticle(worldObj);
 		}
 		if(fuel==Block.sand.blockID){
 			return new SandParticle(worldObj);
 		}
 		if(fuel==Item.gunpowder.itemID){
 			return new GunpowderParticle(worldObj);
 		}
 		if(fuel==Block.glass.blockID){
 			return new GlassParticle(worldObj);
 		}
 		if(fuel==Item.blazePowder.itemID){
 			return new BlazepowderParticle(worldObj);
 		}
 		if(fuel==Block.leaves.blockID){
 			return new LeafParticle(worldObj);
 		}
 		return null;
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt){
 		super.readFromNBT(nbt);
 		this.fuelStored=nbt.getInteger("Fuel");
 
 		this.fuelType=nbt.getInteger("FuelType");
 
 		this.fuelMeta=nbt.getInteger("FuelMeta");
 
 		this.inventory=ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Inventory"));
 	}
 	@Override
 	public void writeToNBT(NBTTagCompound nbt){
 		super.writeToNBT(nbt);
 		nbt.setInteger("Fuel", this.fuelStored);
 		nbt.setInteger("FuelType", this.fuelType);
 
 		nbt.setInteger("FuelMeta", this.fuelMeta);
 		NBTTagCompound inv=new NBTTagCompound();
 		if(this.inventory!=null){
 			this.inventory.writeToNBT(inv);
 		}
 		nbt.setCompoundTag("Inventory", inv);
 	}
 
 	@Override
 	public int getSizeInventory() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		// TODO Auto-generated method stub
 		return this.inventory;
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
		this.inventory.splitStack(j);
		if(this.inventory.stackSize == 0) {
			this.inventory = null;
		}
		return inventory;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemStack) {
 		this.inventory=itemStack;
 		
 	}
 
 	@Override
 	public String getInvName() {
 		// TODO Auto-generated method stub
 		return "Emitter";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		// TODO Auto-generated method stub
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void openChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void closeChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		
 		return isValidFuel(itemstack.itemID);
 	}
 
 	public boolean isValidFuel(int itemstack){
 		return this.getParticleFromFuel(itemstack,0)!=null;
 	}
 
 }
