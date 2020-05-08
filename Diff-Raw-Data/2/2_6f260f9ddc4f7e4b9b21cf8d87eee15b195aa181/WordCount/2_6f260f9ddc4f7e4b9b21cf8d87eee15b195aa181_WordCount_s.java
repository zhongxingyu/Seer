 import java.io.IOException;
 import java.io.File;
 import java.net.URI;
 import java.util.*;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.Configuration; 
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.mapred.lib.IdentityMapper;
 import org.apache.hadoop.mapred.lib.IdentityReducer;
 import org.apache.hadoop.mapred.Partitioner;
 import org.apache.hadoop.fs.PathFilter;
 import org.apache.hadoop.mapred.lib.InputSampler;
 import org.apache.hadoop.mapred.lib.TotalOrderPartitioner;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.io.RawComparator;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class WordCount {
  private static String tmp_path_string = "/tmp/tmp_sort_path";
   private static final Log LOG = LogFactory.getLog(WordCount.class);
 
   public static class ReverseTotalOrderPartitioner <K extends WritableComparable, V> extends TotalOrderPartitioner<K, V> {
   
     public ReverseTotalOrderPartitioner() { }
     @Override 
     public int getPartition(K key, V value, int numPartitions) {
         return numPartitions -1 - super.getPartition(key, value, numPartitions); 
     } 
   }
 
   /* This is used to randomly sample some of our values so we can 
    * get a rough estimate of the distribution. This is based on InputFormat.Sampler,
    * but can't actually extend that interface because it samples values instead of keys
    */
    public static class ValueSampler<K, V> {
    private double freq;
    private int maxSplitsSampled;
    private int numSamples;
 
     public ValueSampler(double freq, int numSamples, int maxSplitsSampled) {
       this.freq = freq;
       this.maxSplitsSampled = maxSplitsSampled;
       this.numSamples= numSamples;
     } 
 
     /*
      * Randomize the split order, then take the specified number of keys from
      * each split sampled, where each key is selected with the specified
      * probability and possibly replaced by a subsequently selected key when
      * the quota of keys from that split is satisfied.
      */
     public V[] getSample(InputFormat<K,V> inf, JobConf job) throws IOException {
       InputSplit[] splits = inf.getSplits(job, job.getNumMapTasks());
       ArrayList<V> samples = new ArrayList<V>(numSamples);
       int splitsToSample = Math.min(maxSplitsSampled, splits.length);
 
       Random r = new Random();
       long seed = r.nextLong();
       r.setSeed(seed);
       LOG.debug("seed: " + seed);
       // shuffle splits
       for (int i = 0; i < splits.length; ++i) {
         InputSplit tmp = splits[i];
         int j = r.nextInt(splits.length);
         splits[i] = splits[j];
         splits[j] = tmp;
       }
       // our target rate is in terms of the maximum number of sample splits,
       // but we accept the possibility of sampling additional splits to hit
       // the target sample keyset
       for (int i = 0; i < splitsToSample ||
                      (i < splits.length && samples.size() < numSamples); ++i) {
         RecordReader<K,V> reader = inf.getRecordReader(splits[i], job,
             Reporter.NULL);
         K key = reader.createKey();
         V value = reader.createValue();
         while (reader.next(key, value)) {
           if (r.nextDouble() <= freq) {
             if (samples.size() < numSamples) {
               samples.add(value);
             } else {
               // When exceeding the maximum number of samples, replace a
               // random element with this one, then adjust the frequency
               // to reflect the possibility of existing elements being
               // pushed out
               int ind = r.nextInt(numSamples);
               if (ind != numSamples) {
                 samples.set(ind, value);
               }
               freq *= (numSamples - 1) / (double) numSamples;
             }
             value = reader.createValue();
           }
         }
         reader.close();
       }
       return (V[])samples.toArray();
     }
   }
 
   /**
    * Write a partition file for the given job, where the values
    * in the partition file are the max value for each partition.
    */
   @SuppressWarnings("unchecked") // getInputFormat, getOutputKeyComparator
   public static <K, V> void writePartitionFile(JobConf job,
       ValueSampler<K,V> sampler) throws IOException {
     final InputFormat<K,V> inf = (InputFormat<K,V>) job.getInputFormat();
     int numPartitions = job.getNumReduceTasks();
     V[] samples = sampler.getSample(inf, job);
     LOG.info("Using " + samples.length + " samples");
     RawComparator<V> comparator =
       (RawComparator<V>) job.getOutputKeyComparator();
     Arrays.sort(samples, comparator);
     Path dst = new Path(ReverseTotalOrderPartitioner.getPartitionFile(job));
     FileSystem fs = dst.getFileSystem(job);
     if (fs.exists(dst)) {
       fs.delete(dst, false);
     }
     SequenceFile.Writer writer = SequenceFile.createWriter(fs, job, dst,
         job.getMapOutputKeyClass(), NullWritable.class);
     NullWritable nullValue = NullWritable.get();
     float stepSize = samples.length / (float) numPartitions;
     int last = -1;
     for(int i = 1; i < numPartitions; ++i) {
       int k = Math.round(stepSize * i);
       // TODO: check this condition?
       
       //Keep walking forward until we see something > than our last split point.
       while (last >= 0 && comparator.compare(samples[last], samples[k]) >= 0) {
         ++k;
       }
       LOG.info("Split with k" + k + " is " + samples[k].toString());
       if (last > 0) {
         LOG.info("last is " + last + " and has val " + samples[last].toString());
       }
       writer.append(samples[k], nullValue);
       last = k;
     }
     writer.close();
     
   }
 
 
     // This maps the text by counting how many times each word occurs. 
     // On each word, emits a <word, 1> pair
     public static class TokenizerMapper 
         extends MapReduceBase implements Mapper<Object, Text, Text, IntWritable>{
 
             private final static IntWritable one = new IntWritable(1);
             private Text word = new Text();
 
             public void map(Object key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter
                     ) throws IOException {
                 // Tokenize by lots of nonalphanumeric chars.
                 // If we did value.ToString.split() we could use a regex instead of hardcoded delims,
                 // but this would use a ton of space.
                 StringTokenizer itr = new StringTokenizer(value.toString(), " \n\t.,;:(){}[]`);");
                 while (itr.hasMoreTokens()) {
                     // Make sure framework knows we are making progress.
                     reporter.progress();
                     // We saw another instance of the enxt word
                     word.set(itr.nextToken());
                     output.collect(word, one);
                 }
             }
         }
 
     // Sums up the counts for each key.
     public static class IntSumReducer 
         extends MapReduceBase implements Reducer<Text,IntWritable,Text,IntWritable> {
             private IntWritable result = new IntWritable();
 
             public void reduce(Text key, Iterator<IntWritable> values, 
                     OutputCollector<Text, IntWritable> output, Reporter reporter
                     ) throws IOException{
                 int sum = 0;
                 while (values.hasNext()) {
                     IntWritable val = values.next();
                     reporter.progress();
                     sum += val.get();
                 }
                 result.set(sum);
                 output.collect(key, result);
             }
         }
 
     public static class SwitchMapper<K, V>                                                                                                                                                                                                                                                                                              
         extends MapReduceBase implements Mapper<K, V, V, K> {
 
             // Swap key and value order.
             public void map(K key, V val,
                     OutputCollector<V, K> output, Reporter reporter)
                 throws IOException {
                     output.collect(val, key);
                 }
         }
 
 
     public static class SwitchReducer<K, V>
         extends MapReduceBase implements Reducer<K, V, V, K> {
 
             // Swap key and value order.
             public void reduce(K key, Iterator<V> values,
                     OutputCollector<V, K> output, Reporter reporter)
                 throws IOException {
                     while (values.hasNext()) {
                         output.collect(values.next(), key);
                     }                                                                                                                                                                                                                                                                    
                 }
         }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration();
         String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
         Path tmp_path = new Path(tmp_path_string);
         if (otherArgs.length != 2) {
             System.err.println("Usage: wordcount <in> <out>");
             System.exit(2);
         }
 
         JobConf jobConf1 = new JobConf(conf, WordCount.class);
         jobConf1.setJobName("Word Count");
 
         jobConf1.setMapperClass(TokenizerMapper.class);        
         jobConf1.setReducerClass(IntSumReducer.class);
         jobConf1.setCombinerClass(IntSumReducer.class);
 
         JobClient client1 = new JobClient(jobConf1);
         jobConf1.setOutputFormat(SequenceFileOutputFormat.class);
         jobConf1.setOutputKeyClass(Text.class);
         jobConf1.setOutputValueClass(IntWritable.class);
         FileInputFormat.setInputPaths(jobConf1, otherArgs[0]);
         FileOutputFormat.setOutputPath(jobConf1, tmp_path);
 
         Date start_time1 = new Date();
         System.out.println("Job started: " + start_time1);
         JobClient.runJob(jobConf1);
         Date end_time1 = new Date();
         System.out.println("Job ended: " + end_time1);
         System.out.println("The job took " + 
                 (end_time1.getTime() - start_time1.getTime()) /1000 + " seconds.");
 
 
 
         /* Everything beneath here is for sorting the output.
          * if tmp_path is the output of the run above, and is a SequenceFile of Text keys
          * and IntWritable values, this run will sort them in decreasing order of value
          */
         Configuration newConf = new Configuration();
         new GenericOptionsParser(newConf, args);
         JobConf jobConf = new JobConf(newConf, WordCount.class);
         tmp_path.getFileSystem(newConf).deleteOnExit(tmp_path);
         jobConf.setJobName("Word Count Sorted");
 
         jobConf.setMapperClass(SwitchMapper.class);        
         jobConf.setReducerClass(SwitchReducer.class);
 
         JobClient client = new JobClient(jobConf);
         jobConf.setInputFormat(SequenceFileInputFormat.class);
         jobConf.setOutputKeyClass(IntWritable.class);
         jobConf.setOutputValueClass(Text.class);
         // Sort in reverse order.
         jobConf.setKeyFieldComparatorOptions("-r");
         
         FileInputFormat.addInputPath(jobConf, tmp_path);
         FileOutputFormat.setOutputPath(jobConf, new Path(otherArgs[1]));
 
         // Sample our input so that we can partition our input evenly among all our reducers.
         ValueSampler<Text, IntWritable> sampler = new ValueSampler<Text, IntWritable>(0.1,500,50);
         Path partitionFile = new Path(tmp_path, "_sortPartitioning");
         // Make sure a global order is maintained across reducers.
         ReverseTotalOrderPartitioner.setPartitionFile(jobConf, partitionFile);
         writePartitionFile(jobConf, sampler);
         jobConf.setPartitionerClass(ReverseTotalOrderPartitioner.class);
         // Make sure all reducers can see the artition file.
         URI partitionUri = new URI(partitionFile.toString() +
             "#" + "_sortPartitioning");
         DistributedCache.addCacheFile(partitionUri, jobConf);
         DistributedCache.createSymlink(jobConf);
 
         Date startTime = new Date();
         System.out.println("Job started: " + startTime);
         JobClient.runJob(jobConf);
         Date end_time = new Date();
         System.out.println("Job ended: " + end_time);
         System.out.println("The job took " + 
                 (end_time.getTime() - startTime.getTime()) /1000 + " seconds.");
                 
 
     }
 }
