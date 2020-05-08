 /*
  *  @author Intexon
  */
 package core.Tile;
 
 import core.Field;
 import core.Unit.Unit;
 import java.awt.Point;
 import org.newdawn.slick.GameContainer;
 
 /**
  *
  * @author Intexon
  */
 public class Spawn extends Tile
 {
   private int spawningInterval = 1000; // ms
   private int lastUpdate = 0;
   
   private Point coords;
   
   public Spawn(Point coords) {
     this.coords = coords;
   }
   
   @Override
   public String getTexturePath()
   {
     return "data/tile/spawn.jpg";
   }
 
   @Override
   public void update(Field field, GameContainer c, int delta)
   {
     if (field.getCurrentState() == Field.STATE_SPAWNING) {
       lastUpdate += delta;
       if (lastUpdate > spawningInterval) {
         lastUpdate = 0;
         try {
          field.addUnit(new Unit(coords));
         } catch (Exception e) {
           System.out.println(e.getMessage());
         }
       }
     }
   }
 }
