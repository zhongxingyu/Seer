 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.math;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 import static java.lang.Math.abs;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 /**
  * A plane in 3D.
  * 
  * @author Martin Pecka
  */
 public class Plane3d implements Cloneable
 {
     /**
      * A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     protected double   a;
 
     /**
      * A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     protected double   b;
 
     /**
      * A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     protected double   c;
 
     /**
      * A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     protected double   d;
 
     /** The normal of the plane. */
     protected Vector3d normal = null;
 
     /**
      * Constructs a standalone copy (clone) of this plane.
      * 
      * @param plane The plane to copy.
      */
     public Plane3d(Plane3d plane)
     {
         a = plane.a;
         b = plane.b;
         c = plane.c;
         d = plane.d;
     }
 
     /**
      * Constructs a plane containing the given three points.
      * 
      * @param p1 A point in the plane.
      * @param p2 A point in the plane.
      * @param p3 A point in the plane.
      * 
      * @throws IllegalArgumentException If the points are colinear.
      */
     public Plane3d(Point3d p1, Point3d p2, Point3d p3) throws IllegalArgumentException
     {
         if (new Line3d(p1, p2).contains(p3)) {
             throw new IllegalArgumentException("Trying to define a plane using three colinear points.");
         }
 
         a = p1.y * (p2.z - p3.z) + p2.y * (p3.z - p1.z) + p3.y * (p1.z - p2.z);
         b = p1.z * (p2.x - p3.x) + p2.z * (p3.x - p1.x) + p3.z * (p1.x - p2.x);
         c = p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y);
         d = -p1.x * (p2.y * p3.z - p3.y * p2.z) - p2.x * (p3.y * p1.z - p1.y * p3.z) - p3.x
                 * (p1.y * p2.z - p2.y * p1.z);
     }
 
     /**
      * Constructs a plane containing the given three points.
      * 
      * @param p1x x-coordinate of a point in the plane.
      * @param p1y y-coordinate of a point in the plane.
      * @param p1z z-coordinate of a point in the plane.
      * @param p2x x-coordinate of a point in the plane.
      * @param p2y y-coordinate of a point in the plane.
      * @param p2z z-coordinate of a point in the plane.
      * @param p3x x-coordinate of a point in the plane.
      * @param p3y y-coordinate of a point in the plane.
      * @param p3z z-coordinate of a point in the plane.
      * 
      * @throws IllegalArgumentException If the points are colinear.
      */
     public Plane3d(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z, double p3x, double p3y,
             double p3z) throws IllegalArgumentException
     {
         this(new Point3d(p1x, p1y, p1z), new Point3d(p2x, p2y, p2z), new Point3d(p3x, p3y, p3z));
     }
 
     public boolean contains(Point3d point)
     {
         return abs(a * point.x + b * point.y + c * point.z + d) < EPSILON;
     }
 
     /**
      * Return the intersection line of this plane and the <code>other</code> plane.
      * 
      * @param other The other plane to find intersection with.
      * @return The intersection line or <code>null</code>, if the planes are equal or parallel (to distinguish these two
      *         states, use {@link Plane3d#epsilonEquals(Plane3d)}.)
      * 
      * @see http://local.wasp.uwa.edu.au/~pbourke/geometry/planeplane/
      */
     public Line3d getIntersection(Plane3d other)
     {
        Vector3d normal = getNormal();
        Vector3d oNormal = other.getNormal();
         Vector3d lineDir = new Vector3d();
         lineDir.cross(normal, oNormal);
         lineDir.normalize();
 
        double d1 = -d;
        double d2 = -other.d;
 
        double n1n1 = normal.dot(normal);
        double n2n2 = oNormal.dot(oNormal);
         double n1n2 = normal.dot(oNormal);
 
        double det = n1n1 * n2n2 - n1n2 * n1n2;
        if (abs(det) < EPSILON)
            return null;
 
        double c1 = (d1 * n2n2 - d2 * n1n2) / det;
        double c2 = (d2 * n1n1 - d1 * n1n2) / det;
 
         Point3d c1n1 = new Point3d(normal);
         c1n1.scale(c1);
         Point3d c2n2 = new Point3d(oNormal);
         c2n2.scale(c2);
 
         Point3d linePoint = new Point3d(c1n1);
         linePoint.add(c2n2);
 
         return new Line3d(linePoint, lineDir);
     }
 
     /**
      * Return the intersection point of this plane and the given line.
      * 
      * @param line The line to search for intersection with.
      * @return The intersection point (as a line with zero direction vector), or <code>null</code> if the line is
      *         parallel to the plane and not lying in it, or return <code>line</code> (if it lies in this plane).
      */
     public Line3d getIntersection(Line3d line)
     {
         // just substitute the parametric equation of the line into the general equation of the plane and extract the
         // line parameter
 
         Vector3d normal = getNormal();
 
         if (Math.abs(normal.dot(line.v)) < EPSILON) {// the line is perpendicular to the plane
             if (this.contains(line.p)) {
                 return line;
             }
             return null;
         }
 
         Vector3d pVect = new Vector3d(line.p);
         double t = -((normal.dot(pVect) + d) / normal.dot(line.v));
 
         Point3d p = new Point3d(line.p.x + t * line.v.x, line.p.y + t * line.v.y, line.p.z + t * line.v.z);
         return new Line3d(p, new Vector3d());
     }
 
     /**
      * @return The normal vector of this plane.
      */
     public Vector3d getNormal()
     {
         if (normal == null)
             normal = new Vector3d(a, b, c);
         return normal;
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = 1;
         long temp;
         temp = Double.doubleToLongBits(a);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(b);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(c);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(d);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         return result;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Plane3d other = (Plane3d) obj;
         if (Double.doubleToLongBits(a) != Double.doubleToLongBits(other.a))
             return false;
         if (Double.doubleToLongBits(b) != Double.doubleToLongBits(other.b))
             return false;
         if (Double.doubleToLongBits(c) != Double.doubleToLongBits(other.c))
             return false;
         if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d))
             return false;
         return true;
     }
 
     /**
      * Return <code>true</code> if the given plane is almost equal to this one.
      * 
      * @param other The plane to compare.
      * @return <code>true</code> if the given plane is almost equal to this one.
      */
     public boolean epsilonEquals(Plane3d other)
     {
         if (other == null)
             return false;
 
         Double quotient = MathHelper.vectorQuotient3d(normal, other.normal);
         if (quotient == null)
             return false;
 
         return Math.abs(this.d - quotient * other.d) < MathHelper.EPSILON;
     }
 
     @Override
     protected Plane3d clone() throws CloneNotSupportedException
     {
         return new Plane3d(this);
     }
 
     @Override
     public String toString()
     {
         return "Plane3d [" + a + "x + " + b + "y + " + c + "z + " + d + " = 0]";
     }
 
     /**
      * @return A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public double getA()
     {
         return a;
     }
 
     /**
      * @param a A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public void setA(double a)
     {
         this.a = a;
         normal = null;
     }
 
     /**
      * @return A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public double getB()
     {
         return b;
     }
 
     /**
      * @param b A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public void setB(double b)
     {
         this.b = b;
         normal = null;
     }
 
     /**
      * @return A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public double getC()
     {
         return c;
     }
 
     /**
      * @param c A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public void setC(double c)
     {
         this.c = c;
         normal = null;
     }
 
     /**
      * @return A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public double getD()
     {
         return d;
     }
 
     /**
      * @param d A coeficient in the general equation of a plane: ax + by + cz + d = 0.
      */
     public void setD(double d)
     {
         this.d = d;
         normal = null;
     }
 }
