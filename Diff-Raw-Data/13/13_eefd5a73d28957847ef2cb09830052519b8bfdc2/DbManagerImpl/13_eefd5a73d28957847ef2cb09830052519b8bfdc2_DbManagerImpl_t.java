 /**
  * Phresco Service Implemenation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Order;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.data.document.mongodb.query.Update;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.User.AuthType;
 import com.photon.phresco.commons.model.VersionInfo;
 import com.photon.phresco.commons.model.VideoInfo;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.DbManager;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.ProjectInfoDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.dao.VideoInfoDAO;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.ServiceConstants;
 
 public class DbManagerImpl extends DbService implements DbManager, ServiceConstants {
 
     @Override
     public ArtifactGroup getArchetypeInfo(String techId, String customerId)
             throws PhrescoException {
 //    	Query techQuery = createCustomerIdQuery(customerId);
     	Query techQuery = new Query();
     	Criteria criteria = Criteria.whereId().is(techId);
     	techQuery.addCriteria(criteria);
     	TechnologyDAO techDAO = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
     			techQuery, TechnologyDAO.class);
     	ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
     			new Query(Criteria.whereId().is(techDAO.getArchetypeGroupDAOId())), ArtifactGroupDAO.class);
         return convertArtifactDAO(artifactGroupDAO);
     }
 
     @Override
     public ApplicationInfo getApplicationInfo(String pilotId)
             throws PhrescoException {
     	Criteria criteria = Criteria.whereId().is(pilotId);
     	Query pilotQuery = new Query();
     	pilotQuery.addCriteria(criteria);
     	ApplicationInfoDAO appDAO = mongoOperation.findOne(APPLICATION_INFO_COLLECTION_NAME, pilotQuery, ApplicationInfoDAO.class);
         return convertApplicationDAO(appDAO);
     }
 
     @Override
     public Technology getTechnologyDoc(String techId)
             throws PhrescoException {
         Technology technology = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
                 new Query(Criteria.whereId().is(techId)), Technology.class);
         return technology;
     }
 
     @Override
     public RepoInfo getRepoInfo(String customerId) throws PhrescoException {
     	Customer customer = mongoOperation.findOne(CUSTOMERS_COLLECTION_NAME, new Query(Criteria.whereId().is(customerId)), Customer.class);
     	if(customer != null) {
     		return customer.getRepoInfo();
     	}
         return null;
     }
 
     @Override
     public void storeCreatedProjects(ProjectInfo projectInfo) throws PhrescoException {
         if(projectInfo != null) {
         	Converter<ProjectInfoDAO, ProjectInfo> projectConverter = 
         		(Converter<ProjectInfoDAO, ProjectInfo>) ConvertersFactory.getConverter(ProjectInfoDAO.class);
         	ProjectInfoDAO projectInfoDAO = projectConverter.convertObjectToDAO(projectInfo);
        	
        	ProjectInfoDAO pDAO = mongoOperation.findOne("projectInfo", 
        			new Query(Criteria.whereId().is(projectInfo.getId())), ProjectInfoDAO.class);
        	if (pDAO != null) {
        		List<String> applicationInfoIdsFromDB = pDAO.getApplicationInfoIds();
            	List<String> applicationInfoIdsNew = projectInfoDAO.getApplicationInfoIds();
            	for (String string : applicationInfoIdsNew) {
    				if(!applicationInfoIdsFromDB.contains(string)) {
    					applicationInfoIdsFromDB.add(string);
    				}
    			}
            	projectInfoDAO.setApplicationInfoIds(applicationInfoIdsFromDB);
        	}
         	mongoOperation.save("projectInfo", projectInfoDAO);
         	List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
         	Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
         		(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
         	for (ApplicationInfo applicationInfo : appInfos) {
         		mongoOperation.save(APPLICATION_INFO_COLLECTION_NAME, appConverter.convertObjectToDAO(applicationInfo));
 			}
         }
     }
 
     @Override
     public void updateCreatedProjects(ApplicationInfo projectInfo)
             throws PhrescoException {
         if(projectInfo != null) {
             mongoOperation.save(CREATEDPROJECTS_COLLECTION_NAME, projectInfo);
         }
     }
     
     @Override
     public void updateUsedObjects(ProjectInfo projectInfo)
             throws PhrescoException {
     	List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 		for (ApplicationInfo applicationInfo : appInfos) {
 			List<String> selectedModulesIds = applicationInfo.getSelectedModules();
 			if(CollectionUtils.isNotEmpty(selectedModulesIds)) {
 				updateUsedObjectsId(selectedModulesIds);
 			}
 			List<String> selectedJSLibs = applicationInfo.getSelectedJSLibs();
 			if(CollectionUtils.isNotEmpty(selectedJSLibs)){
 				updateUsedObjectsId(selectedJSLibs);	
 			}
 			
 			List<String> selectedComponents = applicationInfo.getSelectedComponents();
 			if(CollectionUtils.isNotEmpty(selectedComponents)){
 				updateUsedObjectsId(selectedComponents);
 			}
 			List<ArtifactGroupInfo> selectedServers = applicationInfo.getSelectedServers();
 			if(CollectionUtils.isNotEmpty(selectedServers)){
 				updateUsedObjects(selectedServers);
 			}
 			List<ArtifactGroupInfo> selectedDatabases = applicationInfo.getSelectedDatabases();
 			if(CollectionUtils.isNotEmpty(selectedDatabases)){
 				updateUsedObjects(selectedDatabases);
 			}
 		}
     }
     
     private void updateUsedObjects(List<ArtifactGroupInfo> selectedServers) {
     	for (ArtifactGroupInfo artifactGroupInfo : selectedServers) {
     		updateUsedObjectsId(artifactGroupInfo.getArtifactInfoIds());
 		}
 	}
 
 	private void updateUsedObjectsId(List<String> selectedModulesIds){
     	for (String id : selectedModulesIds) {
 			Query query = new Query(Criteria.where("_id").is(id));
 			mongoOperation.updateFirst(ARTIFACT_INFO_COLLECTION_NAME, 
 					query, Update.update("used", true));
 		}
      }
 
 	@Override
 	public List<ArtifactGroup> findSelectedArtifacts(List<String> ids, String customerId, String type)
 			throws PhrescoException {
 		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 		for (String id : ids) {
 			if(getArtifactGroup(id, customerId, type) != null) {
 				artifactGroups.add(getArtifactGroup(id, customerId, type));
 			}
 		}
 		
 		return artifactGroups;
 	}
 
 	private ArtifactGroup getArtifactGroup(String id, String customerId, String type) throws PhrescoException {
 		com.photon.phresco.commons.model.ArtifactInfo artifactInfo = mongoOperation.findOne(ARTIFACT_INFO_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(id)), com.photon.phresco.commons.model.ArtifactInfo.class);
 		if(artifactInfo == null) {
 			return null;
 		}
 		String artifactGroupId = artifactInfo.getArtifactGroupId();
 		Query query = createCustomerIdQuery(customerId);
 		Criteria typeCriteria = Criteria.whereId().is(type);
 		query.addCriteria(typeCriteria);
 		Criteria artifactGroupCriteria = Criteria.whereId().is(artifactGroupId);
 		query.addCriteria(artifactGroupCriteria);
 		ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, query, ArtifactGroupDAO.class);
 		ArtifactGroup artifactGroup = convertArtifactDAO(artifactGroupDAO);
 		return makeArtifactGroup(artifactGroup, artifactInfo);
 	}
 
 	private ArtifactGroup makeArtifactGroup(ArtifactGroup artifactGroup,
 			com.photon.phresco.commons.model.ArtifactInfo artifactInfo) {
 		ArtifactGroup group = new ArtifactGroup();
 		group.setGroupId(artifactGroup.getGroupId());
 		group.setArtifactId(artifactGroup.getArtifactId());
 		group.setPackaging(artifactGroup.getPackaging());
 		List<com.photon.phresco.commons.model.ArtifactInfo> artifactInfos = new ArrayList<com.photon.phresco.commons.model.ArtifactInfo>();
 		artifactInfos.add(artifactInfo);
 		group.setVersions(artifactInfos);
 		return artifactGroup;
 	}
 
 	@Override
 	public List<WebService> findSelectedWebservices(List<String> ids)
 			throws PhrescoException {
 		return mongoOperation.find(WEBSERVICES_COLLECTION_NAME, new Query(Criteria.whereId().in(ids.toArray())), WebService.class);
 	}
 
 	@Override
 	public List<DownloadInfo> findSelectedServers(List<String> ids, String customerId)
 			throws PhrescoException {
 		Query query = createCustomerIdQuery(customerId);
 		Criteria idCriteria = Criteria.whereId().in(ids.toArray());
 		query.addCriteria(idCriteria);
 		Criteria categoryCriteria = Criteria.where(CATEGORY).is(SERVER);
 		query.addCriteria(categoryCriteria);
 		List<DownloadsDAO> downloadsDAOs = mongoOperation.find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 		return convertDownloadDAOs(downloadsDAOs);
 	}
 
 	@Override
 	public List<DownloadInfo> findSelectedDatabases(List<String> ids, String customerId)
 			throws PhrescoException {
 		Query query = createCustomerIdQuery(customerId);
 		Criteria idCriteria = Criteria.whereId().in(ids.toArray());
 		query.addCriteria(idCriteria);
 		Criteria categoryCriteria = Criteria.where(CATEGORY).is(DATABASE);
 		query.addCriteria(categoryCriteria);
 		List<DownloadsDAO> downloadsDAOs = mongoOperation.find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 		return convertDownloadDAOs(downloadsDAOs);
 	}
 
 	@Override
 	public User authenticate(String username, String password)
 			throws PhrescoException {
 		String decryptedPassword = ServerUtil.decryptString(password);
 		String hashedPwd = ServerUtil.encodeUsingHash(username, decryptedPassword);
 		Query query = new Query();
 		Criteria nameCriteria = Criteria.where(REST_API_NAME).is(username);
 		Criteria pwdCriteria = Criteria.where(PASSWORD).is(hashedPwd);
 		Criteria typeCriteria = Criteria.where(AUTHTYPE).is(AuthType.LOCAL.name());
 		query = query.addCriteria(nameCriteria);
 		query = query.addCriteria(pwdCriteria);
 		query = query.addCriteria(typeCriteria);
 		return mongoOperation.findOne(USERS_COLLECTION_NAME, query, User.class);
 	}
 
 	@Override
 	public ProjectInfo getProjectInfo(String projectInfoId)
 			throws PhrescoException {
 		ProjectInfoDAO projectInfoDAO = mongoOperation.findOne("projectInfo", 
 				new Query(Criteria.whereId().is(projectInfoId)), ProjectInfoDAO.class);
 		Converter<ProjectInfoDAO, ProjectInfo> converter = 
 			(Converter<ProjectInfoDAO, ProjectInfo>) ConvertersFactory.getConverter(ProjectInfoDAO.class);
 		return converter.convertDAOToObject(projectInfoDAO, mongoOperation);
 	}
 
 	@Override
 	public List<VideoInfo> getVideos() throws PhrescoException {
 		List<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
 		List<VideoInfoDAO> videoList = mongoOperation.getCollection(VIDEODAO_COLLECTION_NAME , VideoInfoDAO.class);
 		if (videoList != null) {
 			Converter<VideoInfoDAO, VideoInfo> videoInfoConverter = 
 					(Converter<VideoInfoDAO, VideoInfo>) ConvertersFactory.getConverter(VideoInfoDAO.class);
 			for (VideoInfoDAO videoInfoDAO : videoList) {
 				VideoInfo videoInfo = videoInfoConverter.convertDAOToObject(videoInfoDAO, mongoOperation);
 				videoInfos.add(videoInfo);
 			}
 		}
 		return videoInfos;
 	}
 
 	@Override
 	public String getLatestFrameWorkVersion() throws PhrescoException {
 		Query query = new Query();
 		query.sort().on(DB_COLUMN_CREATIONDATE, Order.DESCENDING);
 		List<VersionInfo> versionInfos = mongoOperation.find("versionInfo", query, VersionInfo.class);
 		return versionInfos.get(0).getFrameworkVersion();
 	}
 
 	@Override
 	public RepoInfo getRepoInfoById(String id) throws PhrescoException {
 		return mongoOperation.findOne(REPOINFO_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), RepoInfo.class);
 	}
 }
