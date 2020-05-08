 package colorwater;
 
 import buildcraft.api.recipes.RefineryRecipe;
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import forestry.api.recipes.ICarpenterManager;
 import forestry.api.recipes.ISqueezerManager;
 import forestry.api.recipes.RecipeManagers;
 
 @Mod(name="ColorWater", version="0.0.0", modid = "ColorWater")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class ColorWater {
 
 	@Instance("ColorWater")
 	public static ColorWater instance;
 	
 	@SidedProxy(clientSide="colorwater.client.ClientProxy", serverSide="colorwater.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		ModConfiguration.loadConfiguration(event.getSuggestedConfigurationFile());
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		ModItems.registerItems();
 		proxy.registerRenderers();
 		MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		ExternalItems.registerExternalItems();
 		registerCraftingRecipe();
 		registerRefineryRecipe();
 		registerCarpenterRecipe();
 		registerSqueezerRecipe();
 	}
 
 	private void registerCraftingRecipe()
 	{
 		int i;
 		for(i = 0; i < 16; ++i){
 			GameRegistry.addRecipe(new ShapelessOreRecipe(ModItems.bucketSub[i].getItemStack(1) ,new Object[]{"dye" + Color.values()[i].name(), Item.bucketEmpty, Item.bucketWater}));
 			if(ModItems.blockGel != null){
 				GameRegistry.addShapelessRecipe(ModItems.bucketGelSub[i].getItemStack(1), new Object[]{ModItems.blockGelSub[i].getItemStack(1), Item.bucketEmpty});
 				GameRegistry.addShapelessRecipe(ModItems.blockGelSub[i].getItemStack(1), new Object[]{ModItems.bucketGelSub[i].getItemStack(1)});
 				GameRegistry.addRecipe(ModItems.blockGelSub[i].getItemStack(1), new Object[]{"bb", "bb", Character.valueOf('b'), ModItems.itemGelSub[i].getItemStack(1)});
 			}
 		}
 	}
 	
 	private void registerRefineryRecipe()
 	{
 		int i;
 		LiquidStack ingredient1;
 		LiquidStack ingredient2;
 		LiquidStack result;
 		for(i = 0; i < 16; ++i){
 			if(Color.mixing[i].length == 2){
 				RefineryRecipe.registerRefineryRecipe(
 						new RefineryRecipe(
 								LiquidDictionary.getLiquid("water" + Color.mixing[i][0].name(), 1),
 								LiquidDictionary.getLiquid("water" + Color.mixing[i][1].name(), 1),
 								LiquidDictionary.getLiquid("water" + Color.values()[i].name(), 2),
 								1, 1
 								)
 						);
 				
 				RefineryRecipe.registerRefineryRecipe(
 						new RefineryRecipe(
 								LiquidDictionary.getLiquid("oil" + Color.mixing[i][0].name(), 1),
 								LiquidDictionary.getLiquid("oil" + Color.mixing[i][1].name(), 1),
 								LiquidDictionary.getLiquid("oil" + Color.values()[i].name(), 2),
 								1, 1
 								)
 						);
 				
 				RefineryRecipe.registerRefineryRecipe(
 						new RefineryRecipe(
 								LiquidDictionary.getLiquid("gel" + Color.mixing[i][0].name(), 1),
 								LiquidDictionary.getLiquid("gel" + Color.mixing[i][1].name(), 1),
 								LiquidDictionary.getLiquid("gel" + Color.values()[i].name(), 2),
 								1, 1
 								)
 						);
 			}
 			
 			if(ExternalItems.fuel != null){
 				RefineryRecipe.registerRefineryRecipe(
 						new RefineryRecipe(
 								new LiquidStack(ExternalItems.fuel.itemID, 1, ExternalItems.fuel.getItemDamage()),
 								LiquidDictionary.getLiquid("water" + Color.values()[i].name(), 1),
 								LiquidDictionary.getLiquid("oil" + Color.values()[i].name(), 2),
 								1, 1
 								)
 						);
 			}
 		}
 	}
 	
 	private void registerCarpenterRecipe() {
 		ICarpenterManager carpenterManager = RecipeManagers.carpenterManager;
 		if(carpenterManager == null){
 			registerCarpenterAlternativeRecipe();
 			return;
 		}
 		
 		int i;
 		ItemStack wool = new ItemStack(Block.cloth, 1, -1);
 		ItemStack string = new ItemStack(Item.silk, 1, -1);
 		ItemStack slimeBall = new ItemStack(Item.slimeBall, 1, -1);
 		String liquidDyeGelBase;
 		if(ExternalItems.fuel != null){
 			liquidDyeGelBase = "oil";
 		}else{
 			liquidDyeGelBase = "water";
 		}
 		for(i = 0; i < 16; ++ i){
 			Color color = Color.values()[i];
 			carpenterManager.addRecipe(
 					5,
 					LiquidDictionary.getLiquid("water"+color.name(), LiquidContainerRegistry.BUCKET_VOLUME/4),
 					null,
 					new ItemStack(Block.cloth, 1, i),
 					new Object[]{"w",Character.valueOf('w'),wool});
 			
 			carpenterManager.addRecipe(
 					5,
 					LiquidDictionary.getLiquid("water"+color.name(), LiquidContainerRegistry.BUCKET_VOLUME/4),
 					null,
 					new ItemStack(Block.cloth, 1, i),
 					new Object[]{"ss", "ss", Character.valueOf('s'), string});
 			
 			if(ModItems.blockGel != null){
 				carpenterManager.addRecipe(
 						5,
 						LiquidDictionary.getLiquid(liquidDyeGelBase+color.name(), LiquidContainerRegistry.BUCKET_VOLUME/2),
 						null,
 						ModItems.blockGelSub[i].getItemStack(1),
 						new Object[]{"ss", "ss", Character.valueOf('s'), slimeBall});
 			}
 		}
 	}
 	
 	private void registerCarpenterAlternativeRecipe()
 	{
 		int i;
 		ItemStack slimeBall = new ItemStack(Item.slimeBall, 1, -1);
 		int itemDyeGelBaseIndex;
 		if(ExternalItems.fuel != null){
 			itemDyeGelBaseIndex = 16;
 		}else{
 			itemDyeGelBaseIndex = 0;
 		}
 		for(i = 0; i < 16; ++i){
 			if(ModItems.blockGel != null){
				ItemStack itemDyeGel = ModItems.bucketSub[itemDyeGelBaseIndex+i].getItemStack(1);
				GameRegistry.addShapelessRecipe(ModItems.blockGelSub[i].getItemStack(1), new Object[]{itemDyeGel, slimeBall, slimeBall, slimeBall, slimeBall});
 			}
 		}
 	}
 	
 	public void registerSqueezerRecipe()
 	{
 		ISqueezerManager squeezerManager = RecipeManagers.squeezerManager;
 		
 		if(squeezerManager == null){
 			return;
 		}
 		
 		int i;
 		for(i = 0; i < 16; ++i){
			squeezerManager.addRecipe(5, new ItemStack[]{ModItems.itemGelSub[i].getItemStack(1)}, new LiquidStack(ModItems.blockGelSub[i].parentID, LiquidContainerRegistry.BUCKET_VOLUME/4, ModItems.blockGelSub[i].meta));
 		}
 	}
 }
