 package state;
 
 import galaxy.generation.DelaunayLaneGenerator;
 import galaxy.generation.MinimumSpanningTreeForce;
 import galaxy.generation.NascentGalaxy;
 import galaxy.generation.RandomStarGenerator;
 import galaxy.generation.SimpleBlobCreator;
 import galaxy.generation.SimplePointCreator;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import military.Ship;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  * Class that represents all the objects in the universe: stars, empires, etc.
  * In particular, it should include all stats required to save/load/serialize the game state.
  * @author Daniel Langdon
  */
 public class Universe
 {
 	private static Universe instance_;
 	public static Universe instance()
 	{
 		return instance_;
 	}
 
 // Internals ==========================================================================================================	
 	private List<Star> stars;
 	private List<Empire> empires;
 	private HashSet<Fleet> fleets;
 	private Empire playerEmpire;
 
 // Public Methods =====================================================================================================
 	public Universe() throws SlickException
 	{
 		instance_ = this;
 		this.empires = new ArrayList<Empire>();
 		this.stars = new ArrayList<Star>();
 		this.fleets = new HashSet<Fleet>();
		NascentGalaxy ng = new NascentGalaxy();
 
 //		ng.addForce(new StaticGalaxyCreator());
 
		ng.addForce(new SimpleBlobCreator(125, 75, 30, 4, 15));
		ng.addForce(new SimplePointCreator(5, 50, 4.0f));
 		ng.addForce(new DelaunayLaneGenerator(0.15f));
 		ng.addForce(new MinimumSpanningTreeForce());
 		
 		ng.addForce(new RandomStarGenerator());
 		ng.unleashAllForces();
 		ng.blosom();
 		
 		/// @todo Remove test empires and organize elsewhere.
 		empires.add(new Empire("Blaps", new Color(180,0,0)));
 		empires.add(new Empire("Bleps", new Color(0,180,0)));
 		empires.add(new Empire("Blips", new Color(0,0,180)));
 		empires.add(new Empire("Blops", new Color(180,180,0)));
 		empires.add(new Empire("Blups", new Color(0,180,180)));
 		playerEmpire = empires.get(0);
 		
 		// Create a bunch of colonies for the empires, just for fun.
 //		Empire[] empireList = new Empire[empires.size()];
 //		empires.toArray(empireList);
 //		for(Star star: stars)
 //		{
 //			if(turn < 5)
 //				new Colony(star, empireList[turn]);
 //			turn = (turn+1)%7;
 //		}
 		
 		// Create a few designs, just for kicks
 		Unit[] figthers = new Unit[23];
 		for(int i=0; i<23; i++)
 			figthers[i] = new Ship(String.format("A-Figther %02d", i), new Image("resources/ship2.png"), 1.0f); 
 		Unit colony = new Ship("Colony Ship", new Image("resources/ship1.png"), 10.0f);
 		
 		// Create a bunch of fleets.
 		Random r = new Random();
 		for(int i=0; i<100; i++)
 		{
 			int empire = r.nextInt(empires.size());
 			int star = r.nextInt(stars.size());
 			
 			Fleet fleet = new Fleet(stars.get(star), empires.get(empire)); 
 			fleets.add(fleet);
 
 			int numFighters = new Random().nextInt(23);
 			for(int j=0; j<numFighters; j++)
 				fleet.addUnits(figthers[j], j*10+1);
 			fleet.addUnits(colony, 5);
 		}
 	}
 
 	/**
 	 * @return A list of all stars.
 	 */
 	public List<Star> getStars()
 	{
 		return stars;
 	}
 
 	/**
 	 * @return A list of all empires.
 	 */
 	public List<Empire> getEmpires()
 	{
 		return empires;
 	}
 
 	/**
 	 * @return A set of all task fleets in the galaxy.
 	 */
 	public HashSet<Fleet> getFleets()
 	{
 		return fleets;
 	}
 	
 	/**
 	 * @return the empire corresponding to the local player
 	 */
 	public Empire getPlayerEmpire()
 	{
 		return playerEmpire;
 	}
 
 	/**
 	 * Removes a fleet from the list of all fleets.
 	 * @param f The fleet to remove.
 	 */
 	public void removeFleet(Fleet f)
 	{
 		f.orbiting().leave(f);
 		fleets.remove(f);
 	}
 
 }
