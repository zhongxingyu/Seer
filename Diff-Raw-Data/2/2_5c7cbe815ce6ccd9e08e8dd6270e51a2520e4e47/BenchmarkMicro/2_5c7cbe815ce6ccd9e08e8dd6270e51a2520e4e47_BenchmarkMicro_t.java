 package com.tinkerpop.bench.benchmark;
 
 import java.io.File;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.tinkerpop.bench.Bench;
 import com.tinkerpop.bench.ConsoleUtils;
 import com.tinkerpop.bench.GlobalConfig;
 import com.tinkerpop.bench.GraphDescriptor;
 import com.tinkerpop.bench.LogUtils;
 import com.tinkerpop.bench.cache.Cache;
 import com.tinkerpop.bench.generator.GraphGenerator;
 import com.tinkerpop.bench.generator.SimpleBarabasiGenerator;
 import com.tinkerpop.bench.log.SummaryLogWriter;
 import com.tinkerpop.bench.operation.OperationDeleteGraph;
 import com.tinkerpop.bench.operation.operations.*;
 import com.tinkerpop.bench.operationFactory.OperationFactory;
 import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
 import com.tinkerpop.bench.operationFactory.factories.OperationFactoryRandomVertex;
 import com.tinkerpop.bench.operationFactory.factories.OperationFactoryRandomVertexPair;
 import com.tinkerpop.blueprints.pgm.impls.bdb.BdbGraph;
 import com.tinkerpop.blueprints.pgm.impls.dex.DexGraph;
 import com.tinkerpop.blueprints.pgm.impls.dup.DupGraph;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
 //import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
 //import com.tinkerpop.blueprints.pgm.impls.rdf.RdfGraph;
 import com.tinkerpop.blueprints.pgm.impls.rdf.impls.NativeStoreRdfGraph;
 import com.tinkerpop.blueprints.pgm.impls.sql.SqlGraph;
 
 import edu.harvard.pass.cpl.CPL;
 import edu.harvard.pass.cpl.CPLException;
 //import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 /**
  * @author Alex Averbuch (alex.averbuch@gmail.com)
  * @author Daniel Margo (dmargo@eecs.harvard.edu)
  * @author Peter Macko (pmacko@eecs.harvard.edu)
  */
 public class BenchmarkMicro extends Benchmark {
 	
 	/// The default file for ingest
 	private static final String DEFAULT_INGEST_FILE = "barabasi_1000_5000.graphml";
 	
 	/// The list of supported databases
 	private static final String[] DATABASE_SHORT_NAMES = { "bdb", "dex", "dup", "hollow", "neo", "rdf", "sql" };
 	private static final String[] DATABASE_WITH_OPTIONAL_ARG = { "sql" };
 	
 	/// The list of supported database classes
 	private static final Class<?>[] DATABASE_CLASSES = { BdbGraph.class, DexGraph.class, DupGraph.class,
 		HollowGraph.class, Neo4jGraph.class, NativeStoreRdfGraph.class, SqlGraph.class };
 	
 	/// The defaults
 	private static final int DEFAULT_OP_COUNT = 1000;
 	private static final int DEFAULT_K_HOPS = 2;
 	
 	/// The graphdb-bench directory
 	private static String graphdbBenchDir = null;
 	
 	/// The number of threads
 	private static int numThreads = 1;
 	
 	
 	/**
 	 * Print the help
 	 */
 	protected static void help() {
 		
 		System.err.println("Usage: runBenchmarkSuite.sh OPTIONS");
 		System.err.println("");
 		System.err.println("General options:");
 		System.err.println("  --dir, -d DIR         Set the database and results directory");
 		System.err.println("  --help                Print this help message");
 		System.err.println("  --no-provenance       Disable provenance collection");
 		System.err.println("  --no-warmup           Disable the initial warmup run");
 		System.err.println("  --threads N           Run N copies of the benchmark concurrently");
 		System.err.println("  --tx-buffer N         Set the size of the transaction buffer");
 		System.err.println("");
 		System.err.println("Options to select a database (select one):");
 		System.err.println("  --bdb                 Berkeley DB, using massive indexing");
 		System.err.println("  --dex                 DEX");
 		System.err.println("  --dup                 Berkeley DB, duplicates "+
 										"on edge lookups and properties");
 		System.err.println("  --hollow              The hollow implementation with no "+
 										"backing database");
 		System.err.println("  --neo                 neo4j");
 		System.err.println("  --rdf                 Sesame RDF");
 		System.err.println("  --sql [ADDR]          MySQL");
 		System.err.println("");
 		System.err.println("Options to select a workload (select multiple):");
 		System.err.println("  --add                 Adding nodes and edges to the database");
 		System.err.println("  --clustering-coeff    Compute the clustering coefficients");
 		System.err.println("  --delete-graph        Delete the entire graph");
 		System.err.println("  --dijkstra            Dijkstra's shortest path algorithm");
         System.err.println("  --dijkstra-property   Shortest paths with in-DB marking.");
 		System.err.println("  --generate MODEL      Generate (or grow) the graph "+
 										" based on the given model");
 		System.err.println("  --get                 \"Get\" microbenchmarks");
 		System.err.println("  --get-k               \"Get\" k-hops microbenchmarks");
 		System.err.println("  --ingest [FILE]       Ingest a file to the database "+
 										" (implies --delete-graph)");
 		System.err.println("");
 		System.err.println("Benchmark options:");
 		System.err.println("  --k-hops K            Set the number of k-hops");
 		System.err.println("  --k-hops K1:K2        Set a range of k-hops");
 		System.err.println("  --op-count N          Set the number of operations");
 		System.err.println("  --warmup-op-count N   Set the number of warmup operations");
 		System.err.println("");
 		System.err.println("Options for model \"Barabasi\":");
 		System.err.println("  --barabasi-n N        The number of vertices");
 		System.err.println("  --barabasi-m M        The number of outgoing edges "+
 										" generated for each vertex");
 	}
 	
 	
 	/**
 	 * Get the given property and expand variables
 	 * 
 	 * @param key the property key
 	 * @param defaultValue the default value
 	 * @return the value
 	 */
 	protected static String getProperty(String key, String defaultValue) {
 		
 		String v = Bench.benchProperties.getProperty(key);
 		if (v == null) return defaultValue;
 		
 		if (v.indexOf("$GRAPHDB_BENCH") >= 0) {
 			if (graphdbBenchDir == null) {
 				ConsoleUtils.error("Could not determine the graphdb-bench directory.");
 				throw new RuntimeException("Could not expand the $GRAPHDB_BENCH variable");
 			}
 			v = v.replaceAll("\\$GRAPHDB_BENCH", graphdbBenchDir);
 		}
 		
 		return v;
 	}
 	
 	
 	/**
 	 * Get the given property and expand variables
 	 * 
 	 * @param key the property key
 	 * @return the value, or null if not found
 	 */
 	protected static String getProperty(String key) {
 		return getProperty(key, null);
 	}
 
 	
 	/**
 	 * Run the benchmarking program
 	 * 
 	 * @param args the command-line arguments
 	 * @throws Exception on error
 	 */
 	public static void run(String[] args) throws Exception {
 		
 		/*
 		 * Initialize
 		 */
 		
 		// Find the graphdb-bench directory
 		
 		URL source = BenchmarkMicro.class.getProtectionDomain().getCodeSource().getLocation();
 		if ("file".equals(source.getProtocol())) {
 			for (File f = new File(source.toURI()); f != null; f = f.getParentFile()) {
 				if (f.getName().equals("graphdb-bench")) {
 					graphdbBenchDir = f.getAbsolutePath();
 					break;
 				}
 			}
 		}
 		
 		if (graphdbBenchDir == null) {
 			ConsoleUtils.warn("Could not determine the graphdb-bench directory.");
 		}
 		
 
 		/*
 		 * Parse the command-line arguments
 		 */
 		
 		OptionParser parser = new OptionParser();
 		
		parser.accepts("annotation").withRequiredArg().ofType(String.class); /* TODO */
		
 		parser.accepts("d").withRequiredArg().ofType(String.class);
 		parser.accepts("dir").withRequiredArg().ofType(String.class);
 		parser.accepts("help");
 		parser.accepts("no-provenance");
 		parser.accepts("no-warmup");
 		parser.accepts("threads").withRequiredArg().ofType(Integer.class);
 		parser.accepts("tx-buffer").withRequiredArg().ofType(Integer.class);
 		
 		
 		// Databases
 		
 		for (int i = 0; i < DATABASE_SHORT_NAMES.length; i++) {
 			
 			boolean withOptArg = false;
 			for (int j = 0; j < DATABASE_WITH_OPTIONAL_ARG.length; j++) {
 				if (DATABASE_SHORT_NAMES[i].equals(DATABASE_WITH_OPTIONAL_ARG[j])) {
 					withOptArg = true;
 					break;
 				}
 			}
 			
 			if (withOptArg) {
 				parser.accepts(DATABASE_SHORT_NAMES[i]).withOptionalArg().ofType(String.class);
 			}
 			else {
 				parser.accepts(DATABASE_SHORT_NAMES[i]);
 			}
 		}
 		
 		
 		// Workloads
 		
 		parser.accepts("add");
 		parser.accepts("clustering-coef");
 		parser.accepts("clustering-coeff");
 		parser.accepts("delete-graph");
 		parser.accepts("dijkstra");
         parser.accepts("dijkstra-property");
 		parser.accepts("generate").withRequiredArg().ofType(String.class);
 		parser.accepts("get");
 		parser.accepts("get-k");
 		parser.accepts("ingest").withOptionalArg().ofType(String.class);
 		
 		
 		// Ingest modifiers
 		
 		parser.accepts("f").withRequiredArg().ofType(String.class);
 		parser.accepts("file").withRequiredArg().ofType(String.class);	/* deprecated */
 		
 		
 		// Benchmark modifiers
 		
 		parser.accepts("k-hops").withRequiredArg().ofType(String.class);
 		parser.accepts("op-count").withRequiredArg().ofType(Integer.class);
 		parser.accepts("warmup-op-count").withRequiredArg().ofType(Integer.class);
 		
 		
 		// Generator modifiers
 		
 		parser.accepts("barabasi-n").withRequiredArg().ofType(Integer.class);
 		parser.accepts("barabasi-m").withRequiredArg().ofType(Integer.class);
 		
 		
 		// Parse the options
 		
 		OptionSet options;
 		
 		try {
 			options = parser.parse(args);
 		}
 		catch (Exception e) {
 			ConsoleUtils.error("Invalid options (please use --help for a list): " + e.getMessage());
 			return;
 		}
 		
 		List<String> nonOptionArguments = options.nonOptionArguments();
 		if (!nonOptionArguments.isEmpty()) {
 			ConsoleUtils.error("Invalid options (please use --help for a list): " + nonOptionArguments);
 			return;
 		}
 		
 		
 		// Handle the options
 		
 		if (options.has("help") || !options.hasOptions()) {
 			help();
 			return;
 		}
 		
 		String ingestFile = DEFAULT_INGEST_FILE;		
 		if (options.has("f") || options.has("file")) {
 			ingestFile = options.valueOf(options.has("f") ? "f" : "file").toString();
 		}
 		if (options.has("ingest")) {
 			if (options.hasArgument("ingest")) {
 				ingestFile = options.valueOf("ingest").toString();
 			}
 		}
 		
 		boolean warmup = true;
 		if (options.has("no-warmup")) {
 			warmup = false;
 		}
 		
 		boolean provenance = true;
 		if (options.has("no-provenance")) {
 			provenance = false;
 		}
 		
 		if (options.has("threads")) {
 			numThreads = (Integer) options.valueOf("threads");
 			if (numThreads < 1) {
 				ConsoleUtils.error("Invalid number of threads -- must be at least 1");
 				return;
 			}
 		}
 		
 		if (options.has("tx-buffer")) {
 			GlobalConfig.transactionBufferSize = (Integer) options.valueOf("tx-buffer");
 			if (GlobalConfig.transactionBufferSize < 1) {
 				ConsoleUtils.error("Invalid size of the transaction buffer -- must be at least 1");
 				return;
 			}
 		}
 		
 		int opCount = DEFAULT_OP_COUNT;
 		if (options.has("op-count")) opCount = (Integer) options.valueOf("op-count");
 		
 		int warmupOpCount = opCount;
 		if (options.has("warmup-op-count")) warmupOpCount = (Integer) options.valueOf("warmup-op-count");
 		
 		int[] kHops;
 		if (options.has("k-hops")) {
 			String kHopsStr = (String) options.valueOf("k-hops");
 			int kc = kHopsStr.indexOf(':');
 			if (kc >= 0) {
 				int k1, k2;
 				try {
 					k1 = Integer.parseInt(kHopsStr.substring(0, kc));
 					k2 = Integer.parseInt(kHopsStr.substring(kc + 1));
 				}
 				catch (NumberFormatException e) {
 					ConsoleUtils.error("Invalid range of k hops (not a number).");
 					return;
 				}
 				if (k1 <= 0 || k1 > k2) {
 					ConsoleUtils.error("Invalid range of k hops.");
 					return;
 				}
 				kHops = new int[k2-k1+1];
 				for (int k = k1; k <= k2; k++) kHops[k-k1] = k;
 			}
 			else {
 				kHops = new int[1];
 				try {
 					kHops[0] = Integer.parseInt(kHopsStr);
 				}
 				catch (NumberFormatException e) {
 					ConsoleUtils.error("Invalid number of k hops (not a number).");
 					return;
 				}
 				if (kHops[0] <= 0) {
 					ConsoleUtils.error("Invalid number of k hops (must be positive).");
 					return;
 				}
 			}
 		}
 		else {
 			kHops = new int[1];
 			kHops[0] = DEFAULT_K_HOPS;
 		}
 		
 		if (provenance) {
 			if (!CPL.isInstalled()) {
 				ConsoleUtils.error("CPL is not installed. Use --no-provenance to disable provenance collection.");
 				return;
 			}
 			else {
 				try {
 					CPL.attachODBC(getProperty(Bench.CPL_ODBC_DSN, "DSN=CPL"));
 				}
 				catch (CPLException e) {
 					ConsoleUtils.error("Could not initialize provenance collection:");
 					ConsoleUtils.error("  " + e.getMessage());
 					return;
 				}
 			}
 		}
 		
 		
 		// Database-specific arguments
 		
 		String dbShortName = null;
 		Class<?> dbClass = null;
 		for (int i = 0; i < DATABASE_SHORT_NAMES.length; i++) {
 			if (options.has(DATABASE_SHORT_NAMES[i])) {
 				if (dbShortName != null) {
 					System.err.println("Error: Multiple databases selected.");
 					return;
 				}
 				dbShortName = DATABASE_SHORT_NAMES[i];
 				dbClass = DATABASE_CLASSES[i];
 				break;
 			}
 		}
 		if (dbShortName == null) {
 			ConsoleUtils.error("No database is selected (please use --help for a list of options).");
 			return;
 		}
 		
 		boolean withGraphPath = true;
 		if (dbClass == HollowGraph.class) withGraphPath = false;
 		
 		String sqlDbPath = null;
 		if (options.has("sql")) {
 			if (options.hasArgument("sql")) {
 				sqlDbPath = options.valueOf("sql").toString();
 			}
 			else {
 				String sqlDbPath_property = getProperty(Bench.DB_SQL_PATH);
 				if (sqlDbPath_property != null) {
 					sqlDbPath = sqlDbPath_property;
 				}
 				else {
 					ConsoleUtils.error("The SQL database path is not specified.");
 					return;
 				}
 			}
 		}
 
 		
 		/*
 		 * Setup the graph generator
 		 */
 		
 		GraphGenerator graphGenerator = null;
 		if (options.has("generate")) {
 			String model = options.valueOf("generate").toString().toLowerCase();
 			
 			if (model.equals("barabasi")) {
 				int n = options.has("barabasi-n") ? (Integer) options.valueOf("barabasi-n") : 1000;
 				int m = options.has("barabasi-m") ? (Integer) options.valueOf("barabasi-m") : 5;
 				graphGenerator = new SimpleBarabasiGenerator(n, m);
 			}
 			
 			if (graphGenerator == null) {
 				ConsoleUtils.error("Unrecognized graph generation model");
 				return;
 			}
 		}
 		
 		
 		/*
 		 * Get the name of the results directory
 		 */
 		
 		String dirResults;
 		if (options.has("d") || options.has("dir")) {
 			dirResults = options.valueOf(options.has("d") ? "d" : "dir").toString();
 			if (!dirResults.endsWith("/")) dirResults += "/";
 		}
 		else {
 			String propDirResults = getProperty(Bench.RESULTS_DIRECTORY);
 			if (propDirResults == null) {
 				ConsoleUtils.error("Property \"" + Bench.RESULTS_DIRECTORY + "\" is not set and --dir is not specified.");
 				return;
 			}
 			if (!propDirResults.endsWith("/")) propDirResults += "/";
 			dirResults = propDirResults + "Micro/";
 		}
 		
 		
 		/*
 		 * Get the name of the ingest file (if necessary)
 		 */
 		
 		if (options.has("ingest")) {
 			if (!(new File(ingestFile)).exists()) {
 				String dirGraphML = getProperty(Bench.DATASETS_DIRECTORY);
 				if (dirGraphML == null) {
 					ConsoleUtils.warn("Property \"" + Bench.DATASETS_DIRECTORY + "\" is not set.");
 					ConsoleUtils.error("File \"" + ingestFile + "\" does not exist.");
 					return;
 				}
 				if (!dirGraphML.endsWith("/")) dirGraphML += "/";
 				if (!(new File(dirGraphML + ingestFile)).exists()) {
 					ConsoleUtils.error("File \"" + ingestFile + "\" does not exist.");
 					return;
 				}
 				else {
 					ingestFile = dirGraphML + ingestFile;
 				}
 			}
 		}
 		else {
 			ingestFile = null;
 		}
 		
 		
 		/*
 		 * Setup the benchmark
 		 */
 		
 		String[] graphmlFiles = new String[] { ingestFile };
 		GraphGenerator[] graphGenerators = new GraphGenerator[] { graphGenerator };
 		
 		Benchmark warmupBenchmark = new BenchmarkMicro(
 				graphmlFiles, graphGenerators, options, warmupOpCount, kHops);
 		
 		Benchmark benchmark = new BenchmarkMicro(
 				graphmlFiles, graphGenerators, options, opCount, kHops);
 		
 		
 		/*
 		 * Build the argument string to be used as a part of the log file name
 		 */
 		
 		GraphDescriptor graphDescriptor = null;
 		
 		StringBuilder sb = new StringBuilder();
 		for (String s : args) {
 			String argName = s.charAt(0) == '-' ? s.substring(s.charAt(1) == '-' ? 2 : 1) : s;
 			if (s.charAt(0) == '-') sb.append('_');
 			sb.append(argName);
 		}
 		
 		sb.append("_mem");
 		sb.append(Math.round(Runtime.getRuntime().maxMemory() / 1024768.0f));
 		sb.append("m");	
 		
 		sb.append("_");
 		sb.append((new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date()));
 		
 		String argString = sb.toString().replaceAll("\\s+", "");
 		
 		
 		/*
 		 * Set the file, directory, and database names
 		 */
 		
 		String dbPrefix = dirResults + dbShortName + "/";
 		String warmupDbDir = null;
 		String dbDir = null;
 		if (!options.has("sql")) {
 			warmupDbDir = dbPrefix + "warmup";
 			dbDir = dbPrefix + "db";
 		}
 		
 		String warmupDbPath = null;
 		String dbPath = null;
 		if (withGraphPath) {
 			if (options.has("sql")) {
 				warmupDbPath = sqlDbPath;
 				dbPath = sqlDbPath;
 			}
 			else {
 				warmupDbPath = warmupDbDir + (options.has("dex") ? "/graph.dex" : "");
 				dbPath = dbDir + (options.has("dex") ? "/graph.dex" : "");
 			}
 		}
 		
 		String logPrefix = dbPrefix + dbShortName;
 		String warmupLogFile = logPrefix + "-warmup" + argString + ".csv";
 		String logFile = logPrefix + argString + ".csv";
 		String summaryLogFile = logPrefix + "-summary" + argString + ".csv";
 		String summaryLogFileText = logPrefix + "-summary" + argString + ".txt";
 		
 		
 		/*
 		 * Print info
 		 */
 		
 		ConsoleUtils.sectionHeader("Tinkubator Graph Database Benchmark");
 		
 		System.out.println("Database    : " + dbShortName);
 		System.out.println("Directory   : " + dirResults);
 		System.out.println("Log File    : " + logFile);
 		System.out.println("Summary Log : " + summaryLogFile);
 		System.out.println("Summary File: " + summaryLogFileText);
 		
 		
 		/*
 		 * Run the benchmark
 		 */
 		
 		LinkedHashMap<String, String> resultFiles = new LinkedHashMap<String, String>();
 		
 		if (warmup) {
 			ConsoleUtils.sectionHeader("Warmup Run");
 			graphDescriptor = new GraphDescriptor(dbClass, warmupDbDir, warmupDbPath);
 			warmupBenchmark.runBenchmark(graphDescriptor, warmupLogFile, numThreads);
 			//resultFiles.put(dbShortName + "-warmup", warmupLogFile);
 			Cache.dropAll();
 		}
 		
 		ConsoleUtils.sectionHeader("Benchmark Run");
 		graphDescriptor = new GraphDescriptor(dbClass, dbDir, dbPath);
 		benchmark.runBenchmark(graphDescriptor, logFile, numThreads);
 		resultFiles.put(dbShortName, logFile);
 		
 		
 		/*
 		 * Create file with summarized results from all databases and operations
 		 */
 		
 		ConsoleUtils.sectionHeader("Summary");
 		
 		SummaryLogWriter summaryLogWriter = new SummaryLogWriter(resultFiles);
 		summaryLogWriter.writeSummary(summaryLogFile);
 		summaryLogWriter.writeSummaryText(summaryLogFileText);
 		summaryLogWriter.writeSummaryText(null);
 	}
 
 	
 	/*
 	 * Instance Code
 	 */
 	
 	private int opCount = DEFAULT_OP_COUNT;
 	private String PROPERTY_KEY = "_id";
 	private int[] kHops;
 
 	private String[] graphmlFilenames = null;
 	private GraphGenerator[] graphGenerators = null;
 	private OptionSet options = null;
 
 	public BenchmarkMicro(String[] graphmlFilenames,
 			GraphGenerator[] graphGenerators, OptionSet options,
 			int opCount, int[] kHops) {
 		this.graphmlFilenames = graphmlFilenames;
 		this.graphGenerators = graphGenerators;
 		this.options = options;
 		this.opCount = opCount;
 		this.kHops = kHops;
 	}
 
 	@Override
 	public ArrayList<OperationFactory> createOperationFactories() {
 		ArrayList<OperationFactory> operationFactories = new ArrayList<OperationFactory>();
 
 		for (String graphmlFilename : graphmlFilenames) {
 
 			// DELETE the graph (also invoked for the ingest benchmark)
 			if (options.has("ingest") || options.has("delete-graph")) {
 				if (numThreads != 1) {
 					throw new UnsupportedOperationException("Operations \"ingest\" "
 							+"and \"delete-graph\" are not supported in the "
 							+"multi-threaded mode");
 				}
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationDeleteGraph.class, 1));
 			}
 
 			// INGEST benchmarks
 			if (options.has("ingest")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationLoadGraphML.class, 1,
 						new String[] { graphmlFilename }, LogUtils.pathToName(graphmlFilename)));
 			}
 			
 			// GENERATE benchmarks
 			if (options.has("generate")) {
 				if (numThreads != 1) {
 					throw new UnsupportedOperationException("Operation \"generate\" "
 							+"is not supported in the multi-threaded mode");
 				}
 				for (GraphGenerator g : graphGenerators) {
 					operationFactories.add(new OperationFactoryGeneric(
 							OperationGenerateGraph.class, 1, new GraphGenerator[] { g }));
 				}
 			}
 
 			// GET microbenchmarks
 			if (options.has("get")) {
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyVertices.class, 1,
 						new Integer[] { opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyVertexProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyEdges.class, 1,
 						new Integer[] { opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGetManyEdgeProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 	
 				// GET_NEIGHBORS ops and variants
 				
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetFirstNeighbor.class, opCount));
  
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetRandomNeighbor.class, opCount));
  
 				operationFactories.add(new OperationFactoryRandomVertex(
 						OperationGetAllNeighbors.class, opCount));
 			}
 			
 			// GET_K_NEIGHBORS ops and variants
 			if (options.has("get-k")) {				
 				for (int k : kHops) {
 					operationFactories.add(new OperationFactoryRandomVertex(
 							OperationGetKFirstNeighbors.class, opCount, new Integer[] { k }));
 				}
 				
 				for (int k : kHops) {				
 					operationFactories.add(new OperationFactoryRandomVertex(
 							OperationGetKRandomNeighbors.class, opCount, new Integer[] { k }));
 				}
 				
 				for (int k : kHops) {
 					operationFactories.add(new OperationFactoryRandomVertex(
 							OperationGetKHopNeighbors.class, opCount, new Integer[] { k }));
 				}
 			}
 			
 			// SHORTEST PATH (Dijkstra's algorithm)
 			if (options.has("dijkstra")) {
 				operationFactories.add(new OperationFactoryRandomVertexPair(
 						OperationGetShortestPath.class, opCount / 2));
             }
 
 			if (options.has("dijkstra-property")) {	
 				if (numThreads != 1) {
 					throw new UnsupportedOperationException("Operation \"dijkstra-property\" "
 							+"is not supported in the multi-threaded mode");
 				}
 				operationFactories.add(new OperationFactoryRandomVertexPair(
 						OperationGetShortestPathProperty.class, opCount / 2));
 			}
 			
 			// CLUSTERING COEFFICIENT benchmarks
 			if (options.has("clustering-coef") || options.has("clustering-coeff")) {
 				//operationFactories.add(new OperationFactoryRandomVertex(
 				//		OperationLocalClusteringCoefficient.class, opCount));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationGlobalClusteringCoefficient.class, 1));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationNetworkAverageClusteringCoefficient.class, 1));
 			}
 			
 			// ADD/SET microbenchmarks
 			if (options.has("add")) {
 				if (numThreads != 1 && GlobalConfig.transactionBufferSize != 1) {
 					throw new UnsupportedOperationException("Set property operations inside \"add\" "
 							+"are not supported in the multi-threaded mode with tx-buffer > 1");
 				}
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationAddManyVertices.class, 1,
 						new Integer[] { opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationSetManyVertexProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationAddManyEdges.class, 1,
 						new Integer[] { opCount }));
 				
 				operationFactories.add(new OperationFactoryGeneric(
 						OperationSetManyEdgeProperties.class, 1,
 						new Object[] { PROPERTY_KEY, opCount }));
 			}
 					
 		}
 
 		return operationFactories;
 	}
 }
