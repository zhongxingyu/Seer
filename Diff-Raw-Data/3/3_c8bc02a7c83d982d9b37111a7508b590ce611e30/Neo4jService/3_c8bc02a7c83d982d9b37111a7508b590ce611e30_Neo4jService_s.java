 package org.neo4j.training.backend;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.helpers.collection.IteratorUtil;
 import org.neo4j.test.ImpermanentGraphDatabase;
 import org.neo4j.tooling.GlobalGraphOperations;
 import org.slf4j.Logger;
 
 import java.util.*;
 
 import static org.neo4j.helpers.collection.MapUtil.map;
 import static org.neo4j.helpers.collection.MapUtil.stringMap;
 
 /**
 * @author mh
 * @since 08.04.12
 */
 class Neo4jService {
 
     private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Neo4jService.class);
 
     private GraphDatabaseService gdb;
 
     private Index index;
     private CypherQueryExecutor cypherQueryExecutor;
     private CypherExportService cypherExportService;
     private String version;
     private boolean initialized;
     private ShutdownHook shutdownHook;
     private Collection<String> history = new LinkedHashSet<>();
 
     Neo4jService() throws Throwable {
         this(createInMemoryDatabase(),true);
     }
 
     private static ImpermanentGraphDatabase createInMemoryDatabase() throws Throwable {
         try {
             ImpermanentGraphDatabase db = new ImpermanentGraphDatabase(stringMap("execution_guard_enabled", "true"));
             db.cleanContent();
             return db;
         } catch(RuntimeException re) {
             Throwable t=re.getCause();
             if (t instanceof RuntimeException) throw (RuntimeException)t;
             if (t instanceof Error) throw (Error)t;
             throw t;
         }
     }
 
     Neo4jService(GraphDatabaseService gdb) throws Throwable {
         this(gdb,false);
     }
 
     private Neo4jService(GraphDatabaseService gdb, boolean ownsDatabase) {
         if (gdb == null) throw new IllegalArgumentException("Graph Database must not be null");
         this.gdb = gdb;
         this.ownsDatabase = ownsDatabase;
         index = new Index(this.gdb);
         cypherQueryExecutor = new CypherQueryExecutor(gdb,index);
         cypherExportService = new CypherExportService(gdb);
     }
 
     public Map cypherQueryViz(String query) {
         final boolean invalidQuery = query == null || query.trim().isEmpty() || cypherQueryExecutor.isMutatingQuery(query);
         return invalidQuery ? cypherQueryViz((CypherQueryExecutor.CypherResult) null) : cypherQueryViz(cypherQuery(query));
     }
     public Map cypherQueryViz(CypherQueryExecutor.CypherResult result) {
         Transaction tx = gdb.beginTx();
         try {
             final SubGraph subGraph = SubGraph.from(gdb).markSelection(result);
             return map("nodes", subGraph.getNodes().values(), "links", subGraph.getRelationshipsWithIndexedEnds().values());
         } finally {
             tx.success();
             tx.finish();
         }
     }
 
     public String exportToCypher() {
         return cypherExportService.export();
     }
 
     public Collection<Map<String,Object>> cypherQueryResults(String query) {
         Collection<Map<String,Object>> result=new ArrayList<Map<String, Object>>();
         for (Map<String, Object> row : cypherQuery(query)) {
             result.add(row);
         }
         return result;
     }
 
     public CypherQueryExecutor.CypherResult initCypherQuery(String query) {
         return cypherQueryExecutor.cypherQuery(query,null);
     }
     public CypherQueryExecutor.CypherResult cypherQuery(String query) {
         addToHistory(query);
         return cypherQueryExecutor.cypherQuery(query,version);
     }
 
     private void addToHistory(String query) {
         if (query==null) return;
         query = query.trim();
         if (history.contains(query)) history.remove(query);
         history.add(query);
     }
 
     public void stop() {
         runShutdownHook();
         shutdownDb();
     }
 
     private void shutdownDb() {
         if (gdb == null) return;
         LOG.warn("Shutting down service "+this);
         if (ownsDatabase) gdb.shutdown();
         index = null;
         cypherQueryExecutor=null;
         cypherExportService =null;
         gdb=null;
     }
 
     private void runShutdownHook() {
         if (shutdownHook == null) return;
         try {
             shutdownHook.shutdown(this);
         } catch (Throwable t) {
             LOG.error("Error during shutdown",t);
         }
     }
 
     public void deleteReferenceNode() {
         if (rootNodeRemovalNotAllowed() || !hasReferenceNode()) return;
         final Node root = gdb.getReferenceNode();
         if (root!=null) {
             final Transaction tx = gdb.beginTx();
             try {
                 root.delete();
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     private boolean rootNodeRemovalNotAllowed() {
         return !ownsDatabase || isInitialized();
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         if (version==null || version.trim().isEmpty()) this.version=null;
         else {
             version = version.replaceAll("^(\\d+\\.\\d+).*","$1");
             if (!version.matches("\\d+\\.\\d+")) throw new IllegalArgumentException("Incorrect version string "+version);
             this.version = version;
         }
     }
 
     public boolean hasReferenceNode() {
         try {
             Transaction tx = gdb.beginTx();
             try {
                 boolean result = gdb.getReferenceNode() != null;
                 tx.success();
                 return result;
             } finally {
                 tx.finish();
             }
         } catch (NotFoundException nfe) {
             return false;
         }
     }
 
     public boolean isMutatingQuery(String query) {
         return cypherQueryExecutor.isMutatingQuery(query);
     }
     public boolean isCypherQuery(String query) {
         return cypherQueryExecutor.isCypherQuery(query);
     }
 
     public GraphDatabaseService getGraphDatabase() {
         return gdb;
     }
 
     public void importGraph(SubGraph graph) {
         final Transaction tx = gdb.beginTx();
         try {
             graph.importTo(gdb, hasReferenceNode());
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     private final boolean ownsDatabase;
     public boolean doesOwnDatabase() {
         return ownsDatabase;
     }
 
     public Neo4jService initializeFrom(SubGraph graph) {
         importGraph(graph);
         setInitialized();
         return this;
     }
 
     public boolean isInitialized() {
         return initialized;
     }
 
     public void setInitialized() {
         this.initialized = true;
     }
 
     public Map exportToJson(Map<String, Object> graph) {
         Map<String,Map<String,Object>> result=new HashMap<String, Map<String, Object>>(graph.size());
         for (Map.Entry<String, Object> entry : graph.entrySet()) {
             Map<String, Object> data = null;
             if (entry.getValue() instanceof Map) {
                 //noinspection unchecked
                 data = (Map<String, Object>) entry.getValue();
             }
             if (entry.getValue() instanceof PropertyContainer) {
                 final PropertyContainer value = (PropertyContainer) entry.getValue();
                 if (value instanceof Node) data=SubGraph.toMap((Node)value);
                 if (value instanceof Relationship) data=SubGraph.toMap((Relationship)value);
             }
             if (data!=null) result.put(entry.getKey(),data);
         }
         return result;
     }
 
     public Transaction begin() {
         return gdb.beginTx();
     }
 
     public Collection<String> getHistory() {
         return history;
     }
 
     public boolean isEmpty() {
         boolean refNode = hasReferenceNode();
         Transaction tx = gdb.beginTx();
         try {
             int count = IteratorUtil.count(GlobalGraphOperations.at(gdb).getAllNodes());
             int emptyCount = refNode ? 1 : 0;
             tx.success();
             return emptyCount >= count;
         } finally {
             tx.finish();
         }
     }
 
     public interface ShutdownHook {
         void shutdown(Neo4jService neo4jService);
     }
     public void setShutdownHook(ShutdownHook shutdownHook) {
         this.shutdownHook = shutdownHook;
     }
 }
