 package btwmod.livemap;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 import btwmods.WorldAPI;
 
 import net.minecraft.src.AnvilChunkLoader;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.NBTTagCompound;
 
 public class RegionIterator implements Iterator<Chunk> {
 	
 	private Deque<QueuedRegion> regions = new ArrayDeque<QueuedRegion>();
 	private QueuedRegion currentRegion = null;
 	private int currentXOffset = 0;
 	private int currentZOffset = 0;
 	
 	private Chunk nextChunk = null;
 	
 	public synchronized void addRegion(int worldIndex, int x, int z) throws NullPointerException, FileNotFoundException {
 		AnvilChunkLoader loader = WorldAPI.getAnvilChunkLoader(worldIndex);
 		if (loader == null)
 			throw new NullPointerException("The IChunkLoader could not be retrieved or is not an instance of AnvilChunkLoader.");
 			
 		File location = WorldAPI.getAnvilSaveLocation(worldIndex);
 		if (location == null)
 			throw new NullPointerException("The anvil save location could not be determined for the world.");
 		
 		if (!new File(new File(location, "region"), "r." + x + "." + z + ".mca").isFile())
 			throw new FileNotFoundException("Region file for " + x + "." + z + " does not exist.");
 		
 		regions.add(new QueuedRegion(worldIndex, location, x, z));
 	}
 	
 	public synchronized int addWorld(int worldIndex) throws NullPointerException, FileNotFoundException, IOException {
 		AnvilChunkLoader loader = WorldAPI.getAnvilChunkLoader(worldIndex);
 		if (loader == null)
 			throw new NullPointerException("The IChunkLoader could not be retrieved or is not an instance of AnvilChunkLoader.");
 		
 		File location = WorldAPI.getAnvilSaveLocation(worldIndex);
 		if (location == null)
 			throw new NullPointerException("The anvil save location could not be determined for the world.");
 				
 		File regionDir = new File(location, "region");
 		if (!regionDir.isDirectory())
 			throw new FileNotFoundException("The anvil save location for the world does not contain a 'region' directory.");
 
 		File[] files = regionDir.listFiles();
 		if (files == null)
 			throw new IOException("The files could not be listed for the 'region' directory in the world's anvil save location.");
 
 		List<QueuedRegion> regions = new ArrayList<QueuedRegion>();
 		for (File file : files) {
 			if (file.isFile() && file.getName().matches("^r\\.[\\-0-9]+\\.[\\-0-9]+\\.mca$")) {
 				String[] split = file.getName().split("\\.");
 				regions.add(new QueuedRegion(worldIndex, location, Integer.parseInt(split[1]), Integer.parseInt(split[2])));
 			}
 		}
 		
 		Collections.shuffle(regions, new Random(System.nanoTime()));
 		this.regions.addAll(regions);
 		return regions.size();
 	}
 	
 	public synchronized int getChunkCount() {
 		return (regions.size() * 32 * 32)
 				+ (currentRegion == null ? 0 : currentXOffset * 32 + currentZOffset + 1);
 	}
 	
 	public synchronized void clear() {
 		regions.clear();
 		currentRegion = null;
 	}
 	
 	private void iterateToNextValidChunk() {
 		if (currentRegion == null)
 			nextRegion();
 		
 		while (nextChunk == null && currentRegion != null) {
 			try {
 				AnvilChunkLoader chunkLoader = WorldAPI.getAnvilChunkLoader(currentRegion.worldIndex);
 				
 				if (chunkLoader != null) {
					NBTTagCompound chunkNBT = chunkLoader.loadChunkNBT(currentRegion.world, currentRegion.regionX << 5 | currentXOffset, currentRegion.regionZ | currentZOffset);
 					
 					if (chunkNBT != null && chunkNBT.hasKey("Level") && chunkNBT.getCompoundTag("Level").hasKey("Sections")) {
 						nextChunk = chunkLoader.readChunkFromNBT(currentRegion.world, chunkNBT.getCompoundTag("Level"), true);
 					}
 				}
 				
 			} catch (IOException e) {
 				// TODO: Handle errors?
 			}
 			
 			nextChunkOffset();
 		}
 	}
 	
 	private void nextChunkOffset() {
 		currentZOffset--;
 		
 		if (currentZOffset < 0) {
 			currentZOffset = 31;
 			currentXOffset--;
 		}
 		
 		if (currentXOffset < 0)
 			nextRegion();
 	}
 	
 	private void nextRegion() {
 		currentRegion = regions.pollFirst();
 		currentXOffset = currentZOffset = 31;
 	}
 
 	@Override
 	public synchronized boolean hasNext() {
 		if (nextChunk == null)
 			iterateToNextValidChunk();
 		
 		return nextChunk != null;
 	}
 
 	@Override
 	public synchronized Chunk next() {
 		if (!hasNext())
 			throw new NoSuchElementException();
 		
 		Chunk chunk = nextChunk;
 		nextChunk = null;
 		return chunk;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 }
