 package workloadgen.loadjobs;
 
 import java.util.Random;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.GenericMRLoadGenerator;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.SequenceFileOutputFormat;
 import org.apache.hadoop.mapred.lib.LongSumReducer;
 import org.apache.hadoop.mapred.lib.RegexMapper;
 
 import workloadgen.WorkloadRunner;
 
 /**
  * create a concrete job 
  *
  */
 public class LoadJobCreator extends GenericMRLoadGenerator{
 	private static Configuration config = initConfig();
 	private static FileSystem fs = initFs(config);
 	
 	public LoadJobCreator(){
 	
 	}
 	
 	private JobConf setupGrep(String indir, String outdir, int numReducers, String regPattern) 
 			throws Exception{
 		JobConf grepJob = new JobConf();
 		grepJob.setJobName("grep-search");
		Path tempDir = new Path("grep-temp-"
 				+ Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
 		FileInputFormat.setInputPaths(grepJob, indir);
 
 		grepJob.setMapperClass(RegexMapper.class);
 		grepJob.set("mapred.mapper.regex", regPattern);
 
 		grepJob.setCombinerClass(LongSumReducer.class);
 		grepJob.setReducerClass(LongSumReducer.class);
 		grepJob.setNumReduceTasks(numReducers);
 
		FileOutputFormat.setOutputPath(grepJob, tempDir);
 		grepJob.setOutputFormat(SequenceFileOutputFormat.class);
 		grepJob.setOutputKeyClass(Text.class);
 		grepJob.setOutputValueClass(LongWritable.class);
		clearDir(outdir);
 		return grepJob;
 	}
 	
 	public LoadJob createGrep(String indir, String outdir, int numReducers, 
 			String regPattern,
 			int timestamp) throws Exception{
 		JobConf grepjob = this.setupGrep(indir, outdir, numReducers, regPattern);
 		return new LoadJob(grepjob, timestamp);
 	}
 	
 	private static Configuration initConfig(){
 		Configuration conf = new Configuration();
 		try {
 			Path fileResource = new Path(WorkloadRunner.confPath);
 			conf.addResource(fileResource);
 		} catch (Exception e) {
 			System.err.println("Error reading config file " + WorkloadRunner.confPath 
 					+ ":"
 					+ e.getMessage());
 			return null;
 		}
 		return conf;
 	}
 	
 	private static FileSystem initFs(Configuration conf){
 		try{
 			return FileSystem.get(conf);
 		}
 		catch(Exception e){
 			System.out.println("init file system error");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private static void clearDir(String dir){
 		try{
 			Path outfile = new Path(dir);
 			System.out.println("deleting:" + outfile);
 			fs.delete(outfile, true);
 			if (!fs.exists(outfile)){
 				System.out.println("Successfully Deleted");
 			}
 		}
 		catch(Exception e){
 			System.out.println("delete file error");
 			e.printStackTrace();
 			
 		}
 	}
 	
 }
