 package io.jacinto.test.model;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import io.jacinto.model.types.BooleanValue;
 import org.junit.Test;
 
 /**
  * Test case for BooleanValue, simple just to make sure that the model type works.
  *
  * @author Edgar Rodriguez <a href="mailto:edgar.rd@gmail.com>edgar.rd_at_gmail.com</a>
  * @since 0.0.1
  */
 public class BooleanValueTest {
 
     public BooleanValueTest() {}
 
     @Test
     public void testBooleanValueCreate() {
         BooleanValue aBooleanVal = new BooleanValue(true);
        Boolean aBool = new Boolean(aBooleanVal.getValue());
         assertTrue(aBool);
     }
 
     @Test
     public void testBooleanEquals() {
         BooleanValue aValue1 = new BooleanValue(false);
         BooleanValue aValue2 = new BooleanValue(true);
 
         assertFalse(aValue1.equals(aValue2));
 
         aValue2 = new BooleanValue(false);
         assertTrue(aValue1.equals(aValue2));
     }
 
 }
