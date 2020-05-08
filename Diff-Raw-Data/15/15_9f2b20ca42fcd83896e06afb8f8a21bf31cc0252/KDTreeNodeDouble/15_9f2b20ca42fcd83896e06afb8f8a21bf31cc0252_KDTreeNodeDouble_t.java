 package net.benrowland.kdtree;
 
 import net.benrowland.tree.Point;
 import net.benrowland.tree.PointDouble;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A Node of Point (of Double), in a KD Tree.
  *
 */
 public class KDTreeNodeDouble extends KDTreeNode<Double> {
 
     // *** Constructors
 
     public KDTreeNodeDouble() {}
 
     public KDTreeNodeDouble(KDTreeNodeDouble parent) {
         super(parent);
     }
 
     public KDTreeNodeDouble(Point<Double> elem) {
         super(elem);
     }
 
     // *** Static factory methods
 
     public static KDTreeNodeDouble buildRoot(List<PointDouble> points) {
         return buildNode(null, points, 0);
     }
 
     /**
      * Builds the current Node (and all below it).  The split axis alternates between X and Y.  The current
      * node is defined as the median point (in remaining points) in the current axis.
      *
      * @param parent
      * @param points
      * @param depth
      * @return
      */
     public static KDTreeNodeDouble buildNode(KDTreeNodeDouble parent, List<PointDouble> points, int depth) {
 
         if(points.isEmpty()) return null;
 
         // Create this node
         final KDTreeNodeDouble node = new KDTreeNodeDouble(parent);
         node.axis = (depth % 2 == 1) ? KDTreeNode.Axis.Y : KDTreeNode.Axis.X;
 
         // Sort points by split axis
         Collections.sort(points, comparatorMap.get(node.axis));
 
         // Find median in current axis, and add it to the Node.
         // todo probably needs fixing to take the median *value* rather than median *index*.
         int medianIndex = points.size() / 2;
         final PointDouble medianPoint = points.get(medianIndex);
         node.elem = medianPoint;
 
         // Build left and right children
         node.leftChild = buildNode(node, points.subList(0, medianIndex), depth+1);
         node.rightChild = buildNode(node, points.subList(medianIndex+1, points.size()), depth+1);
 
         return node;
     }
 
     // *** Member functions
 
     @Override
     protected boolean shouldTryOtherBranchForBetterMatch(Point<Double> point, KDTreeNode<Double> currentBest) {
         return point.distance(currentBest.getElem()) > distanceInAxis(point);
     }
 
     public Double distanceInAxis(Point<Double> point) {
         switch(axis) {
             case X:
                 return Math.abs(elem.getX() - point.getX());
             case Y:
                 return Math.abs(elem.getY() - point.getY());
             default:
                 throw new IllegalStateException("Unknown axis [" + axis + "]");
         }
     }
 
     @Override
     public int compare(Point<Double> other) {
         if (axis == Axis.X) {
             if(other.getX() < elem.getX()) {
                 return -1;
             }
             else {
                 return 1;
             }
         }
         else {
             if(other.getY() < elem.getY()) {
                 return -1;
             }
             else {
                 return 1;
             }
         }
     }
 
     public String toString() {
         return toString(0);
     }
 }
