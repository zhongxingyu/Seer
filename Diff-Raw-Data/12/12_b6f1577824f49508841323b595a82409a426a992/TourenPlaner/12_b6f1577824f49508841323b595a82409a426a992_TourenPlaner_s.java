 package server;
 
 import graphrep.GraphRep;
 import graphrep.GraphRepDumpReader;
 import graphrep.GraphRepTextReader;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import algorithms.AlgorithmFactory;
 import algorithms.GraphAlgorithmFactory;
 import algorithms.ShortestPathCHFactory;
 
 import computecore.AlgorithmRegistry;
 import computecore.ComputeCore;
 
 import config.ConfigManager;
 
 public class TourenPlaner {
 	/**
 	 * @param graphName
 	 *            Original file name of the graph
 	 * @return The filename of the dumped graph
 	 */
 	private static String dumpName(String graphName) {
 		return graphName + ".dat";
 	}
 
 	private static Map<String, Object> getServerInfo(AlgorithmRegistry reg) {
 		Map<String, Object> info = new HashMap<String, Object>(4);
 		info.put("version", new Float(0.1));
 		info.put(
 				"servertype",
 				ConfigManager.getInstance().getEntryBool("private", false) ? "private"
 						: "public");
 		info.put("sslport",
 				ConfigManager.getInstance().getEntryLong("sslport", 8081));
 		// Enumerate Algorithms
 		Collection<AlgorithmFactory> algs = reg.getAlgorithms();
 		Map<String, Object> algInfo;
 		List<Map<String, Object>> algList = new ArrayList<Map<String, Object>>();
 		for (AlgorithmFactory alg : algs) {
 			algInfo = new HashMap<String, Object>(5);
 			algInfo.put("version", alg.getVersion());
 			algInfo.put("name", alg.getAlgName());
 			algInfo.put("urlsuffix", alg.getURLSuffix());
 			if (alg instanceof GraphAlgorithmFactory) {
 				algInfo.put("pointconstraints",
 						((GraphAlgorithmFactory) alg).getPointConstraints());
 				algInfo.put("constraints",
 						((GraphAlgorithmFactory) alg).getConstraints());
 			}
 			algList.add(algInfo);
 		}
 		info.put("algorithms", algList);
 
 		return info;
 	}
 
 	/**
 	 * This is the main class of ToureNPlaner. It passes CLI parameters to the
 	 * handler and creates the httpserver
 	 */
 	public static void main(String[] args) {
 		/**
 		 * inits config manager if config file is provided; also prints usage
 		 * information if necessary
 		 */
 		CLIHandler handler = new CLIHandler(args);
 
 		// if serialize, then ignore whether to read text or dump and read
 		// text graph since it wouldn't make sense to read a serialized
 		// graph just to serialize it. Also do this before anything server
 		// related gets actually started
 		if (handler.serializegraph()) {
 			System.out.println("Dumping Graph");
 			GraphRep graph = null;
 			String graphfilename = ConfigManager.getInstance().getEntryString(
 					"graphfilepath",
 					System.getProperty("user.home") + "/germany-ch.txt");
 			try {
 				graph = new GraphRepTextReader()
 						.createGraphRep(new FileInputStream(graphfilename));
 				utils.GraphSerializer.serialize(new FileOutputStream(
 						dumpName(graphfilename)), graph);
 				System.exit(0);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// uses defaults if it was not initialized by the CLIHandler
 		ConfigManager cm = ConfigManager.getInstance();
 		// Load the Graph
 		GraphRep graph = null;
 		String graphfilename = cm.getEntryString("graphfilepath",
 				System.getProperty("user.home") + "/germany-ch.txt");
 		try {
 			if (handler.loadTextGraph()) {
 				graph = new GraphRepTextReader()
 						.createGraphRep(new FileInputStream(graphfilename));
 			} else {
 				try {
 					graph = new GraphRepDumpReader()
 							.createGraphRep(new FileInputStream(graphfilename
 									+ ".dat"));
 				} catch (InvalidClassException e) {
 					// TODO: unify default options?
 					String textGraphFilename = ConfigManager.getInstance()
 							.getEntryString(
 									"graphfilepath",
 									System.getProperty("user.home")
 											+ "/germany.txt");
 					System.err
 							.println("Dumped Graph version does not match the required version: "
 									+ e.getMessage());
 					System.out
 							.println("Falling back to text reading from file: "
 									+ textGraphFilename
 									+ " (path provided by config file) ...");
 					graph = (new GraphRepTextReader())
 							.createGraphRep(new FileInputStream(
 									textGraphFilename));
 					System.out
 							.println("Graph successfully read. Now replacing old dumped graph ...");
 
 					if ((new File(dumpName(graphfilename))).delete()) {
 						utils.GraphSerializer.serialize(new FileOutputStream(
 								dumpName(graphfilename)), graph);
 						System.out
 								.println("... success. Now running server ...");
 					} else {
 						System.out
 								.println("... failed. Now running server ...");
 					}
 				} catch (IOException e) {
 					String textGraphFilename = ConfigManager.getInstance()
 							.getEntryString(
 									"graphfilepath",
 									System.getProperty("user.home")
 											+ "/germany.txt");
 					System.err.println("Dumped Graph does not exist: "
 							+ e.getMessage());
 					System.out
 							.println("Falling back to text reading from file: "
 									+ textGraphFilename
 									+ " (path provided by config file) ...");
 					graph = (new GraphRepTextReader())
 							.createGraphRep(new FileInputStream(
 									textGraphFilename));
 					System.out
 							.println("Graph successfully read. Now dumping graph ...");
					if ((new File(dumpName(graphfilename))).delete()) {
						utils.GraphSerializer.serialize(new FileOutputStream(
								dumpName(graphfilename)), graph);
						System.out
								.println("... success. Now running server ...");
					} else {
						System.out
								.println("... failed. Now running server ...");
					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			// TODO: server won't calculate graph algorithms without a graph,
 			// but maybe it will provide some other functionality?
 		}
 
 		if (graph == null) {
 			System.out
 					.println("Graph reading FAILED! Only non graph related services will be available!");
 		}
 
 		// Register Algorithms
 		AlgorithmRegistry reg = new AlgorithmRegistry();
 		reg.registerAlgorithm(new ShortestPathCHFactory(graph));
 
 		// Create our ComputeCore that manages all ComputeThreads
 		ComputeCore comCore = new ComputeCore(reg, (int) cm.getEntryLong(
 				"threads", 16), (int) cm.getEntryLong("queuelength", 32));
 
 		// Create ServerInfo object
 		Map<String, Object> serverInfo = getServerInfo(reg);
 
 		new HttpServer(cm, reg, serverInfo, comCore);
 	}
 }
