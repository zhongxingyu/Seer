 public class BinaryTree<T> {
 
 	public Node<T> root;
 
 	public BinaryTree ( ) {
 		root = null;
 	}
 
 	public BinaryTree (Node<T> t) {
 		root = t;
 	}
 	
 	public BinaryTree (T in) {
 		root = new Node<T> (in);
 	}
 	
 	public BinaryTree (T in, Node<T> left, Node<T> right) {
 		root = new Node<T> (in, left, right);
 	}
 	
 	public void print() {
 		if (root != null) {
 			printHelper (root, 0);
 		}
 	}
 
 	private static final String kIndent = "    ";
 
 	private void printHelper(Node<T> root, int indent) {
 		if (root.left != null) 
 			printHelper(root.left, indent + 1);
		println (root.data, indent);
 		if (root.right != null) 
 			printHelper(root.right, indent + 1);
 	}
 
 	private static void println(Object obj, int indent) 
 	{
 		for (int k = 0; k < indent; k++) {
 			System.out.print(kIndent);
 		}
 		System.out.println(obj);
 	}
 	
 }
 
