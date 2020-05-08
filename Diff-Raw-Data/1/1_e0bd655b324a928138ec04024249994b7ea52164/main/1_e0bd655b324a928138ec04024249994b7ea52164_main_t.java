 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 class MainTest
 {
     public static void main(String[] args)
     {
         int dims = 2;
 
         KDTree<Integer> kd = new KDTree(dims);
 
         int[][] points = {{2,3}, {5,4}, {9,6}, {4,7}, {8,1}, {7,2}};
 
         ArrayList<ArrayList<Integer>> point_list = new ArrayList();
         ArrayList<Integer> ta[] = new ArrayList[points.length];
 
         for(int i = 0; i < points.length; i++)
         {
             ta[i] = new ArrayList();
 
             for(int j = 0; j < points[i].length; j++)
             {
                 ta[i].add(points[i][j]);
             }
 
             point_list.add(ta[i]);
         } 
 
   
         KDNode<Integer> root = kd.build(point_list, 0);
 
         printTree(root);
 
         LinkedList<KDNode<Integer>> stack = new LinkedList<KDNode<Integer>>();
         ArrayList<Integer> sp = new ArrayList<Integer>();
         sp.add(8);
         sp.add(5);
         
         kd.buildStack(stack, sp, kd.getRoot(), 0); 
 
         System.out.println();
 
         for(KDNode n : stack)
             System.out.println(n.getPoint());
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
