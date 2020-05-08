 import java.awt.image.BufferedImage;
 
 
 /**
  * Galaxy class handles generation and manipulation for all elements within instantiated galaxy object.
  * @author Bryant
  */
 public class Galaxy extends Location
 {
 	private Cluster CurrentCluster;		// Stores currently loaded cluster
 	private Cluster[] Clusters;			// Stores a "lite" version of all the clusters in the galaxy
 	
 	/**
 	 * Constructor.
 	 * If no parameters are provided, galaxy will be generated at random.
 	 */
 	public Galaxy()
 	{
 		super();
 		Clusters = new Cluster[MAX_CLUSTERS];
 		
 		for (int x = 0; x < Clusters.length; x++)
 		{
 			Clusters[x] = new Cluster(rand.nextLong());
 		}
 		PlaceClusters();
 	}
 	
 	/**
 	 * Constructor.
 	 * If an array of long variables are provided, those seed values will be used for any subsequent generation.
 	 * This can be used to override the MAX_CLUSTER constant and create a galaxy of varying size.
 	 * @param clusters = array of seed values for "lite" cluster generation
 	 */
 	public Galaxy(long[] clusters)
 	{
 		super();
 		Clusters = new Cluster[clusters.length-1];
 		
 		for (int x = 0; x < Clusters.length; x++)
 		{
 			Clusters[x] = new Cluster(rand.nextLong());
 		}
 		PlaceClusters();
 	}
 	
 	/**
 	 * Generates selected cluster and stores it in CurrentCluster. This will remove any previously loaded cluster.
 	 * @param index = desired cluster index.
 	 */
 	public void Generate(int index)
 	{
 		StarSystem.SortOrbitals();
 		CurrentCluster = new Cluster(Clusters[index].GetSeed(),Clusters[index].GetName(),Clusters[index].GetName2(),Clusters[index].GetPic(),this);
 	}
 
 	/**
 	 * This method generates all of the cluster positional data within this galaxy.
 	 */
 	public void PlaceClusters()
 	{
 		double spacing = 360.0 / Clusters.length;
		int radius = 100;
 		for (int z = 0; z < Clusters.length; z++)
 		{
			Clusters[z].SetX((int)((radius + rand.nextInt(250)) * Math.cos(Math.toRadians(spacing*z))));
			Clusters[z].SetY((int)((radius + rand.nextInt(250)) * Math.sin(Math.toRadians(spacing*z))));
 		}
 	}
 	
 	/**
 	 * Returns the currently loaded cluster.
 	 * @return Cluster
 	 */
 	public Cluster GetCluster()
 	{
 		return CurrentCluster;
 	}
 	
 	/**
 	 * Returns an array of type Cluster which provides "lite" versions of all of the clusters in this galaxy.
 	 * @return Cluster[]
 	 */
 	public Cluster[] GetClusters()
 	{
 		return Clusters;
 	}
 	
 	/**
 	 * Returns selected Sector within the currently loaded Cluster.
 	 * @param index = index of desired Sector.
 	 * @return Sector
 	 */
 	public Sector GetSector(int index)
 	{
 		return CurrentCluster.getChild(index);
 	}
 	
 	/**
 	 * Returns selected StarSystem from the Sector passed to this method.
 	 * @param sector = Sector which contains the desired StarSystem.
 	 * @param index = index of desired StarSystem.
 	 * @return StarSystem
 	 */
 	public StarSystem GetStar(Sector sector, int index)
 	{
 		return sector.getChild(index);
 	}
 	
 	/**
 	 * Returns selected StarSystem for selected Sector within the currently loaded Cluster.
 	 * @param sector = index of desired Sector.
 	 * @param index = index of desired StarSystem.
 	 * @return = StarSystem
 	 */
 	public StarSystem GetStar(int sector, int index)
 	{
 		return GetSector(sector).getChild(index);
 	}
 	
 	/** Returns selected Orbital from the StarSystem passed to this method.
 	 * @param star = StarSystem which contains the desired Orbital.
 	 * @param index = index of desired Orbital.
 	 * @return Orbital
 	 */
 	public Orbital GetOrbital(StarSystem star, int index)
 	{
 		return star.getChild(index);
 	}
 	
 	/**
 	 * Returns selected Orbital from the indicated StarSystem which is contained within the target Sector in the currently loaded Cluster.
 	 * @param sector = index of desired Sector.
 	 * @param star = index of desired StarSystem.
 	 * @param index = index of desired Orbital.
 	 * @return Orbital
 	 */
 	public Orbital GetOrbital(int sector, int star, int index)
 	{
 		return GetOrbital(GetStar(sector,star),index);
 	}
 	
 	/**
 	 * Returns Moon from the Planet object passed to this method.
 	 * @param planet = Planet that contains the Moon
 	 * @param index = index of the desired Moon;
 	 * @return Moon
 	 */
 	public Moon GetMoon(Planet planet, int index)
 	{
 		return planet.GetMoon(index);
 	}
 	
 	/**
 	 * Using the provided sector number (which is used to reference a String in Ops) returns the name of selected Sector.
 	 * @param sector = number that corresponds to a String in SectorNames within the Ops object.
 	 * @return String
 	 */
 	public String SectorDetails(int sector)
 	{
 		return Ops.getSector(sector, 0) + " Sector";
 	}
 	
 	/**
 	 * Returns an array of Sector objects from the currently loaded cluster.
 	 * @return Sector[]
 	 */
 	public Sector[] GetSectors ()
 	{
 		return CurrentCluster.getChild();
 	}
 	
 	/**
 	 * Using the provided sector and star numbers (which are used to reference Strings in Ops) returns the name of selected StarSystem.
 	 * @param sector = number that corresponds to a String in SectorNames within the Ops object.
 	 * @param star = number that corresponds to a String in SectorNames within the Ops object.
 	 * @return String
 	 */
 	public String StarDetails(int sector, int star)
 	{
 		return Ops.getSystem(sector,star) + "\n" + GetStar(sector,star).GetDetails();
 	}
 	
 	/**
 	 * Using the provided sector and star numbers (which are used to reference Strings in Ops) returns the name of selected Orbital.
 	 * @param sector = number that corresponds to a String in SectorNames within the Ops object.
 	 * @param star = number that corresponds to a String in SectorNames within the Ops object.
 	 * @param orbital = provides the orbit number of selected orbital for use in name.
 	 * @return String
 	 */
 	public String OrbitalDetails(int sector, int star, int orbital)
 	{
 		return Ops.getPlanet(sector,star,orbital) + "\n" + GetOrbital(sector,star,orbital).GetDetails();
 	}
 	
 	/**
 	 * Using the provided parameters (which are used to reference Strings in Ops) returns the name of selected Moon.
 	 * @param sector = number that corresponds to a String in SectorNames within the Ops object.
 	 * @param star = number that corresponds to a String in SectorNames within the Ops object.
 	 * @param orbital = provides the orbit number of selected orbital for use in name.
 	 * @param moon =  = provides the orbit number of selected moon for use in name.
 	 * @return
 	 */
 	public String MoonDetails(int sector, int star, int orbital, int moon)
 	{
 		return Ops.getMoon(sector, star, orbital, moon) + " - " + GetMoon((Planet) GetOrbital(sector,star,orbital),moon);
 	}
 	
 	/**
 	 * Prints all of the data stored within the currently loaded cluster.
 	 */
 	public void DisplayCluster()
 	{
 		System.out.printf("%s\n", CurrentCluster);
 		for (int x = 0; x < GetCluster().getChild().length; x++)
 		{
 			System.out.printf("%s\n", SectorDetails(x));
 			for (int y = 0; y < GetSector(x).getChild().length; y++)
 			{
 				System.out.printf("%s\n", StarDetails(x,y));
 				for (int z = 0; z < GetStar(x,y).getChild().length; z++)
 				{
 					System.out.printf("%s\n", OrbitalDetails(x,y,z));
 					
 					if (GetOrbital(x,y,z).getClass() == Planet.class)
 					{
 						for (int u = 0; u < ((Planet) GetOrbital(x,y,z)).GetMoon().length; u++)
 						{
 							System.out.printf("\t%s\n", MoonDetails(x, y, z, u));
 						}
 					}
 					System.out.println();
 				}
 			}
 		}
 	}
 	
 	public Location randomStart()
 	{
 	  int randInt = rand.nextInt(GetSectors().length);
 	  return GetSector(randInt);
 	}
 	
 	@Override
 	public BufferedImage GetCenterImage()
 	  {
 		  return Pics.getGalaxy();
 	  }
 }
