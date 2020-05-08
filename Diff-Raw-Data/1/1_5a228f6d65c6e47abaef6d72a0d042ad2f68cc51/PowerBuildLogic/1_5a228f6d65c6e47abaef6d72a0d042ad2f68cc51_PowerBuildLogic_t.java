 package org.etp.portalKit.powerbuild.logic;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.type.TypeReference;
 import org.etp.portalKit.common.service.DeployService;
 import org.etp.portalKit.common.service.PropertiesManager;
 import org.etp.portalKit.common.util.CommandResult;
 import org.etp.portalKit.common.util.JSONUtils;
 import org.etp.portalKit.powerbuild.bean.BuildInformation;
 import org.etp.portalKit.powerbuild.bean.BuildResult;
 import org.etp.portalKit.powerbuild.bean.DeployInformation;
 import org.etp.portalKit.powerbuild.bean.ExecuteParam;
 import org.etp.portalKit.powerbuild.bean.WarFile;
 import org.etp.portalKit.powerbuild.service.BuildListProvider;
 import org.etp.portalKit.powerbuild.service.ExecuteType;
 import org.etp.portalKit.powerbuild.service.MavenExecuteLogManager;
 import org.etp.portalKit.powerbuild.service.MavenExecutor;
 import org.etp.portalKit.setting.bean.SettingsCommand;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.stereotype.Component;
 
 /**
  * The purpose of this class is
  */
 @Component(value = "buildLogic")
 public class PowerBuildLogic {
 
     private String ENVIRONMENT_DEPLOY_JSON = "powerbuild/DeployInformation.json";
 
     @Resource(name = "pathMatchingResourcePatternResolver")
     private PathMatchingResourcePatternResolver pathResolver;
 
     @Resource(name = "customizedBuildListProvider")
     private BuildListProvider buildListProvider;
 
     @Resource(name = "propertiesManager")
     private PropertiesManager prop;
 
     @Resource(name = "mavenExecutor")
     private MavenExecutor executor;
 
     @Resource(name = "deployService")
     private DeployService deployService;
 
     @Resource(name = "mavenExecuteLogManager")
     private MavenExecuteLogManager mavenExecuteLogManager;
 
     /**
      * initialize the basedListtree
      */
     @PostConstruct
     public void initCommbuildList() {
         //        
     }
 
     /**
      * get error message from failure message container.
      * 
      * @param messageId id of an error message.
      * @return error message
      */
     public String getErrorMsgById(String messageId) {
         return mavenExecuteLogManager.get(messageId);
     }
 
     private String getDeployPath() {
         return prop.get(SettingsCommand.TOMCAT_WEBAPPS_PATH);
     }
 
     private String getDesignPath() {
         return prop.get(SettingsCommand.PORTAL_TEAM_PATH);
     }
 
     private boolean checkCanBeDeployed(String absolutePath) {
         File file = new File(absolutePath);
         File[] files = file.listFiles(new FileFilter() {
             @Override
             public boolean accept(File arg0) {
                 return arg0.isDirectory() && "target".equals(arg0.getName());
             }
         });
         if (files.length == 0) {
             return false;
         }
         File target = new File(absolutePath, "target");
         File[] wars = target.listFiles(new FileFilter() {
             @Override
             public boolean accept(File arg0) {
                 return arg0.isFile() && arg0.getName().endsWith(".war");
             }
         });
         if (wars.length == 0) {
             return false;
         }
         return true;
     }
 
     private ExecuteType getExecuteType(ExecuteParam cmd) {
         ExecuteType type = null;
         if (cmd.isBuild() && cmd.isTest()) {
             type = ExecuteType.COMPILE_TEST;
         } else if (cmd.isBuild()) {
             type = ExecuteType.COMPILE;
         } else if (cmd.isTest()) {
             type = ExecuteType.TEST;
         }
         return type;
     }
 
     private List<String> getDependenciesRealPath(String portalRoot, String[] dependencies) {
         List<String> real = new ArrayList<String>();
         if (ArrayUtils.isEmpty(dependencies)) {
             return real;
         }
         for (String depen : dependencies) {
             File file = new File(portalRoot, depen);
             if (file.isDirectory()) {
                 real.add(file.getAbsolutePath());
             }
         }
         return real;
     }
 
     private String getWarRealPath(String portalRoot, String relativePath) {
         File file = new File(portalRoot, relativePath);
         if (file.isDirectory()) {
             return file.getAbsolutePath();
         }
         return null;
     }
 
     /**
      * Build an war file with its corresponding dependencies.
      * 
      * @param war the war file choose to be build.
      * @param param build parameters
      * @return BuildResult
      */
     public BuildResult build(WarFile war, ExecuteParam param) {
         BuildResult result = new BuildResult();
         String portalRoot = getDesignPath();
         String deployPath = getDeployPath();
         ExecuteType type = getExecuteType(param);
         List<String> buildArray = new ArrayList<String>();
         String[] dependencies = war.getDependencies();
 
         buildArray.addAll(getDependenciesRealPath(portalRoot, dependencies));
         String warPath = getWarRealPath(portalRoot, war.getRelativePath());
         if (StringUtils.isNotBlank(warPath)) {
             buildArray.add(warPath);
         }
 
         for (String path : buildArray) {
             CommandResult cr = executor.exec(path, type);
             result.setCommandResult(cr);
             if (!result.isSuccess()) {
                 return result;
             }
         }
         if (param.isDeploy()) {
             result.setSuccess(true);
            result.setDeployed(true);
             if (checkCanBeDeployed(warPath)) {
                 boolean deployed = false;
                 try {
                     deployed = deployService.deployFromMavenFolder(warPath, deployPath);
                 } catch (Exception e) {
                     deployed = deployService.deployFromFolder(warPath, deployPath);
                 }
                 result.setDeployed(deployed);
             }
         }
         return result;
 
     }
 
     private DeployInformation getDeployInformation() {
         return JSONUtils.fromJSONResource(pathResolver.getResource(ENVIRONMENT_DEPLOY_JSON),
                 new TypeReference<DeployInformation>() {
                     //            
                 });
     }
 
     /**
      * Get information which build page needed.
      * 
      * @return BuildInformation
      */
     public BuildInformation getBuildInformation() {
         return new BuildInformation(buildListProvider.retrieveDirTrees(), getDeployInformation());
     }
 }
