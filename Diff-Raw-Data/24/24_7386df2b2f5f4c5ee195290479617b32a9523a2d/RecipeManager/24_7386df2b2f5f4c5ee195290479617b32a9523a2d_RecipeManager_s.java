 package ru.ulmc.extender;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import ru.ulmc.extender.item.ItemManager;
 import ru.ulmc.extender.proxy.CommonProxy;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class RecipeManager {
 	
 	private static void addShapeless(Item item, int quantity, Object... combination) {
 		GameRegistry.addShapelessRecipe(new ItemStack(item, quantity), combination);
 	}
 	
 	private static void addSmelting(Item item, int quantity, float xp, Item inputitem) {
 		GameRegistry.addSmelting(inputitem.itemID, new ItemStack(item, quantity), xp);
 	}
 
 	public static void init(CommonProxy proxy) {
 		/*GameRegistry.addRecipe(new ItemStack(blockReinforcedConcrete, 1), "xxx", "yyy", "xxx", 'x', Block.fenceIron, 'y',
 				itemCementSack);*/
 		/*
 		
 		*/
 		Item ironBlank = ItemManager.getItem("ironBlank");
 		Item ironKey = ItemManager.getItem("ironKey");
 		Item goldenKey = ItemManager.getItem("goldenKey");
 		Item diamondKey = ItemManager.getItem("diamondKey");
 		Item diamondDust = ItemManager.getItem("diamondDust");
 		Item goldDust = ItemManager.getItem("goldDust");
 		Item leatherStrap = ItemManager.getItem("leatherStrap");
 		Item leatherCorset = ItemManager.getItem("leatherCorset");
 		Item spool = ItemManager.getItem("spool");
 		Item goldSpool = ItemManager.getItem("goldSpool");
 		Item fabricStrap = ItemManager.getItem("fabricStrap");
 		Item fabricRoll = ItemManager.getItem("fabricRoll");
 		Item obsidianAlloyBlank = ItemManager.getItem("obsidianAlloyBlank");
 		Item obsidianBrick = ItemManager.getItem("obsidianBrick");
 		Item obsidianAlloyIngot = ItemManager.getItem("obsidianAlloyIngot");
 		Item lambFried = ItemManager.getItem("lambFried");
 		
 		Item diamondSpool = ItemManager.getItem("diamondSpool");
 		Item goldFabricStrap = ItemManager.getItem("goldFabricStrap");
 		Item goldFabricRoll = ItemManager.getItem("goldFabricRoll");
 		Item diamondFabricStrap = ItemManager.getItem("diamondFabricStrap");
 		Item diamondFabricRoll = ItemManager.getItem("diamondFabricRoll");
 		Item chevron = ItemManager.getItem("chevron");
 		Item chevronWithGoldThread = ItemManager.getItem("chevronWithGoldThread");
 		Item chevronWithDiamondThread = ItemManager.getItem("chevronWithDiamondThread");
 		Item goldChevronWithGoldThread = ItemManager.getItem("goldChevronWithGoldThread");
 		Item goldChevronWithDiamondThread = ItemManager.getItem("goldChevronWithDiamondThread");
 		Item diamondChevronWithDiamondThread = ItemManager.getItem("diamondChevronWithDiamondThread");
 		Item diamondChevronWithGoldThread = ItemManager.getItem("diamondChevronWithGoldThread");
 		Item medivalSymbol = ItemManager.getItem("medivalSymbol");
 		Item technoSymbol = ItemManager.getItem("technoSymbol");
 		
 		Item jerkedBeef = ItemManager.getItem("jerkedBeef");
 		Item jerkedPork = ItemManager.getItem("jerkedPork");
 		Item jerkedLamb = ItemManager.getItem("jerkedLamb");
 		Item salt = ItemManager.getItem("salt");
 		Item lambRawMeat = ItemManager.getItem("lambRawMeat");
 		Item porkBelly = ItemManager.getItem("porkBelly");
 		Item salo = ItemManager.getItem("salo");
 		Item saltCrystal = ItemManager.getItem("saltCrystal");
 		
 		Item cementMix = ItemManager.getItem("cementMix");
 		Item cementSack = ItemManager.getItem("cementSack");
 		
 		addShapeless(diamondDust, 2, Item.diamond);
 		addShapeless(goldDust, 1, Item.goldNugget, Item.goldNugget, Item.goldNugget, Item.goldNugget);
 		
 		addShapeless(ironBlank, 4, Item.ingotIron);
 		addShapeless(ironKey, 1, ironBlank);
 		addShapeless(goldenKey, 1, ironBlank, goldDust);
 		addShapeless(diamondKey, 1, ironBlank, diamondDust);
 		addShapeless(spool, 1, Item.silk, Item.silk, Item.silk);
 		addShapeless(leatherStrap, 4, Item.leather);
 		addShapeless(fabricStrap, 1, spool, spool, spool);
 		addShapeless(fabricRoll, 1, fabricStrap, fabricStrap, fabricStrap, fabricStrap, fabricStrap, fabricStrap);		
 		addShapeless(obsidianAlloyBlank, 1, Item.ingotIron, obsidianBrick);
 		addShapeless(obsidianBrick, 2, Block.obsidian);
 		
 		// /		
 		addShapeless(jerkedBeef, 1, salt, Item.beefRaw);
 		addShapeless(jerkedPork, 1, salt, Item.porkRaw);
 		addShapeless(jerkedLamb, 1, salt, lambRawMeat);
 		addShapeless(salo, 1, salt, porkBelly);
 		addShapeless(jerkedBeef, 1, salt, Item.beefRaw);
 		addShapeless(salt, 4, saltCrystal);
 
 		addSmelting(obsidianAlloyIngot, 1, 1, obsidianAlloyBlank);
 		addSmelting(lambFried, 1, 1, lambRawMeat);
 		addSmelting(salt, 8, 0.1F, Item.bucketWater);
 		
 		addSmelting(cementSack, 8, 0.3F, cementMix);
 		addSmelting(salt, 8, 0.1F, Item.bucketWater);
 		
 		addShapeless(goldSpool, 1, goldDust, spool);
 		addShapeless(diamondSpool, 1, diamondDust, spool);
 		addShapeless(goldFabricStrap, 1, goldSpool, goldSpool, goldSpool);
 		addShapeless(goldFabricRoll, 1, goldFabricStrap, goldFabricStrap, goldFabricStrap, goldFabricStrap, goldFabricStrap, goldFabricStrap);
 		addShapeless(diamondFabricStrap, 1, diamondSpool, diamondSpool, diamondSpool);
 		addShapeless(diamondFabricRoll, 1, diamondFabricStrap, diamondFabricStrap, diamondFabricStrap, diamondFabricStrap, diamondFabricStrap, diamondFabricStrap);
 		addShapeless(chevron, 1, spool, fabricRoll, spool);
 		
 		addShapeless(chevronWithGoldThread, 1, goldSpool, fabricStrap, goldSpool);
 		addShapeless(chevronWithDiamondThread, 1, diamondSpool, fabricStrap, diamondSpool);
 		addShapeless(goldChevronWithGoldThread, 1,goldSpool, goldFabricStrap, goldSpool);
 		addShapeless(goldChevronWithDiamondThread, 1, diamondSpool, goldFabricStrap, diamondSpool);
 		addShapeless(diamondChevronWithDiamondThread, 1, diamondSpool, diamondFabricStrap, diamondSpool);
 		addShapeless(diamondChevronWithGoldThread, 1, goldSpool, diamondFabricStrap, goldSpool);
 		
 		addShapeless(medivalSymbol, 1, fabricStrap, spool, new ItemStack(Item.dyePowder, 1, 11));
 		addShapeless(technoSymbol, 1, fabricStrap, spool, new ItemStack(Item.dyePowder, 1, 1));
 		
 		/*
 		// marble and other simple blocks
 		GameRegistry.addRecipe(new ItemStack(itemCementMix, 1), "xkx", "kzk", "xkx", 'k', new ItemStack(Item.dyePowder, 3,
 				15), 'z', Item.bucketWater, 'x', Block.sand);
 
 		GameRegistry.addRecipe(new ItemStack(itemIronStripe, 6), "xxx", 'x', Item.ingotIron);
 		// 
 		
 		//  
 		GameRegistry.addRecipe(new ItemStack(itemWoodChair, 1), "  x", "xxx", "xyx", 'x', itemWoodPlank, 'y', itemNails);
 		GameRegistry.addRecipe(new ItemStack(itemWhiteCottonChair, 1), " zx", "xxx", "xyx", 'x', itemWoodPlank, 'y',
 				itemNails, 'z', new ItemStack(Block.cloth, 1, 0));
 		GameRegistry.addRecipe(new ItemStack(itemBlueCottonChair, 1), " zx", "xxx", "xyx", 'x', itemWoodPlank, 'y',
 				itemNails, 'z', new ItemStack(Block.cloth, 1, 11));
 		GameRegistry.addRecipe(new ItemStack(itemRedCottonChair, 1), " zx", "xxx", "xyx", 'x', itemWoodPlank, 'y',
 				itemNails, 'z', new ItemStack(Block.cloth, 1, 14));
 		GameRegistry.addRecipe(new ItemStack(itemBlackCottonChair, 1), " zx", "xxx", "xyx", 'x', itemWoodPlank, 'y',
 				itemNails, 'z', new ItemStack(Block.cloth, 1, 15));
 		GameRegistry.addRecipe(new ItemStack(itemLeatherChair, 1), " zx", "xxx", "xyx", 'x', itemWoodPlank, 'y', itemNails,
 				'z', Item.leather);
 		GameRegistry.addRecipe(new ItemStack(leatherCorset, 1),
 				"x x",
 				"xyx",
 				"xyx",
 				'x', leatherStrap,
 				'y', Item.silk);
 
 		//   
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 0), "xxx", "rsr", "p p", 'x', Item.stick, 's', chevron, 'p',
 				spool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 1), "xxx", "rsr", "p p", 'x', Item.stick, 's', chevron, 'p',
 				goldSpool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 2), "xxx", "rsr", "p p", 'x', Item.stick, 's', chevron, 'p',
 				goldSpool, 'r', goldFabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 3), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				chevronWithGoldThread, 'p', spool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 4), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				chevronWithGoldThread, 'p', goldSpool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 5), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				chevronWithGoldThread, 'p', spool, 'r', goldFabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 6), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithGoldThread, 'p', spool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 7), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithGoldThread, 'p', goldSpool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 8), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithGoldThread, 'p', spool, 'r', goldFabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 9), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithGoldThread, 'p', goldSpool, 'r', goldFabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 10), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithDiamondThread, 'p', spool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 11), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithDiamondThread, 'p', goldSpool, 'r', fabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 12), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithDiamondThread, 'p', spool, 'r', goldFabricRoll);
 		GameRegistry.addRecipe(new ItemStack(itemFlag, 1, 13), "xxx", "rsr", "p p", 'x', Item.stick, 's',
 				goldChevronWithDiamondThread, 'p', goldSpool, 'r', goldFabricRoll);
 
 		//  
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 0), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', spool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 1), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', diamondSpool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 2), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', diamondSpool, 'r', diamondFabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 3), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', spool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 4), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', diamondSpool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 5), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', spool, 'r', diamondFabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 6), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', spool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 7), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', diamondSpool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 8), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', spool, 'r', diamondFabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 9), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', diamondSpool, 'r', diamondFabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 10), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', spool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 11), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', diamondSpool, 'r', fabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 12), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', spool, 'r', diamondFabricRoll, 'y', medivalSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemMedivalFlag, 1, 13), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', diamondSpool, 'r', diamondFabricRoll, 'y',
 				medivalSymbol);
 		//  
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 0), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', spool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 1), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', diamondSpool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 2), "xxx", "rsr", "pyp", 'x', Item.stick, 's', chevron,
 				'p', diamondSpool, 'r', diamondFabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 3), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', spool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 4), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', diamondSpool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 5), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				chevronWithDiamondThread, 'p', spool, 'r', diamondFabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 6), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', spool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 7), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', diamondSpool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 8), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithGoldThread, 'p', spool, 'r', diamondFabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 9), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 10), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', spool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 11), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', diamondSpool, 'r', fabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 12), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', spool, 'r', diamondFabricRoll, 'y', technoSymbol);
 		GameRegistry.addRecipe(new ItemStack(itemTechnoFlag, 1, 13), "xxx", "rsr", "pyp", 'x', Item.stick, 's',
 				diamondChevronWithDiamondThread, 'p', diamondSpool, 'r', diamondFabricRoll, 'y',
 				technoSymbol);
 				
 		*/
 	}
 
 }
