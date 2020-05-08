 package mods.firstspring.dimchest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class TileEntityDimChest extends TileEntity implements IInventory {
 
 	public float lidAngle;
 	public float prevLidAngle;
 	public int numUsingPlayers;
 	private int ticksSinceSync;
 
 	protected int orient;
 
 	//DimChest
 	String name = "";
 	//LinkChest
 	int x = 0,y = 0,z = 0,dim = 0,linkCardColor = 0;
 	protected boolean hasLinkCard;
 
 	@Override
 	public int getSizeInventory() {
 		IInventory inv = getInventory();
 		if(inv == null)
 			return 0;
 		return getInventory().getSizeInventory();
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int var1) {
 		return getInventory().getStackInSlot(var1);
 	}
 
 	@Override
 	public ItemStack decrStackSize(int var1, int var2) {
 		return getInventory().decrStackSize(var1, var2);
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int var1) {
 		return getInventory().getStackInSlotOnClosing(var1);
 	}
 
 	@Override
 	public void setInventorySlotContents(int var1, ItemStack var2) {
 		getInventory().setInventorySlotContents(var1, var2);
 	}
 
 	@Override
 	public String getInvName() {
 		if(this.blockMetadata == 0)
 			return "container.linkchest";
 		else
 			return "container.dimchest";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer var1) {
 		return (this.hasLinkCard || this.blockMetadata != 0) && getInventory() != null && var1.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
 
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		name = nbt.getString("Name");
		orient = nbt.getInteger("orient");
		x = nbt.getInteger("chestX");
		y = nbt.getInteger("chestY");
		z = nbt.getInteger("chestZ");
		dim = nbt.getInteger("chestDimension");
		hasLinkCard = nbt.getBoolean("hasLinkCard");
		linkCardColor = nbt.getInteger("linkCardColor");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		nbt.setString("Name", name);
		nbt.setInteger("orient", orient);
		nbt.setInteger("chestX", x);
		nbt.setInteger("chestY", y);
		nbt.setInteger("chestZ", z);
		nbt.setInteger("chestDimension", dim);
		nbt.setBoolean("hasLinkCard", hasLinkCard);
		nbt.setInteger("linkCardColor", linkCardColor);
 	}
 
 	//チェストよりコピペ
 	//チェストが開いたり閉じたりする処理
 	public void updateEntity()
 	{
 		super.updateEntity();
 
 		if (++this.ticksSinceSync % 20 * 4 == 0)
 		{
 			this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, DimChest.dimChestID, 1, this.numUsingPlayers);
 		}
 
 		this.prevLidAngle = this.lidAngle;
 		float var1 = 0.1F;
 		double var4;
 
 		if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F)
 		{
 			double var2 = (double)this.xCoord + 0.5D;
 			var4 = (double)this.zCoord + 0.5D;
 			this.worldObj.playSoundEffect(var2, (double)this.yCoord + 0.5D, var4, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
 		}
 
 		if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F)
 		{
 			float var8 = this.lidAngle;
 
 			if (this.numUsingPlayers > 0)
 			{
 				this.lidAngle += var1;
 			}
 			else
 			{
 				this.lidAngle -= var1;
 			}
 
 			if (this.lidAngle > 1.0F)
 			{
 				this.lidAngle = 1.0F;
 			}
 
 			float var3 = 0.5F;
 
 			if (this.lidAngle < var3 && var8 >= var3)
 			{
 				var4 = (double)this.xCoord + 0.5D;
 				double var6 = (double)this.zCoord + 0.5D;
 				this.worldObj.playSoundEffect(var4, (double)this.yCoord + 0.5D, var6, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
 			}
 
 			if (this.lidAngle < 0.0F)
 			{
 				this.lidAngle = 0.0F;
 			}
 		}
 	}
 
 	@Override
 	public void openChest()
 	{
 		++this.numUsingPlayers;
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, DimChest.dimChest.blockID, 1, this.numUsingPlayers);
 	}
 
 	@Override
 	public void closeChest(){
 		--this.numUsingPlayers;
 		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, DimChest.dimChest.blockID, 1, this.numUsingPlayers);
 	}
 
 	public IInventory getInventory(){
 		if(this.getBlockMetadata() == 0){
 			World world = MinecraftServer.getServer().worldServerForDimension(dim);
 			TileEntity tile = world.getBlockTileEntity(x, y, z);
 			if(tile instanceof IInventory)
 				return (IInventory)tile;
 			return null;
 		}
 		if(name.equals(""))
 			return InventoryManager.getInventory("Default");
 		return InventoryManager.getInventory(name);
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(bos);
 
 		int x = xCoord;
 		int y = yCoord;
 		int z = zCoord;
 
 		try
 		{
 			dos.writeInt(xCoord);
 			dos.writeInt(yCoord);
 			dos.writeInt(zCoord);
 			dos.writeInt(orient);
 			dos.writeBoolean(hasLinkCard);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = "DCClient";
 		packet.data    = bos.toByteArray();
 		packet.length  = bos.size();
 		packet.isChunkDataPacket = true;
 
 		return packet;
 	}
 
 	public boolean receiveClientEvent(int par1, int par2)
 	{
 		if (par1 == 1)
 		{
 			this.numUsingPlayers = par2;
 			return true;
 		}
 		else
 		{
 			return super.receiveClientEvent(par1, par2);
 		}
 	}
 
 	public void invalidate()
 	{
 		this.updateContainingBlockInfo();
 		super.invalidate();
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		return true;
 	}
 
 }
