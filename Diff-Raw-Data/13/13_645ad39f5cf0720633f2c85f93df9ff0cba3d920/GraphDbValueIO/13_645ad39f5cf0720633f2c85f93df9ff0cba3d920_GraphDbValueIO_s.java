 package lapd.neo4j;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.index.Index;
 
 public class GraphDbValueIO implements IGraphDbValueIO {
 	
 	private final GraphDbValueInsertionVisitor graphDbValueInsertionVisitor;
 	private final IValueFactory valueFactory;
 	private final GraphDatabaseService graphDb;
 	private final Index<Node> nodeIndex;
 	
 	public GraphDbValueIO(IValueFactory valueFactory) throws IOException {
 		this.valueFactory = valueFactory;
 		String dbPath = getDbPath();
 		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
 		registerShutdownHook(graphDb);
 		nodeIndex = graphDb.index().forNodes("nodes");
 		graphDbValueInsertionVisitor = new GraphDbValueInsertionVisitor(graphDb);		
 	}
 
 	private String getDbPath() throws IOException {
 		Properties prop = new Properties();
		InputStream stream = ClassLoader.class.getResourceAsStream("/config.properties");
 		prop.load(stream);
 		String neo4jDbName = prop.getProperty("neo4jDbName");
 		String userSpecifiedDir = prop.getProperty("databasesDirectory");
 		if (userSpecifiedDir != null)
 			return userSpecifiedDir + "/" + neo4jDbName;
 		return System.getProperty("user.home") + "/databases/" + neo4jDbName;
 	}
 	
 	private static void registerShutdownHook(final GraphDatabaseService graphDb)
 	{
 	    Runtime.getRuntime().addShutdownHook(new Thread() {
 	        @Override
 	        public void run() {
 	            graphDb.shutdown();
 	        }
 	    });
 	}
 	
 	public void shutdown() {
 		graphDb.shutdown();
 	}
 
 	@Override
 	public void write(String id, IValue value) throws GraphDbMappingException {
 		Transaction tx = graphDb.beginTx();
 		try {
 			Node node = value.accept(graphDbValueInsertionVisitor);
 			node.setProperty("id", id);
 			nodeIndex.add(node, "id", id);
 			tx.success();
 		}
 		catch (Exception e) { 
 			throw new GraphDbMappingException(e.getMessage()); 
 		}
 		finally {
 			tx.finish();
 		}
 	}
 
 	@Override
 	public IValue read(String id, Type type) throws GraphDbMappingException {
 		Node node = nodeIndex.get("id", id).getSingle();
 		IValue retrievedValue = type.accept(new GraphDbTypeRetrievalVisitor(node, valueFactory));
 		return retrievedValue;
 	}	
 
 }
