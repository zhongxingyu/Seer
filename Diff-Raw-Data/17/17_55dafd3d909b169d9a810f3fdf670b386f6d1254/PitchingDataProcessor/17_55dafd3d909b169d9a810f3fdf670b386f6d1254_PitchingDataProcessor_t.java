 package com.hadooptraining.lab15;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PitchingDataProcessor extends Configured implements Tool {
 
     public static class PitchingDataProcessorMap extends
             Mapper<LongWritable, Text, Text, PitchingWritable> {
 
         private Text playerIDText = new Text();
         private PitchingWritable pitchingValue = new PitchingWritable();
 
         public void map(LongWritable key, Text value, Context context)
                 throws IOException, InterruptedException {
 
             // Format: playerID,yearID,stint,teamID,lgID,W,L,G,GS,CG,SHO,SV,IPouts,H,ER,HR,BB,SO,BAOpp,ERA,IBB,WP,HBP,BK,BFP,GF,R,SH,SF,GIDP
             // Example: aardsda01,2010,1,SEA,AL,0,6,53,0,0,0,31,149,33,19,5,25,49,,3.44,5,2,2,0,202,43,19,,,
            String entryPattern = "^(\\S+),(\\d{4}),(\\d+),(\\S+),(\\S+),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(?:[1-9]\\d*|0)?(?:\\.\\d+)?,(?:[1-9]\\d*|0)?(?:\\.\\d+)?,(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*),(\\d*)";
 
             Pattern p = Pattern.compile(entryPattern);
             Matcher matcher = p.matcher(value.toString());
             if (!matcher.matches()) {
                 System.err.println("Bad record: " + value.toString());
                 return;
             }
 
            if ((matcher.group(27) == null ) || matcher.group(27).isEmpty())
                return;

             pitchingValue.set(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(27)));
             playerIDText.set(matcher.group(1));
 
             context.write(playerIDText, pitchingValue);
         }
     }
 
     /*
     An alternate and simpler way to parse each of line of Pitching.csv file
 
     public static class PitchingDataProcessorMap extends
             Mapper<LongWritable, Text, Text, PitchingWritable> {
 
         private Text playerIDText = new Text();
         private PitchingWritable pitchingValue = new PitchingWritable();
 
         public void map(Text key, PitchingWritable value, Context context)
                 throws IOException, InterruptedException {
 
             // Format of each line is as follows:
             // playerID,yearID,stint,teamID,lgID,W,L,G,GS,CG,SHO,SV,IPouts,H,ER,HR,BB,SO,BAOpp,ERA,IBB,WP,HBP,BK,BFP,GF,R,SH,SF,GIDP
             String[] pitchingValues = value.toString().split(",");
 
             pitchingValue.set(pitchingValues[0], pitchingValues[1], Integer.parseInt(pitchingValues[26]));
             System.out.println("ID:" + pitchingValue.getPlayerID().toString() + " Year:" + pitchingValue.getYear() + " Runs: " + pitchingValue.getRuns());
             playerIDText.set(pitchingValues[0]);
 
             context.write(playerIDText, pitchingValue);
         }
     }
     */
 
     public static class PitchingDataProcessorReduce extends
             Reducer<Text, PitchingWritable, Text, IntWritable> {
         private IntWritable result = new IntWritable();
 
         public void reduce(Text key, Iterable<PitchingWritable> values, Context context)
                 throws IOException, InterruptedException {
 
             int sum = 0;
             for (PitchingWritable line : values) {
                 sum += line.getRunsAllowed().get();
             }
             result.set(sum);
             context.write(key, result);
         }
     }
 
     @Override
     public int run(String[] args) throws Exception {
         if (args.length < 2) {
             System.err.println("Usage: <input_path> <output_path>");
             System.exit(-1);
         }
 
 		/* input parameters */
         String inputPath = args[0];
         String outputPath = args[1];
 
         Job job = new Job(getConf(), "pitching-analysis");
 
         job.setJarByClass(PitchingDataProcessor.class);
 
         job.setMapperClass(PitchingDataProcessorMap.class);
         job.setReducerClass(PitchingDataProcessorReduce.class);
 
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(PitchingWritable.class);
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(IntWritable.class);
 
         FileInputFormat.setInputPaths(job, new Path(inputPath));
         FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
         return job.waitForCompletion(true) ? 0 : 1;
     }
 
     public static void main(String[] args) throws Exception {
         int res = ToolRunner.run(new Configuration(), new PitchingDataProcessor(), args);
         System.exit(res);
     }
 
 }
 
