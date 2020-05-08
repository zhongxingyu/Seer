 package emris.mods.LeatherWaterSacTFC;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import TFC.Core.Recipes;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid="LeatherWaterSac", name="Leather Water Sac", version="1.1.b75", dependencies = "after:TerraFirmaCraft")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, versionBounds = "[1.1.b75]")
 public class LeatherWaterSac {
 	@Instance("LeatherWaterSac")
 	public static LeatherWaterSac instance;
 	
 	@SidedProxy(clientSide="emris.mods.LeatherWaterSacTFC.ClientProxy", serverSide="emris.mods.LeatherWaterSacTFC.CommonProxy")
 	public static CommonProxy proxy;
 	
 	public final static Item itemLeatherWaterSac = new ItemLeatherWaterSac(5252);
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 		
 		LanguageRegistry.addName(itemLeatherWaterSac, "Leather Water Sac");
 		ItemStack lwSac = new ItemStack(this.itemLeatherWaterSac, 1, this.itemLeatherWaterSac.getMaxDamage());
 		ItemStack leather = new ItemStack(Item.leather, 1);
 		ItemStack string = new ItemStack(Item.silk, 1);
 		
 		Item[] tfcKnives = Recipes.Knives;
 		for(int j = 0; j < tfcKnives.length; j++) {
 			GameRegistry.addShapelessRecipe(lwSac, leather, string, new ItemStack(tfcKnives[j], 1, -1));
 		}
 		
 		GameRegistry.registerCraftingHandler(new CraftingHandler());
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub Method
 		// Working with other MODs
 	}
 }
