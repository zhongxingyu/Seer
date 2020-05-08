 package vooga.rts.map;
 
 import java.util.ArrayList;
 import java.util.List;
 import vooga.rts.gamedesign.sprite.map.Terrain;
 public class MapNode {
     
     private static final int DEFAULT_VALUE = 1;
     
     private List<Terrain> myTerrain;
         private int myHeight;
         /* X and Y are relative to the width of the map as defined by the number of 
          * nodes.
         */
          private int myX;
          private int myY;
     
     public MapNode(int x, int y) {
         myX = x;
         myY = y;
         myTerrain = DEFAULT_VALUE;
     }
     
     public int getX() {
         return myX;
     }
     
     public int getY() {
         return myY;
     }
     
     public void add (Terrain terrain) {
         myTerrain.add(terrain);
         if (terrain.getLevel() > myHeight) {
             myHeight = terrain.getLevel();
         }
     }
 }
