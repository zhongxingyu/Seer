 /**
  * Hold a reference to some data in memory. This holds this reference as an 
  * offset into the memory pool.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @version 2011.08.30
  */
 public class Handle {
	// This handle will be returned in the event of an error.
	public static Handle ERROR_HANDLE = new Handle(-1);
	
 	// Private variables
 	private final int offset;
 	
 	/**
 	 * Create a new handle
 	 * @param offset
 	 */
 	public Handle(int offset)
 	{
 		this.offset = offset;
 	}
 	
 	/**
 	 * Get the offset of the handle
 	 * @return the offset represented by this handle
 	 */
 	public int getOffset()
 	{
 		return this.offset;
 	}
 }
