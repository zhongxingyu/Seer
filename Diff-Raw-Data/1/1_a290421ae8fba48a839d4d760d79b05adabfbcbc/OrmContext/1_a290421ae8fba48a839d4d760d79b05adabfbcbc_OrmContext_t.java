 package com.nnapz.hbaseexplorer;
 
 import com.reinvent.surus.mapping.EntityService;
 import com.reinvent.surus.mapping.HFieldComponent;
 import com.reinvent.surus.primarykey.AbstractPrimaryKey;
 import com.reinvent.surus.system.PoolManager;
 import com.reinvent.surus.system.TableContext;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 /**
  * @author Bohdan Mushkevych
  *         Class provide simple context for all supported
  */
 
 class OrmDefault implements OrmInterface {
     public static final String ROW_KEY = "rowKey";
     private static final Logger log = Logger.getLogger(OrmDefault.class);
 
     @Override
     public boolean containsTable(String tableName) {
         return true;
     }
 
     @Override
     public Map<byte[], Map<Long, Map<String, Map<String, String>>>> parseResults(String tableName, Result[] results) {
         Map<byte[], Map<Long, Map<String, Map<String, String>>>> casted =
                 new TreeMap<byte[], Map<Long, Map<String, Map<String, String>>>>();
 
         Map<byte[], Map<Long, Map<String, Map<byte[], byte[]>>>> res =
                 new TreeMap<byte[], Map<Long, Map<String, Map<byte[], byte[]>>>>();
 
         for (Result row : results) {
             NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = row.getMap();
             Map<Long, Map<String, Map<byte[], byte[]>>> rowByTs = HBaseClient.remapByTimestamp(map);
             res.put(row.getRow(), rowByTs);
         }
 
         // convert qualifier:value pairs from byte[] to String
         for (byte[] rowKey : res.keySet()) {
             Map<Long, Map<String, Map<String, String>>> newMapByRow = new HashMap<Long, Map<String, Map<String, String>>>();
 
             Map<Long, Map<String, Map<byte[], byte[]>>> mapByRowKey = res.get(rowKey);
             for (Long timestamp : mapByRowKey.keySet()) {
                 Map<String, Map<String, String>> newMapByTimestamp = new HashMap<String, Map<String, String>>();
 
                 Map<String, Map<byte[], byte[]>> mapByTimestamp = mapByRowKey.get(timestamp);
                 for (String familyName : mapByTimestamp.keySet()) {
                     Map<String, String> newMapByFamily = new HashMap<String, String>();
 
                     Map<byte[], byte[]> mapByFamily = mapByTimestamp.get(familyName);
                     for (byte[] qualifier : mapByFamily.keySet()) {
                         byte[] value = mapByFamily.get(qualifier);
                         String strQualifier = Bytes.toString(qualifier);
                         String strValue = Bytes.toString(value);
                         newMapByFamily.put(strQualifier, strValue);
                     }
                     newMapByTimestamp.put(familyName, newMapByFamily);
                 }
                 newMapByRow.put(timestamp, newMapByTimestamp);
             }
             casted.put(rowKey, newMapByRow);
         }
 
         return casted;
     }
 
     @Override
     public String parseRowKey(String tableName, byte[] pk) {
         return Bytes.toString(pk);
     }
 
     @Override
     public Map<String, Class> getRowKeyComponents(String tableName) {
         Map<String, Class> ret = new HashMap<String, Class>();
         ret.put(ROW_KEY, String.class);
         return ret;
     }
 
     @Override
     public byte[] generateRowKey(String tableName, Map<String, Object> components) {
         byte[] pk;
         try {
             Object oKey = components.get(ROW_KEY);
             pk = Bytes.toBytes((String) oKey);
         } catch (Exception e) {
             log.error("Unable to form row key from components", e);
             pk = Bytes.toBytes("");
         }
 
         return pk;
     }
 }
 
 class OrmSurus implements OrmInterface {
     private static final Logger log = Logger.getLogger(OrmSurus.class);
     public static final Long VERSION = 1L;
 
     @Override
     public boolean containsTable(String tableName) {
         return TableContext.containsTable(tableName);
     }
 
     @Override
     public Map<byte[], Map<Long, Map<String, Map<String, String>>>> parseResults(String tableName, Result[] results) {
         Map<byte[], Map<Long, Map<String, Map<String, String>>>> casted =
                 new HashMap<byte[], Map<Long, Map<String, Map<String, String>>>>();
 
         try {
             PoolManager poolManager = TableContext.getPoolManager(tableName);
             EntityService entityService = poolManager.getEntityService();
 
             for (Result result : results) {
                 @SuppressWarnings({"unchecked"})
                 Map<String, Map<String, String>> retrievedMap = (Map<String, Map<String, String>>) entityService.parseIntoMap(result);
                 Map<Long, Map<String, Map<String, String>>> wrapper = new HashMap<Long, Map<String, Map<String, String>>>();
                 wrapper.put(VERSION, retrievedMap);
                 casted.put(result.getRow(), wrapper);
             }
         } catch (Exception e) {
             log.error("Unable to parse Result", e);
         }
 
         return casted;
     }
 
     @Override
     public String parseRowKey(String tableName, byte[] pk) {
         String strRowKey;
         try {
             PoolManager poolManager = TableContext.getPoolManager(tableName);
             AbstractPrimaryKey primaryKey = poolManager.getPrimaryKey();
             strRowKey = primaryKey.toString(pk);
         } catch (Exception e) {
             strRowKey = "UNKNOWN";
         }
 
         return strRowKey;
     }
 
     @Override
     public Map<String, Class> getRowKeyComponents(String tableName) {
         PoolManager poolManager = TableContext.getPoolManager(tableName);
         AbstractPrimaryKey primaryKey = poolManager.getPrimaryKey();
         HFieldComponent[] components = primaryKey.getComponents();
 
         Map<String, Class> mapComponents = new HashMap<String, Class>();
         for (HFieldComponent component : components) {
             mapComponents.put(component.name(), component.type());
         }
 
         return mapComponents;
     }
 
     @Override
     public byte[] generateRowKey(String tableName, Map<String, Object> components) {
         PoolManager poolManager = TableContext.getPoolManager(tableName);
         AbstractPrimaryKey primaryKey = poolManager.getPrimaryKey();
         return primaryKey.generateRowKey(components).get();
     }
 }
 
 public class OrmContext {
     private final static List<OrmInterface> CONTEXT = new ArrayList<OrmInterface>();
     private static final Logger log = Logger.getLogger(OrmContext.class);
 
     static {
         CONTEXT.add(new OrmSurus());
     }
 
     /**
      * @param tableName the
      * @return True if non-default (i.e. custom) ORM for the table is registered
      */
     public static boolean containsOrmFor(String tableName) {
         for (OrmInterface orm : CONTEXT) {
             if (orm.containsTable(tableName)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @param tableName the
      * @return custom ORM if @containsOrmFor returns True for given tableName,
      *         and instance of OrmDefault in case table is not covered by any custom ORMs
      */
     public static OrmInterface getOrmFor(String tableName) {
         for (OrmInterface orm : CONTEXT) {
             if (orm.containsTable(tableName)) {
                 return orm;
             }
         }
         return new OrmDefault();
     }
 }
