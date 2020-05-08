 package com.tencent.qqlive.streaming.topology;
 
 import java.util.Properties;
 
 import backtype.storm.Config;
 import backtype.storm.StormSubmitter;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.tuple.Fields;
 
 import com.tencent.qqlive.streaming.bolt.MultiDevVodCountBolt;
 import com.tencent.qqlive.streaming.spout.HinaSourceSpout;
 
 public class MultiDevVodTopology {
 
 	public static void main(String[] args) throws Exception {
 		if (args.length < 1) {
 			System.out.println("Usage: conffile");
 			System.exit(-1);
 		}
 		
 		Properties prop = new Properties();
 		prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(args[0]));
 		
 		Config conf = new Config();
 		for (String key : prop.stringPropertyNames()) {
 			conf.put(key, prop.getProperty(key));
 		}
 		
 //		conf.setNumWorkers(Integer.valueOf((String)conf.get("topology.works")));
 		conf.setNumWorkers(2);
 		conf.setNumAckers(0);
 //		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 100000);
 		
 		TopologyBuilder builder = new TopologyBuilder();
 			
 		builder.setSpout("HinaSourceSpout", new HinaSourceSpout(), 4);
		builder.setBolt("MultiDevVodCountBolt", new MultiDevVodCountBolt(), 2)
 			.fieldsGrouping("HinaSourceSpout", HinaSourceSpout.STREAM_ID_MultDevVod, new Fields("album", "playid", "devtype"));
 		
 		StormSubmitter.submitTopology("MultiDevVod", conf, builder.createTopology());
 	}
 }
