 package nl.tudelft.in4325.a1.indexing;
 
 import java.io.File;
 
 import nl.tudelft.in4325.a1.normalization.AdvancedNormalizationMapper;
 import nl.tudelft.in4325.a1.utils.XmlInputFormat;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 public class DocLevelIndexDriver {
 	
 	//private static final Logger LOGGER = LoggerFactory.getLogger(SimpleNormalizationDriver.class);
 	
 	private static final String PROPERTIES_FILE = "conf/conf.ini";
 	private static final String DEFAULT_PROPERTIES_FILE = "conf/conf.default.ini";
 	private static final String JOB = "DocLevelIndex";
 	
 	public static void main(String[] args) throws Exception{
 
 		//reading input and output paths from configuration file
 		String propertiesPath = DEFAULT_PROPERTIES_FILE;
 		
 		if (new File(PROPERTIES_FILE).exists()){
 			propertiesPath = PROPERTIES_FILE;
 		}
 		
 		Configuration propertiesConfig = null;
 		propertiesConfig = new PropertiesConfiguration(propertiesPath);
		String input = propertiesConfig.getString("source-input");
		String output = propertiesConfig.getString("doc-level-index-output");
 	
 		//configuring Hadoop and running the job
 		org.apache.hadoop.conf.Configuration hadoopConfig = new org.apache.hadoop.conf.Configuration();
 		hadoopConfig.set("xmlinput.start","<page>") ;
 		hadoopConfig.set("xmlinput.end","</page>") ;
 		
         //String[] otherArgs = new GenericOptionsParser(hadoopConfig,args).getRemainingArgs();
         
         Job job = new Job(hadoopConfig, JOB);
         job.setJarByClass(DocLevelIndexDriver.class);
         job.setMapperClass(AdvancedNormalizationMapper.class);
         job.setReducerClass(DocLevelIndexReducer.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(TextArrayWritable.class);
         job.setInputFormatClass(XmlInputFormat.class);
         
         FileInputFormat.addInputPath(job, new Path(input));
         Path outputPath = new Path(output); 
         FileOutputFormat.setOutputPath(job, outputPath);
         
         FileSystem dfs = FileSystem.get(outputPath.toUri(), hadoopConfig);
         if (dfs.exists(outputPath)) {
         	dfs.delete(outputPath, true);
         }
 
         System.exit(job.waitForCompletion(true) ? 0 : 1);
 	}
 
 }
