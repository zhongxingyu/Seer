 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Counter;
 import org.apache.hadoop.mapreduce.Counters;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth2;
 import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
 import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
 
 public class MR2PhaseFPGrowth {
 
 	// hadoop params
 	private static final String PHASE1MINSUP_CONFIG = Commons.PROGNAME
 			+ ".phase1.minsup";
 	private static final String MINSUP_CONFIG = Commons.PROGNAME + ".minsup";
 	private static final String TOTALROW_CONFIG = Commons.PROGNAME
 			+ ".totalrow";
 	private static final String PHASE1OUTDIR_CONFIG = Commons.PROGNAME
 			+ ".phase1.outdir";
 	private static String cachefilesuffix = "-cachecount.file";
 	private static long totalrows;
 
 	private static enum RowCounter {
 		TOTALROW
 	}
 
 	private static final double DISABLECACHE = 200;
 
 	public static void run_on_hadoop_phase1() throws IOException,
 			ClassNotFoundException, InterruptedException {
 		Configuration conf = new Configuration();
 		conf.set(PHASE1MINSUP_CONFIG, Double.toString(phase1minsup));
 		conf.set(MINSUP_CONFIG, Double.toString(minsup));
 		conf.set(PHASE1OUTDIR_CONFIG, outputpath1stphase);
 
 		String jobname = Commons.PROGNAME + " Phase 1";
 		Job job = new Job(conf, jobname);
 		job.setJarByClass(MR2PhaseFPGrowth.class);
 		job.setInputFormatClass(WholeFileInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		job.setMapperClass(Phase1Mapper.class);
 		job.setReducerClass(Phase1Reducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 		FileInputFormat.addInputPath(job, new Path(inputpath));
 		FileOutputFormat.setOutputPath(job, new Path(outputpath1stphase));
 
 		int retval = job.waitForCompletion(true) ? 0 : 1;
 		if (retval != 0) {
 			System.err.println(Commons.PREFIX + "Phase 1 Error, exit");
 			System.exit(retval);
 		}
 
 		Counters counters = job.getCounters();
 		Counter counter = counters.findCounter(RowCounter.TOTALROW);
 		totalrows = counter.getValue();
 
 		System.err.println(Commons.PREFIX + "Total rows: " + totalrows);
 	}
 
 	private static final String SPLIT_NUM_ROWS = "split_num_rows.key";
 
 	public static class Phase1Mapper extends
 			Mapper<NullWritable, BytesWritable, Text, IntWritable> {
 		@Override
 		public void map(NullWritable key, BytesWritable value, Context context)
 				throws IOException, InterruptedException {
 			// byteswritable -> ArrayList<String>
 			String realstr = new String(value.getBytes());
 			String[] rows = realstr.split("\\n");
 			ArrayList<String> dataset = new ArrayList<String>();
 			String thisrow = null;
 			for (int i = 0; i < rows.length; i++) {
 				thisrow = rows[i].trim();
 				if (thisrow.length() != 0) {
 					dataset.add(thisrow);
 				}
 			}
 			int numrows = dataset.size();
 
 			// use fp-growth alg
 			AlgoFPGrowth2 localalgm = new AlgoFPGrowth2();
 			// Itemsets itemsets = localalgm.runAlgorithm2(dataset, null,
 			// minsup / 100.0);
 
 			// use cache version algorithm
 			localalgm.runAlgorithm3(dataset, minsup / 100.0,
 					phase1minsup / 100.0);
 
 			// get two range itemsets
 			Itemsets higher = localalgm.getHigher();
 			Itemsets lower = localalgm.getLower();
 
 			System.err.println("higher size: " + higher.getItemsetsCount());
 			System.err.println("lower size: " + lower.getItemsetsCount());
 
 			int[] items = null;
 			StringBuilder sb = new StringBuilder();
 			if (phase1minsup <= minsup) {
 				// output intermediate data to reduce phase
 				for (List<Itemset> itemsetlist : higher.getLevels()) {
 					for (Itemset itemset : itemsetlist) {
 						items = itemset.getItems();
 						sb.setLength(0);
 						for (int i = 0; i < items.length; i++) {
 							if (i == 0) {
 								sb.append(items[i]);
 							} else {
 								sb.append(Commons.SEPARATOR);
 								sb.append(items[i]);
 							}
 						}
 						context.write(new Text(sb.toString()), new IntWritable(
 								itemset.getAbsoluteSupport()));
 					}
 				}
 
 				if (cacheEnabled(phase1minsup)) {
 					// output cache to hdfs
 					for (List<Itemset> itemsetlist : higher.getLevels()) {
 						for (Itemset itemset : itemsetlist) {
 							items = itemset.getItems();
 							sb.setLength(0);
 							for (int i = 0; i < items.length; i++) {
 								if (i == 0) {
 									sb.append(items[i]);
 								} else {
 									sb.append(Commons.SEPARATOR);
 									sb.append(items[i]);
 								}
 							}
 							bw.write(sb.toString() + " "
 									+ itemset.getAbsoluteSupport() + "\n");
 						}
 					}
 					for (List<Itemset> itemsetlist : lower.getLevels()) {
 						for (Itemset itemset : itemsetlist) {
 							items = itemset.getItems();
 							sb.setLength(0);
 							for (int i = 0; i < items.length; i++) {
 								if (i == 0) {
 									sb.append(items[i]);
 								} else {
 									sb.append(Commons.SEPARATOR);
 									sb.append(items[i]);
 								}
 							}
 							bw.write(sb.toString() + " "
 									+ itemset.getAbsoluteSupport() + "\n");
 						}
 					} // end of for
 				} // end of if
 
 			} else {
 				// phase1minsup > minsup, including cachedisabled
 				// output intermediate data to reduce phase
 				for (List<Itemset> itemsetlist : higher.getLevels()) {
 					for (Itemset itemset : itemsetlist) {
 						items = itemset.getItems();
 						sb.setLength(0);
 						for (int i = 0; i < items.length; i++) {
 							if (i == 0) {
 								sb.append(items[i]);
 							} else {
 								sb.append(Commons.SEPARATOR);
 								sb.append(items[i]);
 							}
 						}
 						context.write(new Text(sb.toString()), new IntWritable(
 								itemset.getAbsoluteSupport()));
 					}
 				}
 				for (List<Itemset> itemsetlist : lower.getLevels()) {
 					for (Itemset itemset : itemsetlist) {
 						items = itemset.getItems();
 						sb.setLength(0);
 						for (int i = 0; i < items.length; i++) {
 							if (i == 0) {
 								sb.append(items[i]);
 							} else {
 								sb.append(Commons.SEPARATOR);
 								sb.append(items[i]);
 							}
 						}
 						context.write(new Text(sb.toString()), new IntWritable(
 								itemset.getAbsoluteSupport()));
 					}
 				}
 
 				if (cacheEnabled(phase1minsup)) {
 					// output cache to hdfs
 					for (List<Itemset> itemsetlist : higher.getLevels()) {
 						for (Itemset itemset : itemsetlist) {
 							items = itemset.getItems();
 							sb.setLength(0);
 							for (int i = 0; i < items.length; i++) {
 								if (i == 0) {
 									sb.append(items[i]);
 								} else {
 									sb.append(Commons.SEPARATOR);
 									sb.append(items[i]);
 								}
 							}
 							bw.write(sb.toString() + " "
 									+ itemset.getAbsoluteSupport() + "\n");
 						}
 					} // end of for
 				} // end of if
 			} // end of else
 
 			context.write(new Text(SPLIT_NUM_ROWS), new IntWritable(numrows));
 		}
 
 		// download param
 		private double phase1minsup;
 		private double minsup;
 		private String output1stphasedir;
 
 		// bookkeeping
 		private long starttime, endtime;
 		private int taskid;
 
 		// cache
 		private BufferedWriter bw;
 
 		@Override
 		public void setup(Context context) throws IOException {
 			// my id
 			taskid = context.getTaskAttemptID().getTaskID().getId();
 
 			// download
 			phase1minsup = context.getConfiguration().getDouble(
 					PHASE1MINSUP_CONFIG, 100);
 			minsup = context.getConfiguration().getDouble(MINSUP_CONFIG, 100);
 			output1stphasedir = context.getConfiguration().get(
 					PHASE1OUTDIR_CONFIG);
 
 			// show params
 			System.err
 					.println(Commons.PREFIX + "phase1minsup: " + phase1minsup);
 			System.err.println(Commons.PREFIX + "minsup: " + minsup);
 			System.err.println(Commons.PREFIX + "cacheEnabled: "
 					+ cacheEnabled(phase1minsup));
 
 			// bookkeeping
 			starttime = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Map Task start time: " + starttime);
 
 			// open cache file for write
 			if (cacheEnabled(phase1minsup)) {
 				FileSystem fs = FileSystem.get(context.getConfiguration());
 				String splitname = ((FileSplit) context.getInputSplit())
 						.getPath().getName();
 				FSDataOutputStream out = fs.create(new Path(output1stphasedir
 						+ "/_" + splitname + cachefilesuffix));
 				bw = new BufferedWriter(new OutputStreamWriter(out));
 			}
 		}
 
 		@Override
 		public void cleanup(Context context) throws IOException {
 			// bookkeeping
 			endtime = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Map Task end time: " + endtime);
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Map Task execution time: " + (endtime - starttime));
 
 			// close cache stream
 			if (cacheEnabled(phase1minsup)) {
 				bw.close();
 			}
 		}
 	}
 
 	public static class Phase1Reducer extends
 			Reducer<Text, IntWritable, Text, IntWritable> {
 		@Override
 		public void reduce(Text key, Iterable<IntWritable> values,
 				Context context) throws IOException, InterruptedException {
 			if (key.toString().equals(SPLIT_NUM_ROWS)) {
 				// compute input total rows
 				int totalrows = 0;
 				for (IntWritable val : values) {
 					totalrows += val.get();
 				}
 				context.getCounter(RowCounter.TOTALROW).increment(totalrows);
 			} else {
 				context.write(key, one);
 			}
 		}
 
 		// bookkeeping
 		private long reducestart, reduceend;
 		private int taskid;
 
 		// save time
 		private IntWritable one = new IntWritable(1);
 
 		@Override
 		public void setup(Context context) {
 			// my id
 			taskid = context.getTaskAttemptID().getTaskID().getId();
 
 			// bookkeeping
 			reducestart = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Reduce Task start time: " + reducestart);
 		}
 
 		@Override
 		public void cleanup(Context context) {
 			// bookkeeping
 			reduceend = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Reduce Task end time: " + reduceend);
 			System.err.println(Commons.PREFIX + "(1/2) " + taskid
 					+ " Reduce Task execution time: "
 					+ (reduceend - reducestart));
 		}
 	}
 
 	public static void run_on_hadoop_phase2() throws IOException,
 			URISyntaxException, ClassNotFoundException, InterruptedException {
 		Configuration conf = new Configuration();
 		conf.set(PHASE1MINSUP_CONFIG, Double.toString(phase1minsup));
 		conf.set(MINSUP_CONFIG, Double.toString(minsup));
 		conf.set(TOTALROW_CONFIG, Long.toString(totalrows));
 		conf.set(PHASE1OUTDIR_CONFIG, outputpath1stphase);
 
 		String jobname = Commons.PROGNAME + " Phase 2";
 		Job job = new Job(conf, jobname);
 		job.setJarByClass(MR2PhaseFPGrowth.class);
 		job.setInputFormatClass(WholeFileInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		job.setMapperClass(Phase2Mapper.class);
 		job.setReducerClass(Phase2Reducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 		FileInputFormat.addInputPath(job, new Path(inputpath));
 		FileOutputFormat.setOutputPath(job, new Path(outputpath));
 
 		// add the global candidates into cache
 		FileSystem fs = FileSystem.get(conf);
 		FileStatus[] fss = fs.listStatus(new Path(outputpath1stphase));
 		int filenum = 0;
 		for (FileStatus status : fss) {
 			Path path = status.getPath();
 			if (path.getName().startsWith("_")) {
 				continue;
 			}
 			filenum++;
 			job.addCacheFile(new URI(path.toString()));
 		}
 
 		System.err.println(Commons.PREFIX + "Just added " + filenum
 				+ " files into distributed cache.");
 		int retval = job.waitForCompletion(true) ? 0 : 1;
 	}
 
 	public static class Phase2Mapper extends
 			Mapper<NullWritable, BytesWritable, Text, IntWritable> {
 		@Override
 		public void map(NullWritable key, BytesWritable value, Context context)
 				throws IOException, InterruptedException {
 			// bytesWritable -> ArrayList<String>
 			String realstr = new String(value.getBytes());
 			String[] rows = realstr.split("\\n");
 			ArrayList<String> dataset = new ArrayList<String>();
 			String thisrow = null;
 			for (int i = 0; i < rows.length; i++) {
 				thisrow = rows[i].trim();
 				if (thisrow.length() != 0) {
 					dataset.add(thisrow);
 				}
 			}
 
 			// parse this once
 			for (String line : dataset) {
 				HashSet<String> set = new HashSet<String>();
 				StringTokenizer st = new StringTokenizer(line,
 						Commons.DATASEPARATOR);
 				while (st.hasMoreTokens()) {
 					set.add(st.nextToken());
 				}
 				split.add(set);
 			}
 
 			// for each global candidate file, count each line
 			if (localFiles != null) {
 				for (int i = 0; i < localFiles.length; i++) {
 					localCount(localFiles[i], dataset, context);
 				}
 			}
 		}
 
 		private void localCount(File file, ArrayList<String> dataset,
 				Context context) throws IOException, InterruptedException {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line;
 			HashSet<String> myset = new HashSet<String>();
 			int count = 0;
 			while ((line = br.readLine()) != null) {
 				// \t comes from hadoop framework
 				String candidate = line.split("\\t")[0];
 				total++;
 				if (cache.containsKey(candidate)) {
 					hit++;
 					context.write(
 							new Text(candidate),
 							new IntWritable(Integer.parseInt(cache
 									.get(candidate))));
 					continue;
 				}
 				StringTokenizer st = new StringTokenizer(candidate,
 						Commons.SEPARATOR);
 				myset.clear();
 				count = 0;
 				while (st.hasMoreTokens()) {
 					myset.add(st.nextToken());
 				}
 				// for each candidate, go over the dataset
 				for (HashSet<String> set : split) {
 					if (set.containsAll(myset)) {
 						count++;
 					}
 				}
 				context.write(new Text(candidate), new IntWritable(count));
 			}
 			br.close();
 		}
 
 		// download params
 		private String output1stphasedir;
 		private double phase1minsup;
 
 		// for distributed cache, global candidate
 		private File[] localFiles;
 
 		// for cache, only parse value when needed
 		private long total, hit;
 		private HashMap<String, String> cache = new HashMap<String, String>();
 
 		// for parse once
 		private ArrayList<HashSet<String>> split = new ArrayList<HashSet<String>>();
 
 		// bookkeeping
 		private long starttime, endtime;
 		private int taskid;
 
 		@Override
 		public void setup(Context context) throws IOException {
 			// download params
 			output1stphasedir = context.getConfiguration().get(
 					PHASE1OUTDIR_CONFIG);
 			phase1minsup = context.getConfiguration().getDouble(
 					PHASE1MINSUP_CONFIG, DISABLECACHE);
 
 			// show params
 			System.err
 					.println(Commons.PREFIX + "phase1minsup: " + phase1minsup);
 			System.err.println(Commons.PREFIX + "output1stphasedir: "
 					+ output1stphasedir);
 			System.err.println(Commons.PREFIX + "cacheEnabled: "
 					+ cacheEnabled(phase1minsup));
 
 			// my id
 			taskid = context.getTaskAttemptID().getTaskID().getId();
 
 			// bookkeeping
 			starttime = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Map Task start time: " + starttime);
 
 			// for global candidate file
 			Path[] cacheFiles = context.getLocalCacheFiles();
 			String[] localFileNames;
 			if (cacheFiles != null) {
 				localFileNames = new String[cacheFiles.length];
 				localFiles = new File[cacheFiles.length];
 				for (int i = 0; i < cacheFiles.length; i++) {
 					localFileNames[i] = cacheFiles[i].toString();
 					localFiles[i] = new File(localFileNames[i]);
 				}
 			}
 
 			// for cache file
 			if (cacheEnabled(phase1minsup)) {
 				FileSystem fs = FileSystem.get(context.getConfiguration());
 				String splitname = ((FileSplit) context.getInputSplit())
 						.getPath().getName();
 				FSDataInputStream in = fs.open(new Path(output1stphasedir
 						+ "/_" + splitname + cachefilesuffix));
 				BufferedReader br = new BufferedReader(
 						new InputStreamReader(in));
 				String line = null;
 				while ((line = br.readLine()) != null) {
 					String[] two = line.split(" ");
 					cache.put(two[0], two[1]);
 				}
 				br.close();
 			}
 		}
 
 		@Override
 		public void cleanup(Context context) {
 			// bookkeeping
 			endtime = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Map Task end time: " + endtime);
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Map Task execution time: " + (endtime - starttime));
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Cache hit: " + hit + " / " + total + " = "
 					+ (total == 0 ? "N/A" : (hit / (double) total)));
 		}
 	}
 
 	public static class Phase2Reducer extends
 			Reducer<Text, IntWritable, Text, IntWritable> {
 		@Override
 		public void reduce(Text key, Iterable<IntWritable> values,
 				Context context) throws IOException, InterruptedException {
 			int sum = 0;
 			for (IntWritable val : values) {
 				sum += val.get();
 			}
 			if ((sum * 100) >= (minsup * totalrows)) {
 				context.write(key, new IntWritable(sum));
 			}
 		}
 
 		// download params
 		private double minsup;
 		private long totalrows;
 
 		// bookkeeping
 		private long reducestart, reduceend;
 		private int taskid;
 
 		@Override
 		public void setup(Context context) {
 			// download params
 			minsup = context.getConfiguration().getDouble(MINSUP_CONFIG, 100);
 			totalrows = context.getConfiguration().getLong(TOTALROW_CONFIG, 1);
 
 			// taskid
 			taskid = context.getTaskAttemptID().getTaskID().getId();
 
 			// show params
 			System.err.println(Commons.PREFIX + "minsup: " + minsup);
 			System.err.println(Commons.PREFIX + "totalrows: " + totalrows);
 
 			// bookkeeping
 			reducestart = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Reduce Task start time: " + reducestart);
 		}
 
 		@Override
 		public void cleanup(Context context) {
 			// bookkeeping
 			reduceend = System.currentTimeMillis();
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Reduce Task end time: " + reduceend);
 			System.err.println(Commons.PREFIX + "(2/2) " + taskid
 					+ " Reduce Task execution time: "
 					+ (reduceend - reducestart));
 		}
 	}
 
 	// don't use the stub program's phase1minsup
 	public static boolean cacheEnabled(double phase1minsup) {
 		return phase1minsup <= 100;
 	}
 
 	// cmd line params
 	private static String inputpath;
 	private static String outputpath;
 	private static double minsup;
 	private static double phase1minsup;
 	private static String outputpath1stphase;
 
 	public static void main(String[] args) throws IOException, ParseException,
 			ClassNotFoundException, InterruptedException, URISyntaxException {
 		// hadoop cmd parse
 		Configuration conf = new Configuration();
 		String[] otherArgs = new GenericOptionsParser(conf, args)
 				.getRemainingArgs();
 
 		// my cmd parse
 		// definition stage
 		Options options = buildOptions();
 
 		// parsing stage
 		CommandLineParser parser = new BasicParser();
 		CommandLine cmd = parser.parse(options, otherArgs);
 
 		// interrogation stage
 		inputpath = cmd.getOptionValue("inpath");
 		outputpath = cmd.getOptionValue("outpath");
 		minsup = Double.parseDouble(cmd.getOptionValue("minsupport"));
 		double beta = 1;
 		if (cmd.hasOption("phase1minsup") && cmd.hasOption("phase1minsupbeta")) {
 			// skip phase1minsup
 			System.err.println(Commons.PREFIX
 					+ "phase1minsup cmd param is skipped");
 			beta = Double.parseDouble(cmd.getOptionValue("phase1minsupbeta"));
 			phase1minsup = beta * minsup;
 		} else if (cmd.hasOption("phase1minsup")
 				&& !cmd.hasOption("phase1minsupbeta")) {
 			phase1minsup = Double.parseDouble(cmd
 					.getOptionValue("phase1minsup"));
 		} else if (!cmd.hasOption("phase1minsup")
 				&& cmd.hasOption("phase1minsupbeta")) {
 			beta = Double.parseDouble(cmd.getOptionValue("phase1minsupbeta"));
 			phase1minsup = beta * minsup;
 		} else {
 			phase1minsup = DISABLECACHE;
 		}
 
 		// verify stage, semantic check
 		// skip minsup's verification
 		if (phase1minsup > 100) {
 			phase1minsup = DISABLECACHE;
 			System.err.println(Commons.PREFIX
 					+ "Cache optimization is disalbed");
 		} else if (phase1minsup < 0) {
 			System.err.println(Commons.PREFIX + "phase1minsup is less than 0");
 			System.err.println(Commons.PREFIX + "phase1minsup set to 0");
 			phase1minsup = 0;
 		}
 		if (phase1minsup <= 100 && phase1minsup >= 0) {
 			System.err
 					.println(Commons.PREFIX + "Cache optimization is enabled");
 		}
 
 		// show stage
 		System.err.println(Commons.PREFIX + "inpath: " + inputpath);
 		System.err.println(Commons.PREFIX + "outpath: " + outputpath);
 		System.err.println(Commons.PREFIX + "minsupport: " + minsup);
 		System.err.println(Commons.PREFIX + "phase1minsup: " + phase1minsup);
 
 		// main logic
 		outputpath1stphase = outputpath + "-1stphase";
 
 		long starttime = System.currentTimeMillis();
 
 		long phase1starttime = System.currentTimeMillis();
 
 		run_on_hadoop_phase1();
 
 		long phase1endtime = System.currentTimeMillis();
 
 		long phase2starttime = System.currentTimeMillis();
 
 		run_on_hadoop_phase2();
 
 		long phase2endtime = System.currentTimeMillis();
 
 		long endtime = System.currentTimeMillis();
 
 		System.err.println(Commons.PREFIX + "Total execution time: "
 				+ (endtime - starttime));
 		System.err.println(Commons.PREFIX + "Phase 1 execution time: "
 				+ (phase1endtime - phase1starttime));
 		System.err.println(Commons.PREFIX + "Phase 2 execution time: "
 				+ (phase2endtime - phase2starttime));
 
 	}
 
 	@SuppressWarnings("static-access")
 	private static Options buildOptions() {
 		Options options = new Options();
 
 		options.addOption(OptionBuilder.withArgName("inpath").hasArg()
 				.isRequired().withDescription("hdfs input folder")
 				.create("inpath"));
 
 		options.addOption(OptionBuilder.withArgName("outpath").hasArg()
 				.isRequired().withDescription("hdfs ouput folder")
 				.create("outpath"));
 
 		options.addOption(OptionBuilder.withArgName("minsupport").hasArg()
 				.isRequired().withDescription("minimum support in percentage")
 				.create("minsupport"));
 
 		options.addOption(OptionBuilder.withArgName("phase1minsup").hasArg()
 				.withDescription("phase 1 minsupport in percentage")
 				.create("phase1minsup"));
 
 		options.addOption(OptionBuilder.withArgName("phase1minsupbeta")
 				.hasArg().withDescription("phase 1 minsup ratio")
				.create("phase2minsupbeta"));
 
 		return options;
 	}
 }
