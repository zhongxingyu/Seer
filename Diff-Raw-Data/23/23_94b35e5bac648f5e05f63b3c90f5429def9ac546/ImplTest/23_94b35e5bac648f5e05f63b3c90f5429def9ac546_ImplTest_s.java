 package net.gageot.coverage;
 
import junit.framework.Assert;
import org.junit.Test;
 
 public class ImplTest {
 	@Test
 	public void should_say_hello() {
 		String greeting = new Impl().sayHello();
 
		Assert.assertEquals("Hello", greeting);
 	}
 }
