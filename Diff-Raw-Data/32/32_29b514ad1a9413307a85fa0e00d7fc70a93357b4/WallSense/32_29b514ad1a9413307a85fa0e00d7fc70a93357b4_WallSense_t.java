 package jgpfun.world2d.senses;
 
 import jgpfun.life.SensorInput;
 import jgpfun.world2d.Body2d;
 import jgpfun.world2d.Organism2d;
 
 /**
  *
  * @author hansinator
  */
 public class WallSense implements SensorInput {
 
     private final double worldWidth, worldHeight;
 
     private final Body2d body;
 
     private int lastSenseVal = 0;
 
 
     public WallSense(int worldWidth, int worldHeight, Body2d body) {
         this.worldWidth = Math.floor(worldWidth);
         this.worldHeight = Math.floor(worldHeight);
         this.body = body;
     }
 
 
     public int sense() {
         double dir = body.dir, temp = 0.0;
         //clip to 2*PI range
        dir = dir - ((double) Math.round(dir / (2 * Math.PI)) * 2 * Math.PI);
 
        if ((body.x < 0) || (body.x >= worldWidth)) {
             //TODO: fix abs stuff
             temp = Math.min(Math.abs(2 * Math.PI - dir), Math.abs(Math.PI - dir));
            if ((body.y < 0) || (body.y >= worldHeight)) {
                temp = Math.min(temp, Math.min(Math.abs(0.5 * Math.PI - dir), Math.abs(1.5 * Math.PI - dir)));
            }
        } else if ((body.y < 0) || (body.y >= worldHeight)) {
            temp = Math.min(Math.abs(0.5 * Math.PI - dir), Math.abs(1.5 * Math.PI - dir));
         }
 
        lastSenseVal = (int) Math.round(temp * Organism2d.intScaleFactor);
         return lastSenseVal;
     }
 
 
     @Override
     public int get() {
         return lastSenseVal;
     }
 
 }
