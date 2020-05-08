 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * The {@code MemManager} class controls how information is stored in memory.
  * They make use of a {@link BufferPool} that stores the actual bytes of
  * information and a {@link FreeBlockList} that keeps track of free space.
  * <p/>
  * In the step-by-step process of handling memory requests: <ul><li>The
  * {@link Controller} queries its {@link RecordArray} to see if it can store a
  * new record.</li> <li>If space is available, the {@code MemManager} will
  * request enough free space from its {@link FreeBlockList}.</li><li>If there is
  * enough free space, it is removed from the {@link FreeBlockList} and a new
  * block of memory is passed to the {@link BufferPool}.</li><li>The memory pool
  * uses the newly allocated space to store the request and the
  * {@code MemManager} passes up a {@link MemHandle} that can be used by
  * higher-level classes to retrieve information stored in the
  * {@link BufferPool}.</li> </ul>
  * <p/>
  * @author orionf22
  * @author rinaldi1
  */
 public class MemManager
 {
 
 	/**
 	 * The {@link BufferPool} managed by this {@code MemManager}. Actual data is
 	 * stored here.
 	 */
 	//private BufferPool pool;
 	private BufferPool bufferPool;
 	/**
 	 * The size of memory blocks ({@link MemBlock} objects) managed by the
 	 * {@link BufferPool} as a {@link Buffer}. This is also the amount by which
 	 * the manager will attempt to expand the pool when space runs out.
 	 */
 	private int BLOCK_SIZE;
 	/**
 	 * The current maximum size, in bytes, of this manager. This value is
 	 * updated if additional space is required and allocated to the manager.
 	 */
 	private int size;
 	/**
 	 * The {@link FreeBlockList} managed by this {@code MemManager}. Free space
 	 * is monitored here.
 	 */
 	private FreeBlockList freeBlocks;
 
 	/**
 	 * Constructs a new {@code MemManager} of the given size, {@code poolSize}.
 	 * <p/>
 	 * @param poolSize the size in bytes of this {@code MemManager}
 	 */
 	public MemManager(int blockSize, int buffers, File file)
 	{
 		size = blockSize;
 		BLOCK_SIZE = blockSize;
 		this.bufferPool = new BufferPool(buffers, file, blockSize);
 		this.freeBlocks = new FreeBlockList(0);
 	}
 
 	/**
 	 * Flushes the {@link BufferPool} and closes its source stream. This ensures
 	 * any in-memory data is written to disk.
 	 * <p/>
 	 * @throws IOException
 	 */
 	public void close() throws IOException
 	{
 		//flush the pool prior to writing stats
 		bufferPool.flush();
 		//finally, close the file stream
 		bufferPool.closeSourceStream();
 	}
 
 	/**
 	 * Inserts a byte array into {@code pool}. If this function has been
 	 * reached, the {@link Controller} has already determined that its
 	 * {@link RecordArray} can store this request. The {@link FreeBlockList} is
 	 * then queried for a free block large enough to store the request. If
 	 * enough space exists, a {@link MemHandle} is passed up marking the address
 	 * of this newly allocated memory. The {@link BufferPool} then uses this
 	 * address to begin inserting the two-byte size sequence and actual data.
 	 * <p/>
 	 * In the event that there is insufficient space within {@code pool}, 100
 	 * bytes are successively added until enough free space exists to honor the
 	 * request. Everything in the original pool is copied into the new pool and
 	 * a new {@link MemBlock} of the recently added space is added to
 	 * {@code freeList}. Merging is automatically handled.
 	 * <p/>
 	 * @param stuff the data to insert
 	 * <p/>
 	 * @return a {@link MemHandle} addressing the start of the two-byte size
 	 *            sequence belonging to the data request
 	 */
 	public MemHandle insert(byte[] stuff)
 	{
 		//Important! Request an additional 2 bytes for the 2-byte size sequence
 		MemHandle insertHandle = freeBlocks.getSpace(stuff.length + 2);
 		//System.out.println(freeBlocks.blocksToString());
 		if (insertHandle.getAddress() >= 0)
 		{
 			bufferPool.set(sizeToBytes((short) stuff.length), insertHandle.getAddress());
 			bufferPool.set(stuff, insertHandle.getAddress() + 2);
 		}
 		//Insufficient free space; continue to add BLOCK_SIZE bytes until enough
 		//space exists
 		else
 		{
			System.out.println("\t\t\t\texpanding pool");
 			int increaseSize = BLOCK_SIZE;
 			int oldSize = size;
 			size += increaseSize;
 			//Add the additional space to the FreeBlockList; important to NOT 
 			//add 1 to the space request as the size (oldSize) is NOT zero-based 
 			//but the freelist and pool ARE zero-based, thus making the +1 
 			//unnecessary and incorrect
 			freeBlocks.reclaimSpace(new MemHandle(oldSize), increaseSize);
 			//Size increased; recursively call insert again till sufficient space
 			return insert(stuff);
 		}
 		return insertHandle;
 	}
 
 	/**
 	 * Removes an existing sequence of bytes from the {@code BufferPool}.
 	 * Instead of visiting each byte and clearing it, the two-byte size sequence
 	 * is set to zero. This means any bytes previously owned by the data are now
 	 * virtually inaccessible; they can be overwritten as needed.
 	 * <p/>
 	 * @param h the {@link MemHandle} addressing the two-byte size sequence of
 	 *             the data to remove
 	 */
 	public int remove(MemHandle h)
 	{
 		int ret = bytesToSize(bufferPool.get(h.getAddress(), 2));
 		//removing from the pool only returns the number of bytes needed to
 		//store the actual record, not including the size sequence prefix, so
 		//add 2 to the reclaim space request
 		freeBlocks.reclaimSpace(h, ret + 2);
 		return ret;
 	}
 
 	/**
 	 * Retrieves a byte sequence from the {@link BufferPool} addressed by
 	 * {@code h}.
 	 * <p/>
 	 * @param h the {@link MemHandle} addressing the two-byte size sequence of
 	 *             the data to retrieve
 	 * <p/>
 	 * @return the byte array of retrieved information
 	 */
 	public byte[] get(MemHandle h)
 	{
 		byte[] got = bufferPool.get(h.getAddress(), 2);
 		int request = bytesToSize(got);
 		return bufferPool.get(h.getAddress() + 2, request);
 	}
 
 	/**
 	 * Converts a {@code short} value, {@code s}, to a two-byte size sequence.
 	 * This is used as a prefix to all stored data byte arrays to denote the
 	 * actual byte length of stored data. All {@link MemHandle} references
 	 * address this two-byte sequence and higher functions use the size to
 	 * determine how many bytes belong to the pertinent data request.
 	 * <p/>
 	 * @param s the size of the stored data
 	 * <p/>
 	 * @return a two-byte sequence of the converted size
 	 */
 	public static byte[] sizeToBytes(short s)
 	{
 		byte[] ret = new byte[2];
 		ret[0] = (byte) (s >> 8);
 		ret[1] = (byte) (s);
 		return ret;
 	}
 
 	/**
 	 * Converts a two-byte array marking the size of stored data to a primitive
 	 * {@code int}. {@code s} should be, at minimum, of size 2, and preferably
 	 * no larger.
 	 * <p/>
 	 * @param s the two-byte size sequence
 	 * <p/>
 	 * @return the {@code int} size
 	 */
 	public static int bytesToSize(byte[] s)
 	{
 		return (s[0] << 8) + s[1];
 	}
 
 	/**
 	 * Returns a String representation of {@code freeBlocks}' free blocks.
 	 * <p/>
 	 * @see FreeBlockList#blocksToString()
 	 * @return the String representation of free space
 	 */
 	public String getFreeBlocks()
 	{
 		return freeBlocks.blocksToString();
 	}
 
 	/**
 	 * Returns a String representation (Buffer ID) of all currently in-memory
 	 * {@link Buffer} objects in {@code bufferPool}.
 	 * <p/>
 	 * @see BufferPool#getBlockIDs()
 	 * @return the String representation of Buffer IDs
 	 */
 	public String getBlockIDs()
 	{
 		return bufferPool.getBlockIDs();
 	}
 }
