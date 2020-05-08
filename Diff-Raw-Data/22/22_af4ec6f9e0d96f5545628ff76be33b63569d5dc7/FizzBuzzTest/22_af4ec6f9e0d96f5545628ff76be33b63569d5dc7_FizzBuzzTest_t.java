 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class FizzBuzzTest {
 	
 	@Before
 	public void before() {
 	}
 	
 	@Test
 	public void _3のときはFizz() throws Exception {
		assertThat(FizzBuzz.fizzbuzz(3), is("Fizz")); 
 	}
 	
 	@Test
 	public void _5のときはBuzz() throws Exception {
		assertThat(FizzBuzz.fizzbuzz(5), is("Buzz")); 
 	}
 	
 	@Test
 	public void _15のときはFizzBuzz() throws Exception {
		assertThat(FizzBuzz.fizzbuzz(15), is("FizzBuzz")); 
 	}
 	
 	@Test
 	public void _1のときは1() throws Exception {
		assertThat(FizzBuzz.fizzbuzz(1), is("1")); 
 	}
 	
 }
