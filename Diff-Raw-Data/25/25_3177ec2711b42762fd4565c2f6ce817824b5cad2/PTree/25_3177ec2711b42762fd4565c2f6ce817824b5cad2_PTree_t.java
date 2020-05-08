 /*
  * PTree -- persistent tree
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2007, 2010, 2012 Mike Dahlin
  *
  */
 
 import java.io.IOException;
 import java.io.EOFException;
 import java.util.concurrent.locks.Condition;
 
 public class PTree{
 	/*
 	 * allocTNodes: (1 sector)
 	 * 		byte array
 	 * 
 	 * Tree Pointers: (32 sectors)
 	 * 		Tnodes (each 32 bytes)
 	 * 
 	 * bit Map: (4 sectors)
 	 * 		4 sectors of bits, each one represents a sectors
 	 * 		(get sector num, x = num/4096 to get bit Map sector
 	 * 		x/8 to get byte number in that sector
 	 * 		x%8 to get the bit in that byte)   (compare to -1 to see if all the 
 	 * 		sectors are taken in that byte)
 	 * 
 	 * Data: (the rest)
 	 * 	
 	 */
 
 	public static final int METADATA_SIZE = 64;
 	public static final int MAX_TREES = 512;
 	public static final int MAX_BLOCK_ID = Integer.MAX_VALUE; 
 
 	//
 	// Arguments to getParam
 	//
 	public static final int ASK_FREE_SPACE = 997;
 	public static final int ASK_MAX_TREES = 13425;
 	public static final int ASK_FREE_TREES = 23421;
 
 	//
 	// TNode structure
 	//
 	public static final int TNODE_POINTERS = 8;
 	public static final int BLOCK_SIZE_BYTES = 1024;
 	public static final int POINTERS_PER_INTERNAL_NODE = 256;
 
 	public static final int TNODES_PER_SECT = 5;
 
 	public static final int ALLOC_LOCATION = 1025;
 	public static final int BITMAP_LOCATION = 1026;
 	public static int TNODE_LOCATION = BITMAP_LOCATION + Disk.NUM_OF_SECTORS/(Disk.SECTOR_SIZE * 8);
 	public static int DATA_LOCATION = TNODE_LOCATION + 103;
 
 
 	private ADisk disk;
 	private SimpleLock lock;
 	private Condition inUse;
 	private boolean beingUsed;
 	private byte[] allocTNodes;
 	private BitMap bitMap;
 
 	/**
 	 * This function is the constructor. If doFormat == false, 
 	 * data stored in previous sessions must remain stored. 
 	 * If doFormat == true, the system should initialize the underlying disk to empty.
 	 * @param doFormat
 	 */
 	public PTree(boolean doFormat)
 	{
 		disk = new ADisk(doFormat); 
 		lock = new SimpleLock();
 		inUse = lock.newCondition();
 		beingUsed = false;
 		allocTNodes = new byte[MAX_TREES];
 		TransID xid = disk.beginTransaction();
 
 		try {
 			disk.readSector(xid, PTree.ALLOC_LOCATION, allocTNodes);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IndexOutOfBoundsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		bitMap = new BitMap(Disk.NUM_OF_SECTORS);
 		byte[][] bits = new byte[bitMap.sec_div][Disk.SECTOR_SIZE];
 		for(int i = 0; i < bitMap.sec_div; i++) {
 			try {
 				disk.readSector(xid, PTree.BITMAP_LOCATION+i, bits[i]);
 			} catch (Exception e) {}
 		}
 		bitMap.passBits(bits);
 		for(int i = 0; i < DATA_LOCATION; i++) {
 			bitMap.set_sector(i);
 		}
 		
 		try {
 			disk.commitTransaction(xid);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public TransID beginTrans()
 	{
 		lock.lock();
 		if (beingUsed) {
 			try {
 				inUse.await();
 			} catch (InterruptedException e) {}
 		}
 
 		beingUsed = true;
 		TransID id = disk.beginTransaction();
 		lock.unlock();
 		return id;
 	}
 
 	public void commitTrans(TransID xid) 
 	throws IOException, IllegalArgumentException
 	{
 		lock.lock();
 
 		// update bitmap on disk
 		byte[][] bits = bitMap.get_bits();
 		for (int i = 0; i < bits.length; i++) 
 			disk.writeSector(xid, BITMAP_LOCATION+i, bits[i]);
 
 		// write allocTNodes
 		disk.writeSector(xid, ALLOC_LOCATION, allocTNodes);
 
 		// set to not in use
 		disk.commitTransaction(xid);
 		beingUsed = false;
 		// signal
 		inUse.signalAll();
 
 		// unlock
 		lock.unlock();
 	}
 
 	public void abortTrans(TransID xid) 
 	throws IOException, IllegalArgumentException
 	{
 		// lock
 		lock.lock();
 
 		disk.abortTransaction(xid);
 		TransID id = disk.beginTransaction();
 		disk.readSector(id, ALLOC_LOCATION, allocTNodes);
 
 		for(int i = 0; i < 4; i++) {
 			disk.readSector(id, BITMAP_LOCATION + i, bitMap.bits[i]);
 		}
 
 		disk.abortTransaction(id);
 
 		beingUsed = false;
 		// signal
 		inUse.signalAll();
 
 		// unlock
 		lock.unlock();
 	}
 
 
 	public int createTree(TransID xid) 
 	throws IOException, IllegalArgumentException, ResourceException
 	{
 		// find a place for a new Tnode
 		int tnum = -1;
 		for(int i = 0; i < allocTNodes.length && tnum == -1; i++ ) {
 			if(allocTNodes[i] == 0) {
 				tnum = i;
 				allocTNodes[i] = 1;
 			}
 		}
 
 		if(tnum == -1) {
 			throw new ResourceException();
 		}
 
 		// add data to Tnode
 		TNode tnode = new TNode(tnum);
 
 		// add write to transaction
 		byte[] buffer = new byte[Disk.SECTOR_SIZE];
 		int begin = (tnum/TNODES_PER_SECT) *TNODES_PER_SECT;
 		disk.readSector(xid, TNODE_LOCATION + begin, buffer);
 		tnode.write_to_buffer(buffer);
 
 		// disk.writeSector(xid, sectorNum, buffer)
 		disk.writeSector(xid, TNODE_LOCATION + begin, buffer);
 
 		// return tnum (the location of the tnode)
 		return tnum;
 	}
 
 	public void deleteTree(TransID xid, int tnum) 
 	throws IOException, IllegalArgumentException
 	{
 		TNode current_node = create_TNode(xid, tnum);
 		allocTNodes[tnum] = 0;
 
 		// free blocks and free blocks in bitmap
 		current_node.free_blocks(xid, bitMap, disk);
 
 		// write the freed allocation of the alloc array to disk with transaction
 		disk.writeSector(xid, PTree.ALLOC_LOCATION, allocTNodes);
 	}
 
 	public TNode create_TNode(TransID xid, int tnum) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
		int begin = (tnum/TNODES_PER_SECT);
 		byte[] buffer = new byte[Disk.SECTOR_SIZE];
 
 		// free in all block used by tree in bit map
 		disk.readSector(xid, TNODE_LOCATION + begin, buffer);
 		TNode current_node = TNode.build_from_buffer(buffer, tnum, disk);
 
 		return current_node;
 	}
 
 	/**
 	 * This function returns the maximum ID of any data block
 	 * stored in the specified tree. Note that blocks in a tree 
 	 * are numbered starting from 0. 
 	 * @param xid
 	 * @param tnum
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 */
 	public int getMaxDataBlockId(TransID xid, int tnum)
 	throws IOException, IllegalArgumentException
 	{
 		TNode current = create_TNode(xid, tnum);
 		return current.total_blocks;
 	}
 
 	public void readData(TransID xid, int tnum, int blockId, byte buffer[])
 	throws IOException, IllegalArgumentException, EOFException
 	{		
 		// find the sectors in the Tnode
 		TNode t = create_TNode(xid, tnum);
 		if(blockId > t.total_blocks) {
 			for(int i = 0; i < buffer.length; i++) {
 				buffer[i] = 0;
 			}
 			return;
 		}
 
 		int s = t.get_sector(xid, blockId, disk);
 
 		// read from ADisk to two buffers
 		byte[] b1 = new byte[Disk.SECTOR_SIZE];
 		byte[] b2 = new byte[Disk.SECTOR_SIZE];
 		disk.readSector(xid, s, b1);
 		disk.readSector(xid, s+1, b2);
 		byte[] b3 = TNode.combine_buffer(b1, b2);
 
 		for(int i = 0; i < buffer.length && i < b3.length; i++ ) {
 			buffer[i] = b3[i];
 		}
 	}
 
 
 	public void writeData(TransID xid, int tnum, int blockId, byte buffer[])
 	throws IOException, IllegalArgumentException
 	{
 		// see if tree contains the blockId'th block.
 		TNode t = create_TNode(xid, tnum);
 		t.writeBlock(xid, blockId, buffer, disk, bitMap);
 
 		// add write to transaction
 		byte[] buffer2 = new byte[Disk.SECTOR_SIZE];
 		int begin = (tnum/TNODES_PER_SECT) *TNODES_PER_SECT;
 		disk.readSector(xid, TNODE_LOCATION + begin, buffer2);
 		t.write_to_buffer(buffer2);
 
 		// disk.writeSector(xid, sectorNum, buffer)
 		disk.writeSector(xid, TNODE_LOCATION + begin, buffer2);
 
 	}
 
 	public void readTreeMetadata(TransID xid, int tnum, byte buffer[])
 	throws IOException, IllegalArgumentException
 	{
 		// find the tnode from the tnum.
 		TNode t = create_TNode(xid, tnum);
 		for(int i = 0; i < buffer.length; i++){
 			buffer[i] = t.metadata[i];
 		}
 	}
 
 
 	public void writeTreeMetadata(TransID xid, int tnum, byte buffer[])
 	throws IOException, IllegalArgumentException
 	{
 		// find the tnode from the tnum.
 		TNode t = create_TNode(xid, tnum);
 		for(int i = 0; i < buffer.length; i++){
 			t.metadata[i] = buffer[i];
 		}
 
 		// and write the sector with adisk
 		byte[] b1 = new byte[Disk.SECTOR_SIZE];
 		int begin = (tnum/TNODES_PER_SECT) *TNODES_PER_SECT;
 		disk.readSector(xid, TNODE_LOCATION + begin, b1);
 		t.write_to_buffer(b1);
 
 		// disk.writeSector(xid, sectorNum, buffer)
 		disk.writeSector(xid, TNODE_LOCATION + begin, b1);
 	}
 
 	public int getParam(int param)
 	throws IOException, IllegalArgumentException
 	{
 		if(param == PTree.ASK_FREE_SPACE) {
 			return bitMap.free_sectors;
 		} else if (param == PTree.ASK_MAX_TREES) {
 			return MAX_TREES;
 		} else if (param == PTree.ASK_FREE_TREES) {
 			int total = 0; 
 			for(int i = 0; i < allocTNodes.length; i++) {
 				if(allocTNodes[i] == 0) total++;
 			}
 			return total;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 
 
 
 
 
 
 
 
 	public static void unit(Tester t) throws IllegalArgumentException, IOException {
 		t.set_object("PTree");
 
 
 
 		// construtor
 		t.set_method("Constructor()");
 		PTree pt1 = new PTree(false);
 		t.is_equal(512, pt1.allocTNodes.length);
 		t.is_true(pt1.disk != null);
 		t.is_true(pt1.lock != null);
 		t.is_true(pt1.inUse != null);
 		t.is_true(pt1.bitMap != null);
 
 
 
 
 
 
 
 		// getParam
 		t.set_method("getParam()");
 		t.is_equal(Disk.NUM_OF_SECTORS - PTree.DATA_LOCATION, pt1.getParam(ASK_FREE_SPACE));
 		t.is_equal(PTree.MAX_TREES, pt1.getParam(PTree.ASK_MAX_TREES));
 		t.is_equal(PTree.MAX_TREES, pt1.getParam(PTree.ASK_FREE_TREES));
 
 
 
 
 
 
 		// beginTrans
 		t.set_method("beginTransaction()");
 		TransID xid1 = pt1.beginTrans();
 		t.is_true(xid1 != null);
 		t.is_true(pt1.beingUsed);
 
 
 
 		// commitTrans
 		// abortTrans
 
 
 		// createTree
 		t.set_method("createTree()");
 		int tnum1 = pt1.createTree(xid1);
 		t.is_equal(0, tnum1);
 		t.is_equal(1, pt1.allocTNodes[0]);
 
 		//t.is_equal(PTree.DATA_LOCATION, pt1.allocTNodes[0]);  // This test fails but I'm not sure what you were trying to test.
 
 		for(int i = 1; i < PTree.MAX_TREES; i++) {  //use up all the nodes
 			tnum1 = pt1.createTree(xid1);
 			t.is_equal(i, tnum1);
 			t.is_equal(1, pt1.allocTNodes[i]);
 		}
 
 		try {
 			tnum1 = pt1.createTree(xid1);  //no more room so should get an error
 		} catch (Exception e) {
 			t.is_true(e instanceof ResourceException);
 		}
 
 
 		// deleteTree
 		t.set_method("deleteTree()");
 		byte data[] = new byte[1024];
 		for(int i = 0; i < 1024; i++) {
 			data[i] = (byte)(i % 128);
 		}
 
 
 		int size = pt1.bitMap.free_sectors;
		
		pt1.writeData(xid1, 0, 0, data);
		
 		for(int i = 511; i >= 0; i--) {
 			/*pt1.writeData(xid1, i, 1, data);
 			pt1.writeData(xid1, i, 2, data);
 			pt1.writeData(xid1, i, 3, data);*/
 			pt1.deleteTree(xid1, i);
 			t.is_equal(0, pt1.allocTNodes[i]);
 		}
 
 
 		// create-TNode
 		// readTreeMetadata
 		// writeTreeMatadata
 		// getMaxDataBlockID
 		t.set_method("getMaxDataBlockId");
 		// readData
 		// writeData
 		pt1.commitTrans(xid1);
 	}
 }
