 package emasher.sockets.pipes;
 
 import cofh.api.energy.IEnergyHandler;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import buildcraft.api.power.PowerHandler.PowerReceiver;
 import buildcraft.api.power.PowerHandler.Type;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileMJAdapter extends TileEntity implements IPowerReceptor, IEnergyHandler
 {
 	public PowerHandler capacitor;
 	
 	public TileMJAdapter()
 	{
 		capacitor = new PowerHandler(this, Type.STORAGE);
 		capacitor.configure(0, 250, 0, 250);
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound data)
 	{
 		super.readFromNBT(data);
 		capacitor.readFromNBT(data);
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound data)
 	{
 		super.writeToNBT(data);
 		capacitor.writeToNBT(data);
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		ForgeDirection d;
 		if(! worldObj.isRemote) for(int i = 0; i < 6; i++)
 		{
 			d = ForgeDirection.getOrientation(i);
 			int xo = xCoord + d.offsetX;
 			int yo = yCoord + d.offsetY;
 			int zo = zCoord + d.offsetZ;
 			
 			TileEntity te = worldObj.getBlockTileEntity(xo, yo, zo);
 			
 			if(te != null)
 			{
 				if(te instanceof IEnergyHandler)
 				{
 					IEnergyHandler ieh = (IEnergyHandler)te;
 					
 					int amnt = ieh.receiveEnergy(d.getOpposite(), 10 * (int)capacitor.useEnergy(0, 250, false), false);
 					capacitor.useEnergy(amnt, amnt, true);
 				}
 				else if(te instanceof IPowerReceptor)
 				{
 					IPowerReceptor ipr = (IPowerReceptor)te;
					PowerReceiver pr = ipr.getPowerReceiver(d.getOpposite());
 					
					if(pr != null)
					{
						int amnt = (int)pr.receiveEnergy(Type.STORAGE, capacitor.useEnergy(0, 250, false), d.getOpposite());
						capacitor.useEnergy(amnt, amnt, true);
					}
 				}
 			}
 		}
 	}
 	
 	// IPowerHandler
 	
 	@Override
 	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
 	{
 		if(! simulate)
 		{
 			capacitor.addEnergy(maxReceive / 10);
 		}
 		
 		return maxReceive;
 	}
 
 	@Override
 	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
 	{
 		return 10 * (int)capacitor.useEnergy(maxExtract / 10, maxExtract / 10, ! simulate);
 	}
 
 	@Override
 	public boolean canInterface(ForgeDirection from)
 	{
 		return true;
 	}
 
 	@Override
 	public int getEnergyStored(ForgeDirection from)
 	{
 		return (int)capacitor.getEnergyStored() * 10;
 	}
 
 	@Override
 	public int getMaxEnergyStored(ForgeDirection from)
 	{
 		return (int)capacitor.getMaxEnergyStored() * 10;
 	}
 
 	// IPowerReceptor
 	
 	@Override
 	public PowerReceiver getPowerReceiver(ForgeDirection side)
 	{
 		return capacitor.getPowerReceiver();
 	}
 
 	@Override
 	public void doWork(PowerHandler workProvider) {}
 
 	@Override
 	public World getWorld()
 	{
 		return worldObj;
 	}
 	
 }
