 package es.uah.cc.ie.metadatastatistics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ResourceSampleImpl implements ResourceSample {
 
     private String name;
     private MetadataSchema schema;
     private ResourceSource source;
     private HashMap<String, Integer> haveFieldCounter = new HashMap<String, Integer>();
     private int validCounter = 0;
     private int size = 0;
 
     public ResourceSampleImpl(String name, MetadataSchema ms, ResourceSource rs) {
         this.name = name;
         this.schema = ms;
         this.source = rs;
         this.processResourceSource();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public ResourceSource getSource() {
         return source;
     }
 
     public final int countHaveField(String field) throws NoSuchFieldException {
         if (!getSchema().getFields().contains(field)) {
             throw new NoSuchFieldException("The resources in this ResourceSample do not have this field");
         }
         return this._countHaveField(field);
     }
 
     /**
      * Counts the number of resources in this ResourceSample which has certain
      * metadata field. This method is called by contHasField when the field
      * exists. This method does not have to check the field existence.
      *
      * @param field the field the resources must have.
      * @return the number of resources which has the specified metadata field.
      */
     protected int _countHaveField(String field) {
         if (this.haveFieldCounter.containsKey(field)) {
             return this.haveFieldCounter.get(field);
         } else {
             return 0;
         }
     }
 
     public MetadataSchema getSchema() {
         return this.schema;
     }
 
     public int countValid() {
         return this.validCounter;
     }
     
     public int incValid() {
         return ++ this.validCounter;
     }
     
     public int size() {
         return this.size;
     }
     
     protected int incSize() {
         return ++ this.size;
     }
 
     private void processResourceSource() {
         ResourceSource src = this.getSource();
         ArrayList<String> fields = this.getSchema().getFields();
         for (Resource res: src) {
             for (String field : fields) {
                 int count = 0;
                 try {
                     count = this.countHaveField(field);
                 } catch (NoSuchFieldException ex) {
                     Logger.getLogger(ResourceSampleImpl.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 this.haveFieldCounter.put(field, count);
             }
             if (res.isValid()) {
                 this.incValid();
             }
             this.incSize();
         }
     }
 }
