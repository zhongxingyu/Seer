 package org.pcollections.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.pcollections.ArrayPVector;
 import org.pcollections.PVector;
 
 public class ArrayPVectorTest {
 	private PVector<String> u;
 
 	@Before
 	public void init() {
 		u = ArrayPVector.empty();
 		u = u.plus("a").plus("b").plus("c").plus("d");
 	}
 
 	@Test
 	public void testEmpty() {
 		PVector<String> v = ArrayPVector.empty();
 		assertEquals(0, v.size());
 		assertTrue(v.isEmpty());
 	}
 
 	@Test
 	public void testSize() {
 		PVector<String> v = ArrayPVector.empty();
 		assertEquals(0, v.size());
 		v = v.plus("a");
 		assertEquals(1, v.size());
 		v = v.plus("b");
 		assertEquals(2, v.size());
 		v = v.plus("c");
 		assertEquals(3, v.size());
 		v = v.plus("d");
 		assertEquals(4, v.size());
 	}
 
 	@Test
 	public void testIsEmpty() {
 		PVector<String> v = ArrayPVector.empty();
 		assertTrue(v.isEmpty());
 		v = v.plus("a");
 		assertFalse(v.isEmpty());
 	}
 
 	@Test
 	public void testContains() {
 		assertFalse(u.contains("x"));
 		assertTrue(u.contains("a"));
 		assertTrue(u.contains("b"));
 		assertTrue(u.contains("c"));
 		assertTrue(u.contains("d"));
 	}
 
 	@Test
 	public void testGet() {
 		assertEquals("a", u.get(0));
 		assertEquals("b", u.get(1));
 		assertEquals("c", u.get(2));
 		assertEquals("d", u.get(3));
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testGet0() {
 		PVector<String> v = ArrayPVector.empty();
 		v.get(0);
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testGet1() {
 		u.get(-1);
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testGet2() {
 		u.get(4);
 	}
 
 	@Test
 	public void testIndexOf() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("x");
 		assertEquals(-1, v.indexOf("y"));
 		assertEquals(0, v.indexOf("a"));
 		assertEquals(1, v.indexOf("b"));
 		assertEquals(2, v.indexOf("c"));
 		assertEquals(3, v.indexOf("d"));
 		assertEquals(8, v.indexOf("x"));
 	}
 
 	@Test
 	public void testLastIndexOf() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("x");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		assertEquals(-1, v.lastIndexOf("y"));
 		assertEquals(5, v.lastIndexOf("a"));
 		assertEquals(6, v.lastIndexOf("b"));
 		assertEquals(7, v.lastIndexOf("c"));
 		assertEquals(8, v.lastIndexOf("d"));
 		assertEquals(0, v.lastIndexOf("x"));
 	}
 
 	@Test
 	public void testMinusObject() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("x");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		assertEquals(9, v.size());
 		PVector<String> o = v;
 		v = v.minus("y");
 		assertEquals(o, v);
 		v = v.minus("x");
 		assertEquals(8, v.size());
 		assertFalse(v.contains("x"));
 		assertEquals(0, v.indexOf("a"));
 		v = v.minus("d");
 		assertEquals(7, v.size());
 		assertEquals(6, v.indexOf("d"));
 		v = v.minus("d");
 		assertEquals(6, v.size());
 		assertFalse(v.contains("d"));
 	}
 
 	@Test
 	public void testMinusInt() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("x");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		assertEquals(9, v.size());
 		v = v.minus(4);
 		assertEquals(8, v.size());
 		assertEquals(7, v.indexOf("d"));
 		v = v.minus(0);
 		assertEquals(7, v.size());
 		assertEquals(0, v.indexOf("a"));
 		assertFalse(v.contains("x"));
 		v = v.minus(6);
 		assertEquals(6, v.size());
 		assertFalse(v.contains("d"));
 	}
 
 	@Test
 	public void testPlusE() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("a");
 		assertEquals(1, v.size());
 		assertEquals("a", v.get(0));
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		assertEquals(5, v.size());
 		assertEquals("a", v.get(0));
 		assertEquals("b", v.get(1));
 		assertEquals("c", v.get(2));
 		assertEquals("d", v.get(3));
 		assertEquals("a", v.get(4));
 	}
 
 	@Test
 	public void testPlusAllCollectionOfQextendsE() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plusAll(u);
 		assertEquals(4, v.size());
 		assertEquals("a", v.get(0));
 		assertEquals("b", v.get(1));
 		assertEquals("c", v.get(2));
 		assertEquals("d", v.get(3));
 		v = v.plusAll(u);
 		assertEquals(8, v.size());
 		assertEquals("a", v.get(0));
 		assertEquals("b", v.get(1));
 		assertEquals("c", v.get(2));
 		assertEquals("d", v.get(3));
 		assertEquals("a", v.get(4));
 		assertEquals("b", v.get(5));
 		assertEquals("c", v.get(6));
 		assertEquals("d", v.get(7));
 
 		List<String> l = Arrays.asList();
 		v = ArrayPVector.empty();
 		v = v.plusAll(l);
 		assertEquals(ArrayPVector.empty(), v);
 	}
 
 	@Test
 	public void testSubList() {
 		PVector<String> v = ArrayPVector.empty();
 		v = v.plus("x");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 		v = v.plus("a");
 		v = v.plus("b");
 		v = v.plus("c");
 		v = v.plus("d");
 
 		PVector<String> s = v.subList(1, 5);
 		assertEquals(u, s);
 
 		s = s.plus("y");
 		assertEquals(9, v.size());
 		assertEquals(5, s.size());
 
 		s = v.subList(0, 9);
 		assertEquals(v, s);
 
 		s = v.subList(4, 4);
 		assertEquals(ArrayPVector.empty(), s);
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testSubList0() {
 		u = u.subList(-1, 3);
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testSubList1() {
 		u = u.subList(0, 5);
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testSubList2() {
 		u = u.subList(3, 2);
 	}
 
 	@Test
 	public void testWith() {
 
 		PVector<String> w = u.with(1, "x");
 
 		assertEquals(4, u.size());
 		assertEquals("a", u.get(0));
 		assertEquals("b", u.get(1));
 		assertEquals("c", u.get(2));
 		assertEquals("d", u.get(3));
 
 		assertEquals(4, w.size());
 		assertEquals("a", w.get(0));
 		assertEquals("x", w.get(1));
 		assertEquals("c", w.get(2));
 		assertEquals("d", w.get(3));
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testWith0() {
 		u = u.with(-1, "x");
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testWith1() {
 		u = u.with(4, "x");
 	}
 
 	@Test
 	public void hashCodeTest() {
 		assertEquals(0, ArrayPVector.empty().hashCode());
 	}
 }
