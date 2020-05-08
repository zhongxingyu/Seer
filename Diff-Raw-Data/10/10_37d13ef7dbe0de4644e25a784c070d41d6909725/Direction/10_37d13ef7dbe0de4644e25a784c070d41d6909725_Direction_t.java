 package cz.sparko.Bugmaze.Helper;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public enum Direction {
     UP(0),
     RIGHT(1),
     DOWN(2),
     LEFT(3);
 
     private final int value;
 
     private Direction(final int newValue) {
         value = newValue;
     }
 
     public int getValue() { return value; }
 
     private static final Map<Integer, Direction> intToTypeMap = new HashMap<Integer, Direction>();
     static {
         for (Direction type : Direction.values())
             intToTypeMap.put(type.value, type);
     }
 
     public static Direction fromInt(int i) {
         Direction type = intToTypeMap.get(Integer.valueOf(i));
         if (type == null)
             return Direction.UP;
         return type;
     }
 
     public float getDegree() { return 90 * value; }
     public float getDegree(Direction that) {
         float degree = (this.getValue() - that.getValue()) * 90;
         if (degree > 180 || degree < -180)
             return 360 - degree;
         return degree;
     }
     public float getCornerWay(Direction that) {
        int diff = this.getValue() - that.getValue();
        if (diff == 1 || diff == -3)
             return 90;
         return -90;
     }
     private static final Coordinate[] directions = {new Coordinate(0, -1),  new Coordinate(1, 0), new Coordinate(0, 1), new Coordinate(-1, 0)};
     public Coordinate getCoordinate() { return directions[value]; }
 }
