 package edu.nyu.storm.starter;
 
 // to use this example, uncomment the twitter4j dependency information in the project.clj,
 // uncomment storm.starter.spout.TwitterSampleSpout, and uncomment this class
 
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.utils.Utils;
 import edu.nyu.storm.bolts.ExtractNoiseWords;
 import edu.nyu.storm.bolts.PrinterBolt;
 import edu.nyu.storm.bolts.TokenExtractor;
 import edu.nyu.storm.spouts.TwitterSpout;
import backtype.storm.tuple.Fields;
import edu.nyu.storm.bolts.WordCounter;
 
 public class PrintStream {        
     public static void main(String[] args) {
         
     	TopologyBuilder builder = new TopologyBuilder();
         
         builder.setSpout("spout", new TwitterSpout());
         builder.setBolt("tokenExtractor", new TokenExtractor())
         	.shuffleGrouping("spout");
         builder.setBolt("noiseFilter", new ExtractNoiseWords())
     	.shuffleGrouping("tokenExtractor");
        builder.setBolt("wordCounter", new WordCounter(),1)
		.fieldsGrouping("noiseFilter", new Fields("filteredToken")); 
//        builder.setBolt("print", new PrinterBolt())
 //   	.shuffleGrouping("noiseFilter");
 	
         
         Config conf = new Config();
         
         LocalCluster cluster = new LocalCluster();
         
         cluster.submitTopology("tredingTopics", conf, builder.createTopology());
         
         Utils.sleep(10000);
         cluster.shutdown();
     }
 }
