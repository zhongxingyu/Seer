 package dinaBOT.navigation;
 
 import dinaBOT.mech.*;
 
 import lejos.nxt.*;
 
 
 public class Navigator implements Navigation, MechConstants {
 	
 	boolean interrupted;
 	
 	Pathing pather;
 	Movement movement;
 	Map map;
 	Odometer odometer;
 	
 	int[] position;
 		
 	double[][] path;
 	int node;	//the current location in the path array
 	
 	public Navigator(Pathing pather, Movement movement, Map map, Odometer odometer) {
 		this.pather = pather;
 		this.movement = movement;
 		this.map = map;
 		this.odometer = odometer;
 				
 		map.registerListener(this);
 	}
 	
 	public boolean goTo(double x, double y) {
 			double[] position = new double[3];
 			
 			position = odometer.getPosition();
 			
 			path = pather.generatePath(position[0], position[1], position[2], x, y);
 
			for(node = 0; (path != null) && (node < path.length); node++) {
 					
 					position = odometer.getPosition();
 
 					movement.goTo(path[node][0], path[node][1], SPEED_MED);
 										
 					if(interrupted) {
 						// get current position
 						position = odometer.getPosition();
 						// generate new path
 						path = pather.generatePath(position[0], position[1], position[2], x, y);
 						// reset position in path
 						node = 0;
 						// reset interrupt flag
 						interrupted = false;
 				}
			}
 				
			if (path == null){
 				LCD.drawString("no path", 0, 0);
 				return false;	//fail
 			}
 			
 		return true;	//success
 		
 	}
 
 	public void interrupt() {
 		interrupted = true;
 		movement.stop();
 	}
 	
 	public void newObstacle(int x, int y) {
 		if(path != null) {
 			for(int i = node ; i < path.length; i++) {
 				// check if new obstacle lies on current path
 				if( (path[i][0] == x*UNIT_TILE) && (path[i][1] == y*UNIT_TILE) ) {
 					//new obstacle in the way, stop all movement and set interrupt flag
 					synchronized(this) {
 						interrupted = true;
 						movement.stop();
 					}
 				}
 			}
 		}
 	}
 }
