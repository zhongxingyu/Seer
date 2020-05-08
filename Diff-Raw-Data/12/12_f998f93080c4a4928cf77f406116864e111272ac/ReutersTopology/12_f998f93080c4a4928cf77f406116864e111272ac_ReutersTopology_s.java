 package com.zuehlke.reuters.storm;
 
 import java.io.File;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.generated.StormTopology;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.utils.Utils;
 
 import com.zuehlke.reuters.storm.bolt.ClassifierBolt;
 import com.zuehlke.reuters.storm.bolt.ExtractionBolt;
 import com.zuehlke.reuters.storm.bolt.PrintBolt;
 import com.zuehlke.reuters.storm.spout.DocumentSpout;
 import com.zuehlke.reuters.storm.spout.RawDocumentSpout;
 
 
 public class ReutersTopology {
 	public static StormTopology buildTopology(File inputDir){
 		TopologyBuilder builder = new TopologyBuilder();
 		builder.setSpout("document", new DocumentSpout(inputDir.listFiles()), 1);
 		builder.setBolt("bodyExtraction", new ExtractionBolt(), 2).shuffleGrouping("document");
 		builder.setBolt("classifier", new ClassifierBolt(), 1).shuffleGrouping("bodyExtraction");
 		
 		return builder.createTopology();
 	}
 	
 	public static StormTopology buildRawDataTopology(File inputDir){
 		TopologyBuilder builder = new TopologyBuilder();
 		builder.setSpout("document", new RawDocumentSpout(inputDir), 1);
 		builder.setBolt("classifier", new ClassifierBolt(), 1).shuffleGrouping("document");
 		builder.setBolt("print", new PrintBolt(), 1).shuffleGrouping("classifier");
 		return builder.createTopology();
 	}
 	
 	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException{
 		Config config = new Config();
 		config.setDebug(false);
 		config.setNumWorkers(2);
 		config.setMaxSpoutPending(1);
 		LocalCluster cluster = new LocalCluster();
<<<<<<< HEAD
 		cluster.submitTopology("reutersTopology", config, buildRawDataTopology(new File("/home/cloudera/workspace/reutersMahout/Data")));
=======
		cluster.submitTopology("reutersTopology", config, buildTopology(new File("/tmp/reuters21578-out")));
>>>>>>> 3426d2ed542c883440138652dff51b72005b3bbc
 		Utils.sleep(10000);
 		cluster.killTopology("reutersTopology");
         cluster.shutdown();
 	}
 }
