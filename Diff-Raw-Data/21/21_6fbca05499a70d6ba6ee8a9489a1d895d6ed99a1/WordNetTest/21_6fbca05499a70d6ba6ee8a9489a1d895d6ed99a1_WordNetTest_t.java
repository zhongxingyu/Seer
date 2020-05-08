 /*
  * Solution proposal to coursera Algorithms Part 2
  * Programming Assignment 1: WordNet
  */
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 /**
  *
  * @author Pierre
  */
 public class WordNetTest {
 
     private static WordNet wordNet;
 
     public WordNetTest() {
     }
 
     @BeforeClass
     public static void load() {
         String synsetFile = WordNetTest.class.getResource("synsets.txt").getFile();
         String hypernymsFile = WordNetTest.class.getResource("hypernyms.txt").getFile();
         wordNet = new WordNet(synsetFile, hypernymsFile);
     }
 
     @Test
     public void test_is_noun() {
 
         assertTrue(wordNet.isNoun("discussion"));
         assertTrue(wordNet.isNoun("parole"));
         assertFalse(wordNet.isNoun("&é'(-è)"));
     }
 
     @Test
     public void test_distance1() {
         assertEquals(2, wordNet.distance("individual", "physical_entity"));
     }
 
     @Test
     public void test_distance3() {
         assertEquals("physical_entity", wordNet.sap("individual", "physical_entity"));
         assertEquals(3, wordNet.distance("municipality", "region"));
     }
 
     @Test
     public void test_distance2() {
         assertEquals(33, wordNet.distance("Black_Plague", "black_marlin"));
         assertEquals(0, wordNet.distance("Black_Plague", "Black_Plague"));
         assertEquals("Black_Death Black_Plague", wordNet.sap("Black_Plague", "Black_Plague"));
         assertEquals(27, wordNet.distance("American_water_spaniel", "histology"));
         assertEquals(29, wordNet.distance("Brown_Swiss", "barrel_roll"));
     }
 
     @Test
     public void test_outcast() {
        Outcast outcast = new Outcast(wordNet);
         assertEquals("Turing", outcast.outcast(new String[]{"Turing", "von_Neumann"}));
        assertEquals("table", outcast.outcast("horse zebra cat bear table".split(" ")));
        assertEquals("cat", outcast.outcast("table chair bed cat".split(" ")));
        assertEquals("jail", outcast.outcast("car horse bike plane foot boat jail".split(" ")));
        
        
     }
 
 }
