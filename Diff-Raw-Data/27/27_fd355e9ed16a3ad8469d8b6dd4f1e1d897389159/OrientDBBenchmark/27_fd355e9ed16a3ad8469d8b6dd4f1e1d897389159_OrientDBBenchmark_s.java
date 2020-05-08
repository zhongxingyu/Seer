 package de.uni.leipzig.IR15.Benchmark.orientdb;
 
 import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
 import com.orientechnologies.orient.core.db.record.OIdentifiable;
 import com.orientechnologies.orient.core.index.OIndexUnique;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 
 import de.uni.leipzig.IR15.Benchmark.Benchmark;
 import de.uni.leipzig.IR15.Connectors.OrientDBConnector;
 import de.uni.leipzig.IR15.Support.Configuration;
 
 /**
  * Abstract Base Class for all benchmarks running on orient graph database. It
  * holds a reference to the database and the word index, it also cares about
  * generating random (and existing) node ids.
 * 
  * @author Sascha 'peil' Ludwig
 * 
  */
 public abstract class OrientDBBenchmark extends Benchmark {
 
 	protected ODocument startVertex;
 	protected OGraphDatabase orientdb;
 	protected OIndexUnique index;
 	protected int maxID;
 
 	private int minOutDegree;
 	private static Configuration orientCfg = Configuration
 			.getInstance("orientdb");
 
 	/**
 	 * Setup the database connection, get the index and get the maximum word id.
 	 */
 	@Override
 	public void setUp() {
 		orientdb = OrientDBConnector.getConnection();
 		index = (OIndexUnique) orientdb.getMetadata().getIndexManager()
 				.getIndexInternal("word_id_index");
 		log.info(index);
 		maxID = findMaxWordID();
 		minOutDegree = orientCfg.getPropertyAsInteger("min_outdegree");
 	}
 
 	/**
 	 * Find a random start vertex.
 	 */
 	@Override
 	public void beforeRun() {
 		startVertex = getRandomNode(minOutDegree);
 	}
 
 	@Override
 	public void afterRun() {
 
 	}
 
 	/**
 	 * Find the maxium word id.
	 * 
 	 * @return
 	 */
 	// gets the max WordID. Needed for random WordID
 	public int findMaxWordID() {
		int m = 0;
		int tmp = 0;
 
 		// get all Vertices aka words
 		Iterable<ODocument> allWords = orientdb.browseVertices();
 		// for all words
 		for (ODocument word : allWords) {
 			tmp = word.field("w_id");
 			// check if w_id is bigger than current max
 			if (tmp > m) {
 				m = tmp;
 			}
 		}
 
 		return m;
 	}
 
 	/**
 	 * Returns a random starting vertex with an out degree greater or equal than
 	 * the given threshold.
	 * 
 	 * @param threshold
 	 *            minimum out degree
 	 * @return random starting vertex
 	 */
 	private ODocument getRandomNode(int threshold) {
 		ODocument sVertex = null;
 		ODocument sinnlosesRumgeCasteVariable = null;
 		int id = 0;
 
 		while (sVertex == null) {
 			id = r.nextInt(maxID);
 
 			if (index.get(id) != null) {
 				sVertex = (ODocument) index.get(id).getRecord();
 				int e = 0;
 
 				for (OIdentifiable outEdge : orientdb.getOutEdges(sVertex)) {
 					sinnlosesRumgeCasteVariable = (ODocument) outEdge
 							.getRecord();
 					if (sinnlosesRumgeCasteVariable.field("type").toString()
 							.equalsIgnoreCase("co_s")) {
 						e++;
 					}
 				}
 
 				if (e < threshold) {
 					sVertex = null;
 				}
 			}
 		}
 		return sVertex;
 	}
 
 	@Override
 	public void tearDown() {
 		OrientDBConnector.destroyConnection();
 	}
 
 	@Override
 	public void warmup() {
 	}
 }
