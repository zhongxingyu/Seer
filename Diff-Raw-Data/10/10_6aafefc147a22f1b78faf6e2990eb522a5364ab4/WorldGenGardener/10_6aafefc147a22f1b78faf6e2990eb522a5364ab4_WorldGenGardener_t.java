 package clashsoft.mods.morefood.world;
 
 import java.util.Random;
 
 import clashsoft.clashsoftapi.util.CSRandom;
 import clashsoft.clashsoftapi.util.CSWorld;
 import clashsoft.mods.morefood.MoreFoodMod;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.feature.WorldGenerator;
 
 public class WorldGenGardener extends WorldGenerator
 {
 	public static final ItemStack[]	possiblestacks	= new ItemStack[] { new ItemStack(MoreFoodMod.edibles, 9, 0), new ItemStack(MoreFoodMod.edibles, 9, 1), new ItemStack(MoreFoodMod.edibles, 9, 3), new ItemStack(MoreFoodMod.edibles, 9, 4), new ItemStack(MoreFoodMod.edibles, 9, 5), new ItemStack(MoreFoodMod.edibles, 9, 6), new ItemStack(MoreFoodMod.edibles, 9, 9), new ItemStack(MoreFoodMod.edibles, 9, 18), new ItemStack(MoreFoodMod.edibles, 9, 24), new ItemStack(Item.seeds, 14), new ItemStack(Item.potato, 9), new ItemStack(Item.carrot, 9), new ItemStack(Item.wheat, 14), };
 	
 	public boolean generate(World world, Random random, int x, int y, int z)
 	{
 		int direction = 2 + random.nextInt(3);
 		CSWorld.setFrame(world, x, y, z, x + 8, y + 5, z + 8, Block.cobblestone.blockID, 0);
 		CSWorld.setCube(world, x + 1, y + 1, z + 1, x + 7, y + 4, z + 7, 0, 0);
 		// Walls
 		CSWorld.setCube(world, x, y + 1, z + 1, x, y + 4, z + 7, Block.planks.blockID, 0);
 		CSWorld.setCube(world, x + 1, y + 1, z, x + 7, y + 4, z, Block.planks.blockID, 0);
 		CSWorld.setCube(world, x + 8, y + 1, z + 1, x + 8, y + 4, z + 7, Block.planks.blockID, 0);
 		CSWorld.setCube(world, x + 1, y + 1, z + 8, x + 7, y + 4, z + 8, Block.planks.blockID, 0);
 		// Floor
 		CSWorld.setCube(world, x + 1, y, z + 1, x + 7, y, z + 7, Block.wood.blockID, 0);
 		// Ceiling
 		CSWorld.setCube(world, x + 1, y + 5, z + 1, x + 7, y + 5, z + 7, Block.glass.blockID, 0);
 		// Windows
 		CSWorld.setBlock(world, x + 2, y + 2, z + 0, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 6, y + 2, z + 0, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 0, y + 2, z + 2, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 0, y + 2, z + 6, Block.thinGlass.blockID, 0);
 		
 		CSWorld.setBlock(world, x + 8, y + 2, z + 2, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 8, y + 2, z + 6, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 2, y + 2, z + 8, Block.thinGlass.blockID, 0);
 		CSWorld.setBlock(world, x + 6, y + 2, z + 8, Block.thinGlass.blockID, 0);
 		
 		CSWorld.setBlock(world, x + 1, y + 1, z + 2, Block.workbench.blockID, 0);
 		CSWorld.setBlock(world, x + 1, y + 1, z + 1, Block.furnaceIdle.blockID, 0);
 		world.setBlockTileEntity(x + 1, y + 1, z + 1, createFurnace());
 		CSWorld.setBlock(world, x + 1, y + 1, z + 3, Block.chest.blockID, 0);
 		world.setBlockTileEntity(x + 1, y + 1, z + 3, createGardenerChest());
 		
 		if (direction == 2) // North
 		{
 			CSWorld.setBlock(world, x + 4, y + 1, z, Block.doorWood.blockID, direction - 2);
 			CSWorld.setBlock(world, x + 4, y + 2, z, Block.doorWood.blockID, direction - 2 + 12);
 			
 			CSWorld.setBlock(world, x + 4, y + 3, z + 1, Block.torchWood.blockID, 3);
 			CSWorld.setBlock(world, x + 4, y + 3, z + 7, Block.torchWood.blockID, 0);
 		}
 		else if (direction == 3)
 		{
 			CSWorld.setBlock(world, x + 4, y + 1, z + 8, Block.doorWood.blockID, direction - 2);
 			CSWorld.setBlock(world, x + 4, y + 2, z + 8, Block.doorWood.blockID, direction - 2 + 12);
 			
 			CSWorld.setBlock(world, x + 4, y + 3, z + 7, Block.torchWood.blockID, 0);
 			CSWorld.setBlock(world, x + 4, y + 3, z + 1, Block.torchWood.blockID, 3);
 		}
 		else if (direction == 4)
 		{
 			CSWorld.setBlock(world, x + 8, y + 1, z + 4, Block.doorWood.blockID, direction - 2);
 			CSWorld.setBlock(world, x + 8, y + 2, z + 4, Block.doorWood.blockID, direction - 2 + 12);
 			
 			CSWorld.setBlock(world, x + 7, y + 3, z + 4, Block.torchWood.blockID, 2);
 			CSWorld.setBlock(world, x + 1, y + 3, z + 4, Block.torchWood.blockID, 0);
 		}
 		else if (direction == 5)
 		{
 			CSWorld.setBlock(world, x, y + 1, z + 4, Block.doorWood.blockID, direction - 2);
 			CSWorld.setBlock(world, x, y + 2, z + 4, Block.doorWood.blockID, direction - 2 + 12);
 			
 			CSWorld.setBlock(world, x + 1, y + 3, z + 4, Block.torchWood.blockID, 1);
 			CSWorld.setBlock(world, x + 7, y + 3, z + 4, Block.torchWood.blockID, 2);
 		}
 		return false;
 	}
 	
 	private TileEntity createFurnace()
 	{
 		TileEntityFurnace furnace = new TileEntityFurnace();
 		furnace.furnaceBurnTime = 40000;
 		furnace.setInventorySlotContents(1, new ItemStack(Item.bowlSoup, 1));
 		return furnace;
 	}
 	
 	private TileEntity createGardenerChest()
 	{
 		TileEntityChest chest = new TileEntityChest();
 		Random rnd = new Random();
 		
 		for (int i = 0; i < CSRandom.nextInt(rnd, 7, 20); i++)
 		{
 			ItemStack is = possiblestacks[rnd.nextInt(possiblestacks.length)];
			is = new ItemStack(is.getItem(), CSRandom.nextInt(rnd, 1, is.stackSize));
 			chest.setInventorySlotContents(rnd.nextInt(chest.getSizeInventory() - 1), is);
 		}
 		return chest;
 	}
 }
