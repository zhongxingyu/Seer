 package tcc.MotherOfPearl;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockClay;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import tcc.MotherOfPearl.blocks.mopBlock;
 import tcc.MotherOfPearl.blocks.mopBlock2;
 import tcc.MotherOfPearl.blocks.mopBlock3;
 import tcc.MotherOfPearl.blocks.mopBlock4;
 import tcc.MotherOfPearl.blocks.mopBlock5;
 import tcc.MotherOfPearl.items.mopItem;
 import tcc.MotherOfPearl.items.mopItem10;
 import tcc.MotherOfPearl.items.mopItem11;
 import tcc.MotherOfPearl.items.mopItem12;
 import tcc.MotherOfPearl.items.mopItem13;
 import tcc.MotherOfPearl.items.mopItem14;
 import tcc.MotherOfPearl.items.mopItem2;
 import tcc.MotherOfPearl.items.mopItem3;
 import tcc.MotherOfPearl.items.mopItem4;
 import tcc.MotherOfPearl.items.mopItem5;
 import tcc.MotherOfPearl.items.mopItem6;
 import tcc.MotherOfPearl.items.mopItem7;
 import tcc.MotherOfPearl.items.mopItem8;
 import tcc.MotherOfPearl.items.mopItem9;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModInfo {
 
 	public static final String NAME = "Mother of Pearl";
 	public static final String MOD_ID = "mop";
 	public static final String version = "v1.0-1.6.2";
 	public static final String PROXY_PATH = "tcc.MotherOfPearl.proxy";
 	public static Item blackPearl;
 	public static Item bootsPearl;
 	public static Item bowPearl;
 	public static Item braShell;
 	public static Item chestplatePearl;
 	public static Item helmetPearl;
 	public static Item knifePearl;
 	public static Item leggingsPearl;
 	public static Item oyster;
 	public static Item oysterMeat;
 	public static Item pearl;
 	public static Item shell;
 	public static Item shovelPearl;
 	public static Item swordPearl;
 	public static Block pearlstone;
 	public static Block blackPearlstone;
 	public static Block pearlstoneBrick;
 	public static Block blackPearlstoneBrick;
 	public static Block clayOyster;
 	
 		
 	public static final ItemStack inkSac = new ItemStack(Item.dyePowder, 1, 0);
 	public static final ItemStack stick = new ItemStack(Item.stick, 1, 0);
 	public static final ItemStack ingotIron = new ItemStack(Item.ingotIron, 1, 0);
 	public static final ItemStack string = new ItemStack(Item.silk, 1, 0);
 	public static final ItemStack stone = new ItemStack(Block.stone, 1, 0);
 	public static final ItemStack stoneBrick = new ItemStack(Block.stoneBrick, 1, 0);
 
 	public static void initItem(){
 		blackPearl = new mopItem(ModMain.blackPearlID);
 		oyster = new mopItem9(ModMain.oysterID);
 		oysterMeat = new mopItem10(ModMain.oysterMeatID, true, 4);
 		pearl = new mopItem11(ModMain.pearlID);
 		shell = new mopItem12(ModMain.shellID);
 	}
 	public static EnumToolMaterial pearlShovel =
 			EnumHelper.addToolMaterial("PearlShovel", 2, 256, 2f, 3f, 25);
 	public static EnumToolMaterial pearlSword =
 			EnumHelper.addToolMaterial("PearlSword", 2, 256, 1f, 2f, 25);
 	public static EnumToolMaterial pearlKnife =
 			EnumHelper.addToolMaterial("PearlKnife", 2, 1000, .5f, 1f, 25);
 		
 	public static void initTools(){
 		bowPearl = new mopItem3(ModMain.bowPearlID);
 		knifePearl = new mopItem7(ModMain.knifePearlID);
 		shovelPearl = new mopItem13(ModMain.shovelPearlID);
 		swordPearl = new mopItem14(ModMain.swordPearlID);
 	}
 	
 	public static EnumArmorMaterial pearlMaterial = 
 			EnumHelper.addArmorMaterial("PearlArmor", 21, new int[] { 3, 9, 6, 3 }, 25);
 	public static EnumArmorMaterial shellMaterial = 
 			EnumHelper.addArmorMaterial("ShellArmor", 5, new int[] { 0, 5, 0, 0 }, 8);
 	
 	public static void initArmor(){
 		bootsPearl = new mopItem2(ModMain.bootsPearlID, pearlMaterial, 0, 3, null);
 		braShell = new mopItem4(ModMain.braShellID, shellMaterial, 0, 1, null);
 		chestplatePearl = new mopItem5(ModMain.chestplatePearlID, pearlMaterial, 0, 1, null);
 		helmetPearl = new mopItem6(ModMain.helmetPearlID, pearlMaterial, 0, 0, null);
 		leggingsPearl = new mopItem8(ModMain.leggingsPearlID, pearlMaterial, 0, 2, null);
 	}
 	// Block Information Section  - Thanks to travi1997 on MinecraftForums.com for helping me figure out my BlockIDs problems.
 	public static void initBlock(){
 		
 		pearlstone = new mopBlock(ModMain.pearlstoneID, Material.rock);
 		MinecraftForge.setBlockHarvestLevel(pearlstone, "Pickaxe", 2);
 		GameRegistry.registerBlock(pearlstone, "pearlstone");
 		
 		blackPearlstone = new mopBlock2(ModMain.blackPearlstoneID, Material.rock);
 		MinecraftForge.setBlockHarvestLevel(blackPearlstone, "Pickaxe", 2);
 		GameRegistry.registerBlock(blackPearlstone, "blackPearlstone");
 		
 		pearlstoneBrick = new mopBlock3(ModMain.pearlstoneBrickID, Material.rock);
 		MinecraftForge.setBlockHarvestLevel(pearlstoneBrick, "Pickaxe", 2);
 		GameRegistry.registerBlock(pearlstoneBrick, "pearlstoneBrick");
 		
 		blackPearlstoneBrick = new mopBlock4(ModMain.blackPearlstoneBrickID, Material.rock);
 		MinecraftForge.setBlockHarvestLevel(blackPearlstoneBrick, "Pickaxe", 2);
 		GameRegistry.registerBlock(blackPearlstoneBrick, "blackPearlstoneBrick");
 
 		clayOyster = new mopBlock5(ModMain.clayOysterID, Material.clay);
		MinecraftForge.setBlockHarvestLevel(clayOyster, "Shovel", 1);
 		GameRegistry.registerBlock(clayOyster, "clayOyster");
 		
 	}
 	
 	public static void languageRegistry() {
 		LanguageRegistry.addName(blackPearl, "Black Pearl");
 		LanguageRegistry.addName(bootsPearl, "Pearly Boots");
 		LanguageRegistry.addName(bowPearl, "Pearl Bow");
 		LanguageRegistry.addName(braShell, "Shell Bra");
 		LanguageRegistry.addName(chestplatePearl, "Pearly Chestplate");
 		LanguageRegistry.addName(helmetPearl, "Pearly Helmet");
 		LanguageRegistry.addName(knifePearl, "Pearling Knife");
 		LanguageRegistry.addName(leggingsPearl, "Pearly leggings"); 
 		LanguageRegistry.addName(oyster, "Oyster");
 		LanguageRegistry.addName(oysterMeat, "Oyster Meat");
 		LanguageRegistry.addName(pearl, "Pearl");
 		LanguageRegistry.addName(shell, "Shell");
 		LanguageRegistry.addName(shovelPearl, "Pearl Shovel");
 		LanguageRegistry.addName(swordPearl, "Pearl Sword");
 		LanguageRegistry.addName(pearlstone, "Pearly Stone");
 		LanguageRegistry.addName(blackPearlstone, "Black Pearly Stone");
 		LanguageRegistry.addName(pearlstoneBrick, "Pearly Stone Brick");
 		LanguageRegistry.addName(blackPearlstoneBrick, "Black Pearly Stone Brick");
 		LanguageRegistry.addName(clayOyster, "Oyster Yielding Clay");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MoP", "en_US", "Mother of Pearl");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MoP_WIP", "en_US", "Mother of Pearl - Unfinished Items");
 	}
 	
 	public static void crafting() {
 		GameRegistry.addRecipe(new ItemStack(blackPearl, 8), 
 				"yyy", 
 				"yby", 
 				"yyy", 
 				'b', inkSac,
 				'y', pearl);
 		GameRegistry.addRecipe(new ItemStack(bootsPearl, 1), 
 				"x x", 
 				"y y", 
 				'x', pearl,
 				'y', ingotIron);
 		GameRegistry.addRecipe(new ItemStack(bowPearl, 1), 
 				"zx ", 
 				"z y", 
 				"zx ", 
 				'x', stick,
 				'y', pearl,
 				'z', string);
 		//GameRegistry.addRecipe(new ItemStack(bowPearl, 1), 
 		//		" xz", 
 		//		"y z", 
 		//		" xz", 
 		//		'x', stick,
 		//		'y', pearl,
 		//		'z', string);
 		GameRegistry.addRecipe(new ItemStack(braShell, 1), 
 				"yxy", 
 				'x', string,
 				'y', shell);
 		GameRegistry.addRecipe(new ItemStack(chestplatePearl, 1), 
 				"x x", 
 				"yxy", 
 				"yyy", 
 				'x', pearl,
 				'y', ingotIron);
 		GameRegistry.addRecipe(new ItemStack(helmetPearl, 1),
 				"xyx", 
 				"y y", 
 				'x', pearl,
 				'y', ingotIron);
 		GameRegistry.addRecipe(new ItemStack(knifePearl, 1), 
 				"y", 
 				"x", 
 				'x', stick,
 				'y', pearl);
 		GameRegistry.addRecipe(new ItemStack(leggingsPearl, 1), 
 				"yxy", 
 				"y y", 
 				"x x", 
 				'x', pearl,
 				'y', ingotIron);
 		GameRegistry.addSmelting(oyster.itemID, new ItemStack(oysterMeat), 0.1f);
 		GameRegistry.addRecipe(new ItemStack(pearl, 8), 
 				"yyy", 
 				"yxy", 
 				"yyy", 
 				'x', stone,
 				'y', oyster);
 		GameRegistry.addShapelessRecipe(new ItemStack(shell, 2), oyster);
 		GameRegistry.addRecipe(new ItemStack(shovelPearl, 1), 
 				"y", 
 				"x", 
 				"x", 
 				'x', stick,
 				'y', pearl);
 		GameRegistry.addRecipe(new ItemStack(swordPearl, 1), 
 				"y", 
 				"y", 
 				"x", 
 				'x', stick,
 				'y', pearl);
 		GameRegistry.addRecipe(new ItemStack(pearlstone, 4), 
 				"byb", 
 				"yby", 
 				"byb",
 				'b', pearl,
 				'y', stone);
 		GameRegistry.addRecipe(new ItemStack(blackPearlstone, 4), 
 				"byb", 
 				"yby", 
 				"byb",
 				'b', blackPearl,
 				'y', stone);
 		GameRegistry.addRecipe(new ItemStack(pearlstoneBrick, 4), 
 				"byb", 
 				"yby", 
 				"byb",
 				'b', pearl,
 				'y', stoneBrick);
 		GameRegistry.addRecipe(new ItemStack(blackPearlstoneBrick, 4), 
 				"byb", 
 				"yby", 
 				"byb",
 				'b', blackPearl,
 				'y', stoneBrick);
 		GameRegistry.addRecipe(new ItemStack(pearlstoneBrick, 4), 
 				"yy", 
 				"yy", 
 				'y', pearlstone);
 		GameRegistry.addRecipe(new ItemStack(blackPearlstoneBrick, 4), 
 				"yy", 
 				"yy", 
 				'y', blackPearlstone);
 	}
 }
