/* 2.6 Design an algorithm and write code to find the first common ancestor
  * of two nodes in a binary tree. Avoid storing additional nodes in a data
  * structure. NOTE: this is not necessarily a binary search tree.
  *
  * Similar questions: check if two nodes are in a same binary tree;
  * */
 import java.util.NoSuchElementException;
 
 public class P0406 {
    private class Node {
       int value;
       Node left, right;
       Node parent;
 
       public Node(int value) {
          this.value = value;
       }
    }
 
    private final static int NO_NODES_IN_TREE = 0;
    private final static int ONE_NODE_IN_TREE = 1;
    private final static int TWO_NODES_IN_TREE = 2; 
 
    // average: O(2*lgN), worst: O(2N)
    public Node commonAncestor(Node n, Node m) {
       while (n != null) {
          Node tmp = m;
          while (tmp != null) {
             if (n == tmp) return n;
             tmp = tmp.parent;
          }
          n = n.parent;
       }
 
       return null;
    }
 
    // check if the two nodes are on the different sides of the current node
    public Node commonAncestor(Node root, Node n, Node m) {
       // if both nodes are in the same side of the current node,
       // then search the two nodes in this side
       if (isInTree(root.left, n) && isInTree(root.left, m))
          return commonAncestor(root.left, n, m);
 
       if (isInTree(root.right, n) && isInTree(root.right, m))
          return commonAncestor(root.right, n, m);
 
       return root;
    }
 
 
    private boolean isInTree(Node root, Node x) {
       if (root == null) return false;
 
       if (root.value == x.value) return true;
 
       return isInTree(root.left, x) || isInTree(root.right, x);
    }
 
    public Node commonAncestorX(Node root, Node n, Node m) {
       if (root == null) return null;
 
       int numOfNodesInLeft = getNumOfNodesInTree(root.left, n, m, 0);
       if (numOfNodesInLeft == NO_NODES_IN_TREE) {
          return commonAncestorX(root.right, n, m);
       }
       else if (numOfNodesInLeft == TWO_NODES_IN_TREE) {
          if (root.left == n || root.left == m)
             return root;
          else
             return commonAncestorX(root.left, n, m);
       }
 
       int numOfNodesInRight = getNumOfNodesInTree(root.right, n, m, 0);
       if (numOfNodesInRight == NO_NODES_IN_TREE) {
          return commonAncestorX(root.left, n, m);
       }
       else if (numOfNodesInRight == TWO_NODES_IN_TREE) {
          if (root.right == n || root.right == m)
             return root;
          else
             return commonAncestorX(root.right, n, m);
       }
 
       return root;
    }
 
    private int getNumOfNodesInTree(Node root, Node n, Node m, int numOfNode) {
       if (root == null) return numOfNode;
 
       if (numOfNode == TWO_NODES_IN_TREE) return numOfNode;
 
       if (root == n || root == m) {
          numOfNode++;
 
          if (numOfNode == TWO_NODES_IN_TREE) return numOfNode;
       }
 
       int left = getNumOfNodesInTree(root.left, n, m, numOfNode);
       int right = getNumOfNodesInTree(root.right, n, m, numOfNode);
 
       return Math.max(left, right);
       /*
       if (left == TWO_NODES_IN_TREE || right == TWO_NODES_IN_TREE)
          return TWO_NODES_IN_TREE;
       else
          return Math.max(left, right);
       */
    }
    public Node buildTree() {
       Node root = new Node(1);
       root.left = new Node(2);
       root.left.parent = root;
       root.right = new Node(3);
       root.right.parent = root;
       root.left.left = new Node(4);
       root.left.left.parent = root.left;
       root.left.right = new Node(5);
       root.left.right.parent = root.left;
       root.right.left = new Node(6);
       root.right.left.parent = root.right;
       root.left.left.left = new Node(7);
       root.left.left.left.parent = root.left.left;
       root.left.left.left.left = new Node(8);
       root.left.left.left.left.parent = root.left.left.left;
       //
       return root;
    }
 
    public static void main(String[] args) {
       P0406 p0406 = new P0406();
       Node root = p0406.buildTree();
       Node n = root.left.left;
       // Node m = root.left.right;
       Node m = root.left.left.left;
       Node common = p0406.commonAncestor(n, m);
       Node common1 = p0406.commonAncestor(root, n, m);
       Node common2 = p0406.commonAncestorX(root, n, m);
       System.out.println("Solution#1: " + common.value);
       System.out.println("Solution#2: " + common1.value);
       System.out.println("Solution#3: " + common2.value);
 
    }
 }
