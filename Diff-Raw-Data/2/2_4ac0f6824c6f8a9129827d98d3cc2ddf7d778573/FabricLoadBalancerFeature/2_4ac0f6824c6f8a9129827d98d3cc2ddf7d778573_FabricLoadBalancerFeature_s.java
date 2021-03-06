 /*
  * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
  * http://fusesource.com
  *
  * The software in this package is published under the terms of the
  * CDDL license a copy of which has been included with this distribution
  * in the license.txt file.
  */
 package org.fusesource.fabric.cxf;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.cxf.Bus;
 import org.apache.cxf.endpoint.ServerLifeCycleManager;
 import org.apache.cxf.feature.AbstractFeature;
 import org.apache.cxf.endpoint.Client;
 import org.apache.zookeeper.ZooDefs;
 import org.apache.zookeeper.data.ACL;
 import org.fusesource.fabric.groups.Group;
 import org.fusesource.fabric.groups.ZooKeeperGroupFactory;
import org.fusesource.fabric.zookeeper.ZKClientFactoryBean;
 import org.linkedin.zookeeper.client.IZKClient;
 
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 public class FabricLoadBalancerFeature extends AbstractFeature implements InitializingBean, DisposableBean {
     private static final transient Log LOG = LogFactory.getLog(FabricLoadBalancerFeature.class);
     @Autowired
     private IZKClient zkClient;
     private String zkRoot = "/fabric/cxf/endpoints/";
     private String fabricPath;
     private Group group;
     private LoadBalanceStrategy loadBalanceStrategy;
     private List<ACL> accessControlList = ZooDefs.Ids.OPEN_ACL_UNSAFE;
 
     public void initialize(Client client, Bus bus) {
         LoadBalanceTargetSelector selector =
             new LoadBalanceTargetSelector();
         selector.setEndpoint(client.getEndpoint());
         selector.setLoadBalanceStrategy(getLoadBalanceStrategy());
         client.setConduitSelector(selector);
     }
 
     public void initialize(Bus bus) {
         FabricServerListener lister = new FabricServerListener(group);
         // register the listener itself
         ServerLifeCycleManager mgr = bus.getExtension(ServerLifeCycleManager.class);
         if (mgr != null) {
             mgr.registerListener(lister);
         } else {
             LOG.warn("Cannot find the ServerLifeCycleManager ");
         }
     }
 
     protected void checkZkConnected() throws Exception {
         if (!zkClient.isConnected()) {
             throw new Exception("Could not connect to ZooKeeper " + zkClient);
         }
     }
 
     public void afterPropertiesSet() throws Exception {
         if (zkClient == null) {
             zkClient = new ZKClientFactoryBean().getObject();
         }
         checkZkConnected();
         group = ZooKeeperGroupFactory.create(getZkClient(), zkRoot + fabricPath, accessControlList);
         if (loadBalanceStrategy == null) {
             loadBalanceStrategy = new RandomLoadBalanceStrategy();
         }
         loadBalanceStrategy.setGroup(group);
     }
 
     public void destroy() throws Exception {
         if (zkClient != null) {
             zkClient.close();
         }
     }
 
     public String getFabricPath() {
         return fabricPath;
     }
 
     public void setFabricPath(String fabricPath) {
         this.fabricPath = fabricPath;
     }
 
     public List<ACL> getAccessControlList() {
         return accessControlList;
     }
 
     public void setAccessControlList(List<ACL> accessControlList) {
         this.accessControlList = accessControlList;
     }
 
     public IZKClient getZkClient() {
         return zkClient;
     }
 
     public void setZkClient(IZKClient zkClient) {
         this.zkClient = zkClient;
     }
 
     public LoadBalanceStrategy getLoadBalanceStrategy() {
         return loadBalanceStrategy;
     }
 
     public void setLoadBalanceStrategy(LoadBalanceStrategy strategy) {
         this.loadBalanceStrategy = strategy;
     }
 
 }
