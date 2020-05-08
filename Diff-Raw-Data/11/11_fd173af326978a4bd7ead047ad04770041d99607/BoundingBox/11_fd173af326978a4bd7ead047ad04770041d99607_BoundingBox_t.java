 package geometry;
 
 public class BoundingBox {
     static public final int BB_INTERSECTS = 0;
    static public final int BB_INFRONT = 1;
    static public final int BB_BEHIND = 2;
     private Point min, max;
     
     public BoundingBox(Point a, Point b) throws GeometryException {
         if (a == null || b == null) throw new GeometryException("Cornerpoints for bounding boxes cannot be null");
         Float[] aCoords = a.getCoords();
         Float[] bCoords = b.getCoords();
         for (int i = 0; i < aCoords.length; i++)
             if (aCoords[i] > bCoords[i])
                     throw new GeometryException("Minimum bounding box point is not minimum");
         this.min = a;
         this.max = b;
     }
     
     public Boolean pointInBox(Point p) {
         Float[] pCoords = p.getCoords();
         Float[] minCoords = this.min.getCoords();
         Float[] maxCoords = this.max.getCoords();
         for (int i = 0; i < pCoords.length; i++)
             if (pCoords[i] < minCoords[i] || pCoords[i] > maxCoords[i]) return false;
         return true;
     }
     
     public Point getMinPoint() {
         return this.min;
     }
     
     public Point getMaxPoint() {
         return this.max;
     }
 
     public BoundingBox transposedBox(Vector v) {
         BoundingBox result = null;
         try {
             result = new BoundingBox(this.min.transposedPoint(v.getCoords()), this.max.transposedPoint(v.getCoords()));
         } catch (GeometryException ge) {}
         return result;
     }
     
     public Boolean intersectWithRay(Vector r, Point p, Boolean infinite) {
         if (infinite) return (this.intersectWithRay(r, p, false) || this.intersectWithRay(r.inverseVector(), p, false));
         
         Float[] rfCoords = r.unitVector().getCoords();
         for (int i = 0; i < rfCoords.length; i++)
             rfCoords[i] = 1.0f / rfCoords[i];
 
         Float[] minCoords = this.min.getCoords();
         Float[] maxCoords = this.max.getCoords();
         Float[] pCoords = p.getCoords();
         Float t1 = (minCoords[0] - pCoords[0])*rfCoords[0];
         Float t2 = (maxCoords[0] - pCoords[0])*rfCoords[0];
         Float t3 = (minCoords[1] - pCoords[1])*rfCoords[1];
         Float t4 = (maxCoords[1] - pCoords[1])*rfCoords[1];
         Float t5 = (minCoords[2] - pCoords[2])*rfCoords[2];
         Float t6 = (maxCoords[2] - pCoords[2])*rfCoords[2];
 
         Float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
         Float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
 
         if (tmax < 0) return false;
         if (tmin > tmax) return false;
         return true;
     }
     
     public int intersectWithPlane(Plane pl) {
         Float[] minCoords = this.min.getCoords();
         Float[] pCoords = this.min.getCoords();
         Float[] maxCoords = this.max.getCoords();
         Float[] nCoords = this.max.getCoords();
         Float[] normalCoords = pl.getNormal().getCoords();
         
         for (int i = 0; i < nCoords.length; i++)
             if (normalCoords[i] >= 0) {
                 pCoords[i] = maxCoords[i];
                 nCoords[i] = minCoords[i];
             }
 
         int result = BB_INFRONT;
         try {
             if (pl.distanceFromPoint(new Point(pCoords)) < 0) result = BB_BEHIND;
             else if (pl.distanceFromPoint(new Point(nCoords)) < 0) result = BB_INTERSECTS;
         } catch (GeometryException ge) {}
         return result;
     }
     
 }
