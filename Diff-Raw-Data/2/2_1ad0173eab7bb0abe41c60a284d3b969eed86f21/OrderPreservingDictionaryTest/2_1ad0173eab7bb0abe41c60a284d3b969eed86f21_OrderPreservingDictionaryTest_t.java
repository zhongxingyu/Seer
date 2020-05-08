 package org.eclipse.swordfish.internal.core.util;
 
 import static org.junit.Assert.*;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class OrderPreservingDictionaryTest {
 	private static final String VALUE = "Bunny";
 	private static final String KEY = "Bugs";
 	private OrderPreservingDictionary<String, String> dictionary;
 	
 	@Before
 	public void setUp() throws Exception {
 		dictionary = new OrderPreservingDictionary<String, String>();
 	}
 
 	
 	@Test
 	public void testChangingSizeEmptyAndFilled() {
 		assertTrue(dictionary.isEmpty());
 		assertEquals(0, dictionary.size());
 
 		dictionary.put(KEY, VALUE);
 		assertTrue(dictionary.hashCode() > 0);
 
 		assertFalse(dictionary.isEmpty());
 		assertEquals(1, dictionary.size());
 
 		dictionary.remove(KEY);
 	
 		assertTrue(dictionary.isEmpty());
 
 		dictionary.put(KEY, VALUE);
 		assertFalse(dictionary.isEmpty());
 
 		dictionary.clear();
 		assertTrue(dictionary.isEmpty());
 	}
 
 	
 	@Test
 	public void testContructFromNullDictionary() {
 		dictionary = new OrderPreservingDictionary<String, String>((Dictionary<String, String>) null);
 		assertTrue(dictionary.isEmpty());
 	}
 
 	
 	@Test
 	public void testContructFromEmptyDictionary() {
 		Dictionary<String, String> source = new Hashtable<String, String>();
 		dictionary = new OrderPreservingDictionary<String, String>(source);
 		assertTrue(dictionary.isEmpty());
 	}
 
 	
 	@Test
 	public void testContructFromDictionary() {
 		String keysNvalues = "QWERTZUIOPASDFGHJKLYXCVBNM789456123";
 		Dictionary<String, String> source = new Hashtable<String, String>();
 		
 		for (int i = 0; i < keysNvalues.length(); i++) {
 			String kv = keysNvalues.substring(i, i + 1);
 			source.put(kv, kv);
 		}
 		
 		dictionary = new OrderPreservingDictionary<String, String>(source);
 		assertFalse(dictionary.isEmpty());
 		assertEquals(keysNvalues.length(), dictionary.size());
 	}
 
 	
 	@Test
 	public void testElements() {
 		dictionary.put(KEY, VALUE);
 		assertEquals(VALUE, dictionary.elements().nextElement());
 	}
 
 	
 	@Test
 	public void testGetObject() {
 		dictionary.put(KEY, VALUE);
 		assertEquals(VALUE, dictionary.get(KEY));
 	}
 
 	
 	@Test
 	public void testKeys() {
 		dictionary.put(KEY, VALUE);
 		assertEquals(KEY, dictionary.keys().nextElement());
 	}
 
 	
 	@Test
 	public void testContainsKey() {
 		dictionary.put(KEY, VALUE);
 		assertTrue(dictionary.containsKey(KEY));
 		assertFalse(dictionary.containsKey(VALUE));
 	}
 
 	
 	@Test
 	public void testContainsValue() {
 		dictionary.put(KEY, VALUE);
 		assertTrue(dictionary.containsValue(VALUE));
 		assertFalse(dictionary.containsValue(KEY));
 	}
 
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testPutNullKey() {
 		dictionary.put(null, VALUE);
 	}
 
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testPutNullValue() {
 		dictionary.put(KEY, null);
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testEntryAndKeySet() {
 		dictionary.put(KEY, VALUE);
 		dictionary.put(VALUE, KEY);
 		Set entries = dictionary.entrySet();
 		Set keys = dictionary.keySet();
 		assertEquals(dictionary.size(), entries.size());
 		assertEquals(dictionary.size(), keys.size());
 	}
 
 	
 	@Test
 	public void testPutAllWithOrder() {
 		String keysNvalues = "QWERTZUIOPASDFGHJKLYXCVBNM789456123";
 		Map<String, String> source = new LinkedHashMap<String, String>();
 		
 		for (int i = 0; i < keysNvalues.length(); i++) {
 			String kv = keysNvalues.substring(i, i + 1);
 			source.put(kv, kv);
 		}
 
 		OrderPreservingDictionary<String, String> other = 
 			new OrderPreservingDictionary<String, String>(source);
 		dictionary.putAll(source);
 		
 		int i = 0;
 		for (String key : dictionary.keySet()) {
 			assertEquals(keysNvalues.substring(i, i + 1), key);
 			i++;
 		}
 		
 		i = 0;
 		for (String value : dictionary.values()) {
 			assertEquals(keysNvalues.substring(i, i + 1), value);
 			i++;
 		}
 		
 		assertEquals(dictionary.toString(), dictionary, other);
 	}
 
 	
 	@Test
 	public void testToString() {
 		dictionary.put(KEY, VALUE);
		assertEquals("{Bugs=Bunny}", dictionary.toString());
 	}
 }
