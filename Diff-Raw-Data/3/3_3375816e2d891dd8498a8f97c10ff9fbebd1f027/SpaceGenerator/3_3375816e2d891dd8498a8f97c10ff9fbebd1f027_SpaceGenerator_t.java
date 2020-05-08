 package com.araeosia.space;
 
 import com.araeosia.space.util.ChunkPair;
 import com.araeosia.space.util.Planet;
 import org.bukkit.World;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 public class SpaceGenerator extends ChunkGenerator {
 	private HashMap<ChunkPair, ArrayList<Planet>> planets = new HashMap<>();
 	private Space plugin;
 
 	public SpaceGenerator(Space plugin){
 		this.plugin = plugin;
 	}
 
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid){
 		ChunkPair cp = new ChunkPair(chunkX, chunkZ);
 		if(!planets.containsKey(cp)){
 			planets.put(cp, new ArrayList<Planet>());
 		}
 		byte[][] result = new byte[world.getMaxHeight() / 16][];
 		ArrayList<Vector> planetsToGenerate = new ArrayList<>();
 		for(int i=0; i<16; i++){
 			for(int j=0; j<world.getMaxHeight(); j++){
 				for(int k=0; k<16; k++){
 					if(random.nextInt(580000)==1337){
 						planetsToGenerate.add(new Vector(i, j, k));
 					}
 				}
 			}
 		}
 		for(Vector v : planetsToGenerate){
 			int coreRadius = random.nextInt(20)+4;
 			int totalRadius = random.nextInt(20)+coreRadius;
 			int coreMaterial = getMaterial(random, false);
 			int shellMaterial = getMaterial(random, true);
 			int coreX = v.getBlockX()+(chunkX*16);
 			int coreZ = v.getBlockZ()+(chunkZ*16);
 			Planet e = new Planet(coreMaterial, coreRadius, shellMaterial, totalRadius, coreX, v.getBlockY(), coreZ, chunkX, chunkZ);
 			plugin.debug("Created planet "+e.save()+" hash "+e.toString());
 			planets.get(cp).add(e);
 		}
 		if(planets.get(cp).size()==0){
 			return result;
 		}
 		ArrayList<Planet> planetsToProcess = (ArrayList<Planet>) planets.get(cp).clone();
 		plugin.debug("Found "+planetsToProcess+" planets that have been cloned.");
 		for(int i=-2; i<3; i++){
 			for(int j=-2; j<3; j++){
 				ChunkPair toCheck = new ChunkPair(chunkX+i, chunkZ+j);
 				if(!planets.containsKey(toCheck)){
 					planets.put(toCheck, new ArrayList<Planet>());
 				}
 				planets.get(toCheck).addAll(planetsToProcess);
 			}
 		}
 		for(Planet p : planetsToProcess){
 			plugin.debug("Currently working with "+p.save()+" hash "+p.toString());
 			for(int i=0; i<16; i++){
 				int realX = i+(p.getChunkX()*16);
 				for(int j=0; j<world.getMaxHeight(); j++){
 					for(int k=0; k<16; k++){
 						int realZ = k+(p.getChunkZ()*16);
						plugin.debug("Checking vector "+p.getVector().toString());
 						if(p.getVector().distance(new Vector(realX, j, realZ))<=p.getCoreRadius()){
 							setBlock(result, i, j, k, p.getCoreMaterial().byteValue());
 						}else if(p.getVector().distance(new Vector(realX, j, realZ))<=p.getTotalRadius()){
 							setBlock(result, i, j, k, p.getShellMaterial().byteValue());
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	private int getMaterial(Random r, boolean shell) {
 		int number = r.nextInt(1000);
 		if(shell){
 			if(number<5){
 				return 56;
 			}else if(number>=5 && number<15){
 				return 14;
 			}else if(number>=15 && number<30){
 				return 15;
 			}else if(number>=30 && number<125){
 				return 1;
 			}else if(number>=125 && number<250){
 				return 20;
 			}else if(number>=250 && number<275){
 				return 16;
 			}else if(number>=275 && number<325){
 				return 25;
 			}else if(number>=325 && number<375){
 				return 82;
 			}else if(number>=375 && number<450){
 				return 89;
 			}else if(number>=450 && number<525){
 				return 45;
 			}else if(number>=525 && number<575){
 				return 73;
 			}else if(number>=575 && number<650){
 				return 0;
 			}else if(number>=650 && number<700){
 				return 86;
 			}else if(number>=700 && number<800){
 				return 3;
 			}else if(number>=800 && number<850){
 				return 98;
 			}else if(number>=850 && number<900){
 				return 48;
 			}else if(number>=900 && number<933){
 				return 49;
 			}else if(number>=933 && number<966){
 				return 47;
 			}else{
 				return 121;
 			}
 		}else{
 			if(number<5){
 				return 56;
 			}else if(number>=5 && number<15){
 				return 14;
 			}else if(number>=15 && number<30){
 				return 15;
 			}else if(number>=30 && number<100){
 				return 1;
 			}else if(number>=100 && number<175){
 				return 10;
 			}else if(number>=175 && number<250){
 				return 8;
 			}else if(number>=250 && number<275){
 				return 16;
 			}else if(number>=275 && number<325){
 				return 25;
 			}else if(number>=325 && number<375){
 				return 7;
 			}else if(number>=375 && number<450){
 				return 89;
 			}else if(number>=450 && number<525){
 				return 45;
 			}else if(number>=525 && number<575){
 				return 73;
 			}else if(number>=575 && number<650){
 				return 0;
 			}else if(number>=650 && number<700){
 				return 86;
 			}else if(number>=700 && number<800){
 				return 3;
 			}else if(number>=800 && number<850){
 				return 98;
 			}else if(number>=850 && number<900){
 				return 48;
 			}else if(number>=900 && number<933){
 				return 49;
 			}else if(number>=933 && number<966){
 				return 47;
 			}else{
 				return 121;
 			}
 		}
 	}
 
 	public void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
 		if (result[y >> 4] == null) //is this chunkpart already initialised?
 		{
 			result[y >> 4] = new byte[4096]; //initialise the chunk part
 		}
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid; //set the block (look above, how this is done)
 	}
 
 	public HashMap<ChunkPair, ArrayList<Planet>> getPlanets() {
 		return planets;
 	}
 }
