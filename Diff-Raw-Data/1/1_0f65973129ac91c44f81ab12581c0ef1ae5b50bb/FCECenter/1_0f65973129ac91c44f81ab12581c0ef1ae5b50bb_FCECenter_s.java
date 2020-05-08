 package filter;
 
 import geometry.GeometryException;
 import geometry.Point;
 
 public class FCECenter {
     private Point center = null;
     private Point sum = null;
     private Integer count = 0;
     
     public FCECenter(Point p) throws FCEException {
         if (p == null) throw new FCEException("All cluster centers must be initialized with non-null points");
         this.center = p;
         try {
             this.sum = Point.zeroPoint(this.center.getDimensions());
         } catch (GeometryException ge) {}
     }
     
     public Integer getDimensions() { return this.center.getDimensions(); }
     
     public Point getCenter() { return this.center; }
     
     public void addToCount(Integer i) throws FCEException {
         if (i <= 0) throw new FCEException("Center point counts can only be incremented");
         this.count += i;
     }
 
     public void addToSum(Point p) throws FCEException {
         if (p == null) throw new FCEException("It is not possible to add a null point to the sum of a center");
         try {
             this.sum = this.sum.transposedPoint(p);
         } catch (GeometryException ge) {
             throw new FCEException("Center sums can be updated only with point of same dimensionality as the center itself");
         }
     }
     
     public void updateCenter() {
         this.center = this.sum.scaledPoint(1.0f/this.count);
         try {
             this.sum = Point.zeroPoint(this.center.getDimensions());
         } catch (GeometryException ge) {}
     }
 }
