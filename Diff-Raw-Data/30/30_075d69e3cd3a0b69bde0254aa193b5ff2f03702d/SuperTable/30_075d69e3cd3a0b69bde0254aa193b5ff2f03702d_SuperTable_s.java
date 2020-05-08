 package fatworm.storage;
 
 import java.util.Map;
 import java.util.HashMap;
 import fatworm.storage.bucket.Bucket;
 import fatworm.util.ByteLib;
 
 public class SuperTable {
     private static class MetaInfo {
         String name;
         int block;
         int schema;
 
         public MetaInfo(String name, int block, int schema) {
             this.name = name;
             this.block = block;
             this.schema = schema;
         }
 
         public MetaInfo(byte[] bytes, int offset) {
             int slen = ByteLib.bytesToInt(bytes, offset);
            name = ByteLib.bytesToString(bytes, 4, slen);
            block = ByteLib.bytesToInt(bytes, slen + 4);
            schema = ByteLib.bytesToInt(bytes, slen + 8);
         }
 
         public byte[] getBytes() {
             byte[] s = ByteLib.stringToBytes(name);
             byte[] ret = new byte[4 + s.length + 8];
 
             ByteLib.intToBytes(s.length, ret, 0);
             System.arraycopy(s, 0, ret, 4, s.length);
             ByteLib.intToBytes(block, ret, 4 + s.length);
            ByteLib.intToBytes(block, ret, 8 + s.length);
 
             return ret;
         }
 
         public int getBytesLen() {
             byte[] s = ByteLib.stringToBytes(name);
             return 12 + s.length;
         }
     }
 
     private Map<String, MetaInfo> tables = new HashMap<String, MetaInfo>();
     private Bucket bucket;
     private IOHelper io;
 
     public void insertTable(String name, int block, int schema) {
         tables.put(name, new MetaInfo(name, block, schema));
     }
 
     public void removeTable(String name) {
         tables.remove(name);
     }
 
     private SuperTable(IOHelper io) {
         this.io = io;
     }
 
     public static SuperTable create(IOHelper io) {
         SuperTable ret = new SuperTable(io);
         ret.bucket = null;
         return ret;
     }
 
     public static SuperTable load(IOHelper io) {
         SuperTable ret = new SuperTable(io);
         ret.bucket = Bucket.load(io, FreeList.reservedBlock);
         byte[] data = ret.bucket.getData();
 
         int len = ByteLib.bytesToInt(data, 0);
         int s = 4;
         for (int i = 0; i < len; ++i) {
             int mlen = ByteLib.bytesToInt(data, s);
             s += 4;
             MetaInfo mi = new MetaInfo(data, s);
             s += mlen;
             ret.tables.put(mi.name, mi);
         }
 
         return ret;
     }
 
     public int getRelation(String table) {
         MetaInfo mi = tables.get(table);
         if (mi == null)
             return 0;
         else
             return mi.block;
     }
 
     public int getSchema(String table) {
         MetaInfo mi = tables.get(table);
         if (mi == null)
             return 0;
         else
             return mi.schema;
     }
 
     public int save() throws java.io.IOException {
         int len = 0;
         for (MetaInfo mi: tables.values())
             len += 4 + mi.getBytesLen();
         len += 4;
 
         byte[] data = new byte[len];
         ByteLib.intToBytes(tables.size(), data, 0);
         
         int s = 4;
         for (MetaInfo mi: tables.values()) {
             byte[] tmp = mi.getBytes();
             ByteLib.intToBytes(tmp.length, data, s);
             s += 4;
             System.arraycopy(tmp, 0, data, s, tmp.length);
             s += tmp.length;
         }
 
         if (bucket == null)
             bucket = Bucket.create(io, data, FreeList.reservedBlock);
         else
             bucket.setData(data);
         return bucket.save();
     }
 }
