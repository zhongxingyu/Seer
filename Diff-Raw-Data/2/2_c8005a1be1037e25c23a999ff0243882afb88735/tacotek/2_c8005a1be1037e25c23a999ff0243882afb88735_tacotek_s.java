 package assets.tacotek.common;
 
 import assets.tacotek.Init.*;
 import assets.tacotek.Items.Items;
 import assets.tacotek.blocks.Blocks;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.client.MinecraftForgeClient;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = tacotek.modID, name = "TacoTek", version = "0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class tacotek {
 
 	public static final String modID = "tacotek";
 	
 	//Create our Main Creative Inventory Tab.
 	public static CreativeTabs tacotekTab = new CreativeTabs("tabTacoTek") {
 		public ItemStack getIconItemStack() {
			return new ItemStack(Items.Flour);
 		}
 	};
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		//Load Creative Tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabTacoTek", "TacoTek");
 		
 		//Add Items & Blocks to the game.
 		Blocks.addBlocks();
 		Items.addItems();
 		
 		//Add Recipes.
 		CraftingInit.addCraftingRecipes();
 		SmeltingInit.addSmeltRecipes();
 	}
 }
