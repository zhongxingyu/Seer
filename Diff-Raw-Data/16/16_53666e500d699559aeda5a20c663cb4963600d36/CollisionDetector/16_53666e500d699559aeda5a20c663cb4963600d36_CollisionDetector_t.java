 package game;
 
 import java.awt.Rectangle;
 import java.util.List;
 
 public class CollisionDetector {
 
 	/**
 	 * Checks for a collision between two sets of lines
 	 * @param set1
 	 * @param set2
 	 * @return Whether a collision was detected
 	 */
 	public static boolean collision(List<CollisionLine> set1, List<CollisionLine> set2) {
 		for (CollisionLine firstLine : set1) {
 			for (CollisionLine secondLine : set2) {
 				if (collision(firstLine, secondLine)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static boolean collision(Rectangle rect, List<CollisionLine> lines) {
 		for (CollisionLine line : lines) {
 			if (rect.intersectsLine(line.x1, line.y1, line.x2, line.y2)) {
				System.out.println(rect.x + " " + rect.y);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static boolean collision(CollisionLine line1, CollisionLine line2) {
 		/*
 		float px = computeDeterminant(
 				computeDeterminant(line1.x1, line1.y1, line1.x2, line1.y2),
 				computeDeterminant(line1.x2, 1, line1.x2, 1),
 				computeDeterminant(line2.x1, line2.y1, line2.x2, line2.y2),
 				computeDeterminant(line2.x1, 1, line2.x2, 1))/
 				computeDeterminant(
 				computeDeterminant(x1)) */
 		float x1 = line1.x1;
 		float x2 = line1.x2;
 		float x3 = line2.x1;
 		float x4 = line2.x2;
 		float y1 = line1.y1;
 		float y2 = line1.y2;
 		float y3 = line2.y1;
 		float y4 = line2.y2;
 		
 		float px = ((x1*y1-y1*x2)*(x3-x4) - (x1-x2)*(x3*y4-y3*x4)) / 
 				((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
 		float py = ((x1*y2-y1*x2)*(y3-y4) - (y1-y2)*(x3*y4-y3*x4)) /
 				((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
 		return checkOnLine(px, py, line1) && checkOnLine(px, py, line2);
 	}
 		
 	private static boolean checkOnLine(float x, float y, CollisionLine line) {
 		return (x >= Math.min(line.x1,line.x2) && x <= Math.max(line.x1, line.x2) 
 				&& y >= Math.min(line.y1, line.y2) && y <= Math.max(line.y1, line.y2));
 	}
 }
