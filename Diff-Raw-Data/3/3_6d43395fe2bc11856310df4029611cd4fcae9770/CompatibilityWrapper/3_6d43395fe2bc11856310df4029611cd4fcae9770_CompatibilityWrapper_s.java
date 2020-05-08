 package tk.blackwolf12333.grieflog.utils;
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.entity.Player;
 
 import tk.blackwolf12333.grieflog.GriefLog;
 import tk.blackwolf12333.grieflog.rollback.SendChangesTask;
 
 public class CompatibilityWrapper {
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_4_R1(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_4_R1.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_4_R1.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_4_R1.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_4_R1.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_4_R1.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_4_R1.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_4_R1.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_4_R1.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_5_R2(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_5_R2.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_5_R2.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_5_R2.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_5_R2.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_5_R2.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_5_R2.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_5_R2.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_5_R2.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_5_R3(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_5_R3.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_5_R3.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_5_R3.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_5_R3.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_5_R3.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_5_R3.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_5_R3.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_5_R3.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_6_R1(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_6_R1.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_6_R1.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_6_R1.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_6_R1.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_6_R1.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_6_R1.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_6_R1.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_6_R1.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_6_R1.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_6_R2(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_6_R2.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_6_R2.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_6_R2.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_6_R2.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_6_R2.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_6_R2.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_6_R2.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_6_R2.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_6_R3(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_6_R3.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_6_R3.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_6_R3.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_6_R3.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_6_R3.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_6_R3.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_6_R3.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_6_R3.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void sendChanges_1_7_R1(SendChangesTask task, HashSet<Chunk> chunks) {
 		HashSet<net.minecraft.server.v1_7_R1.ChunkCoordIntPair> pairs = new HashSet<net.minecraft.server.v1_7_R1.ChunkCoordIntPair>();
 		for (Chunk c : chunks) {
 			pairs.add(new net.minecraft.server.v1_7_R1.ChunkCoordIntPair(c.getX(), c.getZ()));
 		}
 
 		for (Player p : task.getPlayers()) {
 			HashSet<net.minecraft.server.v1_7_R1.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_7_R1.ChunkCoordIntPair>();
 			if (p != null) {
 				net.minecraft.server.v1_7_R1.EntityPlayer ep = ((org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer) p).getHandle();
 				for (Object o : ep.chunkCoordIntPairQueue) {
 					queued.add((net.minecraft.server.v1_7_R1.ChunkCoordIntPair) o);
 				}
 				for (net.minecraft.server.v1_7_R1.ChunkCoordIntPair pair : pairs) {
 					if (!queued.contains(pair)) {
 						ep.chunkCoordIntPairQueue.add(pair);
 					}
 				}
 			}
 		}
 	}
 
 	public void sendChanges(SendChangesTask task, HashSet<Chunk> chunks) {
 		try {
 			Class.forName("net.minecraft.server.v1_5_R2.ChunkCoordIntPair");
 			sendChanges_1_5_R2(task, chunks);
 		} catch (ClassNotFoundException e) {
 			try {
 				Class.forName("net.minecraft.server.v1_4_R1.ChunkCoordIntPair");
 				sendChanges_1_4_R1(task, chunks);
 			} catch (ClassNotFoundException e2) {
 				try {
 					Class.forName("net.minecraft.server.v1_5_R3.ChunkCoordIntPair");
 					sendChanges_1_5_R3(task, chunks);
 				} catch (ClassNotFoundException e3) {
 					try {
 						Class.forName("net.minecraft.server.v1_6_R1.ChunkCoordIntPair");
 						sendChanges_1_6_R1(task, chunks);
 					} catch(ClassNotFoundException e4) {
 						try {
 							Class.forName("net.minecraft.server.v1_6_R2.ChunkCoordIntPair");
 							sendChanges_1_6_R2(task, chunks);
 						} catch(ClassNotFoundException e5) {
 							try {
 								Class.forName("net.minecraft.server.v1_6_R3.ChunkCoordIntPair");
 								sendChanges_1_6_R3(task, chunks);
 							} catch(ClassNotFoundException e6) {
 								try {
 									Class.forName("net.minecraft.server.v1_7_R1.ChunkCoordIntPair");
 									sendChanges_1_7_R1(task, chunks);
 								} catch(ClassNotFoundException e7) {
 									GriefLog.log.warning("You don't have a compatible CraftBukkit version, rollbacks are not possible.");
 									GriefLog.enableRollback = false;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void setBlockFast_1_4_R1(int x, int y, int z, String world, int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_4_R1.Chunk chunk = ((org.bukkit.craftbukkit.v1_4_R1.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 
 	private void setBlockFast_1_5_R2(int x, int y, int z, String world,	int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_5_R2.Chunk chunk = ((org.bukkit.craftbukkit.v1_5_R2.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 
 	private void setBlockFast_1_5_R3(int x, int y, int z, String world,	int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_5_R3.Chunk chunk = ((org.bukkit.craftbukkit.v1_5_R3.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 	
 	private void setBlockFast_1_6_R1(int x, int y, int z, String world,	int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_6_R1.Chunk chunk = ((org.bukkit.craftbukkit.v1_6_R1.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 	
 	private void setBlockFast_1_6_R2(int x, int y, int z, String world,	int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_6_R2.Chunk chunk = ((org.bukkit.craftbukkit.v1_6_R2.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 	
 	private void setBlockFast_1_6_R3(int x, int y, int z, String world,	int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_6_R3.Chunk chunk = ((org.bukkit.craftbukkit.v1_6_R3.CraftChunk) c).getHandle();
 		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 
 	private void setBlockFast_1_7_R1(int x, int y, int z, String world, int typeID, byte data) {
 		Chunk c = Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
 		net.minecraft.server.v1_7_R1.Chunk chunk = ((org.bukkit.craftbukkit.v1_7_R1.CraftChunk) c).getHandle();
		chunk.a(x & 15, y, z & 15, typeID, data);
 	}
 	
 	public void setBlockFast(int x, int y, int z, String world, int typeID,	byte data) {
 		try {
 			Class.forName("net.minecraft.server.v1_4_R1.Chunk");
 			setBlockFast_1_4_R1(x, y, z, world, typeID, data);
 		} catch (ClassNotFoundException e) {
 			try {
 				Class.forName("net.minecraft.server.v1_5_R2.Chunk");
 				setBlockFast_1_5_R2(x, y, z, world, typeID, data);
 			} catch (ClassNotFoundException e2) {
 				try {
 					Class.forName("net.minecraft.server.v1_5_R3.Chunk");
 					setBlockFast_1_5_R3(x, y, z, world, typeID, data);
 				} catch (ClassNotFoundException e3) {
 					try {
 						Class.forName("net.minecraft.server.v1_6_R1.Chunk");
 						setBlockFast_1_6_R1(x, y, z, world, typeID, data);
 					} catch (ClassNotFoundException e4) {
 						try {
 							Class.forName("net.minecraft.server.v1_6_R2.Chunk");
 							setBlockFast_1_6_R2(x, y, z, world, typeID, data);
 						} catch (ClassNotFoundException e5) {
 							try {
 								Class.forName("net.minecraft.server.v1_6_R3.Chunk");
 								setBlockFast_1_6_R3(x, y, z, world, typeID, data);
 							} catch (ClassNotFoundException e6) {
 								try {
 									Class.forName("net.minecraft.server.v1_7_R1.Chunk");
 									setBlockFast_1_7_R1(x, y, z, world, typeID, data);
 								} catch (ClassNotFoundException e7) {
 									GriefLog.log.warning("You don't have a compatible CraftBukkit version, rollbacks are not possible.");
 									GriefLog.enableRollback = false;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
