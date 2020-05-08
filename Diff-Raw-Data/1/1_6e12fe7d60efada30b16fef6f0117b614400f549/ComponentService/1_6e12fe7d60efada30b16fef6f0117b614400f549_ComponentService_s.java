 /*
  * ###
  * Phresco Service Implemenation
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
 
 package com.photon.phresco.service.rest.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.stereotype.Component;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.PlatformType;
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.client.api.Content;
 import com.photon.phresco.service.client.api.Content.Type;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.impl.DbService;
 import com.photon.phresco.service.model.ArtifactInfo;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ServiceConstants;
 import com.phresco.pom.site.Reports;
 import com.sun.jersey.core.header.ContentDisposition;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.MultiPartMediaTypes;
 
 @Component
 @Path(ServiceConstants.REST_API_COMPONENT)
 public class ComponentService extends DbService {
 	
 	private static final Logger S_LOGGER= Logger.getLogger(ComponentService.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private static RepositoryManager repositoryManager;
 	
 	public ComponentService() throws PhrescoException {
 		super();
 		PhrescoServerFactory.initialize();
 		repositoryManager = PhrescoServerFactory.getRepositoryManager();
     }
 	
 	/**
 	 * Returns the list of apptypes
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@GET
 	@Path (REST_API_APPTYPES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findAppTypes(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findAppTypes()");
 	    }
 
 		try {
 			Query query = createCustomerIdQuery(customerId);
 			List<ApplicationType> applicationTypes = mongoOperation.find(APPTYPES_COLLECTION_NAME, query, ApplicationType.class);
 	        return Response.status(Response.Status.OK).entity(applicationTypes).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of apptypes
 	 * @param appTypes
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_APPTYPES)
 	public Response createAppTypes(List<ApplicationType> appTypes) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.createAppTypes(List<ApplicationType> appTypes)");
         }
 	    
 		try {
 			mongoOperation.insertList(APPTYPES_COLLECTION_NAME , appTypes);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.CREATED).build();
 	}
 	
 	/**
 	 * Updates the list of apptypes
 	 * @param appTypes
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_APPTYPES)
 	public Response updateAppTypes(List<ApplicationType> appTypes) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateAppTypes(List<ApplicationType> appTypes)");
 	    }
 		
 	    try {
 	        for (ApplicationType applicationType : appTypes) {
 	            mongoOperation.save(APPTYPES_COLLECTION_NAME , applicationType);
             }
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
         }
 		
 		return Response.status(Response.Status.OK).entity(appTypes).build();
 	}
 	
 	/**
 	 * Deletes the list of apptypes
 	 * @param appTypes
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_APPTYPES)
 	@Produces (MediaType.TEXT_PLAIN)
 	public void deleteAppTypes(List<ApplicationType> appTypes) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteAppTypes(List<ApplicationType> appTypes)");
         }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the apptype by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
 	public Response getApptype(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getApptype(String id)" + id);
 	    }
 		
 		try {
 			ApplicationType appType = mongoOperation.findOne(APPTYPES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
 			if(appType != null) {
 				return Response.status(Response.Status.OK).entity(appType).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).build();
 	}
 	
 	/**
 	 * Updates the list of apptypes
 	 * @param appTypes
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
 	public Response updateAppType(@PathParam(REST_API_PATH_PARAM_ID) String id , ApplicationType appType) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateAppType(String id , ApplicationType appType)" + id);
 	    }
 		
 	    //TODO:Need to check if it is used.
 		try {
 	        mongoOperation.save(APPTYPES_COLLECTION_NAME, appType);
 		} catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
         }
 		
 		return Response.status(Response.Status.OK).entity(appType).build();
 	}
 	
 	/**
 	 * Deletes the apptype by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_APPTYPES + REST_API_PATH_ID)
 	public Response deleteAppType(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteAppType(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(APPTYPES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of technologies
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_TECHNOLOGIES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findTechnologies(@QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_APPTYPEID) String appTypeId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findTechnologies() " + customerId);
 	    }
 	    
 	    try {
 	        List<TechnologyDAO> techDAOList = new ArrayList<TechnologyDAO>();
 			Query query = createCustomerIdQuery(customerId);
 		   
 			if(StringUtils.isNotEmpty(appTypeId)) {
 			    query.addCriteria(Criteria.where(REST_QUERY_APPTYPEID).is(appTypeId));
 			    techDAOList = mongoOperation.find(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		    } else {
 		        techDAOList = mongoOperation.find(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		    }
 		    
 		    List<Technology> techList = new ArrayList<Technology>(techDAOList.size() * 2);
 			Converter<TechnologyDAO, Technology> technologyConverter = 
 		          (Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
 
 			for (TechnologyDAO technologyDAO : techDAOList) {
 				Technology technology = technologyConverter.convertDAOToObject(technologyDAO, mongoOperation);
 				techList.add(technology);
 			}
 			
 			//if empty send error message
 			if (techList.isEmpty()) {
 				return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();	
 			}
 			
 			return Response.status(Response.Status.OK).entity(techList).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECHNOLOGIES_COLLECTION_NAME);
 		}
     	 
 	}
 	
 	/**
 	 * Creates the list of technologies
 	 * @param technologies
 	 * @throws IOException 
 	 * @throws PhrescoException 
 	 */
 	@POST
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
 	@Path (REST_API_TECHNOLOGIES)
 	public Response createTechnologies(MultiPart multiPart) throws PhrescoException, IOException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.createTechnologies(List<Technology> technologies)");
 	    }
 	   return saveOrUpdateTechnology(multiPart);
 	}
 	
 	private Response saveOrUpdateTechnology(MultiPart multiPart) throws PhrescoException {
 		Map<Technology, List<BodyPart>> map = new HashMap<Technology, List<BodyPart>>(); 
 	    List<BodyPart> techs = new ArrayList<BodyPart>();
 	    List<BodyPart> entities = new ArrayList<BodyPart>();
 	    
 	    //To separete the object and binary file
 	    List<BodyPart> bodyParts = multiPart.getBodyParts();
 	    for (BodyPart bodyPart : bodyParts) {
 	        if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
 	            techs.add(bodyPart);
 	        } else {
 	            entities.add(bodyPart);
 	        }
         }
 	    
 	    //To map the binary file with corresponding object
 	    List<BodyPart> foundBodyPart = null;
 	    for (BodyPart tech : techs) {
 	        foundBodyPart = new ArrayList<BodyPart>();
             for (BodyPart bodyPart : entities) {
                 if (tech.getContentDisposition().getFileName().equals(bodyPart.getContentDisposition().getFileName())) {
                     foundBodyPart.add(bodyPart);
                 }
             }
             
             map.put(tech.getEntityAs(Technology.class), foundBodyPart);
         } 
 	    Response response = null;
 	   //Iterate the content map for upload binaries
        Set<Technology> keySet = map.keySet();
 	   for (Technology technology : keySet) {
 	       List<BodyPart> list = map.get(technology);
 	       response = createBinary(technology, list);
 	   }
 	   return response;
 	}
 
 	private Response createBinary(Technology technology, List<BodyPart> list) throws PhrescoException {
 
 		List<ArtifactGroupDAO> artifactGroups = new ArrayList<ArtifactGroupDAO>();
 		List<com.photon.phresco.commons.model.ArtifactInfo> artifactInfos = new ArrayList<com.photon.phresco.commons.model.ArtifactInfo>();
 		
 		Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
 	            (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		ArtifactGroup artifactinfo = null;
 		for (BodyPart bodyPart : list) {
 			
 			//check if this is archetype
             ContentDisposition disposition = bodyPart.getContentDisposition();
 			Type contentType = Content.Type.valueOf(disposition.getType());
 			
 			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 			artifactinfo = technology.getArchetypeInfo();
 			if (!Content.Type.ARCHETYPE.equals(contentType)) {
                 artifactinfo = ServerUtil.getArtifactinfo(bodyPartEntity.getInputStream());
             }
 			
 			//convert Artifact Group
 			ArtifactGroupDAO artfGrpDAO = artifactConverter.convertObjectToDAO(artifactinfo);
 			artifactGroups.add(artfGrpDAO);
 
             //Assuming there will be only one customer will be provided here for 2.0.0
             //Need to handle multiple customer in future
 			String customerId = technology.getCustomerIds().get(0);
 			artifactinfo.setCustomerIds(technology.getCustomerIds());
 			
 			File appJarFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
             uploadBinary(artifactinfo, appJarFile);
             FileUtil.delete(appJarFile);
         }
 		
 		saveTechnology(technology);
 		saveModuleGroup(artifactinfo);
         return Response.status(Response.Status.OK).entity(technology).build();
     }
 	
 	private void saveTechnology(Technology technology) throws PhrescoException {
     	Converter<TechnologyDAO, Technology> techConverter = 
     		(Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
     	TechnologyDAO tech = techConverter.convertObjectToDAO(technology);
     	TechnologyDAO techDAO = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
     			new Query(Criteria.where(REST_API_NAME).is(tech.getName())), TechnologyDAO.class);
     	if(techDAO == null) {
     		mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME, tech);
     	} else {
     		List<String> pluginIds = techDAO.getPluginIds();
     		if(CollectionUtils.isNotEmpty(pluginIds)) {
     			pluginIds.addAll(tech.getPluginIds());
         		techDAO.setPluginIds(pluginIds);
     		}
     		mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME, techDAO);
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
     private boolean uploadBinary(ArtifactGroup archetypeInfo, File artifactFile) throws PhrescoException {
     	
         File pomFile = ServerUtil.createPomFile(archetypeInfo);
         
         //Assuming there will be only one version for the artifactGroup
         List<com.photon.phresco.commons.model.ArtifactInfo> versions = archetypeInfo.getVersions();
         com.photon.phresco.commons.model.ArtifactInfo artifactInfo = versions.get(0);
         
 		ArtifactInfo info = new ArtifactInfo(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId(), archetypeInfo.getClassifier(), 
                 archetypeInfo.getPackaging(), artifactInfo.getVersion());
 		
         info.setPomFile(pomFile);
         boolean addArtifact = true;
         
         List<String> customerIds = archetypeInfo.getCustomerIds();
         //TODO:Need to upload the content into the repository
 //        boolean addArtifact = repositoryManager.addArtifact(info, artifactFile, customerId);
         FileUtil.delete(pomFile);
         return addArtifact;
     }
     
 
 
     /**
 	 * Updates the list of technologies
 	 * @param technologies
 	 * @return
      * @throws PhrescoException 
 	 */
 	@PUT
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHNOLOGIES)
 	public Response updateTechnologies(MultiPart multipart) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateTechnologies(List<Technology> technologies)");
 	    }
 		
 		return saveOrUpdateTechnology(multipart);
 	}
 	
 	/**
 	 * Deletes the list of technologies
 	 * @param technologies
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_TECHNOLOGIES)
 	public void deleteTechnologies(List<Technology> technologies) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteTechnologies(List<WebService> technologies)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	
 	/**
 	 * Get the technology by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 	public Response getTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getTechnology(String id)" + id);
 	    }
 		
 		try {
 			Technology technology = mongoOperation.findOne(TECHNOLOGIES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Technology.class);
 			if (technology != null) {
 				return Response.status(Response.Status.OK).entity(technology).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECHNOLOGIES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 //	/**
 //	 * Updates the technology given by the parameter
 //	 * @param id
 //	 * @param technology
 //	 * @return
 //	 */
 //	@PUT
 //	@Consumes (MediaType.APPLICATION_JSON)
 //	@Produces (MediaType.APPLICATION_JSON)
 //	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 //	public Response updateTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id , Technology technology) {
 //	    if (isDebugEnabled) {
 //	        S_LOGGER.debug("Entered into ComponentService.getTechnology(String id, Technology technology)" + id);
 //	    }
 //		
 //		try {
 //			if (id.equals(technology.getId())) {
 //				mongoOperation.save(TECHNOLOGIES_COLLECTION_NAME, technology);
 //				return Response.status(Response.Status.OK).entity(technology).build();
 //			} 
 //		} catch (Exception e) {
 //			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 //		}
 //		
 //		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 //	}
 	
 	/**
 	 * Deletes the server by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 	public Response deleteTechnology(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteTechnology(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(TECHNOLOGIES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Technology.class);
 			//TODO:Need to check usage before deletion
 			//TODO:Need to remove dependent objects
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
     
 	/**
 	 * Returns the list of settings
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_SETTINGS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findSettings(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findSettings()" + customerId);
 	    }
 		
 		try {
 			Query query = createCustomerIdQuery(customerId);
 			List<SettingsTemplate> settingsList = mongoOperation.find(SETTINGS_COLLECTION_NAME, query, SettingsTemplate.class);
 			return Response.status(Response.Status.NO_CONTENT).entity(settingsList).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of settings
 	 * @param settings
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_SETTINGS)
 	public Response createSettings(List<SettingsTemplate> settings) {
 		if (isDebugEnabled) {
 		    S_LOGGER.debug("Entered into ComponentService.createSettings(List<SettingsTemplate> settings)");
 		}
 		
 		try {
 			mongoOperation.insertList(SETTINGS_COLLECTION_NAME, settings);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.CREATED).build();
 	}
 	
 	/**
 	 * Updates the list of settings
 	 * @param settings
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_SETTINGS)
 	public Response updateSettings(List<SettingsTemplate> settings) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateSettings(List<SettingsTemplate> settings)");
 	    }
 		
 		try {
 			for (SettingsTemplate settingTemplate : settings) {
 				SettingsTemplate settingTemplateInfo = mongoOperation.findOne(SETTINGS_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(settingTemplate.getId())), SettingsTemplate.class);
 				if (settingTemplateInfo != null) {
 					mongoOperation.save(SETTINGS_COLLECTION_NAME, settingTemplate);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(settings).build();
 	}
 	
 	/**
 	 * Deletes the list of settings
 	 * @param settings
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_SETTINGS)
 	public void deleteSettings(List<SettingsTemplate> settings) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateSettings(List<SettingsTemplate> settings)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 		
 	}
 	
 	/**
 	 * Get the settingTemplate by id 
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
 	public Response getSettingsTemplate(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getSettingsTemplate(String id)" + id);
 	    }
 		
 		try {
 			SettingsTemplate settingTemplate = mongoOperation.findOne(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class); 
 			if (settingTemplate != null) {
 				return Response.status(Response.Status.OK).entity(settingTemplate).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of setting
 	 * @param settings
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
 	public Response updateSetting(@PathParam(REST_API_PATH_PARAM_ID) String id , SettingsTemplate settingsTemplate) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateAppType(String id , SettingsTemplate settingsTemplate)" + id);
 	    }
 		
 		try {
 			if (id.equals(settingsTemplate.getId())) {
 				mongoOperation.save(SETTINGS_COLLECTION_NAME, settingsTemplate);
 				return Response.status(Response.Status.OK).entity(settingsTemplate).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(settingsTemplate).build();
 	}
 	
 	/**
 	 * Deletes the settingsTemplate by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
 	public Response deleteSettingsTemplate(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteSettingsTemplate(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of modules
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_MODULES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findModules(@QueryParam(REST_QUERY_TYPE) String type, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findModules()" + type);
 	    }
 	    
 		try {
 			Query query = createCustomerIdQuery(customerId);
 			Criteria criteria = Criteria.where(DB_COLUMN_ARTIFACT_GROUP_TYPE).is(type);
 			Criteria criteria1 = Criteria.where(DB_COLUMN_APPLIESTOTECHID).is(techId);
 			query = query.addCriteria(criteria);
 			query = query.addCriteria(criteria1);
 			List<ArtifactGroupDAO> artifactGroupDAOs = mongoOperation.find(ARTIFACT_GROUP_COLLECTION_NAME,
 					query, ArtifactGroupDAO.class);
 		    List<ArtifactGroup> modules = convertDAOToModule(artifactGroupDAOs);
 		    return Response.status(Response.Status.OK).entity(modules).build();
 
 		} catch(Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 
 	}
 	
 	private List<ArtifactGroup> convertDAOToModule(List<ArtifactGroupDAO> moduleDAOs) throws PhrescoException {
 		Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 	    List<ArtifactGroup> modules = new ArrayList<ArtifactGroup>();
 	    for (ArtifactGroupDAO artifactGroupDAO : moduleDAOs) {
 			ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGroupDAO, mongoOperation);
 			modules.add(artifactGroup);
 		}
         return modules;
     }
 
     /**
      * Creates the list of modules
      * @param modules
      * @return 
      * @throws PhrescoException 
      */
     @POST
     @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_MODULES)
     public Response createModules(MultiPart moduleInfo) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into ComponentService.createModules(List<ModuleGroup> modules)");
         }
         Response createFeatures = createOrUpdateFeatures(moduleInfo);
         return createFeatures;
     }
     
 	
     private Response createOrUpdateFeatures(MultiPart moduleInfo) throws PhrescoException {
     	ArtifactGroup moduleGroup = null;
         BodyPartEntity bodyPartEntity = null;
         File moduleFile = null;
         List<BodyPart> bodyParts = moduleInfo.getBodyParts();
        
         if (CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                     moduleGroup = bodyPart.getEntityAs(ArtifactGroup.class);
                 } else {
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         
         if (moduleGroup == null) {
         }
         
         if(bodyPartEntity == null & moduleGroup != null) {
         	saveModuleGroup(moduleGroup);
         }
         
         if (bodyPartEntity != null) {
             moduleFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
             boolean uploadBinary = uploadBinary(moduleGroup, moduleFile);
             if (uploadBinary) {
             	saveModuleGroup(moduleGroup);
             }
             FileUtil.delete(moduleFile);
         }
         
         
         	
         	//TODO:Old Logic, need to check with Kumar
         	//check if the module already exist
         	//This query should be done based on artifactId, groupId
 //        	ArtifactGroupDAO foundModuleGroup = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 //        			new Query(Criteria.where("name").is(moduleGroup.getName())), ArtifactGroupDAO.class);
 //        	
 //        	if (foundModuleGroup != null) {
 //        		List<String> versions = foundModuleGroup.getVersionIds();
 //        		versions.add(module.getId());
 //        		foundModuleGroup.setVersions(versions);
 //        		saveModuleGroup(foundModuleGroup);
 //        	} else {
 //        	    saveModuleGroup(moduleGroup);
 //        	}
         	
         
         return Response.status(Response.Status.CREATED).entity(moduleGroup).build();
 	}
 
 	private void saveModuleGroup(ArtifactGroup moduleGroup) throws PhrescoException {
         Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         ArtifactGroupDAO moduleGroupDAO = converter.convertObjectToDAO(moduleGroup);
         
         List<com.photon.phresco.commons.model.ArtifactInfo> moduleGroupVersions = moduleGroup.getVersions();
         List<String> versionIds = new ArrayList<String>();
         
         ArtifactGroupDAO moduleDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 		        new Query(Criteria.where(REST_API_NAME).is(moduleGroupDAO.getName())), ArtifactGroupDAO.class);
         
         com.photon.phresco.commons.model.ArtifactInfo newVersion = moduleGroup.getVersions().get(0);
         if(moduleDAO != null) {
         	moduleGroupDAO.setId(moduleDAO.getId());
         	versionIds.addAll(moduleDAO.getVersionIds());
         	List<com.photon.phresco.commons.model.ArtifactInfo> info = mongoOperation.find(ARTIFACT_INFO_COLLECTION_NAME, 
         			new Query(Criteria.where(DB_COLUMN_ARTIFACT_GROUP_ID).is(moduleDAO.getId())), 
         			com.photon.phresco.commons.model.ArtifactInfo.class);
         	
         	List<com.photon.phresco.commons.model.ArtifactInfo> versions = new ArrayList<com.photon.phresco.commons.model.ArtifactInfo>();
         	newVersion.setArtifactGroupId(moduleDAO.getId());
         	versions.add(newVersion);
         	info.addAll(versions);
         	
         	String id = checkVersionAvailable(info, newVersion.getVersion());
         	if(id == newVersion.getId()) {
         		versionIds.add(newVersion.getId());
         	}
 			newVersion.setId(id);
     		mongoOperation.save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }  else {
         		versionIds.add(newVersion.getId());
         		newVersion.setArtifactGroupId(moduleGroupDAO.getId());
                 mongoOperation.save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }
         moduleGroupDAO.setVersionIds(versionIds);
         mongoOperation.save(ARTIFACT_GROUP_COLLECTION_NAME, moduleGroupDAO);
     }
 
 	private String checkVersionAvailable(List<com.photon.phresco.commons.model.ArtifactInfo> info, String version) {
 		for (com.photon.phresco.commons.model.ArtifactInfo artifactInfo : info) {
 			if(artifactInfo.getVersion().equals(version)) {
 				return artifactInfo.getId();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Updates the list of modules
 	 * @param modules
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@PUT
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_MODULES)
 	public Response updateModules(MultiPart multiPart) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateModules(List<ModuleGroup> modules)");
 	    }
 	    Response updateFeatures = createOrUpdateFeatures(multiPart);
 		return updateFeatures;
 	}
 	
 	/**
 	 * Deletes the list of modules
 	 * @param modules
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_MODULES)
 	public void deleteModules(List<ArtifactGroup> modules) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteModules(List<ModuleGroup> modules)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the module by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_MODULES + REST_API_PATH_ID)
 	public Response getModule(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getModule(String id)" + id);
 	    }
 		
 		try {
 			ArtifactGroupDAO moduleDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ArtifactGroupDAO.class);
 			
 			if (moduleDAO != null) {
 		        Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 		            (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		        ArtifactGroup moduleGroup = converter.convertDAOToObject(moduleDAO, mongoOperation);
 				return  Response.status(Response.Status.OK).entity(moduleGroup).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).build();
 	}
 	
 	/**
 	 * Updates the module given by the parameter
 	 * @param id
 	 * @param module
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_MODULES + REST_API_PATH_ID)
 	public Response updatemodule(@PathParam(REST_API_PATH_PARAM_ID) String id, ArtifactGroup module) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updatemodule(String id , ModuleGroup module)" + id);
 	    }
 		
 		try {
 			if (id.equals(module.getId())) {
 				saveModuleGroup(module);
 				return  Response.status(Response.Status.OK).entity(module).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(module).build();
 	}
 	
 	/**
 	 * Deletes the module by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_MODULES + REST_API_PATH_ID)
 	public Response deleteModules(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteModules(String id)" + id);
 	    }
 		
 		try {
 			ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), 
 					ArtifactGroupDAO.class);
 			if(artifactGroupDAO != null) {
 				mongoOperation.remove(ARTIFACT_GROUP_COLLECTION_NAME, 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ArtifactGroupDAO.class);
 				List<String> versionIds = artifactGroupDAO.getVersionIds();
 				for (String versionId : versionIds) {
 					mongoOperation.remove(ARTIFACT_INFO_COLLECTION_NAME, 
 					        new Query(Criteria.whereId().is(versionId)));
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of pilots
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PILOTS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPilots(@QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findPilots()" + customerId);
 	    }
 		try {
 		    List<ApplicationInfoDAO> appInfos = new ArrayList<ApplicationInfoDAO>();
 			Query query = createCustomerIdQuery(customerId);
 			if (StringUtils.isNotEmpty(techId)) {
                 query.addCriteria(Criteria.where("techInfo.version").is(techId));
                 appInfos = mongoOperation.find(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
             } else {
                 appInfos = mongoOperation.find(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
             }
 			return Response.status(Response.Status.OK).entity(appInfos).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPLICATION_INFO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
      * Creates the list of pilots
      * @param projectInfos
      * @return 
      * @throws PhrescoException 
      */
     @POST
     @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_PILOTS)
     public Response createPilots(MultiPart pilotInfo) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into ComponentService.createPilots(List<ProjectInfo> projectInfos)");
         }
 
         return createOrUpdatePilots(pilotInfo);
     }
     
 	private Response createOrUpdatePilots(MultiPart pilotInfo) throws PhrescoException {
 		ApplicationInfo applicationInfo = null;
         BodyPartEntity bodyPartEntity = null;
         File pilotFile = null;
         
         List<BodyPart> bodyParts = pilotInfo.getBodyParts();
         if(CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                     applicationInfo = bodyPart.getEntityAs(ApplicationInfo.class);
                 } else {
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         
         if(bodyPartEntity != null) {
             pilotFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
             saveApplicationInfo(applicationInfo);
             FileUtil.delete(pilotFile);
         }
         
         if(bodyPartEntity == null && applicationInfo != null) {
         	saveApplicationInfo(applicationInfo);
         }
         //TODO:Need to handle uploading of Binaries into repository
 //        boolean uploadBinary = uploadBinary(projectInfo.getArchetypeInfo(), 
 //                pilotFile, projectInfo.getCustomerId());
 //        if(uploadBinary) {
 ////            projectInfo.setProjectURL(createContentURL(projectInfo.getArchetypeInfo()));
             
 //        }
         return Response.status(Response.Status.CREATED).entity(applicationInfo).build();
 	}
 	
 	private void saveApplicationInfo(ApplicationInfo applicationInfo) throws PhrescoException {
 		ApplicationInfoDAO applicationInfoDAOs = mongoOperation.findOne(APPLICATION_INFO_COLLECTION_NAME, 
 				new Query(Criteria.where(REST_API_NAME).is(applicationInfo.getName())), ApplicationInfoDAO.class);
 		if(applicationInfoDAOs == null){
 			Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
 			(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 			ApplicationInfoDAO applicationInfoDAO = appConverter.convertObjectToDAO(applicationInfo);
 			mongoOperation.save(APPLICATION_INFO_COLLECTION_NAME, applicationInfoDAO);
 			ArtifactGroup pilotContent = applicationInfo.getPilotContent();
 			saveModuleGroup(pilotContent);
 		}else{
 			ArtifactGroup pilotContent = applicationInfo.getPilotContent();
 			saveModuleGroup(pilotContent);
 		}
 	}
 	
 	/**
 	 * Updates the list of pilots
 	 * @param projectInfos
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@PUT
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
     @Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PILOTS)
 	public Response updatePilots(MultiPart pilotInfo) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updatePilots(List<ProjectInfo> pilots)");
 	    }
 	    
 		return createOrUpdatePilots(pilotInfo);
 	}
 	
 	/**
 	 * Deletes the list of pilots
 	 * @param pilots
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_PILOTS)
 	public void deletePilots(List<ApplicationInfo> pilots) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deletePilots(List<ProjectInfo> pilots)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the pilot by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PILOTS + REST_API_PATH_ID)
 	public Response getPilot(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getPilot(String id)" + id);
 	    }
 		
 		try {
 			ApplicationInfo appInfo = mongoOperation.findOne(APPLICATION_INFO_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationInfo.class);
 			if (appInfo != null) {
 				return Response.status(Response.Status.OK).entity(appInfo).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPLICATION_INFO_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the pilot given by the parameter
 	 * @param id
 	 * @param pilot
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PILOTS + REST_API_PATH_ID)
 	public Response updatePilot(@PathParam(REST_API_PATH_PARAM_ID) String id , ApplicationInfo pilot) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updatePilot(String id, ProjectInfo pilot)" + id); 
 	    }
 		
 		try {
 			if (id.equals(pilot.getId())) {
 				mongoOperation.save(APPLICATION_INFO_COLLECTION_NAME, pilot);
 				return  Response.status(Response.Status.OK).entity(pilot).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(pilot).build();
 	}
 	
 	/**
 	 * Deletes the pilot by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_PILOTS + REST_API_PATH_ID)
 	public Response deletePilot(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deletePilot(String id)" + id);
 	    }
 		
 		try {
 			ApplicationInfoDAO findOne = mongoOperation.findOne(APPLICATION_INFO_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationInfoDAO.class);
 	        String artifactGroupId = findOne.getArtifactGroupId();
 	        ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 	        		new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
 	        List<String> versionIds = artifactGroupDAO.getVersionIds();
 	        mongoOperation.remove(ARTIFACT_INFO_COLLECTION_NAME, 
 	        		new Query(Criteria.where(REST_API_PATH_PARAM_ID).in(versionIds.toArray())),
 	        		com.photon.phresco.commons.model.ArtifactInfo.class);
 	        mongoOperation.remove(ARTIFACT_GROUP_COLLECTION_NAME, 
 	        		new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
 	        mongoOperation.remove(APPLICATION_INFO_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationInfoDAO.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 
 	/**
 	 * Returns the list of webservices
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_WEBSERVICES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findWebServices(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findWebServices()");
 	    }
 		
 		List<WebService> webServiceList = new ArrayList<WebService>();
 		try {
 			if (!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
 				webServiceList= mongoOperation.find(WEBSERVICES_COLLECTION_NAME, 
 				        new Query(Criteria.where (REST_QUERY_CUSTOMERID).is(customerId)), WebService.class);
 			}
 			webServiceList.addAll(mongoOperation.find(WEBSERVICES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_QUERY_CUSTOMERID)
 			        .is(DEFAULT_CUSTOMER_NAME)), WebService.class));
 			
 			return  Response.status(Response.Status.OK).entity(webServiceList).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of webservices
 	 * @param webServices
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_WEBSERVICES)
 	public Response createWebServices(List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.createWebServices(List<WebService> webServices)");
 	    }
 		
 		try {
 			mongoOperation.insertList(WEBSERVICES_COLLECTION_NAME , webServices);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of webservices
 	 * @param webServices
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_WEBSERVICES)
 	public Response updateWebServices(List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateWebServices(List<WebService> webServices)");
 	    }
 		
 		try {
 			for (WebService webService : webServices) {
 				WebService webServiceInfo = mongoOperation.findOne(WEBSERVICES_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(webService.getId())), WebService.class);
 				if (webServiceInfo != null) {
 					mongoOperation.save(WEBSERVICES_COLLECTION_NAME , webService);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(webServices).build();
 	}
 	
 	/**
 	 * Deletes the list of webservices
 	 * @param webServices
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_WEBSERVICES)
 	public void deleteWebServices(List<WebService> webServices) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteWebServices(List<WebService> webServices)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the database by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
 	public Response getWebService(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getWebService(String id)" + id);
 	    }
 		
 		try {
 			WebService webService = mongoOperation.findOne(WEBSERVICES_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 			if (webService != null) {
 				return Response.status(Response.Status.OK).entity(webService).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the database given by the parameter
 	 * @param id
 	 * @param webService
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
 	public Response updateWebService(@PathParam(REST_API_PATH_PARAM_ID) String id , WebService webService) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateWebService(String id, WebService webService)" + id);
 	    }
 		
 		try {
 			if (id.equals(webService.getId())) {
 				mongoOperation.save(WEBSERVICES_COLLECTION_NAME, webService);
 				return Response.status(Response.Status.OK).entity(webService).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 	}
 	
 	/**
 	 * Deletes the server by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_WEBSERVICES + REST_API_PATH_ID)
 	public Response deleteWebService(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteWebService(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(WEBSERVICES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 
 	/**
      * Returns the list of downloadInfo
      * @return
      */
     @GET
     @Path (REST_API_DOWNLOADS)
     @Produces (MediaType.APPLICATION_JSON)
     public Response findDownloadInfo(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.findDownloadInfo()");
         }
         List<DownloadInfo> downloads = new ArrayList<DownloadInfo>();
         try {
         	Query query = createCustomerIdQuery(customerId);
         	List<DownloadsDAO> downloadList = mongoOperation.find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
             if (downloadList != null) {
             	Converter<DownloadsDAO, DownloadInfo> downloadConverter = 
             		(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
             	for (DownloadsDAO downloadsDAO : downloadList) {
 					DownloadInfo downloadInfo = downloadConverter.convertDAOToObject(downloadsDAO, mongoOperation);
 					downloads.add(downloadInfo);
 				}
                 return Response.status(Response.Status.OK).entity(downloads).build();
             } 
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00006, DOWNLOAD_COLLECTION_NAME);
         }
         
         return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
     }
     
 
     /**
      * Creates the list of downloads
      * @param downloads
      * @return 
      * @throws PhrescoException 
      */
     @POST
     @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_DOWNLOADS)
     public Response createDownloads(MultiPart downloadPart) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into ComponentService.createModules(List<ModuleGroup> modules)");
         }
         
         return createOrUpdateDownloads(downloadPart);
     }
     
     private Response createOrUpdateDownloads(MultiPart downloadPart) throws PhrescoException {
     	DownloadInfo downloadInfo = null;
         BodyPartEntity bodyPartEntity = null;
         File downloadFile = null;
         
         List<BodyPart> bodyParts = downloadPart.getBodyParts();
         if(CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                     downloadInfo = new DownloadInfo();
                     downloadInfo = bodyPart.getEntityAs(DownloadInfo.class);
                 } else {
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         
         if(bodyPartEntity != null) {
             downloadFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
             boolean uploadBinary = uploadBinary(downloadInfo.getArtifactGroup(), downloadFile);
             if(uploadBinary) {
                 saveDownloads(downloadInfo);
             }
             saveDownloads(downloadInfo);
             FileUtil.delete(downloadFile);
         }
         
         if(bodyPartEntity == null && downloadInfo != null) {
         	saveDownloads(downloadInfo);
         }
         
         return Response.status(Response.Status.CREATED).entity(downloadInfo).build();
 		
 	}
 
 	private void saveDownloads(DownloadInfo info) throws PhrescoException {
 		DownloadsDAO downloadsDAO = mongoOperation.findOne(DOWNLOAD_COLLECTION_NAME, 
 				new Query(Criteria.where(REST_API_NAME).is(info.getName())), DownloadsDAO.class);
 		if(downloadsDAO == null) {
 			Converter<DownloadsDAO, DownloadInfo> downlodConverter = 
 					(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
 				DownloadsDAO downloadDAO = downlodConverter.convertObjectToDAO(info);
 				ArtifactGroup artifactGroup = info.getArtifactGroup();
 				saveModuleGroup(artifactGroup);
 				mongoOperation.save(DOWNLOAD_COLLECTION_NAME, downloadDAO);
 		} else {
 			saveModuleGroup(info.getArtifactGroup());
 		}
 		
 		
 	}
     
     /**
      * Updates the list of downloadInfos
      * @param downloads
      * @return
      * @throws PhrescoException 
      */
     @PUT
     @Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_DOWNLOADS)
     public Response updateDownloadInfo(MultiPart multiPart) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateDownloadInfo(List<DownloadInfo> downloads)");
         }
         
         return createOrUpdateDownloads(multiPart);
     }
 
     /**
      * Deletes the list of DownloadInfo
      * @param downloadInfos
      * @throws PhrescoException 
      */
     @DELETE
     @Path (REST_API_DOWNLOADS)
     public void deleteDownloadInfo(List<DownloadInfo> downloadInfos) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteDownloadInfo(List<DownloadInfo> downloadInfos)");
         }
         
         PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
         S_LOGGER.error("PhrescoException Is"  + phrescoException.getErrorMessage());
         throw phrescoException;
     }
 
     /**
      * Get the downloadInfo by id for the given parameter
      * @param id
      * @return
      */
     @GET
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
     public Response getDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getDownloadInfo(String id)" + id);
         }
         
         try {
             DownloadInfo downloadInfo = mongoOperation.findOne(DOWNLOAD_COLLECTION_NAME, 
                     new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), DownloadInfo.class);
             if (downloadInfo != null) {
                 return Response.status(Response.Status.OK).entity(downloadInfo).build();
             } 
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00005, DOWNLOAD_COLLECTION_NAME);
         }
         
         return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
     }
     
     /**
      * Updates the list of downloadInfo by id
      * @param downloadInfos
      * @return
      */
     @PUT
     @Consumes (MediaType.APPLICATION_JSON)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
     public Response updateDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id , DownloadInfo downloadInfo) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateDownloadInfo(String id , DownloadInfo downloadInfos)" + id);
         }
         
         try {
             if (id.equals(downloadInfo.getId())) {
                 mongoOperation.save(DOWNLOAD_COLLECTION_NAME, downloadInfo);
                 return Response.status(Response.Status.OK).entity(downloadInfo).build();
             } 
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
         }
         
         return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
     }
     
     /**
      * Deletes the user by id for the given parameter
      * @param id
      * @return 
      */
     @DELETE
     @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
     public Response deleteDownloadInfo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteDownloadInfo(String id)" + id);
         }
         try {
         DownloadsDAO findOne = mongoOperation.findOne(DOWNLOAD_COLLECTION_NAME, 
         		new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), DownloadsDAO.class);
         String artifactGroupId = findOne.getArtifactGroupId();
         ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
         		new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
         List<String> versionIds = artifactGroupDAO.getVersionIds();
         mongoOperation.remove(ARTIFACT_INFO_COLLECTION_NAME, 
         		new Query(Criteria.where(REST_API_PATH_PARAM_ID).in(versionIds.toArray())), 
         		com.photon.phresco.commons.model.ArtifactInfo.class);
         mongoOperation.remove(ARTIFACT_GROUP_COLLECTION_NAME, 
         		new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
         mongoOperation.remove(DOWNLOAD_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), DownloadInfo.class);
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
         }
         
         return Response.status(Response.Status.OK).build();
     }
     
     /**
 	 * Returns the list of platforms available
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PLATFORMS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPlatforms() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findPlatforms()");
 	    }
 		
 		try {
 			List<PlatformType> platformList = mongoOperation.getCollection(PLATFORMS_COLLECTION_NAME, PlatformType.class);
 			return Response.status(Response.Status.NO_CONTENT).entity(platformList).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of Reports
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_REPORTS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findReports(@QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findReports(String techId)");
 	    }
 		try {
 			List<Reports> reports = mongoOperation.find(REPORTS_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_QUERY_TECHID).is(techId)), Reports.class);
 			return  Response.status(Response.Status.OK).entity(reports).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Reports
 	 * @param reports
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_REPORTS)
 	public Response createReports(List<Reports> reports) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.createReports(List<Reports> reports)");
 	    }
 		
 		try {
 			mongoOperation.insertList(REPORTS_COLLECTION_NAME , reports);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of Reports
 	 * @param reports
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_REPORTS)
 	public Response updateReports(List<Reports> reports) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateReports(List<Reports> reports)");
 	    }
 		
 		try {
 			for (Reports report : reports) {
 				mongoOperation.save(REPORTS_COLLECTION_NAME, report);
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(reports).build();
 	}
 	
 	/**
 	 * Deletes the list of Reports
 	 * @param webServices
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_REPORTS)
 	public void deleteReports(List<Reports> reports) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteReports(List<Reports> reports)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the Reports by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_REPORTS + REST_API_PATH_ID)
 	public Response getReports(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getReports(String id)" + id);
 	    }
 		
 		try {
 			Reports reports = mongoOperation.findOne(REPORTS_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Reports.class);
 			if(reports != null) {
 				return Response.status(Response.Status.OK).entity(reports).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the reports given by the parameter
 	 * @param id
 	 * @param reports
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_REPORTS + REST_API_PATH_ID)
 	public Response updateReports(@PathParam(REST_API_PATH_PARAM_ID) String id , Reports reports) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateReports(String id, Reports reports)" + id);
 	    }
 		
 		try {
 				mongoOperation.save(REPORTS_COLLECTION_NAME, reports);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(reports).build();
 	}
 	
 	/**
 	 * Deletes the Reports by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_REPORTS + REST_API_PATH_ID)
 	public Response deleteReports(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteReportse(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(REPORTS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Reports.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Properties
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PROPERTY)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findProperties() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findProperties()");
 	    }
 		try {
 			List<Property> properties = mongoOperation.getCollection(PROPERTIES_COLLECTION_NAME, Property.class);
 			return  Response.status(Response.Status.OK).entity(properties).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Properties
 	 * @param properties
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PROPERTY)
 	public Response createProperties(List<Property> properties) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.createProperties(List<Property> properties)");
 	    }
 		
 		try {
 			mongoOperation.insertList(PROPERTIES_COLLECTION_NAME , properties);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of Properties
 	 * @param reports
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PROPERTY)
 	public Response updateProperties(List<Property> properties) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateProperties(List<Property> properties)");
 	    }
 		
 		try {
 			for (Property property : properties) {
 				mongoOperation.save(PROPERTIES_COLLECTION_NAME, property);
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(properties).build();
 	}
 	
 	/**
 	 * Deletes the list of Properties
 	 * @param properties
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_PROPERTY)
 	public void deleteProperties(List<Property> properties) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteProperties(List<Property> properties)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the Property by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PROPERTY + REST_API_PATH_ID)
 	public Response getProperty(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.getProperty(String id)" + id);
 	    }
 		
 		try {
 			Property property = mongoOperation.findOne(PROPERTIES_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Property.class);
 			if(property != null) {
 				return Response.status(Response.Status.OK).entity(property).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the property given by the parameter
 	 * @param id
 	 * @param property
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PROPERTY + REST_API_PATH_ID)
 	public Response updateProperty(@PathParam(REST_API_PATH_PARAM_ID) String id , Property property) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.updateProperty(String id, Property property)" + id);
 	    }
 		
 		try {
 			mongoOperation.save(PROPERTIES_COLLECTION_NAME, property);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(property).build();
 	}
 	
 	/**
 	 * Deletes the Property by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_PROPERTY + REST_API_PATH_ID)
 	public Response deleteProperty(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.deleteProperty(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(PROPERTIES_COLLECTION_NAME, 
 			        new Query(Criteria.whereId().is(id)), Property.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Technologyoptions
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_OPTIONS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findOptions() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into ComponentService.findOptions()");
 	    }
 		try {
 			List<TechnologyOptions> techOptions = mongoOperation.getCollection(OPTIONS_COLLECTION_NAME, TechnologyOptions.class);
 			return  Response.status(Response.Status.OK).entity(techOptions).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 	}
 }
