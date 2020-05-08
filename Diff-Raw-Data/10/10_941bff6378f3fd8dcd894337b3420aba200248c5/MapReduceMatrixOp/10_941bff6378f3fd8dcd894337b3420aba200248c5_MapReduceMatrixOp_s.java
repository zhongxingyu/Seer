 package edu.columbia.mipl.mapreduce;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Partitioner;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 
 import edu.columbia.mipl.datastr.PrimitiveDoubleArray;
 import edu.columbia.mipl.matops.DefaultMatrixOperations;
 
 
 public class MapReduceMatrixOp {
 	static final int NUM_MATRIX_SPLIT = 8;
 	static final int MIN_MATRIX_SIZE = NUM_MATRIX_SPLIT * 8;
 
 	private static int count;
 	public static class MatrixMapper extends MapReduceBase
 	implements Mapper<LongWritable, Text, LongWritable, WritableArray> {
 
 		private static final LongWritable newKey = new LongWritable();
 
 		private static String input1;
 		private static String input2;
 
 		private static int operation;
 
 		public void map(LongWritable key, Text val,
 				OutputCollector<LongWritable, WritableArray> output, Reporter reporter)
 						throws IOException {
 
 			FileSplit fs = (FileSplit) reporter.getInputSplit();
 			String fileName = fs.getPath().getName();
 
 			//			System.out.println(fs.getPath().getName());
 			int n = 1;
 
 			if (key.get() == 0) count = 0;
 			WritableArray array = new WritableArray(1, n, key.get());
 
 			String line = val.toString();
 			StringTokenizer itr = new StringTokenizer(line.toLowerCase());
 			while (itr.hasMoreTokens()) {
 
 				if (array.getCol() < n)
 					array.increaseCol();
 
 				array.setValue(0, n - 1, Double.parseDouble(itr.nextToken()));
 				n++;
 			}
 
 			if (fileName.endsWith(input1)) {
 				array.setOperation(operation);
 			}
 			newKey.set(count);
 			output.collect(newKey, array);
 			count++;
 		}
 
 		public void configure(JobConf job) {
 			//			System.out.println(job.get("input1"));
 			//			System.out.println(job.get("input2"));
 
 			input1 = job.get("input1");
 			input2 = job.get("input2");
 
 			operation = job.getInt("op", 0);
 			//			System.out.println(operation);
 		}
 	}
 
 	private static class MatrixPartitioner extends MapReduceBase
 	implements Partitioner<LongWritable, WritableArray> {
 
 		public int getPartition(LongWritable writ, WritableArray arr, int numPartitions) {
 			//			System.out.println("getPartition : " + writ.get() + " " + numPartitions);
 			return 0;
 		}
 	}
 
 	public static class MatrixReducer extends MapReduceBase
	implements Reducer<LongWritable, WritableArray, WritableIndex, WritableArray> {
 		private static String input1;
 		private static String input2;
 
 		private static int operation;
 
 
 		public void reduce(LongWritable key, Iterator<WritableArray> values,
				OutputCollector<WritableIndex, WritableArray> output, Reporter reporter)
 						throws IOException {
 
 			WritableArray sumArr = null;
 			while (values.hasNext()) {
 				WritableArray array = values.next();
 
 				if (sumArr == null) { 
 					sumArr = new WritableArray(array.getRow(), array.getCol(), array.getPos());
 					sumArr.setOperation(array.getOperation());
 					sumArr.copyRange(array, 0, 0, 0, 0, array.getRow(), array.getCol());
 					
 					if (array.getOperation() == MapReduceProxy.MATRIX_ABS) {
 						sumArr = new WritableArray(array.getRow(), array.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().abs(array)).getData(), array.getPos());
 					}
 				}
 				else {
 
 					if (sumArr.isFirstMatrix()) {
 
 						switch (sumArr.getOperation()) {
 						case MapReduceProxy.MATRIX_ADD:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().add(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_SUB:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().sub(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_CELLMUL:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().cellmult(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_CELLDIV:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().celldiv(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						}
 					}
 					else {
 						switch (array.getOperation()) {
 						case MapReduceProxy.MATRIX_ADD:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().add(array, sumArr)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_SUB:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().sub(array, sumArr)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_CELLMUL:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().cellmult(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						case MapReduceProxy.MATRIX_CELLDIV:
 							sumArr = new WritableArray(sumArr.getRow(), sumArr.getCol(), ((PrimitiveDoubleArray) new DefaultMatrixOperations().celldiv(sumArr, array)).getData(), sumArr.getPos());						
 							break;
 						}
 					}
 				}
 
 			}
			output.collect(new WritableIndex(key.get(), key.get()), sumArr);
 		}
 		public void configure(JobConf job) {
 			//			System.out.println(job.get("input1"));
 			//			System.out.println(job.get("input2"));
 
 			input1 = job.get("input1");
 			input2 = job.get("input2");
 
 			operation = job.getInt("op", 0);
 			//			System.out.println(operation);
 		}
 
 	}
 
 
 	/**
 	 * The actual main() method for our program; this is the
 	 * "driver" for the MapReduce job.
 	 */
 	public static void main(String[] args) {
 
 		File folder = new File("output");
 		if (folder.exists()) {
 			for (File c : folder.listFiles())
 				c.delete();
 			folder.delete();
 		}
 		JobClient client = new JobClient();
 		JobConf conf = new JobConf(MapReduceMatrixOp.class);
 
 
 		conf.set("input1", "input1.csv");
 		conf.set("input2", "input2.csv");
 		conf.setInt("op", MapReduceProxy.MATRIX_SUB);
 		conf.setJobName("MatrixSplitter");
 
 		conf.setMapOutputKeyClass(LongWritable.class);
 		conf.setMapOutputValueClass(WritableArray.class);
 
 		conf.setOutputKeyClass(WritableIndex.class);
 		conf.setOutputValueClass(WritableArray.class);
 
 		FileInputFormat.addInputPath(conf, new Path("input/input1.csv"));
 		FileInputFormat.addInputPath(conf, new Path("input/input2.csv"));
 		FileOutputFormat.setOutputPath(conf, new Path("output"));
 
 		conf.setMapperClass(MatrixMapper.class);
 		conf.setPartitionerClass(MatrixPartitioner.class);
 		//		conf.setCombinerClass(MatrixReducer.class);
 		conf.setReducerClass(MatrixReducer.class);
 
 		client.setConf(conf);
 
 		try {
 			JobClient.runJob(conf);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
