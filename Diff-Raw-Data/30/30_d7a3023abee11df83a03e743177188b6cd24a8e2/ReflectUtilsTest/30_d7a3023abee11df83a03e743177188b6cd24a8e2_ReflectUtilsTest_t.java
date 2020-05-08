 package com.alexrnl.commons.utils.object;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.Serializable;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.utils.object.AutoCompareTest.ComparedClass;
 
 /**
  * Test suite for the {@link ReflectUtils} class.
  * @author Alex
  */
 public class ReflectUtilsTest {
	/** Logger */
	private static Logger		lg					= Logger.getLogger(ReflectUtilsTest.class.getName());
	
	/** The name of the field added by Jacoco at execution time. */
	private static final String	JACOCO_FIELD_NAME	= "$jacocoData";
	
 	/** The first compared class */
	private ComparedClass		one;
 	/** The second compared class. */
	private ComparedClass		two;
 	
 	/**
 	 * Annotation for test purposes.
 	 * @author Alex
 	 */
 	@Target({ ElementType.FIELD })
 	@Retention(RetentionPolicy.RUNTIME)
 	private @interface Dumb {
 	}
 	
 	/**
 	 * Class for field annotation tests.
 	 * @author Alex
 	 */
 	private static class DummyFields {
 		/** A test string */
 		@Dumb
 		private String test;
 		/** A dumb flag */
 		private Boolean isFalse;
 	}
 	
 	/**
 	 * Set up attributes.
 	 */
 	@Before
 	public void setUp () {
 		one = new ComparedClass("test", new Integer[] { 1, 2 }, false, new double[] { 2.8 });
 		two = new ComparedClass("track", new Integer[] { 4, 8 }, true, new double[] { -8.8 });
 	}
 	
 	/**
	 * Test method for {@link ReflectUtils#retrieveMethods(Class, Class)}.
 	 */
 	@Test
 	public void testRetrieveMethods () {
 		Logger.getLogger(ReflectUtils.class.getName()).setLevel(Level.INFO);
 		assertEquals(3, ReflectUtils.retrieveMethods(ComparedClass.class, Field.class).size());
 		Logger.getLogger(ReflectUtils.class.getName()).setLevel(Level.FINE);
 		assertEquals(4 + Object.class.getMethods().length, ReflectUtils.retrieveMethods(ComparedClass.class, null).size());
 	}
 	
 	/**
 	 * Test method for {@lin ReflectUtils#retrieveFields(Class, Class)}.
 	 */
 	@Test
 	public void testRetrieveFields () {
 		Logger.getLogger(ReflectUtils.class.getName()).setLevel(Level.INFO);
		final Set<java.lang.reflect.Field> dummyFields = ReflectUtils.retrieveFields(DummyFields.class, null);
		boolean jacoco = false;
		for (final java.lang.reflect.Field field : dummyFields) {
			if (field.getName().equals(JACOCO_FIELD_NAME)) {
				lg.info("Jacoco field has been detected, test will be adjusted.");
				jacoco = true;
				break;
			}
		}
		assertEquals(jacoco ? 3 : 2, dummyFields.size());
 		Logger.getLogger(ReflectUtils.class.getName()).setLevel(Level.FINE);
 		assertEquals(1, ReflectUtils.retrieveFields(DummyFields.class, Dumb.class).size());
 		
 	}
 	
 	/**
	 * Test method for {@link ReflectUtils#invokeMethods(Object, List)}.
 	 */
 	@Test
 	public void testInvokeMethods () {
 		final List<Method> comparedClassMethods = new ArrayList<>(ReflectUtils.retrieveMethods(ComparedClass.class, Field.class));
 		Collections.sort(comparedClassMethods, new Comparator<Method>() {
 			@Override
 			public int compare (final Method m1, final Method m2) {
 				return m1.getName().compareTo(m2.getName());
 			}
 		});
 		try {
 			final List<Object> ones = ReflectUtils.invokeMethods(one, comparedClassMethods);
 			final List<Object> twos = ReflectUtils.invokeMethods(two, comparedClassMethods);
 			assertArrayEquals(new double[] { 2.8 }, (double[]) ones.get(0), 0.01);
 			assertArrayEquals(new double[] { -8.8 }, (double[])  twos.get(0), 0.01);
 			assertArrayEquals(new Integer[] { 1, 2 }, (Integer[]) ones.get(1));
 			assertArrayEquals(new Integer[] { 4, 8 }, (Integer[]) twos.get(1));
 			assertEquals("test", ones.get(2));
 			assertEquals("track", twos.get(2));
 		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 			fail("Error while invoking a method: " + ExceptionUtils.display(e));
 		}
 	}
 	
 	/**
 	 * Test method for {@link ReflectUtils#getAllInterfaces(Class)}.
 	 */
 	@Test
 	public void testGetAllInterfaces () {
 		final Set<Class<?>> objectInterfaces = new HashSet<>(1);
 		objectInterfaces.add(Object.class);
 		assertEquals(objectInterfaces, ReflectUtils.getAllInterfaces(Object.class));
 		
 		final Set<Class<?>> comparedClassInterfaces = new HashSet<>(2);
 		comparedClassInterfaces.add(ComparedClass.class);
 		comparedClassInterfaces.add(Object.class);
 		assertEquals(comparedClassInterfaces, ReflectUtils.getAllInterfaces(ComparedClass.class));
 		
 		final Set<Class<?>> hashMapInterfaces = new HashSet<>(6);
 		hashMapInterfaces.add(Serializable.class);
 		hashMapInterfaces.add(Cloneable.class);
 		hashMapInterfaces.add(Map.class);
 		hashMapInterfaces.add(HashMap.class);
 		hashMapInterfaces.add(AbstractMap.class);
 		hashMapInterfaces.add(Object.class);
 		assertEquals(hashMapInterfaces, ReflectUtils.getAllInterfaces(HashMap.class));
 	}
 	
 	/**
 	 * Test method for {@link ReflectUtils#getAllInterfaces(Class)}.
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testGetAllInterfacesNullPointerException () {
 		ReflectUtils.getAllInterfaces(null);
 	}
 }
