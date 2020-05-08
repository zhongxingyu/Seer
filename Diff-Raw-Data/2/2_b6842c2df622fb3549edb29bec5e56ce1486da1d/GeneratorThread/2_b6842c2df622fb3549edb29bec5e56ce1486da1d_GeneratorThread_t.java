 package com.ubempire.render;
 /*
  * GeneratorThread.java
  *
  * Version 0.1
  *
  * Last Edited
  * 18/07/2011
  *
  * written by codename_B
  * forked by K900
  * forked by Nightgunner5
  */
 
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.World;
 
 public class GeneratorThread extends Thread {
     BananaMapRender plugin;
     int tileX;
     int tileZ;
     World world;
     boolean nether = false;
     boolean done = false;
     ChunkSnapshot[][] region = new ChunkSnapshot[32][32];
 
     GeneratorThread(BananaMapRender plugin, int tileX, int tileZ, World world) {
         super();
         this.plugin = plugin;
         this.tileX = tileX;
         this.tileZ = tileZ;
         this.world = world;
         this.nether = (world.getEnvironment() == World.Environment.NETHER);
     }
 
     @Override
 	public void run() {
     	int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new RegionGatherer(), 1, 10);
     	try {
     		synchronized (region) {
     			region.wait();
 			}
 		} catch (InterruptedException ex) {
 		}
     	plugin.getServer().getScheduler().cancelTask(taskId);
     	
         (new ChunkToPng(plugin)).makeTile(tileX, tileZ, world, region, nether);
         done = true;
     }
 
     private class RegionGatherer implements Runnable {
     	int row = 0;
 
 		public RegionGatherer() {
 		}
 
 		@Override
 		public void run() {
 			if (row < 32) {
				region[row] = BananaMapRender.prepareRegionRow(world, tileX, tileZ, row++);
 			}
     		if (row == 32) {
         		synchronized (region) {
         			region.notifyAll();
     			}
     		}
     	}
     }
 }
