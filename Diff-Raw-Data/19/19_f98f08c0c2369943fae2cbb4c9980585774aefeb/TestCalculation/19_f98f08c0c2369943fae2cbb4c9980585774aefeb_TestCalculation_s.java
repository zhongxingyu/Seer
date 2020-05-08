package CalculatorD.Test;
 import CalculatorD.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * Simple test class for the <code>Calculation</code> class.
  * @author Dustin Leavins
  */
 public class TestCalculation {
     /**
      * Main
      * @param args unused
      */
     public static void main (String [] args) {
         org.junit.runner.JUnitCore.main("CalculatorD.Test.TestCalculation");
     }
 
     @Test
     public void testMain() {
         Calculation calc = new Calculation();
         Fraction a;
         Fraction b;
         Fraction c;
         Fraction result;
 
         //---Addition Test---
         a = new Fraction(1,3);
         b = new Fraction(5,3);
        result = new Fraction(18,9);
         calc.appendNumber(a);
         calc.appendOperation(Operation.PLUS);
         calc.appendNumber(b);
         assertTrue(result.equals(calc.getValue()));
 
         //---Subtraction Test---
         calc.clear();
        result = new Fraction(-12,9);
         calc.appendNumber(a);
         calc.appendOperation(Operation.MINUS);
         calc.appendNumber(b);
         assertTrue(result.equals(calc.getValue()));
 
         //---Multiplication Test---
         calc.clear();
         result = new Fraction(5,9);
         calc.appendNumber(a);
         calc.appendOperation(Operation.MULTIPLY);
         calc.appendNumber(b);
         assertTrue(result.equals(calc.getValue()));
 
         //---Division Test---
         calc.clear();
        result = new Fraction(3, 15);
         calc.appendNumber(a);
         calc.appendOperation(Operation.DIVIDE);
         calc.appendNumber(b);
         assertTrue(result.equals(calc.getValue()));
 
         //---Mixed Tests---
         calc.clear();
         a = new Fraction(5,1);
         b = new Fraction(4,1);
         c = new Fraction(99,1);
 
         result = new Fraction(100,1);
         calc.appendNumber(a);
         calc.appendOperation(Operation.MINUS);
         calc.appendNumber(b);
         calc.appendOperation(Operation.PLUS);
         calc.appendNumber(c);
         assertTrue(result.equals(calc.getValue()));
 
         calc.clear();
         result = new Fraction(100,1);
         calc.appendNumber(a);
         calc.appendOperation(Operation.MULTIPLY);
         calc.appendOperation(Operation.MINUS);
         calc.appendNumber(b);
         calc.appendOperation(Operation.PLUS);
         calc.appendNumber(c);
         assertTrue(result.equals(calc.getValue()));
 
         calc.clear();
         result = new Fraction(103,1);
         calc.appendNumber(a);
         calc.appendNumber(b);
         calc.appendOperation(Operation.PLUS);
         calc.appendNumber(c);
         //System.out.println("5 4 + 99 = 4 + 99 = " + calc.getValue());
         assertTrue(result.equals(calc.getValue()));
     }
 }
