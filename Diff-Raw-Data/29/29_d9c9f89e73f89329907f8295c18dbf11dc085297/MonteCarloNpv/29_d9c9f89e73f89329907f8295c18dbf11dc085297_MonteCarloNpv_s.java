 package org.anvard.mcnpv;
 
 import java.util.concurrent.ForkJoinPool;
 
 import org.springframework.util.StopWatch;
 
 public class MonteCarloNpv {
 
 	private static final int NUM_ITER = 10000000;
 	
 	private Distribution initial;
 	private Distribution year1;
 	private Distribution year2;
 	private Distribution year3;
 	private Distribution year4;
 	private Distribution year5;
 	private Distribution rate;
 
 	public MonteCarloNpv() {
 		initial = new SingleValueDistribution(-20000);
 		year1 = new TriangleDistribution(0, 4000, 10000);
 		year2 = new TriangleDistribution(0, 4000, 10000);
 		year3 = new TriangleDistribution(1000, 8000, 20000);
 		year4 = new TriangleDistribution(1000, 8000, 20000);
 		year5 = new TriangleDistribution(5000, 12000, 40000);
 		rate = new TriangleDistribution(2, 4, 8);
 	}
 
 	public StatsCollector parallel(int minChunkSize, int numChunks) {
 		ForkJoinPool pool = new ForkJoinPool();
 		NpvTask task = new NpvTask(10, NUM_ITER, rate, initial, year1, year2,
 				year3, year4, year5);
 		task.setMinChunkSize(minChunkSize);
 		task.setNumChunks(numChunks);
 		return pool.invoke(task);
 	}
 
 	public StatsCollector sequential() {
 		NpvTask task = new NpvTask(10, NUM_ITER, rate, initial, year1, year2,
 				year3, year4, year5);
 		task.setMinChunkSize(NUM_ITER + 1);
 		// Safe because it won't fork
 		return task.compute();
 	}
 
 	private static void oneSize(String name, int children, int chunkSize, MonteCarloNpv npv, StopWatch sw) {
 		String swName = name + " (children=" + children + ", min fork size="
 				+ chunkSize + ")";
 		System.out.println(swName);
 		sw.start(swName);
 		StatsCollector stats = npv.parallel(chunkSize, children);
 		sw.stop();
 		System.out.println(stats);
 	}
 	
 	private static void allSizes(String name, int children, MonteCarloNpv npv, StopWatch sw) {
 		int[] chunkSizes = { 100, 500, 1000, 2000 };
 
 		for (int i : chunkSizes) {
 			oneSize(name, children, i, npv, sw);
 		}
 	}
 
 	public static void main(String args[]) {
 		MonteCarloNpv npv = new MonteCarloNpv();
 		StopWatch sw = new StopWatch("Monte Carlo NPV");
 
 		sw.start("Sequential");
		npv.sequential();
 		sw.stop();
 
 		allSizes("DivideByTwo", 2, npv, sw);
 		allSizes("DivideByP", Runtime.getRuntime().availableProcessors(), npv, sw);
 		allSizes("Sqrt(n)", -1, npv, sw);
 
 		int chunkSize = 500;
 		oneSize("Parfor", (int)Math.ceil(NUM_ITER/chunkSize), chunkSize, npv, sw);
 		
 		System.out.println(sw.prettyPrint());
 	}
 
 }
