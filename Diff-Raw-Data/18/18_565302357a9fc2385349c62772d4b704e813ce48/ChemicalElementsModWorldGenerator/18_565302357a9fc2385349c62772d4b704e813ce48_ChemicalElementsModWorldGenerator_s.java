 package artsin.chemicalelementsmod;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.biome.BiomeGenDesert;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 
 import cpw.mods.fml.common.IWorldGenerator;
 
 public class ChemicalElementsModWorldGenerator implements IWorldGenerator
 {
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
 	{
 		switch (world.provider.dimensionId)
 		  {
 		   case -1: generateNether(world, random, chunkX*16, chunkZ*16);
 		   case 0: generateSurface(world, random, chunkX*16, chunkZ*16);
 		  }
 	}
 
 	public void generateSurface(World par1World, Random par2Random, int par3, int par4)// 3 - ChunkX, 4 - ChunkZ
 	{
 		int Xcoord = par3 + par2Random.nextInt(16);
 		int Ycoord = par2Random.nextInt(60);
 		int Zcoord = par4 + par2Random.nextInt(16);
 		generateOre(par1World, par2Random, Xcoord, Ycoord, Zcoord, ChemicalElementsMod.HgOre.blockID, 10); // Hg Ore
 		/* ---------- Silver Ore Start ---------- */
 		BiomeGenBase biomegen = par1World.getBiomeGenForCoords(par3, par4);
 		if (biomegen instanceof BiomeGenDesert)
 		{
 			int var5 = 3 + par2Random.nextInt(6);
 	        int var6;
 	        int var7;
 	        int var8;
 
 	        for (var6 = 0; var6 < var5; ++var6)
 	        {
 	            var7 = par3 + par2Random.nextInt(16);
 	            var8 = par2Random.nextInt(28) + 4;
 	            int var9 = par4 + par2Random.nextInt(16);
 	            int var10 = par1World.getBlockId(var7, var8, var9);
 
 	            if (var10 == Block.stone.blockID)
 	            {
	            	EntityPlayer player;
         			if (par1World.playerEntities.size() > 0)
         			{
         				player = (EntityPlayer)par1World.playerEntities.get(0);
         			}
         			else
         			{
         				player = (EntityPlayer)null;
         			}
         			if (player != null)
         			{
         				player.addChatMessage("SILVER GENERATED! Biome: " + biomegen.biomeName + "X: " + var7 + " Y: " + var8 + " Z: " + var9);
        			}
         			
 	                par1World.setBlock(var7, var8, var9, ChemicalElementsMod.SilverOre.blockID);
 	            }
 	        }
 		}
 		/* ---------- Silver Ore End ---------- */
 	}
 	
 	private void generateNether(World world, Random random, int i, int j)
 	{
 		
 	}
 	
 	private void generateOre(World par1World, Random par2Random, int par3, int par4, int par5, int minableBlockId, int numberOfBlocks)
     {
 		int var5 = numberOfBlocks + par2Random.nextInt(5) + 1; //kol-vo
     	int var6;
     	int var7;
     	int var8;
     	int rnd_identifier = new Random().nextInt(1000);
     
     	for (var6 = 0; var6 < var5; ++var6)
     	{
     		var7 = par3 + par2Random.nextInt(16);
     		var8 = par2Random.nextInt(46) + 4; //bilo 28
     		int var9 = par4 + par2Random.nextInt(16);
     		int var10 = par1World.getBlockId(var7, var8, var9);
 
     		if (var10 == Block.stone.blockID)
     		{
     			par1World.setBlock(var7, var8, var9, ChemicalElementsMod.HgOre.blockID);
     		}
     		
     		/*EntityPlayer player;
     		if (par1World.playerEntities.size() > 0)
     		{
     			player = (EntityPlayer)par1World.playerEntities.get(0);
     		}
     		else
     		{
     			player = (EntityPlayer)null;
     		}
     		if (player != null && (par1World.getBlockId(var7, var8, var9) == 2000))
     		{
     			player.addChatMessage("ORE GENERATED! X: " + var7 + " Y: " + var8 + " Z: " + var9 + " RAND:" + rnd_identifier);
     		}*/
     	}
     }
 }
