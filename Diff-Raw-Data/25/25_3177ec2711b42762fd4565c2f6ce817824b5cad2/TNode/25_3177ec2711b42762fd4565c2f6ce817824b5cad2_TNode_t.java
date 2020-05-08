 import java.io.IOException;
 
 public class TNode {
 	int[] ptrs; //sector numbers 32 bytes
 	int TNum;
 	byte height; // 1 byte
 	int total_blocks; //4 bytes
 	byte[] metadata; // 64 bytes
 	byte inUse;
 	InternalNode[] intNodes;
 	// Total			101
 
 	public TNode(int num) {
 		TNum = num;
 		ptrs = new int[PTree.TNODE_POINTERS];
 		height = 1;
 		total_blocks = 0;
 		metadata = new byte[PTree.METADATA_SIZE];
 		intNodes = new InternalNode[8];
 	}
 
 	public int addBlock(TransID xid, int sector, BitMap bitmap, ADisk disk) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		if(total_blocks < 8) {
 			for(int i = 0; i < PTree.TNODE_POINTERS; i++) {
 				if(ptrs[i] == 0) {
 					ptrs[i] = sector;
 					break;
 				}
 			}
 		} else {
 
 			if(total_blocks == 8 || (total_blocks/8)%256 == 0) {
 				expandHeight(bitmap);
 			}
 			double divisor = Math.pow(256, height-1);
 			int the_node = (int) (total_blocks / divisor);
 			if(intNodes[the_node] == null) {
 				intNodes[the_node] = InternalNode.build_from_buffer(getInternalBuffer(xid, ptrs[the_node], disk), ptrs[the_node], height-1); 
 			}
 			
 			intNodes[the_node].addBlock(xid, sector, bitmap, (int)(total_blocks - (the_node * divisor)), height-1, disk);
 			
 			byte[] b1024 = new byte[PTree.BLOCK_SIZE_BYTES];
 			intNodes[the_node].write_to_buffer(b1024, height-1);
 			byte[][] bOut = split_buffer(b1024, 2);
 			disk.writeSector(xid, ptrs[the_node], bOut[0]);
 			disk.writeSector(xid, ptrs[the_node]+1, bOut[1]);
 		}
 
 		total_blocks++;
 
 		return total_blocks-1;
 	}
 
 	private void expandHeight(BitMap bm) {
 		int sect = bm.first_free_block();
 		InternalNode newnode = new InternalNode(sect, height, ptrs);
 		for(int i = 0; i < 8; i++) {
 			intNodes[i] = null;
 			ptrs[i] = 0;
 		}
 		intNodes[0] = newnode;
 		ptrs[0] = sect;
 		height++;
 	}
 
 	public void writeBlock(TransID xid, int blockID, byte[] buffer, ADisk disk, BitMap bitmap) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		int sect = -1;
 		// increase size if needed
 		while(blockID >= total_blocks) {
 			sect = bitmap.first_free_block();
 			addBlock(xid, sect, bitmap, disk);
 		}
 		if(total_blocks < 8) {
 			int sector_num = ptrs[blockID];
 			byte[][] sec_buffers = split_buffer(buffer, 2);
 			disk.writeSector(xid, sector_num, sec_buffers[0]);
 			disk.writeSector(xid, sector_num+1, sec_buffers[1]);
 		} else {
 			//figure out which inode the blockID is in
 			double divisor = Math.pow(256, height-1);
 			int the_node = (int) (blockID / divisor);
 
 			if(intNodes[the_node] == null) {
 				intNodes[the_node] = InternalNode.build_from_buffer(getInternalBuffer(xid, ptrs[the_node], disk), ptrs[the_node], height-1); 
 			}
 
 			intNodes[the_node].writeBlock(xid, (int)(blockID - (the_node * divisor)), buffer, disk, height-1);
 		}
 	}
 
 	public int get_sector(TransID xid, int blockID, ADisk disk) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		double divisor = Math.pow(256, height-1);
 		int the_node = (int) (blockID / divisor);
 
 		// build node if need
 		if(intNodes[the_node] == null) {
 			intNodes[the_node] = InternalNode.build_from_buffer(getInternalBuffer(xid, ptrs[the_node], disk), ptrs[the_node], height-1);
 		}
 
 		return intNodes[the_node].get_sector(xid, (int)(blockID - (the_node * divisor)), disk, height-1);
 	}
 
 	public static byte[] getInternalBuffer(TransID xid, int sector, ADisk disk) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		byte[] buffer1 = new byte[Disk.SECTOR_SIZE];
 		byte[] buffer2 = new byte[Disk.SECTOR_SIZE];
 		disk.readSector(xid, sector, buffer1);
 		disk.readSector(xid, sector+1,buffer2);
 		byte[] buffer = TNode.combine_buffer(buffer1, buffer2);
 		return buffer;
 	}
 
 	public static byte[][] split_buffer(byte[] buffer, int num) {
 		byte[][] ret = new byte[num][512];
 
 		for(int i = 0; i < num; i++) {
 			for(int j = 0; j < 512; j++) {
 				ret[i][j] = buffer[(i*512)+j];
 			}
 		}
 
 		return ret;
 	}
 
 	public static byte[] combine_buffer(byte[] b1, byte[] b2){
 		byte[] ret = new byte[b1.length + b2.length];
 		int ind = 0;
 		for(int i = 0; i < b1.length; i++, ind++) {
 			ret[ind] = b1[i];
 		}
 		for(int i = 0; i < b2.length; i++, ind++) {
 			ret[ind] = b2[i];
 		}
 		return ret;
 	}
 
 	public void write_to_buffer(byte[] buffer) {
 		int spot = TNum % 5;
 		// write ptrs
 		for(int i = 0; i < ptrs.length; i++){
 			Common.intToByte(ptrs[i], buffer, 101*spot+(i*4));
 		}
 		// write height
 		buffer[101*spot+32] = height;
 		//write total_blocks
 		Common.intToByte(total_blocks, buffer, 101*spot + 33);
 		// write meta
 		for(int i = 0; i < metadata.length; i++) {
 			buffer[101*spot+37+i] = metadata[i];
 		}
 	}
 
 	public static TNode build_from_buffer(byte[] buffer, int tnum, ADisk disk) {
 		int spot = tnum % 5;
 		TNode ret = new TNode(tnum);
 		for(int i = 0; i < 8; i++) {
 			ret.ptrs[i] = Common.byteToInt(buffer,spot*101+i*4);
 		}
 		ret.height = buffer[101*spot+32];
 		ret.total_blocks = Common.byteToInt(buffer, spot*101+33);
 		for(int i = 0; i < PTree.METADATA_SIZE; i++) {
 			ret.metadata[i] = buffer[101*spot+37+i];
 		}
 
 		// TODO traverse tree if height > 1
 
 		return ret;
 	}
 
 	public void free_blocks(TransID xid, BitMap bm, ADisk disk) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		if(total_blocks > 8) {
 			// free internal
 			for(int i = 0; i < intNodes.length; i++) {
 				if(ptrs[i] != 0) {
 					if(intNodes[i] == null) {
 						byte[] buffer = getInternalBuffer(xid, ptrs[i], disk);
 						intNodes[i] = InternalNode.build_from_buffer(buffer, ptrs[i], height-1);
 					}
 					intNodes[i].free_blocks(xid, bm, height-1, disk);
 					intNodes[i] = null;
 				}
 			}
 		}
 
 		// free these pointers 
 		for(int i = 0; i < ptrs.length; i++) {
 			if(ptrs[i] != 0) {
 				bm.free_sector(ptrs[i]);
 				bm.free_sector(ptrs[i]+1);
 			}
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public static void unit(Tester t) throws IllegalArgumentException, IndexOutOfBoundsException, IOException {
 		t.set_object("TNode");
 		
		ADisk disk = new ADisk(false);
 		TransID xid = disk.beginTransaction();
 		
 		
 		
 		t.set_method("Constructor");
 		TNode tn1 = new TNode(1);
 		t.is_equal(1, tn1.TNum);
 		t.is_equal(8, tn1.ptrs.length);
 		t.is_equal(1, tn1.height);
 		t.is_equal(0, tn1.total_blocks);
 		t.is_equal(64, tn1.metadata.length);
 		t.is_equal(8, tn1.intNodes.length);
 		for(int i = 0; i < tn1.intNodes.length; i++) {
 			t.is_equal(null, tn1.intNodes[i]);
 		}
 		
 		
 		
 		
 		
 		
 		t.set_method("splitBuffer()");
 		byte data[] = new byte[1024];
 		for(int i = 0; i < 1024; i++) {
 			data[i] = (byte)(i % 128);
 		}
 
 		byte[][] comp = TNode.split_buffer(data,2);
 		for(int x = 0; x < 2; x++) {
 			for(int y = 0; y < 512; y++) {
 				t.is_equal(comp[x][y], (x*512+y)%128);
 			}
 		}
 		
 		
 		
 		
 		
 		
 		
 		// combine buffer
 		byte data2[] = TNode.combine_buffer(comp[0], comp[1]);
 		t.is_equal(data, data2);
 		
 		
 		
 		
 		
 		
 		t.set_method("addBlock() - Data Blocks");
 		BitMap bm = new BitMap(16384, PTree.DATA_LOCATION);
 		
 		int sect = bm.first_free_sector();
 		tn1.addBlock(xid, sect, bm, disk);
 		t.is_equal(1, tn1.total_blocks);
 		t.is_equal(sect, tn1.ptrs[0]);
 
 		for(int i = 1; i < 8; i++) {
 			sect = bm.first_free_sector();
 			tn1.addBlock(xid, sect, bm, disk);
 			t.is_equal(i+1, tn1.total_blocks);
 			t.is_equal(sect, tn1.ptrs[i]);
 		}
 
 		
 		
 		
 		
 		
 		
 		
 		// expand height()
 		TNode tn3 = new TNode(3);
 		tn3.expandHeight(bm);
 		t.is_equal(2, tn3.height);
 		t.is_equal(3, tn3.TNum);
 		t.is_equal(0, tn3.total_blocks);
 		t.is_true(tn3.intNodes[0] != null);
 		int sectI = tn3.ptrs[0];
 		
 		tn3.expandHeight(bm);
 		t.is_equal(3, tn3.height);
 		t.is_equal(0, tn3.total_blocks);
 		t.is_true(tn3.intNodes[0] != null);
 		t.is_equal(sectI, tn3.intNodes[0].ptrs[0]);
 		
 		
 		
 		
 		// addBlock() - To Internal Nodes
 		t.set_method("expand_height()");
 		sect = bm.first_free_sector();
 		tn1.addBlock(xid, sect, bm, disk);
 		t.is_equal(9, tn1.total_blocks);
 		t.is_equal(sect, tn1.intNodes[0].ptrs[8]);
 		t.is_equal(2, tn1.height);
 		
 		sect = bm.first_free_sector();
 		tn1.addBlock(xid, sect, bm, disk);
 		t.is_equal(10, tn1.total_blocks);
 		t.is_equal(sect, tn1.intNodes[0].ptrs[9]);
 		t.is_equal(2, tn1.height);
 		
 		
 		
 		
 		
 		
 		// writeBlock()
 		t.set_method("writeBlock");
 		tn1.addBlock(xid, 10000, bm, disk);
 		t.is_equal(10000, tn1.intNodes[0].ptrs[10]);
 		bm.set_sector(10000);
 		bm.set_sector(10001);
 		byte[] bufferOut = new byte[PTree.BLOCK_SIZE_BYTES];
 		for(int i = 0; i < bufferOut.length; i++) {
 			bufferOut[i] = (byte) (i%128);
 		}
 		tn1.writeBlock(xid, 10, bufferOut, disk, bm);
 		disk.commitTransaction(xid);
 		byte[] bufferWritten = new byte[Disk.SECTOR_SIZE];
 		TransID xid2 = disk.beginTransaction();
 		disk.readSector(xid2, 10000, bufferWritten);
 		int x = 0;
 		for(x = 0; x < bufferWritten.length; x++) {
 			t.is_equal(bufferOut[x], bufferWritten[x]);
 		}
 		
 		
 		
 		
 		
 		
 		// getInternalBuffer()
 		bufferWritten = TNode.getInternalBuffer(xid, 10000, disk);
 		t.is_equal(bufferOut, bufferWritten);
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		t.set_method("build_from_buffer");
 		TNode tn2 = new TNode(0);
 		sect = bm.first_free_sector();
 		tn2.addBlock(xid, sect, bm, disk);
 		int sect2 = bm.first_free_sector();
 		tn2.addBlock(xid, sect2, bm, disk);
 		byte[] buff = new byte[512];
 		tn2.write_to_buffer(buff);
 		TNode test = TNode.build_from_buffer(buff, 0, null);
 		t.is_equal(test.ptrs[0], sect);
 		t.is_equal(test.ptrs[1], sect2);
 		t.is_equal(test.height, 1);
 		t.is_equal(test.total_blocks,2);
 		
 		
 		
 		// free blocks
 	}
 }
