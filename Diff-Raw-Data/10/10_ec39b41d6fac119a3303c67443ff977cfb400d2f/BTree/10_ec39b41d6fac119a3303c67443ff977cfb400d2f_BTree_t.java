 package structures;
 
 public class BTree<K1 extends Comparable<K1>, V1> {
 
 	public static final int t = 2;
 
 	public static class Pair<K2 extends Comparable<K2>, V2> {
 		public K2 key;
 		public V2 value;
 
 		public Pair(K2 key, V2 value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		@Override
 		public String toString() {
 			return "{" + key + " => " + value + "}";
 		}
 
 	}
 
 	public static class Node<K3 extends Comparable<K3>, V3> {
 		public int keyCount;
 		public Pair<K3, V3>[] keys = new Pair[(2 * t) - 1];
 		public Node<K3, V3>[] children = new Node[2 * t];
 		public boolean leaf;
 
 		@Override
 		public String toString() {
 			String s = " [ ";
 			s += children[0];
 
 			for (int i = 0; i < keyCount; i++) {
 				s += " " + keys[i].key + " " + children[i + 1];
 			}
 			s += " ] ";
 
 			return s;
 		}
 	}
 
 	public Node<K1, V1> root;
 
 	public BTree() {
 		Node<K1, V1> node = new Node<K1, V1>();
 		node.leaf = true;
 		node.keyCount = 0;
 		root = node;
 	}
 
 	public Pair<K1, V1> search(K1 key) {
 		return search(root, key);
 	}
 
 	// when we've found it, return (node, position in node)
 	private Pair<K1, V1> search(Node<K1, V1> node, K1 key) {
 		int i = 0;
 		// Iterate until we either hit the key or we've found it.
 		while (i < node.keyCount && node.keys[i].key.compareTo(key) < 0) {
 			i++;
 		}
 
 		// If we've found it
 		if (i < node.keyCount && node.keys[i].key.equals(key)) {
 			return node.keys[i];
 		}
 
 		// If node is a leaf, then we can't recurse
 		if (node.leaf) {
 			return null;
 		} else {
 			return search(node.children[i], key);
 		}
 	}
 
 	// Takes a nonfull internal x node as input with a full child.
 	// It then splits the full child into two nodes and adds the
 	// median to x
 	public void splitChild(Node<K1, V1> parent, int fullChildIndex) {
 		Node<K1, V1> right = new Node<K1, V1>();
 		Node<K1, V1> child = parent.children[fullChildIndex];
 		right.leaf = child.leaf;
 		right.keyCount = t - 1;
 		// Get the median ahead of time
 		Pair<K1, V1> median = child.keys[t - 1];
 
 		// copy over half of keys [0, t-1)
 		for (int i = 0; i < t - 1; i++) {
 			right.keys[i] = child.keys[i + t];
 			child.keys[i + t] = null;
 		}
 
 		// remove median from child list
 		child.keys[t - 1] = null;
 
 		// copy over half of children [0,t)
 		if (!child.leaf) {
 			for (int i = 0; i < t; i++) {
 				right.children[i] = child.children[i + t];
 				child.children[i + t] = null;
 			}
 		}
 
 		child.keyCount = t - 1;
 
 		// Shift right all the children. Must start at keyCount
 		// since # of children = keyCount + 1.
 		for (int i = parent.keyCount; i > fullChildIndex - 1; i--) {
 			parent.children[i + 1] = parent.children[i];
 		}
 
 		parent.children[fullChildIndex + 1] = right;
 
 		// Shift right all keys.
 		for (int i = parent.keyCount - 1; i > fullChildIndex - 1; i--) {
 			parent.keys[i + 1] = parent.keys[i];
 		}
 
 		parent.keys[fullChildIndex] = median;
 		parent.keyCount++;
 	}
 
 	public void insert(Pair<K1, V1> pair) {
 		// If root is not full, then just insert it here
 		if (root.keyCount < root.keys.length) {
 			insertNonFull(root, pair);
 		} else {
 			Node<K1, V1> s = new Node<K1, V1>();
 			Node<K1, V1> oldRoot = root;
 			s.leaf = false;
 			s.children[0] = oldRoot;
 			splitChild(s, 0);
 			this.root = s;
 			insertNonFull(s, pair);
 		}
 	}
 
 	private void insertNonFull(Node<K1, V1> node, Pair<K1, V1> pair) {
 		int i = node.keyCount - 1;
 		if (node.leaf) {
 			// Shift all keys that are greater than what we are inserting
 			while (i >= 0 && pair.key.compareTo(node.keys[i].key) < 0) {
 				node.keys[i + 1] = node.keys[i];
 				i--;
 			}
 			i++;
 			node.keys[i] = pair;
 			node.keyCount++;
 		} else {
 			// Identify where we should go
 			while (i >= 0 && pair.key.compareTo(node.keys[i].key) < 0) {
 				i--;
 			}
 			// Get the next children, since we will always be on the right.
 			i++;
 			// If no space in our child, we must first split
 			if (node.children[i].keyCount == node.children[i].keys.length) {
 				splitChild(node, i);
 				// If we are greater than the new inserted key, we go to the
 				// right again.
 				if (pair.key.compareTo(node.keys[i].key) > 0) {
 					i++;
 				}
 			}
 			insertNonFull(node.children[i], pair);
 		}
 	}
 	
 	public void delete(K1 key) {
 		delete(root, key);
 	}
 	
 	// Note that this has the precondition that the node is either
 	// a root, or has at least t keys.
 	private void delete(Node<K1, V1> node, K1 key) {
 		// Simple case - if node is a leaf, just go delete it
 		if (node.leaf) {
 			for (int i = 0; i < node.keyCount; i++) {
 				if (node.keys[i].key.equals(key)) {
 					// Iterate onwards, shifting everyone down
 					for (int j = i + 1; j < node.keyCount; j++) {
 						node.keys[j - 1] = node.keys[j];
 					}
 					node.keyCount--;
 				}
 			}
 			// nothing we can do, it wasn't in the tree.
 			return;
 		}
 
 		// Find the key where the key would be, or the child it'll be in if it is in the tree.
 		int i = 0;
 		while (i < node.keyCount) {
 			if (node.keys[i].key.compareTo(key) >= 0) {
 				break;
 			}
 			i++;
 		}
 
 		// If k is in the current node, then i == it's position.
 		if (i < node.keyCount && node.keys[i].key.equals(key)) {
 			// if left child has at least t keys, swap k with the predecessor
 			// and then recursively delete
 			if (node.children[i].keyCount >= t) {
 				Pair<K1, V1> predecessor = getPredecessor(node.children[i]);
 				node.keys[i] = predecessor;
 				delete(node.children[i], predecessor.key);
 				return;
 			}
 			// if right child has at least t keys, swap k with the successor
 			// and then recursively delete
 			if (node.children[i + 1].keyCount >= t) {
 				Pair<K1, V1> successor = getSuccessor(node.children[i + 1]);
 				node.keys[i] = successor;
 				delete(node.children[i + 1], successor.key);
 				return;
 			}
 			
 			// if neither children have t keys, then both are at t+1, so we can merge them
 			// into 1 and add k into them.
 			Node<K1, V1> left = node.children[i];
 			Node<K1, V1> right = node.children[i + 1];
 			
 			// First add k as the rightmost child of left
 			left.keys[left.keyCount] = node.keys[i];
 			left.keyCount++;
 			
 			// Connect the right null child of the new k to the leftmost child of right (english lol)
 			left.children[left.keyCount] = right.children[0];
 			
 			// Add all the keys from the right to the left
 			for (int j = 0; j < right.keyCount; j++) {
 				left.keys[left.keyCount] = right.keys[j];
 				left.keyCount++;
 				left.children[left.keyCount] = right.children[j + 1]; // Must do it this way to catch the last child
 			}
 			
 			// Now we want to remove our k from the initial
 			for (int j = i + 1; j < node.keyCount; j++) { 
 				node.keys[j - 1] = node.keys[j];
 				node.children[j] = node.children[j + 1];
 			}
 			
 			node.keyCount--;
 			delete(node.children[i], key);
 			return;
 		} else {
 			// if x is internal, k is not in x so there must be a child with k.
 			// i currently points to the child where k would be.
 			Node<K1, V1> potential = node.children[i];
 			Node<K1, V1> sibling;
 			
 			// if the potential node has t keys, recursively descend
 			if (potential.keyCount >= t) {
 				delete(node.children[i], key);
 				return;
 			} else if (i > 0 && node.children[i - 1].keyCount >= t) {
 				// if left sibling of potential node has t keys, we want to do a rotation
 				sibling = node.children[i - 1];
 				// shift everyone over by 1 in potential to the right
 				for (int j = potential.keyCount; j >= 0; j--) {
 					potential.keys[j] = potential.keys[j - 1];
 					potential.children[j + 1] = potential.children[j];
 				}
 				potential.children[1] = potential.children[0]; // shift last child over.
 				potential.children[0] = sibling.children[sibling.keyCount]; 
 				
 				// bring in the left key in the parent of the potential, and swap it
 				// with rightmost key in the left sibling
 				potential.keys[0] = node.keys[i-1];
 				node.keys[i-1] = sibling.keys[sibling.keyCount - 1];
 				sibling.keys[sibling.keyCount-1] = null;
 				sibling.children[sibling.keyCount] = null;
 				sibling.keyCount--;
 				
 				// recurse down
 				delete(potential, key);
 			} else if (i < node.keyCount && node.children[i + 1].keyCount >= t) {
 				// do a similar rotation if right sibling of potential node has t keys
 				sibling = node.children[i + 1];
 				
 				// swap in the right key in the parent of the potential, and replace it
 				// with the leftomst key in the right sibling
 				potential.keys[potential.keyCount] = node.keys[i];
 				potential.keyCount++;
 				potential.children[potential.keyCount] = sibling.children[0];
 				node.keys[i] = sibling.keys[0];
 				
 				// shift everyone over by 1 in sibling
 				sibling.children[0] = sibling.children[1];
 				for (int j = 1; j < sibling.keyCount; j++) {
 					sibling.keys[j - 1] = sibling.keys[j];
 					sibling.children[j] = sibling.children[j + 1];
 				}
 				sibling.keyCount--;
 
 				// recurse down
 				delete(potential, key);
 			} else {
 				// if neither sibling have at least t, then we merge such that we get potential-left
 				if (i == 0) {
 					i++; // do this so that we can just always have the left and potential logic
 				}
 				
 				sibling = node.children[i - 1];
 				potential = node.children[i];
 				
 				// append key from parent to left sibling
 				sibling.keys[sibling.keyCount] = node.keys[i-1];
 				sibling.keyCount++;
 				sibling.children[sibling.keyCount] = potential.children[0];
 				
 				// append everyone from potential to left sibling
 				for (int j = 0; j < potential.keyCount; j++) {
 					sibling.keys[sibling.keyCount] = potential.keys[j];
 					sibling.keyCount++;
 					sibling.children[sibling.keyCount] = potential.children[j + 1];
 				}
 				
 				// shift everyone left in the parent
				for (int j = i; j < node.keyCount; j++) {
 					node.keys[j - 1] = node.keys[j];
 					node.children[j] = node.children[j + 1];
 				}
 				
 				node.keyCount--;
 				
 				// if root size shrunk, change it
 				if (root.keyCount == 0) root = root.children[0];
 				
 				// recurse down
 				delete(sibling, key);
 				
 			}
 		}
 	}
 
 	// Get the rightmost node of a subtree
 	public Pair<K1, V1> getPredecessor(Node<K1, V1> node) {
 		while (!node.leaf) {
 			node = node.children[0];
 		}
 		return node.keys[node.keyCount - 1];
 	}
 	
 	// Get the leftmost node of a subtree
 	public Pair<K1, V1> getSuccessor(Node<K1, V1> node) {
 		while (!node.leaf) {
 			node = node.children[0];
 		}
 		return node.keys[0];
 	}
 
 	public static void runTests() {
 		BTree<Integer, String> tree = new BTree<Integer, String>();
 
 		tree.insert(new Pair<Integer, String>(1, "Test"));
 		tree.insert(new Pair<Integer, String>(2, "Test2"));
 		tree.insert(new Pair<Integer, String>(3, "Test3"));
 		tree.insert(new Pair<Integer, String>(4, "Test4"));
 		tree.insert(new Pair<Integer, String>(5, "Test5"));
 		tree.insert(new Pair<Integer, String>(6, "Test6"));
 
 		if (!tree.root
 				.toString()
 				.equals(" [  [ null 1 null ]  2  [ null 3 null ]  4  [ null 5 null 6 null ]  ] ")) {
 			System.out
 					.println("Test Failed:\nExpected: [  [ null 1 null ]  2  [ null 3 null ]  4  [ null 5 null 6 null ]  ]\nReceived: "
 							+ tree.root.toString());
 			System.exit(0);
 		}
 
 		if (tree.search(5) == null || !tree.search(5).value.equals("Test5")) {
 			System.out
 					.println("Test Failed:\nExpected: { 5 => Test5 }\nReceived: "
 							+ tree.search(5));
 			System.exit(0);
 		}
 
 		if (tree.search(10) != null) {
 			System.out.println("Test Failed:\nExpected: null\nReceived: "
 					+ tree.search(10));
 			System.exit(0);
 		}
 
 		// Exercise 18.2-1 CLRS
 		String result = " [  [  [ null A null ]  B  [ null C null D null E null ]  F  [ null H null ]  ]  K  [  [ null L null ]  M  [ null N null P null ]  ]  Q  [  [ null R null S null ]  T  [ null V null ]  W  [ null X null Y null Z null ]  ]  ] ";
 		BTree<Character, String> tree2 = new BTree<Character, String>();
 		char[] characters = new char[] { 'F', 'S', 'Q', 'K', 'C', 'L', 'H',
 				'T', 'V', 'W', 'M', 'R', 'N', 'P', 'A', 'B', 'X', 'Y', 'D',
 				'Z', 'E' };
 		for (int i = 0; i < characters.length; i++) {
 			tree2.insert(new Pair<Character, String>(characters[i], "char" + i));
 		}
 
 		if (!tree2.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree2.root);
 			System.exit(0);
 		}
 		
 		// Test deleting
 		BTree<Integer, String> tree3 = new BTree<Integer, String>();
 
 		tree3.insert(new Pair<Integer, String>(1, "Key 1"));
 		tree3.insert(new Pair<Integer, String>(2, "Key 2"));
 		tree3.insert(new Pair<Integer, String>(3, "Key 3"));
 		tree3.insert(new Pair<Integer, String>(4, "Key 4"));
 		
 		result = " [  [ null 1 null ]  2  [ null 3 null 4 null ]  ] ";
 		if (!tree3.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree3.root);
 			System.exit(0);
 		}
 		
 		tree3.delete(2);
 		result = " [  [ null 1 null ]  3  [ null 4 null ]  ] ";
 		if (!tree3.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree3.root);
 			System.exit(0);
 		}
 		
 		tree3.delete(1);
 		result = " [ null 3 null 4 null ] ";
 		if (!tree3.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree3.root);
 			System.exit(0);
 		}
 		
 		tree3.delete(4);
 		result = " [ null 3 null ] ";
 		if (!tree3.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree3.root);
 			System.exit(0);
 		}
 		
 		tree3.delete(3);
 		result = " [ null ] ";
 		if (!tree3.root.toString().equals(result)) {
 			System.out.println("Test Failed:\nExpected: " + result
 					+ "\nReceived: " + tree3.root);
 			System.exit(0);
 		}
 	}
 
 	public static void main(String... args) {
 		runTests();
 		
 		BTree<Integer, String> tree = new BTree<Integer, String>();
 
 
 		tree.insert(new Pair<Integer, String>(1, "Test"));
 		tree.insert(new Pair<Integer, String>(2, "Test2"));
 		tree.insert(new Pair<Integer, String>(3, "Test3"));
 		tree.insert(new Pair<Integer, String>(4, "Test4"));
 		tree.insert(new Pair<Integer, String>(5, "Test5"));
 		tree.insert(new Pair<Integer, String>(6, "Test6"));
 		tree.insert(new Pair<Integer, String>(7, "Test7"));
 		tree.insert(new Pair<Integer, String>(8, "Test8"));
 		tree.insert(new Pair<Integer, String>(9, "Test9"));
 		tree.insert(new Pair<Integer, String>(10, "Test10"));
		tree.delete(1);
 		
 		System.out.println(tree.root);
 	}
 
 }
