 package wikigraph;
 
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Reducer.Context;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 
 import edu.umd.cloud9.collection.XMLInputFormat;
 import edu.umd.cloud9.io.pair.PairOfStringLong;
 import edu.umd.cloud9.io.pair.PairOfStrings;
 	
 	public class BuildUserEditGraph extends Configured implements Tool {
 	    private static final Logger LOG = Logger.getLogger(BuildUserEditGraph.class);
 
         // users & articles with weights?
  	   // users to users consecutive edits with weights
  	   // some sort of user editing diagram
  	   // time to next edit
  	   // highly protected/controversial articles
 	    private static class EditGraphMapper extends Mapper<LongWritable, Text, PairOfStrings, IntWritable> {
 
     	private static String wikistart = "2001-01-01T00:00:00Z";
     	private static long wikistartmillis = javax.xml.bind.DatatypeConverter.parseDateTime(wikistart).getTimeInMillis();
     	private static long millisinday = 86400000;
     	static final Pattern titlePattern = Pattern.compile(".*<title>(.*)<\\/title>.*");
     	static final Pattern beginRevisionPattern = Pattern.compile(".*<revision>.*");
     	static final Pattern endRevisionPattern = Pattern.compile(".*<\\/revision>.*");
     	static final Pattern userNamePattern = Pattern.compile(".*<username>(.*)<\\/username>.*");
     	static final Pattern ipPattern = Pattern.compile(".*<ip>(.*)<\\/ip>.*");
     	static final Pattern nsPattern = Pattern.compile(".*<ns>(.*)<\\/ns>.*");
     	static final Pattern timestampPattern = Pattern.compile(".*<timestamp>(.*)<\\/timestamp>.*");
     	static final Pattern bytesPattern = Pattern.compile(".*<text id=\"(.*)\" bytes=\"(.*)\" \\/>.*");
 		
     	static TreeMap<Long,RevisionRecord> revisionMap = new TreeMap<Long,RevisionRecord>();
     	static TreeMap<PairOfStrings,Integer> userCoEditCounts = new TreeMap<PairOfStrings,Integer>();
     	
         public void map(LongWritable key, Text p, Context context)
                 throws IOException, InterruptedException {
                
                // users & articles with weights?
         	   // users to users consecutive edits with weights
         	   // some sort of user editing diagram
         	   // time to next edit
         	   // highly protected/controversial articles
             String lines[] = p.toString().split("\n");
             revisionMap.clear();
             userCoEditCounts.clear();
             //System.out.println("key = " + key);
             Matcher m;
             String title = null;
             String user = null;
             String ip = null;
             String ns = null;
             String timestamp = null;
             String bytes = null;
 
             long lastTime = 0;
             //Text userOut = null;
             for(String line: lines){
 				//System.out.println("LINE " + line);
             	m = titlePattern.matcher(line);
             	if((m = endRevisionPattern.matcher(line)).matches()){
 
 
             		if(title != null && (user != null || ip != null)
             				&& ns != null && timestamp != null && bytes != null){
 	            		String name;
             			if(user == null){
             				name = ip;
             			}else{
             				name = user;
             			}
             			//userOut = new Text();
             			//userOut.set(name);
             			long time = parseTime(timestamp);
             			
             			RevisionRecord r = new RevisionRecord(Integer.parseInt(ns),time,0,title,Integer.parseInt(bytes));
             			r.setUsername(name);
             			//System.out.println("user = " + name);
             			//System.out.println("last time = " + lastTime);
             			//output.collect(userOut, r);
             			revisionMap.put(time, r);
             			
             			
 					}
             		bytes = null;
             		user = null;
         			ip = null;
         			timestamp = null;
             	}else if((m = titlePattern.matcher(line)).matches()){
             		title = m.group(1);
             		//System.out.println("\tTITLE " + title);
             	}else if((m = userNamePattern.matcher(line)).matches()){
             		user = m.group(1);
             		//System.out.println("\tUSER " + user);
             	}else if((m = ipPattern.matcher(line)).matches()){
             		ip = m.group(1);
             		//System.out.println("\tIP USER " + ip);
             	}else if((m = nsPattern.matcher(line)).matches()){
             		ns = m.group(1);
             		//System.out.println("\tNS " + ns);
             	}else if((m = timestampPattern.matcher(line)).matches()){
             		timestamp = m.group(1);
             		//System.out.println("\tTIMESTAMP " + timestamp);
             	}else if((m = bytesPattern.matcher(line)).matches()){
             		bytes = m.group(2);
             		//System.out.println("\tBYTES " + bytes);
             	}
             }
             
             lastTime = 0;
             long time = 0;
             String lastUser = null;
             PairOfStrings userEdge;
             for(long t : revisionMap.keySet()){
             	RevisionRecord r = revisionMap.get(t);
             	String name = r.getUsername();
 				if(lastUser != null){
             		userEdge = new PairOfStrings();
             		userEdge.set(lastUser, name);
             		if(!userCoEditCounts.containsKey(userEdge)) userCoEditCounts.put(userEdge, 0);
             		userCoEditCounts.put(userEdge, userCoEditCounts.get(userEdge) + 1);
 				}
             	//
 				lastUser = name;
             }
             
         	for(PairOfStrings edge : userCoEditCounts.keySet()){
 				IntWritable count = new IntWritable();
         		count.set(userCoEditCounts.get(edge));
 				context.write(edge, count);
 				//System.out.println("userEdge = " + edge + " count = " + count);
         	}
             
         }
         
 	    
 	    //<timestamp>2004-02-25T18:55:21Z</timestamp>
 	    //yyyy-MM-dd'T'HH:mm:ssz
        public static long parseTime(String timestr) {
           
           Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(timestr);
           long time = (calendar.getTimeInMillis() - wikistartmillis)/millisinday;
 		  return time;
           /*
 		  Date t; 
           try {
 			t = ft.parse(timestr);
 			return t.getTime();
 		  } catch (java.text.ParseException e) {
 				// TODO Auto-generated catch block
 			e.printStackTrace();
 			return 0;
 		  }	
 		  */ 
 	       
        }
     }
 
 	    
 	    private static class EditGraphReducer extends Reducer<PairOfStrings, IntWritable, PairOfStrings, IntWritable> {
 			
 			@Override
 			public void reduce(PairOfStrings edge, Iterable<IntWritable> counts, Context context)
 			       throws IOException, InterruptedException {
 				//System.out.println("key = " + key);
 				int total = 0;
 
 				Iterator<IntWritable> countIt = counts.iterator();
 				while(countIt.hasNext()){
 					IntWritable c = countIt.next();
 					total += c.get();
 				}
 
 				if(total > 1){
 					IntWritable countOut = new IntWritable();
 					countOut.set(total);
 					context.write(edge, countOut);
 				}
 				
 			}
 			
 
 	    }
 
 
 	    
 	 
 	    private static final String INPUT = "input";
 	    private static final String OUTPUT = "output";
 	    	    
 	    @SuppressWarnings("static-access")
 	    @Override
 	    public int run(String[] args) throws Exception {
 	        Options options = new Options();
 	        options.addOption(OptionBuilder.withArgName("path")
 	                .hasArg().withDescription("bz2 input path").create(INPUT));
 	        options.addOption(OptionBuilder.withArgName("path")
 	                .hasArg().withDescription("output path").create(OUTPUT));
 	        
 	        CommandLine cmdline;
 	        CommandLineParser parser = new GnuParser();
 	        try {
 	            cmdline = parser.parse(options, args);
 	        } catch (ParseException exp) {
 	            System.err.println("Error parsing command line: " + exp.getMessage());
 	            return -1;
 	        }
 
 	        if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)){ 
 	            HelpFormatter formatter = new HelpFormatter();
 	            formatter.setWidth(120);
 	            formatter.printHelp(this.getClass().getName(), options);
 	            ToolRunner.printGenericCommandUsage(System.out);
 	            return -1;
 	        }
 
 	        String inputPath = cmdline.getOptionValue(INPUT);
 	        String outputPath = cmdline.getOptionValue(OUTPUT);
 
 	        
 
 	        LOG.info("Tool name: " + this.getClass().getName());
 	        LOG.info(" - input file: " + inputPath);
 	        LOG.info(" - output file: " + outputPath);
 
 	        Configuration conf = getConf();
 	        // Set heap space - using old API
 	        conf.set("mapred.job.map.memory.mb", "2048");
 	        conf.set("mapred.map.child.java.opts", "-Xmx2048m");
 	        conf.set("mapred.job.reduce.memory.mb", "6144");
 	        conf.set("mapred.reduce.child.java.opts", "-Xmx6144m");
 	        conf.set("xmlinput.start","page");
 	        conf.set("xmlinput.end","page");
 	        //conf.set("mapred.child.java.opts", "-Xmx2048m");
 
 	        Job job = Job.getInstance(conf);
 	        //JobConf conf = new JobConf(getConf(), BuildUserProfile.class);
 	        job.setJobName(String.format("BuildUserEditGraph[%s: %s, %s: %s]", INPUT, inputPath, OUTPUT, outputPath));
 	               
 
 	        job.setNumReduceTasks(20);
 
 	        job.setMapperClass(EditGraphMapper.class);
 	        job.setReducerClass(EditGraphReducer.class);
 	        job.setCombinerClass(EditGraphReducer.class);
 	        //job.setPartitionerClass(UserPartitioner.class);
 	        
 	        //conf.setInputFormat(WikipediaPageInputFormat.class);
 	        job.setInputFormatClass(XMLInputFormat.class);
 	        job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	        //conf.setOutputFormat(TextOutputFormat.class);
 	        
 	        
 	        job.setOutputKeyClass(PairOfStrings.class);
 	        job.setOutputValueClass(IntWritable.class);
 	        
 	        FileSystem fs = FileSystem.get(conf);        
 	        Path outPath = new Path(outputPath);
 	        
 	        FileInputFormat.setInputPaths(job, new Path(inputPath));
 	        FileOutputFormat.setOutputPath(job, outPath);
 	        
 	        // Delete the output directory if it exists already.
 	        fs.delete(outPath, true);
 
 	        long startTime = System.currentTimeMillis();
 	        job.waitForCompletion(true);
 	        LOG.info("Total Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
 	        
 	        return 0;
 	    }
 	    public BuildUserEditGraph() {}
 
 	    public static void main(String[] args) throws Exception {
 	        ToolRunner.run(new BuildUserEditGraph(), args);
 	    }
 	}
 
 
