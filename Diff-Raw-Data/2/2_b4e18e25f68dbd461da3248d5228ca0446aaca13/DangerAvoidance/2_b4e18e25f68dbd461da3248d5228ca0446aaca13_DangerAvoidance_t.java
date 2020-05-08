 package isnork.g6;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Set;
 
 public class DangerAvoidance {
 
 	public boolean isLocationDangerous(Set<Observation> whatISee, Point2D pos) {
 		Iterator<Observation> itr = whatISee.iterator();
 		while (itr.hasNext()) {
 			Observation o = itr.next();
 			if (o.isDangerous()) {
 				if (tilesAway(pos, o.getLocation()) <= 2) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public LinkedList<Direction> bestDirections(Set<Observation> whatISee, Direction d, Point2D currentPosition) {
 		LinkedList<Direction> newL = new LinkedList<Direction>();
 		ArrayList<Direction> directionOptions = Direction.allBut(d);
 		Direction bestDirection = null;
 		Point2D bestPoint = null;
 		for (Direction nextD : directionOptions) {
 			double newPosX = currentPosition.getX() + nextD.getDx();
 			double newPosY = currentPosition.getY() + nextD.getDy();
 			Point2D newPoint = new Point2D.Double(newPosX, newPosY);
 			if (!atTheWall(newPoint) && !isLocationDangerous(whatISee, newPoint)){
 				if (bestPoint == null){
 					bestPoint = newPoint;
 					bestDirection = nextD;
 				}
 				else{
 					if (tilesAway(currentPosition, newPoint) < tilesAway(currentPosition, bestPoint)){
 						bestPoint = newPoint;
 						bestDirection = nextD;
 					}
 				}
 			}
 		}
 		if (bestDirection == null){
 			Random r = new Random();
			Direction randomDirection = directionOptions.get(r.nextInt(Direction.values().length-1));
 			newL.add(randomDirection);
 			double newPosX = currentPosition.getX() + randomDirection.getDx();
 			double newPosY = currentPosition.getY() + randomDirection.getDy();
 			Point2D randomPoint = new Point2D.Double(newPosX, newPosY);
 			LinkedList<Direction> temp = bestDirections(whatISee, randomDirection, randomPoint);
 			for (Direction tmpD: temp){
 				newL.add(tmpD);
 			}
 			return newL;
 		}
 		else{
 			newL.add(bestDirection);
 			return newL;
 		}
 	}
 
 	public int tilesAway(Point2D me, Point2D them) {
 		return ((int)PathManager.computeTotalSpaces(me, them));
 	}
 
 	public static boolean atBoat(Point2D p) {
 		if (p.getX() == 0 && p.getY() == 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean atTheWall(Point2D p) {
 		if (Math.abs(p.getX()) == NewPlayer.d || Math.abs(p.getY()) == NewPlayer.d) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
