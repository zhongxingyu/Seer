 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class RegexTester {
 
     @Test
         public void testSingleCharacter() {
             Regex simpleRegex = new Regex("a");
             assertTrue(simpleRegex.matches("a"));
             assertFalse(simpleRegex.matches("aa"));
             assertFalse(simpleRegex.matches(""));
             assertFalse(simpleRegex.matches("A"));
         }
 
     @Test
         public void testCharStar() {
             Regex simpleRegex = new Regex("a*");
             assertTrue(simpleRegex.matches(""));
             assertTrue(simpleRegex.matches("a"));
             assertTrue(simpleRegex.matches("aaa"));
             assertFalse(simpleRegex.matches("b"));
         }
 
     @Test
         public void testCharPlus() {
             Regex simpleRegex = new Regex("a+");
             assertFalse(simpleRegex.matches(""));
             assertTrue(simpleRegex.matches("a"));
             assertTrue(simpleRegex.matches("aa"));
             assertTrue(simpleRegex.matches("aaa"));
             assertFalse(simpleRegex.matches("b"));
         }
 
     @Test
         public void testDotStar() {
             Regex simpleRegex = new Regex(".*");
             assertTrue(simpleRegex.matches("abcdefg"));
             assertTrue(simpleRegex.matches("aaaaa"));
             assertTrue(simpleRegex.matches(""));
         }
 
     @Test
         public void testDotPlus() {
             Regex simpleRegex = new Regex(".+");
             assertTrue(simpleRegex.matches("abcdefg"));
             assertTrue(simpleRegex.matches("aaaaa"));
             assertFalse(simpleRegex.matches(""));
         }
 
     @Test
         public void testBrackets() {
             Regex simpleRegex = new Regex("[a|b]");
             assertTrue(simpleRegex.matches("a"));
             assertTrue(simpleRegex.matches("b"));
             simpleRegex = new Regex("[a|b]+");
             assertFalse(simpleRegex.matches(""));
             assertTrue(simpleRegex.matches("a"));
             assertTrue(simpleRegex.matches("b"));
             assertTrue(simpleRegex.matches("ab"));
             assertTrue(simpleRegex.matches("abab"));
             assertTrue(simpleRegex.matches("bbba"));
         }
 
     @Test
         public void testNestedStatements() {
             Regex simpleRegex = new Regex("[a*|b]");
             assertTrue(simpleRegex.matches("aaaaa"));
             assertTrue(simpleRegex.matches("aaaaab"));
             assertTrue(simpleRegex.matches(""));
             assertTrue(simpleRegex.matches("b"));
             simpleRegex = new Regex("[a*|.]");
         }
 
     @Test
         public void testPartialMatch() {
             Regex simpleRegex = new Regex("a");
             assertTrue(simpleRegex.partialMatch("a").equals("a"));
             assertTrue(simpleRegex.partialMatch("aa").equals("a"));
             assertTrue(simpleRegex.partialMatch("aaa").equals("a"));
             simpleRegex = new Regex("a+");
             assertTrue(simpleRegex.partialMatch("aaaaa").equals("aaaaa"));
             assertTrue(simpleRegex.partialMatch("faaafaaaaaf").equals("aaaaa"));
             //assertNotNull(simpleRegex.partialMatch("a"));
             //assertNotNull(simpleRegex.partialMatch("aa"));
             //assertNotNull(simpleRegex.partialMatch("baa"));
             //assertNull(simpleRegex.partialMatch("b"));
             //assertNull(simpleRegex.partialMatch(""));
         }
 
     @Test
         public void testReplace() {
             Regex simpleRegex = new Regex("aaa");
             assertEquals("bba", simpleRegex.replaceMatching("aaaaaaa", "b"));
             assertEquals("bb", simpleRegex.replaceMatching("aaaaaa", "b"));
 
             simpleRegex = new Regex("ab+");
            assertEquals("fbf", simpleRegex.replaceMatching("abababbabab"));
         }
 }
