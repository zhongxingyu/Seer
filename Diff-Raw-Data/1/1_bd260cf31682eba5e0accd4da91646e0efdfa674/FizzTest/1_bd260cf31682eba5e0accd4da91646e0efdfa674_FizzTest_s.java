 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.*;
 
 public class FizzTest {
 	@Test
 	public void _3のときはFizz() throws Exception {
 		assertThat(FizzBuzz.fizz(3), is("Fizz")); 
 	}
 	
	@Ignore
 	@Test
 	public void _1のときは1() throws Exception {
 		assertThat(FizzBuzz.fizzbuzz(1), is("1")); 
 	}
 }
