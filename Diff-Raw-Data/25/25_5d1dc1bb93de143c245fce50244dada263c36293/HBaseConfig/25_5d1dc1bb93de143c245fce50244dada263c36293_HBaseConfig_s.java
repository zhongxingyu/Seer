 package util;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import server.exception.ErrorCodeException;
 
 public class HBaseConfig {
 
 //    private static ConfigManager cm = ConfigManager.getInstance();
     private static Configuration config=null;
     private static HTablePool pool = null;
 
     public HBaseConfig() {
     	if(config==null)
     	{
     		config = HBaseConfiguration.create();
     	}
         initualize();
         // config.set("hbase.master", "146.169.35.29:60000");
     }
 
     public static void initualize() {
     	System.out.println("initualizeing...");
 //       config.set("hbase.zookeeper.quorum", "192.168.0.4");
        config.set("hbase.zookeeper.quorum", "namenode");
 //        config.set("hbase.zookeeper.property.clientPort", "55556");
 //        config.set("hbase.master.port", "55558");
 //        config.set("hbase.regionserver.port", "55559");
        pool = new HTablePool(config, 100);
     	System.out.println("initualizeing done...");
     }
 
     public static Configuration getConfig() {
     	if(config==null)
     	{
     		config = HBaseConfiguration.create();
     		 initualize();
     	}
         return config;
     }
 
     public static HTablePool getHtablePool() {
     	if(config==null)
     	{
     		config = HBaseConfiguration.create();
     		 initualize();
     	}
         return pool;
     }
 
 //    public static HTableInterface getTable(byte[] tableName) {
 //        return pool.getTable(tableName);
 //    }
     public static HTableInterface getTable(String tableName) throws ErrorCodeException {
         if (pool == null) {
             initualize();
         }
         if (pool == null) {
             System.out.println("Problemmmmmmmmmmmmmmmmmmmmmmmmmmm FIrst");
             throw new ErrorCodeException(AllConstants.ErrorDictionary.HBase_Internal_Error);
         }
         HTableInterface table = pool.getTable(tableName);
         if (table == null) {
             System.out.println("Problemmmmmmmmmmmmmmmmmmmmmmmmmmm!!!!!!!");
             throw new ErrorCodeException(AllConstants.ErrorDictionary.HBase_Internal_Error);
         } else {
             return table;
         }
 
     }
 
     public static void putTable(HTableInterface table) throws IOException {
         if (table != null) {
           table.close();
         }
     }
 
     public static void main(String args[]) {
         try {
 
             HBaseConfig config2 = new HBaseConfig();
             System.out.println(config2.getTable("s3").get(new Get(Bytes.toBytes("bucket2-PPTs/iHealth/steps.jpg"))).getColumn(Bytes.toBytes("pm"), Bytes.toBytes("liguo")));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 }
