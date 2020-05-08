 package fatworm.storage;
 
 import fatworm.storage.bucket.Bucket;
 import fatworm.record.Schema;
 
 public class SchemaOnDisk {
     private Bucket bucket;
     private Schema schema;
 
     private SchemaOnDisk() {
         schema = null;
         bucket = null;
     }
 
     public static SchemaOnDisk load(IOHelper io, int block) {
         SchemaOnDisk ret = new SchemaOnDisk();
         ret.bucket = Bucket.load(io, block);
         if (ret.bucket == null)
             return null;
         byte[] data = ret.bucket.getData();
        ret.schema = new Schema(data, 0);
         return ret;
     }
 
     public static SchemaOnDisk create(IOHelper io, fatworm.record.Schema schema) {
         SchemaOnDisk ret = new SchemaOnDisk();
         ret.schema = schema;
         ret.bucket = Bucket.create(io, null);
         return ret;
     }
 
     public int save() throws java.io.IOException {
         bucket.setData(schema.getBytes());
         return bucket.save();
     }
 
     public Schema schema() {
         return schema;
     }
 
     public int estimatedTupleSize() {
         return schema.estimatedLength() + 4 * schema.columnCount();
     }
 }
