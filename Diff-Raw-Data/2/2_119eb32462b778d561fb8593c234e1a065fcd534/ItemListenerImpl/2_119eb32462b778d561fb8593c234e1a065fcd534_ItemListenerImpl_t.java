 package hudson.plugins.hadoop;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher.LocalLauncher;
 import hudson.model.Hudson;
 import hudson.model.listeners.ItemListener;
 import hudson.remoting.Callable;
 import hudson.util.StreamTaskListener;
 import org.apache.commons.io.FileUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hdfs.server.namenode.NameNode;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.JobTracker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 @Extension
 public class ItemListenerImpl extends ItemListener {
     @Override
     public void onLoaded() {
         try {
             PluginImpl p = PluginImpl.get();
             String hdfsUrl = p.getHdfsUrl();
             if(hdfsUrl!=null) {
                 // start Hadoop namenode and tracker node
                 StreamTaskListener listener = new StreamTaskListener(System.out);
                p.channel = PluginImpl.createHadoopVM(Hudson.getInstance().getRootDir(), listener);
                 p.channel.call(new NameNodeStartTask(hdfsUrl));
                 p.channel.call(new JobTrackerStartTask(hdfsUrl,p.getJobTrackerAddress()));
             } else {
                 LOGGER.info("Skipping Hadoop initialization because we don't know the root URL.");
             }
         } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Failed to start Hadoop on master",e);
         }
     }
 
     /**
      * Starts a {@link NameNode}.
      */
     private static class NameNodeStartTask implements Callable<Void,IOException> {
         private final String hdfsUrl;
 
         private NameNodeStartTask(String hdfsUrl) {
             this.hdfsUrl = hdfsUrl;
         }
 
         public Void call() throws IOException {
             FileUtils.deleteDirectory(new File("/tmp/hadoop"));
             final Configuration conf = new Configuration();
 
             // location of the name node
             conf.set("fs.default.name",hdfsUrl);
             conf.set("dfs.http.address", "0.0.0.0:50070");
             // namespace node stores information here
             conf.set("dfs.name.dir","/tmp/hadoop/namedir");
             // dfs node stores information here
             conf.set("dfs.data.dir","/tmp/hadoop/datadir");
 
             conf.setInt("dfs.replication",1);
 
             System.out.println("Formatting HDFS");
             NameNode.format(conf);
 
             System.out.println("Starting namenode");
             NameNode.createNameNode(new String[0], conf);
             return null;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * Starts a {@link JobTracker}.
      */
     private static class JobTrackerStartTask implements Callable<Void,Exception>, Runnable {
         private final String hdfsUrl;
         private final String jobTrackerAddress;
 
         private JobTrackerStartTask(String hdfsUrl, String jobTrackerAddress) {
             this.hdfsUrl = hdfsUrl;
             this.jobTrackerAddress = jobTrackerAddress;
         }
 
         private transient JobTracker tracker;
 
         public void run() {
             try {
                 tracker.offerService();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public Void call() throws Exception {
 //        Configuration conf = new Configuration();
             JobConf jc = new JobConf();
             jc.set("fs.default.name",hdfsUrl);
             jc.set("mapred.job.tracker",jobTrackerAddress);
             jc.set("mapred.job.tracker.http.address","0.0.0.0:50030");
             jc.set("mapred.local.dir","/tmp/hadoop/mapred");
             tracker = JobTracker.startTracker(jc);
 
             new Thread(this).start();
 
             return null;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     public static PluginImpl get() {
         return Hudson.getInstance().getPlugin(PluginImpl.class);
     }
 
     private static final Logger LOGGER = Logger.getLogger(ItemListenerImpl.class.getName());
 }
