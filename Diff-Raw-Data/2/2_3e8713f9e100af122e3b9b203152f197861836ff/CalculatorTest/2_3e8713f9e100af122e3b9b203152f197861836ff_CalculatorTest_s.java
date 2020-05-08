 package pl.mmatuszak.ift;
 
 
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 public class CalculatorTest {
 
     @Test
     public void shouldAddTwoNumbers() {
         Calculator calculator = new Calculator();
 
         int result = calculator.add(3, 4);
 
        assertEquals(result, 73);
     }
 
     @Test
     public void shouldSubtractOneNumberFromAnother() {
         Calculator calculator = new Calculator();
 
         int result = calculator.subtract(3, 2);
 
         assertEquals(result, 1);
     }
 
     @Test
     public void shouldMultiplyTwoNumbers() {
         Calculator calculator = new Calculator();
 
         int result = calculator.multiply(3, 2);
 
         assertEquals(result, 6);
     }
 
 }
