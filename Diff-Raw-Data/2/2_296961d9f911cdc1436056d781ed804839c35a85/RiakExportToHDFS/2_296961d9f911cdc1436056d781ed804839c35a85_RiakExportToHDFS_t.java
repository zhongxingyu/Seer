 /**
  * Copyright 2010 Mozilla Foundation
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.mozilla.hadoop.riak;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.SequenceFile.CompressionType;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import com.basho.riak.client.RiakBucketInfo;
 import com.basho.riak.client.RiakClient;
 import com.basho.riak.client.RiakObject;
 import com.basho.riak.client.response.BucketResponse;
 import com.basho.riak.client.response.FetchResponse;
 
 public class RiakExportToHDFS implements Tool {
 
 	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RiakExportToHDFS.class);
 	
 	private static final String NAME = "RiakExportToHDFS";
 	
 	// config properties
 	private static final String RIAK_BUCKET = "riak.bucket";
 	private static final String RIAK_SERVERS = "riak.servers";
 	
 	private Configuration conf;
 	
 	public static class RiakExportToHDFSMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
 
 		public enum ReportStats { RIAK_KEY_COUNT, FETCH_RESPONSE_NOT_SUCCESSFUL };
 		
 		private Text outputKey;
 		
 		private String bucket;
 		private RiakClient[] clients;
 		private int serverIdx = 0;
 		
 		/* (non-Javadoc)
 		 * @see org.apache.hadoop.mapreduce.Mapper#setup(org.apache.hadoop.mapreduce.Mapper.Context)
 		 */
 		public void setup(Context context) {
 			outputKey = new Text();
 			
 			Configuration conf = context.getConfiguration();
 			String[] riakServers = conf.getStrings(RIAK_SERVERS);
 			clients = new RiakClient[riakServers.length];
 			for (int i=0; i < riakServers.length; i++) {
 				clients[i] = new RiakClient(riakServers[i]);
 			}
 			bucket = conf.get(RIAK_BUCKET);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
 		 */
 		public void map(LongWritable key, Text value, Context context) throws InterruptedException, IOException {
 			String riakKey = value.toString();
 			FetchResponse fr = clients[serverIdx].fetch(bucket, riakKey);
 			if (fr.isSuccess()) {
 				RiakObject o = fr.getObject();
 				outputKey.set(o.getValue());
 				context.getCounter(ReportStats.RIAK_KEY_COUNT).increment(1L);
 				context.write(outputKey, NullWritable.get());
 			} else {
 				context.getCounter(ReportStats.FETCH_RESPONSE_NOT_SUCCESSFUL).increment(1L);
 			}
 			
 			serverIdx++;
 			if (serverIdx >= clients.length) {
 				serverIdx = 0;
 			}
 		}
 		
 	}	
 
 	/**
 	 * Check the handle and close it
 	 * @param c
 	 */
 	private static void checkAndClose(java.io.Closeable c) {
 		if (c != null) {
 			try {
 				c.close();
 			} catch (IOException e) {
 				LOG.error("Error closing stream", e);
 			}
 		}
 	}
 	
 	/**
 	 * Create the input source files to be used as input for the mappers
 	 * @param riak
 	 * @param bucket
 	 * @param hdfs
 	 * @return
 	 * @throws IOException
 	 */
 	public Path[] createInputSources(RiakClient riak, String bucket, FileSystem hdfs) throws IOException {
 		int suggestedMapRedTasks = conf.getInt("mapred.map.tasks", 1);
 		Path[] inputSources = new Path[suggestedMapRedTasks];
 		for (int i=0; i < inputSources.length; i++) {
 			inputSources[i] = new Path(NAME + "-inputsource" + i + ".txt");
 		}
 		List<BufferedWriter> writers = new ArrayList<BufferedWriter>();
 		int idx = 0;
 		try {
 			BucketResponse br = riak.listBucket(bucket);
 			RiakBucketInfo rbi = br.getBucketInfo();
 			Collection<String> keys = rbi.getKeys();
 			
 			for (Path source : inputSources) {
 				writers.add(new BufferedWriter(new OutputStreamWriter(hdfs.create(source))));
 			}
 			
 			// split keys across N files and MapReduce to copy
 			for (String k : keys) {
 				writers.get(idx).write(k);
 				writers.get(idx).newLine();
 				
 				idx++;
 				if (idx >= inputSources.length) {
 					idx = 0;
 				}
 			}
 		} finally {
 			for (BufferedWriter writer : writers) {
 				checkAndClose(writer);
 			}
 		}
 		
 		return inputSources;
 	}
 	
 	/**
 	 * @param args
 	 * @return
 	 * @throws IOException
 	 * @throws ParseException 
 	 */
 	public Job initJob(String[] args) throws IOException, ParseException {
 
 		String bucket = null;
 		String outputPath = null;
 		for (int idx=0; idx < args.length; idx++) {
 			if (idx == args.length-1) {
 				outputPath = args[idx];
 			} else {
 				bucket = args[idx];
 			}
 		}
 		
 		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
 		conf.set(RIAK_BUCKET, bucket);
 		
 		FileSystem hdfs = null;
 		Path[] inputSources = null;
 		try {
 			RiakClient riak = new RiakClient(conf.getStrings(RIAK_SERVERS)[0]);
 			hdfs = FileSystem.get(getConf());
 			inputSources = createInputSources(riak, bucket, hdfs);
 		} finally {
 			checkAndClose(hdfs);
 		}
 		
 		Job job = new Job(getConf());
 		job.setJobName(NAME);
 		job.setJarByClass(RiakExportToHDFS.class);
 	
 		job.setMapperClass(RiakExportToHDFSMapper.class);
 		
 		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
 		
 		job.setNumReduceTasks(0);
 		
 		job.setInputFormatClass(TextInputFormat.class);
 		
 		for (Path source : inputSources) {
 			System.out.println("Adding input path: " + source.toString());
 			FileInputFormat.addInputPath(job, source);
 		}
 	
 		SequenceFileOutputFormat.setCompressOutput(job, true);
 		SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
 		SequenceFileOutputFormat.setOutputPath(job, new Path(outputPath));
 		
 		return job;
 	}
 
 	/**
 	 * @return
 	 */
 	private static int printUsage() {
 		System.out.println("Usage: " + NAME + " [generic-options] <riak-bucket> <output-path>");
 		System.out.println();
 		GenericOptionsParser.printGenericCommandUsage(System.out);
 		
 		return -1;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
 	 */
 	public int run(String[] args) throws Exception {
 		if (args.length < 2) {
 			return printUsage();
 		}
 		
 		int rc = -1;
 		Job job = initJob(args);
 		job.waitForCompletion(true);
 		if (job.isSuccessful()) {
 			rc = 0;
 			
 			FileSystem hdfs = null;
 			try {
 				hdfs = FileSystem.get(job.getConfiguration());
 				hdfs.delete(new Path(NAME + "-inputsource*.txt"), false);
 			} finally {
 				checkAndClose(hdfs);
 			}
 		}
 		
 		return rc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.conf.Configurable#getConf()
 	 */
 	public Configuration getConf() {
 		return this.conf;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.Configuration)
 	 */
 	public void setConf(Configuration conf) {
 		this.conf = conf;
 	}
 	
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		int res = ToolRunner.run(new Configuration(), new RiakExportToHDFS(), args);
 		System.exit(res);
 	}
 
 }
