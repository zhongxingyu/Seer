 package com.trendmicro.tme.portal;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.googlecode.jmxtrans.JmxTransformer;
 import com.googlecode.jmxtrans.model.JmxProcess;
 import com.googlecode.jmxtrans.model.Query;
 import com.googlecode.jmxtrans.model.Server;
 import com.googlecode.jmxtrans.util.ValidationException;
 import com.trendmicro.codi.ZKSessionManager;
 import com.trendmicro.tme.mfr.BrokerFarm;
 import com.trendmicro.mist.proto.ZooKeeperInfo.Broker;
 
 public class ExchangeMetricCollector {
     private static final String CONFIG_PATH = System.getProperty("com.trendmicro.tme.portal.collector.conf", "/opt/trend/tme/conf/portal-collector/portal-collector.properties");
     private static final Logger logger = LoggerFactory.getLogger(ExchangeMetricCollector.class);
     
     private BrokerFarm brokerFarm;
     private ExchangeMetricWriter writer;
     
     private Query createQuery() {
         Query query = new Query();
         query.setObj("com.sun.messaging.jms.server:type=Destination,subtype=Monitor,desttype=*,name=*");
         query.addAttr("NumMsgs");
         query.addAttr("NumMsgsIn");
         query.addAttr("NumMsgsOut");
         query.addAttr("NumMsgsPendingAcks");
         query.addAttr("NumConsumers");
         query.addAttr("NumProducers");
         query.addAttr("MsgBytesIn");
         query.addAttr("MsgBytesOut");
         query.addAttr("TotalMsgBytes");
         query.addOutputWriter(writer);
         return query;
     }
     
     public ExchangeMetricCollector(BrokerFarm brokerFarm, ExchangeMetricWriter writer) {
         this.brokerFarm = brokerFarm;
         this.writer = writer;
     }
     
     public void run() {
         logger.info("Exchange Metric Collector started");
         while(true) {
             JmxProcess jmxProcess = new JmxProcess();
             for(Broker broker : brokerFarm.getAllBrokers().values()) {
                 if(broker.getStatus() == Broker.Status.ONLINE) {
                     Server server = new Server(broker.getHost(), "5566");
                     server.setUsername("monitorRole");
                     server.setPassword("secret");
                     try {
                         server.addQuery(createQuery());
                     }
                     catch(ValidationException e) {
                         e.printStackTrace();
                     }
                     jmxProcess.addServer(server);
                 }
             }
             JmxTransformer transformer = new JmxTransformer();
             try {
                 transformer.executeStandalone(jmxProcess);
                 Thread.sleep(10000);
             }
             catch(Exception e) {
                 logger.error("Error to collect metrics: ", e);
             }
         }
     }
     
     public static void main(String[] args) {
         try {
             Properties prop = new Properties();
             prop.load(new FileInputStream(CONFIG_PATH));
             // Let the system properties override the ones in the config file
             prop.putAll(System.getProperties());
             
             String zkQuorum = prop.getProperty("com.trendmicro.tme.portal.collector.zookeeper.quorum");
             int zkTimeout = Integer.valueOf(prop.getProperty("com.trendmicro.tme.portal.collector.zookeeper.timeout"));
             
             ZKSessionManager.initialize(zkQuorum, zkTimeout);
             if(!ZKSessionManager.instance().waitConnected(zkTimeout)) {
                 throw new Exception("Cannot connect to ZooKeeper Quorom " + zkQuorum);
             }
             
             ExchangeMetricWriter writer = new ExchangeMetricWriter();
             writer.addSetting("templateFile", prop.getProperty("com.trendmicro.tme.portal.collector.template"));
             writer.addSetting("outputPath", prop.getProperty("com.trendmicro.tme.portal.collector.outputdir"));
            ExchangeMetricCollector collector = new ExchangeMetricCollector(new BrokerFarm(), writer);
             collector.run();
         }
         catch(Exception e) {
             e.printStackTrace();
             logger.error("Portal Collector startup error: ", e);
             System.exit(-1);
         }
     }
 }
