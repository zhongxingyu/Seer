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
 package org.oobium.utils.json;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.*;
 import static org.oobium.utils.StringUtils.asString;
 import static org.oobium.utils.json.JsonUtils.*;
 import static org.oobium.utils.literal.e;
 import static org.oobium.utils.literal.Map;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.oobium.utils.Base64;
 import org.oobium.utils.json.JsonParser;
 
 public class JsonUtilsTests {
 
 	private static class EmptyTestClass {
 	}
 	
 	@SuppressWarnings("unused")
 	private static class TestClass {
 		public static String StaticField = "static field";
 		public static String getStaticMethod() { return "static method"; }
 		public String publicField = "public field";
 		protected String protectedField = "protected field";
 		private String privateField = "private field";
 		public String getPublicMethod() { return "public method"; }
 		protected String getProtectedMethod() { return "protected method"; }
 		private String getPrivateMethod() { return "private method"; }
 		public String field = "you should not see me!";
 		public String getField() { return "field"; }
 	}
 	
 	private String json;
 	private List<Map<String, Object>> list;
 
 	private void setUp() throws Exception {
 		json = "[{\"a\":\"hello\",\"b\":1,\"c\":1.2,\"d\":true,\"e\":{\"e1\":1}},{\"a\":\"bye\",\"b\":2,\"c\":2.1,\"d\":false,\"e\":[{\"e2\":2},{\"e3\":3}]}]";
 
 		list = new ArrayList<Map<String,Object>>();
 		Map<String, Object> map;
 
 		map = new TreeMap<String, Object>();
 		map.put("a", "hello");
 		map.put("b", 1);
 		map.put("c", 1.200);
 		map.put("d", true);
 		
 		Map<String, Object> m2 = new TreeMap<String, Object>();
 		m2.put("e1", 1);
 		map.put("e", m2);
 		
 		list.add(map);
 
 		map = new TreeMap<String, Object>();
 		map.put("a", "bye");
 		map.put("b", 2);
 		map.put("c", 2.100);
 		map.put("d", false);
 		
 		List<Map<String, Object>> l2 = new ArrayList<Map<String,Object>>();
 		
 		m2 = new TreeMap<String, Object>();
 		m2.put("e2", 2);
 		l2.add(m2);
 		
 		m2 = new TreeMap<String, Object>();
 		m2.put("e3", 3);
 
 		map.put("e", l2);
 		l2.add(m2);
 		
 		list.add(map);
 	}
 
 	@Test
 	public void testToObject() throws Exception {
 		assertEquals(1, toObject("1"));
 		assertEquals(1.0, toObject("1.0"));
 		assertEquals(-1, toObject("-1"));
 		assertEquals('1', toObject("'1'"));
 		assertEquals("\n", toObject("\"\n\""));
 		assertEquals('\n', toObject("'\n'"));
 		assertEquals('\n', toObject("'\\u000A'"));
 		assertEquals("1", toObject("\"1\""));
 		assertEquals("hello", toObject("'hello'"));
 		assertEquals("hello's", toObject("'hello\'s'"));
 		assertEquals("hello, goodbye", toObject("'hello, goodbye'"));
 		assertEquals("hello", toObject("\"hello\""));
 		assertEquals("hello, goodbye", toObject("\"hello, goodbye\""));
 		assertEquals("hello", toObject("hello"));
 
 		Date date = new Date();
 		assertEquals(date, toObject("'/Date(" + date.getTime() + ")/'"));
 		
 		assertEquals(null, toObject("null"));
 		assertEquals(true, toObject("true"));
 		assertEquals(false, toObject("false"));
 		
 		assertEquals(HashMap.class, toObject("{}").getClass());
 		assertEquals(LinkedHashMap.class, toObject("{}", true).getClass());
 		
 		assertEquals(ArrayList.class, toObject("[]").getClass());
 
 		assertNotNull(toObject("\"/Date(" + System.currentTimeMillis() + ")/\""));
 		assertNotNull(toObject("\"/Base64(" + new String(Base64.encode("test".getBytes())) + ")/\""));
 	}
 	
 	@Test
 	public void testToMap() throws Exception {
 		assertEquals("{}", toMap(null).toString());
 		assertEquals("{}", toMap("").toString());
 		assertEquals("{}", toMap("{}").toString());
 		assertEquals("{a=b}", toMap("{a:b}").toString());
 		assertEquals("{a=b}", toMap("a:b").toString());
 		assertEquals("{a=b, c=d, e=f}", toMap("{a:b, c:d, e:f}", true).toString());
 		assertEquals("{a=b, c=d, e=f}", toMap("a:b, c:d, e:f", true).toString());
 		assertEquals("{a=b}", toMap("({\n\ta:b\n});").toString());
 
 		assertEquals("{a=b}", toMap("{a:'b'}").toString());
 		assertEquals("{a=b}", toMap("a:'b'").toString());
 
 		assertEquals("{a=b,c}", toMap("{a:'b,c'}").toString());
 		assertEquals("{a=b,c}", toMap("a:'b,c'").toString());
 
 		assertEquals("{a=b'c}", toMap("{a:'b\\'c'}").toString());
 		assertEquals("{a=b'c}", toMap("a:'b\\'c'").toString());
 		assertEquals("{a=b\"c}", toMap("{a:\"b\\\"c\"}").toString());
 		assertEquals("{a=b\"c}", toMap("a:\"b\\\"c\"").toString());
 
 		assertEquals("{a:b=b, c:d=d, e:=f}", toMap("\"a:b\":b, \"c:d\":d, \"e:\":f", true).toString());
 		assertEquals("{a:b=b, c:d=d, e:=f}", toMap("'a:b':b, 'c:d':d, 'e:':f", true).toString());
 		
 		assertEquals(1, toMap("a:1").get("a"));
 		assertEquals(-1, toMap("a:-1").get("a"));
 	}
 	
 	@Test
 	public void testToList() throws Exception {
 		assertEquals("[]", asString(toList(null)));
 		assertEquals("[]", asString(toList("")));
 		assertEquals("[]", asString(toList("[]")));
 		assertEquals("[1]", asString(toList("[1]")));
 		assertEquals("[1]", asString(toList("1")));
 		assertEquals("[1, 2, 3]", asString(toList("[1,2,3]")));
 		assertEquals("[1, 2, 3]", asString(toList("1,2,3")));
 		assertEquals("[1, 2]", asString(toList("[1,2,\"3]")));
 		assertEquals("[1, 2]", asString(toList("([\n\t1,2\n]);")));
 		assertEquals("[1, 2,3]", asString(toList("[1,'2,3']")));
 		assertEquals("[1, 2,3]", asString(toList("1,'2,3'")));
 		assertEquals("[1, 2'3]", asString(toList("[1,'2\\'3']")));
 		assertEquals("[1, 2'3]", asString(toList("1,'2\\'3'")));
 		
 		assertEquals("[/scripts/jquery-1.4.4.js]", asString(toList("([\n\"/scripts/jquery-1.4.4.js\"\n]);")));
 	}
 	
 	@Test
 	public void testToStringList() throws Exception {
 		assertEquals("[]", asString(toStringList(null)));
 		assertEquals("[]", asString(toStringList("")));
 		assertEquals("[]", asString(toStringList("[]")));
 		assertEquals("[1]", asString(toStringList("[1]")));
 		assertEquals("[1]", asString(toStringList("1")));
 		assertEquals("[1]", asString(toStringList("\"1\"")));
 		assertEquals("[1]", asString(toStringList("'1'")));
 		assertEquals("[1, 2, 3]", asString(toStringList("[1,2,3]")));
 		assertEquals("[1, 2, 3]", asString(toStringList("[\"1\",\"2\",\"3\"]")));
 		assertEquals("[1, 2, 3]", asString(toStringList("['1','2','3']")));
 		
 		assertEquals("[/scripts/jquery-1.4.4.js]", asString(toStringList("([\n\"/scripts/jquery-1.4.4.js\"\n]);")));
 	}
 	
 	@Test
 	public void testToStringMap() throws Exception {
 		assertEquals("{}", toStringMap("{}").toString());
 		assertEquals("{a=b}", toStringMap("{a:b}").toString());
 		assertEquals("{a=b}", toStringMap("a:b").toString());
 		assertEquals("{a=b}", toStringMap(" { a : b } ").toString());
 		assertEquals("{a=b, c=d, e=f}", toStringMap("{a:b, c:d, e:f}", true).toString());
 		assertEquals("{a=b, c=d, e=f}", toStringMap("{\"a\":\"b\", \"c\":\"d\", \"e\":\"f\"}", true).toString());
 		assertEquals("{a=b, c}", toStringMap("{a:\"b, c\"}", true).toString());
		
		assertEquals("{a={b:c,d:e}}", toStringMap("{a:{b:c,d:e}}").toString());
		assertEquals("{a=[{b:c},{d:e}]}", toStringMap("{a:[{b:c},{d:e}]}").toString());
 	}
 	
 	@Test
 	public void testFromJson() throws Exception {
 		setUp();
 		JsonParser parser = new JsonParser();
 		parser.setKeepOrder(true);
 		assertEquals(asString(list), asString(parser.toList(json))) ;
 	}
 	
 	@Test
 	public void testToJson() throws Exception {
 		assertEquals("null", toJson((Object) null)) ;
 		assertEquals("true", toJson(true));
 		assertEquals("false", toJson(false));
 		assertEquals("1", toJson(1));
 		assertEquals("1.0", toJson(1.0));
 		assertEquals("'1'", toJson('1'));
 		assertEquals("\"1\"", toJson("1"));
 		assertEquals("'1'", toJson("'1'"));
 		assertEquals("\"1\"", toJson("\"1\""));
 		assertEquals("\"test\n\"", toJson("test\n"));
 		assertEquals("{\"a\":\"b\"}", toJson(Collections.singletonMap("a", "b")));
 		assertEquals("{\"a\":\"b\"}", toJson(Collections.singletonMap("a", "\"b\"")));
 		assertEquals("[\"a\"]", toJson(Collections.singletonList("a")));
 		assertEquals("['a']", toJson(Collections.singletonList('a')));
 		assertEquals("\"/Base64(" + new String(Base64.encode("test".getBytes())) + ")/\"", toJson("test".getBytes()));
 
 		Date date = new Date();
 		assertEquals("\"/Date(" + date.getTime() + ")/\"", toJson(date));
 
 		assertEquals("[\"a\",\"b\"]", toJson(new Object[] { "a", "b" }));
 		assertEquals("['a','b']", toJson(new char[] { 'a', 'b' }));
 
 		setUp();
 		assertEquals(json, toJson(list)) ;
 		
 		assertEquals("{\"a\":\"my string\"}", toJson(Collections.singletonMap("a", "\"my string\""), "a"));
 	}
 	
 	@Ignore
 	@Test
 	public void testToJson_FromClass() {
 		assertEquals("{class:" + EmptyTestClass.class.getName() + "}", toJson(new EmptyTestClass()));
 		assertEquals("{class:org.oobium.utils.JsonUtilsTests$TestClass,field:\"field\",publicField:\"public field\",publicMethod:\"public method\"}", toJson(new TestClass()));
 	}
 
 	@Test
 	public void testConverter() {
 		File file = new File("test", "file");
 		String json = JsonUtils.toJson(file, new IConverter() {
 			@Override
 			public Object convert(Object object) {
 				return "file: " + ((File) object).getAbsolutePath();
 			}
 		});
 		assertEquals("\"file: " + file.getAbsolutePath() + "\"", json);
 	}
 	
 	static class SerTest { String name; int size; }
 	@Test
 	public void testSerialization() throws Exception {
 		SerTest obj = new SerTest();
 		obj.name = "bob";
 		obj.size = 1;
 		
 		String type = "\"" + SERIALIZATION_TYPE_KEY + "\":\"" + obj.getClass().getName() + "\"";
 		
 		assertEquals("{" + type + ",\"name\":\"bob\",\"size\":1}", serialize(obj));
 		
 		assertNotNull(deserialize("{" + type + "}"));
 		assertNotNull(deserialize("{" + type + ",name:\"bob\",size:1}"));
 		assertEquals(SerTest.class, deserialize("{" + type + ",name:\"bob\",size:1}").getClass());
 		assertEquals(obj.name, ((SerTest) deserialize("{" + type + ",name:\"bob\",size:1}")).name);
 		assertEquals(obj.size, ((SerTest) deserialize("{" + type + ",name:\"bob\",size:1}")).size);
 	}
 
 	@Test
 	public void testSerialization_Arrays() throws Exception {
 		assertEquals("['a','b','c']", serialize(new char[] { 'a', 'b', 'c' }));
 		assertEquals("[\"a\",\"b\",\"c\"]", serialize(new String[] { "a", "b", "c" }));
 		
 		assertArrayEquals(new Object[] { 'a', 'b', 'c' }, ((List<?>) deserialize("['a','b','c']")).toArray());
 	}
 
 	@Test
 	public void testSerialization_Lists() throws Exception {
 		assertEquals("['a','b','c']", serialize(asList(new Character[] { 'a', 'b', 'c' })));
 		assertEquals("[\"a\",\"b\",\"c\"]", serialize(asList(new String[] { "a", "b", "c" })));
 		
 		assertEquals(asList(new Character[] { 'a', 'b', 'c' }), deserialize("['a','b','c']"));
 	}
 
 	@Test
 	public void testSerialization_Maps() throws Exception {
 		assertEquals("{\"c\":'d',\"a\":'b'}", serialize(Map(e('a', 'b'), e('c', 'd'))));
 		assertEquals("{\"c\":\"d\",\"a\":\"b\"}", serialize(Map(e("a", "b"), e("c", "d"))));
 		
 		Object o = deserialize("{c:'d',a:'b'}");
 		assertTrue(o instanceof Map);
 		
 		Map<?,?> map = (Map<?,?>) o;
 		assertEquals(2, map.size());
 		assertEquals('d', map.get("c"));
 		assertEquals('b', map.get("a"));
 	}
 
 	@Test
 	public void testKeys() throws Exception {
 		assertEquals("mail.send", toMap("{mail.send: { prop1: val1 }}").keySet().iterator().next());
 	}
 	
 }
