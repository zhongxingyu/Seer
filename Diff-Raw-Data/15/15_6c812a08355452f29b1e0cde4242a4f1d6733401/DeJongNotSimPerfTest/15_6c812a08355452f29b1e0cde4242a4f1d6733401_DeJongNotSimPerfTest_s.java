 package solvers.performance;
 
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.MethodRule;
 
 import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
 import com.carrotsearch.junitbenchmarks.BenchmarkRule;
 
 import solvers.*;
 import testharness.TestHarness;
 import algorithms.Algorithm;
 
 public class DeJongNotSimPerfTest {
 	@Rule
 	public MethodRule benchmarkRun = new BenchmarkRule();
 	  
 	protected TestHarness test = new TestHarness();
 	protected final int runs = 1;
 	protected final int warmup = 0;
 	int dim = 1;
 	
/*
 	@BenchmarkOptions(benchmarkRounds = runs, warmupRounds = warmup)
 	@Test
 	public final void DeJongNotSimPerf_BisectionAll_d5() {
 		Algorithm a = new Bisection_SrtL_CBtC_AllEqS();
 		run(a, dim);
 	}
 	@BenchmarkOptions(benchmarkRounds = runs, warmupRounds = warmup)
 	@Test
 	public final void DeJongNotSimPerf_BisectionBig_d5() {
 		Algorithm a = new Bisection_SrtL_CBtC_BigEqS();
 		int dim = 5;
 		run(a, dim);
 	}	
*/	
 	@BenchmarkOptions(benchmarkRounds = runs, warmupRounds = warmup)
 	@Test
 	public final void DeJongNotSimPerf_PointInterval_d5() {
 		Algorithm a = new PointIntervalBis_SrtL_CBtC_BigEqS();
 		int dim = 5;
 		run(a, dim);
 	}	
 	
 	
 	
 	private final void run(Algorithm a, int dim) {
 		test.f_DeJong_NotSim(a, dim);
 	}
 }
