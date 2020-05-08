 package net.benrowland.tree;
 
 /**
  * Represents a Node in a Tree.
  *
  * @see Tree
 */
public abstract class Node<T extends Number> {
 
     protected Point<T> elem;
     protected Node<T> leftChild;
     protected Node<T> rightChild;
     protected Node<T> parent;
 
     public Node() {
 
     }
 
     public Node(Node parent) {
         this.parent = parent;
     }
 
     public Node(Point<T> elem) {
         this.elem = elem;
     }
 
     /**
      * Compare this node to the element in the other node.
      *
      * @param other
      * @return -1 if this node is "less than" the other node,
      *         +1 if this node is "more than" the other node.
      */
     public abstract int compare(Point<T> other);
 
     /**
      * Insert newNode into this node.
      *
      * @param newNode
      */
     public void insert(Node<T> newNode) {
         int result = this.compare(newNode.getElem());
 
         // If newNode < this node
         if(result == -1) {
             if(leftChild == null) {
                 leftChild = newNode;
             }
             else {
                 leftChild.insert(newNode);
             }
         }
         // if newNode >= this node
         else {
             if(rightChild == null) {
                 rightChild = newNode;
             }
             else {
                 rightChild.insert(newNode);
             }
         }
     }
 
     public Point<T> getElem() {
         return elem;
     }
 
     public Node<T> getLeftChild() {
         return leftChild;
     }
 
     public Node<T> getRightChild() {
         return rightChild;
     }
 
     public Node<T> getParent() {
         return parent;
     }
 
     public void setParent(Node<T> parent) {
         this.parent = parent;
     }
 
     /**
      * Where child is a child of this Node, returns the other child
      * Node.
      *
      * @param child
      * @return
      */
     public Node<T> getOtherChild(Node<T> child) {
         if(child.equals(leftChild)) return rightChild;
         else if(child.equals(rightChild)) return leftChild;
         else throw new IllegalStateException("Child passed in does not match either of my children!");
     }
 
     public String toString(int depth) {
         StringBuilder sb = new StringBuilder();
         for(Integer i=0;i<=depth; i++) sb.append("\t");
         sb.append(elem.toString());
         sb.append("\n");
 
         if(leftChild != null) sb.append(leftChild.toString(depth+1));
         if(rightChild != null) sb.append(rightChild.toString(depth+1));
         return sb.toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Node node = (Node) o;
 
         if (elem != null ? !elem.equals(node.elem) : node.elem != null) return false;
         if (leftChild != null ? !leftChild.equals(node.leftChild) : node.leftChild != null) return false;
         if (parent != null ? !parent.equals(node.parent) : node.parent != null) return false;
         if (rightChild != null ? !rightChild.equals(node.rightChild) : node.rightChild != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = elem != null ? elem.hashCode() : 0;
         result = 31 * result + (leftChild != null ? leftChild.hashCode() : 0);
         result = 31 * result + (rightChild != null ? rightChild.hashCode() : 0);
         result = 31 * result + (parent != null ? parent.hashCode() : 0);
         return result;
     }
 }
