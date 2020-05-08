 package com.mapr.demo.storm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.StormSubmitter;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.tuple.Fields;
 
 import com.google.common.io.Resources;
 import com.mapr.ProtoSpout;
 import com.mapr.TailSpout;
 
 public class TweetTopology {
 
    private static final String TOPOLOGY_NAME = "mapr-storm-demo";
    private static final String FILE_PATTERN = ".*";
     private static final String DEFAULT_TOP_N = "150";
     private static final String DEFAULT_BASE_DIR = "/tmp/mapr-storm-demo";
     private static String baseDir = "";
     public static final Logger log = LoggerFactory.getLogger(TweetTopology.class);
     private static final String PROPERTIES_FILE = "mapr-storm-demo.properties";
 
     public static Properties loadProperties() {
         Properties props = new Properties();
         loadProperties("base.properties", props);
         loadProperties(PROPERTIES_FILE, props);
         return props;
     }
 
     private static Properties loadProperties(String resource, Properties props) {
         try {
             InputStream is = Resources.getResource(resource).openStream();
             log.info("Loading properties from '" + resource + "'.");
             props.load(is);
       } catch (Exception e) {
               log.info("Not loading properties from '" + resource + "'.");
               log.info(e.getMessage());
       }
       return props;
     }
 
     public static void main(String[] args) throws AlreadyAliveException,
             InvalidTopologyException, InterruptedException, IOException {
 
         log.info("---------------------");
         log.info("------STARTING-------");
         log.info("---------------------");
         log.info("Building topology");
 
         TopologyBuilder topologyBuilder = new TopologyBuilder();
 
         Properties props = TweetTopology.loadProperties();
         boolean remote = Boolean.parseBoolean(props.getProperty("remote"));
         int numSpouts = Integer.parseInt(props.getProperty("spouts"));
         baseDir = props.getProperty("base.directory", DEFAULT_BASE_DIR);
         int top_n = Integer.parseInt(props.getProperty("top.n", DEFAULT_TOP_N));
         log.debug(props.toString());
 
 
         // init the MapR Tail Spout
         BlobTupleParser tp = new BlobTupleParser();
         File statusFile = new File(baseDir + "/status");
         File inDir = new File(baseDir);
         Pattern inPattern = Pattern.compile(FILE_PATTERN);
         TailSpout spout = new ProtoSpout(tp, statusFile, inDir, inPattern);
 
         log.debug("Status file: " + statusFile);
         log.debug("inDir      : " + inDir);
         log.debug("inPattern  : " + inPattern);
 
         // TODO this should be set to true, but somebody isn't acking tuples correctly and that causes hangs
         spout.setReliableMode(false);
 
         topologyBuilder.setSpout("mapr_tail_spout", spout, numSpouts);
         topologyBuilder.setBolt("tweet", new TweetBolt(), 1)
                 .shuffleGrouping("mapr_tail_spout");
 
         // tweets
         topologyBuilder.setBolt("tokenizer", new TokenizerBolt(), 1)
                 .shuffleGrouping("tweet", "tweets");
         topologyBuilder.setBolt("tweet_rolling_count", new RollingCountBolt(
                 Integer.parseInt(props.getProperty("window", "3600")), 5), 1)
                 .fieldsGrouping("tokenizer", new Fields("word"));
         topologyBuilder.setBolt("tweet_intermediate_rank", new IntermediateRankingsBolt(top_n), 1)
                 .globalGrouping("tweet_rolling_count");
         topologyBuilder.setBolt("tweet_flush", new FlushRankingsBolt("tweets"), 1)
                 .globalGrouping("tweet_intermediate_rank");
 
         // users
         topologyBuilder.setBolt("user_rolling_count", new RollingCountBolt(
                 Integer.parseInt(props.getProperty("window", "3600")), 5), 1)
                 .shuffleGrouping("tweet", "users");
         topologyBuilder.setBolt("user_intermediate_rank", new IntermediateRankingsBolt(top_n), 1)
                 .globalGrouping("user_rolling_count");
         topologyBuilder.setBolt("user_flush", new FlushRankingsBolt("users"), 1)
                 .globalGrouping("user_intermediate_rank");
 
         Config conf = new Config();
         conf.setDebug(true);
 
         log.info("topology built.");
 
         if (remote) {
             log.info("Sleeping 1 seconds before submitting topology");
             Thread.sleep(1000);
            StormSubmitter.submitTopology(TOPOLOGY_NAME,
                     conf, topologyBuilder.createTopology());
         } else {
             log.info("Submitting topology");
             LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME,
                     conf, topologyBuilder.createTopology());
            Thread.sleep(1000*60*5);
            cluster.shutdown();
         }
     }
 }
