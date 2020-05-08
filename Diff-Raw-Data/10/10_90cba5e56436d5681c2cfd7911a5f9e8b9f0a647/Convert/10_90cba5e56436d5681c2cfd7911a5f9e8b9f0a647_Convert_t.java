 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 import org.apache.mahout.clustering.classify.WeightedVectorWritable;
 import org.apache.mahout.math.NamedVector;
 
 import cern.colt.Arrays;
 import edu.umd.cloud9.io.map.HashMapWritable;
 import edu.umd.cloud9.io.pair.PairOfInts;
 import edu.umd.cloud9.io.triple.TripleOfInts;
 
 public class Convert extends Configured implements Tool {
   private static final Logger LOG = Logger.getLogger(Convert.class);
 
   private static class ConvertMapper extends Mapper<IntWritable, WeightedVectorWritable, IntWritable, TripleOfInts> {
 
     private final static IntWritable KEY = new IntWritable();
     private final static TripleOfInts VALUE = new TripleOfInts();
 
     @Override
     public void map(IntWritable key, WeightedVectorWritable value, Context context) throws IOException,
         InterruptedException {
 
       String[] str = ((NamedVector)value.getVector()).getName().split(" ");
 
       KEY.set(Integer.parseInt(str[0]));
       VALUE.set((int)(Double.parseDouble(str[1]) + 0.5), (int)(Double.parseDouble(str[2]) + 0.5), key.get());
       context.write(KEY, VALUE);
 
     }
   }
 
   // Reducer: sums up all the counts.
   private static class ConvertReducer extends Reducer<IntWritable, TripleOfInts, IntWritable, HashMapWritable<PairOfInts, IntWritable>> {
 
     // Reuse objects.
     private final static IntWritable KEY = new IntWritable();
     private final static HashMapWritable<PairOfInts, IntWritable> VALUE = new HashMapWritable<PairOfInts, IntWritable>();
     private final static PairOfInts FIRST = new PairOfInts();
     private final static IntWritable SECOND = new IntWritable();
 
     @Override
     public void setup(Context context) throws IOException {
       KEY.set(-1);
     }
 
     @Override
     public void reduce(IntWritable key, Iterable<TripleOfInts> values, Context context) throws IOException,
         InterruptedException {
 
       int docId = key.get();
       if (docId != KEY.get()) {
         if (KEY.get() != -1) {
           context.write(KEY, VALUE);
         }
         KEY.set(docId);
         VALUE.clear();
       }
 
       // there is only one value for each group
       Iterator<TripleOfInts> iter = values.iterator();
 
       while (iter.hasNext()) {
         TripleOfInts dat = iter.next();
        PairOfInts first = new PairOfInts(dat.getLeftElement(), dat.getMiddleElement());
        IntWritable second = new IntWritable(dat.getRightElement());
        VALUE.put(first, second);
       }
     }
     
     public void cleanup(Context context) throws IOException, InterruptedException {
       if (VALUE.size() > 0)
         context.write(KEY, VALUE);
     }
   }
 
   /**
    * Creates an instance of this tool.
    */
   public Convert() {
   }
 
   private static final String INPUT = "input";
   private static final String OUTPUT = "output";
   private static final String NUM_REDUCERS = "numReducers";
   private static final String FUNC = "code2lda";
 
   /**
    * Runs this tool.
    */
   @SuppressWarnings({ "static-access" })
   public int run(String[] args) throws Exception {
     Options options = new Options();
 
     options.addOption(OptionBuilder.withArgName("path").hasArg().withDescription("input path")
         .create(INPUT));
     options.addOption(OptionBuilder.withArgName("path").hasArg().withDescription("output path")
         .create(OUTPUT));
     options.addOption(OptionBuilder.withArgName("num").hasArg()
         .withDescription("number of reducers").create(NUM_REDUCERS));
     options.addOption(OptionBuilder.withArgName("arg").hasArg().withDescription("type")
         .create(FUNC));
 
     CommandLine cmdline;
     CommandLineParser parser = new GnuParser();
 
     try {
       cmdline = parser.parse(options, args);
     } catch (ParseException exp) {
       System.err.println("Error parsing command line: " + exp.getMessage());
       return -1;
     }
 
     if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
       System.out.println("args: " + Arrays.toString(args));
       HelpFormatter formatter = new HelpFormatter();
       formatter.setWidth(120);
       formatter.printHelp(this.getClass().getName(), options);
       ToolRunner.printGenericCommandUsage(System.out);
       return -1;
     }
 
     String inputPath = cmdline.getOptionValue(INPUT);
     String outputPath = cmdline.getOptionValue(OUTPUT);
     int reducerTasks = cmdline.hasOption(NUM_REDUCERS) ? Integer.parseInt(cmdline
         .getOptionValue(NUM_REDUCERS)) : 1;
     String func = cmdline.hasOption(FUNC) ? cmdline.getOptionValue(FUNC) : "";
 
     LOG.info("Tool: " + Convert.class.getSimpleName());
     LOG.info(" - input path: " + inputPath);
     LOG.info(" - output path: " + outputPath);
     LOG.info(" - number of reducers: " + reducerTasks);
     LOG.info(" - type: " + func);
 
     Configuration conf = getConf();
     Job job = Job.getInstance(conf);
     job.setJobName(Convert.class.getSimpleName());
     job.setJarByClass(Convert.class);
 
     job.setNumReduceTasks(reducerTasks);
 
     FileInputFormat.setInputPaths(job, new Path(inputPath));
     FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
     job.setInputFormatClass(SequenceFileInputFormat.class);
     job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
     job.setMapOutputKeyClass(IntWritable.class);
     job.setMapOutputValueClass(TripleOfInts.class);
 
     job.setOutputKeyClass(IntWritable.class);
     job.setOutputValueClass(HashMapWritable.class);
 
     job.setMapperClass(ConvertMapper.class);
     job.setReducerClass(ConvertReducer.class);
 
     // Delete the output directory if it exists already.
     Path outputDir = new Path(outputPath);
     FileSystem.get(conf).delete(outputDir, true);
 
     long startTime = System.currentTimeMillis();
     job.waitForCompletion(true);
     LOG.info("Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
 
     return 0;
   }
 
   /**
    * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
    */
   public static void main(String[] args) throws Exception {
     ToolRunner.run(new Convert(), args);
   }
}
