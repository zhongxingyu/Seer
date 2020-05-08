 /*
  *  @author Intexon
  */
 package core.Unit;
 
 import core.Map.Map;
 import java.awt.Point;
 import org.newdawn.slick.util.pathfinding.Path;
 import org.newdawn.slick.util.pathfinding.Path.Step;
 
 /**
  *
  * @author Intexon
  */
 public class Unit
 {
   protected int hitpoints;
   protected int speed = 1;
   
   private int lastUpdate = 0;
  private int updateInterval = 100; // ms
   
   private Path currentPath = null;
   private int index = 0;
   
   private Point coords;
   
   public Unit(Point coords) {
     this.coords = new Point(coords.x, coords.y);
   }
   
   public Point getCoords() {
     return new Point(coords);
   }
   
   public String getTexturePath() {
     return "data/unit/test.png";
   }
   
   public void update(Map m, int delta) {
     lastUpdate += delta;
     if (lastUpdate > updateInterval) {
       if (currentPath == null) {
         return;
       }
       
       if (index < currentPath.getLength()) {
         //double angle = Math.atan2(currentPath.getY(index) - coords.y, currentPath.getX(index) - coords.x);
         coords.setLocation(currentPath.getX(index), currentPath.getY(index));
         ++index;
         
       } else {
         currentPath = null;
         index = 0;
       }
       
       lastUpdate = 0;
     }
   }
 
   public boolean followsPath()
   {
     return currentPath != null;
   }
 
   public void followPath(Path path)
   {
     currentPath = path;
   }
 }
