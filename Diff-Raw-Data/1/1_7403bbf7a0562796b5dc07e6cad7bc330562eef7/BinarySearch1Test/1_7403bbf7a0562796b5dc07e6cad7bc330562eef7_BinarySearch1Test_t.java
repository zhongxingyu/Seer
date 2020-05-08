 package cn.sunjiachao.s7common.arithmetic.search;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import static org.junit.Assert.*;
 
 @RunWith(JUnit4.class)
 public class BinarySearch1Test {
 
 	@Test
 	public void testBinarySearch() {
 		int[] array = { 1, 12, 23, 34, 45, 66, 76, 86, 96, 1066 };
 		BinarySearch1 app = new BinarySearch1(array);
 		int result = app.search(34);
 		assertEquals(3, result);
 	}
 
 }
