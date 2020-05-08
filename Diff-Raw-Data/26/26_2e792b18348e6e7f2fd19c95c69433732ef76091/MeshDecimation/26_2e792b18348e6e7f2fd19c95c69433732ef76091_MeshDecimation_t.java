 package fr.nantes1900.models.decimation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Vector3d;
 
 import Jama.Matrix;
 import fr.nantes1900.models.basis.Edge;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Point;
 import fr.nantes1900.models.basis.Triangle;
 
 /**
  * Extends a Mesh, with new methods to decimate the mesh. It follows the rules
  * of the algorithm Quadric Error Metrics.
  * @author Daniel Lefèvre
  */
 public class MeshDecimation extends Mesh {
 
     /**
      * Serial version UID.
      */
     private static final long serialVersionUID = -6418895519215700898L;
 
     /**
      * The map which associates each point with its qi matrix.
      */
     protected Map<Point, Matrix> mapPM = new HashMap<>();
     /**
      * The map which associates each point with the triangles it belongs to.
      */
     protected Map<Point, List<Triangle>> mapPLT = new HashMap<>();
     /**
      * The map which associated each edge with the collapse error of this edge.
      */
     protected Map<Edge, Double> mapED = new HashMap<>();
 
     /**
      * The current list of the edges which can be collapsed.
      */
     private List<Edge> currentEdges = new ArrayList<>();
 
     /**
      * Constructor. Builds the maps, by finding every triangle associated to
      * every point. It can take a little time.
      * @param c
      *            the mesh in input
      */
     public MeshDecimation(final Collection<? extends Triangle> c) {
         super();
 
         for (Triangle t : c) {
             t.add(this);
             this.add(t);
 
             for (Point p : t.getPoints()) {
                 List<Triangle> l = this.mapPLT.get(p);
                 if (l == null) {
                     l = new ArrayList<>(2);
                     this.mapPLT.put(p, l);
                 }
                 if (!l.contains(t)) {
                     l.add(t);
                 }
             }
         }
         this.currentEdges = this.getEdges();
     }
 
     /**
      * Collapses the edge, replacing it with the new point. Replaces the
      * modified points in every triangles and edges.
      * @param e
      *            the edge to collapse.
      * @param pNew
      *            the point which will replace the edge.
      * @return the list of edges wich have been modified
      */
     public final List<Edge>
             collapseMinusCostPair(final Edge e, final Point pNew) {
 
         Point p1 = e.getP1();
         Point p2 = e.getP2();
 
         // Removes e.
         this.currentEdges.remove(e);
 
         // Adds pNew in the map.
         List<Triangle> triangles = new ArrayList<>();
         triangles.addAll(this.mapPLT.get(p1));
         triangles.addAll(this.mapPLT.get(p2));
         triangles.removeAll(e.getTriangles());
 
         // Tests of capability.
         // If there is not enough triangles...
         if (triangles.size() < 4) {
             return new ArrayList<>();
         }
 
         // If one of the new triangles will have a normal direction change after
         // the edge-collapse, we cannot continue.
         for (Triangle triangle : triangles) {
             List<Point> points = triangle.getPoints();
             if (points.contains(p1)) {
                 points.remove(p1);
                 Vector3d vect1 = new Vector3d(new Edge(points.get(0),
                         points.get(1)).convertToVector3d());
                 Vector3d vect2 = new Vector3d(
                         new Edge(points.get(0), p1).convertToVector3d());
                 Vector3d cross1 = new Vector3d();
                 cross1.cross(vect1, vect2);
 
                 Vector3d vect3 = new Vector3d(
                         new Edge(points.get(0), pNew).convertToVector3d());
                 Vector3d cross2 = new Vector3d();
                 cross2.cross(vect1, vect3);
 
                 if (cross1.dot(cross2) < 0) {
                     return new ArrayList<>();
                 }
             } else if (points.contains(p2)) {
                 points.remove(p2);
                 Vector3d vect1 = new Vector3d(new Edge(points.get(0),
                         points.get(1)).convertToVector3d());
                 Vector3d vect2 = new Vector3d(
                         new Edge(points.get(0), p2).convertToVector3d());
                 Vector3d cross1 = new Vector3d();
                 cross1.cross(vect1, vect2);
 
                 Vector3d vect3 = new Vector3d(
                         new Edge(points.get(0), pNew).convertToVector3d());
                 Vector3d cross2 = new Vector3d();
                 cross2.cross(vect1, vect3);
 
                 if (cross1.dot(cross2) < 0) {
                     return new ArrayList<>();
                 }
             }
         }
 
         // If the two neighbours of one of the triangle which will be removed
         // are neighbours themselves.
         for (Triangle triangle : e.getTriangles()) {
             List<Triangle> list = triangle.getNeighbours();
             list.removeAll(e.getTriangles());
             if (list.size() != 2 || list.get(0).isNeighboor(list.get(1))) {
                 return new ArrayList<>();
             }
         }
 
         // If a point already belongs to the map, it means there is an error
         // somewhere.
         if (this.mapPLT.containsKey(pNew)) {
             return new ArrayList<>();
         }
         // End of the capability tests.
 
         this.mapPLT.put(pNew, triangles);
 
         // List every edge containing p1 or p2, except e.
         List<Edge> edges = new ArrayList<>();
         for (Triangle t : triangles) {
             for (Edge edg : t.getEdges()) {
                 if (edg != e && !edges.contains(edg)
                         && (edg.contains(p1) || edg.contains(p2))) {
                     edges.add(edg);
                 }
             }
         }
 
         // Removes the corresponding triangles.
         this.removeAll(e.getTriangles());
         for (Edge edg : edges) {
             edg.removeTriangles(e.getTriangles());
             for (Point point : edg.getPoints()) {
                 this.mapPLT.get(point).removeAll(e.getTriangles());
             }
         }
 
         // Removes p1, p2, from the map.
         this.mapPLT.remove(p1);
         this.mapPLT.remove(p2);
 
         // Replaces p1, p2 with pNew in the edges.
         for (Edge edg : edges) {
             edg.replace(p1, pNew);
             edg.replace(p2, pNew);
         }
 
         // Merge 4 edges of the old triangle, 2 by 2.
         Map<Edge, Edge> eMap = new HashMap<>();
         for (Triangle t : triangles) {
             for (Edge e1 : t.getEdges()) {
                 Edge e2 = eMap.get(e1);
                 if (e2 == null) {
                     eMap.put(e1, e1);
                 } else if (e1 != e2) {
                     t.replace(e1, e2);
                     // It removes two times because the mechanism of remove
                     // in Lists is quite not perfect.
                     this.currentEdges.remove(e1);
                     this.currentEdges.remove(e1);
                 }
             }
         }
 
         // Recompute normals.
         for (Triangle t : triangles) {
             t.recomputeNormal();
         }
 
         return edges;
     }
 
     /**
      * Computes the error associated to the edge, following the rules of the
      * algorithm. It puts the result in the Map of this class.
      * @param edge
      *            the edge to compute error to
      */
     public final void computeError(final Edge edge) {
         Point p1 = edge.getP1();
         Point p2 = edge.getP2();
 
         Matrix qplus = this.mapPM.get(p1).plus(this.mapPM.get(p2));
 
         Matrix mat = new Matrix(new double[][]{
                 {qplus.get(0, 0), qplus.get(1, 0), qplus.get(2, 0),
                         qplus.get(3, 0)},
                 {qplus.get(0, 1), qplus.get(1, 1), qplus.get(2, 1),
                         qplus.get(3, 1)},
                 {qplus.get(0, 2), qplus.get(1, 2), qplus.get(2, 2),
                         qplus.get(3, 2)}, {0, 0, 0, 1}});
 
         Matrix id = new Matrix(new double[][]{{0}, {0}, {0}, {1}});
         Matrix vnew = mat.inverse().times(id);
 
         Matrix qnew = vnew.transpose().times(qplus).times(vnew);
 
         this.mapED.put(edge, new Double(qnew.get(0, 0)));
     }
 
     /**
      * Computes the position of the new vertex created by collapsing the edge,
      * following the rules of the algorithm.
      * @param edge
      *            the edge to collapse
      * @return the point representing the new vertex
      */
     public final Point computeNewVertex(final Edge edge) {
         Point p1 = edge.getP1();
         Point p2 = edge.getP2();
 
         Matrix qplus = this.mapPM.get(p1).plus(this.mapPM.get(p2));
 
         Matrix mat = new Matrix(new double[][]{
                 {qplus.get(0, 0), qplus.get(1, 0), qplus.get(2, 0),
                         qplus.get(3, 0)},
                 {qplus.get(0, 1), qplus.get(1, 1), qplus.get(2, 1),
                         qplus.get(3, 1)},
                 {qplus.get(0, 2), qplus.get(1, 2), qplus.get(2, 2),
                         qplus.get(3, 2)}, {0, 0, 0, 1}});
 
         Matrix id = new Matrix(new double[][]{{0}, {0}, {0}, {1}});
         Matrix vnew = mat.inverse().times(id);
 
         return new Point(vnew.get(0, 0), vnew.get(1, 0), vnew.get(2, 0));
     }
 
     /**
      * Computes all the qi matrices for every point.
      */
     public final void computeQiMatrices() {
         for (Triangle t : this) {
             for (Point p : t.getPoints()) {
                 this.computeQiMatrix(p);
             }
         }
     }
 
     /**
      * Compute the qi matrix for one point, following the rules of the
      * algorithm. It puts the result in the Map of this class.
      * @param point
      *            the point to compute matrix to.
      */
     public final void computeQiMatrix(final Point point) {
         Matrix qi = new Matrix(4, 4);
 
         for (Triangle tri : this.mapPLT.get(point)) {
             double a = tri.getNormal().getX();
             double b = tri.getNormal().getY();
             double c = tri.getNormal().getZ();
             double d = -tri.getP1().getX() * a - tri.getP1().getY() * b
                     - tri.getP1().getZ() * c;
 
             Matrix kp = new Matrix(new double[][]{{a * a, a * b, a * c, a * d},
                     {a * b, b * b, b * c, b * d}, {c * a, c * b, c * c, c * d},
                     {d * a, d * b, d * c, d * d}});
 
             qi.plusEquals(kp);
         }
 
         this.mapPM.put(point, qi);
     }
 
     /**
      * Returns the number of edges which can be collapsed.
      * @return the size of the attribute currentEdges.
      */
     public final int getEdgeNumber() {
         return this.currentEdges.size();
     }
 
     /**
      * Selects the edge which has the minimal error. It is then the edge which
      * will be collapsed.
      * @return the edge to collapse.
      */
     public final Edge selectMinimalErrorPair() {
         Collections.sort(this.currentEdges, new Comp());
         return this.currentEdges.get(0);
     }
 
     /**
      * Selects the edges which are not too close from the borders (do not
      * contains a point of the border). It puts those in the attribute
      * currentEdges.
      */
     public final void selectValidPairs() {
         List<Edge> bounds = this.returnUnsortedBorders();
 
         for (Triangle t : this) {
             for (Edge e : bounds) {
                 if (t.contains(e.getP1()) || t.contains(e.getP2())) {
                     this.currentEdges.removeAll(t.getEdges());
                 }
             }
         }
     }
 
     /**
      * For each edges which can be collapsed, compute the error associated.
      */
     public final void computeErrors() {
         for (Edge e : this.currentEdges) {
             this.computeError(e);
         }
     }
 
     /**
      * For each edges and points, updates the matrices and the errors.
      * @param edges
      *            the list of edges previously modified, which need to be
      *            updated.
      */
     public final void updateMatricesAndErrors(final List<Edge> edges) {
         for (Edge e : edges) {
             this.computeQiMatrix(e.getP1());
             this.computeQiMatrix(e.getP2());
             this.computeError(e);
         }
     }
 
     /**
      * Implements a comparator for two edges, considering the error associated.
      * @author Daniel Lefèvre
      */
     private class Comp implements Comparator<Edge> {
 
         /**
          * Constructor.
          */
         public Comp() {
         }
 
         /*
          * (non-Javadoc)
          * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
          */
         @SuppressWarnings("boxing")
         @Override
         public int compare(final Edge o1, final Edge o2) {
             if (MeshDecimation.this.mapED.get(o1) > MeshDecimation.this.mapED
                     .get(o2)) {
                 return 1;
             } else if (MeshDecimation.this.mapED.get(o1) < MeshDecimation.this.mapED
                     .get(o2)) {
                 return -1;
             } else {
                 return 0;
             }
         }
     }
 }
