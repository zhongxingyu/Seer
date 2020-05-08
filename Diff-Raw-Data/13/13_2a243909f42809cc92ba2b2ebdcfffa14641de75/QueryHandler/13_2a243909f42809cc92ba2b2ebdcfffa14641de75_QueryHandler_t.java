 package group1.inverted;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 
 public class QueryHandler extends Configured implements Tool {
 
	public static class Map extends Mapper<LongWritable, Text, LongWritable, Text> {
 		@Override
		public void map(LongWritable unused, Text value, Context context)
 				throws IOException, InterruptedException {
 			FileSplit fileSplit = (FileSplit) context.getInputSplit();
 			Path inputDir = fileSplit.getPath().getParent();
 			Path inputParent = inputDir.getParent();
 			String query = readFile(new Path(inputParent, "query.txt"));
 			Set<String> qset = new HashSet<String>();
 			qset.addAll(Arrays.asList(query.toLowerCase().split(" ")));
			
			IndexParser.Index index = IndexParser.parseLine(value.toString());
 
			String current = index.word; // get from index parse method
			String docId = index.docid;
 			if (qset.contains(current)) {
 				context.write(new LongWritable(0), new Text(docId));
 			}
 		}
 
 		protected String readFile(Path p) throws IOException {
 			FileSystem fs = FileSystem.get(new Configuration());
 			FSDataInputStream in = fs.open(p);
 			BufferedReader reader = new BufferedReader(
 					new InputStreamReader(in));
 			String line = null;
 			StringBuilder builder = new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				builder.append(line).append("\n");
 			}
 			reader.close();
 			return builder.toString();
 		}
 	}
 
 	public static class Reduce extends Reducer<LongWritable, Text, Text, Text> {
 		@Override
 		public void reduce(LongWritable key, Iterable<Text> values,
 				Context context) throws IOException, InterruptedException {
 			Set<String> idSet = new HashSet<String>();
 			for (Text t : values) {
 				idSet.add(new String(t.toString()));
 			}
 			for (String id : idSet) {
 				context.write(new Text("Result: "), new Text(id));
 			}
 		}
 	}
 
 	public int run(String[] args) throws Exception {
 		Job job = new Job();
 		job.setJarByClass(QueryHandler.class);
 		job.setJobName("QueryHandler");
 		job.setMapperClass(Map.class);
 		job.setOutputKeyClass(LongWritable.class);
 		job.setOutputValueClass(Text.class);
 		// job.setCombinerClass(Reduce.class);
 		job.setReducerClass(Reduce.class);
 		FileInputFormat.setInputPaths(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
 		job.waitForCompletion(true);
 		return 0;
 	}
 
 }
