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
    	assertEquals("Mira!\nMira, Mira bo Bira Bonana fanna fo Fira\nFee fy mo Mira, Mira!", StringUtils.nameGame("Mira"));
    	assertEquals("Annie!\nAnnie, Annie bo Bannie Bonana fanna fo Fannie\nFee fy mo Mannie, Annie!", StringUtils.nameGame("Annie"));
    	assertEquals("Bo!\nBo, Bo bo Bo Bonana fanna fo Fo\nFee fy mo Mo, Bo!", StringUtils.nameGame("Bo"));
    	assertEquals("Austin!\nAustin, Austin bo Baustin Bonana fanna fo Faustin\nFee fy mo Maustin, Austin!", StringUtils.nameGame("Austin"));
    	assertEquals("Lincoln!\nLincoln, Lincoln bo Bincoln Bonana fanna fo Fincoln\nFee fy mo Mincoln, Lincoln!", StringUtils.nameGame("Lincoln"));
    	assertEquals("Sky!\nSky, Sky bo By Bonana fanna fo Fy\nFee fy mo My, Sky!", StringUtils.nameGame("Sky"));
    	assertEquals("Cyndy!\nCyndy, Cyndy bo Byndy Bonana fanna fo Fyndy\nFee fy mo Myndy, Cyndy!", StringUtils.nameGame("Cyndy"));
    	
     }
 }
