 package learn;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class LearnTest {
 
 	@Test
 	public void test() {
 		Assert.assertTrue(true); //all good
		throw new RuntimeException();
 	}
 }
