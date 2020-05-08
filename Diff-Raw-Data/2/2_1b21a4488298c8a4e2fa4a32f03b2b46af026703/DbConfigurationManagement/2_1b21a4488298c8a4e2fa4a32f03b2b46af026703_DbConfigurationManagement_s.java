 package net.madz.db.core.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 
 import net.madz.db.utils.LogUtils;
 import net.madz.db.utils.MessageConsts;
 import net.madz.db.configuration.*;
 
 public class DbConfigurationManagement {
 
     private static final String NET_MADZ_DB_CONFIGURATION = "net.madz.db.configuration";
     private static final String CONFIGURATION_DATA_SOURCES = "./conf/Databases.xml";
     private static final Map<String, Database> sourceDatabaseCache = new HashMap<String, Database>();
     private static final Map<String, Database> databaseCopiesCache = new HashMap<String, Database>();
     private static final Map<Sku, SkuConf> skuConfs = new HashMap<Sku, SkuConf>();
     private static DatabaseCopiesServer databaseCopiesServer = null;
     private static DatabaseConfig databaseconfig;
     // TODO [Jan 22, 2013][barry][Done] is it immutable?
     private static final String configurationFilePath = System.getProperty(DbConfigurationManagement.NET_MADZ_DB_CONFIGURATION, CONFIGURATION_DATA_SOURCES);
     static {
         loadDatabaseConfiguration();
     }
 
     private static void loadDatabaseConfiguration() {
         InputStream resource = null;
         try {
             final JAXBContext context = JAXBContext.newInstance(DatabaseConfig.class);
             final Unmarshaller shaller = context.createUnmarshaller();
             resource = new FileInputStream(configurationFilePath);
             databaseconfig = (DatabaseConfig) shaller.unmarshal(resource);
             final List<Database> sourceDatabases = databaseconfig.getSourceDatabases().getDatabase();
             final List<Database> databaseCopies = databaseconfig.getDatabaseCopies().getDatabase();
             final List<SkuConf> skuResult = databaseconfig.getSkuConf();
             databaseCopiesServer = databaseconfig.getDatabaseCopiesServer();
             for ( Database item : sourceDatabases ) {
                 sourceDatabaseCache.put(item.getName(), item);
             }
             for ( Database item : databaseCopies ) {
                 databaseCopiesCache.put(item.getName(), item);
             }
             for ( SkuConf item : skuResult ) {
                 skuConfs.put(item.getSku(), item);
             }
         } catch (JAXBException e) {
             throw new IllegalStateException(e);
         } catch (FileNotFoundException e) {
             throw new IllegalStateException(e);
         } finally {
             if ( null != resource ) {
                 try {
                     resource.close();
                 } catch (IOException ignored) {
                 }
             }
         }
     }
 
     public static Connection createConnection(String databaseName, boolean isCopy) {
         Database database = null;
         Connection connection = null;
         if ( isCopy ) {
             database = databaseCopiesCache.get(databaseName);
             if ( database == null ) {
                 if ( null == databaseCopiesServer ) {
                     throw new IllegalStateException("Please configure database-copies-server information in your Databases.xml file.");
                 }
                 database = databaseCopiesServer.getDatabase();
             }
         } else {
             database = sourceDatabaseCache.get(databaseName);
             if ( database == null ) {
                 throw new IllegalStateException("Please configure source database information for database:" + databaseName);
             }
         }
         try {
             Class.forName(skuConfs.get(database.getSku()).getDriverClass());
             connection = DriverManager.getConnection(database.getUrl(), database.getUser(), database.getPassword());
         } catch (ClassNotFoundException e) {
             throw new IllegalStateException(e);
         } catch (SQLException e) {
             throw new IllegalStateException(e);
         }
         return connection;
     }
 
     public static String getSchemaMetaDataPaser(String databaseName, boolean isCopy) {
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final Database database;
         if ( isCopy ) {
             database = databaseCopiesCache.get(databaseName);
             if ( null == database ) {
                 if ( null != databaseCopiesServer ) {
                     return skuConfs.get(databaseCopiesServer.getDatabase().getSku()).getParserClass();
                 }
             }
         } else {
             database = sourceDatabaseCache.get(databaseName);
             if ( null == database ) {
                 throw new IllegalStateException("Please make sure configure source database information for database:" + databaseName);
             }
         }
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final Sku sku = database.getSku();
         final SkuConf skuConf = skuConfs.get(sku);
         return skuConf.getParserClass();
     }
 
     public static synchronized boolean removeDatabaseInfo(String databaseName) throws JAXBException {
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         final Database database = databaseCopiesCache.get(databaseName);
         if ( null == database ) {
             return false;
         }
         databaseconfig.getDatabaseCopies().getDatabase().remove(database);
        databaseCopiesCache.remove(database);
         try {
             final JAXBContext context = JAXBContext.newInstance(DatabaseConfig.class);
             Marshaller marshaller = context.createMarshaller();
             final File file = new File(configurationFilePath);
             marshaller.marshal(databaseconfig, file);
             return true;
         } catch (JAXBException e) {
             // TODO [Jan 22, 2013][barry][Done] How to handle this exception?
             throw e;
         }
     }
 
     public static String getDatabaseGeneratorClass() {
         if ( null == databaseCopiesServer ) {
             throw new IllegalStateException("Please make sure configure target server information.");
         }
         return skuConfs.get(databaseCopiesServer.getDatabase().getSku()).getGeneratorClass();
     }
 
     public static synchronized void addDatabaseInfo(String targetDatabaseName) {
         // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable
         // variables
         // TODO [Jan 22, 2013][barry][Done] How to make the following code more
         // concisely?
         // If target database is configured, just return;
         if ( null != databaseCopiesCache.get(targetDatabaseName) ) {
             return;
         }
         final Database database = new Database();
         database.setName(targetDatabaseName);
         final Database databaseCopyServer = databaseCopiesServer.getDatabase();
         database.setPassword(databaseCopyServer.getPassword());
         database.setSku(databaseCopyServer.getSku());
         database.setUrl(databaseCopyServer.getUrl() + targetDatabaseName);
         database.setUser(databaseCopyServer.getUser());
         databaseconfig.getDatabaseCopies().getDatabase().add(database);
         databaseCopiesCache.put(targetDatabaseName, database);
         // TODO [Jan 22, 2013][barry] Reconsider variable lifecycle scope
         final JAXBContext context;
         try {
             context = JAXBContext.newInstance(DatabaseConfig.class);
             final Marshaller marshaller = context.createMarshaller();
             final File file = new File(configurationFilePath);
             marshaller.marshal(databaseconfig, file);
         } catch (JAXBException e) {
             // TODO [Jan 22, 2013][barry][Done] How to handle this exception
             LogUtils.error(DbConfigurationManagement.class, e);
         }
     }
 
     public static String getDatabaseComparatorClass(final String databaseName) {
         if ( null == databaseName || 0 >= databaseName.trim().length() ) {
             throw new IllegalArgumentException(MessageConsts.DATABASE_NAME_SHOULD_NOT_BE_NULL);
         }
         final Database database = sourceDatabaseCache.get(databaseName);
         final Sku sku = database.getSku();
         return skuConfs.get(sku).getComparatorClass();
     }
 }
