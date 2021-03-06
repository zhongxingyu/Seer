 /*
  * Copyright 2012 The Clustermeister Team.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.nethad.clustermeister.provisioning.ec2;
 
 import com.github.nethad.clustermeister.api.Credentials;
 import com.github.nethad.clustermeister.api.JPPFConstants;
 import com.github.nethad.clustermeister.api.NodeCapabilities;
 import com.github.nethad.clustermeister.api.NodeConfiguration;
 import com.github.nethad.clustermeister.api.NodeType;
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import java.io.File;
 import java.util.Collection;
 import java.util.Collections;
 import org.jclouds.compute.domain.OsFamily;
 import org.jclouds.compute.domain.Template;
 import org.jclouds.compute.domain.TemplateBuilder;
 import org.jclouds.ec2.domain.InstanceType;
 
 /**
  *
  * @author daniel
  */
 public class AmazonNodeConfiguration implements NodeConfiguration {
     
     private AWSInstanceProfile profile = null;
     private NodeType nodeType = NodeType.NODE;
     private Optional<Credentials> credentials = Optional.absent();
     private String driverAddress = "";
     private boolean driverDeployedLocally = false;
     private int managementPort = JPPFConstants.DEFAULT_MANAGEMENT_PORT;
     private NodeCapabilities nodeCapabilities = null;
     private Collection<File> artifactsToPreload = Collections.EMPTY_LIST;
 
     public static AmazonNodeConfiguration fromInstanceProfile(
             AWSInstanceProfile instanceProfile) {
         
         return new AmazonNodeConfiguration(instanceProfile);
     }
 
     /**
      * Default Constructor.
      */
 //    public AmazonNodeConfiguration() {}
     
     private AmazonNodeConfiguration(AWSInstanceProfile instanceProfile) {
         this.profile = instanceProfile;
     }
     
     public void setNodeType(NodeType nodeType) {
         this.nodeType = nodeType;
     }
 
     
     @Override
     public NodeType getType() {
         return nodeType;
     }
 
     public void setCredentials(Credentials credentials) {
         this.credentials = Optional.fromNullable(credentials);
     }
 
     public Optional<Credentials> getCredentials() {
         return credentials;
     }
     
     public void setDriverAddress(String driverAddress) {
         this.driverAddress = driverAddress;
     }
 
     @Override
     public String getDriverAddress() {
         return driverAddress;
     }
 
     public void setDriverDeployedLocally(boolean driverDeployedLocally) {
         this.driverDeployedLocally = driverDeployedLocally;
     }
 
     @Override
     public boolean isDriverDeployedLocally() {
         return driverDeployedLocally;
     }
 
    public String getLocation() {
        return profile.getRegion();
    }

    public Optional<String> getImageId() {
        return profile.getAmiId();
     }
     
     void setManagementPort(int managementPort) {
         this.managementPort = managementPort;
     }
 
     int getManagementPort() {
         return managementPort;
     }
 
     public void setNodeCapabilities(NodeCapabilities nodeCapabilities) {
         this.nodeCapabilities = nodeCapabilities;
     }
 
     public NodeCapabilities getNodeCapabilities() {
         return nodeCapabilities;
     }
     
     Template getTemplate(TemplateBuilder templateBuilder) {
        //takes zone or region
        templateBuilder.locationId(profile.getRegion());
         
        if(getImageId().isPresent()) {
            //TODO: zone vs region! needs region!
             String jCloudsIMageId = 
                    Joiner.on('/').join(getLocation(), getImageId().get());
             templateBuilder.imageId(jCloudsIMageId);
         } else {
             templateBuilder.hardwareId(InstanceType.T1_MICRO);
             templateBuilder.osFamily(OsFamily.AMZN_LINUX);
         }
         return templateBuilder.build();
     }
 
     public void setArtifactsToPreload(Collection<File> artifacts) {
         artifactsToPreload = artifacts;
     }
     
     @Override
     public Collection<File> getArtifactsToPreload() {
         return artifactsToPreload;
     }
 }
