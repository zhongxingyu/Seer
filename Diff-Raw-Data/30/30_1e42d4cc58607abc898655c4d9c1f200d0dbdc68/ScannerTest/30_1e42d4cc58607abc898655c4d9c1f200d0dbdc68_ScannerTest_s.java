 /* This class is part of the XP framework
  *
  * $Id$
  */
 
 package net.xp_framework.unittest.text;
 
 import net.xp_framework.text.*;
 import org.junit.*;
 
 import static org.junit.Assert.*;
 
 public class ScannerTest {
 
     @Test public void integerFromEmptyString() {
         assertEquals(null, Scanner.scan("", "%d").get(0));
     }
 
     @Test public void floatFromEmptyString() {
         assertEquals(null, Scanner.scan("", "%f").get(0));
     }
 
     @Test public void stringFromEmptyString() {
         assertEquals(null, Scanner.scan("", "%s").get(0));
     }
 
     @Test public void hexFromEmptyString() {
         assertEquals(null, Scanner.scan("", "%x").get(0));
     }
 
     @Test public void charFromEmptyString() {
         assertEquals(null, Scanner.scan("", "%c").get(0));
     }
 
     @Test public void integerNumber() {
         assertEquals(1, Scanner.scan("1", "%d").get(0));
     }
 
     @Test public void hexNumber() {
         assertEquals(0xFF, Scanner.scan("FF", "%x").get(0));
     }
 
     @Test public void hexNumberWith0x() {
         assertEquals(0xAA, Scanner.scan("0xAA", "%x").get(0));
     }
 
     @Test public void floatNumber() {
         assertEquals(1.0f, Scanner.scan("1", "%f").get(0));
     }
 
     @Test public void string() {
         assertEquals("str", Scanner.scan("str", "%s").get(0));
     }
 
     @Test public void character() {
         assertEquals('c', Scanner.scan("c", "%c").get(0));
     }
 
     @Test public void helloWorld() {
         assertEquals("World", Scanner.scan("Hello World", "Hello %s").get(0));
     }
 
     @Test public void range() {
         Scanned s= Scanner.scan("[1..2]", "[%d..%d]");
         assertEquals(1, s.get(0));
         assertEquals(2, s.get(1));
     }
 
     @Test public void characterClass() {
         Scanned s= Scanner.scan("abcde", "%[a-d]%c");
         assertEquals("abcd", s.get(0));
         assertEquals('e', s.get(1));
     }
 
     @Test public void hostGroup() {
         Scanned s= Scanner.scan("config[0-9]", "%[^[][%d-%d]");
         assertEquals("config", s.get(0));
         assertEquals(0, s.get(1));
         assertEquals(9, s.get(2));
     }
 
     @Test public void hexColor() {
         Scanned s= Scanner.scan("AABBCC", "%2x%2x%2x");
         assertEquals(0xAA, s.get(0));
         assertEquals(0xBB, s.get(1));
         assertEquals(0xCC, s.get(2));
     }
 }
