 package net.mrblockplacer.JM.CuisineMod;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.Random;
 import net.mrblockplacer.JM.CuisineMod.WorldGenSalt;
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.RenderEngine;
 import net.minecraft.src.RenderGlobal;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldGenMinable;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.client.registry.RenderingRegistry;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "CuisineMod", name = "Cuisine Mod", version = "0.1.1.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class CuisineMod {
 
 	public static WorldGenSalt testgen = new WorldGenSalt();
 	public static GuiHandlerSaltCollector guihandlerSC = new GuiHandlerSaltCollector();
 
 	public static Block saltslab = new BlockSaltSlab(200);
 	public static Block saltblock = new BlockSalt(201);
 	public static Block saltcollectorblock = new BlockSaltCollector(202);
 	public static Block goldLog = new BlockGoldLog(203).setBlockName("GoldLog");
 	public static Block goldLeaf = new BlockGoldLeaf(204, 4).setBlockName("GoldLeaf");
 	public static Block goldSapling = new BlockGoldSapling(205, 7).setBlockName("GoldSapling");
 
 	public static Item salt = new ItemMaterials(5000, 0, "salt");
 	public static Item pepper = new ItemMaterials(5001, 1, "pepper");
 	public static Item filter = new ItemFilter(5002);
 
 	@Instance
 	public static CuisineMod instance;
 
 	@SidedProxy(clientSide = "net.mrblockplacer.JM.CuisineMod.ClientProxy", serverSide = "net.mrblockplacer.JM.CuisineMod.CommonProxy")
 	public static CommonProxy proxy;
 
 	@PreInit
 	public void preInitializationEvent(FMLPreInitializationEvent event) {
 
 	}
 
 	// TODO The thing that neither of us want to do: GUI, goo-ey, graphical user
 	// interface, TMAO (the most annoying object)
 
 	@Init
 	public void loadEvent(FMLInitializationEvent event) {
 
 		LanguageRegistry.addName(saltslab, "Salt Slab");
 		GameRegistry.registerBlock(saltslab);
 		LanguageRegistry.addName(saltblock, "Salt Block");
 		GameRegistry.registerBlock(saltblock);
 		LanguageRegistry.addName(saltcollectorblock, "Salt Collector");
 		GameRegistry.registerBlock(saltcollectorblock);
 		LanguageRegistry.addName(goldLog, "Gold Log");
 		GameRegistry.registerBlock(goldLog);
 		LanguageRegistry.addName(goldLeaf, "Gold Leaf");
 		GameRegistry.registerBlock(goldLeaf);
 		LanguageRegistry.addName(goldSapling, "Gold Sapling");
 		GameRegistry.registerBlock(goldSapling);

 		GameRegistry.registerTileEntity(TileEntitySaltCollector.class, "TESaltCollector");
 
 		LanguageRegistry.addName(salt, "Salt");
 		LanguageRegistry.addName(pepper, "Pepper");
 		LanguageRegistry.addName(filter, "Filter");
 
 		GameRegistry.registerWorldGenerator(testgen);
 
 		ItemStack saltstack = new ItemStack(salt);
 		ItemStack saltblockstack = new ItemStack(saltblock);
 		ItemStack filterstack = new ItemStack(filter);
 		ItemStack stickstack = new ItemStack(Item.stick);
 		ItemStack stringstack = new ItemStack(Item.silk);
 		ItemStack woolstack = new ItemStack(Block.cloth);
 		ItemStack plankstack = new ItemStack(Block.planks);
 		ItemStack saltcollectorblockstack = new ItemStack(saltcollectorblock);
 		ItemStack cheststack = new ItemStack(Block.chest);
 
 		GameRegistry.addRecipe(saltblockstack, new Object[] { "xxx", "xxx", "xxx", Character.valueOf('x'), saltstack });
 		GameRegistry.addRecipe(filterstack, new Object[] { "xyx", "yzy", "xyx", Character.valueOf('x'), stickstack, Character.valueOf('y'), stringstack, Character.valueOf('z'), woolstack });
 		GameRegistry.addRecipe(filterstack, new Object[] { "xyx", "yzy", "xyx", Character.valueOf('y'), stickstack, Character.valueOf('x'), stringstack, Character.valueOf('z'), woolstack });
 		GameRegistry.addRecipe(saltcollectorblockstack, new Object[] { "xyx", "yzy", "xyx", 'x', plankstack, 'y', filterstack, 'z', cheststack });
 
 		GameRegistry.addShapelessRecipe(new ItemStack(salt, 9), saltblockstack);
 
 	}
 
 	@PostInit
 	public void postInitializationEvent(FMLPostInitializationEvent event) {
 	}
 
 }
