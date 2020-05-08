 package dfs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import virtualdisk.VirtualDisk;
 import virtualdisk.VirtualDiskSingleton;
 
 import common.Constants;
 import common.DFileID;
import dblockcache.DBuffer;
 import dblockcache.DBufferCache;
 import dblockcache.DBufferCacheSingleton;
 
 public class DFS {
 
 	private boolean _format;
 	private String _volName;
 	private VirtualDisk _vd;
 	private DBufferCache _dbc;
 	private INode[] _inodes;
 	private int _lastCreatedDFID;
 	private List<Integer> _freeBlocks;
 
 	protected DFS(String volName, boolean format) {
 		_volName = volName;
 		_format = format;
 		_inodes = new INode[Constants.MAX_NUM_FILES];
 		_lastCreatedDFID = 0;
 		_freeBlocks = new ArrayList<Integer>();
 		_vd = VirtualDiskSingleton.getInstance(volName, format);
 		_dbc = DBufferCacheSingleton.getInstance();
 		populateINodesFromDisk();
 	}
 
 	protected DFS(boolean format) {
 		this(Constants.vdiskName, format);
 	}
 
 	protected DFS() {
 		this(Constants.vdiskName, false);
 	}
 
 	/*
 	 * If format is true, the system should format the underlying disk contents,
 	 * i.e., initialize to empty. On success returns true, else return false.
 	 */
 	public boolean format() {
 		// TODO: Do stuff
 		return true;
 	}
 
 	/*
 	 * creates a new DFile and returns the DFileID, which is useful to uniquely
 	 * identify the DFile
 	 */
 	public synchronized DFileID createDFile() {
 		int fileID = findFirstAvailableDFID();
 		_lastCreatedDFID = fileID;
 		// If fileID is -1, then max file limit reached
 		if (fileID < 0) {
 			return null;
 		}
 		// Create file
 		DFileID dfid = new DFileID(fileID);
 		if (_inodes[fileID] == null)
 			_inodes[fileID] = new INode(dfid, true);
 		_inodes[fileID]._isFile = true;
 		return dfid;
 	}
 
 	private int findFirstAvailableDFID() {
 		// Linear search starting at the last created
 		for (int i = 0; i < Constants.MAX_NUM_FILES; i++) {
 			int idx = (i + _lastCreatedDFID) % _inodes.length;
 			INode curr = _inodes[idx];
 			if (curr == null || !curr._isFile)
 				return idx;
 		}
 		return -1;
 	}
 
 	/* destroys the file specified by the DFileID */
 	public void destroyDFile(DFileID dFID) {
 		// Simply mark as no longer existing
 		if (_inodes[dFID.getID()] != null) {
 			INode in = _inodes[dFID.getID()];
			while (in._blocks.size() > 0) {
				_freeBlocks.add(in._blocks.remove(in._blocks.size()-1));
			}
 			in._isFile = false;
 		}
 	}
 
 	/*
 	 * reads the file dfile named by DFileID into the buffer starting from the
 	 * buffer offset startOffset; at most count bytes are transferred
 	 */
 	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
 		INode in = _inodes[dFID.getID()];
 		int bytesRead = 0;
 		for (int block : in._blocks) {
			DBuffer db = _dbc.getBlock(block);
			int newBytes = db.read(buffer,
 					startOffset + bytesRead, count - bytesRead);
			_dbc.releaseBlock(db);
 			bytesRead += newBytes;
 		}
 		// TODO: if count > file size, stop reading at EOF
 		return bytesRead;
 	}
 
 	/*
 	 * writes to the file specified by DFileID from the buffer starting from the
 	 * buffer offset startOffsetl at most count bytes are transferred
 	 */
 	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
 		// TODO: if out of free blocks, throw error
 		if (count > Constants.MAX_NUM_BLOCKS_PER_FILE * Constants.BLOCK_SIZE) {
 			System.err.println("Too many bytes requested to be written.");
 		}
 		INode in = _inodes[dFID.getID()];
 		// Calculate number of blocks needed for write
 		int blocksNeeded = (int) Math.ceil(count / Constants.BLOCK_SIZE);
 		if (blocksNeeded > in._blocks.size()) {
 			while (blocksNeeded > in._blocks.size()) {
 				in._blocks.add(_freeBlocks.remove(0));
 			}
 		} else if (blocksNeeded < in._blocks.size()) {
 			while (blocksNeeded < in._blocks.size()) {
 				_freeBlocks.add(in._blocks.remove(in._blocks.size()-1));
 			}
 		}
 		int bytesWritten = 0;
 		for (int block : in._blocks) {
			DBuffer db = _dbc.getBlock(block);
			int newBytes = db.write(buffer,
 					startOffset + bytesWritten, count - bytesWritten);
			_dbc.releaseBlock(db);
 			bytesWritten += newBytes;
 		}
 		// TODO: write EOF
 		return bytesWritten;
 	}
 
 	/* returns the size in bytes of the file indicated by DFileID. */
 	public int sizeDFile(DFileID dFID) {
 		return _inodes[dFID.getID()].numBlocks() * Constants.BLOCK_SIZE;
 	}
 
 	/*
 	 * List all the existing DFileIDs in the volume
 	 */
 	public List<DFileID> listAllDFiles() {
 		List<DFileID> existing = new ArrayList<DFileID>();
 		for (INode in : _inodes) {
 			if (in != null && in._isFile)
 				existing.add(in._id);
 		}
 		return existing;
 	}
 
 	/*
 	 * Write back all dirty blocks to the volume, and wait for completion.
 	 */
 	public synchronized void sync() {
 		DBufferCacheSingleton.getInstance().sync();
 	}
 
 	/*
 	 * Reads INodes from disk and brings them into memory
 	 */
 	private void populateINodesFromDisk() {
 		// TODO: for now, just add all blocks to free list, but later on, we remove the ones that are used in the inodes already
 		for (int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
 			_freeBlocks.add(i);
 		}
 		// TODO: implement
 	}
 }
