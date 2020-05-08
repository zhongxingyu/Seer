 /*
  * ###
  * Phresco Framework Implementation
  * %%
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * %%
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
 package com.photon.phresco.framework.impl;
 
 import hudson.cli.*;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.net.*;
 import java.text.*;
 import java.util.*;
 
 import org.apache.commons.collections.*;
 import org.apache.commons.lang.*;
 import org.apache.http.client.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.*;
 import org.apache.log4j.*;
 import org.jdom.*;
 
 import com.google.gson.*;
 import com.google.gson.reflect.*;
 import com.photon.phresco.commons.*;
 import com.photon.phresco.commons.model.*;
 import com.photon.phresco.exception.*;
 import com.photon.phresco.framework.*;
 import com.photon.phresco.framework.api.*;
 import com.photon.phresco.framework.model.*;
 import com.photon.phresco.util.*;
 import com.sun.jersey.api.client.*;
 
 public class CIManagerImpl implements CIManager, FrameworkConstants {
 
 	private static final Logger S_LOGGER = Logger.getLogger(CIManagerImpl.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 	
     private CLI cli = null;
     
 	public BufferedReader setup(ProjectInfo projectInfo, ActionType action, List<String> buildArgCmds, String workingDirectory) throws PhrescoException {
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			BufferedReader reader = applicationManager.performAction(projectInfo, action, buildArgCmds, workingDirectory);
 			return reader;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public BufferedReader start(ProjectInfo projectInfo, ActionType action, List<String> buildArgCmds, String workingDirectory) throws PhrescoException {
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			BufferedReader reader = applicationManager.performAction(projectInfo, action, buildArgCmds, workingDirectory);
 			return reader;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public BufferedReader stop(ProjectInfo projectInfo, ActionType action, List<String> buildArgCmds, String workingDirectory) throws PhrescoException {
 		try {
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			BufferedReader reader = applicationManager.performAction(projectInfo, action, buildArgCmds, workingDirectory);
 			return reader;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	} 
 	
 	 public void createJob(ApplicationInfo appInfo, CIJob job) throws PhrescoException {
 		 if (debugEnabled) {
 			 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createJob(Project project, CIJob job)");
 		 }
 		 FileWriter writer = null;
 		 try {
 			 CIJobStatus jobStatus = configureJob(job, FrameworkConstants.CI_CREATE_JOB_COMMAND);
 			 if (jobStatus.getCode() == -1) {
 				 throw new PhrescoException(jobStatus.getMessage());
 			 }
 			 if (debugEnabled) {
 				 S_LOGGER.debug("ProjectInfo = " + appInfo);
 			 }
 			 writeJsonJobs(appInfo, Arrays.asList(job), CI_APPEND_JOBS);
 		 } catch (ClientHandlerException ex) {
 			 if (debugEnabled) {
 				 S_LOGGER.error(ex.getLocalizedMessage());
 			 }
 			 throw new PhrescoException(ex);
 		 } finally {
 			 if (writer != null) {
 				 try {
 					 writer.close();
 				 } catch (IOException e) {
 					 S_LOGGER.error(e.getLocalizedMessage());
 				 }
 			 }
 		 }
 	 }
 
 	 public void updateJob(ApplicationInfo appInfo, CIJob job) throws PhrescoException {
 		 if (debugEnabled) {
 			 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.updateJob(Project project, CIJob job)");
 		 }
 		 FileWriter writer = null;
 		 try {
 			 CIJobStatus jobStatus = configureJob(job, FrameworkConstants.CI_UPDATE_JOB_COMMAND);
 			 if (jobStatus.getCode() == -1) {
 				 throw new PhrescoException(jobStatus.getMessage());
 			 }
 			 if (debugEnabled) {
 				 S_LOGGER.debug("getCustomModules() ProjectInfo = "+ appInfo);
 			 }
 			 updateJsonJob(appInfo, job);
 		 } catch (ClientHandlerException ex) {
 			 if (debugEnabled) {
 				 S_LOGGER.error(ex.getLocalizedMessage());
 			 }
 			 throw new PhrescoException(ex);
 		 } finally {
 			 if (writer != null) {
 				 try {
 					 writer.close();
 				 } catch (IOException e) {
 					 if (debugEnabled) {
 						 S_LOGGER.error(e.getLocalizedMessage());
 					 }
 				 }
 			 }
 		 }
 	 }
 	
     private CIJobStatus configureJob(CIJob job, String jobType) throws PhrescoException {
     	if (debugEnabled) {
     		S_LOGGER.debug("Entering Method CIManagerImpl.createJob(CIJob job)");
     	}
     	try {
             cli = getCLI(job);
             List<String> argList = new ArrayList<String>();
             argList.add(jobType);
             argList.add(job.getName());
             
             String jenkinsTemplateDir = Utility.getJenkinsTemplateDir();
             String configFilePath = jenkinsTemplateDir + job.getRepoType() + HYPHEN + CONFIG_XML;
         	if (debugEnabled) {
         		S_LOGGER.debug("configFilePath ...  " + configFilePath);
         	}
         	
             File configFile = new File(configFilePath);
             ConfigProcessor processor = new ConfigProcessor(configFile);
             customizeNodes(processor, job);
             
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             if (debugEnabled) {
             	S_LOGGER.debug("argList " + argList.toString());
             }
             int result = cli.execute(argList, processor.getConfigAsStream(), System.out, baos);
             
             String message = "Job created successfully";
             if (result == -1) { 
             	byte[] byteArray = baos.toByteArray();
             	message = new String(byteArray);
             }
             if (debugEnabled) {
             	S_LOGGER.debug("message " + message);
             }
             //when svn is selected credential value has to set
             if(SVN.equals(job.getRepoType())) {
             	setSvnCredential(job);
             }
             
             setMailCredential(job);
             return new CIJobStatus(result, message);
         } catch (IOException e) {
             throw new PhrescoException(e);
         } catch (JDOMException e) {
             throw new PhrescoException(e);
         } finally {
             if (cli != null) {
                 try {
                     cli.close();
                 } catch (IOException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error(e.getLocalizedMessage());
             		}
                 } catch (InterruptedException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error(e.getLocalizedMessage());
             		}
                 }
             }
         }
     }
     
     private void setSvnCredential(CIJob job) throws JDOMException, IOException {
        	S_LOGGER.debug("Entering Method CIManagerImpl.setSvnCredential");
         try {
             String jenkinsTemplateDir = Utility.getJenkinsTemplateDir();
             String credentialFilePath = jenkinsTemplateDir + job.getRepoType() + HYPHEN + CREDENTIAL_XML;
             if (debugEnabled) {
             	S_LOGGER.debug("credentialFilePath ... " + credentialFilePath);
             }
             File credentialFile = new File(credentialFilePath);
             
             SvnProcessor processor = new SvnProcessor(credentialFile);
             
 //			DataInputStream in = new DataInputStream(new FileInputStream(credentialFile));
 //			while (in.available() != 0) {
 //				System.out.println(in.readLine());
 //			}
 //			in.close();
 			
             processor.changeNodeValue("credentials/entry//userName", job.getUserName());
             processor.changeNodeValue("credentials/entry//password", job.getPassword());
             processor.writeStream(new File(Utility.getJenkinsHome() + File.separator + job.getName()));
             
             //jenkins home location
 			String jenkinsJobHome = System.getenv(JENKINS_HOME);
             StringBuilder builder = new StringBuilder(jenkinsJobHome);
             builder.append(File.separator);
             
             processor.writeStream(new File(builder.toString() + CI_CREDENTIAL_XML));
 		} catch (Exception e) {
        		S_LOGGER.error("Entered into the catch block of CIManagerImpl.setSvnCredential " + e.getLocalizedMessage());
 		}
     }
     
     private void setMailCredential(CIJob job) {
         if (debugEnabled) {
         	S_LOGGER.debug("Entering Method CIManagerImpl.setMailCredential");
         }
         try {
             String jenkinsTemplateDir = Utility.getJenkinsTemplateDir();
             String mailFilePath = jenkinsTemplateDir + MAIL + HYPHEN + CREDENTIAL_XML;
             if (debugEnabled) {
             	S_LOGGER.debug("configFilePath ... " + mailFilePath);
             }
             File mailFile = new File(mailFilePath);
             
             SvnProcessor processor = new SvnProcessor(mailFile);
             
 //			DataInputStream in = new DataInputStream(new FileInputStream(mailFile));
 //			while (in.available() != 0) {
 //				System.out.println(in.readLine());
 //			}
 //			in.close();
 			
 			// Mail have to go with jenkins running email address
 			InetAddress ownIP = InetAddress.getLocalHost();
 			processor.changeNodeValue(CI_HUDSONURL, HTTP_PROTOCOL + PROTOCOL_POSTFIX + ownIP.getHostAddress() + COLON + job.getJenkinsPort() + FORWARD_SLASH + CI + FORWARD_SLASH);
             processor.changeNodeValue("smtpAuthUsername", job.getSenderEmailId());
             processor.changeNodeValue("smtpAuthPassword", job.getSenderEmailPassword());
             processor.changeNodeValue("adminAddress", job.getSenderEmailId());
             
             //jenkins home location
 			String jenkinsJobHome = System.getenv(JENKINS_HOME);
             StringBuilder builder = new StringBuilder(jenkinsJobHome);
             builder.append(File.separator);
             
             processor.writeStream(new File(builder.toString() + CI_MAILER_XML));
 		} catch (Exception e) {
        		S_LOGGER.error("Entered into the catch block of CIManagerImpl.setMailCredential " + e.getLocalizedMessage());
 		}
     }
     
     private CIJobStatus buildJob(CIJob job) throws PhrescoException {
     	if (debugEnabled) {
     		S_LOGGER.debug("Entering Method CIManagerImpl.buildJob(CIJob job)");
     	}
     	cli = getCLI(job);
         
         List<String> argList = new ArrayList<String>();
         argList.add(FrameworkConstants.CI_BUILD_JOB_COMMAND);
         argList.add(job.getName());
         try {
             int status = cli.execute(argList);
             String message = FrameworkConstants.CI_BUILD_STARTED;
             if (status == FrameworkConstants.JOB_STATUS_NOTOK) {
                 message = FrameworkConstants.CI_BUILD_STARTING_ERROR;
             }
             return new CIJobStatus(status, message);
         } finally {
             if (cli != null) {
                 try {
                     cli.close();
                 } catch (IOException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error(e.getLocalizedMessage());
             		}
                 } catch (InterruptedException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error(e.getLocalizedMessage());
             		}
                 }
             }
         }
     }
     
     private JsonArray getBuildsArray(CIJob job) throws PhrescoException {
     	try {
         	String jenkinsUrl = "http://" + job.getJenkinsUrl() + ":" + job.getJenkinsPort() + "/ci/";
         	String jobNameUtf8 = job.getName().replace(" ", "%20");
         	String buildsJsonUrl = jenkinsUrl + "job/" + jobNameUtf8 + "/api/json";
             String jsonResponse = getJsonResponse(buildsJsonUrl);
             
             JsonParser parser = new JsonParser();
             JsonElement jsonElement = parser.parse(jsonResponse);
             JsonObject jsonObject = jsonElement.getAsJsonObject();
             JsonElement element = jsonObject.get(FrameworkConstants.CI_JOB_JSON_BUILDS);
             
             JsonArray jsonArray = element.getAsJsonArray();
             
             return jsonArray;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
     }
     
     public List<CIBuild> getBuilds(CIJob job) throws PhrescoException {
     	if (debugEnabled) {
     		S_LOGGER.debug("Entering Method CIManagerImpl.getCIBuilds(CIJob job)");
     	}
     	List<CIBuild> ciBuilds = null;
         try {
         	if (debugEnabled) {
         		S_LOGGER.debug("getCIBuilds()  JobName = "+ job.getName());
         	}
             JsonArray jsonArray = getBuildsArray(job);
             ciBuilds = new ArrayList<CIBuild>(jsonArray.size());
             Gson gson = new Gson();
             CIBuild ciBuild = null;
             for (int i = 0; i < jsonArray.size(); i++) {
                 ciBuild = gson.fromJson(jsonArray.get(i), CIBuild.class);
                 setBuildStatus(ciBuild, job);
         		String buildUrl = ciBuild.getUrl();
         		String jenkinUrl = job.getJenkinsUrl() + ":" + job.getJenkinsPort();
         		buildUrl = buildUrl.replaceAll("localhost:" + job.getJenkinsPort(), jenkinUrl); // when displaying url it should display setup machine ip
         		ciBuild.setUrl(buildUrl);
                 ciBuilds.add(ciBuild);
             }
         } catch(Exception e) {
         	if (debugEnabled) {
         		S_LOGGER.debug("Entering Method CIManagerImpl.getCIBuilds(CIJob job) " + e.getLocalizedMessage());
         	}
         }
         return ciBuilds;
     }
     
     private void setBuildStatus(CIBuild ciBuild, CIJob job) throws PhrescoException {
    		S_LOGGER.debug("Entering Method CIManagerImpl.setBuildStatus(CIBuild ciBuild)");
    		S_LOGGER.debug("setBuildStatus()  url = "+ ciBuild.getUrl());
 		String buildUrl = ciBuild.getUrl();
 		String jenkinsUrl = job.getJenkinsUrl() + ":" + job.getJenkinsPort();
 		buildUrl = buildUrl.replaceAll("localhost:" + job.getJenkinsPort(), jenkinsUrl); // display the jenkins running url in ci list
     	String response = getJsonResponse(buildUrl + API_JSON);
         JsonParser parser = new JsonParser();
         JsonElement jsonElement = parser.parse(response);
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         
         JsonElement resultJson = jsonObject.get(FrameworkConstants.CI_JOB_BUILD_RESULT);
         JsonElement idJson = jsonObject.get(FrameworkConstants.CI_JOB_BUILD_ID);
         JsonElement timeJson = jsonObject.get(FrameworkConstants.CI_JOB_BUILD_TIME_STAMP);
         JsonArray asJsonArray = jsonObject.getAsJsonArray(FrameworkConstants.CI_JOB_BUILD_ARTIFACTS);
         
         if(jsonObject.get(FrameworkConstants.CI_JOB_BUILD_RESULT).toString().equals(STRING_NULL)) { // when build is result is not known
         	ciBuild.setStatus(INPROGRESS);
         } else if(resultJson.getAsString().equals(CI_SUCCESS_FLAG) && asJsonArray.size() < 1) { // when build is success and zip relative path is not added in json
             ciBuild.setStatus(INPROGRESS);
         } else {
         	ciBuild.setStatus(resultJson.getAsString());
         	//download path
         	for (JsonElement jsonArtElement : asJsonArray) {
         		String buildDownloadZip = jsonArtElement.getAsJsonObject().get(FrameworkConstants.CI_JOB_BUILD_DOWNLOAD_PATH).toString();
         		if (buildDownloadZip.endsWith(CI_ZIP)) {
         			if (debugEnabled) {
         				S_LOGGER.debug("download artifact " + buildDownloadZip);
         			}
         			ciBuild.setDownload(buildDownloadZip);
         		}
     		}
         }
 
         ciBuild.setId(idJson.getAsString());
         String dispFormat = DD_MM_YYYY_HH_MM_SS;
         ciBuild.setTimeStamp(getDate(timeJson.getAsString(), dispFormat));
     }
 
     private String getDate(String timeStampStr, String format) {
         DateFormat formatter = new SimpleDateFormat(format);
         long timeStamp = Long.parseLong(timeStampStr);
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(timeStamp);
         return formatter.format(calendar.getTime());
     }
     
     private String getJsonResponse(String jsonUrl) throws PhrescoException {
     	if (debugEnabled) {
     		S_LOGGER.debug("Entering Method CIManagerImpl.getJsonResponse(String jsonUrl)");
 			S_LOGGER.debug("getJsonResponse() JSonUrl = "+jsonUrl);
     	}
 		try {
 	        HttpClient httpClient = new DefaultHttpClient();
 	        HttpGet httpget = new HttpGet(jsonUrl);
 	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
             return httpClient.execute(httpget, responseHandler);
         } catch (IOException e) {
             throw new PhrescoException(e);
         }
     }
     
     private CLI getCLI(CIJob job) throws PhrescoException {
     	if (debugEnabled) {
     		S_LOGGER.debug("Entering Method CIManagerImpl.getCLI()");
     	}
         String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + job.getJenkinsUrl() + COLON + job.getJenkinsPort() + FORWARD_SLASH + CI + FORWARD_SLASH;
     	if (debugEnabled) {
     		S_LOGGER.debug("jenkinsUrl to get cli object " + jenkinsUrl);
     	}
         try {
             return new CLI(new URL(jenkinsUrl));
         } catch (MalformedURLException e) {
             throw new PhrescoException(e);
         } catch (IOException e) {
             throw new PhrescoException(e);
         } catch (InterruptedException e) {
             throw new PhrescoException(e);
         }
     }
     
     private void customizeNodes(ConfigProcessor processor, CIJob job) throws JDOMException,PhrescoException {
     	
         //SVN url customization
     	if (SVN.equals(job.getRepoType())) {
     		S_LOGGER.debug("This is svn type project!!!!!");
     		processor.changeNodeValue(SCM_LOCATIONS_REMOTE, job.getSvnUrl());
     	} else if (GIT.equals(job.getRepoType())) {
     		S_LOGGER.debug("This is git type project!!!!!");
     		processor.changeNodeValue(SCM_USER_REMOTE_CONFIGS_URL, job.getSvnUrl());
     		processor.changeNodeValue(SCM_BRANCHES_NAME, job.getBranch());
     		// cloned workspace
     	} else if (CLONED_WORKSPACE.equals(job.getRepoType())) {
     		S_LOGGER.debug("Clonned workspace selected!!!!!!!!!!");
     		processor.useClonedScm(job.getUsedClonnedWorkspace(), SUCCESSFUL);
     	}
         
         //Schedule expression customization
         processor.changeNodeValue(TRIGGERS_SPEC, job.getScheduleExpression());
         
         //Triggers Implementation
         List<String> triggers = job.getTriggers();
         
         processor.createTriggers(TRIGGERS, triggers, job.getScheduleExpression());
         
         //if the technology is java stanalone and functional test , goal have to specified in post build step only
         if(job.isEnablePostBuildStep() && FUNCTIONAL_TEST.equals(job.getOperation())) {
             //Maven command customization
             processor.changeNodeValue(GOALS, CI_FUNCTIONAL_ADAPT.trim());
         } else {
             //Maven command customization
             processor.changeNodeValue(GOALS, job.getMvnCommand());
         }
         
         //Recipients customization
         Map<String, String> email = job.getEmail();
         
         //Failure Reception list
         processor.changeNodeValue(TRIGGER_FAILURE_EMAIL_RECIPIENT_LIST, (String)email.get(FAILURE_EMAILS));
         
         //Success Reception list
         processor.changeNodeValue(TRIGGER_SUCCESS__EMAIL_RECIPIENT_LIST, (String)email.get(SUCCESS_EMAILS));
         
         //enable collabnet file release plugin integration
         if (job.isEnableBuildRelease()) {
         	S_LOGGER.debug("Enablebling collabnet file release plugin ");
         	processor.enableCollabNetBuildReleasePlugin(job);
         }
 
         // use clonned scm
         if(CLONED_WORKSPACE.equals(job.getRepoType())) {
         	S_LOGGER.debug("using cloned workspace ");
         	processor.useClonedScm(job.getUsedClonnedWorkspace(), SUCCESSFUL);
         }
         
         // clone workspace for future use
         if (job.isCloneWorkspace()) { 
         	S_LOGGER.debug("Clonning the workspace ");
             processor.cloneWorkspace(ALL_FILES, SUCCESSFUL, TAR);
         }
         
         // Build Other projects
         if (StringUtils.isNotEmpty(job.getDownStreamProject())) {
         	S_LOGGER.debug("Enabling downstream project!!!!!!");
             processor.buildOtherProjects(job.getDownStreamProject());
         }
         
         // pom location specifier 
         if (StringUtils.isNotEmpty(job.getPomLocation())) {
         	S_LOGGER.debug("POM location changing " + job.getPomLocation());
         	processor.updatePOMLocation(job.getPomLocation());
         }
         
         if (job.isEnablePostBuildStep()) {
         	System.out.println("java stanalone technology with functional test enabled!!!!!!!");
         	String mvnCommand = job.getMvnCommand();
 			String[] ciAdapted = mvnCommand.split(CI_FUNCTIONAL_ADAPT); // java stanalone functional test alone
 			for (String ciCommand : ciAdapted) {
 				S_LOGGER.debug("ciCommand...." + ciCommand);
 			}
 			// iterate over loop
         	processor.enablePostBuildStep(job.getPomLocation(), ciAdapted[1]);
         }
         
         if (job.isEnablePreBuildStep()) {
         	System.out.println("java stanalone technology with functional test enabled!!!!!!!");
         	//iterate over loop
         	List<String> prebuildStepCommands = job.getPrebuildStepCommands();
         	for (String prebuildStepCommand : prebuildStepCommands) {
         		processor.enablePreBuildStep(job.getPomLocation(), prebuildStepCommand);
 			}
         }
         
     }
     
     private CIJobStatus deleteCI(CIJob job, List<String> builds) throws PhrescoException {
     	S_LOGGER.debug("Entering Method CIManagerImpl.deleteCI(CIJob job)");
     	S_LOGGER.debug("Job name " + job.getName());
     	cli = getCLI(job);
         String deleteType = null;
         List<String> argList = new ArrayList<String>();
         S_LOGGER.debug("job name " + job.getName());
         S_LOGGER.debug("Builds " + builds);
         if(CollectionUtils.isEmpty(builds)) {	// delete job
         	S_LOGGER.debug("Job deletion started");
         	S_LOGGER.debug("Command " + FrameworkConstants.CI_JOB_DELETE_COMMAND);
         	deleteType = DELETE_TYPE_JOB;
         	argList.add(FrameworkConstants.CI_JOB_DELETE_COMMAND);
             argList.add(job.getName());
         } else {								// delete Build
         	S_LOGGER.debug("Build deletion started");
         	deleteType = DELETE_TYPE_BUILD;
         	argList.add(FrameworkConstants.CI_BUILD_DELETE_COMMAND);
             argList.add(job.getName());
     	    StringBuilder result = new StringBuilder();
     	    for(String string : builds) {
     	        result.append(string);
     	        result.append(",");
     	    }
     	    String buildNos = result.substring(0, result.length() - 1);
         	argList.add(buildNos);
     		S_LOGGER.debug("Command " + FrameworkConstants.CI_BUILD_DELETE_COMMAND);
     		S_LOGGER.debug("Build numbers " + buildNos);
         }
         try {
             int status = cli.execute(argList);
            String message = deleteType + " deletion started in jenkins";
             if (status == FrameworkConstants.JOB_STATUS_NOTOK) {
             	deleteType = deleteType.substring(0, 1).toLowerCase() + deleteType.substring(1);
                 message = "Error while deleting " + deleteType +" in jenkins";
             }
         	S_LOGGER.debug("Delete CI Status " + status);
         	S_LOGGER.debug("Delete CI Message " + message);
             return new CIJobStatus(status, message);
         } finally {
             if (cli != null) {
                 try {
                     cli.close();
                 } catch (IOException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error("Entered into catch block of CIManagerImpl.deleteCI(CIJob job) " + e.getLocalizedMessage());
             		}
                 } catch (InterruptedException e) {
                 	if (debugEnabled) {
                 		S_LOGGER.error("Entered into catch block of CIManagerImpl.deleteCI(CIJob job) " + e.getLocalizedMessage());
             		}
                 }
             }
         }
     }
     
 	 private CIJob getJob(ApplicationInfo appInfo) throws PhrescoException {
 		 Gson gson = new Gson();
 		 try {
 			 BufferedReader br = new BufferedReader(new FileReader(getCIJobPath(appInfo)));
 			 CIJob job = gson.fromJson(br, CIJob.class);
 			 br.close();
 			 return job;
 		 } catch (FileNotFoundException e) {
 			 S_LOGGER.debug(e.getLocalizedMessage());
 			 return null;
 		 } catch (com.google.gson.JsonParseException e) {
 			 S_LOGGER.debug("it is already adpted project !!!!! " + e.getLocalizedMessage());
 			 return null;
 		 } catch (IOException e) {
 			 S_LOGGER.debug(e.getLocalizedMessage());
 			 return null;
 		 }
 	 }
 
 	 private boolean adaptExistingJobs(ApplicationInfo appInfo) {
 		try {
 			 CIJob existJob = getJob(appInfo);
 			 S_LOGGER.debug("Going to get existing jobs to relocate!!!!!");
 			 if(existJob != null) {
 				 S_LOGGER.debug("Existing job found " + existJob.getName());
 				 boolean deleteExistJob = deleteCIJobFile(appInfo);
 				 Gson gson = new Gson();
 				 List<CIJob> existingJobs = new ArrayList<CIJob>();
 				 existingJobs.addAll(Arrays.asList(existJob));
 				 FileWriter writer = null;
 				 File ciJobFile = new File(getCIJobPath(appInfo));
 				 String jobJson = gson.toJson(existingJobs);
 				 writer = new FileWriter(ciJobFile);
 				 writer.write(jobJson);
 				 writer.flush();
 				 S_LOGGER.debug("Existing job moved to new type of project!!");
 			 }
 			 return true;
 		} catch (Exception e) {
 			S_LOGGER.debug("It is already adapted !!!!! ");
 		}
 		return false;
 	 }
 	 
 	 public List<CIJob> getJobs(ApplicationInfo appInfo) throws PhrescoException {
 		 S_LOGGER.debug("GetJobs Called!");
 		 try {
 			 boolean adaptedProject = adaptExistingJobs(appInfo);
 			 S_LOGGER.debug("Project adapted for new feature => " + adaptedProject);
 			 Gson gson = new Gson();
 			 BufferedReader br = new BufferedReader(new FileReader(getCIJobPath(appInfo)));
 			 Type type = new TypeToken<List<CIJob>>(){}.getType();
 			 List<CIJob> jobs = gson.fromJson(br, type);
 			 br.close();
 			 return jobs;
 		 } catch (FileNotFoundException e) {
 			 S_LOGGER.debug("FileNotFoundException");
 			 return null;
 		 } catch (IOException e) {
 			 S_LOGGER.debug("IOException");
 			 throw new PhrescoException(e);
 		 }
 	 }
 	 
 	 public CIJob getJob(ApplicationInfo appInfo, String jobName) throws PhrescoException {
 		 try {
 			 S_LOGGER.debug("Search for jobName => " + jobName);
 			 if (StringUtils.isEmpty(jobName)) {
 				 return null;
 			 }
 			 List<CIJob> jobs = getJobs(appInfo);
 			 if(CollectionUtils.isEmpty(jobs)) {
 				 S_LOGGER.debug("job list is empty!!!!!!!!");
 				 return null;
 			 }
 			 S_LOGGER.debug("Job list found!!!!!");
 			 for (CIJob job : jobs) {
 				 S_LOGGER.debug("job list job Names => " + job.getName());
 				 if (job.getName().equals(jobName)) {
 					 return job;
 				 }
 			 }
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 		 return null;
 	 }
 	 
 	 private void writeJsonJobs(ApplicationInfo appInfo, List<CIJob> jobs, String status) throws PhrescoException {
 		 try {
 			 if (jobs == null) {
 				 return;
 			 }
 			 Gson gson = new Gson();
 			 List<CIJob> existingJobs = getJobs(appInfo);
 			 if (CI_CREATE_NEW_JOBS.equals(status) || existingJobs == null) {
 				 existingJobs = new ArrayList<CIJob>();
 			 }
 			 existingJobs.addAll(jobs);
 			 FileWriter writer = null;
 			 File ciJobFile = new File(getCIJobPath(appInfo));
 			 String jobJson = gson.toJson(existingJobs);
 			 writer = new FileWriter(ciJobFile);
 			 writer.write(jobJson);
 			 writer.flush();
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 	 
 	 private void deleteJsonJobs(ApplicationInfo appInfo, List<CIJob> selectedJobs) throws PhrescoException {
 		 try {
 			 if (CollectionUtils.isEmpty(selectedJobs)) {
 				 return;
 			 }
 			 Gson gson = new Gson();
 			 List<CIJob> jobs = getJobs(appInfo);
 			 if (CollectionUtils.isEmpty(jobs)) {
 				 return;
 			 }
 			//all values
 			 Iterator<CIJob> iterator = jobs.iterator();
 			//deletable values
 			 for (CIJob selectedInfo : selectedJobs) {
 				 while (iterator.hasNext()) {
 					 CIJob itrCiJob = iterator.next();
 					 if (itrCiJob.getName().equals(selectedInfo.getName())) {
 						 iterator.remove();
 						 break;
 					 }
 				 }
 			 }
 			 writeJsonJobs(appInfo, jobs, CI_CREATE_NEW_JOBS);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 	 
 	 private void updateJsonJob(ApplicationInfo appInfo, CIJob job) throws PhrescoException {
 		 try {
 			 deleteJsonJobs(appInfo, Arrays.asList(job));
 			 writeJsonJobs(appInfo, Arrays.asList(job), CI_APPEND_JOBS);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 	 
 	 private String getCIJobPath(ApplicationInfo appInfo) {
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(appInfo.getAppDirName());
 		 builder.append(File.separator);
 		 builder.append(FOLDER_DOT_PHRESCO);
 		 builder.append(File.separator);
 		 builder.append(CI_JOB_INFO_NAME);
 		 return builder.toString();
 	 }
 	 
 	 public CIJobStatus buildJobs(ApplicationInfo appInfo, List<String> jobsName) throws PhrescoException {
 		 try {
 			 CIJobStatus jobStatus = null;
 			 for (String jobName : jobsName) {
 				 CIJob ciJob = getJob(appInfo, jobName);
 				 jobStatus = buildJob(ciJob);
 			 }
 			 return jobStatus;
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error(ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 	 
 	 public CIJobStatus deleteBuilds(ApplicationInfo appInfo,  Map<String, List<String>> builds) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteCI()");
 		 	try {
 				CIJobStatus deleteCI = null;
 				Iterator iterator = builds.keySet().iterator();  
 				while (iterator.hasNext()) {
 				   String jobName = iterator.next().toString();  
 				   List<String> deleteBuilds = builds.get(jobName);
 				   S_LOGGER.debug("jobName " + jobName + " builds " + deleteBuilds);
 				   CIJob ciJob = getJob(appInfo, jobName);
 					 //job and build numbers
 				   deleteCI = deleteCI(ciJob, deleteBuilds);
 				}
 			 return deleteCI;
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error("Entered into catch block of ProjectAdministratorImpl.deleteCI()" + ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 
 	 public CIJobStatus deleteJobs(ApplicationInfo appInfo,  List<String> jobNames) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteCI()");
 		 try {
 			CIJobStatus deleteCI = null;
 			for (String jobName : jobNames) {
 				S_LOGGER.debug(" Deleteable job name " + jobName);
 				CIJob ciJob = getJob(appInfo, jobName);
 				 //job and build numbers
 				 deleteCI = deleteCI(ciJob, null);
 				 S_LOGGER.debug("write back json data after job deletion successfull");
 				 deleteJsonJobs(appInfo, Arrays.asList(ciJob));
 			}
 			 return deleteCI;
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error("Entered into catch block of ProjectAdministratorImpl.deleteCI()" + ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 	 
 	// When already existing adapted project is created , need to move to new adapted project
 	private boolean deleteCIJobFile(ApplicationInfo appInfo) throws PhrescoException {
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteCI()");
 		try {
        	File ciJobInfo = new File(getCIJobPath(appInfo));
        	return ciJobInfo.delete();
 		} catch (ClientHandlerException ex) {
 	    	S_LOGGER.error("Entered into catch block of ProjectAdministratorImpl.deleteCI()" + ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 		
 	public boolean isJobCreatingBuild(CIJob ciJob) throws PhrescoException {
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl.isBuilding()");
 		try {
 			int isBuilding = getProgressInBuild(ciJob);
 			if (isBuilding > 0) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	private int getProgressInBuild(CIJob job) throws PhrescoException {
 		S_LOGGER.debug("Entering Method CIManagerImpl.isBuilding(CIJob job)");
 		String jenkinsUrl = HTTP_PROTOCOL + PROTOCOL_POSTFIX + job.getJenkinsUrl() + COLON + job.getJenkinsPort() + FORWARD_SLASH + CI + FORWARD_SLASH;
 		String isBuildingUrlUrl = BUSY_EXECUTORS;
 		String jsonResponse = getJsonResponse(jenkinsUrl + isBuildingUrlUrl);
 		int buidInProgress = Integer.parseInt(jsonResponse);
 		S_LOGGER.debug("buidInProgress " + buidInProgress);
 		return buidInProgress;
 	}
 	    
 	public int getTotalBuilds(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			CIJob ciJob = getJob(appInfo);
 			return getTotalBuilds(ciJob);
 		} catch (ClientHandlerException ex) {
 			S_LOGGER.error(ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 		 
 	private int getTotalBuilds(CIJob job) throws PhrescoException {
 		try {
 			S_LOGGER.debug("Entering Method CIManagerImpl.getTotalBuilds(CIJob job)");
 			S_LOGGER.debug("getCIBuilds()  JobName = " + job.getName());
 			JsonArray jsonArray = getBuildsArray(job);
 			Gson gson = new Gson();
 			CIBuild ciBuild = null;
 			if (jsonArray.size() > 0) {
 				ciBuild = gson.fromJson(jsonArray.get(0), CIBuild.class);
 				String buildUrl = ciBuild.getUrl();
 				String jenkinsUrl = job.getJenkinsUrl() + ":" + job.getJenkinsPort();
 				// display the jenkins running url in ci
 				buildUrl = buildUrl.replaceAll("localhost:" + job.getJenkinsPort(), jenkinsUrl);
 																			// list
 				String response = getJsonResponse(buildUrl + API_JSON);
 				JsonParser parser = new JsonParser();
 				JsonElement jsonElement = parser.parse(response);
 				JsonObject jsonObject = jsonElement.getAsJsonObject();
 				JsonElement resultJson = jsonObject.get(FrameworkConstants.CI_JOB_BUILD_RESULT);
 				JsonArray asJsonArray = jsonObject.getAsJsonArray(FrameworkConstants.CI_JOB_BUILD_ARTIFACTS);
 				// when build result is not known
 				if (jsonObject.get(FrameworkConstants.CI_JOB_BUILD_RESULT).toString().equals(STRING_NULL)) {
 					// it indicates the job is in progress and not yet completed
 					return -1;
 				// when build is success and build zip relative path is unknown 
 				} else if (resultJson.getAsString().equals(CI_SUCCESS_FLAG) && asJsonArray.size() < 1) {
 					return -1;
 				} else {
 					return jsonArray.size();
 				}
 			} else {
 				return -1; // When the project is build first time,
 			}
 		} catch (ClientHandlerException ex) {
 			S_LOGGER.error(ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 }
