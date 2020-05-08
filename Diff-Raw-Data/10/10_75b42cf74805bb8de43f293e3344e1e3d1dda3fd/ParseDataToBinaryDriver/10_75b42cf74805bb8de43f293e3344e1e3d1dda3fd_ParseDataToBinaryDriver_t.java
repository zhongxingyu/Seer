 package org.yandex.estimate;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.cli2.builder.ArgumentBuilder;
 import org.apache.commons.cli2.builder.DefaultOptionBuilder;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
 import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.HRegionPartitioner;
 import org.apache.hadoop.hbase.mapreduce.MultiTableOutputFormat;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Reducer.Context;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.mahout.clustering.kmeans.Cluster;
 import org.apache.mahout.clustering.kmeans.KMeansDriver;
 import org.apache.mahout.common.AbstractJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yandex.estimate.FreqCounter1.Mapper1;
 import org.yandex.estimate.FreqCounter1.Reducer1;
 
 import com.google.common.collect.Sets;
 import com.google.common.collect.Sets.SetView;
 
 public class ParseDataToBinaryDriver extends AbstractJob {
 	
 	private static final Logger log = LoggerFactory.getLogger(ParseDataToBinaryDriver.class);
 	public static String BINARY_OUTPUT="binary";
 	public static String BINARY_OUTPUT_KEY="binary";
 	
 	public static String PARSE_OPTION="parse";
 	public static String PARSE_OPTION_KEY="parse";
 	
 	public static String MAXIMUM_EM_RUNS="emRuns";
 	
 	public static String DUMP_TO_CLICK_EVENT="false";
 	
 	public static void main(String[] args) throws Exception {
 		ToolRunner.run(new Configuration(), new ParseDataToBinaryDriver(), args);		
 	}
 	
 	public static DefaultOptionBuilder binarySessionsOutOption() {
 	    return new DefaultOptionBuilder()
 	        .withLongName(BINARY_OUTPUT)
 	        .withRequired(true)
 	        .withArgument(
 	            new ArgumentBuilder().withName(BINARY_OUTPUT).withMinimum(1)
 	                .withMaximum(1).create())
 	        .withDescription(
 	            "The path where job will store binary search sessions")
 	        .withShortName("-bo");
 	  }
 	
 	public static DefaultOptionBuilder justParseOption() {
 	    return new DefaultOptionBuilder()
 	        .withLongName(PARSE_OPTION)
 	        .withRequired(true)
 	        .withArgument(
 	            new ArgumentBuilder().withName(PARSE_OPTION).withMinimum(1)
 	                .withMaximum(1).create())
 	        .withDescription(
 	            "The path where job will store binary search sessions")
 	        .withShortName("-bo");
 	  }	
 	
 	@Override
 	public int run(String[] arg0) throws Exception {
 		addInputOption();
 		addOutputOption();
 		addOption(justParseOption().create());
 		
 		if (parseArguments(arg0) == null) {
 		      return -1;
 		    }
 		Path input = getInputPath();
 		Path output = getOutputPath();
 		String parseFileVal=getOption(PARSE_OPTION);
 		boolean parseFile=false;
 		if (StringUtils.isNotBlank(parseFileVal))
 			parseFile=Boolean.parseBoolean(parseFileVal);
 		
 		if (getConf() == null) {
 			setConf(new Configuration());
 		}
 		run(input,output,parseFile);
 		return 0;
 	}
 	
 	public static class SessionMapper extends Mapper<Text,Session,Text,SessionArray>
 	{
 		
 
 		@Override
 		protected void map(Text key, Session value,
 				org.apache.hadoop.mapreduce.Mapper.Context context)
 				throws IOException, InterruptedException {
 //			HashSet<String> sessionQueries=new HashSet<String>();
 //			for (Writable queryObj:value.getQueries().get())
 //			{
 //				Query query=(Query)queryObj;
 //				sessionQueries.add(""+query.getId());
 //			}
 //			SetView<String> result=Sets.intersection(queries, sessionQueries);
 //			if (!result.isEmpty())
 //			{
 				String fullKey=String.format("%08d",value.getId());
				String srcKey=fullKey.substring(0, 7);
 				SessionArray array=new SessionArray();
 				array.set(new Session[]{value});
 				context.write(new Text(srcKey), array);
 //			}
 		}
 
 //		@Override
 //		protected void setup(org.apache.hadoop.mapreduce.Mapper.Context context)
 //				throws IOException, InterruptedException {
 //			Path[] files=DistributedCache.getLocalCacheFiles(context.getConfiguration());
 //			BufferedReader bufReader=new BufferedReader(new FileReader(files[0].toString()));
 //			String line;
 //			try {
 //					 while ((line = bufReader.readLine()) != null) {
 //						 queries.add(line.trim());
 //					 }
 //				 }
 //			catch (Exception e) {
 //					 e.printStackTrace();
 //				 }
 //			finally {				 
 //				bufReader.close();
 //			}
 //			
 //		}
 		
 		
 		
 	}
 	
 	public static class SessionCombiner extends Reducer<Text, SessionArray, Text,SessionArray> {
 
 		@Override
 		protected void reduce(Text arg0, Iterable<SessionArray> arg1,
 				Context arg2) throws IOException, InterruptedException {
 			Iterator<SessionArray> it=arg1.iterator();
 			List<Session> list=new ArrayList<Session>();
 			while (it.hasNext())
 			{
 				for (Writable obj:it.next().get())
 					list.add((Session)obj);	
 			}
 			
 			SessionArray array=new SessionArray();
 			array.set(list.toArray(new Session[0]));
 			arg2.write(arg0, array);
 				
 			
 		}
 	}
 	
 	public static class SessionReducer extends Reducer<Text,SessionArray,ImmutableBytesWritable,Writable>
 	{
 
 		@Override
 		protected void reduce(Text arg0, Iterable<SessionArray> values,
 				Context context) throws IOException, InterruptedException {
 			Iterator<SessionArray> obsIt = values.iterator();
 			
 			
 			while (obsIt.hasNext()) {
 				for (Writable obj:obsIt.next().get())
 				{
 					Session session = (Session) obj;
 					
 					for (Writable query : session.getQueries().get()) 
 					{
 						// log.info(((Query)query).toString());
 						Query queryObj = (Query) query;
 						for (Writable doc : queryObj.getDocs().get()) 
 						{
 							ImmutableBytesWritable clicksTableKey = new ImmutableBytesWritable(
 									Bytes.toBytes("click_event"));
 							DocEvent docEvent = (DocEvent) doc;
 
 							String id = session.getId() + "-" + queryObj.getId()
 									+ "-" + queryObj.getTime() + "-"
 									+ docEvent.getId();
 							ImmutableBytesWritable docKey = new ImmutableBytesWritable(
 									Bytes.toBytes(id));
 							Put put = new Put(docKey.get());
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("doc_id"),
 									Bytes.toBytes(docEvent.getId()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("query_id"),
 									Bytes.toBytes(docEvent.getQuery_id()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("session_id"),
 									Bytes.toBytes(docEvent.getSession_id()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("time"),
 									Bytes.toBytes(docEvent.getTime()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("distance"),
 									Bytes.toBytes(docEvent.getClick_distance()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("position"),
 									Bytes.toBytes(docEvent.getPosition()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("region"),
 									Bytes.toBytes(docEvent.getRegion()));
 							put.add(Bytes.toBytes("details"),
 									Bytes.toBytes("clicked"),
 									Bytes.toBytes(docEvent.getClicked()));
 							context.write(clicksTableKey, put);
 						}
 					}
 					ImmutableBytesWritable binarySessionsTableKey = new ImmutableBytesWritable(
 							Bytes.toBytes("binary_sessions"));
 					Put binaryPut = new Put(Bytes.toBytes(session.getId()));
 					ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
 					ObjectOutputStream output = new ObjectOutputStream(byteOutput);
 					session.write(output);
 					binaryPut.add(Bytes.toBytes("details"), Bytes.toBytes("data"),
 							byteOutput.toByteArray());
 
 					output.close();
 					byteOutput.close();
 					output = null;
 					byteOutput = null;
 					context.write(binarySessionsTableKey, binaryPut);
 				}
 				
 			}
 		}		
 	}
 	
 	private void run(Path input, Path output, boolean parseFile) throws Exception {
 		log.info("Starting YANDEX RELEVANCE!");
 		
 		Configuration conf=getConf();
 		DistributedCache.addCacheFile(URI.create("/user/gsalazar/yandex/fulltraintestqueries.txt"), conf);
 		if (parseFile)
 		{
 			Job job = new Job(conf, "Parsing log file and putting sessions to disk");
 			if (input==null)
 				throw new RuntimeException("input is null");
 			log.info("Starting parsing logs and write sessions to disk!");
 			job.setMapOutputKeyClass(Text.class);
 		    job.setMapOutputValueClass(DocObservations.class);
 		    job.setOutputKeyClass(Text.class);
 		    job.setOutputValueClass(Session.class);
 		    job.setMapperClass(UrlDocMapper.class);
 		    job.setCombinerClass(UrlDocCombiner.class);
 		    job.setReducerClass(UrlBinaryFileSessionReducer.class);    
 		    
 		    FileInputFormat.addInputPath(job, input);
 		    FileOutputFormat.setOutputPath(job, output);
 		    
		    job.setInputFormatClass(TextInputFormat.class);
 		    job.setOutputFormatClass(SequenceFileOutputFormat.class);
 		    job.setJarByClass(ParseDataToBinaryDriver.class);
 		    job.waitForCompletion(true);
 		}
 		
 	    Job job1 = new Job(conf, "Putting files to tables!!");
 	    log.info("Putting disk sessions file to HBase!");
 		job1.setMapOutputKeyClass(Text.class);
 	    job1.setMapOutputValueClass(SessionArray.class);
 	    job1.setCombinerClass(SessionCombiner.class);
	    job1.setMapperClass(SessionMapper.class);	    
 	    job1.setReducerClass(SessionReducer.class);
 	    job1.setOutputFormatClass(MultiTableOutputFormat.class);	    
 	    job1.setInputFormatClass(SequenceFileInputFormat.class);
 	    FileInputFormat.addInputPath(job1, output);
 	    job1.setJarByClass(ParseDataToBinaryDriver.class);
 	    job1.waitForCompletion(true);
 	    
 	    //TableMapReduceUtil.initTableReducerJob("click_event", UrlBinarySessionReducer.class, job1);	    
 	    	    
 	    	
 	    
 	    
 //	    conf.set(DUMP_TO_CLICK_EVENT,"false");
 //	    Job job2 = new Job(conf, "Putting files to binary_sessions");
 //	    log.info("Putting disk sessions file to HBase binary_sessions!");
 //		job2.setMapOutputKeyClass(Text.class);
 //	    job2.setMapOutputValueClass(Session.class);
 //	    job2.setMapperClass(Mapper.class);
 //	    TableMapReduceUtil.initTableReducerJob("binary_sessions", UrlBinarySessionReducer.class, job2);	    
 //	    FileInputFormat.addInputPath(job2, output);	    
 //	    job2.setInputFormatClass(SequenceFileInputFormat.class);
 //	    job2.setJarByClass(ParseDataToBinaryDriver.class);
 //	    job2.waitForCompletion(true);   
 	}
 	
 }
