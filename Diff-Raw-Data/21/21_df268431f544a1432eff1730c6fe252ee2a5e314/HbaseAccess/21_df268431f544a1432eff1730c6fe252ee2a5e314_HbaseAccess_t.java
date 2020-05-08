 package com.servicenow.syseng.metadata;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SERVICE-NOW\chi.yao
  * Date: 4/16/13
  * Time: 1:17 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.conf.Configuration;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.net.URL;
 
 public class HbaseAccess {
 
     private static final Logger logger = LoggerFactory.getLogger(HbaseAccess.class);
 
     public static final String HBASE_URL = "10.64.14.202";     // TODO: get config
     //public static final String HBASE_URL = "localhost";
     // all meta data live in one table
     private static final String METADATA_TABLE = "m";
     private static final String MAP_ID_TABLE = "map_id";
     private static final String DEFAULT_COLUMN_FAMILY = "cf"; // so far all tables use this column family only
     private static final byte[] DEFAULT_CF_BYTES = DEFAULT_COLUMN_FAMILY.getBytes();
     private static final String DEFAULT_COLUMN = "c"; // so far all tables use this column only
     private static final byte[] DEFAULT_COL_BYTES = DEFAULT_COLUMN.getBytes();
     private static final String MAP_SEQUENCE = "<map_sequence>";
     private static final byte[] MAP_SEQUENCE_BYTES = MAP_SEQUENCE.getBytes();
 
     private static Configuration config = HBaseConfiguration.create();
     private static HBaseAdmin admin;
     private static HTablePool htablePool;
 
 
     // init connections and make sure hbase has the right schema
     public static final void init() {
 
         try {
             config.set("hbase.master", HBASE_URL+":60000");   // NOT USED?????
            String s = config.get("hbase.master");
            System.out.println(s);
            config.set("hbase.zookeeper.quorum", HBASE_URL);
            config.set("hbase.zookeeper.property.clientPort", "2181");
             // test
             //HBaseAdmin.checkHBaseAvailable(config);
             //System.out.println("HBase is running!");
             //  createTable(config);
             //creating a new table
             //HTable mytable = new HTable(config, "mytable");
             //System.out.println("Table mytable obtained ");
 
             HConnection connection = HConnectionManager.getConnection(config);
             admin = new HBaseAdmin(connection);
             htablePool = new HTablePool(config,1); // max versions is 1
 
             // init table m with column family "cf"
             createTableIfNotExists(METADATA_TABLE,DEFAULT_COLUMN_FAMILY);
             // init table map_id with column family "cf"
             createTableIfNotExists(MAP_ID_TABLE,DEFAULT_COLUMN_FAMILY);
             // add record ("<map_sequence>","cf","c",0) to MAP_ID_TABLE
             // <map_sequence>'s value is the current count of maps
             if (!rowExists(MAP_ID_TABLE,MAP_SEQUENCE)) {
                 add(MAP_ID_TABLE,MAP_SEQUENCE,ByteBuffer.allocate(8).putLong(0L).array());
             }
 
         } catch (Exception e) {
             logger.error("Cannot initialize HBase access layer");
             e.printStackTrace();
         }
     }
 
     // add this key-value pair to METADATA_TABLE
     public static final void add(String key, String val) throws IOException {
         add(METADATA_TABLE,key,val);
     }
 
     // has row of the given name
     public static final boolean rowExists(String table, String row) throws IOException {
         return rowExists(table,row,DEFAULT_COLUMN_FAMILY,DEFAULT_COLUMN);
     }
 
     public static final boolean rowExists(String table, String row, String colFamily, String col) throws IOException {
         HTableInterface htable = htablePool.getTable(table);
         Result result = htable.get(new Get(row.getBytes()));
         return !result.isEmpty();
     }
 
     // from METADATA_TABLE
     public static final Collection<String> getKeysWithPrefix(String prefix) throws IOException {
         Scan scan = createRangeScanByPrefix(prefix);
         scan.addColumn(DEFAULT_CF_BYTES,DEFAULT_COL_BYTES).setMaxVersions(1);
         HTableInterface htable = htablePool.getTable(METADATA_TABLE);
         ResultScanner resultScanner = htable.getScanner(scan);
         ArrayList<String> al = new ArrayList<String>(20);
         for (Result r=resultScanner.next();r!=null;r=resultScanner.next()) {
             al.add(new String(r.getRow(),"UTF8"));   // TODO: optimize conversion from byte[] to String
         }
         return al;
     }
 
     // from METADATA_TABLE
     public static final void removeKeysWithPrefix(String prefix) throws IOException {
         Scan scan = createRangeScanByPrefix(prefix);
         scan.addColumn(DEFAULT_CF_BYTES,DEFAULT_COL_BYTES).setMaxVersions(1);
         HTableInterface htable = htablePool.getTable(METADATA_TABLE);
         ResultScanner resultScanner = htable.getScanner(scan);
         // build a delete spec
         ArrayList<Delete> deletes = new ArrayList<Delete>(20);
         for (Result r=resultScanner.next();r!=null;r=resultScanner.next()) {
             deletes.add(new Delete(r.getRow()));
         }
         // TODO: not sure if this can handle large batch, but hbase only support single row delete
         htable.delete(deletes);
     }
 
     // create a scan with a start row and end row matching the prefix
     private static final Scan createRangeScanByPrefix(String prefix) throws IOException {
         // create end row for query
         byte[] beginRow = prefix.getBytes();
         byte[] endRow = beginRow.clone();
         endRow[endRow.length-1] = (byte) (endRow[endRow.length-1]+1);
         return new Scan(beginRow,endRow);
     }
 
     // add key-val pair to table
     private static final void add(String table, String key, String val) throws IOException {
         add(table,key,val.getBytes());
     }
 
     // add key-val pair to table
     private static final void add(String table, String key, byte[] val) throws IOException {
         HTableInterface htable = htablePool.getTable(table);
         htable.setAutoFlush(true); // TODO: to optimize
         htable.put(new Put(key.getBytes()).add(DEFAULT_CF_BYTES,DEFAULT_COL_BYTES,val));
     }
 
     private static final void createTableIfNotExists(String table, String columnFamily) throws IOException {
         if (!admin.tableExists(table)) {
             HTableDescriptor td = new HTableDescriptor(table);
             td.addFamily(new HColumnDescriptor(columnFamily));
             admin.createTable(td);
         }
         if (admin.isTableDisabled(table)) {
             admin.enableTable(table);
         }
         // TODO: not thread safe in Hbase?
     }
 
     // get val for key from table
     // return "" if no value for the key or no such table
     // TODO: is "" better than null?
     private static final String get(String table, String key) throws IOException {
         HTableInterface htable = htablePool.getTable(table);
         htable.setAutoFlush(true); // TODO: to optimize
         Result rs = htable.get(new Get(key.getBytes()));
         if (rs.isEmpty()) {
             return "";
         }
         else {
             byte[] v = rs.getValue(DEFAULT_CF_BYTES,DEFAULT_COL_BYTES);
             return (v==null ? "" : new String(v,"UTF8"));  // TODO: optimize conversion from byte[] to String
         }
     }
 
     // get table 1 char tag for map name
     // if name does not exist, create tag for it
     public static final String getMapTag(String name) throws IOException {
         String tag = get(MAP_ID_TABLE,name);
         if (tag==null || tag.equals("")) {
             HTableInterface htable = htablePool.getTable(MAP_ID_TABLE);
             long id = htable.incrementColumnValue(MAP_SEQUENCE_BYTES,DEFAULT_CF_BYTES,DEFAULT_COL_BYTES,1)-1;
             // assuming id <= 26 so that we can use single char tags
             tag = String.valueOf((char)('a'+id));
             // add this name-tag pair to map_id table
             Put put = new Put(name.getBytes()).add(DEFAULT_CF_BYTES,DEFAULT_COL_BYTES,tag.getBytes());
             htable.put(put);
         }
         return tag;
     }
 
     public static final void close() {
         try {
             htablePool.close();
             admin.close();
         } catch (IOException e) {
             logger.error("Error closing HBase connection");
         }
     }
 
 }
