 package ly.scholr.neo4j.valuecontext;
 
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.index.lucene.QueryContext;
 import org.neo4j.index.lucene.ValueContext;
 import org.neo4j.server.plugins.*;
 
import java.util.Arrays;
 
 public class ValueContextPlugin extends ServerPlugin {
 
   @Name("post_long")
   @PluginTarget(Node.class)
   public Iterable<Node> postLong(
       @Source Node node,
       @Parameter(name = "index") String indexName,
       @Parameter(name = "key") String key,
       @Parameter(name = "value") Long value) {
 
     GraphDatabaseService graphDb = node.getGraphDatabase();
     Index<Node> index = graphDb.index().forNodes(indexName);
 
     Transaction tx = graphDb.beginTx();
     try {
       index.add(node, key, new ValueContext(value).indexNumeric());
       tx.success();
     } finally {
       tx.finish();
     }
    return Arrays.asList();
   }
 
  @Name("get_long_range")
  @PluginTarget(Node.class)
  public Iterable<Node> getLongRange(
      @Source Node node,
       @Parameter(name = "index") String indexName,
       @Parameter(name = "key") String key,
       @Parameter(name = "min") Long minNullable,
       @Parameter(name = "max") Long maxNullable ) {
 
     long min = minNullable == null ? Long.MIN_VALUE : minNullable;
     long max = maxNullable == null ? Long.MAX_VALUE : maxNullable;
     Sort sort = new Sort(new SortField(key, SortField.LONG, false));
     Object query = NumericRangeQuery.newLongRange(key, min, max, true, true);
 
    GraphDatabaseService graphDb = node.getGraphDatabase();
     Index<Node> index = graphDb.index().forNodes(indexName);
 
     Iterable<Node> hits;
     Transaction tx = graphDb.beginTx();
     try {
       hits = index.query(key, new QueryContext(query).sort(sort));
       tx.success();
     } finally {
       tx.finish();
     }
     return hits;
   }
 
 }
