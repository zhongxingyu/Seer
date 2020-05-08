 /**
  * The 'user facing' interface to the memory management core of the program
  * This interface deals exclusively with byte arrays and so should have an 
  * interface on top of it in order to serialize/deserialize objects.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @verson 2011.08.30
  */
 public class MemoryManager {
 	
 	// Private Variables
 	private MemoryPool pool;
 	private FreeBlockList fbl;
 	private MMAlg mmalg;
 	
 	/**
 	 * Instantiate a new Memory Manager
 	 * @param size The byte size of the underlying memory pool
 	 */
 	public MemoryManager(int size)
 	{
 		pool = new MemoryPool(size);
 		fbl = new FreeBlockList(size);
 		mmalg = new BestFitAlg(fbl);
 	}
 	
 	/**
 	 * Insert data into memory
 	 * @param data The data to insert
 	 * @param size The length of data to insert from the buffer
 	 * @return A handle to the inserted data (or an error handle)
 	 */
 	public Handle insert(byte[] data, int size)
 	{
 		int index = mmalg.getFit(size + 1);
 		
 		// Error Checking
 		if (index < 0) {
 			System.err.println(Errors.CannotAllocateMem);
 			return new Handle(-1);
 		}
 		
 		// Allocate the memory from the Free Block List
 		fbl.allocate(index, size + 1);
 		int offset = fbl.getSize(index);
 		
 		// Create our write buffer
 		byte[] write_data = new byte[size + 1];
 		write_data[0] = (byte) size;
 		System.arraycopy(data, 0, write_data, 1, size);
 		
 		// Write to memory
 		pool.write(offset, write_data, size + 1);
 		
 		// Return a handle to this data
 		return new Handle(offset);
 	}
 	
 	/**
 	 * Remove the data from memory
 	 * @param handle A handle for the memory to remove
 	 */
 	public void remove(Handle handle)
 	{
 		// Read the size from the memory pool
 		int size = pool.read(handle.getOffset());
 		
 		// Deallocate this block from the pool
 		fbl.deallocate(handle.getOffset(), size + 1);
 	}
 	
 	/**
 	 * Get the data from memory
 	 * @param handle A handle for the memory to get
 	 * @param data The data array to put the data into
 	 * @param size The size to get from the memory manager
 	 * @return the number of bytes actually copied into the buffer.
 	 */
 	public int get(Handle handle, byte[] data, int size)
 	{
 		// Determine which is smaller.
		int mem_size = pool.read(handle.getOffset()) & 0xFF;
 		int read_size = Math.min(size, mem_size);
 		
 		// Read the data into the data array ptr.
 		pool.read(handle.getOffset() + 1, data, read_size);
 		
 		// How much was actually read.
 		return read_size;
 	}
 	
 	/**
 	 * Dump the underlying Free Block List
 	 */
 	public void dump()
 	{
 		System.out.println(fbl.toString());
 	}
 }
