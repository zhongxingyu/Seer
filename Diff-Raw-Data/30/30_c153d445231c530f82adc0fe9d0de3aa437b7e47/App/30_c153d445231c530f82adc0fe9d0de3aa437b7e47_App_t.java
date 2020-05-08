 package IonCannon;
 
 import IonCannon.io.RedisOutputFormat;
 import IonCannon.mapreduce.mapping.SampleToUrlMapper;
 import IonCannon.mapreduce.mapping.SampleToUrlReducer;
 import IonCannon.mapreduce.sampling.SamplingMapper;
 import IonCannon.mapreduce.sampling.SamplingReducer;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 public class App extends Configured implements Tool
 {
     public int run( String[] args ) throws IOException, ClassNotFoundException, InterruptedException
     {
         Logger logger = LoggerFactory.getLogger(App.class);
 
         Path inputPath = new Path("Laser/sampling/input");
        Path outputPath = new Path("Laser/recommendation/input");
         Path urlMappingOutput = new Path("Laser/sampling/mappingoutput");
 
         Configuration conf = new Configuration();
         conf.set("mapred.job.tracker", "master:54311");
         conf.set("fs.default.name", "hdfs://master:54310/");
 
         //sampler configuration
        conf.set("strengthToLinkFactor", "10");
         conf.set("numberOfLinksPerCategory", "50");
 
 
         FileSystem fs = FileSystem.get(conf);
         fs.delete(outputPath, true);
         fs.delete(urlMappingOutput, true);
	fs.delete(inputPath,true);
 
         //configure gauss sampler
        conf.set("sampler_nUsers", "100"); //int
         conf.set("sampler_nTopics", "5"); //int
        conf.set("sampler_maxSpots", "1"); //int
        conf.set("sampler_defSigma", "0.5"); //float
        conf.set("sampler_minYVal", "0.5"); // float
         conf.set("sample.buffer.mb", "30");
 
         Path userConfigsPath = new Path(inputPath, "userConfigurations");
 
         //sample user configurations
         UserConfigurationSampler.sampleGaussConfigurations(conf, fs, userConfigsPath);
 
         //sample users
         Job job = new Job(conf);
 
         job.setMapperClass(SamplingMapper.class);
         job.setMapOutputKeyClass(IntWritable.class);
         job.setMapOutputValueClass(Text.class);
         job.setReducerClass(SamplingReducer.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.setInputPaths(job, inputPath);
         FileOutputFormat.setOutputPath(job, outputPath);
 
         job.setJarByClass(App.class);
 
         boolean success = job.waitForCompletion(true);
 
         if(!success) {
             logger.info("Sampling user failed.");
             return -1;
         }
 
        conf.set("redisHost","10.239.9.254");
 
         Job mapUrlsToItemIdsJob = new Job(conf);
 
         mapUrlsToItemIdsJob.setMapperClass(SampleToUrlMapper.class);
         mapUrlsToItemIdsJob.setMapOutputKeyClass(LongWritable.class);
         mapUrlsToItemIdsJob.setMapOutputValueClass(LongWritable.class);
         mapUrlsToItemIdsJob.setReducerClass(SampleToUrlReducer.class);
 
         mapUrlsToItemIdsJob.setInputFormatClass(TextInputFormat.class);
         mapUrlsToItemIdsJob.setOutputFormatClass(RedisOutputFormat.class);
 
         FileInputFormat.setInputPaths(mapUrlsToItemIdsJob, outputPath);
         FileOutputFormat.setOutputPath(mapUrlsToItemIdsJob, urlMappingOutput);
 
         mapUrlsToItemIdsJob.setJarByClass(App.class);
 
         success = mapUrlsToItemIdsJob.waitForCompletion(true);
 
         if(!success) {
             logger.info("Sampling user failed.");
             return -1;
         }
 
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
         ToolRunner.run(new App(), args);
     }
 
 }
