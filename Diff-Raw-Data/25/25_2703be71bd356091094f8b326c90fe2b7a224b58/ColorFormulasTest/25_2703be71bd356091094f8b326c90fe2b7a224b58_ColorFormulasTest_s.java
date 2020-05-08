 package test.blh.core.formulas.color;
 
 import blh.core.formulas.Formula;
 import blh.core.formulas.color.Daniels;
 import blh.core.formulas.color.Morey;
 import blh.core.formulas.color.Mosher;
 import blh.core.uncategorized.FullContext;
 import blh.core.uncategorized.MeasuredOrCalculatedValue;
 import blh.core.units.color.ColorPotential;
 import blh.core.units.color.EBC;
 import blh.core.units.color.Lovibond;
 import blh.core.units.color.MaltColorUnit;
 import blh.core.units.volume.Gallons;
 import blh.core.units.weight.Lbs;
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Created by Erik Larkö at 6/28/13 5:20 PM
  */
 public class ColorFormulasTest {
 
     @Test
     public void danielsTest() {
         Daniels f = new Daniels();
         double mcu = 12;
        EBC expectedEBCForMCU12 = new EBC(0);
         doAssert(f, mcu, expectedEBCForMCU12);
     }
 
     @Test
     public void moreyTest() {
         Morey f = new Morey();
         double mcu = 12;
        EBC expectedEBCForMCU12 = new EBC(0);
         doAssert(f, mcu, expectedEBCForMCU12);
     }
 
     @Test
     public void mosherTest() {
         Mosher f = new Mosher();
         double mcu = 12;
        EBC expectedEBCForMCU12 = new EBC(0);
         doAssert(f, mcu, expectedEBCForMCU12);
     }
 
 
     private void doAssert(Formula<EBC> f, double mcuValue, EBC expected) {
         MaltColorUnit mcu = maltColorUnitValueOf(mcuValue);
         FullContext context = new FullContext();
         context.maltColorUnit = new MeasuredOrCalculatedValue<MaltColorUnit>(mcu);
 
         EBC actual = f.calc(context);
 
        Assert.assertEquals(actual.value(), expected.value(), 0.0001);
     }
 
 
     private MaltColorUnit maltColorUnitValueOf(double mcuValue) {
         return new MaltColorUnit(new ColorPotential(new Lovibond(mcuValue), new Lbs(1)), new Gallons(1));
     }
 }
