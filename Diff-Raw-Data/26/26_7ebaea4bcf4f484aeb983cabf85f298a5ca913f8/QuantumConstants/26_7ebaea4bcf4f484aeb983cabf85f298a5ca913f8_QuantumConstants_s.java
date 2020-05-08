 package newTeam.common;
 
 /**
  * Class for keeping adjustable constants for gameplay.  
  */
 import battlecode.common.*;
 
 public final class QuantumConstants {
     
 	/** Total flux needed to explore for more mines */
 	public static final int TOTAL_FLUX_EXPLORE = 120;
 	/** Number of locations to remember for bug navigation */
 	public static final int BUG_MEMORY_LENGTH = 15;
 	/** Bug distance increase before trying different path */
 	public static final double BUG_DISTANCE_SWITCH = 1.2;
 	/** Farthest sensor distance... assumed to be the satellite **/
 	public static final int FARTHEST_SENSOR_DISTANCE = ComponentType.SATELLITE.range;
 	/** Size of map location hashes used **/
 	// Because robots can sense off the map in both directions, the hash must be big enough
 	//   to handle that.
 	public static final int LOCATION_HASH_SIZE = GameConstants.MAP_MAX_WIDTH +
 	                                             2 * FARTHEST_SENSOR_DISTANCE;
 	/** Number of rounds to navigate before setting a new exploration goal */
 	public static final int EXPLORE_TIME = 50;
 	/** Distance to exploration goal */
 	public static final int EXLPORE_GOAL_DISTANCE = 15;
 
         /** Number of soldiers per constructor */
 	public static final int SOLDIERS_PER_CONSTRUCTOR = 4;
 	/** Big integer */
 	public static final int BIG_INT = 65536;
 	/** Whether or not we attack debris */
 	public static final boolean ATTACK_DEBRIS = false;
 	
 	/** Max conceivable number of robots on the map */
 	public static final int MAX_TOTAL_NUMBER_OF_ROBOTS = 2000;
 	/** Max conceivable number of robots sensed */
 	public static final int MAX_NUMBER_OF_SENSED_THINGS = 400;
 	
 	/** Planck's constant */
 	public static final double H = 6.626069E-34;
 	/** Reduced Planck's constant */
 	public static final double H_BAR = 1.0545716E-34;
 	
 	private QuantumConstants() {	
 	}
 }
