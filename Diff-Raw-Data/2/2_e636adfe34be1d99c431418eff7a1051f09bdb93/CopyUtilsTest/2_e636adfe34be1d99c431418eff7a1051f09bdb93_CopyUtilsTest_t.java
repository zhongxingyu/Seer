 package task4;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class CopyUtilsTest {
 
 	@Test
 	public void testCopy() throws Exception {
 		String src = "data/some_text.txt";
		String dst = "data/test";
 		int n = 10;
 		for (int i = 0; i < n; ++i) {
 			Thread.sleep(1000);
 			CopyUtils.copy(src, dst);
 		}
 
 	}
 }
