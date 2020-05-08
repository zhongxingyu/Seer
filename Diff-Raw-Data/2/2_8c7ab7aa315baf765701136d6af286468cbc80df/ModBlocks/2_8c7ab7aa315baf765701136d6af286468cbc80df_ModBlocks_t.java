 package kaimod.core;
 
 import cpw.mods.fml.client.TextureFXManager;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import kaimod.block.*;
 import kaimod.core.client.*;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockFence;
 import net.minecraft.block.BlockFire;
 import net.minecraft.block.BlockFlowing;
 import net.minecraft.block.BlockSoulSand;
 import net.minecraft.block.BlockStairs;
 import net.minecraft.block.BlockStationary;
 import net.minecraft.block.material.MapColor;
 import net.minecraft.block.material.Material;
 import net.minecraft.block.material.MaterialLiquid;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 
 public class ModBlocks {
 
 	public static final Block oreNetherDrexite = (new BlockOre_kaimod(Reference.oreIDs+0, 103)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setBlockName("oreNetherDrexite");
 
 	public static void SetupLanguage () {
 
 		LanguageRegistry.addName(oreNetherDrexite, "Drexite Ore");
 	}
 
 	public static void SetupLevels() {
 
 		MinecraftForge.setBlockHarvestLevel(oreNetherDrexite, "pickaxe", 3);
 	}
 	
 	public static void Register () {
 
 		GameRegistry.registerBlock(oreNetherDrexite, "oreNetherDrexite");
 	}
 	
 	public static void SetupRecipes() {
 		
		GameRegistry.addSmelting(oreNetherDrexite.blockID, new ItemStack(Item.diamond), 1.0F);
 	}
 }
