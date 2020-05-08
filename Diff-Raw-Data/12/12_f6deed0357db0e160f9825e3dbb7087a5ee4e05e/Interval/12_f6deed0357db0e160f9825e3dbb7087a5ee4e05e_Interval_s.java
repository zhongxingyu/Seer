 import java.awt.geom.Point2D;
 
 public class Interval {
     //normalized
     Point2D.Double startGraph;
     Point2D.Double endGraph;
 
     @Override public String toString() {
         return "Start: (" + startGraph.x + ", " + startGraph.y + ") End: (" + endGraph.x + ", " + endGraph.y + ")";
     }
 
     public Interval() {
         this.startGraph = null;
         this.endGraph = null;
     }
 
     public Interval(Interval another) {
         this.startGraph = new Point2D.Double(another.startGraph.x, another.startGraph.y);
         this.endGraph = new Point2D.Double(another.endGraph.x, another.endGraph.y);
     }
 
     public Interval(Point2D.Double start, Point2D.Double end) {
         this.startGraph = start;
         this.endGraph = end;
     }
 
     public Point2D.Double getPolygonStart(Point2D.Double[] poly) {
         double start;
         if (startGraph.x - endGraph.x == 0) {
             start = startGraph.y;
         } else {
             start = startGraph.x;
         }
         int segmentStartIndex = (int) Math.floor(start);
         if (segmentStartIndex < 0 || segmentStartIndex >= poly.length - 1)    {
             System.out.println("Error converting graph point to polygon point.");
             return null;
         }
         Point2D.Double segmentStart = poly[segmentStartIndex];
         Point2D.Double segmentEnd = poly[segmentStartIndex + 1];
         double x = (1 - start)*segmentStart.x + start*segmentEnd.x;
         double y = (1 - start)*segmentStart.y + start*segmentEnd.y;
         return new Point2D.Double(x, y);
     }
 
     public Point2D.Double getPolygonEnd(Point2D.Double[] poly) {
         double end;
         if (startGraph.x - endGraph.x == 0) {
             end = endGraph.y;
         } else {
             end = endGraph.x;
         }
         int segmentEndIndex = (int) Math.floor(end);
         if (segmentEndIndex < 1 || segmentEndIndex >= poly.length)    {
             System.out.println("Error converting graph point to polygon point.");
             return null;
         }
         Point2D.Double segmentStart = poly[segmentEndIndex - 1];
         Point2D.Double segmentEnd = poly[segmentEndIndex];
         double x = (1 - end)*segmentStart.x + end*segmentEnd.x;
         double y = (1 - end)*segmentStart.y + end*segmentEnd.y;
         return new Point2D.Double(x, y);
     }
 
     public Point2D.Double getPolygonMidpoint(Point2D.Double[] poly) {
         double x = (this.getPolygonStart(poly).x + this.getPolygonEnd(poly).x) / 2;
         double y = (this.getPolygonStart(poly).y + this.getPolygonEnd(poly).y) / 2;
         return new Point2D.Double(x, y);
     }
 
     public Point2D.Double getMidpoint() {
         if (isVertical()) {
             return new Point2D.Double(startGraph.x, (startGraph.y + endGraph.y) /2);
         } else {
             return new Point2D.Double((startGraph.x + endGraph.x) / 2, startGraph.y);
         }
     }
 
     public boolean contains(Point2D.Double point) {
         if (point.x == startGraph.x && point.x == endGraph.x) {
             if (point.y >= startGraph.y && point.y <= endGraph.y) {
                 return true;
             }
         } else {
             if (point.x >= startGraph.x && point.x <= endGraph.x) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Interval interval = (Interval) o;
 
         if (endGraph != null ? !endGraph.equals(interval.endGraph) : interval.endGraph != null) return false;
         if (startGraph != null ? !startGraph.equals(interval.startGraph) : interval.startGraph != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = startGraph != null ? startGraph.hashCode() : 0;
         result = 31 * result + (endGraph != null ? endGraph.hashCode() : 0);
         return result;
     }
 
     public boolean intersects(Interval other) {
 
         if (this.isVertical() && other.isVertical()) {
             return (this.startGraph.y <= other.endGraph.y) && (other.startGraph.y <= this.endGraph.y);
         } else if (!this.isVertical() && !other.isVertical()) {
             return (this.startGraph.x <= other.endGraph.x) && (other.startGraph.x <= this.endGraph.x);
         } else {
             return false;
         }
     }
 
     public Interval intersection(Interval other) {
         if (this.intersects(other)) {
             Interval result;
             if (this.isVertical()) {
                 result = new Interval(new Point2D.Double(this.startGraph.x, Math.max(this.startGraph.y, other.startGraph.y)), new Point2D.Double(this.startGraph.x, Math.min(this.endGraph.y, other.endGraph.y)));
 
             } else {
                 result = new Interval(new Point2D.Double(Math.max(this.startGraph.x, other.startGraph.x), this.startGraph.y), new Point2D.Double(Math.min(this.endGraph.x, other.endGraph.x), this.startGraph.y));
             }
             if (result.startGraph.equals(result.endGraph)) {
                 result = null;
             }
             return result;
         } else {
             return null;
         }
     }
 
     public boolean isParallelTo(Interval other) {
         return (this.startGraph.x - this.endGraph.x) + (other.startGraph.x - other.endGraph.x) == 0 ||
                 (this.startGraph.y - this.endGraph.y) + (other.startGraph.y - other.endGraph.y) == 0;
     }
 
     public boolean isVertical() {
         return (this.startGraph.x - this.endGraph.x == 0);
     }
 }
