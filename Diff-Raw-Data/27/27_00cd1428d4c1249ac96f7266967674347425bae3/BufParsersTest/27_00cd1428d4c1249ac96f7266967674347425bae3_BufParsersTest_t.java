 package net.bodz.bas.util.regex;
 
 import java.nio.CharBuffer;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class BufParsersTest
         extends Assert {
 
     @Test
     public void testGetIntCharBuffer() {
         class D {
             void o(String input, String expected) {
                 CharBuffer buf = CharBuffer.wrap(input);
                 int val = BufParsers.getInt(buf, 10, 1023);
                 String actual = val + "|" + buf;
                 assertEquals(expected, actual);
             }
         }
         D d = new D(); //
         d.o("0", "0|"); // 1
         d.o("1xyz", "1|xyz"); //
         d.o("xyz", "-1|xyz"); //
         d.o("", "-1|");
         d.o("1", "1|");
         d.o("00000001", "1|"); //
         d.o("9", "9|");
         d.o("99", "99|");
         d.o("999", "999|");
         d.o("9999", "999|9"); //
 
         d.o("1000", "1000|"); // 11
         d.o("1020", "1020|");
         d.o("1023", "1023|");
         d.o("1024", "102|4");
         d.o("1033", "103|3");
         d.o("01023", "1023|");
     }
 
     @Test
     public void testGetLongCharBuffer() {
         class D {
             void o(String input, String expected) {
                 CharBuffer buf = CharBuffer.wrap(input);
                long val = BufParsers.getLong(buf, 10, 111_222_333_444L);
                 String actual = val + "|" + buf;
                 assertEquals(expected, actual);
             }
         }
         D d = new D(); //
         d.o("0", "0|"); // 1
         d.o("1xyz", "1|xyz"); //
         d.o("xyz", "-1|xyz"); //
         d.o("", "-1|");
         d.o("1", "1|");
         d.o("00000001", "1|");
         d.o("99999999999", "99999999999|");
         d.o("999999999999", "99999999999|9"); //
 
         d.o("111222333443", "111222333443|"); // 11
         d.o("111222333444", "111222333444|"); // 11
         d.o("111222333445", "11122233344|5"); // 11
         d.o("000111222333444", "111222333444|"); // 11
     }
 
 }
