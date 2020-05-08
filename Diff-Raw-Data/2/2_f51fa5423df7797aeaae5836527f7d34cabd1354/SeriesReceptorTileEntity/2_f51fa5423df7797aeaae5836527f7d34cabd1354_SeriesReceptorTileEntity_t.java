 package pixlepix.particlephysics.common.tile;
 
 import java.util.EnumSet;
 
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import pixlepix.particlephysics.common.api.BaseParticle;
 import pixlepix.particlephysics.common.api.IParticleReceptor;
 import universalelectricity.compatibility.TileEntityUniversalElectrical;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import buildcraft.api.power.PowerHandler.PowerReceiver;
 
 public class SeriesReceptorTileEntity extends TileEntityUniversalElectrical implements IParticleReceptor {
 
 	
 	public int excitedTicks=0;
 
 	public float powerToDischarge=0;
 	@Override
 	public void onContact(BaseParticle particle) {
 		if(this.excitedTicks>0){
 			this.receiveElectricity(0.030F*particle.potential,true);
 		}else{
 			this.receiveElectricity(0.010F*particle.potential, true);
 		}
 		this.excitedTicks=20;
 		particle.setDead();
 	}
 	
 	@Override
 	public void updateEntity(){
 		super.updateEntity();
 		this.produce();
 		this.excitedTicks--;
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
 	
 	
 	
 	
 	
 	
 }
