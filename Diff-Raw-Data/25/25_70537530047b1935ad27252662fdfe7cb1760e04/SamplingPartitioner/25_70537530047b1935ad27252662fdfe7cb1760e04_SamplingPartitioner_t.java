 package com.nearinfinity.bulkloader;
 
 
 import org.apache.hadoop.conf.Configurable;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.Partitioner;
 
 public class SamplingPartitioner extends Partitioner<ImmutableBytesWritable, Put> implements Configurable {
 
     public static final String COLUMN_COUNT = "mapreduce.samplepartitioner.columncount";
 
     private int columnCount;
 
     private Configuration conf;
 
     @Override
     public int getPartition(ImmutableBytesWritable immutableBytesWritable, Put put, int numPartitions) {
         byte[] row = immutableBytesWritable.get();
         byte rowKey = row[0];
         int partitions = 0;
         switch (rowKey) {
             case 4:
                partitions = (int) (Bytes.toLong(row, 9, 16) % numPartitions);
                 break;
             case 5:
                partitions = 1 + (int) Bytes.toLong(row, 9, 8);
                break;
            case 6:
                 partitions = 1 + columnCount + (int) Bytes.toLong(row, 9, 8);
                 break;
         }
 
         return Math.abs(partitions % numPartitions);
     }
 
     public static void setColumnCount(Configuration conf, int columnCount) {
         conf.setInt(COLUMN_COUNT, columnCount);
     }
 
     @Override
     public void setConf(Configuration conf) {
         this.conf = conf;
         this.columnCount = conf.getInt(COLUMN_COUNT, 3);
     }
 
     @Override
     public Configuration getConf() {
         return this.conf;
     }
 }
