 package ProcessingKinect;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 import ActiveSpace.Coord;
 import SimpleOpenNI.*;
 
 /**
  * simple abstract wrapper class for a kinect
  * 
  * @author markk
  */
 public class KinectSensor {
 
 	private SimpleOpenNI kinect;
 
 	public KinectSensor(PApplet pa) {
 		kinect = new SimpleOpenNI(pa);
 		kinect.enableDepth();
 		kinect.enableUser(SimpleOpenNI.SKEL_PROFILE_NONE); 
		kinect = null;
 	}
 
 	/**
 	 * query the kinect for the location of the first/only player
 	 * 
 	 * @return (Coord) position of player
 	 */
 	public Coord getPlayerLocation() {
 		return( getPlayerLocation(0));
 	}
 
 	/**
 	 * query the kinect for the location of a specified player
 	 * 
 	 * @param player	(integer) player identifier
 	 * @return (Coord) position of a specified player
 	 */
 	public Coord getPlayerLocation(int player) {
 		
 		// update and read in the user list from the kinect
 		kinect.update();
 		IntVector userList = new IntVector();
 		kinect.getUsers(userList);

 		// get the current position of the specified user
 		if (userList.size() <= player) 
 			return null;  
 		int userId = userList.get(player);
 		PVector position = new PVector();
 		kinect.getCoM(userId, position);	// real world, lens-relative mm
 		
 		// kinect sometimes reports users it can no longer see
 		if (position.z > 0) 
 			return new Coord(position.x, position.y, position.z);
 		else
 			return null;
 	}
 
 	/**
 	 * return the number of players currently tracked by the kinect
 	 * 
 	 * @return	(integer) number of players
 	 */
 	public int getNumberOfPlayers() {
 		kinect.update();
 		IntVector userList = new IntVector();
 		kinect.getUsers(userList);
 		return (int) (userList.size());
 	}	
 }
