 /**
  *
  */
 package features.mouse;
 
 import static org.junit.Assert.*;
 import features.mouse.FizzBuzz;
 
 import org.junit.Test;
 
 /**
  * @author reimei
  *
  */
 public class FizzBuzzTest {
 
 
 
 	// 3で割り切れる場合は "Fizz"を返すこと
 	@Test
 	public void testFizzBuzz3で割り切れる場合() {
 		FizzBuzz target = new FizzBuzz();
 		assertEquals("Fizz", target.fizzBuzz(6)) ;
 
 	}
 	// 5で割り切れる場合は "Buzz"を返すこと
 	@Test
 	public void testFizzBuzz5で割り切れる場合() {
 		FizzBuzz target = new FizzBuzz();
 		assertEquals("Buzz", target.fizzBuzz(10)) ;
 
 	}
 
 	// 3と5で割り切れる場合は"FizzBuzz"を返すこと
 	@Test
 	public void testFizzBuzz_3と5で割り切れる場合() {
 		FizzBuzz target = new FizzBuzz();
 		assertEquals("FizzBuzz", target.fizzBuzz(15)) ;
 	}
 
 	// 3でも5でも割り切れない場合は、その数字 を文字列として返すこと
 	@Test
 	public void testFizzBuzz_3でも5でも割り切れない場合() {
 		FizzBuzz target = new FizzBuzz();
 		assertEquals("13", target.fizzBuzz(13)) ;
 	}
 
 
 
 	// 指定された数字が０の場合、３でも５でも割り切れるのでFizzBuzzを返すこと
 	@Test
 	public void testFizzBuzz_3と5で割り切れる場合０の場合() {
 		FizzBuzz target = new FizzBuzz();
		assertEquals("0", target.fizzBuzz(0)) ;
 	}
 
 
 	// -15も３でも５でも割り切れるのでFizzBuzzを返すこと
 	@Test
 	public void testFizzBuzz_minus15も３でも５でも割り切れる() {
 		FizzBuzz target = new FizzBuzz();
		assertEquals("-15", target.fizzBuzz(-15)) ;
 	}
 //
 //	// intをあふれる場合
 //	@Test
 //	public void testFizzBuzz_intをあふれる場合() {
 //		FizzBuzz target = new FizzBuzz();
 //		assertEquals("FizzBuzz", target.fizzBuzz(-15)) ;
 //	}
 
 }
