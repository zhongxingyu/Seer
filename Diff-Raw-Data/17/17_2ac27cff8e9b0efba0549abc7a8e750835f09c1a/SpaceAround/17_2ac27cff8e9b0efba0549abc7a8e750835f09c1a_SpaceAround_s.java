 package interiores.business.models;
 
 import java.awt.Rectangle;
 
 /**
  *
  * @author hector
  */
 public class SpaceAround
 {
     private int[] passiveOffsets;
     
     public SpaceAround(int north, int east, int south, int west) {
         passiveOffsets = new int[]{ north, east, south, west };
     }
     
     public Rectangle getWholeArea(OrientedRectangle area) {
         int o = area.getOrientation().ordinal();
         
         return new Rectangle(
                 area.x - passiveOffsets[3-o],
                 area.y - passiveOffsets[(4-o) % 4],
                 area.width + passiveOffsets[(3+o) % 4] + passiveOffsets[(1+o) % 4],
                 area.height + passiveOffsets[(2+o) % 4] + passiveOffsets[o]
                 );
     }
     
     @Override
     public String toString() {
         return "N: " + passiveOffsets[0] + ", E: " + passiveOffsets[1] + ", S: " + 
                 passiveOffsets[2] + ", W: " + passiveOffsets[3];
     }
 }
