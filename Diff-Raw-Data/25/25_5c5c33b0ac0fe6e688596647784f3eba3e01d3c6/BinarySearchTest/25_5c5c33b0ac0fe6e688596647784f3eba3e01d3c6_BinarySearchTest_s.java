 package org.bodytrack.client;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Test;
 
public class BinarySearchTest {
 	private static final int SEED = 42424242;
 
 	@Test(expected=NullPointerException.class)
 	public void testNullList() {
 		CollectionUtils.binarySearch(null, 0);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testNullElement() {
 		CollectionUtils.binarySearch(new ArrayList<Integer>(), null);
 	}
 
 	@Test
 	public void testEmptyList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 
 		assertEquals(Collections.binarySearch(lst, 0),
 				CollectionUtils.binarySearch(lst, 0));
 	}
 
 	@Test
 	public void testShortList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 
 		lst.add(5);
 		lst.add(42);
 
 		assertEquals(0, CollectionUtils.binarySearch(lst, 5));
 		assertEquals(1, CollectionUtils.binarySearch(lst, 42));
 
 		assertEquals(Collections.binarySearch(lst, 7),
 				CollectionUtils.binarySearch(lst, 7));
 		assertEquals(Collections.binarySearch(lst, 0),
 				CollectionUtils.binarySearch(lst, 0));
 
 		// Make sure that the list didn't change
 		assertEquals(2, lst.size());
 		assertEquals(5, lst.get(0).intValue());
 		assertEquals(42, lst.get(1).intValue());
 	}
 
 	@Test
 	public void testLongList() {
 		final List<Integer> lst = new ArrayList<Integer>();
 		final Random rg = new Random(SEED);
 
 		for (int i = 0; i < 10000; i++)
 			lst.add(rg.nextInt());
 
 		Collections.sort(lst);
 
 		for (final int n: lst) {
 			assertEquals(Collections.binarySearch(lst, n),
 					CollectionUtils.binarySearch(lst, n));
 		}
 	}
 }
