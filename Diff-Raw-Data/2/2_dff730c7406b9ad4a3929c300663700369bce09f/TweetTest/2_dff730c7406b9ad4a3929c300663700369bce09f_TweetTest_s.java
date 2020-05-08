 import models.Tweet;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class TweetTest extends UnitTest {
 	
 	@Test
 	public void testModelSave() {
 		
 		long count = Tweet.count();
 		Tweet t = new Tweet();
 	
		t.tweet = “my sample tweet”;
 		t.save();
 		
 		long count2 = Tweet.count();
 	
 		Assert.assertEquals(count + 1, count2);
 	}
 }
