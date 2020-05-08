 package org.kalibro.tests;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import org.kalibro.core.abstractentity.Equality;
 import org.kalibro.core.abstractentity.Printer;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.concurrent.TaskMatcher;
 
 public abstract class SpecialAssertions extends MockitoProxy {
 
 	public static void assertClassEquals(Class<?> expectedClass, Object object) {
 		assertEquals(expectedClass, object.getClass());
 	}
 
 	public static void assertClassEquals(String message, Class<?> expectedClass, Object object) {
 		assertEquals(message, expectedClass, object.getClass());
 	}
 
 	public static void assertDifferent(Object... elements) {
 		for (int i = 0; i < elements.length - 1; i++)
 			for (int j = i + 1; j < elements.length; j++)
 				assertFalse("Elements at " + i + " and at " + j + " are equal.", elements[i].equals(elements[j]));
 	}
 
 	public static void assertDoubleEquals(Double expected, Double actual) {
 		assertEquals(expected, actual, 1E-10);
 	}
 
 	public static void assertDoubleEquals(String message, Double expected, Double actual) {
 		assertEquals(message, expected, actual, 1E-10);
 	}
 
 	public static <E extends Comparable<? super E>> void assertSorted(E... elements) {
 		int i = 0;
 		while (i < elements.length - 1)
 			assertTrue("Element " + i + " is greater than its successor.", elements[i].compareTo(elements[++i]) <= 0);
 	}
 
 	public static TaskMatcher assertThat(Task<?> task) {
 		return new TaskMatcher(task);
 	}
 
 	public static <T> void assertDeepEquals(T expected, T actual) {
 		assertDeepEquals("", expected, actual);
 	}
 
 	public static <T> void assertDeepEquals(String message, T expected, T actual) {
 		if (!Equality.areDeepEqual(expected, actual)) {
			assertEquals(Printer.print(expected), Printer.print(actual));
 			fail("Print is the same but they are not deep equal: " + message);
 		}
 	}
 
 	public static <T> T[] array(T... elements) {
 		return elements;
 	}
 
 	public static <T> List<T> list(T... elements) {
 		return new ArrayList<T>(Arrays.asList(elements));
 	}
 
 	public static <T> Set<T> set(T... elements) {
 		return new HashSet<T>(Arrays.asList(elements));
 	}
 
 	public static <T> SortedSet<T> sortedSet(T... elements) {
 		return new TreeSet<T>(Arrays.asList(elements));
 	}
 
 	public static Map<Object, Object> map(Object... elements) {
 		Map<Object, Object> map = new HashMap<Object, Object>();
 		for (int i = 0; i < elements.length; i += 2)
 			map.put(elements[i], elements[i + 1]);
 		return map;
 	}
 }
