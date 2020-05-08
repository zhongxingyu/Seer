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

    @Test
    public void shouldUseSeparators() {
        UidGen uidGen = new UidGen("-", 6, 2, 36);
        String uid = uidGen.getUid();
        System.out.println(uid);
        assertTrue(uid.charAt(2) == '-');
        assertTrue(uid.charAt(5) == '-');
        assertTrue(uid.charAt(8) == '-');
        assertTrue(uid.charAt(11) == '-');
        assertTrue(uid.charAt(14) == '-');
    }
 }
