 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import org.junit.Test;
 
 /** Test case for AVL tree
  * @author Jared Moore
  * @author edited by Steven Han
  * @version Jan 31, 2013
  */
 public class AVLTest {
 
 	@Test
 	public void addAll() {
 		ArrayList<Integer> array = new ArrayList<Integer>();
 		array.add(50);
 		array.add(25);
 		array.add(75);
 		array.add(13);
 		array.add(87);
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.addAll(array); // note that my addAll will call add
		assertEquals((int) tree.getRoot().getData(), 50);
		assertEquals((int) tree.getRoot().getLeft().getData(), 25);
		assertEquals((int) tree.getRoot().getRight().getData(), 75);
		assertEquals((int) tree.getRoot().getLeft().getLeft().getData(), 13);
		assertEquals((int) tree.getRoot().getRight().getRight().getData(), 87);
 	}
 
 	/**
 	 * Test method for {@link AVL#add(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testRightHeavy() {
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25);
 		tree.add(13);
 		tree.add(50);
 		tree.add(37);
 		tree.add(75);
 		tree.add(90);
 		assertEquals(50, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(37, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(90, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testLeftHeavy() {
 
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(75);
 		tree.add(90);
 		tree.add(50);
 		tree.add(60);
 		tree.add(25);
 		tree.add(13);
 		assertEquals(50, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(60, (int) tree.getRoot().getRight().getLeft().getData());
 		assertEquals(90, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testLeftRightHeavy() {
 
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(50);
 		tree.add(75);
 		tree.add(25);
 		tree.add(13);
 		tree.add(37);
 		tree.add(30);
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(30, (int) tree.getRoot().getLeft().getRight().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testRightLeftHeavy() {
 
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25); 
 		tree.add(13);
 		tree.add(50);
 		tree.add(75);
 		tree.add(37);
 		tree.add(38);
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 		assertEquals(13, (int) tree.getRoot().getLeft().getLeft().getData());
 		assertEquals(38, (int) tree.getRoot().getRight().getLeft().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getRight().getData());
 	}
 
 	@Test
 	public void testSimpleRightLeft() {
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(50);
 		tree.add(75);
 		tree.add(60);
 
 		assertEquals(60, (int) tree.getRoot().getData());
 		assertEquals(75, (int) tree.getRoot().getRight().getData());
 		assertEquals(50, (int) tree.getRoot().getLeft().getData());
 	}
 
 	@Test
 	public void testSimpleLeftRight() {
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(50);
 		tree.add(25);
 		tree.add(37);
 
 		assertEquals(37, (int) tree.getRoot().getData());
 		assertEquals(25, (int) tree.getRoot().getLeft().getData());
 		assertEquals(50, (int) tree.getRoot().getRight().getData());
 	}
 
 	/**
 	 * Test method for {@link AVL#contains(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testContains() {
 		AVL<Integer> tree = new AVL<Integer>();
 
 		tree.add(50);
 		tree.add(25);
 		tree.add(75);
 		tree.add(13);
 		tree.add(87);
 
 		assertEquals(true, tree.contains(50));
 		assertEquals(false, tree.contains(100));
 		assertEquals(false, tree.contains(null));
 		tree.add(null);
 		assertEquals(true, tree.contains(null));
 		assertEquals(true, tree.contains(13));
 	}
 
 	/**
 	 * Test method for {@link AVL#remove(java.lang.Comparable)}.
 	 */
 	@Test
 	public void testRemove() {
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(5);
 		tree.add(3);
 		tree.add(2);
 		tree.add(4);
 		tree.add(7);
 		tree.add(6);
 		tree.add(8);
 		tree.add(null);
 
 		tree.remove(3);
 		tree.remove(5);
 		tree.remove(8);
 		tree.remove(null);
 
 		assertEquals(false, tree.contains(3));
 		assertEquals(false, tree.contains(50));
 		assertEquals(false, tree.contains(8));
 		assertEquals(false, tree.contains(null));
 	}
 
 	@Test
 	public void testRemoveEdgeCase1(){
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25); 
 		tree.add(13);
 		tree.add(50);
 
 		tree.remove(25);
 		assertEquals(true, tree.getRoot().getData() != 25);
 	}
 
 	@Test
 	public void testRemoveEdgeCase2(){
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(13);
 		tree.add(25); 
 		tree.add(50);
 
 		tree.remove(25);
 		assertEquals(true, tree.getRoot().getRight().getData() == 50);
 		assertEquals(true, tree.getRoot().getData() != 25);
 	}
 
 	@Test
 	public void testRemoveEdgeCase3(){
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(25); 
 		tree.add(13);
 
 		tree.add(50);
 		tree.add(10);
 
 		tree.remove(50);
 		assertEquals(true, tree.getRoot().getData() == 13);
 		assertEquals(true, tree.getRoot().getLeft().getData() == 10);
 		assertEquals(true, tree.getRoot().getRight().getData() == 25);
 	}
 
 	@Test
 	public void testRemoveEdgeCase4(){
 		AVL<Integer> tree = new AVL<Integer>();
 		tree.add(null);
 
 		tree.remove(null);
 		assertEquals(false, tree.contains(null));
 		assertEquals(true, tree.getRoot() == null);
 	}
 
 	@Test
 	public void testRemoveBossCase(){
 		AVL<Integer> tree = new AVL<Integer>();
 		for (int i = 0; i < 100; i++)
 			tree.add(i);
 
 		assertEquals(true, tree.contains(3));
 		assertEquals(true, tree.contains(2));
 		assertEquals(true, tree.contains(9));
 		assertEquals(true, tree.contains(10));
 		assertEquals(true, tree.contains(21));
 		assertEquals(true, tree.contains(51));
 		assertEquals(true, tree.contains(1));
 		assertEquals(true, tree.contains(7));
 		assertEquals(true, tree.contains(95));
 		assertEquals(true, tree.contains(96));
 		assertEquals(true, tree.contains(97));
 		assertEquals(true, tree.contains(49));
 
 		tree.remove(30);
 		tree.remove(20);
 		tree.remove(90);
 		tree.remove(1);
 		tree.remove(11);
 		tree.remove(50);
 
 		assertEquals(false, tree.contains(30));
 		assertEquals(false, tree.contains(20));
 		assertEquals(false, tree.contains(90));
 		assertEquals(false, tree.contains(1));
 		assertEquals(false, tree.contains(11));
 		assertEquals(false, tree.contains(50));
 
 		//random test cases
 		assertEquals(true, tree.contains(3));
 		assertEquals(true, tree.contains(2));
 		assertEquals(true, tree.contains(9));
 		assertEquals(true, tree.contains(10));
 		assertEquals(true, tree.contains(21));
 		assertEquals(true, tree.contains(51));
 
 		assertEquals(true, tree.contains(7));
 		assertEquals(true, tree.contains(95));
 		assertEquals(true, tree.contains(96));
 		assertEquals(true, tree.contains(97));
 		assertEquals(true, tree.contains(49));
 	}
 }
