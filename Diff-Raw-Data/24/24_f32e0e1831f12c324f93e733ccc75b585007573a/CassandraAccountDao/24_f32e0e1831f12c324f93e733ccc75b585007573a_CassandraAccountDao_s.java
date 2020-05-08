 package com.slandir.account.dao;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.proofpoint.json.JsonCodec;
 import com.slandir.account.model.Account;
 import me.prettyprint.cassandra.model.BasicColumnDefinition;
 import me.prettyprint.cassandra.model.IndexedSlicesQuery;
 import me.prettyprint.cassandra.model.QuorumAllConsistencyLevelPolicy;
 import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.serializers.UUIDSerializer;
 import me.prettyprint.cassandra.service.ThriftCfDef;
 import me.prettyprint.cassandra.service.ThriftKsDef;
 import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
 import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.ddl.ColumnDefinition;
 import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
 import me.prettyprint.hector.api.ddl.ColumnIndexType;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
 import me.prettyprint.hector.api.factory.HFactory;
 
 import java.nio.ByteBuffer;
 import java.util.List;
 import java.util.UUID;
 
 import static com.google.common.collect.Iterables.find;
 
 public class CassandraAccountDao implements AccountDao {
 
     private static final String KEY_SPACE = "account";
     private static final String COLUMN_FAMILY = "account";
    private static final String COLUMN_NAME = "data";
 
     private static final String EMAIL_COLUMN = "email";
     
     private static final JsonCodec<Account> accountCodec = JsonCodec.jsonCodec(Account.class);
 
     private final Keyspace keyspace;
     private final ThriftColumnFamilyTemplate<UUID, String> template;
 
     @Inject
     public CassandraAccountDao(Cluster cluster) {
 
         KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(KEY_SPACE);
         if(keyspaceDefinition == null) {
             cluster.addKeyspace(new ThriftKsDef(KEY_SPACE));
         }
 
         ColumnFamilyDefinition columnFamilyDefinition = find(cluster.describeKeyspace(KEY_SPACE).getCfDefs(), named(COLUMN_FAMILY), null);
         if(columnFamilyDefinition == null) {
 
             //Create secondary index on email
             BasicColumnDefinition emailColumn = new BasicColumnDefinition();
             emailColumn.setName(StringSerializer.get().toByteBuffer(EMAIL_COLUMN));
             emailColumn.setIndexName(String.format("%s_idx", EMAIL_COLUMN));
             emailColumn.setIndexType(ColumnIndexType.KEYS);
             emailColumn.setValidationClass(ComparatorType.ASCIITYPE.getClassName());
 
             cluster.addColumnFamily(new ThriftCfDef(KEY_SPACE, COLUMN_FAMILY, ComparatorType.ASCIITYPE, Lists.newArrayList((ColumnDefinition)emailColumn)));
         }
 
         keyspace = HFactory.createKeyspace(KEY_SPACE, cluster);
         keyspace.setConsistencyLevelPolicy(new QuorumAllConsistencyLevelPolicy());
 
         template = new ThriftColumnFamilyTemplate<UUID, String>(keyspace, COLUMN_FAMILY, UUIDSerializer.get(), StringSerializer.get());
     }
     
     @Override
     public void save(Account account) {
         ColumnFamilyUpdater<UUID, String> columnFamilyUpdater = template.createUpdater(account.getId());
         columnFamilyUpdater.setString(EMAIL_COLUMN, account.getEmail());
         columnFamilyUpdater.setString(COLUMN_NAME, accountCodec.toJson(account));
         template.update(columnFamilyUpdater);
     }
 
     @Override
     public Account fetch(String email) {
         IndexedSlicesQuery<UUID, String, ByteBuffer> indexedSlicesQuery = HFactory.createIndexedSlicesQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), ByteBufferSerializer.get());
         indexedSlicesQuery.addEqualsExpression(EMAIL_COLUMN, StringSerializer.get().toByteBuffer(email));
         indexedSlicesQuery.setColumnFamily(COLUMN_FAMILY);
         indexedSlicesQuery.setColumnNames(COLUMN_NAME);
 
         List<Row<UUID, String, ByteBuffer>> results = indexedSlicesQuery.execute().get().getList();
 
         if(results.size() == 0) {
             return null;
         }
 
         return accountCodec.fromJson(StringSerializer.get().fromByteBuffer(results.get(0).getColumnSlice().getColumnByName(COLUMN_NAME).getValue()));
     }
     
     private static Predicate<ColumnFamilyDefinition> named(final String name) {
         return new Predicate<ColumnFamilyDefinition>() {
             public boolean apply(ColumnFamilyDefinition input) {
                 return input.getName().equals(name);
             }
         };
     }
 }
