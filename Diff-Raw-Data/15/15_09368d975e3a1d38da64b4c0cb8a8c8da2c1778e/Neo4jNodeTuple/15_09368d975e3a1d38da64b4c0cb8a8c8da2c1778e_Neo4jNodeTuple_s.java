 package org.pingles.cascading.neo4j.hadoop;
 
 import cascading.tuple.Fields;
 import cascading.tuple.TupleEntry;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 import org.pingles.cascading.neo4j.IndexSpec;
 
 public class Neo4jNodeTuple extends Neo4jTuple implements Neo4jWritable {
     private final TupleEntry tupleEntry;
     private IndexSpec indexSpec;
     private Fields sinkFields;
 
     public Neo4jNodeTuple(Fields sinkFields, TupleEntry tupleEntry) {
         this(sinkFields, tupleEntry, null);
     }
 
     public Neo4jNodeTuple(Fields sinkFields, TupleEntry tupleEntry, IndexSpec indexSpec) {
         this.tupleEntry = tupleEntry;
         this.indexSpec = indexSpec;
         this.sinkFields = sinkFields;
     }
 
     public void store(GraphDatabaseService service) {
         Transaction transaction = service.beginTx();
 
         try {
             Node node = service.createNode();
             Fields fields = sinkFields;
 
             for (int idx = 0; idx < fields.size(); idx++) {
                 String fieldName = (String) fields.get(idx);
                 Object fieldValue = tupleEntry.getTuple().getObject(idx);
 
                node.setProperty(cleanPropertyName(fieldName), fieldValue);  // FIX: Caused by: com.sun.jersey.api.client.ClientHandlerException: java.net.SocketTimeoutException: Read timed out
             }
 
             if (indexSpec != null) {
                 Index<Node> index = service.index().forNodes(indexSpec.getNodeTypeName());
 
                 Fields indexProperties = indexSpec.getIndexProperties();
                 for (Comparable propName : indexProperties) {
                     index.add(node, cleanPropertyName(propName.toString()), tupleEntry.getObject(propName));
                 }
             }
 
             transaction.success();
         } finally {
             transaction.finish();
         }
     }
 
 }
