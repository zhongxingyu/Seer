 package computational_geometry.model.data_structures;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import computational_geometry.model.beans.Point;
 import computational_geometry.model.beans.Segment;
 
 /**
  * Double Connected Edge list data structure
  * @author eloi
  *
  */
 public class DCEL {
 
     private List<Edge> edges;
     private List<Face> faces;
 
     public DCEL() {
         edges = new ArrayList<DCEL.Edge>();
         faces = new ArrayList<DCEL.Face>();
     }
 
     public class Vert {
         protected Point p;
         protected Edge edge;
 
        @Override
         public String toString() {
             return "vert.p=" + p;
         }
 
         public Vert(Point p, Edge e) {
             this.p = p;
             this.edge = e;
         }
 
         public Point getPoint() {
             return p;
         }
 
         public Edge getEdge() {
             return edge;
         }
     }
 
     public class Edge {
         public boolean isDrawn; // TODO try to remove this field
         protected Vert origin;
         protected Edge twin;
         protected Face face;
         protected Edge next;
         protected Edge prev;
 
        @Override
         public String toString() {
            StringBuilder sb = new StringBuilder("edge_seg=");
            if (getSegment() == null) {
                sb.append("null");
            } else {
                sb.append(getSegment().toString());
            }
            return sb.toString();
         }
 
         public void fill(Vert origin, Edge twin, Face face, Edge next, Edge prev) {
             this.origin = origin;
             this.twin = twin;
             this.face = face;
             this.next = next;
             this.prev = prev;
         }
 
         public Segment getSegment() {
            if (origin == null || twin == null || twin.origin == null) {
                return null;
            } else {
                return new Segment(origin.p, twin.origin.p);
            }
         }
 
         public Vert getOrigin() {
             return origin;
         }
 
         public void setOrigin(Vert origin) {
             this.origin = origin;
         }
 
         public Edge getTwin() {
             return twin;
         }
 
         public Face getFace() {
             return face;
         }
 
         public Edge getNext() {
             return next;
         }
 
         public void setNext(Edge next) {
             this.next = next;
         }
 
         public Edge getPrev() {
             return prev;
         }
 
         public void setPrev(Edge prev) {
             this.prev = prev;
         }
     }
 
     public class Face {
         protected Edge edge;
 
         public Face(Edge e) {
             this.edge = e;
         }
 
         public Edge getEdge() {
             return edge;
         }
 
         public void setEdge(Edge edge) {
             this.edge = edge;
         }
     }
 
     /**
      * Checks if this HalfEdge is valid
      * TODO : write this method and detail description
      * @return
      */
     public boolean isValid() {
         return true;
     }
 
     public void addEdge(Edge e) {
         edges.add(e);
     }
 
     public void removeEdge(Edge e) {
         edges.remove(e);
     }
 
     public void addFace(Face f) {
         faces.add(f);
     }
 
     public void removeFace(Face f) {
         faces.remove(f);
     }
 
     /**
      * Get an iterator for edges, so we can iterate on them
      * without having the actual list and messing around with it
      * @return
      */
     public Iterator<Edge> getEdgeIterator() {
         return edges.iterator();
     }
 
     /**
      * Get an iterator for faces, so we can iterate on them
      * without having the actual list and messing around with it
      * @return
      */
     public Iterator<Face> getFaceIterator() {
         return faces.iterator();
     }
 
 }
