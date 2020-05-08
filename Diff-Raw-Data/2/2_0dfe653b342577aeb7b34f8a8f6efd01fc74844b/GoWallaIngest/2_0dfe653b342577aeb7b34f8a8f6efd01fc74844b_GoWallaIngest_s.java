 package cd.ingest.gowalla;
 
 import cd.ingest.CBTableAssistant;
 import cd.ingest.RowIDGenerator;
 import cloudbase.core.client.mapreduce.CloudbaseFileOutputFormat;
 import cloudbase.core.client.mapreduce.lib.partition.RangePartitioner;
 import cloudbase.core.data.Key;
 import cloudbase.core.data.Value;
 import cloudbase.core.util.CachedConfiguration;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Pattern;
 
 /**
  * User: bfemiano
  * Date: 8/21/12
  * Time: 3:31 PM
  */
 public class GoWallaIngest extends Configured implements Tool {
     public static final String NAME = "gowalla_ingest";
 
     private Configuration conf;
 
     public GoWallaIngest(Configuration conf) {
         this.conf = conf;
     }
 
     public int run(String[] args) throws Exception {
 
         if(args.length < 8) {
             System.err.println(printUsage());
             System.exit(0);
         }
 
         Job job = new Job(conf, "GoWalla ingest to Cloudbase");
         job.setInputFormatClass(TextInputFormat.class);
         job.setMapperClass(GoWallaIngestMapper.class);
         job.setMapOutputKeyClass(Text.class);
         job.setMapOutputValueClass(Text.class);
         job.setReducerClass(GoWallaIngestReducer.class);
         job.setPartitionerClass(RangePartitioner.class);
         job.setJarByClass(getClass());
 
         String input = args[0];
         String outputStr = args[1];
         String instanceName = args[2];
         String tableName = args[3];
         String user = args[4];
         String pass = args[5];
         String zooQuorum = args[6];
         String localSplitFile = args[7];
 
         FileInputFormat.addInputPath(job, new Path(input));
         CloudbaseFileOutputFormat.setOutputPath(job, clearOutputDir(outputStr));
         job.setOutputFormatClass(CloudbaseFileOutputFormat.class);
 
         CBTableAssistant tableAssistant = new CBTableAssistant.Builder().
                 setInstanceName(instanceName).setTableName(tableName).setUser(user)
                 .setPassword(pass).setZooQuorum(zooQuorum).build();
 
         String splitFileInHDFS = "/tmp/splits.txt";
         int numSplits = 0;
         tableAssistant.createTableIfNotExists();
         if(localSplitFile != null) {
             numSplits = tableAssistant.presplitAndWriteHDFSFile(conf, localSplitFile, splitFileInHDFS);
         }
         RangePartitioner.setSplitFile(job, splitFileInHDFS);
         job.setNumReduceTasks(numSplits + 1);
 
         if(job.waitForCompletion(true)) {
             tableAssistant.loadImportDirectory(conf, outputStr);
         }
         return 0;
     }
 
     private String printUsage() {
         return "gowalla_ingest <input> <output> <instance_name> <tablename> " +
                 "<username> <password> <zoohosts> <splits_file_path>";
     }
 
     private Path clearOutputDir(String outputStr)
             throws IOException {
         FileSystem fs = FileSystem.get(conf);
         Path path = new Path(outputStr);
         fs.delete(path, true);
         return path;
     }
 
     public static class GoWallaIngestMapper
             extends Mapper<LongWritable, Text, Text, Text> {
 
         private Text outKey = new Text();
         private static final Pattern tabPattern = Pattern.compile("[\\t]");
         private RowIDGenerator gen = new GoWallaRowIDGenerator();
 
         protected void map(LongWritable key, Text value,
                            Context context) throws IOException, InterruptedException {
 
             String[] values = tabPattern.split(value.toString());
             if(values.length == 5)  {
                 String [] rowKeyFields = new String[] {values[0], values[1]}; //userid, dtg
                 outKey.set(gen.getRowID(rowKeyFields));
                 context.write(outKey, value);
             } else {
                 context.getCounter("GoWalla Ingest", "malformed records").increment(1l);
             }
         }
     }
 
     public static class GoWallaIngestReducer
             extends Reducer<Text, Text, Key, Value> {
 
         private Key outKey;
         private Value outValue = new Value();
         private Text cf = new Text("cf");
         private Text qual = new Text();
         private static final Pattern tabPattern = Pattern.compile("[\\t]");
         private static final Pattern dtgSplitPoint = Pattern.compile("[T]");
         private Random ran = new Random();
 
         private static Map<Integer, Text> cellVis = new HashMap();
         static {
             cellVis.put(1, new Text("testing1&testing2"));
             cellVis.put(2, new Text("testing3"));
             cellVis.put(3, new Text("testing4|testing5"));
             cellVis.put(4, new Text("(testing4&testing6)|testing7"));
             cellVis.put(5, new Text("testing7|testing8|testing9"));
             cellVis.put(6, new Text("testing1&testing2&testing3"));
             cellVis.put(7, new Text("testing2|testing5"));
         }
 
         @Override
         protected void reduce(Text key, Iterable<Text> values,
                               Context context) throws IOException, InterruptedException {
 
             int found = 0;
             for(Text value : values) {
                 String[] cells = tabPattern.split(value.toString());
                 if(cells.length == 5) {
                     if(found < 1) { //don't write duplicates
                         String[] dtgPortions = dtgSplitPoint.split(cells[1]);
                         if(dtgPortions.length < 2)
                             context.getCounter("Go Walla Ingest", "invalid date").increment(1l);
                         String dtg = dtgPortions[0];
                        String time = dtgPortions[1].substring(0,dtgPortions[1].length());
                         write(context,  key , dtg, "dtg");
                         write(context,  key , cells[2], "lat");
                         write(context,  key , cells[3], "loc");
                         write(context,  key , time, "time");
                         write(context,  key , cells[4], "user");
                     } else {
                        context.getCounter("GoWalla Ingest", "duplicates").increment(1l);
                     }
                 } else {
                     context.getCounter("GoWalla Ingest", "malformed records missing a field").increment(1l);
                 }
                 found++;
             }
         }
 
         private void write(Context context, Text key, String cell, String qualStr)
                 throws IOException, InterruptedException {
             if(!cell.toUpperCase().equals("NULL")) {
                 qual.set(qualStr);
                 outKey = new Key(key, cf, qual, getRandomCellVis(), System.currentTimeMillis());
                 outValue.set(cell.getBytes());
                 context.write(outKey, outValue);
             }
         }
 
         private Text getRandomCellVis() {   //random cell label between p1 - p9
              return cellVis.get(ran.nextInt(6) + 1);
         }
 
     }
 
     @Override
     public void setConf(Configuration conf) {
         this.conf = conf;
     }
 
     @Override
     public Configuration getConf() {
         return conf;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = CachedConfiguration.getInstance();
         args = new GenericOptionsParser(conf, args).getRemainingArgs();
         ToolRunner.run(new GoWallaIngest(conf), args);
     }
 }
