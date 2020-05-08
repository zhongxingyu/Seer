 package com.brighttag.sc2cc;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.inject.Inject;
 
 import me.prettyprint.cassandra.serializers.CompositeSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
 import me.prettyprint.hector.api.beans.Composite;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.HSuperColumn;
 import me.prettyprint.hector.api.beans.SuperRow;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Multi-threaded bulk transformer to rewrite SuperColumn-based data 
  * to use Composite Columns instead.
  * 
  * @author codyaray
  * @since 4/19/2012
  */
 public class Migrator {
   private static Logger log = LoggerFactory.getLogger(Migrator.class);
 
   private final Cluster cluster;
   private final Keyspace keyspace;
   private final ListeningExecutorService executor;
 
   @Inject
   public Migrator(Cluster cluster, Keyspace keyspace, ExecutorService executor) {
     this.cluster = cluster;
     this.keyspace = keyspace;
     this.executor = MoreExecutors.listeningDecorator(executor);
   }
 
   public void shutdown() {
     executor.shutdown();
     cluster.getConnectionManager().shutdown();
   }
 
   /**
    * Transforms Cassandra data from using Super Columns to Composite Columns,
    * rewriting data from {@code oldColumnFamily} to {@code newColumnFamily} in
    * the process. The {@code newColumnFamily} schema must already exist.
    *
    * Example:
    * 
    * Original Schema
    *     column_family
    *         row_key
    *             super_column_name
    *                 column_name=column_value
    *
    * Transformed Schema
    *     column_family
    *         row_key
    *             super_column_name:column_name=column_value
    * 
    * Implementation Notes:
    * 
    * All reads are done in the main thread. The SuperRowIterator queries Cassandra for blocks of
    * {@code rowCount} super rows. The rows are then grouped in lists of size {@code taskCount}.
    * Lists are held in a blocking queue of size {@code queueSize} until they can be processed.
    * There are {@code threadNum} worker threads. When the queue is full, the caller thread
    * processes the next task to be submitted.
    * 
    * @param oldColumnFamily the name of the existing column family
    * @param newColumnFamily the name of the new, empty column family
    * @param rowCount the number of rows to read in chunks from the database
    * @param taskSize the number of rows to rewrite per worker
    * @return a list of futures representing the number of rows inserted by each worker
    * @throws InterruptedException 
    */
   public List<ListenableFuture<Integer>> migrate(String oldColumnFamily, String newColumnFamily,
           int rowCount, int taskSize) throws InterruptedException {
     log.info("Transforming column family {} to {} using taskSize {} and rowCount {}",
         new Object[] {oldColumnFamily, newColumnFamily, taskSize, rowCount});
 
     int realRowCount = 0;
     List<ListenableFuture<Integer>> rowCounts = Lists.newArrayList();
     Iterable<SuperRow<String,String,String,String>> keyIterator =
             new SuperRowIterator<String>(keyspace, oldColumnFamily, StringSerializer.get(), rowCount);
 
     List<SuperRow<String,String,String,String>> rows = Lists.newArrayList();
     for (SuperRow<String,String,String,String> row : keyIterator) {
       rows.add(row);
       if (rows.size() % taskSize == 0) {
         realRowCount += rows.size();
        rowCounts.add(doRewrite(newColumnFamily, rows));
         rows = Lists.newArrayList();
       }
     }
     // rewrite remaining rows
    rowCounts.add(doRewrite(newColumnFamily, rows));
 
     log.info("This many rows: {}", realRowCount);
     return rowCounts;
   }
 
   private ListenableFuture<Integer> doRewrite(String columnFamily,
       List<SuperRow<String,String,String,String>> rows) {
     log.debug("Scheduling rewrite for rows {} through {}", rows.get(0).getKey(), rows.get(rows.size()-1).getKey());
     return executor.submit(new ReWriter(columnFamily, rows));
   }
 
   /**
    * A "worker" that rewrites the given {@code rows} to {@code columnFamily},
    * migrating the superColumn/column to composite(superColumn,column).
    */
   private class ReWriter implements Callable<Integer> {
     private final String columnFamily;
    private List<SuperRow<String,String,String,String>> rows; // not final to set null below
 
     ReWriter(String columnFamily, List<SuperRow<String,String,String,String>> rows) {
       this.columnFamily = columnFamily;
       this.rows = ImmutableList.copyOf(rows);
     }
 
     /**
      * Rewrites the row, changing superColumn/column to composite(superColumn,column)
      * @return the number of rows re-written by this worker
      */
     @Override
     public Integer call() throws Exception {
       log.debug("Starting rewrite for rows {} through {}", rows.get(0).getKey(), rows.get(rows.size()-1).getKey());
       int count = rows.size();
 
       Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
       for (SuperRow<String,String,String,String> row : rows) {
         log.debug("Inserting row {}", row);
         for (HSuperColumn<String, String, String> superColumn : row.getSuperSlice().getSuperColumns()) {
           for (HColumn<String, String> column : superColumn.getColumns()) {
             HColumn<Composite,String> newColumn = column(superColumn, column);
             mutator.addInsertion(row.getKey(), columnFamily, newColumn);
           }
         }
       }
       mutator.execute();
       log.info("Inserted {} rows", count);
 
      rows = null; // allow the rows to be reclaimed by GC
       return Integer.valueOf(count);
     }
   }
 
   /**
    * Returns a new column with key {@code superColumn.name:column.name}
    * and value {@code column.value}.
    * 
    * @param superColumn original superColumn
    * @param column original column
    * @return new composite-keyed column
    */
   private static HColumn<Composite, String> column(
       HSuperColumn<String,String,String> superColumn, HColumn<String,String> column) {
     Composite newColKey = composite(superColumn.getName(), column.getName());
     return HFactory.createColumn(
         newColKey, column.getValue(), new CompositeSerializer(), StringSerializer.get());
   }
 
   /**
    * Returns a two-component composite
    * 
    * @param superColumn the first component of the returned composite
    * @param column the second component of the returned composite
    * @return a two-component composite
    */
   private static Composite composite(String superColumn, String column) {
     Composite composite = new Composite();
     composite.addComponent(0, superColumn, ComponentEquality.EQUAL);
     composite.addComponent(1, column, ComponentEquality.EQUAL);
     return composite;
   }
 }
