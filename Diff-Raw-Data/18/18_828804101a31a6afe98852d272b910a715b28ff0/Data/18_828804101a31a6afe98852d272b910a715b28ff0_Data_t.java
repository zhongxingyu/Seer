 package com.dsp.ass3;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Counters;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 
 
 public class Data {
 
     private static String labelsPath,
             startOfData = "{",
             endOfData = "}",
             arffCoordinateDelim = ",";
 
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Data.class.getName()));
 
 
     // Write { N1, N2, hypernym-index : { class, i1:h1, i2:h2, i3:h3,...} }
     // Where [..] is the pair's SPARSE dep-tree vector.
     public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
 
         // ARFF header label map.
         private Map<String, Integer> labels = new HashMap <String, Integer>();
         private String depTree;
 
         private AWSCredentials creds;
         private AmazonS3 s3;
 
         private Text newKey = new Text(),
                 newValue = new Text();
 
 
         // Init headers labels map.
         @Override
         public void setup(Context context) {
             creds = Utils.loadCredentials();
             if (creds == null) {
                 logger.severe("Couldn't load credentials.");
                 return;
             }
 
             s3 = Utils.createS3(creds);
 
             // Fetch label data from url.
            String path = context.getConfiguration().get(Utils.labelsPath, null);
            if (path == null) {
                logger.severe("Error fetching labels path.");
                return;
            }

            InputStream is = Utils.readFromS3(s3, path);
             if (is == null) {
                 return;
             }
 
             // Read input.
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
             int i=1;
             try {
                 // Read dep-trees and set labels in map.
                 while ( (depTree = br.readLine()) != null) {
 
                     // Set coordinate label in labels map.
                     labels.put(depTree, Integer.valueOf(i));
 
                     // Set label coordinate index in vector.
                     i++;
                 }
             } catch (IOException e) {
                 logger.severe(e.getMessage());
                 return;
             }
 
             // Close input stream.
             try {
                 is.close();
             } catch (IOException e) {
                 logger.severe(e.getMessage());
             }
 
             // Close buffer.
             try {
                 br.close();
             } catch (IOException e) {
                 logger.severe(e.getMessage());
                 return;
             }
         }
 
 
         @Override
         public void map(LongWritable key, Text value, Context context)
             throws IOException, InterruptedException {
 
             StringBuilder sb = new StringBuilder();
 
             // Fetch key/value and emit.
             String v = value.toString();
             Integer depTreeIndex;
 
             // Fetch the index of the '\t' before the first biarc label.
             int classIndex = v.indexOf(Utils.biarcDelim),
                 dataIndex = v.indexOf(Utils.biarcDelim, classIndex + 1);
 
             boolean related = v.substring(classIndex + 1, dataIndex).equals("true");
 
             // Prepend beginning of vector char.
             sb.append(startOfData);
 
             // Append un/related as index 0.
             sb.append("0 ").append(related);
 
             // Write SPARSE vector with coordinate indexes.
             // We use the labels map (init in setup() ) to fetch each cooridnate label's index.
             String[] depTrees = v.substring(dataIndex + 1).split(Utils.coordinateDelim);
             String tree, line, hits;
             long hitsIndex;
             List<Coordinate> coordinates = new ArrayList<Coordinate>();
 
             for (int i=0; i < depTrees.length; i++) {
                 // Fetch dep-tree.
                 line = depTrees[i];
 
                 // Fetch hits.
                 hitsIndex = line.lastIndexOf(Utils.biarcDelim);
                 hits = line.substring((int)hitsIndex + 1);
 
                 // Fetch dep-tree index from labels map.
                 tree = line.substring(0, (int)hitsIndex);
                 depTreeIndex = labels.get(tree);
 
                 // Write only non-zero coordinates (this is a SPARSE vector).
                 if (depTreeIndex != null) {
                     // Append coordinate index and value.
                     coordinates.add(new Coordinate(depTreeIndex.longValue(), hits));
                 }
             }
 
             // Sort coordinates.
             java.util.Collections.sort(coordinates);
 
             Iterator<Coordinate> it = coordinates.iterator();
             while (it.hasNext()) {
                 sb.append(arffCoordinateDelim).append(it.next());
             }
 
             // Append end of vector char.
             sb.append(endOfData);
 
             // Fetch noun-pair key and set as key.
             newKey.set(v.substring(0, classIndex));
 
             // Set vector as value.
             newValue.set(sb.toString());
 
             context.write(newKey, newValue);
         }
     }
 
 
     // Partition by key hash code.
     public static class PartitionerClass extends Partitioner<Text, Text> {
         @Override
         public int getPartition(Text key, Text value, int numPartitions) {
             return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
         }
     }
 
 
     // Reducer just passes on data.
     public static class ReduceClass extends Reducer<Text, Text, Text, NullWritable> {
 
         @Override
         public void reduce(Text key, Iterable<Text> values, Context context)
             throws IOException, InterruptedException {
 
             // Emit for each value element (in case for some reason we have more).
             for (Text value : values) {
                 context.write(value, NullWritable.get());
             }
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
 
        // Use this for local testing.
        // labelsPath = args[Utils.argInIndex];

        // Use this for AWS.
        conf.set(Utils.labelsPath, args[Utils.argInIndex]);

         Job job = new Job(conf, "Data");
 
         job.setJarByClass(Pairs.class);
         job.setMapperClass(MapClass.class);
         job.setPartitionerClass(PartitionerClass.class);
         job.setReducerClass(ReduceClass.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         FileInputFormat.addInputPath(job, new Path(args[Utils.argInIndex +1]));
         FileOutputFormat.setOutputPath(job, new Path(args[Utils.argInIndex + 2]));
 
         boolean result = job.waitForCompletion(true);
 
         // Write totalRecord and totalBytes from task counter to file.
         if (result) {
             Counters counters = job.getCounters();
 
             long totalRecords = counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "MAP_OUTPUT_RECORDS").getValue();
             long totalBytes = counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "MAP_OUTPUT_BYTES").getValue();
 
             Utils.uploadCountersToS3(totalRecords, totalBytes, "Data");
         }
 
         System.exit(result ? 0 : 1);
     }
 }
