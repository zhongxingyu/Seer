 package assignment7;
 
 public class EpidemicSystem {
 	Node root = null;
 	
 	static class Node {
 		Node left;
 		Node right;
 		int patient;
 		
 		public Node( int value ) {
 			this.patient = value;
 		}
 
 		public int getPatient() {
 			return patient;
 		}
 
 		public void setLeftChild(Node root) {
 			this.left = root;
 		}
 
 		public boolean delete(int value, Node parent) {
 			if (value < this.patient) {
                 if (left != null)
                       return left.delete(value, this);
                 else
                       return false;
           } else if (value > this.patient) {
                 if (right != null)
                       return right.delete(value, this);
                 else
                       return false;
           } else {
                 if (left != null && right != null) {
                       this.patient = right.minValue();
                       right.delete(this.patient, this);
                 } else if (parent.left == this) {
                       parent.left = (left != null) ? left : right;
                 } else if (parent.right == this) {
                       parent.right = (left != null) ? left : right;
                 }
                 return true;
           }
 		}
 
 		private int minValue() {
 			if ( left == null)
 				return patient;
 			else
 				return left.minValue();
 		}
 
 		public Node getLeft() {
 			return this.left;
 		}
 	}
 
 	public void insert( Node node, int patient) {
 		if ( patient < node.patient) {
 			if ( node.left != null ) {
 				insert( node.left, patient );
 			} else {
 				node.left = new Node(patient);
 			}
 		} else if ( patient > node.patient) {
 			if ( node.right != null ) {
 				insert( node.right, patient );
 			} else {
 				node.right = new Node(patient);
 			}
 		}
 	}
 	public Node search( Node node, int patient ) {
		while( node.patient != patient && node != null ) {
            		if( patient < node.patient ) node = node.left;
            		else if( patient > node.patient ) node = node.right;
            		else node = node.right;
            		return node;
            	}
 	}
 	
 		public boolean delete(int value) {
             if (root == null)
                   return false;
             else {
                   if (root.getPatient() == value) {
                         Node auxRoot = new Node(0);
                         auxRoot.setLeftChild(root);
                         boolean result = root.delete(value, auxRoot);
                         root = auxRoot.getLeft();
                         return result;
                   } else {
                         return root.delete(value, null);
                   }
             }
       }
 		
 	public void printInOrder(Node node) {
 		if (node != null) {
 			printInOrder(node.left);
 			System.out.println("  Traversed " + node.patient);
 			printInOrder(node.right);
 		}
 	}
 	
 	public static void main( String[] agrs) {
 		new EpidemicSystem().run();
 	}
 	
 	public void run() {
 		Node root = new Node(10);
 		this.root = root;
 		insert(root, 6);
 		insert(root, 9);
 		insert(root, 15);
 		insert(root, 20);
 		insert(root, 2);
 		insert(root, 5);
 		insert(root, 9);
 		insert(root, 18);
 		insert(root, 19);
 		printInOrder(root);
 		delete(15);
 		printInOrder(root);
 		
 		
 	}
 }
