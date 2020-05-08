 package mtb.core;
 
 import java.io.File;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import eurysmods.api.ICommonProxy;
 
 import mtb.blocks.BlockMTButton;
 import mtb.items.ItemMTButton;
 import mtb.network.MTBConnection;
 import mtb.tileentities.TileEntityMTButton;
 import net.minecraft.src.Block;
 import net.minecraft.src.EnumMobType;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.Configuration;
 
 public class MTBCore {
 	public static File configFile;
 	public static Configuration configuration;
 	public static boolean initialized = false;
 	
 
 	public static void initialize(ICommonProxy proxy) {
 		if (initialized)
 			return;
 		initialized = true;
 		MTBInit.initialize(proxy);
 	}
 
 	public static void addItems() {
 		MTBBlocks.mtButton.id = configurationProperties();
 		MTBBlocks.mtButton.me =
 				new BlockMTButton(
 						MTBBlocks.mtButton.id,
 						TileEntityMTButton.class,
 						0.5F,
 						Block.soundStoneFootstep,
 						true,
 						true,
 						"mtButton");
 		GameRegistry.registerTileEntity(TileEntityMTButton.class, "mtButton");
 		for (MTBItemButtons button : MTBItemButtons.values()) {
 			button.me = new ItemStack(MTBBlocks.mtButton.me, 1, button.stackID);
 		}
 	}
 
 	public static void registerBlocks() {
 		for (MTBBlocks block : MTBBlocks.values()) {
 			if (block != null && block.me != null) {
 				GameRegistry.registerBlock(block.me, ItemMTButton.class);
 				if (block.name != null) {
 					ModLoader.addName(block.me, block.name);
 				}
 			}
 		}
 	}
 
 	public static void addItemNames() {
 		for (MTBItemButtons button : MTBItemButtons.values()) {
 			if (button != null && button.me != null && button.name != null && !button.name
 					.isEmpty()) {
 				ModLoader.addName(button.me, button.name);
 			}
 		}
 	}
 
 	public static void addRecipes() {
 
 		GameRegistry.addRecipe(MTBItemButtons.oakPlank.me, new Object[] {
 				"X",
 				Character.valueOf('X'),
 				new ItemStack(Block.planks, 1, 0) });
 
 		GameRegistry.addRecipe(MTBItemButtons.sprucePlank.me, new Object[] {
 				"X",
 				Character.valueOf('X'),
 				new ItemStack(Block.planks, 1, 1) });
 
 		GameRegistry.addRecipe(MTBItemButtons.birchPlank.me, new Object[] {
 				"X",
 				Character.valueOf('X'),
 				new ItemStack(Block.planks, 1, 2) });
 
 		GameRegistry.addRecipe(MTBItemButtons.junglePlank.me, new Object[] {
 				"X",
 				Character.valueOf('X'),
 				new ItemStack(Block.planks, 1, 3) });
 
 		GameRegistry.addRecipe(MTBItemButtons.polishedStone.me, new Object[] {
 				"X",
 				Character.valueOf('X'),
 				Block.stoneSingleSlab });
 
 		GameRegistry.addRecipe(MTBItemButtons.cobbleStone.me, new Object[] {
 				"X",
 				"X",
 				Character.valueOf('X'),
 				Block.cobblestone });
 
 		GameRegistry.addRecipe(MTBItemButtons.iron.me, new Object[] {
 				"X",
 				"Y",
 				"X",
 				Character.valueOf('X'),
 				Item.ingotIron,
 				Character.valueOf('Y'),
 				Block.stoneButton });
 
 		GameRegistry.addRecipe(MTBItemButtons.gold.me, new Object[] {
 				"X",
 				"Y",
 				"X",
 				Character.valueOf('X'),
 				Item.ingotGold,
 				Character.valueOf('Y'),
 				MTBItemButtons.iron.me });
 
 		GameRegistry.addRecipe(MTBItemButtons.diamond.me, new Object[] {
 				"X",
 				"Y",
 				"X",
 				Character.valueOf('X'),
 				Item.diamond,
 				Character.valueOf('Y'),
 				MTBItemButtons.gold.me });
 
 		FurnaceRecipes.smelting().addSmelting(
 				MTBBlocks.mtButton.id,
 				MTBItemButtons.iron.stackID,
 				new ItemStack(Item.ingotIron, 2),
 				1);
 		FurnaceRecipes.smelting().addSmelting(
 				MTBBlocks.mtButton.id,
 				MTBItemButtons.gold.stackID,
 				new ItemStack(Item.ingotGold, 2),
 				2);
 		FurnaceRecipes.smelting().addSmelting(
 				MTBBlocks.mtButton.id,
 				MTBItemButtons.diamond.stackID,
 				new ItemStack(Item.diamond, 2),
 				3);
 	}
 
 	public static int configurationProperties() {
 		configuration.load();
 		MTBBlocks.mtButton.id = Integer.parseInt(
 				configuration.get(
 						Configuration.CATEGORY_BLOCK,
 						"mtButton",
 						143).value);
 		MTBBlocks.mtButton.name = "Multi-Textured Button";
 		MTBItemButtons.iron.name = "Iron-Clad Button";
 		MTBItemButtons.iron.stackID = 0;
 		MTBItemButtons.iron.setTextureIndex(22);
 		MTBItemButtons.iron.setSensible(false);
 		MTBItemButtons.iron.setBlockHardness(2.5F);
 		MTBItemButtons.gold.name = "Gold-Plated Button";
 		MTBItemButtons.gold.stackID = 1;
 		MTBItemButtons.gold.setTextureIndex(23);
 		MTBItemButtons.gold.setSensible(false);
 		MTBItemButtons.gold.setBlockHardness(1.5F);
		MTBItemButtons.diamond.name = "Diamond-Encrusted Button";
 		MTBItemButtons.diamond.stackID = 2;
 		MTBItemButtons.diamond.setTextureIndex(24);
 		MTBItemButtons.diamond.setSensible(false);
 		MTBItemButtons.diamond.setBlockHardness(2.5F);
 		MTBItemButtons.oakPlank.name = "Oak Wood Button";
 		MTBItemButtons.oakPlank.stackID = 3;
 		MTBItemButtons.oakPlank.setTextureIndex(4);
 		MTBItemButtons.oakPlank.setSensible(true);
 		MTBItemButtons.oakPlank.setBlockHardness(0.5F);
 		MTBItemButtons.sprucePlank.name = "Spruce Wood Button";
 		MTBItemButtons.sprucePlank.stackID = 4;
 		MTBItemButtons.sprucePlank.setTextureIndex(198);
 		MTBItemButtons.sprucePlank.setSensible(true);
 		MTBItemButtons.sprucePlank.setBlockHardness(0.5F);
 		MTBItemButtons.birchPlank.name = "Birch Wood Button";
 		MTBItemButtons.birchPlank.stackID = 5;
 		MTBItemButtons.birchPlank.setTextureIndex(214);
 		MTBItemButtons.birchPlank.setSensible(true);
 		MTBItemButtons.birchPlank.setBlockHardness(0.5F);
 		MTBItemButtons.junglePlank.name = "Jungle Wood Button";
 		MTBItemButtons.junglePlank.stackID = 6;
 		MTBItemButtons.junglePlank.setTextureIndex(199);
 		MTBItemButtons.junglePlank.setSensible(true);
 		MTBItemButtons.junglePlank.setBlockHardness(0.5F);
 		MTBItemButtons.polishedStone.name = "Polished Stone Button";
 		MTBItemButtons.polishedStone.stackID = 7;
 		MTBItemButtons.polishedStone.setTextureIndex(6);
 		MTBItemButtons.polishedStone.setSensible(false);
 		MTBItemButtons.polishedStone.setBlockHardness(1.0F);
 		MTBItemButtons.cobbleStone.name = "Cobblestone Button";
 		MTBItemButtons.cobbleStone.stackID = 8;
 		MTBItemButtons.cobbleStone.setTextureIndex(16);
 		MTBItemButtons.cobbleStone.setSensible(false);
 		MTBItemButtons.cobbleStone.setBlockHardness(1.0F);
 		configuration.save();
 		return MTBBlocks.mtButton.id;
 	}
 }
