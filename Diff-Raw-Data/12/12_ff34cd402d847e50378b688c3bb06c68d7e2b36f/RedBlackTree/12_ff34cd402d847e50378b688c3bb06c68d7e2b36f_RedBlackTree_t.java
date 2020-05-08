 import java.util.Collection;
 
 /**
  * User: krems
  * Date: 8/7/13
  * Time: 11:02 AM
  */
 
 public class RedBlackTree<T extends Comparable> {
     private RBNode<T> root;
 
     public RedBlackTree(T key) {
         root = new RBNode<T>(key, null, true);
     }
 
     public RedBlackTree(T[] keys) {
         root = new RBNode<T>(keys[0], null, true);
         for (int i = 1; i < keys.length; ++i) {
             this.insert(keys[i]);
         }
     }
 
     public RedBlackTree(Collection<T> keys) {
         this((T[])keys.toArray());
     }
 
     public void insert(T key) {
         if (this.root == null) {
             root = new RBNode<T>(key, null, true);
             return;
         }
         RBNode<T> newNode = insert(root, key);
         insert1(newNode);
     }
 
     public RBNode<T> remove(T key) {
         return null;
     }
 
     public RBNode<T> find(T key) {
         return find(this.root, key);
     }
 
     private RBNode<T> find(RBNode<T> node, T key) {
         if (node == null) {
             return null;
         }
         switch (node.getValue().compareTo(key)) {
             case -1:
                 return find(node.left, key);
             case 0:
                 return node;
             case 1:
                 return find(node.right, key);
             default:
                throw new RuntimeException("Illegal compareTo() return value");
         }
     }
 
     private RBNode<T> insert(RBNode<T> node, T key) {
         if (node.getValue().compareTo(key) <= 0) {
             if (node.left != null) {
                 return insert(node.left, key);
             } else {
                 node.left = new RBNode<T>(key, node, false);
                 return node.left;
             }
         } else {
             if (node.right != null) {
                 return insert(node.right, key);
             } else {
                 node.right = new RBNode<T>(key, node, false);
                 return node.right;
             }
         }
     }

    // when inserted vertex is root
     private void insert1(RBNode<T> node) {
         if (node.parent == null) {
             node.isBlack = true;
         } else {
             insert2(node);
         }
     }
 
    // if parent of inserted vertex is black all is right
     private void insert2(RBNode<T> node) {
         if (node.parent.isBlack) {
             return;
         }
         insert3(node);
     }
 
    // if parent is red and uncle is red
     private void insert3(RBNode<T> node) {
         if (node.getUncle() != null && !node.getUncle().isBlack && !node.parent.isBlack) {
             node.getUncle().isBlack = true;
             node.parent.isBlack = true;
             node.getGrandparent().isBlack = false;
             insert1(node.getGrandparent());
         } else {
             insert4(node);
         }
     }
 
     private void insert4(RBNode<T> node) {
         if (node == node.parent.right && node.parent == node.getGrandparent().left) {
             rotateLeft(node.parent);
             node = node.left;
         } else if (node == node.parent.left && node.parent == node.getGrandparent().right) {
             rotateRight(node.parent);
             node = node.right;
         }
         insert5(node);
     }
 
     private void insert5(RBNode<T> node) {
         node.parent.isBlack = true;
         RBNode<T> grandparent = node.getGrandparent();
         grandparent.isBlack = false;
         if (node.parent == grandparent.left) {
             rotateRight(grandparent);
         } else {
             rotateLeft(grandparent);
         }
     }
 
     private void rotateRight(RBNode<T> node) {
         RBNode<T> newRoot = node.left;
         RBNode<T> rightNephew = node.left.right;
         newRoot.parent = node.parent;
         node.parent = newRoot;
         node.left = rightNephew;
         newRoot.right = node;
     }
 
     private void rotateLeft(RBNode<T> node) {
         RBNode<T> newRoot = node.right;
         RBNode<T> leftNephew = node.right.left;
         newRoot.parent = node.parent;
         node.parent = newRoot;
         node.right = leftNephew;
         newRoot.left = node;
     }
 }
 
 class RBNode<T> {
     public boolean isBlack;
     public RBNode<T> parent;
     public RBNode<T> left;
     public RBNode<T> right;
     private T key;
 
    public RBNode(T key, RBNode<T> parent, boolean isBlack) {
         this.key = key;
         this.parent = parent;
         this.isBlack = isBlack;
     }
 
     public T getValue() {
         return key;
     }
 
     public void setValue(T key) {
         this.key = key;
     }
 
     public RBNode<T> getGrandparent() {
         if (this.parent != null && this.parent.parent != null) {
             return this.parent.parent;
         }
         return null;
     }
 
     public RBNode<T> getUncle() {
         RBNode<T> grandparent = this.getGrandparent();
         if (grandparent == null) {
             return null;
         }
         if (grandparent.left == this.parent) {
             return grandparent.right;
         }
         return grandparent.left;
     }
 }
