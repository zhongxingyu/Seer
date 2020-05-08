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
 
 package com.elasticgrid.platforms.ec2.sla;
 
 import com.elasticgrid.config.EC2Configuration;
 import com.elasticgrid.config.GenericConfiguration;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.ec2.EC2NodeType;
 import com.elasticgrid.platforms.ec2.EC2CloudPlatformManagerFactory;
 import com.elasticgrid.platforms.ec2.EC2Instantiator;
 import com.elasticgrid.platforms.ec2.StartInstanceTask;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import org.rioproject.core.jsb.ServiceBeanContext;
 import org.rioproject.event.EventHandler;
 import org.rioproject.sla.SLA;
 import org.rioproject.sla.ScalingPolicyHandler;
 import com.xerox.amazonws.ec2.EC2Utils;
 import java.io.IOException;
 import static java.lang.String.format;
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * EC2 Scaling Policy Handler.
  *
  * @author Jerome Bernard
  */
 public class EC2ScalingPolicyHandler extends ScalingPolicyHandler {
     private EC2Instantiator ec2;
     private String clusterName;
     private EC2NodeType nodeType;
     private String ami;
     private String override;
     private String awsAccessID, awsSecretKey;
     private Boolean awsSecured;
     private final Properties egProps;
     private static final String AMAZON_INSTANCE_ID_PARAMETER = "amazonInstanceID";
     private static final Logger logger = Logger.getLogger(EC2ScalingPolicyHandler.class.getName());
 
     public EC2ScalingPolicyHandler(SLA sla) throws IOException {
         super(sla);
         egProps = AWSUtils.loadEC2Configuration();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void initialize(Object eventSource, EventHandler eventHandler, ServiceBeanContext context) {
         logger.info("Initialzing EC2 Scaling Policy Handler");
         super.initialize(eventSource, eventHandler, context);
         try {
             clusterName = egProps.getProperty(GenericConfiguration.EG_CLUSTER_NAME);
             if (clusterName == null)
                 clusterName = "elastic-grid";
             boolean ec2Mode = true;
             String egMode = egProps.getProperty(GenericConfiguration.EG_MODE);
             if (egMode != null && egMode.equalsIgnoreCase("lan")) {
                 ec2Mode = false;
             }
             if (ec2Mode)
                 ami = EC2Utils.getInstanceMetadata("ami-id"); // we clone ourselves
             else
                 ami = egProps.getProperty(EC2Configuration.AWS_EC2_AMI32);
             override = egProps.getProperty(EC2Configuration.EG_OVERRIDES_BUCKET);
             awsAccessID = egProps.getProperty(EC2Configuration.AWS_ACCESS_ID);
             awsSecretKey = egProps.getProperty(EC2Configuration.AWS_SECRET_KEY);
             awsSecured = Boolean.parseBoolean(egProps.getProperty(EC2Configuration.AWS_EC2_SECURED));
             if (ec2Mode)
                 nodeType = EC2NodeType.valueOf(EC2Utils.getInstanceMetadata("instance-type"));
             else
                 nodeType = EC2NodeType.SMALL;
             ec2 = new EC2CloudPlatformManagerFactory().getNodeInstantiator();
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Unexpected error when starting EC2 Scaling Policy Handler", e);
             e.printStackTrace();
         }
     }
 
     @Override
     protected void doIncrement() {
         if (ec2 == null) {
             logger.warning("No EC2 node instantiator has been set, hence no increase of EC2 instances will occur");
             return;
         }
         try {
             // ensure the cluster name group exists
             String securityGroupNameForCluster = "elastic-grid-cluster-" + clusterName;
             if (!ec2.getGroupsNames().contains(securityGroupNameForCluster)) {
                 ec2.createSecurityGroup(securityGroupNameForCluster);
             }
             // start another agent node from the currently running AMI
             List<String> instances = new StartInstanceTask(ec2, clusterName, NodeProfile.AGENT, nodeType, override,
                     ami, awsAccessID, awsSecretKey, awsSecured).call();
             String instanceID = instances.get(0);
             logger.log(Level.INFO, "Started Amazon EC2 instance {0}", instanceID);
             context.getServiceBeanConfig().getInitParameters().put(AMAZON_INSTANCE_ID_PARAMETER, instanceID);
         } catch (Exception e) {
             logger.log(Level.SEVERE, format("Can't start EC2 instance from AMI %s", ami), e);
         }
         super.doIncrement();
     }
 
     @Override
     /*
      * Only return true if the decrement needs to be rescheduled
      */
     protected boolean doDecrement() {
         super.doDecrement();
         if (ec2 == null) {
             logger.warning("No EC2 node instantiator has been set, hence no decrease of EC2 instances will occur");
             return false;
         }
         String instanceID = (String) context.getServiceBeanConfig().getInitParameters().get(AMAZON_INSTANCE_ID_PARAMETER);
         try {
             ec2.shutdownInstance(instanceID);
             logger.log(Level.INFO, "Stopped Amazon EC2 instance {0}", instanceID);
         } catch (RemoteException e) {
             logger.log(Level.SEVERE, format("Can't shutdown EC2 instance %s", instanceID), e);
         }
         return false;
     }
 
     @Override
     public String getDescription() {
         return "EC2 Scaling Policy Handler";
     }
 }
