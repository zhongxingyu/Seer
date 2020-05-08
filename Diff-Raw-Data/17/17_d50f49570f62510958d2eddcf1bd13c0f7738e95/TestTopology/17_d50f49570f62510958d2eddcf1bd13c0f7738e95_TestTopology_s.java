 package se.sics.kompics.storm.se.sics.kompics.storm.topologies;
 
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.utils.Utils;
 import org.junit.Test;
 import se.sics.kompics.storm.bolt.KompicsBolt;
 import se.sics.kompics.storm.se.sics.kompics.storm.modules.PrintSetup;
 import se.sics.kompics.storm.se.sics.kompics.storm.modules.ReemmitSetup;
 import se.sics.kompics.storm.se.sics.kompics.storm.modules.TestCounterSpout;
 
 public class TestTopology {
 
     @Test
     public void testLocally() {
         TopologyBuilder builder = new TopologyBuilder();
         builder.setSpout("counter", new TestCounterSpout(10, "num"), 1);
        builder.setBolt("reemmit", new KompicsBolt(new ReemmitSetup()), 1).globalGrouping("counter");
        builder.setBolt("printer", new KompicsBolt(new PrintSetup()), 1).globalGrouping("reemmit");
         Config conf = new Config();
         conf.setDebug(true);
         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("test", conf, builder.createTopology());
         Utils.sleep(5000);
         cluster.killTopology("test");
         cluster.shutdown();
     }
 
 }
