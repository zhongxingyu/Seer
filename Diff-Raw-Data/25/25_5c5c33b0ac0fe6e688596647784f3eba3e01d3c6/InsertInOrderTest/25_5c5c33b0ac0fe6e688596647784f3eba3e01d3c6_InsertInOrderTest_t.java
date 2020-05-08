 package org.bodytrack.client;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Test;
 
public final class InsertInOrderTest {
 	private static final int SEED = 424242;
 
 	@Test(expected=NullPointerException.class)
 	public void testNullList() {
 		CollectionUtils.insertInOrder(null, 0);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testNullElement() {
 		CollectionUtils.insertInOrder(new ArrayList<Integer>(), null);
 	}
 
 	@Test
 	public void testEmptyList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 
 		assertEquals(0, CollectionUtils.insertInOrder(lst, 0));
 
 		assertEquals(1, lst.size());
 		assertEquals(0, lst.get(0).intValue());
 	}
 
 	@Test
 	public void testShortList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 
 		lst.add(5);
 		lst.add(42);
 
 		assertEquals(1, CollectionUtils.insertInOrder(lst, 7));
 
 		assertEquals(3, lst.size());
 
 		assertEquals(5, lst.get(0).intValue());
 		assertEquals(7, lst.get(1).intValue());
 		assertEquals(42, lst.get(2).intValue());
 	}
 
 	@Test
 	public void testLongList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 		final List<Integer> copy = new ArrayList<Integer>();
 		final Random rg = new Random(SEED);
 
 		for (int i = 0; i < 10000; i++) {
 			final int n = rg.nextInt();
 			lst.add(n);
 			CollectionUtils.insertInOrder(copy, n);
 		}
 
 		Collections.sort(lst);
 
 		assertEquals(copy.size(), lst.size());
 
 		for (int i = 0; i < lst.size(); i++)
 			assertEquals(copy.get(i), lst.get(i));
 	}
 }
