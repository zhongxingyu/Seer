 package godsandtitans.core;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import godsandtitans.blocks.BlockAmberActivator;
 import godsandtitans.blocks.BlockAmberOre;
 import godsandtitans.blocks.BlockAmberPortal;
 import godsandtitans.blocks.BlockAmberStone;
 import godsandtitans.blocks.BlockEmberActivator;
 import godsandtitans.blocks.BlockEmberOre;
 import godsandtitans.blocks.BlockEmberPortal;
 import godsandtitans.blocks.BlockEmberStone;
 import godsandtitans.common.CommonProxyGT;
 import godsandtitans.items.ItemAmberGem;
 import godsandtitans.items.ItemAmberMatrix;
 import godsandtitans.items.ItemEmberGem;
 import godsandtitans.items.ItemEmberMatrix;
 import godsandtitans.lib.ModRef;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = ModRef.MOD_ID, name = ModRef.MOD_NAME, version = ModRef.VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class GT
 {
 	@SidedProxy(clientSide = "godsandtitans.client.ClientProxyGT", serverSide = "godsandtitans.common.CommonProxyGT")
 	public static CommonProxyGT proxy;
 
 	// Blocks
 	Block emberOre;
 	Block amberOre;
 
 	Block emberStone;
 	Block amberStone;
 
 	Block emberActivator;
 	Block amberActivator;
 
 	Block emberPortal;
 	Block amberPortal;
 	// Blocks
 
	
	
 	// Items
 	Item emberGem;
 	Item amberGem;
 
 	Item emberMatrix;
 	Item amberMatrix;
 	// Items
 
	
	
 	// Mobs/Entities
 	// Mobs/Entities
 
 	@EventHandler
 	public void startup(FMLPreInitializationEvent event)
 	{
 		//Blocks
 		emberOre = new BlockEmberOre(700);
 		GameRegistry.registerBlock(emberOre, ModRef.MOD_ID + emberOre.getUnlocalizedName());
 		LanguageRegistry.addName(emberOre, "Ember Ore");
 
 		amberOre = new BlockAmberOre(701);
 		GameRegistry.registerBlock(amberOre, ModRef.MOD_ID + amberOre.getUnlocalizedName());
 		LanguageRegistry.addName(amberOre, "Amber Ore");
 
 
 		emberStone = new BlockEmberStone(702);
 		GameRegistry.registerBlock(emberStone, ModRef.MOD_ID + emberStone.getUnlocalizedName());
 		LanguageRegistry.addName(emberStone, "Ember Stone");
 
 		amberStone = new BlockAmberStone(703);
 		GameRegistry.registerBlock(amberStone, ModRef.MOD_ID + amberStone.getUnlocalizedName());
 		LanguageRegistry.addName(amberStone, "Amber Stone");
 
 
 		emberActivator = new BlockEmberActivator(704);
 		GameRegistry.registerBlock(emberActivator, ModRef.MOD_ID + emberActivator.getUnlocalizedName());
 		LanguageRegistry.addName(emberActivator, "Ember Portal Activation Stone");
 
 		amberActivator = new BlockAmberActivator(705);
 		GameRegistry.registerBlock(amberActivator, ModRef.MOD_ID + amberActivator.getUnlocalizedName());
 		LanguageRegistry.addName(amberActivator, "Amber Portal Activation Stone");
 
 
 		emberPortal = new BlockEmberPortal(706);
 		GameRegistry.registerBlock(emberPortal, ModRef.MOD_ID + emberPortal.getUnlocalizedName());
 		LanguageRegistry.addName(emberPortal, "Ember Portal");
 
 		amberPortal = new BlockAmberPortal(707);
 		GameRegistry.registerBlock(amberPortal, ModRef.MOD_ID + amberPortal.getUnlocalizedName());
 		LanguageRegistry.addName(amberPortal, "Amber Portal");
 		//Blocks
 
		
		
 		//Items
 		emberGem = new ItemEmberGem(3700).setUnlocalizedName("emberGem");
 		LanguageRegistry.addName(emberGem, "Ember Gem");
 
 		amberGem = new ItemAmberGem(3701).setUnlocalizedName("amberGem");
 		LanguageRegistry.addName(amberGem, "Amber Gem");
 
 		emberMatrix = new ItemEmberMatrix(3702).setUnlocalizedName("emberMatrix");
 		LanguageRegistry.addName(emberMatrix, "Ember Matrix Crystal");
 
 		amberMatrix = new ItemAmberMatrix(3703).setUnlocalizedName("amberMatrix");
 		LanguageRegistry.addName(amberMatrix, "	Amber Matrix Crystal");
 		//Items
 
		
		
 		//Mobs / Entities
 		//Mobs / Entities
 
		
		
 		//Recipes
 		//Recipes
 
		
		
 		//KeyBinds
 		//KeyBinds
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 
 	}
 }
