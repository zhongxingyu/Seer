 package convolution.rchannel;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 /**
  * 
  * ConvolutionJob
  * 
  * This is the main job class of the Map Reduce job.
  * 
  * From here we wire together the Map, Reduce, Partition, and Writable classes.
  * 
  */
 
 public class ConvolutionJob extends Configured implements Tool {
 	static final String HDFS_KERNEL = "/neuro/lookup/morlet-2000.dat";
 
 	void cacheKernel(JobConf conf) throws IOException {
 		Path hdfsPath = new Path(HDFS_KERNEL);
 		DistributedCache.addCacheFile(hdfsPath.toUri(), conf);
 	}
 
 	static class MultiFileOutput extends MultipleSequenceFileOutputFormat<Text, Text> {
 		protected String getInputFileBasedOutputFileName(JobConf conf, String name) {
 			String fpath = conf.get("map.input.file");
 			String fname = new File(fpath).getName();
 
 			String ratnumber;
 			String sessiondate;
 			String channelid;
 			
 			int indexBegin = 0;
 			int indexEnd = fname.indexOf('-');
 
 			ratnumber = fname.substring(indexBegin, indexEnd);
 			indexBegin = indexEnd+1;
 			indexEnd = fname.indexOf('-', indexBegin);
 			sessiondate = fname.substring(indexBegin, indexEnd);
 			indexBegin = indexEnd+1;
 			indexEnd = fname.indexOf('-', indexBegin);
 			sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 			indexBegin = indexEnd+1;
 			indexEnd = fname.indexOf('-', indexBegin);
 			sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 			indexBegin = indexEnd+4;
 			indexEnd = fname.indexOf('.', indexBegin);
 			channelid = fname.substring(indexBegin, indexEnd);
 
 			return "rat=" + ratnumber +
 				"/dt=" + sessiondate +
 				"/channel=" + channelid + "/" +
 				ratnumber + "-" + sessiondate + "-" + channelid
 			;
 		}
 	}
 
 	public void CreateRats(JobConf conf) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("/neuro/script/hive/createrats.q"));		
 				
 		out.write("DROP TABLE rats;");
 		out.newLine();
 		out.write("DROP TABLE ratsaverage;");
 		out.newLine();
 		out.newLine();
 		out.write("CREATE EXTERNAL TABLE rats(time INT, frequency INT, convolution INT)");
 		out.newLine();
 		out.write("PARTITIONED BY(rat STRING, dt STRING, channel STRING)");
 		out.newLine();
 		out.write("ROW FORMAT DELIMITED FIELDS TERMINATED BY ','");
 		out.newLine();
 		out.write("STORED AS SEQUENCEFILE LOCATION '/neuro/output/rats';");
 		out.newLine();
 		out.newLine();
 		out.write("CREATE TABLE ratsaverage(time INT, frequency INT, convolution INT)");
 		out.newLine();
 		out.write("PARTITIONED BY(rat STRING, dt STRING, channel STRING)");
 		out.newLine();
 		out.write("LOCATION '/neuro/output/ratsaverage';");
 		out.newLine();
 		out.newLine();
 		out.flush();
 		out.close();
 
 	}
 
 	@Override
 	public int run(String[] args) throws Exception {
 
 		System.out.println("\n\nConvolutionJob\n");
 		JobConf conf = new JobConf(getConf(), ConvolutionJob.class);
 		conf.setJobName("ConvolutionJob");
 		
 		this.cacheKernel(conf);
 		this.CreateRats(conf);
 		conf.setMapperClass(ConvolutionMapper.class);
 		List<String> other_args = new ArrayList<String>();
 		for(int i=0; i < args.length; ++i) {
 			try {
 				if ("-m".equals(args[i])) {
 					conf.setNumMapTasks(Integer.parseInt(args[++i]));
 				} else if ("-r".equals(args[i])) {
 					conf.setNumReduceTasks(Integer.parseInt(args[++i]));
 				} else {
 					other_args.add(args[i]);
 				}
 			} catch (NumberFormatException except) {
 				System.out.println("ERROR: Integer expected instead of " + args[i]);
 				return printUsage();
 			} catch (ArrayIndexOutOfBoundsException except) {
 				System.out.println("ERROR: Required parameter missing from " + args[i-1]);
 				return printUsage();
 			}
 		}
 
 		// Make sure there are exactly 2 parameters left.
 		if (other_args.size() != 2) {
 			System.out.println("ERROR: Wrong number of parameters: " + other_args.size() + " instead of 2.");
 			return printUsage();
 		}
 
 		conf.setNumReduceTasks(0);
 		conf.setInputFormat(NonSplittableTextInputFormat.class);
 		conf.setOutputFormat(MultiFileOutput.class);
 		conf.setOutputKeyClass(NullWritable.class);
 		conf.setOutputValueClass(Text.class);
 		conf.setCompressMapOutput(true);
 		conf.set("mapred.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");
 		conf.set("mapred.output.compression.type", "BLOCK");
 
 		FileInputFormat.setInputPaths(conf, other_args.get(0));
 		FileOutputFormat.setOutputPath(conf, new Path(other_args.get(1)));
 		// FileOutputFormat.setCompressOutput(conf, true);
 
 		JobClient.runJob(conf);
 
 		return 0;
 	}
 
 	static int printUsage() {
 		System.out.println("ConvolutionJob [-m <maps>] [-r <reduces>] <input> <output>");
 		ToolRunner.printGenericCommandUsage(System.out);
 		return -1;
 	}
 
 	public static void main(String[] args) throws Exception {
 		int res = ToolRunner.run(new Configuration(), new ConvolutionJob(), args);
 		System.exit(res);
 
 	}
 
 }
