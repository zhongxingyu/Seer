 package org.pingles.cascading.neo4j;
 
 import cascading.tuple.Fields;
 
 import java.io.Serializable;
 
 public class IndexSpec implements Serializable {
     private final String indexName;
     private final Fields fields;
     private final Kind kind;
     public static final IndexSpec BY_ID = new IndexSpec( Kind.BY_ID );
 
     /**
      * Create an IndexSpec
      * @param nodeTypeName - e.g. "users", or "pages" etc.
      * @param fields
      */
     public IndexSpec(String nodeTypeName, Fields fields) {
         this.indexName = nodeTypeName;
         this.fields = fields;
         this.kind = Kind.USER;
     }
 
    private IndexSpec(Kind kind) {
         this.kind = kind;
         this.indexName = "";
         this.fields = Fields.NONE;
     }
 
    private static enum Kind
     {
         BY_ID, USER
     }
 
     public String getNodeTypeName() {
         return indexName;
     }
 
     public Fields getIndexProperties() {
         return fields;
     }
 
     public String getFirstIndexPropertyName() {
         return fields.get(0).toString();
     }
 
     public boolean isByID () {
         return this.kind == Kind.BY_ID;
     }
 }
