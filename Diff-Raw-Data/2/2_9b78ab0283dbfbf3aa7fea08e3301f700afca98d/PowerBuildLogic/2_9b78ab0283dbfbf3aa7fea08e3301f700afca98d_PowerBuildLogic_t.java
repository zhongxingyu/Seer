 package org.etp.portalKit.powerbuild.logic;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import org.apache.commons.io.Charsets;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.type.TypeReference;
 import org.etp.portalKit.common.service.DeployService;
 import org.etp.portalKit.common.service.PropertiesManager;
 import org.etp.portalKit.common.shell.CommandResult;
 import org.etp.portalKit.common.util.JSONUtils;
 import org.etp.portalKit.common.util.PropManagerUtils;
 import org.etp.portalKit.powerbuild.bean.DeployInformation;
 import org.etp.portalKit.powerbuild.bean.request.Selection;
 import org.etp.portalKit.powerbuild.bean.response.BuildResult;
 import org.etp.portalKit.powerbuild.bean.response.DirTree;
 import org.etp.portalKit.powerbuild.service.BuildExecutor;
 import org.etp.portalKit.powerbuild.service.CommonBuildListProvider;
 import org.etp.portalKit.setting.bean.Settings;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.stereotype.Component;
 
 /**
  * The purpose of this class is
  */
 @Component(value = "buildLogic")
 public class PowerBuildLogic {
 
     @Resource(name = "pathMatchingResourcePatternResolver")
     private PathMatchingResourcePatternResolver pathResolver;
 
     @Resource(name = "commonBuildListProvider")
     private CommonBuildListProvider commonBuildListProvider;
 
     @Resource(name = "propertiesManager")
     private PropertiesManager prop;
 
     @Resource(name = "buildExecutor")
     private BuildExecutor executor;
 
     @Resource(name = "deployService")
     private DeployService deployService;
 
     private String COMMON_BUILD_LIST_BASE_JSON = "powerbuild/commonBuildList.json";
     private String ENVIRONMENT_DEPLOY_JSON = "powerbuild/deployInformation.json";
 
     private DeployInformation deployInformation;
 
     /**
      * initialize the basedListtree
      */
     @PostConstruct
     public void initCommbuildList() {
         List<DirTree> list = null;
         org.springframework.core.io.Resource commonBuildListResource = pathResolver
                 .getResource(COMMON_BUILD_LIST_BASE_JSON);
         String json = null;
         try {
             json = FileUtils.readFileToString(commonBuildListResource.getFile(), Charsets.UTF_8);
             list = JSONUtils.fromJSON(json, new TypeReference<List<DirTree>>() {
                 //            
             });
         } catch (IOException e) {
             list = new ArrayList<DirTree>();
         }
 
         commonBuildListProvider.setBasedListTree(list);
 
         org.springframework.core.io.Resource envDeployInfoResource = pathResolver.getResource(ENVIRONMENT_DEPLOY_JSON);
         try {
             json = FileUtils.readFileToString(envDeployInfoResource.getFile(), Charsets.UTF_8);
             deployInformation = JSONUtils.fromJSON(json, new TypeReference<DeployInformation>() {
                 //            
             });
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Get absolute path of specified project name.
      * 
      * @param selection specified project name.
      * @return path
      */
     private String getAbsPathFromCommonBuildList(String selection) {
         List<DirTree> retrieveDirInfo = getCommonBuildListDirTrees();
         String absPath = null;
         for (DirTree dirTree : retrieveDirInfo) {
             for (DirTree subTree : dirTree.getSubDirs()) {
                 if (subTree.getName().equals(selection)) {
                     absPath = subTree.getAbsolutePath();
                     break;
                 }
             }
         }
         return absPath;
     }
 
     private String getFinalPath(String selection, String absPath) {
         String path = null;
         if (StringUtils.isBlank(absPath)) {
             if (StringUtils.isBlank(selection))
                 throw new NullPointerException("selection could not be null or empty if absPath is not defined.");
             path = getAbsPathFromCommonBuildList(selection);
         } else
             path = absPath;
         if (StringUtils.isBlank(path))
             throw new RuntimeException("Incorrect path to build.");
         return path;
     }
 
     private String checkDeployPath() {
         String deployPath = prop.get(Settings.TOMCAT_WEBAPPS_PATH);
         if (StringUtils.isBlank(deployPath))
             throw new RuntimeException("You haven't deploy path setted.");
         return deployPath;
     }
 
     private String checkDesignPath() {
         String path = prop.get(Settings.PORTAL_TEAM_PATH);
         if (StringUtils.isBlank(path))
             throw new RuntimeException("You haven't design path setted.");
         return path;
     }
 
     /**
      * Build one package with specified absPath. Note: if absPath is
      * empty or null, selection will be used to scan from common build
      * list tree for its own absolute path. Otherwise, absPath will be
      * used as a maven project folder to compile.
      * 
      * @param selection used to scan the absolute path from common
      *            build list if absPath is null or empty.
      * @param absPath used to compile a maven project, if exists.
      * @return BuildResult
      */
     public BuildResult build(String selection, String absPath) {
         String path = getFinalPath(selection, absPath);
         CommandResult compile = executor.compile(path);
         BuildResult br = new BuildResult(compile);
         return br;
     }
 
     /**
      * Build and deploy one package with specified absPath to setted
      * web container. Note: if absPath is empty or null, selection
      * will be used to scan from common build list tree for its own
      * absolute path. Otherwise, absPath will be used as a maven
      * project folder to compile.
      * 
      * @param selection used to scan the absolute path from common
      *            build list if absPath is null or empty.
      * @param absPath used to compile a maven project, if exists.
      * @return BuildResult
      */
     public BuildResult buildDeploy(String selection, String absPath) {
         String deployPath = checkDeployPath();
         String path = getFinalPath(selection, absPath);
         BuildResult result = build(null, path);
         if (!result.isSuccess())
             return result;
        boolean deployed = deployService.deployFromFolder(path, deployPath);
         result.setDeployed(deployed);
         return result;
     }
 
     /**
      * Build + deploy a set of packages.
      * 
      * @param deployType referencePortal/multiscreenPortal
      * @return BuildResult
      */
     public BuildResult buildDeploySet(String deployType) {
         String deployPath = checkDeployPath();
         String basePath = checkDesignPath();
         BuildResult result = build(null, new File(basePath, "design").getAbsolutePath());
         if (!result.isSuccess())
             return result;
         if (deployInformation == null)
             throw new RuntimeException("failed to read deployinformation.");
         List<String> deploySet = new ArrayList<String>();
         boolean deployed = true;
         if ("referencePortal".equals(deployType)) {
             for (Map<String, String> fw : deployInformation.getFramework()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             for (Map<String, String> fw : deployInformation.getReferencePortal()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
         } else if ("multiscreenPortal".equals(deployType)) {
             for (Map<String, String> fw : deployInformation.getFramework()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
             for (Map<String, String> fw : deployInformation.getMultiscreenPortal()) {
                 deploySet.add(new File(basePath, fw.get("relativePath")).getAbsolutePath());
             }
         }
         deployed = deployService.deployListFromFolder(deploySet, deployPath);
         result.setDeployed(deployed);
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private List<String> getDefaultSelectionFromProperties() {
         List<String> defaultSelection = null;
         String defs = prop.get(Selection.SPEC_DEFAULT);
         if (!StringUtils.isBlank(defs))
             defaultSelection = (List<String>) PropManagerUtils.fromString(defs);
         else
             defaultSelection = new ArrayList<String>();
         return defaultSelection;
     }
 
     /**
      * Get Specified build trees due to design path setted in settings
      * page.
      * 
      * @return List<DirTree>
      */
     public List<DirTree> getCommonBuildListDirTrees() {
         String basePath = prop.get(Settings.PORTAL_TEAM_PATH);
         if (StringUtils.isBlank(basePath))
             return new ArrayList<DirTree>();
         List<String> defaultSelection = getDefaultSelectionFromProperties();
         commonBuildListProvider.setBasePath(basePath);
         commonBuildListProvider.setDefaultSelection(defaultSelection);
         commonBuildListProvider.resetDirInfo();
         return commonBuildListProvider.retrieveDirTrees();
     }
 
     /**
      * Set default selections to settings page.
      * 
      * @param selection
      */
     public void setSelectionsToSettings(Selection selection) {
         prop.fromBean(selection);
     }
 }
