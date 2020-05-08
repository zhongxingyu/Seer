 package test.java.utils;
 
 import main.java.utils.*;
 
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class CalculadoraTest {
   private static Calculadora calc;
  
   @BeforeClass
   public void beforeClass() {
 	  calc = new Calculadora();
   }
 
   @Test
   public void shouldSumTwoNumbers(){
 	  System.out.println ("CalculadoraTest.shouldSumTwoNumbers");
 	  int result = calc.soma(1, 3);
 	  Assert.assertEquals(result,4);
   }
   
   @Test
   public void shouldSumTwoNumbers2(){
 	  System.out.println ("CalculadoraTest.shouldSumTwoNumbers2");
 	  int result = calc.soma(1, 3);
 	  Assert.assertEquals(result,4);
   }
   
   @Test
   public void shouldSumTwoNumbers3(){
 	  System.out.println ("CalculadoraTest.shouldSumTwoNumbers3");
 	  int result = calc.soma(1, 3);
 	  Assert.assertEquals(result,4);
   }
   
   @Test
   public void shouldNotPass(){
 	  System.out.println ("CalculadoraTest.shouldNotPass");
 	  int result = calc.soma(1, 1);
	  Assert.assertEquals(result,30);
   }
   
   @Test
   public void shouldSumBigNumbers(){
 	  System.out.println ("CalculadoraTest.shouldSumBigNumbers");
 	  int result = calc.soma(1000, 3000);
 	  Assert.assertEquals(result,4000);
   }
   
   @Test
   public void shouldSumNegativeNumbers(){
 	  System.out.println ("CalculadoraTest.shouldSumNegativeNumbers");
 	  int result = calc.soma(-34, -16);
 	  Assert.assertEquals(result,-50);
   }
 }
