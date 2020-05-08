 package se.chalmers.datastrukt.lab2;
 
 import testSortCol.CollectionWithGet;
 import datastructures.BinarySearchTree;
 
 
 public class SplayTree<E extends Comparable<? super E>> extends
 		BinarySearchTree<E> implements CollectionWithGet<E> {
 
     /* Rotera 1 steg i hgervarv, dvs 
     x'                 y'
    / \                / \
   y'  C   -->        A   x'
  / \                    / \  
 A   B                  B   C
 */
 	private void zag(Entry x) {
 		Entry y = x.left;
 		E temp = x.element;
 		x.element = y.element;
 		y.element = temp;
 		x.left = y.left;
 		if (x.left != null) {
 			x.left.parent = x;
 		}
 		y.left = y.right;
 		y.right = x.right;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		x.right = y;
 
 	} // rotateRight
 
 /* Rotera 1 steg i vnstervarv, dvs 
     x'                 y'
    / \                / \
   A   y'  -->        x'  C
      / \            / \  
     B   C          A   B   
 */
 	
 	private void zig(Entry x) {
 		Entry y = x.right;
 		E temp = x.element;
 		x.element = y.element;
 		y.element = temp;
 		x.right = y.right;
 		if (x.right != null) {
 			x.right.parent = x;
 		}
 		y.right = y.left;
 		y.left = x.left;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		x.left = y;
 
 	} // rotateLeft
 
 /* Rotera 2 steg i hgervarv, dvs 
     x'                  z'
    / \                /   \
   y'  D   -->        y'    x'
  / \                / \   / \
 A   z'             A   B C   D
    / \  
   B   C  
 */
 	private void zagZig(Entry x) {
 		Entry y = x.left, z = x.left.right;
 		E e = x.element;
 		x.element = z.element;
 		z.element = e;
 		y.right = z.left;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		z.left = z.right;
 		z.right = x.right;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		x.right = z;
 		z.parent = x;
 
 	} // doubleRotateRight
 
 /* Rotera 2 steg i vnstervarv, dvs 
     x'                  z'
    / \                /   \
   A   y'   -->       x'    y'
      / \            / \   / \
     z   D          A   B C   D
    / \  
   B   C  
 */
 	private void zigZag(Entry x) {
 		Entry y = x.right, z = x.right.left;
 		E e = x.element;
 		x.element = z.element;
 		z.element = e;
 		y.left = z.right;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		z.right = z.left;
 		z.left = x.left;
 
 		if (z.left != null) {
 			z.left.parent = z;
 		}
 		x.left = z;
 		z.parent = x;
 
 	} // doubleRotateLeft
 
 	
 	/* Rotera ? steg i ?varv, dvs 
         x' 								z'
        / \							   / \
       a	  y'						  y'  d
          / \         --->			 / \ 
         b   z'						x'  c
            / \					   / \
           c   d					  a   b
 */
 	private void zigZig( Entry x) {
 		Entry y = x.right, z = x.right.right;
 		E tmp = x.element;
 		x.element = z.element;
 		z.element = tmp;
 		x.right = z.right;
 		if (x.right != null) {
 			x.right.parent = x;
 		}
 		y.right = z.left;
 		if (y.right != null) {
 			y.right.parent = y;
 		}
 		z.right = y.left;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		z.left = x.left;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		x.left = y;
 		y.left = z;
 		
 	}
 
 
 	
 	/* Rotera ? steg i ?varv, dvs 
         	x'				z'
            / \			   / \
           y'  d			  a   y'
          / \			     / \
         z'  c				b   x'
        / \					   / \
       a   b					  c   d
 
 	 */
 	private void ZagZag( Entry x) {
 		Entry y = x.left, z = x.left.left;
 		E tmp = x.element;
 		x.element = z.element;
 		z.element = tmp;
 		x.left = z.left;
 		if (x.left != null) {
 			x.left.parent = x;
 		}
 		y.left = z.right;
 		if (y.left != null) {
 			y.left.parent = y;
 		}
 		z.left = y.right;
 		if (z.left != null) {
 			z.left.parent = z;
 		}
 		z.right = x.right;
 		if (z.right != null) {
 			z.right.parent = z;
 		}
 		y.right = z;
 		x.right = y;
 		
 	}
 	//int searchMetodh = 0;
 	public Entry search(E e, Entry entry) {
 		//searchMetodh++;
 		//System.out.println("search " + searchMetodh);
 		Entry previous = null;
 		Entry current = entry;
 		int jfr = e.compareTo(current.element);
 		while (jfr != 0) {
 			if (jfr > 0) {
 				previous = current;
 				current = current.right;
 			} else {
 				previous = current;
 				current = current.left;
 			}
 
 			if (current == null ) {
 				splay(previous);
 				return null;
 			}
 			jfr = e.compareTo(current.element);
 		}
 		return current;
 	}
 	//private int splayMethod = 0;
 	public Entry splay(Entry entry) {
 		//splayMethod++;
 		//System.out.println("splayMetoden krs antal ggr " + splayMethod);
 		if(entry == null || entry.parent == null) {
 			return null;
 		}
 		while (entry.parent != null) {
 			System.out.println("splay");
 			if (entry.parent.parent == null) {
 				if (entry.parent.left != null && entry.parent.left.equals(entry)) {
 					zag(entry.parent);
 				} else {
 					zig(entry.parent);
 				}
 				entry = entry.parent;
 				return entry;
 			} else {
 				Entry grandParent = entry.parent.parent;
 				if (grandParent.left != null && grandParent.left.left != null
 						&& grandParent.left.left.equals(entry)) {
 					ZagZag(grandParent);
 					entry= grandParent;
 				} else if (grandParent.right != null
 						&& grandParent.right.left != null
 						&& grandParent.right.left.equals(entry)) {
 					zigZag(grandParent);
 					 entry = entry.parent;
 				} else if (grandParent.left != null
 						&& grandParent.left.right != null
 						&& grandParent.left.right.equals(entry)) {
 					zagZig(grandParent);
 					 entry = entry.parent;
 				} else if (grandParent.right != null
 						&& grandParent.right.right != null
 						&& grandParent.right.right.equals(entry)) {
 					zigZig(grandParent);
 					 entry = grandParent;
 				}
 				//entry = grandParent;
 			}
 		}
 		return entry;
 	}
 	
 	//int get = 0;
 	@Override
 	public E get(E e) {
 		//get++;
 		//System.out.println("getmetoden krs antal ggr" + get);
 		if (e == null || root == null) {
 			return null;
 		}
 
 		if (e.compareTo(root.element) == 0) {
 			return e;
 		}
 
 		Entry entry = search(e, root);
 		if (entry == null) {
 			return null;
 		}
 		entry = splay(entry);
 		
 		return entry.element;
 	}
 }
