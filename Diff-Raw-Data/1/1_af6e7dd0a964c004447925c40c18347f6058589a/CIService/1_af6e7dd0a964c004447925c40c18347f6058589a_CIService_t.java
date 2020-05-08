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
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.CIBuild;
 import com.photon.phresco.commons.model.CIJob;
 import com.photon.phresco.commons.model.CIJobStatus;
 import com.photon.phresco.commons.model.ContinuousDelivery;
 import com.photon.phresco.commons.model.ProjectDelivery;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.api.CIManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.impl.CIManagerImpl;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class CIService.
  */
 @Path("/ci")
 public class CIService extends RestBase implements FrameworkConstants, ServiceConstants,Constants {
 
 	
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
 			if(projectId.equals("null")) {
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
 			ResponseInfo<List<CIBuild>> finalOutput = responseDataEvaluation(responseData, null, "Builds returned successfully", builds);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<CIBuild>> finalOutput = responseDataEvaluation(responseData, e, "No Builds to return",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 	public Response createJob(ContinuousDelivery continuousDelivery, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir)
 	throws PhrescoException {
 		ResponseInfo<ContinuousDelivery> responseData = new ResponseInfo<ContinuousDelivery>();
 		ResponseInfo<ContinuousDelivery> finalOutput;
 		try {
 			if(projectId.equals("null")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			List<CIJob> jobs = continuousDelivery.getJobs();
 			boolean coreCreateJob = coreCreateJob(continuousDelivery, projectId, appDir);
 			if (coreCreateJob) {
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) created successfully", continuousDelivery);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) creation Failed", null);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, "Job creation Failed", null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 	public Response createClone(@QueryParam(REST_QUERY_CLONE_NAME) String cloneName, @QueryParam(REST_QUERY_ENV_NAME) String envName,
 			@QueryParam(REST_QUERY_CONTINOUSNAME) String continuousName, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir)
 	throws PhrescoException {
 		ResponseInfo<ContinuousDelivery> responseData = new ResponseInfo<ContinuousDelivery>();
 		ResponseInfo<ContinuousDelivery> finalOutput;
 		try {
 			if(projectId.equals("null")) {
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
 			boolean coreCreateJob = coreCreateJob(continuousDelivery, projectId, appDir);
 
 			if (coreCreateJob) {
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) created successfully", continuousDelivery);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) creation Failed", null);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, "Job creation Failed", null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
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
 	@PUT
 	@Path("/update")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateJob(ContinuousDelivery continuousDelivery, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPDIR_NAME) String appDir)
 	throws PhrescoException {
 		ResponseInfo<CIJobStatus> responseData = new ResponseInfo<CIJobStatus>();
 		boolean updateJob = false;
 		CIJobStatus status = null;
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			if(projectId.equals("null")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			List<CIJob> tempCiJobs = new ArrayList<CIJob>(); 
 			List<CIJob> tempJobs = new ArrayList<CIJob>();
 			List<CIJob> newJobs = continuousDelivery.getJobs();
 			List<CIJob> oldJobs = ciManager.getOldJobs(projectId, continuousDelivery, appDir);
 			tempCiJobs.addAll(oldJobs);
 
 			for (CIJob ciJob : newJobs) {//t1,t2,t3	
 				boolean exists = false;
 				for (CIJob oldCiJob : oldJobs) {//t1,t3,t2
 					if(oldCiJob.getJobName().equals(ciJob.getJobName())) {
 						boolean equals = ciJob.equals(oldCiJob);
 						//tempCiJobs.add(ciJob);
 						ApplicationInfo applicationInfo = new ApplicationInfo();
 						if(!equals) {
 							exists = true;
 							if(StringUtils.isNotEmpty(ciJob.getAppDirName())) {
 								applicationInfo = FrameworkServiceUtil.getApplicationInfo(ciJob.getAppDirName());
 							}
 							CIJob job = setPreBuildCmds(ciJob,  applicationInfo, appDir, projectId, continuousDelivery.getName());
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
 					CIJob jobWithCmds = setPreBuildCmds(ciJob,  applicationInfo, appDir, projectId, continuousDelivery.getName());
 					boolean createJob = ciManager.createJob(jobWithCmds);
 					if(createJob) {
 						tempJobs.add(ciJob);
 					}
 					exists = false;
 				} 
 			}
 			boolean clearContinuousDelivery = ciManager.clearContinuousDelivery(continuousDelivery.getName(), projectId, appDir);
 			if(clearContinuousDelivery) {
 				continuousDelivery.setJobs(tempJobs);
 			}
 
 			boolean createJsonJobs = ciManager.createJsonJobs(continuousDelivery, tempJobs, projectId, appDir);
 			if (createJsonJobs) {
 				/*Iterator<CIJob> iterator = oldJobs.iterator();
 				for(CIJob job:tempJobs) {
 					while (iterator.hasNext()) {
 						CIJob itrCiJob = iterator.next();
 						if (itrCiJob.getJobName().equals(job.getJobName())) {
 							iterator.remove();
 							break;
 						}
 					}
 				}*/
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
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) Updated successfully", continuousDelivery);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, "Job(s) updation Failed", null);
 			}
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, "Job updation Failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		}
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
 		if(projectId.equals("null")) {
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
 			finalOutput= responseDataEvaluation(responseData, null, "Continuous Delivery Fetched successfully",	matchingContinuous);
 		} else {
 			finalOutput = responseDataEvaluation(responseData, null, "Could not find Delivery named "+continuousName,	matchingContinuous);
 		}
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
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
 			if(projectId.equals("null")) {
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
 
 			ResponseInfo<ProjectDelivery> finalOutput = responseDataEvaluation(responseData, null,
 					"Continuous Delivery List Successfully", projectContinuousDelivery);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, "Job creation Failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 			if(projectId.equals("null")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJobStatus ciJobStatus = null;
 			List<CIJob> jobs = Utility.getJobs(continuousName, projectId, ciJobInfo);
 			ciJobStatus = ciManager.deleteJobs(appDir, jobs, projectId, continuousName);
 			ciManager.clearContinuousDelivery(continuousName, projectId, appDir);
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, null, "Job deleted successfully",
 					ciJobStatus);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, e, "Job deletion Failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 			if(projectId.equals("null")) {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDir);
 				projectId = projectInfo.getId();
 			}
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			CIJobStatus deleteBuilds = null;
 			List<ProjectDelivery> ciJobInfo = ciManager.getCiJobInfo(appDir);
 			CIJob specificJob = ciManager.getJob(jobName, projectId, ciJobInfo, continuousName);
 			if (specificJob != null) {
 				deleteBuilds = ciManager.deleteBuilds(specificJob, buildNumber);
 				finalOutput = responseDataEvaluation(responseData, null, "Build deleted successfully",	deleteBuilds);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, "Build deletion Failed",	deleteBuilds);
 			}
 
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		} catch (PhrescoException e) {
 			finalOutput = responseDataEvaluation(responseData, e, "Build deletion Failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 			if(projectId.equals("null")) {
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
 				finalOutput = responseDataEvaluation(responseData, null, "Build Triggered success", buildJobs);
 			} else {
 				finalOutput = responseDataEvaluation(responseData, null, "Build Triggered Failed", buildJobs);
 			}
 
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		} catch (PhrescoException e) {
 			ResponseInfo<CIJobStatus> finalOutput = responseDataEvaluation(responseData, e, "Build failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 	@POST
 	@Path("/mail")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response getEmailConfiguration(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPID) String appId)
 	throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ApplicationInfo applicationInfo = applicationManager.getApplicationInfo(customerId, projectId, appId);
 			String smtpAuthUsername = ciManager.getMailConfiguration(SMTP_AUTH_USERNAME);
 			String smtpAuthPassword = ciManager.getMailConfiguration(SMTP_AUTH_PASSWORD);
 		} catch (PhrescoException e) {
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, "Returned mail confiuration Failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		}
 		return null;
 	}
 
 	/**
 	 * @param senderEmailId
 	 * @param senderEmailPassword
 	 * @param customerId
 	 * @param projectId
 	 * @param appId
 	 * @return
 	 * @throws PhrescoException
 	 */
 	@POST
 	@Path("/savemail")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response saveEmailConfiguration(@QueryParam(REST_QUERY_SENDER_MAIL_ID) String senderEmailId,
 			@QueryParam(REST_QUERY_SENDER_MAIL_PWD) String senderEmailPassword, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_APPID) String appId)
 	throws PhrescoException {
 		ResponseInfo responseData = new ResponseInfo();
 		try {
 			CIManager ciManager = PhrescoFrameworkFactory.getCIManager();
 			//			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			//			ApplicationInfo applicationInfo = applicationManager.getApplicationInfo(customerId, projectId, appId);
 			String jenkinsPort = FrameworkServiceUtil.getJenkinsPortNo();
 			ciManager.saveMailConfiguration(jenkinsPort, senderEmailId, senderEmailPassword);
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, null, "Mail Configuration saved successfully",
 					null);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		} catch (PhrescoException e) {
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, "Save mail Configuration failed",
 					null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 		try {
 			URL url = new URL(HTTP_PROTOCOL + PROTOCOL_POSTFIX + LOCALHOST + FrameworkConstants.COLON
 					+ Integer.parseInt(FrameworkServiceUtil.getJenkinsPortNo()) + FrameworkConstants.FORWARD_SLASH + CI);
 			URLConnection connection = url.openConnection();
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 			int code = httpConnection.getResponseCode();
 			localJenkinsAlive = code + "";
 		} catch (ConnectException e) {
 			localJenkinsAlive = CODE_404;
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null, "Jenkins not found",
 					localJenkinsAlive);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		} catch (Exception e) {
 			localJenkinsAlive = CODE_404;
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null, "Jenkins not found",
 					localJenkinsAlive);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		}
 		ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null, "Jenkins is Alive",
 				localJenkinsAlive);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
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
 		try {
 			if(projectId.equals("null")) {
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
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null, "Return Job Status successfully", jobStatus);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e, "Failed to get Job Status",	null);
 			return Response.status(Status.NOT_FOUND).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 			.build();
 		}
 	}
 	
 	//core method used to create jobs in contDelivery
 	private boolean coreCreateJob(ContinuousDelivery continuousDelivery, String  projectId, String appDir) throws PhrescoException {
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
 				CIJob jobWithCmds = setPreBuildCmds(job,  applicationInfo, appDir, projectId, continuousDelivery.getName());
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
 	
 	public CIJob setPreBuildCmds(CIJob job, ApplicationInfo appInfo, String appDir, String id, String name) throws PhrescoException {
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
 				String attachPattern = "do_not_checkin/archives/" + attacheMentPattern + "/*.pdf";
 				job.setAttachmentsPattern(attachPattern); //do_not_checkin/archives/cumulativeReports/*.pdf
 				// if the enable build release option is choosed in UI, the file pattenr value will be used
 				job.setCollabNetFileReleasePattern(attachPattern);
 
 				// here we can set necessary values in request and we can change object value as well...
 				// getting sonar url
 				
 				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 				String usingSonarUrl = frameworkUtil.getSonarURL();
 				// logo and theme
 				//				String logoImageString = getLogoImageString();
 				//				String themeColorJson = getThemeColorJson();
 
 				// object change
 				job.setSonarUrl(usingSonarUrl);
 				//				job.setLogo(logoImageString);
 				//				job.setTheme(themeColorJson);
 
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
 
 }
