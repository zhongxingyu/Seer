 /**
  * Copyright (c) Scott Killen, 2012
  * 
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package bunyan.items;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.World;
 import bunyan.KeyPressManager;
 import bunyan.api.Direction;
 import bunyan.api.DirectionalBlock;
 import bunyan.api.ITurnable;
 import bunyan.blocks.BunyanBlock;
 
 public class LogTurner extends Item {
 
 	public LogTurner(int par1) {
 		super(par1);
 		setIconIndex(0);
 	}
 
 	@Override
 	public String getTextureFile() {
 		return "/bunyan/items/items.png";
 	}
 
 	@Override
 	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
 			World world, int x, int y, int z, int side)
 	{
 		final int id = world.getBlockId(x, y, z);
 		if (id == 0 || id != Block.wood.blockID
 				&& !(Block.blocksList[id] instanceof ITurnable))
 			return false;
 		Direction face = Direction.fromValue(side);
 		if (KeyPressManager.isModeKeyPressed())
 			face = face.oppositeSide();
 		if (id == Block.wood.blockID) {
 			if (side != 0 && side != 1) {
 				final int metadata = world.getBlockMetadata(x, y, z);
 				world.setBlock(x, y, z,
 						BunyanBlock.turnableVanillaWood.blockID);
 				DirectionalBlock.setDataAndFacing(world, x, y, z,
 						metadata, face, true);
 			}
 		} else
 			((ITurnable) Block.blocksList[id]).onLogTurner(player,
 					world, x, y, z, face);
 		return true;
 	}
 
 }
