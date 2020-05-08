 package com.amee.base.utils;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertTrue;
 
 public class UidGenTest {
 
     @Test
     public void shouldBe12CharsLong() {
         assertTrue(UidGen.INSTANCE_12.getUid().length() == 12);
     }
 
     @Test
     public void shouldBe16CharsLong() {
         assertTrue(UidGen.INSTANCE_16.getUid().length() == 16);
     }
 
     @Test
     public void shouldBeValid12() {
         assertTrue(UidGen.INSTANCE_12.isValid(UidGen.INSTANCE_12.getUid()));
     }
 
     @Test
     public void shouldBeValid16() {
         assertTrue(UidGen.INSTANCE_16.isValid(UidGen.INSTANCE_16.getUid()));
     }
 }
