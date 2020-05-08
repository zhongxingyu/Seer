 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.KeyValueTextInputFormat;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 
 public class Phase2 extends Configured implements Tool {
 	
 	public static class Phase2Mapper extends MapReduceBase 
 			implements Mapper<Text, Text, Text, Text> {
 
 		@Override
 		public void map(Text key, Text value, OutputCollector<Text, Text> output,
 				Reporter reporter) throws IOException {
 			// TODO Auto-generated method stub
 			String[] parts = value.toString().split(":");
 			
 			if(parts.length > 1) {
 				String PRStr = parts[0];
 				String nodesStr = parts[1];
 				String[] nodes = nodesStr.split(",");
 				
 				int count = nodes.length;
 				for(int i = 0; i < nodes.length; i++) {
 					String tmp = PRStr;
 					tmp += ":";
 					tmp += Integer.toString(count);
 					output.collect(new Text(nodes[i]), new Text(tmp));
 				}
 				output.collect(key, new Text(nodesStr));
 			}
 		}
 	}
 	
 	/*
 	 * Calculating PageRank
 	 */
 	public static class Phase2Reducer extends MapReduceBase 
 			implements Reducer<Text, Text, Text, Text> {
 
 		@Override
 		public void reduce(Text key, Iterator<Text> values,
 				OutputCollector<Text, Text> output, Reporter reporter)
 				throws IOException {
 			// TODO Auto-generated method stub
 			
 			// make a copy of values
 			ArrayList<String> values2 = new ArrayList<String>();
 			while(values.hasNext()) {
 				String value = values.next().toString();
 				values2.add(value);
 			}
 			
 			String nodesStr = "";
 			
 			// 0.85
 			float damping = 0.85f;
 			
 			float newPR = 0.0f;
 			for(int i = 0; i < values2.size(); i++) {
 				String value = values2.get(i);
 				String[] parts = value.split(":");
 				if(parts.length > 1) {
 					float PR = Float.parseFloat(parts[0]);
 					int links = Integer.parseInt(parts[1]);
					newPR += (PR/links*damping + (1-damping)); // updating PageRank
 				} else if(parts.length == 1) {
 					// System.out.printf("(%s, %s)\n", key.toString(), value);
 					nodesStr = value;
 				}
 			}
 			String tmp = Float.toString(newPR);
 			tmp += ":";
 			tmp += nodesStr;
 			//System.out.printf("[%s][%s]", key, tmp);
 			output.collect(key, new Text(tmp));
 		}
 	}
 
 	@Override
 	public int run(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		
 		Configuration conf = getConf();
         JobConf job = new JobConf(conf, Phase2.class);
         
         Path in = new Path(args[0]);
         Path out = new Path(args[1]);
         FileInputFormat.setInputPaths(job, in);
         FileOutputFormat.setOutputPath(job, out);
         
         job.setJobName("PageRankPhase2");
         job.setMapperClass(Phase2Mapper.class);
         job.setReducerClass(Phase2Reducer.class);
 
         job.setInputFormat(KeyValueTextInputFormat.class);
         job.set("key.value.separator.in.input.line", "\t");
 
         job.setOutputFormat(TextOutputFormat.class);
         
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
         
         JobClient.runJob(job);
 		return 0;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		int res = ToolRunner.run(new Configuration(), new Phase2(), args);
         System.exit(res);
 	}
}
