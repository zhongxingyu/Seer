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
 		try {
 			populateINodesFromDisk();
 		} catch (DFSCorruptionException e) {
 			e.printStackTrace();
 		}
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
 		_vd.formatStore();
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
 		_inodes[fileID].isFile(true);
 		return dfid;
 	}
 
 	private int findFirstAvailableDFID() {
 		// Linear search starting at the last created
 		for (int i = 0; i < Constants.MAX_NUM_FILES; i++) {
 			int idx = (i + _lastCreatedDFID) % _inodes.length;
 			INode curr = _inodes[idx];
 			if (curr == null || !curr.isFile()) return idx;
 		}
 		return -1;
 	}
 
 	/* destroys the file specified by the DFileID */
 	public void destroyDFile(DFileID dFID) {
 		// Simply mark as no longer existing
 		if (_inodes[dFID.getID()] != null) {
 			INode in = _inodes[dFID.getID()];
 			while (in.numBlocks() > 0) {
 				_freeBlocks.add(in.blocks().remove(in.blocks().size()-1));
 			}
 			in.isFile(false);
 		}
 	}
 
 	/*
 	 * reads the file dfile named by DFileID into the buffer starting from the
 	 * buffer offset startOffset; at most count bytes are transferred
 	 */
 	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
 		INode in = _inodes[dFID.getID()];
 		in.acquireReadLock();
 		if (count > in.size()) count = in.size();
 		int bytesRead = readBlocks(in.blocks(), buffer, startOffset, count, true);
 		in.releaseReadLock();
 		return bytesRead;
 	}
 	
 	private int readBlocks(List<Integer> blocks, byte[] buffer, int startOffset, int count, boolean isFile) {
 		int bytesRead = 0;
 		for (int block : blocks) {
 			DBuffer db = _dbc.getBlock(block);
 			db.waitValid();
 			int newBytes = db.read(buffer,
 					startOffset + bytesRead, count - bytesRead);
 			_dbc.releaseBlock(db);
 			bytesRead += newBytes;
 		}
 		return bytesRead;
 	}
 
 	/*
 	 * writes to the file specified by DFileID from the buffer starting from the
 	 * buffer offset startOffsetl at most count bytes are transferred
 	 */
 	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
 		if (count > Constants.MAX_NUM_BLOCKS_PER_FILE * Constants.BLOCK_SIZE) {
 			System.err.println("Too many bytes requested to be written.");
 		}
 		INode in = _inodes[dFID.getID()];
 		in.acquireWriteLock();
 		// Check if dirty; if it is then write out the inode
 		int blocksNeeded = (int) Math.ceil((float) count / Constants.BLOCK_SIZE);
 		if (blocksNeeded - in.numBlocks() > _freeBlocks.size()) {
 			System.err.println("Not enough free blocks to write file");
 		}
 		int bytesWritten = writeBlocks(in.blocks(), buffer, startOffset, count, true);
 		boolean inodeIsDirty = in.size() != bytesWritten;
 		// Save bytes written to inode
 		in.size(bytesWritten);
 		// Write inode if dirty
 		if (inodeIsDirty) {
 			writeBlocks(INode.inodeBlocks(dFID), in.serialize(), 0, INode.inodeSize(), false);
 		}
 		in.releaseWriteLock();
 		return bytesWritten;
 	}
 	
 	private int writeBlocks(List<Integer> blocks, byte[] buffer, int startOffset, int count, boolean isFile) {
 		// Calculate number of blocks needed for write
 		int blocksNeeded = (int) Math.ceil((float) count / Constants.BLOCK_SIZE);
 		if (isFile) {
 			if (blocksNeeded > blocks.size()) {
 				while (blocksNeeded > blocks.size()) {
 					blocks.add(_freeBlocks.remove(0));
 				}
 			} else if (blocksNeeded < blocks.size()) {
 				while (blocksNeeded < blocks.size()) {
 					_freeBlocks.add(blocks.remove(blocks.size()-1));
 				}
 			}
 		}
 		int bytesWritten = 0;
 		for (int block : blocks) {
 			DBuffer db = _dbc.getBlock(block);
 			db.waitValid();
 			int newBytes = db.write(buffer,
 					startOffset + bytesWritten, count - bytesWritten);
 			db.waitValid();
 			_dbc.releaseBlock(db);
 			bytesWritten += newBytes;
 		}
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
 			if (in != null && in.isFile())
 				existing.add(in.id());
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
 	private void populateINodesFromDisk() throws DFSCorruptionException {
 		for (int i = Constants.FILE_REGION_OFFSET; i < Constants.NUM_OF_BLOCKS; i++) {
 			_freeBlocks.add(i);
 		}
 		// Populate inodes
 		byte[] buffer = new byte[INode.inodeSize()];
 		for (int i = 0; i < Constants.MAX_NUM_FILES; i++) {
 			readBlocks(INode.inodeBlocks(i), buffer, 0, INode.inodeSize(), false);
 			_inodes[i] = new INode(buffer);
 			int inId = _inodes[i].id().getID();
 			if (inId != i && inId != 0) {
 				throw new DFSCorruptionException(String.format(
 					"INode ID read from disk does not match location (memid: %d, diskid: %d)",
 					i,
 					_inodes[i].id().getID()
 				));
 			}
 			if (inId != 0 && i > 0) _inodes[i].id(i);
 			// Remove all the blocks allocated to this inode
 			if (_inodes[i].isFile()) {
 				for (int block : _inodes[i].blocks()) {
 					if (!_freeBlocks.remove(new Integer(block))) {
 						throw new DFSCorruptionException("Same block allocated to multiple inodes");
 					}
 				}
 			}
 		}
 	}
 	
 	private class DFSCorruptionException extends Exception {
 		private static final long serialVersionUID = -7533228807562873783L;
 
 		public DFSCorruptionException(String message) {
 			super(message);
 		}
 	}
 }
