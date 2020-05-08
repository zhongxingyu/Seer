 package fatworm.storage;
 
 import java.io.RandomAccessFile;
 import fatworm.storage.bplustree.*;
 import fatworm.storage.bucket.Bucket;
 import fatworm.util.ByteLib;
 import fatworm.record.Schema;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Arrays;
 import java.util.Random;
 
 public class StorageManager implements IOHelper {
     private File file;
     private FreeList freeList;
     private SuperTable superTable;
 
     public StorageManager(String name) throws java.io.FileNotFoundException, java.io.IOException {
         load(name);
     }
 
     public void testBucket() throws java.io.IOException {
         Random rand = new Random();
         int datalen = 10000;
         byte[] data = new byte[datalen];
         for (int i = 0; i < datalen; ++i)
             data[i] = (byte) rand.nextInt(255);
 
         Bucket bucket = Bucket.create(this, data);
         int block = bucket.save();
 
         bucket = Bucket.load(this, block);
         if (!Arrays.equals(bucket.getData(), data))
             System.out.println("Wrong data");
         else
             System.out.println("Correct data");
     }
 
     public void testBPlusTree() throws java.io.IOException {
         long t0 = System.nanoTime();
         //int block = BPlusTree.create(this, new IntegerComparator(), BPlusTree.KeySize.FIXED_4_BYTES).getBlock();
         int block = BPlusTree.create(this, new StringComparator(), BPlusTree.KeySize.VARIANT).getBlock();
         //BPlusTree bptree = BPlusTree.load(this, new IntegerComparator(), block);
         BPlusTree bptree = BPlusTree.load(this, new StringComparator(), block);
         if (bptree == null)
             System.err.println("bptree == null");
         else {
             java.util.Random rand = new java.util.Random();
             /*
             int range = 1000;
             List<Integer>[] data = new List[range];
             for (int i = 0; i < range; ++i)
                 data[i] = new LinkedList<Integer>();
             for (int i = 0; i < 1000000; ++i) {
                 int t = rand.nextInt(range);
                 data[t].add(new Integer(i));
                 byte[] tmp = new byte[4];
                 ByteLib.intToBytes(t, tmp, 0);
                 bptree.insert(tmp, i);
             }
             t0 = System.nanoTime() - t0;
             if (bptree.check() == false)
                 System.out.println("check failed");
             else
                 System.out.println("check ok");
 
             for (int i = 0; i < range; ++i) {
                 byte[] tmp = new byte[4];
                 ByteLib.intToBytes(i, tmp, 0);
                 List<Integer> result = bptree.find(tmp);
                 if (!result.containsAll(data[i]) || !data[i].containsAll(result)) {
                     System.out.println("Data error");
                     System.out.println("Return value:");
                     for (Integer ii: result)
                         System.out.print(ii + " ");
                     System.out.println();
                     System.out.println("Raw data:");
                     for (Integer ii: data[i])
                         System.out.print(ii + " ");
                     System.out.println();
                 } else {
                     System.out.println("check ok, contains " + result.size() + " values");
                 }
             }*/
             Set<String> data = new HashSet<String>();
             for (int i = 0; i < 10000; ++i) {
                 int l = rand.nextInt(100);
                 StringBuilder sb = new StringBuilder();
                 for (int j = 0; j < l; ++j)
                     sb.append((char) (48 + rand.nextInt(26)));
 
                 data.add(sb.toString());
                 bptree.insert(ByteLib.stringToBytes(sb.toString()), i);
             }
             t0 = System.nanoTime() - t0;
             if (bptree.check() == false)
                 System.out.println("check failed");
             else
                 System.out.println("check ok, data count: " + data.size());
 
             System.out.println("Total time: " + t0);
             System.out.println("Disk IO time: " + File.totalTime + " (" + (double) File.totalTime / (double) t0 * 100 + "%)");
         }
     }
 
     private void load(String name) throws java.io.FileNotFoundException, java.io.IOException {
         RandomAccessFile raf = new RandomAccessFile(name, "rw");
         file = new File(raf);
 
         freeList = FreeList.load(file);
         if (freeList == null) {
             freeList = new FreeList();
             superTable = SuperTable.create(this);
         } else
             superTable = SuperTable.load(this);
     }
 
     public void close() throws java.io.IOException {
         superTable.save();
         freeList.save(file);
     }
 
     public boolean readBlock(int block, byte[] data, int offset) throws java.io.IOException {
         ++readCount;
         return file.readBlock(block, data, offset);
     }
 
     public void writeBlock(int block, byte[] data, int offset) throws java.io.IOException {
         ++writeCount;
         file.writeBlock(block, data, offset);
     }
 
     long readCount = 0, writeCount = 0;
 
     public int occupy() {
         return freeList.occupy();
     }
 
     public void free(int block) {
         freeList.free(block);
     }
 
     public int getBlockSize() {
         return File.blockSize;
     }
 
    public Table getTable(String table) {
         int relation = superTable.getRelation(table), schema = superTable.getSchema(table);
         if (relation == 0 || schema == 0)
             return null;
         else
             return Table.load(this, relation, table, schema);
     }
 
     // returns null if the table name already existed
     public Table insertTable(String table, Schema schema) throws java.io.IOException {
         SchemaOnDisk ss = SchemaOnDisk.create(this, schema);
         int sBlock = ss.save();
         Table st = Table.create(this, table, sBlock);
         int tBlock = st.save();
         superTable.insertTable(table, tBlock, sBlock);
         return st;
     }
 }
