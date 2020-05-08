 package net.benrowland.tree;
 
 /**
  * A Tree of elements of type T.
  */
public class Tree<T extends Number> {
 
     protected Node<T> root;
 
     public void insert(Node<T> node) {
         if(root == null) {
             root = node;
         }
         else {
             root.insert(node);
         }
     }
 
     public Node getRootNode() {
         return root;
     }
 }
