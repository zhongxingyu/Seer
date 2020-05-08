 package operators.join.SimpleJoin;
 
 import java.io.IOException;
 import java.util.*;
 
 import operators.join.TextPair;
 import operators.selection.SelectionFilter;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.mapred.lib.MultipleInputs;
 import org.apache.hadoop.util.*;
 
 import relations.Relation;
 
 public class ReduceSideJoin extends Configured implements Tool {
 
 	/**
 	 * Outer is the smaller relation, i.e. it's values per key are copied into
 	 * mem in the reduce phase
 	 * 
 	 */
 	public static final String PREFIX_JOIN_SMALLER = "smaller_";
 	public static final String PREFIX_JOIN_LARGER = "larger_";
 
 	protected static final String COLUMN_SEPARATOR_RE = "\\|";
 	protected static final String COLUMN_SEPARATOR = "|";
 
 	private static boolean IS_LOCAL = true;
 	private static final boolean DEBUG = true;
 
 	public static class OuterMapper extends ReduceSideJoinAbstractMapper {
 		public void configure(JobConf conf) {
 			reduceOrder = "0";
 			joinCol = conf.getInt("OuterJoinColumn", 0);
 			selectionFilter = new SelectionFilter(conf, PREFIX_JOIN_SMALLER);
 		}
 	}
 
 	public static class InnerMapper extends ReduceSideJoinAbstractMapper {
 		public void configure(JobConf conf) {
 			reduceOrder = "1";
 			joinCol = conf.getInt("InnerJoinColumn", 0);
 			selectionFilter = new SelectionFilter(conf, PREFIX_JOIN_LARGER);
 		}
 
 	}
 
 	public static class ReduceSideJoinAbstractMapper extends MapReduceBase implements Mapper<LongWritable, Text, TextPair, TextPair> {
 
 		// Overridden by child class
 		protected static String reduceOrder;
 		protected static int joinCol; // TODO: this forces NO Parallel jobs
 
 		// TODO: add projection
 		protected static SelectionFilter selectionFilter;
 
 		public void map(LongWritable key, Text value, OutputCollector<TextPair, TextPair> output, Reporter reporter) throws IOException {
 			String[] tuple = value.toString().split(COLUMN_SEPARATOR_RE);
 
 			// System.out.println("map in: " + value);
 
 			// filter the rows out that don't pass the selection
 			if (!selectionFilter.checkSelection(tuple))
 				return;
 
 			if (DEBUG)
 				System.out.println("sel ok: " + value);
 
 			StringBuffer attrs = new StringBuffer();
 			for (int i = 0; i < tuple.length; i++) {
 				// TODO: for this is simplier and in general it's ok to leave it
 				// like this.
 				// if (i != joinCol) {
 				attrs.append(tuple[i] + COLUMN_SEPARATOR);
 				// }
 			}
 
 			if (attrs.length() > 0) {
 				attrs.deleteCharAt(attrs.length() - 1);
 			}
 
 			output.collect(new TextPair(tuple[joinCol], reduceOrder), new TextPair(attrs.toString(), reduceOrder));
 		}
 
 	}
 
 	public static class KeyPartitioner implements Partitioner<TextPair, TextPair> {
 		@Override
 		public int getPartition(TextPair key, TextPair value, int numPartitions) {
 			return (key.getFirst().hashCode() & Integer.MAX_VALUE) % numPartitions;
 		}
 
 		@Override
 		public void configure(JobConf conf) {
 		}
 	}
 
 	public static class JoinReducer extends MapReduceBase implements Reducer<TextPair, TextPair, Text, Text> {
 
 		public void reduce(TextPair key, Iterator<TextPair> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
 			ArrayList<String> buffer = new ArrayList<String>();
 			Text tag = key.getSecond();
 			TextPair value = null;
 			String attrs = null;
 			String bAttrs = null;
 
 			// System.out.println(key.getFirst().toString() + "\t" +
 			// key.getSecond().toString());
 			while (values.hasNext()) {
 				value = values.next();
 
 				// System.out.println(value.getFirst().toString() + "\t" +
 				// value.getSecond().toString());
 				if ((value.getSecond().compareTo(tag) == 0)) {
 					buffer.add(value.getFirst().toString());
 				} else {
 					bAttrs = value.getFirst().toString();
 					for (String val : buffer) {
 						if ("".compareTo(val) != 0 && "".compareTo(bAttrs) != 0) {
 							attrs = val + COLUMN_SEPARATOR + bAttrs;
 						} else {
 							attrs = val + bAttrs;
 						}
 						if (DEBUG)
 							System.out.println("reduce out:" + key.getFirst() + "-->" + attrs);
 						output.collect(key.getFirst(), new Text(attrs));
 					}
 				}
 			}
 			// System.out.println("----------");
 		}
 	}
 
 	public int run(String[] args) throws Exception {
 		if (args.length != 5) {
 			System.out.println("USAGE: <prog name> <R input> <R joincol> <S input> <S joincol> <output>");
 			return -1;
 		}
 
 		String inInnerRelation = args[2];
 		String inOuterRelation = args[0];
 
 		int innerJoinCol = Integer.parseInt(args[3]);
 		int outerJoinCol = Integer.parseInt(args[1]);
 
 		String output = args[4];
 
 		JobConf conf = getJoinConf(getConf(), inInnerRelation, innerJoinCol, inOuterRelation, outerJoinCol, output);
 
 		// Run job
 		JobClient.runJob(conf);
 		return 0;
 	}
 
 	/**
 	 * Convience method to made definitions shorter
 	 * 
 	 * TODO: shall I change string parameters into Path? What about hdfs paths?
 	 * 
 	 * TODO: change params inner/outer into smaller/larger -- so it would be
 	 * more obvious
 	 * 
 	 * @param
 	 * @return
 	 */
 	public static JobConf getConf(Relation larger, String largerJoinCol, Relation smaller, String smallerJoinCol, Relation outRelation) {
 		return ReduceSideJoin.getJoinConf(new Configuration(), larger.storageFileName, larger.schema.columnIndex(largerJoinCol), smaller.storageFileName,
 				smaller.schema.columnIndex(smallerJoinCol), outRelation.storageFileName);
 	}
 
 	/**
 	 * Convience method to made definitions shorter: Natural join
 	 * 
 	 * more obvious
 	 * 
 	 * @param
 	 * @return
 	 */
 	public static JobConf getConf(Relation larger, Relation smaller, String naturalJoinCol, Relation outRelation) {
 		return getConf(larger, naturalJoinCol, smaller, naturalJoinCol, outRelation);
 	}
 
 	/**
 	 * Returns default configuration
 	 * 
 	 * TODO: shall I change string parameters into Path? What about hdfs paths?
 	 * 
 	 * TODO: change params inner/outer into smaller/larger -- so it would be
 	 * more obvious
 	 * 
 	 * @param
 	 * @return
 	 */
 	public static JobConf getJoinConf(Configuration conf_, String inLargerPath, int largerJoinCol, String inSmallerPath, int smallerJoinCol, String outputPath) {
 		JobConf conf = new JobConf(conf_, ReduceSideJoin.class);
 
 		if (IS_LOCAL) {
 			conf.set("mapred.job.tracker", "local");
 			conf.set("fs.default.name", "local");
 		}
 
 		// Mapper classes & Input files
 		MultipleInputs.addInputPath(conf, new Path(inSmallerPath), TextInputFormat.class, OuterMapper.class);
 		MultipleInputs.addInputPath(conf, new Path(inLargerPath), TextInputFormat.class, InnerMapper.class);
 
 		// Output path
 		FileOutputFormat.setOutputPath(conf, new Path(outputPath));
 
 		// Mapper output class
 		conf.setMapOutputKeyClass(TextPair.class);
 		conf.setMapOutputValueClass(TextPair.class);
 
 		// Mapper Value Grouping
 		conf.setOutputValueGroupingComparator(TextPair.FirstComparator.class);
 
 		// Partitioner
 		conf.setPartitionerClass(KeyPartitioner.class);
 
 		// Reducer
 		conf.setReducerClass(JoinReducer.class);
 
 		// Reducer output
 		conf.setOutputKeyClass(Text.class);
 
 		// Set ReduceSideJoin columns
 		conf.setInt("OuterJoinColumn", smallerJoinCol);
 		conf.setInt("InnerJoinColumn", largerJoinCol);
 
 		if (DEBUG) {
 			System.out.println("Reduce side join:" + inSmallerPath + " X " + inLargerPath + "-->" + outputPath + "Join col idx: smaller=" + smallerJoinCol
 					+ "; larger=" + largerJoinCol);
 		}
 
 		return conf;
 	}
 
 	public static void main(String[] args) throws Exception {
 		int res = ToolRunner.run(new Configuration(), new ReduceSideJoin(), args);
 		System.exit(res);
 	}
 }
