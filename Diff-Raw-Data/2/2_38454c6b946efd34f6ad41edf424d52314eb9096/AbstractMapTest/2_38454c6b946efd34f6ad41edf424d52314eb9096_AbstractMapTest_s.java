 package au.id.tmm.datastructures.map;
 
 import au.id.tmm.datastructures.Iterator;
 import au.id.tmm.datastructures.set.Set;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public abstract class AbstractMapTest {
 
     private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
     private static final int START_KEY = 0;
     private static final String START_LETTER = String.valueOf(ALPHABET.charAt(START_KEY));
 
     private static final int MID_KEY = 12;
     private static final String MID_LETTER = String.valueOf(ALPHABET.charAt(MID_KEY));
 
     private static final int LAST_KEY = 25;
     private static final String LAST_LETTER = String.valueOf(ALPHABET.charAt(LAST_KEY));
 
 
     public abstract Map<Integer, String> constructNewConcreteMap(int initialCapacity);
 
     @Test
     public void testPut() throws Exception {
 
         final int INITIAL_CAPACITY = 5;
         final String TEST_NEW_PUT = "Hello";
 
         Map<Integer, String> map = this.constructNewConcreteMap(INITIAL_CAPACITY);
 
         for (int i = 0; i < ALPHABET.length(); i++) {
             map.put(i, String.valueOf(ALPHABET.charAt(i)));
         }
 
         assertEquals(map.get(START_KEY), START_LETTER);
         assertEquals(map.get(MID_KEY), MID_LETTER);
         assertEquals(map.get(LAST_KEY), LAST_LETTER);
 
         map.put(MID_KEY, TEST_NEW_PUT);
 
         assertEquals(map.get(MID_KEY), TEST_NEW_PUT);
 
     }
 
     @Test
     public void testRemove() throws Exception {
         Map<Integer, String> map = this.generateAlphabetMap();
 
         map.remove(START_KEY);
         map.remove(MID_KEY);
 
         assertNull(map.get(START_KEY));
         assertNull(map.get(MID_KEY));
         assertEquals(map.get(LAST_KEY), LAST_LETTER);
     }
 
     @Test
     public void testGet() throws Exception {
 
         Map<Integer, String> map = this.generateAlphabetMap();
 
         assertEquals(map.get(START_KEY), START_LETTER);
         assertEquals(map.get(MID_KEY), MID_LETTER);
         assertEquals(map.get(LAST_KEY), LAST_LETTER);
 
     }
 
     @Test
     public void testContainsKey() throws Exception {
 
         final Integer KEY_NOT_CONTAINED = 123;
 
         Map<Integer, String> map = this.generateAlphabetMap();
 
         assertTrue(map.containsKey(START_KEY));
         assertTrue(map.containsKey(MID_KEY));
         assertTrue(map.containsKey(LAST_KEY));
 
         assertFalse(map.containsKey(KEY_NOT_CONTAINED));
 
     }
 
     @Test
     public void testContainsValue() throws Exception {
 
         final String VALUE_NOT_CONTAINED = "NOT CONTAINED";
 
         Map<Integer, String> map = this.generateAlphabetMap();
 
         assertTrue(map.containsValue(START_LETTER));
         assertTrue(map.containsValue(MID_LETTER));
         assertTrue(map.containsValue(LAST_LETTER));
 
         assertFalse(map.containsValue(VALUE_NOT_CONTAINED));
 
     }
 
     @Test
     public void testGetKeySet() throws Exception {
 
         Set<Integer> keySet = this.generateAlphabetMap().getKeySet();
 
         for (int i = 0; i < ALPHABET.length(); i++) {
             assertTrue(keySet.contains(i));
         }
 
     }
 
     @Test
     public void testGetValueSet() throws Exception {
 
         Set<String> valueSet = this.generateAlphabetMap().getValueSet();
 
         for (int i = 0; i < ALPHABET.length(); i++) {
             assertTrue(valueSet.contains(String.valueOf(ALPHABET.charAt(i))));
         }
 
     }
 
     @Test
     public void testGetEntrySet() throws Exception {
 
         Set<Entry<Integer, String>> entrySet = this.generateAlphabetMap().getEntrySet();
 
         for (Iterator<Entry<Integer, String>> it = entrySet.iterator(); it.hasNext();) {
             Entry<Integer, String> currentEntry = it.next();
 
            assertEquals(currentEntry.getValue(), String.valueOf(ALPHABET.charAt(Integer.parseInt(currentEntry.getValue()))));
         }
 
     }
 
     @Test
     public void testClear() throws Exception {
         Map<Integer, String> map = this.generateAlphabetMap();
 
         map.clear();
 
         assertTrue(map.isEmpty());
     }
 
     @Test
     public void testIsEmpty() throws Exception {
         Map<Integer, String> map = this.generateAlphabetMap();
 
         assertFalse(map.isEmpty());
 
         map.clear();
 
         assertTrue(map.isEmpty());
     }
 
     @Test
     public void testGetSize() throws Exception {
 
         final Integer TEST_ADD_KEY_1 = 123;
         final String TEST_ADD_VALUE_1 = "Test add 1";
 
         final Integer TEST_ADD_KEY_2 = 532;
         final String TEST_ADD_VALUE_2 = "Test add 2";
 
         Map<Integer, String> map = this.generateAlphabetMap();
 
         assertEquals(map.getSize(), ALPHABET.length());
 
         map.put(TEST_ADD_KEY_1, TEST_ADD_VALUE_1);
 
         assertEquals(map.getSize(), ALPHABET.length() + 1);
 
         map.put(MID_KEY, MID_LETTER);
 
         assertEquals(map.getSize(), ALPHABET.length() + 1);
 
         map.clear();
 
         assertEquals(map.getSize(), 0);
 
         map.put(TEST_ADD_KEY_2, TEST_ADD_VALUE_2);
 
         assertEquals(map.getSize(), 1);
 
     }
 
     private Map<Integer, String> generateAlphabetMap() {
         Map<Integer, String> newMap = this.constructNewConcreteMap(ALPHABET.length());
 
         for (int i = 0; i < ALPHABET.length(); i++) {
             newMap.put(i, String.valueOf(ALPHABET.charAt(i)));
         }
 
         return newMap;
     }
 }
