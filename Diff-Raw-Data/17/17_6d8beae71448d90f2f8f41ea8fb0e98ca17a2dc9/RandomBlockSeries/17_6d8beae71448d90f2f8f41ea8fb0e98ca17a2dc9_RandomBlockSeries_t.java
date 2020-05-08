 package com.LRFLEW.bukkit.skygrid.rseries;
 
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class RandomBlockSeries {
 	private GridBlockAbstract[] reset;
 	private GridBlockAbstract[] series;
 	private int pos;
 	public final int hight;
 	
 	public RandomBlockSeries (World world) {
		reset = new GridBlockAbstract[4 * world.getMaxHeight()];
 		hight = world.getMaxHeight();
 		
 		int i = 0;
 		for (int x = 0; x < 4; x++) {
 			for (int y = 0; y < world.getMaxHeight() / 4; y++) {
 				for (int z = 0; z < 4; z++) {
 					reset[i] = new GridBlockAbstract(x, y, z);
 					i++;
 				}
 			}
 		}
 		
 		reset();
 	}
 	
 	public void reset() {
 		pos = 0;
 		series = reset.clone();
 	}
 	
 	public Block nextBlock(Chunk chunk, Random random) {
 		return nextAbstractBlock(random).getBlock(chunk);
 	}
 	
 	public GridBlockAbstract nextAbstractBlock(Random random) {
 		int swap = pos + random.nextInt(series.length - pos);
 		GridBlockAbstract temp = series[pos];
 		series[pos] = series[swap];
 		series[swap] = temp;
 		return series[pos++];
 	}
 	
 }
