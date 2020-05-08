 package us.yuxin.hump;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.BlockingQueue;
 
 import com.hazelcast.client.HazelcastClient;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.compress.CompressionCodec;
 import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.thirdparty.guava.common.base.Throwables;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 
 public class HumpDumpExecutor implements HumpExecutor {
   FileSystem fs;
   RCFileStore store;
   Configuration conf;
 
   BlockingQueue<String> feedbackQueue;
   HazelcastClient client;
 
   StoreCounter globalCounter;
   StoreCounter singleCounter;
   int taskCounter;
 
   @Override
   public void setup(Mapper.Context context) throws IOException, InterruptedException {
     conf = context.getConfiguration();
     client = HumpGridClient.getClient(conf);
     feedbackQueue = client.getQueue(Hump.HUMP_HAZELCAST_FEEDBACK_QUEUE);
 
     fs = FileSystem.get(context.getConfiguration());
     CompressionCodec codec = null;
 
     globalCounter = new StoreCounter();
     singleCounter = new StoreCounter();
     taskCounter = 0;
 
     if (conf.get(Hump.CONF_HUMP_COMPRESSION_CODEC) != null) {
       try {
         codec = (CompressionCodec) conf.getClass(Hump.CONF_HUMP_COMPRESSION_CODEC, null).newInstance();
       } catch (InstantiationException e) {
         e.printStackTrace();
         throw new IOException("Invalid compression codec", e);
       } catch (IllegalAccessException e) {
         e.printStackTrace();
         throw new IOException("Invalid compression codec", e);
       }
     }
     store = new RCFileStore(fs, conf, codec);
   }
 
   @Override
   public void run(Mapper.Context context, Text serial, Text taskInfo) throws IOException, InterruptedException {
     System.out.println("HumpDumpExecutor.run -- " + serial.toString() + ":" + taskInfo.toString());
     singleCounter.reset();
     ++taskCounter;
     long beginTime;
     long endTime;
 
     beginTime = System.currentTimeMillis();
 
     ObjectMapper mapper = new ObjectMapper();
     JsonNode root = mapper.readValue(taskInfo.toString(), JsonNode.class);
 
     JdbcSource source = new JdbcSource();
 
     String dbType = "mysql";
     if (root.get("type") != null) {
       dbType = root.get("type").getTextValue();
     }
 
     if (root.get("driver") != null) {
       source.setDriver(root.get("driver").getTextValue());
     } else {
       if (dbType.equals("mysql")) {
         source.setDriver("com.mysql.jdbc.Driver");
       }
     }
 
     String url;
 
     if (root.get("url") != null) {
       url = root.get("url").getTextValue();
     } else {
       String host = root.get("host").getTextValue();
       String port = "";
       if (root.get("port") != null) {
         port = ":" + root.get("port").getTextValue();
       }
 
       String db = root.get("db").getTextValue();
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
 
     source.setUsername(root.get("username").getTextValue());
     if (root.get("password") != null)
       source.setPassword(root.get("password").getTextValue());
     if (root.get("query") != null)
       source.setQuery(root.get("query").getTextValue());
     if (root.get("table") != null) {
       String stmt = "SELECT * FROM " + root.get("table").getTextValue();
       source.setQuery(stmt);
     }
 
     JdbcSourceMetadata metadata = null;
     String target = root.get("target").getTextValue();
     Exception ex = null;
     try {
       source.open();
       metadata = new JdbcSourceMetadata();
       metadata.setJdbcSource(source);
 
       store.store(new Path(target), source, null, singleCounter);
       source.close();
       endTime = System.currentTimeMillis();
       singleCounter.during = endTime - beginTime;
     } catch (SQLException e) {
       e.printStackTrace();
       ex = e;
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
       ex = e;
     }
 
     globalCounter.plus(singleCounter);
     if (ex != null) {
       System.out.println("ERROR");
     } else {
       System.out.println("OK...");
     }
 
     // ---- Send feedback.
     ObjectNode feedback = mapper.createObjectNode();
 
     String id;
     String name;
     if (root.get("id") != null) {
       id = root.get("id").getTextValue();
     } else if (root.get("table") != null) {
       id = root.get("table").getTextValue();
     } else {
       id = context.getTaskAttemptID().toString() + "/" + taskCounter;
     }
 
     if (root.get("name") != null) {
       name = root.get("name").getTextValue();
     } else {
       name = id;
     }
 
     feedback.put("id", id);
     feedback.put("name", name);
     feedback.put("beginTime", new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date(beginTime)));
     feedback.put("target", root.get("target").getTextValue());
     feedback.put("taskid", context.getTaskAttemptID().toString());
 
     if (ex != null) {
       feedback.put("code", 1);
       feedback.put("status", "ERROR");
       feedback.put("message", ex.getMessage());
       feedback.put("exception", Throwables.getStackTraceAsString(ex));
     } else {
       feedback.put("status", "OK");
       feedback.put("code", 0);
       feedback.put("rows", singleCounter.rows);
       feedback.put("cells", singleCounter.cells);
       feedback.put("nullCells", singleCounter.nullCells);
       feedback.put("cellBytes", singleCounter.bytes);
 
       feedback.put("during", singleCounter.during);
       feedback.put("columns", metadata.columnNames);
       feedback.put("columnTypes", metadata.columnHiveTypes);
     }
 
     feedbackQueue.offer(mapper.writeValueAsString(feedback));
   }
 
   @Override
   public void cleanup(Mapper.Context context) throws IOException, InterruptedException {
     fs.close();
   }
 }
