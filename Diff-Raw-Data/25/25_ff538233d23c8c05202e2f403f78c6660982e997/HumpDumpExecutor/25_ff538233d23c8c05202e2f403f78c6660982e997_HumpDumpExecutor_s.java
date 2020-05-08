 package us.yuxin.hump;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.BlockingQueue;
 
 import com.google.common.base.Throwables;
 import com.hazelcast.client.HazelcastClient;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.compress.CompressionCodec;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 
 public class HumpDumpExecutor implements HumpExecutor {
   FileSystem fs;
   Store store;
   CompressionCodec codec;
   String codecExtension;
   String formatExtension;
   Configuration conf;
 
   BlockingQueue<String> feedbackQueue;
   HazelcastClient client;
 
   int taskCounter;
   boolean humpUpdate;
 
   StoreCounter globalCounter;
   StoreCounter singleCounter;
 
   ObjectMapper mapper;
 
   // --- Task variables
   long taskBeginTime;
   long taskEndTime;
 
   String url;
   String id;
   String name;
   String target;
   String realTarget;
 
   JsonNode task;
   ObjectNode feed;
 
   JdbcSource source;
   JdbcSourceMetadata metadata;
 
   Exception taskEx;
 
   Mapper.Context context;
   int skipCode;
 
   private final static int SKIP_CODE_NOSKIP = 0;
   private final static int SKIP_CODE_UPDATE = 1;
 
 
   @Override
   public void setup(Mapper.Context context) throws IOException, InterruptedException {
     this.context = context;
 
     conf = context.getConfiguration();
     client = HumpGridClient.getClient(conf);
     feedbackQueue = client.getQueue(Hump.HUMP_HAZELCAST_FEEDBACK_QUEUE);
 
     fs = FileSystem.get(context.getConfiguration());
     codec = null;
 
     if (conf.get(Hump.CONF_HUMP_COMPRESSION_CODEC) != null) {
      try {
        codec = (CompressionCodec) conf.getClass(Hump.CONF_HUMP_COMPRESSION_CODEC, null).newInstance();
        codecExtension = codec.getDefaultExtension();
      } catch (InstantiationException e) {
        e.printStackTrace();
        throw new IOException("Invalid compression codec", e);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        throw new IOException("Invalid compression codec", e);
      }
     }
 
     humpUpdate = conf.getBoolean(Hump.CONF_HUMP_UPDATE, false);
 
     String outputFormat = conf.get(Hump.CONF_HUMP_OUTOUT_FORMAT);
     if (outputFormat.equals("text")) {
       store = new TextStore(fs, conf, codec);
     } else {
       store = new RCFileStore(fs, conf, codec);
     }
 
     formatExtension = "." + store.getFormatId();
     store.setUseTemporary(!conf.getBoolean(Hump.CONF_HUMP_DUMP_DIRECT, false));
 
     globalCounter = new StoreCounter();
     singleCounter = new StoreCounter();
     taskCounter = 0;
     mapper = new ObjectMapper();
   }
 
 
   private void setupIdAndName() {
     if (task.get("id") != null) {
       id = task.get("id").getTextValue();
     } else if (task.get("table") != null) {
       id = task.get("table").getTextValue();
     } else {
       id = context.getTaskAttemptID().toString() + "/" + taskCounter;
     }
 
     if (task.get("name") != null) {
       name = task.get("name").getTextValue();
     } else {
       name = id;
     }
   }
 
 
   private void setupSource() {
     source = new JdbcSource();
     String dbType = "mysql";
     if (task.get("type") != null) {
       dbType = task.get("type").getTextValue();
     }
 
     if (task.get("driver") != null) {
       source.setDriver(task.get("driver").getTextValue());
     } else {
       if (dbType.equals("mysql")) {
         source.setDriver("com.mysql.jdbc.Driver");
       }
     }
 
     if (task.get("url") != null) {
       url = task.get("url").getTextValue();
     } else {
       String host = task.get("host").getTextValue();
       String port = "";
       if (task.get("port") != null) {
         port = ":" + task.get("port").getTextValue();
       }
 
       String db = task.get("db").getTextValue();
       url = "jdbc:" + dbType + "://" + host + port + "/" + db;
     }
 
     if (conf.get(Hump.CONF_HUMP_JDBC_PARAMETERS) != null) {
       if (!url.contains("?")) {
         url = url + "?" + conf.get(Hump.CONF_HUMP_JDBC_PARAMETERS);
       } else {
         url = url + "&" + conf.get(Hump.CONF_HUMP_JDBC_PARAMETERS);
       }
     }
 
     System.out.println("JDBC URL:" + url);
     source.setUrl(url);
 
     source.setUsername(task.get("username").getTextValue());
     if (task.get("password") != null)
       source.setPassword(task.get("password").getTextValue());
     if (task.get("query") != null)
       source.setQuery(task.get("query").getTextValue());
     if (task.get("table") != null) {
       String stmt = "SELECT * FROM " + task.get("table").getTextValue();
       source.setQuery(stmt);
     }
   }
 
 
   private void dumpSource() throws IOException {
     metadata = null;
     taskEx = null;
 
     try {
       source.open();
       metadata = new JdbcSourceMetadata();
       metadata.setJdbcSource(source);
 
       store.store(new Path(realTarget), source, null, singleCounter);
       source.close();
 
     } catch (SQLException e) {
       e.printStackTrace();
       taskEx = e;
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
       taskEx = e;
     }
   }
 
 
   @Override
   public void run(Mapper.Context context, Text serial, Text taskInfo) throws IOException, InterruptedException {
     System.out.println("HumpDumpExecutor.run -- " + serial.toString() + ":" + taskInfo.toString());
 
     singleCounter.reset();
     skipCode = SKIP_CODE_NOSKIP;
 
     ++taskCounter;
     taskBeginTime = System.currentTimeMillis();
 
     task = mapper.readValue(taskInfo.toString(), JsonNode.class);
     setTarget(task.get("target").getTextValue());
 
     if (humpUpdate) {
       if (isTargetExist()) {
         skipCode = SKIP_CODE_UPDATE;
       }
     }
 
     if (skipCode == SKIP_CODE_NOSKIP) {
       setupSource();
       dumpSource();
     }
 
     taskEndTime = System.currentTimeMillis();
 
     singleCounter.during = taskEndTime - taskBeginTime;
     globalCounter.plus(singleCounter);
 
     if (taskEx != null) {
       System.out.println("ERROR");
     } else if (skipCode != SKIP_CODE_NOSKIP) {
       System.out.println("SKIP");
     } else {
       System.out.println("OK");
     }
 
     setupIdAndName();
     feedback();
   }
 
 
   private void setTarget(String target) {
     this.target = target;
 
     if (this.target.indexOf(0) != '/') {
       this.target = conf.get(Hump.CONF_HUMP_OUTPUT_BASEPATH, "") + "/" + target;
     } else {
       this.target = target;
     }
 
 
     realTarget = this.target + formatExtension;
     if (codec != null)
       realTarget = realTarget + codecExtension;
   }
 
 
   private boolean isTargetExist() throws IOException {
     return fs.exists(new Path(realTarget));
   }
 
 
   private void feedback() throws IOException {
     // ---- Send feed.
     feed = mapper.createObjectNode();
     feed.put("id", id);
     feed.put("name", name);
     feed.put("beginTime", new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date(taskBeginTime)));
     feed.put("target", task.get("target"));
     feed.put("path", realTarget);
     if (codec != null) {
       feed.put("cext", codecExtension);
     }
     feed.put("taskid", context.getTaskAttemptID().toString());
 
     if (taskEx != null) {
       feed.put("code", Hump.RETCODE_ERROR);
       feed.put("status", "ERROR");
       feed.put("message", taskEx.getMessage());
       feed.put("exception", Throwables.getStackTraceAsString(taskEx));
     } else if (skipCode == SKIP_CODE_UPDATE) {
       feed.put("code", Hump.RETCODE_SKIP);
       feed.put("status", "SKIP");
       feed.put("message", "[" + realTarget + "] existed");
     } else {
       feed.put("status", "OK");
       feed.put("code", Hump.RETCODE_OK);
       feed.put("rows", singleCounter.rows);
       feed.put("cells", singleCounter.cells);
       feed.put("nullCells", singleCounter.nullCells);
       feed.put("cellBytes", singleCounter.inBytes);
       feed.put("fileBytes", singleCounter.outBytes);
 
       feed.put("during", singleCounter.during);
       feed.put("columns", metadata.columnNames);
       feed.put("columnTypes", metadata.columnHiveTypes);
       feed.put("format", store.getFormatId());
     }
     feedbackQueue.offer(mapper.writeValueAsString(feed));
   }
 
   @Override
   public void cleanup(Mapper.Context context) throws IOException, InterruptedException {
     fs.close();
   }
 }
