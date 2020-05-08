 package balle.world.objects;
 
 import balle.world.Coord;
 import balle.world.Line;
 
 public class Goal implements StaticFieldObject {
 
     private final boolean leftGoal;
     private final double  minX, maxX, minY, maxY;
 
     public double getMinX() {
         return minX;
     }
 
     public double getMaxX() {
         return maxX;
     }
 
     public double getMinY() {
         return minY;
     }
 
     public double getMaxY() {
         return maxY;
     }
     
     public boolean isLeftGoal() {
     	return leftGoal;
     }
     
     public boolean isRightGoal() {
     	return !leftGoal;
     }
 
     public Goal(boolean leftGoal, double minX, double maxX, double minY,
             double maxY) {
         this.leftGoal = leftGoal;
         this.minX = minX;
         this.maxX = maxX;
         this.minY = minY;
         this.maxY = maxY;
     }
 
     @Override
     public Coord getPosition() {
		return new Coord((minX + maxX) / 2, (minY + maxY) / 2);
     }
 
     @Override
     public boolean containsCoord(Coord point) {
         if (point.getX() > maxX)
             return false;
         if (point.getX() < minX)
             return false;
         if (point.getY() > maxY)
             return false;
         if (point.getY() < minY)
             return false;
         return true;
     }
 
     /**
      * Gets the coordinate of the left goal post.
      * 
      * @return the left goal post coord
      */
     public Coord getLeftPostCoord() {
         if (leftGoal)
             return new Coord(minX, maxY);
         else
             return new Coord(maxX, minY);
     }
 
     /**
      * Gets the coordinate of the right goal post.
      * 
      * @return the right goal post coord
      */
     public Coord getRightPostCoord() {
         if (leftGoal)
             return new Coord(minX, minY);
         else
             return new Coord(maxX, maxY);
     }
 
     /**
      * Returns the Goal Line
      * 
      * @return
      */
     public Line getGoalLine() {
         return new Line(getLeftPostCoord(), getRightPostCoord());
     }
 
     @Override
 	public boolean intersects(Line line) {
 		if (containsCoord(line.getA()) || containsCoord(line.getB())) return true;
 		
 		double lMinX, lMaxX, lMinY, lMaxY;
 		lMinX = Math.min(line.getA().getX(), line.getB().getX());
 		lMaxX = Math.max(line.getA().getX(), line.getB().getX());
 		lMinY = Math.min(line.getA().getY(), line.getB().getY());
 		lMaxY = Math.max(line.getA().getY(), line.getB().getY());
 		
 		return lMinX < minX && maxX < lMaxX && lMinY < minY && maxY < lMaxY;
 	}
     
 }
