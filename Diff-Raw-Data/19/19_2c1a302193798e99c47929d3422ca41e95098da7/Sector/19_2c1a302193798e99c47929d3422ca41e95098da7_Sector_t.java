 
 /**
  * The Cluster class provides the next structural tier after the Cluster class for galaxy generation and manipulation.
  * @author Bryant
  */
 public class Sector extends Location
 {
	private static final int DEFAULT_RADIUS = 150;
	private static final int DISTANCE_MOD = 150;
 	private StarSystem[] StarSystems;	//Holds all StarSystem information for this Sector.
 	private int Name;					// Number corresponding to entry in Ops.SectorNames 
 	private int x;						// X coordinate data.
 	private int y;						// Y coordinate data.
 
 	/**
 	 * Constructor.
 	 * @param name = provided by Cluster. Value corresponds to an entry in Ops.
 	 */
 	public Sector(int name, Location cluster)
 	{
 		super();
 		Name = name;
 		Picture = Pics.getSectorPic(rand.nextInt(10));
 		parent = cluster;
 		Generate();
 	}
 	
 	/**
 	 * Generates all StarSystems within this Sector, as well as positional data.
 	 */
 	public void Generate()
 	{
 		StarSystems = new StarSystem[1 + rand.nextInt(MAX_SYSTEMS)];
 		double spacing = 360.0 / StarSystems.length;
 		for (int z = 0; z < StarSystems.length; z++)
 		{
 			StarSystems[z] = new StarSystem((byte)z,Name,(byte)rand.nextInt(7));
			StarSystems[z].SetX((int)(((DEFAULT_RADIUS + rand.nextInt(DISTANCE_MOD)) * Math.cos(Math.toRadians(spacing*z)))));
			StarSystems[z].SetY((int)(((DEFAULT_RADIUS + rand.nextInt(DISTANCE_MOD)) * Math.sin(Math.toRadians(spacing*z)))));
 			StarSystems[z].setParent(this);  //Ryan's code hope it works
 			
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 * Returns String representation of this Sector
 	 */
 	@Override
 	public String toString()
 	{
 		return Ops.getSector(Name,0) + " Sector";
 	}
 	
 	/**
 	 * Returns indicated StarSystem stored within this Sector.
 	 * @param index = index of desired StarSystem within this Sector.
 	 * @return StarSystem
 	 */
 	public StarSystem getChild(int index)
 	{
 		return getChild()[index];
 	}
 	
 	/**
 	 * If no index is provided, GetStar returns all StarSystems within this Sector.
 	 * @return StarSystem[]
 	 */
 	public StarSystem[] getChild()
 	{
 		return StarSystems;
 	}
 	
 	
 	/**
 	 * Returns X coordinate for this Sector within its Cluster.
 	 * @return int
 	 */
 	public int GetX()
 	{
 		return x;
 	}
 	
 	/**
 	 * Returns Y coordinate for this Sector within its Cluster.
 	 * @return int
 	 */
 	public int GetY()
 	{
 		return y;
 	}
 	
 	/**
 	 * Sets the X coordinate for this Sector within its Cluster.
 	 * @param X = desired X coordinate.
 	 */
 	public void SetX(int X)
 	{
 		x = X;
 	}
 	
 	/**
 	 * Sets the Y coordinate for this Sector within its Cluster.
 	 * @param Y = desired Y coordinate.
 	 */
 	public void SetY(int Y)
 	{
 		y = Y;
 	}
 	
 	public int whatAmI()
 	{
 	  return 2;
 	}
 }
