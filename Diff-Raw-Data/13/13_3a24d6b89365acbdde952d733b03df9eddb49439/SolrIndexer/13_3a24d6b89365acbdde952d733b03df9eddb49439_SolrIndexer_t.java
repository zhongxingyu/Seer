 package jp.projects.miya.hadoop_solr_indexer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class SolrIndexer extends Configured implements Tool {
 	/**
 	 *
 	 */
 	private static final Logger LOG = LoggerFactory.getLogger(SolrIndexer.class);
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
 	 */
 	@Override
 	public int run(String[] args) throws Exception {
		if (args.length != 1) {
 			this.printUsage();
 			return -1;
 		} else {
 			Configuration conf = this.getConf();
 			Job job = new Job(conf);
 			if (conf.get(Constants.CSV_HEADER_PROPERTY) == null
 				|| conf.get(Constants.SOLR_URL) == null
 				|| conf.get(Constants.SEND_BUFFER_SIZE) == null
 				|| conf.get(Constants.REQUEST_THREAD_COUNT) == null) {
 				this.printUsage();
 				return -1;
 			}
 
 			job.setJarByClass(SolrIndexer.class);
 			// job.setSomeProperty(...);
 
 			job.setInputFormatClass(TextInputFormat.class);
 			job.setOutputFormatClass(NullOutputFormat.class);
 			FileInputFormat.setInputPaths(job, new Path(args[0]));
 
 			job.setMapperClass(IndexMapper.class);
 
 			job.setNumReduceTasks(0);
 
 			job.setMapOutputKeyClass(NullWritable.class);
 			job.setMapOutputValueClass(NullWritable.class);
 			job.setOutputKeyClass(NullWritable.class);
 			job.setOutputValueClass(NullWritable.class);
 
 			return job.waitForCompletion(true) ? 0 : 1;
 		}
 	}
 
 	private void printUsage() {
 		System.err.printf(
 				"Usage: %s [generic options] -D " + Constants.CSV_HEADER_PROPERTY + "=<header-list>" +
 						"-D " + Constants.SOLR_URL + "=<solr url>" +
 						"-D " + Constants.SEND_BUFFER_SIZE + "=<send buffer size>" +
 						"-D " + Constants.REQUEST_THREAD_COUNT + "=<thread count>" +
						" <input dir>\n",
 				getClass().getSimpleName());
 		ToolRunner.printGenericCommandUsage(System.err);
 	}
 
 	/**
 	 *
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		SolrIndexer.LOG.info("Solr Indexer Start..");
 		int exitCode = ToolRunner.run(new SolrIndexer(), args);
 		System.exit(exitCode);
 	}
 }
