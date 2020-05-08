 package de.uni.leipzig.IR15.Benchmark.suites;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import de.uni.leipzig.IR15.Benchmark.Benchmark;
 import de.uni.leipzig.IR15.Benchmark.ImportBenchmark;
 import de.uni.leipzig.IR15.Importer.DEXImporter;
 
 public class DEXBenchmarkSuite extends AbstractBenchmarkSuite {
	
 	public static Logger log = Logger.getLogger(DEXBenchmarkSuite.class);
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<Benchmark> benchmarks = new ArrayList<Benchmark>();
		
 		Benchmark dexImportBench = new ImportBenchmark(new DEXImporter());
 		dexImportBench.setWarmups(0);
 		dexImportBench.setRuns(1);
 		benchmarks.add(dexImportBench);
 
		Benchmark dexNeighbours = new de.uni.leipzig.IR15.Benchmark.dex.BFS_Benchmark(
				5, DEXImporter.RelTypes.CO_S, 137);
		dexNeighbours.setRuns(100);
		dexNeighbours.setWarmups(10);
		benchmarks.add(dexNeighbours);
		
 		runBenchmarks(benchmarks);
 	}
 }
