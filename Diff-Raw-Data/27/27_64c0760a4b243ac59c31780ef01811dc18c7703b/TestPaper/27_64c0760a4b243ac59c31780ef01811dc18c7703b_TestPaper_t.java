 package de.tum.in.dss.psquare;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class TestPaper {
 
 	// Observations reported in the paper
 	private final float[] observations = { 0.02f, 0.15f, 0.74f, 0.83f, 3.39f, 22.37f, 10.15f, 15.43f, 38.62f, 15.92f, 34.60f, 10.28f, 1.47f, 0.40f, 0.05f,
 			11.39f, 0.27f, 0.42f, 0.09f, 11.37f };
 
 	// Percentile to estimate
 	private final float pvalue = 0.5f;
 
 	@Test
 	public void testPaperCalculation() {
 		PSquared psquared = new PSquared(pvalue);
 
 		double ps = 0;
		int run = 1;
		
 		for (float value : observations) {
			System.out.println("--- observation: " + (run));
 			ps = psquared.accept(value);
 			psquared.dump();
 
 			testRun(run, psquared.n, psquared.n_desired);
 			run++;
 		}
 
 		System.out.println("estimated percentile: " + ps);
 	}
 
 	private void testRun(int run, int[] positions, float[] desired) {
 		switch (run) {
 		case 6:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(1, positions[1]);
 			Assert.assertEquals(2, positions[2]);
 			Assert.assertEquals(3, positions[3]);
 			Assert.assertEquals(5, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(1.25f, desired[1]));
 			Assert.assertTrue(test(2.5f, desired[2]));
 			Assert.assertTrue(test(3.75f, desired[3]));
 			Assert.assertTrue(test(5f, desired[4]));
 			break;
 
 		case 7:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(1, positions[1]);
 			Assert.assertEquals(2, positions[2]);
 			Assert.assertEquals(4, positions[3]);
 			Assert.assertEquals(6, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(1.5f, desired[1]));
 			Assert.assertTrue(test(3f, desired[2]));
 			Assert.assertTrue(test(4.5f, desired[3]));
 			Assert.assertTrue(test(6f, desired[4]));
 			break;
 
 		case 8:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(1, positions[1]);
 			Assert.assertEquals(3, positions[2]);
 			Assert.assertEquals(5, positions[3]);
 			Assert.assertEquals(7, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(1.75f, desired[1]));
 			Assert.assertTrue(test(3.5f, desired[2]));
 			Assert.assertTrue(test(5.25f, desired[3]));
 			Assert.assertTrue(test(7f, desired[4]));
 			break;
 
 		case 9:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(2, positions[1]);
 			Assert.assertEquals(4, positions[2]);
 			Assert.assertEquals(6, positions[3]);
 			Assert.assertEquals(8, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(2f, desired[1]));
 			Assert.assertTrue(test(4f, desired[2]));
 			Assert.assertTrue(test(6f, desired[3]));
 			Assert.assertTrue(test(8f, desired[4]));
 			break;
 
 		case 10:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(2, positions[1]);
 			Assert.assertEquals(4, positions[2]);
 			Assert.assertEquals(6, positions[3]);
 			Assert.assertEquals(9, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(2.25f, desired[1]));
 			Assert.assertTrue(test(4.5f, desired[2]));
 			Assert.assertTrue(test(6.75f, desired[3]));
 			Assert.assertTrue(test(9f, desired[4]));
 			break;
 
 		case 11:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(2, positions[1]);
 			Assert.assertEquals(5, positions[2]);
 			Assert.assertEquals(7, positions[3]);
 			Assert.assertEquals(10, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(2.5f, desired[1]));
 			Assert.assertTrue(test(5f, desired[2]));
 			Assert.assertTrue(test(7.5f, desired[3]));
 			Assert.assertTrue(test(10f, desired[4]));
 			break;
 
 		case 12:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(2, positions[1]);
 			Assert.assertEquals(5, positions[2]);
 			Assert.assertEquals(8, positions[3]);
 			Assert.assertEquals(11, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(2.75f, desired[1]));
 			Assert.assertTrue(test(5.5f, desired[2]));
 			Assert.assertTrue(test(8.25f, desired[3]));
 			Assert.assertTrue(test(11f, desired[4]));
 			break;
 
 		case 13:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(3, positions[1]);
 			Assert.assertEquals(6, positions[2]);
 			Assert.assertEquals(9, positions[3]);
 			Assert.assertEquals(12, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(3f, desired[1]));
 			Assert.assertTrue(test(6f, desired[2]));
 			Assert.assertTrue(test(9f, desired[3]));
 			Assert.assertTrue(test(12f, desired[4]));
 			break;
 
 		case 14:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(4, positions[1]);
 			Assert.assertEquals(7, positions[2]);
 			Assert.assertEquals(10, positions[3]);
 			Assert.assertEquals(13, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(3.25f, desired[1]));
 			Assert.assertTrue(test(6.5f, desired[2]));
 			Assert.assertTrue(test(9.75f, desired[3]));
 			Assert.assertTrue(test(13f, desired[4]));
 			break;
 
 		case 15:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(4, positions[1]);
 			Assert.assertEquals(7, positions[2]);
 			Assert.assertEquals(11, positions[3]);
 			Assert.assertEquals(14, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(3.5f, desired[1]));
 			Assert.assertTrue(test(7f, desired[2]));
 			Assert.assertTrue(test(10.5f, desired[3]));
 			Assert.assertTrue(test(14f, desired[4]));
 			break;
 
 		case 16:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(4, positions[1]);
 			Assert.assertEquals(7, positions[2]);
 			Assert.assertEquals(12, positions[3]);
 			Assert.assertEquals(15, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(3.75f, desired[1]));
 			Assert.assertTrue(test(7.5f, desired[2]));
 			Assert.assertTrue(test(11.25f, desired[3]));
 			Assert.assertTrue(test(15f, desired[4]));
 			break;
 
 		case 17:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(4, positions[1]);
 			Assert.assertEquals(8, positions[2]);
 			Assert.assertEquals(12, positions[3]);
 			Assert.assertEquals(16, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(4f, desired[1]));
 			Assert.assertTrue(test(8f, desired[2]));
 			Assert.assertTrue(test(12f, desired[3]));
 			Assert.assertTrue(test(16f, desired[4]));
 			break;
 
 		case 18:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(5, positions[1]);
 			Assert.assertEquals(9, positions[2]);
 			Assert.assertEquals(13, positions[3]);
 			Assert.assertEquals(17, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(4.25f, desired[1]));
 			Assert.assertTrue(test(8.5f, desired[2]));
 			Assert.assertTrue(test(12.75f, desired[3]));
 			Assert.assertTrue(test(17f, desired[4]));
 			break;
 
 		case 19:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(5, positions[1]);
 			Assert.assertEquals(9, positions[2]);
 			Assert.assertEquals(14, positions[3]);
 			Assert.assertEquals(18, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(4.5f, desired[1]));
 			Assert.assertTrue(test(9f, desired[2]));
 			Assert.assertTrue(test(13.5f, desired[3]));
 			Assert.assertTrue(test(18f, desired[4]));
 			break;
 
 		case 20:
 			Assert.assertEquals(0, positions[0]);
 			Assert.assertEquals(5, positions[1]);
 			Assert.assertEquals(9, positions[2]);
 			Assert.assertEquals(15, positions[3]);
 			Assert.assertEquals(19, positions[4]);
 
 			Assert.assertTrue(test(0f, desired[0]));
 			Assert.assertTrue(test(4.75f, desired[1]));
 			Assert.assertTrue(test(9.5f, desired[2]));
 			Assert.assertTrue(test(14.25f, desired[3]));
 			Assert.assertTrue(test(19f, desired[4]));
 			break;
 
 		}
 	}
 
 	private boolean test(float expected, float value) {
 		float diff = Math.abs(value - expected);
 		return diff < 0.00001;
 	}
 }
