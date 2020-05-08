 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
public class CommonAncestor {
 
     static public Stack<TreeNode> pathToRoot(TreeNode node) {
         TreeNode runner = node;
         Stack<TreeNode> path = new Stack<TreeNode>();
         do {
             path.push(runner);
             runner = runner.parent;
         } while (runner != null);
 
         return path;
     }
 
     static public class TreeNode {
        TreeNode parent;
 
        TreeNode findFirstCommonAncestor(TreeNode other) {
            Stack<TreeNode> path1 = pathToRoot(this);
            Stack<TreeNode> path2 = pathToRoot(other);
            TreeNode commonAncestor;
            do {
                commonAncestor = path1.peek();
                path1.pop();
                path2.pop();
            } while (path1.size() > 0 && path2.size() > 0 && path1.peek() == path2.peek());
 
            return commonAncestor;
        }
     }
 
     static void childs(TreeNode parent, TreeNode left, TreeNode right) {
         left.parent = parent;
         right.parent = parent;
     }
 
     static void assertEquals(TreeNode node1, TreeNode node2) {
         if (node1 != node2) {
             throw new RuntimeException(node1 + " does not equals " + node2);
         }
 
     }
 
     public static void main(String[] args) {
         TreeNode A = new TreeNode();
         TreeNode B = new TreeNode();
         TreeNode C = new TreeNode();
         TreeNode D = new TreeNode();
         TreeNode E = new TreeNode();
         TreeNode F = new TreeNode();
         TreeNode G = new TreeNode();
         TreeNode H = new TreeNode();
         TreeNode I = new TreeNode();
 
         childs(A, B, C);
         childs(B, D, E);
         childs(E, F, G);
         childs(C, H, I);
 
         assertEquals(A.findFirstCommonAncestor(A), A);
         assertEquals(A.findFirstCommonAncestor(E), A);
         assertEquals(B.findFirstCommonAncestor(E), B);
         assertEquals(B.findFirstCommonAncestor(C), A);
         assertEquals(F.findFirstCommonAncestor(I), A);
         System.out.println("ok");
     }
 }
