 package com.nearinfinity.bulkloader;
 
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.io.Writable;
 
 import java.io.IOException;
 
 public class SmallLoaderReducer extends TableReducer<ImmutableBytesWritable, Put, Writable> {
     @Override
     public void reduce(ImmutableBytesWritable key, Iterable<Put> rowKeys, Context context) throws IOException, InterruptedException {
        for (Put put : rowKeys) {
            context.write(null, put);
         }
     }
 }
