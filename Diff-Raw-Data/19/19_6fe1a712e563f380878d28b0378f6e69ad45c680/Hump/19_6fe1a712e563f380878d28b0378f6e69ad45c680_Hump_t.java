 package us.yuxin.hump;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URL;
 import java.util.concurrent.BlockingQueue;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.config.GroupConfig;
 import com.hazelcast.core.Hazelcast;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 // TODO Hazelcast default configuration file.
 // TODO Counter Implement.
 // TODO Hazelcast Logging configuration.
 // TODO Shared library path can be set.
 
 public class Hump extends Configured implements Tool {
   public static final String HUMP_HAZELCAST_GROUP = "hump";
   public static final String HUMP_HAZELCAST_PASSWORD = "humpps";
   public static final String HUMP_HAZELCAST_TASK_QUEUE = "hump.task.queue";
   public static final String HUMP_HAZELCAST_FEEDBACK_QUEUE = "hump.feedback.queue";
 
   public static final int HUMP_TASKS = 5;
 
   public static final String CONF_HUMP_COMPRESSION_CODEC = "hump.compression.codec";
   public static final String CONF_HUMP_HAZELCAST_ENDPOINT = "hump.hazelcast.endpoint";
   public static final String CONF_HUMP_HAZELCAST_GROUP = "hump.hazelcast.group";
   public static final String CONF_HUMP_HAZELCAST_PASSWORD = "hump.hazelcast.password";
   public static final String CONF_HUMP_HAZELCAST_LOGGING_TYPE = "hump.hazelcast.logging.type";
   public static final String CONF_HUMP_TASKS = "hump.tasks";
   public static final String CONF_HUMP_TASK_CLASS = "hump.task.class";
   public static final String CONF_HUMP_TASK_SHUFFLE = "hump.task.shuffle";
   public static final String CONF_HUMP_JDBC_PARAMETERS = "hump.jdbc.parameters";
   public static final String CONF_HUMP_RESULT_FULL = "hump.result.full";
   public static final String CONF_HUMP_RESULT_SUMMARY = "hump.result.summary";
   public static final String CONF_HUMP_RESULT_FAILURE = "hump.result.failure";
 
  public static final int RETCODE_ERROR = 0;
  public static final int RETCODE_OK = 1;
 
   BlockingQueue<String> taskQueue;
   BlockingQueue<String> feedbackQueue;
 
   String argv[];
   HumpFeeder feeder;
   Thread feederThread;
 
   HumpCollector collector;
   Thread collectorThread;
 
 
   public int run(String[] argv) throws Exception {
     this.argv = argv;
 
     gridInit();
     feederInit();
     collectorInit();
 
     runJob();
 
     collectorShutdown();
     gridShutdown();
 
     return 0;
   }
 
 
   private void gridInit() {
     Configuration conf = getConf();
     conf.setIfUnset(CONF_HUMP_HAZELCAST_GROUP, HUMP_HAZELCAST_GROUP);
     conf.setIfUnset(CONF_HUMP_HAZELCAST_PASSWORD, HUMP_HAZELCAST_PASSWORD);
 
     Config cfg = new Config();
 
     cfg.setGroupConfig(new GroupConfig(
       conf.get(CONF_HUMP_HAZELCAST_GROUP),
       conf.get(CONF_HUMP_HAZELCAST_PASSWORD)));
 
     cfg.setProperty("hazelcast.logging.type",
       conf.get(CONF_HUMP_HAZELCAST_LOGGING_TYPE, "none"));
 
     cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
     cfg.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
     Hazelcast.init(cfg);
 
     InetSocketAddress addr = Hazelcast.getCluster().getLocalMember().getInetSocketAddress();
     conf.set(CONF_HUMP_HAZELCAST_ENDPOINT, addr.getAddress().toString().substring(1) + ":" + addr.getPort());
 
     taskQueue = Hazelcast.getQueue(HUMP_HAZELCAST_TASK_QUEUE);
     feedbackQueue = Hazelcast.getQueue(HUMP_HAZELCAST_FEEDBACK_QUEUE);
   }
 
 
   private void feederInit() throws IOException {
     Configuration conf = getConf();
     File sources[];
 
     if (argv != null && argv.length >= 1) {
       sources = new File[argv.length];
       for (int i = 0; i < sources.length; ++i)
         sources[i] = new File(argv[i]);
     } else {
       sources = new File[]{new File("hump-tasks.json")};
     }
 
     feeder = new HumpFeeder();
     feeder.setup(conf, sources, taskQueue, feedbackQueue,
       conf.getInt(CONF_HUMP_TASKS, HUMP_TASKS));
 
     feederThread = new Thread(feeder);
     feederThread.setDaemon(true);
     feederThread.setName("Hump Feeder");
     feederThread.start();
   }
 
 
   private void collectorInit() {
     collector = new HumpCollector();
     collector.setup(getConf(), feedbackQueue);
 
     collectorThread = new Thread(collector);
     collectorThread.setDaemon(true);
     collectorThread.setName("Hump Collector");
     collectorThread.start();
   }
 
 
   private void gridShutdown() {
     Hazelcast.shutdownAll();
   }
 
   private void collectorShutdown() {
     feedbackQueue.offer("STOP");
     try {
       collectorThread.join();
     } catch (InterruptedException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
   }
 
 
   private void runJob() throws IOException, ClassNotFoundException, InterruptedException {
     Configuration conf = getConf();
 
     FileSystem fs = FileSystem.get(conf);
     FileStatus[] fileStatuses = fs.listStatus(new Path("/is/app/hump/lib"));
 
     for (FileStatus fileStatus : fileStatuses) {
       if (fileStatus.getPath().toString().endsWith(".jar")) {
         DistributedCache.addArchiveToClassPath(fileStatus.getPath(), conf);
       }
     }
     fs.close();
 
     Job job = new Job(conf);
 
     job.setJobName("Hump-Sample");
     job.setJarByClass(HumpMapper.class);
     job.setMapperClass(HumpMapper.class);
 
     job.setMapOutputKeyClass(Text.class);
     job.setMapOutputValueClass(NullWritable.class);
     job.setOutputFormatClass(NullOutputFormat.class);
 
     job.setInputFormatClass(HumpInputFormat.class);
     FileInputFormat.addInputPath(job, new Path("ignored"));
 
     job.setNumReduceTasks(0);
     job.waitForCompletion(false);
   }
 
 
   protected static Configuration prepareConfiguration() {
     Configuration conf = new Configuration();
     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     if (classLoader == null) {
       classLoader = Hump.class.getClassLoader();
     }
 
     URL defaultURL = classLoader.getResource("hump-default.xml");
     if (defaultURL != null)
       conf.addResource(defaultURL);
 
     URL siteURL = classLoader.getResource("hump-site.xml");
     if (siteURL != null)
       conf.addResource(siteURL);
 
     return conf;
   }
 
 
   public static void main(String[] args) throws Exception {
     Configuration conf = prepareConfiguration();
     int res = ToolRunner.run(conf, new Hump(), args);
     System.exit(res);
   }
 }
