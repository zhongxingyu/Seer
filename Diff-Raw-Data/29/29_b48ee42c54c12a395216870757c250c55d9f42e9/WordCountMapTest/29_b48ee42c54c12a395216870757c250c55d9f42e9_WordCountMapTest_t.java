 package cs.dsn.dwc;
 
import cs.dsn.dwc.wordcounter.WordCountMap;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Tests for WordCountMap
  */
 public class WordCountMapTest {
   @Test
   public void testConstructorFromString() {
     String test = "Test 1\n" +
             "this 4\n" +
             "String 24\n";
 
     WordCountMap counts = new WordCountMap(test);
 
     assertEquals(counts.get("test"), 1);
     assertEquals(counts.get("this"), 4);
     assertEquals(counts.get("string"), 24);
   }
 
   @Test
   public void testWordIncrement() throws Exception {
     String word = "TEST";
     WordCountMap map = new WordCountMap();
     assertEquals(map.get(word), 0);
 
     map.increment(word);
     assertEquals(map.get(word), 1);
 
     map.increment(word);
     assertEquals(map.get(word), 2);
   }
 
   @Test
   public void testWordIncrementBy() throws Exception {
     String word = "TEST";
     WordCountMap map = new WordCountMap();
     assertEquals(map.get(word), 0);
 
     map.incrementBy(word, 1);
     assertEquals(map.get(word), 1);
 
     map.incrementBy(word, 5);
     assertEquals(map.get(word), 6);
 
     map.increment(word);
     assertEquals(map.get(word), 7);
   }
 
   @Test
   public void testAddNonexistingWord() throws Exception {
     String test = "test";
     WordCountMap testMap = new WordCountMap();
     WordCountMap thatMap = new WordCountMap();
 
     testMap.increment(test);
     assertEquals(testMap.get(test), 1);
     assertEquals(thatMap.get(test), 0);
     thatMap.add(testMap);
     assertEquals(thatMap.get(test), 1);
   }
 
   @Test
   public void testAddExistingWord() throws Exception {
     String test = "test";
     String that = "that";
     WordCountMap testMap = new WordCountMap();
     WordCountMap thatMap = new WordCountMap();
 
     testMap.increment(test);
     thatMap.increment(test);
     thatMap.incrementBy(that, 12);
     assertEquals(testMap.get(test), 1);
     assertEquals(testMap.get(that), 0);
     assertEquals(thatMap.get(test), 1);
     assertEquals(thatMap.get(that), 12);
 
     thatMap.add(testMap);
     assertEquals(testMap.get(test), 1);
     assertEquals(testMap.get(that), 0);
     assertEquals(thatMap.get(test), 2);
     assertEquals(thatMap.get(that), 12);
 
     testMap.add(thatMap);
     assertEquals(testMap.get(test), 3);
     assertEquals(testMap.get(that), 12);
     assertEquals(thatMap.get(test), 2);
     assertEquals(thatMap.get(that), 12);
   }
 
   @Test
   public void testGetWords() throws Exception {
     String test = "test";
     String that = "that";
     String andThis = "andThis";
     WordCountMap testMap = new WordCountMap();
     assertEquals(testMap.words().size(), 0);
 
     testMap.increment(test);
     assertEquals(testMap.words().size(), 1);
 
     testMap.incrementBy(test, 12);
     assertEquals(testMap.words().size(), 1);
 
     testMap.increment(that);
     assertEquals(testMap.words().size(), 2);
 
     WordCountMap thisMap = new WordCountMap();
     thisMap.increment(andThis);
     testMap.add(thisMap);
     assertEquals(testMap.words().size(), 3);
   }
 
   @Test
   public void testCaseInsensitive() throws Exception {
     String Test = "Test";
     String test = "test";
     String TeSt = "TeSt";
     WordCountMap testMap = new WordCountMap();
 
     testMap.increment(Test);
     assertEquals(testMap.get(Test), 1);
     assertEquals(testMap.get(test), 1);
     assertEquals(testMap.get(TeSt), 1);
     assertEquals(testMap.words().size(), 1);
 
     testMap.increment(test);
     assertEquals(testMap.get(Test), 2);
     assertEquals(testMap.get(test), 2);
     assertEquals(testMap.get(TeSt), 2);
     assertEquals(testMap.words().size(), 1);
 
     testMap.increment(TeSt);
     assertEquals(testMap.get(Test), 3);
     assertEquals(testMap.get(test), 3);
     assertEquals(testMap.get(TeSt), 3);
     assertEquals(testMap.words().size(), 1);
   }
 }
