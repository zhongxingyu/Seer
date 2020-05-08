 package net.thumbtack.research.nosql.clients;
 
 import net.thumbtack.research.nosql.Configurator;
 import net.thumbtack.research.nosql.utils.StringSerializer;
 import org.apache.cassandra.locator.SimpleStrategy;
 import org.apache.cassandra.thrift.*;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 import org.apache.thrift.transport.TTransportException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public final class CassandraClient implements Database {
     private static final String DEFAULT_HOST = "localhost";
     private static final int DEFAULT_PORT = 9160;
     private static final String KEY_SPACE_PROPERTY = "cassandra.keySpace";
     private static final String DEFAULT_KEY_SPACE = "key_space";
     private static final String COLUMN_FAMILY_PROPERTY = "cassandra.columnFamily";
     private static final String DEFAULT_COLUMN_FAMILY = "column_family";
     private static final String COLUMN_NAME_PROPERTY = "cassandra.columnName";
     private static final String DEFAULT_COLUMN_NAME = "column_name";
 
     private static final String READ_CONSISTENCY_LEVEL_PROPERTY = "cassandra.readConsistencyLevel";
     private static final String WRITE_CONSISTENCY_LEVEL_PROPERTY = "cassandra.writeConsistencyLevel";
 
     private static final String REPLICATION_FACTOR_PROPERTY = "cassandra.replicationFactor";
     private static final String DEFAULT_REPLICATION_FACTOR = "1";
 
     private static final String STRATEGY_REPLICATION_FACTOR_PROPERTY = "replication_factor";
 
     private static final Logger log = LoggerFactory.getLogger(CassandraClient.class);
 
     private static final StringSerializer ss = StringSerializer.get();
 
     private ConsistencyLevel readConsistencyLevel;
     private ConsistencyLevel writeConsistencyLevel;
 
     private String columnFamily;
     private String columnName;
 
     private Cassandra.Client client;
     private TTransport transport;
 
     private final Map<String, List<Mutation>> mutationMap = new HashMap<>(1);
     private ColumnOrSuperColumn superColumn;
 
     CassandraClient() {
     }
 
     @Override
     public void close() throws Exception {
         transport.close();
     }
 
     @Override
     public void init(Configurator configurator) {
         if(log.isDebugEnabled()) {
             log.debug("Configurator: " + configurator);
         }
         try {
             String host = configurator.getNextDbHost(DEFAULT_HOST);
             int port = configurator.getDbPort(DEFAULT_PORT);
             log.debug("Client initialization: " + host + ":" + port);
             transport = new TFramedTransport(new TSocket(host, port));
             client = new Cassandra.Client(new TBinaryProtocol(transport));
             transport.open();
 
             String keySpace = configurator.getString(KEY_SPACE_PROPERTY, DEFAULT_KEY_SPACE);
             String replicationFactor = configurator.getString(REPLICATION_FACTOR_PROPERTY, DEFAULT_REPLICATION_FACTOR);
             columnFamily = configurator.getString(COLUMN_FAMILY_PROPERTY, DEFAULT_COLUMN_FAMILY);
             columnName = configurator.getString(COLUMN_NAME_PROPERTY, DEFAULT_COLUMN_NAME);
 
             setReplicationFactor(keySpace, replicationFactor);
 
             client.set_keyspace(keySpace);
 
             readConsistencyLevel = getConsistencyLevel(
                     configurator,
                     READ_CONSISTENCY_LEVEL_PROPERTY,
                     ConsistencyLevel.ONE
             );
             writeConsistencyLevel = getConsistencyLevel(
                     configurator,
                     WRITE_CONSISTENCY_LEVEL_PROPERTY,
                     ConsistencyLevel.ONE
             );
             columnFamily = configurator.getString(COLUMN_FAMILY_PROPERTY, DEFAULT_COLUMN_FAMILY);
             columnName = configurator.getString(COLUMN_NAME_PROPERTY, DEFAULT_COLUMN_NAME);
 
             client.truncate(columnFamily);
         } catch (TException e) {
             e.printStackTrace();
             log.error(e.getMessage());
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void write(String key, ByteBuffer value) {
 
         Map<ByteBuffer, Map<String, List<Mutation>>> record = new HashMap<>(1);
 
         ByteBuffer wrappedKey;
 
         ColumnOrSuperColumn superColumn = getSuperColumn();
         Column col = superColumn.column;
         wrappedKey = ss.toByteBuffer(key);
         col.setName(ss.toByteBuffer(columnName));
         col.setValue(value);
         col.setTimestamp(System.currentTimeMillis());
 
         record.put(wrappedKey, mutationMap);
        Exception exception = null;
         for (int i=0; i<10; i++) {
             try {
                 client.batch_mutate(record, writeConsistencyLevel);
             } catch (TTransportException e) {
                 e.printStackTrace();
                 log.error(e.getMessage() + " TTransportException.Type: " + e.getType());
                exception = e;
                 continue;
             } catch (TException e) {
                 e.printStackTrace();
                 log.error(e.getMessage());
                exception = e;
                 continue;
             }
             if(log.isDebugEnabled()) {
                 log.debug("Written key:" + ss.fromByteBuffer(wrappedKey) + " value: " + ss.fromByteBuffer(value));
             }
             return;
         }
        throw new RuntimeException(exception);
     }
 
     @Override
     public ByteBuffer read(String key) {
         ColumnPath parent = new ColumnPath(columnFamily);
         try {
             parent.column = ss.toByteBuffer(columnName);
             return client.get(
                     ss.toByteBuffer(key),
                     parent,
                     readConsistencyLevel
             ).column.value;
         }catch (NotFoundException e) {
             log.debug(e.getMessage());
         } catch (TException e) {
             log.error(e.toString());
             throw new RuntimeException(e);
         }
         return null;
     }
 
     private ConsistencyLevel getConsistencyLevel(Configurator configurator, String name, ConsistencyLevel def) {
         String levelName = configurator.getString(name, null);
         if (levelName != null && ConsistencyLevel.valueOf(levelName) != null) {
             return ConsistencyLevel.valueOf(levelName);
         }
         return def;
     }
 
     private void initMutationMap() {
         superColumn = new ColumnOrSuperColumn();
         superColumn.setColumn(new Column());
         List<Mutation> mutations = new ArrayList<>(1);
         mutations.add(new Mutation().setColumn_or_supercolumn(superColumn));
         mutationMap.put(columnFamily, mutations);
     }
 
     private ColumnOrSuperColumn getSuperColumn() {
         if(superColumn == null) {
             initMutationMap();
         }
         return superColumn;
     }
 
     private void setReplicationFactor(String keySpace, String replicationFactor) throws TException {
         KsDef ksDef;
         try {
             ksDef = client.describe_keyspace(keySpace);
             ksDef.strategy_options.put(STRATEGY_REPLICATION_FACTOR_PROPERTY, replicationFactor);
             ksDef.cf_defs.clear();
             client.system_update_keyspace(ksDef);
         }
         catch (NotFoundException e) {
             List<CfDef> cfDefList = new ArrayList<>();
             CfDef cfDef = new CfDef(keySpace, columnFamily);
             cfDefList.add(cfDef);
             ksDef = new KsDef(keySpace, SimpleStrategy.class.getName(), cfDefList);
             Map<String, String> strategyOptions = new HashMap<>();
             strategyOptions.put(STRATEGY_REPLICATION_FACTOR_PROPERTY, replicationFactor);
             ksDef.setStrategy_options(strategyOptions);
             client.system_add_keyspace(ksDef);
         }
     }
 }
