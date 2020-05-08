 package com.interview.binary;
 
 import org.junit.Assert;
 import org.testng.annotations.Test;
 
 /**
  * @author zhouqianli
  */
 @Test
 public class BinaryTest {
     public void testAddEmptyValues() {
         String r = new Binary("").add(null).toString();
         Assert.assertEquals("0", r);
     }
    public void testAddInvalidValues() {
        String r = new Binary("123").add(null).add("2").add("00221").toString();
        Assert.assertEquals("101", r);
    }
 
     public void testAddSingleValue_1_0() {
         String r = new Binary("1").add("0").toString();
         Assert.assertEquals("1", r);
     }
 
     public void testAddSingleValue_0_1() {
         String r = new Binary("0").add("1").toString();
         Assert.assertEquals("1", r);
     }
 
     public void testAddSingleValue_0_0() {
         String r = new Binary("0").add("0").toString();
         Assert.assertEquals("0", r);
     }
 
     public void testAddSingleValue_1_1() {
         String r = new Binary("1").add("1").toString();
         Assert.assertEquals("10", r);
     }
 
     public void testAddValues_110_100() {
         String r = new Binary("110").add("100").toString();
         Assert.assertEquals("1010", r);
     }
     public void testAddValues_11110_1010() {
         String r = new Binary("11110").add("1010").add("01").toString();
         Assert.assertEquals("101001", r);
     }
 
 }
