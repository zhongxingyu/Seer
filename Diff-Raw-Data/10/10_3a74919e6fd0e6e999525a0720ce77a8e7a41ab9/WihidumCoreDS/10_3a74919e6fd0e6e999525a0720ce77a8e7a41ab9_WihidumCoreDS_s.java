 package org.wso2.carbon.cep.wihidum.core.internal;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.component.ComponentContext;
 import org.wso2.carbon.brokermanager.core.BrokerManagerService;
 import org.wso2.carbon.cep.wihidum.core.cluster.ClusterManager;
 
 /**
  * @scr.component name="wihidumcoreservice.component" immediate="true"
 * @scr.reference name="broker.service"
 * interface="org.wso2.carbon.broker.core.BrokerService" cardinality="1..1"
  * policy="dynamic" bind="setBrokerManagerService" unbind="unsetBrokerManagerService"
  **/
 public class WihidumCoreDS {
     private static final Log log = LogFactory.getLog(WihidumCoreDS.class);
 
 
     protected void activate(ComponentContext context){
         try{
         ClusterManager clusterManager = ClusterManager.getInstant();
         clusterManager.initiate();
         log.info("Successfully initiated cluster manager");
         }catch (Throwable e){
             log.error("Can not initiate cluster service ", e);
         }
     }
 
    protected void setBrokerManagerService(BrokerManagerService brokerManagerService) {
         WihidumCoreValueHolder.getInstance().setBrokerManagerService(brokerManagerService);
     }
 
    protected void unsetBrokerManagerService(BrokerManagerService brokerManagerService) {
         WihidumCoreValueHolder.getInstance().unsetBrokerManagerService();
     }
 }
