 package com.slandir.submission.dao;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.proofpoint.json.JsonCodec;
 import com.slandir.submission.model.Grievance;
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
import me.prettyprint.hector.api.mutation.Mutator;
 
 import javax.annotation.Nullable;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import static com.google.common.collect.Iterables.find;
 
 public class CassandraGrievanceDao implements GrievanceDao {
 
     private static final String KEY_SPACE = "submission";
     private static final String COLUMN_FAMILY = "grievance";
    private static final String COLUMN_NAME = "representation";
 
     private static final String ACCOUNT_ID_COLUMN = "account_id";
     private static final String PERSON_ID_COLUMN = "person_id";
 
     private static final JsonCodec<Grievance> grievanceCodec = JsonCodec.jsonCodec(Grievance.class);
     
     private final Keyspace keyspace;
     private final ThriftColumnFamilyTemplate<UUID, String> template;
 
     @Inject
     public CassandraGrievanceDao(Cluster cluster) {
 
         KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(KEY_SPACE);
         if(keyspaceDefinition == null) {
             cluster.addKeyspace(new ThriftKsDef(KEY_SPACE));
         }
 
         ColumnFamilyDefinition columnFamilyDefinition = find(cluster.describeKeyspace(KEY_SPACE).getCfDefs(), named(COLUMN_FAMILY), null);
         if(columnFamilyDefinition == null) {
 
             //Create secondary index on account_id
             BasicColumnDefinition accountIdColumn = new BasicColumnDefinition();
             accountIdColumn.setName(StringSerializer.get().toByteBuffer(ACCOUNT_ID_COLUMN));
             accountIdColumn.setIndexName(String.format("%s_idx", ACCOUNT_ID_COLUMN));
             accountIdColumn.setIndexType(ColumnIndexType.KEYS);
             accountIdColumn.setValidationClass(ComparatorType.UUIDTYPE.getClassName());
 
             //Create secondary index on person_id
             BasicColumnDefinition personIdColumn = new BasicColumnDefinition();
             personIdColumn.setName(StringSerializer.get().toByteBuffer(PERSON_ID_COLUMN));
             personIdColumn.setIndexName(String.format("%s_idx", PERSON_ID_COLUMN));
             personIdColumn.setIndexType(ColumnIndexType.KEYS);
             personIdColumn.setValidationClass(ComparatorType.UUIDTYPE.getClassName());
 
             cluster.addColumnFamily(new ThriftCfDef(KEY_SPACE, COLUMN_FAMILY, ComparatorType.ASCIITYPE, Lists.newArrayList((ColumnDefinition)accountIdColumn, personIdColumn)));
         }
 
         keyspace = HFactory.createKeyspace(KEY_SPACE, cluster);
         keyspace.setConsistencyLevelPolicy(new QuorumAllConsistencyLevelPolicy());
         
         template = new ThriftColumnFamilyTemplate<UUID, String>(keyspace, COLUMN_FAMILY, UUIDSerializer.get(), StringSerializer.get());
     }
 
     @Override
     public List<Grievance> fetchByPerson(UUID personId) {
         IndexedSlicesQuery<UUID, String, ByteBuffer> indexedSlicesQuery = HFactory.createIndexedSlicesQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), ByteBufferSerializer.get());
         indexedSlicesQuery.addEqualsExpression(PERSON_ID_COLUMN, UUIDSerializer.get().toByteBuffer(personId));
         indexedSlicesQuery.setColumnFamily(COLUMN_FAMILY);
         indexedSlicesQuery.setColumnNames(COLUMN_NAME);
 
         List<Row<UUID, String, ByteBuffer>> results = indexedSlicesQuery.execute().get().getList();
         return Lists.transform(results, new Function<Row<UUID, String, ByteBuffer>, Grievance>() {
             @Override
             public Grievance apply(@Nullable Row<UUID, String, ByteBuffer> row) {
                 return grievanceCodec.fromJson(StringSerializer.get().fromByteBuffer(row.getColumnSlice().getColumnByName(COLUMN_NAME).getValue()));
             }
         });
     }
 
     @Override
     public List<Grievance> fetchByAccount(UUID accountId) {
         IndexedSlicesQuery<UUID, String, ByteBuffer> indexedSlicesQuery = HFactory.createIndexedSlicesQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), ByteBufferSerializer.get());
         indexedSlicesQuery.addEqualsExpression(ACCOUNT_ID_COLUMN, UUIDSerializer.get().toByteBuffer(accountId));
         indexedSlicesQuery.setColumnFamily(COLUMN_FAMILY);
         indexedSlicesQuery.setColumnNames(COLUMN_NAME);
 
         List<Row<UUID, String, ByteBuffer>> results = indexedSlicesQuery.execute().get().getList();
         
         if(results.isEmpty()) {
             return Collections.emptyList();
         }
         
         return Lists.transform(results, new Function<Row<UUID, String, ByteBuffer>, Grievance>() {
             @Override
             public Grievance apply(@Nullable Row<UUID, String, ByteBuffer> row) {
                 return grievanceCodec.fromJson(StringSerializer.get().fromByteBuffer(row.getColumnSlice().getColumnByName(COLUMN_NAME).getValue()));
             }
         });
     }
 
     @Override
     public void save(Grievance grievance) {
         ColumnFamilyUpdater<UUID, String> columnFamilyUpdater = template.createUpdater(grievance.getId());
         columnFamilyUpdater.setUUID(PERSON_ID_COLUMN, grievance.getPersonId());
         columnFamilyUpdater.setUUID(ACCOUNT_ID_COLUMN, grievance.getAccountId());
         columnFamilyUpdater.setString(COLUMN_NAME, grievanceCodec.toJson(grievance));
         template.update(columnFamilyUpdater);
     }
 
     @Override
     public void remove(UUID grievanceId) {
        Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
        mutator.delete(grievanceId, template.getColumnFamily(), null, null);
     }
 
     private static Predicate<ColumnFamilyDefinition> named(final String name) {
         return new Predicate<ColumnFamilyDefinition>() {
             public boolean apply(ColumnFamilyDefinition input) {
                 return input.getName().equals(name);
             }
         };
     }
 }
