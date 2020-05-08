 package ip.industrialProcessing.power.utils;
 
 import java.util.Arrays;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import ip.industrialProcessing.machines.MachineItemStack;
 import ip.industrialProcessing.power.IPowerAcceptor;
 
 public class PowerDistributor {
 
     private float totalSiemens = 0;
     private float[] siemens = new float[0];
     private PowerAcceptorConnection[] outputs = new PowerAcceptorConnection[0];
 
     public PowerAcceptorConnection[] getOutputs() {
 	return outputs;
     }
 
     public void setOutputs(PowerAcceptorConnection... outputs) {
 	if (outputs == null)
 	    outputs = new PowerAcceptorConnection[0];
 	this.outputs = outputs;
 	this.siemens = new float[outputs.length];
 	Arrays.fill(this.siemens, 0f);
 	this.totalSiemens = 0;
     }
 
     public float getResistance(float voltage, World world) {
 	this.totalSiemens = 0;
 	for (int i = 0; i < outputs.length; i++) {
 	    PowerAcceptorConnection connection = outputs[i];
 	    if (connection != null && connection.connectedFrom != null) {
 		IPowerAcceptor acceptor = getAcceptor(connection, world);
 		if (acceptor != null) {
 		    totalSiemens += siemens[i] = 1 / acceptor.getResistance(connection.connectedFrom, voltage);
 		}
 	    }
 	}
 	if (totalSiemens == 0)
 	    return Float.POSITIVE_INFINITY;
 	return 1 / totalSiemens;
     }
 
     public void distributePower(float voltage, float coulombs, World world) {
 	if (Float.isNaN(voltage))
 	    voltage = 0;
 	for (int i = 0; i < siemens.length; i++) {
 	    PowerAcceptorConnection connection = outputs[i];
 	    if (connection != null && connection.connectedFrom != null) {
 		IPowerAcceptor acceptor = getAcceptor(connection, world);
 		if (acceptor != null) {
 		    float distribution = (this.totalSiemens == 0) ? 0.0f : coulombs * this.siemens[i] / this.totalSiemens;
 		    if (Float.isNaN(distribution))
 			distribution = 0;
 		    acceptor.applyPower(connection.connectedFrom, distribution, voltage);
 		}
 	    }
 	}
     }
 
     private IPowerAcceptor getAcceptor(PowerAcceptorConnection connection, World world) {
 	TileEntity entity = world.getBlockTileEntity(connection.x, connection.y, connection.z);
 	if (entity instanceof IPowerAcceptor)
 	    return (IPowerAcceptor) entity;
 	return null;
     }
 
     public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
 	NBTTagList list = new NBTTagList();
 	int connectionCount = 0;
 	for (int i = 0; i < this.outputs.length; i++) {
 	    PowerAcceptorConnection connection = outputs[i];
 	    if (connection != null && connection.connectedFrom != null) {
 		NBTTagCompound compound = new NBTTagCompound();
 		compound.setInteger("X", connection.x);
 		compound.setInteger("Y", connection.y);
 		compound.setInteger("Z", connection.z);
 		compound.setInteger("Dir", connection.connectedFrom.ordinal());
 		list.appendTag(compound);
 		connectionCount++;
 	    }
 	}
 	par1nbtTagCompound.setTag("Connections", list);
 	par1nbtTagCompound.setInteger("ConnectionCount", connectionCount);
     }
 
     public void readFromNBT(NBTTagCompound par1Compound) {
 	int connectionCount = par1Compound.getInteger("ConnectionCount");
 	NBTTagList nbttaglist = par1Compound.getTagList("Connections");
 	PowerAcceptorConnection[] connections = new PowerAcceptorConnection[connectionCount];
 	for (int i = 0; i < connections.length; i++) {
 	    NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
 	    int x = nbttagcompound1.getInteger("X");
 	    int y = nbttagcompound1.getInteger("Y");
 	    int z = nbttagcompound1.getInteger("Z");
 	    ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[nbttagcompound1.getInteger("Dir")];
	    System.out.println("Acceptor at " + x + " " + y + " " + z + " " + dir);
 	    connections[i] = new PowerAcceptorConnection(x, y, z, dir);
 	}
 	this.setOutputs(connections);
     }
 }
