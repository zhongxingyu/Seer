 package com.develogical.crypto;
 
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class SubstitutionCipherTest {
 
     @Test
     public void encodesStringSubstitutingLetters() {
         String result = new SubstitutionCipher("ex", "xe").encode("the quick brown fox");
         assertThat(result, is("thx quick brown foe"));
     }
     
     @Test
     public void encodesStringSubstitutingAllOccurrencesOfLetters() {
         String result = new SubstitutionCipher("th", "av").encode("the thin thrush theme");
        assertThat(result, is("ave avin avrusv aveme"));
     }
 
     @Test
     public void decodesStringSubstitutingLetters() {
         String result = new SubstitutionCipher("ex", "xe").decode("thx quick brown foe");
         assertThat(result, is("the quick brown fox"));
     }
 }
