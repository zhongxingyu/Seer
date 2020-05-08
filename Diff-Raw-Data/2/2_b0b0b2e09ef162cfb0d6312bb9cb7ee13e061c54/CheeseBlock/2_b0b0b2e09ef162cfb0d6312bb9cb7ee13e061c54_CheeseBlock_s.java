 package com.github.gustav9797.CheeseMod;
 
 import java.util.Random;
 
 import org.lwjgl.util.vector.Vector3f;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 public class CheeseBlock extends Block 
 {
 
 	int spreadAmount = 64;
 	int[] xLoop = { 1, -1, 0, 0, 0, 0 };
 	int[] yLoop = { 0, 0, 1, -1, 0, 0 };
 	int[] zLoop = { 0, 0, 0, 0, 1, -1 };
 	int[] blocksToTurnCheese = { dirt.blockID, sand.blockID,
 			gravel.blockID };
 
 	Random random = new Random();
 
 	public CheeseBlock(int id, int texture, Material material) 
 	{
 		super(id, texture, material);
 		super.setTickRandomly(true);
 		blockID = id;
 	}
 
 	@Override
 	public String getTextureFile() 
 	{
 		return "/CheeseMod/terrain.png";
 	}
 
 	public int tickRate()
 	{
 		return 1000;
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) 
 	{
 		ModLoader.getMinecraftInstance().thePlayer.sendChatMessage("Stop annoying me! I am Mr. Cheese number " + getMetadata(world, x, y, z));
 		return false;
 	}
 	
 	public int getMetadata(World world, int x, int y, int z)
 	{
 		return(world.getBlockMetadata(x, y, z));
 	}
 
 	double calcDist(Vector3f v1, Vector3f v2) 
 	{
 		Vector3f v = new Vector3f();
 		v.x = v1.x - v2.x;
 		v.y = v1.y - v2.y;
 		v.z = v1.z - v2.z;
 		return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
 	}
 	
 
 	@Override
 	public void updateTick(World world, int x, int y, int z, Random random) 
 	{
 		if (world.getBlockMetadata(x, y, z) < spreadAmount) 
 		{
 			int side = random.nextInt(6);
 			int xToSpread = 0;
 			int yToSpread = 0;
 			int zToSpread = 0;
 			switch (side) 
 			{
 			case 0:
 				xToSpread--;
 				break;
 			case 1:
 				xToSpread++;
 				break;
 			case 2:
 				yToSpread--;
 				break;
 			case 3:
 				zToSpread--;
 				break;
 			case 4:
 				zToSpread++;
 				break;
 			case 5:
 				yToSpread++;
 				break;
 				
 			}
 			
 			//int up = random.nextInt(700);
 			//if(up == 1)
 			//	yToSpread = 1;
 
 			int xSpreadPos = x + xToSpread;
 			int ySpreadPos = y + yToSpread;
 			int zSpreadPos = z + zToSpread;
 
 			for (int i = 0; i < 6; i++) 
 			{
 				int xVar = xLoop[i];
 				int yVar = yLoop[i];
 				int zVar = zLoop[i];
 				int tempId = world.getBlockId(x + xVar, y + yVar, z + zVar);
 				if (tempId == mushroomRed.blockID || tempId == mushroomBrown.blockID) 
 				{
 					turnCheeseIntoInfected(world, 64, x, y, z);
 				}
 			}
 
 			try 
 			{
 				Grow(world, random, xSpreadPos, ySpreadPos, zSpreadPos);
 				/*if (isEatable(world.getBlockId(xSpreadPos, ySpreadPos, zSpreadPos))) 
 				{
 					if (random.nextInt(8) == 0)
 						world.setBlockAndMetadataWithNotify(xSpreadPos, ySpreadPos, zSpreadPos, this.blockID, world.getBlockMetadata(x, y, z)+1);
 					else
 						world.setBlockAndMetadataWithNotify(xSpreadPos, ySpreadPos, zSpreadPos, this.blockID, world.getBlockMetadata(x, y, z));
 				}*/
 				
 				int xx, yy, zz;
 				
 				for (int i = 0; i < 6; i++)
 				{
 					xx = x;
 					yy = y;
 					zz = z;
 					
 					switch(i)
 					{
 					case 0:
 						xx++;
 						break;
 					case 1:
 						yy++;
 						break;
 					case 2:
 						zz++;
 						break;
 					case 3:
 						xx--;
 						break;
 					case 4:
 						yy--;
 						break;
 					case 5:
 						zz--;
 						break;
 					}
 					
 					int block = world.getBlockId(xx, yy, zz);
 					
 					if (calcNeighbors(world, xx, yy, zz) >= 4 && isEatable(block) || block == 0)
 					{
 						Grow(world, random, xx, yy, zz);//world.setBlockMetadataWithNotify(xx, yy, zz, (world.getBlockMetadata(x, y, z) + 3));
 					}
 				}
 			} catch (Throwable e) {}
 		}
 	}
 	
 	
 	
 	protected boolean isEatable(int blockId) {
 		return (blockId == 2 || blockId == 3 || blockId == 5 || blockId == 6 || blockId == 8 || blockId == 9 || blockId == 12 || blockId == 13 || blockId == 17 || blockId == 18 || blockId == 81 || blockId == 82 || blockId == 91 || blockId == 125 || blockId == 126 || blockId == 134);
 	}
 	
 	protected void Grow(World world, Random random, int x, int y, int z)
 	{
 		int oldBlockID = world.getBlockId(x, y, z);
 		if (isEatable(oldBlockID) || (oldBlockID == 0 && random.nextInt(8) < calcNeighbors(world, x, y, z) - ((random.nextInt(128) == 0)? 0:((random.nextInt(4) == 0)? 1:2))))
 		{
 			int blockID;
 			
 			if (CheeseMod.cheeseTypes.containsKey(oldBlockID))
 			{
 				blockID = (Integer)CheeseMod.cheeseTypes.get(oldBlockID);
 			}
 			else if (random.nextInt(16) == 0)
 			{
 				blockID = CheeseMod.glowcheese.blockID;
 			}
 			else
 			{
 				blockID = this.blockID;
 			}
 			
 			int metadata = world.getBlockMetadata(x, y, z)+1;
 			//if (random.nextInt(2) == 0)
 				//metadata++;
 			
 			world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.bow", 4.0F, world.rand.nextFloat() * 0.1F + 0.9F);
 			world.setBlockAndMetadataWithNotify(x, y, z, this.blockID, metadata);
 		}
 	}
 	
 	public static int blockID;
 
 	private void turnCheeseIntoInfected(World world, int amount, int x, int y, int z) 
 	{
 
		world.setBlock(x, y, z, infectedId);
 		
 		if (amount > 0)
 		{
 			for (int i = 0; i < 6; i++) 
 			{
 				try {
 					int xVar = xLoop[i];
 					int yVar = yLoop[i];
 					int zVar = zLoop[i];
 					// System.out.println(xVar + " " + yVar + "" + zVar);
 	
 					if (world.getBlockId(x + xVar, y + yVar, z + zVar) == super.blockID) 
 					{
 						turnCheeseIntoInfected(world, amount-1, x + xVar, y + yVar, z + zVar);
 					}
 				} catch (Throwable e) {}
 			}
 		}
 	}
 
 	private int calcNeighbors(World world, int x, int y, int z)
 	{
 		int xx,yy,zz;
 		int neighbors = 0;
 		for (int i = 0; i < 6; i++)
 		{
 			xx = x;
 			yy = y;
 			zz = z;
 			
 			switch(i)
 			{
 			case 0:
 				xx++;
 				break;
 			case 1:
 				yy++;
 				break;
 			case 2:
 				zz++;
 				break;
 			case 3:
 				xx--;
 				break;
 			case 4:
 				yy--;
 				break;
 			case 5:
 				zz--;
 				break;
 			}
 			
 			if (isCheese(world.getBlockId(xx, yy, zz)))
 				neighbors++;
 		}
 		return neighbors;
 	}
 	
 	private boolean isCheese(int blockID)
 	{
 		return (blockID == this.blockID);
 	}
 }
