 
 package org.wso2.carbon.cep.wihidum.core.cluster;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.core.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
 import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
 import org.wso2.carbon.cep.wihidum.core.internal.WihidumCoreValueHolder;
 import org.wso2.carbon.context.CarbonContext;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 public class ClusterManager {
 
     private static ClusterManager clusterManager;
     private HazelcastInstance hazelcastInstance;
     private static final Log log = LogFactory.getLog(ClusterManager.class);
 
     private ClusterManager() {
         hazelcastInstance = Hazelcast.newHazelcastInstance(new Config().setInstanceName(UUID.randomUUID().toString()));
     }
 
 
     public static ClusterManager getInstant() {
         if (clusterManager == null) {
             clusterManager = new ClusterManager();
 
         }
         return clusterManager;
 
     }
 
     public void initiate() {
         Cluster cluster = hazelcastInstance.getCluster();
         Member localMember = cluster.getLocalMember();
         Set<Member> memberList = cluster.getMembers();
         cluster.addMembershipListener(new MembershipListener() {
             public void memberAdded(MembershipEvent membersipEvent) {
                 configureBrokers(membersipEvent.getMember());
             }
 
             public void memberRemoved(MembershipEvent membersipEvent) {
                 //TODO
             }
         });
         for (Member member : memberList) {
             configureBrokers(member);
         }
     }
 
     private synchronized void configureBrokers(Member member) {
        String brokerName = member.getInetSocketAddress().getAddress().toString().substring(1);
         BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
        brokerConfiguration.setName(brokerName);
         brokerConfiguration.setType(Constants.AGENT_BROKER_TYPE);
         int tenantId = CarbonContext.getCurrentContext().getTenantId();
         Map<String,String> properties = new HashMap<String, String>();
         PropertyGenerator generator;
 
        generator = new PropertyGenerator(brokerName);
         properties.put("receiverURL",generator.getReceiverURL());
         properties.put("authenticatorURL",generator.getAuthenticatorURL());
         properties.put("username",generator.getUsername());
         properties.put("password",generator.getPassword());
 
         // add broker properties
         for (Map.Entry<String, String> entry : properties.entrySet()) {
             brokerConfiguration.addProperty(entry.getKey(), entry.getValue());
         }
         // add broker configuration
         try {
            WihidumCoreValueHolder.getInstance().getBrokerManagerService().addBrokerConfiguration(brokerConfiguration, -1234);
             //testBrokerConfiguration(brokerName);
         } catch (BMConfigurationException e) {
             log.error("Cannot add broker for CEP node on " + member.getInetSocketAddress());
         }
 
 
     }
 
 
 }
