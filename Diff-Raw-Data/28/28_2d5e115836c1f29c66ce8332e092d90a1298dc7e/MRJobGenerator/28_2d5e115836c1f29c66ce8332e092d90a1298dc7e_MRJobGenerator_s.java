 package edu.berkeley.icsi.cdfs.wlgen;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Job;
 
 final class MRJobGenerator {
 
 	static Job toMRJob(final String basePath, final MapReduceJob mapReduceJob) throws IOException {
 
 		final Configuration conf = new Configuration();
 		ClusterConfigurator.addClusterConfiguration(conf);
 		conf.set(FixedByteInputFormat.INPUT_PATH, basePath + java.io.File.separator
 			+ mapReduceJob.getInputFile().getName());
 		conf.setInt(FixedByteInputFormat.NUMBER_OF_MAPPERS, mapReduceJob.getNumberOfMapTasks());
 		double ioRatio = (double) mapReduceJob.getInputFile().getUncompressedFileSize()
 			/ (double) mapReduceJob.getSizeOfIntermediateData();
 		conf.setFloat(MapTask.INPUT_OUTPUT_RATIO, (float) ioRatio);
 		ioRatio = (double) mapReduceJob.getSizeOfIntermediateData()
 			/ (double) mapReduceJob.getOutputFile().getUncompressedFileSize();
 		conf.setFloat(ReduceTask.INPUT_OUTPUT_RATIO, (float) ioRatio);
 		conf.set(FixedByteOutputFormat.OUTPUT_PATH, basePath + java.io.File.separator
 			+ mapReduceJob.getOutputFile().getName());
 		conf.set(ReducePartitioner.DATA_DISTRIBUTION,
 			ReducePartitioner.encodeDataDistribution(mapReduceJob.getDataDistribution()));
 
 		final Job job = new Job(conf, mapReduceJob.getJobID());
 		job.setMapperClass(MapTask.class);
 		job.setReducerClass(ReduceTask.class);
 		job.setPartitionerClass(ReducePartitioner.class);
 		job.setNumReduceTasks(mapReduceJob.getNumberOfReduceTasks());
 		job.setInputFormatClass(FixedByteInputFormat.class);
 		job.setOutputFormatClass(FixedByteOutputFormat.class);
 		job.setMapOutputKeyClass(FixedByteRecord.class);
 		job.setMapOutputValueClass(NullWritable.class);
 		job.setPartitionerClass(ReducePartitioner.class);
 
 		return job;
 	}
 }
