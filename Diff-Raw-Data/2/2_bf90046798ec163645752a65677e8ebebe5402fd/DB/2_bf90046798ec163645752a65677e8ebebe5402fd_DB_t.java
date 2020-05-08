package cloud_base;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.hbase.util.PoolMap;
 
 public class DB {
 
   private final static int HPOOL_SIZE = 50;
   private final static String FAMILY_NAME = "cf";
   private final static byte[] FAMILY_NAME_BYTES = Bytes.toBytes(FAMILY_NAME);
   private final static String KEY_VERSION = "version";
   private final static String KEY_DATA = "data";
   private final static byte[] KEY_DATA_BYTES = Bytes.toBytes(KEY_DATA);
 
   private static Configuration conf = HBaseConfiguration.create();
   private static HBaseAdmin admin;
   private static HTablePool putPool = new HTablePool(conf, HPOOL_SIZE,
       PoolMap.PoolType.ThreadLocal);
   private static HTablePool getPool = new HTablePool(conf, HPOOL_SIZE,
       PoolMap.PoolType.ThreadLocal);
 
   public static void init() {
   }
 
   public static void create(String table) {
     HTableDescriptor tableDesc = new HTableDescriptor(table);
     HColumnDescriptor colDesc = new HColumnDescriptor(FAMILY_NAME);
     tableDesc.addFamily(colDesc);
     try {
       admin.createTable(tableDesc);
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   public static void put(String table, String row, long version, String data) {
     Put put = new Put(Bytes.toBytes(row));
     put.add(FAMILY_NAME_BYTES, KEY_DATA_BYTES, version, Bytes.toBytes(data));
     HTableInterface htable = putPool.getTable(table);
     try {
       htable.put(put);
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         htable.close();
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
   }
 
   public static Map<String, Object> get(String table, String row, long version) {
     Get get = new Get(Bytes.toBytes(row));
     HTableInterface htable = getPool.getTable(table);
     Map<String, Object> obj = new HashMap<String, Object>();
     try {
       get.setTimeRange(version + 1, Long.MAX_VALUE);
       get.setMaxVersions(1);
       Result r = htable.get(get);
       byte[] dataBytes = r.getValue(FAMILY_NAME_BYTES, KEY_DATA_BYTES);
       if(dataBytes != null){
         String data = Bytes.toString(dataBytes);
         version = r.raw()[0].getTimestamp();
         obj.put(KEY_VERSION, version);
         obj.put(KEY_DATA, data);        
       }
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         htable.close();
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
     return obj;
   }
 }
