 package com.nearinfinity.honeycomb;
 
 import au.com.bytecode.opencsv.CSVParser;
 import com.nearinfinity.honeycomb.hbaseclient.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import java.io.IOException;
import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

 public class BulkLoadMapper
         extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {
     static final Log LOG = LogFactory.getLog(BulkLoadMapper.class);
 
     private CSVParser csvParser;
     private String[] sqlColumns;
     private Map<String, ColumnMetadata> columnMetadata;
     private List<List<String>> indexColumns;
     private TableInfo tableInfo;
 
     public enum Counters {ROWS, FAILED_ROWS, PUTS}
 
 
     @Override
     protected void setup(Context context)
             throws IOException, InterruptedException {
         Configuration conf = context.getConfiguration();
 
         char separator = conf.get("importtsv.separator", " ").charAt(0);
        LOG.info(format("Separator = %X", (int) separator));
         csvParser = new CSVParser(separator);
         sqlColumns = conf.getStrings("honeycomb.sql.columns");
 
 
         // Setup
         String zkQuorum = conf.get("zk.quorum");
         String sqlTable = conf.get("honeycomb.sql.table");
         String hbTable = conf.get("honeycomb.hb.table");
 
         // Check that necessary configuration variables are set
         if (zkQuorum == null) {
             LOG.error("zk.quorum not set.  Job will fail.");
             throw new IOException("zk.quorum not set");
         }
         if (sqlTable == null) {
             LOG.error("honeycomb.sql.table not set.  Job will fail.");
             throw new IOException("honeycomb.sql.table not set");
         }
         if (sqlColumns == null) {
             LOG.error("honeycomb.sql.columns not set.  Job will fail.");
             throw new IOException("honeycomb.sql.columns not set");
         }
         if (hbTable == null) {
             LOG.error("honeycomb.hb.table not set.  Job will fail.");
             throw new IOException("honeycomb.hb.table not set");
         }
 
         HTable table = new HTable(conf, hbTable);
 
         tableInfo = TableCache.getTableInfo(sqlTable, table);
         indexColumns = Index.indexForTable(tableInfo.tableMetadata());
 
         // Setup column metadata map: column_name -> column_meta
         columnMetadata = new TreeMap<String, ColumnMetadata>();
         for (String sqlColumn : sqlColumns) {
            ColumnMetadata metadata = tableInfo.getColumnMetadata(sqlColumn);
            checkNotNull(metadata, format("Column %s is missing metadata.", sqlColumn));
            columnMetadata.put(sqlColumn, metadata);
         }
     }
 
     @Override
     public void map(LongWritable offset, Text line, Context context)
             throws InterruptedException {
         try {
             String[] fields = csvParser.parseLine(line.toString());
 
             if (sqlColumns.length != fields.length) {
                 throw new IllegalArgumentException(
                        format("Line contains wrong number of columns: %s. Expected: %d Was: %d", line.toString(), sqlColumns.length, fields.length));
             }
 
             // Create value map to pass to put list creator
             Map<String, byte[]> valueMap = new TreeMap<String, byte[]>();
 
             for (int i = 0; i < fields.length; i++) {
                 String sqlColumn = sqlColumns[i];
                 byte[] value = ValueParser.parse(fields[i],
                         columnMetadata.get(sqlColumns[i]));
                 if (value == null) {
                     break;
                 } // null field
                 valueMap.put(sqlColumn, value);
             }
 
             List<Put> puts = PutListFactory.createDataInsertPutList(valueMap,
                     tableInfo, indexColumns);
 
             for (Put put : puts) {
                 context.write(new ImmutableBytesWritable(put.getRow()), put);
             }
 
             context.getCounter(Counters.ROWS).increment(1);
             context.getCounter(Counters.PUTS).increment(puts.size());
 
         } catch (IOException e) {
             LOG.error("CSVParser unable to parse line: " + line.toString(), e);
             context.getCounter(Counters.FAILED_ROWS).increment(1);
         } catch (IllegalArgumentException e) {
             LOG.error(e.getMessage());
             context.getCounter(Counters.FAILED_ROWS).increment(1);
         } catch (ParseException e) {
             LOG.error(e.getMessage());
             context.getCounter(Counters.FAILED_ROWS).increment(1);
         }
     }
 
     private class ColumnMismatchException extends Throwable {
     }
 }
