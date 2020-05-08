 /*
   CS 61C Project1: Small World
 
   Name:
   Login:
 
   Name:
   Login:
  */
 
 import java.io.BufferedReader;
 
 import java.io.BufferedWriter;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.Math;
 import java.util.*;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.io.SequenceFile;
 
 public class SmallWorld {
 	// Maximum depth for any breadth-first search
 	public static final int MAX_ITERATIONS = 20;
 
 	// Skeleton code uses this to share denom cmd-line arg across cluster
 	public static final String DENOM_PATH = "denom.txt";
 
 	// Counter for visited nodes in each bfs iteration
 	public static enum ValueUse {
 		VISITED
 	};
 
 	// Custom Writable subclass to keep track of nodes visited in the past.
 	public static class StateWritable implements Writable {
 		public long dest;
 		public HashSet<Long> hist; // guaranteed to have size<MAX_ITERATIONS;
 									// even smaller
 									// on average due to small world property
 
 		public StateWritable(long dest, HashSet<Long> hist) {
 			this.dist = dist;
 			this.hist = hist;
 		}
 
 		public StateWritable(long dest) {
    		this(dest, new HashSet<Long>());
     	}
 
 		public StateWritable() {
 			this(-1L);
 		}
 
 		public void setDest(long dest) {
 			this.dest = dest;
 		}
 
 		public void addHist(long vertex) {
 			hist.add(vertex);
 		}
 
 		public void write(DataOutput out) throws IOException {
 			out.writeLong(dest);
 			out.writeLong(hist.size());
 			for (long vertex : hist) {
 				out.writeLong(vertex);
 			}
 		}
 
 		public void readFields(DataInput in) throws IOException {
     		dest = in.readLong();
     		int size = in.readLong();
    		hist = new HashSet<Long>();
     		for (int i=0; i<size; i++) {
     			hist.add(in.readLong());
     		}
     	}
 
 		public String toString() {
 			String out = "(" + dest + ", [";
 			for (long vertex : hist) {
 				out += vertex;
 			}
 			return out + "])";
 		}
 	}
 
 	/* MAPREDUCE CLASSSES */
 
 	// Format mapper: converts values to the (destination, history) and randomly
 	// selects root nodes
 	public static class FormatMap extends
 			Mapper<LongWritable, LongWritable, LongWritable, StateWritable> {
 		public long denom;
 
 		// Load the cached denom value
 		@Override
 		public void setup(Context context) {
 			try {
 				Configuration conf = context.getConfiguration();
 				Path cachedDenomPath = DistributedCache
 						.getLocalCacheFiles(conf)[0];
 				BufferedReader reader = new BufferedReader(new FileReader(
 						cachedDenomPath.toString()));
 				String denomStr = reader.readLine();
 				reader.close();
 				denom = Long.decode(denomStr);
 			} catch (IOException ioe) {
 				System.err
 						.println("IOException reading denom from distributed cache");
 				System.err.println(ioe.toString());
 			}
 		}
 
 		public void map(LongWritable key, LongWritable dest, Context context)
                 throws IOException, InterruptedException {
         	if (Math.random() < 1.0 / denom) {
        		HashSet<Long> root = new HashSet<Long>();
         		root.add(key.get());
         		context.write(key, new StateWritable(dest.get(), root));
         	}
         	else {
         		context.write(key, new StateWritable(dest.get()));
         	}
         }
 	}
 
 	// Format reducer: move along, nothing to see here.
 	public static class FormatReduce extends
 			Reducer<LongWritable, StateWritable, LongWritable, StateWritable> {
 		public void reduce(LongWritable key, Iterable<StateWritable> values,
 				Context context) throws IOException, InterruptedException {
 			for (StateWritable value : values) {
 				context.write(key, value);
 			}
 		}
 	}
 
 	// BFS Mapper: advance the frontier by one level and keep count of nodes
 	// visited in the process.
 	public static class BFSMap extends
 			Mapper<LongWritable, StateWritable, LongWritable, StateWritable> {
 		public void map(LongWritable key, StateWritable value, Context context)
 				throws IOException, InterruptedException {
 			long dest = value.dest;
 			HashSet<Long> hist = value.hist;
 			if (!hist.contains(dest) && hist.size > 0 && hist.size <= MAX_ITERATIONS) {
 				hist.add(dest);
 				context.write(new LongWritable(dest), new StateWritable(-1, hist));
 				context.getCounter(ValueUse.VISITED).increment(1);
 			}
 			else {
				context. write(key, new StateWritable(dest, new HashSet<Long>()));
 			}
 		}
 	}
 
 	public static class BFSReduce extends
 			Reducer<LongWritable, StateWritable, LongWritable, StateWritable> {
 		public void reduce(LongWritable key, Iterable<StateWritable> values,
 				Context context) throws IOException, InterruptedException {
			ArrayList<HashSet<Long>> hists = new ArrayList<HashSet<Long>>();
			ArrayList<Long> dests = new ArrayList<Long>();
 			
 			for (StateWritable value : values) {
 				if (value.dest > 0) dests.add(value.dest);
 				if (value.hist.size() > 0) hists.add(value.hist);
 			}
 			
 			for (long dest : dests) {
 				for (HashSet<Long> hist : hists) {
 					context.write(key, new StateWritable(dest, hist));
 				}
 			}
 		}
 	}
 
 	// Shares denom argument across the cluster via DistributedCache
 	public static void shareDenom(String denomStr, Configuration conf) {
 		try {
 			Path localDenomPath = new Path(DENOM_PATH + "-source");
 			Path remoteDenomPath = new Path(DENOM_PATH);
 			BufferedWriter writer = new BufferedWriter(new FileWriter(
 					localDenomPath.toString()));
 			writer.write(denomStr);
 			writer.newLine();
 			writer.close();
 			FileSystem fs = FileSystem.get(conf);
 			fs.copyFromLocalFile(true, true, localDenomPath, remoteDenomPath);
 			DistributedCache.addCacheFile(remoteDenomPath.toUri(), conf);
 		} catch (IOException ioe) {
 			System.err.println("IOException writing to distributed cache");
 			System.err.println(ioe.toString());
 		}
 	}
 
 	public static void main(String[] rawArgs) throws Exception {
 		GenericOptionsParser parser = new GenericOptionsParser(rawArgs);
 		Configuration conf = parser.getConfiguration();
 		String[] args = parser.getRemainingArgs();
 
 		// Set denom from command line arguments
 		shareDenom(args[2], conf);
 
 		// Setting up mapreduce job to load in graph
 		Job job = new Job(conf, "load graph");
 		job.setJarByClass(SmallWorld.class);
 
 		job.setMapOutputKeyClass(LongWritable.class);
 		job.setMapOutputValueClass(StateWritable.class);
 		job.setOutputKeyClass(LongWritable.class);
 		job.setOutputValueClass(StateWritable.class);
 
 		job.setMapperClass(FormatMap.class);
 		job.setReducerClass(FormatReduce.class);
 
 		job.setInputFormatClass(SequenceFileInputFormat.class);
 		job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
 		// Input from command-line argument, output to predictable place
 		FileInputFormat.addInputPath(job, new Path(args[0]));
 		FileOutputFormat.setOutputPath(job, new Path("bfs-0-out"));
 
 		// Actually starts job, and waits for it to finish
 		job.waitForCompletion(true);
 
 		// Repeats your BFS mapreduce
 		int i = 0;
		ArrayList<Long> histogram = new ArrayList<Long>();
 		// Will need to change terminating conditions to respond to data
 		while (i < MAX_ITERATIONS) {
 			job = new Job(conf, "bfs" + i);
 			job.setJarByClass(SmallWorld.class);
 
 			job.setMapOutputKeyClass(LongWritable.class);
 			job.setMapOutputValueClass(StateWritable.class);
 			job.setOutputKeyClass(LongWritable.class);
 			job.setOutputValueClass(StateWritable.class);
 
 			job.setMapperClass(BFSMap.class);
 			job.setReducerClass(BFSReduce.class);
 
 			job.setInputFormatClass(SequenceFileInputFormat.class);
 			job.setOutputFormatClass(SequenceFileOutputFormat.class);
 
 			// Notice how each mapreduce job gets gets its own output dir
 			FileInputFormat.addInputPath(job, new Path("bfs-" + i + "-out"));
 			FileOutputFormat.setOutputPath(job, new Path("bfs-" + (i + 1)
 					+ "-out"));
 
 			job.waitForCompletion(true);
 			histogram.add(job.getCounters().findCounter(ValueUse.VISITED).getValue());
 			i++;
 		}
 
 		// Hapoop the histogram into a file.
 		writer = new SequenceFile.Writer(FileSystem.get(conf), conf, "./output.txt", Long, Long);
 		for (long i=0; i<histogram.size(); i++) {
 			writer.append(i, ArrayList.get(i));
 		}
 	}
 }
