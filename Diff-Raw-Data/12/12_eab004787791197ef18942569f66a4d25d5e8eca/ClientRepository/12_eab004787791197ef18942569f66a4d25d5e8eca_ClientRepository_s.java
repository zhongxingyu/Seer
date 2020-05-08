 /**
  * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
  *
  * This file is part of Snooze, a scalable, autonomic, and
  * energy-aware virtual machine (VM) management framework.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  */
 package org.inria.myriads.snoozeclient.database.api;
 
 import java.util.List;
 
 import org.inria.myriads.snoozecommon.communication.NetworkAddress;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.monitoring.NetworkDemand;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualClusterSubmissionRequest;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualClusterSubmissionResponse;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualMachineTemplate;
 
 /**
  * Client repository interface.
  * 
  * @author Eugen Feller
  *
  */
 public interface ClientRepository 
 {
     /**
      * Defines a cluster.
      * 
      * @param virtualClusterId  The virtual cluster identifier
      * @return                  true if everything ok, false otherwise
      * @throws Exception        The exception
      */
     boolean defineVirtualCluster(String virtualClusterId) 
         throws Exception;
     
     /**
      * Undefines a cluster.
      * 
      * @param virtualClusterId  The virtual cluster identifier
      * @return                  true if everything ok, false otherwise
      * @throws Exception        The exception
      */
     boolean undefineVirtualCluster(String virtualClusterId)
         throws Exception;
     
     /**
      * Add a virtual machine template.
      * 
      * @param template              The virtual machine template
      * @param virtualClusterId      The virtual cluster identifier
      * @return                      true if everything ok, false otherwise
      * @throws Exception            The exception
      */
     boolean addVirtualMachineTemplate(VirtualMachineTemplate template, String virtualClusterId)
         throws Exception;
         
     /**
      * Removes virtual machine description from a virtual cluster.
      * 
      * @param virtualMachineId    The virtual machine identifier
      * @param virtualClusterId    The virtual cluster identifier
      * @return                    true if everything ok, false otherwise
      * @throws Exception          The exception
      */
     boolean removeVirtualMachineDescription(String virtualMachineId, String virtualClusterId) 
         throws Exception;
         
     /**
      * Creates virtual cluster submission request.
      * 
      * @param attributeName     The attribute name
      * @param attributeType     The attribute type
      * @return                  The virtual cluster submission request
      * @throws Exception        The exception
      */
     VirtualClusterSubmissionRequest createVirtualClusterSubmissionRequest(String attributeName, 
                                                                           AttributeType attributeType) 
         throws Exception;
 
     /**
      * Returns virtual machine names.
      * 
      * @param virtualClusterId  The virtual cluster identifier
      * @return                  The list of virtual machine identifiers
      * @throws Exception        The exception
      */
     List<String> getVirtualMachineIds(String virtualClusterId) 
         throws Exception;
     
     /**
      * Prints the clusters.
      * 
      * @throws Exception        The exception
      */
     void printVirtualClusters() 
         throws Exception;
 
     /**
      * Prints the cluster content.
      * 
      * @param virtualClusterId  The virtual cluster identifier
      * @throws Exception        The exception
      */
     void printVirtualCluster(String virtualClusterId) 
         throws Exception;
 
     /**
      * Returns the virtual machine meta data.
      * 
      * @param virtualMachineId  The virtual machine identifier
      * @return                  The virtual machine meta data
      * @throws Exception        The exception
      */
     VirtualMachineMetaData getVirtualMachineMetaData(String virtualMachineId)   
         throws Exception;
 
     /**
      * Adds virtual cluster response.
      * 
      * @param virtualClusterResponse    The virtual cluster response
      * @throws Exception                The exception
      */
     void addVirtualClusterResponse(VirtualClusterSubmissionResponse virtualClusterResponse) 
         throws Exception;
 
     /**
      * Updates virtual machine meta data.
      * 
      * @param virtualMachineId      The virtual machine identifier
      * @param localControllerId     The local controller identifier
      * @param groupManagerAddress   The group manager address
      * @throws Exception 
      */
     void updateVirtualMachineMetaData(String virtualMachineId, 
                                       String localControllerId,
                                       NetworkAddress groupManagerAddress)
         throws Exception;
     
     /**
      * 
      * Gets the content of the templates of the virtual machine.
      * 
      * @param virtualMachineId          The virtual machine id
      * @return                          The content of the template
      * @throws Exception 
      */
     String getVirtualMachineTemplateContent(String virtualMachineId)
         throws Exception;
     
     /**
      * 
      * Gets the path of the templates of the virtual machine.
      * 
      * @param virtualMachineId          The virtual machine id
      * @return                          The template path
     * @throws Exception
      */
     String getVirtualMachineTemplate(String virtualMachineId)
             throws Exception;
     
     /**
      * 
      * Update the network capacity demand.
      * 
      * @param virtualMachineId          The virtual machine id
      * @param networkDemand             The network demand
     * @throws Exception
      */
     void updateNetworkCapacityDemand(String virtualMachineId, NetworkDemand networkDemand) 
             throws Exception;
 }
