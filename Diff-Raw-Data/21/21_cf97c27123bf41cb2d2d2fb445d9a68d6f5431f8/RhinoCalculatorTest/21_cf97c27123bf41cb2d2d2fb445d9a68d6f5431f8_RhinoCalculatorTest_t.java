 package net.binout.codestory2013.calculator;
 
 import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 public class RhinoCalculatorTest {
 
     private RhinoCalculator calculator = new RhinoCalculator();
 
     @DataProvider
     public Object[][] operations() {
         return new Object[][] {
                 new String[] {"1+1",  "2"},
                new String[] {"(1+2)/2",  "1.5"},
        };
     }
 
     @Test(dataProvider = "operations")
     public void should_compute_simple_add(String op, String expected) {
         String result = calculator.calculate(op);
         Assert.assertNotNull(result);
         Assert.assertEquals(result, expected);
     }
 }
