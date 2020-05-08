 package cn.uc.udac.zjj.main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.StormSubmitter;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.tuple.Fields;
 import backtype.storm.utils.Utils;
 import cn.uc.udac.zjj.bolts.*;
 import cn.uc.udac.zjj.spouts.*;
 
 
 public class StormMain {
 
 	static public void main(String[] args) {
 		TopologyBuilder builder = new TopologyBuilder();
 
 		builder.setSpout("s_log", new SpoutLog(), 16);
 		builder.setBolt("b_sn_date_site_pv", new BoltSnDateSitePv(), 1).shuffleGrouping("s_log");
 		builder.setBolt("b_site_date_pv", new BoltSiteDatePv(), 1).shuffleGrouping("s_log");
 		builder.setBolt("b_url_date_pv", new BoltUrlDatePv(), 16).shuffleGrouping("s_log");
		builder.setBolt("b_crawler", new BoltCrawler(), 16).shuffleGrouping("b_date_url_pv");
 		builder.setBolt("b_word_segment", new BoltWordSegment(), 16).shuffleGrouping("b_crawler");
 		
 		Config conf = new Config();
 		conf.setNumWorkers(5);
 		Map myconf = Utils.findAndReadConfigFile("zjj.yaml");
 		conf.putAll(myconf);
 
 		try {
 			StormSubmitter.submitTopology("zjj", conf, builder.createTopology());
 		} catch (AlreadyAliveException e) {
 			e.printStackTrace();
 		} catch (InvalidTopologyException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 }
