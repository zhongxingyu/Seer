 package ch.cern.dss.teamcity.agent;
 
 
 import ch.cern.dss.teamcity.common.AbiCheckerConstants;
 import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
 import jetbrains.buildServer.agent.BuildAgentConfiguration;
 import jetbrains.buildServer.agent.runner.CommandLineBuildService;
 import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
 import org.jetbrains.annotations.NotNull;
 
 public class AbiCheckerCommandLineBuildServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {
 
     @NotNull
     @Override
     public CommandLineBuildService createService() {
         return new AbiCheckerBuildService();
     }
 
     @NotNull
     @Override
     public AgentBuildRunnerInfo getBuildRunnerInfo() {
         return this;
     }
 
     @NotNull
     @Override
     public String getType() {
         return AbiCheckerConstants.TYPE;
     }
 
     @Override
     public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {

        // Check for existence of rpm2cpio, cpio, tar
         return true;
     }
 }
