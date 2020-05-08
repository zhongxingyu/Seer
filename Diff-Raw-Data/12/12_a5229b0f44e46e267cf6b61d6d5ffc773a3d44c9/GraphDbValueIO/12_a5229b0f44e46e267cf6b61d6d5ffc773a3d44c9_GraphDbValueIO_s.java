 package lapd.databases.neo4j;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 import org.neo4j.cypher.javacompat.ExecutionEngine;
 import org.neo4j.cypher.javacompat.ExecutionResult;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.tooling.GlobalGraphOperations;
 import org.osgi.framework.Bundle;
 
 public class GraphDbValueIO extends AbstractGraphDbValueIO {
 	
 	private static final GraphDbValueIO instance;
 	
 	static {
         try { 
         	instance = new GraphDbValueIO();
         } catch (IOException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
 	
 	public static GraphDbValueIO getInstance() {
 		return instance;
 	}
 	
 	private final GraphDbValueInsertionVisitor graphDbValueInsertionVisitor;
 	private IValueFactory valueFactory;
 	private final GraphDatabaseService graphDb;
 	private final Index<Node> nodeIndex;
 	private String dbDirectoryPath;
 	private final ExecutionEngine queryEngine;
 	private final GlobalGraphOperations graphOperations;
 	
 	
 	private GraphDbValueIO() throws IOException {
 		Map<String, String> config = new HashMap<String, String>();
		config.put("cache_type", "soft");
 		config.put("use_memory_mapped_buffers", "true");
 		config.put("logical_log_rotation_threshold", "500M");
 		config.put("neostore.nodestore.db.mapped_memory", "100M");
 		config.put("neostore.relationshipstore.db.mapped_memory", "100M");
 		config.put("neostore.propertystore.db.mapped_memory", "100M");
 		config.put("neostore.propertystore.db.strings.mapped_memory", "100M");
 		config.put("neostore.propertystore.db.arrays.mapped_memory", "0M");
 		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(fetchDbPath()).setConfig(config).newGraphDatabase();
 		queryEngine = new ExecutionEngine(graphDb);
 		registerShutdownHook(graphDb);
 		nodeIndex = graphDb.index().forNodes("nodes");
 		graphOperations = GlobalGraphOperations.at(graphDb);
 		graphDbValueInsertionVisitor = new GraphDbValueInsertionVisitor(graphDb, nodeIndex);		
 	}
 	
 	public void init(IValueFactory valueFactory) {
 		this.valueFactory = valueFactory;
 	}
 	
 	public String getDbDirectoryPath() {
 		return dbDirectoryPath;
 	}	
 	
 	private String fetchDbPath() throws IOException {
 		Properties prop = new Properties();
 		InputStream stream;
 		Bundle bundle = Platform.getBundle("lapd");
 		if (bundle != null)
 			stream = FileLocator.openStream(bundle,	new Path("bin/config.properties"), false);
 		else
 			stream = ClassLoader.class.getResourceAsStream("/config.properties");
 		prop.load(stream);
 		String neo4jDbName = prop.getProperty("neo4jDbName");
 		String userSpecifiedDir = prop.getProperty("databasesDirectory");		
 		if (userSpecifiedDir != null)
 			dbDirectoryPath = userSpecifiedDir;
 		else
 			dbDirectoryPath = System.getProperty("user.home") + "/databases";
 		return dbDirectoryPath + "/" + neo4jDbName;
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
 		if (nodeIndex.get("id", id).size() != 0)
 			throw new GraphDbMappingException("Cannot write value to database. The id already exists.");
 		writeToDb(id, value);
 	}
 
 	private void writeToDb(String id, IValue value)	throws GraphDbMappingException {
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
 	public void write(String id, IValue value, boolean deleteOld) throws GraphDbMappingException {
 		if (!deleteOld)
 			write(id, value);
 		else {
 			queryEngine.execute("start n=node:nodes(id = '" + id + "') match n-[r]-() delete n, r");
 			writeToDb(id, value);
 		}
 	}
 	
 	@Override
 	public IValue read(String id, TypeStore typeStore) throws GraphDbMappingException {
 		Node node = nodeIndex.get("id", id).getSingle();
 		if (node == null)
 			throw new IdNotFoundException("Id " + id + " not found.");
 		return new TypeDeducer(node, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(node, valueFactory, typeStore));
 	}
 
 	@Override
 	public IValue read(String id, Type type, TypeStore typeStore) throws GraphDbMappingException {
 		Node node = nodeIndex.get("id", id).getSingle();
 		if (node == null)
 			throw new IdNotFoundException("Id " + id + " not found.");
 		try {
 			return type.accept(new GraphDbValueRetrievalVisitor(node, valueFactory, typeStore));
 		}
 		catch (NotFoundException e) {
 			throw new GraphDbMappingException("Could not find value. The id and type probably did not match.");
 		}
 	}
 
 	@Override
 	public IValue executeQuery(String query, Type type, TypeStore typeStore, boolean isCollection) throws GraphDbMappingException {
 		ExecutionResult result = queryEngine.execute(query);
 		if (!result.columns().isEmpty()) {
 			Iterator<Node> column = result.columnAs(result.columns().get(0));
 			if (isCollection) {				
 				List<IValue> resultsList = new ArrayList<IValue>();			
 				while (column.hasNext()) {
 					Node node = column.next();					
 					IValue value = type.getElementType().accept(new GraphDbValueRetrievalVisitor(node, valueFactory, typeStore));
 					resultsList.add(value);
 				}
 				return valueFactory.set(resultsList.toArray(new IValue[resultsList.size()]));
 			}
 			else if (column.hasNext()) {
 				Node node = column.next();
 				try {
 					return type.accept(new GraphDbValueRetrievalVisitor(node, valueFactory, typeStore));
 				}
 				catch (NotFoundException e) {
 					throw new GraphDbMappingException("The type probably did not match the query result.");
 				}
 			}
 		}
 		throw new GraphDbMappingException("No query results were found.");
 	}
 
 	@Override
 	public boolean idExists(String id) {
 		Node node = nodeIndex.get("id", id).getSingle();
 		return node == null ? false : true;
 	}
 
 	@Override
 	public ISet executeJavaQuery(int queryId, String graphId, Type type, TypeStore typeStore) throws GraphDbMappingException {		
 		switch(queryId) {
 			case 1: return Queries.recursiveMethodsQ(graphOperations.getAllNodes(), valueFactory, type.getElementType(), typeStore);
 			case 2: return Queries.switchQ(nodeIndex, valueFactory, type.getElementType(), typeStore);
 			case 3: return Queries.reachabilityQ(nodeIndex.get("loc", graphId).getSingle(), valueFactory, type.getElementType(), typeStore);
 			case 4: return Queries.exceptionQ(nodeIndex, valueFactory, type.getElementType(), typeStore);
 			default: throw new GraphDbMappingException("Unknown query id.");
 		}
 	}
 
 }
