 import javax.media.opengl.GL;
 
 /**
  * This class makes a shape that is somewhat like a square prism.
  * The faces all look like rectangles in the xz plane,
  * but when viewed otherwise, the top face is slightly uneven.
  *
  * This is used to show a small volume element between the xz plane
  * and a surface.
  *
  * @author Brian Mock
  */
 public class Prismoid {
     private Rect bottom;
     private Rect top;
     private Rect side1;
     private Rect side2;
     private Rect side3;
     private Rect side4;
 
     private float[][] topPoints;
     private float[][] botPoints;
 
     public
     Prismoid(Rect top) {
         this.top = top;
 
         topPoints = top.getVertices();
 
         // The bottom is merely the top dropped into the xz plane
         bottom = new Rect(
             Util.zeroOutY(topPoints[0]),
             Util.zeroOutY(topPoints[1]),
             Util.zeroOutY(topPoints[2]),
             Util.zeroOutY(topPoints[3])
         );
 
         botPoints = bottom.getVertices();
 
         side1 = makeRect(0, 1);
         side2 = makeRect(1, 2);
         side3 = makeRect(2, 3);
         side4 = makeRect(3, 0);
     }
 
     private Rect
     makeRect(int i, int j) {
         return new Rect(
            topPoints[i], topPoints[j],
            botPoints[i], botPoints[j]
         );
     }
 
     public void
     draw(GL gl) {
         Rect[] rects = {
             bottom, top,
             side1, side2,
             side3, side4
         };
 
         for (Rect rect: rects) {
             rect.draw(gl);
         }
     }
 }
