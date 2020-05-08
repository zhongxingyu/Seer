 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils.coercion;
 
 import static java.util.Arrays.asList;
 import static java.util.Collections.singletonMap;
 import static org.junit.Assert.*;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.SERIALIZATION_TYPE_KEY;
import static org.oobium.utils.StringUtils.*;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.junit.Test;
 import org.oobium.utils.Base64;
 import org.oobium.utils.json.JsonModel;
 import org.oobium.utils.json.JsonUtils;
 
 public class TypeCoercerTests {
 	
 	public static class Model1 implements JsonModel {
 		private Object id;
 		private Map<String, Object> data = new HashMap<String, Object>();
 		public Model1() { }
 		public Model1(String field, Object value) { put(field, value); }
 		public Object get(String field) { return data.get(field); }
 		public Map<String, Object> getAll() { return data; }
 		public Object getId() { return id; }
 		public boolean isBlank() { return data.isEmpty(); }
 		public boolean isEmpty() { return data.isEmpty(); }
 		public boolean isNew() { return id == null; }
 		public boolean isSet(String field) { return data.containsKey(field); }
 		public JsonModel put(String field, Object value) { data.put(field, value); return this; }
 		public JsonModel putAll(Map<String, Object> data) { this.data.putAll(data); return this; }
 		public JsonModel putAll(String json) { data.putAll(JsonUtils.toMap(json)); return this; }
 		public JsonModel set(String field, Object value) { return put(field, value); }
 		public JsonModel putAll(JsonModel model) { return putAll(model.getAll()); }
 		public JsonModel setAll(Map<String, Object> data) { return putAll(data); }
 		public JsonModel setAll(String json) { return putAll(json); }
 		public JsonModel setId(Object id) { this.id = id; return this; }
 		public String toJson() { return data.toString(); }
 	}
 	
 	@Test
 	public void testBigDecimal() throws Exception {
 		assertEquals(new BigDecimal(100.00d), coerce(100.00d, BigDecimal.class));
 		assertEquals(new BigDecimal(100.00f), coerce(100.00f, BigDecimal.class));
 		assertEquals(new BigDecimal(100l), coerce(100l, BigDecimal.class));
 		assertEquals(new BigDecimal(100), coerce(100, BigDecimal.class));
 		assertEquals(new BigDecimal("100.00"), coerce("100.00", BigDecimal.class));
 		assertFalse(new BigDecimal("100.000").equals(coerce("100.00", BigDecimal.class)));
 	}
 	
 	@Test
 	public void testBoolean() throws Exception {
 		assertFalse(coerce(0, Boolean.class));
 		assertFalse(coerce(0, boolean.class));
 		assertTrue(coerce(1, Boolean.class));
 		assertTrue(coerce(1, boolean.class));
 
 		assertFalse(coerce(0.0f, Boolean.class));
 		assertFalse(coerce(0.0f, boolean.class));
 		assertTrue(coerce(0.01f, Boolean.class));
 		assertTrue(coerce(0.1f, boolean.class));
 		assertTrue(coerce(1.0f, Boolean.class));
 		assertTrue(coerce(1.0f, boolean.class));
 
 		assertFalse(coerce(0l, Boolean.class));
 		assertFalse(coerce(0l, boolean.class));
 		assertTrue(coerce(1l, Boolean.class));
 		assertTrue(coerce(1l, boolean.class));
 		
 		assertFalse(coerce("t", Boolean.class));
 		assertFalse(coerce("t", boolean.class));
 		assertTrue(coerce("true", Boolean.class));
 		assertTrue(coerce("true", boolean.class));
 	}
 	
 	@Test
 	public void testDate() throws Exception {
 		long date = System.currentTimeMillis();
 		assertEquals(new Date(date), coerce("/Date("+date+")/", Date.class));
 	}
 	
 	@Test
 	public void testFile() throws Exception {
 		assertEquals(new File("someplace"), coerce("someplace", File.class));
 	}
 	
 	@Test
 	public void testStringToMap() throws Exception {
 		assertEquals(Collections.singletonMap("a", "b"), coerce("{a:\"b\"}", Map.class));
 	}
 	
 	@Test
 	public void testMapToString() throws Exception {
 		// presently coded to be a JSON formatted string
 		assertEquals("{\"a\":\"b\"}", coerce(Collections.singletonMap("a", "b"), String.class));
 	}
 	
 	@Test
 	public void testMapToJsonModel() throws Exception {
 		assertNull(coerce(singletonMap("a", "b"), JsonModel.class)); // can't instantiate an interface - return null
 		assertNotNull(coerce(singletonMap("a", "b"), Model1.class));
 		assertEquals("b", coerce(singletonMap("a", "b"), Model1.class).get("a"));
 	}
 	
 	@Test
 	public void testArrayToString() throws Exception {
 		assertEquals("[\"a\",\"b\",\"c\"]", coerce(new String[] { "a", "b", "c" }, String.class));
 	}
 
 	@Test
 	public void testArrayToArray() throws Exception {
 		assertArrayEquals(new String[] { "1", "2", "3" }, coerce(new int[] { 1, 2, 3 }, String[].class));
 		assertArrayEquals(new String[] { "1", "2", "3" }, coerce(new Integer[] { 1, 2, 3 }, String[].class));
 		assertArrayEquals(new String[] { "1", "2", "3" }, coerce(new Object[] { 1, 2, 3 }, String[].class));
 		assertArrayEquals(new String[] { "1" }, coerce(1, String[].class));
 		assertArrayEquals(new Integer[] { 1 }, coerce(1, Integer[].class));
 		assertArrayEquals(new int[] { 1 }, coerce(1, int[].class));
 	}
 	
 	@Test
 	public void testIterableToArray() throws Exception {
 		assertArrayEquals(new String[] { "1", "2", "3" }, coerce(asList("1", "2", "3"), String[].class));
 		assertArrayEquals(new int[] { 1, 2, 3 }, coerce(asList(1, 2, 3), int[].class));
 		assertArrayEquals(new int[] { 1, 2, 3 }, coerce(asList("1", "2", "3"), int[].class));
 	}
 
 	@Test
 	public void testCollectionToCollection() throws Exception {
 		assertArrayEquals(asList(1, 2, 3).toArray(), coerce("[1,2,3]", Set.class).toArray());
 		assertArrayEquals(asList(1, 2, 3).toArray(), coerce("[1,2,3]", List.class).toArray());
 		assertArrayEquals(asList(1, 2, 3).toArray(), coerce("[1,2,3]", Collection.class).toArray());
 		assertArrayEquals(asList(1, 2, 3).toArray(), coerce("[1,2,3]", LinkedList.class).toArray());
 		assertArrayEquals(asList("1", "2", "3").toArray(), coerce(asList("1", "2", "3"), Set.class).toArray());
 		
 		Set<Object> s = new HashSet<Object>(asList("1", "2", "3"));
 		assertEquals(TreeSet.class, coerce(s, TreeSet.class).getClass());
 		assertEquals(3, coerce(s, TreeSet.class).size());
 	}
 	
 	private enum TestType { ZERO, ONE, TWO, THREE }
 	@Test
 	public void testEnum() throws Exception {
 		assertEquals(new Integer(1), coerce(TestType.ONE, Integer.class));
 		assertEquals("ONE", coerce(TestType.ONE, String.class));
 		assertEquals(TestType.ONE, coerce("ONE", TestType.class));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testEnumException() throws Exception {
 		coerce("one", TestType.class);
 	}
 	
 	static class SerializeTest { String name; int size; }
 	@Test
 	public void testSerialization() throws Exception {
 		SerializeTest obj = new SerializeTest();
 		obj.name = "bob";
 		obj.size = 1;
 		
 		String type = SERIALIZATION_TYPE_KEY + "\":\"" + obj.getClass().getName();
 		
 		assertEquals("{\"" + type + "\",\"name\":\"bob\",\"size\":1}", coerce(obj, String.class));
 		
 		assertNotNull(coerce("{\"" + type + "\"}", SerializeTest.class));
 		assertNotNull(coerce("{\"" + type + "\",\"name\":\"bob\",\"size\":1}", SerializeTest.class));
 		assertEquals(obj.name, coerce("{\"" + type + "\",\"name\":\"bob\",\"size\":1}", SerializeTest.class).name);
 		assertEquals(obj.size, coerce("{\"" + type + "\",\"name\":\"bob\",\"size\":1}", SerializeTest.class).size);
 	}
 
 	@Test
 	public void testByteArrayToString() throws Exception {
 		assertEquals("/Base64(" + Base64.encode("hello") + ")/", coerce("hello".getBytes(), String.class));
 	}
 	
 	@Test
 	public void testByteArrayToStringAndBack() throws Exception {
 		byte[] bytes = new byte[] { 1, 2, 3 };
 		String string = coerce(bytes, String.class);
 		assertEquals("/Base64(" + new String(Base64.encode(bytes)) + ")/", string);
 		assertArrayEquals(bytes, coerce(string, byte[].class));
 	}
 	
 	@Test
	public void testStringToStringArray() throws Exception {
		assertEquals("[a:b]", asString(coerce("a:b", String[].class)));
		assertEquals("[{a=b}]", asString(coerce("a:b", Object[].class)));
	}
	
	@Test
 	public void testStringToByteArray() throws Exception {
 		assertArrayEquals("hello".getBytes(), coerce("hello", byte[].class));
 		assertArrayEquals(new byte[] { 1, 2, 3 }, coerce("/Base64(" + new String(Base64.encode(new byte[] { 1, 2, 3 })) + ")/", byte[].class));
 	}
 	
 	@Test
 	public void testStringToByteArrayAndBack() throws Exception {
 		// not really what it is intended for... storing bytes as a string is generally more useful
 		String string1 = "hello";
 		byte[] bytes = coerce(string1, byte[].class);
 		assertArrayEquals("hello".getBytes(), bytes);
 		
 		String string2 = "/Base64(" + Base64.encode("hello") + ")/";
 		assertEquals(string2, coerce(bytes, String.class));
 		assertEquals(string1, new String(coerce(string2, byte[].class)));
 	}
 	
 }
