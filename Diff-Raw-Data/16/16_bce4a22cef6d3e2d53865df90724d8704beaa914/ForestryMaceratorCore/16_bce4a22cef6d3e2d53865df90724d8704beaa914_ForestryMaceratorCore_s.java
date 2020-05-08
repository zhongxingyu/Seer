 package ForestryMacerator;
 
 import java.util.logging.Logger;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 import buildcraft.api.liquids.LiquidStack;
 
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import forestry.api.core.BlockInterface;
 import forestry.api.core.ItemInterface;
 import forestry.api.recipes.RecipeManagers;
 
 @Mod(modid = "ForestryMacerator", name = "Forestry Macerator Addon", version = "1.0.0")
 public class ForestryMaceratorCore {
 	public static Logger cmLog = Logger.getLogger("ForestryMacerator");
 	public static ShapedOreRecipe copperRecipe;
 	public static ShapedOreRecipe tinRecipe;
 	
 	@Instance("ForestryMacerator")
 	public static ForestryMaceratorCore instance;
 	
 	@PreInit
 	public void loadConfiguration(FMLPreInitializationEvent evt) {
 		cmLog.setParent(FMLLog.getLogger());
 		cmLog.info("Starting ForestryMacerator v1.0.0");
 		
 		
 	}
 	
 	
 	
 	@Init
 	public void initialize(FMLInitializationEvent evt) {
 		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(Item.ingotIron,2),new Object[]{"   "," X ","   ", Character.valueOf('X'), Block.oreIron});
		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(Item.ingotGold,2),new Object[]{"   "," X ","   ", Character.valueOf('X'), Block.oreGold});
 		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(Item.flint,1),new Object[]{"   "," X ","   ", Character.valueOf('X'), Block.gravel});
		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(ItemInterface.getItem("ingotCopper").getItem(),2), new Object[]{"   "," X ","   ", Character.valueOf('X'), BlockInterface.getBlock("oreCopper")});
 		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(Block.gravel,1),new Object[]{"   "," X ","   ", Character.valueOf('X'), Block.cobblestone});
		RecipeManagers.carpenterManager.addRecipe(45,new LiquidStack(Block.waterStill.blockID, 3000), null ,new ItemStack(ItemInterface.getItem("ingotTin").getItem(),2), new Object[]{"   "," X ","   ", Character.valueOf('X'), BlockInterface.getBlock("oreTin")});
 		
 	}
 	
 	public String getPriorities() {
 	    return "after:mod_IC2;after:mod_BuildCraftCore;after:mod_BuildCraftEnergy;after:mod_BuildCraftFactory;after:mod_BuildCraftSilicon;after:mod_BuildCraftTransport;after:mod_RedPowerWorld;after:mod_Forestry";
 	  }
 
 	public void ModsLoaded() {
 		
 	}
 
 }
