 package convexhull;
 
import convexhull.Edge;
 import java.awt.*;
 import java.util.Vector;
 
 /**
  * JarvisMarch.java
  * <p/>
  * Class to Implement the Jarvis's March Convex Hull Algorithm.
  *
  * @author Chet Mancini Wheaton College, CS 445, Fall 2008 Convex Hull Project Dec 4, 2008
  */
 
 public class JarvisMarch extends Algorithm {
 
     /**
      * A vector of vertices to pull from creating the hull.  Initially a copy of vertices from the graph.
      */
     Vector<Vertex> copy = new Vector<Vertex>();
 
     /**
      * A vector of vertices making up the convex hull.
      */
     Vector<Vertex> hull = new Vector<Vertex>();
 
     public JarvisMarch() {
     }
 
     /**
      * Get the next vertex going up the right side of the convex hull
      * <p/>
      * Finds the vertex with the smallest polar angle to the current one. Only look at vertices greater than the current y
      * coordinate (less than because flipped). Draw lines to vertices we're looking at in a red line. If a smaller polar angle is
      * found, save that one to return later.
      *
      * @param current the vertex currently on
      * @return the next vertex
      */
     private Vertex nextRight(Vertex current) {
         Vertex toReturn = copy.firstElement();
         double angle = 1000;
         if (current.polarAngleRad(toReturn) > 0) {
             angle = current.polarAngleRad(toReturn);
         }
         for (int i = 0; i < copy.size(); i++) {
             Vertex possible = copy.elementAt(i);
             if (possible.getY() < current.getY() && !possible.equals(toReturn)) {
                 pane.setPenColor(Color.RED);
                 graph.drawLine(current, possible);
                 pause(200);
                 pane.clearNoShow();
                 graph.drawAll();
                 double possibleAngle = current.polarAngleRad(possible);
                 if (possibleAngle < angle && possibleAngle >= 0) {
                     toReturn = possible;
                     angle = possibleAngle;
                 }
             }
         }
         return toReturn;
     }
 
     /**
      * Get the next vertex going down the left side of the convex hull
      * Finds the vertex with the largest (most negative) negative polar angle. Only look at vertices less than the current y
      * coordinate (greater than because flipped). Draw lines to vertices we're looking at in a red line. If a smaller polar angle is
      * found, save that one to return later.
      *
      * @param current the vertex currently on
      * @return the next vertex
      */
     private Vertex nextLeft(Vertex current) {
         Vertex toReturn = copy.firstElement();
         double angle = current.polarAngleRad(toReturn);
         for (int i = 0; i < copy.size(); i++) {
             Vertex possible = copy.elementAt(i);
             if (possible.getY() > current.getY() && !possible.equals(toReturn)) {
                 double possibleAngle = current.polarAngleRad(possible);
                 pane.setPenColor(Color.RED);
                 graph.drawLine(current, possible);
                 pause(200);
                 pane.clearNoShow();
                 graph.drawAll();
                 if (possibleAngle < angle && possibleAngle <= 0) {
                     toReturn = possible;
                     angle = possibleAngle;
                 }
             }
         }
         return toReturn;
     }
 
     /**
      * Get the vertex with the greatest y coordinate.
      */
     private Vertex getYMax() {
         Vertex ymax = copy.firstElement();
         for (int i = 0; i < copy.size(); i++) {
             if (copy.elementAt(i).getY() < ymax.getY()) {
                 ymax = copy.elementAt(i);
             }
         }
         return ymax;
     }
 
     /**
      * Run the algorithm Copy vertices into vector to work with. label all the vertices Set the pen radius for drawing lines. Get
      * the top vertex, which is where we will stop Go up to the top and then back down finding the next, adding it to the graph,
      * adding it to the hull, and pausing for animation. When the current vertex is back to our starting point, quit.
      */
     @Override
     public void runAlgorithm() {
 
         for (int i = 0; i < graph.vertices.size(); i++) {
             copy.add(graph.vertices.elementAt(i));
         }
 
         pause();
         graph.label();
         pane.setPenRadius(.005);
 
         Vertex ymax = getYMax();
 
         hull.add(copy.firstElement());
         boolean rightSide = true;
         Vertex current = hull.firstElement();
         do {
             if (rightSide) {
                 Vertex next = nextRight(current);
                 graph.addDispEdge(new Edge(current, next));
                 graph.addArc(current.getX(), current.getY(), 20, 0, Math.toDegrees(current.polarAngleRad(next)));
                 pause();
                 hull.add(next);
                 copy.remove(next);
                 current = next;
                 if (next.equals(ymax)) {
                     rightSide = false;
                 }
             }
             else {
                 Vertex next = nextLeft(current);
                 graph.addDispEdge(new Edge(current, next));
                 graph.addArc(current.getX(), current.getY(), 20, 180, Math.toDegrees(current.polarAngleRad(next)));
                 pause();
                 hull.add(next);
                 copy.remove(next);
                 current = next;
             }
         }
         while (!(current.equals(hull.elementAt(0))));
     }
 }
