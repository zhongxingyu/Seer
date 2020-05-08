 package com.nearinfinity.mysqlengine.jni;
 
 import com.nearinfinity.mysqlengine.Connection;
 import com.nearinfinity.mysqlengine.HBaseClient;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/1/12
  * Time: 9:46 AM
  * To change this template use File | Settings | File Templates.
  */
 public class HBaseAdapter {
     private static AtomicLong connectionCounter;
     private static Map<Long, Connection> clientPool;
     private static HBaseClient client;
     private static boolean configured;
     private static final Logger logger = Logger.getLogger(HBaseAdapter.class);
 
     static {
         try {
             logger.info("HBaseAdapter: Static initializer");
 
             //Read config options from adapter.conf
             Scanner confFile = new Scanner(new File("/etc/mysql/adapter.conf"));
             Map<String, String> params = new HashMap<String, String>();
             while (confFile.hasNextLine()) {
                 Scanner line = new Scanner(confFile.nextLine());
                 params.put(line.next(), line.next());
             }
 
             //Initiliaze class variables
             client = new HBaseClient(params.get("hbase_table_name"), params.get("zk_quorum"));
             connectionCounter = new AtomicLong(0L);
             clientPool = new ConcurrentHashMap<Long, Connection>();
 
             //We are now configured
             configured = true;
         }
         catch (FileNotFoundException e) {
             logger.warn("HBaseAdapter::GotFileNotFoundException", e);
             e.printStackTrace();
         }
     }
 
     public static boolean isConfigured() {
         return configured;
     }
 
     public static boolean createTable(String tableName, List<String> columnNames) throws HBaseAdapterException {
         logger.info("HBaseAdapter: Creating table " + tableName);
         try {
             client.createTableFull(tableName, columnNames);
         }
         catch (IOException e) {
             throw new HBaseAdapterException("IOException", e.toString());
         }
         return true;
     }
 
     public static long startScan(String tableName) throws HBaseAdapterException {
         long scanId = connectionCounter.incrementAndGet();
         try {
             ResultScanner scanner = client.getTableScanner(tableName);
             clientPool.put(scanId, new Connection(tableName, scanner));
         }
         catch (IOException e) {
             throw new HBaseAdapterException("IOException", e.toString());
         }
         return scanId;
     }
 
     public static Row nextRow(long scanId) throws HBaseAdapterException {
         Connection conn = clientPool.get(scanId);
         if (conn == null) {
             throw new HBaseAdapterException("Cannot find scanId key", "");
         }
         Row row = new Row();
         try {
             Result result = conn.getScanner().next();
             if (result == null) {
                 return row;
             }
             Map<String, byte[]> values = client.parseRow(result, conn.getTableName());
             row.setRowMap(values);
         }
         catch  (IOException e) {
             throw new HBaseAdapterException("IOException", e.toString());
         }
         return row;
     }
 
     public static void endScan(long scanId) throws HBaseAdapterException {
         Connection conn = clientPool.remove(scanId);
         if (conn == null) {
             throw new HBaseAdapterException("ScanId does not exist", "");
         }
     }
 
     public static boolean writeRow(String tableName, Map<String, byte[]> values) throws HBaseAdapterException {
         try {
             client.writeRow(tableName, values);
         }
         catch (IOException e) {
             throw new HBaseAdapterException("IOException", e.toString());
         }
         return true;
     }
 }
