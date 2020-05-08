 package edu.ucsb.cs56.projects.misc.translate_to_secret_languages.gibberish_translator;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 /**
  * The test class englishToGibberish, to test the englishToGibberish class
  * @author Christian Rivera Cruz and Adam Kazberuk
  * @author Alex Molina
  * @version CS56, Spring 2013
  * @see englishToPigLatin
  */
 
 public class EnglishToGibberishTester
 {
     /**
     test method that translates to English from Pig Latin
        @see EnglishToGibberish#fromGibberish()
      */
    
     @Test public void testFromGibberish()
     {
 	// should return stub. This method is from another issue
        assertEquals("STUB Issue #1",EnglishToGibberish.fromGibberish("iceway"));
     }
     
     /**
     test toGibberish for a word that begins with a consonant
        @see EnglishToGibberish#toGibberish()
      */
    
     @Test public void testToGibberishConsonant()
     {
 	// should return dog with "uvug" inserted after the d
         assertEquals("duvugog",EnglishToGibberish.toGibberish("dog"));
     }
 
     /**
     test toGibberish for a word that begins with a vowel
        @see EnglishToGibberish#toGibberish()
      */
    
     @Test public void testToGibberishVowel()
     {
 	// should return dog with "uvug" inserted after the d
         assertEquals("uvugat",EnglishToGibberish.toGibberish("at"));
     }
 
     /**
     test toGibberish for a word that begins with a vowel and word that begins with a consonant
        @see EnglishToGibberish#toGibberish()
      */
    
     @Test public void testToGibberishVowelConsonant()
     {
 	// should return dog with "uvug" inserted after the d
         assertEquals("uvugat duvugog",EnglishToGibberish.toGibberish("at dog"));
     }
     
 }
