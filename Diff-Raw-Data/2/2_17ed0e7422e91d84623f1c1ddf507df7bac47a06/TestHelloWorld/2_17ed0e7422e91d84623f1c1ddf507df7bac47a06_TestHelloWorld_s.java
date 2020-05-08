 import static org.junit.Assert.*;
 
 import no.bekk.fagdag.mylyn.HelloWorld;
 
 import org.junit.Test;
 
 
 public class TestHelloWorld {
 
 	@Test
 	public void testHelloWorld() {
 		HelloWorld helloWorld = new HelloWorld();
		assertEquals("Hello World!", helloWorld.sayHello());
 	}
 	
 	
 
 }
