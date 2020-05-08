 package edu.grinnell.csc207.turneral1.utils;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class StringUtilsTest {
 
     /** Have JUnit run this test() method. */
     @Test
     public void test() throws Exception {
 
         assertArrayEquals("simple case",new String[]{"a","b","c"},
         	StringUtils.splitAt("a:b:c", ':'));
         assertArrayEquals("no sperator",new String[]{"a"},
         	StringUtils.splitAt("a", ':'));
         assertArrayEquals("space speceator", new String[]{"a","b"},
         	StringUtils.splitAt("a b", ' '));
         assertArrayEquals("leading empty field",new String[]{"","b","c"},
         	StringUtils.splitAt(":b:c", ':'));
         assertArrayEquals("trailing empty field",new String[]{"b","c", ""},
         	StringUtils.splitAt("b:c:", ':'));
         assertArrayEquals("leading and trailing empty field",
         	new String[]{"","b","c",""},StringUtils.splitAt(":b:c:", ':'));
         assertArrayEquals("middle empty field",new String[]{"b","","c"},
         	StringUtils.splitAt("b::c", ':'));
 
         assertArrayEquals("simple case",new String[]{"a","b","c"},
         	StringUtils.splitCSV("a,b,c"));
         assertArrayEquals("quote used",new String[]{ "a,b", "c" },
         	StringUtils.splitCSV("\"a,b\",c"));
         assertArrayEquals("multiple quotes used",
         	new String[]{  "a", "b,b\"", "c" },
         	StringUtils.splitCSV("a,\"b,b\"\"\",c"));
         assertArrayEquals("no sperator",new String[]{"a"},
         	StringUtils.splitCSV("a"));
         assertArrayEquals("leading empty field",new String[]{"","b","c"},
         	StringUtils.splitCSV(",b,c"));
         assertArrayEquals("trailing empty field",new String[]{"b","c", ""},
         	StringUtils.splitCSV("b,c,"));
         assertArrayEquals("leading and trailing empty field",
         	new String[]{"","b","c",""}, StringUtils.splitCSV(",b,c,"));
         assertArrayEquals("middle empty field",new String[]{"b","","c"},
         	StringUtils.splitCSV("b,,c"));
     }
     @Test
     public void deLeetTest() {
 	assertEquals("abelnot", StringUtils.deLeet("@|331|\\|0+"));
 	assertEquals(" leeeet ", StringUtils.deLeet(" 13333+ "));
 	assertEquals("rebelsky", StringUtils.deLeet("r3|331sky"));
     } // deLeetTest
     
     // We ran some experiments on nameGame - it works fine!
     
     @Test
     public void fewestCoinsTest() {
 	assertArrayEquals(new int[]{}, StringUtils.fewestCoins(0));
 	assertArrayEquals(new int[]{7,7,7,7}, StringUtils.fewestCoins(28));
     } // fewestCoinsTest
 
 } // StringUtilsTest
