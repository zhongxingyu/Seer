 package gestures.filters;
 
 import entities.ships.Ship;
 import game.Variables;
 
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 
 
 public class Drift implements Filter{
 	/**
 	 * The vertical bound tilt angle that the drift refuse (90 +- this variable)
 	 */
 	public static final int TRACE_DRIFT_BORNES_TOP = 10;
 	/**
 	 * The horizontal bound tilt angle that the drift refuse(0 + this variable and 180 - this variable)
 	 */
 	public static final int TRACE_DRIFT_BORNES_BOT = 10;
 	
 	private double angle=0;
 	
 	/**
 	 * Check if the angle of the Right is correctly for a left or right drift, but no vertical or horizontal courbe
 	 * @param angle
 	 * @return true if the angle is correct
 	 */
 	public boolean checkAngle(double angle){
 		if((angle >= 90-TRACE_DRIFT_BORNES_TOP && angle <= 90+TRACE_DRIFT_BORNES_TOP )||
 				angle -TRACE_DRIFT_BORNES_BOT <= 0 || angle + TRACE_DRIFT_BORNES_BOT >= 180 )
 			return false;
 		return true;
 	}
 
 	/**
 	 * Check if a list of Vec2 is correctly set, with the angle calcul (Point by point)
 	 */
 	@Override
 	public boolean check(List<Vec2> trace){
 		if(!trace.isEmpty()){
 			angle = Filters.getAngle(trace);
 			if(Filters.isAffine(trace) && checkAngle(angle))
 				return true;
 			return false;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Move the ship in the good direction, depending on the angle of drift
 	 */
 	@Override
 	public void apply(Ship ship) {
 		int vitX;
 		if(angle < 90)
 			vitX = Variables.SPEED_MAIN_SHIP;
 		else
 			vitX = -Variables.SPEED_MAIN_SHIP;
		ship.setVelocity(vitX, Variables.SPEED_MAIN_SHIP);		
 	}
 }
