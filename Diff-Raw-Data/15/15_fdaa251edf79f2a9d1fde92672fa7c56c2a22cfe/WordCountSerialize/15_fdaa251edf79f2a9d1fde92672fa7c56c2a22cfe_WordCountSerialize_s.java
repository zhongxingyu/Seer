 package wordcount;
 
 /**
  * @author Vojin Jovanovic
  */
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.SequenceFileInputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import scala.Tuple2;
import scala.colleciton.distributed.hadoop.MetaPathFilter;
 
 /**
  * This is an example Hadoop Map/Reduce application.
  * It reads the text input files, breaks each line into words
  * and counts them. The output is a locally sorted list of words and the
  * count of how often they occurred.
  * <p/>
  * To run: bin/hadoop jar build/hadoop-examples.jar wordcount
  * [-m <i>maps</i>] [-r <i>reduces</i>] <i>in-dir</i> <i>out-dir</i>
  */
 public class WordCountSerialize extends Configured implements Tool {
 
     /**
      * Counts the words in each line.
      * For each line of input, break the line into words and emit them as
      * (<b>word</b>, <b>1</b>).
      */
     public static class MapClass extends MapReduceBase
             implements Mapper<NullWritable, BytesWritable, Text, IntWritable> {
 
         private final static IntWritable one = new IntWritable(1);
         private Text word = new Text();
 
         public void map(NullWritable key, BytesWritable value,
                         OutputCollector<Text, IntWritable> output,
                         Reporter reporter) throws IOException {
             ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes());
             ObjectInputStream ois = new ObjectInputStream(bais);
 
             String line = null;
             try {
                 line = ois.readObject().toString();
                 StringTokenizer itr = new StringTokenizer(line);
                 while (itr.hasMoreTokens()) {
                     word.set(itr.nextToken());
                     output.collect(word, one);
                 }
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
     }
 
     static int printUsage() {
         System.out.println("wordcount-serialize [-m <maps>] [-r <reduces>] <input> <output>");
         ToolRunner.printGenericCommandUsage(System.out);
         return -1;
     }
 
     /**
      * The main driver for word count map/reduce program.
      * Invoke this method to submit the map/reduce job.
      *
      * @throws IOException When there is communication problems with the
      *                     job tracker.
      */
     public int run(String[] args) throws Exception {
         JobConf conf = new JobConf(getConf(), WordCount.class);
         conf.setJobName("wordcount-serialize");
 
 
         conf.setMapperClass(MapClass.class);
         conf.setCombinerClass(Combine.class);
         conf.setReducerClass(Reduce.class);
         conf.setInputFormat(SequenceFileInputFormat.class);
         conf.setOutputFormat(SequenceFileOutputFormat.class);
 
         conf.setMapOutputKeyClass(Text.class);
         conf.setMapOutputValueClass(IntWritable.class);
         conf.setOutputKeyClass(NullWritable.class);
         conf.setOutputValueClass(BytesWritable.class);
 
         List<String> other_args = new ArrayList<String>();
         for (int i = 0; i < args.length; ++i) {
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
                 System.out.println("ERROR: Required parameter missing from " +
                         args[i - 1]);
                 return printUsage();
             }
         }
         // Make sure there are exactly 2 parameters left.
         if (other_args.size() != 2) {
             System.out.println("ERROR: Wrong number of parameters: " +
                     other_args.size() + " instead of 2.");
             return printUsage();
         }
 
 
         FileInputFormat.setInputPaths(conf, other_args.get(0));
        FileInputFormat.setInputPathFilter(conf, MetaPathFilter.class);
         FileOutputFormat.setOutputPath(conf, new Path(other_args.get(1)));
 
         JobClient.runJob(conf);
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
         int res = ToolRunner.run(new Configuration(), new WordCountSerialize(), args);
         System.exit(res);
     }
 
 
     /**
      * A reducer class that just emits the sum of the input values.
      */
     public static class Combine extends MapReduceBase
             implements Reducer<Text, IntWritable, Text, IntWritable> {
 
         public void reduce(Text key, Iterator<IntWritable> values,
                            OutputCollector<Text, IntWritable> output,
                            Reporter reporter) throws IOException {
             int sum = 0;
             while (values.hasNext()) {
                 sum += values.next().get();
             }
             output.collect(key, new IntWritable(sum));
         }
     }
 
     public static class Reduce extends MapReduceBase
             implements Reducer<Text, IntWritable, NullWritable, BytesWritable> {
 
         public void reduce(Text key, Iterator<IntWritable> values,
                            OutputCollector<NullWritable, BytesWritable> output,
                            Reporter reporter) throws IOException {
             int sum = 0;
             while (values.hasNext()) {
                 sum += values.next().get();
             }
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);
             oos.writeObject(new Tuple2<String, Integer>(key.toString(), sum));
             oos.flush();
 
             output.collect(NullWritable.get(), new BytesWritable(baos.toByteArray()));
         }
     }
 
 }
 
