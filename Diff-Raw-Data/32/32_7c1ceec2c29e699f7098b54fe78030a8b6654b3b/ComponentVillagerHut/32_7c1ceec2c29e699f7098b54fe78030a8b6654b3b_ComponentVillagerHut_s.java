 package MysticWorld.WorldGen;
 
 import java.util.List;
 import java.util.Random;
 
 import MysticOres.Blocks.BlockHandler;
 import MysticStones.MysticStones;
 import net.minecraft.block.Block;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.structure.ComponentVillage;
 import net.minecraft.world.gen.structure.ComponentVillageStartPiece;
 import net.minecraft.world.gen.structure.StructureBoundingBox;
 import net.minecraft.world.gen.structure.StructureComponent;
 
 public class ComponentVillagerHut extends ComponentVillage
 {
 	private int averageGroundLevel = -1;
 
 	public ComponentVillagerHut(ComponentVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5)
 	{
 		super(par1ComponentVillageStartPiece, par2);
 		this.coordBaseMode = par5;
 		this.boundingBox = par4StructureBoundingBox;
 	}
 	public static ComponentVillagerHut buildComponent (ComponentVillageStartPiece villagePiece, List pieces, Random random, int p1, int p2, int p3, int p4, int p5)
 	{
 		StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 7, 6, 7, p4);
 		return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null ? new ComponentVillagerHut(villagePiece, p5, random,
 				structureboundingbox, p4) : null;
 	}
 	public boolean addComponentParts (World world, Random random, StructureBoundingBox sbb)
 	{
 		if (this.averageGroundLevel < 0)
 		{
 			this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);
 			if (this.averageGroundLevel < 0)
 			{
 				return true;
 			}
 			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 4, 0);
 		}
 		/*------------------------------------------------------------------Base------------------------------------------------------------------------*/
 		this.fillWithMetadataBlocks(world, sbb, 0, 0, 0, 6, 0, 6, MysticStones.StoneBricks.blockID, 2, MysticStones.StoneBricks.blockID, 2, false);
 		this.fillWithBlocks(world, sbb, 0, 5, 0, 6, 5, 6, Block.cobblestoneWall.blockID, Block.cobblestoneWall.blockID, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 0, 1, 5, 0, 5, MysticStones.StoneBricks.blockID, 2, MysticStones.StoneBricks.blockID, 2, false);
 		this.fillWithMetadataBlocks(world, sbb, 2, 0, 2, 4, 0, 4, MysticStones.ReinforcedBricks.blockID, 4, MysticStones.ReinforcedBricks.blockID, 4, false);
 		/*------------------------------------------------------------------Edges-------------------------------------------------------------------------*/
 		this.fillWithMetadataBlocks(world, sbb, 0, 1, 0, 0, 4, 0, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 0, 1, 6, 0, 4, 6, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 6, 1, 0, 6, 4, 0, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 6, 1, 6, 6, 4, 6, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		/*-------------------------------------------------------------------Walls------------------------------------------------------------------------*/
 		this.fillWithMetadataBlocks(world, sbb, 0, 1, 1, 0, 1, 5, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 1, 0, 5, 1, 0, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 6, 1, 1, 6, 1, 5, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 1, 6, 5, 1, 6, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 0, 3, 1, 0, 3, 5, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 3, 0, 5, 3, 0, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 6, 3, 1, 6, 3, 5, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 3, 6, 5, 3, 6, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		this.fillWithMetadataBlocks(world, sbb, 0, 4, 1, 0, 4, 5, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 4, 0, 5, 4, 0, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 6, 4, 1, 6, 4, 5, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 4, 6, 5, 4, 6, Block.stoneBrick.blockID, 0, Block.stoneBrick.blockID, 0, false);
 		this.fillWithBlocks(world, sbb, 1, 1, 1, 5, 5, 5, 0, 0, false);
 		this.fillWithMetadataBlocks(world, sbb, 1, 4, 1, 5, 4, 5, MysticStones.Planks.blockID, 5, MysticStones.Planks.blockID, 5, false);
 		/*---------------------------------------------------------Glass and Door--------------------------------------------------------------------------*/
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 1, 2, 0, sbb);
 		this.placeBlockAtCurrentPosition(world, MysticStones.Planks.blockID, 5, 2, 2, 0, sbb);
 		this.placeDoorAtCurrentPosition(world, sbb, random, 3, 1, 0, this.getMetadataWithOffset(Block.doorWood.blockID, 1));
 		this.placeBlockAtCurrentPosition(world, MysticStones.Planks.blockID, 5, 4, 2, 0, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 5, 2, 0, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 1, 2, 6, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 2, 2, 6, sbb);
 		this.placeBlockAtCurrentPosition(world, MysticStones.Planks.blockID, 5, 3, 2, 6, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 4, 2, 6, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 5, 2, 6, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 0, 2, 1, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 0, 2, 2, sbb);
 		this.placeBlockAtCurrentPosition(world, MysticStones.Planks.blockID, 5, 0, 2, 3, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 0, 2, 4, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 0, 2, 5, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 6, 2, 1, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 6, 2, 2, sbb);
 		this.placeBlockAtCurrentPosition(world, MysticStones.Planks.blockID, 5, 6, 2, 3, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 6, 2, 4, sbb);
		this.placeBlockAtCurrentPosition(world, BlockHandler.ReinforcedGlass.blockID, 0, 6, 2, 5, sbb);
 		/*-------------------------------------------------------------Ladders---------------------------------------------------------------------------*/
 		int i = this.getMetadataWithOffset(Block.ladder.blockID, 3);
 		this.placeBlockAtCurrentPosition(world, Block.ladder.blockID, i, 3, 1, 5, sbb);
 		this.placeBlockAtCurrentPosition(world, Block.ladder.blockID, i, 3, 2, 5, sbb);
 		this.placeBlockAtCurrentPosition(world, Block.ladder.blockID, i, 3, 3, 5, sbb);
 		this.placeBlockAtCurrentPosition(world, Block.ladder.blockID, i, 3, 4, 5, sbb);
 		/*---------------------------------------------------------------Inside--------------------------------------------------------------------------*/
 		this.placeBlockAtCurrentPosition(world, Block.furnaceIdle.blockID, 0, 1, 1, 1, sbb);
 		this.placeBlockAtCurrentPosition(world, Block.workbench.blockID, 0, 1, 1, 4, sbb);
 		this.placeBlockAtCurrentPosition(world, Block.anvil.blockID, 2, 1, 1, 5, sbb);
 		this.generateStructureChestContents(world, sbb, random, 4, 1, 5, MysticHutUtil.MysticHutChest.getItems(random), MysticHutUtil.MysticHutChest.getCount(random));
 
 		for (int l = 0; l < 6; ++l)
 		{
 			for (int i1 = 0; i1 < 9; ++i1)
 			{
 				this.clearCurrentPositionBlocksUpwards(world, i1, 9, l, sbb);
 				this.fillCurrentPositionBlocksDownwards(world, MysticStones.StoneBricks.blockID, 2, i1, -1, l, sbb);
 			}
 		}
 		this.spawnVillagers(world, sbb, 3, 1, 3, 1);
 		return true;
 	}
 	protected int getVillagerType (int par1)
 	{
 		return 98943;
 	}
 }
