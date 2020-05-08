 package cabbagegl;
 
 import java.util.*;
 
 // whoa
 
class KDTree {
     public static KDTree buildTree(List<Vector3> pts) {
         List<Vector3> ptsCpy = new ArrayList<Vector3>(pts);
         return new KDTree(buildTree(ptsCpy, 0));
     }
 
     private static KDNode buildTree(List<Vector3> pts, int depth) {
         KDNode retNode;
         if (pts.isEmpty()) {
             retNode = null;
         } else {
             retNode = new KDNode();
             int axis = depth % 3;
 
             // Find the median of the input points on axis
             Collections.sort(pts, new VectorAxisCompare(axis));
             int median = pts.size() / 2;
 
             // Build subtrees using data to left and right of median
             retNode.location = pts.get(median);
             retNode.left = buildTree(pts.subList(0, median), depth + 1);
             retNode.right = buildTree(pts.subList(median + 1, pts.size()),
                     depth + 1);
         }
 
         return retNode;
     }
     
     private KDNode root;
     private KDTree(KDNode iroot) {
         root = iroot;
     }
 
     public List<Vector3> nearestNeighbors(Vector3 point, int k) {
         List<Vector3> closest = new LinkedList<Vector3>();
         nearestNeighbors(root, null, point, k, 0, closest,
                 new LinkedList<Double>());
         return closest;
     }
 
     private void nearestNeighbors(KDNode n, KDNode oChild, Vector3 point, int k,
             int depth, List<Vector3> soFar, List<Double> dSoFar) {
         // Find the closest leaf node
         KDNode next;
         KDNode nOChild;
         int axis = depth % 3;
         Vector3 nodePt = n.location;
         double nodeAxis = nodePt.get(axis);
         double pntAxis = point.get(axis);
         if (pntAxis < nodeAxis) {
             next = n.left;
             nOChild = n.right;
         } else {
             next = n.right;
             nOChild = n.left;
         }
 
         if (next != null) {
             // Keep traversing the tree
             nearestNeighbors(next, nOChild, point, k, depth + 1, soFar, dSoFar);
         }
         assimilate(nodePt, point, k, soFar, dSoFar);
         // Walking back up the tree now
         double bestDist = dSoFar.get(dSoFar.size() - 1);
         double spDist = Math.abs(nodeAxis - pntAxis);
         // Walk the other side if there are potentially closer points there
         if (spDist < bestDist && oChild != null) {
             nearestNeighbors(oChild, null, point, k, depth, soFar, dSoFar); 
         }
     }
 
     private static void assimilate(Vector3 comp, Vector3 point, int k,
             List<Vector3> soFar, List<Double> dSoFar) {
         double compDist = comp.distTo(point);
         boolean added = false;
         // Insert in order
         for (int i = 0; i < soFar.size(); i++) {
             if (compDist < dSoFar.get(i)) {
                 soFar.add(i, comp);
                 dSoFar.add(i, compDist);
                 added = true;
                 break;
             }
         }
         // Add to the end of the list if it isn't added
         if (!added) {
             soFar.add(comp);
             dSoFar.add(compDist);
         }
 
         // Ensure the list isn't too big
         while (soFar.size() > k) {
             soFar.remove(k);
             dSoFar.remove(k);
         }
     }
 
 
     private static class KDNode {
         public KDNode left;
         public KDNode right;
         public Vector3 location;
     }
 
     private static class VectorAxisCompare implements Comparator<Vector3> {
         private int axis;
         public VectorAxisCompare(int iaxis) {
             axis = iaxis;
         }
 
         public int compare(Vector3 a, Vector3 b) {
             int ret = 0;
             double comp = a.get(axis) - b.get(axis);
             // Can't just cast because -.1 truncates to 0
             if (comp < 0) {
                 ret = -1;
             } else if (comp > 0) {
                 ret = 1;
             }
             return ret;
         }
     }
 
     public String toString() {
         String retStr = "";
         int depth = 0;
         List<KDNode> curr = new LinkedList<KDNode>();
         List<KDNode> next = new LinkedList<KDNode>();
         curr.add(root);
         while (!curr.isEmpty()) {
             retStr += depth + ":\n";
             for (KDNode i : curr) {
                 if (i.left != null)
                     next.add(i.left);
                 else
                     System.out.println("left is null");
                 if (i.right != null)
                     next.add(i.right);
                 else
                     System.out.println("right is null");
                 retStr += i.location.toString() + "\n";
             }
             List<KDNode> temp = curr;
             curr = next;
             next = temp;
             next.clear();
             depth++;
         }
         return retStr;
     }
 }
