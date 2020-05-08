 package com.huskycode.jpaquery.util;
 
 import org.junit.Test;
 
 import java.util.Random;
 
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 
 /** */
 public class RandomizerTest {
 
     private Random any = new Random();
     private static final int MAX_LENGTH = 100;
     
     @Test
     public void testGetString() {
        assertThat(Randomizer.getString(),instanceOf(String.class));
          
         int length = Math.abs(any.nextInt(MAX_LENGTH));
         String expected = Randomizer.getString(length);
 
         assertThat(expected,instanceOf(String.class));
         assertThat(expected.length(), equalTo(length));
     }
 
 
 
     @Test
     public void testGetAsciiString() {
        assertThat(Randomizer.getAsciiString(),instanceOf(String.class));

         int length = Math.abs(any.nextInt(MAX_LENGTH));
         String expected = Randomizer.getAsciiString(length);
 
         assertThat(expected,instanceOf(String.class));
         assertThat(expected.length(), equalTo(length));
 
         length = 0;
         expected = "";
         assertEquals(expected, "");
     }
 
     @Test
     public void testGetInt() {
         assertThat(Randomizer.getInt(),instanceOf(Integer.class));
     }
 
     @Test
     public void testGetNonNegativeInt() {
         int expected = Randomizer.getNonNegativeInt();
         assertThat(expected,instanceOf(Integer.class));
         assertTrue(expected >= 0);
     }
 
     @Test
     public void testGetLong() {
         assertThat(Randomizer.getLong(),instanceOf(Long.class));
     }
 
     @Test
     public void testGetNonNegativeLong() {
         long expected = Randomizer.getNonNegativeLong();
         assertThat(expected,instanceOf(Long.class));
         assertTrue(expected >= 0);
     }
 
     @Test
     public void testGetDouble() {
         assertThat(Randomizer.getDouble(),instanceOf(Double.class));
     }
 
     @Test
     public void testGetBoolean() {
         assertThat(Randomizer.getBoolean(),instanceOf(Boolean.class));
     }
 }
