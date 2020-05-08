 package se.mickelus.modjam.gen;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import se.mickelus.modjam.Constants;
 import se.mickelus.modjam.segment.Segment;
 import se.mickelus.modjam.segment.SegmentPlaceholder;
 import se.mickelus.modjam.segment.SegmentStore;
 
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraftforge.common.ChestGenHooks;
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class GenerationHandler implements IWorldGenerator {
 	
 	List<SegmentPlaceholder> pendingSegments;
 	List<int[]> occupiedPositions;
 	
 	
 	public GenerationHandler() {
 		
 		pendingSegments = new ArrayList<SegmentPlaceholder>();
 		occupiedPositions = new LinkedList<int[]>();
 		GameRegistry.registerWorldGenerator(this);
 	}
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world,
 			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		//world.setBlock(chunkX*16+1, 120, chunkZ*16+1, 1);
 		
 		boolean pendingGeneration;
 		
 		// randomly spawn a start segment
 		if(random.nextInt(Constants.DUNGEON_CHANCE) == 0) {
 			
 			// randomly select a height
 			int y = random.nextInt(3);
 			
 			// add placeholder to pending-list
 			pendingSegments.add(new SegmentPlaceholder(chunkX, y, chunkZ, 0, 0, 0, 0, 0, 0, Segment.TYPE_START));
 		}
 		
 		
 		// while there are placeholders for un-generated chunks
 		do {
 			pendingGeneration = false;
 			// iterate over all placeholders
 			for (int i = 0; i<pendingSegments.size(); i++) {
 				// if placeholder is in generated chunk
 				if(chunkProvider.chunkExists(pendingSegments.get(i).getX(), pendingSegments.get(i).getZ())) {
 					SegmentPlaceholder ph = pendingSegments.get(i);
 					
 					pendingSegments.remove(ph);
 					i--;
 					
 					// generate segment based on placeholder
 					generateSegment(ph, world, random);
 					
 					
 					pendingGeneration = true;
 				}
 			}
 				
 		} while(pendingGeneration);
 
 	}
 	
 	
 	private void generateSegment(SegmentPlaceholder placeholder, World world, Random random) {
 		// ask the segmentstore for a segment matching the placeholder
 		Segment segment = SegmentStore.getSegment(placeholder, random);
 		
 		// get segment position
 		int x = placeholder.getX() * 16;
 		int y = placeholder.getY() * 16;
 		int z = placeholder.getZ() * 16;
 		
 		// TODO : we should not have to handle this
 		if(segment == null) {
 			System.out.println("Unable to generate segment");
 			return;
 		}
 		
 		System.out.println(String.format("new seg x:%d y:%d z:%d %b",
 				x, y, z,
 				isOccupied(placeholder.getX(), placeholder.getY(), placeholder.getZ())));
 
 		// generate blocks
 		for(int sy = 0; sy < 16; sy++) {
 			for(int sz = 0; sz < 16; sz++) {
 				for(int sx = 0; sx < 16; sx++) {
 					int blockID = segment.getBlockID(sx, sy, sz);
 					if(blockID != -1) {
 						world.setBlock(x+sx, y+sy+4, z+sz, blockID, segment.getBlockData(sx, sy, sz), 2);
						if(blockID == 146){
 							TileEntityChest tc = (TileEntityChest) world.getBlockTileEntity(x+sx, y+sy, z+sz);
 							Random rand = new Random();
 							int items = rand.nextInt(5)+1;
 							for(int i = 0; i < items; i++) {
 								tc.setInventorySlotContents(rand.nextInt(tc.getSizeInventory()), ChestGenHooks.getOneItem(ChestGenHooks.DUNGEON_CHEST, rand));
 							}
 						}
 					}
 					
 				}
 			}
 		}
 		
 		// spawn tile entities
 		
 		
 		// spawn entities
 		
 
 		
 		// mark this position as occupied
 		occupyPosition(placeholder.getX(), placeholder.getY(), placeholder.getZ());
 		
 		
 		// updated pending segments based on this segments interfaces
 		updatePendingSegments(placeholder.getX(), placeholder.getY(), placeholder.getZ(), segment);
 		
 		
 		
 		
 	}
 	
 	/**
 	 * Check if the chunk at the given chunk coordinates has pending segments
 	 * @param chunkX
 	 * 		Chunk x coordinate
 	 * @param chunkZ
 	 * 		Chunk y coordinate
 	 * @return
 	 * 		True if there are pending segments in this chunk, otherwise false.
 	 */
 	private boolean chunkHasPending(int chunkX, int chunkZ) {
 		for (SegmentPlaceholder ph : pendingSegments) {
 			if(ph.getX() == chunkX && ph.getZ() == chunkZ) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private void updatePendingSegments(int x, int y, int z, int top, int bottom, int north, int south, int east, int west) {
 				
 		if(top != 0 && y<3 && !isOccupied(x, y+1, z))
 			getPlaceholder(x, y+1, z).setInterfaceBottom(top);
 		
 		if(bottom != 0 && y>0 && !isOccupied(x, y-1, z))
 			getPlaceholder(x, y-1, z).setInterfaceTop(bottom);
 		
 		if(north != 0 && !isOccupied(x, y, z-1))
 			getPlaceholder(x, y, z-1).setInterfaceSouth(north);
 		
 		if(south != 0 && !isOccupied(x, y, z+1))
 			getPlaceholder(x, y, z+1).setInterfaceNorth(south);
 		
 		if(east != 0 && !isOccupied(x+1, y, z))
 			getPlaceholder(x+1, y, z).setInterfaceWest(east);
 		
 		if(west != 0 && !isOccupied(x-1, y, z))
 			getPlaceholder(x-1, y, z).setInterfaceEast(west);
 	}
 	
 	private void updatePendingSegments(int x, int y, int z, Segment segment) {
 		updatePendingSegments(x, y, z, 
 				segment.getInterfaceTop(), segment.getInterfaceBottom(), 
 				segment.getInterfaceNorth(), segment.getInterfaceSouth(),
 				segment.getInterfaceEast(), segment.getInterfaceWest());
 	}
 	
 	
 	private SegmentPlaceholder getPlaceholder(int x, int y, int z) {
 		for (SegmentPlaceholder ph : pendingSegments) {
 			if(ph.getX() == x && ph.getY() == y && ph.getZ() == z) {
 				return ph;
 			}
 		}
 		
 		//System.out.println(String.format("new ph  %d %d %d %b", x, y, z, isOccupied(x, y, z)));
 		SegmentPlaceholder ph = new SegmentPlaceholder(x, y, z, 0, 0, 0, 0, 0, 0, 0);
 		pendingSegments.add(ph);
 		
 		return ph;
 	}
 
 	
 	private boolean isOccupied(int x, int y, int z) {
 		for (int[] pos : occupiedPositions) {
 			if(pos[0] == x && pos[1] == y && pos[2] == z) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void occupyPosition(int x, int y, int z) {
 		occupiedPositions.add(new int[] {x, y, z});
 	}
 	
 	private void clearOccupied() {
 		occupiedPositions.clear();
 	}
 	
 	
 	
 	
 	
 	
 	
 }
