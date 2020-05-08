 package com.vbitz.MinecraftScript.blocks;
 
 import java.util.HashMap;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 
 import com.vbitz.MinecraftScript.MinecraftScriptWorldAPI;
 import com.vbitz.MinecraftScript.ScriptingManager;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Material;
 import net.minecraft.src.World;
 
 public class ScriptedBlock extends Block {
 	
 	public Function rightClickFunction = null;
 	public Function updateFunction = null;
 	
 	public EntityPlayer lastUpdater = null;
 	
 	public HashMap<Integer, Integer> blockTextures = new HashMap<Integer, Integer>();
 	
 	public ScriptedBlock(int blockId) {
 		super(blockId, Material.ground);
 	}
 	
 	@Override
 	public int getBlockTextureFromSide(int par1) {
 		if (blockTextures.containsKey(par1)) {
 			return blockTextures.get(par1);
 		} else {
 			return 0;
 		}
 	}
 	
 	@Override
 	public boolean onBlockActivated(World par1World, int worldX, int worldY,
 			int worldZ, EntityPlayer par5EntityPlayer, int par6, float par7,
 			float par8, float par9) {
 		if (rightClickFunction != null) {
 			ScriptingManager.enterContext();
			ScriptingManager.runFunction(rightClickFunction, new MinecraftScriptWorldAPI(par1World, par5EntityPlayer), worldX, worldY, worldZ);
 			ScriptingManager.exitContext();
 		}
 		return true;
 	}
 	
 	@Override
 	public void onNeighborBlockChange(World par1World, int worldX, int worldY,
 			int worldZ, int neiBlock) {
 		if (updateFunction != null) {
 			ScriptingManager.enterContext();
			ScriptingManager.runFunction(updateFunction, new MinecraftScriptWorldAPI(par1World, lastUpdater), worldX, worldY, worldZ);
 			ScriptingManager.exitContext();
 		}
 		super.onNeighborBlockChange(par1World, worldX, worldY, worldZ, neiBlock);
 	}
 
 }
