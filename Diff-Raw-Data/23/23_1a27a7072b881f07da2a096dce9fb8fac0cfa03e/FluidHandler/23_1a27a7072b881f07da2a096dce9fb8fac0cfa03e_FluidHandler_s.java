 package common.fluids;
 
 import common.IDHandler;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class FluidHandler{
     
     public static Fluid Wine;
     
     private static void registerFluids(){
         FluidRegistry.registerFluid(Wine);
     }
 
     private static void initializeLiquids(){
        Wine = new BlockWine(IDHandler.WineID, Wine, Material.water);
     }
 
     private static void initializeContainers(){
         
     }
 
    private static void registerBlocks(){
        GameRegistry.registerBlock(Wine, "Wine");
     }
 }
