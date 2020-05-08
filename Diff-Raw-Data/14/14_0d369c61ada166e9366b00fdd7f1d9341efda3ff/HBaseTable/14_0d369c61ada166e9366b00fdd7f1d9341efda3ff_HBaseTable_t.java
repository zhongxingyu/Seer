 package com.nearinfinity.honeycomb.hbase;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.name.Named;
 import com.nearinfinity.honeycomb.Scanner;
 import com.nearinfinity.honeycomb.Table;
 import com.nearinfinity.honeycomb.config.ConfigConstants;
 import com.nearinfinity.honeycomb.config.Constants;
 import com.nearinfinity.honeycomb.exceptions.RowNotFoundException;
 import com.nearinfinity.honeycomb.hbase.rowkey.DataRowKey;
 import com.nearinfinity.honeycomb.hbase.rowkey.IndexRowKey;
 import com.nearinfinity.honeycomb.hbase.rowkey.IndexRowKeyBuilder;
 import com.nearinfinity.honeycomb.hbase.rowkey.SortOrder;
 import com.nearinfinity.honeycomb.mysql.*;
 import com.nearinfinity.honeycomb.mysql.Row;
 import com.nearinfinity.honeycomb.mysql.gen.QueryType;
 import com.nearinfinity.honeycomb.mysql.schema.IndexSchema;
 import com.nearinfinity.honeycomb.mysql.schema.TableSchema;
 import com.nearinfinity.honeycomb.util.Verify;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.log4j.Logger;
 
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.List;
 import java.util.UUID;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class HBaseTable implements Table {
     private static final Logger logger = Logger.getLogger(HBaseTable.class);
     private final HTableInterface hTable;
     private final HBaseStore store;
     private final long tableId;
     private final MutationFactory mutationFactory;
     private long writeBufferSize = ConfigConstants.DEFAULT_WRITE_BUFFER_SIZE;
 
     @Inject
     public HBaseTable(HTableInterface hTable, HBaseStore store, @Assisted Long tableId) {
         Verify.isValidId(tableId);
         this.hTable = checkNotNull(hTable);
         this.store = checkNotNull(store);
         this.tableId = tableId;
         this.mutationFactory = new MutationFactory(store);
     }
 
     @Inject
     public void setWriterBufferSize(final @Named(ConfigConstants.PROP_WRITE_BUFFER_SIZE) Long bufferSize) {
         writeBufferSize = bufferSize;
     }
 
     @Override
     public void insert(Row row) {
         checkNotNull(row);
         HBaseOperations.performPut(hTable, mutationFactory.insert(tableId, row));
     }
 
     @Override
     public void insertTableIndex(final IndexSchema indexSchema) {
         checkNotNull(indexSchema, "The index schema is invalid");
         final Collection<IndexSchema> indices = ImmutableList.of(indexSchema);
         final Scanner scanner = tableScan();
         while (scanner.hasNext()) {
             HBaseOperations.performPut(hTable,
                     mutationFactory.insertIndices(tableId, scanner.next(), indices));
         }
 
         Util.closeQuietly(scanner);
     }
 
     @Override
     public void deleteTableIndex(final IndexSchema indexSchema) {
         checkNotNull(indexSchema, "The index schema is invalid");
 
         batchDeleteData(tableScan(), ImmutableList.of(indexSchema), false);
     }
 
     @Override
     public void update(Row oldRow, Row newRow, Collection<IndexSchema> changedIndices) {
         checkNotNull(newRow);
 
         // Delete indices that have changed
         final List<Delete> deletes =
                 mutationFactory.deleteIndices(tableId, oldRow, changedIndices);
         // Insert data row and indices that have changed
         final List<Put> puts =
                 mutationFactory.insert(tableId, newRow);
 
         HBaseOperations.performDelete(hTable, deletes);
        HBaseOperations.performPut(hTable, puts);
     }
 
     @Override
     public void delete(final UUID uuid) {
         checkNotNull(uuid);
         Row row = get(uuid);  // TODO: Should be passed in from C++ side
         HBaseOperations.performDelete(hTable, mutationFactory.delete(tableId, row));
     }
 
     @Override
     public void deleteAllRows() {
         batchDeleteData(tableScan(), store.getSchema(tableId).getIndices(), true);
     }
 
     @Override
     public void flush() {
         HBaseOperations.performFlush(hTable);
     }
 
     @Override
     public Row get(UUID uuid) {
         DataRowKey dataRow = new DataRowKey(tableId, uuid);
         Get get = new Get(dataRow.encode());
         Result result = HBaseOperations.performGet(hTable, get);
         if (result.isEmpty()) {
             throw new RowNotFoundException(uuid);
         }
         return Row.deserialize(result.getValue(Constants.DEFAULT_COLUMN_FAMILY,
                 new byte[0]));
     }
 
     @Override
     public Scanner tableScan() {
         DataRowKey startRow = new DataRowKey(tableId);
         DataRowKey endRow = new DataRowKey(tableId + 1);
         return createScannerForRange(startRow.encode(), endRow.encode());
     }
 
     @Override
     public Scanner ascendingIndexScanAt(QueryKey key) {
         IndexRowKey startRow = indexPrefixedForTable(key)
                 .withSortOrder(SortOrder.Ascending)
                 .build();
 
         long indexId = store.getIndexId(tableId, key.getIndexName());
 
         IndexRowKey endRow = IndexRowKeyBuilder
                 .newBuilder(tableId, indexId + 1)
                 .withSortOrder(SortOrder.Ascending)
                 .build();
         return createScannerForRange(startRow.encode(), endRow.encode());
     }
 
     @Override
     public Scanner ascendingIndexScanAfter(QueryKey key) {
         IndexRowKey startRow = indexPrefixedForTable(key)
                 .withSortOrder(SortOrder.Ascending)
                 .build();
 
         long indexId = store.getIndexId(tableId, key.getIndexName());
 
         IndexRowKey endRow = IndexRowKeyBuilder
                 .newBuilder(tableId, indexId + 1)
                 .withSortOrder(SortOrder.Ascending)
                 .build();
         return createScannerForRange(incrementRowKey(startRow.encode()), endRow.encode());
     }
 
     @Override
     public Scanner descendingIndexScanAt(QueryKey key) {
         IndexRowKey startRow = indexPrefixedForTable(key)
                 .withSortOrder(SortOrder.Descending)
                 .build();
 
         long indexId = store.getIndexId(tableId, key.getIndexName());
 
         IndexRowKey endRow = IndexRowKeyBuilder
                 .newBuilder(tableId, indexId + 1)
                 .withSortOrder(SortOrder.Descending)
                 .build();
         return createScannerForRange(startRow.encode(), endRow.encode());
     }
 
     @Override
     public Scanner descendingIndexScanAfter(QueryKey key) {
         IndexRowKey startRow = indexPrefixedForTable(key)
                 .withSortOrder(SortOrder.Descending)
                 .build();
 
         long indexId = store.getIndexId(tableId, key.getIndexName());
 
         IndexRowKey endRow = IndexRowKeyBuilder
                 .newBuilder(tableId, indexId + 1)
                 .withSortOrder(SortOrder.Descending)
                 .build();
         return createScannerForRange(incrementRowKey(startRow.encode()), endRow.encode());
     }
 
     @Override
     public Scanner indexScanExact(QueryKey key) {
         IndexRowKey row = indexPrefixedForTable(key).withSortOrder(SortOrder.Ascending).build();
 
         // Scan is [start, end) : increment to set end to next possible row
         return createScannerForRange(row.encode(), incrementRowKey(row.encode()));
     }
 
     @Override
     public void close() {
         Util.closeQuietly(hTable);
     }
 
     private static byte[] incrementRowKey(byte[] key) {
         BigInteger integer = new BigInteger(key);
         return integer.add(new BigInteger("1")).toByteArray();
     }
 
     /**
      * Perform a batch delete operation over the rows obtained from the
      * specified data scanner
      *
      * @param dataScanner The {@link Scanner} used to gather rows
      * @param indices     The table index mapping to use during this operation
      * @param deleteRow   Indicate that each row obtained from the data scanner should be deleted
      */
     private void batchDeleteData(final Scanner dataScanner, final Collection<IndexSchema> indices, boolean deleteRow) {
         long numDeletes = 0;
         int deletesPerRow = 2 * indices.size() + (deleteRow ? 1 : 0);
         final List<Delete> deletes = Lists.newLinkedList();
 
         while (dataScanner.hasNext()) {
             final Row row = dataScanner.next();
 
             if (deleteRow) {
                 deletes.addAll(mutationFactory.delete(tableId, row));
             } else {
                 deletes.addAll(mutationFactory.deleteIndices(tableId, row));
             }
 
             numDeletes += deletesPerRow;
 
             if (numDeletes > writeBufferSize) {
                 HBaseOperations.performDelete(hTable, deletes);
                 numDeletes = 0;
             }
         }
 
         Util.closeQuietly(dataScanner);
         HBaseOperations.performDelete(hTable, deletes);
     }
 
     private IndexRowKeyBuilder indexPrefixedForTable(final QueryKey key) {
         final TableSchema schema = store.getSchema(tableId);
         final long indexId = store.getIndexId(tableId, key.getIndexName());
         final IndexRowKeyBuilder indexRowBuilder =
                 IndexRowKeyBuilder.newBuilder(tableId, indexId);
 
         if (key.getQueryType() == QueryType.INDEX_LAST
                 || key.getQueryType() == QueryType.INDEX_FIRST) {
             return indexRowBuilder;
         }
 
         return indexRowBuilder.withQueryKey(key, schema);
     }
 
     private Scanner createScannerForRange(byte[] start, byte[] end) {
         Scan scan = new Scan(start, end);
         ResultScanner scanner = HBaseOperations.getScanner(hTable, scan);
         return new HBaseScanner(scanner);
     }
 }
 
