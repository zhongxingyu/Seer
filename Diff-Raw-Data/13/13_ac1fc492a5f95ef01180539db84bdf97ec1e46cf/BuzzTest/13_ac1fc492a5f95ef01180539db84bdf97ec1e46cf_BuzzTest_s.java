 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.*;
 
 public class BuzzTest {
 	@Test
 	public void _5のときはBuzz() throws Exception {
 		assertThat(FizzBuzz.buzz(5), is("Buzz")); 
 	}
 	
 	@Test
	public void _1のときは1() throws Exception {
		assertThat(FizzBuzz.fizzbuzz(1), is("1")); 
 	}
 }
