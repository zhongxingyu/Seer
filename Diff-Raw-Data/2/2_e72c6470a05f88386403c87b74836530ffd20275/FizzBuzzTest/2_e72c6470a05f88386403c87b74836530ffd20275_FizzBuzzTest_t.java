 package com.github.kpacha.jkata.fizzBuzz.test;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import com.github.kpacha.jkata.fizzBuzz.FizzBuzz;
 
 @RunWith(Parameterized.class)
 public class FizzBuzzTest extends TestCase {
 
     private Object result;
     private int number;
 
     public FizzBuzzTest(int number, Object result) {
 	this.number = number;
 	this.result = result;
     }
 
     @Test
     public void testGenerator() {
 	assertEquals(result, FizzBuzz.generate(number));
     }
 
     @Parameterized.Parameters
     public static List<Object[]> data() {
 	final Object[][] objects = { { 1, 1 }, { 2, 2 }, { 3, "Fizz" },
		{ 4, 4 }, { 5, "Buzz" } };
 	return Arrays.asList(objects);
     }
 }
