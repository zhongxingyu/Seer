 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.platforms.ec2;
 
 import com.elasticgrid.cluster.spi.CloudPlatformManager;
 import com.elasticgrid.model.Cluster;
 import com.elasticgrid.model.ClusterAlreadyRunningException;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.Discovery;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.NodeProfileInfo;
 import com.elasticgrid.model.ec2.EC2Cluster;
 import com.elasticgrid.model.ec2.EC2Node;
 import com.elasticgrid.model.ec2.EC2NodeType;
 import com.elasticgrid.model.ec2.impl.EC2ClusterImpl;
 import com.elasticgrid.platforms.ec2.discovery.EC2ClusterLocator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Service;
 import org.apache.commons.lang.StringUtils;
 import static java.lang.String.format;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @Service("ec2CloudPlatformManager")
 public class EC2CloudPlatformManager implements CloudPlatformManager<EC2Cluster> {
     private EC2Instantiator nodeInstantiator;
 
     @Autowired(required = true)
     private EC2ClusterLocator clusterLocator;
 
     private String dropBucket;
     private String overridesBucket;
     private String keyName;
     private String awsAccessID, awsSecretKey;
     private boolean awsSecured = true;
     private String ami32, ami64;
     private ExecutorService executor = Executors.newFixedThreadPool(5);
     private static final Logger logger = Logger.getLogger(EC2CloudPlatformManager.class.getName());
 
     public String getName() {
         return "Amazon EC2";
     }
 
     public void startCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         // ensure the cluster is not already running
         Cluster cluster = cluster(clusterName);
         if (cluster != null && cluster.isRunning()) {
             throw new ClusterAlreadyRunningException(cluster);
         }
 
         List<Future<List<String>>> futures = new LinkedList<Future<List<String>>>();
         for (NodeProfileInfo nodeProfileInfo : clusterTopology) {
             for (int i = 0; i < nodeProfileInfo.getNumber(); i++) {
                 String ami;
                 switch ((EC2NodeType) nodeProfileInfo.getNodeType()) {
                     case SMALL:
                         ami = ami32;
                         break;
                     case MEDIUM_HIGH_CPU:
                         ami = ami32;
                         break;
                     case LARGE:
                         ami = ami64;
                         break;
                     case EXTRA_LARGE:
                         ami = ami64;
                         break;
                     case EXTRA_LARGE_HIGH_CPU:
                         ami = ami64;
                         break;
                     default:
                         throw new IllegalArgumentException(format("Unexpected Amazon EC2 instance type '%s'",
                                 nodeProfileInfo.getNodeType().getName()));
                 }
                 logger.log(Level.INFO, "Starting cluster ["+clusterName+"], " +
                                        "type ["+nodeProfileInfo.getNodeType().getName()+"], " +
                                        "using AMI ["+ami+"]");
                 String override = null;
                 if (nodeProfileInfo.hasOverride())
                     override = "s3://" + overridesBucket;
                 futures.add(executor.submit(new StartInstanceTask(nodeInstantiator, clusterName,
                         nodeProfileInfo.getNodeProfile(), (EC2NodeType) nodeProfileInfo.getNodeType(),
                         override, ami, awsAccessID, awsSecretKey, awsSecured)));
             }
         }
 
         // wait for the threads to finish
         for (Future<List<String>> future : futures) {
             future.get(5 * 60, TimeUnit.SECONDS);
         }
     }
 
     public void stopCluster(String clusterName) throws ClusterException, RemoteException {
         logger.log(Level.INFO, "Stopping cluster ''{0}''", new Object[] { clusterName });
         // locate all nodes in the cluster
         Collection<EC2Node> nodes = clusterLocator.findNodes(clusterName);
         // stop each node one by one
         for (EC2Node node : nodes) {
             nodeInstantiator.shutdownInstance(node.getInstanceID());
         }
     }
 
     public Collection<EC2Cluster> findClusters() throws ClusterException, RemoteException {
         logger.log(Level.INFO, "Searching for clusters running on EC2...");
         Collection<String> clustersNames = clusterLocator.findClusters();
         List<EC2Cluster> clusters = new ArrayList<EC2Cluster>(clustersNames.size());
         for (String cluster : clustersNames) {
             clusters.add(cluster(cluster));
         }
         return clusters;
     }
 
     public EC2Cluster cluster(String name) throws RemoteException, ClusterException {
         EC2Cluster cluster = new EC2ClusterImpl();
         Set<EC2Node> nodes = clusterLocator.findNodes(name);
         if (nodes == null)
             return (EC2Cluster) cluster.name(name);
         else
             return (EC2Cluster) cluster.name(name).addNodes(nodes);
     }
 
     public void resizeCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         // inspect the current cluster in order to figure out its topology
         EC2Cluster cluster = cluster(clusterName);
 
         Set<EC2Node> monitors = cluster.getMonitorNodes();
         Set<EC2Node> agents = cluster.getAgentNodes();
         // figure out which MONITORs are actually MONITOR_AND_AGENT and update the MONITOR set
         Set<EC2Node> monitorsAndAgents = new HashSet<EC2Node>();
         Iterator<EC2Node> monitorsIterator = monitors.iterator();
         while (monitorsIterator.hasNext()) {
             EC2Node ec2Node =  monitors.iterator().next();
             if (ec2Node.getProfile().isAgent()) {
                 monitorsAndAgents.add(ec2Node);
                 monitorsIterator.remove();
             }
         }
 
         int numberOfMonitors = monitors.size();
         int numberOfMonitorsAndAgents = monitorsAndAgents.size();
         int numberOfAgents = agents.size();
 
         List<Future> futures = new LinkedList<Future>();
         for (NodeProfileInfo nodeProfileInfo : clusterTopology) {
             for (int i = 0; i < nodeProfileInfo.getNumber(); i++) {
                 String ami;
                 switch ((EC2NodeType) nodeProfileInfo.getNodeType()) {
                     case SMALL:
                         ami = ami32;
                         break;
                     case MEDIUM_HIGH_CPU:
                         ami = ami32;
                         break;
                     case LARGE:
                         ami = ami64;
                         break;
                     case EXTRA_LARGE:
                         ami = ami64;
                         break;
                     case EXTRA_LARGE_HIGH_CPU:
                         ami = ami64;
                         break;
                     default:
                         throw new IllegalArgumentException(format("Unexpected Amazon EC2 instance type '%s'",
                                 nodeProfileInfo.getNodeType().getName()));
                 }
                 int number = 0;
                 Iterator<EC2Node> nodesIterator = null;
                 switch (nodeProfileInfo.getNodeProfile()) {
                     case MONITOR:
                         number = numberOfMonitors - nodeProfileInfo.getNumber();
                         nodesIterator = monitors.iterator();
                         break;
                     case MONITOR_AND_AGENT:
                         number = numberOfMonitorsAndAgents - nodeProfileInfo.getNumber();
                         nodesIterator = monitorsAndAgents.iterator();
                         break;
                     case AGENT:
                         number = numberOfAgents - nodeProfileInfo.getNumber();
                         nodesIterator = agents.iterator();
                         break;
                 }
                 if (number > 0) {
                     logger.log(Level.INFO, "Scaling cluster ''{0}'' with {1} additional node(s)...", new Object[]{clusterName, number });
                     String profile = null;
                     switch (nodeProfileInfo.getNodeProfile()) {
                         case AGENT:
                             profile = "agent";
                             break;
                         case MONITOR:
                             profile = "monitor";
                             break;
                         case MONITOR_AND_AGENT:
                             profile = "monitor";
                             break;
                     }
                     String override = null;
                     if (nodeProfileInfo.hasOverride())
                         override = "s3://" + overridesBucket + "/" + clusterName + "/start-" + profile + ".groovy";
                     for (int j = 0; j < number; i++) {
                         futures.add(executor.submit(new StartInstanceTask(nodeInstantiator, clusterName, NodeProfile.MONITOR,
                             (EC2NodeType) nodeProfileInfo.getNodeType(), override, ami, awsAccessID, awsSecretKey, awsSecured)));
                     }
                 } else {
                     logger.log(Level.INFO, "Decreasing cluster ''{0}'' by {1} node(s)...", new Object[]{clusterName, Math.abs(number) });
                     int numberToStop = Math.abs(number);
                     while (numberToStop > 0) {
                         EC2Node node = nodesIterator.next();
                         futures.add(executor.submit(new StopInstanceTask(nodeInstantiator, node.getInstanceID())));
                     }
                 }
             }
         }
 
         // wait for the threads to finish
         for (Future future : futures) {
             future.get(5 * 60, TimeUnit.SECONDS);
         }
     }
 
     @Autowired(required = true)
     public void setNodeInstantiator(EC2Instantiator nodeInstantiator) {
         this.nodeInstantiator = nodeInstantiator;
     }
 
     @Required
     public void setDropBucket(String dropBucket) {
         this.dropBucket = dropBucket;
     }
 
     @Required
     public void setOverridesBucket(String overridesBucket) {
         this.overridesBucket = overridesBucket;
     }
 
     @Required
     public void setAwsAccessID(String awsAccessID) {
         this.awsAccessID = awsAccessID;
     }
 
     @Required
     public void setAwsSecretKey(String awsSecretKey) {
         this.awsSecretKey = awsSecretKey;
     }
 
     @Required
     public void setAwsSecured(boolean awsSecured) {
         this.awsSecured = awsSecured;
     }
 
     @Required
     public void setKeyName(String keyName) {
         this.keyName = keyName;
     }
 
     @Required
     public void setAmi32(String ami32) {
         this.ami32 = ami32;
         logger.info("\n********************\nSET AMI32 TO "+ami32);
     }
 
     @Required
     public void setAmi64(String ami64) {
         this.ami64 = ami64;
         logger.info("\n********************\nSET AMI64 TO "+ami64);
     }
 
     public void setClusterLocator(EC2ClusterLocator clusterLocator) {
         this.clusterLocator = clusterLocator;
     }
 
     class StartInstanceTask implements Callable<List<String>> {
         private EC2Instantiator nodeInstantiator;
         private String clusterName;
         private NodeProfile profile;
         private EC2NodeType instanceType;
         private String ami;
         private String userData;
                                                                                        
         public StartInstanceTask(EC2Instantiator nodeInstantiator, String clusterName, NodeProfile profile,
                                  EC2NodeType instanceType, String override, String ami,
                                  String awsAccessID, String awsSecretKey, boolean awsSecured) {
             this.nodeInstantiator = nodeInstantiator;
             this.clusterName = clusterName;
             this.profile = profile;
             this.instanceType = instanceType;
             this.ami = ami;
 
             StringBuffer buffer = new StringBuffer();
             buffer.append("CLUSTER_NAME=").append(clusterName);
             buffer.append(",AWS_ACCESS_ID=").append(awsAccessID);
             buffer.append(",AWS_SECRET_KEY=").append(awsSecretKey);
             buffer.append(",AWS_EC2_AMI32=").append(ami32);
             buffer.append(",AWS_EC2_AMI64=").append(ami64);
             buffer.append(",AWS_EC2_KEYPAIR=").append(keyName);
             buffer.append(",AWS_SQS_SECURED=").append(awsSecured);
             buffer.append(",DROP_BUCKET=").append(dropBucket);
             if (StringUtils.isNotEmpty(override))
                 buffer.append(",OVERRIDES_URL=").append(override);
             this.userData = buffer.toString();
         }
 
         public List<String> call() throws RemoteException {
             // ensure the Discovery.MONITOR group exists
             if (!nodeInstantiator.getGroupsNames().contains(Discovery.MONITOR.getGroupName())) {
                 nodeInstantiator.createSecurityGroup(Discovery.MONITOR.getGroupName());
             }
             // ensure the Discovery.AGENT group exists
             if (!nodeInstantiator.getGroupsNames().contains(Discovery.AGENT.getGroupName())) {
                 nodeInstantiator.createSecurityGroup(Discovery.AGENT.getGroupName());
             }
             // ensure the cluster name group exists
            String securityGroupNameForCluster = "elastic-grid-cluster-" + clusterName;            
             if (!nodeInstantiator.getGroupsNames().contains(securityGroupNameForCluster)) {
                nodeInstantiator.createSecurityGroup(securityGroupNameForCluster);
             }
             // start the agent node
             List<String> groups = null;
             switch (profile) {
                 case MONITOR:
                     groups = Arrays.asList(securityGroupNameForCluster, Discovery.MONITOR.getGroupName(), "elastic-grid");
                     break;
                 case AGENT:
                     groups = Arrays.asList(securityGroupNameForCluster, Discovery.AGENT.getGroupName(), "elastic-grid");
                     break;
                 case MONITOR_AND_AGENT:
                     groups = Arrays.asList(securityGroupNameForCluster, Discovery.MONITOR.getGroupName(),
                             Discovery.AGENT.getGroupName(), "elastic-grid");
                     break;
             }
             logger.log(Level.INFO, "Starting 1 Amazon EC2 instance from AMI {0} using groups {1} and user data {2}...",
                                        new Object[] { ami, groups.toString(), userData });
             return nodeInstantiator.startInstances(ami, 1, 1, groups, userData, keyName, true, instanceType);
         }
     }
 
     class StopInstanceTask implements Callable<Void> {
         private EC2Instantiator nodeInstantiator;
         private String instanceID;
 
         public StopInstanceTask(EC2Instantiator nodeInstantiator, String instanceID) {
             this.nodeInstantiator = nodeInstantiator;
             this.instanceID = instanceID;
         }
 
         public Void call() throws RemoteException {
             nodeInstantiator.shutdownInstance(instanceID);
             return null;
         }
     }
 }
