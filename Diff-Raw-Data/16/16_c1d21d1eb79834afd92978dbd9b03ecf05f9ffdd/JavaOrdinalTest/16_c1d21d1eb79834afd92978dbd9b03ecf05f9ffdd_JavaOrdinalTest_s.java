 package uk.me.chrs.inflect;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static uk.me.chrs.inflect.Inflect.*;
 
 public class JavaOrdinalTest {
 
   @Test
   public void ordinalsInJava() {
     assertEquals("7th", ordinal(7));
     assertEquals("9000th", ordinal(9000l));
     assertEquals("twenty-first", ordinal("twenty-one"));
     assertEquals("seventy-first", textOrdinal(71));
    assertEquals("8 typos", count(8, "typo"));
   }
 }
