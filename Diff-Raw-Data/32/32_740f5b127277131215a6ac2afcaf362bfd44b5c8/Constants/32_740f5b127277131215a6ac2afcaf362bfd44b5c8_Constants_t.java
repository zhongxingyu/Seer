 package edu.brown.cs32.goingrogue.constants;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.CreatureStats;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class Constants {  
 	// An ideal framerate -- Slick may use a smaller one if lag occurs
 //	public static final int FRAMERATE=2; //TEST
 	
 	public static final int TILE_SIZE=40;
     
     // starting player stats
 	public static final CreatureStats STARTING_PLAYER_STATS
 		= new CreatureStats(500, 250, 100, 50, .01);
 	
 	//Default image rate for loaded animations
 	public static final int DEFAULT_IMAGE_RATE=5;
 	
	private static long uid = 0;
	public static long getUID(){
		long rv = uid;
 		uid++;
 		return rv;
 	}
 }
