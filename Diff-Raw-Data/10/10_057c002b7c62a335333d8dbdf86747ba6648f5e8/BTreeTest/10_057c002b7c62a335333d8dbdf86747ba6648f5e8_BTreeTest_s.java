 package com.github.davidmoten.structures.btree;
 
 import static com.github.davidmoten.structures.btree.BTree.builder;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Test;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Iterables;
 
 public class BTreeTest {
 
 	private static final int MANY_VALUES = 1000;
 
 	/**
 	 * Given nothing
 	 * 
 	 * When I create a BTree of degree 1
 	 * 
 	 * Then an exception is thrown
 	 * 
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testInstantiatingBTreeOfDegree1ThrowsException() {
 		builder(Integer.class).degree(1).build();
 	}
 
 	/**
 	 * Given an empty BTree<String> of degree 3
 	 * 
 	 * When I insert string "1"
 	 * 
 	 * Then the node is inserted as the first key in the first node of the BTree
 	 * 
 	 * When I insert string "2"
 	 * 
 	 * Then the node is inserted as the second key in the first node of the
 	 * BTree after "2"
 	 * 
 	 */
 	@Test
 	public void test1() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build();
 		t.add(1);
 		assertEquals(1, t.getKeys().size());
 		assertEquals(1, (int) t.getKeys().get(0).value());
 		t.add(2);
 		assertEquals(2, t.getKeys().size());
 		assertEquals(1, (int) t.getKeys().get(0).value());
 		assertEquals(2, (int) t.getKeys().get(1).value());
 	}
 
 	/**
 	 * Given an empty BTree<String> of degree 3
 	 * 
 	 * When I insert strings "1","2,"3" in order
 	 * 
 	 * Then the top node is {"2"}, left is node {"1"}, right is node {"3"}
 	 * 
 	 */
 	@Test
 	public void test2() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(1, 2, 3);
 		assertEquals(1, t.getKeys().size());
 		Key<Integer> top = t.getKeys().get(0);
 		assertEquals(2, (int) top.value());
 		assertEquals(1, top.getLeft().get().getKeys().size());
 		assertEquals(1, (int) top.getLeft().get().getKeys().get(0).value());
 		assertEquals(1, top.getRight().get().getKeys().size());
 		assertEquals(3, (int) top.getRight().get().getKeys().get(0).value());
 	}
 
 	/**
 	 * Given an empty BTree<Integer> of degree 3
 	 * 
 	 * When I insert 2,1 in order
 	 * 
 	 * Then the top node is 1,2
 	 * 
 	 */
 	@Test
 	public void test2_5() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build().add(2, 1);
 		assertEquals(2, t.getKeys().size());
 		assertEquals(1, (int) t.getKeys().get(0).value());
 		assertEquals(2, (int) t.getKeys().get(1).value());
 	}
 
 	/**
 	 * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
 	 * order.
 	 * 
 	 * When I insert 4
 	 * 
 	 * Then the node containing 3 has 4 added to it.
 	 * 
 	 */
 	@Test
 	public void test3() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(1, 2, 3, 4);
 		Key<Integer> top = t.getKeys().get(0);
 		System.out.println(t);
 		assertEquals(4, (int) top.getRight().get().getKeys().get(1).value());
 	}
 
 	/**
 	 * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
 	 * order.
 	 * 
 	 * When I insert 0
 	 * 
 	 * Then the node containing 1 has 0 inserted at start.
 	 * 
 	 */
 	@Test
 	public void test4() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(1, 2, 3);
 		t.add(0);
 		Key<Integer> top = t.getKeys().get(0);
 		System.out.println(t);
 		assertEquals(0, (int) top.getLeft().get().getKeys().get(0).value());
 	}
 
 	/**
 	 * Given an empty BTree<String> of degree 3 with inserted values 1,2,3 in
 	 * order.
 	 * 
 	 * When I insert 0
 	 * 
 	 * Then the node containing 1 has 0 inserted at start.
 	 * 
 	 */
 	@Test
 	public void test5() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(1, 2, 3, 0);
 		Key<Integer> top = t.getKeys().get(0);
 		assertKeyValuesAre(top.getLeft().get().getKeys(), 0, 1);
 		assertKeyValuesAre(top.getRight().get().getKeys(), 3);
 	}
 
 	/**
	 * Given an empty BTree<String> of degree 3 with inserted values 1,2,3,0 in
	 * order.
 	 * 
	 * When I insert 0.5
 	 * 
	 * Then the root=0.5,2.0, node.left=0, node.right=1
 	 * 
 	 */
 	@Test
 	public void test6() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(10, 20, 30, 0, 5);
 		System.out.println(t);
 		assertKeyValuesAre(t.getKeys(), 5, 20);
 		assertKeyValuesAre(t.getKeys().get(0).getLeft().get().getKeys(), 0);
 		assertKeyValuesAre(t.getKeys().get(0).getRight().get().getKeys(), 10);
 	}
 
 	/**
 	 * <p>
 	 * Given an empty BTree<String> of degree 3
 	 * </p>
 	 * 
 	 * <p>
 	 * When I insert 1,2,3,4,5,6,7
 	 * </p>
 	 * 
 	 * <p>
 	 * Then returns this tree:
 	 * </p>
 	 * 
 	 * <pre>
 	 *     4
 	 *    /  \
 	 *   2     6
 	 *  / \   / \
 	 * 1  3  5   7
 	 * </pre>
 	 * 
 	 * <p>
 	 * This is worked example from wikipedia <a
 	 * href="http://en.wikipedia.org/wiki/B-tree">article</a>.
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test7() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(10, 20, 30, 40, 50, 60, 70);
 		assertKeyValuesAre(t.getKeys(), 40);
 		assertKeyValuesAre(t.getKeys().get(0).getLeft().get().getKeys(), 20);
 		assertKeyValuesAre(t.getKeys().get(0).getRight().get().getKeys(), 60);
 		assertKeyValuesAre(t.getKeys().get(0).getLeft().get().getKeys().get(0)
 				.getLeft().get().getKeys(), 10);
 		assertKeyValuesAre(t.getKeys().get(0).getLeft().get().getKeys().get(0)
 				.getRight().get().getKeys(), 30);
 		assertKeyValuesAre(t.getKeys().get(0).getRight().get().getKeys().get(0)
 				.getLeft().get().getKeys(), 50);
 		assertKeyValuesAre(t.getKeys().get(0).getRight().get().getKeys().get(0)
 				.getRight().get().getKeys(), 70);
 		System.out.println("iterated=" + Iterables.toString(t));
 	}
 
 	/**
 	 * <p>
 	 * Given an empty BTree<String> of EVEN degree 4
 	 * </p>
 	 * 
 	 * <p>
 	 * When 1,2,3,4 are inserted in order
 	 * </p>
 	 * 
 	 * <p>
 	 * Then b-tree looks like:
 	 * </p>
 	 * 
 	 * <pre>
 	 *   2
 	 *  / \
 	 * 1   3,4
 	 * </pre>
 	 * 
 	 */
 	@Test
 	public void testSplitWhenDegreeIsEven() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build()
 				.add(10, 20, 30, 40);
 		assertKeyValuesAre(t.getKeys(), 20);
 		assertKeyValuesAre(t.getKeys().get(0).getLeft().get().getKeys(), 10);
 		assertKeyValuesAre(t.getKeys().get(0).getRight().get().getKeys(), 30,
 				40);
 	}
 
 	/**
 	 * <p>
 	 * Given a BTree<String> of degree 3 with 1,2,3,4,5,6,7 inserted
 	 * </p>
 	 * 
 	 * <p>
 	 * When I find 1 or 2 or 3 or 4 or 5 or 6 or 7
 	 * </p>
 	 * 
 	 * <p>
 	 * Then returns 1 or 2 or 3 or 4 or 5 or 6 or 7
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test8() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(1, 2, 3, 4, 5, 6, 7);
 		for (int i = 1; i <= 7; i++) {
 			assertEquals(i, (int) t.find(i).get());
 		}
 	}
 
 	/**
 	 * <p>
 	 * Given a BTree<String> of degree 3 with 1,2,3,4,5,6,7 inserted
 	 * </p>
 	 * 
 	 * <p>
 	 * When I find 0.5 or 1.5 or 7.5
 	 * </p>
 	 * 
 	 * <p>
 	 * Then returns absent
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test9() {
 		BTree<Integer> t = builder(Integer.class).degree(3).build()
 				.add(10, 20, 30, 40, 50, 60, 70);
 		assertEquals(Optional.absent(), t.find(5));
 		assertEquals(Optional.absent(), t.find(15));
 		assertEquals(Optional.absent(), t.find(75));
 	}
 
 	/**
 	 * <p>
 	 * Given an empty BTree<String> of degree 3
 	 * </p>
 	 * 
 	 * <p>
 	 * When I find any value
 	 * </p>
 	 * 
 	 * <p>
 	 * Then returns absent
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test10() {
 		BTree<Double> t = builder(Double.class).degree(3).build();
 		assertEquals(Optional.absent(), t.find(1.0));
 	}
 
 	/**
 	 * <p>
 	 * Given a BTree<String> of degree 4 with 1,2,3 inserted
 	 * </p>
 	 * 
 	 * <p>
 	 * When I delete 2
 	 * </p>
 	 * 
 	 * <p>
 	 * Then find 2 returns absent
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test11() {
 		BTree<Double> t = builder(Double.class).degree(4).build()
 				.add(1.0, 2.0, 3.0);
 		t.delete(2.0);
 		assertEquals(Optional.absent(), t.find(2.0));
 	}
 
 	/**
 	 * <p>
 	 * Given a BTree<String> of degree 4 with 1,2,3 inserted
 	 * </p>
 	 * 
 	 * <p>
 	 * When I delete 1,2,3
 	 * </p>
 	 * 
 	 * <p>
 	 * Then find 2 returns absent
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void test12() {
 		BTree<Double> t = builder(Double.class).degree(4).build()
 				.add(1.0, 2.0, 3.0);
 		t.delete(1.0);
 		t.delete(2.0);
 		t.delete(3.0);
 		assertEquals(Optional.absent(), t.find(2.0));
 	}
 
 	/**
 	 * <p>
 	 * Given an empty BTree<Integer>
 	 * </p>
 	 * 
 	 * <p>
 	 * When iterate it
 	 * </p>
 	 * 
 	 * <p>
 	 * Then the iterator has no values
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void testIteratorOnEmptyBTree() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build();
 		assertFalse(t.iterator().hasNext());
 	}
 
 	/**
 	 * <p>
 	 * Given an BTree<Integer> with one value
 	 * </p>
 	 * 
 	 * <p>
 	 * When iterate it
 	 * </p>
 	 * 
 	 * <p>
 	 * Then the iterator returns that value only
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void testIteratorOnBTreeWithOneValue() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build().add(1);
 		checkEquals(t, 1);
 	}
 
 	/**
 	 * <p>
 	 * Given an BTree<Integer> with two values
 	 * </p>
 	 * 
 	 * <p>
 	 * When iterate it
 	 * </p>
 	 * 
 	 * <p>
 	 * Then the iterator returns those two values only
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void testIteratorOnBTreeWithTwoValue() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build().add(1, 2);
 		checkEquals(t, 1, 2);
 	}
 
 	/**
 	 * <p>
 	 * Given an BTree<Integer> with 5 values
 	 * </p>
 	 * 
 	 * <p>
 	 * When iterate it
 	 * </p>
 	 * 
 	 * <p>
 	 * Then the iterator returns those values only
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void testIteratorOnBTreeWith5Values() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build();
 		t.add(1, 2, 3, 4, 5);
 		checkEquals(t, 1, 2, 3, 4, 5);
 	}
 
 	@Test
 	public void testIteratorOnBTreeWith6Values() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build();
 		t.add(1, 2, 3, 4, 5, 6);
 		System.out.println(t);
 		checkEquals(t, 1, 2, 3, 4, 5, 6);
 	}
 
 	/**
 	 * <p>
 	 * Given an BTree<Integer> with 7 values
 	 * </p>
 	 * 
 	 * <p>
 	 * When iterate it
 	 * </p>
 	 * 
 	 * <p>
 	 * Then the iterator returns that value only
 	 * </p>
 	 * 
 	 */
 	@Test
 	public void testIteratorOnBTreeWith7Values() {
 		BTree<Integer> t = builder(Integer.class).degree(4).build();
 		t.add(1, 2, 3, 4, 5, 6, 7);
 		checkEquals(t, 1, 2, 3, 4, 5, 6, 7);
 	}
 
 	private static <T> void checkEquals(Iterable<T> iterable, T... values) {
 		int i = 0;
 		for (T t : iterable) {
 			assertTrue("not enough values", i < values.length);
 			assertEquals(t, values[i]);
 			i++;
 		}
 		assertTrue("iterable has " + i + " elements but values has "
 				+ values.length, values.length == i);
 	}
 
 	/**
 	 * Given a btree with numbers from 1..N added in order
 	 * 
 	 * Where N = 1 to 1000
 	 * 
 	 * Then the iterator returns 1,2,..,N
 	 */
 	@Test
 	public void testIteratorOnBTreeWithNValues() {
 		for (int n = 1; n <= 1000; n++) {
 			BTree<Integer> t = builder(Integer.class).degree(4).build();
 			for (int i = 1; i <= n; i++)
 				t.add(i);
 			Iterator<Integer> it = t.iterator();
 			for (int i = 1; i <= n; i++) {
 				assertTrue("element " + i + " does not exist (n=" + n + ")",
 						it.hasNext());
 				int next = it.next();
 				if (i != next)
 					System.out.println(t);
 				assertEquals("n=" + n, i, next);
 			}
 			assertFalse(it.hasNext());
 		}
 	}
 
 	/**
 	 * Given a btree with numbers from 1..N added in reverse order
 	 * 
 	 * Where N = 1 to 1000
 	 * 
 	 * Then the iterator returns 1,2,..,N
 	 */
 	@Test
 	public void testIteratorOnBTreeWithNValuesAddedInReverseOrder() {
 		for (int n = 1; n <= MANY_VALUES; n++) {
 			BTree<Integer> t = builder(Integer.class).degree(4).build();
 			for (int i = n; i >= 1; i--) {
 				t.add(i);
 			}
 			Iterator<Integer> it = t.iterator();
 			for (int i = 1; i <= n; i++) {
 				try {
 					assertTrue("element " + i + " does not exist", it.hasNext());
 					int next = it.next();
 					assertEquals("n=" + n, i, next);
 				} catch (RuntimeException e) {
 					System.out.println(t);
 					System.out.println("n=" + n + ",i=" + i);
 					throw e;
 				}
 			}
 			assertFalse(it.hasNext());
 		}
 	}
 
 	@Test
 	public void testSaveOneItem() {
 		File f = new File("target/test1.index");
 		clear(f);
 		BTree<Integer> t = builder(Integer.class).degree(3).metadata(f).build();
 		t.add(1);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		assertTrue(t2.find(1).isPresent());
 	}
 
 	@Test
 	public void testSaveTwoItems() {
 		File f = new File("target/test2.index");
 		clear(f);
 		BTree<Integer> t = builder(Integer.class).degree(3).metadata(f).build();
 		t.add(1);
 		t.add(2);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		assertTrue(t2.find(1).isPresent());
 		assertTrue(t2.find(2).isPresent());
 	}
 
 	@Test
 	public void testSaveThreeItemsDepthTwo() {
 		File f = new File("target/test3.index");
 		clear(f);
 		builder(Integer.class).degree(3).metadata(f).build().add(1, 2, 3);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		checkEquals(t2, 1, 2, 3);
 	}
 
 	@Test
 	public void testSaveFourItemsDepthTwo() {
 		File f = new File("target/test4.index");
 		clear(f);
 		BTree<Integer> t = builder(Integer.class).degree(3).metadata(f).build()
 				.add(1, 2, 3, 4);
 		System.out.println(t);
 		t.displayFile();
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		System.out.println(t2);
 		// System.out.println(Lists.newArrayList(t2));
 		checkEquals(t2, 1, 2, 3, 4);
 	}
 
 	@Test
 	public void testSaveSevenItemsDepthThree() {
 		File f = new File("target/test5.index");
 		clear(f);
 		builder(Integer.class).degree(3).metadata(f).build()
 				.add(1, 2, 3, 4, 5, 6, 7);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		checkEquals(t2, 1, 2, 3, 4, 5, 6, 7);
 	}
 
 	@Test
 	public void testSaveManyItems() {
 		File f = new File("target/test6.index");
 		clear(f);
 		Integer[] values = new Integer[MANY_VALUES];
 		for (int i = 0; i < values.length; i++)
 			values[i] = i + 1;
 
 		builder(Integer.class).degree(100).metadata(f).build().add(values);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		checkEquals(t2, values);
 	}
 
 	@Test
 	public void testSaveManyItemsWithSmallNodeCache() {
 		File f = new File("target/test7.index");
 		clear(f);
 		Integer[] values = new Integer[MANY_VALUES];
 		for (int i = 0; i < values.length; i++)
 			values[i] = i + 1;
 
 		builder(Integer.class).degree(100).metadata(f).cacheSize(10).build()
 				.add(values);
 		BTree<Integer> t2 = builder(Integer.class).degree(3).metadata(f)
 				.build();
 		checkEquals(t2, values);
 	}
 
 	private void clear(File f) {
 		f.delete();
 		new File(f.getAbsolutePath() + ".storage").delete();
 	}
 
 	@Test
 	public void testConcurrencyDoesNotProvokeException()
 			throws InterruptedException {
 		File f = new File("target/testConcurrency1.index");
 		clear(f);
 		final AtomicBoolean continue1 = new AtomicBoolean(true);
 		final AtomicBoolean continue2 = new AtomicBoolean(true);
 		final BTree<Integer> tree = builder(Integer.class).degree(3)
 				.metadata(f).build();
 		final AtomicInteger count = new AtomicInteger();
 		final int MAX_COUNT = 1000;
 		Thread t1 = new Thread(new Runnable() {
 			@Override
 			public void run() {
 
 				while (continue1.get() && count.incrementAndGet() < MAX_COUNT) {
 					tree.add(count.get());
 					System.out.print(" add ");
 					Thread.yield();
 				}
 			}
 		});
 		Thread t2 = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (continue2.get() && count.get() < MAX_COUNT) {
 					Optional<Integer> previous = Optional.absent();
 					for (Integer key : tree) {
 						System.out.print(key + ",");
 						if (previous.isPresent() && key - previous.get() != 1)
 							throw new RuntimeException("out of order!");
 						previous = Optional.of(key);
 					}
 					System.out.println("iterated");
 					Thread.yield();
 				}
 			}
 		});
 		t1.start();
 		t2.start();
 		Thread.sleep(3000);
 		continue1.set(false);
 		continue2.set(false);
 
 		final BTree<Integer> tree2 = builder(Integer.class).degree(3)
 				.metadata(f).build();
 		for (Integer key : tree2)
 			System.out.print(key + ",");
 		System.out.println();
 
 	}
 
 	private static void assertKeyValuesAre(List<? extends Key<Integer>> keys,
 			Integer... expected) {
 		String msg = "expected " + expected + " but was " + keys;
 		assertEquals(msg, expected.length, keys.size());
 		for (int i = 0; i < expected.length; i++)
 			assertEquals(msg, expected[i], keys.get(i).value());
 	}
 
 }
