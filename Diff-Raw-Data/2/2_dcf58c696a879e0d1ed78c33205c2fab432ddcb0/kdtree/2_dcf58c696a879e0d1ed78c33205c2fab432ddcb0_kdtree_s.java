 package naybur.kdtree;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Collections;
 import java.util.LinkedList;
 
 class KDTree
 {
     private KDNode root;
     private int dims;
 
     public KDNode getRoot()
     {
         return root;
     }
 
     public KDTree(int dims)
     {
         this.dims = dims;
     }
     
     private class MyComparator<T extends Double> implements Comparator<ArrayList<T>>
     {
         private int axis;
 
         @Override
         public int compare(ArrayList<T> a, ArrayList<T> b)
         {
             if(a.get(axis) > b.get(axis))
                 return 1;
             else if(a.get(axis) < b.get(axis))
                 return -1;
             else 
                 return 0;
         }
 
         public void setAxis(int axis)
         {
             this.axis = axis;
         }
     }
 
     public KDNode build(ArrayList<ArrayList<Double>> point_list, int depth)
     {
         int axis = depth % dims;
         int size = point_list.size();
         int mid = (int)(size/2);
         
         MyComparator c = new MyComparator();
 
         KDNode node;
 
         axis = depth%dims;
         c.setAxis(axis);
 
         Collections.sort(point_list, c); 
         
         if(size > 0)
         {
             ArrayList<Double> median = point_list.get((int)(point_list.size()/2));
 
             if(median.size() != dims)
             {
                 System.out.print("Invalid dimension for input point: ");
                 System.out.println(median);
                 System.out.println("Expected dimension " + dims + " but received " + median.size() + " dimensions"); 
             }
 
             node = new KDNode(median, axis);
         }
         else
             return null;
 
         node.setLeft( build(new ArrayList(point_list.subList(0, mid)), depth+1) );
         node.setRight( build(new ArrayList(point_list.subList(mid+1, size)), depth+1) );
 
         this.root = node;
 
         return node;
     }
 
     public LinkedList<KDNode> buildStack(LinkedList<KDNode> stack, ArrayList<Double> search_point, KDNode node, int axis)
     {
         stack.push(node);
        
         MyComparator c = new MyComparator();
 
         c.setAxis(axis);
 
         if(c.compare(search_point, node.getPoint()) < 0 && node.getLeft() != null)
         {
             buildStack(stack, search_point, node.getLeft(), node.getLeft().getAxis());
         }
        else if(c.compare(search_point, node.getPoint()) > 0 && node.getRight() != null)
         {
             buildStack(stack, search_point, node.getRight(), node.getRight().getAxis());
         }
 
         return stack;
     }
 
     public KDNode findNearest(ArrayList<Double> search_point)
     {
 
         LinkedList<KDNode> stack = new LinkedList();
         
         MyComparator c = new MyComparator();
 
         buildStack(stack, search_point, root, 0);
 
         KDNode node = stack.pop();
         KDNode closest = node;
         
         double best_dist = sqDist(node.getPoint(), search_point);
         double dist;
 
         while(!stack.isEmpty())
         {
             node = stack.pop();
 
             dist = sqDist(node.getPoint(), search_point);
 
             if( dist < best_dist )
             {
                 best_dist = dist;
                 closest = node; 
             }
 
             double dist_splitting_plane = sqDist(node.getPoint(), node.getSplittingPlane()); 
 
             if( best_dist < dist_splitting_plane )
             {
                 c.setAxis(node.getAxis());
 
                 KDNode left = node.getLeft();
                 KDNode right = node.getRight();
 
                 if(c.compare(search_point, node.getPoint()) > 0 && left != null)
                 {
                     buildStack(stack, search_point, left, left.getAxis());
                 }
                 else if(c.compare(search_point, node.getPoint()) < 0 && right != null)
                 {
                     buildStack(stack, search_point, right, right.getAxis());
                 }
             }
 
         }
 
         return closest;
     }
 
     public static double sqDist(ArrayList<Double> a, ArrayList<Double> b)
     {
         double dist = 0;
 
         for(int i = 0; i < a.size(); i++)
         {
             dist += Math.pow(a.get(i) - b.get(i), 2);
         }
 
         return dist;
     }
 
     public static void printTree(KDNode node)
     {
         LinkedList<KDNode> queue = new LinkedList();
 
         queue.add(node);
 
 //        System.out.println(node.getPoint()); 
 
         while(!queue.isEmpty())
         {
             KDNode n = queue.pop();
 
             System.out.println(n.getPoint());
 
             if(n.getLeft() != null)
             {
                 queue.add(n.getLeft());
             }
 
             if(n.getRight() != null)
             {
                 queue.add(n.getRight());
             }
         }
     }
 }
