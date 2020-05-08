 /*
  * The Bouncing Block Mod was made in a joint effort by TechnicalParadox & Gim949
  * "This mod adds new blocks to the game that allow for even more possibilities in
  * the world of Minecraft!"
  */
 
 package tech.bouncingblockmod;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.command.ICommand;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.WrongUsageException;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemShears;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 // Info for Minecraft Forge
 @Mod (modid= Reference.MOD_ID, name= Reference.MOD_NAME, version= Reference.MOD_VERSION)
 @NetworkMod (clientSideRequired=true, serverSideRequired=false)
 
 public class BouncingBlockModMain {
 	
 	// Item IDs
 	int bouncingBlockID;
 	int paddingBlockID;
 	int launcherBlockID;
 	int speedBlockID;
 	int cannonBlockNorthID;
 	int cannonBlockEastID;
 	int cannonBlockSouthID;
 	int cannonBlockWestID;
 	
 	// Blocks
 	public static BouncingBlock bouncingBlock;
 	public static PaddingBlock paddingBlock;
 	public static LauncherBlock launcherBlock;
 	public static SpeedBlock speedBlock;
 	public static CannonBlockNorth cannonBlockNorth;
 	public static CannonBlockEast cannonBlockEast;
 	public static CannonBlockSouth cannonBlockSouth;
 	public static CannonBlockWest cannonBlockWest;
 	
 	// Instance of Mod
 	public static BouncingBlockModMain instance;
 	
 	// PreInitialization Events
 	@EventHandler
 	public void preInit (FMLPreInitializationEvent event) {
 		
 		// Config Code
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		
 		// Load config
 		config.load();
 		
 		// Get Block IDs from config file
 		bouncingBlockID = config.get("Block IDs", "Bouncing Block ID", 1475).getInt();
 		paddingBlockID = config.get("Block IDs", "Padding Block ID", 1476).getInt();
 		launcherBlockID = config.get("Block IDs", "Launcher Block ID", 1477).getInt();
 		speedBlockID = config.get("Block IDs", "Speed Block ID", 1478).getInt();
 		cannonBlockNorthID = config.get("Block IDs", "Cannon Block North ID", 1479).getInt();
 		cannonBlockEastID = config.get("Block IDs", "Cannon Block East ID", 1480).getInt();
 		cannonBlockSouthID = config.get("Block IDs", "Cannon Block South ID", 1481).getInt();
 		cannonBlockWestID = config.get("Block IDs", "Cannon Block West ID", 1482).getInt();
 		
 		// Save config
 		config.save();
 		
 		// Initialize Blocks
 		this.bouncingBlock = new BouncingBlock (bouncingBlockID, Material.ground);
 		this.paddingBlock = new PaddingBlock (paddingBlockID, Material.ground);
 		this.launcherBlock = new LauncherBlock (launcherBlockID, Material.ground);
 		this.speedBlock = new SpeedBlock (speedBlockID, Material.ground);
 		this.cannonBlockNorth = new CannonBlockNorth (cannonBlockNorthID, Material.ground);
 		this.cannonBlockEast = new CannonBlockEast (cannonBlockEastID, Material.ground);
 		this.cannonBlockSouth = new CannonBlockSouth (cannonBlockSouthID, Material.ground);
 		this.cannonBlockWest = new CannonBlockWest (cannonBlockWestID, Material.ground);
 		
 		// Information needed for the Bouncing Block
 		LanguageRegistry.addName(bouncingBlock, "Bouncing Block");
 		MinecraftForge.setBlockHarvestLevel(bouncingBlock, "anything", 0);
 		GameRegistry.registerBlock(bouncingBlock, "bouncingBlock");
 		
 		// Information needed for the Padding Block
 		LanguageRegistry.addName(paddingBlock, "Padding Block");
 		MinecraftForge.setBlockHarvestLevel(paddingBlock, "anything", 0);
 		GameRegistry.registerBlock(paddingBlock, "paddingBlock");
 		
 		// Information needed for the Launcher Block
 		LanguageRegistry.addName(launcherBlock, "Launcher Block");
 		MinecraftForge.setBlockHarvestLevel(launcherBlock, "anything", 0);
 		GameRegistry.registerBlock(launcherBlock, "launcherBlock");
 		
 		// Information needed for the Speed Block
 		LanguageRegistry.addName(speedBlock, "Speed Block");
 		MinecraftForge.setBlockHarvestLevel(speedBlock, "anything", 0);
 		GameRegistry.registerBlock(speedBlock, "speedBlock");
 		
 		// Information needed for the Cannon Block - North
 		LanguageRegistry.addName(cannonBlockNorth, "Cannon Block - North");
 		MinecraftForge.setBlockHarvestLevel(cannonBlockNorth, "anything", 0);
 		GameRegistry.registerBlock(cannonBlockNorth, "cannonBlockNorth");
 		
 		// Information needed for the Cannon Block - East
 		LanguageRegistry.addName(cannonBlockEast, "Cannon Block - East");
 		MinecraftForge.setBlockHarvestLevel(cannonBlockEast, "anything", 0);
 		GameRegistry.registerBlock(cannonBlockEast, "cannonBlockEast");
 		
 		// Information needed for the Cannon Block - South
 		LanguageRegistry.addName(cannonBlockSouth, "Cannon Block - South");
 		MinecraftForge.setBlockHarvestLevel(cannonBlockSouth, "anything", 0);
 		GameRegistry.registerBlock(cannonBlockSouth, "cannonBlockSouth");
 		
 		// Information needed for the Cannon Block- West
 		LanguageRegistry.addName(cannonBlockWest, "Cannon Block - West");
 		MinecraftForge.setBlockHarvestLevel(cannonBlockWest, "anything", 0);
 		GameRegistry.registerBlock(cannonBlockWest, "cannonBlockWest");
 		
 		// Crafting Recipes
 			// Bouncing Block
 			GameRegistry.addRecipe(new ItemStack(bouncingBlock, 3),
 					"xsx",
 					"sys",
 					"xsx",
 					'x', new ItemStack(Block.cloth, 1, 15), 's', Item.silk, 'y', Item.slimeBall
 					);
 			// Padding Block
 			GameRegistry.addRecipe(new ItemStack(paddingBlock, 3),
 					"zsz",
 					"szs",
 					"zsz",
 					'z', new ItemStack(Block.cloth, 1, 0), 's', Item.silk);
 			// Launcher Block
 			GameRegistry.addRecipe(new ItemStack(launcherBlock, 3),
 					"isi",
 					"ipi",
 					"iri",
					'i', Item.ingotIron, 'p', Block.pistonBase, 'r', Block.torchRedstoneIdle, 's', Block.pressurePlateIron);
 			// Cannon Block - North
 			GameRegistry.addRecipe(new ItemStack(cannonBlockNorth, 3),
 					"sps",
 					"iri",
 					"iii",
					'i', Item.ingotIron, 'p', Block.pistonBase, 'r', Block.torchRedstoneIdle, 's', Block.pressurePlateIron);
 			// Cannon Block - East
 			GameRegistry.addRecipe(new ItemStack(cannonBlockEast, 3),
 					"iis",
 					"irp",
 					"iis",
					'i', Item.ingotIron, 'p', Block.pistonBase, 'r', Block.torchRedstoneIdle, 's', Block.pressurePlateIron);
 			// Cannon Block - South
 			GameRegistry.addRecipe(new ItemStack(cannonBlockSouth, 3),
 					"iii",
 					"iri",
 					"sps",
					'i', Item.ingotIron, 'p', Block.pistonBase, 'r', Block.torchRedstoneIdle, 's', Block.pressurePlateIron);
 			// Cannon Block - West
 			GameRegistry.addRecipe(new ItemStack(cannonBlockWest, 3),
 					"sii",
 					"pri",
 					"sii",
					'i', Item.ingotIron, 'p', Block.pistonBase, 'r', Block.torchRedstoneIdle, 's', Block.pressurePlateIron);
 			// Speed Block - Recipe 1
 			GameRegistry.addRecipe(new ItemStack(speedBlock, 3),
 					"iii",
 					"sss",
 					"sss",
 					'i', Block.ice, 's', Item.snowball);
			// Speed Block - Recipe 2
			GameRegistry.addRecipe(new ItemStack(speedBlock, 3),
					"sss",
					"sls",
					"sss",
					's', Block.snow, 'l', (new ItemStack(Item.dyePowder, 1, 4)));
 	}
 	
 }
