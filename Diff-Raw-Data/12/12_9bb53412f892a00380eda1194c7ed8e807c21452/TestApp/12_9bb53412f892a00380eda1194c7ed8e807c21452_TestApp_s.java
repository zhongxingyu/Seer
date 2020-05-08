 package org.openengsb.loom.java;
 
 import org.openengsb.connector.usernamepassword.Password;
 import org.openengsb.domain.example.ExampleDomain;
 import org.openengsb.loom.java.jms.JmsProtocolHandler;
 
 public class TestApp {
     private static final String baseURL = "failover:(tcp://localhost:6549)?timeout=6000";
 
     public static void main(String[] args) throws Exception {
        JmsProtocolHandler jmsConfig = new JmsProtocolHandler(baseURL);
         ProxyConnectorFactory domainFactory = new ProxyConnectorFactory(jmsConfig, "admin", new Password("password"));
         ExampleDomain handler = new ExampleConnector();
         String uuid = domainFactory.createConnector("example");
         domainFactory.registerConnector(uuid, handler);
         System.out.println(uuid);
         System.in.read();
         domainFactory.deleteConnector(uuid);
         jmsConfig.destroy();
     }
 }
