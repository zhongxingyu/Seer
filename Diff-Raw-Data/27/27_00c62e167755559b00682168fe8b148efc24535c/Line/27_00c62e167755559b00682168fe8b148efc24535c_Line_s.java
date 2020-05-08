 package il.ac.idc.jdt.extra.constraint.datamodel;
 
 import com.google.common.base.Objects;
 import il.ac.idc.jdt.Point;
 
 import java.util.Arrays;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static java.lang.String.format;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Innak
  * Date: 6/1/12
  * Time: 7:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Line {
 
     private final Point p1;
     private final Point p2;
 
     public Line(Point p1, Point p2) {
         checkNotNull(p1, "p1 cannot be null");
         checkNotNull(p2, "p2 cannot be null");
         checkArgument(!p1.equals(p2), "both points cannot be the same");
 
         this.p1 = p1;
         this.p2 = p2;
     }
 
     public Point getP1() {
         return p1;
     }
 
     public Point getP2() {
         return p2;
     }
 
     @Override
     public boolean equals(Object o)
     {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Line line = (Line) o;
 
         return Arrays.equals(points(), line.points());
     }
 
     public Point[] points()
     {
         Point[] ordered = new Point[2];
 
         if (p1.getX() < p2.getX())
         {
             ordered[0] = p1; ordered[1] = p2;
         }
         else if (p1.getX() == p2.getX())
         {
             ordered[0] = p1.getY() < p2.getY() ? p1 : p2;
         }
         else
         {
             ordered[0] = p2; ordered[1] = p1;
         }
 
         return ordered;
     }
 
     @Override
     public int hashCode()
     {
         return Objects.hashCode(points());
     }
 
 
     @Override
     public String toString()
     {
         return format("[%s:%s - %s:%s]", p1.getX(), p1.getY(), p2.getX(), p2.getY());
     }
 
     public boolean isConnectedToLine(Point p11, Point p22) {
         if (p1.equals(p11) || p1.equals(p22)) {
             return true;
         }
 
         if (p2.equals(p11) || p2.equals(p22)) {
             return true;
         }
 
         return false;
     }
 }
