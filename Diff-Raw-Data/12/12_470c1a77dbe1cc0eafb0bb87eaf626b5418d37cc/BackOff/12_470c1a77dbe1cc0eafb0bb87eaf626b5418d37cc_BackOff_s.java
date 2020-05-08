 package fr.umlv.escape.gesture;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.game.Player;
 import fr.umlv.escape.gesture.GestureDetector.GestureType;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.world.EscapeWorld;
 
 import android.graphics.Point;
 
 public class BackOff implements Gesture {
 	private final int MARGIN_ERROR = 30;		// margin error for the back off gesture
 	private final int MIN_NUMBER_POINT = 3;
 	private Vec2 force;
 	
 	/** Detect if a point list represent a back off.
 	 *  
 	 * @param pointList the list of point used to detect the gesture.
 	 * @return True if the gesture is recognized else false.
 	 */
 	@Override
 	public boolean isRecognized(ArrayList<Point> pointList) {
 		Point firstPoint;
 		Point previous;
 		
 		if(pointList.size()<MIN_NUMBER_POINT){
 			return false;
 		}
 	
 		previous=pointList.get(0);
 		firstPoint=previous;
 
 		Point tmp;
 		for(int i=1; i<pointList.size();++i){
 			tmp=pointList.get(i);
 
 			if(previous.y > tmp.y 				    ||
 			   tmp.x<(firstPoint.x-(MARGIN_ERROR))	||
 			   tmp.x>(firstPoint.x+MARGIN_ERROR)){
 				return false;
 			}
 			previous=tmp;
 		}
 
		force.set(0, (previous.y-firstPoint.y)/EscapeWorld.SCALE);
 		return true;
 	}
 
 	@Override
 	public void apply(Ship playerShip) {
 		if(playerShip.getCurrentWeapon().getLoadingBullet() == null){
 			playerShip.move(force);
 			playerShip.setImage(FrontApplication.frontImage.getImage("default_ship_player"));
 		}
 	}
 }
