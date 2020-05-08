 package com.cordelta.barr.david;
 
 import java.lang.ref.WeakReference;
 import java.util.WeakHashMap;
 
 public class Node {
 
     public static Node search(Node root, Sequence<String> key) {
         if (key.value() == null)
             return root;
         while (root != null) {
             int cmp = key.value().compareTo(root.token);
             if (cmp == 0 && key.next().value() == null)
                 break;
             root = cmp < 0 ? root.left : cmp == 0 ? root.middle : root.right;
         }
         return root;
     }
 
     private Node balance() {
         if (left != null && left.hash > hash)
             return left.right(left(left.right));
         if (right != null && right.hash > hash)
             return right.left(right(right.left));
         return this;
     }
 
     public static Node insert(Node root, Sequence<String> key) {
         if (key.value() == null)
             return root;
         if (root == null)
             root = new Node(null, null, null, key.value());
         int cmp = key.value().compareTo(root.token);
         if (cmp == 0)
             return root.middle(insert(root.middle, key.next()));
         if (cmp < 0)
             return root.left(insert(root.left, key)).balance();
         return root.right(insert(root.right, key)).balance();
     }
 
    private static Node removeRoot(Node root) {
        if (root.left == null) {
            if (root.right == null)
                return null;
        } else if (root.right == null || root.left.hash > root.right.hash)
            return root.left.right(removeRoot(root.left(root.left.right)));
        return root.right.left(removeRoot(root.right(root.right.left)));
    }

     public static Node remove(Node root, Sequence<String> key) {
         if (key.value() == null || root == null)
             return null;
         int cmp = key.value().compareTo(root.token);
        if (cmp == 0 && key.next().value() == null)
            return removeRoot(root);
         if (cmp == 0)
             return root.middle(remove(root.middle, key));
         if (cmp < 0)
             return root.left(remove(root.left, key));
         return root.right(remove(root.right, key));
     }
 
     public static Node graft(Node root, Sequence<String> key, Node source) {
         if (key.value() == null)
             return source.middle;
         if (root == null)
             root = new Node(null, null, null, key.value());
         int cmp = key.value().compareTo(root.token);
         if (cmp == 0)
             return root.middle(graft(root.middle, key.next(), source));
         if (cmp < 0)
             return root.left(graft(root.left, key, source)).balance();
         return root.right(graft(root.right, key, source)).balance();
     }
 
     public static Node first(Node root) {
         Node result = root;
         while (result != null && result.left != null)
             result = result.left;
         return result;
     }
 
     public static Node next(Node root, Node node) {
         int cmp;
         Node result = first(node.right);
         if (result == null)
             while (root != null && (cmp = node.token.compareTo(root.token)) != 0)
                 if (cmp < 0) {
                     result = root;
                     root = root.left;
                 } else
                     root = root.right;
         return result;
     }
 
     public static void diff(String path, Node aRoot, Node bRoot, DeltaHandler handler) {
         Node a = first(aRoot);
         Node b = first(bRoot);
         while (a != null || b != null) {
             int cmp = a == null ? 1 : b == null ? -1 : a.token.compareTo(b.token);
             String deltaPath = path + "/" + (cmp <= 0 ? a.token : b.token);
             handler.delta(deltaPath, cmp > 0 ? null : a, cmp < 0 ? null : b);
             if (cmp >= 0 && b.middle != null)
                 diff(deltaPath, cmp > 0 || a == null ? null : a.middle, b.middle, handler);
             if (cmp <= 0)
                 a = next(aRoot, a);
             if (cmp >= 0)
                 b = next(bRoot, b);
         }
     }
 
     public interface DeltaHandler {
         public void delta(String path, Node a, Node b);
     }
 
     public final String token;
     public final Node left, right, middle;
 
     public Node left(Node left) {
         return new Node(left, middle, right, token).intern();
     }
 
     public Node right(Node right) {
         return new Node(left, middle, right, token).intern();
     }
 
     public Node middle(Node middle) {
         return new Node(left, middle, right, token.intern());
     }
 
     private static WeakHashMap<Node, WeakReference<Node>> map =
             new WeakHashMap<Node, WeakReference<Node>>();
     public final int hash;
 
     private Node(Node left, Node middle, Node right, String token) {
         this.left = left;
         this.middle = middle;
         this.right = right;
         this.token = token.intern();
         int hash = token.hashCode();
         hash = 31 * hash + (left != null ? left.hash : 0);
         hash = 31 * hash + (right != null ? right.hash : 0);
         hash = 31 * hash + (middle != null ? middle.hash : 0);
         this.hash = hash * (int) 2654435761l;
     }
 
     private Node intern() {
         if (map.containsKey(this))
             return map.get(this).get();
         map.put(this, new WeakReference<Node>(this));
         return this;
     }
 
     public static int count() {
         return map.size();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Node)) return false;
 
         Node node = (Node) o;
 
         if (hash != node.hash) return false;
         if (left != null ? !left.equals(node.left) : node.left != null) return false;
         if (middle != null ? !middle.equals(node.middle) : node.middle != null) return false;
         if (right != null ? !right.equals(node.right) : node.right != null) return false;
         if (token != null ? !token.equals(node.token) : node.token != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return hash;
     }
 
     @Override
     public String toString() {
         final StringBuffer sb = new StringBuffer();
         toString(0, this, sb);
         return sb.toString();
     }
 
     private static void toString(int depth, Node that, StringBuffer sb) {
         if (that == null) {
             return;
         }
         toString(depth, that.left, sb);
         for (int i = 0; i < depth; ++i) sb.append(' ');
         sb.append('\'').append(that.token).append("'\n");
         toString(depth + 1, that.middle, sb);
         toString(depth, that.right, sb);
     }
 }
