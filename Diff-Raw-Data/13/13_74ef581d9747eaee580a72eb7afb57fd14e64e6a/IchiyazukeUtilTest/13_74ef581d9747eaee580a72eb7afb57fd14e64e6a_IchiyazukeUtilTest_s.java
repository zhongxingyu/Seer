 package util;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class IchiyazukeUtilTest {
 
 	@Test
 	public void testSplitCharAndTex() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testSplitCharAndTex2() {
 		//String test = "次の値を求めなさい。\" sin{15^{circ}} '";
 		String test = "次の値を求めなさい。\" sin{15^{circ}}\'次の値を求めなさい。\" sin{15^{circ}} \'";
 		//String expectation = "期待値ほげほげ";
 
 		IchiyazukeUtil ichiyazukeUtil = new IchiyazukeUtil();
		String result = ichiyazukeUtil.splitCharAndTex(test);
 		//assertEquals("結果と期待値が合いません.", expectation, test);
 
 		System.out.println(result);
 	}
 }
