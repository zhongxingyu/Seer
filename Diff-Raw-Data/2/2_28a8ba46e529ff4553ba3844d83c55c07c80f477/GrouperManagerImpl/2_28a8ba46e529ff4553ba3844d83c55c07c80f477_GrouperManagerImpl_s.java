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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.osgi.service.event.Event;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
 import org.sakaiproject.nakamura.grouper.api.GrouperManager;
 import org.sakaiproject.nakamura.grouper.api.GrouperNameManager;
 import org.sakaiproject.nakamura.grouper.exception.GrouperException;
 import org.sakaiproject.nakamura.grouper.exception.GrouperWSException;
 import org.sakaiproject.nakamura.grouper.util.GrouperHttpUtil;
 import org.sakaiproject.nakamura.grouper.util.GrouperJsonUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
 import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDetail;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
 import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
 import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
 import edu.internet2.middleware.grouperClient.ws.beans.WsRestDeleteMemberRequest;
 import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupDeleteRequest;
 import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
 import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
 
 @Service
 @Component
 public class GrouperManagerImpl implements GrouperManager {
 
	private static final Logger log = LoggerFactory.getLogger(GrouperManager.class);
 
 	@Reference
 	protected GrouperConfiguration grouperConfiguration;
 
 	@Reference
 	protected GrouperNameManager grouperNameManager;
 	
 	@Reference
 	protected Repository repository;
 
 	/** 
 	 * @{inheritDoc}
 	 */
 	public void createGroup(String groupId, String[] groupTypes) throws GrouperException {
 		try {
 			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
 			AuthorizableManager authorizableManager = session.getAuthorizableManager();
 			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
 
 			if (!authorizable.isGroup()){
 				log.error("{} is not a group", authorizable.getId());
 				return;
 			}
 
 			String grouperName = grouperNameManager.getGrouperName(groupId);
 			String grouperExtension = BaseGrouperNameProvider.getGrouperExtension(groupId, grouperConfiguration);
 
 			log.debug("Creating a new Grouper Group = {} for sakai authorizableId = {}", 
 					grouperName, groupId);
 
 			// Fill out the group save request beans
 			WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();
 			WsGroupToSave wsGroupToSave = new WsGroupToSave();
 			wsGroupToSave.setWsGroupLookup(new WsGroupLookup(grouperName, null));
 			WsGroup wsGroup = new WsGroup();
 			wsGroup.setDescription((String)authorizable.getProperty("sakai:group-description"));
 			wsGroup.setDisplayExtension(grouperExtension);
 			wsGroup.setExtension(grouperExtension);
 			wsGroup.setName(grouperName);
 
 			// More detailed group info
 			WsGroupDetail groupDetail = new WsGroupDetail();
 			groupDetail.setTypeNames(groupTypes);
 			wsGroup.setDetail(groupDetail);
 
 			// Package up the request
 			wsGroupToSave.setWsGroup(wsGroup);
 			wsGroupToSave.setCreateParentStemsIfNotExist("T");
 			groupSave.setWsGroupToSaves(new WsGroupToSave[]{ wsGroupToSave });
 
 			JSONObject response = post("/groups", groupSave);
 
 			WsGroupSaveResults results = (WsGroupSaveResults)JSONObject.toBean(
 					response.getJSONObject("WsGroupSaveResults"), WsGroupSaveResults.class);
 			if (!"T".equals(results.getResultMetadata().getSuccess())) {
 				throw new GrouperWSException(results);
 			}
 
 			authorizable.setProperty(GROUPER_NAME_PROP, grouperName);
 			authorizableManager.updateAuthorizable(authorizable);
 			session.logout();
 
 			log.debug("Success! Created a new Grouper Group = {} for sakai authorizableId = {}", 
 					grouperName, groupId);
 		}
 		catch (StorageClientException sce) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId, sce);
 		}
 		catch (AccessDeniedException ade) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.", ade);
 		}
 	}
 
 	/**
 	 * @{inheritDoc}
 	 */
 	public void deleteGroup(String groupId) throws GrouperException {
 		// Try to figure out the grouper name for this group.
 		// If its still a group check for the GROUPER_NAME_PROP property
 		// If not use the groupIdHelper
 		try {
 			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
 			AuthorizableManager authorizableManager = session.getAuthorizableManager();
 			String grouperName = null;
 			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
 			if (authorizable != null){
 				if (!authorizable.isGroup()){
 					log.error("{} is not a group", authorizable.getId());
 					return;
 				}
 				Group group = (Group) authorizable;
 				grouperName = (String)group.getProperty(GROUPER_NAME_PROP);
 			}
 			if (grouperName == null){
 				grouperName = grouperNameManager.getGrouperName(groupId);
 			}
 
 			log.debug("Deleting Grouper Group = {} for sakai authorizableId = {}",
 					grouperName, groupId);
 			internalDeleteGroup(grouperName);
 			session.logout();
 		}
 		catch (StorageClientException sce){
 			throw new GrouperException("Unable to fetch authorizable for " + groupId);
 		}
 		catch (AccessDeniedException e) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.");
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deleteGroup(String groupId, Map<String, Object> attributes) throws GrouperException{
 		String grouperName = null;
 		if (attributes != null ){
 			grouperName = (String)attributes.get(GROUPER_NAME_PROP);
 		}
 
 		if (grouperName != null){
 			internalDeleteGroup(grouperName);
 		}
 		else {
 			deleteGroup(groupId);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param groupIdentifier either the grouper name or uuid
 	 * @throws GrouperException 
 	 */
 	private void internalDeleteGroup(String groupIdentifier) throws GrouperException{
 		try {
 			// Fill out the group delete request beans
 			WsRestGroupDeleteRequest groupDelete = new WsRestGroupDeleteRequest();
 			groupDelete.setWsGroupLookups(new WsGroupLookup[]{ new WsGroupLookup(groupIdentifier, null) });
 
 			// Send the request and parse the result, throwing an exception on failure.
 			JSONObject response = post("/groups", groupDelete);
 			WsGroupDeleteResults results = (WsGroupDeleteResults)JSONObject.toBean(
 					response.getJSONObject("WsGroupDeleteResults"), WsGroupDeleteResults.class);
 			if (!"T".equals(results.getResultMetadata().getSuccess())) {
 					throw new GrouperWSException(results);
 			}
 		}
 		catch (Exception e) {
 			throw new GrouperException(e.getMessage());
 		}
 	}
 	
 	/**
 	 * @{inheritDoc}
 	 */
 	public void addMemberships(String groupId, Collection<String> membersToAdd) throws GrouperException{
 		try {
 			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
 			AuthorizableManager authorizableManager = session.getAuthorizableManager();
 			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
 
 			if (!authorizable.isGroup()){
 				log.error("{} is not a group", authorizable.getId());
 				return;
 			}
 
 			String grouperName = grouperNameManager.getGrouperName(groupId);
 			String membersString = StringUtils.join(membersToAdd, ',');
 			log.debug("Adding members: Group = {} members = {}", 
 						grouperName, membersString);
 
 			// Clean the list of principles/subjects to be added.
 			Collection<String> cleanedMembersToAdd = new ArrayList<String>();
 			for (String subjectId: membersToAdd){
 				Authorizable authorizableToAdd = authorizableManager.findAuthorizable(subjectId);
 				if (authorizableToAdd == null){
 					log.error("Cannot find {}", subjectId);
 					continue;
 				}
 				if (authorizableToAdd.isGroup()){
 					log.error("Adding groups as members is not supported yet.");
 					continue;
 				}
 				// Don't bother adding the admin user as a member. 
 				// It probably doesn't exist in grouper.
 				if (subjectId.equals("admin")){
 					continue;
 				}
 				cleanedMembersToAdd.add(subjectId);
 			}
 
 			if (!cleanedMembersToAdd.isEmpty()){
 				// Each subjectId must have a lookup 
 				WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToAdd.size()];
 				int  i = 0;
 				for (String subjectId: membersToAdd){
 					// TODO - Specify the Grouper subject source in the lookup.
 					subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
 					i++;
 				}
 
 				WsRestAddMemberRequest addMembers = new WsRestAddMemberRequest();
 				// Don't overwrite the entire group membership. just add to it.
 				addMembers.setReplaceAllExisting("F");
 				addMembers.setSubjectLookups(subjectLookups);
 
 				String urlPath = "/groups/" + grouperName + "/members";
 				urlPath = urlPath.replace(":", "%3A");				
 				// Send the request and parse the result, throwing an exception on failure.
 				JSONObject response = post(urlPath, addMembers);
 				WsAddMemberResults results = (WsAddMemberResults)JSONObject.toBean(
 						response.getJSONObject("WsAddMemberResults"), WsAddMemberResults.class);
 				if (!"T".equals(results.getResultMetadata().getSuccess())) {
 						throw new GrouperWSException(results);
 				}
 				session.logout();
 				log.debug("Success! Added members: Group = {} members = {}", 
 						grouperName, membersString);
 			}
 		}
 		catch (StorageClientException sce) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId);
 		} 
 		catch (AccessDeniedException ade) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.");
 		}
 	}
 
 	/**
 	 * @{inheritDoc}
 	 */
 	public void removeMemberships(String groupId, Collection<String> membersToRemove) throws GrouperException {
 		try {
 			Session session = repository.loginAdministrative(grouperConfiguration.getIgnoredUserId());
 			AuthorizableManager authorizableManager = session.getAuthorizableManager();
 			Authorizable authorizable = authorizableManager.findAuthorizable(groupId);
 
 			if (!authorizable.isGroup()){
 				log.error("{} is not a group", authorizable.getId());
 				return;
 			}
 
 			String grouperName = grouperNameManager.getGrouperName(groupId);
 			String membersString = StringUtils.join(membersToRemove, ',');
 			log.debug("Removing members: Group = {} members = {}", 
 						grouperName, membersString);
 
 			WsRestDeleteMemberRequest deleteMembers = new WsRestDeleteMemberRequest();
 			// Each subjectId must have a lookup 
 			WsSubjectLookup[] subjectLookups = new WsSubjectLookup[membersToRemove.size()];
 			int  i = 0;
 			for (String subjectId: membersToRemove){
 				subjectLookups[i] = new WsSubjectLookup(subjectId, null, null);
 				i++;
 			}
 			deleteMembers.setSubjectLookups(subjectLookups);
 			String urlPath = "/groups/" + grouperName + "/members";
 			urlPath = urlPath.replace(":", "%3A");
 			JSONObject response = post(urlPath, deleteMembers);
 
 			WsDeleteMemberResults results = (WsDeleteMemberResults)JSONObject.toBean(
 					response.getJSONObject("WsDeleteMemberResults"), WsDeleteMemberResults.class);
 			if (!"T".equals(results.getResultMetadata().getSuccess())) {
 					throw new GrouperWSException(results);
 			}
 
 			log.debug("Success! Added members: Group = {} members = {}", 
 					grouperName, membersString);
 		}
 		catch (StorageClientException sce) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId);
 		} 
 		catch (AccessDeniedException ade) {
 			throw new GrouperException("Unable to fetch authorizable for " + groupId + ". Access Denied.");
 		}
 	}
 	
 	/**
 	 * @{inheritDoc}
 	 */
 	public void updateGroup(String groupId, Event event) throws GrouperException {
 		// TODO Auto-generated method stub
 	}
 	
 	/**
 	 * Issue an HTTP POST to Grouper Web Services
 	 * 
 	 * TODO: Is there a better type for the grouperRequestBean parameter?
 	 * 
 	 * @param grouperRequestBean a Grouper WS bean representing a grouper action
 	 * @return the parsed JSON response
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws GrouperException
 	 */
 	private JSONObject post(String urlPath, Object grouperRequestBean) throws GrouperException  {
 		try {
 			// URL e.g. http://localhost:9090/grouper-ws/servicesRest/v1_6_003/...
 			HttpClient client = GrouperHttpUtil.getHttpClient(grouperConfiguration);		            
 			String grouperWsRestUrl = grouperConfiguration.getRestWsUrlString() + urlPath;
 	        PostMethod method = new PostMethod(grouperWsRestUrl);
 	        method.setRequestHeader("Connection", "close");
 
 		    // Encode the request and send it off
 		    String requestDocument = GrouperJsonUtil.toJSONString(grouperRequestBean);
 		    method.setRequestEntity(new StringRequestEntity(requestDocument, "text/x-json", "UTF-8"));
 
 		    int responseCode = client.executeMethod(method);
 		    log.info("POST to {} : {}", grouperWsRestUrl, responseCode);
 		    String responseString = IOUtils.toString(method.getResponseBodyAsStream());
 		    return JSONObject.fromObject(responseString);
 		}
 		catch (Exception e) {
 			throw new GrouperException(e.getMessage());
 		}
 	}
 }
