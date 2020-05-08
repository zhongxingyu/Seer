 package dblockcache;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.*;
 import virtualdisk.VirtualDisk;
 
 import common.Constants;
 
 public class DBufferCache {
 	
 	private int _cacheSize;
 	public ArrayList<DBuffer> bufList; //list of DBuffers in cache. MRU -> LRU
 	private int maxBlocks;
 	private int numBlocks;
 	public VirtualDisk myVD;
 	public int[] blockMap; //an array of 1s and 0s. A 0 at index i means block ID=i is free, 1 means it is occupied
 	
 	public int nextINodeCounter; //increments by one each time we create an Inode BUFFER, since each file has an Inode block
 	
 	/*
 	 * Constructor: allocates a cacheSize number of cache blocks, each
 	 * containing BLOCK-size bytes data, in memory
 	 */
 	public DBufferCache(int cacheSize) throws IOException {
 		_cacheSize = cacheSize * Constants.BLOCK_SIZE;
 		bufList = new ArrayList<DBuffer>();
 		maxBlocks = cacheSize;
 		numBlocks = 0;
 		blockMap = new int[Constants.NUM_OF_DATA_BLOCKS];
 		System.out.println("this many data blocks" + Constants.NUM_OF_DATA_BLOCKS);
 	}
 	
 	public void getCounts() throws IOException { //used to initialize superblock data from disk	
 		DBuffer superBlock = getBlock(0); //the super block has ID =0
 		ByteBuffer wrapped = ByteBuffer.wrap(superBlock.contents);
 		nextINodeCounter = wrapped.getInt(); //the id we give the first Inode
		nextINodeCounter = nextINodeCounter/(Constants.INODES_PER_BLOCK)+1;
 		System.out.println("Next Inode block ID: " + nextINodeCounter);
 		//pulling the blockmap from disk
 		System.out.println("superblock in cache!");
 		releaseBlock(superBlock);
 	}
 	
 	public void storeCounts(int numberOfFiles) throws IllegalArgumentException, IOException {
 		DBuffer superBlock = getBlock(0);
 		byte[] num = ByteBuffer.allocate(4).putInt(numberOfFiles).array();
 		superBlock.write(num, 0, 4);
 		sync();	
 		releaseBlock(superBlock);
 	}
 	
 	public void getAllMapBlocks() throws IOException {
 		System.out.println("BLock map size: "+ blockMap.length);
 		int offset = 0;
 		int id = Constants.NUM_OF_INODE_BLOCKS+1;
 		int increment = Constants.BLOCK_SIZE/4; //the array size one Dbuffer can store
 		while (id <= Constants.NUM_OF_INODE_BLOCKS+Constants.NUM_OF_MAP_BLOCKS) {
 			getMapBlock(offset, id);
 			offset += Constants.BLOCK_SIZE/4;
 			id++;
 		}
 	}
 	
 	public void destroy(int id) { //called when a client wants to delete a file
 		int index = id - Constants.DATA_BLOCK_FIRST;
 		blockMap[index] = 0; //freeing this particular index in the blockmap
 		DBuffer temp  = null;
 		for (DBuffer buf: bufList) {
 			if (buf.ID == id) 
 				temp = buf; //remove this buffer from the cache
 		}
 		if (temp != null)
 			bufList.remove(temp);
  	}
 	
 	public void storeAllMapBlocks() throws IOException { //write blockmap to a cache buffer, which is then backed up to disk
 		System.out.println("storing block map");
 		System.out.println(Constants.NUM_OF_MAP_BLOCKS*(Constants.BLOCK_SIZE/4));
 		for(int i=0; i<Constants.NUM_OF_MAP_BLOCKS; i++){ // for every block in the map
 			DBuffer freelist = getBlock(Constants.NUM_OF_INODE_BLOCKS+1+i);
 			for(int j=0; j<(Constants.BLOCK_SIZE/4)-10; j++) { // for every int that fits in that block
 				int currentBlockID = i*(Constants.BLOCK_SIZE/4) + j;
 				byte[] data = ByteBuffer.allocate(4).putInt(blockMap[currentBlockID]).array();
 				freelist.write(data, j*4, 4);
 			}
 			sync();
 			releaseBlock(freelist);
 		}
 	}
 
 	public void checkMap(int index) {
 		System.out.println("Printing Block Map at index " + index);
 		System.out.println(blockMap[index]);
 	}
 	
 	public void getMapBlock(int offset, int id) throws IOException { //initialize blockmap from disk, offset is how far into blockMap we are writing into
 
 		DBuffer freelist = getBlock(id);
 		
 		byte[] contents = freelist.contents;
 		//System.out.println("content size: " + contents.length);	
 		for (int i = 0; i < contents.length; i+=4) {
 			byte[] temp = new byte[4];
 			for (int j =0; j < 4; j++) {
 				temp[j] = contents[i+j];
 			}	
 			ByteBuffer wrapped = ByteBuffer.wrap(temp);	
 			int n = wrapped.getInt();
 			//System.out.print(n + " ");
 			int index = offset + (i/4);
 			if (index >= blockMap.length) //stop writing to blockmap if it is full
 				return;
 			//System.out.println("n = " + n);
 			//System.out.println("Accessing blockMap at index: " + index);
 			blockMap[index] = n;
 		}
 		releaseBlock(freelist);
 	}
 		
 
 	
 	public int getNextDataBlockID() { //traverses the blockMap to find the first free id to assign a datablock
 		for (int i = 0; i < blockMap.length; i++) {
 			if (blockMap[i] == 0) {
 				int id = i;
 				blockMap[i] = 1; //this block Id is now taken
 				return id;
 			}
 		}
 		return -1; //this means we have no more free blocks, aka disk is full
 	}
 	
 	
 	
 	/*
 	 * Get buffer for block specified by blockID. The buffer is "held" until the
 	 * caller releases it. A "held" buffer cannot be evicted: its block ID
 	 * cannot change.
 	 */
 	public synchronized DBuffer getBlock(int blockID) throws IllegalArgumentException, IOException {
 		//System.out.println("looking for block" + blockID);
 		//System.out.println("I contain buffers" + bufList);
 //		if (blockID == 0)
 //			System.out.println("looking for superblock in disk");
 //		if (blockID >= Constants.NUM_OF_INODE_BLOCKS+1)
 //			System.out.println("looking for blockmap in disk");
 //		
 //		for(DBuffer buf: bufList){
 //			System.out.print(buf.ID + "  ");
 //		}
 		for (int i = 0; i < bufList.size(); i++) {
 			DBuffer current = bufList.get(i);
 			if (current.ID == blockID) {//our cache contains the buffer for the block we want
 				while (current.held) { //we need to wait
 					try {
 						wait();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				bufList.remove(current); //remove block from cache
 				bufList.add(0,current); //add it back at beginning since its is MRU
 				current.held = true;
 				return current;
 			}
 		}
 		//System.out.println("couldn't find the block!");
 		//our cache does not contain the buffer currently
 		DBuffer ourBlock = new DBuffer(blockID, false, false);
 		if (numBlocks == maxBlocks) {//our cache is full, we need to evict a block
 			bufList.remove(numBlocks-1); //remove LRU block
 			bufList.add(0,ourBlock); //add new block to beginning as MRU
 		}
 		else {
 			//System.out.println("adding a block!");
 			bufList.add(0,ourBlock);
 			numBlocks++;
 		}	
 		//do some stuff to fill the new buffer with the blocks contents
 		//also evict some block
 		ourBlock.held = true;
 		ourBlock.startFetch(myVD);
 		ourBlock.waitValid();
 		return ourBlock;	
 	}
 
 	
 	public synchronized DBuffer getBufToWriteTo() throws IllegalArgumentException, IOException {
 		int id = getNextDataBlockID()+Constants.DATA_BLOCK_FIRST; //the 0 index in blockMap corresponds to id DATA_BLOCK_FIRST, the id of the first data block
 		DBuffer ourBlock = new DBuffer(id, false, false);
 		if (numBlocks == maxBlocks) {//our cache is full, we need to evict a block
 			bufList.remove(numBlocks-1); //remove LRU block
 			bufList.add(0,ourBlock); //add new block to beginning as MRU
 		}
 		else {
 			bufList.add(0,ourBlock);
 			numBlocks++;
 		}	
 		ourBlock.held = true;
 		ourBlock.startFetch(myVD);
 		ourBlock.waitValid();
 		return ourBlock;
 	}
 	
 	public DBuffer getInodeBufToWriteTo() throws IllegalArgumentException, IOException { //returns an Inode buffer to DFS to write to
 		nextINodeCounter+=1;
 		DBuffer ourBlock = new DBuffer(nextINodeCounter, false, false);
 		if (numBlocks == maxBlocks) {//our cache is full, we need to evict a block
 			bufList.remove(numBlocks-1); //remove LRU block
 			bufList.add(0,ourBlock); //add new block to beginning as MRU
 		}
 		else {
 			bufList.add(0,ourBlock);
 			numBlocks++;
 		}	
 		ourBlock.held = true;
 		ourBlock.startFetch(myVD);
 		ourBlock.waitValid();
 		return ourBlock;
 	}
 
 	/* Release the buffer so that others waiting on it can use it */
 	public synchronized void releaseBlock(DBuffer buf) {
 		buf.held = false;
 		notifyAll();
 	}
 	
 	
 	
 	/*
 	 * sync() writes back all dirty blocks to the volume and wait for completion.
 	 * The sync() method should maintain clean block copies in DBufferCache.
 	 */
 	public void sync() throws IllegalArgumentException, IOException {
 		for (int i = 0; i < bufList.size(); i++) {
 			DBuffer current = bufList.get(i);
 			if (!current.isClean) {
 				current.startPush(myVD);//write the block contents to disk
 				current.waitClean();
 			}
 		}
 	}
 	
 
 }
