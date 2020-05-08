 package de.tum.in.dss.psquare;
 
 import java.util.Random;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class TestPercentile {
 
 	private final float pvalue = 0.99f;
 
 	private float[] randomTestData(int factor, int values) {
 		float[] test = new float[values];
 		Random rand = new Random();
 		for (int i = 0; i < test.length; i++) {
 			test[i] = Math.abs(rand.nextFloat() * factor);
 		}
 		return test;
 	}
 
 	@Test
 	public void testAccept() {
 		PSquared psquared = new PSquared(pvalue);
 		float[] test = randomTestData(100, 10000);
 		for (float value : test) {
 			double p = psquared.accept(value);
			Assert.assertTrue(p >= 0);
 		}
 	}
 
 	private void runWithPercentile(float ptest, float[] test) {
 		PSquared psquared = new PSquared(ptest);
 		double ps = 0;
 		for (float value : test) {
 			ps = psquared.accept(value);
 		}
 
 		//
 		org.apache.commons.math3.stat.descriptive.rank.Percentile p2 = new org.apache.commons.math3.stat.descriptive.rank.Percentile(
 				ptest * 100);
 		double[] dall = new double[test.length];
 		for (int i = 0; i < test.length; i++)
 			dall[i] = test[i];
 		double apache = p2.evaluate(dall);
 
 		System.out.println(ptest + ": with " + test.length + " got: " + apache
 				+ " - " + ps);
 		double max = Math.max(apache, ps);
 		double percentage = Math.abs(apache - ps) / max;
 		Assert.assertTrue(percentage < 0.01);
 	}
 
 	@Test
 	public void test99Percentile() {
 		float[] test = randomTestData(100, 10000);
 		runWithPercentile(0.99f, test);
 	}
 
 	@Test
 	public void test90Percentile() {
 		float[] test = randomTestData(100, 10000);
 		runWithPercentile(0.90f, test);
 	}
 
 	@Test
 	public void test20Percentile() {
 		float[] test = randomTestData(100, 100000);
 		runWithPercentile(0.20f, test);
 	}
 
 	@Test
 	public void test5Percentile() {
 		float[] test = randomTestData(50, 990000);
 		runWithPercentile(0.50f, test);
 	}
 
 	@Test
 	public void test99PercentileHighValues() {
 		float[] test = randomTestData(100000, 10000);
 		runWithPercentile(0.99f, test);
 	}
 
 	@Test
 	public void test90PercentileHighValues() {
 		float[] test = randomTestData(100000, 100000);
 		runWithPercentile(0.90f, test);
 	}
 
 	@Test
 	public void test20PercentileHighValues() {
 		float[] test = randomTestData(100000, 100000);
 		runWithPercentile(0.20f, test);
 	}
 
 	@Test
 	public void test5PercentileHighValues() {
 		float[] test = randomTestData(100000, 100000);
 		runWithPercentile(0.05f, test);
 	}
 }
