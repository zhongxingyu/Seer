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
 	private PRQuadTree<City> quadtree;
 	private BinarySearchTree<String, City> bst;
	private static final int MAX_SIZE = (1 << 14);
 	
 	/**
 	 * Instantiate a new Database instance.
 	 */
 	public Database()
 	{
		quadtree = new PRQuadTree<City>(MAX_SIZE);
 		bst = new BinarySearchTree<String, City>();
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
 		City city = new City(x, y, name);
 		
 		// Try inserting into the quad tree first.
 		if (!quadtree.insert(x, y, city)) {
 			System.out.println(OutputMessages.InsertDup);
 			return;
 		}
 		
 		// If that succeeded, insert into the BST
 		bst.insert(name, city);
 		
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
 		City city = quadtree.remove(x, y);
 		
 		// Make sure we're removing something.
 		if (city == null) {
 			System.out.println(OutputMessages.RemoveNoFound);
 			return;
 		}
 		
 		// Remove from the BST.
 		bst.remove(city.getName());
 		
 		// Print the removal message
 		System.out.println(OutputMessages.formatRemoveCity(city));
 	}
 	
 	/**
 	 * Remove a value from the database
 	 * @param name The name of the city to remove.
 	 */
 	public void remove(String name)
 	{
 		// Remove the city from the BST
 		City city = bst.remove(name);
 		
 		// Make sure we're removing something
 		if (city == null) {
 			System.out.println(OutputMessages.RemoveNoFound);
 			return;
 		}
 		
 		// Remove from the Quad Tree
 		quadtree.remove(city.getX(), city.getY());
 		
 		// Print the removal message
 		System.out.println(OutputMessages.formatRemoveCity(city));
 	}
 	
 	/**
 	 * Find data about cities based on a name search.
 	 * @param name The name to search on.
 	 */
 	public void find( String name )
 	{
 		// Query the BST
 		ArrayList<City> found = bst.find(name);
 		
 		// Perform output.
 		System.out.println(OutputMessages.findCRF);
 		if (found.size() == 0) {
 			System.out.println(OutputMessages.findNoRecords);
 		} else {
 			for( City city : found ) {
 				System.out.println(OutputMessages.formatFindRecord(city));
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
 		// TODO
 	}
 	
 	/**
 	 * Print a listing of the PRQuadTree nodes in preorder
 	 */
 	public void debug()
 	{
 		System.out.println(quadtree.toString());
 	}
 	
 	/**
 	 * Initialize the database to be empty
 	 */
 	public void makenull()
 	{
 		// Reinitialize the database.
		quadtree = new PRQuadTree<City>(MAX_SIZE);
 		bst = new BinarySearchTree<String, City>();
 		
 		// Output success
 		System.out.println(OutputMessages.MakeNullSuccess);
 	}
 }
