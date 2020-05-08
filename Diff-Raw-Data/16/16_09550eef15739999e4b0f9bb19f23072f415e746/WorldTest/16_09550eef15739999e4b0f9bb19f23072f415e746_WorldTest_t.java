 
 package jcaverntest;
 
 import junit.framework.*;
 
 import jcavern.*;
 
 /**
  * Tests functionality of jcavern.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class WorldTest extends TestCase
 {
 	/** * A model of the game world */
 	private World			mWorld;
 	
 	/** * The representation of the player */
 	private Player			mPlayer;
 
 	/**
 	 * Creates a suite of tests.
 	 */
 	public WorldTest(String name)
 	{
 		super(name);
 	}
 
 	/**
 	 * Builds a world and a player, puts the player in the world.
 	 */
 	public void setUp() throws ThingCollisionException
 	{
 		// Create a world
 		mWorld = new World();
 
 		// Create a player
 		mPlayer  = new Player("Bill");
 
 		// Put the player in the world
 		mWorld.place(new Location(5, 5), mPlayer);
 	}
 	
 	/**
 	 * Tears down after tests. Not currently in use.
 	 */
 	public void tearDown()
 	{
 		mWorld = null;
 		mPlayer = null;
 	}
 	
 	
 	/**
 	 * Tests whether you can find the player where you left him.
 	 */
 	public void testFindPlayer() throws JCavernInternalError
 	{
 		Location	expectedLocation = new Location(5, 5);
 		Location	foundLocation = mWorld.getLocation(mPlayer);
 		
 		assert("testFindPlayer", expectedLocation.equals(foundLocation));
 	}
 	
 	/**
 	 * Tests whether two trees can be placed into the same location.
 	 */
 	public void testCollision() throws ThingCollisionException, JCavernInternalError
 	{
 		Tree		aTree = new Tree();
 		Tree		anotherTree = new Tree();
 		Location	aLocation = mWorld.getRandomEmptyLocation();
 		
 		mWorld.place(aLocation, aTree);
 		
 		try
 		{
 			mWorld.place(aLocation, anotherTree);
 			fail("testCollision exception");
 		}
 		catch(ThingCollisionException e)
 		{
 		}
 	}
 	
 	/**
 	 * Tests whether you can find a tree that ain't there.
 	 */
 	public void testRetrieval() throws ThingCollisionException
 	{
 		Tree		aTree = new Tree();
 		
 		try
 		{
 			mWorld.getLocation(aTree);
 			fail("testRetrieval exception");
 		}
 		catch(JCavernInternalError e)
 		{
 		}
 	}
 	
	public void testBounds()
	{
		assert("5, -1 out", mWorld.inBounds(new Location(5, -1)) == false);
		assert("-1, 5 out", mWorld.inBounds(new Location(-1, 5)) == false);
		
		assert("5, 0 in", mWorld.inBounds(new Location(5, 0)) == true);
		assert("0, 5 in", mWorld.inBounds(new Location(0, 5)) == true);
		
		assert("5, 20 out", mWorld.inBounds(new Location(5, 20)) == false);
		assert("20, 5 out", mWorld.inBounds(new Location(20, 5)) == false);
		
		assert("5, 19 in", mWorld.inBounds(new Location(5, 19)) == true);
		assert("19, 5 in", mWorld.inBounds(new Location(19, 5)) == true);

	}
	
 	public void testGetThings()
 	{
 		assert("found something", mWorld.getThings().size() > 0);
 		assert("found something", mWorld.getThings().contains(mPlayer));
 	}
 
 	/**
 	 * Tests whether you can remove a tree that ain't there.
 	 */
 	public void testBogusRemoval() throws ThingCollisionException
 	{
 		Tree		aTree = new Tree();
 		
 		try
 		{
 			mWorld.remove(aTree);
 			fail("testBogusRemoval exception");
 		}
 		catch(JCavernInternalError e)
 		{
 		}
 	}
 }
