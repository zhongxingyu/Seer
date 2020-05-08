 package com.oboturov.ht.jobs;
 
 import com.oboturov.ht.Nuplet;
 import com.oboturov.ht.Tweet;
 import com.oboturov.ht.User;
 import com.oboturov.ht.etl.NupletCreator;
 import com.oboturov.ht.etl.TweetsReader;
 import com.oboturov.ht.etl.language_identification.LanguageIdentificationWithLangGuess;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.mapred.lib.ChainMapper;
 import org.apache.hadoop.mapred.lib.LongSumReducer;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import java.io.IOException;
 
 /**
  * @author aoboturov
  */
 public class LanguagesCounter extends Configured implements Tool {
 
     private static final String UNIDENTIFIED_LANGUAGE = "Unidentified";
 
     public static class LanguageProjectionMap extends MapReduceBase implements Mapper<User, Nuplet, Text, LongWritable> {
         @Override
         public void map(final User user, final Nuplet nuplet, final OutputCollector<Text, LongWritable> outputCollector, final Reporter reporter) throws IOException {
             if (null != nuplet.getLang()) {
                 outputCollector.collect(new Text(nuplet.getLang()), new LongWritable(1L));
             } else {
                 outputCollector.collect(new Text(UNIDENTIFIED_LANGUAGE), new LongWritable(1L));
             }
         }
     }
 
     @Override
     public int run(final String[] args) throws Exception {
         final Configuration config = getConf();
 
        final JobConf jobConf = new JobConf(config, LanguagesCounter.class);
         jobConf.setJobName("languages-count");
 
         jobConf.setOutputKeyClass(User.class);
         jobConf.setOutputValueClass(Nuplet.class);
 
         jobConf.setCombinerClass(LongSumReducer.class);
         jobConf.setReducerClass(LongSumReducer.class);
 
         jobConf.setInputFormat(TextInputFormat.class);
         jobConf.setOutputFormat(TextOutputFormat.class);
 
         // Extract tweets
         ChainMapper.addMapper(
                 jobConf,
                 TweetsReader.Map.class,
                 LongWritable.class,
                 Text.class,
                 LongWritable.class,
                 Tweet.class,
                 false,
                 new JobConf(false)
         );
         // Map tweets to user-tweet pair.
         ChainMapper.addMapper(
                 jobConf,
                 NupletCreator.Map.class,
                 LongWritable.class,
                 Tweet.class,
                 User.class,
                 Nuplet.class,
                 true,
                 new JobConf(false)
         );
         // Detect tweets language
         ChainMapper.addMapper(
                 jobConf,
                 LanguageIdentificationWithLangGuess.LanguageIdentificationMap.class,
                 User.class,
                 Nuplet.class,
                 User.class,
                 Nuplet.class,
                 true,
                 new JobConf(false)
         );
         // Project onto language
         ChainMapper.addMapper(
                 jobConf,
                 LanguageProjectionMap.class,
                 User.class,
                 Nuplet.class,
                 Text.class,
                 LongWritable.class,
                 true,
                 new JobConf(false)
         );
 
         FileInputFormat.setInputPaths(jobConf, new Path(args[args.length - 2]));
         FileOutputFormat.setOutputPath(jobConf, new Path(args[args.length - 1]));
 
         JobClient.runJob(jobConf);
 
         return 0;
     }
 
     public static void main(final String args[]) throws Exception {
         for (String arg: args) {
             System.out.println(arg);
         }
 
         // Let ToolRunner handle generic command-line options
         int res = ToolRunner.run(new LanguagesCounter(), args);
 
         System.exit(res);
     }
 }
