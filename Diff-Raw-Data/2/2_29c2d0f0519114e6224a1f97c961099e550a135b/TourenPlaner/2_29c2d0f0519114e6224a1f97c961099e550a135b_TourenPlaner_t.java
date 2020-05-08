 /**
  * $$\\ToureNPlaner\\$$
  */
 package server;
 
 import algorithms.*;
 import computecore.AlgorithmManagerFactory;
 import computecore.AlgorithmRegistry;
 import computecore.ComputeCore;
 import computecore.SharingAMFactory;
 import config.ConfigManager;
 import graphrep.GraphRep;
 import graphrep.GraphRepDumpReader;
 import graphrep.GraphRepTextReader;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TourenPlaner {
 
     private static Logger log = Logger.getLogger("tourenplaner");
 
     /**
      * @param graphName Original file name of the graph
      * @return The filename of the dumped graph
      */
     private static String dumpName(String graphName) {
         return graphName + ".dat";
     }
 
     private static Map<String, Object> getServerInfo(AlgorithmRegistry reg) {
         Map<String, Object> info = new HashMap<String, Object>(4);
         info.put("version", new Float(0.1));
         info.put("servertype", ConfigManager.getInstance().getEntryBool("private", false) ? "private" : "public");
         info.put("sslport", ConfigManager.getInstance().getEntryInt("sslport", 8081));
         // Enumerate Algorithms
         Collection<AlgorithmFactory> algs = reg.getAlgorithms();
         Map<String, Object> algInfo;
         List<Map<String, Object>> algList = new ArrayList<Map<String, Object>>();
         for (AlgorithmFactory alg : algs) {
             algInfo = new HashMap<String, Object>(5);
             algInfo.put("version", alg.getVersion());
             algInfo.put("name", alg.getAlgName());
             algInfo.put("urlsuffix", alg.getURLSuffix());
             algInfo.put("hidden", alg.hidden());
             if (alg instanceof GraphAlgorithmFactory) {
                 algInfo.put("pointconstraints", ((GraphAlgorithmFactory) alg).getPointConstraints());
                 algInfo.put("constraints", ((GraphAlgorithmFactory) alg).getConstraints());
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
         // TODO remove when not needed as default
         Logger.getLogger("algorithms").setLevel(Level.FINEST);
         Logger.getLogger("server").setLevel(Level.FINEST);
         // Create globally shared ObjectMapper so we reuse it's data structures
         ObjectMapper mapper = new ObjectMapper();
         // make all property names in sent json lowercase
         // http://wiki.fasterxml.com/JacksonFeaturePropertyNamingStrategy
         mapper.setPropertyNamingStrategy(new JSONLowerCaseStrategy());
 
         /**
          * inits config manager if config file is provided; also prints usage
          * information if necessary
          */
         CLIHandler handler = new CLIHandler(mapper, args);
 
         // uses defaults if it was not initialized by the CLIHandler
         ConfigManager cm = ConfigManager.getInstance();
         String graphfilename = cm.getEntryString("graphfilepath", System.getProperty("user.home") + "/germany-ch.txt");
         // The Graph
         GraphRep graph = null;
 
         // if serialize, then ignore whether to read text or dump and read
         // text graph since it wouldn't make sense to read a serialized
         // graph just to serialize it. Also do this before anything server
         // related gets actually started
         if (handler.serializegraph()) {
             log.info("Dumping Graph");
             try {
                 graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                 utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
             } catch (IOException e) {
                 log.severe("IOError: " + e.getMessage());
             }
             System.exit(0);
         }
 
         try {
             if (handler.loadTextGraph()) {
                 graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
             } else {
                 try {
                     graph = new GraphRepDumpReader().createGraphRep(new FileInputStream(dumpName(graphfilename)));
                 } catch (InvalidClassException e) {
                     log.warning("Dumped Graph version does not match the required version: " + e.getMessage());
                     log.info("Falling back to text reading from file: " + graphfilename + " (path provided by config file)");
                     graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                     
 
                     if (graph != null && new File(dumpName(graphfilename)).delete()) {
                         log.info("Graph successfully read. Now replacing old dumped graph");
                         utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
                     } else if (graph != null) {
                         log.warning("creating dump failed but graph loaded");
                     }
                 } catch (IOException e) {
                     log.log(Level.WARNING, "loading text graph failed", e);
                     log.info("Falling back to text reading from file " + graphfilename + " (path provided by config file)");
                    graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                     log.info("Graph successfully read. Now dumping graph");
                     utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
                 }
             }
         } catch (IOException e) {
             log.log(Level.SEVERE, "Exception during graph loading", e);
         }
 
         if (graph == null) {
             log.severe("Reading graph failed");
             System.exit(1);
         }
 
         // Register Algorithms
         AlgorithmRegistry reg = new AlgorithmRegistry();
         //reg.registerAlgorithm(new ShortestPathFactory(graph));
         reg.registerAlgorithm(new ShortestPathCHFactory(graph));
         reg.registerAlgorithm(new TravelingSalesmenFactory(graph));
         reg.registerAlgorithm(new NNSearchFactory(graph));
 
         // Create our ComputeCore that manages all ComputeThreads
         ComputeCore comCore = new ComputeCore(reg, cm.getEntryInt("threads", 16), cm.getEntryInt("queuelength", 32));
         AlgorithmManagerFactory amFac = new SharingAMFactory(graph);
         comCore.start(amFac);
 
 
         // Create ServerInfo object
         Map<String, Object> serverInfo = getServerInfo(reg);
 
         new HttpServer(mapper, cm, reg, serverInfo, comCore);
     }
 
 }
