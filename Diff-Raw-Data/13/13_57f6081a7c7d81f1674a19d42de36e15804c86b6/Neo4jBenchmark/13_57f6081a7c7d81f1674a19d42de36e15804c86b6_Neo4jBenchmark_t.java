 package de.uni.leipzig.IR15.Benchmark.neo4j;
 
 import org.neo4j.cypher.ExecutionEngine;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.helpers.collection.IteratorUtil;
 import org.neo4j.tooling.GlobalGraphOperations;
 
 import de.uni.leipzig.IR15.Benchmark.Benchmark;
 import de.uni.leipzig.IR15.Connectors.Neo4JConnector;
 import de.uni.leipzig.IR15.Importer.Neo4JImporter;
 import de.uni.leipzig.IR15.Support.Configuration;
 
 /**
  * Abstract Base Class for all benchmarks running on neo4j graph database. It
  * holds a reference to the database and the word index, it also cares about
  * generating random (and existing) node ids.
  * 
  * @author Martin 's1ck' Junghanns
  * 
  */
 public abstract class Neo4jBenchmark extends Benchmark {
 
 	protected Index<Node> index;
 	protected GraphDatabaseService neo4j;
 	protected ExecutionEngine engine;
 	protected Node startNode;
 	private int minOutDegree;
 	private static Configuration neo4jConfig = Configuration
 			.getInstance("neo4j");
 
 	/**
 	 * The maximum value for word id (needed for random id generation)
 	 */
 	protected int maxWordID;
 
 	/**
 	 * Setup the database connection and get the max w_id.
 	 */
 	@Override
 	public void setUp() {
 		neo4j = Neo4JConnector.getConnection();
 		engine = new ExecutionEngine(neo4j);
 		// get index
 		index = neo4j.index().forNodes("words");
 		// select the highest word_id
 		maxWordID = getMaxWordID();
 
 		minOutDegree = neo4jConfig.getPropertyAsInteger("min_outdegree");
 	}
 
 	/**
 	 * Get a random node before each run.
 	 */
 	@Override
 	public void beforeRun() {
		startNode = getRandomNode(minOutDegree);		
 	}
 
 	@Override
 	public void afterRun() {
 	}
 
 	/**
 	 * Cleanup after running benchmarks.
 	 */
 	@Override
 	public void tearDown() {
 		Neo4JConnector.destroyConnection();
 	}
 
 	@Override
 	@SuppressWarnings("unused")
 	public void warmup() {
 		// warmup the caches
 		log.info("Warming up the caches ...");
 		for (Node v : GlobalGraphOperations.at(neo4j).getAllNodes()) {
 		}
 		for (Relationship e : GlobalGraphOperations.at(neo4j)
 				.getAllRelationships()) {
 		}
 		log.info("done");
 	}
 
 	/**
 	 * Creates random id until a matching db entity is found.
 	 * 
 	 * @return random node
 	 */
 	protected Node getRandomNode() {
 		return getRandomNode(0);
 	}
 
 	/**
 	 * Returns a random node with an out degree greater or equal than the given
 	 * treshold.
 	 * 
 	 * @param treshold
 	 * @return random node
 	 */
 	protected Node getRandomNode(int treshold) {
 		Node v = null;
 		int id = 0;
 
 		while (v == null) {
 			id = r.nextInt(maxWordID);
 			v = index.get("w_id", id).getSingle();
 			if (v != null) {
 				if (IteratorUtil.asCollection(
 						v.getRelationships(Neo4JImporter.RelTypes.CO_S,
 								Direction.OUTGOING)).size() < treshold) {
 					v = null;
 				}
 			}
 		}
 		return v;
 	}
 
 	private int getMaxWordID() {
 		int maxWordID = Integer.MIN_VALUE;
 
 		for (Node v : GlobalGraphOperations.at(neo4j).getAllNodes()) {
 			if (v.hasProperty("w_id")) {
 				if ((Integer) v.getProperty("w_id") > maxWordID) {
 					maxWordID = (Integer) v.getProperty("w_id");
 				}
 			}
 		}
 		return maxWordID;
 	}
 	
 	@Override
 	public String getDatabaseName() {
 		return "Neo4j";
 	}
 }
