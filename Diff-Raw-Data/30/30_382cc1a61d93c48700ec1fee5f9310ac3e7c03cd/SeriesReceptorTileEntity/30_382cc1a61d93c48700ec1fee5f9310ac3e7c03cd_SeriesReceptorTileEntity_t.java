 package pixlepix.particlephysics.common.tile;
 
 import java.util.EnumSet;
 
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import pixlepix.particlephysics.common.api.BaseParticle;
 import pixlepix.particlephysics.common.api.IParticleReceptor;
 import universalelectricity.compatibility.Compatibility;
 import universalelectricity.compatibility.TileEntityUniversalElectrical;
 import cofh.api.energy.IEnergyHandler;
 
 public class SeriesReceptorTileEntity extends TileEntityUniversalElectrical implements IParticleReceptor {
 	//UE fixes
 	//Thanks to Ybilta
 	@Override
 	public double getOfferedEnergy()
 	{
 	return Math.min(this.getEnergyStored() * Compatibility.TO_IC2_RATIO, this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO);
 	}
 	//actually send TE power
 	private void sendTEPower() {
 	    TileEntity tile = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
 	    if(tile != null && tile instanceof IEnergyHandler) {
 	        IEnergyHandler energy = (IEnergyHandler)tile;
 	        int send = energy.receiveEnergy(ForgeDirection.UP, (int)Math.min(this.getEnergyStored() * Compatibility.TO_TE_RATIO, this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_TE_RATIO), true);
 	        energy.receiveEnergy(ForgeDirection.UP, send, false);
 	        this.provideElectricity((float) send * Compatibility.TE_RATIO, true);
 	    }
 	    tile = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
 	    if(tile != null && tile instanceof IEnergyHandler) {
 	        IEnergyHandler energy = (IEnergyHandler)tile;
 	        int send = energy.receiveEnergy(ForgeDirection.DOWN, (int)Math.min(this.getEnergyStored() * Compatibility.TO_TE_RATIO, this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_TE_RATIO), true);
 	        energy.receiveEnergy(ForgeDirection.DOWN, send, false);
 	        this.provideElectricity((float) send * Compatibility.TE_RATIO, true);
 	    }
 	}
 	//make sure we can't get energy from TE
 	@Override
 	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
 	{
 	    return 0;
 	}
 	
 	public int excitedTicks=0;
 
 	public float powerToDischarge=0;
 	@Override
 	public void onContact(BaseParticle particle) {
 		if(this.excitedTicks>0){
 			this.setEnergyStored(this.getEnergyStored()+0.00090F*particle.potential);
 		}else{
 			this.setEnergyStored(this.getEnergyStored()+0.00030F*particle.potential);
 		}
 		this.excitedTicks=20;
 		particle.setDead();
 	}
 	
 	@Override
 	public void updateEntity(){
 		super.updateEntity();
 		this.produce();
 		this.excitedTicks--;
		sendTEPower();
 	}	
 	
 	@Override
 	public float getRequest(ForgeDirection direction) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public float getProvide(ForgeDirection direction) {
 		return 20000;
 	}
 
 	@Override
 	public float getMaxEnergyStored() {
 		// TODO Auto-generated method stub
 		return 1000000;
 	}
 	
 	@Override
 	public EnumSet<ForgeDirection> getOutputDirections()
 	{
		return EnumSet.of(ForgeDirection.UP, ForgeDirection.DOWN);
 	}
 	
 	@Override
 	public boolean canConnect(ForgeDirection direction){
 		if(direction.equals(ForgeDirection.UP)||direction.equals(ForgeDirection.DOWN)){
 			return true;
 		}
 		return false;
 		
 	}
 }
