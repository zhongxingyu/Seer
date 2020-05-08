 /**
  * Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
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
 package com.jsmadja.wall.hudsonclient;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.WebApplicationException;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.xerces.dom.ElementNSImpl;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 
 import com.jsmadja.wall.hudsonclient.builder.HudsonUrlBuilder;
 import com.jsmadja.wall.hudsonclient.builder.TestResultBuilder;
 import com.jsmadja.wall.hudsonclient.domain.HudsonBuild;
 import com.jsmadja.wall.hudsonclient.domain.HudsonProject;
 import com.jsmadja.wall.hudsonclient.generated.hudson.hudsonmodel.HudsonModelHudson;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmoduleset.HudsonMavenMavenModule;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmoduleset.HudsonMavenMavenModuleSet;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmoduleset.HudsonModelJob;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmoduleset.HudsonModelRun;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmodulesetbuild.HudsonMavenMavenModuleSetBuild;
 import com.jsmadja.wall.hudsonclient.generated.hudson.mavenmodulesetbuild.HudsonModelUser;
 import com.jsmadja.wall.projectwall.domain.TestResult;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class Hudson {
 
     private static final int UNBUILT_PROJECT = -1;
 
     private static final Logger LOG = LoggerFactory.getLogger(Hudson.class);
 
     private static final String DEFAULT_STATE = "NEW";
 
     private HudsonUrlBuilder hudsonUrlBuilder;
     private TestResultBuilder hudsonTestService = new TestResultBuilder();
 
     private Client client;
 
     private Cache cache;
 
     public Hudson(String hudsonUrl) {
         hudsonUrlBuilder = new HudsonUrlBuilder(hudsonUrl);
         ClientConfig clientConfig = new DefaultClientConfig();
         clientConfig.getClasses();
         client = buildJerseyClient(clientConfig);
 
         if (LOG.isInfoEnabled()) {
             LOG.info("Initialize hudson with url " + hudsonUrl);
         }
 
         CacheManager cacheManager = CacheManager.create();
         cache = cacheManager.getCache("hudson_projects_cache");
     }
 
     /**
      * @return List of all available projects on Hudson
      */
     public final List<HudsonProject> findAllProjects() {
         String projectsUrl = hudsonUrlBuilder.getAllProjectsUrl();
         if (LOG.isDebugEnabled()) {
             LOG.debug("All project url : " + projectsUrl);
         }
         WebResource hudsonResource = client.resource(projectsUrl);
         HudsonModelHudson hudson = hudsonResource.get(HudsonModelHudson.class);
         List<HudsonProject> projects = new ArrayList<HudsonProject>();
         List<Object> hudsonProjects = hudson.getJob();
         if (LOG.isDebugEnabled()) {
             LOG.debug(hudsonProjects.size()+" projects to update");
         }
 
         int idxProject = 1;
         for (Object project : hudsonProjects) {
             ElementNSImpl element = (ElementNSImpl) project;
             String name = getProjectName(element);
             if (LOG.isDebugEnabled()) {
                 LOG.debug(idxProject+"/"+hudsonProjects.size()+" create project "+name);
                 idxProject++;
             }
 
             try {
                 HudsonProject hudsonProject = findProject(name);
                 projects.add(hudsonProject);
             } catch (HudsonProjectNotFoundException e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Can't find project with name [" + name
                             + "] but should be because the list comes from Hudson itself", e);
                 }
             }
         }
         return projects;
     }
 
     /**
      * @param projectName
      * @param buildNumber
      * @return HudsonBuild found in Hudson with its project name and build number
      * @throws HudsonBuildNotFoundException
      */
     public final HudsonBuild findBuild(String projectName, int buildNumber) throws HudsonBuildNotFoundException {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Find build with project name [" + projectName + "] and buildNumber [" + buildNumber + "]");
             }
 
             HudsonMavenMavenModuleSetBuild setBuild = findBuildByProjectNameAndBuildNumber(projectName, buildNumber);
             return createHudsonBuildFrom(projectName, buildNumber, setBuild);
         } catch (UniformInterfaceException e) {
             String message = "No build #" + buildNumber + " for project " + projectName;
             if (LOG.isDebugEnabled()) {
                 LOG.debug(message, e);
             }
             throw new HudsonBuildNotFoundException(message, e);
         } catch(WebApplicationException e) {
             String message = "Error while loading build #" + buildNumber + " for project " + projectName;
             if (LOG.isDebugEnabled()) {
                 LOG.debug(message, e);
             }
             throw new HudsonBuildNotFoundException(message, e);
         }
     }
 
     private HudsonBuild createHudsonBuildFrom(String projectName, int buildNumber, HudsonMavenMavenModuleSetBuild setBuild) {
         String cacheKey = "hudsonbuild_"+projectName+"_"+buildNumber;
        if (cache.isKeyInCache(cacheKey)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(cacheKey+" is in cache");
             }
            return (HudsonBuild) cache.get(cacheKey).getObjectValue();
         }
 
         String testResultUrl = hudsonUrlBuilder.getTestResultUrl(projectName, buildNumber);
         WebResource testResultResource = client.resource(testResultUrl);
         TestResult testResult = hudsonTestService.build(testResultResource);
 
         HudsonBuild hudsonBuild = new HudsonBuild();
         hudsonBuild.setState(getState(setBuild));
         hudsonBuild.setDuration(setBuild.getDuration());
         hudsonBuild.setStartTime(new Date(setBuild.getTimestamp()));
         hudsonBuild.setSuccessful(isSuccessful(setBuild));
         hudsonBuild.setCommiters(getCommiters(setBuild));
         hudsonBuild.setTestResult(testResult);
         hudsonBuild.setBuildNumber(buildNumber);
 
         cache.put(new Element(cacheKey, hudsonBuild));
         if (LOG.isDebugEnabled()) {
             LOG.debug("put "+cacheKey+" in cache");
         }
 
         return hudsonBuild;
     }
 
     private String getState(HudsonMavenMavenModuleSetBuild setBuild) {
         if (setBuild == null) {
             return "NEW";
         }
         ElementNSImpl element = (ElementNSImpl)  setBuild.getResult();
         if (element == null) {
             return "NEW";
         }
 
         return element.getFirstChild().getNodeValue();
     }
 
     /**
      * @param projectName
      * @return HudsonProject found with its name
      * @throws HudsonProjectNotFoundException
      */
     public final HudsonProject findProject(String projectName) throws HudsonProjectNotFoundException {
         try {
             HudsonMavenMavenModuleSet moduleSet = findJobByProjectName(projectName);
             return createHudsonProjectFrom(moduleSet);
         } catch (HudsonBuildNotFoundException e) {
             throw new HudsonProjectNotFoundException(e);
         }
     }
 
     /**
      * If there is no success job in history, the average build duration time is the max duration time
      * Else we compute the average success build duration
      * @param projectName
      * @return Average build duration time computed with old successful jobs
      * @throws HudsonProjectNotFoundException
      */
     public final long getAverageBuildDurationTime(String projectName) throws HudsonProjectNotFoundException {
         HudsonProject hudsonProject = findProject(projectName);
         long averageTime;
 
         if (isNeverSuccessful(hudsonProject)) {
             averageTime = maxDurationTime(hudsonProject);
         } else {
             float sumBuildDurationTime = 0;
             int[] builds = hudsonProject.getBuildNumbers();
             for (int buildNumber : builds) {
                 try {
                     HudsonBuild build = findBuild(projectName, buildNumber);
                     if (build.isSuccessful()) {
                         sumBuildDurationTime += build.getDuration();
                     }
                 } catch (HudsonBuildNotFoundException e) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(e.getMessage());
                     }
                 }
             }
             averageTime = (long) (sumBuildDurationTime / builds.length);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Average build time of " + projectName + " is " + averageTime + " ms");
         }
 
         return averageTime;
     }
 
     private long maxDurationTime(HudsonProject hudsonProject) {
         long max = 0;
         int[] builds = hudsonProject.getBuildNumbers();
         for (int buildNumber : builds) {
             try {
                 HudsonBuild build = findBuild(hudsonProject.getName(), buildNumber);
                 if (build.getDuration() > max) {
                     max = build.getDuration();
                 }
             } catch (HudsonBuildNotFoundException e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug(e.getMessage());
                 }
             }
         }
         return max;
     }
 
     private boolean isNeverSuccessful(HudsonProject hudsonProject) {
         int[] builds = hudsonProject.getBuildNumbers();
         for (int buildNumber : builds) {
             try {
                 HudsonBuild build = findBuild(hudsonProject.getName(), buildNumber);
                 if (build.isSuccessful()) {
                     return false;
                 }
             } catch (HudsonBuildNotFoundException e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug(e.getMessage());
                 }
             }
         }
         return true;
     }
 
     private String[] getCommiters(HudsonMavenMavenModuleSetBuild setBuild) {
         List<HudsonModelUser> users = setBuild.getCulprit();
         String[] commiters = new String[users.size()];
         for (int i = 0; i < users.size(); i++) {
             commiters[i] = users.get(i).getFullName();
         }
         return commiters;
     }
 
     private boolean getIsBuilding(HudsonModelJob modelJob) {
         String color = modelJob.getColor().value();
         return color.endsWith("_anime");
     }
 
     private HudsonProject createHudsonProjectFrom(HudsonMavenMavenModuleSet moduleSet) throws HudsonBuildNotFoundException {
         String artifactId = null;
         HudsonBuild lastCompletedHudsonBuild = null, currentHudsonBuild = null;
 
         int lastCompleteBuildNumber = UNBUILT_PROJECT, currentBuildNumber = UNBUILT_PROJECT;
         String name = moduleSet.getName();
         String description = moduleSet.getDescription();
 
         // current build number
         HudsonModelRun currentHudsonRun = moduleSet.getLastBuild();
         if (currentHudsonRun != null) {
             currentBuildNumber = currentHudsonRun.getNumber();
         }
         if (currentBuildNumber != UNBUILT_PROJECT) {
             currentHudsonBuild = findBuild(name, currentBuildNumber);
         }
 
         // last complete build number
         boolean isBuilding = getIsBuilding(moduleSet);
         HudsonModelRun lastCompletedHudsonRun;
         if (isBuilding) {
             lastCompletedHudsonRun = moduleSet.getLastCompletedBuild();
         } else {
             lastCompletedHudsonRun = moduleSet.getLastBuild();
         }
         if (lastCompletedHudsonRun != null) {
             lastCompleteBuildNumber = lastCompletedHudsonRun.getNumber();
         }
         if (lastCompleteBuildNumber != UNBUILT_PROJECT) {
             lastCompletedHudsonBuild = findBuild(name, lastCompleteBuildNumber);
         }
 
         // artifact id
         if (!moduleSet.getModule().isEmpty()) {
             HudsonMavenMavenModule firstModule = moduleSet.getModule().get(0);
             artifactId = firstModule.getName();
         }
 
         HudsonProject hudsonProject = new HudsonProject();
         hudsonProject.setBuilding(isBuilding);
         hudsonProject.setLastBuildNumber(lastCompleteBuildNumber);
         hudsonProject.setName(name);
         hudsonProject.setDescription(description);
         hudsonProject.setArtifactId(artifactId);
         hudsonProject.setBuildNumbers(getBuildNumbers(moduleSet));
         hudsonProject.setCompletedBuild(lastCompletedHudsonBuild);
         hudsonProject.setCurrentBuild(currentHudsonBuild);
         return hudsonProject;
     }
 
     /**
      * @param hudsonProject
      * @return An array of successful build numbers
      */
     public final int[] getSuccessfulBuildNumbers(HudsonProject hudsonProject) {
         List<Integer> successfulBuildNumbers = new ArrayList<Integer>();
         for (Integer buildNumber : hudsonProject.getBuildNumbers()) {
             try {
                 HudsonBuild build = findBuild(hudsonProject.getName(), buildNumber);
                 if (build.isSuccessful()) {
                     successfulBuildNumbers.add(buildNumber);
                 }
             } catch (HudsonBuildNotFoundException e) {
                 LOG.warn(e.getMessage(), e);
             }
         }
         int[] successfulBuilds = new int[successfulBuildNumbers.size()];
         for (int i = 0; i < successfulBuildNumbers.size(); i++) {
             successfulBuilds[i] = successfulBuildNumbers.get(i);
         }
         return successfulBuilds;
     }
 
     private int[] getBuildNumbers(HudsonModelJob modelJob) {
         List<HudsonModelRun> builds = modelJob.getBuild();
         int[] buildNumbers = new int[builds.size()];
         for (int i = 0; i < builds.size(); i++) {
             buildNumbers[i] = builds.get(i).getNumber();
         }
         return buildNumbers;
     }
 
     private boolean isSuccessful(HudsonMavenMavenModuleSetBuild job) {
         ElementNSImpl element = (ElementNSImpl) job.getResult();
 
         if (element == null) {
             return false;
         }
 
         Node result = element.getFirstChild();
         if (result == null) {
             return false;
         }
 
         return "SUCCESS".equals(result.getNodeValue());
     }
 
     private String getProjectName(ElementNSImpl element) {
         return element.getFirstChild().getFirstChild().getNodeValue();
     }
 
     /**
      * @param projectName
      * @return Date which we think the project will finish to build
      * @throws HudsonProjectNotFoundException
      */
     public final Date getEstimatedFinishTime(String projectName) throws HudsonProjectNotFoundException {
         HudsonProject project = findProject(projectName);
         HudsonBuild lastBuild = project.getCompletedBuild();
         Date startTime = lastBuild.getStartTime();
         long averageBuildDurationTime = getAverageBuildDurationTime(projectName);
         DateTime estimatedFinishTime = new DateTime(startTime.getTime()).plus(averageBuildDurationTime);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Estimated finish time of project " + projectName + " is " + estimatedFinishTime + " ms");
         }
 
         return estimatedFinishTime.toDate();
     }
 
     Client buildJerseyClient(ClientConfig clientConfig) {
         return Client.create(clientConfig);
     }
 
     public boolean isBuilding(String projectName) throws HudsonProjectNotFoundException {
         HudsonModelJob job = findJobByProjectName(projectName);
         return getIsBuilding(job);
     }
 
     private HudsonMavenMavenModuleSet findJobByProjectName(String projectName) throws HudsonProjectNotFoundException {
        if (cache.isKeyInCache(projectName)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(projectName+" is in cache");
             }
            return (HudsonMavenMavenModuleSet) cache.get(projectName).getObjectValue();
         }
         try {
             String projectUrl = hudsonUrlBuilder.getProjectUrl(projectName);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Project url : " + projectUrl);
             }
             WebResource projectResource = client.resource(projectUrl);
             HudsonMavenMavenModuleSet moduleSet = projectResource.get(HudsonMavenMavenModuleSet.class);
             cache.put(new Element(projectName, moduleSet));
             if (LOG.isDebugEnabled()) {
                 LOG.debug("put "+projectName+" in cache");
             }
             return moduleSet;
         } catch(UniformInterfaceException e) {
             throw new HudsonProjectNotFoundException(e);
         }
     }
 
     public String getState(String projectName) throws HudsonProjectNotFoundException {
         try {
             int lastBuildNumber = getLastBuildNumber(projectName);
             HudsonMavenMavenModuleSetBuild build = findBuildByProjectNameAndBuildNumber(projectName, lastBuildNumber);
             return getState(build);
         } catch(HudsonProjectNotFoundException e) {
             throw new HudsonProjectNotFoundException(e);
         } catch (HudsonBuildNotFoundException e) {
             return DEFAULT_STATE;
         }
     }
 
     private HudsonMavenMavenModuleSetBuild findBuildByProjectNameAndBuildNumber(String projectName, int buildNumber) {
         String cacheKey = "build_"+projectName+"_"+buildNumber;
        if (cache.isKeyInCache(cacheKey)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(cacheKey+" is in cache");
             }
            return (HudsonMavenMavenModuleSetBuild) cache.get(cacheKey).getObjectValue();
         }
 
         String buildUrl = hudsonUrlBuilder.getBuildUrl(projectName, buildNumber);
         if (LOG.isDebugEnabled()) {
             LOG.debug("Build url : " + buildUrl);
         }
         WebResource jobResource = client.resource(buildUrl);
         HudsonMavenMavenModuleSetBuild setBuild = jobResource.get(HudsonMavenMavenModuleSetBuild.class);
         cache.put(new Element(cacheKey, setBuild));
         if (LOG.isDebugEnabled()) {
             LOG.debug("put "+cacheKey+" in cache");
         }
         return setBuild;
     }
 
     public int getLastBuildNumber(String projectName) throws HudsonProjectNotFoundException, HudsonBuildNotFoundException {
         HudsonMavenMavenModuleSet job = findJobByProjectName(projectName);
         HudsonModelRun run = job.getLastBuild();
         if (run == null) {
             throw new HudsonBuildNotFoundException("Project "+projectName+" has no last build");
         }
         return run.getNumber();
     }
 }
