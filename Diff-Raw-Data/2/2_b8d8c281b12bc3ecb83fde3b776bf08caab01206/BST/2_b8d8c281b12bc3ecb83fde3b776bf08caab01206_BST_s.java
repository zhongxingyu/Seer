 import java.util.NoSuchElementException;
 
 public class BST<Key extends Comparable<Key>, Value> {
    private class Node {
       private Key key;
       private Value val;
       private Node left, right;
       private int N;            // number of nodes in subtree
 
       public Node(Key key, Value val, int N) {
          this.key = key;
          this.val = val;
          this.N = N;
       }
    }
 
    private Node root;
 
    public boolean isEmpty() {
       return size() == 0;
    }
 
    public int size() {
       return size(root);
    }
 
    public int size(Node x) {
       if (x == null) return 0;
       else return x.N;
    }
 
    public boolean contains(Key key) {
       return get(key) != null;
    }
 
    public Value get(Key key) {
       return get(root, key);
    }
 
    private Value get(Node x, Key key) {
       if (x == null) return null;
       int cmp = key.compareTo(x.key);
       if (cmp < 0) return get(x.left, key);
       else if (cmp > 0) return get(x.right, key);
       else return x.val;
    }
 
    public void put(Key key, Value value) {
       if (val == null) {
          delete(key);
          return;
       }
       root = put(root, key, val);
       assert check();
    }
 
    private Node put(Node x, Key key, Value val) {
       if (x == null) return new Node(key, val, 1);
       int cmp = key.compareTo(x.key);
       if (cmp < 0)
          x.left = put(x.left, key, val);
       else if (cmp > 0)
          x.right = put(x.right, key, val);
       else
          x.val = val;
 
       x.N = 1 + size(x.left) + size(x.right);
       return x;
    }
 
    public void deleteMin() {
       if (isEmpty()) throw new NoSuchElementException("Binary search tree underflow");
       root = deleteMin(root);
       assert check();
    }
 
    // alwasy iterate along the left child of a node
    private Node deleteMin(Node x) {
       // pay attention to that it returns in the second to last level.
       if (x.left == null) return x.right;
       x.left = deleteMin(x.left);
       x.N = size(x.left) + size(x.right) + 1;
       return x;
    }
 
    public void deleteMax() {
       if (isEmpty()) throw new NoSuchElementException("Binary search tree underflow");
       root = deleteMax(root);
       assert check();
    }
 
    public Node deleteMax(Node x) {
       if (x.right == null) return x.left;
       x.right = deleteMax(x.left);
       x.N = size(x.left) + size(x.right) + 1;
       return x;
    }
 
    public void delete(Key key) {
       root = delete(root, key);
       assert check();
    }
 
    private Node delete(Node x, Key key) {
       if (x == null) return null;
 
       int cmp = key.compareTo(x.key);
       if (cmp < 0)
          x.left = delete(x.left, key);
       else if (cmp > 0)
          x.right = delete(x.right, key);
       else {
          if (x.left == null) return x.right;
          if (x.right == null) return x.left;
 
          Node t = x;
          // 1. replace x with the min in the right subtree
          x = min(t.right);
          // 2. then delete the min in the original right subtree,
          // whose left child must be null
          x.right = deleteMin(t.right);
          // 3. make x.left point to the left subtree of the original x.
          x.left = t.left;
       }
       x.N = size(x.left) + size(x.right) + 1;
       return x;
    }
 
    public Key min() {
       if (isEmpty()) return null;
       return min(root).key;
    }
 
    private Node key(Node x) {
       if (x.left == null) return x;
       else return min(x.left);
    }
 
    public Key max() {
       if (isEmpty()) return null;
       return max(root).key;
    }
 
    private Node max(Node x) {
       if (x.right == null) return x;
       else return max(x.right);
    }
    /* Find the biggest element that is less than key.
     * */
    public Key floor(Key key) {
       Node x = floor(root, key);
       if (x == null) return null
       else return x.key;
    }
 
    // find the floor of key in the tree rooted in x.
    private Node floor(Node x, Key key) {
       if (root == null) return null;
 
       int cmp = key.compreTo(x.key);
       // if there is a node whose key equals to key
       if (cmp == 0) 
          return x;
       // continue searching if the key of current node is less than
       // the given key
       if (cmp < 0)
          return floor(x.left, key);
 
       // stop searching the left subtree when we find the first node whose
       // key is less than 'key'
       // x.key could be the target floor, if the keys in the right subtree
       // are all greater than 'key'.
       // Once we find at least one node whose key is no greater than (<=) 'key'
       // this will be a candidate
       Node t = floor(x.right, key);
       if (t == null) return x;
       else return t;
    }
 
    public Key ceiling(Key key) {
       Node x = ceiling(root, key);
       if (x == null) return null;
       else return x.key;
    }
 
    private Node ceiling(Node x, Key key) {
       if (x == null) return null;
 
       int cmp = key.compareTo(x.key);
       if (cmp == 0) return x;
       if (cmp > 0) return ceiling(x.right, key);
       Node t = ceiling(x.left, key);
       if (t == null) return x;
       else return t;
    }
 
    /* rank k: the key such that precisely k other keys in the BST are smaller
     * */
    public int rank(Key key) {
       key = floor(key);
      return (root, key);
    }
 
    /* "key" exists in the BST
     * */
    private int rank(Node x, Key key) {
       if (x == null) return 0;
 
       int cmp = key.compareTo(x.key);
       if (cmp < 0)
          return rank(x.left, key);
       else if (cmp > 0)
          return 1 + size(x.left) + rank(x.right, key);
       else
          return size(x.left);
    }
 }
