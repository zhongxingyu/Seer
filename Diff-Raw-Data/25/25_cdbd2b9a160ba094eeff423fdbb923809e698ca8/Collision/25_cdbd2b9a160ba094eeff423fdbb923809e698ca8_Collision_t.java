 package com.liongrid.gameengine;
 
 import com.liongrid.gameengine.tools.Vector2;
 
 public class Collision {
 	private static final int CIRCLE = Shape.CIRCLE;
 	private static final int SQUARE = Shape.SQUARE;
 	public static boolean collides(Shape shape1, Shape shape2){
 		
 		if(shape1.getShape() == CIRCLE && shape2.getShape() == CIRCLE){
 			return collides((Shape.Circle) shape1, 
 							(Shape.Circle) shape2);
 		}
 		return false;
 	}
 	
 	public static float distance2(Shape shape1, Shape shape2){
 		if(shape1.getShape() == CIRCLE && shape2.getShape() == CIRCLE){
 			return distance2((Shape.Circle) shape1, 
 					(Shape.Circle) shape2);
 		}
 		return Float.NaN;
 	}
 	
 	private static boolean collides(Shape.Circle circle1, Shape.Circle circle2){
 		Vector2 pos1 = circle1.getPos();
 		Vector2 pos2 = circle2.getPos();
 		float distance = circle1.getRadius() + circle2.getRadius();
 		
 		if(pos1.distance2(pos2) < (distance)*(distance)) return true;
 		return false;
 	}
 	
 	private static float distance2(Shape.Circle circle1, Shape.Circle circle2){
 		Vector2 pos1 = circle1.getPos();
 		Vector2 pos2 = circle2.getPos();
		float r1 = circle1.getRadius();
		float r2 = circle2.getRadius();
		return pos1.distance2(pos2) - (r1+r2)*(r1+r2);
 	}
 }
