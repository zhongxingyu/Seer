 package org.blh.core.uncategorized;
 
 import org.blh.core.unit.DoubleUnit;
 import org.blh.recipe.formulas.Formula;
 import org.blh.recipe.uncategorized.FullContext;
 import org.blh.recipe.uncategorized.InputtedOrCalculatedValue;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author thinner
  */
 public class InputtedOrCalculatedValueTest {
 
     @Test
     public void testInputConstructor() {
        InputtedOrCalculatedValue<DoubleUnit> v = new InputtedOrCalculatedValue<>(new DoubleUnit(2d) {});
 
         Assert.assertTrue(v.isInputted());
         Assert.assertEquals(2d, v.value().value(), 0);
     }
 
     @Test(expected = NullPointerException.class)
     public void testInputConstructorNull() {
         new InputtedOrCalculatedValue<>(null);
     }
 
     @Test
     public void testCalculatedConstructor() {
         Formula<DoubleUnit> f = new Formula<DoubleUnit>() {
 
             @Override
             public DoubleUnit calc(FullContext context) {
                 return new DoubleUnit(1) {};
             }
         };
         FullContext context = new FullContext();
 
         InputtedOrCalculatedValue<DoubleUnit> v = new InputtedOrCalculatedValue<>(f, context);
 
         Assert.assertFalse(v.isInputted());
         Assert.assertEquals(1d, v.value().value(), 0);
     }
 
     @Test(expected = NullPointerException.class)
     public void testSetValueNull() {
         InputtedOrCalculatedValue<DoubleUnit> v = new InputtedOrCalculatedValue<DoubleUnit>(new DoubleUnit(2) {});
         v.setValue(null);
     }
 
     @Test
     public void testSetValueFromInputted() {
         InputtedOrCalculatedValue<DoubleUnit> v = new InputtedOrCalculatedValue<DoubleUnit>(new DoubleUnit(2d) {});
         v.setValue(new DoubleUnit(3d) {});
 
         Assert.assertTrue(v.isInputted());
         Assert.assertEquals(3d, v.value().value(), 0);
     }
 
     @Test
     public void testSetValueFromCalculated() {
         Formula<DoubleUnit> f = new Formula<DoubleUnit>() {
 
             @Override
             public DoubleUnit calc(FullContext context) {
                 return new DoubleUnit(1) {};
             }
         };
         FullContext context = new FullContext();
         InputtedOrCalculatedValue<DoubleUnit> v = new InputtedOrCalculatedValue<>(f, context);
 
         v.setValue(new DoubleUnit(3d) {});
 
         Assert.assertTrue(v.isInputted());
         Assert.assertEquals(3d, v.value().value(), 0);
     }
 }
