 package org.hackreduce.streetmapper;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.hackreduce.mappers.XMLInputFormat;
 import org.hackreduce.streetmapper.model.NodeRecord;
 import org.hackreduce.streetmapper.model.OsmRecord;
 import org.hackreduce.streetmapper.model.WayNodeRecord;
 import org.hackreduce.streetmapper.model.WayRecord;
 
 /**
  * This MapReduce job will count the total number of records in the data dump.
  */
 public class WayCounter extends Configured implements Tool {
 
 	public enum Count {
 		TOTAL_RECORDS,
 		WAY_RECORDS,
 		NODE_RECORDS,
 		UNIQUE_KEYS,
 		UNKNOWN_RECORDS
 	}
 
 	public static class RecordCounterMapper extends WayMapper<Text, WayNodeRecord> {
 
 		// Our own made up key to send all counts to a single Reducer, so we can
 		// aggregate a total value.
 		public static final Text TOTAL_COUNT = new Text("total");
 
 		@Override
 		protected void map(OsmRecord record, Context context) throws IOException,
 				InterruptedException {
 
 			WayNodeRecord output = new WayNodeRecord(record);
 			
 			context.getCounter(Count.TOTAL_RECORDS).increment(1);
 			if (record instanceof WayRecord) {
 				context.getCounter(Count.WAY_RECORDS).increment(1);
 			} else if (record instanceof NodeRecord) {
 				context.getCounter(Count.NODE_RECORDS).increment(1);
 			}
 			if (output == null) {
 				context.getCounter(Count.UNKNOWN_RECORDS).increment(1);
 			} else {
 				context.write(TOTAL_COUNT, output);
 			}
 		}
 
 	}
 
 	public static class RecordCounterReducer extends Reducer<Text, WayNodeRecord, Text, LongWritable> {
 
 		@Override
 		protected void reduce(Text key, Iterable<WayNodeRecord> values, Context context) throws IOException, InterruptedException {
 
 			long count = 0;
 			for (WayNodeRecord value : values) {
 				count += 1;
 			}
 
 			context.write(key, new LongWritable(count));
 		}
 
 	}
 
 	@Override
 	public int run(String[] args) throws Exception {
 		Configuration conf = getConf();
 
         if (args.length != 2) {
         	System.err.println("Usage: " + getClass().getName() + " <input> <output>");
         	System.exit(2);
         }
 
         // Creating the MapReduce job (configuration) object
         Job job = new Job(conf);
         job.setJarByClass(getClass());
         job.setJobName(getClass().getName());
 
         // Tell the job which Mapper and Reducer to use (classes defined above)
         job.setMapperClass(RecordCounterMapper.class);
 		job.setReducerClass(RecordCounterReducer.class);
 
 		// The OpenStreetMap datasets are XML files with each way and node information enclosed within
 		// the <node></node> and <way></way> tags
 		job.setInputFormatClass(XMLInputFormat.class);
 		XMLMultiRecordReader.setTags(job, "way,node");
 
 		// This is what the Mapper will be outputting to the Reducer
 		job.setMapOutputKeyClass(Text.class);
 		job.setMapOutputValueClass(WayNodeRecord.class);
 
 		// This is what the Reducer will be outputting
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(LongWritable.class);
 
 		// Setting the input folder of the job 
 		FileInputFormat.addInputPath(job, new Path(args[0]));
 
 		// Preparing the output folder by first deleting it if it exists
         Path output = new Path(args[1]);
         FileSystem.get(conf).delete(output, true);
 	    FileOutputFormat.setOutputPath(job, output);
 
 		return job.waitForCompletion(true) ? 0 : 1;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		int result = ToolRunner.run(new Configuration(), new WayCounter(), args);
 		System.exit(result);
 	}
 
 }
