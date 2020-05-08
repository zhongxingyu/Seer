 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.grouper;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.commons.osgi.OsgiUtil;
 import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
 import org.sakaiproject.nakamura.grouper.api.GrouperNameManager;
 import org.sakaiproject.nakamura.grouper.api.GrouperNameProvider;
 import org.sakaiproject.nakamura.util.osgi.AbstractOrderedService;
 
 /**
  * Generate a namne for a group in grouper based on the groupId of a Group in nakamura.
  *
  * Holds an ordered {@link List} of {@link GrouperNameProvider}s. The first one to return
  * an answer for the group will be used.
  *
  * @author froese
  *
  */
 @Service
 @Component
 public class GrouperNameManagerImpl extends AbstractOrderedService<GrouperNameProvider> implements GrouperNameManager { 
 
 	protected GrouperNameProvider[] orderedServices = new GrouperNameProvider[0];
 
 	@Reference
 	protected GrouperConfiguration config;
 
 	@Override
 	public String getGrouperName(String groupId) {
 		String gn = null;
 		for (GrouperNameProvider gnp: orderedServices){
 			gn = gnp.getGrouperName(groupId);
 			if (gn != null){
 				return gn;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get the extension for this group in Grouper.
 	 * The extension is the last component in a Grouper name
 	 */
 	public String getGrouperExtension(String groupId) {
 		return BaseGrouperNameProvider.getGrouperExtension(groupId, config);
 	}
 
	protected void bindAuthorizablePostProcessor(GrouperNameProvider service, Map<String, Object> properties) {
		addService(service, properties);
 	}
 
	protected void unbindAuthorizablePostProcessor(GrouperNameProvider service, Map<String, Object> properties) {
		removeService(service, properties);
 	}
 
	protected void saveArray(List<GrouperNameProvider> serviceList) {
		orderedServices = serviceList.toArray(new GrouperNameProvider[serviceList.size()]);
 	}
 
 	@Override
 	protected Comparator<? super GrouperNameProvider> getComparator(
 			final Map<GrouperNameProvider, Map<String, Object>> propertiesMap) {
 		return new Comparator<GrouperNameProvider>() {
 			public int compare(GrouperNameProvider o1, GrouperNameProvider o2) {
 				Map<String, Object> props1 = propertiesMap.get(o1);
 				Map<String, Object> props2 = propertiesMap.get(o2);
 
 				return OsgiUtil.getComparableForServiceRanking(props1).compareTo(props2);
 			}
 		};
 	}
 
 }
