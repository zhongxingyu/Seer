 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.*;
 
 public class Statistics {
 
 	// group number, node number, label
 	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text> {
 		public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> output, Reporter reporter) 
 				throws IOException {
         	LongWritable one = new LongWritable(1);
 			String line = value.toString();
 			output.collect(one, new Text(line.substring(line.indexOf(' ')+1)));
 			}
 		}
	}
 	
 	public static class Reduce extends MapReduceBase implements Reducer<LongWritable, Text, LongWritable, Text> {
		public static long TOTAL_POINTS = 10000*10000;
 		public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<LongWritable, Text> output, Reporter reporter) 
 				throws IOException {
         	LongWritable one = new LongWritable(1);
 			
			Map<long, ArrayList<long>> components = new HashMap<long, ArrayList<long>>();
			Set<long> uniqueNodes = new HashSet<long>();
 						
 			while (values.hasNext()) {
 				String[] line = values.next().toString().split(' ');
				long node = Long.parseLong(line[0]);
				long label = Long.parseLong(line[1]);
 				if (!uniqueNodes.contains(node)) {
 					uniqueNodes.add(node);
					ArrayList<long> nodes = (components.containsKey(label)) ? components.get(label) : new ArrayList<long>();
 					nodes.add(node);
 				}
 			}
 			
 			long totalEdges = 0;
 			long totalNodes = uniqueNodes.size();
 			long totalComponents = components.size();
 
 			float sum = 0;
			for (ArrayList<long> componentNodes : components.values()) {
 				int ccSize = componentNodes.size();
 				sum += ccSize * ccSize;
 			}
 			
 			float averageBurnCount = sum / TOTAL_POINTS;
 			float averageComponentSize = sum / totalNodes;
 			
 			String s = "Number of vertices: %s \n" + 
 						"Number of edges: %s \n" + 
 						"Number of distinct connected components: %s \n" + 
 						"Average size of connected components: %s \n" + 
 						"Average burn count: %s \n";
			String stats = String.format(s, totalNodes.toString(), totalEdges.toString(), 
				totalComponents.toString(), averageComponentSize.toString(), averageBurnCount.toString());
 						
 			output.collect(one, new Text(stats));
 		}
 	}
 	
 	public static void main(String[] args) throws Exception {
 		JobConf conf = new JobConf(Statistics.class);
 		conf.setJobName("statistics");
 		
 		conf.setOutputKeyClass(Text.class);
 		conf.setOutputValueClass(IntWritable.class);
 		
 		conf.setMapperClass(Map.class);
 		conf.setCombinerClass(Reduce.class);
 		conf.setReducerClass(Reduce.class);
 		
 		conf.setInputFormat(TextInputFormat.class);
 		conf.setOutputFormat(TextOutputFormat.class);
 		
 		FileInputFormat.setInputPaths(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 		
 		JobClient.runJob(conf);
 	}
 }


