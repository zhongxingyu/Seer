 package com.nearinfinity.honeycomb.mysql;
 
 import com.google.common.collect.*;
 import com.nearinfinity.honeycomb.Scanner;
 import com.nearinfinity.honeycomb.Store;
 import com.nearinfinity.honeycomb.Table;
 import com.nearinfinity.honeycomb.mysql.gen.IndexSchema;
 import com.nearinfinity.honeycomb.mysql.gen.QueryType;
 import com.nearinfinity.honeycomb.mysql.gen.TableSchema;
 import org.apache.log4j.Logger;
 
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.*;
 import static java.lang.String.format;
 
 public class HandlerProxy {
     private static final Logger logger = Logger.getLogger(HandlerProxy.class);
     private final StoreFactory storeFactory;
     private Store store;
     private Table table;
     private String tableName;
     private Scanner currentScanner;
 
     public HandlerProxy(StoreFactory storeFactory) {
         this.storeFactory = storeFactory;
     }
 
     /**
      * Create a table with the given specifications.  The table is not open when
      * this is called.
      *
      * @param tableName             Name of the table
      * @param tableSpace            Indicates what store to create the table in.
      *                              If null, create the table in the default store.
      * @param serializedTableSchema Serialized TableSchema avro object
      * @param autoInc               Initial auto increment value
      */
     public void createTable(String tableName, String tableSpace,
                             byte[] serializedTableSchema, long autoInc) {
         Verify.isNotNullOrEmpty(tableName);
         checkNotNull(serializedTableSchema);
 
         store = storeFactory.createStore(tableSpace);
         TableSchema tableSchema = Util.deserializeTableSchema(serializedTableSchema);
         Verify.isValidTableSchema(tableSchema);
         store.createTable(tableName, tableSchema);
         store.incrementAutoInc(tableName, autoInc);
     }
 
     /**
      * Drop the table with the given specifications.  The table is not open when
      * this is called.
      *
      * @param tableName  Name of the table to be dropped
      * @param tableSpace What store to drop table from.  If null, use default.
      */
     public void dropTable(String tableName, String tableSpace) {
         Verify.isNotNullOrEmpty(tableName);
         Store store = storeFactory.createStore(tableSpace);
         Table table;
         table = store.openTable(tableName);
 
         table.deleteAllRows();
         Util.closeQuietly(table);
         store.deleteTable(tableName);
     }
 
     public void openTable(String tableName, String tableSpace) {
         Verify.isNotNullOrEmpty(tableName);
         this.tableName = tableName;
         store = storeFactory.createStore(tableSpace);
         table = store.openTable(this.tableName);
     }
 
     public void closeTable() {
         tableName = null;
         store = null;
         Util.closeQuietly(table);
         table = null;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     /**
      * Updates the existing SQL table name representation in the underlying
      * {@link Store} implementation to the specified new table name.  The table
      * is not open when this is called.
      *
      * @param originalName The existing name of the table, not null or empty
      * @param tableSpace   The store which contains the table
      * @param newName      The new table name to represent, not null or empty
      */
     public void renameTable(final String originalName, final String tableSpace,
                             final String newName) {
         Verify.isNotNullOrEmpty(originalName, "Original table name must have value.");
         Verify.isNotNullOrEmpty(newName, "New table name must have value.");
         checkArgument(!originalName.equals(newName), "New table name must be different than original.");
 
         Store store = storeFactory.createStore(tableSpace);
         store.renameTable(originalName, newName);
         tableName = newName;
     }
 
     public long getRowCount() {
         checkTableOpen();
 
         return store.getRowCount(tableName);
     }
 
     public void incrementRowCount(long amount) {
         checkTableOpen();
 
         store.incrementRowCount(tableName, amount);
     }
 
     public void truncateRowCount() {
         checkTableOpen();
         store.truncateRowCount(tableName);
     }
 
     public long getAutoIncrement() {
         checkTableOpen();
         if (!Verify.hasAutoIncrementColumn(store.getSchema(tableName))) {
             throw new IllegalArgumentException(format("Table %s does not" +
                     " contain an auto increment column.", tableName));
         }
 
         return store.getAutoInc(tableName);
     }
 
     /**
      * Set the auto increment value of the table to the max of value and the
      * current value.
      *
      * @param value
      * @return
      */
     public void setAutoIncrement(long value) {
         checkTableOpen();
         store.setAutoInc(tableName, value);
     }
 
     /**
      * Increment the auto increment value of the table by amount, and return the
      * next auto increment value.  The next value will be the current value,
      * not the incremented value (equivalently, the incremented value - amount).
      *
      * @param amount
      * @return
      */
     public long incrementAutoIncrement(long amount) {
         checkTableOpen();
         if (!Verify.hasAutoIncrementColumn(store.getSchema(tableName))) {
             throw new IllegalArgumentException(format("Table %s does not contain an auto increment column.", tableName));
         }
 
         return store.incrementAutoInc(getTableName(), amount) - amount;
     }
 
     public void truncateAutoIncrement() {
         checkTableOpen();
         store.truncateAutoInc(tableName);
     }
 
     /**
      * Add the provided index information to the table.  The table must be open
      * before this operation can be performed.
      *
      * @param indexName        The name of the index to add, not null or empty
      * @param serializedSchema The byte representation of the {@link IndexSchema} for this index, not null
      */
     public void addIndex(String indexName, byte[] serializedSchema) {
         Verify.isNotNullOrEmpty(indexName, "The index name is invalid");
         checkNotNull(serializedSchema);
         checkTableOpen();
 
         IndexSchema schema = Util.deserializeIndexSchema(serializedSchema);
         checkArgument(!schema.getIsUnique(), "Honeycomb does not support adding unique indices without a table rebuild.");
 
         store.addIndex(tableName, indexName, schema);
         table.insertTableIndex(indexName, schema);
     }
 
     /**
      * Drop the index specified by the index name from the table. The table must be open
      * before this operation can be performed.
      *
      * @param indexName The name of the index to add, not null or empty
      */
     public void dropIndex(String indexName) {
         Verify.isNotNullOrEmpty(indexName, "The index name is invalid");
         checkTableOpen();
 
         TableSchema tableSchema = store.getSchema(tableName);
         IndexSchema indexSchema = tableSchema.getIndices().get(indexName);
         table.deleteTableIndex(indexName, indexSchema);
         store.dropIndex(tableName, indexName);
     }
 
     /**
      * Check whether the index contains a row with the same field values and a
      * distinct UUID.
      *
      * @param indexName
      * @param serializedRow
      */
     public boolean indexContainsDuplicate(String indexName, byte[] serializedRow, byte[] originalRowBytes) {
         // This method must get its own table because it may be called during
         // a full table scan.
         checkNotNull(indexName);
         checkNotNull(serializedRow);
 
         Row row = Row.deserialize(serializedRow);
         Map<String, ByteBuffer> differenceMap;
         if (originalRowBytes != null) {
             Row originalRow = Row.deserialize(originalRowBytes);
             differenceMap = computeUpdateDifference(originalRow, row);
         } else {
             differenceMap = row.getRecords();
         }
 
         Table t = store.openTable(tableName);
         IndexKey key = new IndexKey(indexName, null, differenceMap);
         Scanner scanner = t.indexScanExact(key);
         Set<String> columns = Sets.newHashSet(store.getSchema(tableName).getIndices().get(indexName).getColumns());
 
         try {
             while (scanner.hasNext()) {
                 Row next = scanner.next();
                boolean indexDuplicate = row.isDuplicateForIndex(next, columns);
                if (indexDuplicate) {
                     return true;
                 }
             }
             return false;
         } finally {
             Util.closeQuietly(scanner);
             Util.closeQuietly(t);
         }
     }
 
     /**
      * Insert row into table.
      *
      * @param rowBytes Serialized row to be written
      * @return true if the write succeeds, or false if a uniqueness constraint
      *         is violated.
      */
     public void insertRow(byte[] rowBytes) {
         checkTableOpen();
         checkNotNull(rowBytes);
         TableSchema schema = store.getSchema(tableName);
         Row row = Row.deserialize(rowBytes);
         row.setRandomUUID();
         String auto_inc_col = Util.getAutoIncrementColumn(schema);
         if (auto_inc_col != null) {
             ByteBuffer bb = row.getRecords().get(auto_inc_col);
             long auto_inc = bb.getLong();
            long next_auto_inc = auto_inc + 1;
            if (auto_inc > next_auto_inc) { // The autoincrement will wrap around. MySQL says don't wrap.
                next_auto_inc = auto_inc;
            }

             bb.rewind();
            store.setAutoInc(tableName, next_auto_inc);
         }
         table.insert(row);
     }
 
     public void deleteRow(byte[] uuidBytes) {
         checkTableOpen();
         table.delete(Util.bytesToUUID(uuidBytes));
     }
 
     public void updateRow(byte[] rowBytes) {
         checkTableOpen();
         checkNotNull(rowBytes);
         Row updatedRow = Row.deserialize(rowBytes);
         TableSchema schema = store.getSchema(tableName);
         Row oldRow = table.get(updatedRow.getUUID());
         if (schema.getIndices().isEmpty()) {
             table.update(oldRow, updatedRow, ImmutableMap.<String, IndexSchema>of());
         }
 
         MapDifference<String, ByteBuffer> diff = Maps.difference(oldRow.getRecords(),
                 updatedRow.getRecords());
 
         Set<String> changedColumns = Sets.union(
                 diff.entriesDiffering().keySet(),
                 diff.entriesOnlyOnRight().keySet());
 
         ImmutableMap.Builder<String, IndexSchema> changedIndices = ImmutableMap.builder();
 
         for (Map.Entry<String, IndexSchema> index : schema.getIndices().entrySet()) {
             Set<String> indexColumns = ImmutableSet.copyOf(index.getValue().getColumns());
             if (!Sets.intersection(changedColumns, indexColumns).isEmpty()) {
                 changedIndices.put(index);
             }
         }
 
         table.update(oldRow, updatedRow, changedIndices.build());
     }
 
     /**
      * Delete all rows in the table.
      */
     public void deleteAllRows() {
         checkTableOpen();
         store.truncateRowCount(tableName);
         table.deleteAllRows();
     }
 
     /**
      * Delete all rows in the table, and reset the auto increment value.
      */
     public void truncateTable() {
         checkTableOpen();
         deleteAllRows();
         store.truncateAutoInc(tableName);
     }
 
     public void flush() {
         // MySQL will call flush on the handler without an open table, which is
         // a no-op
         if (table != null) {
             table.flush();
         }
     }
 
     public void startTableScan() {
         checkTableOpen();
         if (currentScanner != null) {
             endScan();
         }
 
         currentScanner = table.tableScan();
     }
 
     public void startIndexScan(byte[] indexKeys) {
         checkTableOpen();
         if (currentScanner != null) {
             endScan();
         }
         checkNotNull(indexKeys, "Index scan requires non-null key");
 
         IndexKey key = IndexKey.deserialize(indexKeys);
         QueryType queryType = key.getQueryType();
         switch (queryType) {
             case EXACT_KEY:
                 currentScanner = table.indexScanExact(key);
                 break;
             case AFTER_KEY:
                 currentScanner = table.ascendingIndexScanAfter(key);
                 break;
             case BEFORE_KEY:
                 currentScanner = table.descendingIndexScanAfter(key);
                 break;
             case INDEX_FIRST:
                 currentScanner = table.ascendingIndexScanAt(key);
                 break;
             case INDEX_LAST:
                 currentScanner = table.descendingIndexScanAt(key);
                 break;
             case KEY_OR_NEXT:
                 currentScanner = table.ascendingIndexScanAt(key);
                 break;
             case KEY_OR_PREVIOUS:
                 currentScanner = table.descendingIndexScanAt(key);
                 break;
             default:
                 throw new IllegalArgumentException(format("Not a supported type of query %s", queryType));
         }
 
     }
 
     public byte[] getNextRow() {
         checkNotNull(currentScanner, "Scanner cannot be null to get next row.");
         if (!currentScanner.hasNext()) {
             return null;
         }
 
         return currentScanner.next().serialize();
     }
 
     public byte[] getRow(byte[] uuid) {
         checkTableOpen();
         checkNotNull(uuid, "Get row cannot have a null UUID.");
         return table.get(Util.bytesToUUID(uuid)).serialize();
     }
 
     public void endScan() {
         Util.closeQuietly(currentScanner);
         currentScanner = null;
     }
 
     private Map<String, ByteBuffer> computeUpdateDifference(Row originalRow, Row row) {
         MapDifference<String, ByteBuffer> difference = Maps.difference(originalRow.getRecords(), row.getRecords());
         Map<String, MapDifference.ValueDifference<ByteBuffer>> differenceMap = difference.entriesDiffering();
         ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();
         for (Map.Entry<String, MapDifference.ValueDifference<ByteBuffer>> entry : differenceMap.entrySet()) {
             builder.put(entry.getKey(), entry.getValue().rightValue());
         }
 
         return builder.build();
     }
 
     private void checkTableOpen() {
         checkState(table != null, "Table must be opened before used.");
     }
 }
