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
 
 import com.elasticgrid.config.EC2Configuration;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.Discovery;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.ec2.EC2NodeType;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import org.apache.commons.lang.StringUtils;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class StartInstanceTask implements Callable<List<String>> {
     private EC2Instantiator nodeInstantiator;
     private String clusterName;
     private NodeProfile profile;
     private EC2NodeType instanceType;
     private String ami;
     private String keypair;
     private String userData;
     private static final Logger logger = Logger.getLogger(StartInstanceTask.class.getName());
 
     public StartInstanceTask(EC2Instantiator nodeInstantiator, String clusterName, NodeProfile profile,
                              EC2NodeType instanceType, String override, String ami,
                              String awsAccessID, String awsSecretKey, boolean awsSecured) throws ClusterException {
         this.nodeInstantiator = nodeInstantiator;
         this.clusterName = clusterName;
         this.profile = profile;
         this.instanceType = instanceType;
         this.ami = ami;
 
         try {
             Properties egProps = AWSUtils.loadEC2Configuration();
             String ami32 = egProps.getProperty(EC2Configuration.AWS_EC2_AMI32);
             String ami64 = egProps.getProperty(EC2Configuration.AWS_EC2_AMI64);
             keypair = egProps.getProperty(EC2Configuration.AWS_EC2_KEYPAIR);
             String dropBucket = egProps.getProperty(EC2Configuration.EG_DROP_BUCKET);
 
             StringBuffer buffer = new StringBuffer();
             buffer.append("CLUSTER_NAME=").append(clusterName);
             buffer.append(",AWS_ACCESS_ID=").append(awsAccessID);
             buffer.append(",AWS_SECRET_KEY=").append(awsSecretKey);
             buffer.append(",AWS_EC2_AMI32=").append(ami32);
             buffer.append(",AWS_EC2_AMI64=").append(ami64);
             buffer.append(",AWS_EC2_KEYPAIR=").append(keypair);
             buffer.append(",AWS_SQS_SECURED=").append(awsSecured);
             buffer.append(",DROP_BUCKET=").append(dropBucket);
             if (StringUtils.isNotEmpty(override))
                 buffer.append(",OVERRIDES_URL=").append(override);
             this.userData = buffer.toString();
         } catch (IOException e) {
             throw new ClusterException("Can't read Elastic Grid configuration file", e);
         }
     }
 
     public List<String> call() throws RemoteException {
         String securityGroupNameForCluster = "elastic-grid-cluster-" + clusterName;
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
        logger.log(Level.FINE, "Starting 1 Amazon EC2 instance from AMI {0} using groups {1} and user data {2}...",
                 new Object[]{ami, groups.toString(), userData});
         return nodeInstantiator.startInstances(ami, 1, 1, groups, userData, keypair, true, instanceType);
     }
 }
