 package emasher.gas.fluids;
 
 import net.minecraft.block.Block;
 import net.minecraft.util.Icon;
 import net.minecraftforge.fluids.Fluid;
 
 public class FluidGas extends Fluid
 {
 	public int internalID;
 	
	public static final String[] NAMES = new String[]{"Natural Gas", "Propellent", "Hydrogen", "Smoke", "Toxic Gas", "Deadly Neurotoxin", "Corossive Gas", "Nether Plasma"};
 	
 	public FluidGas(String fluidName, Block theBlock, int iID)
 	{
 		super(fluidName);
 		internalID = iID;
 		this.setBlockID(theBlock);
 		//this.setStillIcon(theBlock.getBlockTextureFromSide(0));
 		//this.setFlowingIcon(theBlock.getBlockTextureFromSide(0));
 		this.setGaseous(true);
 		this.setViscosity(100);
 		this.setDensity(-1000);
 		this.setUnlocalizedName(fluidName);
 		
 	}
 	
 	@Override
 	public String getLocalizedName()
 	{
 		return NAMES[internalID];
 	}
 	
 	@Override
 	public Icon getStillIcon()
     {
         return Block.blocksList[this.blockID].getBlockTextureFromSide(0);
     }
 	
 	@Override
     public Icon getFlowingIcon()
     {
         return Block.blocksList[this.blockID].getBlockTextureFromSide(0);
     }
 
 }
