 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.math;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 import static java.lang.Math.abs;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import org.apache.log4j.Logger;
 
 /**
  * A representation of a 3D triangle.
  * 
  * @author Martin Pecka
  */
 public class Triangle3d implements Cloneable
 {
     protected Point3d          p1;
     protected Point3d          p2;
     protected Point3d          p3;
 
     protected HalfSpace3d      hs1;
     protected HalfSpace3d      hs2;
     protected HalfSpace3d      hs3;
     protected Plane3d          plane;
 
     protected Segment3d        s1;
     protected Segment3d        s2;
     protected Segment3d        s3;
 
     /** The list of neighbors. */
     protected List<Triangle3d> neighbors   = new LinkedList<Triangle3d>();
 
     /** The read-only view on the neighbors list. */
     protected List<Triangle3d> neighborsRO = Collections.unmodifiableList(neighbors);
 
     /**
      * @return The list of neighbor triangles. The returned list is read-only. Triangles with just a single common point
      *         aren't considered as neighbors.
      */
     public List<? extends Triangle3d> getNeighbors()
     {
         return neighborsRO;
     }
 
     /**
      * Create a triangle with the given vertices.
      * 
      * @param p1 A vertex.
      * @param p2 A vertex.
      * @param p3 A vertex.
      * 
      * @throws IllegalArgumentException If the given points lie in one line.
      */
     public Triangle3d(Point3d p1, Point3d p2, Point3d p3) throws IllegalArgumentException
     {
         this.p1 = p1;
         this.p2 = p2;
         this.p3 = p3;
 
         if (new Line3d(p1, p2).contains(p3)) {
             throw new IllegalArgumentException("Trying to create a triangle from colinear points.");
         }
 
         recomputeDerivedItems();
     }
 
     /**
      * Create a triangle with the given vertices.
      * 
      * @param p1x A vertex's x coordinate.
      * @param p1y A vertex's y coordinate.
      * @param p1z A vertex's z coordinate.
      * @param p2x A vertex's x coordinate.
      * @param p2y A vertex's y coordinate.
      * @param p2z A vertex's z coordinate.
      * @param p3x A vertex's x coordinate.
      * @param p3y A vertex's y coordinate.
      * @param p3z A vertex's z coordinate.
      * 
      * @throws IllegalArgumentException If the given points lie in one line.
      */
     public Triangle3d(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z, double p3x, double p3y,
             double p3z) throws IllegalArgumentException
     {
         this(new Point3d(p1x, p1y, p1z), new Point3d(p2x, p2y, p2z), new Point3d(p3x, p3y, p3z));
     }
 
     /**
      * Compute new values of all the helper fields such as plane, s1, s2, s3 and so...
      */
     protected void recomputeDerivedItems()
     {
         plane = new Plane3d(p1, p2, p3);
         hs1 = HalfSpace3d.createPerpendicularToTriangle(p1, p2, p3);
         hs2 = HalfSpace3d.createPerpendicularToTriangle(p2, p3, p1);
         hs3 = HalfSpace3d.createPerpendicularToTriangle(p3, p1, p2);
 
         s1 = new Segment3d(p1, p2);
         s2 = new Segment3d(p2, p3);
        s3 = new Segment3d(p3, p1);
     }
 
     /**
      * Change the points of this triangle. Only non-null points will be changed.
      * 
      * @param p1
      * @param p2
      * @param p3
      */
     public void setPoints(Point3d p1, Point3d p2, Point3d p3)
     {
         if (p1 != null)
             this.p1 = p1;
         if (p2 != null)
             this.p2 = p2;
         if (p3 != null)
             this.p3 = p3;
 
         recomputeDerivedItems();
     }
 
     /**
      * Return the coordinates of the first point.
      * 
      * @return The coordinates of the first point.
      */
     public Point3d getP1()
     {
         return p1;
     }
 
     /**
      * Return the coordinates of the second point.
      * 
      * @return The coordinates of the second point.
      */
     public Point3d getP2()
     {
         return p2;
     }
 
     /**
      * Return the coordinates of the third point.
      * 
      * @return The coordinates of the third point.
      */
     public Point3d getP3()
     {
         return p3;
     }
 
     /**
      * @return An array of vertices of the triangle. Further modifications to this array will have no effect on the
      *         triangle.
      */
     public Point3d[] getVertices()
     {
         return new Point3d[] { p1, p2, p3 };
     }
 
     /**
      * @return The first side of the triangle.
      */
     public Segment3d getS1()
     {
         return s1;
     }
 
     /**
      * @return The second side of the triangle.
      */
     public Segment3d getS2()
     {
         return s2;
     }
 
     /**
      * @return The third side of the triangle.
      */
     public Segment3d getS3()
     {
         return s3;
     }
 
     /**
      * @return An array of all edges of the triangle. Further modifications to this array will have no effect on the
      *         triangle.
      */
     public Segment3d[] getEdges()
     {
         return new Segment3d[] { s1, s2, s3 };
     }
 
     /**
      * @return The plane the triangle lies in.
      */
     public Plane3d getPlane()
     {
         return plane;
     }
 
     /**
      * Returns true if this triangle contains the given point.
      * 
      * @param point The point to check.
      * @return Whether this triangle contains the given point.
      */
     public boolean contains(Point3d point)
     {
         return plane.contains(point) && hs1.contains(point) && hs2.contains(point) && hs3.contains(point);
     }
 
     /**
      * Return <code>true</code> if the given point lies in one of the sides of this triangle.
      * 
      * @param point The point to check.
      * @return <code>true</code> if the given point lies in one of the sides of this triangle.
      */
     public boolean sidesContain(Point3d point)
     {
         return s1.contains(point) || s2.contains(point) || s3.contains(point);
     }
 
     /**
      * Returns true if the given triangle has a common edge with this triangle.
      * 
      * If <code>strict</code> is true, then the edges must match exactly. If it is false, it is sufficient that the
      * edges overlap.
      * 
      * @param t The triangle to try to find common edge with.
      * @param strict If true, then the edges must match exactly. If it is false, it is sufficient that the edges
      *            overlap.
      * @return true if the given triangle has a common edge with this triangle.
      */
     public boolean hasCommonEdge(Triangle3d t, boolean strict)
     {
         return getCommonEdge(t, strict) != null;
     }
 
     /**
      * Returns the common part of edges of this and the given triangle. If they have just a common point, a segment
      * with zero direction vector will be returned. If the triangles overlay by more than an edge, the result is
      * undefined. If the triangles do not have a common segment, <code>null</code> will be returned.
      * 
      * @param t The segment to find common edge with.
      * @param strict If true, then the edges must match exactly. If it is false, it is sufficient that the edges
      *            overlap.
      * @return The common part of edges of this and the given triangle.
      */
     public Segment3d getCommonEdge(Triangle3d t, boolean strict)
     {
         Segment3d result = null;
         for (Segment3d edge1 : getEdges()) {
             for (Segment3d edge2 : t.getEdges()) {
                 if (strict) {
                     if (edge1.epsilonEquals(edge2, true))
                         return new Segment3d(edge1);
                 } else {
                     if (edge1.overlaps(edge2)) {
                         Segment3d intersection = edge1.getIntersection(edge2);
                         // if the intersection isn't just a point, we can surely return
                         if (intersection != null && !intersection.getVector().epsilonEquals(new Vector3d(), EPSILON))
                             return intersection;
                         if (intersection != null)
                             result = intersection;
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      * Return <code>true</code> if the given point is a vertex of this triangle.
      * 
      * @param point The point to check.
      * @return <code>true</code> if the given point is a vertex of this triangle.
      */
     public boolean isVertex(Point3d point)
     {
         return p1.epsilonEquals(point, EPSILON) || p2.epsilonEquals(point, EPSILON) || p3.epsilonEquals(point, EPSILON);
     }
 
     /**
      * Return the point corresponding to the given barycentric coordinates in this triangle.
      * 
      * @param b The barycentric coordinates to convert.
      * @return The point corresponding to the given barycentric coordinates in this triangle.
      */
     public Point3d interpolatePointFromBarycentric(Vector3d b)
     {
         Point3d result = new Point3d();
         result.x = b.x * p1.x + b.y * p2.x + b.z * p3.x;
         result.y = b.x * p1.y + b.y * p2.y + b.z * p3.y;
         result.z = b.x * p1.z + b.y * p2.z + b.z * p3.z;
         return result;
     }
 
     /**
      * Return the barycentric coordinates of the given point.
      * 
      * @param p The point to compute coordinates of.
      * @return The barycentric coordinates of the given point.
      * 
      * @see http://facultyfp.salisbury.edu/despickler/personal/C482/Resources/barycentric.pdf
      */
     public Vector3d getBarycentricCoordinates(Point3d p)
     {
         Vector3d c_a = new Vector3d(p3);
         c_a.sub(p1);
         Vector3d a_c = new Vector3d(p1);
         a_c.sub(p3);
         Vector3d c_b = new Vector3d(p3);
         c_b.sub(p2);
         Vector3d b_a = new Vector3d(p2);
         b_a.sub(p1);
         Vector3d p_a = new Vector3d(p);
         p_a.sub(p1);
         Vector3d p_b = new Vector3d(p);
         p_b.sub(p2);
         Vector3d p_c = new Vector3d(p);
         p_c.sub(p3);
 
         Vector3d n = new Vector3d();
         Vector3d na = new Vector3d();
         Vector3d nb = new Vector3d();
         Vector3d nc = new Vector3d();
 
         n.cross(b_a, c_a);
         na.cross(c_b, p_b);
         nb.cross(a_c, p_c);
         nc.cross(b_a, p_a);
 
         double nLengthSq = n.lengthSquared();
 
         return new Vector3d(n.dot(na) / nLengthSq, n.dot(nb) / nLengthSq, n.dot(nc) / nLengthSq);
     }
 
     /**
      * Return the intersection points of this triangle and the given line.
      * 
      * @param line The line to get intersections with.
      * @return A segment that defines the intersection of the given line (or segment) and this triangle (the segment can
      *         be zero-length), or <code>null</code>, if no intersection exists. A segment start or end inside the
      *         triangle is also taken as an intersection.
      */
     public Segment3d getIntersection(Line3d line)
     {
         if (abs(line.v.dot(getNormal())) < EPSILON) {
             // the line is parallel to the triangle's plane
 
             if (!plane.contains(line.p))
                 return null; // the line doesn't lie in the triangle's plane
 
             Segment3d intersection = null;
             List<Point3d> intersections = new ArrayList<Point3d>(3);
 
             for (Segment3d s : getEdges()) { // find intersections with edges
                 intersection = s.getIntersection(line);
                 if (intersection != null && intersection.v.epsilonEquals(new Vector3d(), EPSILON)) {
                     intersections.add(intersection.p);
                 } else if (intersection != null) {
                     // the line lies on the same line as the edge and they have nonempty intersection - we can return
                     return intersection;
                 }
             }
 
             if (line instanceof Segment3d) {
                 // a segment can start or end inside the triangle
                 for (Point3d p : ((Segment3d) line).getPoints()) {
                     if (this.contains(p)/* && !sidesContain(p) */) {
                         intersections.add(p);
                     }
                 }
             }
 
             // rounding erros may affect the method a lot, so ensure it is a little more tolerant
             MathHelper.removeEpsilonEqualPoints(intersections, 2d * EPSILON);
 
             for (int i = 0; i < intersections.size(); i++) {
                 if (!sidesContain(intersections.get(i))) {
                     Point3d substitution = null;
                     for (int j = 0; j < intersections.size(); j++) {
                         if (j == i)
                             continue;
                         if (sidesContain(intersections.get(j))
                                 && intersections.get(i).distance(intersections.get(j)) < 10d * EPSILON) {
                             substitution = intersections.get(j);
                             break;
                         }
                     }
                     if (substitution != null) {
                         intersections.remove(i--);
                     }
                 }
             }
 
             double i = 2d;
             while (intersections.size() > 2 && i < 10d) {
                 MathHelper.removeEpsilonEqualPoints(intersections, i++ * EPSILON);
             }
             if (i > 2d)
                 Logger.getLogger(getClass()).warn(
                         "Used " + (i - 1)
                                 + "*EPSILON for joining intersection points. The resulting intersection points are "
                                 + intersections);
 
             if (intersections.size() == 2) {
                 return new Segment3d(intersections.get(0), intersections.get(1));
             } else if (intersections.size() == 1) {
                 return new Segment3d(intersections.get(0), intersections.get(0));
             } else if (intersections.size() == 0) {
                 return null;
             } else {
                 throw new IllegalStateException("Illegal count of intersections of a line and triangle: "
                         + intersections.size());
             }
         } else {
             // the line isn't parallel to the triangle's plane
             Line3d intersection = plane.getIntersection(line);
             // line.contains(...) is being called because the line can be also a Segment3d
             if (intersection != null && intersection.v.epsilonEquals(new Vector3d(), EPSILON)) {
                 return new Segment3d(intersection.p, intersection.p);
             } else {
                 assert false : "Triangle3d#getIntersection(Line3d): line not parallel to the triangle's plane, but its intersection with the plane isn't a single point";
                 return null;
             }
         }
     }
 
     /**
      * "Cut" this triangle by the given segment and return the triangles that are created by the cut.
      * 
      * @param segment The segment to cut with.
      * @return The newly created triangles.
      * 
      * @throws IllegalArgumentException If the segment doesn't define a cut of this triangle or if it doesn't relate to
      *             this triangle.
      */
     @SuppressWarnings("unchecked")
     public <T extends Triangle3d> List<T> subdivideTriangle(IntersectionWithTriangle<T> segment)
             throws IllegalArgumentException
     {
         if (!this.epsilonEquals(segment.triangle)) {
             throw new IllegalArgumentException(
                     "Triangle3d#subdivideTriangle(): The given intersection segment doesn't relate to this triangle.");
         }
 
         if (!this.sidesContain(segment.p) || !this.sidesContain(segment.p2)) {
             throw new IllegalArgumentException(
                     "Triangle3d#subdivideTriangle(): Trying to subdivide a triangle by an invalid cut segment.");
         }
 
         List<T> triangles = new LinkedList<T>();
 
         // a cut along a side doesn't subdivide the triangle
         if (segment.isWholeSideIntersection()) {
             triangles.add((T) this);
             return triangles;
         }
 
         // cache the two points of intersection - p1, p2
         Point3d p1 = segment.getP1();
         Point3d p2 = segment.getP2();
 
         Line3d segmentAsLine = new Line3d(p1, p2);
 
         // not a case where one of the intersection points is a vertex (but two distinct intersection points exist);
         // if the line is a segment, neither the start point nor the end point lie inside the triangle
         if (!p1.epsilonEquals(p2, EPSILON) && !this.isVertex(p1) && !this.isVertex(p2) && this.sidesContain(p1)
                 && this.sidesContain(p2) && !segmentAsLine.contains(getP1()) && !segmentAsLine.contains(getP2())
                 && !segmentAsLine.contains(getP3())) {
 
             // the checks with segmentAsLine may seem redundant, but it it shows it is needed due to the
             // non-transitivity of floating point arithmetics
 
             /*
              * _________________________|_p1______________________________________________________________________
              * __________________v*-----*-------------------*tv1__________________________________________________
              * ___________________\_____|.................../_____________________________________________________
              * ____________________\____|................/,/______________________________________________________
              * _____________________\___|............/,,,,/_______________________________________________________
              * ______________________\__|......../,,,,,,,/________________________________________________________
              * _______________________\_|..../,,,,,,,,,,/_________________________________________________________
              * ________________________\|/,,,,,,,,,,,,,/__________________________________________________________
              * _________________________*,p2,,,,,,,,,,/___________________________________________________________
              * _________________________|\,,,,,,,,,,,/____________________________________________________________
              * _________________________|_\,,,,,,,,,/_____________________________________________________________
              * _________________________|__\,,,,,,,/______________________________________________________________
              * _____________________________\,,,,,/_______________________________________________________________
              * ______________________________\,,,/________________________________________________________________
              * _______________________________\,/_________________________________________________________________
              * ________________________________*tv2_______________________________________________________________
              * Please view this ASCII graphics without line-breaking (or break lines at minimum 120 characters)
              */
 
             // find the sides of 3D triangle which contain the intersection points p1, p2 - save them into "sides"
             List<Segment3d> sides = new ArrayList<Segment3d>(2);
             for (Segment3d edge : this.getEdges()) {
                 if (edge.contains(p1) || edge.contains(p2))
                     sides.add(edge);
             }
 
             // set v to the vertex of 3D triangle which lies alone in the halfplane defined by the triangle's plane
             // and line p1p2
             Point3d v = sides.get(0).getIntersection(sides.get(1)).p; // we can assume the intersection is a single
                                                                       // point
 
             // tv1 is a vertex of 3D triangle such that p1 lies on the segment tv1v
             // tv2 is a vertex of 3D triangle such that p2 lies on the segment tv2v
             List<Point3d> triangleVertices = new ArrayList<Point3d>(2);
             for (Point3d p : this.getVertices()) {
                 if (!p.epsilonEquals(v, EPSILON))
                     triangleVertices.add(p);
             }
             Point3d tv1 = triangleVertices.get(0);
             Point3d tv2 = triangleVertices.get(1);
             if (!new Line3d(v, tv1).contains(p1)) {
                 Point3d tmp = tv2;
                 tv2 = tv1;
                 tv1 = tmp;
             }
             // construct the three newly defined triangles
             Vector3d tNormal = this.getNormal();
 
             Vector3d normal = new Triangle3d(p1, p2, v).getNormal();
             if (tNormal.angle(normal) < EPSILON)
                 triangles.add((T) createSubtriangle(p1, p2, v));
             else
                 triangles.add((T) createSubtriangle(p1, v, p2));
 
             normal = new Triangle3d(p1, p2, tv1).getNormal();
             if (tNormal.angle(normal) < EPSILON)
                 triangles.add((T) createSubtriangle(p1, p2, tv1));
             else
                 triangles.add((T) createSubtriangle(p1, tv1, p2));
 
             normal = new Triangle3d(p2, tv1, tv2).getNormal();
             if (tNormal.angle(normal) < EPSILON)
                 triangles.add((T) createSubtriangle(p2, tv1, tv2));
             else
                 triangles.add((T) createSubtriangle(p2, tv2, tv1));
 
         } else if (!p1.epsilonEquals(p2, EPSILON) && (this.isVertex(p1) || this.isVertex(p2))) {
             // one of the intersection points is a vertex; the other inters. point is distinct from that one
             /*
              * ________________________________|__________________________________________________________________
              * ________________tv1*------------*p-----------*tv2__________________________________________________
              * ___________________\............|,,,,,,,,,,,,/_____________________________________________________
              * ____________________\...........|,,,,,,,,,,,/______________________________________________________
              * _____________________\..........|,,,,,,,,,,/_______________________________________________________
              * ______________________\.........|,,,,,,,,,/________________________________________________________
              * _______________________\........|,,,,,,,,/_________________________________________________________
              * ________________________\.......|,,,,,,,/__________________________________________________________
              * _________________________\......|,,,,,,/___________________________________________________________
              * __________________________\.....|,,,,,/____________________________________________________________
              * ___________________________\....|,,,,/_____________________________________________________________
              * ____________________________\...|,,,/______________________________________________________________
              * _____________________________\..|,,/_______________________________________________________________
              * ______________________________\.|,/________________________________________________________________
              * _______________________________\|/_________________________________________________________________
              * ________________________________*v_________________________________________________________________
              * Please view this ASCII graphics without line-breaking (or break lines at minimum 120 characters)
              */
 
             // v is the intersection point which is also a vertex; p is the other intersection point
             Point3d v, p;
             if (this.isVertex(p1)) {
                 v = p1;
                 p = p2;
             } else {
                 v = p2;
                 p = p1;
             }
 
             // tv1, tv2 are the other vertices (v is the third one) of the 3D triangle
             List<Point3d> triangleVertices = new ArrayList<Point3d>(2);
             for (Point3d vert : this.getVertices()) {
                 if (!vert.epsilonEquals(v, EPSILON))
                     triangleVertices.add(vert);
             }
             Point3d tv1 = triangleVertices.get(0);
             Point3d tv2 = triangleVertices.get(1);
 
             Vector3d tNormal = this.getNormal();
             Vector3d normal = new Triangle3d(v, p, tv1).getNormal();
             // add two new triangles
             if (tNormal.angle(normal) < EPSILON)
                 triangles.add((T) createSubtriangle(v, p, tv1));
             else
                 triangles.add((T) createSubtriangle(v, tv1, p));
 
             normal = new Triangle3d(v, p, tv2).getNormal();
             if (tNormal.angle(normal) < EPSILON)
                 triangles.add((T) createSubtriangle(v, p, tv2));
             else
                 triangles.add((T) createSubtriangle(v, tv2, p));
         } else if (p1.epsilonEquals(p2, EPSILON) && this.isVertex(p1)) {
             // the line intersects the triangle in a single vertex, no need to divide the triangle
             triangles.add((T) this);
         } else if (p1.epsilonEquals(p2, EPSILON) && sidesContain(p2)) {
             // the segment starts in the interior of a side, no need to divide the triangle
             triangles.add((T) this);
         } else if (p1.epsilonEquals(p2, EPSILON)) {
             // the fold isn't parallel to the triangle's plane - something's weird
             throw new IllegalArgumentException(
                     "Triangle3d#subdivideTriangle(): a cut segment not parallel to the triangle's plane");
         } else {
             assert false : "Triangle3d#subdivideTriangle(): unexpected branch taken.";
         }
 
         if (triangles.size() > 1) {
             // remove this triangle from the neighbors' neighbors lists and add new triangles as neighbors
             for (Triangle3d n : neighbors) {
                 n.neighbors.remove(this);
                 for (T t : triangles) {
                     Segment3d commonEdge = n.getCommonEdge(t, false);
                     // don't consider triangles with single common point as neighbors
                     if (commonEdge != null && !commonEdge.getVector().epsilonEquals(new Vector3d(), EPSILON)) {
                         n.neighbors.add(t);
                         t.neighbors.add(n);
                     }
                 }
             }
 
             // find and add neighbors among the new triangles
             int i = 0;
             for (T t1 : triangles) {
                 if (i + 1 <= triangles.size() - 1) {
                     for (T t2 : triangles.subList(i + 1, triangles.size())) {
                         Segment3d commonEdge = t1.getCommonEdge(t2, false);
                         // don't consider triangles with single common point as neighbors
                         if (commonEdge != null && !commonEdge.getVector().epsilonEquals(new Vector3d(), EPSILON)) {
                             t1.neighbors.add(t2);
                             t2.neighbors.add(t1);
                         }
                     }
                 }
                 i++;
             }
 
         }
 
         return triangles;
     }
 
     /**
      * Creates a triangle from the given points.
      * 
      * The given points should define a triangle that is whole contained in this triangle.
      * <b>This method should be overriden in all subclasses and must return a triangle castable to this triangle's
      * type.</b>
      * 
      * @param p1 A vertex of the triangle.
      * @param p2 A vertex of the triangle.
      * @param p3 A vertex of the triangle.
      * @return A triangle from the given points.
      */
     protected Triangle3d createSubtriangle(Point3d p1, Point3d p2, Point3d p3)
     {
         return new Triangle3d(p1, p2, p3);
     }
 
     /**
      * @return The normalized normal vector to the triangle's plane.
      */
     public Vector3d getNormal()
     {
         Vector3d normal = new Vector3d();
         normal.normalize(plane.getNormal());
         return normal;
     }
 
     /**
      * <p>
      * <b>The list of neighbors is not cloned, just the same references are copied.</b>
      * </p>
      * 
      * {@inheritDoc}
      */
     @Override
     public Triangle3d clone() throws CloneNotSupportedException
     {
         Triangle3d result = new Triangle3d(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
         result.neighbors.addAll(this.neighbors);
         return result;
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
         result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
         result = prime * result + ((p3 == null) ? 0 : p3.hashCode());
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
         Triangle3d other = (Triangle3d) obj;
         if (p1 == null) {
             if (other.p1 != null)
                 return false;
         } else if (!p1.equals(other.p1))
             return false;
         if (p2 == null) {
             if (other.p2 != null)
                 return false;
         } else if (!p2.equals(other.p2))
             return false;
         if (p3 == null) {
             if (other.p3 != null)
                 return false;
         } else if (!p3.equals(other.p3))
             return false;
         return true;
     }
 
     /**
      * Return <code>true</code> if the given triangle is almost equal to this one.
      * 
      * @param other The triangle to compare.
      * @return <code>true</code> if the given triangle is almost equal to this one.
      */
     public boolean epsilonEquals(Triangle3d other)
     {
         if (other == null)
             return false;
         if (p1 == null) {
             if (other.p1 != null)
                 return false;
         } else if (!p1.epsilonEquals(other.p1, EPSILON))
             return false;
         if (p2 == null) {
             if (other.p2 != null)
                 return false;
         } else if (!p2.epsilonEquals(other.p2, EPSILON))
             return false;
         if (p3 == null) {
             if (other.p3 != null)
                 return false;
         } else if (!p3.epsilonEquals(other.p3, EPSILON))
             return false;
         return true;
     }
 
     @Override
     public String toString()
     {
         return "Triangle3d [" + p1 + ", " + p2 + ", " + p3 + "]";
     }
 
 }
