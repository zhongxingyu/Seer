 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
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
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 
 /* Dachuan Huang, June 2014 */
 /* hdc1112@gmail.com */
 /* This program is one MapReduce implementation of Apriori algorithm */
 /* It implements the algorithm from the link below */
 /* http://www.ijric.org/volumes/Vo12/Vol12No7.pdf */
 
 /* This program is tested in Hadoop 2.2.0 */
 /* Input files are put in the input folder */
 /* Each input file will be treated as one FileSplit */
 /* Each input file's format is a boolean matrix */
 /* Row means transaction, column means item */
 /* For example */
 /* 1 0 1 1 1 1 0 */
 /* 0 0 1 1 0 0 0 */
 /* 1 1 1 0 1 0 1 */
 /* 1 1 0 1 0 1 1 */
 
 /* 1st Phase's output records the global candidates */
 /* 2nd Phase's output is the frequent itemset */
 
 public class MR2PhaseApriori {
 
 	private static String PREFIX = "[MR2PhaseApriori] ";
 
 	private static String inputpath;
 	private static String outputpath1stphase;
 	private static String outputpath;
 
 	private static int items;
 	private static int minsupport;
 
 	private static String ITEMS_CONFIG = "MR2PhaseApriori.items.value";
 	private static String MINSUPPORT_CONFIG = "MR2PhaseApriori.minsupport.value";
 
 	private static String SPLIT_NUM_ROWS = "split_num_rows.key";
 
 	private static enum TOTALROWCOUNTER {
 		TOTALROW
 	}
 
 	private static String TOTAL_ROW_CONFIG = "MR2PhaseApriori.totalrow.value";
 	private static long totalrows;
 
 	/* for phase 1 */
 
 	public static void run_on_hadoop_phase1() throws IllegalArgumentException,
 			IOException, ClassNotFoundException, InterruptedException {
 		Configuration conf = new Configuration();
 
 		conf.set(ITEMS_CONFIG, Integer.toString(items));
 		conf.set(MINSUPPORT_CONFIG, Integer.toString(minsupport));
 
 		String jobname = "MapReduce 2Phase Apriori, Phase 1";
 		Job job = new Job(conf, jobname);
 		job.setJarByClass(MR2PhaseApriori.class);
 		job.setInputFormatClass(WholeFileInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		job.setMapperClass(FirstPhaseMapper.class);
 		job.setCombinerClass(FirstPhaseReducer.class);
 		job.setReducerClass(FirstPhaseReducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 		FileInputFormat.addInputPath(job, new Path(inputpath));
 		FileOutputFormat.setOutputPath(job, new Path(outputpath1stphase));
 
 		int retval = job.waitForCompletion(true) ? 0 : 1;
 		if (retval != 0) {
 			System.exit(retval);
 		}
 
 		Counters counters = job.getCounters();
 		Counter c = counters.findCounter(TOTALROWCOUNTER.TOTALROW);
 		totalrows = c.getValue();
 
 		System.err.println(PREFIX + "Total rows: " + totalrows);
 	}
 
 	public static class FirstPhaseMapper extends
 			Mapper<NullWritable, BytesWritable, Text, IntWritable> {
		
		public static Log LOG = LogFactory.getLog(FirstPhaseMapper.class);
 
 		private int transactions;
 		private int items;
 		private int minsupport;
 
 		@Override
 		public void setup(Context context) {
 			items = context.getConfiguration().getInt(ITEMS_CONFIG, 0);
 			minsupport = context.getConfiguration()
 					.getInt(MINSUPPORT_CONFIG, 0);
 		}
 
 		@Override
 		public void map(NullWritable key, BytesWritable value, Context context)
 				throws IOException, InterruptedException {
 
 			// transform the bytes to strings
 			String realstr = new String(value.getBytes());
 			String[] rows = realstr.split("\\n");
 			ArrayList<String> dataset = new ArrayList<String>();
 			for (int i = 0; i < rows.length; i++) {
 				String thisrow = rows[i].trim();
 				if (thisrow.length() != 0) {
 					dataset.add(thisrow);
 				}
 			}
 			// get the number of rows from dataset itself
 			transactions = dataset.size();
 
 			// from now on, the input split is a ArrayList<String>
 			// now call the local apriori algorithm
 
 			LocalApriori localalg = new LocalApriori(transactions, items,
 					minsupport, dataset);
 			localalg.apriori();
 			ArrayList<ArrayList<String>> frequent = localalg.frequentItemset();
 			ArrayList<ArrayList<Integer>> occurrences = localalg.frequencies();
 
 			int loopc = 0, loopc2 = 0;
 			for (Iterator<ArrayList<String>> it = frequent.iterator(); it
 					.hasNext(); loopc++) {
 				ArrayList<String> thisitemset = it.next();
 				ArrayList<Integer> thisfreq = occurrences.get(loopc);
 				loopc2 = 0;
 				for (Iterator<String> it2 = thisitemset.iterator(); it2
 						.hasNext(); loopc2++) {
 					context.write(new Text(it2.next()), new IntWritable(
 							thisfreq.get(loopc2)));
 				}
 			}
 
 			context.write(new Text(SPLIT_NUM_ROWS), new IntWritable(
 					transactions));
 		}
 	}
 
 	public static class FirstPhaseReducer extends
 			Reducer<Text, IntWritable, Text, IntWritable> {
 		private IntWritable one = new IntWritable(1);
 
 		@Override
 		public void reduce(Text key, Iterable<IntWritable> values,
 				Context context) throws IOException, InterruptedException {
 
 			if (key.toString().equalsIgnoreCase(SPLIT_NUM_ROWS)) {
 				int totalrows = 0;
 				for (IntWritable val : values) {
 					totalrows += val.get();
 				}
 				context.getCounter(TOTALROWCOUNTER.TOTALROW).increment(
 						totalrows);
 			} else {
 				// the following is for debugging purpose
 				// int sum = 0;
 				// for (IntWritable val : values) {
 				// sum += val.get();
 				// }
 				// context.write(key, new IntWritable(sum));
 				// the following is from paper's description
 				context.write(key, one);
 			}
 		}
 	}
 
 	/* for phase 2 */
 
 	public static void run_on_hadoop_phase2() throws IOException,
 			ClassNotFoundException, InterruptedException, URISyntaxException {
 		Configuration conf = new Configuration();
 
 		conf.set(ITEMS_CONFIG, Integer.toString(items));
 		conf.set(MINSUPPORT_CONFIG, Integer.toString(minsupport));
 
 		conf.set(TOTAL_ROW_CONFIG, Long.toString(totalrows));
 
 		String jobname = "MapReduce 2Phase Apriori, Phase 2";
 		Job job = new Job(conf, jobname);
 		job.setJarByClass(MR2PhaseApriori.class);
 		job.setInputFormatClass(WholeFileInputFormat.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 		job.setMapperClass(SecondPhaseMapper.class);
 		job.setReducerClass(SecondPhaseReducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(IntWritable.class);
 		FileInputFormat.addInputPath(job, new Path(inputpath));
 		FileOutputFormat.setOutputPath(job, new Path(outputpath));
 
 		FileSystem fs = FileSystem.get(conf);
 		FileStatus[] fss = fs.listStatus(new Path(outputpath1stphase));
 		for (FileStatus status : fss) {
 			Path path = status.getPath();
 			if (path.getName().startsWith("_"))
 				continue;
 			job.addCacheFile(new URI(path.toString()));
 		}
 
 		int retval = job.waitForCompletion(true) ? 0 : 1;
 
 //		if (retval != 0) {
 //			System.exit(retval);
 //		}
 	}
 
 	public static class SecondPhaseMapper extends
 			Mapper<NullWritable, BytesWritable, Text, IntWritable> {
 
 		private int items;
 
 		private String[] localFileNames;
 		private File[] localFiles;
 
 		@Override
 		public void setup(Context context) throws IOException {
 			items = context.getConfiguration().getInt(ITEMS_CONFIG, 0);
 			minsupport = context.getConfiguration()
 					.getInt(MINSUPPORT_CONFIG, 0);
 
 			Path[] cacheFiles = context.getLocalCacheFiles();
 			if (cacheFiles != null) {
 				localFileNames = new String[cacheFiles.length];
 				localFiles = new File[cacheFiles.length];
 				for (int i = 0; i < cacheFiles.length; i++) {
 					localFileNames[i] = cacheFiles[i].toString();
 					localFiles[i] = new File(localFileNames[i]);
 				}
 			}
 		}
 
 		@Override
 		public void map(NullWritable key, BytesWritable value, Context context)
 				throws IOException, InterruptedException {
 
 			// transform the bytes to strings
 			String realstr = new String(value.getBytes());
 			String[] rows = realstr.split("\\n");
 			ArrayList<String> dataset = new ArrayList<String>();
 			for (int i = 0; i < rows.length; i++) {
 				String thisrow = rows[i].trim();
 				if (thisrow.length() != 0) {
 					dataset.add(thisrow);
 				}
 			}
 
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
 			while ((line = br.readLine()) != null) {
 				String itemset = line.split("\\t")[0];
 				int count = 0;
 				for (Iterator<String> it = dataset.iterator(); it.hasNext();) {
 					String transaction = it.next();
 					if (included(itemset, transaction)) {
 						count++;
 					}
 				}
 				context.write(new Text(itemset), new IntWritable(count));
 			}
 			br.close();
 		}
 
 		private boolean included(String itemset, String transaction) {
 			boolean[] trans = new boolean[items];
 			StringTokenizer st = new StringTokenizer(transaction,
 					LocalApriori.DATASETSEPARATOR);
 			for (int i = 0; i < items; i++) {
 				trans[i] = st.nextToken().equalsIgnoreCase(
 						LocalApriori.PURCHASED);
 			}
 			StringTokenizer st2 = new StringTokenizer(itemset,
 					LocalApriori.SEPARATOR);
 			boolean match = false;
 			while (st2.hasMoreTokens()) {
 				match = trans[Integer.parseInt(st2.nextToken()) - 1];
 				if (!match) {
 					return false;
 				}
 			}
 			return true;
 		}
 	}
 
 	public static class SecondPhaseReducer extends
 			Reducer<Text, IntWritable, Text, IntWritable> {
 
 		private long totalrows;
 		private int minsupport;
 
 		@Override
 		public void setup(Context context) throws IOException {
 			minsupport = context.getConfiguration()
 					.getInt(MINSUPPORT_CONFIG, 0);
 			totalrows = context.getConfiguration().getLong(TOTAL_ROW_CONFIG, 0);
 		}
 
 		@Override
 		public void reduce(Text key, Iterable<IntWritable> values,
 				Context context) throws IOException, InterruptedException {
 			int sum = 0;
 			for (IntWritable val : values) {
 				sum += val.get();
 			}
 			if ((sum * 100) >= (minsupport * totalrows)) {
 				context.write(key, new IntWritable(sum));
 			}
 		}
 	}
 
 	/* main function */
 
 	public static void main(String[] args) throws IOException,
 			ClassNotFoundException, InterruptedException, URISyntaxException {
 		Configuration conf = new Configuration();
 		String[] otherArgs = new GenericOptionsParser(conf, args)
 				.getRemainingArgs();
 		if (otherArgs.length != 4) {
 			System.err.print(PREFIX + "Usage: MR2PhaseApriori <in> <out>");
 			System.err.print(" <columns/items>");
 			System.err.println(" <minimum support>");
 			System.exit(2);
 		}
 
 		inputpath = otherArgs[0];
 		outputpath = otherArgs[1];
 		items = Integer.parseInt(otherArgs[2]);
 		minsupport = Integer.parseInt(otherArgs[3]);
 
 		outputpath1stphase = outputpath + "-1stphase";
 
 		long starttime = System.currentTimeMillis();
 
 		run_on_hadoop_phase1();
 
 		run_on_hadoop_phase2();
 
 		long endtime = System.currentTimeMillis();
 
 		System.err.println(PREFIX + "Total execution time: "
 				+ (endtime - starttime));
 	}
 }
