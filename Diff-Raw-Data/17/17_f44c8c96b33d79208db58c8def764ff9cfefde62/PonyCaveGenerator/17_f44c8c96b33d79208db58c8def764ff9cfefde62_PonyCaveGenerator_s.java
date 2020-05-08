 package net.nexisonline.spade.populators;
 
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import libnoiseforjava.module.RidgedSimplex;
 import net.minecraft.server.Block;
 import net.nexisonline.spade.InterpolatedDensityMap;
 import net.nexisonline.spade.SpadeLogging;
 import net.nexisonline.spade.SpadePlugin;
 import toxi.math.noise.SimplexNoise;
 
 public class PonyCaveGenerator extends SpadeEffectGenerator
 {
 
 	InterpolatedDensityMap m_interpolator;
 
 	RidgedSimplex m_ridged1;
 	RidgedSimplex m_ridged2;
 
 	SimplexNoise m_xTurbulence;
 	SimplexNoise m_yTurbulence;
 	SimplexNoise m_zTurbulence;
 
 	SimplexNoise m_simplex1;
 	SimplexNoise m_simplex2;
 	SimplexNoise m_simplex3;
 	SimplexNoise m_simplex4;
 
 	SimplexNoise m_simplex5;
 	SimplexNoise m_simplex6;
 
 	public PonyCaveGenerator(SpadePlugin plugin,ConfigurationNode node,long seed) {
 		super(plugin,node,seed);
		
 		// Configure
		
		
 		// Setup
 		m_interpolator = new InterpolatedDensityMap();
 
 		m_xTurbulence = new SimplexNoise(seed + 1);
 		m_xTurbulence.setFrequency(0.05);
 		m_xTurbulence.setAmplitude(10);
 
 		m_yTurbulence = new SimplexNoise(seed + 2);
 		m_yTurbulence.setFrequency(0.05);
 		m_yTurbulence.setAmplitude(10);
 
 		m_zTurbulence = new SimplexNoise(seed + 3);
 		m_zTurbulence.setFrequency(0.05);
 		m_zTurbulence.setAmplitude(10);
 
 		m_ridged1 = new RidgedSimplex(seed + 7);
 		m_ridged1.setFrequency(0.01);
 		m_ridged1.setAmplitude(9);
 
 		m_ridged2 = new RidgedSimplex(seed + 8);
 		m_ridged2.setFrequency(0.01);
 		m_ridged2.setAmplitude(11);
 	}
	
 
 	public static SpadeEffectGenerator getInstance(SpadePlugin plugin, ConfigurationNode node, long seed) {
 		return new PonyCaveGenerator(plugin,node,seed);
 	}
 
 	public void populate(World world, Random random, Chunk chunk) {
 		int X=chunk.getX();
 		int Z=chunk.getZ();
 		SpadeLogging.info(String.format("Generating caves in chunk (%d,%d)",chunk.getX(),chunk.getZ()));
 		double density = 0;
 
 		for (int x = 0; x < 16; x+=5)
 		{
 			for (int z = 0; z < 16; z+=5)
 			{
 				for (int y = 0; y < 128; y += 16)
 				{
 					double posX = x + (X * 16);
 					double posY = y - 64;
 					double posZ = z + (Z * 16);
 
 					double warpX = posX + m_xTurbulence.sample(posX, posY, posZ);
 					double warpY = posY + m_yTurbulence.sample(posX, posY, posZ);
 					double warpZ = posZ + m_zTurbulence.sample(posX, posY, posZ);
 
 					density = -12;
 
 					density += m_ridged1.sample(warpX, warpY, warpZ);					
 					density += m_ridged2.sample(warpX, warpY, warpZ);
 
 					//density -= (y * 0.5);
 
 					m_interpolator.setDensity(x, y, z, density);
 				}
 			}
 		}
 
 		m_interpolator.interpolate();
 
 		for (int x = 0; x < 16; x++)
 		{
 			for (int z = 0; z < 16; z++)
 			{
 				for (int y = 126; y > 2; y--)
 				{
 					byte id = (byte) chunk.getBlock(x, y, z).getTypeId();
 
 					if (m_interpolator.getDensity(x, y, z) > 5 &&
 							!blockIsWater(chunk,x,y,z) &&
 							!(
 									id==Block.LAVA.id ||
 									id==Block.STATIONARY_LAVA.id ||
 									id==Block.BEDROCK.id
 							) && 
 							!blockIsWater(chunk,x+1,y,z) && 
 							!blockIsWater(chunk,x-1,y,z) && 
 							!blockIsWater(chunk,x,y+1,z) && 
 							!blockIsWater(chunk,x,y-1,z) &&
 							!blockIsWater(chunk,x,y,z+1) && 
 							!blockIsWater(chunk,x,y,z-1)
 					)
 					{
						chunk.getBlock(x,y,z).setTypeId(((y<10)?Block.STATIONARY_LAVA.id:0));
 					}
 				}
 			}
 		}
 	}
 
 	private boolean blockIsWater(Chunk chunk,int x, int y, int z) {
 		if(x<0||x>15||z<0||z>15||y<0||y>127) return false;
 		byte id = (byte) chunk.getBlock(x, y, z).getTypeId();
 		return	id==Block.WATER.id ||
 		id==Block.STATIONARY_WATER.id;
 	}
 
 	@Override
 	public ConfigurationNode getConfiguration() {
 		ConfigurationNode cfg = Configuration.getEmptyNode();
 		return cfg;
 	}
 }
 
