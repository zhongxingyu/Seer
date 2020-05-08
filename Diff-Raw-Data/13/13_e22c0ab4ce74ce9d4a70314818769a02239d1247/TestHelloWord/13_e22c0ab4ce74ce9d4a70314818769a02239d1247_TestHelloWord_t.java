 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class TestHelloWord {
 	
 	@Test
 	public void HelloWordTest(){
		Assert.assertEquals("Hello Word Guti", Hello.HelloWord("Guti"));
 	}
 	@Test
 	public void HelloWordTest2(){
		Assert.assertEquals("Hello Word Thomas", Hello.HelloWord("Thomas"));
 	}
 }
