 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 IMPORT_BLOCKS
 IMPORT_ITEMS
 import net.minecraft.item.ItemStack;
 
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 
 import com.qzx.au.core.Color;
 
 public class ModRecipes {
 	public static void init(){
 		ItemStack stoneDoubleSlab = new ItemStack(MC_BLOCK.stoneDoubleSlab);
 		ItemStack stoneSmoothSlab = new ItemStack(MC_BLOCK.stoneDoubleSlab, 1, 8);
 		ItemStack stoneSingleSlab = new ItemStack(MC_BLOCK.stoneSingleSlab);
 
 		ItemStack cobblestone = new ItemStack(MC_BLOCK.cobblestone);
 		ItemStack stone = new ItemStack(MC_BLOCK.stone);
 		ItemStack stoneBrick = new ItemStack(MC_BLOCK.stoneBrick);
 		ItemStack chiseledBrick = new ItemStack(MC_BLOCK.stoneBrick, 1, 3);
 		ItemStack gravel = new ItemStack(MC_BLOCK.gravel);
 		ItemStack sand = new ItemStack(MC_BLOCK.sand);
 		ItemStack grassBlock = new ItemStack(MC_BLOCK.grass);
 		ItemStack vine = new ItemStack(MC_BLOCK.vine);
 		ItemStack glass = new ItemStack(MC_BLOCK.glass);
 		ItemStack glassPane = new ItemStack(MC_BLOCK.thinGlass);
 		ItemStack ironBars = new ItemStack(MC_BLOCK.fenceIron);
 		ItemStack redstoneTorch = new ItemStack(MC_BLOCK.torchRedstoneActive);
 		ItemStack redstoneDust = new ItemStack(MC_ITEM.redstone);
 		ItemStack glowstone = new ItemStack(MC_BLOCK.glowStone);
 		ItemStack diamond = new ItemStack(MC_ITEM.diamond);
 		#ifdef MC152
 		ItemStack glowstoneDust = new ItemStack(MC_ITEM.lightStoneDust);
 		#else
 		ItemStack glowstoneDust = new ItemStack(MC_ITEM.glowstone);
 		#endif
 		ItemStack[] dyes = new ItemStack[16];
 		for(int c = 0; c < 16; c++)
 			dyes[c] = new ItemStack(MC_ITEM.dyePowder, 1, c);
 
 		//////////
 
 		if(Cfg.enableChromaInfuser){
 			// CRAFT cauldron + red dye, green dye, blue dye -> chroma infuser
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(THIS_MOD.blockChromaInfuser), "rgb", " c ", "   ",
 									'c', new ItemStack(MC_ITEM.cauldron), 'r', Color.oreDyes[1], 'g', Color.oreDyes[2], 'b', Color.oreDyes[4]));
 		}
 
 		//////////
 
 		if(Cfg.enableStoneHalfSlabs){
 			ItemStack coloredStoneHalfSlabsSmooth = new ItemStack(THIS_MOD.blockStoneHalfSlabsSmooth);
 			ItemStack coloredStoneHalfSlabs = new ItemStack(THIS_MOD.blockStoneHalfSlabs);
 			ItemStack coloredStoneHalfSlab = new ItemStack(THIS_MOD.blockStoneHalfSlab[0]);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, stoneSmoothSlab, coloredStoneHalfSlabsSmooth);
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, stoneSmoothSlab, coloredStoneHalfSlabs);
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, stoneDoubleSlab, coloredStoneHalfSlabsSmooth);
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, stoneDoubleSlab, coloredStoneHalfSlabs);
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, stoneSingleSlab, coloredStoneHalfSlab);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredStoneHalfSlabsSmooth, coloredStoneHalfSlabsSmooth);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredStoneHalfSlabsSmooth, coloredStoneHalfSlabs);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredStoneHalfSlabs, coloredStoneHalfSlabsSmooth);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredStoneHalfSlabs, coloredStoneHalfSlabs);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredStoneHalfSlab, coloredStoneHalfSlab);
 			// SMELT <colored> stone slabs -> stone slabs
 			GameRegistry.addSmelting(coloredStoneHalfSlabsSmooth.itemID, stoneSmoothSlab, 1.0f);
 			GameRegistry.addSmelting(coloredStoneHalfSlabs.itemID, stoneDoubleSlab, 1.0f);
 			GameRegistry.addSmelting(coloredStoneHalfSlab.itemID, stoneSingleSlab, 1.0f);
 
 			for(int c = 0; c < 16; c++){
 				// CRAFT 4 <colored> stone half slabs -> 2 <colored> stone smooth slabs
 				GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneHalfSlabsSmooth, 2, c), "ss", "ss", 's', new ItemStack(THIS_MOD.blockStoneHalfSlab[0], 1, c));
 				// CRAFT 2 <colored> stone half slabs -> <colored> stone double slab
 				GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneHalfSlabs, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockStoneHalfSlab[0], 1, c));
 
 				// CRAFT <colored> stone smooth slabs -> 2 <colored> stone half slabs
 				GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneHalfSlab[0], 2, c), "s", 's', new ItemStack(THIS_MOD.blockStoneHalfSlabsSmooth, 1, c));
 				// CRAFT <colored> stone double slabs -> 2 <colored> stone half slabs
 				GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneHalfSlab[0], 2, c), "d", 'd', new ItemStack(THIS_MOD.blockStoneHalfSlabs, 1, c));
 			}
 		}
 
 		//////////
 
 		if(Cfg.enableCobble){
 			ItemStack coloredCobble = new ItemStack(THIS_MOD.blockCobble);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, cobblestone, coloredCobble);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredCobble, coloredCobble);
 			// SMELT <colored> cobble -> cobblestone
 			GameRegistry.addSmelting(coloredCobble.itemID, cobblestone, 1.0f);
 
 			// stairs
 			if(Cfg.enableCobbleStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockCobbleStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockCobble, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockCobble, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockCobbleStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableCobbleSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockCobbleSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockCobble, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockCobble, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockCobbleSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableStone){
 			ItemStack coloredStone = new ItemStack(THIS_MOD.blockStone);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, stone, coloredStone);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredStone, coloredStone);
 			// SMELT <colored> stone -> stone
 			GameRegistry.addSmelting(coloredStone.itemID, stone, 1.0f);
 
 			// stairs
 			if(Cfg.enableStoneStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockStone, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStone, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockStoneStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableStoneSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockStone, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStone, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockStoneSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableStoneBrick){
 			ItemStack coloredStoneBrick = new ItemStack(THIS_MOD.blockStoneBrick);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, stoneBrick, coloredStoneBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredStoneBrick, coloredStoneBrick);
 			// SMELT <colored> stoneBrick -> stoneBrick
 			GameRegistry.addSmelting(coloredStoneBrick.itemID, stoneBrick, 1.0f);
 
 			// stairs
 			if(Cfg.enableStoneBrickStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneBrickStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockStoneBrick, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneBrick, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockStoneBrickStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableStoneBrickSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneBrickSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockStoneBrick, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockStoneBrick, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockStoneBrickSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableChiseledBrick){
 			ItemStack coloredChiseledBrick = new ItemStack(THIS_MOD.blockChiseledBrick);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE_DOT, stone, coloredChiseledBrick);
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE_DOT, chiseledBrick, coloredChiseledBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredChiseledBrick, coloredChiseledBrick);
 			// SMELT <colored> chiseledBrick -> chiseledBrick
 			GameRegistry.addSmelting(coloredChiseledBrick.itemID, chiseledBrick, 1.0f);
 
 			// no stairs -- texture doesn't align correctly
 
 			// slabs
 			if(Cfg.enableChiseledBrickSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockChiseledBrickSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockChiseledBrick, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockChiseledBrick, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockChiseledBrickSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableSmoothBrick){
 			ItemStack coloredSmoothBrick = new ItemStack(THIS_MOD.blockSmoothBrick);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, stone, coloredSmoothBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredSmoothBrick, coloredSmoothBrick);
 			// SMELT <colored> smoothBrick -> stone
 			GameRegistry.addSmelting(coloredSmoothBrick.itemID, stone, 1.0f);
 
 			// stairs
 			if(Cfg.enableSmoothBrickStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSmoothBrickStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockSmoothBrick, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSmoothBrick, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockSmoothBrickStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableSmoothBrickSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSmoothBrickSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockSmoothBrick, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSmoothBrick, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockSmoothBrickSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		// convert any colored stone-based to any other colored stone-based
 		if(Cfg.enableStone && Cfg.enableChiseledBrick){
 			ItemStack coloredStone = new ItemStack(THIS_MOD.blockStone);
 			ItemStack coloredChiseledBrick = new ItemStack(THIS_MOD.blockChiseledBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredChiseledBrick, coloredStone);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredStone, coloredChiseledBrick);
 		}
 		if(Cfg.enableStone && Cfg.enableSmoothBrick){
 			ItemStack coloredStone = new ItemStack(THIS_MOD.blockStone);
 			ItemStack coloredSmoothBrick = new ItemStack(THIS_MOD.blockSmoothBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredSmoothBrick, coloredStone);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredStone, coloredSmoothBrick);
 		}
 		if(Cfg.enableChiseledBrick && Cfg.enableSmoothBrick){
 			ItemStack coloredChiseledBrick = new ItemStack(THIS_MOD.blockChiseledBrick);
 			ItemStack coloredSmoothBrick = new ItemStack(THIS_MOD.blockSmoothBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredSmoothBrick, coloredChiseledBrick);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredChiseledBrick, coloredSmoothBrick);
 		}
 
 		//////////
 
 		if(Cfg.enableGravel){
 			ItemStack coloredGravel = new ItemStack(THIS_MOD.blockGravel);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, gravel, coloredGravel);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredGravel, coloredGravel);
 			// SMELT <colored> gravel -> gravel
 			GameRegistry.addSmelting(coloredGravel.itemID, gravel, 1.0f);
 
 			// no stairs -- texture doesn't align correctly
 			// slabs
 			if(Cfg.enableGravelSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockGravelSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockGravel, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockGravel, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockGravelSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableSand){
 			ItemStack coloredSand = new ItemStack(THIS_MOD.blockSand);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, sand, coloredSand);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, coloredSand, coloredSand);
 			// SMELT <colored> sand -> sand
 			GameRegistry.addSmelting(coloredSand.itemID, sand, 1.0f);
 
 			// stairs
 			if(Cfg.enableSandStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSandStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockSand, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSand, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockSandStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableSandSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSandSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockSand, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockSand, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockSandSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableArtificialGrass){
 			ItemStack artificialGrass = new ItemStack(THIS_MOD.blockArtificialGrass);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_BLANK, grassBlock, artificialGrass);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_BLANK, artificialGrass, artificialGrass);
 
 			// stairs
 			if(Cfg.enableArtificialGrassStairs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockArtificialGrassStairs[c], 4), "b  ", "bb ", "bbb", 'b', new ItemStack(THIS_MOD.blockArtificialGrass, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockArtificialGrass, 3, c), "s", "s", 's', new ItemStack(THIS_MOD.blockArtificialGrassStairs[c], 1));
 				}
 
 			// slabs
 			if(Cfg.enableArtificialGrassSlabs)
 				for(int c = 0; c < 16; c++){
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockArtificialGrassSlab[0], 6, c), "bbb", 'b', new ItemStack(THIS_MOD.blockArtificialGrass, 1, c));
 					GameRegistry.addRecipe(new ItemStack(THIS_MOD.blockArtificialGrass, 1, c), "s", "s", 's', new ItemStack(THIS_MOD.blockArtificialGrassSlab[0], 1, c));
 				}
 		}
 
 		//////////
 
 		if(Cfg.enableArtificialVine){
 			final String group1 = "artificialVine1";
 			final String group2 = "artificialVine2";
 
 			// CHROMA INFUSER recipes
 			for(int c = 0; c < 16; c++){
 				ItemStack artificialVine = new ItemStack(THIS_MOD.blockArtificialVine[c]);
 				ChromaRegistry.addRecipeUniColor(ChromaButton.BUTTON_BLANK, vine, c, group1, artificialVine);
 				for(int s = 0; s < 16; s++)
 					if(c != s)
 						ChromaRegistry.addRecipeUniColor(ChromaButton.BUTTON_BLANK, new ItemStack(THIS_MOD.blockArtificialVine[s]), c, group2, artificialVine);
 			}
 		}
 
 		//////////
 
 		if(Cfg.enableGlass){
 			ItemStack coloredGlass = new ItemStack(THIS_MOD.blockGlass);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, glass, coloredGlass);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlass, coloredGlass);
 			// SMELT <colored> glass -> glass
 			GameRegistry.addSmelting(coloredGlass.itemID, glass, 1.0f);
 		}
 
 		if(Cfg.enableGlassTinted){
 			ItemStack coloredGlassTinted = new ItemStack(THIS_MOD.blockGlassTinted);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE_DOT, glass, coloredGlassTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlassTinted, coloredGlassTinted);
 			// SMELT <colored tinted> glass -> glass
 			GameRegistry.addSmelting(coloredGlassTinted.itemID, glass, 1.0f);
 		}
 
 		if(Cfg.enableGlassTintedNoFrame){
 			ItemStack coloredGlassTintedNoFrame = new ItemStack(THIS_MOD.blockGlassTintedNoFrame);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_DOT, glass, coloredGlassTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlassTintedNoFrame, coloredGlassTintedNoFrame);
 			// SMELT <tinted frameless> glass -> glass
 			GameRegistry.addSmelting(coloredGlassTintedNoFrame.itemID, glass, 1.0f);
 		}
 
 		//////////
 
 		if(Cfg.enableGlassPane){
 			ItemStack coloredGlassPane = new ItemStack(THIS_MOD.blockGlassPane);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, glassPane, coloredGlassPane);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlassPane, coloredGlassPane);
 			// SMELT <colored> glass pane -> glass pane
 			GameRegistry.addSmelting(coloredGlassPane.itemID, glassPane, 1.0f);
 
 			// CRAFT 6 <colored> glass -> 16 <colored> glass panes
 			for(int c = 0; c < 16; c++)
 				GameRegistry.addShapedRecipe(new ItemStack(THIS_MOD.blockGlassPane, 16, c), "ggg", "ggg", "   ", 'g', new ItemStack(THIS_MOD.blockGlass, 1, c));
 		}
 
 		if(Cfg.enableGlassPaneTinted){
 			ItemStack coloredGlassPaneTinted = new ItemStack(THIS_MOD.blockGlassPaneTinted);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE_DOT, glassPane, coloredGlassPaneTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlassPaneTinted, coloredGlassPaneTinted);
 			// SMELT <colored tinted> glass pane -> glass pane
 			GameRegistry.addSmelting(coloredGlassPaneTinted.itemID, glassPane, 1.0f);
 
 			// CRAFT 6 <colored> glass -> 16 <colored> glass panes
 			for(int c = 0; c < 16; c++)
 				GameRegistry.addShapedRecipe(new ItemStack(THIS_MOD.blockGlassPaneTinted, 16, c), "ggg", "ggg", "   ", 'g', new ItemStack(THIS_MOD.blockGlassTinted, 1, c));
 		}
 
 		if(Cfg.enableGlassPaneTintedNoFrame){
 			ItemStack coloredGlassPaneTintedNoFrame = new ItemStack(THIS_MOD.blockGlassPaneTintedNoFrame);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_DOT, glassPane, coloredGlassPaneTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlassPaneTintedNoFrame, coloredGlassPaneTintedNoFrame);
 			// SMELT <tinted frameless> glass pane -> glass pane
 			GameRegistry.addSmelting(coloredGlassPaneTintedNoFrame.itemID, glassPane, 1.0f);
 
 			// CRAFT 6 <colored> glass -> 16 <colored> glass panes
 			for(int c = 0; c < 16; c++)
 				GameRegistry.addShapedRecipe(new ItemStack(THIS_MOD.blockGlassPaneTintedNoFrame, 16, c), "ggg", "ggg", "   ", 'g', new ItemStack(THIS_MOD.blockGlassTintedNoFrame, 1, c));
 		}
 
 		//////////
 
 		// convert any colored glass to any other colored glass
 		if(Cfg.enableGlass && Cfg.enableGlassTinted){
 			ItemStack coloredGlass = new ItemStack(THIS_MOD.blockGlass);
 			ItemStack coloredGlassTinted = new ItemStack(THIS_MOD.blockGlassTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlassTinted, coloredGlass);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlass, coloredGlassTinted);
 		}
 		if(Cfg.enableGlass && Cfg.enableGlassTintedNoFrame){
 			ItemStack coloredGlass = new ItemStack(THIS_MOD.blockGlass);
 			ItemStack coloredGlassTintedNoFrame = new ItemStack(THIS_MOD.blockGlassTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlassTintedNoFrame, coloredGlass);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlass, coloredGlassTintedNoFrame);
 		}
 		if(Cfg.enableGlassTinted && Cfg.enableGlassTintedNoFrame){
 			ItemStack coloredGlassTinted = new ItemStack(THIS_MOD.blockGlassTinted);
 			ItemStack coloredGlassTintedNoFrame = new ItemStack(THIS_MOD.blockGlassTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlassTintedNoFrame, coloredGlassTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlassTinted, coloredGlassTintedNoFrame);
 		}
 
 		// convert any colored glass pane to any other colored glass pane
 		if(Cfg.enableGlassPane && Cfg.enableGlassPaneTinted){
 			ItemStack coloredGlassPane = new ItemStack(THIS_MOD.blockGlassPane);
 			ItemStack coloredGlassPaneTinted = new ItemStack(THIS_MOD.blockGlassPaneTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlassPaneTinted, coloredGlassPane);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlassPane, coloredGlassPaneTinted);
 		}
 		if(Cfg.enableGlassPane && Cfg.enableGlassPaneTintedNoFrame){
 			ItemStack coloredGlassPane = new ItemStack(THIS_MOD.blockGlassPane);
 			ItemStack coloredGlassPaneTintedNoFrame = new ItemStack(THIS_MOD.blockGlassPaneTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredGlassPaneTintedNoFrame, coloredGlassPane);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlassPane, coloredGlassPaneTintedNoFrame);
 		}
 		if(Cfg.enableGlassPaneTinted && Cfg.enableGlassPaneTintedNoFrame){
 			ItemStack coloredGlassPaneTinted = new ItemStack(THIS_MOD.blockGlassPaneTinted);
 			ItemStack coloredGlassPaneTintedNoFrame = new ItemStack(THIS_MOD.blockGlassPaneTintedNoFrame);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE_DOT, coloredGlassPaneTintedNoFrame, coloredGlassPaneTinted);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_DOT, coloredGlassPaneTinted, coloredGlassPaneTintedNoFrame);
 		}
 
 		//////////
 
 		if(Cfg.enableIronBars){
 			ItemStack coloredIronBars = new ItemStack(THIS_MOD.blockIronBars);
 
 			// CHROMA INFUSER recipes
 			ChromaRegistry.addRecipe(ChromaButton.BUTTON_SQUARE, ironBars, coloredIronBars);
 			ChromaRegistry.addRecipeColored(ChromaButton.BUTTON_SQUARE, coloredIronBars, coloredIronBars);
 			// SMELT <colored> iron bars -> iron bars
 			GameRegistry.addSmelting(coloredIronBars.itemID, ironBars, 1.0f);
 		}
 
 		//////////
 
 		if(Cfg.enableLamps){
 			ItemStack coloredLamp = new ItemStack(THIS_MOD.blockLamp);
 
 			if(Cfg.enableGlassPaneTinted){
 				// CRAFT 6 tinted glass panes + dye + glowstone + redstone -> <colored> lamp
 				for(int c = 0; c < 16; c++)
 					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(THIS_MOD.blockLamp, 1, c), "pgp", "p-p", "prp",
 											'p', new ItemStack(THIS_MOD.blockGlassPaneTinted, 1, c), 'g', glowstone, '-', Color.oreDyes[c], 'r', redstoneDust));
 			} else {
 				// CRAFT 6 glass panes + dye + glowstone + redstone -> <colored> lamp
 				for(int c = 0; c < 16; c++)
 					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(THIS_MOD.blockLamp, 1, c), "pgp", "p-p", "prp",
 											'p', glassPane, 'g', glowstone, '-', Color.oreDyes[c], 'r', redstoneDust));
 			}
 			// CRAFT <colored> lamp + dye -> <colored> lamp
 			for(int g = 0; g < 16; g++){
 				ItemStack anyLamp = new ItemStack(THIS_MOD.blockLamp, 1, g);
 				for(int c = 0; c < 16; c++)
 					if(g != c)
 						GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(THIS_MOD.blockLamp, 1, c), anyLamp, Color.oreDyes[c]));
 			}
 
 			//////////
 
 			ItemStack coloredInvertedLamp = new ItemStack(THIS_MOD.blockInvertedLamp);
 
 			if(Cfg.enableGlassPaneTinted){
 				// CRAFT 6 glass panes + dye + glowstone + redstone torch -> <colored> inverted lamp
 				for(int c = 0; c < 16; c++)
 					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(THIS_MOD.blockInvertedLamp, 1, c), "pgp", "p-p", "prp",
 											'p', new ItemStack(THIS_MOD.blockGlassPaneTinted, 1, c), 'g', glowstone, '-', Color.oreDyes[c], 'r', redstoneTorch));
 			} else {
 				// CRAFT 6 glass panes + dye + glowstone + redstone torch -> <colored> inverted lamp
 				for(int c = 0; c < 16; c++)
 					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(THIS_MOD.blockInvertedLamp, 1, c), "pgp", "p-p", "prp",
 											'p', glassPane, 'g', glowstone, '-', Color.oreDyes[c], 'r', redstoneTorch));
 			}
 			// CRAFT <colored> inverted lamp + dye -> <colored> inverted lamp
 			for(int g = 0; g < 16; g++){
 				ItemStack anyInvertedLamp = new ItemStack(THIS_MOD.blockInvertedLamp, 1, g);
 				for(int c = 0; c < 16; c++)
 					if(g != c)
 						GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(THIS_MOD.blockInvertedLamp, 1, c), anyInvertedLamp, Color.oreDyes[c]));
 			}
 
 			//////////
 
 			for(int c = 0; c < 16; c++){
 				// CRAFT <colored> lamp + redstone torch -> <colored> inverted lamp
 				// CRAFT <colored> inverted lamp + redstone dust -> <colored> lamp
 				ItemStack anyLamp = new ItemStack(THIS_MOD.blockLamp, 1, c);
 				ItemStack anyInvertedLamp = new ItemStack(THIS_MOD.blockInvertedLamp, 1, c);
 				GameRegistry.addShapelessRecipe(anyInvertedLamp, anyLamp, redstoneTorch);
 				GameRegistry.addShapelessRecipe(anyLamp, anyInvertedLamp, redstoneDust);
 			}
 		}
 
 		//////////
 
 		if(Cfg.enableFlowers){
 			// flower to dye recipes
 			GameRegistry.addShapelessRecipe(new ItemStack(THIS_MOD.itemFlowerDye, 1, 0), new ItemStack(THIS_MOD.blockFlower, 1, 0)); // black
 			GameRegistry.addShapelessRecipe(dyes[1],  new ItemStack(THIS_MOD.blockFlower, 1, 1)); // red
 			GameRegistry.addShapelessRecipe(new ItemStack(THIS_MOD.itemFlowerDye, 1, 2), new ItemStack(THIS_MOD.blockFlower, 1, 2)); // green
 			GameRegistry.addShapelessRecipe(new ItemStack(THIS_MOD.itemFlowerDye, 1, 3), new ItemStack(THIS_MOD.blockFlower, 1, 3)); // brown
 			GameRegistry.addShapelessRecipe(new ItemStack(THIS_MOD.itemFlowerDye, 1, 4), new ItemStack(THIS_MOD.blockFlower, 1, 4)); // blue
 			GameRegistry.addShapelessRecipe(dyes[5],  new ItemStack(THIS_MOD.blockFlower, 1, 5)); // purple
 			GameRegistry.addShapelessRecipe(dyes[6],  new ItemStack(THIS_MOD.blockFlower, 1, 6)); // cyan
 			GameRegistry.addShapelessRecipe(dyes[7],  new ItemStack(THIS_MOD.blockFlower, 1, 7)); // light gray
 			GameRegistry.addShapelessRecipe(dyes[8],  new ItemStack(THIS_MOD.blockFlower, 1, 8)); // gray
 			GameRegistry.addShapelessRecipe(dyes[9],  new ItemStack(THIS_MOD.blockFlower, 1, 9)); // pink
 			GameRegistry.addShapelessRecipe(dyes[10], new ItemStack(THIS_MOD.blockFlower, 1, 10)); // lime
 			GameRegistry.addShapelessRecipe(dyes[11], new ItemStack(THIS_MOD.blockFlower, 1, 11)); // yellow
 			GameRegistry.addShapelessRecipe(dyes[12], new ItemStack(THIS_MOD.blockFlower, 1, 12)); // light blue
 			GameRegistry.addShapelessRecipe(dyes[13], new ItemStack(THIS_MOD.blockFlower, 1, 13)); // magenta
 			GameRegistry.addShapelessRecipe(dyes[14], new ItemStack(THIS_MOD.blockFlower, 1, 14)); // orange
 			GameRegistry.addShapelessRecipe(new ItemStack(THIS_MOD.itemFlowerDye, 1, 15), new ItemStack(THIS_MOD.blockFlower, 1, 15)); // white
 		}
 
 		//////////
 
 		if(Cfg.enableEnderCube){
 			ItemStack enderCube = new ItemStack(THIS_MOD.blockEnderCube);
 
 			// CRAFT glass + 3 eye of ender + redstone dust + 4 gold ingots -> ender cube
 			GameRegistry.addRecipe(enderCube, "xex", "eoe", "xrx",
 									'o', new ItemStack(MC_BLOCK.obsidian), 'e', new ItemStack(MC_ITEM.eyeOfEnder), 'r', redstoneDust, 'x', new ItemStack(MC_ITEM.ingotGold));
 
 			//////////
 
 			if(Cfg.enableEnderStar){
 				ItemStack enderStar = new ItemStack(THIS_MOD.itemEnderStar);
 
 				// CRAFT 8 ender cubes + 1 nether star -> ender star
 				GameRegistry.addRecipe(enderStar, "ccc", "csc", "ccc", 'c', enderCube, 's', new ItemStack(MC_ITEM.netherStar));
 
 				//////////
 
 				if(Cfg.enableEnderWand){
 					ItemStack enderWand = new ItemStack(THIS_MOD.itemEnderWand);
 
 					// CRAFT ender star + 2 diamonds + 2 gold ingots -> ender wand
 					GameRegistry.addRecipe(enderWand, " ds", " gd", "g  ", 's', enderStar, 'd', diamond, 'g', new ItemStack(MC_ITEM.ingotGold));
 				}
 
 				//////////
 
 				if(Cfg.enableEnderMagnet){
 					ItemStack enderMagnet = new ItemStack(THIS_MOD.itemEnderMagnet);
 
 					// CRAFT 5 ender stars + 2 diamonds -> ender magnet
 					GameRegistry.addRecipe(enderMagnet, "s s", "s s", "dsd", 's', enderStar, 'd', diamond);
 
 					//////////
 
 					if(Cfg.enableEnderXT){
 						ItemStack enderXT = new ItemStack(THIS_MOD.itemEnderXT);
 
 						// CRAFT 4 ender stars + 3 ender magnet + 2 diamonds -> ender xt
 						GameRegistry.addRecipe(enderXT, "sds", "mdm", "sms", 's', enderStar, 'm', enderMagnet, 'd', diamond);
 					}
 				}
 			}
 		}
 
 		//////////
 
 		// SMELT egg -> fried egg
 		if(Cfg.enableFriedEgg){
 			GameRegistry.addSmelting(MC_ITEM.egg.itemID, new ItemStack(THIS_MOD.itemFriedEgg), 1.0f);
 		}
 
 		//////////
 
 		// SMELT rotten flesh -> cooked flesh
 		if(Cfg.enableCookedFlesh){
 			ItemStack cookedFlesh = new ItemStack(THIS_MOD.itemCookedFlesh);
 			GameRegistry.addSmelting(MC_ITEM.rottenFlesh.itemID, cookedFlesh, 1.0f);
 
 			// CRAFT cooked flesh -> leather
 			if(Cfg.enableCookedFleshToLeather){
 				ItemStack leather = new ItemStack(MC_ITEM.leather);
 				switch(Cfg.nrCookedFleshToLeather){
 				case 1: GameRegistry.addShapelessRecipe(leather, cookedFlesh); break;
 				case 2: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh); break;
 				case 3: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				case 4: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				case 5: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				case 6: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				case 7: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				case 8: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				default: GameRegistry.addShapelessRecipe(leather, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh, cookedFlesh); break;
 				}
 			}
 		}
 
 		//////////
 
 		if(Cfg.enableDiamondShears){
 			GameRegistry.addShapedRecipe(new ItemStack(THIS_MOD.itemDiamondShears), "d d", " s ", "   ", 'd', diamond, 's', new ItemStack(MC_ITEM.shears));
 		}
 
 		//////////
 
 		if(Cfg.enableChiseledBrickCrafting){
 			// CRAFT 2 stoneBrick slabs -> chiseledBrick (1.8 recipe)
 			GameRegistry.addRecipe(new ItemStack(MC_BLOCK.stoneBrick, 1, 3), "b", "b", 'b', new ItemStack(MC_BLOCK.stoneSingleSlab, 1, 5));
 		}
 		if(Cfg.enableMossyBrickCrafting){
 			// CRAFT stoneBrick + vine -> mossy stone brick (1.8 recipe)
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.stoneBrick, 1, 1), stoneBrick, vine);
 		}
 		if(Cfg.enableMossyCobbleCrafting){
 			// CRAFT cobblestone + vine -> mossy cobble (1.8 recipe)
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.cobblestoneMossy), cobblestone, vine);
 		}
 		if(Cfg.enableCrackedBrickCrafting){
 			// CRAFT stoneBrick + ice -> cracked stone brick
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.stoneBrick, 1, 2), stoneBrick, new ItemStack(MC_BLOCK.ice));
 		}
 
 		if(Cfg.enableStoneSlabFullCrafting){
 			// CRAFT 2 stone slabs -> stone slab (full)
 			GameRegistry.addShapedRecipe(stoneDoubleSlab, "s", "s", 's', stoneSingleSlab);
 			// UNCRAFT
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.stoneSingleSlab, 2), stoneDoubleSlab);
 		}
 		if(Cfg.enableStoneSlabFullSmoothCrafting){
 			// CRAFT 4 stone slabs -> 2 stone slab (full smooth)
			GameRegistry.addShapedRecipe(new ItemStack(MC_BLOCK.stoneDoubleSlab, 2, 8), "ss", "ss", 's', stoneSingleSlab);
 			// UNCRAFT
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.stoneSingleSlab, 2), stoneSmoothSlab);
 		}
 		if(Cfg.enableSandstoneSlabFullSmoothCrafting){
 			// CRAFT 4 sandstone slabs -> 2 sandstone slab (full smooth)
 			GameRegistry.addShapedRecipe(new ItemStack(MC_BLOCK.stoneDoubleSlab, 2, 9), "ss", "ss", 's', new ItemStack(MC_BLOCK.stoneSingleSlab, 1, 1));
 			// UNCRAFT
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.stoneSingleSlab, 2, 1), new ItemStack(MC_BLOCK.stoneDoubleSlab, 1, 9));
 		}
 		if(Cfg.enableGrassBlockCrafting){
 			// CRAFT tall grass + dirt -> grass block
 			GameRegistry.addShapelessRecipe(grassBlock, new ItemStack(MC_BLOCK.tallGrass, 1, 1), new ItemStack(MC_BLOCK.dirt));
 		}
 		if(Cfg.enableMyceliumCrafting){
 			// CRAFT brown mushroom + red mushroom + grass block -> mycelium block
 			GameRegistry.addShapelessRecipe(new ItemStack(MC_BLOCK.mycelium), new ItemStack(MC_BLOCK.mushroomBrown), new ItemStack(MC_BLOCK.mushroomRed), grassBlock);
 		}
 
 		//////////
 
 		// vanilla wool
 		ItemStack cloth = new ItemStack(MC_BLOCK.cloth);
 		ChromaRegistry.addRecipeColoredReversed(ChromaButton.BUTTON_BLANK, cloth, cloth);
 
 		#ifndef MC152
 			// vanilla hardened clay (1.6)
 			ItemStack stainedClay = new ItemStack(MC_BLOCK.stainedClay);
 			ChromaRegistry.addRecipeReversed(ChromaButton.BUTTON_BLANK, new ItemStack(MC_BLOCK.hardenedClay), stainedClay);
 			ChromaRegistry.addRecipeColoredReversed(ChromaButton.BUTTON_BLANK, stainedClay, stainedClay);
 			// vanilla carpet (1.6)
 			ItemStack carpet = new ItemStack(MC_BLOCK.carpet);
 			ChromaRegistry.addRecipeColoredReversed(ChromaButton.BUTTON_BLANK, carpet, carpet);
 		#endif
 
 		#if !defined MC152 && !defined MC164
 			// vanilla stained glass (1.7)
 			ItemStack stainedGlass = new ItemStack(MC_BLOCK.stained_glass);
 			ChromaRegistry.addRecipeReversed(ChromaButton.BUTTON_BLANK, glass, stainedGlass);
 			ChromaRegistry.addRecipeColoredReversed(ChromaButton.BUTTON_BLANK, stainedGlass, stainedGlass);
 			// vanilla stained glass panes (1.7)
 			ItemStack stainedGlassPane = new ItemStack(MC_BLOCK.stained_glass_pane);
 			ChromaRegistry.addRecipeReversed(ChromaButton.BUTTON_BLANK, glassPane, stainedGlassPane);
 			ChromaRegistry.addRecipeColoredReversed(ChromaButton.BUTTON_BLANK, stainedGlassPane, stainedGlassPane);
 		#endif
 	}
 }
