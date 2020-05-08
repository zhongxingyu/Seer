 package widux.creativetools;
 
 import buildcraft.api.core.SafeTimeTracker;
 import buildcraft.api.power.IPowerProvider;
 import buildcraft.api.power.IPowerReceptor;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileEntityPowerDuplicator extends TileEntity implements IPowerProvider
 {
 	
 	private PowerMode currentMode = PowerMode.NONE;
 	private int powerStrength; // Applies to: BC, IC, UE, RP
 	private int packetSize; // Applies to: IC
 	
 	private static final String NBT_POWERMODE = "PowerModeValue";
 	private static final String NBT_STRENGTH = "PowerStrength";
 	private static final String NBT_SIZE = "PowerPacketSize";
 	
 	public TileEntityPowerDuplicator()
 	{
 		
 	}
 	
 	public void readFromNBT(NBTTagCompound nbt)
 	{
 		super.readFromNBT(nbt);
 		this.currentMode = PowerMode.toMode(nbt.getInteger(NBT_POWERMODE));
 		this.powerStrength = nbt.getInteger(NBT_STRENGTH);
 		this.packetSize = nbt.getInteger(NBT_SIZE);
 	}
 	
 	public void writeToNBT(NBTTagCompound nbt)
 	{
 		super.writeToNBT(nbt);
 		nbt.setInteger(NBT_POWERMODE, this.currentMode.toInt());
 		nbt.setInteger(NBT_STRENGTH, this.powerStrength);
 		nbt.setInteger(NBT_SIZE, this.packetSize);
 	}
 	
 	public void setPowerMode(PowerMode newMode)
 	{
 		this.currentMode = newMode;
 	}
 	
 	public void setPowerStrength(int newStrength)
 	{
 		this.powerStrength = newStrength;
 	}
 	
 	public void setPacketSize(int newSize)
 	{
 		this.packetSize = newSize;
 	}
 	
 	public PowerMode getPowerMode()
 	{
 		return this.currentMode;
 	}
 	
 	public int getPowerStrength()
 	{
 		return this.powerStrength;
 	}
 	
 	public int getPacketSize()
 	{
 		return this.packetSize;
 	}
 	
 	public boolean isUsableByPlayer(EntityPlayer player)
 	{
 		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
 	}
 
 	
 	public int getLatency() {
		//TODO DaFUQ!
 		return 0;
 	}
 
 	
 	public int getMinEnergyReceived() {
 		return 0;
 	}
 
 	
 	public int getMaxEnergyReceived() {
 		return 0;
 	}
 
 	
 	public int getMaxEnergyStored() {
		return Integer.MAX_VALUE;
 	}
 
 	
 	public int getActivationEnergy() {
		//TODO Confirm.
 		return 0;
 	}
 
 	
 	public float getEnergyStored() {
		//TODO Confirm.
		return this.powerStrength;
 	}
 
 	
 	public void configure(int latency, int minEnergyReceived,
 			int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		//TODO Confirm.
 		
 	}
 
 	
 	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		//TODO Confirm.
 		
 	}
 
 	
 	public boolean update(IPowerReceptor receptor) {
		//TODO Confirm.
 		return false;
 	}
 
 	
 	public boolean preConditions(IPowerReceptor receptor) {
		//TODO Confirm.
 		return true;
 	}
 
 	
 	public float useEnergy(float min, float max, boolean doUse) {
		//TODO Confirm.
 		return 0;
 	}
 	
 	public void receiveEnergy(float quantity, ForgeDirection from) {
 		//Doesn't do SHIT!
 	}
 	
 	public boolean isPowerSource(ForgeDirection from) {
 		return true;
 	}
 
 	public SafeTimeTracker getTimeTracker() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
