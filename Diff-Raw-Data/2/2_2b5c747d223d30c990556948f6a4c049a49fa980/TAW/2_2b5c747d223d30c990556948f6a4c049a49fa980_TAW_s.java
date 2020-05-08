 package jw.taw.common;
 
 import java.util.Random;
 
 import jw.taw.common.block.Blocks;
 import jw.taw.common.item.Items;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
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
 
 @Mod(modid="TAW", name="Tools and Weapons Mod", version="0.0.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class TAW {
 	// The instance of this mod that Forge uses
 	@Instance("TAW")
 	public static TAW instance;
 	
	@SidedProxy(clientSide="jw.taw.client.ClientProxy", serverSide="jw.taw.CommonProxy")
 	public static CommonProxy proxy;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		// Stub method
 	}
 	
 	@Init
 	public void init(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 		GameRegistry.registerWorldGenerator(new TriniumOreGenerator());
 		Blocks.addBlocks();
 		Items.registerAllItems();
 		addRecipes();
 	}
 	
 	// Adds all the tool recipes to the game
 	private void addRecipes() {
 		Block obs = Block.obsidian;
 		Item stk = Item.stick;
 		
 		GameRegistry.addSmelting(Items.industrialGlassMaterials.itemID, new ItemStack(Blocks.hardyGlass), 0.1f);
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		// Stub method
 	}
 }
