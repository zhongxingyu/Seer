 package fr.umlv.escape.gesture;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.jbox2d.common.Vec2;
 
 import android.graphics.Point;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.gesture.GestureDetector.GestureType;
 import fr.umlv.escape.ship.Ship;
import fr.umlv.escape.world.EscapeWorld;
 
 public class Horizontal implements Gesture{
 	private final int MARGIN_ERROR = 30;		// margin error for the back off gesture
 	private final int MIN_NUMBER_POINT = 3;
 	private Vec2 force;
 	
 	@Override
 	public boolean isRecognized(ArrayList<Point> pointList) {
 		Point firstPoint;
 		Point previous;
 
 		if(pointList.size()<MIN_NUMBER_POINT){
 			return false;
 		}
 		
 		previous=pointList.get(0);
 		firstPoint=previous;
 		int numOfPoint=1;
 		Point tmp;
 		for(int i=1;i<pointList.size() && i<10;++i){
 			tmp=pointList.get(i);
 			if(tmp.y<(firstPoint.y-MARGIN_ERROR)		||
 			   tmp.y>(firstPoint.y+MARGIN_ERROR)){
 				return false;
 			}
 			previous=tmp;
 			numOfPoint++;
 		}
		force = new Vec2((previous.x-firstPoint.x)/EscapeWorld.SCALE, 0);
 		return true;
 	}
 
 	@Override
 	public void apply(Ship ship) {
 		if(ship.getCurrentWeapon().getLoadingBullet() == null){
 			ship.move(force);
 			ship.setImage(FrontApplication.frontImage.getImage("default_ship_player"));
 		}
 	}
 
 }
