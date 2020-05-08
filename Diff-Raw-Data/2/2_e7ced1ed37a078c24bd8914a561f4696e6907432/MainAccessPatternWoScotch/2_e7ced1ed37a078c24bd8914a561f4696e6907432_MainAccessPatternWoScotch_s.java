 package com.volkan.accesspattern;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.volkan.Configuration;
 import com.volkan.interpartitiontraverse.JsonHelper;
 
 public class MainAccessPatternWoScotch extends MainAccessPatternWeight{
 
 	private static final Logger logger = LoggerFactory.getLogger(MainAccessPatternWoScotch.class);
 	
 	Map<Integer, SortedSet<Long>> partitionGidsMap;
 	
 	public static void main(String[] args) throws Exception {
 		MainAccessPatternWoScotch mainAccessPatternWoScotch = new MainAccessPatternWoScotch();
 		mainAccessPatternWoScotch.work();
 	}
 	
 	public MainAccessPatternWoScotch() {
 		super();
 		partitionGidsMap = new HashMap<Integer, SortedSet<Long>>();
 	}
 	
 	public void work() throws Exception {
 		db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
 		registerShutdownHook();
 		
 		boolean useExistingAccessPatterns = true;
 		
 		// RECOMMENDATION
 		Map<String, Object> jsonMap = JsonHelper
 				.createJsonMapWithDirectionsAndRelTypes(
 						Arrays.asList("OUT", "IN", "OUT"),
 						Arrays.asList("follows", "follows", "follows"));
 		String jsonsOutputDir = "src/main/resources/jsons/erdos/3depth/";
 		String ending = "out_in_out.json";
 
 		createOrUseExistingAPs(jsonMap, jsonsOutputDir, ending,
 				useExistingAccessPatterns);
 
 		// //2 Depths
 		jsonMap = JsonHelper
 				.createJsonMapWithDirectionsAndRelTypes(
 						Arrays.asList("OUT", "IN"),
 						Arrays.asList("follows", "follows"));
 		jsonsOutputDir = "src/main/resources/jsons/erdos/2depth/";
 		ending = "out_in.json";
 
 		createOrUseExistingAPs(jsonMap, jsonsOutputDir, ending,
 				useExistingAccessPatterns);
 		writeGidPartitionMapForRuby();
 		
 		writeGidPartitionMapForRubyForLastPartition();
 	}
 	
 	protected void processRandomID(TraversalDescription traversalDescription, 
 			Integer randomID, int count) throws Exception
 	{
 		SortedSet<Long> nodesInTraversal = 
 				collectConnectedNodeIDsOfStartNodeID(randomID, traversalDescription);
 		logger.info("randomID: "+randomID+" node count in traversal="
 						+nodesInTraversal.size()+" count: "+ count);	
 		
 		int partition = randomID % (PARTITION_COUNT) + 6474;
 		SortedSet<Long> gids = partitionGidsMap.get(partition);
 		if (gids == null) {
 			gids = new TreeSet<Long>();
 			partitionGidsMap.put(partition, gids);
 		}
 		gids.add(randomID.longValue());
 		gids.addAll(nodesInTraversal);
 	}
 	
 	/**
 	 * Write gid_partition_h_X for partitions except LAST_PARTITION
 	 * @throws IOException
 	 */
 	private void writeGidPartitionMapForRuby() throws IOException {
 		for (Integer partition : partitionGidsMap.keySet()) {
 			String fileName = Configuration.GID_PARTITION_MAP + "_" + partition;
 			BufferedWriter gpartInputFile = new BufferedWriter(new FileWriter(fileName));
 			
 			SortedSet<Long> gids = partitionGidsMap.get(partition);
 			for (Long gid : gids) {
 				gpartInputFile.write(gid+","+partition+"\n");
 			}
 			
 			assignMissingGidsToLastPartition(gpartInputFile, gids);
 			
 			if (gpartInputFile != null)
 				gpartInputFile.close();
 		}
 	}
 
 	protected void assignMissingGidsToLastPartition(
 			BufferedWriter gpartInputFile, SortedSet<Long> gids) throws IOException 
 	{
 		for (long i = 1; i <= MAX_NODE_COUNT; i++) {
 			if (!gids.contains(i)) {
 				gpartInputFile.write(i+","+LAST_PARTITION+"\n");
 			}
 		}
 	}
 
 	/**
 	 * Writes gid_partition_h_X just for the last partition
 	 * @throws IOException
 	 */
 	private void writeGidPartitionMapForRubyForLastPartition() throws IOException{
		String fileName = Configuration.GID_PARTITION_MAP;
 		BufferedWriter gpartInputFile = new BufferedWriter(new FileWriter(fileName));
 		
 		for (long i = 1; i <= MAX_NODE_COUNT; i++) {
 			gpartInputFile.write(i+","+LAST_PARTITION+"\n");
 		}
 		
 		if (gpartInputFile != null)
 			gpartInputFile.close();
 	}
 }
