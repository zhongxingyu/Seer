 package com.slandir.identity.dao;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.proofpoint.json.JsonCodec;
 import com.slandir.identity.model.Person;
 import com.slandir.identity.type.State;
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
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.ddl.ColumnDefinition;
 import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
 import me.prettyprint.hector.api.ddl.ColumnIndexType;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.SliceQuery;
 import org.apache.commons.lang.StringUtils;
 
 import javax.annotation.Nullable;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import static com.google.common.collect.Iterables.find;
 
 public class CassandraPersonDao implements  PersonDao {
 
     private static final String KEY_SPACE = "identity";
     private static final String COLUMN_FAMILY = "person";
    private static final String COLUMN_NAME = "representation";
 
     private static final String FIRST_NAME_COLUMN = "first_name";
     private static final String MIDDLE_NAME_COLUMN = "middle_name";
     private static final String LAST_NAME_COLUMN = "last_name";
     private static final String STATE_COLUMN = "state";
 
     private static final JsonCodec<Person> personCodec = JsonCodec.jsonCodec(Person.class);
 
     private final Keyspace keyspace;
     private final ThriftColumnFamilyTemplate<UUID, String> template;
 
     @Inject
     public CassandraPersonDao(Cluster cluster) {
 
         KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(KEY_SPACE);
         if(keyspaceDefinition == null) {
             cluster.addKeyspace(new ThriftKsDef(KEY_SPACE));
         }
 
         ColumnFamilyDefinition columnFamilyDefinition = find(cluster.describeKeyspace(KEY_SPACE).getCfDefs(), named(COLUMN_FAMILY), null);
         if(columnFamilyDefinition == null) {
 
             //Create secondary index on first_name
             BasicColumnDefinition firstNameColumn = new BasicColumnDefinition();
             firstNameColumn.setName(StringSerializer.get().toByteBuffer(FIRST_NAME_COLUMN));
             firstNameColumn.setIndexName(String.format("%s_idx", FIRST_NAME_COLUMN));
             firstNameColumn.setIndexType(ColumnIndexType.KEYS);
             firstNameColumn.setValidationClass(ComparatorType.ASCIITYPE.getClassName());
 
             //Create secondary index on middle_name
             BasicColumnDefinition middleNameColumn = new BasicColumnDefinition();
             middleNameColumn.setName(StringSerializer.get().toByteBuffer(MIDDLE_NAME_COLUMN));
             middleNameColumn.setIndexName(String.format("%s_idx", MIDDLE_NAME_COLUMN));
             middleNameColumn.setIndexType(ColumnIndexType.KEYS);
             middleNameColumn.setValidationClass(ComparatorType.ASCIITYPE.getClassName());
 
             //Create secondary index on last_name
             BasicColumnDefinition lastNameColumn = new BasicColumnDefinition();
             lastNameColumn.setName(StringSerializer.get().toByteBuffer(LAST_NAME_COLUMN));
             lastNameColumn.setIndexName(String.format("%s_idx", LAST_NAME_COLUMN));
             lastNameColumn.setIndexType(ColumnIndexType.KEYS);
             lastNameColumn.setValidationClass(ComparatorType.ASCIITYPE.getClassName());
 
             cluster.addColumnFamily(new ThriftCfDef(KEY_SPACE, COLUMN_FAMILY, ComparatorType.ASCIITYPE, Lists.newArrayList((ColumnDefinition)firstNameColumn, middleNameColumn, lastNameColumn)));
         }
 
         keyspace = HFactory.createKeyspace(KEY_SPACE, cluster);
         keyspace.setConsistencyLevelPolicy(new QuorumAllConsistencyLevelPolicy());
 
         template = new ThriftColumnFamilyTemplate<UUID, String>(keyspace, COLUMN_FAMILY, UUIDSerializer.get(), StringSerializer.get());
     }
 
     @Override
     public Person get(UUID id) {
         SliceQuery<UUID, String, String> result = HFactory.createSliceQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), StringSerializer.get());
         result.setColumnFamily(COLUMN_FAMILY);
         result.setKey(id);
         result.setColumnNames(COLUMN_NAME);
         QueryResult<ColumnSlice<String, String>> columnSlice = result.execute();
         return personCodec.fromJson(columnSlice.get().getColumnByName(COLUMN_NAME).getValue());
     }
 
     @Override
     public List<Person> fetch(String firstName, String middleName, String lastName, State state) {
         IndexedSlicesQuery<UUID, String, ByteBuffer> indexedSlicesQuery = HFactory.createIndexedSlicesQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), ByteBufferSerializer.get());
         if(!StringUtils.isBlank(firstName)) {
             indexedSlicesQuery.addEqualsExpression(FIRST_NAME_COLUMN, StringSerializer.get().toByteBuffer(firstName.toLowerCase()));
         }
         if(!StringUtils.isBlank(middleName)) {
             indexedSlicesQuery.addEqualsExpression(MIDDLE_NAME_COLUMN, StringSerializer.get().toByteBuffer(middleName.toLowerCase()));
         }
         if(!StringUtils.isBlank(lastName)) {
             indexedSlicesQuery.addEqualsExpression(LAST_NAME_COLUMN, StringSerializer.get().toByteBuffer(lastName.toLowerCase()));
         }
         if(state != null) {
             indexedSlicesQuery.addEqualsExpression(STATE_COLUMN, StringSerializer.get().toByteBuffer(state.toString()));
         }
         indexedSlicesQuery.setColumnFamily(COLUMN_FAMILY);
         indexedSlicesQuery.setColumnNames(COLUMN_NAME);
 
         List<Row<UUID, String, ByteBuffer>> results = indexedSlicesQuery.execute().get().getList();
         
         if(results.isEmpty()) {
             return Collections.emptyList();
         }
         
         return Lists.transform(results, new Function<Row<UUID, String, ByteBuffer>, Person>() {
             @Override
             public Person apply(@Nullable Row<UUID, String, ByteBuffer> row) {
                 return personCodec.fromJson(StringSerializer.get().fromByteBuffer(row.getColumnSlice().getColumnByName(COLUMN_NAME).getValue()));
             }
         });
     }
 
     @Override
     public void save(Person person) {
         ColumnFamilyUpdater<UUID, String> columnFamilyUpdater = template.createUpdater(person.getId());
         if(!StringUtils.isBlank(person.getFirstName())) {
             columnFamilyUpdater.setString(FIRST_NAME_COLUMN, person.getFirstName().toLowerCase());
         }
         if(!StringUtils.isBlank(person.getMiddleName())) {
             columnFamilyUpdater.setString(MIDDLE_NAME_COLUMN, person.getMiddleName().toLowerCase());
         }
         if(!StringUtils.isBlank(person.getLastName())) {
             columnFamilyUpdater.setString(LAST_NAME_COLUMN, person.getLastName().toLowerCase());
         }
         if(person.getAddress() != null && person.getAddress().getState() != null) {
             columnFamilyUpdater.setString(STATE_COLUMN, person.getAddress().getState());
         }
         columnFamilyUpdater.setString(COLUMN_NAME, personCodec.toJson(person));
         template.update(columnFamilyUpdater);
     }
 
     private static Predicate<ColumnFamilyDefinition> named(final String name) {
         return new Predicate<ColumnFamilyDefinition>() {
             public boolean apply(ColumnFamilyDefinition input) {
                 return input.getName().equals(name);
             }
         };
     }
 
 }
