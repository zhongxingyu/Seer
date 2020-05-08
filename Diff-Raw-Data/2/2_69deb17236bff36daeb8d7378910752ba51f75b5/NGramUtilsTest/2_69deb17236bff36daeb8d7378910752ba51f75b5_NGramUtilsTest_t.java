 package com.github.samplett.nlp.util;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
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
     Double d = NGramUtils.calculateBigramProbability("I","<s>",set);
     assertTrue(d>0);
     assertEquals(Double.valueOf(0.6666666666666666d),d);
     d = NGramUtils.calculateBigramProbability("</s>","Sam",set);
     assertEquals(Double.valueOf(0.5d),d);
     d = NGramUtils.calculateBigramProbability("Sam","<s>",set);
     assertEquals(Double.valueOf(0.3333333333333333d),d);
   }
 }
