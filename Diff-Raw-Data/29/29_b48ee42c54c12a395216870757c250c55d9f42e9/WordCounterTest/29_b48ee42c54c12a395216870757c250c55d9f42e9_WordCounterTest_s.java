 package cs.dsn.dwc;
 
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
 * Created with IntelliJ IDEA.
 * User: jack
 * Date: 13/02/2013
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
  */
 public class WordCounterTest {
   /**
    * Test using example sentence from assignment sheet.
    *
    * Input:
    *   "There are two pens on the table, one is red and the other is black."
    *
    * Output:
    *    Word    | Count
    *   -----------------
    *    There   |   1
    *    Are     |   1
    *    Two     |   1
    *    Pens    |   1
    *    On      |   1
    *    The     |   2
    *    Table   |   1
    *    One     |   1
    *    Is      |   2
    *    Red     |   1
    *    And     |   1
    *    Other   |   1
    *    Black   |   1
    */
   @Test
   public void testCount() {
     WordCounter counter = new WordCounter("There are two pens on the table, one is red and the other is black.");
     WordCountMap counts = counter.count();
 
     Set<String> expectedWords = new HashSet<String>(Arrays.asList(
             "there", "are", "two", "pens", "on", "the", "table", "one", "is", "red", "and", "other", "black"
     ));
 
     assertEquals(expectedWords, counts.words());
 
     assertEquals(counts.get("There"), 1);
     assertEquals(counts.get("Are"), 1);
     assertEquals(counts.get("Two"), 1);
     assertEquals(counts.get("Pens"), 1);
     assertEquals(counts.get("On"), 1);
     assertEquals(counts.get("The"), 2);
     assertEquals(counts.get("Table"), 1);
     assertEquals(counts.get("One"), 1);
     assertEquals(counts.get("Is"), 2);
     assertEquals(counts.get("Red"), 1);
     assertEquals(counts.get("And"), 1);
     assertEquals(counts.get("Other"), 1);
     assertEquals(counts.get("Black"), 1);
   }
 }
