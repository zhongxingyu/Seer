 package chordest.util;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import chordest.util.DataUtil;
 
 public class DataUtilTest {
 
 //	@Test
 	public void testShrinkSmoke() {
 		double[][] array = new double[8][];
 		array[0] = new double[] { 1, 2 };
 		array[1] = new double[] { 3, 4 };
 		array[2] = new double[] { 5, 6 };
 		array[3] = new double[] { 7, 8 };
 		array[4] = new double[] { 1, 2 };
 		array[5] = new double[] { 3, 4 };
 		array[6] = new double[] { 5, 6 };
 		array[7] = new double[] { 7, 8 };
 		double[][] s1 = DataUtil.shrink(array, 4);
 		Assert.assertEquals(2, s1.length);
 		double eps = 0.00001;
 		Assert.assertTrue(Math.abs(4 - s1[0][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[0][1]) < eps);
 		Assert.assertTrue(Math.abs(4 - s1[1][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[1][1]) < eps);
 	}
 
 //	@Test
 	public void testShrink9by4() {
 		double[][] array = new double[9][];
 		array[0] = new double[] { 1, 2 };
 		array[1] = new double[] { 3, 4 };
 		array[2] = new double[] { 5, 6 };
 		array[3] = new double[] { 7, 8 };
 		array[4] = new double[] { 1, 2 };
 		array[5] = new double[] { 3, 4 };
 		array[6] = new double[] { 5, 6 };
 		array[7] = new double[] { 7, 8 };
 		array[8] = new double[] { 1, 2 };
 		double[][] s1 = DataUtil.shrink(array, 4);
 		Assert.assertEquals(3, s1.length);
 		double eps = 0.00001;
 		Assert.assertTrue(Math.abs(4 - s1[0][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[0][1]) < eps);
 		Assert.assertTrue(Math.abs(4 - s1[1][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[1][1]) < eps);
 		Assert.assertTrue(Math.abs(1 - s1[2][0]) < eps);
 		Assert.assertTrue(Math.abs(2 - s1[2][1]) < eps);
 	}
 
 //	@Test
 	public void testShrink10by4() {
 		double[][] array = new double[10][];
 		array[0] = new double[] { 1, 2 };
 		array[1] = new double[] { 3, 4 };
 		array[2] = new double[] { 5, 6 };
 		array[3] = new double[] { 7, 8 };
 		array[4] = new double[] { 1, 2 };
 		array[5] = new double[] { 3, 4 };
 		array[6] = new double[] { 5, 6 };
 		array[7] = new double[] { 7, 8 };
 		array[8] = new double[] { 1, 2 };
 		array[9] = new double[] { 3, 4 };
 		double[][] s1 = DataUtil.shrink(array, 4);
 		Assert.assertEquals(3, s1.length);
 		double eps = 0.00001;
 		Assert.assertTrue(Math.abs(4 - s1[0][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[0][1]) < eps);
 		Assert.assertTrue(Math.abs(4 - s1[1][0]) < eps);
 		Assert.assertTrue(Math.abs(5 - s1[1][1]) < eps);
 		Assert.assertTrue(Math.abs(2 - s1[2][0]) < eps);
 		Assert.assertTrue(Math.abs(3 - s1[2][1]) < eps);
 	}
 
 	@Test
 	public void testExpandBeatTimes() {
 		double[] array = new double[] { 1, 2, 4 };
 		double[] result = DataUtil.makeMoreFrequent(array, 4);
 		double eps = 0.00001;
 		Assert.assertEquals(9, result.length);
 		Assert.assertTrue(Math.abs(result[0] - 1) < eps);
 		Assert.assertTrue(Math.abs(result[1] - 1.25) < eps);
 		Assert.assertTrue(Math.abs(result[2] - 1.5) < eps);
 		Assert.assertTrue(Math.abs(result[3] - 1.75) < eps);
 		Assert.assertTrue(Math.abs(result[4] - 2) < eps);
 		Assert.assertTrue(Math.abs(result[5] - 2.5) < eps);
 		Assert.assertTrue(Math.abs(result[6] - 3) < eps);
 		Assert.assertTrue(Math.abs(result[7] - 3.5) < eps);
 		Assert.assertTrue(Math.abs(result[8] - 4) < eps);
 	}
 
 	@Test
 	public void testReduce() {
 		double[] array = new double[] { 
 				2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1,
 				2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1,
 				2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1,
 				2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1 };
 		double[] result = DataUtil.reduce(array, 2);
 		Assert.assertEquals(24, result.length);
 		for (int i = 0; i < 24; i++) {
			Assert.assertEquals(3.2, result[i]);
 		}
 	}
 
 }
