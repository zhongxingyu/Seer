 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 //import cpw.mods.fml.relauncher.Side;
 //import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStairs;
 //import net.minecraft.block.material.Material;
 //import net.minecraft.client.renderer.texture.IconRegister;
 //import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 //import net.minecraft.item.ItemBlock;
 //import net.minecraft.util.Icon;
 
 import net.minecraftforge.common.MinecraftForge;
 
 class BlockStairsColored extends BlockStairs {
 	public BlockStairsColored(int id, String name, String readableName, Block block, int blockMeta){
 		super(id, block, blockMeta);
 		this.setUnlocalizedName(name);
 		GameRegistry.registerBlock(this, name);
 		LanguageRegistry.addName(this, readableName);
 
 		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0); // wooden pickaxe
 
 		GameRegistry.addRecipe(new ItemStack(this, 4), "b  ", "bb ", "bbb", 'b', new ItemStack(block, 1, blockMeta));
 	}
 }
