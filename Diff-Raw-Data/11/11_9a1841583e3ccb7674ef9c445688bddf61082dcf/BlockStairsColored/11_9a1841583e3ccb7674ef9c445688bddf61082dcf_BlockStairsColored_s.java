 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStairs;
 import net.minecraft.item.ItemStack;
 
 import net.minecraftforge.common.MinecraftForge;
 
 import com.qzx.au.core.Light;
 
 class BlockStairsColored extends BlockStairs {
 	public BlockStairsColored(int id, String name, String readableName, Block block, int blockMeta){
 		super(id, block, blockMeta);
 		this.setUnlocalizedName(name);
 		GameRegistry.registerBlock(this, name);
 		LanguageRegistry.addName(this, readableName);
 
 		// hack to fix lighting glitch
 		if(Cfg.enableLightingHack)
			this.setLightValue(Light.level[1]);
 
 		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0); // wooden pickaxe
 
 		GameRegistry.addRecipe(new ItemStack(this, 4), "b  ", "bb ", "bbb", 'b', new ItemStack(block, 1, blockMeta));
 	}
 }
