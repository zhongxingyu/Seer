 package fr.nantes1900.models.basis;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import javax.vecmath.Vector3d;
 
 /**
  * Implements a triangle, composed of three points, three edges, and one vector
  * as a normal.
  * @author Daniel Lefevre, Eric Berthe, Valentin Roger, Elsa Arroud-Vignod
  */
 /**
  * @author Daniel
  */
 public class Triangle {
 
     /**
      * Number of vertices of a triangle.
      */
     private static final int NB_VERTICES = 3;
 
     /**
      * Normal of the triangle.
      */
     private final Vector3d normal = new Vector3d();
 
     /**
      * Array of three edges of the triangle.
      */
     private final Edge[] edges = new Edge[NB_VERTICES];
 
     /**
      * The list of the meshes which contain this triangle.
      */
     private final List<Mesh> meshes = new ArrayList<>();
 
     /**
      * Constructor of the triangle.
      * @param edge1
      *            one edge composed by two of the three points
      * @param edge2
      *            one edge composed by two of the three points
      * @param edge3
      *            one edge composed by two of the three points
      * @param normalNew
      *            the normal of the triangle
      */
     public Triangle(final Edge edge1, final Edge edge2, final Edge edge3,
             final Vector3d normalNew) {
 
         this.normal.set(normalNew);
         this.edges[0] = edge1;
         this.edges[1] = edge2;
         this.edges[2] = edge3;
         this.edges[0].addTriangle(this);
         this.edges[1].addTriangle(this);
         this.edges[2].addTriangle(this);
     }
 
     /**
      * Copy constructor of the triangle. Caution : do not use this constructor
      * to create double Triangle with equal values and different references !
      * This method create new points so as the old ones are not modified.
      * @param triangle
      *            the triangle to copy
      */
     public Triangle(final Triangle triangle) {
         this.normal.set(new Vector3d(triangle.normal));
         this.edges[0] = triangle.getE1();
         this.edges[1] = triangle.getE2();
         this.edges[2] = triangle.getE3();
         this.edges[0].addTriangle(this);
         this.edges[1].addTriangle(this);
         this.edges[2].addTriangle(this);
     }
 
     /**
      * Checks if the this triangle is oriented as the other triangle with an
      * angle error. The error is in degree.
      * @param face
      *            the other triangle to compare with
      * @param error
      *            the angle error
      * @return true if they have the same orientation, false otherwise
      */
     public final boolean angle(final Triangle face, final double error) {
         return this.angle(face.normal, error);
     }
 
     /**
      * Adds a mesh to the attribute list of the meshes containing this triangle.
      * @param mesh
      *            the mesh to add
      * @return true if it was correctly added
      */
     public final boolean add(final Mesh mesh) {
         if (!this.meshes.contains(mesh)) {
             return this.meshes.add(mesh);
         }
         return false;
     }
 
     /**
      * Returns the attribute list of the meshes containing this triangle.
      * @return the attribute list of the meshes containing this triangle.
      */
     public final List<Mesh> getMeshes() {
         return this.meshes;
     }
 
     /**
      * Checks if this triangle is oriented as the vector with an error on the
      * angle. The error is in degree.
      * @param vector
      *            the vector to compare with
      * @param error
      *            the orientation error
      * @return true if its normal and the vector have the same orientation,
      *         false otherwise
      */
     public final boolean angle(final Vector3d vector, final double error) {
         return this.normal.angle(vector) * Edge.CONVERSION_PI_DEGREES < error;
     }
 
     /**
      * Checks if this triangle contains the edge e.
      * @param edge
      *            the edge to check
      * @return true if the edge e is one of the edges of this triangle, false
      *         otherwise.
      */
     public final boolean contains(final Edge edge) {
         for (Edge e : this.edges) {
             if (edge == e) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if p is one of the three points of this. Use the method equals of
      * this class.
      * @param point
      *            the point to check
      * @return true is one point is equal with p
      */
     public final boolean contains(final Point point) {
         for (final Edge edge : this.edges) {
             if (edge.contains(point)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the list of the points of this. Computes it from the list of
      * edges, ordering it from the direction of the normal
      * @return the list of points
      */
     public final List<Point> getPoints() {
         List<Point> points = new ArrayList<>();
 
         Point p1 = this.edges[0].getP1();
         Point p2 = this.edges[0].getP2();
         Vector3d edge0 = this.edges[0].convertToVector3d();
         Point p3 = null;
         try {
             if (this.edges[1].contains(p1)) {
                 p3 = this.edges[1].returnOther(p1);
             } else {
                 p3 = this.edges[2].returnOther(p1);
             }
         } catch (Exception exc) {
             if (this.edges[2].getP1() != p1 && this.edges[2].getP1() != p2) {
                 p3 = this.edges[2].getP1();
             } else if (this.edges[2].getP2() != p1
                     && this.edges[2].getP2() != p2) {
                 p3 = this.edges[2].getP2();
             }
         }
         Vector3d edge1 = new Vector3d(p3.getX() - p1.getX(), p3.getY()
                 - p1.getY(), p3.getZ() - p1.getZ());
 
         Vector3d vect = new Vector3d();
         vect.cross(edge0, edge1);
 
         points.add(p1);
 
         if (vect.dot(this.normal) > 0) {
             points.add(p2);
             points.add(p3);
         } else {
             points.add(p3);
             points.add(p2);
         }
 
         return points;
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public final boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (this.getClass() != obj.getClass()) {
             return false;
         }
         final Triangle other = (Triangle) obj;
 
         return this.contains(other.getE1()) && this.contains(other.getE2())
                 && this.contains(other.getE3());
     }
 
     /**
      * Getter.
      * @return the first edge
      */
     public final Edge getE1() {
         return this.edges[0];
     }
 
     /**
      * Getter.
      * @return the second edge
      */
     public final Edge getE2() {
         return this.edges[1];
     }
 
     /**
      * Getter.
      * @return the third edge
      */
     public final Edge getE3() {
         return this.edges[2];
     }
 
     /**
      * Getter.
      * @return a collection containing the three edges
      */
     public final Collection<Edge> getEdges() {
         return Arrays.asList(this.edges);
     }
 
     /**
      * Returns the neighbours of this triangle. Look in the edges to find the
      * other triangles which share those edges.
      * @return a list of the neighbours triangles
      */
     public final List<Triangle> getNeighbours() {
         final List<Triangle> list = new ArrayList<>();
         Triangle other;
 
         for (final Edge e : this.edges) {
             other = e.returnOther(this);
             if (other != null) {
                 list.add(other);
             }
         }
 
         return list;
     }
 
     /**
      * Getter.
      * @return the normal
      */
     public final Vector3d getNormal() {
         return this.normal;
     }
 
     /**
      * Returns the number of neighbours of this triangle. Two triangles are
      * neighbours if they share one edge.
      * @return the number of neighbours
      */
     public final int getNumNeighbours() {
 
         int number = 0;
         for (final Edge e : this.edges) {
             number += e.getNumberTriangles() - 1;
         }
 
         return number;
     }
 
     /**
      * Getter.
      * @return the first point
      */
     public final Point getP1() {
         return this.getPoints().get(0);
     }
 
     /**
      * Getter.
      * @return the second point
      */
     public final Point getP2() {
         return this.getPoints().get(1);
     }
 
     /**
      * Getter.
      * @return the third point
      */
     public final Point getP3() {
         return this.getPoints().get(2);
     }
 
     /**
      * Returns a collection of Double composed of the coordinates of the points.
      * @return a collection of the coordinates of the points
      */
     @SuppressWarnings("boxing")
     public final List<Double> getPointsAsCoordinates() {
         final List<Double> list = new ArrayList<>();
         for (final Point p : this.getPoints()) {
             for (final double d : p.getPointAsCoordinates()) {
                 list.add(d);
             }
         }
         return list;
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public final int hashCode() {
         List<Point> points = this.getPoints();
         return points.get(0).hashCode() + points.get(1).hashCode()
                 + points.get(2).hashCode();
     }
 
     /**
      * Checks if this triangle is located between two planes. These planes are
      * normal to the vector, and are located from each side of the point with a
      * distance to the point equal to the error.
      * @param vect
      *            the vector normal of the two planes
      * @param p
      *            the point which locates the planes in space. It's at the
      *            middle of the two planes.
      * @param error
      *            the distance between the planes and the point
      * @return true if the first point of the triangle is located between those
      *         two planes, false otherwise.
      */
     public final boolean isInPlanes(final Vector3d vect, final Point p,
             final double error) {
 
         final Edge axisNormalGround = new Edge(new Point(0, 0, 0), new Point(
                 vect.x, vect.y, vect.z));
 
         final Point pAverage = axisNormalGround.project(p);
 
         final Point projectedPoint = axisNormalGround.project(this.getP1());
 
         return projectedPoint.distance(pAverage) < error;
     }
 
     /**
      * Checks if a triangle is a neighbour of this triangle.
      * @param triangle
      *            the triangle to check
      * @return true if they share an edge, false otherwise.
      */
     public final boolean isNeighboor(final Triangle triangle) {
 
         Triangle other;
         for (final Edge e : this.edges) {
             other = e.returnOther(this);
             if (other == triangle) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Checks if the vector is normal to the normal of this triangle, with an
      * error. Caution : this error is not in degrees ! The error is compared
      * with the result of a dot product. Then it must be between 0 and 1.
      * @param norm
      *            the vector to compare with
      * @param error
      *            the orientation error
      * @return true if this triangle is normal to norm with an error, false
      *         otherwise.
      */
     public final boolean isNormalTo(final Vector3d norm, final double error) {
         return this.normal.dot(norm) < error && this.normal.dot(norm) > -error;
     }
 
     /**
      * Recursive method which returns in ret all the neighbours (and the
      * neighbours of these neighbours...) of this triangle which belong to the
      * container.
      * @param ret
      *            the mesh in which are returned the triangles
      * @param container
      *            the mesh which must contain all the triangles
      */
     public final void returnNeighbours(final Mesh ret, final Mesh container) {
 
         // Add this triangle.
         ret.add(this);
 
         Triangle other;
 
         // For each neighbour, ...
         for (final Edge e : this.edges) {
             other = e.returnOther(this);
             // ... Which is not yet in ret, and which belongs to container ...
             if (other != null && container.contains(other)
                     && !ret.contains(other)) {
 
                 // ... Calls this method on this neighbour.
                 other.returnNeighbours(ret, container);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public final String toString() {
         List<Point> points = this.getPoints();
         return points.get(0).toString() + "\n" + points.get(1).toString()
                 + "\n" + points.get(2).toString() + "\n"
                 + this.normal.toString();
     }
 
     /**
      * Calculates the average x-coordinate of the three points.
      * @return the average x-coordinate of the three points
      */
     public final double xAverage() {
         List<Point> points = this.getPoints();
         return (points.get(0).getX() + points.get(1).getX() + points.get(2)
                 .getX()) / Triangle.NB_VERTICES;
     }
 
     /**
      * Calculates the x-maximum of the three points.
      * @return the x-maximum of the three points
      */
     public final double xMax() {
         List<Point> points = this.getPoints();
         return Math.max(points.get(0).getX(),
                 Math.max(points.get(1).getX(), points.get(2).getX()));
     }
 
     /**
      * Calculates the point at the x-maximum of the three points.
      * @return the point at the x-maximum of the three points.
      */
     public final Point xMaxPoint() {
         final double xMax = this.xMax();
         for (final Point p : this.getPoints()) {
             if (p.getX() == xMax) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Calculates the x-minimum of the three points.
      * @return the x-minimum of the three points
      */
     public final double xMin() {
         List<Point> points = this.getPoints();
         return Math.min(points.get(0).getX(),
                 Math.min(points.get(1).getX(), points.get(2).getX()));
     }
 
     /**
      * Calculates the point at the x-minimum of the three points.
      * @return the point at the x-minimum of the three points.
      */
     public final Point xMinPoint() {
         final double xMin = this.xMin();
         for (final Point p : this.getPoints()) {
             if (p.getX() == xMin) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Calculates the average y-coordinate of the three points.
      * @return the average y-coordinate of the three points
      */
     public final double yAverage() {
         List<Point> points = this.getPoints();
         return (points.get(0).getY() + points.get(1).getY() + points.get(2)
                 .getY()) / Triangle.NB_VERTICES;
     }
 
     /**
      * Calculates the y-maximum of the three points.
      * @return the y-maximum of the three points
      */
     public final double yMax() {
         List<Point> points = this.getPoints();
         return Math.max(points.get(0).getY(),
                 Math.max(points.get(1).getY(), points.get(2).getY()));
     }
 
     /**
      * Calculates the point at the y-maximum of the three points.
      * @return the point at the y-maximum of the three points.
      */
     public final Point yMaxPoint() {
         final double yMax = this.yMax();
         for (final Point p : this.getPoints()) {
             if (p.getY() == yMax) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Calculates the y-minimum of the three points.
      * @return the y-minimum of the three points
      */
     public final double yMin() {
         List<Point> points = this.getPoints();
         return Math.min(points.get(0).getY(),
                 Math.min(points.get(1).getY(), points.get(2).getY()));
     }
 
     /**
      * Calculates the point at the y-minimum of the three points.
      * @return the point at the y-minimum of the three points.
      */
     public final Point yMinPoint() {
         final double yMin = this.yMin();
         for (final Point p : this.getPoints()) {
             if (p.getY() == yMin) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Calculates the average z-coordinate of the three points.
      * @return the average z-coordinate of the three points
      */
     public final double zAverage() {
         List<Point> points = this.getPoints();
         final double zAverage = points.get(0).getZ() + points.get(1).getZ()
                 + points.get(2).getZ();
         return zAverage / Triangle.NB_VERTICES;
     }
 
     /**
      * Calculates the z-maximum of the three points.
      * @return the z-maximum of the three points
      */
     public final double zMax() {
         List<Point> points = this.getPoints();
         return Math.max(points.get(0).getZ(),
                 Math.max(points.get(1).getZ(), points.get(2).getZ()));
     }
 
     /**
      * Calculates the point at the z-maximum of the three points.
      * @return the point at the z-maximum of the three points.
      */
     public final Point zMaxPoint() {
         final double zMax = this.zMax();
         for (final Point p : this.getPoints()) {
             if (p.getZ() == zMax) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Calculates the z-minimum of the three points.
      * @return the z-minimum of the three points
      */
     public final double zMin() {
         List<Point> points = this.getPoints();
         return Math.min(points.get(0).getZ(),
                 Math.min(points.get(1).getZ(), points.get(2).getZ()));
     }
 
     /**
      * Calculates the point at the z-minimum of the three points.
      * @return the point at the z-minimum of the three points.
      */
     public final Point zMinPoint() {
         final double zMin = this.zMin();
         for (final Point p : this.getPoints()) {
             if (p.getZ() == zMin) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Setter.
      * @param e
      *            the new edge
      */
     public final void setE1(final Edge e) {
         this.synchronizeBeginning();
         this.edges[0] = e;
         this.synchronizeEnd();
     }
 
     /**
      * Setter.
      * @param e
      *            the new edge
      */
     public final void setE2(final Edge e) {
         this.synchronizeBeginning();
         this.edges[1] = e;
         this.synchronizeEnd();
     }
 
     /**
      * Setter.
      * @param e
      *            the new edge
      */
     public final void setE3(final Edge e) {
         this.synchronizeBeginning();
         this.edges[2] = e;
         this.synchronizeEnd();
     }
 
     /**
      * Replaces an old edge by a new one.
      * @param eOld
      *            the old edge
      * @param eNew
      *            the new one
      */
     public final void replace(final Edge eOld, final Edge eNew) {
         if (this.edges[0] == eOld) {
             this.synchronizeBeginning();
 
             eOld.removeTriangle(this);
             eNew.addTriangle(this);
 
             this.edges[0] = eNew;
 
             this.synchronizeEnd();
 
         } else if (this.edges[1] == eOld) {
             this.synchronizeBeginning();
 
             eOld.removeTriangle(this);
             eNew.addTriangle(this);
 
             this.edges[1] = eNew;
 
             this.synchronizeEnd();
 
         } else if (this.edges[2] == eOld) {
             this.synchronizeBeginning();
 
             eOld.removeTriangle(this);
             eNew.addTriangle(this);
 
             this.edges[2] = eNew;
 
             this.synchronizeEnd();
         }
     }
 
     /**
      * Begins the synchronization by removing this triangle from all the meshes
      * which contain it.
      */
     public final void synchronizeBeginning() {
         for (Mesh m : this.meshes) {
             m.remove(this);
         }
     }
 
     /**
      * Ends the synchronization by adding this triangle from all the meshes
      * which have to contain it.
      */
     public final void synchronizeEnd() {
         for (Mesh m : this.meshes) {
             m.add(this);
         }
     }
 
     /**
      * Removes a mesh from the attribute list of meshes.
      * @param mesh
      *            the mesh
      * @return true if it was done correctly, false otherwise
      */
     public final boolean remove(final Mesh mesh) {
         return this.meshes.remove(mesh);
     }
 
     /**
      * Computes the normal from the order of the points.
      */
     public final void recomputeNormal() {
        Vector3d norm = new Vector3d();
        norm.cross(this.edges[0].convertToVector3d(),
                this.edges[1].convertToVector3d());
        if (norm.dot(this.normal) < 0) {
            norm.negate();
        }
        this.normal.set(norm);
     }
 
     /**
      * Computes the area of the triangle, using the Héron formula.
      * @return the area of the triangle
      */
     public final double computeArea() {
         // Héron formula.
         double a = this.edges[0].length();
         double b = this.edges[1].length();
         double c = this.edges[2].length();
         double s = 0.5 * (a + b + c);
         return Math.sqrt(s * (s - a) * (s - b) * (s - c));
     }
 
     /**
      * Projects a point on the triangle, and returns the projection.
      * @param p
      *            the point to project
      * @return the projected point
      */
     public final Point project(final Point p) {
         double a = this.normal.getX();
         double b = this.normal.getY();
         double c = this.normal.getZ();
         double k1 = (p.getX() - this.getP1().getX()) * a
                 + (p.getY() - this.getP1().getY()) * b
                 + (p.getZ() - this.getP1().getZ()) * c;
         double k = -k1 / (a * a + b * b + c * c);
 
         return new Point(p.getX() + a * k, p.getY() + b * k, p.getZ() + c * k);
     }
 
     /**
      * Checks if the point is inside this.
      * @param d
      *            the point
      * @return true if it is, false if not
      */
     public final boolean isInside(final Point d) {
         Point a = this.getP1();
         Point b = this.getP2();
         Point c = this.getP3();
 
         if (d.equals(a) || d.equals(b) || d.equals(c)) {
             return true;
         }
 
         Vector3d ab = new Vector3d(b.getX() - a.getX(), b.getY() - a.getY(),
                 b.getZ() - a.getZ());
         Vector3d bc = new Vector3d(c.getX() - b.getX(), c.getY() - b.getY(),
                 c.getZ() - b.getZ());
         Vector3d ca = new Vector3d(a.getX() - c.getX(), a.getY() - c.getY(),
                 a.getZ() - c.getZ());
 
         Vector3d ad = new Vector3d(d.getX() - a.getX(), d.getY() - a.getY(),
                 d.getZ() - a.getZ());
         Vector3d bd = new Vector3d(d.getX() - b.getX(), d.getY() - b.getY(),
                 d.getZ() - b.getZ());
         Vector3d cd = new Vector3d(d.getX() - c.getX(), d.getY() - c.getY(),
                 d.getZ() - c.getZ());
 
         Vector3d cross1 = new Vector3d();
         Vector3d cross2 = new Vector3d();
         Vector3d cross3 = new Vector3d();
 
         cross1.cross(ad, ab);
         cross2.cross(bd, bc);
         cross3.cross(cd, ca);
 
         double dot1 = cross1.dot(cross2);
         double dot2 = cross1.dot(cross3);
         double dot3 = cross2.dot(cross3);
 
         return (dot1 > 0 && dot2 > 0 && dot3 > 0)
                 || (dot1 < 0 && dot2 < 0 && dot3 < 0);
     }
 }
