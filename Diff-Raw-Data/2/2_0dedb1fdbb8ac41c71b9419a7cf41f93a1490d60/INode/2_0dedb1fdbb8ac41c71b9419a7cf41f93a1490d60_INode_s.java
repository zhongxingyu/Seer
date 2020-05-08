 package dfs;
 
 import common.Constants;
 import common.DFileID;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class INode {
 	DFileID _id;
 	boolean _isFile;
 	List<Integer> _blocks;
 	
 	public INode(DFileID id, boolean create) {
 		_id = id;
 		_isFile = create;
 		_blocks = new BoundedLinkedList<Integer>(Constants.MAX_NUM_BLOCKS_PER_FILE);
 	}
 	
 	public INode(byte[] data) {
 		this(toDFID(data), data[4] == 0x1);
 		for (int i = 5; i < data.length; i++) {
 			int blockid = data[i];
 			if (blockid > 0) {
 				_blocks.add(blockid);
 			}
 		}
 	}
 	
 	public DFileID id() {
 		return _id;
 	}
 	
 	public void id(int i) {
 		_id = new DFileID(i);
 	}
 	
 	public boolean isFile() {
 		return _isFile;
 	}
 	
 	public void isFile(boolean setter) {
 		_isFile = setter;
 	}
 	
 	public List<Integer> blocks() {
 		return _blocks;
 	}
 	
 	public static List<Integer> inodeBlocks(int i) {
 		int iNodeStartBlock = Constants.INODE_REGION_OFFSET + i * INode.inodeSize();
 		List<Integer> blockList = new ArrayList<Integer>();
 		for (int j = 0; j < Constants.INODE_SIZE_IN_BLOCKS; j++) {
 			blockList.add(iNodeStartBlock + Constants.INODE_SIZE_IN_BLOCKS*i + j);
 		}
 		return blockList;
 	}
 	
 	public static List<Integer> inodeBlocks(DFileID dfid) {
 		return INode.inodeBlocks(dfid.getID());
 	}
 	
 	public byte[] serialize() {
 		byte[] serialized = new byte[inodeSize()];
 		// Write dfileid
 		System.arraycopy(toBytes(_id.getID()), 0, serialized, 4*0, 4);
 		// Write isfile bit
		serialized[5] = (_isFile) ? (byte) 1 : 0;
 		// Write blocks
 		int i = 0;
 		for (Integer block : _blocks) {
 			System.arraycopy(toBytes(block), 0, serialized, 5 + 4*i++, 4);
 		}
 		return serialized;
 	}
 	
 	public static int inodeSize() {
 		return 4 + 1 + (4 * Constants.MAX_NUM_BLOCKS_PER_FILE);
 	}
 	
 	public int numBlocks() {
 		return _blocks.size();
 	}
 	
 	private byte[] toBytes(int i) {
 		byte[] byteRep= new byte[4];
 		byteRep[0] = (byte) (i >> 24);
 		byteRep[1] = (byte) (i >> 16);
 		byteRep[2] = (byte) (i >> 8);
 		byteRep[3] = (byte) (i);
 		return byteRep;
 	}
 	
 	public static DFileID toDFID(byte[] bs) {
 		int id = 0;
 		for (int i = 0; i < 4; i++) {
 			id += ((int)bs[i] << (8 * (3 - i)));
 		}
 		return new DFileID(id);
 	}
 }
