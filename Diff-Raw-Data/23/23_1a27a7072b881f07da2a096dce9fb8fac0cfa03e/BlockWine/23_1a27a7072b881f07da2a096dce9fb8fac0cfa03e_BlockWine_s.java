 package common.fluids;
 
 import common.Reference;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraftforge.fluids.BlockFluidClassic;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidStack;
 
 public class BlockWine extends BlockFluidClassic{
     
     public static Icon WineStillIcon;
     public static Icon WineFlowingIcon;
     
     public BlockWine(int id, Fluid fluid, Material material) {
         super(id, fluid, material);
         stack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         WineStillIcon = iconRegister.registerIcon(Reference.modid + ":" + "Wine");
         WineFlowingIcon = iconRegister.registerIcon(Reference.modid + ":" + "WineFlowing");
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2)
     {
         return par1 != 0 && par1 != 1 ? WineFlowingIcon : WineStillIcon;
     }
 }
