 package assets.tacotek.common;
 
 import java.io.File;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import assets.tacotek.Items.ItemsHelper;
 import assets.tacotek.blocks.BlocksHelper;
 import assets.tacotek.proxy.ServerProxy;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(
 		//Gives forge info about mod.
 		modid = tacotek.modID,
 		name = "TacoTek",
 		version = "0.3.0",
 		dependencies = 	"required-after:IC2;" +
 				"after:ComputerCraft;" +
 				"after:BuildCraft|Core;" +
 				"after:BuildCraft|Transport;" +
 				"after:BuildCraft|Builders;" +
 				"after:BuildCraft|Silicon;" +
 				"after:LogisticsPipes|Main" )
 
 //Mods is required on client and server.
 @NetworkMod(clientSideRequired = true)
 public class tacotek {
 
 	@SidedProxy(clientSide = "assets.tacotek.proxy.ClientProxy", serverSide = "assets.tacotek.proxy.ServerProxy")
 	public static ServerProxy proxy;
 
 	//Mod name.
 	public static final String modID = "tacotek";
 
 	//Adds creative tab for mod.
 	public static CreativeTabs tacotekTab = new CreativeTabs("tabTacoTek") { public ItemStack getIconItemStack() {return new ItemStack(ItemsHelper.taco);} };
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		
 		//Builds config.
 		Configuration config = new Configuration(new File("config/TacoTek.cfg"));
 		config.load();
 		IDsHelper.runConfiguration(config);
 		config.save();
 
 		//Load Creative Tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabTacoTek", "TacoTek");
 
 		//Imports blocks.
 		BlocksHelper.setupBlocks();
 
 		//Imports items.
 		ItemsHelper.setupItems(proxy);
 
 		//Imports crafting recipes.
 		craftingRecipes();
 
 		//Imports smelting recipes.
 		smeltingRecipes();
 
 		//GameRegistry.registerWorldGenerator(new WorldGenOres());
 
 		//NetworkRegistry.instance().registerGuiHandler(instance, guihandler);
 
 		//TickHandler
 		TickRegistry.registerTickHandler(new TacoTickHandler(), Side.SERVER);
 	}
 
 	private static void smeltingRecipes() {
 
 		//Dough->bread.
 		GameRegistry.addSmelting(ItemsHelper.dough.itemID, new ItemStack(Item.bread, 1), 0.5F);
 
 		//Uncooked tortilla->tortilla.
 		GameRegistry.addSmelting(ItemsHelper.uncookedTortilla.itemID, new ItemStack(ItemsHelper.tortilla, 1), 1.0F);
 	}
 
 	private static void craftingRecipes() {
 
 		//Shapeless:wheat to flour.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.flour, 1),
 				Item.wheat );
 
 		//Shapeless:Flour+bucket_water->dough.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.dough, 1),
 				ItemsHelper.flour, Item.bucketWater );
 
 		//Shapeless:4 bucket_water->salt.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.salt, 1),
 				Item.bucketWater, Item.bucketWater, Item.bucketWater, Item.bucketWater );
 
 		//Shapeless:dough->uncookedTortilla.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.uncookedTortilla, 1),
 				ItemsHelper.dough );
 
 		//Shapeless:2 bucket_milk,salt->cheese.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.cheese, 1),
 				Item.bucketMilk, Item.bucketMilk, ItemsHelper.salt );
 
 		//Shapeless:tacoBox->9 tacos.
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.taco, 9),
 				BlocksHelper.tacoBox );
 
 		//Shaped:cheese,beef,tortilla->taco.
 		GameRegistry.addRecipe(new ItemStack(ItemsHelper.taco, 1), new Object[]
 				{ "C", "B", "T",
 			'C', ItemsHelper.cheese, 
 			'B', Item.beefCooked,
 			'T', ItemsHelper.tortilla} );
 
 		//Shapeless:9 tacos->tacoBox
 		GameRegistry.addRecipe(new ItemStack(BlocksHelper.tacoBox, 1), new Object[]
 				{"TTT","TTT","TTT",
 			'T', ItemsHelper.taco} );
 
 		GameRegistry.addRecipe(new ItemStack(ItemsHelper.torch_placer, 1, ItemsHelper.torch_placer.getMaxDamage()), new Object[]
 				{"   "," T ","   ",
 			'T', Item.appleRed} );
 	
 		//Shapeless:2 appleRed, sugar, dough->applePie.
 				GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.applePie, 1),
 						Item.appleRed, Item.sugar, ItemsHelper.dough );
 	       
 				//Shapeless:3 appleRed, bowl->appleSauce.
 				GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.appleSauce, 1),
 						Item.appleRed, Item.bowlEmpty );
 				
 				//Shapeless:3 appleRed, glassBottle->appleCider.
 				GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.appleCider, 1),
 						Item.appleRed, Item.glassBottle );
 	
 				//Shaped:4 wheat->bagel.
 				GameRegistry.addRecipe(new ItemStack(ItemsHelper.bagel, 1), new Object[]
 						{ " W ", "W W", " W ",
 					'W', Item.wheat} );	
 	
 				//Shaped:5 bucketMilk, glassBottle ->bottleofMilk.
 				GameRegistry.addShapelessRecipe(new ItemStack(ItemsHelper.bottleofMilk, 4),
 					Item.bucketMilk, Item.glassBottle, Item.glassBottle, Item.glassBottle, Item.glassBottle );	
 	
 				//Shaped:2 cheese, bakedPotato ->cheesyPotato.
 				GameRegistry.addRecipe(new ItemStack(ItemsHelper.cheesyPotato, 1), new Object[]
 						{ " ", "C", "B",
 						'C', ItemsHelper.cheese,
 						'B', Item.bakedPotato} );
	
				//Shaped:3 dyePowder,3 ->chocolateBar.
				GameRegistry.addRecipe(new ItemStack(ItemsHelper.chocolateBar, 1), new Object[]
						{ "C", "C", "C",
					'C', Item.dyePowder,3} );			
 	}
 	 }
 	
