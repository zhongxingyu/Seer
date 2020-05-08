 package hk.hku.cs.c7802.test;
 
 import static org.junit.Assert.*;
 
 import hk.hku.cs.c7802.montecarlo.Antithetic;
 import hk.hku.cs.c7802.montecarlo.NormalGenerator;
 import hk.hku.cs.c7802.montecarlo.RandomGenerator;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class NormalGeneratorTest {
 	NormalGenerator[] ngs;
 	Antithetic[] ants;
 	RandomGenerator[] ngs_ants;
 	
 	@Before
 	public void setUp() throws Exception {
 		final int N = 3;
 		ngs = new NormalGenerator[N];
 		ngs[0] = new NormalGenerator.BigNumberAlgorithm();
 		ngs[1] = new NormalGenerator.BoxMuller1();
 		ngs[2] = new NormalGenerator.BoxMuller2();
 		ants = new Antithetic[N];
 		for(int i = 0; i < N; i++) {
 			ants[i] = new Antithetic(ngs[i]);
 		}
 		ngs_ants = new RandomGenerator[2 * N];
 		for(int i = 0; i < N; i++) {
 			ngs_ants[i] = ngs[i];
 			ngs_ants[i + N] = ants[i];
 		}
 	}
 	
 	@Test
 	public void testSetSeed() {
 		for(RandomGenerator ng: ngs_ants) {
 			ng.setSeed(2);
 			double x = ng.next();
 			double y = ng.next();
 			ng.setSeed(2);
 			double xx = ng.next();
 			double yy = ng.next();
 			assertEquals(x, xx, 1e-8);
 			assertEquals(y, yy, 1e-8);
 		}
 	}
 	
 	private void testNormalDistribution(NormalGenerator ng, long seed, int N, double error) {
 		ng.setSeed(seed);
 		double sum = 0;
 		double sqSum = 0;
 		for(int i = 0; i < N; i++) {
 			double r = ng.next();; 
 			sum += r;
 			sqSum += r * r;
 		}
 		double E = sum / N;
 		double variance = (sqSum / N - E * E);
 		
 		assertEquals(0, E, error);
 		assertEquals(1, variance, error);
 	}
 	
 	private void testAntithetic(Antithetic ant, int seed) {
 		int M = 8;
 		int N = 12;
 		
 		// generate by Antithetic or non antithetic
 		double rand[][][] = new double[2][M][N]; 
 		for(int k = 0; k < 2; k++) {
 			ant.setSeed(seed);
 			for(int i = 0; i < M; i++) {
 				for(int j = 0; j < N; j++) {
 					rand[k][i][j] = ant.next();
 				}
 				if(k == 1) {
 					ant.nextPath();
 				}
 			}
 		}
 		
 		// value the function, here I use the stock price model
 		// the function is just for demo, not necessary correct
 		double s[][] = new double[2][M];
 		double r = 0.04;
 		double deltaT = 30 / 365.0;
 		double volatility = 0.23;
 		// double expected = Math.pow(1 + r * deltaT, 360 / 30);
 		double stockPrice0 = 1;
 		for(int k = 0; k < 2; k++) {
 			for(int i = 0; i < M; i++) {
 				double stockPrice = stockPrice0;
 				for(int j = 0; j < N; j++) {
 					double changeRate = r * deltaT + volatility * rand[k][i][j] * Math.sqrt(deltaT);
 					stockPrice *= 1 + changeRate;
 				}
 				s[k][i] = stockPrice;
 			}
 		}
 		
 		// compare the variance
 		double V[] = new double[2];
 		for(int k = 0; k < 2; k++) {
 			double sum = 0;
 			double sumSq = 0;
 			for(int i = 0; i < M; i++) {
 				sum += s[k][i];
 				sumSq += s[k][i] * s[k][i];
 			}
 			double E = sum / M;
 			V[k] = sumSq - E * E;
 		}
 		
 		System.err.println("V1 = " + V[1]);
 		System.err.println("V0 = " + V[0]);
 		assertTrue("Antithetic should achieve a smaller variance", V[1] < V[0]);
 	}
 	@Test
 	public void testBigNumberAlgorithm() {
 		this.testNormalDistribution(ngs[0], 2, 10000, 0.02);
 		this.testNormalDistribution(ngs[0], 7, 10000, 0.04);
 		this.testNormalDistribution(ngs[0], 19, 10000, 0.02);
 	}
 
 	@Test
 	public void testBoxMuller1() {
 		this.testNormalDistribution(ngs[1], 2, 10000, 0.02);
 		this.testNormalDistribution(ngs[1], 7, 10000, 0.02);
 		this.testNormalDistribution(ngs[1], 19, 10000, 0.02);
 	}
 	
 	@Test
 	public void testBoxMuller2() {
 		this.testNormalDistribution(ngs[2], 2, 10000, 0.02);
 		this.testNormalDistribution(ngs[2], 7, 10000, 0.02);
 		this.testNormalDistribution(ngs[2], 19, 10000, 0.02);
 	}
 	
 	private double squareSum(double x[], int n) {
 		double sum = 0;
 		for(int i = 0 ; i < n; i++) {
 			sum += x[i] * x[i];
 		}
 		return sum;
 	}
 	
 	@Test
 	public void testSimpleAntithetics() {
 		int N = 12;
 		double x[] = new double[N];
 		double y[] = new double[N / 2];
 		Antithetic ant = ants[2];
		//ant.setSeed(2);
 		double sum1 = 0, sqsum1 = 0, sum2 = 0, sqsum2 = 0;
 		for(int i = 0; i < N; i++) {
 			x[i] = ant.next();
 			x[i] *= x[i];
 			sum1 += x[i];
 			sqsum1 += x[i] * x[i];
 			if(i < N / 2) {
 				sum2 += x[i];
 				sqsum2 += x[i] * x[i];
 			}
 		}
 		ant.nextPath();
 		for(int i = 0; i < N / 2; i++) {
 			y[i] = ant.next();
 			y[i] *= y[i];
 			sum2 += y[i];
 			sqsum2 += y[i] * y[i];
 		}		
 		double avg1 = sum1 / N;
 		double avg2 = sum2 / N;
 		double variance1 = (sqsum1/(N-1) - sum1 * sum1 /((double)N* (N-1)));
 		double variance2 = (sqsum2/(N-1) - sum2 * sum2 /((double)N* (N-1)));
 		System.err.println(String.format("Average: %f %f", avg1, avg2));
 		System.err.println(String.format("Variance: %f %f", variance1, variance2));
		assertTrue(variance1 > 2 * variance2);
 	}
 	
 	@Test
 	public void testAntithetics() {
 		for(Antithetic ant: ants) { 
 			this.testAntithetic(ant, 2);
 		}
 			
 	}
 }
