 package edu.berkeley.icsi.cdfs.wlgen;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.InputFormat;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 
 public final class FixedByteInputFormat extends InputFormat<FixedByteRecord, NullWritable> {
 
 	public static final String INPUT_PATH = "input.path";
 
 	public static final String NUMBER_OF_MAPPERS = "number.of.mappers";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public RecordReader<FixedByteRecord, NullWritable> createRecordReader(final InputSplit arg0,
 			final TaskAttemptContext arg1) throws IOException, InterruptedException {
 
 		final FixedByteInputSplit is = (FixedByteInputSplit) arg0;
 		return new FixedByteRecordReader(is, arg1.getConfiguration());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<InputSplit> getSplits(final JobContext arg0) throws IOException, InterruptedException {
 
 		final Configuration conf = arg0.getConfiguration();
 		final String ip = conf.get(INPUT_PATH, null);
 		if (ip == null) {
 			throw new IllegalStateException("Cannot find input path");
 		}
 
 		final int numberOfMappers = conf.getInt(NUMBER_OF_MAPPERS, -1);
 		if (numberOfMappers < 0) {
 			throw new IllegalStateException("Cannot determine the number of mappers");
 		}
 
 		final List<InputSplit> inputSplits = new ArrayList<InputSplit>(numberOfMappers);
 
 		// Talk to the file system to generate input splits
 		final Path inputPath = new Path(ip);
 		final FileSystem fs = inputPath.getFileSystem(conf);
 		final FileStatus fileStatus = fs.getFileStatus(inputPath);
 		if (fileStatus == null) {
 			throw new IllegalStateException("Cannot determine file status for " + ip);
 		}
 
 		final BlockLocation[] blockLocations = fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
 		if (blockLocations == null) {
 			throw new IllegalStateException("Cannot determine block locations for " + ip);
 		}
 
 		if (blockLocations.length != numberOfMappers) {
			throw new IllegalStateException(blockLocations.length + " blocks but " + numberOfMappers
				+ " mappers + (file " + ip + " length " + fileStatus.getLen() + ")");
 		}
 
 		for (int i = 0; i < blockLocations.length; ++i) {
 			inputSplits.add(new FixedByteInputSplit(inputPath, blockLocations[i].getOffset(), blockLocations[i]
 				.getLength(), blockLocations[i].getHosts()));
 		}
 
 		return inputSplits;
 	}
 
 }
