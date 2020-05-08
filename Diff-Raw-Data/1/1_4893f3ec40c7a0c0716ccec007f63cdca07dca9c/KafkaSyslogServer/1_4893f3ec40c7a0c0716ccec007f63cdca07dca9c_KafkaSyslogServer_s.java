 package com.rackspace.lull;
 
 import kafka.javaapi.producer.Producer;
 import kafka.producer.ProducerConfig;
import kafka.message.Message;
 import org.apache.log4j.Logger;
 import org.productivity.java.syslog4j.server.SyslogServer;
 import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
 import org.productivity.java.syslog4j.server.SyslogServerIF;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Properties;
 
 
 public class KafkaSyslogServer {
     private static final Logger LOG = Logger.getLogger(KafkaSyslogServer.class);
     private static final String KAFKA_PROPERTIES_RESOURCE_NAME = "/kafka.producer.properties";
 
 
     public static void main(String [] args) throws IOException {
         final SyslogServerIF udpSyslogServer = SyslogServer.getThreadedInstance("udp");
         final SyslogServerIF tcpSyslogServer = SyslogServer.getThreadedInstance("tcp");
 
         SyslogServerConfigIF udpSyslogServerConfig = udpSyslogServer.getConfig();
         SyslogServerConfigIF tcpSyslogServerConfig = tcpSyslogServer.getConfig();
 
         final Properties kafkaProperties = getDefaultKafkaProperties();
         final ProducerConfig producerConfig = new ProducerConfig(kafkaProperties);
         final Producer<String, String> producer = new Producer<String, String>(producerConfig);
 
 
         // Add producer and syslog server shutdown hooks
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 if (producer != null) {
                     LOG.info("Closing producer...");
                     producer.close();
                 }
                 LOG.info("Shutting down syslog server.");
                 SyslogServer.shutdown();
             }
         });
 
         udpSyslogServerConfig.addEventHandler(new KafkaEventHandler(producer));
         tcpSyslogServerConfig.addEventHandler(new KafkaEventHandler(producer));
 
         try {
             udpSyslogServer.getThread().join();
             tcpSyslogServer.getThread().join();
         } catch (InterruptedException e) {
             LOG.error("Main thread interrupted.");
         }
     }
 
     protected static Properties getDefaultKafkaProperties() throws IOException {
         final Properties props = new Properties();
         final URL propUrl = KafkaSyslogServer.class.getResource(KAFKA_PROPERTIES_RESOURCE_NAME);
         if (propUrl == null) {
             throw new IllegalArgumentException("Could not find the properties file: " + KAFKA_PROPERTIES_RESOURCE_NAME);
         }
 
         final InputStream in = propUrl.openStream();
         try {
             props.load(in);
         } finally {
             in.close();
         }
 
         return props;
     }
 
 
 }
