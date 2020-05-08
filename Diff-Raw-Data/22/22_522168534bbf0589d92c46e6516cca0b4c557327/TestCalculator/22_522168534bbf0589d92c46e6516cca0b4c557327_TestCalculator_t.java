 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.testcalculator;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import ws.calculator.ArithmeticException_Exception;
 
 /**
  *
  * @author Wojtek
  */
 public class TestCalculator {
 
     public TestCalculator() {
     }
 
     @Test
     public void testAdd() {
         ws.calculator.CalculatorServiceService service = new ws.calculator.CalculatorServiceService();
         ws.calculator.CalculatorService port = service.getCalculatorServicePort();
 
         int arg0 = 2;
         int arg1 = 2;
 
         int result = port.add(arg0, arg1);
         assertEquals(4, result);
 
     }
 
     @Test
     public void testSubtract() {
         ws.calculator.CalculatorServiceService service = new ws.calculator.CalculatorServiceService();
         ws.calculator.CalculatorService port = service.getCalculatorServicePort();
 
         int arg0 = 2;
         int arg1 = 2;
 
         int result = port.subtract(arg0, arg1);
         assertEquals(0, result);
     }
 
     @Test
     public void testMultiply() {
         ws.calculator.CalculatorServiceService service = new ws.calculator.CalculatorServiceService();
         ws.calculator.CalculatorService port = service.getCalculatorServicePort();
 
         int arg0 = 3;
         int arg1 = 3;
 
         int result = port.multiply(arg0, arg1);
         assertEquals(9, result);
     }
 
     @Test
     public void testDivide() {
         try {
             ws.calculator.CalculatorServiceService service = new ws.calculator.CalculatorServiceService();
             ws.calculator.CalculatorService port = service.getCalculatorServicePort();
 
             int arg0 = 3;
             int arg1 = 1;
 
             int result = port.divide(arg0, arg1);
             assertEquals(3, result);
         } catch (ArithmeticException_Exception ex) {
             Logger.getLogger(TestCalculator.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
    @Test//(expected = ArithmeticException.class)
    public void testDivideByZero() throws ArithmeticException_Exception {
         ws.calculator.CalculatorServiceService service = new ws.calculator.CalculatorServiceService();
         ws.calculator.CalculatorService port = service.getCalculatorServicePort();
 
         int arg0 = 3;
         int arg1 = 0;
 
        int result = port.divide(arg0, arg1);
     }
 }
 
 
