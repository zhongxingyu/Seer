 package ip.industrialProcessing.power.meters;
 
 import ic2.api.Direction;
 import ip.industrialProcessing.DirectionUtils;
 import ip.industrialProcessing.LocalDirection;
 import ip.industrialProcessing.client.render.IAnimationProgress;
 import ip.industrialProcessing.power.IPowerAcceptor;
 import ip.industrialProcessing.power.TileEntityPowerGenerator;
 import ip.industrialProcessing.power.utils.PowerAcceptorConnection;
 import ip.industrialProcessing.power.utils.PowerDistributor;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileEntityAmpMeter extends TileEntityPowerGenerator implements IPowerAcceptor, IAnimationProgress {
 
     LocalDirection inputSide = LocalDirection.BACK;
     LocalDirection outputSide = LocalDirection.UP;
 
     private PowerDistributor distributor;
     private boolean unVerified = true;
     private float voltage = 0;
     private float resistance = Float.POSITIVE_INFINITY;
     private float coulombs = 0;
     private float angle;
 
     public TileEntityAmpMeter() {
 	super(1000);
 	unVerified = true;
 	this.distributor = new PowerDistributor();
     }
 
     @Override
     public void updateEntity() {
 	super.updateEntity();
 	if (unVerified)
 	    checkOutput();
 	if (!this.worldObj.isRemote) {
	    this.angle = this.coulombs * 20 / 150; // * 20 = coulomb -> amp, /25 =
	}					 // amp -> angle
 	    if (voltage > 0) // distribute last pass
 		this.distributor.distributePower(voltage, coulombs, this.worldObj);
 	    this.coulombs = 0;
 	    // fetch resistance for next pass
 	    this.resistance = this.distributor.getResistance(this.voltage, this.worldObj);
 	    notifyBlockChange();
	
     }
 
     @Override
     public boolean canOutputPower(ForgeDirection opposite) {
 	LocalDirection output = DirectionUtils.GetLocalDirection(opposite, getForwardDirection());
 	return output == outputSide;
     }
 
     @Override
     public float getVoltage() {
 	return this.voltage;
     }
 
     @Override
     public float getAnimationProgress(float scale) {
 	return this.angle * scale;
     }
 
     @Override
     public float getResistance(ForgeDirection side, float voltage) {
 	return this.resistance;
     }
 
     @Override
     public void applyPower(ForgeDirection side, float coulombs, float voltage) {
 	this.voltage = voltage;
 	this.coulombs += coulombs;
     }
 
     @Override
     public boolean canAcceptPower(ForgeDirection side) {
 	LocalDirection input = DirectionUtils.GetLocalDirection(side, getForwardDirection());
 	return input == inputSide;
     }
 
     @Override
     protected boolean isValidInput(int slot, int itemID) {
 	return false;
     }
 
     public void checkConnections() {
 	unVerified = true;
     }
 
     private void checkOutput() {
 	ForgeDirection output = DirectionUtils.GetWorldDirection(outputSide, getForwardDirection());
 	IPowerAcceptor acceptor = getAcceptor(output);
 	if (acceptor == null || !acceptor.canAcceptPower(output.getOpposite()))
 	    this.distributor.setOutputs();
 	PowerAcceptorConnection connection = new PowerAcceptorConnection(xCoord + output.offsetX, yCoord + output.offsetY, zCoord + output.offsetZ, output.getOpposite());
 	this.distributor.setOutputs(connection);
 	unVerified = false;
     }
 
     private IPowerAcceptor getAcceptor(ForgeDirection output) {
 	TileEntity entity = this.worldObj.getBlockTileEntity(xCoord + output.offsetX, yCoord + output.offsetY, zCoord + output.offsetZ);
 	if (entity instanceof IPowerAcceptor)
 	    return (IPowerAcceptor) entity;
 	return null;
     }
 
     @Override
     public float getCharge(float amount) {
 	float delta = Math.min(this.coulombs, amount);
 	this.coulombs -= delta;
 	return delta;
     }
 
     @Override
     public void writeToNBT(NBTTagCompound nbt) {
 	super.writeToNBT(nbt);
 	nbt.setFloat("Angle", this.angle);
     }
 
     @Override
     public void readFromNBT(NBTTagCompound nbt) {
 	super.readFromNBT(nbt);
 	this.angle = nbt.getFloat("Angle");
     }
 }
