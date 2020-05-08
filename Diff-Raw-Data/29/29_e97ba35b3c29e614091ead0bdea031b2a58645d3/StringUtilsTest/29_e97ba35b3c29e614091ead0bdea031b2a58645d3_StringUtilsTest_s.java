 package edu.grinnell.csc207.goldstei1.utils;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class StringUtilsTest {
 
     @Test
     public void splitAttest() {
 	assertArrayEquals(new String[] { "a", "b", "c" },
 		StringUtils.splitAt("a:b:c", ':'));
 	assertArrayEquals(new String[] { "a", "b", "c" },
 		StringUtils.splitAt("a b c", ' '));
 	assertArrayEquals(new String[] { "a:b:c" },
 		StringUtils.splitAt("a:b:c", ' '));
 	assertArrayEquals("one field", new String[] { "a" },
 		StringUtils.splitAt("a", ':'));
 	assertArrayEquals("empty inner field", new String[] { "a:", "c" },
 		StringUtils.splitAt("a:&c", '&'));
 	assertArrayEquals("leading empty field", new String[] { "", "a" },
 		StringUtils.splitAt(":a", ':'));
 	assertArrayEquals("trailing empty field", new String[] { "a", "" },
 		StringUtils.splitAt("a:", ':'));
     }
 
     @Test
     public void splitCSVTest() {
     	assertArrayEquals(new String[] { "a", "b", "c" },
     			StringUtils.splitCSV("a,b,c", ','));
     	assertArrayEquals(new String[] { "a,b", "c" },
     			StringUtils.splitCSV("\"a,b\",c", ','));
     	assertArrayEquals(new String[] { "a", "b,b\"", "c" },
     			StringUtils.splitCSV("a,\"b,b\"\"\",c", ','));
     	assertArrayEquals(new String[] { "Jenny said", " \"I lost", " Bob\"", " to Bob"},
     			StringUtils.splitCSV("Jenny said, \"\"I lost, Bob\"\", to Bob", ','));
     	assertArrayEquals(new String[] { "Jenny said", " \"I lost, Bob\"", " to Bob" },
     			StringUtils.splitCSV("Jenny said, \"\"I lost\",\" Bob\"\", to Bob", ','));
     	
     }
     
     @Test
     public void deLeetTest() {
     	assertEquals("e", StringUtils.deLeet("3"));
     	assertEquals("leet", StringUtils.deLeet("133+"));
     	assertEquals("eat banana", StringUtils.deLeet("3@+ |3@|\\|@|\\|@"));
     	assertEquals("tuff stuff", StringUtils.deLeet("+|_||=|= $+|_||=|="));
     	assertEquals("last case", StringUtils.deLeet("1@$+ (@$3"));
     }
     
     @Test
     public void nameGameTest() {
     	assertEquals("Shirley!\nShirley, Shirley bo Birley Bonana fanna fo Firley\nFee fy mo Mirley, Shirley!", StringUtils.nameGame("Shirley"));
     }
 }
