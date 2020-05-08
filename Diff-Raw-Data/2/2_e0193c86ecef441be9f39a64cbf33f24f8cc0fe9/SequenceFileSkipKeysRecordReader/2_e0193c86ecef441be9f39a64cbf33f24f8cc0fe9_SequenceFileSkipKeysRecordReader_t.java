 package org.occ.matsu;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.io.SequenceFile;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONValue;
 
 public class SequenceFileSkipKeysRecordReader implements RecordReader<Text, Text> {
     protected Configuration configuration;
     private SequenceFile.Reader reader;
     private long start;
     private long end;
     boolean more = true;
 
     boolean restrictBands;
     List<String> restrictBandsTo;
 
     public SequenceFileSkipKeysRecordReader() { }
 
     public void initialize(InputSplit split, TaskAttemptContext context) throws IOException {
         initialize(split, context.getConfiguration());
     }
 
     public void initialize(InputSplit split, Configuration conf) throws IOException {
         Path path = ((FileSplit)(split)).getPath();
         FileSystem fs = path.getFileSystem(conf);
 
         configuration = conf;
         reader = new SequenceFile.Reader(fs, path, conf);
 
         start = ((FileSplit)(split)).getStart();
         end = start + split.getLength();
         if (start > reader.getPosition()) {
             reader.sync(start);
         }
 
         start = reader.getPosition();
         more = start < end;
 
         String stringRestrictBands = configuration.get("org.occ.matsu.restrictBands");
         if (stringRestrictBands == null) {
             restrictBands = false;
         } else {
             restrictBands = (stringRestrictBands.toLowerCase().equals("true"));
         }
 
         if (restrictBands) {
             String jsonRestrictBandsTo = configuration.get("org.occ.matsu.restrictBandsTo");
             restrictBandsTo = new ArrayList<String>();
             restrictBandsTo.add("metadata");
             restrictBandsTo.add("bands");
             restrictBandsTo.add("shape");
 
             JSONArray array = ((JSONArray)(JSONValue.parse(jsonRestrictBandsTo)));
             if (array == null) {
                 throw new IOException("Cannot parse org.occ.matsu.restrictBandsTo.  Be sure to quote it like this: org.occ.matsu.restrictBandsTo='[\"B01\",\"B02\",\"B03\"]'");
             }
 
             for (Object item : array) {
                 restrictBandsTo.add(item.toString());
             }
         }
     }
 
     public Class getKeyClass() { return reader.getKeyClass(); }
     public Class getValueClass() { return reader.getValueClass(); }
     public Text createKey() { return new Text(); }
     public Text createValue() { return new Text(); }
 
     public synchronized boolean next(Text key, Text value) throws IOException {
         if (!more) return false;
         long pos = reader.getPosition();
         boolean remaining = reader.next(key);
 
         if (remaining) {
            if (!restrictBands  ||  restrictBandsTo.contains(key.toString())) {
                 reader.getCurrentValue(value);
             } else {
                 value.set("EMPTY");
             }
         }
         if (pos >= end  &&  reader.syncSeen()) {
             more = false;
         } else {
             more = remaining;
         }
         return more;
     }
 
     public float getProgress() throws IOException {
         if (end == start) {
             return 0.0f;
         } else {
             return Math.min(1.0f, (reader.getPosition() - start) / (float)(end - start));
         }
     }
 
     public synchronized long getPos() throws IOException {
         return reader.getPosition();
     }
 
     public synchronized void close() throws IOException {
         reader.close();
     }
 
 }
