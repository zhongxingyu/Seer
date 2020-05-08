 import java.util.*;
 
 public class DSABinarySearchTree<
 	K extends Comparable<K>,
 	V extends Comparable<V>
 > {
 	private class TreeNode<
 		K extends Comparable<K>,
 		V extends Comparable<V>
 	> {
 		private K key;
 		private V value;
 		private TreeNode<K,V> left;
 		private TreeNode<K,V> right;
 		public TreeNode(K key, V value) {
 			this.key = key;
 			this.value = value;
 			left = right = null;
 		}
 		public String toString() {
 			return "[" + this.key + " = " + this.value + "]";
 		}
 	}
 	private TreeNode<K,V> root;
 	public V find(K key) {
 		TreeNode<K,V> cur = root;
 		while (cur != null && !key.equals(cur.key)) {
 			if (key.compareTo(cur.key) < 0)
 				cur = cur.left;
 			else
 				cur = cur.right;
 		}
 		if (cur == null)
 			throw new NoSuchElementException();
 		return cur.value;
 	}
 	public void insert(K key, V value) {
 		TreeNode<K,V> cur = root, parent = null, child;
 		while (cur != null) {
 			int comp = key.compareTo(cur.key);
 			parent = cur;
 			if (comp == 0)
 				throw new IllegalArgumentException();
 			else if (comp < 0)
 				cur = cur.left;
 			else
 				cur = cur.right;
 		}
 		child = new TreeNode<K,V>(key, value);
 		if (parent == null)
 			root = child;
 		else if (key.compareTo(parent.key) < 0)
 			parent.left = child;
 		else
 			parent.right = child;
 	}
 	public void delete(K key) {
 		TreeNode<K,V> cur = root, parent = null;
 		while (cur != null && !key.equals(cur.key)) {
 			parent = cur;
 			if (key.compareTo(cur.key) < 0)
 				cur = cur.left;
 			else
 				cur = cur.right;
 		}
 		if (cur == null)
 			throw new NoSuchElementException();
 		else if (cur.left == null || cur.right == null)
 			deleteChineseNode(cur, parent);
 		else
 			deleteKurdishNode(cur, parent);
 	}
 	public void deleteChineseNode(
 		TreeNode<K,V> cur,
 		TreeNode<K,V> parent
 	) {
 		TreeNode<K,V> promoted;
 		if (cur.left == null)
 			promoted = cur.left;
 		else
 			promoted = cur.right;
 		if (parent == null)
 			root = promoted;
 		else if (parent.left == cur)
 			parent.left = promoted;
 		else
 			parent.right = promoted;
 	}
 	public void deleteKurdishNode(
 		TreeNode<K,V> cur,
 		TreeNode<K,V> parent
 	) {
 		TreeNode<K,V> successor, succParent;
 		successor = cur.right;
 		succParent = parent;
 		while (successor.left != null) {
 			succParent = successor;
 			successor = successor.left;
 		}
		deleteChineseNode(successor, succParent);
 		cur.key = successor.key;
 		cur.value = successor.value;
 	}
 	public int height() {
 		return height(root, 0);
 	}
 	public int height(TreeNode<K,V> start, int count) {
 		int leftHeight, rightHeight;
 		if (start == null) {
 			return count;
 		} else {
 			leftHeight = height(start.left, count + 1);
 			rightHeight = height(start.right, count + 1);
 			if (leftHeight > rightHeight)
 				return leftHeight;
 			else
 				return rightHeight;
 		}
 	}
 	public String toString() {
 		return toString(root, 0, 0);
 	}
 	public String toString(TreeNode<K,V> start, int indent, int dir) {
 		String r = "";
 		if (start != null && start.left != null)
 			r += toString(start.left, indent + 1, -1);
 		for (int i = 0; i < indent; i++) {
 			for (int j = 0; j < 4; j++)
 				if (j == 0 && i == indent - 1)
 					r +=	dir < 0 ? "/" :
 						dir > 0 ? "\\" : "";
 				else if (i == indent - 1)
 					r += "-";
 				else
 					r += " ";
 		}
 		if (start != null)
 			r += start + "\n";
 		else
 			r += "(null)\n";
 		if (start != null && start.right != null)
 			r += toString(start.right, indent + 1, 1);
 		return r;
 	}
 	public static void main(String[] args) {
 		DSABinarySearchTree<String, String> t =
 			new DSABinarySearchTree<String, String>();
 		t.insert("delan@azabani.com", "Delan Azabani");
 		t.insert("dave.cooper@iinet.net.au", "David Cooper");
 		t.insert("rms@gnu.org", "Richard Stallman");
 		t.insert("me@kyerussell.com", "Kye Russell");
 		t.insert("torvalds@linux-foundation.org", "Linus Torvalds");
 		System.out.print(t);
 		t.delete("delan@azabani.com");
 		System.out.print(t);
 		System.out.println(t.find("dave.cooper@iinet.net.au"));
 		System.out.println(t.find("rms@gnu.org"));
 		System.out.println(t.find("me@kyerussell.com"));
 		try {
 			System.out.println(t.find("delan@azabani.com"));
 		} catch (NoSuchElementException e) {
 			System.out.println("delan@azabani.com not found");
 		}
 	}
 }
