 package com.nexized.cross.manager;
 
 /*
  * Author: nexized
  * Last changed: 2013-08-13 by nexized
  * Licensed under nexized cross license - see license.txt for further information
  */
 
 import com.nexized.cross.block.*;
 import com.nexized.cross.conf.*;
 import com.nexized.emec.emec;
 import com.nexized.emec.configuration.emecConfigurationFile;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.MinecraftForge;
 
 public class blockManager {
 	
 			// @oreBlocks
 			public static Block oreAluminum;
 			public static Block oreCopper;
 			public static Block orePlatinum;
 			public static Block oreSilver;
 			public static Block oreTin;
 			public static Block oreExperience;
 			
 			// @ingotBlocks
 			public static Block blockAluminum;
 			public static Block blockBronze;
 			public static Block blockCopper;
 			public static Block blockPlatinum;
 			public static Block blockSilver;
 			public static Block blockSteel;
 			public static Block blockTin;
 			public static Block emeraldShardBlock;
 			
 			// @addBlocks
 			public static Block blockFosil;
 			public static Block blockFreezer;
 			public static Block blockCloud;
 			
 			// @fusionFurnance
 			public static Block furnanceFusion;
 			public static Block furnanceFusionOn;
 			
 			public static void init() {
				oreAluminum = new crossOre(emecConfigurationFile.oreAluminumID, Material.rock).setHardness(3.0F).setUnlocalizedName("oreAluminum").setCreativeTab(emec.tabEnhanceMeCraft);
 				
 				MinecraftForge.setBlockHarvestLevel(oreAluminum, "pickaxe", 2);
 				GameRegistry.registerBlock(oreAluminum, oreAluminum.getUnlocalizedName());
				
 			}
 			
 			public static void addBlocks(idManager idm, CreativeTabs tab)
 			{
 				// @Defaults 
 				float oreResistance = 5.0F; 
 				float blockResistance = 12.0F;
 				
 				// @oreBlocks

 				oreCopper = new crossOre(idm.getBlockID("oreCopper"), Material.rock, tab).setHardness(1.7F).setResistance(oreResistance).setUnlocalizedName("oreCopper");
 				orePlatinum = new crossOre(idm.getBlockID("orePlatinum"), Material.rock, tab).setHardness(5.0F).setResistance(oreResistance).setUnlocalizedName("orePlatinum");
 				oreSilver = new crossOre(idm.getBlockID("oreSilver"), Material.rock, tab).setHardness(3.0F).setResistance(oreResistance).setUnlocalizedName("oreSilver");
 				oreTin = new crossOre(idm.getBlockID("oreTin"), Material.rock, tab).setHardness(1.7F).setResistance(oreResistance).setUnlocalizedName("oreTin");
 				oreExperience = new crossOre(idm.getBlockID("oreExperience"), Material.rock, tab).setHardness(3.0F).setResistance(oreResistance).setUnlocalizedName("oreExperience");
 			
 				// @ingotBlocks
 				blockAluminum = new crossBlock(idm.getBlockID("blockAluminum"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockAluminum");
 				blockBronze = new crossBlock(idm.getBlockID("blockBronze"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockBronze");
 				blockCopper = new crossBlock(idm.getBlockID("blockCopper"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockCopper");
 				blockPlatinum = new crossBlock(idm.getBlockID("blockPlatinum"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockPlatinum");
 				blockSilver = new crossBlock(idm.getBlockID("blockSilver"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockSilver");
 				blockSteel = new crossBlock(idm.getBlockID("blockSteel"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockSteel");
 				blockTin = new crossBlock(idm.getBlockID("blockTin"), Material.iron, tab).setHardness(7.0F).setResistance(blockResistance).setUnlocalizedName("blockTin");
 				
 			
 				// @addBlocks
 				blockFosil = new crossBlock(idm.getBlockID("blockFosil"), Material.rock, tab).setHardness(1.7F).setResistance(oreResistance).setUnlocalizedName("blockFosil");
 				blockFreezer = new crossBlock(idm.getBlockID("blockFreezer"), Material.rock, tab).setHardness(4.0F).setResistance(blockResistance).setUnlocalizedName("blockFreezer");
 				blockCloud = new crossBlock(idm.getBlockID("blockCloud"), Material.rock, tab).setHardness(4.0F).setResistance(blockResistance).setUnlocalizedName("blockCloud");
 			
 				// @FusionFurnance
 				furnanceFusion = new crossFusionFurnance(idm.getBlockID("furnanceFusion"), false, tab).setHardness(3.5F).setResistance(10.0F).setUnlocalizedName("furnanceFusion");
 				furnanceFusionOn = new crossFusionFurnance(idm.getBlockID("furnanceFusionOn"), true, tab).setHardness(3.5F).setLightValue(1.0F).setUnlocalizedName("furnanceFusionOn");
 			}
 			
 			public static void registerBlocks() {
 				// @setBlockHarvestLevel

 				MinecraftForge.setBlockHarvestLevel(oreCopper, "pickaxe", 1);
 				MinecraftForge.setBlockHarvestLevel(orePlatinum, "pickaxe", 3);
 				MinecraftForge.setBlockHarvestLevel(oreSilver, "pickaxe", 2);
 				MinecraftForge.setBlockHarvestLevel(oreTin, "pickaxe", 1);
 				MinecraftForge.setBlockHarvestLevel(oreExperience, "pickaxe", 2);
 				
 				// registerBlocks
				
 				GameRegistry.registerBlock(oreCopper, oreCopper.getUnlocalizedName());
 				GameRegistry.registerBlock(orePlatinum, orePlatinum.getUnlocalizedName());
 				GameRegistry.registerBlock(oreSilver, oreSilver.getUnlocalizedName());
 				GameRegistry.registerBlock(oreTin, oreTin.getUnlocalizedName());
 				GameRegistry.registerBlock(oreExperience, oreExperience.getUnlocalizedName());
 				
 				GameRegistry.registerBlock(blockAluminum, blockAluminum.getUnlocalizedName());
 				GameRegistry.registerBlock(blockCopper, blockCopper.getUnlocalizedName());
 				GameRegistry.registerBlock(blockPlatinum, blockPlatinum.getUnlocalizedName());
 				GameRegistry.registerBlock(blockSilver, blockSilver.getUnlocalizedName());
 				GameRegistry.registerBlock(blockSteel, blockSteel.getUnlocalizedName());
 				GameRegistry.registerBlock(blockTin, blockTin.getUnlocalizedName());
 				
 				GameRegistry.registerBlock(blockFosil, blockFosil.getUnlocalizedName());
 				GameRegistry.registerBlock(blockFreezer, blockFreezer.getUnlocalizedName());
 				GameRegistry.registerBlock(blockCloud, blockCloud.getUnlocalizedName());
 				
 				GameRegistry.registerBlock(furnanceFusion, furnanceFusion.getUnlocalizedName());
 				// GameRegistry.registerBlock(furnanceFusionOn, furnanceFusionOn.getUnlocalizedName());
 			}
 			
 }
