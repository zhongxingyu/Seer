 package avlTree;
 
 import range.*;
 import tree.*;
 
 public class AvlTree implements Tree{
 
   private static AvlNode doubleWithLeftChild(final AvlNode k3) {
     k3.left = AvlTree.rotateWithRightChild(k3.left);
     return AvlTree.rotateWithLeftChild(k3);
   }
 
 
   private static AvlNode doubleWithRightChild(final AvlNode k1) {
     k1.right = AvlTree.rotateWithLeftChild(k1.right);
     return AvlTree.rotateWithRightChild(k1);
   }
 
   /**
    * Return the height of node t, or -1, if null.
    */
   private static int height(final AvlNode t) {
     return t == null ? -1 : t.height;
   }
 
   /**
    * Return maximum of lhs and rhs.
    */
   private static int max(final int lhs, final int rhs) {
     return lhs > rhs ? lhs : rhs;
   }
 
   /**
    * Rotate binary tree node with left child. For AVL trees, this is a single
    * rotation for case 1. Update heights, then return new root.
    */
   private static AvlNode rotateWithLeftChild(final AvlNode k2) {
     final AvlNode k1 = k2.left;
     k2.left = k1.right;
     k1.right = k2;
     k2.height = AvlTree.max(AvlTree.height(k2.left), AvlTree.height(k2.right)) + 1;
     k1.height = AvlTree.max(AvlTree.height(k1.left), k2.height) + 1;
     return k1;
   }
 
 
   private static AvlNode rotateWithRightChild(final AvlNode k1) {
     final AvlNode k2 = k1.right;
     k1.right = k2.left;
     k2.left = k1;
     k1.height = AvlTree.max(AvlTree.height(k1.left), AvlTree.height(k1.right)) + 1;
     k2.height = AvlTree.max(AvlTree.height(k2.right), k1.height) + 1;
     return k2;
   }
 
   /** The tree root. */
   private AvlNode root;
 
   /**
    * Construct the tree.
    */
   public AvlTree() {
     this.root = null;
   }
 
   boolean balanced() {
     return balanced(this.root);
   }
 
   private boolean balanced(final AvlNode an) {
     if (an == null) {
       return true;
     }
     final int lh = AvlTree.height(an.left);
     final int rh = AvlTree.height(an.right);
     return ((lh == rh) || (lh == rh + 1) || (lh + 1 == rh))
         && balanced(an.left) && balanced(an.right);
   }
 
 
   private int elementAt(final AvlNode t) {
     return t == null ? -1 : t.element;
   }
 
   public int find(final int x) {
     return elementAt(find(x, this.root));
   }
 
 
   private AvlNode find(final int x, AvlNode t) {
     while (t != null) {
       if (x < t.element) {
         t = t.left;
       } else if (x > t.element) {
         t = t.right;
       } else {
         return t; // Match
       }
     }
 
     return null; // No match
   }
 
 
   public int findMax() {
     return elementAt(findMax(this.root));
   }
 
 
   private AvlNode findMax(AvlNode t) {
     if (t == null) {
       return t;
     }
 
     while (t.right != null) {
       t = t.right;
     }
     return t;
   }
 
 
   public int findMin() {
     return elementAt(findMin(this.root));
   }
 
 
   private AvlNode findMin(AvlNode t) {
     if (t == null) {
       return t;
     }
 
     while (t.left != null) {
       t = t.left;
     }
     return t;
   }
 
 
   public void insert(final int x) {
     this.root = insert(x, this.root);
   }
 
 
   private AvlNode insert(final int x, AvlNode t) {
     if (t == null) {
       t = new AvlNode(x, null, null);
     } else if (x < t.element) {
       t.left = insert(x, t.left);
       if (AvlTree.height(t.left) - AvlTree.height(t.right) == 2) {
         if (x < t.left.element) {
           t = AvlTree.rotateWithLeftChild(t);
         } else {
           t = AvlTree.doubleWithLeftChild(t);
         }
       }
     } else if (x > t.element) {
       t.right = insert(x, t.right);
       if (AvlTree.height(t.right) - AvlTree.height(t.left) == 2) {
         if (x > t.right.element) {
           t = AvlTree.rotateWithRightChild(t);
         } else {
           t = AvlTree.doubleWithRightChild(t);
         }
       }
     } else {
       ; // Duplicate; do nothing
     }
     t.height = AvlTree.max(AvlTree.height(t.left), AvlTree.height(t.right)) + 1;
     return t;
   }
 
 
   public boolean isEmpty() {
     return this.root == null;
   }
 
 
   public void makeEmpty() {
     this.root = null;
   }
 
   boolean maxElement(final int max) {
     return maxElement(max, this.root);
   }
 
   private boolean maxElement(final int max, final AvlNode t) {
     if (t == null) {
       return true;
     }
     if (max < t.element) {
       return false;
     }
     return maxElement(max, t.left) && maxElement(max, t.right);
   }
 
   boolean minElement(final int min) {
     return minElement(min, this.root);
   }
 
   private boolean minElement(final int min, final AvlNode t) {
     if (t == null) {
       return true;
     }
     if (min > t.element) {
       return false;
     }
     return minElement(min, t.left) && minElement(min, t.right);
   }
 
   boolean ordered() {
     return ordered(this.root, new Range());
   }
 
   private boolean ordered(final AvlNode t, final Range range) {
     if (t == null) {
       return true;
     }
     if (!range.inRange(t.element)) {
       return false;
     }
     boolean ret = true;
     ret = ordered(t.left, range.setUpper(t.element));
     ret = ret && ordered(t.right, range.setLower(t.element));
     return ret;
   }
 
 
   public void printTree() {
     if (isEmpty()) {
       System.out.println("Empty tree");
     } else {
       printTree(this.root);
     }
   }
 
 
   private void printTree(final AvlNode t) {
     if (t != null) {
       printTree(t.left);
       System.out.println(t.element);
       printTree(t.right);
     }
   }
 
 
  public void delete( int x ) {
 	root = remove(x, root);
    }
 public AvlNode remove(int x, AvlNode t) {
 	if (t==null) 	{ 
		System.out.println("Sorry but you're mistaken, " + t + " doesn't exist in this tree :)\n");
 		return null;
 	}
	System.out.println("Remove starts... " + t.element + " and " + x);
 	if (compareTo(x,t.element) < 0 ) {
 		t.left = remove(x,t.left);
 		int l = t.left != null ? t.left.height : 0;
 		
 		if((t.right != null) && (t.right.height - l >= 2)) {
 			int rightHeight = t.right.right != null ? t.right.right.height : 0;
 			int leftHeight = t.right.left != null ? t.right.left.height : 0;
 			
 			if(rightHeight >= leftHeight)
 				t = rotateWithLeftChild(t);				
 			else
 				t = doubleWithRightChild(t);
 		}
 	}
 	else if (compareTo(x,t.element) > 0) {
 		t.right = remove(x,t.right);
 		int r = t.right != null ? t.right.height : 0;
 		if((t.left != null) && (t.left.height - r >= 2)) {
 			int leftHeight = t.left.left != null ? t.left.left.height : 0;
 			int rightHeight = t.left.right != null ? t.left.right.height : 0;
 			if(leftHeight >= rightHeight)
 				t = rotateWithRightChild(t);				
 			else
 				t = doubleWithLeftChild(t);
 		}
 	}
 
 	else if(t.left !=null) {
 		t.element = findMax(t.left).element;
 		remove(t.element, t.left);
 	
 		if((t.right != null) && (t.right.height - t.left.height >= 2)) {
 			int rightHeight = t.right.right != null ? t.right.right.height : 0;
 			int leftHeight = t.right.left != null ? t.right.left.height : 0;
 	
 			if(rightHeight >= leftHeight)
 				t = rotateWithLeftChild(t);				
 			else
 				t = doubleWithRightChild(t);
 		}
 	}
 	
 	else
 		t = (t.left != null) ? t.left : t.right;
 	
 	if(t != null) {
 		int leftHeight = t.left != null ? t.left.height : 0;
 		int rightHeight = t.right!= null ? t.right.height : 0;
 		t.height = Math.max(leftHeight,rightHeight) + 1;
 	}
 	return t;
 	
 }
 
   boolean wellFormed() {
     return wellFormed(this.root);
   }
 
   private boolean wellFormed(final AvlNode an) {
     if (an == null) {
       return true;
     }
     if (AvlTree.height(an) != Math.max(AvlTree.height(an.left), AvlTree
         .height(an.right)) + 1) {
       return false;
     }
     return wellFormed(an.left) && wellFormed(an.right);
   }
   
   private int compareTo(int x,int elem){
 	  if (x < elem)
 		  return -1;
 	  else if (x == elem)
 		  return 0;
 	  else
 		  return 1;
   }
 }
