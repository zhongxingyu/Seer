 package edu.msergey.jalg.exercises.ch5.ex09;
 
 import org.junit.Test;
import sun.plugin.dom.exception.InvalidStateException;

 import static org.junit.Assert.assertEquals;
 
 public class RecursivePostfixCalculatorTest {
     @Test
     public void test_constructor_abc() {
         new RecursivePostfixCalculator("23+");
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_constructor_empty() {
         new RecursivePostfixCalculator("");
     }
 
     @Test (expected = IllegalArgumentException.class)
     public void test_constructor_null() {
         new RecursivePostfixCalculator(null);
     }
 
     @Test
     public void test_calc_sum_7_4() {
         RecursivePostfixCalculator calculator = new RecursivePostfixCalculator("74+");
 
         int actual = calculator.calc();
 
         assertEquals(11, actual);
     }
 
     @Test
     public void test_calc_mul_2_3() {
         RecursivePostfixCalculator calculator = new RecursivePostfixCalculator("23*");
 
         int actual = calculator.calc();
 
         assertEquals(6, actual);
     }
 
     @Test
     public void test_calc_multiple_operations() {
         RecursivePostfixCalculator calculator = new RecursivePostfixCalculator("74+23*+");
 
         int actual = calculator.calc();
 
         assertEquals(17, actual);
     }
 
     @Test (expected = IllegalStateException.class)
     public void test_calc_invalid_symbols() {
         RecursivePostfixCalculator calculator = new RecursivePostfixCalculator("!");
 
         calculator.calc();
     }
 }
