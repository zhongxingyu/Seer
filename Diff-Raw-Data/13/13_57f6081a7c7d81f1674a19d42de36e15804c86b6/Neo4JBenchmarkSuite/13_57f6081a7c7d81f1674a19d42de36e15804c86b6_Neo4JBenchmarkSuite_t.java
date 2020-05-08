 package de.uni.leipzig.IR15.Benchmark.suites;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.uni.leipzig.IR15.Benchmark.Benchmark;
 import de.uni.leipzig.IR15.Benchmark.ImportBenchmark;
 import de.uni.leipzig.IR15.Importer.Neo4JImporter;
 import de.uni.leipzig.IR15.Support.Configuration;
 
 /**
  * Neo4j benchmark suite that runs all the configured benchmarks.
  * 
  * @author IR-Team
  * 
  */
 public class Neo4JBenchmarkSuite extends AbstractBenchmarkSuite {
 
 	public static Configuration neoCfg = Configuration.getInstance("neo4j");
 
 	/**
 	 * Initialize and run all benchmarks.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<Benchmark> benchmarks = new ArrayList<Benchmark>();
 
 		boolean logToFile = neoCfg.getPropertyAsBoolean("log");
 		boolean doWarmup = neoCfg.getPropertyAsBoolean("warmup");
 
 		/**
 		 * Import
 		 */
 
 		if (neoCfg.getPropertyAsBoolean("import")) {
 			Benchmark neo4jImportBench = new ImportBenchmark(
 					new Neo4JImporter());
 			neo4jImportBench.setRuns(1);
 			benchmarks.add(neo4jImportBench);
 		}
 
 		/**
 		 * Cypher
 		 */
 
 		if (neoCfg.getPropertyAsBoolean("query1_cypher")) {
 			Benchmark query_1 = new de.uni.leipzig.IR15.Benchmark.neo4j.Query1_Cypher_Benchmark();
 			query_1.setRuns(100);
 			benchmarks.add(query_1);
 		}
 		if (neoCfg.getPropertyAsBoolean("query2_cypher")) {
 			Benchmark query_2 = new de.uni.leipzig.IR15.Benchmark.neo4j.Query2_Cypher_Benchmark();
 			query_2.setRuns(100);
 			benchmarks.add(query_2);
 		}
 		if (neoCfg.getPropertyAsBoolean("query3_cypher")) {
 			Benchmark query_3 = new de.uni.leipzig.IR15.Benchmark.neo4j.Query3_Cypher_Benchmark();
 			query_3.setRuns(100);
 			benchmarks.add(query_3);
 		}
 
 		/**
 		 * TraversalAPI
 		 */
 
 		if (neoCfg.getPropertyAsBoolean("query1_traversal")) {
 			Benchmark query_1_traveral = new de.uni.leipzig.IR15.Benchmark.neo4j.Query1_Traverser_Benchmark();
 			query_1_traveral.setRuns(100);
 			benchmarks.add(query_1_traveral);
 		}
 		if (neoCfg.getPropertyAsBoolean("query2_traversal")) {
 			Benchmark query_2_traversal = new de.uni.leipzig.IR15.Benchmark.neo4j.Query2_Traverser_Benchmark();
 			query_2_traversal.setRuns(100);
 			benchmarks.add(query_2_traversal);
 		}
 		if (neoCfg.getPropertyAsBoolean("query3_traversal")) {
 			Benchmark query_3_traversal = new de.uni.leipzig.IR15.Benchmark.neo4j.Query3_Traverser_Benchmark();
 			query_3_traversal.setRuns(100);
 			benchmarks.add(query_3_traversal);
 		}
 		/**
 		 * Native API
 		 */
 
 		if (neoCfg.getPropertyAsBoolean("query1_native")) {
 			Benchmark query_1_native = new de.uni.leipzig.IR15.Benchmark.neo4j.Query1_Native_Benchmark();
 			query_1_native.setRuns(100);
 			benchmarks.add(query_1_native);
 		}
		if (neoCfg.getPropertyAsBoolean("query2_native")) {
 			Benchmark query_2_native = new de.uni.leipzig.IR15.Benchmark.neo4j.Query2_Native_Benchmark();
 			query_2_native.setRuns(100);
 			benchmarks.add(query_2_native);
 		}
		if (neoCfg.getPropertyAsBoolean("query3_native")) {
 			Benchmark query_3_native = new de.uni.leipzig.IR15.Benchmark.neo4j.Query3_Native_Benchmark();
 			query_3_native.setRuns(100);
 			benchmarks.add(query_3_native);
 		}
 		
 		runBenchmarks(benchmarks, logToFile, doWarmup);
 	}
 }
