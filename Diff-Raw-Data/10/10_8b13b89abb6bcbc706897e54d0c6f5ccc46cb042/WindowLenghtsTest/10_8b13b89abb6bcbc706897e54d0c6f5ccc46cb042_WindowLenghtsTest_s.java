 package experimental;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import chordest.transform.CQConstants;
 import chordest.transform.ScaleInfo;
 
 public class WindowLenghtsTest {
 
 	@Test
 	public void testWindowLengths() {
 		CQConstants cqConstants = CQConstants.getInstance(44100,
 				new ScaleInfo(7, 12), 440, -45);
		int windowSize = cqConstants.getWindowLengthForComponent(0) + 1;
 		Assert.assertTrue(windowSize > 100);
 		System.out.println(windowSize);
 	}
 
 }
