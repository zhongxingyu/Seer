 package cgl.imr.samples.parallcap.ivy.a;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import cgl.imr.base.Key;
 import cgl.imr.base.MapOutputCollector;
 import cgl.imr.base.MapTask;
 import cgl.imr.base.TwisterException;
 import cgl.imr.base.Value;
 import cgl.imr.base.impl.JobConf;
 import cgl.imr.base.impl.MapperConf;
 import cgl.imr.data.file.FileData;
 import cgl.imr.types.IntKey;
 import cgl.imr.types.MemCacheAddress;
 import cgl.imr.worker.MemCache;
 
 public class ParallCAPMapTask implements MapTask {
 	
 	private StaticGraphData graph;
 	private Map<Integer, Node> headNodes;
 	private JobConf jobConf;
 	private Map<Integer, List<Node>> nodeMap;
 
 	@Override
 	public void close() throws TwisterException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void configure(JobConf jobConf, MapperConf mapConf)
 			throws TwisterException {
 		// TODO Auto-generated method stub
 		this.jobConf = jobConf;
 		FileData fileData = (FileData) mapConf.getDataPartition();
 		this.graph = new StaticGraphData();
 		this.nodeMap = new HashMap<Integer, List<Node>>();
 		this.headNodes = new HashMap<Integer, Node>();
 		
 		try {
 			this.graph.loadDataFromTextFile(fileData.getFileName());
 		} catch (Exception e) {
 			throw new TwisterException(e);
 		}
 		
 		for (List<Node> list : graph.getAdjlist()) {
 			headNodes.put(list.get(0).getId(), list.get(0));
 			nodeMap.put(list.get(0).getId(), list.subList(1, list.size()));
 		}
 	}
 
 	/*
 	 * map task in charge of the local bfs search, go one step further in each iteration
 	 * */
 	@Override
 	public void map(MapOutputCollector mapOutputCollector, Key key, Value val)
 			throws TwisterException {
 		System.out.println("MapTask is started!");
 		System.out.println("Job: " + jobConf.getJobId() + "\n" +
 			"MapTask: " + jobConf.getMapClass());
 		
 		//load gray value data from memcache
 		MemCacheAddress memCacheKey = (MemCacheAddress) val;
 		if (memCacheKey.getRange() != 1) {
 			throw new TwisterException("MemCache size is not 1.");
 		}
 		NodeVectorValue grayNodes = (NodeVectorValue) (MemCache
 				.getInstance().get(this.jobConf.getJobId(),
 				memCacheKey.getMemCacheKeyBase() + memCacheKey.getStart()));
 		
 		Map<Key, Value> mapOutputKeyValues = new HashMap<Key, Value>();
 		
 		List<Node> curList = null;
 		
 		//mark nodes to gray according to grayNodes vector
 		for (Node grayNode : grayNodes.getGrayNodeList()) {
 			curList = nodeMap.get(grayNode.getId());
 			for (Node tmp : curList) {
 				//Node toMarkNode = new Node(tmpNode.getId());
 				/*
 				System.out.println("to mark gray node id: " + toMarkNode.getId());
 				System.out.println("to mark node initial trace path:");
 				for (List<Integer> tmpPath: toMarkNode.getTraceHistrory()) {
 					for (Integer tmp : tmpPath) {
 						System.out.println(" " + tmp);
 					}
 					System.out.println("");
 				}
 				*/
 				Node toMarkNode = new Node(tmp.getId());
 				toMarkNode.setTag(CAPConstraints.Gray);
 				if (grayNode.getPathCount() != 0) {
 					for (List<Integer> ppath : grayNode.getTraceHistrory()) {
 						List<Integer> path = new ArrayList<Integer>(ppath);
 						path.add(grayNode.getId());
 						/*
 						System.out.println("current path is");
 						for (Integer interger : path) {
 							System.out.print(interger + " ");
 						}
 						*/
 						toMarkNode.getTraceHistrory().add(path);
 						toMarkNode.getNumOfTraceNodes().add(path.size());
 						toMarkNode.incPathCnt();
 					}
 				} else { //first add trace node
 					List<Integer> path = new ArrayList<Integer>();
 					path.add(grayNode.getId());
 					toMarkNode.getTraceHistrory().add(path);
 					toMarkNode.getNumOfTraceNodes().add(path.size());
 					toMarkNode.incPathCnt();
 				}
 				
 				Node markedNode = null;
 				if ( (markedNode = (Node)mapOutputKeyValues.get(new IntKey(toMarkNode.getId()))) == null)
 					mapOutputKeyValues.put(new IntKey(toMarkNode.getId()), toMarkNode);
 				else {
 					//we need to merge trace paths if current mark node has more than one
 					//ancestors for the same length
 					for (List<Integer> pathList : markedNode.getTraceHistrory()) {
 						toMarkNode.getTraceHistrory().add(pathList);
 						toMarkNode.getNumOfTraceNodes().add(pathList.size());
 						toMarkNode.incPathCnt();
 					}
 					mapOutputKeyValues.put(new IntKey(toMarkNode.getId()), toMarkNode);
 				}
 				
 				
 				//print gray node current history path
 				System.out.print("Node id:" + toMarkNode.getId() + "\n" + " path");
 				int i = 0;
 				for (List<Integer>  list : toMarkNode.getTraceHistrory()) {
 					System.out.print(i + ": ");
 					for (Integer integer : list) {
 						System.out.print(integer + " ");
 					}
 					i++;
 				}
 				System.out.println("");
 			}
 		}
 		
 		// collect
 		if (!mapOutputKeyValues.isEmpty()) {
 			mapOutputCollector.collect(mapOutputKeyValues);
 		} else {
			System.out.println("Empty gray node list!");
 		}
 		
 		mapOutputKeyValues.clear();
 		System.out.println("Map task is completed!");
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
