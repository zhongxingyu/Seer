 package mecanique;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 
 
 public class Spring extends RubberBand {
     protected static final double RIGOR = 0.8666;
     
     public Spring(PhysicalObject l, PhysicalObject r) {
         super(l, r);
     }
     
     @Override
     protected Color getTenseColor() {
         return Color.PINK;
     }
     
     @Override
     protected Color getNormalColor() {
         return Color.GRAY;
     }
     
     
     @Override
     public Point2D getAcceleration(PhysicalObject object) {
         double d = distance();
         double f = Math.abs(RIGOR * (d - LENGTH));
         double theta = Math.atan2(getY2() - getY1(), getX2() - getX1());
        int direction = object == left_object ? 1 : -1;
         int invert = d < LENGTH ? -1 : 1;
         
         return new Point2D.Double(
             invert * direction * f * Math.cos(theta),
             invert * direction * f * Math.sin(theta)
         );
     }
     
     
     public static class Placement extends RubberBand.Placement {
         protected Placement(SystemDisplay panneau) {
             super(panneau);
         }
         
         @Override
         protected RubberBand getInstance(PhysicalObject left_po) {
             return new Spring(left_po, null);
         }
         
         @Override
         public String getName() {
             return "Ressort";
         }
     }
 }
