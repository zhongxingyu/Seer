 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package algs;
 
 /**
  *
  * @author rob
  */
public class BinaryTree
 {
     public static  class Node
     {
         public Node left;
         public Node right;
         public int value;
 
         public Node(int value)
         {
             this.value = value;
             left = right = null;
         }
     }
     public static class Offset
     {
         int offest;
     }
 
     public static void inorder(Node root)
     {
         if(root == null)
             return;
 
         inorder(root.left);
         System.out.println(root.value + " ");
         inorder(root.right);
     }
 
     public static void addNode(Node root, Node newNode)
     {
         if(newNode.value < root.value)
         {
             if(root.left != null)
                 addNode(root.left, newNode);
             else
                 root.left = newNode;
         }
         else
         {
             if(root.right != null)
                 addNode(root.right, newNode);
             else
                 root.right = newNode;
         }
     }
 
     public static void kBiggest(Node root, int K, Offset current)
     {
         if(root == null)
             return;
 
         kBiggest(root.right, K, current);
         if(K == current.offest)
         {
             System.out.println(root.value);
             current.offest++;
             return;
         }
         else
         {
             current.offest++;
         }
         if(current.offest<=K)
             kBiggest(root.left, K, current);
 
     }
 
     public static void main(String[] args)
     {
         Node root = new Node(5);
         addNode(root, new Node(3));
         addNode(root, new Node(7));
         addNode(root, new Node(2));
         addNode(root, new Node(9));
         addNode(root, new Node(1));
         addNode(root, new Node(4));
 
         inorder(root);
 
         System.out.println("---");
         Offset offset = new Offset();
         offset.offest = 0;
         kBiggest(root, 5, offset);
         offset.offest = 0;
         kBiggest(root, 4, offset);
         offset.offest = 0;
         kBiggest(root, 3, offset);
         offset.offest = 0;
         kBiggest(root, 2, offset);
         offset.offest = 0;
         kBiggest(root, 1, offset);
         offset.offest = 0;
         kBiggest(root, 0, offset);
 
     }
 
 }
