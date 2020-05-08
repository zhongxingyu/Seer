 package mapreduce;
 
 import java.util.Date;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 
 import clusterer.KmeansCluster;
 import config.Constants;
 
 public class KmeansDriver {
 	public static void configure() {
 
 	}
 
 	public static void main(String[] args) {
 		Date start = new Date();
 
 		if (args.length < 2) {
 			System.out
 					.println("Usage: program <input> <clusters> [max iteration]");
 			System.exit(0);
 		}
 
 		Configuration conf = new Configuration();
 
 		Counter converge = null;
 		Counter total = null;
 		Counter totalfile = null;
 		int maxIteraion = 50;
 		if ((args.length == 3) && (args[2] != null) && (!args[2].isEmpty()))
 			maxIteraion = Integer.parseInt(args[2]);
 		else
 			maxIteraion = 50;
 		System.out.println("Max Iteration is " + maxIteraion);
 		Path in = new Path(args[0]);
 		Path out;
 		int iterCounter = 0;
 		conf.setFloat(Constants.THRESHOLD, 0.00001f);
 		try {
 			do {
 				if (iterCounter == 0)
 					conf.set(Constants.CLUSTER_PATH, args[1]);
 				else
 					// load the output of last iteration
 					conf.set(Constants.CLUSTER_PATH, args[1] + ".part"
 							+ (iterCounter - 1) + "/part-r-00000");
 
 				Job job = new Job(conf);
 				job.setNumReduceTasks(Constants.REDUCERAMOUNT);
 				job.setJobName("K-means clustering");
 				job.setJarByClass(KmeansDriver.class);
 				job.setMapperClass(KmeansMapper.class);
 				job.setCombinerClass(KmeansCombiner.class);
 				job.setReducerClass(KmeansReducer.class);
 
 				job.setOutputKeyClass(LongWritable.class);
 				job.setOutputValueClass(KmeansCluster.class);
 				job.setMapOutputKeyClass(LongWritable.class);
 				job.setMapOutputValueClass(KmeansCluster.class);
 
 				job.setInputFormatClass(TextInputFormat.class);
 				job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
 				out = new Path(args[1] + ".part" + iterCounter);
 				FileInputFormat.addInputPath(job, in);
 				SequenceFileOutputFormat.setOutputPath(job, out);
 
 				job.waitForCompletion(true);
 
 				converge = job.getCounters().getGroup(Constants.COUNTER_GROUP)
 						.findCounter(Constants.COUNTER_CONVERGED);
 				total = job.getCounters().getGroup(Constants.COUNTER_GROUP)
 						.findCounter(Constants.COUNTER_TOTAL);
 				totalfile = job.getCounters().getGroup(Constants.COUNTER_GROUP)
 						.findCounter(Constants.COUNTER_FILE);
 				System.out
 						.println("CONVERGED: " + converge.getValue()
 								+ "\tTotal: " + total.getValue()
 								/ totalfile.getValue());
 				iterCounter++;
 			} while ((converge.getValue() < total.getValue()
 					/ totalfile.getValue())
					&& (iterCounter < maxIteraion));
 
 			conf.set(Constants.CLUSTER_PATH, args[1] + ".part"
 					+ (iterCounter - 1) + "/part-r-00000");
 			Job job = new Job(conf);
 			job.setNumReduceTasks(0);
 			job.setJobName("K-means clustering");
 			job.setJarByClass(KmeansDriver.class);
 			job.setMapperClass(KmeansClusterMapper.class);
 
 			job.setOutputKeyClass(Text.class);
 			job.setOutputValueClass(Text.class);
 			job.setMapOutputKeyClass(Text.class);
 			job.setMapOutputValueClass(Text.class);
 
 			out = new Path(args[1] + ".final");
 			FileInputFormat.addInputPath(job, in);
 			FileOutputFormat.setOutputPath(job, out);
 
 			job.waitForCompletion(true);
 		} catch (Exception e) {
 			// TODO:
 			// a better error report routine
 			e.printStackTrace();
 		}
 		Date finish = new Date();
 		System.out.println("All clusters converged. k-means finishs.");
 		System.out.println("It takes " + (finish.getTime() - start.getTime())
 				/ 1000.0 + "s to accomplish clustering.");
 	}
 }
