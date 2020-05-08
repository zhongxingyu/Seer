 package net.gageot.coverage;
 
import static org.junit.Assert.*;

import org.junit.*;
 
 public class ImplTest {
 	@Test
 	public void should_say_hello() {
 		String greeting = new Impl().sayHello();
 
		assertEquals("Hello", greeting);
 	}
 }
