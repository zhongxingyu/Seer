 /**
  * Copyright (C) 2012 Jacob Scott <jascottytechie@gmail.com>
  *
  * Description: Cloud Generator for Bukkit
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package me.jascotty2.clouds;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.logging.Level;
 import me.jascotty2.libv2.io.CheckInput;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 public class Clouds extends JavaPlugin implements Listener {
 
 	int minCloudHeight = 6;
 	int maxCloudDepth = 3;
 	int cloudFloor = 80;
 	int cloudCeiling = 200;
 	int ground[] = new int[]{1, 2, 3, 4, 8, 9, 10, 11, 12, 13, 14, 15, 16, 48, 49, 79, 82, 110};
 	Map<World, SimplexOctaveGenerator> worldNoiseGenerators = new HashMap<World, SimplexOctaveGenerator>();
 	double noiseScale = 1 / 19.0;
 	int noiseOctaves = 6;
 	double noisefreq = 0.4, noiseamp = .3, yScale = 2.1;
 	double noisethreshold = .6;
 	int min_cloud_size = 5, max_cloud_size = -1;
 	int block_id = 80, block_data = 0;
 
 	@Override
 	public void onEnable() {
 		load();
 		getServer().getPluginManager().registerEvents(this, this);
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 	}
 
 	void load() {
 		saveDefaultConfig();
 		reloadConfig();
 		
 		minCloudHeight = configAssertInt("minCloudHeight", 0, 200);
 		maxCloudDepth = configAssertInt("maxCloudDepth", 1, 100);
 		cloudFloor = configAssertInt("cloudFloor", 0, 200);
 		cloudCeiling = configAssertInt("cloudCeiling", 5, 255);
 		noiseScale = configAssertDouble("noiseScale", 0.0000001, 999);
 		noiseOctaves = configAssertInt("noiseOctaves", 1, 50);
 		noisefreq = getConfig().getDouble("noisefreq");
 		noiseamp = getConfig().getDouble("noiseamp");
 		yScale = getConfig().getDouble("yScale");
 		noisethreshold = getConfig().getDouble("noisethreshold");
 		min_cloud_size = configAssertInt("min_cloud_size", 0, 5000);
 		block_id = getConfig().getInt("block_id");
 		block_data = configAssertInt("block_data", 0, 16);
 		max_cloud_size = configAssertInt("max_cloud_size", -1, -1);
 		
 		// validate data
 		if(noisefreq == 0) {
 			getConfig().set("noisefreq", noisefreq = 1);
 		}
 		if(noiseamp == 0) {
 			getConfig().set("noiseamp", noiseamp = 1);
 		}
 		if(cloudCeiling < cloudFloor) {
 			if(cloudFloor > 250) {
 				getConfig().set("cloudFloor", cloudFloor = (cloudFloor - 10));
 			}
 			getConfig().set("cloudCeiling", cloudCeiling = cloudFloor + 10);
 		}
 		if(Material.getMaterial(block_id) == null) {
 			getConfig().set("block_id", block_id = 80);
 		}
 		
 		worldNoiseGenerators.clear();
 		for (World w : getServer().getWorlds()) {
 			if (w.getEnvironment() == Environment.NORMAL) {
 				SimplexOctaveGenerator noise = new SimplexOctaveGenerator(w.getSeed(), noiseOctaves);
 				noise.setScale(noiseScale);
 				worldNoiseGenerators.put(w, noise);
 			}
 		}
 		
 		saveConfig();
 	}
 	
 	int configAssertInt(String key, int lowerBound, int upperBound) {
 		int value = getConfig().getInt(key);
 		if(value < lowerBound) {
 			getConfig().set(key, lowerBound);
 			return lowerBound;
 		} else if(upperBound > lowerBound && value > upperBound) {
 			getConfig().set(key, upperBound);
 			return upperBound;
 		}
 		return value;
 	}
 
 	double configAssertDouble(String key, double lowerBound, double upperBound) {
 		double value = getConfig().getDouble(key);
 		if(value < lowerBound) {
 			getConfig().set(key, lowerBound);
 			return lowerBound;
 		} else if(upperBound > lowerBound && value > upperBound) {
 			getConfig().set(key, upperBound);
 			return upperBound;
 		}
 		return value;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String commandLabel, String[] args) {
 		if (args.length >= 1 && (args[0].equalsIgnoreCase("generate") || args[0].equalsIgnoreCase("gen"))) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Must be a player to use this command");
 				return true;
 			}
 			int radius = 1;
 			if(args.length > 2 || (args.length == 2 && (radius = CheckInput.GetInt(args[1], 0)) <= 0)) {
 				sender.sendMessage(ChatColor.RED + "Invalid argument");
 				return false;
 			}
 			if(radius == 1) {
 				// clear existing clouds here
 				clearClouds(((Player) sender).getLocation().getChunk());
 				// regenerate chunk
 				genClouds(((Player) sender).getLocation().getChunk(), true);
 				sender.sendMessage(ChatColor.AQUA + "Clouds Regenerated!");
 			} else {
 				if (runID != -1) {
 					sender.sendMessage("Still Busy!");
 					sender.sendMessage("On Run " + run.run + " of " + (run.area * run.area));
 					return true;
 				} else {
 				// start regenerating
 					runID = getServer().getScheduler().scheduleSyncRepeatingTask(this, run = new RunThread(sender, ((Player) sender).getLocation().getWorld(), radius), 1, 1);
 					sender.sendMessage(ChatColor.AQUA + "Starting!");
 				}
 			}
 			
 		} else if (args.length == 1 && (args[0].equalsIgnoreCase("reload"))) {
 			load();
 			sender.sendMessage(ChatColor.AQUA + "Generator Settings Reloaded!");
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private static class Point3D {
 
 		public int x, y, z;
 
 		public Point3D() {
 		}
 
 		public Point3D(int x, int y, int z) {
 			this.x = x;
 			this.y = y;
 			this.z = z;
 		}
 	}
 
 	int runID = -1;
 	RunThread run = null;
 
 	class RunThread implements Runnable {
 
 		int run = 0;
 		private int area = 19;
 		private int areaix = -(area / 2);
 		private int areaiy = -(area / 2);
 		CommandSender p;
 		World w;
 		//PerlinNoiseGenerator noise;
 
 		RunThread(CommandSender sender, World world) {
 			p = sender;
 			w = world;
 			//noise= worldNoiseGenerators.get(w);
 		}
 
 		RunThread(CommandSender sender, World world, int radius) {
 			p = sender;
 			w = world;
 			//noise= worldNoiseGenerators.get(w);
 			area = (radius * 2) + 1;
 			areaix = -(area / 2);
 			areaiy = -(area / 2);
 		}
 		
 		@Override
 		public void run() {
 			final int cx = areaix + (run % area);
 			final int cy = areaiy + (run / area);
 
 			try {
 				Chunk c = w.getChunkAt(cx, cy);
 				// clear task
 				clearClouds(c);
 				// generate
				genClouds(c, false);
 			} catch (Exception ex) {
 				getLogger().log(Level.SEVERE, "Error in Chunk Generator:", ex);
 				p.sendMessage(ChatColor.RED + "Error in Cloud Chunk Generator (Check log for details)");
 				p.getServer().getScheduler().cancelTask(runID);
 				runID = -1;
 			}
 			if (++run >= area * area) {
 				p.getServer().getScheduler().cancelTask(runID);
 				runID = -1;
 				p.sendMessage(ChatColor.AQUA + "Done!");
 			}
 		}
 	}
 //	public static void main(String[] args) {
 //
 //		SimplexOctaveGenerator noise = new SimplexOctaveGenerator(592724999, 6);
 //		noise.setScale(1 / 20.0);
 //		final double freq = .4, amp = .3, yMod = 2.1;
 //		final double threshold = .6;
 //
 ////		int run = 4;
 ////		int area = 19;
 ////		int areaix = -(area / 2);
 ////		int areaiz = -(area / 2);
 ////		final int cx = areaix + (run % area);
 ////		final int cz = areaiz + (run / area);
 ////
 ////		double ix = (cx * 16) + 0.5,
 ////				iz = (cz * 16) + 0.5;
 //
 //		double ix = 0, iz = 0;
 //		
 //		final int maxSpan = 4;
 //		final int start = maxSpan * 16, end = (maxSpan * 2 + 1) * 16;
 //		
 //		long count = 0, startT = System.currentTimeMillis();
 //		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, total = 0, result;
 //
 //		double[][] array = new double[end][end];
 //		for (int x = 0; x < array.length; ++x) {
 //			for (int z = 0; z < array[x].length; ++z) {
 //				total += result = array[x][z] = noise.noise(ix + x - start, 0, iz + z - start, freq, amp, true) - threshold;
 //				if (result < min) {
 //					min = result;
 //				}
 //				if (result > max) {
 //					max = result;
 //				}
 //				++count;
 //			}
 //		}
 //		printArray(array);
 //		
 //		long endT = System.currentTimeMillis();
 //		System.out.println(count + " in " + String.format("%.2f", (endT - startT) / 1000.) + " seconds " + String.format("(%.2f/second)", count / ((double) endT - startT)));
 //		System.out.println("mean: " + (total / count));
 //		System.out.println("min: " + min);
 //		System.out.println("max: " + max);
 //		//System.out.printf("mod: %f\n", ((max) % .1) / .1);
 //	}
 //
 //	static void printArray(double array[][]) {
 //		final boolean markChunks = false;
 //		for (int x = 0; x < array.length; ++x) {
 //			if (markChunks) {
 //				if (x > 0 && x % 16 == 0) {
 //					for (int z = 0; z < array[x].length; ++z) {
 //						if (z > 0 && z % 16 == 0) {
 //							System.out.print("|");
 //						}
 //						if (x < array[x].length / 2) {
 //							// lower line
 //							System.out.print("\u2581");
 //						} else {
 //							// upper line
 //							System.out.print("\u2594");
 //						}
 //					}
 //					System.out.println();
 //				}
 //			}
 //			for (int z = 0; z < array[x].length; ++z) {
 //				if (markChunks && z > 0 && z % 16 == 0) {
 //					System.out.print("|");
 //				}
 //				System.out.print(array[x][z] > 0 ? "\u2588" : "\u2591");
 //			}
 //			System.out.println();
 //		}
 //	}
 //
 //	static void printArray(double array[][], boolean append) {
 //		Writer out;
 //		try {
 //			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cloud_out.txt", append), "UTF-8"));
 //			try {
 //				for (int x = 0; x < array.length; ++x) {
 //					if (x > 0 && x % 16 == 0) {
 //						for (int z = 0; z < array[x].length; ++z) {
 //							if (z > 0 && z % 16 == 0) {
 //								//System.out.print("|");
 //								out.write("|");
 //							}
 //							if (x < array[x].length / 2) {
 //								// lower line
 //								//System.out.print("\u2581");
 //								out.write("\u2581");
 //							} else {
 //								// upper line
 //								//System.out.print("\u2594");
 //								out.write("\u2594");
 //							}
 //						}
 //						//System.out.println();
 //						out.write("\n");
 //					}
 //					for (int z = 0; z < array[x].length; ++z) {
 //						if (z > 0 && z % 16 == 0) {
 //							//System.out.print("|");
 //							out.write("|");
 //						}
 //						//System.out.print(array[x][z] > 0 ? "\u2588" : "\u2591");
 //						out.write(array[x][z] > 0 ? "\u2588" : "\u2591");
 //					}
 //					//System.out.println();
 //					out.write("\n");
 //				}
 //			} finally {
 //				out.close();
 //			}
 //		} catch (Exception ex) {
 //			Logger.getLogger(Clouds.class.getName()).log(Level.SEVERE, null, ex);
 //		}
 //
 //	}
 	void clearClouds(Chunk c) {
 		for (int x = 0; x < 16; ++x) {
 			for (int y = cloudFloor; y <= cloudCeiling; ++y) {
 				for (int z = 0; z < 16; ++z) {
 					if (c.getBlock(x, y, z).getTypeId() == 80) {
 						c.getBlock(x, y, z).setTypeId(0);
 					}
 				}
 			}
 		}
 	}
 
 	void genClouds(Chunk c, boolean force) {
 		if (c.getWorld().getEnvironment() != Environment.NORMAL) {
 			return;
 		}
 		if (!force) {
 			List<MetadataValue> meta = c.getBlock(0, 0, 0).getMetadata("clouds");
 			if (!meta.isEmpty()) {
 				// just in case, check the data
 				boolean ignore = false;
 				for (MetadataValue v : meta) {
 					if (v.value() instanceof Boolean && (Boolean) v.value()) {
 						ignore = true;
 						break;
 					}
 				}
 				if (ignore) {
 					return;
 				}
 			}
 		}
 		SimplexOctaveGenerator noise = worldNoiseGenerators.get(c.getWorld());
 		if (noise == null) {
 			worldNoiseGenerators.put(c.getWorld(), noise = new SimplexOctaveGenerator(c.getWorld().getSeed(), noiseOctaves));
 			noise.setScale(noiseScale);
 		}
 
 		double ix = (c.getX() * 16),
 				iz = (c.getZ() * 16); //(Math.abs(c.getZ() + 10000) * 16) + 0.5;
 
 		// generate a double-array of the values returned for this layer (subtract threshold from each cell)
 		// for any values > 0, check that all neighbors are set
 		// once all set, return to the chunk section - for each defined area, calculate the cloud base height
 		// then add top 2 layers (adding next layer if prior exists)
 		// additionally - calculate all 3 layers before determining base height
 
 		double[][][] cloudchunk = new double[16][3][16];
 		boolean cloud = false;
 		for (int x = 0; x < 16; ++x) {
 			for (int y = 0; y < maxCloudDepth; ++y) {
 				for (int z = 0; z < 16; ++z) {
 					cloudchunk[x][y][z] = (noise.noise(ix + x, y * yScale, iz + z, noisefreq, noiseamp, true)) - noisethreshold;
 					if (cloudchunk[x][y][z] > 0) {
 						cloud = true;
 					}
 				}
 			}
 		}
 		if (!cloud) {
 			return;
 		}
 
 		// quick height map of the chunk
 		int landHeight[][] = new int[16][16];
 		for (int x = 0; x < 16; ++x) {
 			for (int z = 0; z < 16; ++z) {
 				for (int y = 255; y > 0; --y) {
 					final Block b = c.getBlock(x, y, z);
 					if (!b.isEmpty() && b.getType() != Material.SNOW_BLOCK) {
 						boolean isground = false;
 						for (int i = 0; i < ground.length && !isground; ++i) {
 							if (ground[i] == b.getTypeId()) {
 								isground = true;
 							}
 						}
 						if (isground) {
 							landHeight[x][z] = y;
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		// ready space for 5 chunks on each side
 		final int maxSpan = 5;
 		final int start = maxSpan * 16, end = (maxSpan * 2 + 1) * 16;
 		double[][][] cloudCache = new double[end][3][end];
 
 		// find & seperate the clouds
 		Stack<Point3D> path = new Stack<Point3D>();
 		double[][][] tempCloud = new double[16][3][16];
 		double total;
 		int count;
 		for (int x = 0; x < 16; ++x) {
 			int y = 0;
 			//for (; y < maxCloudDepth; ++y) {
 			for (int z = 0; z < 16; ++z) {
 				if (cloudchunk[x][y][z] > 0) {
 					for (int x2 = 0; x2 < 16; ++x2) {
 						for (int y2 = 0; y2 < maxCloudDepth; ++y2) {
 							for (int z2 = 0; z2 < 16; ++z2) {
 								tempCloud[x2][y2][z2] = 0;
 							}
 						}
 					}
 					count = 1;
 					tempCloud[x][y][z] = total = cloudchunk[x][y][z];
 					cloudchunk[x][y][z] = 0;
 
 					Point3D current = new Point3D(x, y, z);
 					while (current != null) {
 						int x2, y2, z2;
 						double v;
 
 						if (false // spacer, for formatting, lol
 								|| ((x2 = current.x + 1) < start && (y2 = current.y) >= 0 && (z2 = current.z) >= -start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								|| ((x2 = current.x) >= -start && (y2 = current.y) >= 0 && (z2 = current.z + 1) < start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								|| ((x2 = current.x) >= -start && (y2 = current.y + 1) < maxCloudDepth && (z2 = current.z) >= -start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								|| ((x2 = current.x - 1) >= -start && (y2 = current.y) >= 0 && (z2 = current.z) >= -start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								|| ((x2 = current.x) >= -start && (y2 = current.y) >= 0 && (z2 = current.z - 1) >= -start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								|| ((x2 = current.x) >= -start && (y2 = current.y - 1) >= 0 && (z2 = current.z) >= -start && (v = ((x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) ? cloudchunk[x2][y2][z2] : (cloudCache[start + x2][y2][start + z2] == 0 ? (cloudCache[start + x2][y2][start + z2] = (noise.noise(x2 + ix, y2 * yScale, z2 + iz, noisefreq, noiseamp, true) - noisethreshold)) : cloudCache[start + x2][y2][start + z2]))) > 0) //
 								) {
 							total += v;
 							++count;
 							if (x2 >= 0 && x2 < 16 && z2 >= 0 && z2 < 16) {
 								tempCloud[x2][y2][z2] = v;
 								cloudchunk[x2][y2][z2] = 0;
 							} else {
 								cloudCache[start + x2][y2][start + z2] = -1;
 							}
 							path.push(current);
 							current = new Point3D(x2, y2, z2);
 						} else if (!path.isEmpty()) {
 							current = path.pop();
 						} else {
 							current = null;
 						}
 					}
 					if (count >= min_cloud_size && (max_cloud_size <= 0 || count <= max_cloud_size)) {
 						// now have the cloud
 						total /= count;
 						// total is now a value 0 < x < freq - threshold
 						int height = cloudFloor + (int) Math.round((cloudCeiling - cloudFloor - (maxCloudDepth - 1)) * ((total % .05) / .05));
 						int groundHeight = 0;
 
 						// don't generate the cloud if it collides with something
 						boolean collision = false;
 						for (int x2 = 0; x2 < 16 && !collision; ++x2) {
 							for (int y2 = 0; y2 < maxCloudDepth && !collision; ++y2) {
 								for (int z2 = 0; z2 < 16 && !collision; ++z2) {
 									if (tempCloud[x2][y2][z2] > 0) {
 										if (landHeight[x2][z2] - y > groundHeight) {
 											groundHeight = landHeight[x2][z2] - y;
 										}
 										if (!c.getBlock(x2, height + y2, z2).isEmpty()) {
 											collision = true;
 										}
 									}
 								}
 							}
 						}
 						//minCloudHeight
 						if (!collision && height - groundHeight >= minCloudHeight) {
 							for (int x2 = 0; x2 < 16; ++x2) {
 								for (int y2 = 0; y2 < maxCloudDepth; ++y2) {
 									for (int z2 = 0; z2 < 16; ++z2) {
 										if (tempCloud[x2][y2][z2] > 0) {
 											c.getBlock(x2, height + y2, z2).setTypeIdAndData(block_id, (byte) block_data, false);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			// double-checking...
 			path.clear();
 			//}
 		}
 		// mark this chunk so don't mistakenly re-generate clouds
 		c.getBlock(0, 0, 0).setTypeId(7);
 		c.getBlock(0, 0, 0).setMetadata("clouds", new FixedMetadataValue(this, true));
 	}
 
 	@EventHandler
 	void onChunkGen(ChunkLoadEvent event) {
 		genClouds(event.getChunk(), false);
 	}
 
 	@EventHandler
 	void onSpawn(CreatureSpawnEvent event) {
 		// cancel spawning on 'clouds'
 		Block b = event.getLocation().getBlock();
 		while (b != null && b.isEmpty()) {
 			b = b.getRelative(BlockFace.DOWN);
 		}
 		if (b != null
 				&& b.getType() == Material.SNOW_BLOCK
 				&& b.getY() >= cloudFloor
 				&& event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
 			event.setCancelled(true);
 		}
 	}
 	
 //	@EventHandler
 //	void testHorseSpawn(org.bukkit.event.player.PlayerInteractEvent event) {
 //		if(event.getAction() == Action.RIGHT_CLICK_BLOCK 
 //				&& event.getClickedBlock().getTypeId() == 2
 //				&& event.getPlayer().getItemInHand().getTypeId() == 0) {
 //			
 //			org.bukkit.Location l = event.getClickedBlock().getLocation();
 //			org.bukkit.craftbukkit.v1_6_R1.CraftWorld w = (org.bukkit.craftbukkit.v1_6_R1.CraftWorld) event.getPlayer().getWorld();
 //			net.minecraft.server.v1_6_R1.EntityHorse h = new net.minecraft.server.v1_6_R1.EntityHorse(w.getHandle());
 //			h.setLocation(l.getX(), l.getY() + 1, l.getZ(), l.getYaw(), l.getPitch());
 //			h.p(0);
 //			h.q(0);
 //			
 //			w.getHandle().addEntity(h);
 //
 //		}
 //	}
 	
 }
