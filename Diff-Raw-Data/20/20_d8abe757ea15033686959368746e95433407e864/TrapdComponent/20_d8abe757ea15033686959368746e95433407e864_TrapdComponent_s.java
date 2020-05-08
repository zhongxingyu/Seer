 package org.opennms.ng.services.trapd;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 
 import org.apache.camel.Endpoint;
 import org.apache.camel.impl.DefaultComponent;
 import org.opennms.netmgt.snmp.SnmpV3User;
 
 public class TrapdComponent extends DefaultComponent {
 
     private TrapdIpMgr trapdIpMgr;
 
     private String snmpTrapAddress;
 
     private Integer snmpTrapPort;
 
     private List<SnmpV3User> snmpV3Users;
 
     private TrapQueueProcessorFactory trapQueueProcessorFactory;
 
     private ExecutorService backlogQ;
 
     public TrapdComponent() {
     }
 
     public TrapdComponent(TrapdIpMgr trapdIpMgr, String snmpTrapAddress, Integer snmpTrapPort, List<SnmpV3User> snmpV3Users, TrapQueueProcessorFactory trapQueueProcessorFactory, ExecutorService backlogQ) {
         this.trapdIpMgr = trapdIpMgr;
         this.snmpTrapAddress = snmpTrapAddress;
         this.snmpTrapPort = snmpTrapPort;
         this.snmpV3Users = snmpV3Users;
         this.trapQueueProcessorFactory = trapQueueProcessorFactory;
         this.backlogQ = backlogQ;
     }
 
     @Override
     protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
 
         TrapdConfiguration config = new TrapdConfiguration();
         config.setTrapdIpMgr(trapdIpMgr);
         config.setSnmpTrapAddress(snmpTrapAddress);
         config.setSnmpTrapPort(snmpTrapPort);
         config.setSnmpV3Users(snmpV3Users);
         config.setTrapQueueProcessorFactory(trapQueueProcessorFactory);
         config.setBacklogQ(backlogQ);
 
         return new TrapdEndpoint(uri,remaining,this,config);
     }
 
     public void setTrapdIpMgr(TrapdIpMgr trapdIpMgr) {
         this.trapdIpMgr = trapdIpMgr;
     }
 
     public void setSnmpTrapAddress(String snmpTrapAddress) {
         this.snmpTrapAddress = snmpTrapAddress;
     }
 
     public void setSnmpTrapPort(Integer snmpTrapPort) {
         this.snmpTrapPort = snmpTrapPort;
     }
 
     public void setSnmpV3Users(List<SnmpV3User> snmpV3Users) {
         this.snmpV3Users = snmpV3Users;
     }
 
     public void setTrapQueueProcessorFactory(TrapQueueProcessorFactory trapQueueProcessorFactory) {
         this.trapQueueProcessorFactory = trapQueueProcessorFactory;
     }
 
     public void setBacklogQ(ExecutorService backlogQ) {
         this.backlogQ = backlogQ;
     }
 }
