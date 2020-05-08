 package gestures.filters;
 
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 
 import entities.ships.Ship;
 import game.Variables;
 
 
 
 public class Backoff implements Filter {
 	
 	/**
 	 * The vertical bound tilt angle that the drift refuse (180 +- this variable)
 	 */
 	public static final int TRACE_DRIFT_BORNES_BOT = 10;/* We refuse affine courbe which increase perpendiculary, with this bornes*/
 	
 	/**
 	 * Check if the angle of the Right is correct for backoff
 	 * @param angle
 	 * @return true if the angle is correct
 	 */
 	public boolean checkAngle(double angle){
 		System.out.println(angle);
 		if((angle >= 270-TRACE_DRIFT_BORNES_BOT && angle <= 270+TRACE_DRIFT_BORNES_BOT ))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Check if a list of Vec2 is correctly set, with the angle calcul (Point by point)
 	 */
 	@Override
 	public boolean check(List<Vec2> trace){
 		if(!trace.isEmpty()){
 			double angle = Filters.getAngle(trace);
 			if(Filters.isAffine(trace) && checkAngle(angle))
 				return true;
 			return false;
 		}
 		return false;
 	}
 
 	/**
 	 * Move the ship in the good direction, the bottom
 	 */
 	@Override
 	public void apply(Ship ship) {
		ship.setVelocity(0, -Variables.SPEED_MAIN_SHIP);
 		System.out.println("Backup OK");
 		
 	}
 	
 }
