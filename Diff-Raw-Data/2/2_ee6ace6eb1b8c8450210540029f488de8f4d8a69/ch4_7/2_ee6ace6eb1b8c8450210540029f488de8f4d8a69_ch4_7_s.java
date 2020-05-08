 import java.util.*;
 
 public class ch4_7 {
 
 	public static void main(String[] args) {
 	
 	}
 
 
 	// assume we can access the parent node
 	public static TreeNode ancestor(TreeNode a, TreeNode b) {
 		if (a == null || b == null)
 			return null;
 
 		if (a == b)
 			return a.parent;
 
 		// calculate depth of each node
 		int da = depth(a);
 		int db = depth(b);
 
 		if (da > db) {
 			int diff = da - db;
 			for (int i = 0; i < diff; i++) {
 				a = a.parent;
 			}
 		} else {
 			int diff = db - da;
 			for (int i = 0; i < diff; i++) {
 				b = b.parent;
 			}
 		}
 
 		while (a.parent != b.parent) {
 			a = a.parent;
 			b = b.parent;
 		}
 
 		return a.parent;
 	}
 
 	private static int depth(TreeNode t) {
 		int d = 0;
 		while (t != null) {
 			d++;
 			t = t.parent;
 		}
 		return d;
 	}
 
 	// no parent pointer, the idea is to find the first node to make the 2 nodes under different sides of this node
 	public static TreeNode ancestor2Caller(TreeNode root, TreeNode a, TreeNode b) {
 		// a b must exist
 		if (!contains(root, a) || !contains(root, b))
 			return null;
 		return ancestor2(root, a, b);
 	}
 
 	public static TreeNode ancestor2(TreeNode root, TreeNode a, TreeNode b) {
 		if (root == null)
 			return null;
 
 		if (root == a || root == b)
 			return root;
 
 		boolean aOnLeft = contains(root.left, a);
 		boolean bOnLeft = contains(root.right, b);
 
 		if (aOnLeft != bOnLeft)
 			return root;
 
 		if (aOnLeft == true) {
 			return ancestor2(root.left, a, b);
 		} else {
 			return ancestor2(root.right, a, b);
 		}
 
 	}
 
 	// check tree rooted at root contains n
 	private static boolean contains(TreeNode root, TreeNode n) {
 		if (root == null || n == null)
 			return false;
 		if (root == n)
 			return true;
 		else 
 			return contains(root.left, n) || contains(root.right, n);
 	}
 
 
 
 
 	public static TreeNode ancestor3Caller(TreeNode root, TreeNode a, TreeNode b) {
 		// a b must exist
 		if (!contains(root, a) || !contains(root, b))
 			return null;
 		return ancestor3(root, a, b);
 	}
 
 	public static TreeNode ancestor3(TreeNode root, TreeNode a, TreeNode b) {
 		if (root == null)
 			return null;
 
 		if (root == a || root == b)
 			return root;
 
 		TreeNode l = ancestor3(root.left, a, b);
 		TreeNode r = ancestor3(root.right, a, b);
 
 		if (l != null && r != null) {
 			return root;
 		}
 
		return l == null ? null : r;
 	}
 
 	public static class Result {
 		public TreeNode node;
 		public boolean isLCA;
 		public Result(TreeNode n, boolean is) {
 			node = n;
 			isLCA = is;
 		}
 	}
 
 	public static TreeNode ancestor4Caller(TreeNode root, TreeNode a, TreeNode b) {
 		Result r = ancestor4(root, a, b);
 		if (r.isLCA == true) {
 			return r.node;
 		} else {
 			return null;
 		}
 	}
 
 	public static Result ancestor4(TreeNode root, TreeNode a, TreeNode b) {
 		if (root == null)
 			return new Result(null, false);
 
 		if (root == a && root == b)
 			return new Result(root, true);
 
 		Result lr = ancestor4(root.left, a, b);
 		Result rr = ancestor4(root.right, a, b);
 
 		if (lr.isLCA) {
 			return lr;
 		}
 
 		if (rr.isLCA) {
 			return rr;
 		}
 
 		if (lr.node != null && rr.node != null) {
 			return new Result(root, true);
 		} else  if (root == a || root == b) {
 			return new Result(root, lr.node != null || rr.node != null);
 		} else {
 			return new Result(lr.node != null ? lr.node : rr.node, false);
 		}
 
 
 
 	}
 
 }
