 package pixlepix.missioncontrol.common.tile;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 
 public class EnderInventoryLinkTileEntity extends InventoryLinkTileEntity implements IInventory {
 	public String player;
 	@Override
 	public int getSizeInventory() {
 		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)){
 			return 0;
 		}
		return 31;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		if(player==null){
 			return null;
 		}
 		
 		EntityPlayer entityPlayer=worldObj.getPlayerEntityByName(player);
 		if(entityPlayer==null){
 			return null;
 		}
 		return entityPlayer.getInventoryEnderChest().getStackInSlot(i);
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 
 		animatePacket();
 		if(player==null){
 			return null;
 		}
 		
 		EntityPlayer entityPlayer=worldObj.getPlayerEntityByName(player);
 		if(entityPlayer==null){
 			return null;
 		}
 
 		return entityPlayer.getInventoryEnderChest().decrStackSize(i, j);
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 
 		animatePacket();
 		if(player==null){
 			return;
 		}
 		
 		EntityPlayer entityPlayer=worldObj.getPlayerEntityByName(player);
 		if(entityPlayer==null){
 			return;
 		}
 		entityPlayer.getInventoryEnderChest().setInventorySlotContents(i, itemstack);
 	}
 
 	@Override
 	public String getInvName() {
 		// TODO Auto-generated method stub
 		return "Inventory Link";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public void onInventoryChanged() {
 		if(player==null){
 			return;
 		}
 		
 		EntityPlayer entityPlayer=worldObj.getPlayerEntityByName(player);
 		if(entityPlayer==null){
 			return;
 		}
 		entityPlayer.getInventoryEnderChest().onInventoryChanged();
 		
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
 		return true;
 	}
 
 }
