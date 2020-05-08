 import org.junit.Test;
 
 /**
  * Various tests from the project template.
  */
 public class TemplateTests extends AbstractTest {
 
 	public static void main(String[] args) {
 		// Call test1 so that the pointer analysis can work
 		test1();
 
 		try {
 			test4();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Safe
 	public static void test1() {
 		int[] code = new int[7];
 		for (int i = 0; i < 7; ++i) {
 			code[i] = (i * 3) % 7;
 		}
 		test3(code);
 		test2(code);
 	}
 
 	@Test
 	public void _test1() {
 		assertAnalysis("test1");
 	}
 
	@Safe
 	public static void test2(int[] code) {
 		// The pointer analysis should be able to tell you that code was
 		// allocated with size 7 and prove this method.
 		int sum = 0;
 		for (int i = 7; i >= 0; --i) {
 			sum += code[i];
 		}
 		System.out.println("Sum = " + sum);
 	}
 
 	@Test
 	public void _test2() {
 		assertAnalysis("test2");
 	}
 
 	@Unsafe
 	public static void test3(int[] code) {
 		int[] revcode = new int[7];
 		for (int i = 0; i < 7; ++i) {
 			// For this code, the analysis may be imprecise. We may not be able
 			// to tell if the contents of an array fits into bounds.
 			revcode[code[i]] = i;
 		}
 	}
 
 	@Test
 	public void _test3() {
 		assertAnalysis("test3");
 	}
 
 	@Unsafe
 	public static void test4() {
 		int[] code = new int[7];
 		for (int i = 0; i < 8; ++i) {
 			code[i] = i;
 		}
 	}
 
 	@Test
 	public void _test4() {
 		assertAnalysis("test4");
 	}
 
 }
