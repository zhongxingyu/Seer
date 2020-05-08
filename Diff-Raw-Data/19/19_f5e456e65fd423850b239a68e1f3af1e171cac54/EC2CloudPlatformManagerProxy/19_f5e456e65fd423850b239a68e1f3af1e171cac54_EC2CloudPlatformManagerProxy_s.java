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
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.NodeProfileInfo;
 import com.elasticgrid.model.ec2.EC2Cluster;
 import net.jini.id.Uuid;
 import org.rioproject.resources.servicecore.AbstractProxy;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 
 /**
  * Proxy for the {@link com.elasticgrid.platforms.lan.LANCloudPlatformManager}.
  * @author Jerome Bernard
  */
 public class EC2CloudPlatformManagerProxy extends AbstractProxy implements CloudPlatformManager<EC2Cluster>, Remote {
     private CloudPlatformManager<EC2Cluster> cloud;
 
     public EC2CloudPlatformManagerProxy(CloudPlatformManager<EC2Cluster> server, Uuid uuid) {
         super(server, uuid);
         this.cloud = server;
     }
 
     public String getName() throws RemoteException {
         return cloud.getName();
     }
 
     public void startCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         cloud.startCluster(clusterName, clusterTopology);
     }
 
     public void stopCluster(String clusterName) throws ClusterException, RemoteException {
         cloud.stopCluster(clusterName);
     }
 
     public Collection<EC2Cluster> findClusters() throws ClusterException, RemoteException {
         return cloud.findClusters();
     }
 
     public EC2Cluster cluster(String name) throws RemoteException, ClusterException {
         return cloud.cluster(name);
     }
 
     public void resizeCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         cloud.resizeCluster(clusterName, clusterTopology);
     }
 
 }
