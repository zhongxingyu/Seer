 package org.apache.pig.test.utils;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 public class TestDataType {
 
     @Test
     public void testDataType() {
         assertEquals('i', DataType.INT.character());
         assertEquals(DataType.INT, DataType.fromChar('i'));
         assertEquals(DataType.INT, DataType.fromInt(105));
         try {
             DataType.fromChar('x');
             fail("Exception expected");
         } catch (IllegalArgumentException e) {
         }
         assertNull(DataType.fromInt(25));
     }



 }
