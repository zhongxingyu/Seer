 package com.hmsonline.storm.cassandra.client;
 
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import storm.trident.tuple.TridentTuple;
 import backtype.storm.tuple.Tuple;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.hmsonline.storm.cassandra.StormCassandraConstants;
 import com.hmsonline.storm.cassandra.bolt.mapper.TridentTupleMapper;
 import com.hmsonline.storm.cassandra.bolt.mapper.TupleCounterMapper;
 import com.hmsonline.storm.cassandra.bolt.mapper.TupleMapper;
 import com.netflix.astyanax.AstyanaxConfiguration;
 import com.netflix.astyanax.AstyanaxContext;
 import com.netflix.astyanax.Cluster;
 import com.netflix.astyanax.Keyspace;
 import com.netflix.astyanax.MutationBatch;
 import com.netflix.astyanax.Serializer;
 import com.netflix.astyanax.annotations.Component;
 import com.netflix.astyanax.connectionpool.ConnectionPoolConfiguration;
 import com.netflix.astyanax.connectionpool.ConnectionPoolMonitor;
 import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
 import com.netflix.astyanax.connectionpool.OperationResult;
 import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
 import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
 import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
 import com.netflix.astyanax.model.ByteBufferRange;
 import com.netflix.astyanax.model.Column;
 import com.netflix.astyanax.model.ColumnFamily;
 import com.netflix.astyanax.model.ColumnList;
 import com.netflix.astyanax.serializers.AnnotatedCompositeSerializer;
 import com.netflix.astyanax.serializers.BigIntegerSerializer;
 import com.netflix.astyanax.serializers.BooleanSerializer;
 import com.netflix.astyanax.serializers.ByteBufferSerializer;
 import com.netflix.astyanax.serializers.ByteSerializer;
 import com.netflix.astyanax.serializers.BytesArraySerializer;
 import com.netflix.astyanax.serializers.DateSerializer;
 import com.netflix.astyanax.serializers.DoubleSerializer;
 import com.netflix.astyanax.serializers.FloatSerializer;
 import com.netflix.astyanax.serializers.Int32Serializer;
 import com.netflix.astyanax.serializers.LongSerializer;
 import com.netflix.astyanax.serializers.ObjectSerializer;
 import com.netflix.astyanax.serializers.ShortSerializer;
 import com.netflix.astyanax.serializers.StringSerializer;
 import com.netflix.astyanax.serializers.UUIDSerializer;
 import com.netflix.astyanax.thrift.ThriftFamilyFactory;
 import com.netflix.astyanax.util.RangeBuilder;
 
 /**
  * 
  * @author tgoetz
  * 
  */
 public class AstyanaxClient<K, C, V> {
     private static final long serialVersionUID = 1L;
     private static final Logger LOG = LoggerFactory.getLogger(AstyanaxClient.class);
     public static final String CASSANDRA_CLUSTER_NAME = "cassandra.clusterName";
     public static final String ASTYANAX_CONFIGURATION = "astyanax.configuration";
     public static final String ASTYANAX_CONNECTION_POOL_CONFIGURATION = "astyanax.connectionPoolConfiguration";
     public static final String ASTYANAX_CONNECTION_POOL_MONITOR = "astyanax.connectioPoolMonitor";
     private AstyanaxContext<Keyspace> astyanaxContext;
     protected Cluster cluster;
     protected Keyspace keyspace;
 
     // not static since we're carting instances around and do not want to share
     // them
     // between bolts
     private final Map<String, Object> DEFAULTS = new ImmutableMap.Builder<String, Object>()
             .put(CASSANDRA_CLUSTER_NAME, "ClusterName")
             .put(ASTYANAX_CONFIGURATION, new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.NONE))
             .put(ASTYANAX_CONNECTION_POOL_CONFIGURATION,
                     new ConnectionPoolConfigurationImpl("MyConnectionPool").setMaxConnsPerHost(1))
             .put(ASTYANAX_CONNECTION_POOL_MONITOR, new CountingConnectionPoolMonitor()).build();
 
 //    public AstyanaxClient(Class<K> columnNameClass, Class<V> columnValueClass) {
 //        super(columnNameClass, columnValueClass);
 //    }
 
     protected AstyanaxContext<Keyspace> createContext(Map<String, Object> config) {
         Map<String, Object> settings = Maps.newHashMap();
         for (Map.Entry<String, Object> defaultEntry : DEFAULTS.entrySet()) {
             if (config.containsKey(defaultEntry.getKey())) {
                 settings.put(defaultEntry.getKey(), config.get(defaultEntry.getKey()));
             } else {
                 settings.put(defaultEntry.getKey(), defaultEntry.getValue());
             }
         }
         // in the defaults case, we don't know the seed hosts until context
         // creation time
         if (settings.get(ASTYANAX_CONNECTION_POOL_CONFIGURATION) instanceof ConnectionPoolConfigurationImpl) {
             ConnectionPoolConfigurationImpl cpConfig = (ConnectionPoolConfigurationImpl) settings
                     .get(ASTYANAX_CONNECTION_POOL_CONFIGURATION);
             cpConfig.setSeeds((String) config.get(StormCassandraConstants.CASSANDRA_HOST));
         }
 
         return new AstyanaxContext.Builder()
                 .forCluster((String) settings.get(CASSANDRA_CLUSTER_NAME))
                 .forKeyspace((String) config.get(StormCassandraConstants.CASSANDRA_KEYSPACE))
                 .withAstyanaxConfiguration((AstyanaxConfiguration) settings.get(ASTYANAX_CONFIGURATION))
                 .withConnectionPoolConfiguration(
                         (ConnectionPoolConfiguration) settings.get(ASTYANAX_CONNECTION_POOL_CONFIGURATION))
                 .withConnectionPoolMonitor((ConnectionPoolMonitor) settings.get(ASTYANAX_CONNECTION_POOL_MONITOR))
                 .buildKeyspace(ThriftFamilyFactory.getInstance());
     }
 
     public void start(Map<String, Object> config) {
         try {
             this.astyanaxContext = createContext(config);
 
             this.astyanaxContext.start();
             this.keyspace = this.astyanaxContext.getEntity();
             // test the connection
             this.keyspace.describeKeyspace();
         } catch (Throwable e) {
             LOG.warn("Astyanax initialization failed.", e);
             throw new IllegalStateException("Failed to prepare Astyanax", e);
         }
     }
 
     public void stop() {
         this.astyanaxContext.shutdown();
     }
 
     public Map<C, V> lookup(TupleMapper<K, C, V> tupleMapper, Tuple input) throws Exception {
         String cf = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         Class<K> keyClass = tupleMapper.getKeyClass();
         Class<C> colClass = tupleMapper.getColumnNameClass();
         
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(cf, (Serializer<K>)serializerFor(keyClass),
                 (Serializer<C>)serializerFor(colClass));
         OperationResult<ColumnList<C>> result;
         result = this.keyspace.prepareQuery(columnFamily).getKey(rowKey).execute();
         ColumnList<C> columns = (ColumnList<C>) result.getResult();
         HashMap<C, V> retval = new HashMap<C, V>();
         Iterator<Column<C>> it = columns.iterator();
         while(it.hasNext()){
             Column<C> col= it.next();
             retval.put(col.getName(), col.getValue((Serializer<V>)serializerFor(tupleMapper.getColumnValueClass())));
         }
         return retval;
     }
     
     
     public Map<C, V> lookup(TridentTupleMapper<K, C, V> tupleMapper, TridentTuple input) throws Exception {
         String cf = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         Class<K> keyClass = tupleMapper.getKeyClass();
         Class<C> colClass = tupleMapper.getColumnNameClass();
         
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(cf, (Serializer<K>)serializerFor(keyClass),
                 (Serializer<C>)serializerFor(colClass));
         OperationResult<ColumnList<C>> result;
         result = this.keyspace.prepareQuery(columnFamily).getKey(rowKey).execute();
         ColumnList<C> columns = (ColumnList<C>) result.getResult();
         HashMap<C, V> retval = new HashMap<C, V>();
         Iterator<Column<C>> it = columns.iterator();
         while(it.hasNext()){
             Column<C> col= it.next();
             retval.put(col.getName(), col.getValue((Serializer<V>)serializerFor(tupleMapper.getColumnValueClass())));
         }
         return retval;
     }
     
     
     public Map<C, V> lookup(TupleMapper<K, C, V> tupleMapper, Tuple input, List<C> slice) throws Exception {
         String cf = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         Class<K> keyClass = tupleMapper.getKeyClass();
         Class<C> colClass = tupleMapper.getColumnNameClass();
         
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(cf, (Serializer<K>)serializerFor(keyClass),
                 (Serializer<C>)serializerFor(colClass));
         OperationResult<ColumnList<C>> result;
         result = this.keyspace.prepareQuery(columnFamily).getKey(rowKey).withColumnSlice((Collection<C>) slice).execute();
         ColumnList<C> columns = (ColumnList<C>) result.getResult();
         HashMap<C, V> retval = new HashMap<C, V>();
         Iterator<Column<C>> it = columns.iterator();
         while(it.hasNext()){
             Column<C> col= it.next();
             retval.put(col.getName(), col.getValue((Serializer<V>)serializerFor(tupleMapper.getColumnValueClass())));
         }
         return retval;        
     }
 
     public Map<C, V> lookup(TupleMapper<K, C, V> tupleMapper, Tuple input, C start, C end) throws Exception {
         if (start == null || end == null) {
             return null;
         }
         
         String cf = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         Class<K> keyClass = tupleMapper.getKeyClass();
         Class<C> colClass = tupleMapper.getColumnNameClass();
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(cf, (Serializer<K>)serializerFor(keyClass),
                 (Serializer<C>)serializerFor(colClass));
         OperationResult<ColumnList<C>> result= this.keyspace.prepareQuery(columnFamily).getKey(rowKey)
                 .withColumnRange(getRangeBuilder(start, end, (Serializer<C>)serializerFor(colClass))).execute();
         ColumnList<C> columns = (ColumnList<C>) result.getResult();
         HashMap<C, V> retval = new HashMap<C, V>();
         Iterator<Column<C>> it = columns.iterator();
         while(it.hasNext()){
             Column<C> col= it.next();
             retval.put(col.getName(), col.getValue((Serializer<V>)serializerFor(tupleMapper.getColumnValueClass())));
         }
         return retval;        
     }
 
     
     public Map<C, V> lookup(TridentTupleMapper<K, C, V> tupleMapper, TridentTuple input, C start, C end) throws Exception {
         if (start == null || end == null) {
             return null;
         }
         
         String cf = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         Class<K> keyClass = tupleMapper.getKeyClass();
         Class<C> colClass = tupleMapper.getColumnNameClass();
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(cf, (Serializer<K>)serializerFor(keyClass),
                 (Serializer<C>)serializerFor(colClass));
         OperationResult<ColumnList<C>> result= this.keyspace.prepareQuery(columnFamily).getKey(rowKey)
                 .withColumnRange(getRangeBuilder(start, end, (Serializer<C>)serializerFor(colClass))).execute();
         ColumnList<C> columns = (ColumnList<C>) result.getResult();
         HashMap<C, V> retval = new HashMap<C, V>();
         Iterator<Column<C>> it = columns.iterator();
         while(it.hasNext()){
             Column<C> col= it.next();
             retval.put(col.getName(), col.getValue((Serializer<V>)serializerFor(tupleMapper.getColumnValueClass())));
         }
         return retval;        
     }
 
     
     
     public void writeTuple(Tuple input, TupleMapper<K, C, V> tupleMapper) throws Exception {
         String columnFamilyName = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         MutationBatch mutation = keyspace.prepareMutationBatch();
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(columnFamilyName, (Serializer<K>)serializerFor(tupleMapper.getKeyClass()),
                 (Serializer<C>)serializerFor(tupleMapper.getColumnNameClass()));
         this.addTupleToMutation(input, columnFamily, rowKey, mutation, tupleMapper);
         mutation.execute();
     }
 
     public void writeTuple(TridentTuple input, TridentTupleMapper<K, C, V> tupleMapper) throws Exception {
         String columnFamilyName = tupleMapper.mapToColumnFamily(input);
         K rowKey = tupleMapper.mapToRowKey(input);
         MutationBatch mutation = keyspace.prepareMutationBatch();
         ColumnFamily<K, C> columnFamily = new ColumnFamily<K, C>(columnFamilyName, (Serializer<K>)serializerFor(tupleMapper.getKeyClass()),
                 (Serializer<C>)serializerFor(tupleMapper.getColumnNameClass()));
         this.addTupleToMutation(input, columnFamily, rowKey, mutation, tupleMapper);
         mutation.execute();
     }
 
     public void writeTuples(List<Tuple> inputs, TupleMapper<K, C, V> tupleMapper) throws Exception {
         MutationBatch mutation = keyspace.prepareMutationBatch();
         for (Tuple input : inputs) {
             String columnFamilyName = tupleMapper.mapToColumnFamily(input);
             K rowKey = tupleMapper.mapToRowKey(input);
             ColumnFamily<K, C> columnFamily = new ColumnFamily<K,C>(columnFamilyName,
                     (Serializer<K>)serializerFor(tupleMapper.getKeyClass()), (Serializer<C>)this.serializerFor(tupleMapper.getColumnNameClass()));
             this.addTupleToMutation(input, columnFamily, rowKey, mutation, tupleMapper);
         }
         mutation.execute();
     }
 
     private void addTupleToMutation(Tuple input, ColumnFamily<K, C> columnFamily, K rowKey,
             MutationBatch mutation, TupleMapper<K, C, V> tupleMapper) {
         Map<C, V> columns = tupleMapper.mapToColumns(input);
         for (Map.Entry<C, V> entry : columns.entrySet()) {
             mutation.withRow(columnFamily, rowKey).putColumn(entry.getKey(), entry.getValue(),
                     (Serializer<V>)this.serializerFor(tupleMapper.getColumnValueClass()), null);
         }
     }
 
     private void addTupleToMutation(TridentTuple input, ColumnFamily<K, C> columnFamily, K rowKey,
             MutationBatch mutation, TridentTupleMapper<K, C, V> tupleMapper) {
         Map<C, V> columns = tupleMapper.mapToColumns(input);
         if (tupleMapper.shouldDelete(input)) {
             for (Map.Entry<C, V> entry : columns.entrySet()) {
                 mutation.withRow(columnFamily, rowKey).deleteColumn(entry.getKey());
             }
         } else {
             for (Map.Entry<C, V> entry : columns.entrySet()) {
                 mutation.withRow(columnFamily, rowKey).putColumn(entry.getKey(), entry.getValue(),
                        serializerFor(tupleMapper.getColumnValueClass()), null);
             }
         }
     }
 
     public void incrementCountColumn(Tuple input, TupleCounterMapper tupleMapper) throws Exception {
         String columnFamilyName = tupleMapper.mapToColumnFamily(input);
         String rowKey = (String) tupleMapper.mapToRowKey(input);
         long incrementAmount = tupleMapper.mapToIncrementAmount(input);
         MutationBatch mutation = keyspace.prepareMutationBatch();
         ColumnFamily<String, String> columnFamily = new ColumnFamily<String, String>(columnFamilyName,
                 StringSerializer.get(), StringSerializer.get());
         for (String columnName : tupleMapper.mapToColumnList(input)) {
             mutation.withRow(columnFamily, rowKey).incrementCounterColumn(columnName, incrementAmount);
         }
         mutation.execute();
     }
 
     public void incrementCountColumns(List<Tuple> inputs, TupleCounterMapper tupleMapper) throws Exception {
         MutationBatch mutation = keyspace.prepareMutationBatch();
         for (Tuple input : inputs) {
             String columnFamilyName = tupleMapper.mapToColumnFamily(input);
             String rowKey = (String) tupleMapper.mapToRowKey(input);
             long incrementAmount = tupleMapper.mapToIncrementAmount(input);
             ColumnFamily<String, String> columnFamily = new ColumnFamily<String, String>(columnFamilyName,
                     StringSerializer.get(), StringSerializer.get());
             for (String columnName : tupleMapper.mapToColumnList(input)) {
                 mutation.withRow(columnFamily, rowKey).incrementCounterColumn(columnName, incrementAmount);
             }
         }
         mutation.execute();
     }
 
 //    private Serializer<K> getColumnNameSerializer() {
 //            return getSerializer(this.getColumnNameClass());
 //    }
 //
 //    private Serializer<V> getColumnValueSerializer() {
 //            return serializerFor(this.getColumnValueClass());
 //    }
 
     private ByteBufferRange getRangeBuilder(C start, C end, Serializer<C> serializer) {
         if (!(serializer instanceof AnnotatedCompositeSerializer)) {
             return new RangeBuilder().setStart(start, serializerFor(start.getClass())).setEnd(end, serializerFor(end.getClass())).build();
         } else {
             return ((AnnotatedCompositeSerializer<C>) serializer).buildRange().greaterThanEquals(start)
                     .lessThanEquals(end).build();
         }
     }
 
     private static boolean containsComponentAnnotation(Class<?> c) {
         List<Field> fields = getInheritedFields(c);
         for (Field field : fields) {
             if (field.getAnnotation(Component.class) != null) {
                 return true;
             }
         }
         return false;
     }
 
     private static List<Field> getInheritedFields(Class<?> type) {
         List<Field> result = new ArrayList<Field>();
 
         Class<?> i = type;
         while (i != null && i != Object.class) {
             for (Field field : i.getDeclaredFields()) {
                 if (!field.isSynthetic()) {
                     result.add(field);
                 }
             }
             i = i.getSuperclass();
         }
 
         return result;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private static <T> Serializer<T> serializerFor(Class<?> valueClass) {
         Serializer serializer = null;
         if (valueClass.equals(UUID.class)) {
             serializer = UUIDSerializer.get();
         } else if (valueClass.equals(String.class)) {
             serializer = StringSerializer.get();
         } else if (valueClass.equals(Long.class) || valueClass.equals(long.class)) {
             serializer = LongSerializer.get();
         } else if (valueClass.equals(Integer.class) || valueClass.equals(int.class)) {
             serializer = Int32Serializer.get();
         } else if (valueClass.equals(Short.class) || valueClass.equals(short.class)) {
             serializer = ShortSerializer.get();
         } else if (valueClass.equals(Byte.class) || valueClass.equals(byte.class)) {
             serializer = ByteSerializer.get();
         } else if (valueClass.equals(Float.class) || valueClass.equals(float.class)) {
             serializer = FloatSerializer.get();
         } else if (valueClass.equals(Double.class) || valueClass.equals(double.class)) {
             serializer = DoubleSerializer.get();
         } else if (valueClass.equals(BigInteger.class)) {
             serializer = BigIntegerSerializer.get();
 //        } else if (valueClass.equals(BigDecimal.class)) {
 //            serializer = BigDecimalSerializer.get();
         } else if (valueClass.equals(Boolean.class) || valueClass.equals(boolean.class)) {
             serializer = BooleanSerializer.get();
         } else if (valueClass.equals(byte[].class)) {
             serializer = BytesArraySerializer.get();
         } else if (valueClass.equals(ByteBuffer.class)) {
             serializer = ByteBufferSerializer.get();
         } else if (valueClass.equals(Date.class)) {
             serializer = DateSerializer.get();
         }
         if (serializer == null) {
             if(containsComponentAnnotation(valueClass)){
                 serializer = new AnnotatedCompositeSerializer(valueClass);
             } else{
                 serializer = ObjectSerializer.get();
             }
         }
         return serializer;
     }
 
 }
