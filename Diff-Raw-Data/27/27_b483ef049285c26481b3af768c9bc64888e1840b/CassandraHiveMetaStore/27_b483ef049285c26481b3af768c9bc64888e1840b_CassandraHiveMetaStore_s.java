 package org.apache.cassandra.hadoop.hive.metastore;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
 import org.apache.cassandra.thrift.Cassandra;
 import org.apache.cassandra.thrift.CfDef;
 import org.apache.cassandra.thrift.InvalidRequestException;
 import org.apache.cassandra.thrift.KsDef;
 import org.apache.cassandra.thrift.NotFoundException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hive.metastore.RawStore;
 import org.apache.hadoop.hive.metastore.api.Database;
 import org.apache.hadoop.hive.metastore.api.Index;
 import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hadoop.hive.metastore.api.Partition;
 import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
 import org.apache.hadoop.hive.metastore.api.PrincipalType;
 import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
 import org.apache.hadoop.hive.metastore.api.Role;
 import org.apache.hadoop.hive.metastore.api.Table;
 import org.apache.hadoop.hive.metastore.api.Type;
 import org.apache.hadoop.hive.metastore.model.MDBPrivilege;
 import org.apache.hadoop.hive.metastore.model.MGlobalPrivilege;
 import org.apache.hadoop.hive.metastore.model.MPartitionColumnPrivilege;
 import org.apache.hadoop.hive.metastore.model.MPartitionPrivilege;
 import org.apache.hadoop.hive.metastore.model.MRoleMap;
 import org.apache.hadoop.hive.metastore.model.MTableColumnPrivilege;
 import org.apache.hadoop.hive.metastore.model.MTablePrivilege;
 import org.apache.thrift.TBase;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Serializes thrift structs for Hive Meta Store to Apache Cassandra.
  * 
  * All of the 'entities' in the meta store schema go into a single row for 
  * the database for which they belong.
  * 
  * Database names are stored in a special row with the key '__databases__'
  * 
  * Meta information such as roles and privileges (that is, 'entities' that
  * can be cross-database) go into the row with the key '__meta__'
  *
  *
  * @author zznate
  */
 public class CassandraHiveMetaStore implements RawStore {        
     
     private static final Logger log = LoggerFactory.getLogger(CassandraHiveMetaStore.class);
     private Configuration configuration;
     private MetaStorePersister metaStorePersister;
     private CassandraClientHolder cassandraClientHolder;
     
     public CassandraHiveMetaStore()
     {
         log.debug("Creating CassandraHiveMetaStore");
     }
     
 
     public void setConf(Configuration conf)
     {
         configuration = conf;
         cassandraClientHolder = new CassandraClientHolder(configuration);
         
         Cassandra.Iface client = cassandraClientHolder.getClient();        
         createSchemaIfNeeded(client, conf);                        
         metaStorePersister = new MetaStorePersister(configuration);
     }
     
     public Configuration getConf()
     {        
         return configuration;
     }    
     
     private void createSchemaIfNeeded(Cassandra.Iface client, Configuration conf) 
     {
         // Database_entities : {[name].name=name}
         // FIXME add these params to configuration
         // databaseName=metastore_db;create=true
         try {
             client.set_keyspace(cassandraClientHolder.getKeyspaceName());
             return;
         } catch (InvalidRequestException ire) {
             log.info("HiveMetaStore keyspace did not exist. Creating.");
         } catch (Exception e) {
             throw new CassandraHiveMetaStoreException("Could not create or validate existing schema", e);
         }
         CfDef cf = new CfDef(cassandraClientHolder.getKeyspaceName(), 
                 cassandraClientHolder.getColumnFamily());
         cf.setComparator_type("UTF8Type");
         KsDef ks = new KsDef(cassandraClientHolder.getKeyspaceName(), 
                 "org.apache.cassandra.locator.SimpleStrategy", 
                 conf.getInt(CassandraClientHolder.CONF_PARAM_REPLICATION_FACTOR, 1), 
                 Arrays.asList(cf));
         try {
             client.system_add_keyspace(ks);
         } catch (Exception e) {
             throw new CassandraHiveMetaStoreException("Could not create Hive MetaStore database: " + e.getMessage(), e);
         }
     }
 
     
     public void createDatabase(Database database) throws InvalidObjectException, MetaException
     {
         log.debug("createDatabase with {}", database);
 
         metaStorePersister.save(database.metaDataMap, database, database.getName());
         metaStorePersister.save(database.metaDataMap, database, CassandraClientHolder.DATABASES_ROW_KEY);
     }
 
     
     public Database getDatabase(String databaseName) throws NoSuchObjectException
     {
         log.debug("in getDatabase with database name: {}", databaseName);
         Database db = new Database();
         try 
         {
             metaStorePersister.load(db, databaseName);
         } 
         catch (NotFoundException e) 
         {
             throw new NoSuchObjectException("Database named " + databaseName + " did not exist.");
         }        
         return db;
     }
     
     public List<String> getDatabases(String databaseNamePattern) throws MetaException
     {
         log.debug("in getDatabases with databaseNamePattern: {}", databaseNamePattern);
         List<TBase> databases = metaStorePersister.find(new Database(), 
                 CassandraClientHolder.DATABASES_ROW_KEY, databaseNamePattern,100);
         List<String> results = new ArrayList<String>(databases.size());
         for (TBase tBase : databases)
         {
             Database db = (Database)tBase;
             if ( StringUtils.isEmpty(databaseNamePattern) || db.getName().matches(databaseNamePattern) )
                 results.add(db.getName());            
         }
         return results;
     }    
     
     public boolean alterDatabase(String databaseName, Database database)
             throws NoSuchObjectException, MetaException
     {
         try 
         {
             createDatabase(database);
         } 
         catch (InvalidObjectException e) 
         {
             throw new CassandraHiveMetaStoreException("Error attempting to alter database: " + databaseName, e);
         }        
         return true;
     }
     
     public boolean dropDatabase(String databaseName) throws NoSuchObjectException,
             MetaException
     {        
         Database database = new Database();
         database.setName(databaseName);
         metaStorePersister.remove(database, databaseName);
         return true;
     }
 
     public List<String> getAllDatabases() throws MetaException
     {
         return getDatabases(StringUtils.EMPTY);
     }
 
     
     public void createTable(Table table) throws InvalidObjectException, MetaException
     {                   
         metaStorePersister.save(table.metaDataMap, table, table.getDbName());        
     }
 
     public Table getTable(String databaseName, String tableName) throws MetaException
     {
         log.debug("in getTable with database name: {} and table name: {}", databaseName, tableName);
         Table table = new Table();
         table.setTableName(tableName);            
         try 
         {
             metaStorePersister.load(table, databaseName);
         } 
         catch (NotFoundException e) 
         {
             //throw new MetaException("Table: " + tableName + " did not exist in database: " + databaseName);
             return null;
         }
         return table;
     }
     
     /**
      * Retrieve the tables for the given database and pattern.
      * 
      * @param dbName
      * @param tableNamePattern the pattern passed as is to {@link String#matches(String)} of
      * {@link Table#getTableName()}
      */
     public List<String> getTables(String dbName, String tableNamePattern)
     throws MetaException
     {
         log.info("in getTables with dbName: {} and tableNamePattern: {}", dbName, tableNamePattern);
         List<TBase> tables = metaStorePersister.find(new Table(), dbName);
         List<String> results = new ArrayList<String>(tables.size());
         for (TBase tBase : tables)
         {
             Table table = (Table)tBase;
             if ( StringUtils.isEmpty(tableNamePattern) || table.getTableName().matches(tableNamePattern))
                 results.add(table.getTableName());
         }
         return results;
     }
 
     public List<String> getAllTables(String databaseName) throws MetaException
     {
         log.debug("in getAllTables");
         return getTables(databaseName, StringUtils.EMPTY);
     }
     
     public void alterTable(String databaseName, String tableName, Table table)
             throws InvalidObjectException, MetaException
     {
         if ( log.isDebugEnabled() ) 
             log.debug("Altering table {} on datbase: {} Table: {}",
                     new Object[]{tableName, databaseName, table});
         createTable(table);
     }   
     
     public boolean dropTable(String databaseName, String tableName) throws MetaException
     {
         Table table = new Table();
         table.setDbName(databaseName);
         table.setTableName(tableName);
         metaStorePersister.remove(table, databaseName);
         return true;
     }
     
     public boolean addIndex(Index index) throws InvalidObjectException,
             MetaException
     {
         metaStorePersister.save(index.metaDataMap, index, index.getDbName());
         return false;
     }
 
     public Index getIndex(String databaseName, String tableName, String indexName)
             throws MetaException
     {
         Index index = new Index();
         index.setDbName(databaseName);
         index.setIndexName(indexName);
         index.setOrigTableName(tableName);
         try {
             metaStorePersister.load(index, databaseName);
         } catch (NotFoundException nfe) {
             throw new MetaException("Index: " + indexName + " did not exist for table: " + tableName + " in database: " + databaseName );
         }
         return index;
     }
     
     public List<Index> getIndexes(String databaseName, String originalTableName, int max)
             throws MetaException
     {
         List results = metaStorePersister.find(new Index(), databaseName, originalTableName, max);
         return (List<Index>)results;
     }
     
     public List<String> listIndexNames(String databaseName, String originalTableName, short max)
             throws MetaException
     {     
         List<Index> indexes = getIndexes(databaseName, originalTableName, max);
         List<String> results = new ArrayList<String>(indexes.size());
         for (Index index : indexes)
         {
             results.add(index.getIndexName());
         }        
         return results;
     }
     
     public void alterIndex(String databaseName, String originalTableName, 
             String indexName, Index index)
             throws InvalidObjectException, MetaException
     {
         if ( log.isDebugEnabled() )
             log.debug("Altering index {} on database: {} and table: {} Index: {}", 
                     new Object[]{ indexName, databaseName, originalTableName, index});
         addIndex(index);
     }
     
     public boolean dropIndex(String databaseName, String originalTableName, String indexName)
             throws MetaException
     {
         Index index = new Index();
         index.setDbName(databaseName);
         index.setOrigTableName(originalTableName);
         index.setIndexName(indexName);
         metaStorePersister.remove(index, databaseName);
         return true;
     }
 
     public boolean addPartition(Partition partition) throws InvalidObjectException,
             MetaException
     {
         metaStorePersister.save(partition.metaDataMap, partition, partition.getDbName());
         return true;
     }
         
     public Partition getPartition(String databaseName, String tableName, List<String> partitions)
             throws MetaException, NoSuchObjectException
     {
         Partition partition = new Partition();
         partition.setDbName(databaseName);
         partition.setTableName(tableName);
         partition.setValues(partitions);
         try {
             metaStorePersister.load(partition, databaseName);
         } catch (NotFoundException e) {
             throw new NoSuchObjectException("Could not find partition for: " + partitions + " on table: " + tableName + " in database: " + databaseName);
         }
         return partition;
     }
     
     public List<Partition> getPartitions(String databaseName, String tableName, int max)
             throws MetaException
     {
         List results = metaStorePersister.find(new Partition(), databaseName, null, max);
         
         return (List<Partition>)results;
     }
     
     public List<String> listPartitionNames(String databaseName, String tableName, short max)
             throws MetaException
     {   
         List<Partition> partitions = getPartitions(databaseName, tableName, max);
         List<String> results = new ArrayList<String>(partitions.size());
         for (Partition partition : partitions)
         {
             results.addAll(partition.getValues());
         }
         return results;
     }
 
     public void alterPartition(String databaseName, String tableName, Partition partition)
             throws InvalidObjectException, MetaException
     {
         if ( log.isDebugEnabled() ) 
             log.debug("Altering partiion for table {} on database: {} Partition: {}",
                     new Object[]{tableName, databaseName, partition});
         addPartition(partition);
     }
 
     public boolean dropPartition(String databaseName, String tableName, List<String> partitions)
             throws MetaException
     {
         Partition partition = new Partition();
         partition.setDbName(databaseName);
         partition.setTableName(tableName);
         partition.setValues(partitions);
         metaStorePersister.remove(partition, databaseName);
         return true;
     }       
 
     public boolean addRole(String roleName, String ownerName)
             throws InvalidObjectException, MetaException, NoSuchObjectException
     {
         Role role = new Role();
         role.setOwnerName(ownerName);
         role.setRoleName(roleName);
         metaStorePersister.save(role.metaDataMap, role, CassandraClientHolder.META_DB_ROW_KEY);
         return true;
     }
     
     public Role getRole(String roleName) throws NoSuchObjectException
     {
         Role role = new Role();
         role.setRoleName(roleName);
         try {
             metaStorePersister.load(role, CassandraClientHolder.META_DB_ROW_KEY);
         } catch (NotFoundException nfe) {
             throw new NoSuchObjectException("could not find role: " + roleName);
         }
         return role;
     }
         
     public boolean createType(Type type)
     {
         metaStorePersister.save(type.metaDataMap, type, CassandraClientHolder.META_DB_ROW_KEY);
         return true;
     }
     
     public Type getType(String type)
     {
         Type t = new Type();
         t.setName(type);
         try {
             metaStorePersister.load(t, CassandraClientHolder.META_DB_ROW_KEY);
         } catch (NotFoundException e) {
             return null;
         }
         return t;
     }
     
     public boolean dropType(String type)
     {
         Type t = new Type();
         t.setName(type);
         metaStorePersister.remove(t, CassandraClientHolder.META_DB_ROW_KEY);
         return true;
     }
 
 
     public boolean commitTransaction()
     {
         // FIXME default to true for now
         return true;
     }
 
     @Override
     public PrincipalPrivilegeSet getColumnPrivilegeSet(String arg0,
             String arg1, String arg2, String arg3, String arg4,
             List<String> arg5) throws InvalidObjectException, MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public PrincipalPrivilegeSet getDBPrivilegeSet(String arg0, String arg1,
             List<String> arg2) throws InvalidObjectException, MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public PrincipalPrivilegeSet getPartitionPrivilegeSet(String arg0,
             String arg1, String arg2, String arg3, List<String> arg4)
             throws InvalidObjectException, MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Partition getPartitionWithAuth(String arg0, String arg1,
             List<String> arg2, String arg3, List<String> arg4)
             throws MetaException, NoSuchObjectException, InvalidObjectException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<Partition> getPartitionsByFilter(String databaseName, String tableName,
             String filter, short maxPartitions) throws MetaException,
             NoSuchObjectException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<Partition> getPartitionsWithAuth(String arg0, String arg1,
             short arg2, String arg3, List<String> arg4) throws MetaException,
             NoSuchObjectException, InvalidObjectException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public PrincipalPrivilegeSet getTablePrivilegeSet(String arg0, String arg1,
             String arg2, List<String> arg3) throws InvalidObjectException,
             MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
 
     @Override
     public PrincipalPrivilegeSet getUserPrivilegeSet(String arg0,
             List<String> arg1) throws InvalidObjectException, MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean grantPrivileges(PrivilegeBag arg0)
             throws InvalidObjectException, MetaException, NoSuchObjectException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean grantRole(Role arg0, String arg1, PrincipalType arg2,
             String arg3, PrincipalType arg4, boolean arg5)
             throws MetaException, NoSuchObjectException, InvalidObjectException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public List<MTablePrivilege> listAllTableGrants(String arg0,
             PrincipalType arg1, String arg2, String arg3)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<String> listPartitionNamesByFilter(String arg0, String arg1,
             String arg2, short arg3) throws MetaException
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MDBPrivilege> listPrincipalDBGrants(String arg0,
             PrincipalType arg1, String arg2)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MGlobalPrivilege> listPrincipalGlobalGrants(String arg0,
             PrincipalType arg1)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MPartitionColumnPrivilege> listPrincipalPartitionColumnGrants(
             String arg0, PrincipalType arg1, String arg2, String arg3,
             String arg4, String arg5)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MPartitionPrivilege> listPrincipalPartitionGrants(String arg0,
             PrincipalType arg1, String arg2, String arg3, String arg4)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MTableColumnPrivilege> listPrincipalTableColumnGrants(
             String arg0, PrincipalType arg1, String arg2, String arg3,
             String arg4)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<String> listRoleNames()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public List<MRoleMap> listRoles(String arg0, PrincipalType arg1)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean openTransaction()
     {
         // TODO Auto-generated method stub
         return true;
     }
 
     @Override
     public boolean removeRole(String arg0) throws MetaException,
             NoSuchObjectException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean revokePrivileges(PrivilegeBag arg0)
             throws InvalidObjectException, MetaException, NoSuchObjectException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean revokeRole(Role arg0, String arg1, PrincipalType arg2)
             throws MetaException, NoSuchObjectException
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void rollbackTransaction()
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void shutdown()
     {
         // TODO Auto-generated method stub
 
     }    
     
 
 
 }
