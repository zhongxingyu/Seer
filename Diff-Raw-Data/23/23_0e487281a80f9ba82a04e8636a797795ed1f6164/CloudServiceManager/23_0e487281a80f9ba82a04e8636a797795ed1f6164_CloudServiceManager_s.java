 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package test.cli.cloudify.cloud.services;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 import test.cli.cloudify.cloud.services.ec2.Ec2WinCloudService;
 import test.cli.cloudify.cloud.services.hp.HpCloudService;
 import test.cli.cloudify.cloud.services.rackspace.RackspaceCloudService;
import test.cli.cloudify.cloud.services.terremark.TerremarkCloudService;
 
 public class CloudServiceManager {
 
 	private static CloudServiceManager instance = null;
 	
 	private static final Map<String, Map<String, CloudService>> allCloudServices = 
 			new HashMap<String, Map<String, CloudService>>();
 
 	protected CloudServiceManager() {
 		// Exists only to defeat instantiation.
 	}
 
 	public static CloudServiceManager getInstance() {
 		if (instance == null) {
 			instance = new CloudServiceManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Gets the service instance of the given cloud and with the specified unique name
 	 * @param cloudName The cloudName of the service
 	 * @param uniqueName The unique name of the service
 	 * @return The matching cached CloudService instance, or a new instance of non was cached.
 	 */
 	public CloudService getCloudService(String cloudName, String uniqueName) {
 		if (allCloudServices.get(cloudName) == null) {
 			allCloudServices.put(cloudName, new HashMap<String, CloudService>());
 		}
 		
 		Map<String, CloudService> servicesByNamesMap = allCloudServices.get(cloudName);
 		if (servicesByNamesMap.get(uniqueName) == null) {
 			servicesByNamesMap.put(uniqueName, createCloudService(cloudName, uniqueName));	
 		}
 		
 		return servicesByNamesMap.get(uniqueName);
 	}
 	
 	/**
 	 * Gets all the cloud services
 	 * @return A set of all cached cloud services
 	 */
 	public Set<CloudService> getAllCloudServices() {
 		Set<CloudService> cloudServicesSet = new HashSet<CloudService>();
 		
 		for (Entry<String, Map<String, CloudService>> cloudServicesMap: allCloudServices.entrySet()) {
 			cloudServicesSet.addAll(cloudServicesMap.getValue().values());
 		}
 		
 		return cloudServicesSet;
 	}
 	
 	public void clearCache() {
 		allCloudServices.clear();
 	}
 
 	private CloudService createCloudService(String cloudName, String serviceUniqueName) {
 		CloudService cloudService = null;
 
		if ("BYON".equalsIgnoreCase(cloudName)) {
 			cloudService = new ByonCloudService(serviceUniqueName);
		} else if ("EC2".equalsIgnoreCase(cloudName)) {
 			cloudService = new Ec2CloudService(serviceUniqueName);
		} else if ("EC2_Win".equalsIgnoreCase(cloudName)) {
 			cloudService = new Ec2WinCloudService(serviceUniqueName);
		} else if ("HP".equalsIgnoreCase(cloudName)) {
 			cloudService = new HpCloudService(serviceUniqueName);
		} else if ("Rackspace".equalsIgnoreCase(cloudName)) {
 			cloudService = new RackspaceCloudService(serviceUniqueName);
		} else if ("Terremark".equalsIgnoreCase(cloudName)) {
			cloudService = TerremarkCloudService.getService(serviceUniqueName);
 		}
 		return cloudService;
 	}
 }
