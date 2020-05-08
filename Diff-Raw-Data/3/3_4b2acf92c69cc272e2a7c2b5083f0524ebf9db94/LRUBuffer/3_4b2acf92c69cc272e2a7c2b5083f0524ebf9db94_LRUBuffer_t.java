 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 /**
  * A buffer that is backed by a file on the disk. The LRUBuffer maintains a 
  * pointer back to the LRUBufferPool that allocated it so that it can pass
  * updates when it is used.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @version 2011.10.12
  */
 public class LRUBuffer implements Buffer {
 	private LRUBufferPool pool;
 	private RandomAccessFile disk;
 	private int offset;
 	private int size;
 	private byte[] data;
 	private boolean dirty;
 	private boolean loaded;
 	
 	/**
 	 * Create a new buffer.
 	 * @param pool The LRUBufferPool that spawned this Buffer. 
 	 * @param file The file that backs this buffer.
 	 * @param offset The offset of this buffer in the file.
 	 * @param size The size of this buffer (int bytes).
 	 */
 	public LRUBuffer(LRUBufferPool pool, RandomAccessFile disk, int offset, 
                      int size)
 	{
 		this.pool = pool;
 		this.disk = disk;
 		this.offset = offset;
 		this.size = size;
 		this.dirty = false;
 		this.loaded = false;
 	}
 
 	/**
 	 * Read the contents of the block represented by this buffer. This will
 	 * return a new byte array of size() size.
 	 * Also, alert the allocating pool that we were used.
 	 * @return A byte array with the contents of this Buffer.
 	 */
 	@Override
 	public byte[] read() {
 		// Alert the pool that we were used.
 		pool.markUsed(this);
 		
 		// Make sure we have the data to return.
 		if (!loaded) { readFromDisk(); }
 		
 		// Copy the data
 		byte[] retArray = data.clone();
 		
 		// Return
 		return retArray;
 	}
 
 	/**
 	 * 'Write' the contents of the argument to this block. It is not guaranteed
 	 * that this write will happen when this is called. This marks the Buffer
 	 * as dirty to tell it that a write needs to happen when it gets flushed 
 	 * from memory. 
 	 * Also, alert the allocating pool that we were used.
 	 * @param data The data to write to the block represented by the buffer.
 	 */
 	@Override
 	public void write(byte[] data) {	
 		// Alert the pool that we were used.
 		pool.markUsed(this);
 		
 		// Copy the data into our private memory
 		this.data = data.clone();
 		
 		// Mark ourself as loaded and dirty
 		this.loaded = true;
 		this.dirty = true;
 	}
 
 	/**
 	 * What is the size of the block represented by this Buffer?
 	 * @return The size of the block.
 	 */
 	@Override
 	public int size() {
 		return size;
 	}
 
 	/**
 	 * Write the data to storage and free the memory that this buffer
 	 * represents. Any later reads or writes will require touching the disk.
 	 * Also, alert the allocating buffer that we were flushed.
 	 */
 	@Override
 	public void flush() {
 		assert(loaded);
 
 		// If the data is dirty, write out to the disk.
 		if (dirty) { writeToDisk(); }
 		
 		// Free our memory (When the GC gets around to it.)
 		data = null;
 		loaded = false;
 	}
 	
 	/**
 	 * Internal call to read from the disk.
 	 * Updates the data buffer with a the data from the disk.
 	 * Marks the file as loaded and as clean.
 	 */
 	private void readFromDisk()
 	{
		data = new byte[size];
 		try {
 			disk.seek(offset);
 			disk.read(data);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		loaded = true;
 		dirty = false;
 	}
 	
 	/**
 	 * Internal call to write from memory to the disk.
 	 * Marks the file as clean.
 	 */
 	private void writeToDisk()
 	{
 		try {
 			disk.seek(offset);
 			disk.write(data);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		dirty = false;
 	}
 }
