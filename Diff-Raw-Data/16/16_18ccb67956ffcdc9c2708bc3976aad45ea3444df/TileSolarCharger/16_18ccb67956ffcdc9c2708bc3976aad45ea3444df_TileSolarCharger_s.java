 package sbfp.machines.tiles;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import sbfp.PacketHandler;
 import sbfp.modsbfp;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.FMLLog;
 
 public class TileSolarCharger extends TileProcessor implements IInventory{
 
 	private ItemStack[] inventory = new ItemStack[8];
 	private boolean hasItem = false;
	public static final int maxWorkTicks = 10*20; //45 Seconds to make 1 piece of charged redstone
 	@Override
 	public void updateEntity(){
 		super.updateEntity();
 		if(!this.hasItem && this.canWork()){
 			this.hasItem = true;
 			FMLLog.info("Working now");
 			for(int i = 0; i<4;i++){
 				if(this.inventory[i] != null){
 					this.inventory[i].stackSize--;
 					if(this.inventory[i].stackSize <= 0) this.inventory[i] = null;
 					break;
 				}
 			}
		}else if(this.canWork() && this.hasItem){
			
 		}
 		
 	}
 
 	private boolean canWork(){
 		
 		if((this.inventory[0] == null && this.inventory[1] == null && this.inventory[2] == null && this.inventory[3] == null)){
 			if(this.hasItem) return true;
 			return false;
 		}
 		if(this.inventory[4] != null){
 			if(this.inventory[4].stackSize+1>this.inventory[4].getMaxStackSize()) return false;
 		}
 		//if(!this.worldObj.isDaytime()) return false;
 		//if(!(this.worldObj.canBlockSeeTheSky(this.xCoord,this.yCoord+1,this.zCoord))) return false;
 		
 		return true;
 		
 	}
 
 	public int getWorkTicks(){
 		return this.workTicks;
 	}
 
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		return PacketHandler.getPacket(modsbfp.modid, this, this.workTicks);
 	}
 	
 	@Override
 	public void handleData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
 	{
 		try
 		{
 			if (this.worldObj.isRemote)
 			{
 				this.workTicks = dataStream.readInt();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	@Override
 	public int getSizeInventory(){
 		return this.inventory.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int par1)
 	{
 		return this.inventory[par1];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int par1, int par2)
 	{
 		if (this.inventory[par1] != null)
 		{
 			ItemStack var3;
 
 			if (this.inventory[par1].stackSize <= par2)
 			{
 				var3 = this.inventory[par1];
 				this.inventory[par1] = null;
 				return var3;
 			}
 			else
 			{
 				var3 = this.inventory[par1].splitStack(par2);
 
 				if (this.inventory[par1].stackSize == 0)
 				{
 					this.inventory[par1] = null;
 				}
 
 				return var3;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int par1)
 	{
 		if (this.inventory[par1] != null)
 		{
 			ItemStack var2 = this.inventory[par1];
 			this.inventory[par1] = null;
 			return var2;
 		}
 		else
 		{
 			return null;
 		}
 }
 
 	@Override
 	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
 	{
 		this.inventory[par1] = par2ItemStack;
 
 		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
 		{
 			par2ItemStack.stackSize = this.getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName(){
 		return "Sunlight Collector";
 	}
 
 	@Override
 	public boolean isInvNameLocalized(){
 		return true;
 	}
 
 	@Override
 	public int getInventoryStackLimit(){
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
 	{
 		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
 }	
 
 	@Override
 	public void openChest(){}
 
 	@Override
 	public void closeChest(){}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack is){
 		if(is.itemID == Item.redstone.itemID && i < 3) return true;
 		return false;
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound){
 		super.writeToNBT(tagCompound);
 		tagCompound.setBoolean("isWorking",this.hasItem);
 		NBTTagList tagList = new NBTTagList();
 		for (int var3 = 0; var3 < this.inventory.length; ++var3){
 			if (this.inventory[var3] != null){
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("slot", (byte) var3);
 				this.inventory[var3].writeToNBT(var4);
 				tagList.appendTag(var4);
 			}
 		}
 
 		tagCompound.setTag("items", tagList);
 	}
 	
 	public void readFromNBT(NBTTagCompound tagCompound){
 		super.readFromNBT(tagCompound);
 		//this.hasItem = tagCompound.getBoolean("isWorking");
 		NBTTagList var2 = tagCompound.getTagList("items");
 		for (int var3 = 0; var3 < var2.tagCount(); ++var3){
 			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(var3);
 			byte var5 = var4.getByte("slot");
 			if (var5 >= 0 && var5 < this.inventory.length){
 				this.inventory[var5] = ItemStack.loadItemStackFromNBT(var4);
 			}
 		}
 	}
 
 }
