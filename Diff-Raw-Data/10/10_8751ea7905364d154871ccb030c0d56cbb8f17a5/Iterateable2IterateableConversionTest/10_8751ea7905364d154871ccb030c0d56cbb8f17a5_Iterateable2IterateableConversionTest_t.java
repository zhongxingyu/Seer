 /*
  * Copyright 2002-2005 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */	
 package test.net.sf.sojo.conversion;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import junit.framework.TestCase;
 import net.sf.sojo.core.Converter;
 import net.sf.sojo.core.UniqueIdGenerator;
 import net.sf.sojo.core.conversion.ComplexBean2MapConversion;
 import net.sf.sojo.core.conversion.Iterateable2IterateableConversion;
 import net.sf.sojo.core.conversion.IterateableMap2MapConversion;
 import net.sf.sojo.core.conversion.NullConversion;
 import test.net.sf.sojo.model.Node;
 
 public class Iterateable2IterateableConversionTest extends TestCase {
 
 	public void testList() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("Test-1");
 		lvList.add("Test-2");
 		Object lvResult = c.convert(lvList);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(lvList.size(), lvListAfter.size());		
 		assertEquals(lvList.get(0), lvListAfter.get(0));
 		assertEquals(lvList.get(1), lvListAfter.get(1));
 	}
 	
 	public void testMakeSimpleConverterWithListWithNotAddNullElement() throws Exception {
 		List<?> v = new ArrayList<Object>();
 		v.add(null);
 
 		Converter c = new Converter();
 		Iterateable2IterateableConversion lvConversion = new Iterateable2IterateableConversion(ArrayList.class);
 		lvConversion.setIgnoreNullValues(true);
 		c.addConversion(lvConversion);
 
 		Object lvResult = c.convert(v);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> v2 = (List<?>) lvResult;
 		assertEquals(v2.size(), 0);
 	}
 	
 	public void testMakeSimpleConverterWithListWithAddNullElement() throws Exception {
 		List<?> v = new ArrayList<Object>();
 		v.add(null);
 
 		Converter c = new Converter();
 		c.addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 
 		Object lvResult = c.convert(v);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> v2 = (List<?>) lvResult;
 		assertEquals(v2.size(), 1);
 	}
 
 	public void testMakeSimpleConverterWithListWithNullElement() throws Exception {
 		List<?> v = new ArrayList<Object>();
 		v.add(null);
 
 		Converter c = new Converter();
 		c.addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		c.addConversion(new NullConversion("~my~null~value~"));
 
 		Object lvResult = c.convert(v);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> v2 = (List<?>) lvResult;
 		assertEquals(v2.size(), 1);
 		assertEquals("~my~null~value~", v2.get(0));
 		
 		lvResult = c.convert(lvResult);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		v2 = (List<?>) lvResult;
 		assertEquals(v2.size(), 1);
 		assertNull(v2.get(0));
 	}
 
 
 	public void testListWithoutConversion() throws Exception {
 		Converter c = new Converter();
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("Test-1");
 		lvList.add("Test-2");
 		Object lvResult = c.convert(lvList);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		assertTrue(lvResult instanceof ArrayList);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(lvList.size(), lvListAfter.size());		
 		assertEquals(lvList.get(0), lvListAfter.get(0));
 		assertEquals(lvList.get(1), lvListAfter.get(1));
 	}
 
 	public void testDoubleList() throws Exception {
 		Converter c = new Converter();
		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		List<Object> lvList = new ArrayList<Object>();
 		lvList.add("Test-1");
 		List<String> lvList2 = new ArrayList<String>();
 		lvList2.add("Test-2");
 		lvList.add(lvList2);
 		
 		Object lvResult = c.convert(lvList);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(lvList.size(), lvListAfter.size());		
 		assertEquals(lvList.get(0), lvListAfter.get(0));
 		assertTrue(lvListAfter.get(1) instanceof List);
 		assertEquals(lvList.get(1), lvListAfter.get(1));
 		
 		List<?> v = (List<?>) lvListAfter.get(1);
 		assertEquals(lvList2.size(), v.size());
 		assertEquals(lvList2.get(0), v.get(0));
 	}
 
 	public void testDefaultCollectionType() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion());
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("Test-1");
 		lvList.add("Test-2");
 		Object lvResult = c.convert(lvList);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(lvList.size(), lvListAfter.size());		
 		assertEquals(lvList.get(0), lvListAfter.get(0));
 		assertEquals(lvList.get(1), lvListAfter.get(1));
 	}
 
 	public void testHashSet() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(HashSet.class));
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("Test-1");
 		lvList.add("Test-2");
 		Object lvResult = c.convert(lvList);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof Set);
 		assertTrue(lvResult instanceof HashSet);
 		Set<?> lvSetAfter = (Set<?>) lvResult;
 		assertEquals(lvList.size(), lvSetAfter.size());
 		String s[] = (String[]) lvSetAfter.toArray(new String[2]);
 		assertEquals(lvList.get(0), s[0]);
 		assertEquals(lvList.get(1), s[1]);
 	}
 	
 	public void testHashSet2() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(HashSet.class));
 		Set<Node> lvSet = new HashSet<Node>();
 		Node n = new Node("Test-1");
 		lvSet.add(n);
 		lvSet.add(n);
 		
 		Object lvResult = c.convert(lvSet);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof Set);
 		assertTrue(lvResult instanceof HashSet);
 		Set<?> lvSetAfter = (Set<?>) lvResult;
 		assertEquals(lvSet.size(), lvSetAfter.size());
 		
 		Node node[] = (Node[]) lvSet.toArray(new Node[2]);
 		Node nodesAfter[] = (Node[]) lvSetAfter.toArray(new Node[2]);
 		assertEquals(node[0], nodesAfter[0]);
 		assertEquals(node[1], nodesAfter[1]);
 	}
 
 
 	public void testTreeSet() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(TreeSet.class));
 		Set<String> lvSet = new HashSet<String>();
 		lvSet.add("Test-1");
 		lvSet.add("Test-2");
 		Object lvResult = c.convert(lvSet);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof Set);
 		assertTrue(lvResult instanceof TreeSet);
 		Set<?> lvSetAfter = (Set<?>) lvResult;
 		assertEquals(lvSet.size(), lvSetAfter.size());
 		
 		Iterator<String> iter = lvSet.iterator();
 		Iterator<?> iterAfter = lvSetAfter.iterator();
 		assertEquals(iter.next(), iterAfter.next());
 		assertEquals(iter.next(), iterAfter.next());
 	}
 
 
 	public void testNullValueForBean() throws Exception {
 		Converter c = new Converter();
 		c.addConversion(new Iterateable2IterateableConversion(HashSet.class));
 
 		Object lvResult = c.convert(null);
 		assertNull(lvResult);
 	}
 
 	public void testInvalidCollectionType() throws Exception {
 		try {
 			Converter c = new Converter();
 			c.addConversion(new Iterateable2IterateableConversion(HashMap.class));
 			fail("Invalid Collection-Type: HashMap");
 		} catch (IllegalArgumentException e) {
 			assertNotNull(e);
 		}
 	}
 
 	public void testInvalidCollectionImplType() throws Exception {
 		try {
 			Converter c = new Converter();
 			c.addConversion(new Iterateable2IterateableConversion(List.class));
 			fail("List is not a implementation.");
 		} catch (IllegalArgumentException e) {
 			assertNotNull(e);
 		}
 	}
 
 	public void testArrayList() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		String s[] = new String[] {"Test-1", "Test-2"};
 		Object lvResult = c.convert(s);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(s.length, lvListAfter.size());		
 		assertEquals(s[0], lvListAfter.get(0));
 		assertEquals(s[1], lvListAfter.get(1));
 	}
 
 	public void testObjectArray2ArrayList() throws Exception {
 		Converter c = new Converter();
 		c.getConversionHandler().addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		Object o[] = new Object[] {new Node("Test-1"), "Test-2"};
 		Object lvResult = c.convert(o);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		assertTrue(lvResult instanceof ArrayList);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(o.length, lvListAfter.size());		
 		assertEquals(o[0], lvListAfter.get(0));
 		assertEquals(o[1], lvListAfter.get(1));
 	}
 
 	public void testObjectArray2ArrayList2() throws Exception {
 		Converter c = new Converter();
 		c.addConversion(new Iterateable2IterateableConversion(ArrayList.class));
 		c.addConversion(new IterateableMap2MapConversion(HashMap.class));
 		c.addConversion(new ComplexBean2MapConversion(HashMap.class));
 		
 		Node n = new Node("Test-1");
 		Object o[] = new Object[] {n, "Test-2", n};
 		Object lvResult = c.convert(o);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof List);
 		assertTrue(lvResult instanceof ArrayList);
 		List<?> lvListAfter = (List<?>) lvResult;
 		assertEquals(o.length, lvListAfter.size());		
 		assertEquals(o[1], lvListAfter.get(1));
 		
 		Map<?, ?> lvNodeMap = (Map<?, ?>) lvListAfter.get(0);
 		assertEquals(Node.class.getName(), lvNodeMap.get("class"));
 		assertEquals("Test-1", lvNodeMap.get("name"));
 		String lvUniqueId = (String) lvNodeMap.get(UniqueIdGenerator.UNIQUE_ID_PROPERTY);
 		assertNotNull(lvUniqueId);
 		String lvUniqueId2 = (String) lvListAfter.get(2);
 		assertEquals(UniqueIdGenerator.UNIQUE_ID_PROPERTY + lvUniqueId, lvUniqueId2);
 	}
 
 	
 	public void testToCollectionDirect() throws Exception {
 		Converter c = new Converter();
 		c.addConversion(new Iterateable2IterateableConversion());
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("1");
 		lvList.add("2");
 		Object lvResult = c.convert(lvList, LinkedList.class);
 		assertNotNull(lvResult);
 		assertTrue(lvResult instanceof LinkedList);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testIgnoreNullValues() throws Exception {
 		Converter c = new Converter();
 		Iterateable2IterateableConversion lvConversion = new Iterateable2IterateableConversion();
 		c.addConversion(lvConversion);
 		
 		List<String> lvList = new ArrayList<String>();
 		lvList.add("1");
 		lvList.add(null);
 		lvList.add("3");
 		List<?> newList = (List<?>) c.convert(lvList);
		assertEquals(3, newList.size());
 		
 		lvConversion.setIgnoreNullValues(true);
		List<?> newNullList = (List<String>) c.convert(lvList);
		assertEquals(2, newNullList.size());
 	}
 
 }
