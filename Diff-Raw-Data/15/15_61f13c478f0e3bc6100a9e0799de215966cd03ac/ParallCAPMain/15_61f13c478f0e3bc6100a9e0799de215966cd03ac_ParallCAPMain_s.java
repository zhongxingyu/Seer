 package cgl.imr.samples.parallcap.ivy.a;
 
 /*
  * Main program for parallel CAP path discovery Twister application.
  * @author Fei Teng tf.richard@gmail.com
  * */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.safehaus.uuid.UUIDGenerator;
 
 import cgl.imr.base.TwisterException;
 import cgl.imr.base.TwisterMonitor;
 import cgl.imr.base.Value;
 import cgl.imr.base.impl.JobConf;
 import cgl.imr.client.TwisterDriver;
 import cgl.imr.types.MemCacheAddress;
 import cgl.imr.base.SerializationException;
 
 public class ParallCAPMain {
 	private static Map<Integer, Map<Integer, List<List<Integer>>>> queryIdMatrix;
 	private static Qns qns;
 	private static Map<Integer, Boolean> queryTable;  
 	
 	public ParallCAPMain() throws SerializationException{
 		qns = new Qns();
 
 		try {
 		Class testClass = Class.forName("cgl.imr.samples.parallcap.ivy.a.Node");
 		Value node = (Value)testClass.newInstance();
 		System.out.println("test node id: " + ((Node)node).getId());
 		} catch (Exception e) {
 			throw new SerializationException(e);
 			};
 
 		queryIdMatrix = new HashMap<Integer, Map<Integer, List<List<Integer>>>>();
 		final List<Node> queryList = qns.getQueryNodes();
 		for (int i = 0; i < queryList.size() - 1; i++) {
 			queryIdMatrix.put(queryList.get(i).getId(), 
 					new HashMap<Integer, List<List<Integer>>>());
 		}
 		
 		for (Integer integer : queryIdMatrix.keySet()) {
 			for (int i = 1; i < queryList.size(); i++) {
 				//initialize the matrix so as each cell is empty
 				queryIdMatrix.get(integer).put(queryList.get(i).getId(), new ArrayList<List<Integer>>());
 			}
 		}
 		
 		System.out.println("initial query matrix:");
 		for (Integer m : queryIdMatrix.keySet()) {
 			for (Integer n: queryIdMatrix.get(m).keySet()) {
 				System.out.println("( " + m + ", " + n + " )");
 			}
 			System.out.println("\n");
 		}
 		
 		//initial query table
 		queryTable = new HashMap<Integer, Boolean>();
 		for (Node node : qns.getQueryNodes()) {
 			queryTable.put(node.getId(), true);
 		}
 	}
 	
 	//mark query matrix based on the current gray node list 
 	//and query nodes
 	private static void markQueryMatrix(List<Value> grayNodesValue) throws Exception{
 		if (grayNodesValue.size() != 1) {
 			//System.out.println("invalid value size in driver");
 			throw new TwisterException("invalid value size in driver");
 		}
 		NodeVectorValue grayVector = (NodeVectorValue)grayNodesValue.get(0);
 		List<Node> grayNodes = grayVector.getGrayNodeList();
 		
 		for (int i = 0; i < grayNodes.size(); i++) {
 			Node markNode = grayNodes.get(i);
 			System.out.println("currently mark gray node: " + markNode.getId());
 			for (List<Integer> path : markNode.getTraceHistrory()) {
 				System.out.println("path:");
 				for (Integer pathelem : path) {
 					System.out.println(pathelem + " ");
 				}
 				System.out.println("");
 				Integer src = path.get(0);
 				Integer dst = markNode.getId();
 				
 				if (src != dst && queryTable.get(dst) != null && queryIdMatrix.get(src) != null) {
 					//add this path to cell[src][dst]
 					if (queryIdMatrix.get(src).get(dst) != null) {
 						queryIdMatrix.get(src).get(dst).add(path);
 						//remove this value because it can never lead us to valid xe-fragment
 						grayNodes.remove(markNode);
 						
 					}
 				}
 			}
 		}
 		//print current query matrix
 		for (Integer m : queryIdMatrix.keySet()) {
 			for (Integer n: queryIdMatrix.get(m).keySet()) {
 				System.out.println("( " + m + ", " + n + " )");
 				for (List<Integer> path : queryIdMatrix.get(m).get(n)) {
 					for (Integer integer : path) {
 						System.out.print(integer + " ");
 					}
 					System.out.println("");
 				}
 			}
 			System.out.println("\n");
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		/*
 		 * Main program to run parallel cap MapReduce job
 		 * @param args
 		 * @throws Exception
 		 * */
 		ParallCAPMain pcapMain = new ParallCAPMain();	
 		if (args.length != 4) {
 			String errorReport = "ParallelCap: the Correct arguments are \n"
 					+ "java cgl.imr.samples.parallcap.parallelcapmain "
 					+ "<partition file> <num map tasks> <num reduce tasks> <numLoop>";
 			System.out.println(errorReport);
 			System.exit(0);
 		}
 		
 		String partitionFile = args[0];
 		int numMapTasks = Integer.parseInt(args[1]);
 		int numReduceTasks = Integer.parseInt(args[2]);
 		int numLoop = Integer.parseInt(args[3]);
 		List<Value> grayNodes = null;
 
 		double beginTime = System.currentTimeMillis();
 		try {
 			grayNodes = ParallCAPMain.driveMapReduce(numMapTasks, numReduceTasks, partitionFile, numLoop);
 			System.out.println("Current gray nodes: ");
 			for (Value val : grayNodes) {
 				NodeVectorValue tmpVec = (NodeVectorValue)val;
 				for (Node node : tmpVec.getGrayNodeList()) {
 					System.out.println(node.getId());
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		double endTime = System.currentTimeMillis();
 		
 		// Print test stats
 		double timeInSeconds = ((double)(endTime - beginTime)) / 1000;
 		System.out
 				.println("------------------------------------------------------");
 		System.out.println("Parallel Cap took " + timeInSeconds
 				+ " seconds.");
 		System.out
 				.println("------------------------------------------------------");
 		
 		//exit(0) is necessary or else app will not return to os. reason unknown
 		System.exit(0);
 	}
 	
 	public static List<Value> driveMapReduce(int numMapTasks, int numReduceTasks, 
 			String partitionFile, int numLoop) throws Exception {
 		//JobConfigurations
 		Hashtable<String, String> properties = new Hashtable<String, String>();
 		properties.put("PATHLIMIT", String.valueOf(qns.getLength()));
 		JobConf jobConf = new JobConf("ParallCap-map-reduce"
 				+ UUIDGenerator.getInstance().generateTimeBasedUUID());
 		jobConf.setMapperClass(ParallCAPMapTask.class);
 		jobConf.setReducerClass(ParallelCAPReduceTask.class);
 		jobConf.setCombinerClass(ParallelCAPCombiner.class);
 		jobConf.setNumMapTasks(numMapTasks);
 		jobConf.setNumReduceTasks(numReduceTasks);
 		jobConf.setFaultTolerance();
 		jobConf.setProperties(properties);
 		TwisterDriver driver = new TwisterDriver(jobConf);
 		driver.configureMaps(partitionFile);
 		
 		List <Value> grayNodes = new ArrayList<Value>();
 		//initialize the dynamic data first
 		int grayNodeSize = qns.getQueryNodes().size();
 		for (Node node : qns.getQueryNodes()) {
 			node.setTag(CAPConstraints.Gray);
 		}
 		
 		NodeVectorValue nodeVecVal = new NodeVectorValue(grayNodeSize, qns.getQueryNodes());
 		grayNodes.add(nodeVecVal);
 		
 		int bfsIterCnt = 0;
 		for (; bfsIterCnt < numLoop; bfsIterCnt++) {
 			System.out.println("Search step " + bfsIterCnt);
 			MemCacheAddress memCacheKey = driver.addToMemCache(grayNodes);
 			TwisterMonitor monitor = driver.runMapReduceBCast(memCacheKey);
 			monitor.monitorTillCompletion();
 			driver.cleanMemCache();
 			grayNodes = ((ParallelCAPCombiner) driver.getCurrentCombiner())
 					.getResults();
 			markQueryMatrix(grayNodes);		
 		}
 		driver.close();
 		return grayNodes;
 		
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
