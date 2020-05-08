 /**
  * Copyright (C) 2012 White Source Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.whitesource.teamcity.agent;
 
 import jetbrains.buildServer.agent.AgentRunningBuild;
 import jetbrains.buildServer.agent.BuildRunnerContext;
 import jetbrains.buildServer.log.Loggers;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildServer.util.StringUtil;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.whitesource.agent.api.ChecksumUtils;
 import org.whitesource.agent.api.model.AgentProjectInfo;
 import org.whitesource.agent.api.model.Coordinates;
 import org.whitesource.agent.api.model.DependencyInfo;
 import org.whitesource.teamcity.common.Constants;
 import org.whitesource.teamcity.common.WssUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Concrete implementation for maven job type.
  * Based on teamcity's report for maven projects.
  *
  * @author Edo.Shor
  */
 public class MavenOssInfoExtractor extends BaseOssInfoExtractor {
 
     /* --- Static members --- */
 
     private static final String MAVEN_BUILD_INFO_XML = "maven-build-info.xml";
 
     private static final String LOG_COMPONENT = "MavenExtractor";
 
     /* --- Members --- */
 
     protected Map<String, String> moduleTokens;
 
     protected boolean ignorePomModules;
 
     private String topMostProjectName;
 
     /* --- Constructors --- */
 
     /**
      * Constructor
      *
      * @param runner
      */
     public MavenOssInfoExtractor(BuildRunnerContext runner) {
         super(runner);
 
         Map<String, String> runnerParameters = runner.getRunnerParameters();
         ignorePomModules = Boolean.parseBoolean(runnerParameters.get(Constants.RUNNER_IGNORE_POM_MODULES));
         moduleTokens = WssUtils.splitParametersMap(runnerParameters.get(Constants.RUNNER_MODULE_TOKENS));
     }
 
     /* --- Concrete implementation methods --- */
 
     @Override
     public Collection<AgentProjectInfo> extract() {
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "Collection started"));
 
         // read maven build info
         Element root = readBuildInfo();
         if (Loggers.AGENT.isDebugEnabled()) {
             XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
             Loggers.AGENT.debug(WssUtils.logMsg(LOG_COMPONENT, xmlOutputter.outputString(root)));
         }
 
         // extract project structure (reactor hierarchy)
         Map<String, Coordinates> hierarchy = new HashMap<String, Coordinates>();
         processHierarchy(root.getChild("hierarchy"), hierarchy);
 
         // extract information of each project
         Collection<AgentProjectInfo> projectInfos = new ArrayList<AgentProjectInfo>();
         Element projects = root.getChild("projects");
         if (projects != null) {
             projectInfos = extractProjects(projects, hierarchy);
 
             Element topMostProject = extractTopProject(root);
             if (topMostProject != null) {
                 topMostProjectName = topMostProject.getChildText("name");
                 if (topMostProjectName == null) {
                     topMostProjectName = topMostProject.getChildText("artifactId");
                 }
             }
         }
 
         return projectInfos;
     }
 
     public String getTopMostProjectName() {
         return topMostProjectName;
     }
 
     /* --- Private methods --- */
 
     private Element readBuildInfo() {
         Element buildInfo;
 
         try {
             AgentRunningBuild build = runner.getBuild();
             File buildInfoFile = new File(build.getBuildTempDirectory(), MAVEN_BUILD_INFO_XML);
             buildInfo = FileUtil.parseDocument(buildInfoFile);
         } catch (JDOMException e) {
             throw new IllegalStateException("Maven build info report bad format.", e);
         } catch (IOException e) {
             throw new IllegalStateException("Error reading maven build info report.", e);
         }
 
         return buildInfo;
     }
 
     private void processHierarchy(Element root, Map<String, Coordinates> hierarchy) {
        if (root == null) { return; }
 
         List<Element> nodes = root.getChildren("node");
         for(Element node : nodes) {
             Coordinates parentCoordinates= idToCoordinates(node.getChildText("id"));
             Element children = node.getChild("children");
             List<Element> childrenNodes = children.getChildren("node");
             for (Element child : childrenNodes) {
                 hierarchy.put(child.getChildText("id"), parentCoordinates);
                 processHierarchy(child, hierarchy);
             }
         }
     }
 
     private Coordinates idToCoordinates(String id) {
         Coordinates coordinates = null;
 
         String[] split = id.split(":");
         if (split.length > 3) {
             coordinates = new Coordinates(split[0], split[1], split[3]);
         }
 
         return coordinates;
     }
 
     private Collection<AgentProjectInfo> extractProjects(Element projects, Map<String, Coordinates> hierarchy) {
         Collection<AgentProjectInfo> projectInfos = new ArrayList<AgentProjectInfo>();
 
         AgentRunningBuild build = runner.getBuild();
 
         List<Element> projectList = projects.getChildren("project");
         for (Element projectElement : projectList) {
             if (shouldProcess(projectElement)) {
                 AgentProjectInfo projectInfo = new AgentProjectInfo();
 
                 // project coordinates
                 String groupId = projectElement.getChildText("groupId");
                 String artifactId = projectElement.getChildText("artifactId");
                 String version = projectElement.getChildText("version");
                 projectInfo.setCoordinates(new Coordinates(groupId, artifactId, version));
                 projectInfo.setParentCoordinates(hierarchy.get(projectElement.getChildText("id")));
                 build.getBuildLogger().message("Processing " + artifactId);
 
                 // project token, if any.
                 if (projectList.size() == 1) {
                     projectInfo.setProjectToken(projectToken);
                 } else {
                     projectInfo.setProjectToken(moduleTokens.get(artifactId));
                 }
 
                 // extract dependencies information
                 extractDependencies(projectElement, projectInfo);
                 build.getBuildLogger().message("Found " + projectInfo.getDependencies().size() + " direct dependencies");
 
                 projectInfos.add(projectInfo);
             }
         }
 
         return projectInfos;
     }
 
     private boolean shouldProcess(Element project) {
         boolean process = true;
 
         String artifactId = project.getChildText("artifactId");
         String packaging = project.getChildText("packaging");
 
         if (ignorePomModules && "pom".equals(packaging)) {
             process = false;
         } else if (!excludes.isEmpty() && matchAny(artifactId, excludes)) {
             process = false;
         } else if (!includes.isEmpty() && matchAny(artifactId, includes)) {
             process = true;
         }
 
         return process;
     }
 
     private boolean matchAny(String value, List<String> patterns) {
         boolean match = false;
 
         Iterator<String> it = patterns.iterator();
         while (it.hasNext() && !match) {
             String regex = it.next().replace(".", "\\.").replace("*", ".*");
             match = value.matches(regex);
         }
 
         return match;
     }
 
     private void extractDependencies(Element projectElement, AgentProjectInfo projectInfo) {
         Element dependencyArtifacts = projectElement.getChild("dependencyArtifacts");
        if (dependencyArtifacts == null) { return; }
 
         List<Element> dependencyList = dependencyArtifacts.getChildren("artifact");
         for (Element dependencyElement : dependencyList) {
             if (isDirectDependency(dependencyElement)) {
                 DependencyInfo info = new DependencyInfo();
 
                 info.setGroupId(dependencyElement.getChildText("groupId"));
                 info.setArtifactId(dependencyElement.getChildText("artifactId"));
                 info.setVersion(dependencyElement.getChildText("version"));
                 info.setClassifier(dependencyElement.getChildText("classifier"));
                 info.setType(dependencyElement.getChildText("type"));
                 info.setScope(dependencyElement.getChildText("scope"));
 
                 String dependencyPath = dependencyElement.getChildText("path");
                 if (!StringUtil.isEmptyOrSpaces(dependencyPath)) {
                     info.setSystemPath(dependencyPath);
                     try {
                         info.setSha1(ChecksumUtils.calculateSHA1(new File(dependencyPath)));
                     } catch (IOException e) {
                         Loggers.AGENT.debug("Unable to calculate SHA-1 for " + dependencyPath);
                     }
                 }
 
                 projectInfo.getDependencies().add(info);
             }
         }
     }
 
     private boolean isDirectDependency(Element dependencyElement) {
         // check the dependency trail to see only two dependents.
         // Expecting actual module and self for direct dependencies.
         return dependencyElement.getChild("dependencyTrail").getChildren("id").size() == 2;
     }
 
     private Element extractTopProject(Element root) {
         Element hierarchy = root.getChild("hierarchy");
         Element projects = root.getChild("projects");
         if (hierarchy ==  null || projects ==  null) { return null; }
 
         // get top most project ID
         String topProjectId = null;
         List<Element> nodes = hierarchy.getChildren("node");
         if (!nodes.isEmpty()) {
             topProjectId = nodes.get(0).getChildText("id");
         }
         if (StringUtil.isEmptyOrSpaces(topProjectId)) { return null; }
 
         // get top most project element
         Element topMostProject = null;
         List<Element> projectList = projects.getChildren("project");
         Iterator<Element> iterator = projectList.iterator();
         while (iterator.hasNext() && topMostProject == null) {
             Element projectElement = iterator.next();
             if (topProjectId.equals(projectElement.getChildText("id"))) {
                 topMostProject = projectElement;
 
             }
         }
 
         return topMostProject;
     }
 
 }
