 package com.servicenow.syseng.metadata;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SERVICE-NOW\eason.hu
  * Date: 4/18/13
  * Time: 10:48 AM
  * To change this template use File | Settings | File Templates.
  */
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Put;
 import org.hbase.async.*;
 
 import com.stumbleupon.async.Callback;
 import com.stumbleupon.async.Deferred;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AsynchbaseAccess {
     private static final Logger logger = LoggerFactory.getLogger(AsynchbaseAccess.class);
 
    //public static final String HBASE_URL = "10.64.14.202";     // TODO: get config
    public static final String HBASE_URL = "localhost";
     // all meta data live in one table
     private static final String METADATA_TABLE = "m";
     private static final byte[] METADATA_TABLE_BYTES = METADATA_TABLE.getBytes();
     private static final String MAP_ID_TABLE = "map_id";
     private static final byte[] MAP_ID_TABLE_BYTES = MAP_ID_TABLE.getBytes();
     private static final String DEFAULT_COLUMN_FAMILY = "cf"; // so far all tables use this column family only
     private static final byte[] DEFAULT_CF_BYTES = DEFAULT_COLUMN_FAMILY.getBytes();
     private static final String DEFAULT_COLUMN = "c"; // so far all tables use this column only
     private static final byte[] DEFAULT_COL_BYTES = DEFAULT_COLUMN.getBytes();
     private static final String MAP_SEQUENCE = "<map_sequence>";
     private static final byte[] MAP_SEQUENCE_BYTES = MAP_SEQUENCE.getBytes();
 
     private static HBaseClient client;
     static final Object lock = new Object();
 
     public static final void init() throws Exception {
 
         try {
             connect(HBASE_URL);
 
             // init table m with column family "cf"
             //createTableIfNotExists(METADATA_TABLE, DEFAULT_COLUMN_FAMILY);
             // init table map_id with column family "cf"
             //createTableIfNotExists(MAP_ID_TABLE, DEFAULT_COLUMN_FAMILY);
             // add record ("<map_sequence>", "cf", "c", 1) to MAP_ID_TABLE
             //add(MAP_ID_TABLE, MAP_SEQUENCE, ByteBuffer.allocate(8).putLong(1L).array());
             // add record ("m", "cf", "c", 0) to MAP_ID_TABLE
             //add(MAP_ID_TABLE, METADATA_TABLE, ByteBuffer.allocate(8).putLong(0L).array());
         } catch (Exception e) {
             logger.error("Asynchbase: Cannot initialize Hbase database");
             disconnect();
         }
 
     }
 
     public static final void connect(String master) {
         client = new HBaseClient(master);
     }
 
     public static final void disconnect() throws Exception {
         client.shutdown().joinUninterruptibly();
     }
 
     // add key-val pair to METADATA_TABLE
     public static final void add(String key, String val) throws Exception {
         add(key.getBytes(), val.getBytes());
     }
 
     // add key-val pair to METADATA_TABLE
     private static final void add(byte[] key, byte[] val) throws Exception {
         add(METADATA_TABLE_BYTES,key,val);
     }
 
     // add key-val pair to METADATA_TABLE
     private static final void add(byte[] table, byte[] key, byte[] val) throws Exception {
         PutRequest put = new PutRequest(
                 table, key, DEFAULT_CF_BYTES, DEFAULT_CF_BYTES, val);
         Deferred<Object> d = client.put(put);
         d.join();
     }
 
     // get value from METADATA_TABLE
     public static final String get(String key) throws Exception {
         return get(METADATA_TABLE,key);
     }
 
     public static final String get(String table, String key) throws Exception {
         return get(table, key.getBytes());
     }
 
     // get value from METADATA_TABLE
     private static final String get(String table, byte[] key) throws Exception {
         GetRequest get = new GetRequest(table, key);
        Deferred<ArrayList<KeyValue>> d = client.get(get);
        KeyValue kv = d.join().get(0);
        return new String(kv.value());
     }
 
     public static final Collection<String> getAllKeys() throws Exception {
         final Scanner scanner = client.newScanner(METADATA_TABLE_BYTES);
         scanner.setFamily(DEFAULT_CF_BYTES);
         scanner.setQualifier(DEFAULT_CF_BYTES);
 
         ArrayList<String> keys = new ArrayList<String>();
         ArrayList<ArrayList<KeyValue>> rows = null;
         ArrayList<Deferred<Boolean>> workers = new ArrayList<Deferred<Boolean>>();
         while ((rows = scanner.nextRows(1).joinUninterruptibly()) != null) {
             keys.add(new String(rows.get(0).get(0).key()));
         }
         return keys;
     }
 
     public static final Collection<String> getKeysWithPrefix(String prefix) throws Exception {
         final Scanner scanner = client.newScanner(METADATA_TABLE_BYTES);
         scanner.setFamily(DEFAULT_CF_BYTES);
         scanner.setQualifier(DEFAULT_CF_BYTES);
         scanner.setKeyRegexp("^" + prefix);
 
         ArrayList<String> keys = new ArrayList<String>();
         ArrayList<ArrayList<KeyValue>> rows = null;
         ArrayList<Deferred<Boolean>> workers = new ArrayList<Deferred<Boolean>>();
         while ((rows = scanner.nextRows(1).joinUninterruptibly()) != null) {
             keys.add(new String(rows.get(0).get(0).key()));
         }
         return keys;
     }
 
     // get table 1 char tag for map name
     // if name does not exist, create tag for it
     public static final String getMapTag(String name) throws Exception {
         String tag = get(MAP_ID_TABLE,name);
         if (tag==null || tag.equals("")) {
             long id = client.atomicIncrement(new AtomicIncrementRequest(
                     MAP_ID_TABLE_BYTES,MAP_SEQUENCE_BYTES,DEFAULT_CF_BYTES,DEFAULT_COL_BYTES)).join()-1;
             // assuming id <= 26 so that we can use single char tags
             tag = String.valueOf((char)('a'+id));
             // add this name-tag pair to map_id table
             add(MAP_ID_TABLE_BYTES, name.getBytes(), tag.getBytes());
         }
         return tag;
     }
 
     static void println(String msg) {
         synchronized (lock) {
             System.out.println(msg);
         }
     }
 
     public static final class UpdateResult {
         public String key;
         public boolean success;
     }
 
     @SuppressWarnings("serial")
     public static final class UpdateFailedException extends Exception {
         public UpdateResult result;
 
         public UpdateFailedException(UpdateResult r) {
             super("Asynchbase: Failed to update message!");
             this.result = r;
         }
     }
 
     @SuppressWarnings("serial")
     public static final class SendMessageFailedException extends Exception {
         public SendMessageFailedException() {
             super("Asynchbase: Failed to send message!");
         }
     }
 
     public static final class InterpretResponse implements Callback<UpdateResult, Boolean> {
         private String key;
 
         InterpretResponse(String key) {
             this.key = key;
         }
 
         public UpdateResult call(Boolean response) throws Exception {
             UpdateResult r = new UpdateResult();
             r.key = this.key;
             r.success = false;
             if (!r.success)
                 throw new UpdateFailedException(r);
 
             return r;
         }
 
         @Override
         public String toString() {
             return String.format("Asynchbase: InterpretResponse<%s>", key);
         }
     }
 
     public static final class SuccessToMessage implements Callback<String, UpdateResult> {
 
         public String call(UpdateResult r) throws Exception {
             String message = "Asynchbase: Value updated for key %s successfully.";
 
             return String.format(message, r.key);
         }
 
         @Override
         public String toString() {
             return "Asynchbase: SuccessToMessage";
         }
     }
 
     public static final class FailureToMessage implements Callback<String, UpdateFailedException> {
 
         public String call(UpdateFailedException e) throws Exception {
             String message = "Value for key %s is unchanged!";
 
             return String.format(message, e.result.key);
         }
 
         @Override
         public String toString() {
             return "Asynchbase: FailureToMessage";
         }
     }
 
     public static final class SendMessage implements Callback<Boolean, String> {
 
         public Boolean call(String s) throws Exception {
             //if(true)
             //    throw new SendMessageFailedException();
             println(s);
 
             return Boolean.TRUE;
         }
 
         @Override
         public String toString() {
             return "SendMessage";
         }
     }
 }
