 package ip.industrialProcessing.fluids;
 
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidRegistry;
 
 public class ItemFluid extends Fluid{
 
 	public ItemFluid(String fluidName, int density,int viscosity) {
 		super(fluidName);
 		setDensity( density ); 
 		setViscosity( viscosity );
		FluidRegistry.registerFluid(this);
 	}
 
 
 }
