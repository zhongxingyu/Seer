 import java.util.ArrayList;
 import java.awt.image.BufferedImage;
 
 /**
  * The StarSystem class provides the next structural tier after the Sector class for galaxy generation and manipulation.
  * @author Bryant
  */
 public class StarSystem extends Location
 {	
 	public static int MIN_HZ = 470;		// Determines minimum radius of habitable zone
 	public static int MAX_HZ = 1800;	// Determines maximum radius of habitable zone
 	private static ArrayList<Integer> HotOrbitals = new ArrayList<Integer>(); 	// Used for determining orbital generation
 	private static ArrayList<Integer> HZOrbitals = new ArrayList<Integer>();	// Used for determining orbital generation
 	private static ArrayList<Integer> ColdOrbitals = new ArrayList<Integer>();	// Used for determining orbital generation
 	
 	private Orbital[] Orbitals;		// Stores information for all orbitals in this StarSystem.
 	private byte Star;				// Number corresponds to the first part of a StarSystem's name (i.e. Alpha, Beta, etc.)
 	private StarRecord StarClass;	// Used to reference data for a particular star class.
 	private int x;					// X coordinate location within this StarSystem's sector.
 	private int y;					// Y coordinate location within this StarSystem's sector.
 	private BufferedImage Halo;		// References image information for this star's halo effect.
 	
 	/**
 	 * Constructor.
 	 * @param star = Number corresponds to the first part of a StarSystem's name (i.e. Alpha, Beta, etc.)
 	 * @param sector = Number corresponds to the second part of a StarSystem's name (i.e. Aquilae, Draconis. etc.)
 	 * @param starclass = Number corresponds to the stellar classification of this StarSystem's star.
 	 */
 	public StarSystem(byte star, int sector, byte starclass)
 	{
 		super();
 		Orbitals = new Orbital[1 + rand.nextInt(MAX_PLANETS)];
 		Star = star;
 		StarClass = Ops.getStarDetails(starclass);
 		Picture = Pics.getStarPic(starclass);
 		Halo = Pics.getStarHaloPic(starclass);
 		int u = 10000 / Orbitals.length;
 		int z = u;
 		for (int v = 0; v < Orbitals.length; v++)
 		{
 			GenOrbital(rand.nextInt(z), v);
 			z += u;
 		}
 	}
 
 	/**
 	 * Generates selected orbital and determines if it's a planet, ring or belt.
 	 * @param radius = relative distance from star, determines habitability rating.
 	 * @param index = index of selected orbital within this StarSystem.
 	 */
 	public void GenOrbital(int radius, int index)
 	{
 		byte oclass = (byte) GenOrbitalClass(radius);	
 		if (Ops.getOrbitalClasses(oclass).GetType() < 2) // is planet
 		{
 			Orbitals[index] = new Planet(radius, oclass, StarClass.getID(),this);
 		}
 		else // is ring or belt
 		{
 			Orbitals[index] = new Ring(radius,oclass,StarClass.getID(),this);
 		}
 	}
 	
 	/**
 	 * Determines an orbital's habitablity rating based off of its distance from parent star.
 	 * @param radius = relative distance from star
 	 * @return int
 	 */
 	public static int GenOrbitalClass(int radius)
 	{
 		if (radius < MIN_HZ) // Too hot!
 		{
 			return (HotOrbitals.get(rand.nextInt(HotOrbitals.size())));
 		}
 		else if ( radius > MAX_HZ) // Too cold!
 		{
 			return (ColdOrbitals.get(rand.nextInt(ColdOrbitals.size())));
 		}
 		else // Just right
 		{
 			return (HZOrbitals.get(rand.nextInt(HZOrbitals.size())));
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return Ops.getSystem(Star);
 	}
 	
 	/**
 	 * Returns indicated Orbital stored within this StarSystem.
 	 * @param index = index of desired Orbital
 	 * @return Orbital
 	 */
 	public Orbital getChild(int index)
 	{
 		return getChild()[index];
 	}
 	
 	/**
 	 * If no index is provided, GetOrbital returns all Orbitals within this StarSystem.
 	 * @return
 	 */
 	public Orbital[] getChild()
 	{
 		return Orbitals;
 	}
 	
 	/**
 	 * Sets the X coordinate for this StarSystem within its Sector.
 	 * @param X = desired X coordinate.
 	 */
 	public void SetX(int X)
 	{
		x = X;
 	}
 	
 	/**
 	 * Returns X coordinate for this StarSystem within its Sector.
 	 * @return byte
 	 */
 	public int GetX()
 	{
 		return x;
 	}
 	
 	/**
 	 * Sets the Y coordinate for this StarSystem within its Sector.
 	 * @param Y = desired Y coordinate.
 	 */
 	public void SetY(int Y)
 	{
		y = Y;
 	}
 	
 	/**
 	 * Returns Y coordinate for this StarSystem within its Sector.
 	 * @return byte
 	 */
 	public int GetY()
 	{
 		return y;
 	}
 	
 	/**
 	 * Returns StarRecord information specific to this StarSystem.
 	 * @return StarRecord
 	 */
 	public StarRecord GetStarClass()
 	{
 		return StarClass;
 	}
 	
 	/**
 	 * Returns a detailed description of this StarSystem's star's properties.
 	 * @return String
 	 */
 	public String GetDetails()
 	{
 		int subclass = (Math.abs(x) % 10);
 		return String.format("Class: %s%d - %s Star\n" +
 							 "Mass: %.2f solar masses\n" +
 							 "Radius: %.2f solar radii\n" +
 							 "Temp: %d K\n" +
 							 "Luminosity: %.2f",
 							 StarClass.getClassification(), GetSubClass(subclass), StarClass.getColor(),
 							 StarClass.getMinMass() + ((StarClass.getMaxMass() - StarClass.getMinMass())/(subclass+1)),
 							 StarClass.getMinRadius() + ((StarClass.getMaxRadius() - StarClass.getMinRadius())/(subclass+1)),
 							 StarClass.getMinTemp() + ((StarClass.getMaxTemp() - StarClass.getMinTemp())/(subclass+1)),
 							 StarClass.getExactLuminosity((byte)subclass));
 	}
 	
 	/**
 	 * Shift a star's subclass to an appropriate value (9 is lowest subclass, 0 is highest)
 	 * @param subclass = subclass data derived from X coordinate. Ranges from 0-9.
 	 * @return int
 	 */
 	public static int GetSubClass(int subclass)
 	{
 		return 9 - ((subclass) % 10);
 	}
 	
 	/**
 	 * Generates this star's stellar classification.
 	 * @return
 	 */
 	public static byte GenStarClass()
 	{
 		return (byte) ((Ops.getStarClass(rand.nextInt(Ops.getStarFreqCeiling()))) + rand.nextInt(10)); 
 	}
 	
 	/**
 	 * Used for determining where particular planets can generate
 	 */
 	public static void SortOrbitals()
 	{
 		for (int x = 0; x < Ops.getOrbitalClasses().size(); x++)
 		{
 			switch (Ops.getOrbitalClasses(x).GetHZRating())
 			{
 				case 1:
 					HotOrbitals.add(x);
 					break;
 				case 2:
 					HZOrbitals.add(x);
 					break;
 				case 3:
 					ColdOrbitals.add(x);
 					break;
 				case 4:
 					HotOrbitals.add(x);
 					HZOrbitals.add(x);
 					ColdOrbitals.add(x);
 					break;
 			}
 		}
 	}
 	
 	public BufferedImage getHalo()
 	{
 		return Halo;
 	}
 	
 	public Location getParent()
 	{
 	  return parent;
 	}
 	
 	public int whatAmI()
 	{
 	  return 3;
 	}
 	
 /*	public int[][] getOrbits()
 	{
 		int[][] orbits = new int[Orbitals.length][2];
 		
 		for(int x = 0; x < orbits.length; x++)
 		{
 			orbits[x][0] = (int)((x+1) * 10 * Math.sin(Math.toRadians(rand.nextInt(360))));
 			orbits[x][1] = (int)((x+1) * 10 * Math.cos(Math.toRadians(rand.nextInt(360))));
 		}
 		
 		return orbits;
 	}*/
 	
 }
