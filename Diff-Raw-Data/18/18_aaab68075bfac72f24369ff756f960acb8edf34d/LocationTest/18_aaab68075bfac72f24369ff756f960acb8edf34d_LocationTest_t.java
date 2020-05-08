 
 package jcaverntest;
 
 import junit.framework.*;
 
 import jcavern.*;
 
 /**
  * Tests functionality of Location.
  *
  * @author	Bill Walker
 * @version	$Id$
  */
 public class LocationTest extends TestCase
 {	
 	/**
 	 * Creates a suite of tests.
 	 */
 	public LocationTest(String name)
 	{
 		super(name);
 	}
 
 	public void testReciprocal()
 	{
 		Location zero = new Location(10, 10);
 
 		assert("north vs south", zero.getNeighbor(Location.NORTH).getNeighbor(Location.SOUTH).equals(zero));
 		assert("south vs north", zero.getNeighbor(Location.SOUTH).getNeighbor(Location.NORTH).equals(zero));
 		
 		assert("northeast vs southwest", zero.getNeighbor(Location.NORTHEAST).getNeighbor(Location.SOUTHWEST).equals(zero));
 		assert("southwest vs northeast", zero.getNeighbor(Location.SOUTHWEST).getNeighbor(Location.NORTHEAST).equals(zero));
 		
 		assert("east vs west", zero.getNeighbor(Location.EAST).getNeighbor(Location.WEST).equals(zero));
 		assert("west vs east", zero.getNeighbor(Location.WEST).getNeighbor(Location.EAST).equals(zero));
 		
 		assert("southeast vs northwest", zero.getNeighbor(Location.SOUTHEAST).getNeighbor(Location.NORTHWEST).equals(zero));
 		assert("northwest vs southeast", zero.getNeighbor(Location.NORTHWEST).getNeighbor(Location.SOUTHEAST).equals(zero));		
 	}
 	
 	public void testNeighborToward()
 	{
 		Location zero = new Location(10, 10);
 		
 		assert("neighbor toward east", zero.getNeighborToward(new Location(20, 10)).equals(new Location(11, 10)));
 		assert("neighbor toward west", zero.getNeighborToward(new Location(0, 10)).equals(new Location(9, 10)));
 		
 		assert("neighbor toward north", zero.getNeighborToward(new Location(10, 0)).equals(new Location(10, 9)));
 		assert("neighbor toward south", zero.getNeighborToward(new Location(10, 20)).equals(new Location(10, 11)));
 		
 		assert("neighbor toward northeast", zero.getNeighborToward(new Location(20, 0)).equals(new Location(11, 9)));
 		assert("neighbor toward northwest", zero.getNeighborToward(new Location(0, 0)).equals(new Location(9, 9)));
 		
 		assert("neighbor toward southeast", zero.getNeighborToward(new Location(20, 20)).equals(new Location(11, 11)));
 		assert("neighbor toward southwest", zero.getNeighborToward(new Location(0, 20)).equals(new Location(9, 11)));
 	}
 }
