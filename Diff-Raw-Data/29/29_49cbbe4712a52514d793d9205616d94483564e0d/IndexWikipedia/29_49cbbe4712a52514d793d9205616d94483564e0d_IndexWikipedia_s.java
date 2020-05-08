 package com.springsense.wikiindex;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 
 import edu.umd.cloud9.io.JSONObjectWritable;
 
 public class IndexWikipedia extends Configured implements Tool {
 	private static final String MATRIX_DIR_OPTION = "matrixDir";
 
 	private static final Logger LOG = Logger.getLogger(IndexWikipedia.class);
 
 	private static final String INPUT_OPTION = "input";
 	private static final String OUTPUT_OPTION = "output";
 
 	public IndexWikipedia() {
 		super();
 	}
 
 	@SuppressWarnings("static-access")
 	@Override
 	public int run(String[] args) throws Exception {
 		Options options = new Options();
 		options.addOption(OptionBuilder.withArgName("path").hasArg()
 				.withDescription("XML dump file").create(INPUT_OPTION));
 		options.addOption(OptionBuilder.withArgName("path").hasArg()
 				.withDescription("output path").create(OUTPUT_OPTION));
 		options.addOption(OptionBuilder.withArgName("path").hasArg()
 				.withDescription("matrix data directory")
 				.create(MATRIX_DIR_OPTION));
 
 		CommandLine cmdline;
 		CommandLineParser parser = new GnuParser();
 		try {
 			cmdline = parser.parse(options, args);
 		} catch (ParseException exp) {
 			System.err.println("Error parsing command line: "
 					+ exp.getMessage());
 			return -1;
 		}
 
 		if (!cmdline.hasOption(INPUT_OPTION)
 				|| !cmdline.hasOption(OUTPUT_OPTION)
 				|| !cmdline.hasOption(MATRIX_DIR_OPTION)) {
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp(this.getClass().getName(), options);
 			ToolRunner.printGenericCommandUsage(System.out);
 			return -1;
 		}
 
 		String inputPath = cmdline.getOptionValue(INPUT_OPTION);
 		String outputPath = cmdline.getOptionValue(OUTPUT_OPTION);
 		String matrixDir = cmdline.getOptionValue(MATRIX_DIR_OPTION);
 
 		LOG.info("Tool name: " + this.getClass().getName());
 		LOG.info(" - XML dump file: " + inputPath);
 		LOG.info(" - output path: " + outputPath);
 		LOG.info(" - matrix data dir: " + matrixDir);
 
 		Configuration conf = getConf();
 
 		conf.set(IndexWikiMapper.DISAMBIG_MATRIX_DATA_DIR_KEY, matrixDir);
 		Job job = new Job(conf, String.format(
 				"IndexWikipedia[%s: %s, %s: %s, %s: %s]", INPUT_OPTION,
 				inputPath, OUTPUT_OPTION, outputPath, MATRIX_DIR_OPTION,
 				matrixDir));
 
		job.setNumReduceTasks(0);
 
 		FileInputFormat.setInputPaths(job, new Path(inputPath));
 		FileOutputFormat.setOutputPath(job, new Path(outputPath));
 
 		job.setInputFormatClass(WikipediaPageInputFormat2.class);
 		job.setOutputFormatClass(TextOutputFormat.class);
 
 		job.setJarByClass(getClass());
 		job.setMapperClass(IndexWikiMapper.class);
 		job.setReducerClass(JsonMultivalueMergeReducer.class);
 		job.setOutputKeyClass(Text.class);
 		job.setOutputValueClass(JSONObjectWritable.class);
 
 		return job.waitForCompletion(true) ? 0 : 1;
 	}
 
 	public static void main(String[] args) throws Exception {
 		ToolRunner.run(new IndexWikipedia(), args);
 	}
 }
