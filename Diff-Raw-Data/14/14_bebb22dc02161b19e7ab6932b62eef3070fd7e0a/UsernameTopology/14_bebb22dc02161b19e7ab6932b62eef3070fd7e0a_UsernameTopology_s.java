 package com.mapr.demo.storm;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.StormSubmitter;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.topology.TopologyBuilder;
 import com.google.common.io.Resources;
 import com.mapr.TailSpout;
 import com.mapr.storm.streamparser.CountBlobStreamParserFactory;
 import com.mapr.storm.streamparser.StreamParserFactory;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 public class UsernameTopology {
 
     private static final String FILETYPE = "users";
     private static final String DEFAULT_TOP_N = "50";
     private static final String DEFAULT_BASE_DIR = "/tmp/mapr-spout-test";
     public static final Logger Log = Logger.getLogger(UsernameTopology.class);
     private static final String PROPERTIES_FILE = "conf/test.properties";
 
     public static Properties loadProperties() throws IOException {
         Properties props = new Properties();
         InputStream base = Resources.getResource("base.properties").openStream();
         props.load(base);
         base.close();
 
        FileInputStream in = new FileInputStream(PROPERTIES_FILE);
        props.load(in);
        in.close();
 
         return props;
     }
 
     public static void main(String[] args) throws AlreadyAliveException,
             InvalidTopologyException, InterruptedException, IOException {
 
         Log.info("---------------------");
         Log.info("------STARTING-------");
         Log.info("---------------------");
         Log.info("Building topology");
 
         TopologyBuilder topologyBuilder = new TopologyBuilder();
 
         Properties props = UsernameTopology.loadProperties();
         boolean remote = Boolean.parseBoolean(props.getProperty("remote"));
         int numSpouts = Integer.parseInt(props.getProperty("spouts"));
         String baseDir = props.getProperty("base.directory", DEFAULT_BASE_DIR);
         int top_n = Integer.parseInt(props.getProperty("top.n", DEFAULT_TOP_N));
 
         // init the MapR Tail Spout
         StreamParserFactory spf = new CountBlobStreamParserFactory();
         File statusFile = new File(baseDir + "/status_" + FILETYPE);
         File inDir = new File(baseDir);
         Pattern inPattern = Pattern.compile(FILETYPE);
         TailSpout spout = new TailSpout(spf, statusFile, inDir, inPattern);
         spout.setReliableMode(true);
 
         topologyBuilder.setSpout("mapr_tail_spout", spout, numSpouts);
         topologyBuilder.setBolt("rolling_count", new RollingCountBolt(Integer.parseInt(props.getProperty("window", "7200")), 5), 1)
                 .shuffleGrouping("mapr_tail_spout");
         topologyBuilder.setBolt("intermediate_rank", new IntermediateRankingsBolt(top_n), 1)
                 .globalGrouping("rolling_count");
         topologyBuilder.setBolt("flush", new FlushRankingsBolt(FILETYPE), 1)
                 .globalGrouping("intermediate_rank");
 
         Config conf = new Config();
         conf.setDebug(true);
 
         Log.info("topology built.");
 
 /*
         // TODO: properties file
         conf.setNumWorkers(300);
         conf.setMaxSpoutPending(5000);
         conf.setMaxTaskParallelism(500);
 */
         if (remote) {
             Log.info("Sleeping 1 seconds before submitting topology");
             Thread.sleep(1000);
             StormSubmitter.submitTopology("mapr-spout-test Username Topology",
                     conf, topologyBuilder.createTopology());
         } else {
             LocalCluster cluster = new LocalCluster();
             cluster.submitTopology("mapr-spout-test Local Username Topology",
                     conf, topologyBuilder.createTopology());
 
 /*
             // TODO: rest of this is for DEV only
             Thread.sleep(600000);
 
             log.info("DONE");
             cluster.shutdown();
             System.exit(0);
 */
         }
 
     }
 }
