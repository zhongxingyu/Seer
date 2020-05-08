 package mods.recipear.modules;
 
 import ic2.core.AdvRecipe;
 import ic2.core.AdvShapelessRecipe;
 import mods.recipear.Recipear;
 import mods.recipear.RecipearLogger;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.item.crafting.ShapedRecipes;
 import net.minecraft.item.crafting.ShapelessRecipes;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 
@Mod(modid = "Recipear2|IC2", name = "RecipearIC2", version = "1.0", dependencies="required-after:Recipear2@[2.0,)")
 public class RecipearIC2 {
 	
 	private boolean ic2 = false;
 	
 	@EventHandler
 	public void PostInit(FMLPostInitializationEvent event) 
 	{
 		ic2 = Loader.isModLoaded("IC2");
 		
 		if(ic2) {
 			RecipearLogger.info("[IC2] Starting in " + event.getSide().toString() + " Mode");
 		} else {
 			RecipearLogger.info("[IC2] Could not find IC2");
 		}
 	}
	
	public void setCraftingRecipeOutput(IRecipe iRecipe, ItemStack output) {	
		if (iRecipe instanceof AdvRecipe) {
			((AdvRecipe)iRecipe).output = output; 
		} else if (iRecipe instanceof AdvShapelessRecipe) { 
			((AdvShapelessRecipe)iRecipe).output = output; 
		}
	}
	
 }
