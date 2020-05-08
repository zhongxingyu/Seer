 package fatworm.storage;
 
 import java.io.RandomAccessFile;
 import fatworm.storage.bplustree.*;
 import fatworm.storage.bucket.Bucket;
 import fatworm.util.ByteLib;
 import fatworm.record.Schema;
 import fatworm.storagemanager.*;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Map;
 import java.util.HashMap;
 
 public class Database implements IOHelper {
     private File file;
     private FreeList freeList;
     private SuperTable superTable;
 
     private Map<String, Table> tables;
 
     long readCount = 0, writeCount = 0;
 
     private Database(String name) throws java.io.FileNotFoundException {
         RandomAccessFile raf = new RandomAccessFile(name, "rw");
         file = new File(raf);
         tables = new HashMap<String, Table>();
     }
 
     public static Database create(String name) throws java.io.FileNotFoundException, java.io.IOException {
         Database db = new Database(name);
         db.freeList = new FreeList();
         db.superTable = SuperTable.create(db);
         return db;
     }
 
     public static Database load(String name) throws java.io.FileNotFoundException, java.io.IOException {
         Database db = new Database(name);
 
         db.freeList = FreeList.load(db.file);
         db.superTable = SuperTable.load(db);
 
         return db;
     }
 
     public void save() throws java.io.IOException {
         superTable.save();
         freeList.save(file);
         for (Table t: tables.values())
             t.save();
     }
 
     public boolean readBlock(int block, byte[] data, int offset) throws java.io.IOException {
         ++readCount;
         return file.readBlock(block, data, offset);
     }
 
     public void writeBlock(int block, byte[] data, int offset) throws java.io.IOException {
         ++writeCount;
         file.writeBlock(block, data, offset);
     }
 
     public int occupy() {
         return freeList.occupy();
     }
 
     public void free(int block) {
         freeList.free(block);
     }
 
     public int getBlockSize() {
         return File.blockSize;
     }
 
     public Table getTable(String table) throws java.io.IOException {
         if (tables.containsKey(table))
             return tables.get(table);
         else {
             int relation = superTable.getRelation(table), schema = superTable.getSchema(table);
             if (relation == 0)
                 return null;
             else {
                 Table ret = Table.load(this, relation, schema);
                 tables.put(table, ret);
                 return ret;
             }
         }
     }
 
     // returns null if the table name already existed
     public Table insertTable(String table, Schema schema) throws java.io.IOException {
         if (getTable(table) != null)
             return null;
         int sBlock;
         if (schema == null)
             sBlock = 0;
         else {
             SchemaOnDisk ss = SchemaOnDisk.create(this, schema);
             sBlock = ss.save();
         }
         Table st = Table.create(this, sBlock);
         for (int i = 0; i < schema.columnCount(); ++i)
             if (schema.primaryKey(i))
                 st.createIndex(schema.name(i));
        int tBlock = st.save();
         superTable.insertTable(table, tBlock, sBlock);
         tables.put(table, st);
         return st;
     }
 
     Table insertTable(String table, int tupleSize) throws java.io.IOException {
         if (getTable(table) != null)
             return null;
         int sBlock;
         Table st = Table.createTemp(this, tupleSize);
        int tBlock = st.save();
         superTable.insertTable(table, tBlock, 0);
         tables.put(table, st);
         return st;
     }
 
     public void dropTable(String tablename) throws java.io.IOException {
         Table table = getTable(tablename);
         if (table != null) {
             table.save();
             table.remove();
             superTable.removeTable(tablename);
             tables.remove(tablename);
         }
     }
 
     public void close() {
         try {
             save();
         	file.close();
         } catch (java.io.IOException e) {
         }
     }
 }
