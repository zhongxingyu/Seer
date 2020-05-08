 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.StormSubmitter;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 import spout.RandomSentenceSpout;
 import bolt.SplitSentence;
 import bolt.WordCount;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This topology demonstrates Storm's stream groupings and multilang capabilities.
  */
 public class WordCountTopology {
 
   public static void main(String[] args) throws Exception {
 
     TopologyBuilder builder = new TopologyBuilder();
 
     builder.setSpout("spout", new RandomSentenceSpout(), 5);
 
     builder.setBolt("split", new SplitSentence(), 8).shuffleGrouping("spout");
     builder.setBolt("count", new WordCount(), 12).fieldsGrouping("split", new Fields("word"));
 
     Config conf = new Config();
     conf.setDebug(true);
 
 
     if (args != null && args.length > 0) {
       conf.setNumWorkers(3);
      System.setProperty("storm.jar", Class.forName("backtype.storm.StormSubmitter").getProtectionDomain().getCodeSource().getLocation().getPath());
       StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
     }
     else {
       conf.setMaxTaskParallelism(3);
 
       LocalCluster cluster = new LocalCluster();
       cluster.submitTopology("word-count", conf, builder.createTopology());
 
       Thread.sleep(10000);
 
       cluster.shutdown();
     }
   }
 }
