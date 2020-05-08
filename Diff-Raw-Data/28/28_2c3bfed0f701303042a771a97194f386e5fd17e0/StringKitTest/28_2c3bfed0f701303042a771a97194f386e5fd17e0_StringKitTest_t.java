 package q.util;
 
 import java.util.Arrays;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Mar 10, 2011
  * 
  */
 public class StringKitTest {
 
 	@Test
 	public void testCapitalize() {
 		Assert.assertEquals(null, StringKit.capitalize(null));
 		Assert.assertEquals("", StringKit.capitalize(""));
 		Assert.assertEquals(" ", StringKit.capitalize(" "));
 		Assert.assertEquals("Test", StringKit.capitalize("test"));
 		Assert.assertEquals("Test", StringKit.capitalize("Test"));
 		Assert.assertEquals("T", StringKit.capitalize("t"));
 		Assert.assertEquals("T", StringKit.capitalize("T"));
 		Assert.assertEquals("ATest", StringKit.capitalize("aTest"));
 		Assert.assertEquals("ATest", StringKit.capitalize("ATest"));
 	}
 
 	/**
 	 * StringKit.capitalize() is 5 times speed than org.apache.commons.lang.StringUtils.caplitalize()
 	 */
 	private void testCapitalizeSpeed() {
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < 10000000; i++)
 			StringUtils.capitalize("Tsdaf");
 		long end = System.currentTimeMillis();
 		System.out.println(end - start);
 
 		start = System.currentTimeMillis();
 		for (int i = 0; i < 10000000; i++)
 			StringKit.capitalize("Tsdaf");
 		end = System.currentTimeMillis();
 		System.out.println(end - start);
 	}
 
 	@Test
 	public void testEscapeJson() {
		Assert.assertEquals("\\r\\n\\\"", StringKit.escapeJson("\r\n\""));
	}

	public void testEscapeJson2() {
		String src = "http://qimg.net/w/sx/3antepmkc9t.jpg";
		Assert.assertEquals("http://qimg.net/w/sx/3antepmkc9t.jpg",StringKit.escapeJson(src));
 	}
 
 	@Test
 	public void testSplit() {
 		String[] items = StringKit.split("a b c", ' ');
 		Assert.assertTrue(Arrays.equals(items, new String[] { "a", "b", "c" }));
 	}
 
 	@Test
 	public void testSplitMax() {
 		String[] items = StringKit.split("a b c", ' ', 2);
 		Assert.assertEquals(Arrays.toString(new String[] { "a", "b c" }), Arrays.toString(items));
 	}
 
 	public static void main(String[] args) {
		// new StringKitTest().testCapitalizeSpeed();
		System.out.println((int) '\r');
		System.out.println((int) '\n');
 	}
 }
