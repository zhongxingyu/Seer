 package com.xingcloud.xa.secondaryindex;
 
 import com.xingcloud.xa.secondaryindex.utils.QueryUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.io.hfile.Compression;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.hbase.util.Pair;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangchangli
  * Date: 5/17/13
  * Time: 10:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class HPutTask implements Runnable  {
   private static final Log LOG = LogFactory.getLog(SecondaryIndexWriter.class); 
   private String tableName;
   private Map<String,List<Index>> indexs;
   
   private static Map<String, Boolean> tables;
   private static HBaseAdmin admin;
   
 //  static {
 //    admin = new HBaseAdmin();
 //    initTables();  
 //  }
   
   public HPutTask(String tableName, Map<String,List<Index>> indexs){
     
     this.tableName = tableName;
     this.indexs = indexs;
   }
   
   @Override
   public void run() {
     try{
     for (Map.Entry<String, List<Index>> entry : indexs.entrySet()) {
       boolean putHbase = true;
       while (putHbase) {
         HTable table = null;
         long currentTime = System.currentTimeMillis();
         try {
 
           table = new HTable(HBaseConf.getInstance().getHBaseConf(entry.getKey()),QueryUtils.getUIIndexTableName(tableName, null));
           LOG.info(tableName + " init htable .." + currentTime);
           table.setAutoFlush(false);
           table.setWriteBufferSize(Constants.WRITE_BUFFER_SIZE);
           
           Pair<List<Delete>, List<Put>> deletePut = optimizePuts(entry.getValue());
           
           table.delete(deletePut.getFirst());
           table.put(deletePut.getSecond());
           
           table.flushCommits();
 
           putHbase = false;
           LOG.info(tableName + " " + entry.getKey() + " put hbase size:" + entry.getValue().size() +
             " completed .tablename is " + tableName + " using "
             + (System.currentTimeMillis() - currentTime) + "ms");
         } catch (IOException e) {
           if (e.getMessage().contains("interrupted")) {
             throw e;
           }
           LOG.error(tableName + entry.getKey() + e.getMessage(), e);
           if (e.getMessage().contains("HConnectionImplementation") && e.getMessage().contains("closed")) {
             HConnectionManager.deleteConnection(HBaseConf.getInstance().getHBaseConf(entry.getKey()), true);
             LOG.warn("delete connection to " + entry.getKey());
           }
           putHbase = true;
 
           LOG.info("trying put hbase " + tableName + " " + entry.getKey() + "again...tablename " +
             ":" + tableName);
           Thread.sleep(5000);
         } finally {
           try {
             if (table != null) {
               table.close();
               LOG.info(tableName + " close this htable." + currentTime);
             }
           } catch (IOException e) {
             LOG.error(tableName + e.getMessage(), e);
           }
         }
       }
     }   
     }catch (Exception e){
       
     }
   }
   
   private Pair<List<Delete>, List<Put>> optimizePuts(List<Index> indexs){
     Pair<List<Delete>, List<Put>> result = new Pair<List<Delete>, List<Put>>();
     result.setFirst(new ArrayList<Delete>());
     result.setSecond(new ArrayList<Put>());
 
     Map<Index, Integer> combieMap = new HashMap<Index, Integer>();
     int operation;
     for(Index index: indexs){
       operation = 1;
       if(index.getOperation().equals("delete")){
         operation = -1;
       }
       
       if(!combieMap.containsKey(index)){
         combieMap.put(index, operation);  
       }else{
         int i = combieMap.get(index) + operation;
         if(i>1) i = 1;
         if(i<-1) i = -1;
         combieMap.put(index, i); 
         index.setValue(index.getValue());
       }
       
     }

     for(Map.Entry<Index, Integer> entry:combieMap.entrySet()){      
       Index index = entry.getKey();
       
       System.out.println(index.toString());
       
       if(-1 == entry.getValue()){
         Delete delete = new Delete(QueryUtils.getUIIndexRowKey(index.getPropertyID(), Bytes.toBytes(index.getValue())));
         delete.deleteColumns(Constants.columnFamily.getBytes(), index.getUid().getBytes());
         delete.setWriteToWAL(Constants.deuTableWalSwitch);
         result.getFirst().add(delete);
       }else if (1 == entry.getValue()){
         Put put = new Put(QueryUtils.getUIIndexRowKey(index.getPropertyID(), Bytes.toBytes(index.getValue())));
         put.setWriteToWAL(Constants.deuTableWalSwitch);
         put.add(Constants.columnFamily.getBytes(), index.getUid().getBytes(), index.getValue().getBytes());
         result.getSecond().add(put);
       }
     }
     return result;
   }
 
   private static void initTables() {
     try {
       HTableDescriptor[] tableDescriptors = admin.listTables();
       for(HTableDescriptor tableDescriptor: tableDescriptors){
         tables.put(tableDescriptor.getNameAsString(), true);
       }  
     }catch (Exception e){      
       e.printStackTrace();
     }
 
   }
 
   private void createTable(HBaseAdmin admin, String tableName, String... families) throws IOException {
     HTableDescriptor table = new HTableDescriptor(tableName);
     for(String family: families){
       HColumnDescriptor columnDescriptor = new HColumnDescriptor(family);
       if(tableName.endsWith("_index"))
         columnDescriptor.setMaxVersions(1);
       columnDescriptor.setBlocksize(512 * 1024);
       columnDescriptor.setCompressionType(Compression.Algorithm.LZO);
       table.addFamily(columnDescriptor);
     }
     admin.createTable(table);
     tables.put(tableName, true);
   }
 
   public void checkTable(HBaseAdmin admin, String tableName, String... families) throws IOException {
     if(!tableExists(admin, tableName)){
       createTable(admin, tableName, families);
     }
   }
 
   private boolean tableExists(HBaseAdmin admin, String tableName) throws IOException {
     return tables.containsKey(tableName);
   }
 
 }
