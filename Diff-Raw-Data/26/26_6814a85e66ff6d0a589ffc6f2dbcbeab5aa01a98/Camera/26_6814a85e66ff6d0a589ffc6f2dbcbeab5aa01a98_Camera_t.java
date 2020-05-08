 import java.awt.Point;
 
 
 /*
  * camera class
  */
 public class Camera {
 	
 	public Point position;
 	public int viewWidth, viewHeight;
 	
 	public Camera (Point camPos, Point viewSize){
 		position = camPos;
 		viewWidth = viewSize.x;
 		viewHeight = viewSize.y;
 		
 	}
 	
 	public boolean isInView(Point pos){
 		return isInView(pos, new Point(0,0));
 	}
 	
 	public boolean isInView(Point pos, Point size){
 		if(pos.x > position.x - size.x && pos.x + size.x < position.x + viewWidth + size.x){
 			if(pos.y > position.y - size.y && pos.y + size.y < position.y + viewHeight + size.y){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	//convert a world pos to screen pos
 	public Point toScreenPosition(Point p){
 		Point ret = new Point(0,0);
		ret.x =-( ( position.x ) - p.x);
 		ret.y = ( position.y ) - p.y;
 		return ret;
 	}
 
 	public Point toWorldPosition(Point p){
 		return null;
 	}
 	
 }
