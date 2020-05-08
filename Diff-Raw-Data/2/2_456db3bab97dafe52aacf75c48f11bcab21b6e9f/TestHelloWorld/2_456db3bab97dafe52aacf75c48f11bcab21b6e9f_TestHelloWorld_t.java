 package no.bekk.fagdag.mylyn;
 import static org.junit.Assert.*;
 
 import no.bekk.fagdag.mylyn.App;
 
 import org.junit.Test;
 
 
 public class TestHelloWorld {
 
 	@Test
 	public void testHelloWorld() {
 		App helloWorld = new App();
		assertEquals("Halla Balla!", helloWorld.sayHello());
 	}
 }
