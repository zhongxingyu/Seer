 package com.rackspace.cableway;
 
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Tuple;
 import org.apache.log4j.Logger;
 
 public class PrinterBolt extends BaseBasicBolt {
    private static final Logger LOG = Logger.getLogger(SyslogTopology.class);
 
     @Override
     public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        LOG.debug(tuple.toString());
     }
 }
