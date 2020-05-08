 package com.kxen.han.projection.pfpg;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Stopwatch;
 import com.google.common.collect.Maps;
 import com.kxen.han.projection.hadoop.writable.GraphLinksWritable;
 import com.kxen.han.projection.hadoop.writable.TransactionTree;
 import com.kxen.han.projection.hadoop.writable.TransactionWritable;
 
 
 /**
  * Parallel bipartite graph projection on Hadoop
  * Based on paper: http://infolab.stanford.edu/~echang/recsys08-69.pdf
  * 
  * @author Han JU
  *
  */
 public class ParallelProjection 
 extends Configured implements Tool {
 	
 	private static final Logger log = LoggerFactory.getLogger(ParallelProjection.class);
 	
 	/** cli interface set-up */
 	static public final String IN = "i";
 	static public final String OUT = "o";
 	static public final String TMP = "tmp";
 	static public final String GROUP = "g";
 	static public final String SUPP = "s";
 	static public final String RED = "r";
 	static public final String START = "startFrom";
 	static public final String FPG = "useObjectFPG";
 	
 	private static Options OPTIONS;
 	
 	static {
 		OPTIONS = new Options();
 		OPTIONS.addOption(IN, "input", true, "Input Path");
 		OPTIONS.addOption(OUT, "output", true, "Output Path");
 		OPTIONS.addOption(TMP, "tempDir", true, "Temp Directory");
 		OPTIONS.addOption(GROUP, "numGroup", true, "Number of Groups");
 		OPTIONS.addOption(SUPP, "minSupport", true, "Minimum Support Threshold");
 		OPTIONS.addOption(RED, "numReduce", true, 
 				"Number of reducers per machine for projection");
 		OPTIONS.addOption(START, true, "Input Path");
 		OPTIONS.addOption(FPG, false, "Input Path");
 	}
 	
 	/** constants */
 	public static final String F_LIST = "f-list";
 	public static final String MIN_SUPPORT = "minSupport";
 	public static final String NUM_GROUP = "numGroup";
 	public static final String MAX_PER_GROUP = "maxPerGroup";
 	public static final String PARALLEL_COUNTING = "parallel-counting";
 	public static final String PRE_PROCESSING = "pre-processing";
 	public static final String PARALLEL_PROJECTION = "parallel-projection";
 	public static final String FILE_PATTERN = "part-*";
 	public static final String PARALLEL_AGGREGATION = "parallel-aggregation";
 	
 	public static final int REDUCE_SLOT = 32;
 	public static final int MACHINE = 4;
	public static final int MEM = 16 * 1024 - 512;	// 512m for system use
 	
 	private ParallelProjection() {}
 	
 	/**
 	 * Run every step of parallel bipartite graph projection
 	 * 	- parallel counting
 	 * 	- single machine grouping
 	 * 	- parallel FP-Tree projection
 	 */
 	public static void runProjection(Configuration conf) 
 					throws IOException, InterruptedException, ClassNotFoundException {
 		// set-up common conf. for all jobs
		String input = conf.get(ParallelProjectionDriver.IN);
		String output = conf.get(ParallelProjectionDriver.OUT);
		String tmp = conf.get(ParallelProjectionDriver.TMP);
 		int minSupport = conf.getInt(MIN_SUPPORT, 2);
		int startFrom = conf.getInt(ParallelProjectionDriver.START, 1);
 
 		conf.set("mapred.output.compression.codec","org.apache.hadoop.io.compress.SnappyCodec");
 	    conf.set("mapred.map.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");
 		
 	    Stopwatch sw = new Stopwatch();
 		Stopwatch swAll = new Stopwatch();
 		
 		swAll.start();
 		
 		// step 0, pre-processing
 		// change pair representation to transaction
 		if (startFrom <= 1) {
 			sw.start();
 			startPreProcessing(input, tmp, conf); 
 			sw.stop();
 			log.info("Pre-processing finished, took {} ms ...", sw.elapsed(TimeUnit.MILLISECONDS));
 		}
 
 		// step 1, parallel counting
 		// output counts (singleton support) at {tmp}/{PRE_PROCESSING}
 		String transacInput = tmp+"/"+PRE_PROCESSING;
 		if (startFrom <= 2) {
 			sw.reset().start();
 			startParallelCounting(transacInput, tmp, conf); 
 			sw.stop();
 			log.info("Parallel counting finished, took {} ms ...", sw.elapsed(TimeUnit.MILLISECONDS));
 		}
 		
 		// step 2, grouping
 		// read counting results, generate F-list, group it into G-list
 		// single machine
 		if (startFrom <= 3) {
 			sw.reset().start();
 			generateFList(tmp, minSupport, conf);
 			sw.stop();
 			log.info("Generating F-list finished, took {} ms ...", sw.elapsed(TimeUnit.MILLISECONDS));
 		}
 	
 		// step 3, parallel FP-Tree
 		// build local FP-Tree and do partial projection
 		if (startFrom <= 4) {
 			sw.reset().start();
 			startParallelProjection(transacInput, output, conf);
 			sw.stop();
 			log.info("Parallel projection finished, took {} ms ...", sw.elapsed(TimeUnit.MILLISECONDS));
 		}
 
 		/*
 		 * not used for the moment
 
 		// step4, aggregation
 		// aggregate partial projection results
 		if (startFrom <= 5) {
 			sw.reset().start();
 			String partialResults = tmp+"/"+PARALLEL_PROJECTION;
 			startParallelAggregation(partialResults, output, conf); 
 			sw.stop();
 			log.info("Parallel aggregation finished, took {} ms ...,", sw.elapsed(TimeUnit.MILLISECONDS));
 		}
 		*/
 		
 		swAll.stop();
 		log.info("All finished, took {} ms ...", swAll.elapsed(TimeUnit.MILLISECONDS));
 	}
 	
 	/**
 	 * Delete a folder with its containing files on HDFS
 	 * @param toDelete
 	 */
 	public static void delete(Path toDelete, Configuration conf) throws IOException {
 		FileSystem.get(conf).delete(toDelete, true);
 	}
 	
 	/**
 	 * Start step 0, pre-processing 
 	 */
 	public static void startPreProcessing(
 			String input,
 			String tmp,
 			Configuration conf) throws IOException, InterruptedException, ClassNotFoundException {
 		conf.set("mapred.compress.map.output", "true");
 	    conf.set("mapred.output.compression.type", "BLOCK");
 
 		Job job = new Job(conf, "Pre processing with input:" + input);
 		job.setJarByClass(ParallelProjection.class);
 		
 		int numReduce = Integer.parseInt(conf.get(NUM_GROUP));
 		// job.setNumReduceTasks(Math.min(numReduce, REDUCE_SLOT));
 		job.setNumReduceTasks(500);
 		
 	    // setting input and output path
 	    FileInputFormat.addInputPath(job, new Path(input));
 	    Path output = new Path(tmp, PRE_PROCESSING);
 	    
 	    // delete existing tmp folder
 	    delete(output, conf);
 	    
 	    FileOutputFormat.setOutputPath(job, output);
 	    job.setInputFormatClass(TextInputFormat.class);
 	    job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	    
 	    job.setMapOutputKeyClass(Text.class);
 	    job.setMapOutputValueClass(Text.class);
 	    job.setOutputKeyClass(LongWritable.class);
 	    job.setOutputValueClass(TransactionWritable.class);
 	    job.setMapperClass(PreProcessingMapper.class);
 	    job.setReducerClass(PreProcessingReducer.class);
 	    
 	    if (!job.waitForCompletion(false)) {
 	    	throw new IllegalStateException("Job failed ...");
 	    }
 	}
 	
 	/**
 	 * Start step 1, parallel counting of items
 	 */
 	public static void startParallelCounting(
 			String input,
 			String tmp,
 			Configuration conf) throws IOException, InterruptedException, ClassNotFoundException {
 	    conf.set("mapred.compress.map.output", "true");
 	    conf.set("mapred.output.compression.type", "BLOCK");
 	    	    
 	    Job job = new Job(conf, "Parallel counting with input: " + input);
 	    job.setJarByClass(ParallelProjection.class); 	// what use ???
 
 	    // setting input and output path
 	    FileInputFormat.addInputPath(job, new Path(input));
 	    Path output = new Path(tmp, PARALLEL_COUNTING);
 	    
 	    // delete existing tmp folder
 	    delete(output, conf);
 	    
 	    FileOutputFormat.setOutputPath(job, output);
 	    job.setInputFormatClass(SequenceFileInputFormat.class);
 	    job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	    
 	    job.setOutputKeyClass(LongWritable.class);
 	    job.setOutputValueClass(LongWritable.class);
 	    job.setMapperClass(ParallelCountingMapper.class);
 	    job.setCombinerClass(ParallelCountingReducer.class);
 	    job.setReducerClass(ParallelCountingReducer.class);
 	    
 	    if (!job.waitForCompletion(false)) {
 	    	throw new IllegalStateException("Job failed ...");
 	    }
 	}
 	
 	/**
 	 * Start step 2, generate f-list and save to HDFS
 	 */
 	public static void generateFList(
 			String tmp,
 			int minSupport,
 			Configuration conf) throws IOException {
 		// read
 		FileSystem fs = FileSystem.get(conf);
 		LongWritable key = new LongWritable(); 
 		LongWritable value = new LongWritable(); 
 		Map<Long, Long> freq = Maps.newHashMap();
 		Path countResult = new Path(tmp+"/"+PARALLEL_COUNTING, FILE_PATTERN);
 		FileStatus[] status = fs.globStatus(countResult);
 		for (FileStatus s : status) {
 			SequenceFile.Reader reader = new SequenceFile.Reader(fs, s.getPath(), conf);
 			while (reader.next(key, value)) {
 				if (value.get() >= minSupport) {
 					freq.put(key.get(), value.get());
 				}
 			}
 			reader.close();
 		}
 		
 		// save to HDFS
 		Path fListPath = new Path(tmp, F_LIST); 
 		OutputStream out = fs.create(fListPath);
 		ObjectOutputStream oos = new ObjectOutputStream(out);
 		oos.writeObject(freq);
 		oos.close();
 		DistributedCache.addCacheFile(fListPath.toUri(), conf);		// add to dCache
 		
 	    // set param to control group size in MR jobs
 	    int numGroup = conf.getInt(NUM_GROUP, 100);
 	    int maxPerGroup = freq.size() / numGroup;
 	    if (freq.size() % numGroup != 0) {
 	      maxPerGroup++;
 	    }
 	    conf.set(MAX_PER_GROUP, Integer.toString(maxPerGroup));
 	}
 	
 	/**
 	 * Start step 3, parallel projection 
 	 */
 	public static void startParallelProjection(
 			String input,
 			String output,
 			Configuration conf) throws IOException, InterruptedException, ClassNotFoundException {
 		conf.set("mapred.compress.map.output", "true");
 		conf.set("mapred.output.compress", "false");
 	    conf.set("mapred.output.compression.type", "BLOCK");
 	    
 	    // result is big, so set replication to 1
 	    conf.set("dfs.replication", "1");
 	    // memeory issue, can't affort to have reduce and maps run in parallel
 	    conf.set("mapred.reduce.slowstart.completed.maps", "1.00");
 	    
 	    // TODO jvm reuse seems a bad idea for this job
 	    // jvm used for mapper still exist in reduce phase, occupy memory and do nothing
 	    conf.set("mapred.job.reuse.jvm.num.tasks", "1");
 	    
 	    // use more memory for shuffling, less disk spills
 	    conf.set("io.sort.factor", "50");
 	    conf.set("io.sort.mb", "500");
 	    
 	    // for write heavy jobs, no socket timeout
 	    // bug in hadoop 1.0.2, need to set a large number, 0 not working
 	    conf.set("dfs.socket.timeout", "99999999");
 	    conf.set("dfs.datanode.socket.write.timeout", "99999999");
 	    
 	    // reduce side shuffle can use more memory
 	    conf.set("mapred.job.shuffle.input.buffer.percent", "0.90");
 	    
 		// memory control
 		// TODO use task memory monitoring
 		int numReduce = Integer.parseInt(conf.get(RED));
 		String memArg = "-Xmx" + (MEM / numReduce) + "m";
 		log.info("Memory for each task is {} ...", memArg);
 	    conf.set("mapred.child.java.opts", memArg);
 
 	    Job job = new Job(conf, "Parallel projection with input: " + input);
 		job.setJarByClass(ParallelProjection.class);
 		
 		job.setNumReduceTasks(numReduce * MACHINE);
 	    
 	    // setting input and output path
 	    FileInputFormat.addInputPath(job, new Path(input));
 	    Path out = new Path(output, PARALLEL_PROJECTION);
 	    
 	    // delete existing tmp folder
 	    delete(out, conf);
 	    
 	    FileOutputFormat.setOutputPath(job, out);
 	    job.setInputFormatClass(SequenceFileInputFormat.class);
 	    // job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	    job.setOutputFormatClass(TextOutputFormat.class);
 	    
 	    job.setMapOutputKeyClass(IntWritable.class);
 	    job.setMapOutputValueClass(TransactionTree.class);
 	    job.setOutputKeyClass(NullWritable.class);
 	    job.setOutputValueClass(Text.class);
 	    
 	    // for inspecting mapper output size
 //	    job.setOutputKeyClass(IntWritable.class);
 //	    job.setOutputValueClass(TransactionTree.class);
 	    
 	    job.setMapperClass(ParallelProjectionMapper.class);
 	    // job.setCombinerClass(ParallelProjectionCombiner.class);
 	    
	    if (conf.getBoolean(ParallelProjectionDriver.FPG, false)) 
 	    	job.setReducerClass(ParallelProjectionReducer.class);	// FPG
 	    else 
 	    	job.setReducerClass(ParallelProjectionReducer2.class);	// FPG2
 	    
 	    if (!job.waitForCompletion(false)) {
 	    	throw new IllegalStateException("Job failed ...");
 	    }
 	}
 	
 	/**
 	 * Start step 4, aggregation of partial results
 	 * Not needed if we take all graph edges
 	 */
 	public static void startParallelAggregation(
 			String input,
 			String output,
 			Configuration conf) throws IOException, InterruptedException, ClassNotFoundException {
 		conf.set("mapred.compress.map.output", "true");
 		conf.set("mapred.output.compress", "false");
 	    conf.set("mapred.output.compression.type", "BLOCK");
 	    conf.set("mapred.tasktracker.reduce.tasks.maximum", "8");	// reset back
 	    conf.set("mapred.child.java.opts", "-Xmx2g");
 		
 		Job job = new Job(conf, "Parallel aggregation with input: " + input);
 		job.setJarByClass(ParallelProjection.class);
 		
 		job.setNumReduceTasks(REDUCE_SLOT);
 
 		// setting input and output path
 	    FileInputFormat.addInputPath(job, new Path(input));
 	    Path out = new Path(output, PARALLEL_AGGREGATION);
 	    
 	    // delete existing output folder
 	    delete(out, conf);
 	    
 	    FileOutputFormat.setOutputPath(job, out);
 	    job.setInputFormatClass(SequenceFileInputFormat.class);
 	    job.setOutputFormatClass(TextOutputFormat.class);
 	    
 	    job.setMapOutputKeyClass(IntWritable.class);
 	    job.setMapOutputValueClass(GraphLinksWritable.class);
 	    job.setOutputKeyClass(NullWritable.class);
 	    job.setOutputValueClass(Text.class);
 	    job.setReducerClass(ParallelAggregationReducer.class);
 	    
 	    if (!job.waitForCompletion(false)) {
 	    	throw new IllegalStateException("Job failed ...");
 	    }
 	}
 	
 	/**
 	 * Entry point for parallel bipartite graph projection
 	 */
 	public int run(String[] args) throws Exception {
 		CommandLineParser parser = new BasicParser();
 		CommandLine cmd = parser.parse(OPTIONS, args);
 		
 		Configuration conf = new Configuration();
 		
 		conf.set(NUM_GROUP, cmd.getOptionValue(GROUP));
 		conf.set(IN, cmd.getOptionValue(IN));
 		conf.set(OUT, cmd.getOptionValue(OUT));
 		conf.set(TMP, cmd.getOptionValue(TMP));
 		conf.set(MIN_SUPPORT, cmd.getOptionValue(SUPP, "2"));
 		conf.set(START, cmd.getOptionValue(START, "1"));
 		conf.set(RED, cmd.getOptionValue(RED, "4"));
 		conf.setBoolean(FPG, cmd.hasOption(FPG));
 		ParallelProjection.runProjection(conf);
 		return 0;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		ToolRunner.run(new ParallelProjection(), args);
 	}
 }
