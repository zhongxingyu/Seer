 /*
  * ###
  * Phresco Service
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
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
  * ###
  */
 
 package com.photon.phresco.service.impl;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.ValidatorFactory;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.MongoConfig;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ApplicationTypeDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.model.ServerConfiguration;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ServiceConstants;
 
 public class DbService implements ServiceConstants {
 
 	private static final Logger S_LOGGER = Logger.getLogger(DbService.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private static final String MONGO_TEMPLATE = "mongoTemplate";
 	protected MongoOperations mongoOperation;
 	private ServerConfiguration serverConfig = null;
 	protected DbService() {
 		ApplicationContext ctx = new AnnotationConfigApplicationContext(MongoConfig.class);
     	mongoOperation = (MongoOperations)ctx.getBean(MONGO_TEMPLATE);
     	serverConfig = PhrescoServerFactory.getServerConfig();
 	}
 
 	protected Query createCustomerIdQuery(String customerId) {
 		List<String> customerIds = new ArrayList<String>();
 		customerIds.add(customerId);
 		
 		if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
 			customerIds.add(DEFAULT_CUSTOMER_NAME);
 		}
 
 		Criteria criteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customerIds.toArray());
 		Query query = new Query(criteria);
 
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("query.getQueryObject() " + query.getQueryObject());
 	    }
 	    
 	    return query;
 	}
 	
 	protected List<ArtifactGroup> convertArtifactDAOs(List<ArtifactGroupDAO> artifactGroupDAOs) throws PhrescoException {
 		Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 		for (ArtifactGroupDAO artifactDAO : artifactGroupDAOs) {
 			artifactGroups.add(converter.convertDAOToObject(artifactDAO, mongoOperation));
 		}
 		return artifactGroups;
 	}
 	
 	protected List<DownloadInfo> convertDownloadDAOs(List<DownloadsDAO> downloadsDAOs) throws PhrescoException {
 		List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
 		Converter<DownloadsDAO, DownloadInfo> converter = 
 			(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
 		for (DownloadsDAO downloadsDAO : downloadsDAOs) {
 			infos.add(converter.convertDAOToObject(downloadsDAO, mongoOperation));
 		}
 		return infos;
 	}
 	
 	protected ArtifactGroup convertArtifactDAO(ArtifactGroupDAO artifactGroupDAO) throws PhrescoException {
 		Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		return converter.convertDAOToObject(artifactGroupDAO, mongoOperation);
 	}
 	
 	protected ApplicationInfo convertApplicationDAO(ApplicationInfoDAO applicationInfoDAO) throws PhrescoException {
 		Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
 			(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 		return appConverter.convertDAOToObject(applicationInfoDAO, mongoOperation);
 	}
 	
 	protected long count(String collection, Query query ) {  
         return mongoOperation.executeCommand(
             "{ " +
                 "\"count\" : \"" + collection + "\"," +
                 "\"query\" : " + query.getQueryObject().toString() + 
             " }"  ).getLong( "n" );
     }
 	
 	protected boolean validate(Object object) throws PhrescoException {
 		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
 		javax.validation.Validator validator = factory.getValidator();
 		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
 		if (constraintViolations.isEmpty()) {
 			return true;
 		}
 		for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
 			throw new PhrescoException(constraintViolation.getMessage());
 		}
 		return false;
 	}
 	
 	protected void createArtifact(String collectionName, Object object) {
 		try {
 			if(validate(object)) {
 				mongoOperation.save(collectionName, object);
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e);
 		}
 	}
 	
 	/**
      * Upload binaries using the given artifact info
      * @param archetypeInfo
      * @param artifactFile
      * @param customerId
      * @return
      * @throws PhrescoException
      */
     protected boolean uploadBinary(ArtifactGroup archetypeInfo, File artifactFile) throws PhrescoException {
     	PhrescoServerFactory.initialize();
     	RepositoryManager repositoryManager = PhrescoServerFactory.getRepositoryManager();
         File pomFile = ServerUtil.createPomFile(archetypeInfo);
         
         //Assuming there will be only one version for the artifactGroup
         List<com.photon.phresco.commons.model.ArtifactInfo> versions = archetypeInfo.getVersions();
         com.photon.phresco.commons.model.ArtifactInfo artifactInfo = versions.get(0);
         
 		com.photon.phresco.service.model.ArtifactInfo info = new com.photon.phresco.service.model.ArtifactInfo(archetypeInfo.getGroupId(), 
 				archetypeInfo.getArtifactId(), archetypeInfo.getClassifier(), archetypeInfo.getPackaging(), artifactInfo.getVersion());
 		
         info.setPomFile(pomFile);
         
         List<String> customerIds = archetypeInfo.getCustomerIds();
         String customerId = customerIds.get(0);
         boolean addArtifact = repositoryManager.addArtifact(info, artifactFile, customerId);
         FileUtil.delete(pomFile);
         return addArtifact;
     }
     
     protected Technology getTechnologyById(String techId) throws PhrescoException {
     	TechnologyDAO technologyDAO = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
     			new Query(Criteria.whereId().is(techId)), TechnologyDAO.class);
     	Converter<TechnologyDAO, Technology> technologyConverter = 
 	          (Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
     	return technologyConverter.convertDAOToObject(technologyDAO, mongoOperation);
     }
     
     protected ApplicationTypeDAO getApptypeById(String id) {
     	return mongoOperation.findOne(APPTYPES_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), 
     			ApplicationTypeDAO.class);
     }
     
     protected Object performFindOne(String id, String collectionName) {
     	return mongoOperation.findOne(collectionName, 
     			new Query(Criteria.whereId().is(id)), Object.class);
     }
     
     protected void saveFileToDB(String id, InputStream is) throws PhrescoException {
     	getGridFs().remove(id);
 		GridFSInputFile file = getGridFs().createFile(is);
 		file.setFilename(id);
 		file.save(); 
     }
     
     protected InputStream getFileFromDB(String id) throws PhrescoException {
 		GridFSDBFile imageForOutput = getGridFs().findOne(id);
 		InputStream inputStream = imageForOutput.getInputStream();
 		return inputStream; 
     }
     
 	private GridFS getGridFs() throws PhrescoException {
 
 		try {
 			Mongo mongo = new Mongo(serverConfig.getDbHost(),
 					serverConfig.getDbPort());
 			DB db = mongo.getDB(serverConfig.getDbName());
 			GridFS gfsPhoto = new GridFS(db, "icons");
 			return gfsPhoto;
 		} catch (UnknownHostException e) {
 			throw new PhrescoException(e);
 		} catch (MongoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	protected List<Customer> findCustomersFromDB() {
     	try {
     		List<Customer> customers = new ArrayList<Customer>();
     		List<Customer> customersInDb = mongoOperation.getCollection(CUSTOMERDAO_COLLECTION_NAME, Customer.class);
     		if (CollectionUtils.isNotEmpty(customersInDb)) {
     			for (Customer customer : customersInDb) {
    				List<String> applicableTechnologies = new ArrayList<String>();
     				List<String> customerApplicableTechnologies = customer.getApplicableTechnologies();
 					List<String> fromDB = getApplicableForomDB(customer.getId(), customerApplicableTechnologies);
 					if(CollectionUtils.isNotEmpty(customerApplicableTechnologies)) {
 						applicableTechnologies.addAll(customerApplicableTechnologies);
 					}
 					if(CollectionUtils.isNotEmpty(fromDB)) {
 						applicableTechnologies.addAll(fromDB);
 					}
 					List<Technology> technologies = createTechnology(applicableTechnologies);
 					List<TechnologyGroup> technologyGroups = createTechnologyInfo(technologies);
 					Map<String, List<TechnologyGroup>> appTypeMap = new HashMap<String, List<TechnologyGroup>>();
 					createAppTypeMap(technologyGroups, appTypeMap);
 					List<ApplicationType> applicationTypes = createApplicationTypes(appTypeMap);
 					customer.setApplicableAppTypes(applicationTypes);
 					customers.add(customer);
 				}
     			return customers;
     		}
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMERS_COLLECTION_NAME);
 		}
 		return null;
 	}
 	
 	private List<String> getApplicableForomDB(String customerId, List<String> applicableTechnologies) {
 		List<String> techIds = new ArrayList<String>();
 		List<TechnologyDAO> techs = mongoOperation.find(TECHNOLOGIES_COLLECTION_NAME, 
 				new Query(Criteria.where(DB_COLUMN_CUSTOMERIDS).is(customerId)), TechnologyDAO.class);
 		if(customerId.equals(DEFAULT_CUSTOMER_NAME)) {
 			for (TechnologyDAO tech : techs) {
 				if(!applicableTechnologies.contains(tech.getId())) {
 					techIds.add(tech.getId());
 				}
 			}
 			return techIds;
 		}
 		if(CollectionUtils.isNotEmpty(techs)) {
 			for (TechnologyDAO dao : techs) {
 				techIds.add(dao.getId());
 			}
 		}
 		return techIds;
 	}
 
 	private List<Technology> createTechnology(List<String> applicableTechnologies) throws PhrescoException {
 		List<Technology> technologies = new ArrayList<Technology>();
 		try {
 			for (String techId : applicableTechnologies) {
 				technologies.add(getTechnologyById(techId));
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return technologies;
 	}
 	
 	private List<TechnologyGroup> createTechnologyInfo(List<Technology> technologies) {
 		Map<String, List<TechnologyInfo>> techGroupMap = new HashMap<String, List<TechnologyInfo>>();
 		for (Technology technology : technologies) {
 			createTechGroupMap(technology, techGroupMap);
 		}
 		List<TechnologyGroup> technologyGroups = createTechnologyGroup(techGroupMap);
 		
 		return technologyGroups;
 	}
 	
 	private List<TechnologyGroup> createTechnologyGroup(Map<String, List<TechnologyInfo>> techGroupMap) {
 		Set<String> keySet = techGroupMap.keySet();
 		List<TechnologyGroup> techGroups = new ArrayList<TechnologyGroup>();
 		for (String key : keySet) {
 			List<TechnologyInfo> list = techGroupMap.get(key);
 			TechnologyGroup techGroup = new TechnologyGroup();
 			TechnologyGroup technologyGroup = getTechnologyGroup(key);
 			techGroup.setId(key);
 			techGroup.setName(technologyGroup.getName());
 			techGroup.setAppTypeId(list.get(0).getAppTypeId());
 			techGroup.setTechInfos(list);
 			techGroups.add(techGroup);
 		}
 		
 		return techGroups;
 	}
 	
 	private List<ApplicationType> createApplicationTypes(Map<String, List<TechnologyGroup>> appTypeMap) {
 		Set<String> keySet = appTypeMap.keySet();
 		List<ApplicationType> appTypes = new ArrayList<ApplicationType>();
 		for (String key : keySet) {
 			List<TechnologyGroup> list = appTypeMap.get(key);
 			ApplicationType appType = new ApplicationType();
 			String appTypeId = list.get(0).getAppTypeId();
 			ApplicationTypeDAO apptypeById = getApptypeById(appTypeId);
 			appType.setId(appTypeId);
 			appType.setName(apptypeById.getName());
 			appType.setTechGroups(list);
 			appTypes.add(appType);
 		}
 		
 		return appTypes;
 	}
 	
 	private void createAppTypeMap(List<TechnologyGroup> techGroups, Map<String, List<TechnologyGroup>> appTypeMap) {
 		for (TechnologyGroup technologyGroup : techGroups) {
 			String appTypeId = technologyGroup.getAppTypeId();
 			List<TechnologyGroup> newTechGroups = new ArrayList<TechnologyGroup>();
 			if (appTypeMap.containsKey(appTypeId)) {
 				List<TechnologyGroup> list = appTypeMap.get(appTypeId);
 				list.add(technologyGroup);
 				appTypeMap.put(appTypeId, list);
 			} else {
 				newTechGroups.add(technologyGroup);
 				appTypeMap.put(appTypeId, newTechGroups);
 			}
 		}
 	}
 	
 	private void createTechGroupMap(Technology technology, Map<String, List<TechnologyInfo>> techGroupMap) {
 		TechnologyInfo techInfo = createTechInfo(technology);
 		String techGroupId = technology.getTechGroupId();
 		List<TechnologyInfo> infos = new ArrayList<TechnologyInfo>();
 		if(techGroupMap.containsKey(techGroupId)) {
 			List<TechnologyInfo> list = techGroupMap.get(techGroupId);
 			list.add(techInfo);
 			techGroupMap.put(techGroupId, list);
 		} else {
 			infos.add(techInfo);
 			techGroupMap.put(techGroupId, infos);
 		}
 	}
 
 	private TechnologyInfo createTechInfo(Technology technology) {
 		TechnologyInfo info = new TechnologyInfo();
 		info.setId(technology.getId());
 		info.setName(technology.getName());
 		info.setAppTypeId(technology.getAppTypeId());
 		info.setTechVersions(technology.getTechVersions());
 		return info;
 	}
 	
 	private TechnologyGroup getTechnologyGroup(String id) {
 		return mongoOperation.findOne(TECH_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), 
     			TechnologyGroup.class);
 	}
 	
 }
