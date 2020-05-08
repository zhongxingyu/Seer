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
	public void _1のときは空白() throws Exception {
		assertThat(FizzBuzz.buzz(1), is("")); 
 	}
 }
