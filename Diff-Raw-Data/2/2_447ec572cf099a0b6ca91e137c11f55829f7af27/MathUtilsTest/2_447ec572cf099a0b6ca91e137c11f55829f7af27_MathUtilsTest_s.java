 package com.telstra.webtest.utils;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class MathUtilsTest {
     @Test
     public void should_round_100_to_100() {
        assertEquals(100.0, MathUtils.round(200));
     }
 
     @Test
     public void should_round_1_2222_to_1_22() {
         assertEquals(1.22, MathUtils.round(1.2222));
     }
 }
