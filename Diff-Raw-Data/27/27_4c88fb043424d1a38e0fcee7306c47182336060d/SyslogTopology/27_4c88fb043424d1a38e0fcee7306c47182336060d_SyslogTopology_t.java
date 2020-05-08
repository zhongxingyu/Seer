 package com.rackspace.cableway;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.topology.TopologyBuilder;
 import org.apache.log4j.Logger;
import storm.kafka.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SyslogTopology {
     private static final Logger LOG = Logger.getLogger(SyslogTopology.class);
 
 
     public static void main(String [] args) {
         List<HostPort> hosts = new ArrayList<HostPort>(2);
         hosts.add(new HostPort("kafka-01.threatboundary.com", 6667));
         hosts.add(new HostPort("kafka-02.threatboundary.com", 6667));
 
         SpoutConfig spoutConfig = new SpoutConfig(
                //new KafkaConfig.StaticHosts(hosts, 2),  // list of Kafka brokers
                new KafkaConfig.ZkHosts("zk-01.threatboundary.com", "/brokers"),
                 "syslog", // topic to read from
                 "/kafkastorm", // the root path in Zookeeper for the spout to store the consumer offsets
                 "discovery"); // an id for this consumer for storing the consumer offsets in Zookeeper
        spoutConfig.forceFromStart = true;
        spoutConfig.scheme = new StringScheme();
         KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
 
 
         TopologyBuilder builder = new TopologyBuilder();
         builder.setSpout("kafka", kafkaSpout, 2);
        builder.setBolt("printer", new PrinterBolt(), 2).shuffleGrouping("kafka");
         //builder.setBolt("es", new ElasticSearchBolt(), 2).shuffleGrouping("kafka");
         //builder.setBolt("correlation", new CorrelationBolt(), 5).fieldsGrouping();
        LOG.info("Submitting topology.");
 
         Config conf = new Config();
         conf.setDebug(true);
 
         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("syslog", conf, builder.createTopology());
     }
 }
