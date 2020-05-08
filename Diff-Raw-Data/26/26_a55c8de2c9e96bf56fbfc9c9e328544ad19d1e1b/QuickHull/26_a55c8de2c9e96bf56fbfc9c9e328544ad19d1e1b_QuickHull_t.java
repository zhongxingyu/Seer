 package convexhull.algorithms;
 
 import convexhull.datastructures.LinkedListNode;
 import convexhull.datastructures.LinkedList;
 import convexhull.main.ConvexHull;
 import java.awt.geom.Point2D;
 
 /**
  *
  * Class for QuickHull algo Wikipedia: Quick Hull is a method of computing the
  * convex hull of a finite set of points in the plane. It uses a divide and
  * conquer approach similar to that of QuickSort, which its name derives from.
  * Its average case complexity is considered to be O(n * log(n)), whereas in the
  * worst case it takes O(n2) (quadratic).
  *
  * @author Aleksi Markkanen
  */
 public class QuickHull implements Algorithm {
 
     /**
      * Runs a QuickHull algorithm on the input points.
      *
      * @param points Set of points whose convex hull needs to be computed
      * @return Points that constitute the convex hull of the input set
      */
     @Override
     public LinkedList useAlgorithm(LinkedList points) {
         ConvexHull.startTimer();
         Point2D.Double minX = null;
         Point2D.Double maxX = null;
         Point2D.Double current = null;
         LinkedListNode node = points.getHead();
         while (node != null) { // Find the points with the smallest and greatest X coordinates
             current = node.getPoint();
             if (minX == null || minX.getX() > current.getX()) {
                 minX = current;
             }
             if (maxX == null || maxX.getX() < current.getX()) {
                 maxX = current;
             }
             node = node.getNext();
         }
 
         // Next, split the data into two lists depending on which side of the line (minX,maxX) they lie.
         // Points that lie on the line may be ignored.
 
         LinkedList positive = new LinkedList();
         LinkedList negative = new LinkedList();
 
         node = points.getHead();
         double sign;
         positive.insert(minX);
         positive.insert(maxX);
         negative.insert(maxX);
         negative.insert(minX);
 
         while (node != null) { // iterate over all points and split them
             sign = checkRotation(minX, maxX, node.getPoint());
             if (sign < 0) {
                 negative.insert(node.getPoint());
 
             }
             if (sign > 0) {
                 positive.insert(node.getPoint());
             }
             node = node.getNext();
         }
 
 
         LinkedList pos = iterate(positive);
         if (pos != null) {
             node = pos.getHead();
         }
         LinkedList neg = iterate(negative);
         while (node != null) {
             neg.insert(node.getPoint());
             node = node.getNext();
         }
         // Insert the first point as the last, thus closing the polygon.
         // This is in compliance with the Matlab output of the convex hull.
         neg.insert(neg.getHead().getPoint());
         System.out.println("Algorithm ran in " + ConvexHull.stopTimer() + "ms");
         return neg;
 
     }
 
     /**
      * Takes three points and returns their orientation w.r. each other
      *
      * @param A First point
      * @param B Second point
      * @param P Third point
      * @return Value is negative if points are given in clockwise orientation,
      * positive if counter-clockwise and zero if the points lie on the same line
      */
     private Double checkRotation(Point2D.Double A, Point2D.Double B, Point2D.Double P) {
 
         return (A.getX() * (B.getY() - P.getY()) + B.getX() * (P.getY() - A.getY()) + P.getX() * (A.getY() - B.getY()));
 
     }
 
     /**
      * Method takes as input points. First two, A and B, are parts of the convex
      * hull. Then we find a point P = argmax(dist(AB,P)). P is now a part of the
      * convex hull. Then the data set is split and iterate calls itself
      * recursively until the points are exhausted.
      *
      * @param points Input points
      * @return Convex hull of the inputted list of points
      */
     private LinkedList iterate(LinkedList points) {
 
         // Check for trivial input.
         if (points.getLength() < 4) {
             return trivialHull(points);
         }
 
         // Aux. variables
         LinkedListNode head = points.getHead();
         Point2D.Double A = head.getPoint();
         Point2D.Double B = head.getNext().getPoint();
 
         LinkedListNode current;
         LinkedList negative = new LinkedList();
         LinkedList positive = new LinkedList();
         
         double dist;
         double maxDist = 0;
         double sign;
 
         // Find a point from the inserted list that has the maximum distance from the line AB     
         Point2D.Double P = findPivotPoint(A, B, head);
         // Triangle ABP is the "pivot triangle"; lines AP and BP divide the dataset,
         // if extended indefinitely.
         // P is part of the convex hull
        
        // If no candidates are found, we return only the endpoints of the line AB and
        // terminate the recursion.
        // Pathological case.
        if(P == null) {
            positive.insert(A);
            positive.insert(B);
            return positive;
        }
         current = head;
         positive.insert(A);
         positive.insert(P);
         negative.insert(P);
         negative.insert(B);
 
         // We loop through all the inserted points.
         // Let C be our current point. If triangle APC is traversed clockwise,
         // C can contribute to the convex hull. Otherwise, it can be discarded.
 
         // Same is true for the triangle PBC.
         while (current != null) {
             if (checkRotation(A, P, current.getPoint()) > 0) {
                 positive.insert(current.getPoint());
             }
             if (checkRotation(P, B, current.getPoint()) > 0) {
                 negative.insert(current.getPoint());
             }
             current = current.getNext();
         }
 
         // We have divided the dataset with respect to the lines AP and BP.
         // Now we recursively iterate.
 
         LinkedList res1 = iterate(positive);
         LinkedList res2 = iterate(negative);
 
         // Merge results.
 
         LinkedList result = new LinkedList();
         current = res1.getHead();
 
         while (current != null) {
             result.insert(current.getPoint());
             current = current.getNext();
         }
         current = res2.getHead();
         while (current != null) {
             result.insert(current.getPoint());
             current = current.getNext();
         }
 
         return result;
     }
 
     /**
      * If input for iterate() is trivial, this method is called
      *
      * @param points List of points of length <4.
      * @return Empty list for <1 point, otherwise trivial hulls of the input
      * list.
      */
     private LinkedList trivialHull(LinkedList points) {
         LinkedList res = new LinkedList();
         if (points.getLength() < 2) {
             return res;
         } else if (points.getLength() == 2) {
             res.insert(points.getHead().getNext().getPoint());
             return res;
         } else {
             res.insert(points.getHead().getNext().getNext().getPoint());
             res.insert(points.getHead().getNext().getPoint());
             return res;
         }
     }
 
     /**
     * Finds a point P from the inputted list so that P = argmax(dist(AB,P)).
     * If there is no P so that dist(AB,P) > 0, return null.
     * @param A First point
     * @param B Second point
     * @param head List
     * @return Point P, null if N/A
      */
     private Point2D.Double findPivotPoint(Point2D.Double A, Point2D.Double B, LinkedListNode head) {
         Point2D.Double tempPoint, P;
         double dist;
         double maxDist = 0;
         P = A;
         for (LinkedListNode current = head; current != null; current = current.getNext()) {
             tempPoint = current.getPoint();
             // This is not the normal Euclidean distance, but linearly equivalent to it.
             dist = Math.abs((tempPoint.getX() - A.getX()) * (B.getY() - A.getY()) - (tempPoint.getY() - A.getY()) * (B.getX() - A.getX()));
             if (maxDist < dist) {
                 maxDist = dist;
                 P = tempPoint;
             }
         }
        if (maxDist == 0) {
            return null;
        }
         return P;
     }
 }
