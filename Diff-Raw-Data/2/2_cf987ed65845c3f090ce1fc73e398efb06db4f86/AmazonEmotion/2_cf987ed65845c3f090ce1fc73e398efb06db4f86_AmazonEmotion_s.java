 package amazonEmotion;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.util.CoreMap;
 
 
 /**
  * This MapReduce job will get the title and score of each article.
  *
  */
 public class AmazonEmotion extends Configured implements Tool {
 	
 	public static String[] emotions = new String[]{"anger", "anticipation", "disgust", "fear", "joy", 
 		"sadness", "surprise", "trust", "noEmotion", "positive", "negative", "noSentiment"};
 	
 	/*public static HashSet<String> angerWords;			// 0
 	public static HashSet<String> anticipationWords;	// 1
 	public static HashSet<String> disgustWords;			// 2
 	public static HashSet<String> fearWords;			// 3
 	public static HashSet<String> joyWords;				// 4
 	public static HashSet<String> sadnessWords;			// 5
 	public static HashSet<String> surpriseWords;		// 6
 	public static HashSet<String> trustWords;			// 7
 	public static HashSet<String> noEmotionWords;		// 8
 
 	public static HashSet<String> positiveWords;		// 9
 	public static HashSet<String> negativeWords;		// 10
 	public static HashSet<String> noSentimentWords;		// 11
 														// 12 is the total identifiable word count
 	*/
 	/*public static class DoubleArrayWritable extends ArrayWritable
 	{
 	    public DoubleArrayWritable() {
 	        super(DoubleWritable.class);
 	    }
 	    public DoubleArrayWritable(DoubleWritable[] values) {
 	        super(DoubleWritable.class, values);
 	    }
 	}*/
 
 	
 	public static void main(String[] args) throws Exception {
 		
 		if (args.length != 2) {
        	System.err.println("Usage: programName <input> <output> <emotionsFile>");
         	System.exit(2);
         }
         
         //loadEmotions(args[2]);
 		
 		int result = ToolRunner.run(new Configuration(), new AmazonEmotion(), args);
 		System.exit(result);
 	}
 
 
 	@Override
 	public int run(String[] args) throws Exception {
 		Configuration conf = getConf();
 
         /*if (args.length != 3) {
         	System.err.println("Usage: " + getClass().getName() + " <input> <output> <emotionsFile>");
         	System.exit(2);
         }
         
         loadEmotions(args[2]);*/
 
         // Creating the MapReduce job (configuration) object
         Job job = new Job(conf);
         job.setJarByClass(getClass());
         job.setJobName(getClass().getName());
 
         // Tell the job which Mapper and Reducer to use (classes defined above)
         job.setMapperClass(ReviewMapper.class);
 		job.setReducerClass(AmazonReducer.class);
 
 		// This is what the Mapper will be outputting to the Reducer
 		job.setMapOutputKeyClass(DoubleWritable.class);
 		job.setMapOutputValueClass(DoubleArrayWritable.class);
 
 		// This is what the Reducer will be outputting
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(Text.class);
 
 		// Setting the input folder of the job 
 		FileInputFormat.addInputPath(job, new Path(args[0]));
 
 		// Preparing the output folder by first deleting it if it exists
         Path output = new Path(args[1]);
         FileSystem.get(conf).delete(output, true);
 	    FileOutputFormat.setOutputPath(job, output);
 	    
 	    AmazonMapper.configureJob(job);
 
 		return job.waitForCompletion(true) ? 0 : 1;
 	}
 
 
 
 }
