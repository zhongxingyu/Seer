 /**
  * Service Web Archive
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
 package com.photon.phresco.service.rest.api;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.stereotype.Component;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactElement;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.FunctionalFramework;
 import com.photon.phresco.commons.model.FunctionalFrameworkProperties;
 import com.photon.phresco.commons.model.License;
 import com.photon.phresco.commons.model.PlatformType;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.client.api.Content.Type;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ApplicationTypeDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.CustomerDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.ProjectInfoDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.impl.DbService;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ServiceConstants;
 import com.phresco.pom.site.Reports;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.MultiPartMediaTypes;
 
 @Component
 @Path(ServiceConstants.REST_API_COMPONENT)
 public class ComponentService extends DbService {
 	
 	private static final SplunkLogger LOGGER = SplunkLogger.getSplunkLogger(LoginService.class.getName());
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	private static RepositoryManager repositoryManager;
 	
 	private static String exceptionString ="PhrescoException Is";
 	
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
 	public Response findAppTypes(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) 
 		throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findAppTypes : Entry");
 	        LOGGER.debug("ComponentService.findAppTypes ", "remoteAddress=" + request.getRemoteAddr() , "customer" + getCustomerNameById(customerId),
 	        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"));
 	    }
 		try {
 			List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
 			List<ApplicationType> applicationTypeDAOs = DbService.getMongoOperation().getCollection(APPTYPES_COLLECTION_NAME, ApplicationType.class);
 			
 			for (ApplicationType applicationType : applicationTypeDAOs) {
 				List<TechnologyGroup> techGroupss = new ArrayList<TechnologyGroup>();
 				Query query = createCustomerIdQuery(customerId);
 				Criteria appCriteria = Criteria.where("appTypeId").is(applicationType.getId());
 				query.addCriteria(appCriteria);
 				List<TechnologyGroup> techGroups = DbService.getMongoOperation().find(TECH_GROUP_COLLECTION_NAME, query, TechnologyGroup.class);
				System.out.println("List of TechGroups===>" +techGroups);
 				for (TechnologyGroup techGroup : techGroups) {
 					query = createCustomerIdQuery(customerId);
 					Criteria criteria = Criteria.where("techGroupId").is(techGroup.getId());
 					query.addCriteria(criteria);
 					List<TechnologyInfo> techInfos = DbService.getMongoOperation().find("techInfos", query, TechnologyInfo.class);
 					techGroup.setTechInfos(techInfos);
 					techGroupss.add(techGroup);
 				}
 				applicationType.setTechGroups(techGroupss);
 				applicationTypes.add(applicationType);
 			}
 			if(CollectionUtils.isEmpty(applicationTypes)) {
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.findAppTypes", "status=\"Not Found\"", "remoteAddress=" + request.getRemoteAddr() , 
 			        		"customer" + getCustomerNameById(customerId), "endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"));
 			    }
 				return Response.status(Response.Status.NO_CONTENT).build();
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findAppTypes : Exit");
 		    }
 	        return Response.status(Response.Status.OK).entity(applicationTypes).build();
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.findAppTypes", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
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
 	public Response createAppTypes(@Context HttpServletRequest request, List<ApplicationType> appTypes) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createAppTypes : Entry");
 	        LOGGER.debug("ComponentService.createAppTypes" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() + 
 	        		"user=" + request.getParameter("userId") , "appType=" + appTypes.get(0).getId());
         }
 		try {
 			for (ApplicationType applicationType : appTypes) {
 				if(validate(applicationType)) {
 					DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , applicationType);
 				}
 			}
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.createAppTypes" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , 
 		        		"endpoint=" + request.getRequestURI(), "user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 	        }
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createAppTypes : Exit");
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
 	public Response updateAppTypes(@Context HttpServletRequest request, List<ApplicationType> appTypes) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppTypes : Entry");
 	        LOGGER.debug("ComponentService.updateAppTypes" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "appType=" + appTypes.get(0).getId());
 	    }
 		
 	    try {
 	        for (ApplicationType applicationType : appTypes) {
 	            DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , applicationType);
             }
         } catch (Exception e) {
         	LOGGER.error("ComponentService.createAppTypes" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
             throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
         }
         if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppTypes : Exit");
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
 	public void deleteAppTypes(@Context HttpServletRequest request, List<ApplicationType> appTypes) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppTypes : Entry");
 	        LOGGER.debug("ComponentService.deleteAppTypes" , "remoteAddress=" + request.getRemoteAddr() ,"endpoint=" + request.getRequestURI() , 
     		"user=" + request.getParameter("userId"));
         }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error("ComponentService.deleteAppTypes" , "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
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
 	public Response getApptype(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getApptype : Entry");
 	        LOGGER.debug("ComponentService.getApptype", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			ApplicationType appType = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
 			if(appType != null) {
 				return Response.status(Response.Status.OK).entity(appType).build();
 			} else {
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.getApptype", "status=\"Bad Request\"" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "id=" + id);
 			    }
 			}
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.getApptype" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getApptype : Exit");
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
 	public Response updateAppType(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , ApplicationType appType) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Entry");
 	        LOGGER.debug("ComponentService.updateAppType" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 	        DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME, appType);
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
         }
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Exit");
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
 	public Response deleteAppType(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppType : Entry");
 	        LOGGER.debug("ComponentService.deleteAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		try {
 			ApplicationTypeDAO appType = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(id)), ApplicationTypeDAO.class);
 			if(appType != null) {
 				List<String> techGroups = appType.getTechGroupIds();
 				if(CollectionUtils.isNotEmpty(techGroups)) {
 					for (String techGroupId : techGroups) {
 						deleteTechGroup(techGroupId);
 					}
 				}
 				DbService.getMongoOperation().remove(APPTYPES_COLLECTION_NAME, 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationTypeDAO.class);
 			} else {
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.deleteAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Bad Request\"", "id=" + id);
 			    }
 			}
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId") ,"status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppType : Exit");
 	    }
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	private void deleteTechGroup(String id) {
 		TechnologyGroup techGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 		List<TechnologyInfo> techInfos = techGroup.getTechInfos();
 		for (TechnologyInfo technologyInfo : techInfos) {
 			deleteTechnologyObject(technologyInfo.getId());
 		}
 		DbService.getMongoOperation().remove(TECH_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 	}
 	
 	private void deleteTechnologyObject(String id) {
 		Query query = new Query(Criteria.whereId().is(id));
 		TechnologyDAO technologyDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		if(isDebugEnabled) {
 			LOGGER.warn("ComponentService.deleteTechnologyObject", "id=" + id , "status=\"Bad Request\"", 
 					"message=" + "Technology Not Found");
 		}
 		if(technologyDAO != null) {
 			String archetypeGroupDAOId = technologyDAO.getArchetypeGroupDAOId();
 			deleteAttifact(archetypeGroupDAOId);
 			List<String> pluginIds = technologyDAO.getPluginIds();
 			if (CollectionUtils.isNotEmpty(pluginIds)) {
 				for (String pluginId : pluginIds) {
 					deleteAttifact(pluginId);
 				}
 			}
 			DbService.getMongoOperation().remove(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		}
 	}
 	
 	/**
 	 * Returns the list of technologies
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_TECHNOLOGIES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findTechnologies(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
 			@QueryParam(REST_QUERY_APPTYPEID) String appTypeId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findTechnologies : Entry");
 	        LOGGER.debug("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId), "appTypeId=" + appTypeId);
 	    }
 	    List<TechnologyDAO> techDAOList = new ArrayList<TechnologyDAO>();
 	    try {
 			Query query = createCustomerIdQuery(customerId);
 		   
 			if(StringUtils.isNotEmpty(appTypeId)) {
 			    query.addCriteria(Criteria.where(REST_QUERY_APPTYPEID).is(appTypeId));
 		    } 
 			
 			techDAOList = DbService.getMongoOperation().find(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 			if(StringUtils.isEmpty(appTypeId) && !StringUtils.equals(customerId, DEFAULT_CUSTOMER_NAME)) {
 				CustomerDAO customerDAO = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
 				List<String> applicableTechnologies = customerDAO.getApplicableTechnologies();
 				if(CollectionUtils.isNotEmpty(applicableTechnologies)) {
 					List<TechnologyDAO> customerTechs = DbService.getMongoOperation().find(TECHNOLOGIES_COLLECTION_NAME, 
 							new Query(Criteria.whereId().in(applicableTechnologies.toArray())), TechnologyDAO.class);
 					techDAOList.addAll(customerTechs);
 				}
 			}
 			
 		    List<Technology> techList = new ArrayList<Technology>();
 			Converter<TechnologyDAO, Technology> technologyConverter = 
 		          (Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
 
 			for (TechnologyDAO technologyDAO : techDAOList) {
 				Technology technology = technologyConverter.convertDAOToObject(technologyDAO, DbService.getMongoOperation());
 				techList.add(technology);
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 		    }
 			if (CollectionUtils.isEmpty(techList)) {
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , 
 			        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"), "status=\"Not Found\"");
 			    }
 				return Response.status(Response.Status.NO_CONTENT).build();	
 			}
 			
 			ResponseBuilder response = Response.status(Response.Status.OK);
 			
 			response.header(Constants.ARTIFACT_COUNT_RESULT, count(TECHNOLOGIES_COLLECTION_NAME, query));
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies : Exit");
 		    }
 			return response.entity(techList).build();
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies ", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
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
 	public Response createTechnologies(@Context HttpServletRequest request, MultiPart multiPart) throws PhrescoException, IOException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createTechnologies");
 	        LOGGER.debug("ComponentService.createTechnologies" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	   return saveOrUpdateTechnology(multiPart);
 	}
 	
 	private Response saveOrUpdateTechnology(MultiPart multiPart) throws PhrescoException {
 		Technology technology = null;
 		List<BodyPart> entities = new ArrayList<BodyPart>();
 		Map<ArtifactGroup, BodyPart> archetypeMap = new HashMap<ArtifactGroup, BodyPart>();
 		Map<ArtifactGroup, BodyPart> pluginMap = new HashMap<ArtifactGroup, BodyPart>();
 
 		// To separete the object and binary file
 		List<BodyPart> bodyParts = multiPart.getBodyParts();
 		for (BodyPart bodyPart : bodyParts) {
 			if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
 				technology = bodyPart.getEntityAs(Technology.class);
 			} else {
 				entities.add(bodyPart);
 			}
 		}
 
 		if (technology == null) {
 			if(isDebugEnabled) {
 				LOGGER.debug("ComponentService.saveOrUpdateTechnology", "status=\"Bad Request\"" ,"message=" + "Technology Is Null");
 			}
 			return null;
 		}
 		if(isDebugEnabled) {
 			LOGGER.debug("ComponentService.saveOrUpdateTechnology", "customer" + getCustomerNameById(technology.getCustomerIds().get(0)), 
 					"techId=" + technology.getId());
 		}
 		for (BodyPart bodyPart : entities) {
 			if (bodyPart.getContentDisposition().getFileName()
 					.equals(technology.getName())) {
 				archetypeMap.put(technology.getArchetypeInfo(), bodyPart);
 			} else {
 				List<ArtifactGroup> plugins = technology.getPlugins();
 				for (ArtifactGroup artifactGroup : plugins) {
 					if (artifactGroup.getName().equals(
 							bodyPart.getContentDisposition().getFileName())) {
 						pluginMap.put(artifactGroup, bodyPart);
 					}
 				}
 			}
 		}
 
 		Set<ArtifactGroup> archetypeSet = archetypeMap.keySet();
 		for (ArtifactGroup artifactGroup : archetypeSet) {
 			createArtifacts(artifactGroup, archetypeMap.get(artifactGroup));
 		}
 
 		Set<ArtifactGroup> pluginSet = pluginMap.keySet();
 		for (ArtifactGroup artifactGroup : pluginSet) {
 			createArtifacts(artifactGroup, pluginMap.get(artifactGroup));
 		}
 		saveTechnology(technology);
 		if(isDebugEnabled) {
 			LOGGER.debug("ComponentService.saveOrUpdateTechnology : Exit");
 		} 
 	   return Response.status(Response.Status.OK).entity(technology).build();
 	}
 
 	private void createArtifacts(ArtifactGroup artifactGroup, BodyPart bodyPart) throws PhrescoException {
 		BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 		File artifactFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
 				artifactGroup.getPackaging(), artifactGroup.getName());
 		if(!artifactFile.exists()) {
 			if(isDebugEnabled) {
 				LOGGER.info("ComponentService.createArtifacts", "customer" + getCustomerNameById(artifactGroup.getCustomerIds().get(0)), 
 						"artifactId=" + artifactGroup.getId() + "message=" + "Artifact File Not Found");
 			}
 		}
 		uploadBinary(artifactGroup, artifactFile);
 	}
 
 	private void saveTechnology(Technology technology) throws PhrescoException {
 		if(!validate(technology)) {
 			return;
 		}
     	Converter<TechnologyDAO, Technology> techConverter = 
     		(Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
     	TechnologyDAO technologyDAO = techConverter.convertObjectToDAO(technology);
     	String archetypeId = createArchetypeId(technology);
     	technologyDAO.setArchetypeGroupDAOId(archetypeId);
     	ArtifactGroup archetypeInfo = technology.getArchetypeInfo();
     	archetypeInfo.setId(archetypeId);
     	saveModuleGroup(archetypeInfo);
     	if(CollectionUtils.isNotEmpty(technology.getPlugins())) {
     		List<String> pluginIds = createPluginIds(technology);
         	technologyDAO.setPluginIds(pluginIds);
         	for (ArtifactGroup plugin : technology.getPlugins()) {
         		ArtifactGroupDAO agDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
         				new Query(Criteria.where("artifactId").is(plugin.getArtifactId())), ArtifactGroupDAO.class);
         		if(agDAO != null) {
         			plugin.setId(agDAO.getId());
         		}
 				saveModuleGroup(plugin);
 			}
     	}
     	
     	addTechnologyToGroup(technologyDAO);
     	DbService.getMongoOperation().save(TECHNOLOGIES_COLLECTION_NAME, technologyDAO);
     	DbService.getMongoOperation().save("techInfos", createTechInfo(technologyDAO));
 	}
 	
 	private TechnologyInfo createTechInfo(TechnologyDAO dao) {
 		TechnologyInfo info = new TechnologyInfo();
 		info.setId(dao.getId());
 		info.setAppTypeId(dao.getAppTypeId());
 		info.setName(dao.getName());
 		info.setCustomerIds(dao.getCustomerIds());
 		info.setTechGroupId(dao.getTechGroupId());
 		return info;
 	}
 	
 	private void addTechnologyToGroup(TechnologyDAO technologyDAO) {
 		TechnologyGroup technologyGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technologyDAO.getTechGroupId())), TechnologyGroup.class);
 		if(technologyGroup != null) {
 			List<TechnologyInfo> foundTechInfos = technologyGroup.getTechInfos();
 			TechnologyInfo info = new TechnologyInfo();
 			info.setId(technologyDAO.getId());
 			info.setAppTypeId(technologyDAO.getAppTypeId());
 			info.setName(technologyDAO.getName());
 			info.setCreationDate(technologyDAO.getCreationDate());			
 			if(CollectionUtils.isEmpty(foundTechInfos)) {
 				foundTechInfos = new ArrayList<TechnologyInfo>();
 				foundTechInfos.add(info);
 			} else {
 				foundTechInfos.add(info);    
 			}
 			technologyGroup.setTechInfos(foundTechInfos);
 			DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, technologyGroup);
 		}
 	}
 
 	private List<String> createPluginIds(Technology technology) {
 		List<String> pluginIds = new ArrayList<String>();
 		TechnologyDAO techDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technology.getId())), TechnologyDAO.class);
 		if(techDAO == null) {
 			for (ArtifactGroup plugin : technology.getPlugins()) {
 				pluginIds.add(plugin.getId());
 			}
 			return pluginIds;
 		}
 		pluginIds = techDAO.getPluginIds();
 		List<ArtifactGroup> plugins = technology.getPlugins();
 		for (ArtifactGroup artifactGroup : plugins) {
 			if(CollectionUtils.isNotEmpty(pluginIds)) {
 				boolean pluginAvailable = checkPluginAvailable(pluginIds, artifactGroup.getArtifactId());
 				if(pluginAvailable) {
 					pluginIds.add(artifactGroup.getId());
 				} else {
 					pluginIds.add(artifactGroup.getId());
 				}
 			}
 		}
 		return pluginIds;
 	}
 
 	private boolean checkPluginAvailable(List<String> pluginIds, String artifactId) {
 		for (String pluginId : pluginIds) {
 			ArtifactGroupDAO pluginFound = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(pluginId)), ArtifactGroupDAO.class);
 			if(pluginFound != null) {
 				if(pluginFound.getArtifactId().equals(artifactId)) {
 					return false;
 				} else {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private String createArchetypeId(Technology technology) {
 		TechnologyDAO techDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technology.getId())), TechnologyDAO.class);
 		if(techDAO == null) {
 			return technology.getArchetypeInfo().getId();
 		} else {
 			return techDAO.getArchetypeGroupDAOId();
 		}
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
 	public Response updateTechnologies(@Context HttpServletRequest request, MultiPart multipart) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnologies : Entry");
 	        LOGGER.debug("ComponentService.createTechnologies" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
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
 	public void deleteTechnologies(@Context HttpServletRequest request, List<Technology> technologies) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteTechnologies : Entry");
 	        LOGGER.debug("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error("ComponentService.deleteTechnologies", "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		throw phrescoException;
 	}
 	
 	
 	/**
 	 * Get the technology by id for the given parameter
 	 * @param id
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 	public Response getTechnology(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) throws PhrescoException {
 	    if (isDebugEnabled) {
 	    	LOGGER.debug("ComponentService.getTechnology : Entry");
 	    	LOGGER.debug("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		Technology technology = getTechnologyById(id);
 		if(technology == null) {
 			LOGGER.warn("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id , "status=\"Not Found\"");
 		}
 		if (isDebugEnabled) {
 	    	LOGGER.debug("ComponentService.getTechnology : Exit");
 	    }
 		return Response.status(Response.Status.OK).entity(technology).build();
 	}
 	
 	/**
 	 * Updates the technology given by the parameter
 	 * @param id
 	 * @param technology
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 	public Response updateTechnology(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , Technology technology) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnology");
 	        LOGGER.debug("ComponentService.updateTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "techId=" + technology.getId());
 	    }
 		try {
 			if (id.equals(technology.getId())) {
 				DbService.getMongoOperation().save(TECHNOLOGIES_COLLECTION_NAME, technology);
 				return Response.status(Response.Status.OK).entity(technology).build();
 			} 
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.updateTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnology : Exit");
 	    }
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 	}
 	
 	/**
 	 * Deletes the server by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_TECHNOLOGIES + REST_API_PATH_ID)
 	public Response deleteTechnology(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteTechnology : Entry");
 	        LOGGER.debug("ComponentService.deleteTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		
 		try {
 			deleteTechnologyObject(id);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		LOGGER.debug("ComponentService.deleteTechnology : Exit");
 		return Response.status(Response.Status.OK).build();
 	}
     
 	/**
 	 * Returns the list of settings
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_SETTINGS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findSettings(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_TECHID) String techId, @QueryParam(REST_QUERY_TYPE) String type) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findSettings : Entry");
 	        LOGGER.debug("ComponentService.findSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId) , "techId=" + techId, "type=" + type);
 	    }
 	    List<SettingsTemplate> settings = new ArrayList<SettingsTemplate>();
 		try {
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			if(StringUtils.isNotEmpty(techId)) {
 			    query.addCriteria(Criteria.where("appliesToTechs._id").is(techId));
 			} else {
 				Criteria customerCri = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(Arrays.asList(DEFAULT_CUSTOMER_NAME, customerId).toArray());
 				query.addCriteria(customerCri);
 			}
 			if(StringUtils.isNotEmpty(type)) {
 				query.addCriteria(Criteria.where(REST_API_NAME).is(type));
 				SettingsTemplate setting = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
 						query, SettingsTemplate.class);
 				return Response.status(Response.Status.OK).entity(setting).build();
 			}
 			List<SettingsTemplate> settingsList = DbService.getMongoOperation().find(SETTINGS_COLLECTION_NAME, query, SettingsTemplate.class);
 			for (SettingsTemplate settingsTemplate : settingsList) {
 				List<Element> types = getTypes(settingsTemplate.getName(), customerId);
 				settingsTemplate.setPossibleTypes(types);
 				settings.add(settingsTemplate);
 			}
 			if(CollectionUtils.isEmpty(settings)) {
 				if(isDebugEnabled) {
 					LOGGER.debug("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "status=\"Not Found\"", "message=\"" + "Settings Not Found" + "\"");
 				}
 				return Response.status(Response.Status.NO_CONTENT).build();
 			}
 			if(isDebugEnabled) {
 				LOGGER.debug("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 				LOGGER.debug("ComponentService.findSettings : Exit");
 			}
 			return Response.status(Response.Status.OK).entity(settings).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 	}
 	
 	private List<Element> getTypes(String type, String customerId) {
 		List<Element> types = new ArrayList<Element>();
 		Query query = createCustomerIdQuery(customerId);
 		Criteria typeCriteria = Criteria.where(CATEGORY).is(type.toUpperCase());
 		query.addCriteria(typeCriteria);
 		List<DownloadsDAO> downloads = DbService.getMongoOperation().find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 		for (DownloadsDAO downloadsDAO : downloads) {
 			Element element = new Element();
 			element.setId(downloadsDAO.getId());
 			element.setName(downloadsDAO.getName());
 			types.add(element);
 		}
 		return types;
 	}
 	
 	/**
 	 * Creates the list of settings
 	 * @param settings
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_SETTINGS)
 	public Response createSettings(@Context HttpServletRequest request, List<SettingsTemplate> settings) {
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.createSettings : Entry");
 		    LOGGER.debug("ComponentService.createSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(settings.get(0).getCustomerIds().get(0)));
 		}
 		try {
 			for (SettingsTemplate settingsTemplate : settings) {
 				if(validate(settingsTemplate)) {
 					DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, settingsTemplate);
 				}
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.createSettings : Exit");
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
 	public Response updateSettings(@Context HttpServletRequest request, List<SettingsTemplate> settings) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateSettings : Entry");
 	        LOGGER.debug("ComponentService.updateSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(settings.get(0).getCustomerIds().get(0)));
 	    }
 		
 		try {
 			for (SettingsTemplate settingTemplate : settings) {
 				SettingsTemplate settingTemplateInfo = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(settingTemplate.getId())), SettingsTemplate.class);
 				if (settingTemplateInfo != null) {
 					DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, settingTemplate);
 				}
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.updateSettings : Exit");
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
 	        LOGGER.debug("ComponentService.deleteSettings : Entry");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if(isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteSettings", "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		}
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
 	public Response getSettingsTemplate(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getSettingsTemplate : Entry");
 	        LOGGER.debug("ComponentService.getSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			SettingsTemplate settingTemplate = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class); 
 			if (settingTemplate != null) {
 				return Response.status(Response.Status.OK).entity(settingTemplate).build();
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getSettingsTemplate : Exit");
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
 	public Response updateSetting(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , 
 			SettingsTemplate settingsTemplate) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Entry");
 	        LOGGER.debug("ComponentService.getSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id , "customer" + getCustomerNameById(settingsTemplate.getCustomerIds().get(0)));
 	    }
 		try {
 			SettingsTemplate fromDb = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
 			List<String> customerIds = fromDb.getCustomerIds();
 			if(!customerIds.contains(settingsTemplate.getCustomerIds().get(0))) {
 				customerIds.add(settingsTemplate.getCustomerIds().get(0));
 			}
 			List<Element> appliesToTechs = fromDb.getAppliesToTechs();
 			List<Element> newAppliesToTechs = settingsTemplate.getAppliesToTechs();
 			for (Element element : newAppliesToTechs) {
 				if(! checkPreviouslyAvail(appliesToTechs, element)) {
 					appliesToTechs.add(element);
 				}
 			}
 			settingsTemplate.setAppliesToTechs(appliesToTechs);
 			settingsTemplate.setCustomerIds(customerIds);
 			DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, settingsTemplate);
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.updateAppType : Exit");
 		    }
 			return Response.status(Response.Status.OK).entity(settingsTemplate).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	private boolean checkPreviouslyAvail(List<Element> appliesToTechsInDb, Element newTech) {
 		for (Element element : appliesToTechsInDb) {
 			if(element.getId().equals(newTech.getId())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Deletes the settingsTemplate by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_SETTINGS + REST_API_PATH_ID)
 	public Response deleteSettingsTemplate(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate : Entry");
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate : Exit");
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
 	public Response findModules(@Context HttpServletRequest request, @QueryParam(REST_QUERY_TYPE) String type, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_TECHID) String techId, @QueryParam(REST_LIMIT_VALUE) String count,
 			@QueryParam(REST_SKIP_VALUE) String start) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findModules : Entry");
 	        LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId), "techId=" + techId);
 	    }
 		try {
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			Criteria typeQuery = Criteria.where(DB_COLUMN_ARTIFACT_GROUP_TYPE).is(type);
 			List<String> technologies = new ArrayList<String>();
 			technologies.add(techId);
 			if(StringUtils.isNotEmpty(techId)) {
 				Technology techInDB = getTechnologyById(techId);
 				if(CollectionUtils.isNotEmpty(techInDB.getArchetypeFeatures())) {
 					technologies.addAll(techInDB.getArchetypeFeatures());
 				}
 			}
 			Criteria techIdQuery = Criteria.where(DB_COLUMN_APPLIESTOTECHID).in(technologies.toArray());
 			query = query.addCriteria(typeQuery);
 			query = query.addCriteria(techIdQuery);
 			if(StringUtils.isNotEmpty(count)){
 				query.skip(Integer.parseInt(start)).limit(Integer.parseInt(count));
 			}
 			List<ArtifactGroupDAO> artifactGroupDAOs = DbService.getMongoOperation().find(ARTIFACT_GROUP_COLLECTION_NAME,
 					query, ArtifactGroupDAO.class);
 			if(CollectionUtils.isEmpty(artifactGroupDAOs)) {
 				if(isDebugEnabled) {
 					LOGGER.warn("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Not Found\"", "message=" + "Not Found");
 				}
 		    	return Response.status(Response.Status.NO_CONTENT).build();
 		    }
 			if(isDebugEnabled) {
 				LOGGER.info("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 			}
 		    List<ArtifactGroup> modules = convertDAOToModule(artifactGroupDAOs);
 		 
 		    ResponseBuilder response = Response.status(Response.Status.OK);
 		    response.header(Constants.ARTIFACT_COUNT_RESULT, count(ARTIFACT_GROUP_COLLECTION_NAME, query));
 		    if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findModules : Exit");
 		    }
 			return response.entity(modules).build();
 		    
 		} catch(Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 
 	}
 	
     private List<ArtifactGroup> convertDAOToModule(List<ArtifactGroupDAO> moduleDAOs) throws PhrescoException {
 		Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 	    List<ArtifactGroup> modules = new ArrayList<ArtifactGroup>();
 	    for (ArtifactGroupDAO artifactGroupDAO : moduleDAOs) {
 			ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGroupDAO, DbService.getMongoOperation());
 			ArtifactElement artifactElement = DbService.getMongoOperation().findOne(ARTIFACT_ELEMENT_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(artifactGroupDAO.getId())), ArtifactElement.class);
 		    if(artifactElement != null) {
 		    	artifactGroup.setDescription(artifactElement.getDescription());
 		    	artifactGroup.setHelpText(artifactElement.getHelpText());
 		    	artifactGroup.setSystem(artifactElement.isSystem());
 		    	artifactGroup.setLicenseId(artifactElement.getLicenseId());
 		    	artifactGroup.setCreationDate(artifactElement.getCreationDate());
 		    }
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
     public Response createModules(@Context HttpServletRequest request, MultiPart moduleInfo) throws PhrescoException {
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createModules : Entry");
         	LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
         }
         return createOrUpdateFeatures(moduleInfo);
     }
     
 	
     private Response createOrUpdateFeatures(MultiPart moduleInfo) throws PhrescoException {
     	ArtifactGroup moduleGroup = null;
         File moduleFile = null;
         List<BodyPart> bodyParts = moduleInfo.getBodyParts();
         Map<String, BodyPartEntity> bodyPartEntityMap = new HashMap<String, BodyPartEntity>();
         moduleGroup = createBodyPart(moduleGroup, bodyParts, bodyPartEntityMap);
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createOrUpdateFeatures " , "customer" + getCustomerNameById(moduleGroup.getCustomerIds().get(0)), 
         			"id" + moduleGroup.getId());
         }
         if(bodyPartEntityMap.isEmpty()) {
         	saveModuleGroup(moduleGroup);
         }
         if (!bodyPartEntityMap.isEmpty()) {
         	BodyPartEntity bodyPartEntity = bodyPartEntityMap.get(Type.ARCHETYPE.name());
         	if (bodyPartEntity != null) {
         		if(moduleGroup.getType().name().equals(FEATURE_TYPE_JS)) {
 					moduleGroup.setGroupId(JS_GROUP_ID);
 					moduleGroup.setArtifactId(moduleGroup.getName().toLowerCase());
 				}
         		moduleFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
         				moduleGroup.getPackaging(), moduleGroup.getName());
         		boolean uploadBinary = uploadBinary(moduleGroup, moduleFile);
                 if (uploadBinary) {
                 	saveModuleGroup(moduleGroup);
                 }
                 FileUtil.delete(moduleFile);
 			}
         	if(bodyPartEntityMap.get(Type.ICON.name()) != null) {
         		BodyPartEntity iconEntity = bodyPartEntityMap.get(Type.ICON.name());
             	File iconFile = ServerUtil.writeFileFromStream(iconEntity.getInputStream(), null, ICON_EXT, moduleGroup.getName());
             	moduleGroup.setPackaging(ICON_EXT);
         		boolean uploadBinary = uploadBinary(moduleGroup, iconFile);
         		FileUtil.delete(iconFile);
         		if(!uploadBinary) {
         			throw new PhrescoException("Module Icon Uploading Failed...");
         		}
         	}
         }
         bodyPartEntityMap.clear();
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createOrUpdateFeatures : Exit");
         }
         return Response.status(Response.Status.CREATED).entity(moduleGroup).build();
 	}
 
 	public ArtifactGroup createBodyPart(ArtifactGroup moduleGroup, List<BodyPart> bodyParts,
 			Map<String, BodyPartEntity> bodyPartEntityMap) {
 		if (CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                     moduleGroup = bodyPart.getEntityAs(ArtifactGroup.class);
                 } else {
                 	bodyPartEntityMap.put(bodyPart.getContentDisposition().getFileName(), (BodyPartEntity) bodyPart.getEntity());
                 }
             }
         }
 		return moduleGroup;
 	}
 	
 	private void saveModuleGroup(ArtifactGroup moduleGroup) throws PhrescoException {
 		if(!validate(moduleGroup)) {
 			return;
 		}
         Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         ArtifactGroupDAO moduleGroupDAO = converter.convertObjectToDAO(moduleGroup);
         
         List<String> versionIds = new ArrayList<String>();
         
         ArtifactGroupDAO moduleDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 		        new Query(Criteria.whereId().is(moduleGroupDAO.getId())), ArtifactGroupDAO.class);
         String newCustomerId = moduleGroup.getCustomerIds().get(0);
         List<String> customerIds = new ArrayList<String>();
         if(moduleDAO != null) {
         	 customerIds = moduleDAO.getCustomerIds();
              if(!moduleDAO.getCustomerIds().contains(newCustomerId)) {
              	customerIds.add(newCustomerId);
              	moduleGroupDAO.setCustomerIds(customerIds);
              }
         }
         com.photon.phresco.commons.model.ArtifactInfo newVersion = moduleGroup.getVersions().get(0);
         if(moduleDAO != null) {
         	moduleGroupDAO.setId(moduleDAO.getId());
         	versionIds.addAll(moduleDAO.getVersionIds());
         	List<com.photon.phresco.commons.model.ArtifactInfo> info = DbService.getMongoOperation().find(ARTIFACT_INFO_COLLECTION_NAME, 
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
 			DbService.getMongoOperation().save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }  else if(moduleDAO == null){
         	
         		versionIds.add(newVersion.getId());
         		newVersion.setArtifactGroupId(moduleGroupDAO.getId());
         		DbService.getMongoOperation().save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }
         moduleGroupDAO.setVersionIds(versionIds);
         DbService.getMongoOperation().save("artifactElement", createArtifactElement(moduleGroup));
         DbService.getMongoOperation().save(ARTIFACT_GROUP_COLLECTION_NAME, moduleGroupDAO);
     }
 	
 	private ArtifactElement createArtifactElement(ArtifactGroup artifactGroup) {
 		ArtifactElement artifactElement = new ArtifactElement();
 		artifactElement.setId(artifactGroup.getId());
 		artifactElement.setArtifactGroupId(artifactGroup.getId());
 		artifactElement.setHelpText(artifactGroup.getHelpText());
 		artifactElement.setDescription(artifactGroup.getDescription());
 		artifactElement.setCreationDate(artifactGroup.getCreationDate());
 		artifactElement.setSystem(artifactGroup.isSystem());
 		return artifactElement;
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
 	public Response updateModules(@Context HttpServletRequest request, MultiPart multiPart) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateModules :  Entry");
 	        LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 		return createOrUpdateFeatures(multiPart);
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
 	        LOGGER.debug("ComponentService.deleteModules : Entry");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules" ,"status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 	    }
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
 	public Response getModule(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getModule : Entry");
 	        LOGGER.debug("ComponentService.getModule" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		try {
 			ArtifactGroupDAO moduleDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ArtifactGroupDAO.class);
 			
 			if (moduleDAO != null) {
 		        Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 		            (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		        ArtifactGroup moduleGroup = converter.convertDAOToObject(moduleDAO, DbService.getMongoOperation());
 		        ArtifactElement artifactElement = DbService.getMongoOperation().findOne("artifactElement", 
 						new Query(Criteria.whereId().is(moduleGroup.getId())), ArtifactElement.class);
 			    if(artifactElement != null) {
 			    	moduleGroup.setDescription(artifactElement.getDescription());
 			    	moduleGroup.setHelpText(artifactElement.getHelpText());
 			    	moduleGroup.setSystem(artifactElement.isSystem());
 			    	moduleGroup.setLicenseId(artifactElement.getLicenseId());
 			    	moduleGroup.setCreationDate(artifactElement.getCreationDate());
 			    	return  Response.status(Response.Status.OK).entity(moduleGroup).build();
 			    } else {
 			    	if(isDebugEnabled) {
 						LOGGER.info("ComponentService.getModule", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"NotFound\"");
 					}
 			    	return Response.status(Response.Status.NO_CONTENT).build();
 			    }
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getModule", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 		return null;
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
 	public Response updatemodule(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id, ArtifactGroup module) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatemodule : Entry");
 	        LOGGER.debug("ComponentService.updatemodule" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id , "customer" + getCustomerNameById(module.getCustomerIds().get(0)));
 	    }
 		try {
 			if (id.equals(module.getId())) {
 				saveModuleGroup(module);
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.updatemodule : Exit");
 			    }
 				return  Response.status(Response.Status.OK).entity(module).build();
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updatemodule", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
 	public Response deleteModules(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules : Entry");
 	        LOGGER.debug("ComponentService.deleteModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		return deleteAttifact(id);
 	}
 	
 	private Response deleteAttifact(String id) {
 		try {
 			Query query = new Query(Criteria.whereId().is(id));
 			ArtifactGroupDAO artifactGroupDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 					query, ArtifactGroupDAO.class);
 			if(artifactGroupDAO != null) {
 				DbService.getMongoOperation().remove(ARTIFACT_GROUP_COLLECTION_NAME, query, ArtifactGroupDAO.class);
 				List<String> versionIds = artifactGroupDAO.getVersionIds();
 				DbService.getMongoOperation().remove(ARTIFACT_INFO_COLLECTION_NAME, new Query(Criteria.whereId().in(versionIds.toArray())), ArtifactInfo.class);
 			} else {
 				ArtifactInfo artifactInfo = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 				if(artifactInfo != null) {
 					ArtifactInfo info = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 					artifactGroupDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 							new Query(Criteria.whereId().is(info.getArtifactGroupId())), ArtifactGroupDAO.class);
 					List<String> versionIds = artifactGroupDAO.getVersionIds();
 					versionIds.remove(id);
 					artifactGroupDAO.setVersionIds(versionIds);
 					DbService.getMongoOperation().save(ARTIFACT_GROUP_COLLECTION_NAME, artifactGroupDAO);
 					DbService.getMongoOperation().remove(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 				}
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.deleteAttifact " , "query=" + query.getQueryObject().toString());
 		    }
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteAttifact", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules : Exit");
 	    }
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the artifactInfo
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_ARTIFACTINFO)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response getArtifactInfo(@Context HttpServletRequest request, @QueryParam(REST_QUERY_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getArtifactInfo : Entry");
 	        LOGGER.debug("ComponentService.getArtifactInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		try {
 			Query query = new Query(Criteria.whereId().is(id));
 			ArtifactInfo info = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.getArtifactInfo : Exit");
 			}
 			return  Response.status(Response.Status.OK).entity(info).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getArtifactInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_INFO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of pilots
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PILOTS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPilots(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPilots : Entry");
 	        LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "customer" + getCustomerNameById(customerId) , "techId=" + techId);
 	    }
 	    List<ApplicationInfo> applicationInfos = new ArrayList<ApplicationInfo>();
 		try {
 		    List<ApplicationInfoDAO> appInfos = new ArrayList<ApplicationInfoDAO>();
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			query.addCriteria(Criteria.where(REST_QUERY_ISPILOT).is(true));
 			Converter<ApplicationInfoDAO, ApplicationInfo> pilotConverter = 
             		(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 			if (StringUtils.isNotEmpty(techId)) {
                 query.addCriteria(Criteria.where(TECHINFO_VERSION).is(techId));
 			}
             appInfos = DbService.getMongoOperation().find(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
             for (ApplicationInfoDAO applicationInfoDAO : appInfos) {
 	            ApplicationInfo applicationInfo = pilotConverter.convertDAOToObject(applicationInfoDAO, DbService.getMongoOperation());
 	            applicationInfos.add(applicationInfo);
 			}
             
 			if(CollectionUtils.isEmpty(applicationInfos)) {
 				if (isDebugEnabled) {
 			        LOGGER.info("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId") , "status=\"Not Found\"");
 			    }
 				return Response.status(Response.Status.NO_CONTENT).build();
 			}
 			if (isDebugEnabled) {
 		        LOGGER.info("ComponentService.findPilots" , "query=" + query.getQueryObject().toString());
 		    }
 			ResponseBuilder response = Response.status(Response.Status.OK);
 			response.header(Constants.ARTIFACT_COUNT_RESULT, count(APPLICATION_INFO_COLLECTION_NAME, query));
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findPilots : Exit");
 		    }
 			return response.entity(applicationInfos).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findPilots", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
     public Response createPilots(@Context HttpServletRequest request, MultiPart pilotInfo) throws PhrescoException {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createPilots : Entry");
             LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
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
         	 pilotFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
              		applicationInfo.getPilotContent().getPackaging(), applicationInfo.getName());
              boolean uploadBinary = uploadBinary(applicationInfo.getPilotContent(), pilotFile);
              if(uploadBinary) {
              	saveApplicationInfo(applicationInfo);
              } 
              FileUtil.delete(pilotFile);
         }
        
         
         if(bodyPartEntity == null && applicationInfo != null) {
         	saveApplicationInfo(applicationInfo);
         }
         return Response.status(Response.Status.CREATED).entity(applicationInfo).build();
 	}
 	
 	private void saveApplicationInfo(ApplicationInfo applicationInfo) throws PhrescoException {
 		if(!validate(applicationInfo)) {
 			return;
 		}
 		Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
 		(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 		ApplicationInfoDAO applicationInfoDAO = appConverter.convertObjectToDAO(applicationInfo);
 		DbService.getMongoOperation().save(APPLICATION_INFO_COLLECTION_NAME, applicationInfoDAO);
 		ArtifactGroup pilotContent = applicationInfo.getPilotContent();
 		saveModuleGroup(pilotContent);
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
 	public Response updatePilots(@Context HttpServletRequest request, MultiPart pilotInfo) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatePilots : Entry");
 	        LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
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
 	        LOGGER.debug("ComponentService.deletePilots : Entry");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilots" , "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 	    }
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
 	public Response getPilot(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getPilot : Entry ");
 	        LOGGER.debug("ComponentService.getPilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			ApplicationInfo appInfo = DbService.getMongoOperation().findOne(APPLICATION_INFO_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationInfo.class);
 			
 			if (appInfo != null) {
 				return Response.status(Response.Status.OK).entity(appInfo).build();
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getPilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
 	public Response updatePilot(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , ApplicationInfo pilot) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatePilot : Entry"); 
 	        LOGGER.debug("ComponentService.updatePilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			if (id.equals(pilot.getId())) {
 				DbService.getMongoOperation().save(APPLICATION_INFO_COLLECTION_NAME, pilot);
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.updatePilot : Exit"); 
 			    }
 				return  Response.status(Response.Status.OK).entity(pilot).build();
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updatePilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
 	public Response deletePilot(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilot : Entry ");
 	        LOGGER.debug("ComponentService.deletePilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 	    Query query = new Query(Criteria.whereId().is(id));
 	    ApplicationInfoDAO applicationInfoDAO = DbService.getMongoOperation().findOne(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
 	    if(applicationInfoDAO != null) {
 	    	deleteAttifact(applicationInfoDAO.getArtifactGroupId());
 	    	DbService.getMongoOperation().remove(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
 	    } else {
 	    	if (isDebugEnabled) {
 		        LOGGER.info("ComponentService.deletePilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Not Found\"");
 		        LOGGER.info("ComponentService.deletePilot", "query=" + query.getQueryObject().toString());
 		    }
 	    }
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilot : Exit ");
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
 	public Response findWebServices(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findWebServices : Entry ");
 	        LOGGER.debug("ComponentService.findWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId));
 	    }
 		try {
 			List<WebService> webServiceList = DbService.getMongoOperation().getCollection(WEBSERVICES_COLLECTION_NAME, WebService.class);
 			if(CollectionUtils.isEmpty(webServiceList)) {
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.findWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Not Found\"");
 			    }
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findWebServices : Exit ");
 		    }
 			return  Response.status(Response.Status.OK).entity(webServiceList).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
 	public Response createWebServices(@Context HttpServletRequest request, List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createWebServices : Entry");
 	        LOGGER.debug("ComponentService.createWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + webServices.get(0).getId());
 	    }
 		
 		try {
 			for (WebService webService : webServices) {
 				if(validate(webService)) {
 					DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME , webService);
 				}
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createWebServices : Exit");
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
 	public Response updateWebServices(@Context HttpServletRequest request, List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebServices : Entry");
 	        LOGGER.debug("ComponentService.updateWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + webServices.get(0).getId());
 	    }
 		try {
 			for (WebService webService : webServices) {
 				WebService webServiceInfo = DbService.getMongoOperation().findOne(WEBSERVICES_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(webService.getId())), WebService.class);
 				if (webServiceInfo != null) {
 					DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME , webService);
 				}
 			}
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebServices : Exit");
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
 	public void deleteWebServices(@Context HttpServletRequest request, List<WebService> webServices) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebServices : Entry");
 	    }
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if(isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
     		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		}
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
 	public Response getWebService(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getWebService : Entry");
 	        LOGGER.debug("ComponentService.getWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			WebService webService = DbService.getMongoOperation().findOne(WEBSERVICES_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 			if (webService != null) {
 				return Response.status(Response.Status.OK).entity(webService).build();
 			} 
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getWebService : Exit");
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
 	public Response updateWebService(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , WebService webService) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebService : Entry");
 	        LOGGER.debug("ComponentService.updateWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		
 		try {
 			if (id.equals(webService.getId())) {
 				DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME, webService);
 				return Response.status(Response.Status.OK).entity(webService).build();
 			} 
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebService : Exit");
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
 	public Response deleteWebService(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebService : Entry ");
 	        LOGGER.debug("ComponentService.updateWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(WEBSERVICES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebService : Exit ");
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
     public Response findDownloadInfo(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
     		@QueryParam(REST_QUERY_TECHID) String techId, @QueryParam(REST_QUERY_TYPE) String type, 
     		@QueryParam(REST_QUERY_PLATFORM) String platform) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.findDownloadInfo : Entry");
             LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customerId=" + customerId, "techId=" + techId, "type=" + type, "platform=" + platform);
         }
         List<DownloadInfo> downloads = new ArrayList<DownloadInfo>();
         Query query = new Query();
         try {
         	if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
         	if(StringUtils.isNotEmpty(platform)) {
         		query.addCriteria(Criteria.where(DB_COLUMN_PLATFORM).is(platform));
         	}
         	if(StringUtils.isNotEmpty(techId)) {
         		Criteria techIdCriteria = Criteria.where(APPLIES_TO_TECHIDS).in(techId);
             	query.addCriteria(techIdCriteria);
         	}
         	if(StringUtils.isNotEmpty(type)) {
         		Criteria typeCriteria = Criteria.where(CATEGORY).is(type);
         		query.addCriteria(typeCriteria);
         	}
         	if(StringUtils.isEmpty(techId) && StringUtils.isEmpty(type) && StringUtils.isEmpty(platform)) {
         		Criteria customerCri = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(Arrays.asList(DEFAULT_CUSTOMER_NAME, customerId).toArray());
 				query.addCriteria(customerCri);
         	}
         	List<DownloadsDAO> downloadList = DbService.getMongoOperation().find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
         	if(CollectionUtils.isEmpty(downloadList)) {
         		LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
     	        		"user=" + request.getParameter("userId"), "status=\"Bad Request\"", "message=" + "Not Found");
             	return  Response.status(Response.Status.NO_CONTENT).build();
             }
         	Converter<DownloadsDAO, DownloadInfo> downloadConverter = 
         		(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
         	for (DownloadsDAO downloadsDAO : downloadList) {
 				DownloadInfo downloadInfo = downloadConverter.convertDAOToObject(downloadsDAO, DbService.getMongoOperation());
 				downloads.add(downloadInfo);
 			}
         	LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
         } catch (Exception e) {
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00006, DOWNLOAD_COLLECTION_NAME);
         }
         ResponseBuilder response = Response.status(Response.Status.OK);
         response.header(Constants.ARTIFACT_COUNT_RESULT, count(DOWNLOAD_COLLECTION_NAME, query));
 		return response.entity(downloads).build();
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
     public Response createDownloads(@Context HttpServletRequest request, MultiPart downloadPart) throws PhrescoException {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads : Entry");
             LOGGER.debug("ComponentService.createDownloads" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
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
                     downloadInfo = bodyPart.getEntityAs(DownloadInfo.class);
                 } else if(bodyPart.getMediaType().equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)){
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads " , "id=" + downloadInfo.getId() , "customer=" + 
             		getCustomerNameById(downloadInfo.getCustomerIds().get(0)));
         }
         if(bodyPartEntity != null) {
             downloadFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null,
             		downloadInfo.getArtifactGroup().getPackaging(), downloadInfo.getName());
             boolean uploadBinary = uploadBinary(downloadInfo.getArtifactGroup(), downloadFile);
             if(uploadBinary) {
                 saveDownloads(downloadInfo);
             }
             FileUtil.delete(downloadFile);
         }
         
         if(bodyPartEntity == null && downloadInfo != null) {
         	saveDownloads(downloadInfo);
         }
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads : Exit");
         }
         return Response.status(Response.Status.CREATED).entity(downloadInfo).build();
 		
 	}
 
     private void saveDownloads(DownloadInfo info) throws PhrescoException {
     	if(!validate(info)) {
     		return;
     	}
     	Converter<DownloadsDAO, DownloadInfo> downlodConverter = 
     		(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
     	DownloadsDAO downloadDAO = downlodConverter.convertObjectToDAO(info);
     	DownloadsDAO dDao = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME,  new Query(Criteria.whereId().is(info.getId())), DownloadsDAO.class);
     	if(dDao != null) {
     		List<String> appliesToTechIdsDB = dDao.getAppliesToTechIds();
     		List<String> appliesToTechIds = info.getAppliesToTechIds();
     		for (String techId : appliesToTechIds) {
 				if(!appliesToTechIdsDB.contains(techId)) {
 					appliesToTechIdsDB.add(techId);
 				}
 			}
     		downloadDAO.setAppliesToTechIds(appliesToTechIdsDB);
     		List<String> customerIdsInDb = dDao.getCustomerIds();
     		if(!customerIdsInDb.contains(info.getCustomerIds().get(0))) {
     			customerIdsInDb.add(info.getCustomerIds().get(0));
     		}
     		downloadDAO.setCustomerIds(customerIdsInDb);
     	}
     	ArtifactGroup artifactGroup = info.getArtifactGroup();
     	saveModuleGroup(artifactGroup);
     	DbService.getMongoOperation().save(DOWNLOAD_COLLECTION_NAME, downloadDAO);
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
     public Response updateDownloadInfo(@Context HttpServletRequest request, MultiPart multiPart) throws PhrescoException {
     	if (isDebugEnabled) {
             LOGGER.debug("ComponentService.updateDownloadInfo : Entry");
             LOGGER.debug("ComponentService.createDownloads" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
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
             LOGGER.debug("ComponentService.deleteDownloadInfo : Entry");
         }
         
         PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
         if (isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteDownloadInfo", "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
         }
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
     public Response getDownloadInfo(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.getDownloadInfo : Entry ");
             LOGGER.debug("ComponentService.getDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         
         try {
         	DownloadsDAO downloadDAO = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME, 
                     new Query(Criteria.whereId().is(id)), DownloadsDAO.class);
         	if(downloadDAO != null) {
     			Converter<DownloadsDAO, DownloadInfo> downlodConverter = 
     					(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
     			DownloadInfo downloadInfo = downlodConverter.convertDAOToObject(downloadDAO, DbService.getMongoOperation());
     			return Response.status(Response.Status.OK).entity(downloadInfo).build();
         	}
         	
         } catch (Exception e) {
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getDownloadInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
     public Response updateDownloadInfo(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id , DownloadInfo downloadInfo) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.updateDownloadInfo : Entry ");
             LOGGER.debug("ComponentService.updateDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         try {
             if (id.equals(downloadInfo.getId())) {
             	DbService.getMongoOperation().save(DOWNLOAD_COLLECTION_NAME, downloadInfo);
                 return Response.status(Response.Status.OK).entity(downloadInfo).build();
             } 
         } catch (Exception e) {
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateDownloadInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
         }
         LOGGER.debug("ComponentService.updateDownloadInfo : Exit ");
         return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
     }
     
     /**
      * Deletes the user by id for the given parameter
      * @param id
      * @return 
      */
     @DELETE
     @Path (REST_API_DOWNLOADS + REST_API_PATH_ID)
     public Response deleteDownloadInfo(@Context HttpServletRequest request, @PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.deleteDownloadInfo : Entry");
             LOGGER.debug("ComponentService.deleteDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         Query query = new Query(Criteria.whereId().is(id));
         LOGGER.debug("ComponentService.deleteDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 	    DownloadsDAO downloadsDAO = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 	    if(downloadsDAO != null) {
 	    	deleteAttifact(downloadsDAO.getArtifactGroupId());
 	    	DbService.getMongoOperation().remove(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 	    }
 	    LOGGER.debug("ComponentService.deleteDownloadInfo : Exit");
 		return Response.status(Response.Status.OK).build(); 
     }
     
     /**
 	 * Returns the list of platforms available
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PLATFORMS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPlatforms(@Context HttpServletRequest request) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPlatforms : Entry ");
 	        LOGGER.debug("ComponentService.findPlatforms" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 		
 		try {
 			List<PlatformType> platformList = DbService.getMongoOperation().getCollection(PLATFORMS_COLLECTION_NAME, PlatformType.class);
 			if(CollectionUtils.isEmpty(platformList)) {
 				return Response.status(Response.Status.NO_CONTENT).build();
 			}
 			LOGGER.debug("ComponentService.findPlatforms : Exit ");
 			return Response.status(Response.Status.OK).entity(platformList).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findPlatforms", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
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
 	public Response findReports(@Context HttpServletRequest request ,@QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findReports : Entry ");
 	        LOGGER.debug("ComponentService.findReports" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "techId=" + techId);
 	    }
 	    List<Reports> reports = new ArrayList<Reports>();
 		try {
 			if(StringUtils.isEmpty(techId)) {
 				reports = DbService.getMongoOperation().getCollection(REPORTS_COLLECTION_NAME, Reports.class);
 			} else {
 				TechnologyDAO tech = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(techId)), TechnologyDAO.class);
 				if(tech != null) {
 					List<String> reportIds = tech.getReports();
 					if(CollectionUtils.isNotEmpty(reportIds)) {
 						reports = DbService.getMongoOperation().find(REPORTS_COLLECTION_NAME, 
 								new Query(Criteria.whereId().in(reportIds.toArray())), Reports.class);
 					}
 				}
 			}
 			if(CollectionUtils.isEmpty(reports)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			LOGGER.debug("ComponentService.findReports : Exit ");
 			return  Response.status(Response.Status.OK).entity(reports).build();
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findReports", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, REPORTS_COLLECTION_NAME);
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
 	        LOGGER.debug("Entered into ComponentService.createReports(List<Reports> reports)");
 	    }
 		
 		try {
 			for (Reports report : reports) {
 				if(validate(report)) {
 					DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME , report);
 				}
 			}
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
 	        LOGGER.debug("Entered into ComponentService.updateReports(List<Reports> reports)");
 	    }
 		
 		try {
 			for (Reports report : reports) {
 				DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME, report);
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
 	        LOGGER.debug("Entered into ComponentService.deleteReports(List<Reports> reports)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
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
 	        LOGGER.debug("Entered into ComponentService.getReports(String id)" + id);
 	    }
 		
 		try {
 			Reports reports = DbService.getMongoOperation().findOne(REPORTS_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Reports.class);
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
 	        LOGGER.debug("Entered into ComponentService.updateReports(String id, Reports reports)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME, reports);
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
 	        LOGGER.debug("Entered into ComponentService.deleteReportse(String id)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().remove(REPORTS_COLLECTION_NAME, 
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
 	        LOGGER.debug("Entered into ComponentService.findProperties()");
 	    }
 		try {
 			List<Property> properties = DbService.getMongoOperation().getCollection(PROPERTIES_COLLECTION_NAME, Property.class);
 			if(CollectionUtils.isEmpty(properties)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
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
 	        LOGGER.debug("Entered into ComponentService.createProperties(List<Property> properties)");
 	    }
 		
 		try {
 			for (Property property : properties) {
 				if(validate(property)) {
 					DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME , property);
 				}
 			}
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
 	        LOGGER.debug("Entered into ComponentService.updateProperties(List<Property> properties)");
 	    }
 	   
 		try {
 			for (Property property : properties) {
 				DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME, property);
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
 	        LOGGER.debug("Entered into ComponentService.deleteProperties(List<Property> properties)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
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
 	        LOGGER.debug("Entered into ComponentService.getProperty(String id)" + id);
 	    }
 		
 		try {
 			Property property = DbService.getMongoOperation().findOne(PROPERTIES_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Property.class);
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
 	        LOGGER.debug("Entered into ComponentService.updateProperty(String id, Property property)" + id);
 	    }
 	   	try {
 	   		DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME, property);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(property).build();
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
 	        LOGGER.debug("Entered into ComponentService.deleteProperty(String id)" + id);
 	    }
 		
 		try {
 	   		DbService.getMongoOperation().remove(PROPERTIES_COLLECTION_NAME, 
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
 	        LOGGER.debug("Entered into ComponentService.findOptions()");
 	    }
 		try {
 			List<TechnologyOptions> techOptions = DbService.getMongoOperation().getCollection(OPTIONS_COLLECTION_NAME, TechnologyOptions.class);
 			if(CollectionUtils.isEmpty(techOptions)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			return  Response.status(Response.Status.OK).entity(techOptions).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of Functional test frameworks
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_OPTIONS_FUNCTIONAL)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findFunctionalTestFrameworks(@QueryParam(REST_QUERY_TECHID) String techId, @QueryParam("name") String name) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findFunctionalTestFrameworks()");
 	    }
 		try {
 			if (StringUtils.isNotEmpty(techId) && StringUtils.isNotEmpty(name)) {
 				FunctionalFramework functionalFramework = DbService.getMongoOperation().findOne(FUNCTIONAL_FRAMEWORK_COLLECTION_NAME, 
 						new Query(Criteria.where("name").is(name)), FunctionalFramework.class);
 				List<FunctionalFrameworkProperties> funcFrameworkProperties = functionalFramework.getFuncFrameworkProperties();
 				if (CollectionUtils.isNotEmpty(funcFrameworkProperties)) {
 					for (FunctionalFrameworkProperties functionalFrameworkProperties : funcFrameworkProperties) {
 						if (functionalFrameworkProperties.getTechId().equals(techId)) {
 							functionalFramework.setFuncFrameworkProperties(Collections.singletonList(functionalFrameworkProperties));
 							break;
 						}
 					}
 				}
 				return  Response.status(Response.Status.OK).entity(functionalFramework).build();
 			}
 			List<FunctionalFramework> options = DbService.getMongoOperation().getCollection(FUNCTIONAL_FRAMEWORK_COLLECTION_NAME, FunctionalFramework.class);
 			if (CollectionUtils.isEmpty(options)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			return  Response.status(Response.Status.OK).entity(options).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, FUNCTIONAL_FRAMEWORK_COLLECTION_NAME);
 		}
 	}
 	
 	/**
      * Returns the list of Technologyoptions
      * @return
      */
     @GET
     @Path (REST_API_OPTIONS_CUSTOMER)
     @Produces (MediaType.APPLICATION_JSON)
     public Response findCustomerOptions() {
         if (isDebugEnabled) {
             LOGGER.debug("Entered into ComponentService.findCustomerOptions()");
         }
         try {
             List<TechnologyOptions> techOptions = DbService.getMongoOperation().getCollection(CUSTOMER_OPTIONS_COLLECTION_NAME, TechnologyOptions.class);
             if(CollectionUtils.isEmpty(techOptions)) {
                 return  Response.status(Response.Status.NO_CONTENT).build();
             }
             return  Response.status(Response.Status.OK).entity(techOptions).build();
         } catch (Exception e) {
             throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMER_OPTIONS_COLLECTION_NAME);
         }
     }
 	
 	/**
 	 * Returns the list of Licenses
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_LICENSE)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findLicenses() {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findLicenses()");
 	    }
 		try {
 			List<License> licenses = DbService.getMongoOperation().getCollection(LICENSE_COLLECTION_NAME, License.class);
 			if(CollectionUtils.isEmpty(licenses)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			return  Response.status(Response.Status.OK).entity(licenses).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Properties
 	 * @param properties
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_LICENSE)
 	public Response createLicenses(List<License> licenses) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createLicenses(List<License> licenses)");
 	    }
 		
 		try {
 			for (License license : licenses) {
 				if(validate(license)) {
 					DbService.getMongoOperation().save(LICENSE_COLLECTION_NAME , license);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Technology Options
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_TECHGROUPS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findTechnologyGroups(@QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam(REST_QUERY_APPTYPEID) String appTypeId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findTechnologyGroups()");
 	    }
 	    List<TechnologyGroup> technologyGroups = new ArrayList<TechnologyGroup>();
 		try {
 			if(StringUtils.isNotEmpty(customerId) && StringUtils.isNotEmpty(appTypeId)) {
 				technologyGroups = getTechGroupByCustomer(customerId, appTypeId);
 				return  Response.status(Response.Status.OK).entity(technologyGroups).build();
 			}
 			if(CollectionUtils.isEmpty(technologyGroups)) {
 				return  Response.status(Response.Status.NO_CONTENT).build();
 			}
 			return  Response.status(Response.Status.OK).entity(technologyGroups).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECH_GROUP_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Technology Groups
 	 * @param techGroups
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHGROUPS)
 	public Response createTechnologyGroups(List<TechnologyGroup> techGroups) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		
 		try {
 			for (TechnologyGroup technologyGroup : techGroups) {
 				if(validate(technologyGroup)) {
 					DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME , technologyGroup);
 					
 					ApplicationTypeDAO type = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 							new Query(Criteria.whereId().is(technologyGroup.getAppTypeId())), ApplicationTypeDAO.class);
 					List<String> techGroupIds = type.getTechGroupIds();
 					techGroupIds.add(technologyGroup.getId());
 					type.setTechGroupIds(techGroupIds);
 					
 					DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , type);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of TechnologyGroups
 	 * @param techGroups
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHGROUPS)
 	public Response updateTechnologyGroups(List<TechnologyGroup> techGroups) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		
 		try {
 			for (TechnologyGroup techGroup : techGroups) {
 				DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, techGroup);
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(techGroups).build();
 	}
 	
 	/**
 	 * Deletes the list of TechnologyGroups
 	 * @param properties
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_TECHGROUPS)
 	public void deleteTechnologyGroups(List<TechnologyGroup> techGroups) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the TechnologyGroup by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHGROUPS + REST_API_PATH_ID)
 	public Response getTechnologyGroup(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.getTechnologyGroup(String id)" + id);
 	    }
 		
 		try {
 			TechnologyGroup techGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 			if(techGroup != null) {
 				return Response.status(Response.Status.OK).entity(techGroup).build();
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECH_GROUP_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the TechnologyGroup given by the parameter
 	 * @param id
 	 * @param techGroup
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_TECHGROUPS + REST_API_PATH_ID)
 	public Response updateTechnologyGroup(@PathParam(REST_API_PATH_PARAM_ID) String id , TechnologyGroup techGroup) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateTechnologyGroup(String id, TechnologyGroup techGroup)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, techGroup);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(techGroup).build();
 	}
 	
 	/**
 	 * Deletes the TechnologyGroup by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_TECHGROUPS + REST_API_PATH_ID)
 	public Response deleteTechnologyGroup(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteTechnologyGroup((String id)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().remove(TECH_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 			
 			ApplicationTypeDAO type = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 					new Query(Criteria.where(TECH_GROUP_ID).is(id)), ApplicationTypeDAO.class);
 			List<String> techGroupIds = type.getTechGroupIds();
 			techGroupIds.remove(id);
 			type.setTechGroupIds(techGroupIds);
 			
 			DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , type);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Prebuilt Projects
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PREBUILT)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPreBuiltProjects(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPreBuiltProjects : Entry ");
 	        LOGGER.debug("ComponentService.findPreBuiltProjects ", "remoteAddress=" + request.getRemoteAddr() , "customer" + getCustomerNameById(customerId),
 	        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"), "customer=" + getCustomerNameById(customerId));
 	    }
 	    List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 		try {
 			List<ProjectInfoDAO> projectDAOs = DbService.getMongoOperation().find(PROJECTINFO_COLLECTION_NAME, 
 					new Query(Criteria.where(DB_COLUMN_PREBUILT).is(true)), ProjectInfoDAO.class);
 			Converter<ProjectInfoDAO, ProjectInfo> converter = (Converter<ProjectInfoDAO, ProjectInfo>) 
 				ConvertersFactory.getFrameworkConverter(ProjectInfoDAO.class);
 			for (ProjectInfoDAO projectInfoDAO : projectDAOs) {
 				ProjectInfo pInfo = converter.convertDAOToObject(projectInfoDAO, DbService.getMongoOperation());
 				if(isApplicable(customerId, pInfo)) {
 					projectInfos.add(pInfo);
 				}
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findPreBuiltProjects : Exit ");
 		    }
 			return Response.status(Response.Status.OK).entity(projectInfos).build();
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.findPreBuiltProjects", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 		
 	}
 
 	private boolean isApplicable(String customerId, ProjectInfo pInfo) {
 		CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
 		if(customer == null) {
 			return false;
 		}
 		List<String> techs = customer.getApplicableTechnologies();
 		List<ApplicationInfo> appInfos = pInfo.getAppInfos();
 		if(CollectionUtils.isEmpty(appInfos)) {
 			return false;
 		}
 		for (ApplicationInfo applicationInfo : appInfos) {
 			if(!techs.contains(applicationInfo.getTechInfo().getId())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
