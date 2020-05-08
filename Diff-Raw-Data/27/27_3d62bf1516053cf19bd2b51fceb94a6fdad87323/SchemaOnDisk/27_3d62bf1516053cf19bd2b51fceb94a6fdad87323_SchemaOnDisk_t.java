 package fatworm.storage;
 
 import fatworm.storage.bucket.Bucket;
 import fatworm.record.Schema;
 import fatworm.util.ByteBuffer;
 import fatworm.storage.bplustree.*;
 
 import java.util.Map;
 import java.util.HashMap;
 
 public class SchemaOnDisk {
     private Bucket bucket;
     private Schema schema;
     private Map<String, BPlusTree> index;
 
     private SchemaOnDisk() {
         schema = null;
         bucket = null;
         index = new HashMap<String, BPlusTree>();
     }
 
     public static SchemaOnDisk load(IOHelper io, int block) {
         SchemaOnDisk ret = new SchemaOnDisk();
         ret.bucket = Bucket.load(io, block);
         if (ret.bucket == null)
             return null;
 
         byte[] data = ret.bucket.getData();
         ByteBuffer buffer = new ByteBuffer(data);
 
         ret.schema = new Schema(buffer);
 
         int len = ret.schema.columnCount();
         for (int i = 0; i < len; ++i) {
             int iBlock = buffer.getInt();
             if (iBlock != 0) {
                 DataAdapter da = ret.adapter(ret.schema.name(i));
                 BPlusTree tree = BPlusTree.load(io, da.comparator(), iBlock);
                 ret.index.put(ret.schema.name(i), tree);
                System.out.println("loaded index on " + ret.schema.name(i));
             }
         }
         return ret;
     }
 
     public static SchemaOnDisk create(IOHelper io, fatworm.record.Schema schema) {
         SchemaOnDisk ret = new SchemaOnDisk();
         ret.schema = schema;
         ret.bucket = Bucket.create(io);
         int len = schema.columnCount();
         return ret;
     }
 
     public void remove() throws java.io.IOException {
         bucket.remove();
         for (BPlusTree tree: index.values())
             tree.remove();
         index = new HashMap<String, BPlusTree>();
     }
 
     public int save() throws java.io.IOException {
         ByteBuffer buffer = new ByteBuffer();
         schema.getBytes(buffer);
         int len = schema.columnCount();
         for (int i = 0; i < len; ++i) {
             BPlusTree tree = index.get(schema.name(i));
             if (tree == null)
                 buffer.putInt(0);
            else {
                 buffer.putInt(tree.save());
            }
         }
         bucket.setData(buffer.array());
         return bucket.save();
     }
 
     public Schema schema() {
         return schema;
     }
 
     public int estimatedTupleSize() {
         return schema.estimatedLength() + 4 * schema.columnCount() + 4;
     }
 
     public BPlusTree getBPlusTree(String col) {
         return index.get(col);
     }
 
     public void putBPlusTree(String col, BPlusTree tree) {
         index.put(col, tree);
     }
 
     public void removeBPlusTree(String col) throws java.io.IOException {
         BPlusTree tree = index.remove(col);
         if (tree != null)
             tree.remove();
     }
 
     public DataAdapter adapter(String col) {
         int type = schema.type(col);
         int length = schema.length(col);
         return new DataAdapter(type, length);
     }
 }
