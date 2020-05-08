 package com.github.tteofili.nlputils;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Testcase for {@link NGramUtils}
  */
 public class NGramUtilsTest {
   @Test
   public void testBigram() {
     Collection<String[]> set = new LinkedList<String[]>();
     set.add(new String[]{"<s>","I","am","Sam","</s>"});
     set.add(new String[]{"<s>","Sam","I","am","</s>"});
     set.add(new String[]{"<s>","I","do","not","like","green","eggs","and","ham","</s>"});
     set.add(new String[]{});
     Double d = NGramUtils.calculateBigramMLProbability("I", "<s>", set);
     assertTrue(d>0);
     assertEquals(Double.valueOf(0.6666666666666666d),d);
     d = NGramUtils.calculateBigramMLProbability("</s>", "Sam", set);
     assertEquals(Double.valueOf(0.5d),d);
     d = NGramUtils.calculateBigramMLProbability("Sam", "<s>", set);
     assertEquals(Double.valueOf(0.3333333333333333d),d);
   }
 
   @Test
   public void testTrigram() {
     Collection<String[]> set = new LinkedList<String[]>();
     set.add(new String[]{"<s>","I","am","Sam","</s>"});
     set.add(new String[]{"<s>","Sam","I","am","</s>"});
     set.add(new String[]{"<s>","I","do","not","like","green","eggs","and","ham","</s>"});
     set.add(new String[]{});
     Double d = NGramUtils.calculateTrigramMLProbability("I", "am", "Sam",set);
     assertTrue(d>0);
     assertEquals(Double.valueOf(0.5),d);
     d = NGramUtils.calculateTrigramMLProbability("Sam","I", "am", set);
     assertEquals(Double.valueOf(1d),d);
   }
 
   @Test
   public void testLinearInterpolation() throws Exception {
     Collection<String[]> set = new LinkedList<String[]>();
     set.add(new String[]{"the","green","book","STOP"});
     set.add(new String[]{"my","blue","book","STOP"});
     set.add(new String[]{"his","green","house","STOP"});
     set.add(new String[]{"book","STOP"});
     Double lambda = 1d/3d;
     Double d = NGramUtils.calculateLinearInterpolationProbability("the", "green", "book", set, lambda, lambda, lambda);
     assertNotNull(d);
     assertTrue(d > 0);
    assertEquals("wrong result", Double.valueOf(0.5714285714285714d), d);
   }
 
 }
