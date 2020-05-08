 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test.blh.core.units.pressure;
 
 import blh.core.units.pressure.Bar;
 import blh.core.units.pressure.BarA;
 import blh.core.units.pressure.PSI;
 import blh.core.units.pressure.PSIA;
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author thinner
  */
 public class PSIATest {
     
     @Test
     public void testDouble() {
         PSIA actual = new PSIA(2);
         double expected = 2;
         
         Assert.assertEquals(expected, actual.value());
     }
     
     @Test
     public void testPSI() {
         PSIA actual = new PSIA(new PSI(2));
         PSIA expected = new PSIA(2 + PSI.CONVERSION_UNIT);
         
         Assert.assertEquals(expected.value(), actual.value());
     }
 
     @Test
     public void testBar() {
         PSIA actual = new PSIA(new Bar(1));
         PSIA expected = new PSIA(2 * PSI.CONVERSION_UNIT);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
     
     @Test
     public void testBarA() {
         PSIA actual = new PSIA(new BarA(1));
        PSIA expected = new PSIA(2 * PSI.CONVERSION_UNIT);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
 
     @Test
     public void testToPSI() {
         PSI actual = new PSIA(3 + PSI.CONVERSION_UNIT).toPSI();
         PSI expected = new PSI(3);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
     
     @Test
     public void testToBar() {
         Bar actual = new PSIA(3 + PSI.CONVERSION_UNIT).toBar();
         Bar expected = new Bar(1.206842719);
         
         Assert.assertEquals(expected.value(), actual.value(), 0.00001);
     }
 }
