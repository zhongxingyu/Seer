 package com.nearinfinity.bulkloader;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.io.Writable;
 
 import java.io.IOException;
 
 public class SmallLoaderReducer extends TableReducer<ImmutableBytesWritable, Put, Writable> {
    private static final Log LOG = LogFactory.getLog(SmallLoaderReducer.class);

     @Override
     public void reduce(ImmutableBytesWritable key, Iterable<Put> rowKeys, Context context) throws IOException, InterruptedException {
        try {
            for (Put put : rowKeys) {
                context.write(null, put);
            }
        } catch (Exception e) {
            LOG.error("Reduce error", e);
         }
     }
 }
