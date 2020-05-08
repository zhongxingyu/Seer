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
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.HttpMethod;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RequestPart;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.HandlerMapping;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.LogInfo;
 import com.photon.phresco.commons.model.Permission;
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.Role;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.User.AuthType;
 import com.photon.phresco.commons.model.VideoInfo;
 import com.photon.phresco.commons.model.VideoType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.client.impl.ClientHelper;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.BaseDAO;
 import com.photon.phresco.service.dao.CustomerDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.dao.VideoInfoDAO;
 import com.photon.phresco.service.dao.VideoTypeDAO;
 import com.photon.phresco.service.impl.DbService;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.ServiceConstants;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.MultiPartMediaTypes;
 import com.wordnik.swagger.annotations.ApiError;
 import com.wordnik.swagger.annotations.ApiErrors;
 import com.wordnik.swagger.annotations.ApiOperation;
 import com.wordnik.swagger.annotations.ApiParam;
 
 @Controller
 @RequestMapping(value = ServiceConstants.REST_API_ADMIN)
 public class AdminService extends DbService {
 	
 	private static final Logger S_LOGGER = Logger.getLogger("SplunkLogger");
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private RepositoryManager repositoryManager = null;
 	private static String exceptionString = "PhrescoException Is";
 	
     public AdminService() throws PhrescoException {
     	super();
     	PhrescoServerFactory.initialize();
     	repositoryManager = PhrescoServerFactory.getRepositoryManager();
     }
     
     /**
      * Returns the list of customers and customer by name
      * @return
      * @throws PhrescoException 
      */
     @ApiOperation(value = " Lists all customers and returns single customer if customer name is present")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Customers not found")})
     @RequestMapping(value= ServiceConstants.REST_API_CUSTOMERS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody List<Customer> findCustomer(HttpServletResponse response, 
     		@ApiParam(value = "The name of the customer to fetch", name = REST_QUERY_NAME) @QueryParam(REST_QUERY_NAME) 
     		String name) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.findCustomer()");
         }
         List<Customer> customers = new ArrayList<Customer>();
         if(StringUtils.isNotEmpty(name)) {
         	Converter<CustomerDAO, Customer> converter = (Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
         	List<CustomerDAO> customerDAOs = DbService.getMongoOperation().
         		find(CUSTOMERS_COLLECTION_NAME, new Query(Criteria.where(REST_QUERY_NAME).is(name)), CustomerDAO.class);
         	for (CustomerDAO customerDAO : customerDAOs) {
 				Customer convertDAOToObject = converter.convertDAOToObject(customerDAO, DbService.getMongoOperation());
 				customers.add(convertDAOToObject);
 			}
         	return customers;
         }
 		customers = findCustomersFromDB();
 		if(CollectionUtils.isEmpty(customers)) {
 			response.setStatus(204);
 			return customers;
 		}
 		response.setStatus(200);
 		return customers;
     }
     
     @ApiOperation(value = "Get customer icon by given customerid")
     @ApiErrors(value = {@ApiError(code=204, reason = "Icon not found")})
     @RequestMapping(value= REST_API_ICON, produces = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.GET)
     public @ResponseBody byte[] getIcon(HttpServletResponse response, 
     		@ApiParam(value = "The Id of the customer to get icon", name = REST_QUERY_CUSTOMERID) @QueryParam(REST_QUERY_CUSTOMERID) 
     		String customerId, @ApiParam(value = "The context of the customer to get icon", name = "Context") 
     		@QueryParam("context") String context) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getIcon(String id)");
         }
         byte[] byteArray = null;
         InputStream iconStream = null;
 		CustomerDAO customerDAO =  null;
 		if(StringUtils.isEmpty(context) && StringUtils.isEmpty(customerId)) {
 			return byteArray;
 		}
 		if(StringUtils.isNotEmpty(context)) {
 			customerDAO = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 					 new Query(Criteria.where("context").is(context)), CustomerDAO.class);
 		} else {
 			customerDAO = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
					 new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
 		}
 		String id = customerDAO.getId();
 		Converter<CustomerDAO, Customer> converter = (Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
 		Customer customer = converter.convertDAOToObject(customerDAO, DbService.getMongoOperation());
         String repourl = customer.getRepoInfo().getGroupRepoURL();
         String artifactId = filterString(customer.getName());
         String contentURL = ServerUtil.createContentURL("customers", artifactId, "1.0", "png");
         if(! id.equals(DEFAULT_CUSTOMER_NAME)) {
         	CustomerDAO defCustomer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         			new Query(Criteria.whereId().is(DEFAULT_CUSTOMER_NAME)), CustomerDAO.class);
         	repourl = converter.convertDAOToObject(defCustomer, DbService.getMongoOperation()).getRepoInfo().getGroupRepoURL();
         }
         try {
 			URL url = new URL(repourl + "/" + contentURL);
 			iconStream = url.openStream();
 			if(iconStream == null) {
 	        	response.setStatus(204);
 	        	return null;
 	        }
 			byteArray = IOUtils.toByteArray(iconStream);
 			response.setStatus(200);
 		} catch (MalformedURLException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
         
     	return byteArray;
     }
     
     private String filterString(String inputString) {
     	return StringUtils.replaceEach(inputString, 
         		new String[]{" ","'","&","(",")","{","}"}, new String[]{"-","","","","","",""}).toLowerCase();
     }
     
     @ApiOperation(value = "Get customer properties by given customer context")
     @ApiErrors(value = {@ApiError(code=204, reason = "Icon not found"), @ApiError(code=500, reason = "Failed to retrive")})
     @RequestMapping(value= "/customers/properties", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody Customer getCustomerProperties(HttpServletResponse response, HttpServletRequest request, 
     		@ApiParam(value = "The context of the customer to get properties", name = "context") @QueryParam(REST_QUERY_CONTEXT) String context)
     		throws PhrescoException, IOException {
     	 if (isDebugEnabled) {
              S_LOGGER.debug("Entered into AdminService.getCustomerProperties()");
          }
     	 Customer customerInfo = null;
     	 if(StringUtils.isNotEmpty(context)) {
 				CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 				        new Query(Criteria.where("context").is(context)), CustomerDAO.class);
 			if (customer != null) {
 				Converter<CustomerDAO, Customer> customerConverter = 
 					(Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
 				customerInfo = customerConverter.convertDAOToObject(customer, DbService.getMongoOperation());
 			}
 		}
     	if(customerInfo == null ) {
     		 response.setStatus(204);
     		 return customerInfo;
     	}
     	response.setStatus(200);
 		return customerInfo;
     }
     
     /**
      * Creates the list of customers
      * @param customer
      * @return 
      * @throws IOException 
      */
     @ApiOperation(value = " Creates a new customer ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_CUSTOMERS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
     public @ResponseBody void createCustomer(HttpServletRequest request, HttpServletResponse response, 
     		@RequestPart(value = "icon", required = false) ByteArrayResource moduleFile,
     		@RequestParam(value = "customer", required = false) byte[] customerData) throws IOException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.createCustomer(List<Customer> customer)");
         }
         Customer customer = new Gson().fromJson(new String(customerData), Customer.class);
         saveCustomer(response, moduleFile.getByteArray(), customer);
     }
 
     private Customer saveCustomer(HttpServletResponse response, byte[] iconStream, Customer customer) {
     	try {
     		if(validate(customer)) {
 				RepoInfo repoInfo = customer.getRepoInfo();
     			String repoName = repoInfo.getRepoName();
     			if(StringUtils.isEmpty(repoInfo.getReleaseRepoURL())) {
     				repoInfo = repositoryManager.createCustomerRepository(customer.getId(), repoName);
     				customer.setRepoInfo(repoInfo);
     			}
     			if(iconStream != null) {
     				ArtifactGroup artifactGroup = new ArtifactGroup();
     				artifactGroup.setGroupId("customers");
     				artifactGroup.setArtifactId(filterString(customer.getName()));
     				artifactGroup.setPackaging("png");
     				artifactGroup.setCustomerIds(Collections.singletonList(DEFAULT_CUSTOMER_NAME));
     				ArtifactInfo info = new ArtifactInfo();
     				info.setVersion("1.0");
     				artifactGroup.setVersions(Collections.singletonList(info));
     				File artifcatFile = new File(ServerUtil.getTempFolderPath() + "/"
     		                + customer.getName() + "." + "png");
     				ServerUtil.convertByteArrayToFile(artifcatFile, iconStream);
     				uploadIcon(artifactGroup, artifcatFile);
     			}
     			Converter<CustomerDAO, Customer> customerConverter = 
         			(Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
     			CustomerDAO customerDAO = customerConverter.convertObjectToDAO(customer);
 		        DbService.getMongoOperation().save(CUSTOMERDAO_COLLECTION_NAME, customerDAO);
 		        DbService.getMongoOperation().save(REPOINFO_COLLECTION_NAME, customer.getRepoInfo());
 		        List<TechnologyDAO> techDAOs = DbService.getMongoOperation().find(TECHNOLOGIES_COLLECTION_NAME, 
 		        		new Query(Criteria.whereId().in(customerDAO.getApplicableTechnologies().toArray())), TechnologyDAO.class);
 		        if(CollectionUtils.isNotEmpty(techDAOs)) {
 		        	for (TechnologyDAO technologyDAO : techDAOs) {
 						List<String> customerIds = technologyDAO.getCustomerIds();
 						customerIds.add(customerDAO.getId());
 						technologyDAO.setCustomerIds(customerIds);
 						DbService.getMongoOperation().save(TECHNOLOGIES_COLLECTION_NAME, technologyDAO);
 						
 						TechnologyGroup tg = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 								new Query(Criteria.whereId().is(technologyDAO.getTechGroupId())), TechnologyGroup.class);
 						customerIds = tg.getCustomerIds();
 						customerIds.add(customerDAO.getId());
 						tg.setCustomerIds(customerIds);
 						DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, tg);
 						
 						TechnologyInfo techInfo = DbService.getMongoOperation().findOne("techInfos", 
 								new Query(Criteria.whereId().is(technologyDAO.getId())), TechnologyInfo.class);
 						customerIds = techInfo.getCustomerIds();
 						customerIds.add(customerDAO.getId());
 						techInfo.setCustomerIds(customerIds);
 						DbService.getMongoOperation().save("techInfos", techInfo);
 					}
 		        }
 			}	
     	} catch (Exception e) {
     		e.printStackTrace();
     		response.setStatus(500);
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		return customer;
     }
     
     /**
      * Updates the list of customers
      * @param customers
      * @return
      * @throws IOException 
      */
     @ApiOperation(value = " Creates a new customer ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_CUSTOMERS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
     public @ResponseBody void updateCustomer(HttpServletResponse response, @RequestPart(value = "icon", required = false) ByteArrayResource moduleFile,
     		@RequestParam(value = "customer", required = false) byte[] customerData) throws IOException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateCustomer(List<Customer> customers)");
         }
         System.out.println("Entyerd To Service ........ ");
         Customer customer = new Gson().fromJson(new String(customerData), Customer.class);
         saveCustomer(response, moduleFile.getByteArray(), customer);
     }
 
     /**
      * Deletes the list of customers
      * @param deleteCustomers
      * @throws PhrescoException 
      */
     @ApiOperation(value = " Deletes the customer ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_CUSTOMERS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
     public @ResponseBody void deleteCustomer(HttpServletResponse response, @ApiParam(required = true, name = "customers", 
     		value = "The customer to delete")@RequestBody List<Customer> customers) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteCustomer(List<Customer> deleteCustomers)");
         }
         response.setStatus(500);
     	PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
     	S_LOGGER.error(exceptionString + phrescoException.getErrorMessage());
     	throw phrescoException;
     }
 
     /**
      * Get the customer by id for the given parameter
      * @param id
      * @return
      */
     @ApiOperation(value = " Retrieves a customer based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"),@ApiError(code=505, reason = "Customer not found")})
     @RequestMapping(value= REST_API_CUSTOMERS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody Customer getCustomer(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
     		value = "The id of the customer that needs to be retrieved")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getCustomer(String id)" + id);
         }
         Customer customer = null;
     	try {
     		CustomerDAO customerDAO= DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
     		        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), CustomerDAO.class);
     		if(customerDAO == null) {
     			response.setStatus(204);
     			return customer;
     		}
 			Converter<CustomerDAO, Customer> customerConverter = 
     			(Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
 			customer = customerConverter.convertDAOToObject(customerDAO, DbService.getMongoOperation());
 			response.setStatus(200);
 			return customer;
     	} catch (Exception e) {
     		response.setStatus(500);
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMERS_COLLECTION_NAME);
     	}
     }
     
     /**
      * Updates the list of customers by Id
      * @param customer
      * @return
      */
     @ApiOperation(value = " Updates a customer based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_CUSTOMERS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
     public @ResponseBody void updateCustomer(HttpServletResponse  response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
     		value = "The id of the customer that needs to be retrieved")@PathVariable(REST_API_PATH_PARAM_ID) String id, 
     		@ApiParam(required = true, name = "customer", value = "The customer data to be update")@RequestBody Customer customer) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateCustomer(String id , Customer updateCustomers)" + id);
         }
     	try {
 			Converter<CustomerDAO, Customer> customerConverter = 
     			(Converter<CustomerDAO, Customer>) ConvertersFactory.getConverter(CustomerDAO.class);
 			CustomerDAO customerDao = customerConverter.convertObjectToDAO(customer);
     		DbService.getMongoOperation().save(CUSTOMERS_COLLECTION_NAME, customerDao);
     		response.setStatus(200);
     	} catch (Exception e) {
     		response.setStatus(500);
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
     }
     
     /**
      * Deletes the customer by id for the given parameter
      * @param id
      * @return 
      */
     @ApiOperation(value = " Deletes a customer based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_CUSTOMERS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
     public @ResponseBody void deleteCustomer(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
     		value = "The id of the customer that needs to delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteCustomer(String id)" + id);
         }
     	try {
     		DbService.getMongoOperation().remove(CUSTOMERS_COLLECTION_NAME, 
     				new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), CustomerDAO.class);
     		response.setStatus(200);
     	} catch (Exception e) {
     		response.setStatus(500);
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
     	}
     }
 
 	
 	/**
 	 * Returns the list of videos
 	 * @return
 	 */
     @ApiOperation(value = " Lists all the videos ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=205, reason = "Videos not found")})
     @RequestMapping(value= REST_API_VIDEOS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<VideoInfo> findVideos(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findVideos()");
 	    }
 		List<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
 		try {
 			List<VideoInfoDAO> videoList = DbService.getMongoOperation().getCollection(VIDEODAO_COLLECTION_NAME , VideoInfoDAO.class);
 			if (videoList != null) {
 				Converter<VideoInfoDAO, VideoInfo> videoInfoConverter =  (Converter<VideoInfoDAO, VideoInfo>) ConvertersFactory.getConverter(VideoInfoDAO.class);
 				for (VideoInfoDAO videoInfoDAO : videoList) {
 					VideoInfo videoInfo = videoInfoConverter.convertDAOToObject(videoInfoDAO, DbService.getMongoOperation());
 					videoInfos.add(videoInfo);
 				}
 			}
 			if(CollectionUtils.isEmpty(videoInfos)) {
 				response.setStatus(204);
 				return videoInfos;
 			}
 			response.setStatus(200);
 			return videoInfos;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, VIDEOS_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of videos
 	 * @param videos
 	 * @return 
 	 * @throws PhrescoException 
 	 */
     @ApiOperation(value = " Creates a new video ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_VIDEOS, consumes = MultiPartMediaTypes.MULTIPART_MIXED, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createVideo(@ApiParam(required = true, name = "videos", 
     		value = "The multipart value of video to create")@RequestBody MultiPart videos) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createVideo(List<VideoInfo> videos)");
 	    }
 	    createOrUpdateVideo(videos);
 	}
 
 	private void createOrUpdateVideo(MultiPart videosinfo)
 			throws PhrescoException {
 		VideoInfo video = null;
 		BodyPartEntity videoBodyPartEntity = null;
 		BodyPartEntity iconBodyPartEntity = null;
 		File videoFile = null;
 		List<BodyPart> bodyParts = videosinfo.getBodyParts();
 		File iconFile = null;
 		
 		if (CollectionUtils.isNotEmpty(bodyParts)) {
 			for (BodyPart bodyPart : bodyParts) {
 				if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_VALUE)) {
 					video = bodyPart.getEntityAs(VideoInfo.class);
 					video.setCustomerIds(Arrays.asList(DEFAULT_CUSTOMER_NAME));
 				} else {
 					if(bodyPart.getContentDisposition().getFileName().equals(Type.ICON.name())) {
 						iconBodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 					} else {
 						videoBodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 					}
 				}
 			}
 		}
 
 		String groupId = "videos.homepage";
 		String version = "1.0";
 		String artifactId = video.getName().toLowerCase().replace(" ", "");
 		video.setImageurl("/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version
 				+ "/" + artifactId + "-" + version + "." + "png");
 		
 		if(videoBodyPartEntity != null && iconBodyPartEntity != null) {
 			videoFile = ServerUtil.writeFileFromStream(videoBodyPartEntity.getInputStream(), null, 
 					video.getVideoList().get(0).getArtifactGroup().getPackaging(), video.getName());
 			iconFile = ServerUtil.writeFileFromStream(iconBodyPartEntity.getInputStream(), null, 
 					"png", video.getName());
 			List<VideoType> videoTypeList = video.getVideoList();
 			for (VideoType videoType : videoTypeList) {
 					ArtifactGroup artifactGroup = videoType.getArtifactGroup();
 					videoType.setUrl("/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version
 					+ "/" + artifactId + "-" + version + "." + artifactGroup.getPackaging());
 					videoType.setType(artifactGroup.getPackaging());
 					artifactGroup.setGroupId(groupId);
 					artifactGroup.setArtifactId(artifactId);
 					com.photon.phresco.commons.model.ArtifactInfo info = new com.photon.phresco.commons.model.ArtifactInfo();
 					info.setVersion(version);
 					artifactGroup.setVersions(Arrays.asList(info));
 					artifactGroup.setCustomerIds(Arrays.asList(DEFAULT_CUSTOMER_NAME));
 				if (artifactGroup != null) {
 					boolean uploadBinary = uploadBinary(artifactGroup, videoFile);
 					uploadIcon(artifactGroup, iconFile);
 					if (uploadBinary) {
 						saveVideos(video);
 					}
 				}
 			}
 		}
 		
 		if(videoBodyPartEntity == null && video != null) {
 			List<VideoType> videoTypeList = video.getVideoList();
 			for (VideoType videoType : videoTypeList) {
 				ArtifactGroup artifactGroup = videoType.getArtifactGroup();
 				artifactGroup.setGroupId("videos.homepage");
 				artifactGroup.setArtifactId(video.getName().toLowerCase());
 				com.photon.phresco.commons.model.ArtifactInfo info = new com.photon.phresco.commons.model.ArtifactInfo();
 				info.setVersion("1.0");
 				artifactGroup.setVersions(Arrays.asList(info));
 				artifactGroup.setCustomerIds(Arrays.asList(DEFAULT_CUSTOMER_NAME));
 				saveVideos(video);
 			}
 		}
 	}
 	
 	private boolean uploadIcon(ArtifactGroup artifactGroup, File iconFile) throws PhrescoException {
 		ArtifactGroup iconGroup = artifactGroup;
 		iconGroup.setPackaging("png");
 		return uploadBinary(iconGroup, iconFile);
 	}
 
 	private void saveVideos(VideoInfo video) throws PhrescoException {
 		if(!validate(video)) {
 			return;
 		}
 		Converter<VideoInfoDAO, VideoInfo> converter = 
 	            (Converter<VideoInfoDAO, VideoInfo>) ConvertersFactory.getConverter(VideoInfoDAO.class);
 		VideoInfoDAO videoDAO = converter.convertObjectToDAO(video);
 		DbService.getMongoOperation().save(VIDEODAO_COLLECTION_NAME, videoDAO);
 		List<VideoType> videoList = video.getVideoList();
 		Converter<VideoTypeDAO, VideoType> videoTypeconverter = 
             (Converter<VideoTypeDAO, VideoType>) ConvertersFactory.getConverter(VideoTypeDAO.class);
 		Converter<ArtifactGroupDAO, ArtifactGroup> agConverter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		for (VideoType videoType : videoList) {
 			DbService.getMongoOperation().save(VIDEOTYPESDAO_COLLECTION_NAME, videoTypeconverter.convertObjectToDAO(videoType));
 			
 			saveModuleGroup(videoType.getArtifactGroup());
 		}
 	}
 	
 	
 	private void saveModuleGroup(ArtifactGroup moduleGroup) throws PhrescoException {
         Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         ArtifactGroupDAO moduleGroupDAO = converter.convertObjectToDAO(moduleGroup);
 //        List<com.photon.phresco.commons.model.ArtifactInfo> moduleGroupVersions = moduleGroup.getVersions();
         List<String> versionIds = new ArrayList<String>();
         
         ArtifactGroupDAO moduleDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 		        new Query(Criteria.where(REST_API_NAME).is(moduleGroupDAO.getName())), ArtifactGroupDAO.class);
         
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
         }  else {
         		versionIds.add(newVersion.getId());
         		newVersion.setArtifactGroupId(moduleGroupDAO.getId());
                 DbService.getMongoOperation().save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }
         moduleGroupDAO.setVersionIds(versionIds);
         DbService.getMongoOperation().save(ARTIFACT_GROUP_COLLECTION_NAME, moduleGroupDAO);
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
 	 * Updates the list of Videos
 	 * @param videos
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Update a video ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_VIDEOS, consumes = MultiPartMediaTypes.MULTIPART_MIXED, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateVideos(@ApiParam(required = true, name = "videos", 
     		value = "The multipart value of video to be update")@RequestBody MultiPart videos) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateVideos(List<VideoInfo> videos)");
 	    }
 		createOrUpdateVideo(videos);
 	}
 
 	/**
 	 * Deletes the list of Videos
 	 * @param videos
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes a list of videos ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_VIDEOS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteVideos(HttpServletResponse response, @ApiParam(required = true, name = "videos", 
     		value = "Videos to be delete")@RequestBody List<VideoInfo> videos) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteVideos(List<VideoInfo> videos)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the videos by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrieves a video based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"),@ApiError(code=205, reason = "Video not found")})
     @RequestMapping(value= REST_API_VIDEOS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody VideoInfo getVideo(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
     		value = "The id of the video that needs to retrived")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.getVideo(String id)" + id);
 	    }
 	    VideoInfo videoInfo = null;
 		try {
 			VideoInfoDAO videoInfoDAO = DbService.getMongoOperation().findOne(VIDEODAO_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), VideoInfoDAO.class);
 			if (videoInfoDAO != null) {
 				Converter<VideoInfoDAO, VideoInfo> videoInfoConverter = 
 						(Converter<VideoInfoDAO, VideoInfo>) ConvertersFactory.getConverter(VideoInfoDAO.class);
 				videoInfo = videoInfoConverter.convertDAOToObject(videoInfoDAO, DbService.getMongoOperation());
 			}
 			if(videoInfo == null) {
 				response.setStatus(204);
 				return videoInfo;
 			}
 			response.setStatus(200);
 			return videoInfo;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, VIDEODAO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the list of video bu Id
 	 * @param multipartVideo
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Updates a video based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_VIDEOS + REST_API_PATH_ID, consumes = MultiPartMediaTypes.MULTIPART_MIXED ,
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateVideo(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
     		value = "The id of the video that needs to update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
     		@ApiParam(required = true, name = "multipartVideo", value = "The multipart value of video")@RequestBody MultiPart multipartVideo) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateVideo(String id , VideoInfo videoInfo)" + id);
 	    }
 		createOrUpdateVideo(multipartVideo);
 	}
 
 	
 	/**
 	 * Deletes the Video by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Deletes a video based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_VIDEOS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteVideo(HttpServletResponse response,
 			@ApiParam(name = REST_API_PATH_PARAM_ID , required = true, value = "The id of the video that needs to delete")
 			@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteVideo(String id)" + id);
 	    }
 		try {
 			VideoInfoDAO videoInfoDAO = DbService.getMongoOperation().findOne(VIDEODAO_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(id)), VideoInfoDAO.class);
 			if(videoInfoDAO != null) {
 				DbService.getMongoOperation().remove(VIDEODAO_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), VideoInfoDAO.class);
 				List<VideoTypeDAO> videotypeDAOs = DbService.getMongoOperation().find(VIDEODAO_COLLECTION_NAME, 
 						new Query(Criteria.whereId().in(videoInfoDAO.getVideoListId().toArray())), VideoTypeDAO.class);
 				if(CollectionUtils.isNotEmpty(videotypeDAOs)) {
 					for (VideoTypeDAO videoTypeDAO : videotypeDAOs) {
 						ArtifactGroupDAO artifactGroupDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(videoTypeDAO.getArtifactGroupId())), 
 								ArtifactGroupDAO.class);
 						if(artifactGroupDAO != null) {
 							DbService.getMongoOperation().remove(ARTIFACT_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(videoTypeDAO.getArtifactGroupId())), 
 									ArtifactGroupDAO.class);
 							DbService.getMongoOperation().remove(ARTIFACT_INFO_COLLECTION_NAME, 
 									new Query(Criteria.whereId().in(artifactGroupDAO.getVersionIds().toArray())), com.photon.phresco.commons.model.ArtifactInfo.class);
 						}
 					}
 					DbService.getMongoOperation().remove(VIDEOTYPESDAO_COLLECTION_NAME, 
 							new Query(Criteria.whereId().in(videoInfoDAO.getVideoListId().toArray())), VideoTypeDAO.class);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 	}
 	
 	/**
 	 * Returns the list of users
 	 * @return
 	 */
 	@ApiOperation(value = " Lists all the Users ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Users not found")})
     @RequestMapping(value= REST_API_USERS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<User> findUsers(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findUsers()");
 	    }
 		try {
 			List<User> userList = DbService.getMongoOperation().getCollection(USERS_COLLECTION_NAME, User.class);
 			if (userList.isEmpty()) {
 				response.setStatus(204);
 				return userList;
 			}
 			response.setStatus(200);
 			return userList;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, USERS_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of users
 	 * @param users
 	 * @return 
 	 */
 	@ApiOperation(value = " Create a new User ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_USERS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createUser(HttpServletResponse response, @ApiParam(required = true, name = "users", 
 			value = "List of users to create")@RequestBody List<User> users) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createUser(List<User> users)");
 	    }
 		try {
 			for (User user : users) {
 				if(validate(user)) {
 					user.setPhrescoEnabled(true);
 					user.setAuthType(AuthType.LOCAL);
 					user.setPassword(ServerUtil.encodeUsingHash(user.getName(),user.getPassword()));
 					DbService.getMongoOperation().save(USERS_COLLECTION_NAME, user);
 				}
 			}
 		response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	/**
 	 * Returns List Of users form LDAP
 	 * @param users
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Sync all the users from LDAP to database ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Users not found")})
     @RequestMapping(value= REST_API_USERS_IMPORT, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody List<User> importUsers(HttpServletResponse response, @ApiParam(required = true, name = "user", 
 			value = "The Logged User Data")@RequestBody User user) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createUser(List<User> users)");
 	    }
 	    
 	    PhrescoServerFactory.initialize();
         RepositoryManager repoMgr = PhrescoServerFactory.getRepositoryManager();
     	Client client = ClientHelper.createClient();
         WebResource resource = client.resource(repoMgr.getAuthServiceURL() + "/ldap/import");
         resource.accept(MediaType.APPLICATION_JSON_VALUE);
         ClientResponse clientResponse = resource.type(MediaType.APPLICATION_JSON_VALUE).post(ClientResponse.class, user);
         GenericType<List<User>> genericType = new GenericType<List<User>>() {};
         List<User> users = clientResponse.getEntity(genericType);
         //To save the users into user table
         if(CollectionUtils.isEmpty(users)) {
         	response.setStatus(204);
         	return users;
         }
         DbService.getMongoOperation().insertList(USERS_COLLECTION_NAME, users);
         response.setStatus(200);
         return users;
 	}
 
 	
 	/**
 	 * Updates the list of Users
 	 * @param users
 	 * @return
 	 */
 	@ApiOperation(value = "Update user ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_USERS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateUsers(HttpServletResponse response, @ApiParam(required = true, name = "users", 
 			value = "The user data to update ")@RequestBody List<User> users) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateUsers(List<User> users)");
 	    }
 		try {
 			for (User user : users) {
 				User userInfo = DbService.getMongoOperation().findOne(USERS_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(user.getId())), User.class);
 				if (userInfo  != null) {
 					DbService.getMongoOperation().save(USERS_COLLECTION_NAME, user);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 
 	/**
 	 * Deletes the list of Users
 	 * @param users
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = "Delete list of users")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupportd operation")})
     @RequestMapping(value= REST_API_USERS, method = RequestMethod.DELETE)
 	public void deleteUsers(HttpServletResponse response, @ApiParam(required = true, name = "users", 
 			value = "The list of users to delete ")@RequestBody List<User> users) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteUsers(List<User> users)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the user by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = "Retrieves a user based on their id")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "User not found")})
     @RequestMapping(value= REST_API_USERS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody User getUser(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the user that needs to be retrieved")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.getUser(String id)" + id);
 	    }
 		try {
 			User userInfo = DbService.getMongoOperation().findOne(USERS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), User.class);
 			if (userInfo == null) {
 				response.setStatus(204);
 				return userInfo;
 			} 
 			response.setStatus(200);
 			return userInfo;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, USERS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the list of User by id
 	 * @param users
 	 * @return
 	 */
 	@ApiOperation(value = "Updates a user based on their id")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_USERS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateUser(HttpServletResponse response, @ApiParam(name = "Id" , required = true, 
 			value = "The id of the user that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "user" , required = true, value = "The user that needs to be update") @RequestBody User user) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateUser(String id , User user)" + id);
 	    }
 		try {
 			if (id.equals(user.getId())) {
 				DbService.getMongoOperation().save(USERS_COLLECTION_NAME, user);
 			} 
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the user by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = "Deletes a user based on their id")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_USERS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteUser(HttpServletResponse response,
 			@ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the user that needs to be delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteUser(String id)" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(USERS_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), User.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 	}
 	
 	/**
 	 * Returns the Roles
 	 * @return
 	 */
 	@ApiOperation(value = "Retrives all roles and roles by appliesto")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Roles not found")})
     @RequestMapping(value= REST_API_ROLES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<Role> findRoles(HttpServletResponse response, @ApiParam(name = "applyTo" , 
 			value = "Applies to framework or server") @QueryParam(REST_QUERY_APPLIESTO) String applyTo) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findRoles()");
 	    }
 		List<Role> roles = new ArrayList<Role>();
 		try {
 			if(StringUtils.isNotEmpty(applyTo)) {
 				roles = DbService.getMongoOperation().find(ROLES_COLLECTION_NAME, 
 						new Query(Criteria.where("appliesTo").is(applyTo)), Role.class);
 				if(CollectionUtils.isEmpty(roles)) {
 					response.setStatus(204);
 				}
 				return roles;
 			}
 			roles = DbService.getMongoOperation().getCollection(ROLES_COLLECTION_NAME , Role.class);
 			if (CollectionUtils.isEmpty(roles)) {
 				response.setStatus(204);
 				return roles;
 			} 
 			return roles;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, ROLES_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of Roles
 	 * @param roles
 	 * @return 
 	 */
 	@ApiOperation(value = "Create a list of roles")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_ROLES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createRoles(HttpServletResponse response, @ApiParam(name = "roles" , required = true, 
 			value = "List of roles to create") @RequestBody List<Role> roles) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createRoles(List<Role> roles)");
 	    }
 		try {
 			for (Role role : roles) {
 				if(validate(role)) {
 					DbService.getMongoOperation().save(ROLES_COLLECTION_NAME , role);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 
 	/**
 	 * Updates the list of Roles
 	 * @param roles
 	 * @return
 	 */
 	@ApiOperation(value = "Update a list of roles")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_ROLES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateRoles(HttpServletResponse response, @ApiParam(name = "roles" , required = true, 
 			value = "List of roles to update") @RequestBody List<Role> roles) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateRoles(List<Role> roles)");
 	    }
 		try {
 			for (Role role : roles) {
 				Role roleInfo = DbService.getMongoOperation().findOne(ROLES_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(role.getId())), Role.class);
 				if (roleInfo  != null) {
 					DbService.getMongoOperation().save(ROLES_COLLECTION_NAME, role);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 
 	/**
 	 * Deletes the list of Roles
 	 * @param roles
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = "Delete a list of roles")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_ROLES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteRoles(HttpServletResponse response, @ApiParam(name = "roles" , required = true, 
 			value = "List of roles to delete") @RequestBody List<Role> roles) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteRoles(List<Role> roles)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error(exceptionString  + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the Role by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = "Retrives a role by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Roles not found")})
     @RequestMapping(value= REST_API_ROLES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody Role getRole(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the role that needs to be retrieved")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.Response getRole(String id)" + id);
 	    }
 		try {
 			Role role = DbService.getMongoOperation().findOne(ROLES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Role.class);
 			if (role == null) {
 				response.setStatus(204);
 				return role;
 			} 
 			response.setStatus(200);
 			return role;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ROLES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the list of role by id
 	 * @param role
 	 * @return
 	 */
 	@ApiOperation(value = "Updates a role by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_ROLES + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateRole(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the role that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id ,
 			@ApiParam(name = "role" , required = true, value = "The role to be update")@RequestBody Role role) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateRole(String id , Role role)" + id);
 	    }
 		try {
 			if (id.equals(role.getId())) {
 				DbService.getMongoOperation().save(ROLES_COLLECTION_NAME, role);
 			} 
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the role by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = "Deletes a role by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_ROLES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteRole(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the role that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteRole(String id)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().remove(ROLES_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Role.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 	}
 	
 
 	/**
 	 * Returns the Permisions
 	 * @return
 	 */
 	@ApiOperation(value = "Retrives all permissions and permissions by appliesto")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Permissions not found")})
     @RequestMapping(value= REST_API_PERMISSIONS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<Permission> findPermissions(HttpServletResponse response, @ApiParam(name = "applyTo" , 
 			value = "Appliesto framework or service")@QueryParam(REST_QUERY_APPLIESTO) String applyTo) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findPermissions()");
 	    }
 		List<Permission> permissions = new ArrayList<Permission>();
 		try {
 			if(StringUtils.isNotBlank(applyTo)) {
 				permissions = DbService.getMongoOperation().find(PERMISSION_COLLECTION_NAME, new Query(Criteria.where("appliesTo").is(applyTo)), Permission.class);
 				if(CollectionUtils.isEmpty(permissions)) {
 					response.setStatus(204);
 				}
 				return permissions;
 			}
 			permissions = DbService.getMongoOperation().getCollection(PERMISSION_COLLECTION_NAME , Permission.class);
 			if (permissions == null) {
 				response.setStatus(204);
 			} 
 			return permissions;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, PERMISSION_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of permissions
 	 * @param permission
 	 * @return 
 	 */
 	@ApiOperation(value = "Creates a list of permissions")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_PERMISSIONS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createPermission(HttpServletResponse response, @ApiParam(name = "permissions" , required = true, 
 			value = "List of permissions to create")@RequestBody List<Permission> permissions) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createPermission(List<Permission> permissions)");
 	    }
 		try {
 			for (Permission permission : permissions) {
 				if(validate(permission)) {
 					DbService.getMongoOperation().save(PERMISSION_COLLECTION_NAME , permission);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 	}
 
 	/**
 	 * Updates the list of permissions
 	 * @param permission
 	 * @return
 	 */
 	@ApiOperation(value = "Updates a list of permissions")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_PERMISSIONS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody Response updatePermissions(HttpServletResponse response, @ApiParam(name = "permissions" , required = true, 
 			value = "List of permissions to update")@RequestBody List<Permission> permissions) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updatePermissions(List<Permission> permissions)");
 	    }
 		
 		try {
 			for (Permission permission : permissions) {
 				Permission permisonInfo = DbService.getMongoOperation().findOne(PERMISSION_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(permission.getId())), Permission.class);
 				if (permisonInfo  != null) {
 					DbService.getMongoOperation().save(PERMISSION_COLLECTION_NAME, permission);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(permissions).build();
 	}
 
 	/**
 	 * Deletes the list of permissions
 	 * @param permission
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = "Deletes a list of permissions")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_PERMISSIONS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deletePermissions(HttpServletResponse response, @ApiParam(name = "permissions" , required = true, 
 			value = "List of permissions to delete")@RequestBody List<Permission> permissions) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deletePermissions(List<Permission> permissions)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error(exceptionString  + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the Role by id for the given parameter
 	 * @param id
 	 * @return 
 	 * @return
 	 */
 	@ApiOperation(value = "Retrives a permission by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Permission not found")})
     @RequestMapping(value= REST_API_PERMISSIONS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody Permission getPermission(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the permission that needs to be retrieved")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.Response getPermission(String id)" + id);
 	    }
 		try {
 			Permission permission = DbService.getMongoOperation().findOne(PERMISSION_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Permission.class);
 			if (permission == null) {
 				response.setStatus(204);
 				return permission;
 			}
 			response.setStatus(200);
 			return permission;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, PERMISSION_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the list of permissions by id
 	 * @param permission
 	 * @return
 	 */
 	@ApiOperation(value = "Updates a permission by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_PERMISSIONS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updatePermission(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The id of the permission that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "Permission" , required = true, value = "The permission that needs to be update")Permission permission) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updatePermission(String id , Permission permission)" + id);
 	    }
 		try {
 			if (id.equals(permission.getId())) {
 				DbService.getMongoOperation().save(PERMISSION_COLLECTION_NAME, permission);
 			} 
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the permission by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = "Deletes a permission by their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_PERMISSIONS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deletePermission(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , required = true, 
 			value = "The permission that needs to be delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deletePermission(String id)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().remove(PERMISSION_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Permission.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 	}
 	
 	/**
      * Creating the jforum 
      * @param id
      * @return
      */
 	@ApiOperation(value = "Create a list if properties")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_FORUMS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
     public @ResponseBody void createForum(HttpServletResponse response, @ApiParam(name = "properties" , required = true, 
 			value = "The list of properties that needs to be create")@RequestBody List<Property> properties) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createForum(List<AdminConfigInfo> adminConfigInfo)");
 	    }
 		try {
 			for (Property property : properties) {
 				if(validate(property)) {
 					DbService.getMongoOperation().save(FORUM_COLLECTION_NAME , property);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	 /**
      * Get the customer by id for the given parameter
      * @param id
      * @return
      */
 	@ApiOperation(value = "Retrives the properties by customerId")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Property not found")})
     @RequestMapping(value= REST_API_FORUMS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody Property getForum(HttpServletResponse response, @ApiParam(name = REST_QUERY_CUSTOMERID , required = true, 
 			value = "The customerId to retrive properties") @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getForum(String id)");
         }
     	try {
     		Property adminConfig = DbService.getMongoOperation().findOne(FORUM_COLLECTION_NAME, 
     		        new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), Property.class);
     		if (adminConfig == null) {
     			response.setStatus(204);
     			return adminConfig;
     		}
     		response.setStatus(200);
 			return adminConfig;
     	} catch (Exception e) {
     		response.setStatus(500);
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, FORUM_COLLECTION_NAME);
     	}
     }
     
     /**
 	 * Creates the list of LogInfo
 	 * @param logInfo
 	 * @return 
 	 */
 	@ApiOperation(value = "Creates the list of logs")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_LOG, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createLog(HttpServletResponse response, @ApiParam(name = "logs" , required = true, 
 			value = "The list of loginfos to create")@RequestBody List<LogInfo> logs) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createLog(List<LogInfo> logInfos)");
 	    }
 		
 		try {
 			for (LogInfo logInfo : logs) {
 				DbService.getMongoOperation().save(LOG_COLLECTION_NAME, logInfo);
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
  }
