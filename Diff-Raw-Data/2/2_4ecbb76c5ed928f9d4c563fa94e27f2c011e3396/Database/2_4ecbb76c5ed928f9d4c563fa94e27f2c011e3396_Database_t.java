 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * This is the primary database class for the PRprog project.
  * 
  * This class implements the interface that the user lays out in the control
  * file. This class then converts the input arguments into the proper internal
  * classes, and inserts them in to the internal data storage structures.
  * 
  * All of these classes are typed void. They 'return' their value to stdout.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @version 2011.10.09
  */
 public class Database {
 	private PRQuadTree quadtree;
 	private BinarySearchTree bst;
 	private MemoryManager mem;
 	private BPByteArray bp_array;
 	private BufferPool bp;
 	
 	private int num_buf;
 	private int block_sz;
 	
 	private static final int MAX_SIZE = (1 << 14);
 	
 	/**
 	 * Instantiate a new Database instance.
 	 * @throws IOException 
 	 */
 	public Database(int num_buf, int block_sz) throws IOException
 	{
 		// Store args
 		this.num_buf = num_buf;
 		this.block_sz = block_sz;
 		
 		// Open the file
 		File fp = new File("p4bin.dat");
 		if (fp.exists()) {
 			fp.delete();
 			fp.createNewFile();
 		} else {
 			fp.createNewFile();
 		}
 		
 		// Set up the buffer pool.
 		bp = new LRUBufferPool(fp, num_buf, block_sz);
 		
 		// Wrap it in a BPByteArray
 		bp_array = new BPByteArray(bp);
 		
 		// Set up memory
 		mem = new MemoryManager(bp_array, block_sz);
 		
 		// Put a BST and QuadTree on top
 		bst = new BinarySearchTree(mem);
 		quadtree = new PRQuadTree(mem, MAX_SIZE);
 	}
 	
 	/**
 	 * Insert a new record into the database
 	 * @param x The x coordinate of the city to insert
 	 * @param y the y coordinate of the city to insert
 	 * @param name The name of the city to insert
 	 */
 	public void insert( Integer x, Integer y, String name )
 	{
 		// Make sure x and y are valid.
 		if ((x < 0) || (x >= (MAX_SIZE))) {
 			System.out.println(OutputMessages.InsertBadX);
 			return;
 		}
 		if ((y < 0) || (y >= (MAX_SIZE))) {
 			System.out.println(OutputMessages.InsertBadY);
 			return;
 		}
 		
 		// Create the city record.
 		Handle cName = DiskString.alloc(mem, name);
 		Handle city = City.alloc(mem, x, y, cName);
 		
 		// Try inserting into the quad tree first.
 		if (!quadtree.insert(x, y, city)) {
 			System.out.println(OutputMessages.InsertDup);
 			return;
 		}
 		
 		// If that succeeded, insert into the BST
 		bst.insert(cName, city);
 		
 		// Output success
 		System.out.println(OutputMessages.InsertSuccess);
 	}
 	
 	/**
 	 * Remove a value from the database.
 	 * @param x The x coordinate of the city to remove
 	 * @param y The y coordinate of the city to remove
 	 */
 	public void remove( Integer x, Integer y )
 	{
 		// Make sure x and y are valid.
 		if ((x < 0) || (x >= (MAX_SIZE))) {
 			System.out.println(OutputMessages.RemoveBadX);
 			return;
 		}
 		if ((y < 0) || (y >= (MAX_SIZE))) {
 			System.out.println(OutputMessages.RemoveBadY);
 			return;
 		}
 		
 		// Remove from the quad tree.
 		Handle city = quadtree.remove(x, y);
 		
 		// Make sure we're removing something.
 		if (city == null) {
 			System.out.println(OutputMessages.RemoveNoFound);
 			return;
 		}
 		
 		City c = City.deref(mem, city);
 		
 		// Remove from the BST.
 		bst.remove(c.getName());
 		
 		// Print the removal message
 		System.out.println(OutputMessages.formatRemoveCity(c));
 		
 		// Delete the city record
 		mem.remove(city);
 	}
 	
 	/**
 	 * Remove a value from the database
 	 * @param name The name of the city to remove.
 	 */
 	public void remove(String name)
 	{
 		// Remove the city from the BST
 		Handle city = bst.remove(name);
 		
 		// Make sure we're removing something
 		if (city == null) {
 			System.out.println(OutputMessages.RemoveNoFound);
 			return;
 		}
 		
 		City c = City.deref(mem, city);
 		
 		// Remove from the Quad Tree
 		quadtree.remove(c.getX(), c.getY());
 		
 		// Print the removal message
 		System.out.println(OutputMessages.formatRemoveCity(c));
 		
 		// Delete the city
 		mem.remove(city);
 	}
 	
 	/**
 	 * Find data about cities based on a name search.
 	 * @param name The name to search on.
 	 */
 	public void find( String name )
 	{
 		// Query the BST
 		ArrayList<Handle> found = bst.find(name);
 		
 		// Perform output.
 		System.out.println(OutputMessages.findCRF);
 		if (found.size() == 0) {
 			System.out.println(OutputMessages.findNoRecords);
 		} else {
 			for( Handle city : found ) {
 				System.out.println(OutputMessages.formatFindRecord(City.deref(mem, city)));
 			}
 		}
 	}
 	
 	/**
 	 * Perform a radius search on the database.
 	 * @param x The x coordinate of the center of the circle
 	 * @param y The y coordinate of the center of the circle
 	 * @param radius The radius to search in.
 	 */
 	public void search( Integer x, Integer y, Integer radius )
 	{
 		if (Math.abs(x) >= MAX_SIZE) {
 			System.out.println(OutputMessages.SearchBadX);
 			return;
 		}
 		if (Math.abs(y) >= MAX_SIZE) {
 			System.out.println(OutputMessages.SearchBadX);
 			return;
 		}
 		if ((radius < 0) || (radius >= MAX_SIZE)) {
 			System.out.println(OutputMessages.SearchBadRadius);
 			return;
 		}
 		
 		List<Handle> list = new ArrayList<Handle>();
 		int visited = quadtree.radius_search(x, y, radius, list);
 		
 		// Perform output.
 		System.out.println(OutputMessages.findCRF);
 		if (list.size() == 0) {
 			System.out.println(OutputMessages.findNoRecords);
 		} else {
 			for( Handle city : list ) {
 				System.out.println(OutputMessages.formatFindRecord(City.deref(mem, city)));
 			}
 		}
 		System.out.println(OutputMessages.formatVisitedNodes(visited));
 	}
 	
 	/**
 	 * Print a listing of the PRQuadTree nodes in preorder
 	 */
 	public void debug()
 	{
 		System.out.println(quadtree.toString());
 		System.out.println("Buffers: " + bp.debug());
		System.out.println(mem.dump());
 	}
 	
 	/**
 	 * Initialize the database to be empty
 	 */
 	public void makenull()
 	{
 		// Open the file
 		File fp = new File("p4bin.dat");
 		if (fp.exists()) {
 			fp.delete();
 			try {
 				fp.createNewFile();
 			} catch (IOException e) {
 				System.err.println("Error Reopening File.");
 				e.printStackTrace();
 			}
 		}
 		
 		// Set up the buffer pool.
 		try {
 			bp = new LRUBufferPool(fp, num_buf, block_sz);
 		} catch (IOException e) {
 			System.err.println("Error Reopening Buffer Pool.");
 			e.printStackTrace();
 		}
 		
 		// Wrap it in a BPByteArray
 		bp_array = new BPByteArray(bp);
 		
 		// Set up memory
 		mem = new MemoryManager(bp_array, block_sz);
 		
 		// Put a BST and QuadTree on top
 		bst = new BinarySearchTree(mem);
 		quadtree = new PRQuadTree(mem, MAX_SIZE);
 		
 		// Output success
 		System.out.println(OutputMessages.MakeNullSuccess);
 	}
 }
