 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class TestHelloWord {
 	
 	@Test
 	public void HelloWordTest(){
		Assert.assertEquals("Hello Word", Hello.HelloWord("Guti"));
 	}
 	@Test
 	public void HelloWordTest2(){
		Assert.assertEquals("Hello Word by Thomas", Hello.HelloWord());
 	}
 }
