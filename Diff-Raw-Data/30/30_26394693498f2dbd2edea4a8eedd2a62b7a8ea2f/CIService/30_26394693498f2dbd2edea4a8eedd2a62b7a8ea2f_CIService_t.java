 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.CIBuild;
 import com.photon.phresco.commons.model.CIJob;
 import com.photon.phresco.commons.model.CIJobStatus;
 import com.photon.phresco.commons.model.ContinuousDelivery;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.ProjectDelivery;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.CIManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.impl.CIManagerImpl;
 import com.photon.phresco.framework.model.RepoDetail;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class CIService.
  */
 
 @Path("/ci")
 public class CIService extends RestBase implements FrameworkConstants, ServiceConstants, Constants, ResponseCodes {
 
 	private ServiceManager serviceManager = null;
 	
 	/**
 	 * @param projectId
 	 * @param appDir
 	 * @param jobName
 	 * @param continuousName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/builds")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getBuilds(@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir,
 			@QueryParam(REST_QUERY_NAME) String jobName, @QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName) throws PhrescoException {
 		ResponseInfo<List<CIBuild>> responseData = new ResponseInfo<List<CIBuild>>();
 		List<CIBuild> builds = null;
 		CIJob job = null;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			List<CIJob> ciJobs = Utility.getJobs(continuousName, projectId, ciJobInfo);
 			for (CIJob ciJob : ciJobs) {
 				if(ciJob.getJobName().equals(jobName)) {
 					job = ciJob;
 				}
 			}
 			builds = ciManager.getBuilds(job);
 			ResponseInfo<List<CIBuild>> finalOutput = responseDataEvaluation(responseData, null, builds, RESPONSE_STATUS_SUCCESS, PHR800001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<CIBuild>> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810001);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 
 	/**
 	 * @param continuousDelivery
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@POST
 	@Path("/create")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response createJob(@Context HttpServletRequest request, ContinuousDelivery continuousDelivery, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, @QueryParam(REST_QUERY_USERID) String userId)
 	throws PhrescoException {
 		ResponseInfo<ContinuousDelivery> responseData = new ResponseInfo<ContinuousDelivery>();
 		ResponseInfo<ContinuousDelivery> finalOutput;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			List<CIJob> jobs = continuousDelivery.getJobs();
 			//boolean validate = jobNameValidation(jobs);
 			boolean coreCreateJob = coreCreateJob(continuousDelivery, projectId, appDir, userId, customerId, request);
 			if (coreCreateJob) {
 				finalOutput = responseDataEvaluation(responseData, null, continuousDelivery, RESPONSE_STATUS_SUCCESS, PHR800002);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, continuousDelivery, RESPONSE_STATUS_FAILURE, PHR810003);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810003);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 	
 	@GET
 	@Path("/validation")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response jobNameValidataion(@QueryParam(REST_QUERY_NAME) String jobName, @QueryParam("flag") String flag, @QueryParam(REST_QUERY_USERID) String userId)
 					throws PhrescoException, JSONException, ClientProtocolException, IOException {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		boolean hasTrue = true;
		String name = null;
 		try {
 			if(!flag.equalsIgnoreCase("update")) {
 				JSONArray jobs = new JSONArray("["+jobName+"]");
 				InetAddress thisIp = InetAddress.getLocalHost();
 				String port = FrameworkServiceUtil.getJenkinsPortNo();
 				String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + thisIp.getHostAddress() + ":" + port + "/" + CI + "/api/json";
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpGet httpget = new HttpGet(jenkinsUrl);
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				String execute = httpClient.execute(httpget, responseHandler);
 				
 				JSONArray jsonArray = new JSONArray("["+execute.toString()+"]");
 			    JSONObject jsonObject = jsonArray.getJSONObject(0);
 			    String json = jsonObject.getString("jobs");
 			    JSONArray jobJsonArray = new JSONArray(json);
 			    for (int k = 0; k<jobs.length(); k++) {
 				    for (int j = 0; j<jobJsonArray.length(); j++) {
 				    	 JSONObject jsonObject1 = jobJsonArray.getJSONObject(j);
 				    	 if(jobs.get(k).toString().equals(jsonObject1.getString("name"))) {
 				    		 hasTrue = false;
				    		 name = jobs.get(k).toString();
 				    		 break;
 				    	 }
 				    }
 				}
 			}
 		    if(hasTrue) {
 				ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null, hasTrue, RESPONSE_STATUS_SUCCESS, PHR800003);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		    } else {
		    	ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null, name, RESPONSE_STATUS_FAILURE, PHR810031);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		    }
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810031);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 
 	/**
 	 * @param cloneName
 	 * @param envName
 	 * @param continuousName
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@POST
 	@Path("/clone")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response createClone(@Context HttpServletRequest request, @QueryParam(REST_QUERY_CLONE_NAME) String cloneName, @QueryParam(REST_QUERY_ENV_NAME) String envName,
 			@QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, @QueryParam(REST_QUERY_USERID) String userId)
 	throws PhrescoException {
 		ResponseInfo<ContinuousDelivery> responseData = new ResponseInfo<ContinuousDelivery>();
 		ResponseInfo<ContinuousDelivery> finalOutput;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			String trimmedName = continuousName.trim();
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ContinuousDelivery continuousDelivery = new ContinuousDelivery();
 
 			List<CIJob> ciJobs = new ArrayList<CIJob>(); 
 			List<CIJob> jobs = new ArrayList<CIJob>();
 
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			ContinuousDelivery specificContinuousDelivery = Utility.getContinuousDelivery(projectId, trimmedName, ciJobInfo);
 			if(specificContinuousDelivery.getName().equals(trimmedName)) {
 				continuousDelivery.setName(cloneName);
 				continuousDelivery.setEnvName(envName);
 				jobs = specificContinuousDelivery.getJobs();
 			}
 
 			for(CIJob cijob: jobs) {
 				cijob.setJobName(cijob.getJobName()+"-"+envName);
 				cijob.setEnvironmentName(envName);
 				if(StringUtils.isNotEmpty(cijob.getDownstreamApplication())) {
 					cijob.setDownstreamApplication(cijob.getDownstreamApplication()+"-"+envName);
 				}
 				if(StringUtils.isNotEmpty(cijob.getUpstreamApplication())) {
 					cijob.setUsedClonnedWorkspace(cijob.getUpstreamApplication()+"-"+envName);
 					cijob.setUpstreamApplication(cijob.getUpstreamApplication()+"-"+envName);
 				}
 
 				ciJobs.add(cijob);
 			}
 			continuousDelivery.setJobs(ciJobs);
 			boolean coreCreateJob = coreCreateJob(continuousDelivery, projectId, appDir, userId, customerId, request);
 
 			if (coreCreateJob) {
 				finalOutput = responseDataEvaluation(responseData, null, continuousDelivery, RESPONSE_STATUS_SUCCESS, PHR800002);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_FAILURE, PHR810003);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810003);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		}
 	}
 	
 	/**
 	 * @param continuousDelivery
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 * @throws JSONException 
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 */
 	@PUT
 	@Path("/update")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateJob(@Context HttpServletRequest request, ContinuousDelivery continuousDelivery, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, @QueryParam(REST_QUERY_USERID) String userId)
 	throws PhrescoException, ClientProtocolException, IOException, JSONException {
 		ResponseInfo<CIJobStatus> responseData = new ResponseInfo<CIJobStatus>();
 		boolean updateJob = false;
 		CIJobStatus status = null;
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			List<CIJob> tempCiJobs = new ArrayList<CIJob>(); 
 			List<CIJob> tempJobs = new ArrayList<CIJob>();
 			List<CIJob> newJobs = continuousDelivery.getJobs();
 			List<CIJob> oldJobs = ciManager.getOldJobs(projectId, continuousDelivery, appDir);
 			tempCiJobs.addAll(oldJobs);
 
 			for (CIJob ciJob : newJobs) {
 				boolean exists = false;
 				for (CIJob oldCiJob : oldJobs) {
 					if(oldCiJob.getJobName().equals(ciJob.getJobName())) {
 						boolean equals = ciJob.equals(oldCiJob);
 						//tempCiJobs.add(ciJob);
 						ApplicationInfo applicationInfo = new ApplicationInfo();
 						if(!equals) {
 							exists = true;
 							if(StringUtils.isNotEmpty(ciJob.getAppDirName())) {
 								applicationInfo = FrameworkServiceUtil.getApplicationInfo(ciJob.getAppDirName());
 							}
 							CIJob job = setPreBuildCmds(ciJob,  applicationInfo, appDir, projectId, continuousDelivery.getName(), userId, customerId, request);
 							updateJob = ciManager.updateJob(job);
 							//						if(updateJob) {
 							tempJobs.add(ciJob);
 							//						}
 						} else {
 							exists = true;
 							tempJobs.add(ciJob);
 						}
 					} 
 				}
 				if (!exists) {
 					ApplicationInfo applicationInfo = new ApplicationInfo();
 					if(StringUtils.isNotEmpty(ciJob.getAppDirName())) {
 						applicationInfo = FrameworkServiceUtil.getApplicationInfo(ciJob.getAppDirName());
 					}
 					CIJob jobWithCmds = setPreBuildCmds(ciJob,  applicationInfo, appDir, projectId, continuousDelivery.getName(), userId, customerId, request);
 					boolean validateJob = validateJob(jobWithCmds.getJobName());
 					if(validateJob){
 						boolean createJob = ciManager.createJob(jobWithCmds);
 						if(createJob) {
 							tempJobs.add(ciJob);
 						}
 						exists = false;
 					} else {
						ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null, jobWithCmds.getJobName(), RESPONSE_STATUS_FAILURE, PHR810031);
 						return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 					}
 					
 				} 
 			}
 			boolean clearContinuousDelivery = ciManager.clearContinuousDelivery(continuousDelivery.getName(), projectId, appDir);
 			if(clearContinuousDelivery) {
 				continuousDelivery.setJobs(tempJobs);
 			}
 
 			boolean createJsonJobs = ciManager.createJsonJobs(continuousDelivery, tempJobs, projectId, appDir);
 			if (createJsonJobs) {
 				for(CIJob job:tempJobs) {
 					for (CIJob ciJob2 : tempCiJobs) {
 						if(ciJob2.getJobName().equals(job.getJobName())) {
 							oldJobs.remove(ciJob2);
 						}
 					}
 				}
 				status = ciManager.deleteJobs(appDir, oldJobs, projectId, continuousDelivery.getName());
 			}
 			ResponseInfo<CIJobStatus> finalOutput;
 			if (createJsonJobs) {
 				finalOutput = responseDataEvaluation(responseData, null, continuousDelivery, RESPONSE_STATUS_SUCCESS, PHR800020);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_FAILURE, PHR810005);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810005);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 	
 	private boolean validateJob(String jobName) throws PhrescoException, ClientProtocolException, IOException, JSONException {
 		boolean hasTrue = true;
 		InetAddress thisIp = InetAddress.getLocalHost();
 		String port = FrameworkServiceUtil.getJenkinsPortNo();
 		String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + thisIp.getHostAddress() + ":" + port + "/" + CI + "/api/json";
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(jenkinsUrl);
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 		String execute = httpClient.execute(httpget, responseHandler);
 		
 		JSONArray jsonArray = new JSONArray("["+execute.toString()+"]");
 	    JSONObject jsonObject = jsonArray.getJSONObject(0);
 	    String json = jsonObject.getString("jobs");
 	    JSONArray jobJsonArray = new JSONArray(json);
 	    for (int j = 0; j<jobJsonArray.length(); j++) {
 	    	 JSONObject jsonObject1 = jobJsonArray.getJSONObject(j);
 	    	 if(jobName.equals(jsonObject1.getString("name"))) {
 	    		 hasTrue = false;
 	    		 break;
 	    	 }
 	    }
 	    return hasTrue;
 		
 	}
 
 	/**
 	 * @param projectId
 	 * @param continuousName
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/editContinuousView")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response editContinuousView(@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_NAME) String continuousName, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir)
 	throws PhrescoException {
 		ResponseInfo<ContinuousDelivery> responseData = new ResponseInfo<ContinuousDelivery>();
 		ContinuousDelivery matchingContinuous = null;
 		boolean exist = false;
 		if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 			ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 			projectId = projectInfo.getId();
 		}
 		CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 		List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 		ContinuousDelivery continuousDelivery = Utility.getContinuousDelivery(projectId, continuousName.trim(), ciJobInfo);
 		if (continuousDelivery.getName().equalsIgnoreCase(continuousName)) {
 			matchingContinuous = continuousDelivery;
 			exist = true;
 		}
 
 		ResponseInfo<ContinuousDelivery> finalOutput;
 		if(exist) {
 			finalOutput= responseDataEvaluation(responseData, null, matchingContinuous, RESPONSE_STATUS_SUCCESS, PHR800004);
 		} else {
 			finalOutput = responseDataEvaluation(responseData, null, matchingContinuous, RESPONSE_STATUS_FAILURE, PHR810006);
 		}
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 	}
 	
 	/**
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/list")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getContinuousDeliveryJob(@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir)
 	throws PhrescoException {
 		ResponseInfo<Boolean> responseData = new ResponseInfo<Boolean>();
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ProjectDelivery projectContinuousDelivery = new ProjectDelivery();
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			if(CollectionUtils.isNotEmpty(ciJobInfo)) {
 				ProjectDelivery projectDelivery = Utility.getProjectDelivery(projectId, ciJobInfo);
 				if (projectDelivery != null) {
 					List<ContinuousDelivery> continuousDeliveries = projectDelivery.getContinuousDeliveries();
 					List<ContinuousDelivery> contDeliveryList = new ArrayList<ContinuousDelivery>();
 					for (ContinuousDelivery continuousDelivery : continuousDeliveries) {
 
 						List<CIJob> ciJobs = continuousDelivery.getJobs();
 						String downstreamApplication = "";
 						ContinuousDelivery contDelivery = new ContinuousDelivery();
 						List<CIJob> jobs = new ArrayList<CIJob>();
 						for (CIJob ciJob : ciJobs) {
 							if(StringUtils.isEmpty(ciJob.getUpstreamApplication())) {
 								jobs.add(ciJob);
 								downstreamApplication = ciJob.getDownstreamApplication();
 								if(ciJobs.size() == 1) {
 									contDelivery.setJobs(jobs);
 								}
 							}
 						}
 						if(StringUtils.isNotEmpty(downstreamApplication)) {
 							int flag = 0;
 							for(int i=0;i<ciJobs.size();i++){
 								for (CIJob ciJob : ciJobs) {
 									if(ciJob.getJobName().equals(downstreamApplication)) {
 										jobs.add(ciJob);
 										if(StringUtils.isEmpty(ciJob.getDownstreamApplication())) {
 											flag = 1;
 											contDelivery.setJobs(jobs);
 											downstreamApplication = "";
 											break;
 										} else {
 											downstreamApplication = ciJob.getDownstreamApplication();
 										}
 										if(flag == 1) {
 											break;
 										}
 									}
 									if(flag == 1) {
 										break;
 									}
 								}
 							}
 						} 
 						contDelivery.setName(continuousDelivery.getName());
 						contDelivery.setEnvName(continuousDelivery.getEnvName());
 						contDeliveryList.add(contDelivery);
 						projectContinuousDelivery.setContinuousDeliveries(contDeliveryList);
 					}
 				}
 				projectContinuousDelivery.setId(projectId);
 			}
 
 			ResponseInfo<ProjectDelivery> finalOutput = responseDataEvaluation(responseData, null, projectContinuousDelivery, RESPONSE_STATUS_SUCCESS, PHR800005);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810007);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 
 	/**
 	 * @param continuousName
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@DELETE
 	@Path("/delete")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response delete(@QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir) throws PhrescoException {
 		ResponseInfo<CIJobStatus> responseData = new ResponseInfo<CIJobStatus>();
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJobStatus ciJobStatus = null;
 			List<CIJob> jobs = Utility.getJobs(continuousName, projectId, ciJobInfo);
 			ciJobStatus = ciManager.deleteJobs(appDir, jobs, projectId, continuousName);
 			ciManager.clearContinuousDelivery(continuousName, projectId, appDir);
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, null, ciJobStatus, RESPONSE_STATUS_SUCCESS, PHR800006);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810009);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 
 	/**
 	 * @param buildNumber
 	 * @param jobName
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @param continuousName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@DELETE
 	@Path("/deletebuilds")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response deleteBuilds(@QueryParam(REST_QUERY_BUILD_NUMBER) String buildNumber, @QueryParam(REST_QUERY_NAME) String jobName, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, @QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName)
 	throws PhrescoException {
 		ResponseInfo<CIJobStatus> responseData = new ResponseInfo<CIJobStatus>();
 		ResponseInfo<CIJobStatus> finalOutput ;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			CIJobStatus deleteBuilds = null;
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJob specificJob = ciManager.getJob(jobName, projectId, ciJobInfo, continuousName);
 			if (specificJob != null) {
 				deleteBuilds = ciManager.deleteBuilds(specificJob, buildNumber);
 				finalOutput = responseDataEvaluation(responseData, null, deleteBuilds, RESPONSE_STATUS_SUCCESS, PHR800007);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, deleteBuilds, RESPONSE_STATUS_FAILURE, PHR810011);
 			}
 
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810011);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 	
 	/**
 	 * @param name
 	 * @param projectId
 	 * @param appDir
 	 * @param continuousName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@POST
 	@Path("/build")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response build(@QueryParam(REST_QUERY_NAME) String name,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, @QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName) throws PhrescoException {
 		ResponseInfo<CIJobStatus> responseData = new ResponseInfo<CIJobStatus>();
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			CIJobStatus buildJobs = null;
 			ResponseInfo<CIJobStatus> finalOutput; 
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJob specificJob = ciManager.getJob(name, projectId, ciJobInfo, continuousName);
 			if (specificJob != null) {
 				buildJobs = ciManager.generateBuild(specificJob);
 				finalOutput = responseDataEvaluation(responseData, null, buildJobs, RESPONSE_STATUS_SUCCESS, PHR800008);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, buildJobs, RESPONSE_STATUS_FAILURE, PHR810012);
 			}
 
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810013);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 
 	/**
 	 * @param customerId
 	 * @param projectId
 	 * @param appId
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/mail")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getEmailConfiguration()
 	throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		ResponseInfo finalOutput = null;
 		List<RepoDetail> confluenceDetail = new ArrayList<RepoDetail>();
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			String[] cred = new String[2];
 			cred[0] = ciManager.getMailConfiguration(SMTP_AUTH_USERNAME);
 			String mailConfiguration = ciManager.getMailConfiguration(SMTP_AUTH_PASSWORD);
 			if(StringUtils.isNotEmpty(mailConfiguration)) {
 				cred[1] = ciManager.decyPassword(mailConfiguration);
 			} else {
 				cred[1] = "";
 			}
 			
 			finalOutput = responseDataEvaluation(responseData, null, cred, RESPONSE_STATUS_SUCCESS, PHR800009);
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810014);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		.build();
 	}
 	
 	/**
 	 * @return
 	 * @throws PhrescoException
 	 * @throws UnknownHostException
 	 */
 	@GET
 	@Path("/jenkinsUrl")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getJenkinsUrl()throws PhrescoException, UnknownHostException {
 		ResponseInfo responseData = new ResponseInfo();
 		ResponseInfo finalOutput = null;
 		InetAddress thisIp;
 		try {
 			thisIp = InetAddress.getLocalHost();
 			String host = thisIp.getHostAddress();
 			String port = FrameworkServiceUtil.getJenkinsPortNo();
 			String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + host + FrameworkConstants.COLON + port + FrameworkConstants.FORWARD_SLASH + CI ;
 			finalOutput = responseDataEvaluation(responseData, null, jenkinsUrl, RESPONSE_STATUS_SUCCESS, PHR800024);
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810037);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 				.build();
 	}
 	
 	/**
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/confluence")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getConfluenceConfiguration()	throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		ResponseInfo finalOutput = null;
 		List<RepoDetail> confluenceDetail = new ArrayList<RepoDetail>();
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			org.codehaus.jettison.json.JSONArray confluenceConfiguration = ciManager.getConfluenceConfiguration();
 			if (confluenceConfiguration != null) {
 				for (int i = 0; i < confluenceConfiguration.length(); i++) {
 					RepoDetail repo = new RepoDetail();
 					org.codehaus.jettison.json.JSONObject JSONobject = (org.codehaus.jettison.json.JSONObject) confluenceConfiguration.get(i);
 					repo.setRepoUrl(JSONobject.getString(REPO_URL));
 					repo.setUserName(JSONobject.getString(USERNAME));
 					repo.setPassword(JSONobject.getString(FrameworkConstants.PASSWORD));
 					confluenceDetail.add(repo);
 				}
 			}
 			finalOutput = responseDataEvaluation(responseData, null, confluenceDetail, RESPONSE_STATUS_SUCCESS, PHR800021);
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810034);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (org.codehaus.jettison.json.JSONException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810034);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		.build();
 	}
 	
 	/**
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@POST
 	@Path("/global")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response setGlobalConfiguration(List<RepoDetail> repodetails, @QueryParam(REST_QUERY_EMAIL_ADDRESS) String emailAddress, @QueryParam(REST_QUERY_EMAIL_PASSWORD) String emailPassword) throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		ResponseInfo finalOutput = null;
 		boolean setGlobalConfiguration = false;
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + LOCALHOST + FrameworkConstants.COLON + 3579 + FrameworkConstants.FORWARD_SLASH + CI ;
 			String submitUrl = jenkinsUrl + FrameworkConstants.FORWARD_SLASH + CONFIG_SUBMIT;
 			org.json.JSONArray JSONarray = new org.json.JSONArray();
 			for (RepoDetail repodetail : repodetails) {
 				org.json.JSONObject confluenceObj = new org.json.JSONObject();
 				confluenceObj.put(CONFLUENCE_SITE_URL, repodetail.getRepoUrl());
 				confluenceObj.put(CONFLUENCE_USERNAME, repodetail.getUserName());
 				confluenceObj.put(FrameworkConstants.PASSWORD, repodetail.getPassword());
 				JSONarray.put(confluenceObj);
 			}
 			setGlobalConfiguration = ciManager.setGlobalConfiguration(jenkinsUrl, submitUrl, JSONarray, emailAddress, emailPassword);
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, setGlobalConfiguration, RESPONSE_STATUS_ERROR, PHR810034);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (org.json.JSONException e) {
 			finalOutput = responseDataEvaluation(responseData, e, setGlobalConfiguration, RESPONSE_STATUS_ERROR, PHR810034);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 		finalOutput = responseDataEvaluation(responseData, null, setGlobalConfiguration, RESPONSE_STATUS_SUCCESS, PHR800022);
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		.build();
 	}
 	
 	/**
 	 * @param buildDownloadUrl
 	 * @param downloadJobName
 	 * @param customerId
 	 * @param projectId
 	 * @param appDir
 	 * @param continuousName
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@GET
 	@Path("/downloadBuild")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response CIBuildDownload(@QueryParam(REST_QUERY_BUILD_DOWNLOAD_URL) String buildDownloadUrl,
 			@QueryParam(REST_QUERY_DOWNLOAD_JOB_NAME) String downloadJobName, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir, 
 			@QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName)
 			throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		ResponseInfo finalOutput = null;
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJob job = ciManager.getJob(downloadJobName, projectId, ciJobInfo, continuousName);
 			// Get it from web path
 			String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + job.getJenkinsUrl() + ":" + job.getJenkinsPort() + "/" + CI + "/job/";
 			buildDownloadUrl = buildDownloadUrl.replace(" ", "%20");
 			String url = jenkinsUrl+job.getJobName()+"/ws/"+buildDownloadUrl;
 			finalOutput = responseDataEvaluation(responseData, null, url, RESPONSE_STATUS_SUCCESS, PHR800010);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		} catch (Exception e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810035);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 					.build();
 		}
 	}
 	
 	
 	/**
 	 * @return
 	 */
 	@GET
 	@Path("/isAlive")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response localJenkinsLocalAlive() {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		String localJenkinsAlive = "";
 		ResponseInfo<String> finalOutput;
 		String statusCode= "";
 		try {
 			URL url = new URL(HTTP_PROTOCOL + PROTOCOL_POSTFIX + LOCALHOST + FrameworkConstants.COLON
 					+ Integer.parseInt(FrameworkServiceUtil.getJenkinsPortNo()) + FrameworkConstants.FORWARD_SLASH + CI);
 			URLConnection connection = url.openConnection();
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 			int code = httpConnection.getResponseCode();
 			localJenkinsAlive = code + "";
 			statusCode = PHR800011;
 		} catch (ConnectException e) {
 			localJenkinsAlive = CODE_404;
 			statusCode = PHR810017;
 		} catch (Exception e) {
 			localJenkinsAlive = CODE_404;
 			statusCode = PHR810017;
 		}
 		finalOutput = responseDataEvaluation(responseData, null, localJenkinsAlive, RESPONSE_STATUS_SUCCESS, statusCode);
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 		.build();
 	}
 
 	/**
 	 * @param jobName
 	 * @param continuousName
 	 * @param projectId
 	 * @param appDir
 	 * @return
 	 */
 	@GET
 	@Path("/jobStatus")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getStatus(@QueryParam(REST_QUERY_NAME) String jobName, @QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		ResponseInfo<Boolean> finalOutput = null;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			CIJob job = null;
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			List<CIJob> ciJobs = Utility.getJobs(continuousName, projectId, ciJobInfo);
 			for (CIJob ciJob : ciJobs) {
 				if(ciJob.getJobName().equals(jobName)) {
 					job = ciJob;
 				}
 			}
 			String jobStatus = ciManager.getJobStatus(job);
 			finalOutput = responseDataEvaluation(responseData, null, jobStatus, RESPONSE_STATUS_SUCCESS, PHR800012);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810018);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 	
 	@GET
 	@Path("/lastBuildStatus")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getLastBuildStatus(@QueryParam(REST_QUERY_NAME) String jobName, @QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		ResponseInfo<Boolean> finalOutput = null;
 		try {
 			if(projectId == null || projectId.equals("null") || projectId.equals("")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			CIJob job = null;
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			List<CIJob> ciJobs = Utility.getJobs(continuousName, projectId, ciJobInfo);
 			for (CIJob ciJob : ciJobs) {
 				if(ciJob.getJobName().equals(jobName)) {
 					job = ciJob;
 				}
 			}
 			CIBuild build = ciManager.getStatusInfo(job);
 			finalOutput = responseDataEvaluation(responseData, null, build, RESPONSE_STATUS_SUCCESS, PHR800023);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR810036);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER)
 			.build();
 		}
 	}
 	
 	//core method used to create jobs in contDelivery
 	private boolean coreCreateJob(ContinuousDelivery continuousDelivery, String  projectId, String appDir, String userId, String customerId, HttpServletRequest request) throws PhrescoException {
 		CIManagerImpl ciManager = null;
 		boolean createJsonJobs = false;
 		try {
 			ciManager = new CIManagerImpl();
 			ApplicationInfo applicationInfo = null;
 			List<CIJob> ciJobs = new ArrayList<CIJob>(); 
 			List<CIJob> jobs = continuousDelivery.getJobs();
 			for(CIJob job : jobs) {	
 				if(StringUtils.isNotEmpty(job.getAppDirName())) {
 					applicationInfo = FrameworkServiceUtil.getApplicationInfo(job.getAppDirName());
 				}
 				CIJob jobWithCmds = setPreBuildCmds(job,  applicationInfo, appDir, projectId, continuousDelivery.getName(), userId, customerId, request);
 				boolean createJob = ciManager.createJob(job);
 				if (createJob) {
 					ciJobs.add(job);
 				}
 			}
 			if (CollectionUtils.isNotEmpty(ciJobs)) {
 				createJsonJobs = ciManager.createJsonJobs(continuousDelivery, ciJobs, projectId, appDir);
 			}
 			return createJsonJobs;
 		} catch (PhrescoException e) {
 			throw new PhrescoException();
 		}
 	}
 	
 	public CIJob setPreBuildCmds(CIJob job, ApplicationInfo appInfo, String appDir, String id, String name, String userId, String customerId, HttpServletRequest request) throws PhrescoException {
 		try {
 			List<String> preBuildStepCmds = new ArrayList<String>();
 
 			String operation = job.getOperation();
 
 			String mvncmd = "";
 
 			String pomFileName = Utility.getPomFileName(appInfo);
 			job.setPomLocation(pomFileName);
 
 			InetAddress thisIp = InetAddress.getLocalHost();
 			job.setJenkinsUrl(thisIp.getHostAddress());
 			job.setJenkinsPort(FrameworkServiceUtil.getJenkinsPortNo());
 
 			List<Parameter> parameters = null;
 
 			String integrationType = GLOBAL;
 			if(StringUtils.isNotEmpty(appDir)) {
 				integrationType = LOCAL;
 			}
 			Technology technology = null;
 			if (StringUtils.isNotEmpty(userId)) {
 				ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 				technology = serviceManager.getTechnology(appInfo.getTechInfo().getId());
 			}
 			
 			if (BUILD.equalsIgnoreCase(operation)) {
 				// enable archiving
 				job.setEnableArtifactArchiver(true);
 				// if the enable build release option is choosed in UI, the file pattenr value will be used
 				job.setCollabNetFileReleasePattern(CI_BUILD_EXT);
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_PACKAGE, job.getAppDirName()));
 				if(phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_PACKAGE);
 				}
 				ActionType actionType = ActionType.BUILD;
 				mvncmd =  actionType.getActionType().toString();
 
 				String buildPrebuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_PACKAGE +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 
 				if(!Constants.POM_NAME.equals(pomFileName)) {
 					buildPrebuildCmd = buildPrebuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				buildPrebuildCmd = buildPrebuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(buildPrebuildCmd);
 			} else if (DEPLOY.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_DEPLOY, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_DEPLOY);
 				}
 				ActionType actionType = ActionType.DEPLOY;
 				mvncmd =  actionType.getActionType().toString();
 
 				String deployPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_DEPLOY +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					deployPreBuildCmd = deployPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				deployPreBuildCmd = deployPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(deployPreBuildCmd);
 			} else if (PDF_REPORT.equals(operation)) {
 				// for pdf report attachment patterns
 				// based on test report type, it should be specified
 				//TODO validation for TestType
 				String attacheMentPattern = "cumulativeReports";
 				
 				// enable archiving
 				job.setEnableArtifactArchiver(true);
 				job.setTechnologyName(technology.getName());
 				String attachPattern = "do_not_checkin/archives/" + attacheMentPattern + "/*.pdf";
 				job.setAttachmentsPattern(attachPattern); //do_not_checkin/archives/cumulativeReports/*.pdf
 				// if the enable build release option is choosed in UI, the file pattenr value will be used
 				job.setCollabNetFileReleasePattern(attachPattern);
 
 				// here we can set necessary values in request and we can change object value as well...
 				// getting sonar url
 				
 //				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				String usingSonarUrl = FrameworkServiceUtil.getSonarURL(request);
 				// logo and theme
 				//				String logoImageString = getLogoImageString();
 				//				String themeColorJson = getThemeColorJson();
 
 				// object change
 				job.setSonarUrl(usingSonarUrl);
 				job.setLogo(getLogoImageString(userId, customerId));
 				job.setTheme(getThemeColorJson(userId, customerId));
 
 				// set values in request
 //								sonarUrl = usingSonarUrl;
 				//				logo = logoImageString;
 				//				theme = themeColorJson;
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_PDF_REPORT, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_PDF_REPORT);
 				}
 				ActionType actionType = ActionType.PDF_REPORT;
 				mvncmd =  actionType.getActionType().toString();
 
 				String pdfPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_PDF_REPORT +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					pdfPreBuildCmd = pdfPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				pdfPreBuildCmd = pdfPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(pdfPreBuildCmd);
 			} else if (CODE_VALIDATION.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_VALIDATE_CODE, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_VALIDATE_CODE);
 				}
 				ActionType actionType = ActionType.CODE_VALIDATE;
 				mvncmd =  actionType.getActionType().toString();
 
 				String deployPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_VALIDATE_CODE +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					deployPreBuildCmd = deployPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				deployPreBuildCmd = deployPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(deployPreBuildCmd);
 			} else if (UNIT_TEST.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_UNIT_TEST, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_UNIT_TEST);
 				}
 				ActionType actionType = ActionType.UNIT_TEST;
 				mvncmd =  actionType.getActionType().toString();
 
 				String unitTestPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_UNIT_TEST +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					unitTestPreBuildCmd = unitTestPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				unitTestPreBuildCmd = unitTestPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(unitTestPreBuildCmd);
 				if (job.isCoberturaPlugin()) {
 					job.setEnablePostBuildStep(true);
 					List<String> postBuildStepCmds = new ArrayList<String>();
 					postBuildStepCmds.add(MAVEN_SEP_COBER);
 					postBuildStepCmds.add("shell#SEP#cd ${WORKSPACE}\n${GCOV_HOME} -r ${WORKSPACE} -x -o coverage.xml");
 					if (CollectionUtils.isNotEmpty(postBuildStepCmds)) {
 						job.setPostbuildStepCommands(postBuildStepCmds);
 					}
 				}
 			} else if (FUNCTIONAL_TEST.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_FUNCTIONAL_TEST, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 					String seleniumToolType = "";
 					seleniumToolType = frameworkUtil.getSeleniumToolType(appInfo);
 	
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_FUNCTIONAL_TEST + HYPHEN + seleniumToolType);
 				}
 				ActionType actionType = ActionType.FUNCTIONAL_TEST;
 				mvncmd =  actionType.getActionType().toString();
 				String functionalTestPrebuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_FUNCTIONAL_TEST +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					functionalTestPrebuildCmd = functionalTestPrebuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				functionalTestPrebuildCmd = functionalTestPrebuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(functionalTestPrebuildCmd);
 			} else if (LOAD_TEST.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_LOAD_TEST, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_LOAD_TEST);
 				}
 				ActionType actionType = ActionType.LOAD_TEST;
 				mvncmd =  actionType.getActionType().toString();
 
 				String loadTestPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_LOAD_TEST +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					loadTestPreBuildCmd = loadTestPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				loadTestPreBuildCmd = loadTestPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(loadTestPreBuildCmd);
 			} else if (PERFORMANCE_TEST_CI.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_PERFORMANCE_TEST, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_PERFORMANCE_TEST);
 				}
 				ActionType actionType = ActionType.PERFORMANCE_TEST;
 				mvncmd =  actionType.getActionType().toString();
 
 				String performanceTestPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_PERFORMANCE_TEST +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					performanceTestPreBuildCmd = performanceTestPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				performanceTestPreBuildCmd = performanceTestPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(performanceTestPreBuildCmd);
 			} else if (COMPONENT_TEST_CI.equals(operation)) {
 				File phrescoPluginInfoFilePath = new File(FrameworkServiceUtil.getPhrescoPluginInfoFilePath(Constants.PHASE_CI, Constants.PHASE_COMPONENT_TEST, job.getAppDirName()));
 				if (phrescoPluginInfoFilePath.exists()) {
 					MojoProcessor mojo = new MojoProcessor(phrescoPluginInfoFilePath);
 					//To get maven build arguments
 					parameters = FrameworkServiceUtil.getMojoParameters(mojo, Constants.PHASE_COMPONENT_TEST);
 				}
 				ActionType actionType = ActionType.COMPONENT_TEST;
 				mvncmd =  actionType.getActionType().toString();
 
 				String componentTestPreBuildCmd = CI_PRE_BUILD_STEP + " -Dgoal=" + Constants.PHASE_CI + " -Dphase=" + Constants.PHASE_COMPONENT_TEST +
 				CREATIONTYPE + integrationType + ID + id + CONTINUOUSNAME + name;
 				if(!POM_NAME.equals(pomFileName)) {
 					componentTestPreBuildCmd = componentTestPreBuildCmd + " -f " + pomFileName; 
 				}
 				// To handle multi module project
 				componentTestPreBuildCmd = componentTestPreBuildCmd + FrameworkConstants.SPACE + HYPHEN_N;
 				preBuildStepCmds.add(componentTestPreBuildCmd);
 			}
 
 
 			List<String> buildArgCmds = FrameworkServiceUtil.getMavenArgCommands(parameters);
 			if(!POM_NAME.equals(pomFileName)) {
 				buildArgCmds.add(HYPHEN_F);
 				buildArgCmds.add(pomFileName);
 			}
 			if (!CollectionUtils.isEmpty(buildArgCmds)) {
 				for (String buildArgCmd : buildArgCmds) {
 					mvncmd = mvncmd + FrameworkConstants.SPACE + buildArgCmd;
 				}
 			}
 
 			// for build job alone existing do_not_checkin need to be cleared
 			// For pdf report, it should clear existing pdf reports in do_not_checkin folder
 			if (BUILD.equals(operation) || PDF_REPORT.equals(operation)) {
 				mvncmd = CI_PROFILE + mvncmd;
 			}
 
 			// To handle multi module project
 			mvncmd = mvncmd + FrameworkConstants.SPACE + HYPHEN_N;
 			job.setMvnCommand(mvncmd);
 			// prebuild step enable
 			job.setEnablePreBuildStep(true);
 
 			job.setPrebuildStepCommands(preBuildStepCmds);		
 
 		} catch (UnknownHostException e) {
 			throw new PhrescoException();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException();
 		}
 		return job;
 	}
 	
 	protected String getLogoImageString(String username, String customerId) throws PhrescoException {
 		String encodeImg = "";
 		try {
 			InputStream fileInputStream = null;
 			fileInputStream = getServiceManager(username).getIcon(customerId);
 			if (fileInputStream != null) {
 				byte[] imgByte = null;
 				imgByte = IOUtils.toByteArray(fileInputStream);
 				byte[] encodedImage = Base64.encodeBase64(imgByte);
 				encodeImg = new String(encodedImage);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return encodeImg;
 	}
 	
 	protected ServiceManager getServiceManager(String username) throws PhrescoException {
 		this.serviceManager = RestBase.CONTEXT_MANAGER_MAP.get(username);
 		return serviceManager;
 	}
 
 	protected String getThemeColorJson(String username, String customerId) throws PhrescoException {
 		String themeJsonStr = "";
 		try {
 			Customer customer = getServiceManager(username).getCustomer(customerId);
 			if (customer != null) {
 				Map<String, String> frameworkTheme = customer.getFrameworkTheme();
 				if (frameworkTheme != null) {
 					Gson gson = new Gson();
 					themeJsonStr = gson.toJson(frameworkTheme);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return themeJsonStr;
 	}
 	
 }
