 package geometry;
 
 public class Vector {
     private Point endpoint;
     
     public Vector(Float x, Float y, Float z) {
         this.endpoint = new Point(x, y, z);
     }
 
     public Vector(Point start, Point end) {
         Vector v1 = new Vector(start);
         Vector v2 = new Vector(end);
         v2.subtract(v1);
         this.endpoint = v2.getPoint();
     }
 
     public Vector(Point p) {
         this.endpoint = new Point(p);
     }
 
     public Vector(Float[] coords) throws GeometryException {
         this.endpoint = new Point(coords);
     }
     
     public Float[] getCoords() {
         return this.endpoint.getCoords();
     }
     
     public Point getPoint() { return this.endpoint; }
 
     public Vector inverseVector() {
         return new Vector(this.endpoint.symmetricPoint());
     }
 
     public void add(Vector v) {
         try {
             this.endpoint = this.endpoint.transposedPoint(v.getCoords());
         } catch (GeometryException ge) {}
     }
     
     public void subtract(Vector v) {
         try {
             this.endpoint = this.endpoint.transposedPoint(v.inverseVector().getCoords());
         } catch (GeometryException ge) {}
     }
     
     public Float getMeasure() {
         Float result = 0.0f;
         try {
             result = this.endpoint.distanceFrom(Point.AXIS_ZERO);
         } catch (GeometryException ge) {}
         return result;
     }
     
     public void scale(Float factor) {
         this.endpoint.scaledPoint(factor);
     }
     
     public Vector unitVector() {
         Vector result = null;
         try {
             result = new Vector(this.endpoint.scaledPoint(this.getMeasure()).getCoords());
         } catch (GeometryException ge) {}
         return result;
     }
     
     public Float getProjection(Vector v) {
         Float[] vCoords = v.getCoords();
         Float[] thisCoords = this.getCoords();
         Float dp = 0.0f;
         for (int i = 0; i < vCoords.length; i++)
             dp += vCoords[i]*thisCoords[i];
         return dp/this.getMeasure();
     }
     
    public Float distangeFromPoint(Point p) {
         Vector n = this.unitVector();
         Vector pv = new Vector(p).inverseVector();
         n.scale(this.getProjection(pv));
         pv.subtract(n);
         return pv.getMeasure();
     }
 }
