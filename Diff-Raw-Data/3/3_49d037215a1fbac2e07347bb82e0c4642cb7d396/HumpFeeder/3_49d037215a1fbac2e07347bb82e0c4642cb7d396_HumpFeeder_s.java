 package us.yuxin.hump;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 
 import com.google.common.collect.Lists;
 import org.apache.hadoop.conf.Configuration;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 public class HumpFeeder implements Runnable {
   private int parallel;
 
  private ObjectMapper mapper;
   private BlockingQueue<String> taskQueue;
   private BlockingQueue<String> feedbackQueue;
   private Configuration conf;
   List<String> tasks;
 
   public void setup(Configuration conf, File[] sources,
     BlockingQueue<String> taskQueue,
     BlockingQueue<String> feedbackQueue, int parallel) {
     this.conf = conf;
 
     this.taskQueue = taskQueue;
     this.feedbackQueue = feedbackQueue;
     this.parallel = parallel;
 
     setupSources(sources);
   }
 
   private void setupSources(File[] sources) {
     tasks = new LinkedList<String>();
 
     for (File source : sources) {
       try {
         JsonNode root = mapper.readValue(source, JsonNode.class);
         for (JsonNode node: root) {
           tasks.add(mapper.writeValueAsString(node));
         }
       } catch (IOException e) {
         e.printStackTrace();
         // TODO Exception handler.
       }
     }
 
     if (conf.getBoolean(Hump.CONF_HUMP_TASK_SHUFFLE, true)) {
       Collections.shuffle(tasks);
     }
   }
 
   public void setup(Configuration conf, File jsonSource,
     BlockingQueue<String> taskQueue, BlockingQueue<String> feedbackQueue, int parallel) throws IOException {
     File[] sources = new File[1];
     sources[0] = jsonSource;
 
     setup(conf, sources, taskQueue, feedbackQueue, parallel);
   }
 
   public void run() {
     for (String task: tasks) {
       taskQueue.offer(task);
     }
     for (int i = 0; i < parallel; ++i) {
       taskQueue.offer("STOP");
     }
   }
 }
