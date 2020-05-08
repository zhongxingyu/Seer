 package group1.inverted;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class Indexer extends Configured implements Tool {
 
 	public static class Map extends Mapper<LongWritable, Text, Text, Posting> {
 
 		@Override
 		protected void map(LongWritable key, Text value, Context context)
 				throws IOException, InterruptedException {
 			FileSplit fileSplit = (FileSplit) context.getInputSplit();
 			String filename = fileSplit.getPath().getName();
 			// Create the tokenizer
 			Tokenizer t;
 			try {
 				t = new Tokenizer(filename, value);
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 
 			// Create the collection for accumulation of word and word count
 			// associations
 			java.util.Map<String, Integer> index = new java.util.HashMap<String, Integer>();
 
 			// Collect word counts
 			Iterator<String> iter = t.iterator();
 			while (iter.hasNext()) {
 				String word = iter.next();
 				if (index.containsKey(word)) {
 					index.put(word, index.get(word) + 1);
 				} else {
 					index.put(word, 1);
 				}
 			}
 
 			// Emit postings
 			Iterator<String> keys = index.keySet().iterator();
 			while (keys.hasNext()) {
 				String word = keys.next();
 				Posting p = new Posting();
 				p.docid = t.getDocId();
 				p.count = index.get(word);
 				context.write(new Text(word), p);
 			}
 
 		}
 
 		public void map(LongWritable key, Text contents,
 				OutputCollector<Text, Posting> output, Reporter reporter)
 				throws IOException {
 		}
 
 	}
 
 	/**
 	 * A reducer class that just emits its input.
 	 */
 	public static class Reduce extends Reducer<Text, Posting, Text, Posting> {
 
 		@Override
 		protected void reduce(Text key, Iterable<Posting> values,
 				Context context) throws IOException, InterruptedException {
 			Iterator<Posting> i = values.iterator();
 			while (i.hasNext()) {
 				context.write(key, i.next());
 			}
 		}
 
 	}
 
 	public int run(String[] args) throws Exception {
 
 		Job job = new Job();
 		job.setJarByClass(Indexer.class);
 		// job.setInputFormatClass(TextInputFormat.class);
 		job.setJobName("Indexer");
 		job.setMapperClass(Map.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(Posting.class);
 		job.setCombinerClass(Reduce.class);
 		job.setReducerClass(Reduce.class);
 		FileInputFormat.setInputPaths(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 		job.waitForCompletion(true);
 		return 0;
 	}
 
 	public static void main(String[] args) throws Exception {
 		String[] newArgs = { args[1], args[2] };
 		int res = ToolRunner.run(new Indexer(), newArgs);
 		System.exit(res);
 	}
 
 }
