 package common.fluids;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
 import net.minecraft.util.Icon;
 import net.minecraftforge.fluids.Fluid;
 
 public class Wine extends Fluid{
     public Wine(String fluidName){
         super(fluidName);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getStillIcon(){
         return BlockWine.WineStillIcon;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getFlowingIcon(){
         return BlockWine.WineFlowingIcon;
     }   
 }
