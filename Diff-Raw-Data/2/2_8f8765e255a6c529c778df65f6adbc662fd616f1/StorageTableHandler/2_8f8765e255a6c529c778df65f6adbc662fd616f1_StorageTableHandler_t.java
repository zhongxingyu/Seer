 /*
  * $Id$
  *
  * Copyright (c) 2012 Runal House.
  * All Rights Reserved.
  */
 package net.runal.jtool.logging;
 
 import com.microsoft.windowsazure.serviceruntime.RoleEnvironment;
 import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
 import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
 import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
 import com.microsoft.windowsazure.services.core.storage.StorageException;
 import com.microsoft.windowsazure.services.table.client.CloudTable;
 import com.microsoft.windowsazure.services.table.client.CloudTableClient;
 import com.microsoft.windowsazure.services.table.client.TableOperation;
 import com.microsoft.windowsazure.services.table.client.TableQuery;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.*;
 import java.util.regex.Pattern;
 import net.runal.jtool.storage.StorageConstants;
 import net.runal.jtool.util.CloudProperties;
 
 /**
  *
  * @author yasuo
  */
 public class StorageTableHandler extends Handler {
     private static final Logger LOGGER
         = Logger.getLogger(StorageTableHandler.class.getName());
 
     private static Exception log(Level level, String msg, Throwable thrown) throws Error {
         LOGGER.log(level, msg, thrown);
         if(thrown instanceof Exception) {
             return (Exception)thrown;
         } else if(thrown instanceof Error) {
             throw (Error)thrown;
         } else {
             return new IllegalArgumentException(thrown);
         }
     }
     private static void log(Level level, String pattern, Object... args) {
         LOGGER.log(level, pattern, args);
     }
 
     public static final String TABLE_NAME_KEY = "TableName";
     public static final String DEFAULT_TABLE_NAME = "LogRecordTable";
 
     //
     private CloudTable table;
     private final List<LogRecordTableEntity> list;
 
     public StorageTableHandler() {
         list = new ArrayList<LogRecordTableEntity>(10);
         LogManager manager = LogManager.getLogManager();
         manager.checkAccess();
         configure();
         connect();
     }
 
     private int limit;
     private Cfg cfg;
 
     private Level getLevelProperty(String string, Level defaultValue) {
         try {
             return Level.parse(string);
         } catch(Exception ex) {
             return defaultValue;
         }
     }
     private Filter getFilterProperty(String string, Filter defaultValue) {
         try {
             Class<?> cls = Class.forName(string);
             if(Filter.class.isAssignableFrom(cls)) {
                 Object instance = cls.newInstance();
                 return (Filter) instance;
             } else {
                 return defaultValue;
             }
         } catch(Exception ex) {
             return defaultValue;
         }
     }
     private Formatter getFormatterProperty(String string, Formatter defaultValue) {
         try {
             Class<?> cls = Class.forName(string);
             if(Formatter.class.isAssignableFrom(cls)) {
                 Object instance = cls.newInstance();
                 return (Formatter) instance;
             } else {
                 return defaultValue;
             }
         } catch(Exception ex) {
             return defaultValue;
         }
     }
 //    private String getStringProperty(String string, String defaultValue) {
 //        if(string instanceof String) {
 //            return string;
 //        } else {
 //            return defaultValue;
 //        }
 //    }
     private int getIntProperty(String string, int defaultValue) {
         try {
             return Integer.valueOf(string);
         } catch(Exception ex) {
             return defaultValue;
         }
     }
 
     
     
     
     
     
     
     
     
 ////////////////////////////////////////////////////////////////////////////////
 
 private class Cfg implements StorageConstants {
 
     private String accountName;
     private String accountKey;
     private String endPoint;
     private String connectionString;
     private String diagnosticsConnectionString;
     private String tableName;
 
     private void configure() {
         final LogManager manager = LogManager.getLogManager();
         final CloudProperties prop = CloudProperties.getInstance();
        final String cname = StorageTableHandler.class.getName();
 
         //
         accountName = manager.getProperty(cname + "." +ACCOUNT_NAME_KEY);
         if(accountName == null) {
             accountName = prop.getProperty(cname + "." +ACCOUNT_NAME_KEY);
         }
         if(accountName == null) {
             accountName = prop.getProperty(ACCOUNT_NAME_KEY);
         }
         //
         accountKey = manager.getProperty(cname + "." +ACCOUNT_KEY_KEY);
         if(accountKey == null) {
             accountKey = prop.getProperty(cname + "." +ACCOUNT_KEY_KEY);
         }
         if(accountKey == null) {
             accountKey = prop.getProperty(ACCOUNT_KEY_KEY);
         }
         //
         endPoint = manager.getProperty(cname + "." +ENDPOINT_KEY);
         if(endPoint == null) {
             endPoint = prop.getProperty(cname + "." +ENDPOINT_KEY);
         }
         if(endPoint == null) {
             endPoint = prop.getProperty(ENDPOINT_KEY);
         }
         //
         connectionString = manager.getProperty(cname + "." +CONNECTION_STRING_KEY);
         if(connectionString == null) {
             connectionString = prop.getProperty(cname + "." +CONNECTION_STRING_KEY);
         }
         if(connectionString == null) {
             connectionString = prop.getProperty(CONNECTION_STRING_KEY);
         }
 
         //
         if(RoleEnvironment.isAvailable()) {
             diagnosticsConnectionString = RoleEnvironment
                 .getConfigurationSettings().get(DIAGNOSTICS_CONNECTION_STRING_KEY);
         } else {
             diagnosticsConnectionString = null;
         }
 
         //
         if(endPoint != null && accountName != null && !accountName.isEmpty()) {
             endPoint = endPoint.replaceFirst(Pattern.quote("${" + ACCOUNT_NAME_KEY +"}"), accountName);
         }
 
         //
         tableName = manager.getProperty(cname + "." +TABLE_NAME_KEY);
         if(tableName == null) {
             tableName = DEFAULT_TABLE_NAME;
         }
     }
 
     /**
      * TABLEストレージクライアントを取得します。
      * 
      * @return
      */
     private CloudTableClient getClient() {
         configure();
         //
         switch(0) {
             case 0:
             try {
                 StorageCredentials credentails
                     = new StorageCredentialsAccountAndKey(accountName, accountKey);
                 //
                 CloudTableClient newClient
                     = new CloudTableClient(new URI(endPoint), credentails);
                 log(Level.CONFIG, "getClient({0}, {1})", ACCOUNT_NAME_KEY, ACCOUNT_KEY_KEY);
                 return newClient;
             } catch (Exception ex) {
                 log(Level.FINEST, null, ex);
             }
             case 1:
             try {
                 CloudStorageAccount storageAccount
                     = CloudStorageAccount.parse(connectionString);
                 //
                 CloudTableClient newClient
                     = storageAccount.createCloudTableClient();
                 log(Level.CONFIG, "getClient({0})", CONNECTION_STRING_KEY);
                 return newClient;
             } catch (Exception ex) {
                 log(Level.FINEST, null, ex);
             }
             case 2:
             try {
                 CloudStorageAccount storageAccount
                     = CloudStorageAccount.parse(diagnosticsConnectionString);
                 //
                 CloudTableClient newClient
                     = storageAccount.createCloudTableClient();
                 log(Level.CONFIG, "getClient({0})", DIAGNOSTICS_CONNECTION_STRING_KEY);
                 return newClient;
             } catch (Exception ex) {
                 log(Level.FINEST, null, ex);
             }
             default:
             log(Level.CONFIG, "getClient()");
             return null;
         }
     }
 
 }
 ////////////////////////////////////////////////////////////////////////////////
 
     private void configure() {
         LogManager manager = LogManager.getLogManager();
         CloudProperties prop = CloudProperties.getInstance();
 
         String cname = getClass().getName();
 
         limit = getIntProperty(manager.getProperty(cname + ".limit"), 10);
         if (limit < 0) {
             limit = 0;
         }
 
         setLevel(getLevelProperty(manager.getProperty(cname + ".level"), Level.ALL));
         setFilter(getFilterProperty(manager.getProperty(cname + ".filter"), null));
         setFormatter(getFormatterProperty(manager.getProperty(cname + ".formatter"), new XMLFormatter()));
 
         cfg = new Cfg();
     }
 
     private void connect() {
         LogManager manager = LogManager.getLogManager();
         manager.checkAccess();
 
         try {
             CloudTableClient newClient = cfg.getClient();
             if(newClient == null) {
                 table = null;
             } else {
                 CloudTable newTable = new CloudTable(cfg.tableName, newClient);
                 table = newTable;
             }
         } catch (Exception ex) {
         }
     }
     
     public URI getEndPointURI() {
         if (table != null) {
             return table.getServiceClient().getEndpoint();
         } else {
             return null;
         }
     }
 
     public String getTableName() {
         if (table != null) {
             return table.getName();
         } else {
             return null;
         }
     }
 
     public Iterator<LogRecordTableEntity> query(String condition) throws StorageException {
         if (table != null) {
             if(!table.exists()) {
                 return Collections.emptyIterator();
             }
 
             // クエリ
             TableQuery<LogRecordTableEntity> query
                 = TableQuery.from(table.getName(), LogRecordTableEntity.class)
                 .where(condition)
                 ;
             return table.getServiceClient().execute(query).iterator();
         } else {
             throw new IllegalStateException();
         }
     }
 
     @Override
     public boolean isLoggable(LogRecord record) {
         if (table == null || record == null) {
             return false;
         }
         return super.isLoggable(record);
     }
 
     @Override
     public void publish(LogRecord record) {
         if (!isLoggable(record)) {
             return;
         }
         String msg;
         try {
             msg = getFormatter().format(record);
         } catch (Exception ex) {
             // We don't want to throw an exception here, but we
             // report the exception to any registered ErrorManager.
             reportError(null, ex, ErrorManager.FORMAT_FAILURE);
             return;
         }
 
         synchronized(list) {
             if(limit == 0 || limit > list.size()) {
                 list.add(new LogRecordTableEntity(record, msg));
             } else {
                 reportError(null, new IllegalStateException("overflow"), ErrorManager.WRITE_FAILURE);
             }
         }
         flush();
     }
 
     private void write(Iterator<LogRecordTableEntity> it) throws StorageException {
         while(it.hasNext()) {
             TableOperation insert = TableOperation.insert(it.next());
             table.getServiceClient().execute(table.getName(), insert);
             it.remove();
         }
     }
     @Override
     public void flush() {
         if (table != null) {
             synchronized(list) {
                 final Iterator<LogRecordTableEntity> it = list.iterator();
                 try {
                     write(it);
                 } catch(StorageException ex) {
                     reportError(null, ex, ErrorManager.WRITE_FAILURE);
                     try {
                         if(!table.exists()) {
                             table.create();
                         }
                         write(it);
                     } catch(StorageException ex2) {
                         reportError(null, ex2, ErrorManager.FLUSH_FAILURE);
                     }
                 }
             }
         }
     }
 
     @Override
     public void close() throws SecurityException {
         flush();
         table = null;
     }
 
 }
