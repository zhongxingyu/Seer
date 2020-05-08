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
 
 import com.intellij.openapi.diagnostic.Logger;
 import jetbrains.buildServer.ExtensionHolder;
 import jetbrains.buildServer.agent.*;
 import jetbrains.buildServer.log.Loggers;
 import jetbrains.buildServer.util.EventDispatcher;
 import jetbrains.buildServer.util.StringUtil;
 import org.apache.commons.lang.StringUtils;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.util.CollectionUtils;
 import org.whitesource.agent.api.dispatch.CheckPoliciesResult;
 import org.whitesource.agent.api.dispatch.UpdateInventoryResult;
 import org.whitesource.agent.api.model.AgentProjectInfo;
 import org.whitesource.agent.api.model.DependencyInfo;
 import org.whitesource.agent.client.WhitesourceService;
 import org.whitesource.agent.client.WssServiceException;
 import org.whitesource.agent.report.PolicyCheckReport;
 import org.whitesource.teamcity.common.Constants;
 import org.whitesource.teamcity.common.WssUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Edo.Shor
  */
 public class WhitesourceLifeCycleListener extends AgentLifeCycleAdapter {
 
     /* --- Static members --- */
 
     private static final String LOG_COMPONENT = "LifeCycleListener";
 
     private ExtensionHolder extensionHolder;
 
     /* --- Constructors --- */
 
     /**
      * Constructor
      */
     public WhitesourceLifeCycleListener(@NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                                         @NotNull final ExtensionHolder extensionHolder) {
         this.extensionHolder = extensionHolder;
         eventDispatcher.addListener(this);
     }
 
     @Override
     public void agentInitialized(@NotNull BuildAgent agent) {
         super.agentInitialized(agent);
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "initialized"));
     }
 
     /* --- Interface implementation methods --- */
 
     @Override
     public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
         super.beforeRunnerStart(runner);
 
         if (shouldUpdate(runner)) {
             Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "before runner start "
                     + runner.getBuild().getProjectName() + " type " + runner.getName()));
         }
     }
 
     @Override
     public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
         super.runnerFinished(runner, status);
 
         AgentRunningBuild build = runner.getBuild();
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "runner finished " + build.getProjectName() + " type " + runner.getName()));
 
         if (!shouldUpdate(runner)) { return; } // no need to update white source...
 
         final BuildProgressLogger buildLogger = build.getBuildLogger();
         buildLogger.message("Updating White Source");
 
         // make sure we have an organization token
         Map<String, String> runnerParameters = runner.getRunnerParameters();
         String orgToken = runnerParameters.get(Constants.RUNNER_OVERRIDE_ORGANIZATION_TOKEN);
         if (StringUtil.isEmptyOrSpaces(orgToken)) {
             orgToken = runnerParameters.get(Constants.RUNNER_ORGANIZATION_TOKEN);
         }
         if (StringUtil.isEmptyOrSpaces(orgToken)) {
             stopBuildOnError((AgentRunningBuildEx) build,
                     new IllegalStateException("Empty organization token. Please make sure an organization token is defined for this runner"));
             return;
         }
 
         // should we check policies first ?
         boolean shouldCheckPolicies;
         String overrideCheckPolicies = runnerParameters.get(Constants.RUNNER_OVERRIDE_CHECK_POLICIES);
         if (StringUtils.isBlank(overrideCheckPolicies) ||
                 "global".equals(overrideCheckPolicies)) {
             shouldCheckPolicies = Boolean.parseBoolean(runnerParameters.get(Constants.RUNNER_CHECK_POLICIES));
         } else {
            shouldCheckPolicies = "enabled".equals(overrideCheckPolicies);
         }
 
         String product = runnerParameters.get(Constants.RUNNER_PRODUCT);
         String productVersion = runnerParameters.get(Constants.RUNNER_PRODUCT_VERSION);
 
         // collect OSS usage information
         buildLogger.message("Collecting OSS usage information");
         Collection<AgentProjectInfo> projectInfos;
         if (WssUtils.isMavenRunType(runner.getRunType())) {
             MavenOssInfoExtractor extractor = new MavenOssInfoExtractor(runner);
             projectInfos = extractor.extract();
             if (StringUtil.isEmptyOrSpaces(product)) {
                 product = extractor.getTopMostProjectName();
             }
         } else {
             GenericOssInfoExtractor extractor = new GenericOssInfoExtractor(runner);
             projectInfos = extractor.extract();
         }
         debugAgentProjectInfos(projectInfos);
 
         // send to white source
         if (CollectionUtils.isEmpty(projectInfos)) {
             buildLogger.message("No open source information found.");
         } else {
             WhitesourceService service = createServiceClient(runner);
             try{
                 if (shouldCheckPolicies) {
                     buildLogger.message("Checking policies");
                     CheckPoliciesResult result = service.checkPolicies(orgToken, product, productVersion, projectInfos);
                     policyCheckReport(runner, result);
                     if (result.hasRejections()) {
                         stopBuild((AgentRunningBuildEx) build, "Open source rejected by organization policies.");
                     } else {
                         buildLogger.message("All dependencies conform with open source policies.");
                         sendUpdate(orgToken, product, productVersion, projectInfos, service, buildLogger);
                     }
                 } else {
                     sendUpdate(orgToken, product, productVersion, projectInfos, service, buildLogger);
                 }
             } catch (WssServiceException e) {
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } catch (IOException e) {
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } catch (RuntimeException e) {
                 Loggers.AGENT.error(WssUtils.logMsg(LOG_COMPONENT, "Runtime Error"), e);
                 stopBuildOnError((AgentRunningBuildEx) build, e);
             } finally {
                 service.shutdown();
             }
         }
     }
 
     private void policyCheckReport(BuildRunnerContext runner, CheckPoliciesResult result) throws IOException {
         AgentRunningBuild build = runner.getBuild();
 
        PolicyCheckReport report = new PolicyCheckReport(result, build.getProjectName(), build.getBuildNumber());
        File reportArchive = report.generate(build.getBuildTempDirectory(), true);
 
        ArtifactsPublisher publisher = extensionHolder.getExtensions(ArtifactsPublisher.class).iterator().next();
        Map<File, String> artifactsToPublish = new HashMap<File, String>();
        artifactsToPublish.put(reportArchive, "");
        publisher.publishFiles(artifactsToPublish);
     }
 
     /* --- Private methods --- */
 
     private boolean shouldUpdate(BuildRunnerContext runner) {
         String shouldUpdate = runner.getRunnerParameters().get(Constants.RUNNER_DO_UPDATE);
         return !StringUtil.isEmptyOrSpaces(shouldUpdate) && Boolean.parseBoolean(shouldUpdate);
     }
 
     private WhitesourceService createServiceClient(BuildRunnerContext runner) {
         Map<String, String> runnerParameters = runner.getRunnerParameters();
         String serviceUrl = runnerParameters.get(Constants.RUNNER_SERVICE_URL);
         WhitesourceService service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, serviceUrl);
 
         String proxyHost = runnerParameters.get(Constants.RUNNER_PROXY_HOST);
         if (!StringUtil.isEmptyOrSpaces(proxyHost)) {
             int port = Integer.parseInt(runnerParameters.get(Constants.RUNNER_PROXY_PORT));
             String username = runnerParameters.get(Constants.RUNNER_PROXY_USERNAME);
             String password = runnerParameters.get(Constants.RUNNER_PROXY_PASSWORD);
             service.getClient().setProxy(proxyHost, port, username, password);
         }
 
         return service;
     }
 
     private void sendUpdate(String orgToken, String product, String productVersion, Collection<AgentProjectInfo> projectInfos,
                             WhitesourceService service, BuildProgressLogger buildLogger)
             throws WssServiceException {
 
         buildLogger.message("Sending to White Source");
         UpdateInventoryResult updateResult = service.update(orgToken, product, productVersion, projectInfos);
         logUpdateResult(updateResult, buildLogger);
     }
 
     private void logUpdateResult(UpdateInventoryResult result, BuildProgressLogger logger) {
         Loggers.AGENT.info(WssUtils.logMsg(LOG_COMPONENT, "update success"));
 
         logger.message("White Source update results: ");
         logger.message("White Source organization: " + result.getOrganization());
         logger.message(result.getCreatedProjects().size() + " Newly created projects:");
         logger.message(StringUtil.join(result.getCreatedProjects(), ","));
         logger.message(result.getUpdatedProjects().size() + " existing projects were updated:");
         logger.message(StringUtil.join(result.getUpdatedProjects(), ","));
     }
 
     private void stopBuildOnError(AgentRunningBuildEx build, Exception e) {
         Loggers.AGENT.warn(WssUtils.logMsg(LOG_COMPONENT, "Stopping build"), e);
 
         BuildProgressLogger logger = build.getBuildLogger();
         String errorMessage = e.getLocalizedMessage();
         logger.buildFailureDescription(errorMessage);
         logger.exception(e);
         logger.flush();
         build.stopBuild(errorMessage);
     }
 
     private void stopBuild(AgentRunningBuildEx build, String message) {
         Loggers.AGENT.warn(WssUtils.logMsg(LOG_COMPONENT, "Stopping build: + message"));
 
         BuildProgressLogger logger = build.getBuildLogger();
         logger.buildFailureDescription(message);
         logger.flush();
         build.stopBuild(message);
     }
 
     private void debugAgentProjectInfos(Collection<AgentProjectInfo> projectInfos) {
         final Logger log = Loggers.AGENT;
 
         log.info("----------------- dumping projectInfos -----------------");
         log.info("Total number of projects : " + projectInfos.size());
         for (AgentProjectInfo projectInfo : projectInfos) {
             log.info("Project coordiantes: " + projectInfo.getCoordinates());
             log.info("Project parent coordiantes: " + projectInfo.getParentCoordinates());
 
             Collection<DependencyInfo> dependencies = projectInfo.getDependencies();
             log.info("total # of dependencies: " + dependencies.size());
             for (DependencyInfo info : dependencies) {
                 log.info(info + " SHA-1: " + info.getSha1());
             }
         }
         log.info("----------------- dump finished -----------------");
 
     }
 }
