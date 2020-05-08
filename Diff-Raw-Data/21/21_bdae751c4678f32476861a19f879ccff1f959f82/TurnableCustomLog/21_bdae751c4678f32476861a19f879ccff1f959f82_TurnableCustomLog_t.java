 /**
  * Copyright (c) Scott Killen, 2012
  * 
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package bunyan.blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.World;
 import bunyan.api.Direction;
 import bunyan.api.DirectionalBlock;
 import bunyan.api.TurnableLog;
 
 public class TurnableCustomLog extends TurnableLog {
 
 	public static final int	metaFir		= 1;
 	public static final int	metaAcacia	= 2;
 
 	public TurnableCustomLog(int id) {
 		super(id, 70);
 	}
 
 	@Override
 	public void addCreativeItems(ArrayList itemList) {
 		itemList.add(new ItemStack(blockID, 1, metaFir));
 		itemList.add(new ItemStack(blockID, 1, metaAcacia));
 	}
 
 	@Override
	protected void dropBlockAsItem_do(World world, int x, int y, int z,
			ItemStack itemstack)
	{
		itemstack.itemID = new ItemStack(BunyanBlock.wood).itemID;
		super.dropBlockAsItem_do(world, x, y, z, itemstack);
	}

	@Override
 	public String getTextureFile() {
 		return "/bunyan/blocks/blocks.png";
 	}
 
 	@Override
 	public int getTextureOffsetFromFacingSideAndMetadata(
 			Direction facing, Direction side, int metadata)
 	{
 		int column = 0;
 
 		switch (side) {
 			case DOWN:
 			case UP:
 				switch (facing) {
 					case NORTH:
 						column = 1;
 						break;
 					case SOUTH:
 						column = 3;
 						break;
 					case WEST:
 						column = 4;
 						break;
 					case EAST:
 						column = 2;
 						break;
 					default:
 						break;
 				}
 				if (side == Direction.DOWN) column += 4;
 				break;
 			case NORTH:
 				switch (facing) {
 					case NORTH:
 					case SOUTH:
 						column = 0;
 						break;
 					case WEST:
 						column = 2;
 						break;
 					case EAST:
 						column = 4;
 						break;
 					default:
 						break;
 				}
 				break;
 			case SOUTH:
 				switch (facing) {
 					case NORTH:
 					case SOUTH:
 						column = 0;
 						break;
 					case WEST:
 						column = 4;
 						break;
 					case EAST:
 						column = 2;
 						break;
 					default:
 						break;
 				}
 				break;
 			case WEST:
 				switch (facing) {
 					case NORTH:
 						column = 4;
 						break;
 					case SOUTH:
 						column = 2;
 						break;
 					case WEST:
 					case EAST:
 						column = 0;
 						break;
 					default:
 						break;
 				}
 				break;
 			case EAST:
 				switch (facing) {
 					case NORTH:
 						column = 2;
 						break;
 					case SOUTH:
 						column = 4;
 						break;
 					case WEST:
 					case EAST:
 						column = 0;
 						break;
 					default:
 						break;
 				}
 		}
 		return column + getDataFromMetadata(metadata) * 16;
 	}
 
 	@Override
 	public int idDropped(int metadata, Random random, int alwaysZero) {
 		return BunyanBlock.wood.blockID;
 	}
 
 	@Override
 	public void onLogTurner(EntityPlayer player, World world, int x,
 			int y, int z, Direction side)
 	{
 		if (side == Direction.UP || side == Direction.DOWN) {
 			final int metadata = DirectionalBlock
 					.getDataFromMetadata(world
 							.getBlockMetadata(x, y, z));
 			world.setBlockAndMetadataWithNotify(x, y, z,
 					BunyanBlock.wood.blockID, metadata);
 		} else
 			DirectionalBlock.setFacing(world, x, y, z, side, true);
 	}
 
 }
