 package org.etp.portalKit.powerbuild.service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.type.TypeReference;
 import org.etp.portalKit.common.service.PropertiesManager;
 import org.etp.portalKit.common.util.JSONUtils;
 import org.etp.portalKit.common.util.MavenUtils;
 import org.etp.portalKit.common.util.ObjectUtil;
 import org.etp.portalKit.powerbuild.bean.DirTree;
 import org.etp.portalKit.setting.bean.SettingsCommand;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.stereotype.Component;
 import org.springframework.util.CollectionUtils;
 
 /**
  * The purpose of this class is to provide special directories
  * information with DirTree.
  */
 @Component(value = "customizedBuildListProvider")
 public class CustomizedBuildListProvider implements BuildListProvider {
 
     @Resource(name = "pathMatchingResourcePatternResolver")
     private PathMatchingResourcePatternResolver pathResolver;
 
     @Resource(name = "propertiesManager")
     private PropertiesManager prop;
 
     private String basePath;
     private List<DirTree> basedListTree;
     private List<DirTree> retrieveTree;
 
     private String COMMON_BUILD_LIST_BASE_JSON = "powerbuild/CustomizedBuildList.json";
 
    private String ONE_KEY_WIDGET = "portal-widget-onekey-war-3.4.2.war";
     private String EMBEDDED_TOMCAT = "AppData\\Local\\CustomizedTomcat";
 
     private String embeddedTomcatPath;
 
     @PostConstruct
     private void init() {
         basedListTree = JSONUtils.fromJSONResource(pathResolver.getResource(COMMON_BUILD_LIST_BASE_JSON),
                 new TypeReference<List<DirTree>>() {
                     //
                 });
         if (CollectionUtils.isEmpty(basedListTree)) {
             throw new RuntimeException("Load CustomizedBuildList.json error.");
         }
         prop.addObserver(this);
         String userHome = System.getProperty("user.home");
         if (StringUtils.isBlank(userHome)) {
             resetDirInfo();
             return;
         }
         File embeddedTomcat = new File(userHome, EMBEDDED_TOMCAT);
         if (!embeddedTomcat.isDirectory()) {
             resetDirInfo();
             return;
         }
         File onekey = new File(embeddedTomcat, ONE_KEY_WIDGET);
         if (!onekey.isFile()) {
             resetDirInfo();
             return;
         }
         embeddedTomcatPath = embeddedTomcat.getAbsolutePath();
         resetDirInfo();
     }
 
     /**
      * Iterate basedListTree, set the absolute path for each item
      * based on the relative path, and specified workspace which has
      * set in settings page. Note: each converted absolute path should
      * a regular maven project folder(include a pom.xml and a target
      * folder), if not, remvoe this item from the DirTree which this
      * item belongs to.
      */
     @SuppressWarnings("unchecked")
     private void resetDirInfo() {
         basePath = prop.get(SettingsCommand.PORTAL_TEAM_PATH);
         if (StringUtils.isBlank(basePath)) {
             retrieveTree = new ArrayList<DirTree>();
             return;
         }
         if (CollectionUtils.isEmpty(basedListTree)) {
             retrieveTree = new ArrayList<DirTree>();
             return;
         }
 
         retrieveTree = (List<DirTree>) ObjectUtil.clone(basedListTree);
         Iterator<DirTree> itr = retrieveTree.iterator();
         while (itr.hasNext()) {
             DirTree tree = itr.next();
             iterateTreesForConvertPath(tree, itr);
         }
         itr = retrieveTree.iterator();
         while (itr.hasNext()) {
             DirTree tree = itr.next();
             iterateTreesForRemoveNoChildItem(tree, itr);
         }
     }
 
     /**
      * @return get the converted result.
      */
     @Override
     public List<DirTree> retrieveDirTrees() {
         return retrieveTree;
     }
 
     /**
      * iterate dirTree to check whether the current DirTree contains
      * any child. if not, remove this Dir from current DirTree.
      * Otherwise, continue iterate the sub DirTree.
      * 
      * @param tree The DirTree which will be iterated for removing
      *            no-child item.
      * @param itr used to remove this dir from current DirTree
      */
     private void iterateTreesForRemoveNoChildItem(DirTree tree, Iterator<DirTree> itr) {
         if (CollectionUtils.isEmpty(tree.getSubDirs()) && StringUtils.isBlank(tree.getRelativePath())) {
             itr.remove();
         } else {
             List<DirTree> subs = tree.getSubDirs();
             Iterator<DirTree> subItr = subs.iterator();
             while (subItr.hasNext()) {
                 DirTree subTree = subItr.next();
                 iterateTreesForRemoveNoChildItem(subTree, subItr);
             }
         }
     }
 
     /**
      * iterate dirTree to check whether the relativePath is empty or
      * not, if not, combine it with base and set to absolutePath.
      * Note: if converted absolute path is not a regular maven project
      * path, remove this dir from DirTree.
      * 
      * @param tree The DirTree which will be iterated for converting
      *            absolute path.
      * @param itr used to remove this dir from current DirTree.
      */
     private void iterateTreesForConvertPath(DirTree tree, Iterator<DirTree> itr) {
         if (StringUtils.isBlank(tree.getRelativePath())) {
             List<DirTree> subs = tree.getSubDirs();
             if (!CollectionUtils.isEmpty(subs)) {
                 Iterator<DirTree> subItr = subs.iterator();
                 while (subItr.hasNext()) {
                     DirTree subTree = subItr.next();
                     iterateTreesForConvertPath(subTree, subItr);
                 }
             }
         } else {
             String relativePath = tree.getRelativePath();
             if (ONE_KEY_WIDGET.equals(relativePath)) {
                 if (!StringUtils.isBlank(embeddedTomcatPath)) {
                     tree.setAbsolutePath(embeddedTomcatPath);
                 } else {
                     tree.setRelativePath("");
                 }
                 return;
             }
             File relative = new File(basePath, relativePath);
             if (MavenUtils.isMavenProject(relative)) {
                 tree.setAbsolutePath(relative.getAbsolutePath());
             } else {
                 itr.remove();
             }
         }
     }
 
     @Override
     public void update(Observable o, Object key) {
         if (key.equals(SettingsCommand.PORTAL_TEAM_PATH)) {
             resetDirInfo();
         }
     }
 }
