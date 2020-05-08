 package travis;
 
 import java.util.*;
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class VectorTest {
 	
 	// increase for longer runtimes and more thorough stochastic tests
 	// (mainly used for scaling up the time taken for benchmarks though)
 	// runtime should increase linearly in this number
 	private final int thoroughness = 100;
 	private final Random rand = new Random(9001);
 	
 	@Test
 	public void bitHacksTests() {
 		int n = 250 * thoroughness, k = 25;
 		for(int i=0; i<n; i++) {
 			
 			int tag = rand.nextInt(k);
 			int idx = rand.nextInt(k);
 			long packed = Vector.pack(tag, idx);
 			int unpackedTag = Vector.unpackTag(packed);
 			int unpackedIdx = Vector.unpackIndex(packed);
 			
 			//System.out.printf("tag=%s idx=%s packed=%s unpackedTag=%s unpackedIdx=%s\n",
 			//		Integer.toBinaryString(tag), Integer.toBinaryString(idx), Long.toBinaryString(packed),
 			//		Integer.toBinaryString(unpackedTag), Integer.toBinaryString(unpackedIdx));
 			
 			//System.out.printf("mask=%s\n", Long.toBinaryString((1l<<32)-1));
 			
 			Assert.assertEquals(tag, unpackedTag);
 			Assert.assertEquals(idx, unpackedIdx);
 		}
 	}
 	
 	@Test
 	public void basicTests() {
 		int size = 50;
 		Vector d = new Vector(size);
 		d.add(0, 1d);
 		d.add(2, -2d);
 		d.add(2, 4d);
 		Assert.assertEquals(1d, d.get(0));
 		Assert.assertEquals(0d, d.get(1));
 		Assert.assertEquals(2d, d.get(2));
 		
 		d.clear();
 		Vector s = Vector.sparse(false);
 		int t = 50 * thoroughness;
 		for(int n=1; n<=1000; n*=10) {
 			for(int iter=0; iter<t; iter++) {
 				for(int i=0; i<n; i++) {
 					int idx = rand.nextInt(size);
 					double val = rand.nextInt(10) - 5;
 					d.add(idx, val);
 					s.add(idx, val);
 				}
 				for(int i=0; i<size; i++)
 					Assert.assertEquals(d.get(i), s.get(i));
 
 				int dl0 = d.l0Norm();
 				int sl0 = s.l0Norm();
 				if(dl0 != sl0)
 					System.out.printf("sparse-l0=%d dense-l0=%d\n", s.l0Norm(), d.l0Norm());
 
 				Assert.assertEquals(d.l0Norm(), s.l0Norm());
 				Assert.assertEquals(d.l2Norm(), s.l2Norm());
 				Assert.assertEquals(d.l1Norm(), s.l1Norm());
 				Assert.assertEquals(d.lInfNorm(), s.lInfNorm());
 				
 				d.clear();
 				s.clear();
 				Assert.assertEquals(0, d.l0Norm());
 				Assert.assertEquals(0d, d.l1Norm());
 				Assert.assertEquals(0d, d.l2Norm());
 				Assert.assertEquals(0d, d.lInfNorm());
 				Assert.assertEquals(0, s.l0Norm());
 				Assert.assertEquals(0d, s.l1Norm());
 				Assert.assertEquals(0d, s.l2Norm());
 				Assert.assertEquals(0d, s.lInfNorm());
 			}
 		}
 	}
 
 	@Test
 	public void normTests() {
 		Vector d = new Vector(10);
 		d.add(0, 1d);
 		d.add(2, -2d);
 		Assert.assertEquals(Math.sqrt(5d), d.l2Norm());
 		Assert.assertEquals(3d, d.l1Norm());
 		Assert.assertEquals(2d, d.lInfNorm());
 		Assert.assertEquals(2, d.l0Norm());
 		
 		Vector s = Vector.sparse(false);
 		s.add(0, 1d);
 		s.add(2, -2d);
 		Assert.assertEquals(Math.sqrt(5d), s.l2Norm());
 		Assert.assertEquals(3d, s.l1Norm());
 		Assert.assertEquals(2d, s.lInfNorm());
 		Assert.assertEquals(2, s.l0Norm());
 		
 		Vector t = Vector.sparse(false);
 		t.add(0, 0, 1d);
 		t.add(1, 2, -2d);
 		Assert.assertEquals(Math.sqrt(5d), t.l2Norm());
 		Assert.assertEquals(3d, t.l1Norm());
 		Assert.assertEquals(2d, t.lInfNorm());
 		Assert.assertEquals(2, t.l0Norm());
 	}
 	
 	@Test
 	public void scaleTests() {
		int iter = 10;
 		int iter = 10 * thoroughness;
 		for(int size=10; size<=1000; size*=10) {
 			for(int i=0; i<iter; i++) {
 				for(Double factor : Arrays.asList(0.001d, 0.1d, 10d, 1000d)) {
 					Vector d = Vector.dense(size);
 					Vector s = Vector.sparse();
 					int seed = rand.nextInt();
 					randomAdds(d, size, size/2, seed);
 					randomAdds(s, size, size/2, seed);
 					for(Vector v : Arrays.asList(d, s)) {
 						int l0 = v.l0Norm();
 						double l1 = v.l1Norm();
 						double l2 = v.l2Norm();
 						double li = v.lInfNorm();
 						v.scale(factor);
 						Assert.assertEquals(l0, v.l0Norm());
 						assertClose(l1 * factor, v.l1Norm());
 						assertClose(l2 * factor, v.l2Norm());
 						assertClose(li * factor, v.lInfNorm());
 					}
 				}
 			}
 		}
 	}
 	
 	@Test
 	public void dotTests() {
 		Vector ones = Vector.rep(1d, 10);
 		Vector s = Vector.sparse(false);
 		Assert.assertEquals(0d, ones.dot(s));
 		s.add(0, 1d);
 		Assert.assertEquals(1d, ones.dot(s));
 		s.add(2, 2d);
 		Assert.assertEquals(3d, ones.dot(s));
 		s.add(4, -3d);
 		Assert.assertEquals(0d, ones.dot(s));
 		s.add(6, 4d);
 		Assert.assertEquals(4d, ones.dot(s));
 		
 		int tries = 50 * thoroughness;
 		for(int pow=1; pow<=3; pow++) {
 			int nonzero = (int) Math.pow(10, pow), range = (int)(10d * Math.pow(10, pow));
 			for(int t=0; t<tries; t++) {
 				Vector d1 = Vector.dense(range);
 				Vector d2 = Vector.dense(range);
 				Vector s1 = Vector.sparse(false);
 				Vector s2 = Vector.sparse(false);
 				for(int i=0; i<nonzero; i++) {
 					int i1 = rand.nextInt(range);
 					int i2 = rand.nextInt(range);
 					double v1 = rand.nextGaussian();
 					double v2 = rand.nextGaussian();
 					d1.add(i1, v1); s1.add(i1, v1);
 					d2.add(i2, v2); s2.add(i2, v2);
 				}
 				double dd = d1.dot(d2);
 				double sd = s1.dot(s2);
 				//System.out.printf("dense=%.4f sparse=%.4f\n", dd, sd);
 				Assert.assertEquals(dd, sd);
 			}
 		}
 	}
 	
 	@Test
 	public void probTest() {
 		Vector v = Vector.rep(1d, 3);
 		v.makeProbDist();
 		Assert.assertEquals(1/3d, v.get(0));
 		Assert.assertEquals(1/3d, v.get(1));
 		Assert.assertEquals(1/3d, v.get(2));
 		
 		v.clear();
 		//v = Vector.rep(30d, 3);
 		v.makeProbDist();
 		Assert.assertEquals(1/3d, v.get(0));
 		Assert.assertEquals(1/3d, v.get(1));
 		Assert.assertEquals(1/3d, v.get(2));
 		
 		v.clear();
 		//v = Vector.rep(-30d, 3);
 		v.makeProbDist();
 		Assert.assertEquals(1/3d, v.get(0));
 		Assert.assertEquals(1/3d, v.get(1));
 		Assert.assertEquals(1/3d, v.get(2));
 		
 		v = Vector.rep(0d, 2);
 		v.set(0, 1d);
 		v.makeProbDist();
 		double p = Math.exp(1d) / (1d + Math.exp(1d));
 		assertClose(p, v.get(0));
 		assertClose(1d - p, v.get(1));
 		
 		v.clear();
 		v.add(0, 9001d);
 		v.makeProbDist();
 		assertClose(1d, v.get(0));
 		assertClose(0d, v.get(1));
 		
 		v.clear();
 		v.add(0, -9001d);
 		v.makeProbDist();
 		assertClose(0d, v.get(0));
 		assertClose(1d, v.get(1));
 		
 		v.clear();
 		v.add(0, 1d);
 		v.makeProbDist(0.0001d);
 		assertClose(1d, v.get(0));
 		assertClose(0d, v.get(1));
 		
 		v.clear();
 		v.add(0, 1d);
 		v.makeProbDist(9999d);
 		assertClose(1/2d, v.get(0), 1e-4);
 		assertClose(1/2d, v.get(1), 1e-4);
 	}
 	
 	@Test
 	public void sumAddTests() {
 		int iter = 4 * thoroughness;
 		for(Integer size : Arrays.asList(10, 100, 500)) {
 			for(Double addFactor : Arrays.asList(1/8d, 1d, 8d)) {
 				int adds = (int) Math.ceil(size * addFactor);
 
 				Vector s = Vector.sparse(false);
 				Vector s2 = Vector.sparse(false);
 				Vector d = Vector.dense(size);
 				Vector d2 = Vector.dense(size);
 
 				s.printWarnings = false;
 				s2.printWarnings = false;
 				d.printWarnings = false;
 				d2.printWarnings = false;
 
 				for(int i=0; i<iter; i++) {
 					int seed = rand.nextInt();
 					randomAdds(s, size, adds, seed);
 					randomAdds(s2, size, adds, seed);
 					randomAdds(d, size, adds, seed);
 					randomAdds(d2, size, adds, seed);
 
 					Vector sd1 = Vector.sum(s, d);
 					Vector sd2 = Vector.sum(d, s);
 					Vector ss = Vector.sum(s, s);
 					Vector dd = Vector.sum(d, d);
 
 					// binary op (no side-effects)
 					assertClose(sd1, sd2, size);
 					assertClose(sd2, ss, size);
 					assertClose(ss, dd, size);
 
 					// sparse += dense
 					s.add(d);
 					assertClose(sd1, s, size);
 
 					// sparse += sparse
 					s.add(s2, -1d);
 					assertClose(s, s2, size);
 
 					// dense += sparse
 					d.add(s2);
 					assertClose(sd1, d, size);
 
 					// dense += dense
 					d.add(d2, -1d);
 					assertClose(d, d2, size);
 
 					s.clear();
 					s2.clear();
 					d.clear();
 					d2.clear();
 				}
 			}
 		}
 	}
 	
 	public void randomAdds(Vector v, int size, int adds, int seed) { randomAdds(v, size, adds, seed, false); }
 	public void randomAdds(Vector v, int size, int adds, int seed, boolean addIntegerValues) {
 		Random r = new Random(seed);
 		double value;
 		for(int i=0; i<adds; i++) {
 			int idx = r.nextInt(size);
 			if(addIntegerValues) value = r.nextInt(10) - 5;
 			else value = r.nextGaussian();
 			v.add(idx, value);
 		}
 	}
 	
 	public static void assertClose(Vector a, Vector b, int size) { assertClose(a, b, size, 1e-8); }
 	public static void assertClose(Vector a, Vector b, int size, double epsilon) {
 		for(int i=0; i<size; i++)
 			assertClose(a.get(i), b.get(i), epsilon);
 	}
 	
 	public static void assertClose(double expected, double actual) { assertClose(expected, actual, 1e-8); }
 	public static void assertClose(double expected, double actual, double epsilon) {
 		double diff = actual - expected;
 		Assert.assertTrue(
 				String.format("expected=%.3g actual=%.3g diff=%.3g > %.3g", expected, actual, diff, epsilon),
 				Math.abs(diff) <= epsilon);
 	}
 
 }
