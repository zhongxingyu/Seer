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
 
 import com.elasticgrid.cluster.spi.CloudPlatformManagerFactory;
 import com.elasticgrid.config.EC2Configuration;
 import com.elasticgrid.model.ec2.EC2Cluster;
 import com.elasticgrid.platforms.ec2.discovery.EC2ClusterLocator;
 import com.elasticgrid.platforms.ec2.discovery.EC2SecurityGroupsClusterLocator;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import com.xerox.amazonws.ec2.Jec2;
 import java.io.IOException;
 import java.util.Properties;
 
 public class EC2CloudPlatformManagerFactory implements CloudPlatformManagerFactory<EC2Cluster> {
     static EC2CloudPlatformManager instance;
     static EC2SecurityGroupsClusterLocator clusterLocator;
     static EC2Instantiator nodeInstantiator;
     static Jec2 ec2;
 
     public EC2CloudPlatformManager getInstance() throws IOException {
         if (instance == null) {
             Properties config = AWSUtils.loadEC2Configuration();
 
             String awsAccessId = config.getProperty(EC2Configuration.AWS_ACCESS_ID);
             String awsSecretKey = config.getProperty(EC2Configuration.AWS_SECRET_KEY);
             boolean secured = Boolean.parseBoolean(config.getProperty(EC2Configuration.AWS_EC2_SECURED));
 
             instance = new EC2CloudPlatformManager();
             instance.setOverridesBucket(config.getProperty(EC2Configuration.EG_OVERRIDES_BUCKET));
             instance.setAwsAccessID(awsAccessId);
             instance.setAwsSecretKey(awsSecretKey);
             instance.setAwsSecured(secured);
             instance.setAmi32(config.getProperty(EC2Configuration.AWS_EC2_AMI32));
            instance.setAmi64(config.getProperty(EC2Configuration.AWS_EC2_AMI64));
             instance.setClusterLocator(getClusterLocator());
             instance.setNodeInstantiator(getNodeInstantiator());
             String timeout = config.getProperty(EC2Configuration.EG_CLUSTER_START_STOP_TIMEOUT);
             if (timeout != null)
                 instance.setStartStopTimeout(Integer.parseInt(timeout));
         }
 
         return instance;
     }
 
     public EC2ClusterLocator getClusterLocator() throws IOException {
         if (clusterLocator == null) {
             clusterLocator = new EC2SecurityGroupsClusterLocator();
             clusterLocator.setEc2(getEC2());
         }
         return clusterLocator;
     }
 
     public EC2Instantiator getNodeInstantiator() throws IOException {
         if (nodeInstantiator == null) {
             nodeInstantiator = new EC2InstantiatorImpl();
             ((EC2InstantiatorImpl) nodeInstantiator).setEc2(getEC2());
         }
         return nodeInstantiator;
     }
 
     private Jec2 getEC2() throws IOException {
         if (ec2 == null) {
             Properties config = AWSUtils.loadEC2Configuration();
             String awsAccessId = config.getProperty(EC2Configuration.AWS_ACCESS_ID);
             String awsSecretKey = config.getProperty(EC2Configuration.AWS_SECRET_KEY);
             boolean secured = Boolean.parseBoolean(config.getProperty(EC2Configuration.AWS_EC2_SECURED));
             ec2 = new Jec2(awsAccessId, awsSecretKey, secured);
         }
         return ec2;
     }
 
 }
