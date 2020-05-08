 package mods.firstspring.advfiller.lib;
 
 import java.util.ArrayList;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class BlockLib
 {
 
 	/**
 	 * @param stack
 	 *            :挿入するアイテム
 	 * @param inv
 	 *            :挿入先インベントリ
 	 * @return 余ったアイテムもしくはNull
 	 */
 	public static ItemStack insertStackToInventory(ItemStack s, IInventory inv)
 	{
 		ItemStack stack = s.copy();
 		for (int i = 0; i < inv.getSizeInventory(); i++)
 		{
 			ItemStack is = inv.getStackInSlot(i);
 			if (is == null)
 			{
 				inv.setInventorySlotContents(i, stack);
 				return null;
 			}
 			if (stack.isItemEqual(is))
 			{
 				int move = Math.min(stack.stackSize, 64 - is.stackSize);
 				stack.stackSize -= move;
 				is.stackSize += move;
 				if (stack.stackSize == 0)
 					return null;
 			}
 		}
 		return stack;
 	}
 
 	public static ItemStack insertStackToNearInventory(ItemStack s, TileEntity t)
 	{
 		TileEntity[] tile = new TileEntity[6];
 		World w = t.worldObj;
 		tile[0] = w.getBlockTileEntity(t.xCoord + 1, t.yCoord, t.zCoord);
 		tile[1] = w.getBlockTileEntity(t.xCoord - 1, t.yCoord, t.zCoord);
 		tile[2] = w.getBlockTileEntity(t.xCoord, t.yCoord + 1, t.zCoord);
 		tile[3] = w.getBlockTileEntity(t.xCoord, t.yCoord - 1, t.zCoord);
 		tile[4] = w.getBlockTileEntity(t.xCoord, t.yCoord, t.zCoord + 1);
 		tile[5] = w.getBlockTileEntity(t.xCoord, t.yCoord, t.zCoord - 1);
 		for (TileEntity til : tile)
 			if (til instanceof IInventory)
 			{
 				s = insertStackToInventory(s, (IInventory) til);
 				if (s == null)
 					return null;
 			}
 		return s;
 	}
 
 	public static ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z)
 	{
 		Block block = Block.blocksList[world.getBlockId(x, y, z)];
 		if (block == null)
 			return new ArrayList();
 		int meta = world.getBlockMetadata(x, y, z);
 		return block.getBlockDropped(world, x, y, z, meta, 0);
 	}
 	
 	public static ArrayList<ItemStack> getBlockSilkDropped(World world, int x, int y, int z)
 	{
 		ArrayList<ItemStack> スタック = new ArrayList<ItemStack>();
 		
 		if(Block.blocksList[world.getBlockId(x,y,z)] == null)
 			return スタック;
 			
 		if (Block.blocksList[world.getBlockId(x,y,z)].canSilkHarvest(world, null,x,y,z, world.getBlockMetadata(x,y,z)))
 		{
 			int j = 0;
 
 			if (world.getBlockId(x, y, z) >= 0 && world.getBlockId(x, y, z) < Item.itemsList.length && Item.itemsList[world.getBlockId(x,y,z)].getHasSubtypes())
 			{
 				j = world.getBlockMetadata(x, y, z);
 			}
 			
 			スタック.add(new ItemStack(world.getBlockId(x, y, z), 1, j));
 		}
 		else
 		{
 			ArrayList<ItemStack> items = Block.blocksList[world.getBlockId(x,y,z)].getBlockDropped(world, x, y, z, world.getBlockMetadata(x,y,z), 3);
 
             for (ItemStack item : items)
             {
             	スタック.add(item);
             }
 		}
 		return スタック;
 	}
 
 	public static boolean canChangeBlock(World world, int x, int y, int z)
 	{
 		return canChangeBlock(world.getBlockId(x, y, z), world, x, y, z);
 	}
 
 	public static boolean canChangeBlock(int blockID, World world, int x, int y, int z)
 	{
 		Block block = Block.blocksList[blockID];
 
 		if (blockID == 0 || block == null || block.isAirBlock(world, x, y, z))
 			return true;
 
 		if (block.getBlockHardness(world, x, y, z) < 0)
 			return false;
 
 		if (blockID == Block.lavaStill.blockID || blockID == Block.lavaMoving.blockID)
 			return false;
 
 		return true;
 	}
 
 }
