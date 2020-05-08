 package fr.utbm.vi51.environment;
 
 import fr.utbm.vi51.util.Point3D;
 
 /**
  * @author Top-K
  *
  */
 public class Perception {
    private static final int perceptionSize = 10;
     private Square[][][] perceivedMap;
     private Point3D positionInPerceivedMap;
 
     protected Perception(Body body) {
         Square[][][] map = Environment.getInstance().getMap();
         Point3D pos = body.getPosition();
 
         //Ensure that the top left and bottom right points are not out of the global map
         Point3D perceptionTopLeft = new Point3D();
         perceptionTopLeft.z = pos.z;
         perceptionTopLeft.x = Math.max(pos.x - perceptionSize, 0);
         perceptionTopLeft.y = Math.max(pos.y - perceptionSize, 0);
 
         Point3D perceptionBottomRight = new Point3D();
         perceptionBottomRight.z = pos.z;
         perceptionBottomRight.x = Math.min(pos.x + perceptionSize, map.length - 1);
         perceptionBottomRight.y = Math.min(pos.y + perceptionSize, map[0].length - 1);
 
         //Get the perceived part of the global map
         perceivedMap = new Square[perceptionBottomRight.x - perceptionTopLeft.x
                 + 1][perceptionBottomRight.y - perceptionTopLeft.y + 1][1];
         for (int i = 0; i < perceptionBottomRight.x - perceptionTopLeft.x + 1; ++i) {
             for (int j = 0; j < perceptionBottomRight.y - perceptionTopLeft.y
                     + 1; ++j) {
                 perceivedMap[i][j][0] = map[perceptionTopLeft.x + i][perceptionTopLeft.y
                         + j][pos.z];
                 if (perceivedMap[i][j][0].getObjects().contains(body)) {
                     positionInPerceivedMap = new Point3D(i, j, 0);
                 }
             }
         }
         assert positionInPerceivedMap != null;
     }
 
     public Square[][][] getPerceivedMap() {
         return perceivedMap;
     }
 
     public Point3D getPositionInPerceivedMap() {
         return positionInPerceivedMap;
     }
 }
