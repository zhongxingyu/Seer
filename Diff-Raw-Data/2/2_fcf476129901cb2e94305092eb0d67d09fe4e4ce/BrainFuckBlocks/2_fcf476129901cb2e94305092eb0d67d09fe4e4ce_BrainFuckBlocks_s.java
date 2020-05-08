 package com.dmillerw.brainFuckBlocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
 import com.dmillerw.brainFuckBlocks.block.BlockHandler;
 import com.dmillerw.brainFuckBlocks.block.BlockIDs;
 import com.dmillerw.brainFuckBlocks.core.CommonProxy;
 import com.dmillerw.brainFuckBlocks.core.CreativeTabBrainFuck;
 import com.dmillerw.brainFuckBlocks.helper.LogHelper;
 import com.dmillerw.brainFuckBlocks.item.ItemHandler;
 import com.dmillerw.brainFuckBlocks.item.ItemIDs;
 import com.dmillerw.brainFuckBlocks.lib.ModInfo;
 import com.dmillerw.brainFuckBlocks.lib.UserPreferences;
 import com.dmillerw.brainFuckBlocks.network.BFPacketHandler;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid=ModInfo.MOD_ID, name=ModInfo.MOD_NAME, version=ModInfo.MOD_VERSION)
 @NetworkMod(serverSideRequired=false, clientSideRequired=true, packetHandler=BFPacketHandler.class)
 public class BrainFuckBlocks {
 	@Instance(ModInfo.MOD_ID)
 	public static BrainFuckBlocks instance;
 	@SidedProxy(serverSide="com.dmillerw.brainFuckBlocks.core.CommonProxy", clientSide="com.dmillerw.brainFuckBlocks.client.ClientProxy")
 	public static CommonProxy proxy;
 	
 	public static CreativeTabs creativeTabBF = new CreativeTabBrainFuck("brainFuck");
 	
 	public static int wireRenderID = 0;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent e) {
 		//Config stuffs
 		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
 		try {
 			config.load();
 			
 			BlockIDs.bfCodeID = config.getBlock("brainFuckCodeBlockID", BlockIDs.bfCodeDefaultID).getInt();
 			BlockIDs.bfCPUID = config.getBlock("bfCPU", BlockIDs.bfCPUDefaultID).getInt();
 			BlockIDs.bfWireID = config.getBlock("bfWire", BlockIDs.bfWireDefaultID).getInt();
 			BlockIDs.bfPeripheralID = config.getBlock("bfPeripheral", BlockIDs.bfPeriphperalDefaultID).getInt();
 			
 			Property craftingEnable = config.get(Configuration.CATEGORY_GENERAL, "codeBlockCraftingEnabled", UserPreferences.codeBlockCraftingEnableDefault);
 			craftingEnable.comment = "Should code blocks have crafting recipes? If enabled, the blocks will drop themselves when broken. If false, they'll drop nothing and a code block creation item will exist.";
 			UserPreferences.codeBlockCraftingEnable = craftingEnable.getBoolean(UserPreferences.codeBlockCraftingEnableDefault);
 			
 			ItemIDs.bfWrenchID = config.getItem("bfWrench", ItemIDs.bfWrenchDefaultID).getInt();
 			ItemIDs.bfCodeWriterID = config.getItem("bfCodeWriter", ItemIDs.bfCodeWriterDefaultID).getInt();
 			ItemIDs.bfCraftingComponentID = config.getItem("bfCraftingComponent", ItemIDs.bfCraftingComponentDefaultID).getInt();
 		} catch(Exception ex) {
 			LogHelper.log("Failed to load config. Assuming defaults!");
 			
 			BlockIDs.bfCodeID = BlockIDs.bfCodeDefaultID;
 			BlockIDs.bfCPUID = BlockIDs.bfCPUDefaultID;
 			BlockIDs.bfWireID = BlockIDs.bfWireDefaultID;
 			BlockIDs.bfPeripheralID = BlockIDs.bfPeriphperalDefaultID;
 			
 			UserPreferences.codeBlockCraftingEnable = UserPreferences.codeBlockCraftingEnableDefault;
 			
 			ItemIDs.bfWrenchID = ItemIDs.bfWrenchDefaultID;
 			ItemIDs.bfCodeWriterID = ItemIDs.bfCodeWriterDefaultID;
 			ItemIDs.bfCraftingComponentID = ItemIDs.bfCraftingComponentDefaultID;
 		} finally {
 			if (config.hasChanged()) {
 				config.save();
 			}
 		}
 	}
 	
 	@Init
 	public void init(FMLInitializationEvent e) {
 		//Initializing logger
 		LogHelper.init();
 		
 		//Adds proper localization string for creative tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.brainFuck", "BrainFuck Blocks");
 		
 		//Initializes blocks
 		BlockHandler.init();
 		
 		//Initializes items
 		ItemHandler.init();
 		
 		//Initializes TileEntities
 		proxy.registerTileEntities();
 		
 		//Initializes recipes
 		initializeRecipes();
 	}
 
 	public static void initializeRecipes() {
 		/* BLOCKS */
 		//Machine casing
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, 1, 8), new Object[] {"RRR", "RCR", "DID", 'R', Item.redstone, 'C', new ItemStack(ItemHandler.bfCraftingComponent, 1, 2), 'I', Block.blockSteel});
 		//CPU
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCPU), new Object[] {"T", "A", "M", 'T', new ItemStack(ItemHandler.bfCraftingComponent, 1, 4), 'A', new ItemStack(ItemHandler.bfCraftingComponent, 1, 3), 'M', new ItemStack(BlockHandler.bfCode, 1, 8)});
 		//Wire
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfWire, 16), new Object[] {" B ", "BRB", " B ", 'B', new ItemStack(Block.cloth, 1, 15), 'R', Item.redstone});
 		//Redstone Data Interpreter
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfPeripheral, 1, 0), new Object[] {"R", "M", 'R', Block.blockRedstone, 'M', new ItemStack(BlockHandler.bfCode, 1, 4)});
 		//Redstone Input Interpreter
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfPeripheral, 1, 1), new Object[] {"R", "M", 'R', Block.blockRedstone, 'M', new ItemStack(BlockHandler.bfCode, 1, 5)});
 		//Chat Data Interpreter
 		GameRegistry.addRecipe(new ItemStack(BlockHandler.bfPeripheral, 1, 2), new Object[] {"S", "M", 'S', Item.sign, 'M', new ItemStack(BlockHandler.bfCode, 1, 4)});
 		
 		//Code blocks
 		if (UserPreferences.codeBlockCraftingEnable) {
 			int CRAFTING_AMOUNT = 16;
 			
 			//Increment Pointer
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 0), new Object[] {"RII", "IIR", "RII", 'R', Item.redstone, 'I', Item.ingotIron});
 			
 			//Decrement Pointer
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 1), new Object[] {"IIR", "RII", "IIR", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Increment Data
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 2), new Object[] {"IRI", "RRR", "IRI", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Decrement Data
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 3), new Object[] {"III", "RRR", "III", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Output
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 4), new Object[] {"IRI", "RIR", "IRI", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Input
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 5), new Object[] {"IRI", "IRI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Bracket open
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 6), new Object[] {"RRI", "RII", "RRI", 'R', Item.redstone, 'I', Item.ingotIron});
 		
 			//Bracket close
 			GameRegistry.addRecipe(new ItemStack(BlockHandler.bfCode, CRAFTING_AMOUNT, 7), new Object[] {"IRR", "IIR", "IRR", 'R', Item.redstone, 'I', Item.ingotIron});
 		}
 		
 		/* CRAFTING COMPONENTS */
 		//Metal Spool
 		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCraftingComponent, 1, 0), new Object[] {" I ", "I I", " I ", 'I', Item.ingotIron});
 
 		//Strip of Paper
 		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCraftingComponent, 16, 1), new Object[] {"PPP", 'P', Item.paper});
 		
 		//Circut
 		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCraftingComponent, 1, 2), new Object[] {"RIR", "GIG", "LIL", 'R', Item.redstone, 'I', Item.ingotIron, 'G', Item.lightStoneDust, 'L', new ItemStack(Item.dyePowder, 1, 4)});
 		
 		//Advanced Circut
 		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCraftingComponent, 1, 3), new Object[] {"RRR", "RCR", "RRR", 'R', Item.redstone, 'C', new ItemStack(ItemHandler.bfCraftingComponent, 1, 2)});
 		
 		//Data Tape
 		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCraftingComponent, 1, 4), new Object[] {"SPS", 'S', new ItemStack(ItemHandler.bfCraftingComponent, 1, 0), 'P', new ItemStack(ItemHandler.bfCraftingComponent, 16, 1)}); 
 		
 		/* ITEMS */
 		//Wrench
		GameRegistry.addRecipe(new ItemStack(ItemHandler.bfWrench), new Object[] {"  RI", "BIR", "IB ", 'R', new ItemStack(Item.dyePowder, 1, 1), 'B', new ItemStack(Item.dyePowder, 1, 1), 'I', Item.ingotIron});
 	
 		if (!UserPreferences.codeBlockCraftingEnable) {
 			GameRegistry.addRecipe(new ItemStack(ItemHandler.bfCodeWriter), new Object[] {"III", "IGI", "SRS", 'I', Item.ingotIron, 'G', Block.thinGlass, 'S', Block.stone, 'R', Item.redstone});
 		}
 	}
 	
 }
