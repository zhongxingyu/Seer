package edu.msergey.jalg.exercises.ch5.eqx38;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 public class MaxInt64FibonacciTest {
     @Test
     public void test_evalCount() {
         int actual = MaxInt64Fibonacci.evalCount();
         assertEquals(91, actual);
     }
 }
