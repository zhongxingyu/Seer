 package shannon;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 
 public class ShanCalc extends Configured implements Tool {
 	@Override
 	public int run(String[] args) throws Exception {
 		Job job = new Job();
 
		job.setJobName("ShanCalc");
 
 		// ~ Now where can we find them classes?
 		job.setJarByClass(ShanMap.class);
 
 		// ~ Set map/combine/reduce classes
 		job.setMapperClass(ShanMap.class);
 		job.setCombinerClass(DoubleSumReducer.class);
 		job.setReducerClass(DoubleSumReducer.class);
 
 		// ~ Define input/output types
 		job.setInputFormatClass(KeyValueTextInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(DoubleWritable.class);
 
 		// ~ Specify input/output
 		FileInputFormat.setInputPathFilter(job, SimpleInputFilter.class);
		SimpleInputFilter.setFilter("part");
		Path inputPath = new Path("/user/naward09/ngramcount-1segment-output/*");
		Path outputPath = new Path("/user/naward09/ngramcount-1segment-output_SHANNON/");
		//Path inputPath = new Path("/tmp/compressedMULTIOUTPUT/*");
		//Path outputPath = new Path("/tmp/outData_calcShanCOMPRESSEDIN/");
		
 		FileInputFormat.addInputPath(job, inputPath);
 		FileOutputFormat.setOutputPath(job, outputPath);
 
 		System.out.println("Output path is " + outputPath.toString());
 
 		boolean verbose = false;
 		return (job.waitForCompletion(verbose) ? 1 : 0);
 	}
 }
