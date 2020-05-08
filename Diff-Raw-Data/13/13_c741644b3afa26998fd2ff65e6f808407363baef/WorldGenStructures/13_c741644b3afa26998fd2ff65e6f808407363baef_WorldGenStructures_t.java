 package mods.Electrolysm.electro.basic.world;
 
 import static net.minecraftforge.common.ChestGenHooks.STRONGHOLD_CORRIDOR;
 
 import java.util.Random;
 
 import mods.Electrolysm.electro.electrolysmCore;
 import mods.Electrolysm.electro.biology.entity.EntityZombie_Scientist;
 import net.minecraft.block.Block;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.tileentity.TileEntityDispenser;
 import net.minecraft.util.Facing;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldManager;
 import net.minecraft.world.WorldProvider;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraftforge.common.BiomeDictionary;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class WorldGenStructures implements IWorldGenerator{
 	@Override
 	public void generate(Random random, int x, int z, World world,IChunkProvider chunkGenerator, IChunkProvider chunkProvider){
 
 		//Make sure it's not generating in the end or nether
 		if(world.provider.dimensionId != 1 && world.provider.dimensionId != -1){
 				generateSurface(world, random, x*16, z*16);
 			}
 			
 		}
     
 	public static void generateSurface(World world, Random random, int x, int z){
 
 		//Science Lab Generation Code:
 	if(world.getBiomeGenForCoords(x, z) == electrolysmCore.diseasedBiomeObj)
 	{
 		if(random.nextInt(100) == 1)
 		{
 			for(int i = 0; i < 1; i++)
 			{		
 				int xCoord = ((x + random.nextInt(16)));
 				int zCoord = z + random.nextInt(16);
 				int yCoord = getSurface(world, xCoord, zCoord);
 				int mossyStone = Block.cobblestoneMossy.blockID;
 				int cobble = Block.cobblestone.blockID;
 				int glow = Block.glowStone.blockID;
 				int ironBar = Block.fenceIron.blockID;
 				int wood = Block.wood.blockID;
 				int grass = electrolysmCore.diseasedGrass.blockID;
 				int stairs = Block.stairsCobblestone.blockID;
 				int pressure = Block.pressurePlateStone.blockID;
 				int dispenser = Block.tnt.blockID;
 				int redstone = Block.redstoneWire.blockID;
 				int stoneSlabHalf = 44;
 				int stoneSlabFull = Block.stoneDoubleSlab.blockID;
 				int desk = electrolysmCore.desk.blockID;
 				int books = Block.bookShelf.blockID;
				int dirt = Block.dirt.blockID;
 
 
 				//Layer 2
 				for(int gx = 0; gx < 16; gx++)
 				{
 					for(int gy = 0; gy < 16; gy++)
 					{
 					createBlock(world, xCoord + gx, yCoord, zCoord - gy, grass, 0);
 					}
 				}
 				for(int gx = 0; gx < 16; gx++)
 				{
 					for(int gy = 0; gy < 16; gy++)
 					{
					createBlock(world, xCoord + gx, yCoord - 1, zCoord - gy, dirt, 0);
 					}
 				}
 				for(int gx = 0; gx < 16; gx++)
 				{
 					for(int gy = 0; gy < 16; gy++)
 					{
					createBlock(world, xCoord + gx, yCoord - 2, zCoord - gy, dirt, 0);
 					}
 				}
 				for(int gx = 0; gx < 16; gx++)
 				{
 					for(int gy = 0; gy < 16; gy++)
 					{
					createBlock(world, xCoord + gx, yCoord - 3, zCoord - gy, dirt, 0);
 					}
 				}
 				for(int gx = 0; gx < 16; gx++)
 				{
 					for(int gy = 0; gy < 16; gy++)
 					{
					createBlock(world, xCoord + gx, yCoord - 4, zCoord - gy, dirt, 0);
 					}
 				}
 				
 				createBlock(world, xCoord + 6, yCoord, zCoord - 10, dispenser, 0);
 				createBlock(world, xCoord + 9, yCoord, zCoord - 10, dispenser, 0);
 				
 				//Half slabs!
 				for(int l = 0; l <= 12; l++)
 				{
 					createBlock(world, xCoord, yCoord + 4, zCoord - 13 + l, stoneSlabHalf, 0);
 				}
 				for(int l = 0; l <= 15; l++)
 				{
 					createBlock(world, xCoord + l, yCoord + 4, zCoord - 1, stoneSlabHalf, 0);
 				}
 				for(int l = 0; l <= 12; l++)
 				{
 					createBlock(world, xCoord + 15, yCoord + 4, zCoord - 13 + l, stoneSlabHalf, 0);
 				}
 				createBlock(world, xCoord + 1, yCoord + 4, zCoord - 10, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 2, yCoord + 4, zCoord - 10, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 3, yCoord + 4, zCoord - 10, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 4, yCoord + 4, zCoord - 10, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 5, yCoord + 4, zCoord - 10, stoneSlabHalf, 0);
 
 				createBlock(world, xCoord + 1, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 2, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 3, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 4, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 5, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				
 				createBlock(world, xCoord + 13, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				
 				createBlock(world, xCoord + 11, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 13, stoneSlabHalf, 0);
 				
 				createBlock(world, xCoord + 8, yCoord + 4, zCoord - 7, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 7, yCoord + 4, zCoord - 7, stoneSlabHalf, 0);
 				//======================
 				createBlock(world, xCoord + 6, yCoord + 4, zCoord - 11, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 6, yCoord + 4, zCoord - 12, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 6, yCoord + 4, zCoord - 9, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 6, yCoord + 4, zCoord - 8, stoneSlabHalf, 0);
 				
 				createBlock(world, xCoord + 9, yCoord + 4, zCoord - 11, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 9, yCoord + 4, zCoord - 12, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 9, yCoord + 4, zCoord - 9, stoneSlabHalf, 0);
 				createBlock(world, xCoord + 9, yCoord + 4, zCoord - 8, stoneSlabHalf, 0);
 				
 				createBlock(world, xCoord + 8, yCoord + 4, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 7, yCoord + 4, zCoord - 6, cobble, 0);
 				
 				//Layer 3 Wood
 				for(int WH = 1; WH <= 4 ; WH++){
 					createBlock(world, xCoord, yCoord + WH, zCoord - 1, wood, 0);
 					createBlock(world, xCoord, yCoord + WH, zCoord - 4, wood, 0);
 					createBlock(world, xCoord, yCoord + WH, zCoord - 7, wood, 0);
 					createBlock(world, xCoord, yCoord + WH, zCoord - 10, wood, 0);
 					createBlock(world, xCoord, yCoord + WH, zCoord - 13, wood, 0);
 					createBlock(world, xCoord + 6, yCoord + WH, zCoord - 13, wood, 0);
 					createBlock(world, xCoord + 9, yCoord + WH, zCoord - 13, wood, 0);
 					createBlock(world, xCoord + 12, yCoord + WH, zCoord - 13, wood, 0);
 					createBlock(world, xCoord + 15, yCoord + WH, zCoord - 13, wood, 0);
 					createBlock(world, xCoord + 15, yCoord + WH, zCoord - 10, wood, 0);
 					createBlock(world, xCoord + 15, yCoord + WH, zCoord - 7, wood, 0);
 					createBlock(world, xCoord + 15, yCoord + WH, zCoord - 4, wood, 0);
 					createBlock(world, xCoord + 15, yCoord + WH, zCoord - 1, wood, 0);
 					createBlock(world, xCoord + 12, yCoord + WH, zCoord - 1, wood, 0);
 					createBlock(world, xCoord + 9, yCoord + WH, zCoord - 1, wood, 0);
 					createBlock(world, xCoord + 6, yCoord + WH, zCoord - 1, wood, 0);
 					createBlock(world, xCoord + 3, yCoord + WH, zCoord - 1, wood, 0);
 
 					createBlock(world, xCoord + 9, yCoord + WH, zCoord - 10, wood, 0);
 					createBlock(world, xCoord + 9, yCoord + WH, zCoord - 7, wood, 0);
 					createBlock(world, xCoord + 6, yCoord + WH, zCoord - 10, wood, 0);
 					createBlock(world, xCoord + 6, yCoord + WH, zCoord - 7, wood, 0);
 
 				}
 				for(int CH = 1; CH <= 4; CH++){
 					//Wall 1
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 3, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 4, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 5, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 6, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 7, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 8, cobble, 0);
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 9, cobble, 0);
 					//Wall 2
 					createBlock(world, xCoord + 1, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 2, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 3, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 4, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 5, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 6, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 7, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 8, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 9, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 11, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 12, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 13, yCoord + CH, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 2, cobble, 0);
 					//Wall 3
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 3, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 4, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 5, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 6, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 7, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 8, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 9, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 10, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 11, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + CH, zCoord - 12, cobble, 0);
 					//Wall 3/4
 					createBlock(world, xCoord + 13, yCoord + CH, zCoord - 12, cobble, 0);
 					createBlock(world, xCoord + 12, yCoord + CH, zCoord - 12, cobble, 0);
 					createBlock(world, xCoord + 11, yCoord + CH, zCoord - 12, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 12, cobble, 0);
 					
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 11, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 10, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 9, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 8, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 7, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + CH, zCoord - 6, cobble, 0);
 					
 					createBlock(world, xCoord + 9, yCoord + CH, zCoord - 6, cobble, 0);
 
 					createBlock(world, xCoord + 6, yCoord + CH, zCoord - 6, cobble, 0);
 					createBlock(world, xCoord + 5, yCoord + CH, zCoord - 6, cobble, 0);
 
 					createBlock(world, xCoord + 5, yCoord + CH, zCoord - 7, cobble, 0);
 					createBlock(world, xCoord + 5, yCoord + CH, zCoord - 8, cobble, 0);
 					createBlock(world, xCoord + 5, yCoord + CH, zCoord - 9, cobble, 0);
 
 					createBlock(world, xCoord + 4, yCoord + CH, zCoord - 9, cobble, 0);
 					createBlock(world, xCoord + 3, yCoord + CH, zCoord - 9, cobble, 0);
 					createBlock(world, xCoord + 2, yCoord + CH, zCoord - 9, cobble, 0);
 
 				}
 
 				
 				
 				for(int W2H = 5; W2H <= 8; W2H++)
 				{
 					//Wall 2
 					createBlock(world, xCoord + 2, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 3, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 4, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 5, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 6, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 7, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 8, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 9, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 10, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 11, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 12, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 13, yCoord + W2H, zCoord - 2, cobble, 0);
 					createBlock(world, xCoord + 14, yCoord + W2H, zCoord - 2, cobble, 0);
 				}
 				
 				for(int C1 = 0; C1 <= 4; C1++)
 				{
 					for(int Width = 0; Width < 5; Width++)
 					{
 					createBlock(world, xCoord + 5 - Width, yCoord + 5 + C1, zCoord - 9 + C1, stairs, 2);
 						for(int Length = 0; Length <= 2; Length++)
 						{
 						createBlock(world, xCoord + 1 + Width, yCoord + 9, zCoord - 4 + Length, cobble, 0);
 						}
 					} 
 				}
 				for(int C2 = 0; C2 <= 4; C2++)
 				{
 					for(int Width = 0; Width < 4; Width++)
 					{
 						createBlock(world, xCoord + 9 - Width, yCoord + 5 + C2, zCoord - 6 + C2, stairs, 2);
 					}
 				}
 				for(int C3 = 0; C3 <= 4; C3++)
 				{
 					for(int Width = 0; Width < 5; Width++)
 					{
 						createBlock(world, xCoord + 14 - Width, yCoord + 5 + C3, zCoord - 12 + C3, stairs, 2);
 						for(int Length = 0; Length < 6; Length++)
 						{
 						createBlock(world, xCoord + 10 + Width, yCoord + 9, zCoord - 7 + Length, cobble, 0);
 						}
 					}
 				}
 				//Wall 1 layer 1
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 2, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 3, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 5, zCoord - 8, cobble, 0);
 				//Wall Layer 2
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 2, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 3, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 6, zCoord - 7, cobble, 0);
 				//Wall Layer 3
 				createBlock(world, xCoord + 1, yCoord + 7, zCoord - 2, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 7, zCoord - 3, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 7, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 7, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 7, zCoord - 6, cobble, 0);
 				//Wall Layer 4
 				createBlock(world, xCoord + 1, yCoord + 8, zCoord - 2, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 8, zCoord - 3, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 8, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 1, yCoord + 8, zCoord - 5, cobble, 0);
 				/**
 				 * ===============================================================
 				 */
 				//Wall 2 Layer 1
 				createBlock(world, xCoord + 5, yCoord + 4, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 4, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 4, zCoord - 7, cobble, 0);
 				//Wall 2 Layer 2
 				createBlock(world, xCoord + 5, yCoord + 5, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 5, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 5, zCoord - 6, cobble, 0);
 				//Wall 2 Layer 3
 				createBlock(world, xCoord + 5, yCoord + 6, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 6, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 6, zCoord - 5, cobble, 0);
 				//Wall 2 Layer 4
 				createBlock(world, xCoord + 5, yCoord + 7, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 7, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 7, zCoord - 4, cobble, 0);
 				//Wall 2 Layer 5
 				createBlock(world, xCoord + 5, yCoord + 8, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 8, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 5, yCoord + 8, zCoord - 3, cobble, 0);
 				/**
 				 * ===============================================================
 				 */
 				//Wall 3 Layer 1
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 12, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 11, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 4, zCoord - 7, cobble, 0);
 				//Wall 3 Layer 2
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 11, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 5, zCoord - 6, cobble, 0);
 				//Wall 3 Layer 3
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 6, zCoord - 5, cobble, 0);
 				//Wall 3 Layer 4
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 7, zCoord - 4, cobble, 0);
 				//Wall 3 Layer 5
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 10, yCoord + 8, zCoord - 3, cobble, 0);
 				/**
 				 * ===============================================================
 				 */
 				//Wall 4 layer 1
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 12, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 11, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 4, zCoord - 3, cobble, 0);
 				//Wall 4 Layer 2
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 11, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 5, zCoord - 3, cobble, 0);
 				//Wall 4 Layer 3
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 10, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 6, zCoord - 3, cobble, 0);
 				//Wall 4 Layer 4
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 9, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 7, zCoord - 3, cobble, 0);
 				//Wall 4 Layer 5
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 8, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 7, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 6, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 5, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 4, cobble, 0);
 				createBlock(world, xCoord + 14, yCoord + 8, zCoord - 3, cobble, 0);
 				/**
 				 * ===============================================================
 				 *					Decorations!!
 				 * ===============================================================
 				 */
 				
 					//Slabs above wood
 					createBlock(world, xCoord, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 					createBlock(world, xCoord, yCoord + 5, zCoord - 4, stoneSlabHalf, 0);
 					createBlock(world, xCoord, yCoord + 5, zCoord - 7, stoneSlabHalf, 0);
 					createBlock(world, xCoord, yCoord + 5, zCoord - 10, stoneSlabHalf, 0);
 					createBlock(world, xCoord, yCoord + 5, zCoord - 13, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 6, yCoord + 5, zCoord - 13, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 9, yCoord + 5, zCoord - 13, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 12, yCoord + 5, zCoord - 13, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 15, yCoord + 5, zCoord - 13, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 15, yCoord + 5, zCoord - 10, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 15, yCoord + 5, zCoord - 7, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 15, yCoord + 5, zCoord - 4, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 15, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 12, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 9, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 6, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 3, yCoord + 5, zCoord - 1, stoneSlabHalf, 0);
 
 					createBlock(world, xCoord + 9, yCoord + 5, zCoord - 10, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 9, yCoord + 5, zCoord - 7, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 6, yCoord + 5, zCoord - 10, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 6, yCoord + 5, zCoord - 7, stoneSlabHalf, 0);
 					
 					//Other
 					createBlock(world, xCoord, yCoord, zCoord, cobble, 0);
 					createBlock(world, xCoord, yCoord, zCoord, cobble, 0);
 					
 					//IronBars
 					createBlock(world, xCoord, yCoord + 1, zCoord - 11, ironBar, 0);
 					createBlock(world, xCoord, yCoord + 1, zCoord - 12, ironBar, 0);
 					
 					createBlock(world, xCoord + 1, yCoord + 1, zCoord - 13, ironBar, 0);
 					createBlock(world, xCoord + 2, yCoord + 1, zCoord - 13, ironBar, 0);
 					createBlock(world, xCoord + 3, yCoord + 1, zCoord - 13, ironBar, 0);
 					createBlock(world, xCoord + 4, yCoord + 1, zCoord - 13, ironBar, 0);
 					createBlock(world, xCoord + 5, yCoord + 1, zCoord - 13, ironBar, 0);
 
 					createBlock(world, xCoord + 6, yCoord + 1, zCoord - 11, ironBar, 0);
 					createBlock(world, xCoord + 6, yCoord + 1, zCoord - 12, ironBar, 0);
 					
 					createBlock(world, xCoord + 2, yCoord + 2, zCoord - 9, ironBar, 0);
 					createBlock(world, xCoord + 2, yCoord + 3, zCoord - 9, ironBar, 0);
 					createBlock(world, xCoord + 3, yCoord + 2, zCoord - 9, ironBar, 0);
 					createBlock(world, xCoord + 3, yCoord + 3, zCoord - 9, ironBar, 0);
 					createBlock(world, xCoord + 4, yCoord + 2, zCoord - 9, ironBar, 0);
 					createBlock(world, xCoord + 4, yCoord + 3, zCoord - 9, ironBar, 0);
 
 					createBlock(world, xCoord + 3, yCoord + 7, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 3, yCoord + 8, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 6, yCoord + 7, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 6, yCoord + 8, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 9, yCoord + 7, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 9, yCoord + 8, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 12, yCoord + 7, zCoord - 2, ironBar, 0);
 					createBlock(world, xCoord + 12, yCoord + 8, zCoord - 2, ironBar, 0);
 				
 					//Floor
 					for(int length = 0; length < 13; length++)
 					{
 						createBlock(world, xCoord + 7, yCoord, zCoord - 15 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 8, yCoord, zCoord - 15 + length, stoneSlabFull, 0);
 					}
 					for(int length = 0; length < 6; length++)
 					{
 						createBlock(world, xCoord + 2, yCoord, zCoord - 8 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 3, yCoord, zCoord - 8 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 4, yCoord, zCoord - 8 + length, stoneSlabFull, 0);
 					}
 					for(int length = 0; length < 9; length++)
 					{
 						createBlock(world, xCoord + 11, yCoord, zCoord - 11 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 12, yCoord, zCoord - 11 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 13, yCoord, zCoord - 11 + length, stoneSlabFull, 0);
 					}
 					for(int length = 0; length < 3; length++)
 					{
 						createBlock(world, xCoord + 5, yCoord, zCoord - 5 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 6, yCoord, zCoord - 5 + length, stoneSlabFull, 0);
 					}
 					for(int length = 0; length < 3; length++)
 					{
 						createBlock(world, xCoord + 9, yCoord, zCoord - 5 + length, stoneSlabFull, 0);
 						createBlock(world, xCoord + 10, yCoord, zCoord - 5 + length, stoneSlabFull, 0);
 					}
 					//Tnt Explosion
 					//Layer 1
 					createBlock(world, xCoord + 7, yCoord - 1, zCoord - 10, redstone, 0);
 					createBlock(world, xCoord + 8, yCoord - 1, zCoord - 10, redstone, 0);
 					createBlock(world, xCoord + 7, yCoord + 1, zCoord - 10, pressure, 0);
 					createBlock(world, xCoord + 8, yCoord + 1, zCoord - 10, pressure, 0);
 			
 					//Cage!
 					createBlock(world, xCoord + 9, yCoord + 1, zCoord - 3, ironBar, 0);
 					createBlock(world, xCoord + 9, yCoord + 2, zCoord - 3, ironBar, 0);
 					createBlock(world, xCoord + 9, yCoord + 3, zCoord - 3, ironBar, 0);
 					
 					createBlock(world, xCoord + 11, yCoord + 1, zCoord - 3, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 2, zCoord - 3, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 3, zCoord - 3, ironBar, 0);
 					
 					createBlock(world, xCoord + 9, yCoord + 3, zCoord - 4, ironBar, 0);
 					createBlock(world, xCoord + 10, yCoord + 3, zCoord - 4, ironBar, 0);
 					createBlock(world, xCoord + 10, yCoord + 3, zCoord - 4, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 2, zCoord - 4, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 1, zCoord - 4, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 1, zCoord - 4, ironBar, 0);
 
 					createBlock(world, xCoord + 10, yCoord + 4, zCoord - 3, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 11, yCoord + 4, zCoord - 3, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 12, yCoord + 4, zCoord - 3, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 10, yCoord + 4, zCoord - 4, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 11, yCoord + 4, zCoord - 4, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 12, yCoord + 4, zCoord - 4, stoneSlabHalf, 0);
 					
 					//=================
 					createBlock(world, xCoord + 11, yCoord + 1, zCoord - 6, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 2, zCoord - 6, ironBar, 0);
 					createBlock(world, xCoord + 11, yCoord + 3, zCoord - 6, ironBar, 0);
 					createBlock(world, xCoord + 13, yCoord + 1, zCoord - 6, ironBar, 0);
 					createBlock(world, xCoord + 13, yCoord + 2, zCoord - 6, ironBar, 0);
 					createBlock(world, xCoord + 13, yCoord + 3, zCoord - 6, ironBar, 0);
 					
 					createBlock(world, xCoord + 11, yCoord + 4, zCoord - 6, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 12, yCoord + 4, zCoord - 6, stoneSlabHalf, 0);
 					createBlock(world, xCoord + 13, yCoord + 4, zCoord - 6, stoneSlabHalf, 0);
 					
 					createBlock(world, xCoord + 4, yCoord + 1, zCoord - 8, desk, 0);
 					createBlock(world, xCoord + 3, yCoord + 1, zCoord - 8, desk, 0);
 					createBlock(world, xCoord + 2, yCoord + 1, zCoord - 8, desk, 0);
 					
 					createBlock(world, xCoord + 13, yCoord + 1, zCoord - 11, desk, 0);
 					createBlock(world, xCoord + 12, yCoord + 1, zCoord - 11, books, 0);
 					createBlock(world, xCoord + 11, yCoord + 1, zCoord - 11, books, 0);
 					
 					spawnZombie_Scientist(world, xCoord, yCoord, zCoord);
 			}	
 		}
 	}
 }
 	private static void createBlock(World world, int xCoord, int yCoord, int zCoord, int blockID, int metadata)
 	{
 		world.setBlock(xCoord, yCoord, zCoord, blockID, metadata, 2);
 	}
 
 	private static int getSurface(World world, int x, int z)
 	{
 		int height = 256;
 
 		boolean continueQ = true;
 
 		while(continueQ)
 		{
 			if(world.isAirBlock(x, height, z))
 			{
 				height = height - 1;
 			}
 			else
 			{
 				continueQ = false;
 			}
 
 		}
 
 		return height + 1;
 
 				}
 	
 	public static EntityZombie_Scientist spawnZombie_Scientist(World world, int x, int y,
 			int z) {
 		EntityZombie_Scientist robot = new EntityZombie_Scientist(world);
 
 		EntityLiving entity = robot;
 		robot.setLocationAndAngles(x, y, z, MathHelper
 				.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F), 0.0F);
 		entity.rotationYawHead = entity.rotationYaw;
 		entity.renderYawOffset = entity.rotationYaw;
 		entity.initCreature();
 		world.spawnEntityInWorld(robot);
 		entity.playLivingSound();
 
 		return robot;
 	}
 	}
