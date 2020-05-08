 package com.etsy.rdb;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.RecordWriter;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.util.Progressable;
 
public class RDBOutputFormat extends FileOutputFormat<RDBString, RDBString> {
 
   protected static class RedisRecordWriter implements RecordWriter<RDBString, RDBString> {
 
     private RDBOutputStream out;
 
     public RedisRecordWriter(DataOutputStream out) throws IOException {
       this.out = new RDBOutputStream(out);
       this.out.writeHeader();
       this.out.writeDatabaseSelector(0);
     }
 
     public synchronized void write(RDBString key, RDBString value) throws IOException {
       out.writeString(key, value);
     }
 
     public synchronized void close(Reporter reporter) throws IOException {
       out.writeFooter();
       out.close();
     }
   }
 
   public RecordWriter getRecordWriter(FileSystem ignored, JobConf job, String name, Progressable progress) throws IOException {
     Path file = FileOutputFormat.getTaskOutputPath(job, name);
     FileSystem fs = file.getFileSystem(job);
     FSDataOutputStream fileOut = fs.create(file, progress);
     return new RedisRecordWriter(fileOut);
   }
}
