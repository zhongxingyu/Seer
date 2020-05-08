 package game.open2d;
 
 import android.util.Log;
 import object.GameObject;
 
 public class GameTools {
 	public static enum Gesture {
 		UP,
 		UP_RIGHT,
 		RIGHT,
 		DOWN_RIGHT,
 		DOWN,
 		DOWN_LEFT,
 		LEFT,
 		UP_LEFT
 	}
 	
 	public static final int SWIPE_LEFT = 30;
 	public static final int SWIPE_RIGHT = -30;
 	public static final int SWIPE_UP = 30;
 	public static final int SWIPE_DOWN = -30;
 	
 	public static boolean boxColDetect(GameObject object1, GameObject object2){
 		float left1 = object1.getX();
 		float right1 = object1.getX() + object1.getWidth();
 		float top1 = object1.getY() + object1.getHeight();
 		float bottom1 = object1.getY();
 		
 		float left2 = object2.getX();
 		float right2 = object2.getX() + object2.getWidth();
 		float top2 = object2.getY() + object2.getHeight();
 		float bottom2 = object2.getY();
 		
 		if (bottom1 > top2) return false;
 		if (top1 < bottom2) return false;
 		if (right1 < left2) return false;
 		if (left1 > right2) return false;
 		
 		return true;
 	}
 	
 	public static boolean boxColDetect(GameObject object1, GameObject object2, float buffer){
 		float left1 = object1.getX() + buffer;
 		float right1 = object1.getX() + object1.getWidth() - buffer;
 		float top1 = object1.getY() + object1.getHeight() - buffer;
 		float bottom1 = object1.getY() + buffer;
 		
 		float left2 = object2.getX() + buffer;
 		float right2 = object2.getX() + object2.getWidth() - buffer;
 		float top2 = object2.getY() + object2.getHeight() - buffer;
 		float bottom2 = object2.getY() + buffer;
 		
 		if (bottom1 > top2) return false;
 		if (top1 < bottom2) return false;
 		if (right1 < left2) return false;
 		if (left1 > right2) return false;
 		
 		return true;
 	}
 	
 	public static Gesture gestureDetection(float xDiff, float yDiff){
 		Gesture horizontal = gestureDetectionHorizontal(xDiff);
 		Gesture vertical = gestureDetectionVertical(yDiff);
 		
 		if(horizontal == Gesture.RIGHT && vertical == Gesture.UP)
 			return Gesture.UP_RIGHT;
 		else if(horizontal == Gesture.RIGHT && vertical == Gesture.DOWN)
 			return Gesture.DOWN_RIGHT;
 		else if(horizontal == Gesture.LEFT&& vertical == Gesture.UP)
 			return Gesture.UP_LEFT;
 		else if(horizontal == Gesture.LEFT&& vertical == Gesture.DOWN)
 			return Gesture.DOWN_LEFT;
 		
 		if(horizontal == null)
 			return vertical;
 		if(vertical == null)
 			return horizontal;
 		
 		return null;
 	}
 	
 	public static Gesture gestureDetectionHorizontal(float xDiff){
 		if(xDiff > SWIPE_LEFT){
 			return Gesture.LEFT;
 		} else if(xDiff < SWIPE_RIGHT){
 			return Gesture.RIGHT;
 		}
 		
 		return null;
 	}
 	
 	public static Gesture gestureDetectionVertical(float yDiff){
 		if(yDiff > SWIPE_UP){
 			return Gesture.UP;
 		} else if(yDiff < SWIPE_DOWN){
 			return Gesture.DOWN;
 		}
 		
 		return null;
 	}
 	
 	public static Gesture gestureBreakdownHorizontal(Gesture gesture){
 		if(gesture == Gesture.RIGHT || gesture == Gesture.UP_RIGHT || gesture == Gesture.DOWN_RIGHT)
 			return Gesture.RIGHT;
 		else if(gesture == Gesture.LEFT || gesture == Gesture.UP_LEFT || gesture == Gesture.DOWN_LEFT)
 			return Gesture.LEFT;
 
 		return null;
 	}
 	
 	public static Gesture gestureBreakdownVertical(Gesture gesture){
 		if(gesture == Gesture.UP || gesture == Gesture.UP_RIGHT || gesture == Gesture.UP_LEFT)
 			return Gesture.UP;
		else if(gesture == Gesture.DOWN || gesture == Gesture.DOWN_LEFT || gesture == Gesture.DOWN_LEFT)
 			return Gesture.DOWN;
 
 		return null;
 	}
 }
