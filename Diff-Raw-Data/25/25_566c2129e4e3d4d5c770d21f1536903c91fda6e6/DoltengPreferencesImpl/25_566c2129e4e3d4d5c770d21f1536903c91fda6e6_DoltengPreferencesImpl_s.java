 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.preferences.impl;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 
 import jp.aonir.fuzzyxml.FuzzyXMLDocument;
 import jp.aonir.fuzzyxml.XPath;
 
 import org.apache.commons.jxpath.JXPathContext;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.preference.IPersistentPreferenceStore;
 import org.seasar.dolteng.eclipse.Constants;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
 import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
 import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;
 import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
 import org.seasar.dolteng.eclipse.util.ResourcesUtil;
 import org.seasar.dolteng.eclipse.util.S2ContainerUtil;
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.util.DocumentBuilderFactoryUtil;
 import org.seasar.framework.util.StringUtil;
 import org.w3c.dom.Document;
 
 /**
  * @author taichi
  * 
  */
 public class DoltengPreferencesImpl implements DoltengPreferences {
 
     private static final String TOMCAT_PLUGIN_PREF = ".tomcatplugin";
 
     private static final String WST_PLUGIN_PREF = ".settings/org.eclipse.wst.common.component";
 
     private static final String FLEX_BUILDER_PLUGIN_PREF = ".actionScriptProperties";
     
     private static final String DEFAULT_WEBCONENT_ROOT = "src/main/webapp";
 
     private IProject project;
 
     private HierarchicalPreferenceStore store;
 
     private Map<String, ConnectionConfig> connections = new HashMap<String, ConnectionConfig>();
 
     private NamingConvention namingConvention;
 
     public DoltengPreferencesImpl(IProject project) {
         super();
         if (project == null) {
             throw new IllegalArgumentException();
         }
         this.project = project;
 
         this.store = new HierarchicalPreferenceStore(new ProjectScope(project),
                 Constants.ID_PLUGIN);
         setUpValues();
 
         for (IPersistentPreferenceStore child : store.getChildren()) {
             addConnectionConfig(new ConnectionConfigImpl(child));
         }
     }
 
     public void setUpValues() {
         loadfromOtherPlugin();
 
         this.namingConvention = S2ContainerUtil
                 .loadNamingConvensions(this.project);
 
         String s = this.store.getString(Constants.PREF_DEFAULT_SRC_PATH);
         if (StringUtil.isEmpty(s)) {
             this.setDefaultSrcPath(project.getFullPath().append(
                     "/src/main/java").toString());
         }
         s = this.store.getString(Constants.PREF_DEFAULT_ROOT_PACKAGE);
         if (StringUtil.isEmpty(s)) {
             String[] pkgs = this.namingConvention.getRootPackageNames();
             if (pkgs != null && 0 < pkgs.length) {
                 this.setDefaultRootPackageName(pkgs[0]);
             }
         }
         s = this.store.getString(Constants.PREF_DEFAULT_RESOURCE_PATH);
         if (StringUtil.isEmpty(s)) {
             this.setDefaultResourcePath(project.getFullPath().append(
                     "/src/main/resources").toString());
         }
 
         setViewType(getViewType());
         setDaoType(getDaoType());
     }
 
     protected void loadfromOtherPlugin() {
         try {
            IFile file = ResourcesUtil.toFile(this.project
                     .findMember(TOMCAT_PLUGIN_PREF));
            if (file != null) {
                readFromTomcatPlugin(file);
             }
            file = ResourcesUtil.toFile(this.project
                     .findMember(WST_PLUGIN_PREF));
            if (file != null) {
                readFromWST(file);
            } else {
                 this.store.setValue(Constants.PREF_WEBCONTENTS_ROOT, DEFAULT_WEBCONENT_ROOT);
                 this.store.setValue(Constants.PREF_SERVLET_PATH, project.getName());
             }
            file = ResourcesUtil.toFile(this.project
                     .findMember(FLEX_BUILDER_PLUGIN_PREF));
            if (file != null) {
                readFromFlexBuilder(file);
             }
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     protected void readFromTomcatPlugin(IFile file) throws Exception {
         FuzzyXMLDocument doc = FuzzyXMLUtil.parse(file);
         String rootDir = (String) XPath.getValue(doc.getDocumentElement(),
                 "//rootDir");
         if (StringUtil.isEmpty(rootDir) == false) {
             this.store.setValue(Constants.PREF_WEBCONTENTS_ROOT, rootDir);
         }
         String path = (String) XPath.getValue(doc.getDocumentElement(),
                 "//webPath");
         if (StringUtil.isEmpty(path)) {
             this.store.setValue(Constants.PREF_SERVLET_PATH, "");
         } else {
             this.store.setValue(Constants.PREF_SERVLET_PATH, path);
         }
     }
 
     protected void readFromWST(IFile file) throws Exception {
         FuzzyXMLDocument doc = FuzzyXMLUtil.parse(file);
         String rootDir = (String) XPath.getValue(doc.getDocumentElement(),
                 "//wb-module/wb-resource[@deploy-path='/']/@source-path");
         if (StringUtil.isEmpty(rootDir) == false) {
             if (rootDir.startsWith("/")) {
                 rootDir = rootDir.substring(1);
             }
             this.store.setValue(Constants.PREF_WEBCONTENTS_ROOT, rootDir);
         }
 
         String path = (String) XPath.getValue(doc.getDocumentElement(),
                 "//wb-module/property[@name='context-root']/@value");
         if (StringUtil.isEmpty(path)) {
             this.store.setValue(Constants.PREF_SERVLET_PATH, "");
         } else {
             if (!path.startsWith("/")) {
                 path = "/" + path;
             }
             this.store.setValue(Constants.PREF_SERVLET_PATH, path);
         }
     }
 
     protected void readFromFlexBuilder(IFile file) throws Exception {
         // 何故かFuzzyXMLParserでは、flexのXMLを適切に読み出す事が出来なかった為。
         DocumentBuilder builder = DocumentBuilderFactoryUtil
                 .newDocumentBuilder();
         Document d = builder.parse(file.getContents());
         JXPathContext ctx = JXPathContext.newContext(d);
         Object o = ctx.getValue("//compiler/@sourceFolderPath");
         if (o != null) {
             IResource r = this.project.findMember(o.toString());
             if (r != null && r.exists()) {
                 setFlexSourceFolderPath(r.getFullPath().toString());
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getRawPreferences()
      */
     public IPersistentPreferenceStore getRawPreferences() {
         return this.store;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getNamingConvention()
      */
     public NamingConvention getNamingConvention() {
         return this.namingConvention;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getWebContentsRoot()
      */
     public String getWebContentsRoot() {
         return this.store.getString(Constants.PREF_WEBCONTENTS_ROOT);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getServletPath()
      */
     public String getServletPath() {
         return this.store.getString(Constants.PREF_SERVLET_PATH);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getWebServerPort()
      */
     public String getWebServer() {
         return this.store.getString(Constants.PREF_WEB_SERVER);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setWebServerPort(java.lang.String)
      */
     public void setWebServerPort(String port) {
         this.store.setValue(Constants.PREF_WEB_SERVER, port);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getNecessaryDicons()
      */
     public Set<String> getNecessaryDicons() {
         String dicons = this.store.getString(Constants.PREF_NECESSARYDICONS);
         return new HashSet<String>(Arrays.asList(dicons.split(",")));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setNecessaryDicons(java.util.Set)
      */
     public void setNecessaryDicons(Set dicons) {
         if (dicons != null && 0 < dicons.size()) {
             StringBuffer stb = new StringBuffer();
             for (Iterator i = dicons.iterator(); i.hasNext();) {
                 stb.append(i.next());
                 stb.append(',');
 
             }
             stb.setLength(stb.length() - 1);
             this.store.setValue(Constants.PREF_NECESSARYDICONS, stb.toString());
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getViewType()
      */
     public String getViewType() {
         return this.store.getString(Constants.PREF_VIEW_TYPE);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setViewType(java.lang.String)
      */
     public void setViewType(String type) {
         this.store.setValue(Constants.PREF_VIEW_TYPE, type);
         try {
             this.project.setPersistentProperty(Constants.PROP_VIEW_TYPE,
                     getViewType());
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDaoType()
      */
     public String getDaoType() {
         return this.store.getString(Constants.PREF_DAO_TYPE);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDaoType(java.lang.String)
      */
     public void setDaoType(String type) {
         this.store.setValue(Constants.PREF_DAO_TYPE, type);
         try {
             this.project.setPersistentProperty(Constants.PROP_DAO_TYPE,
                     getDaoType());
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#addConnectionConfig(org.seasar.dolteng.eclipse.preferences.ConnectionConfig)
      */
     public void addConnectionConfig(ConnectionConfig config) {
         this.connections.put(config.getName(), config);
         this.store.addChild(config.getName(), config.toPreferenceStore());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getAllOfConnectionConfig()
      */
     public ConnectionConfig[] getAllOfConnectionConfig() {
         Collection<ConnectionConfig> list = this.connections.values();
         return list.toArray(new ConnectionConfig[list.size()]);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getConnectionConfig(java.lang.String)
      */
     public ConnectionConfig getConnectionConfig(String name) {
         return this.connections.get(name);
     }
 
     public boolean isUsePageMarker() {
         return this.store.getBoolean(Constants.PREF_USE_PAGE_MARKER);
     }
 
     public void setUsePageMarker(boolean is) {
         this.store.setValue(Constants.PREF_USE_PAGE_MARKER, is);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#isUseDIMarker()
      */
     public boolean isUseDIMarker() {
         return this.store.getBoolean(Constants.PREF_USE_DI_MARKER);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setUseDIMarker(boolean)
      */
     public void setUseDIMarker(boolean is) {
         this.store.setValue(Constants.PREF_USE_DI_MARKER, is);
     }
 
     public boolean isUseSqlMarker() {
         return this.store.getBoolean(Constants.PREF_USE_SQL_MARKER);
     }
 
     public void setUseSqlMarker(boolean is) {
         this.store.setValue(Constants.PREF_USE_SQL_MARKER, is);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getOrmXmlOutputPath()
      */
     public IPath getOrmXmlOutputPath() {
         return new Path(store.getString(Constants.PREF_ORM_XML_OUTPUT_PATH));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setOrmXmlOutputPath(java.lang.String)
      */
     public void setOrmXmlOutputPath(String path) {
         if (StringUtil.isEmpty(path) == false) {
             this.store.setValue(Constants.PREF_ORM_XML_OUTPUT_PATH, path);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultResourcePath()
      */
     public IPath getDefaultResourcePath() {
         return new Path(this.store
                 .getString(Constants.PREF_DEFAULT_RESOURCE_PATH));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultSrcPath()
      */
     public IPath getDefaultSrcPath() {
         return new Path(this.store.getString(Constants.PREF_DEFAULT_SRC_PATH));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultResourcePath(java.lang.String)
      */
     public void setDefaultResourcePath(String path) {
         this.store.setValue(Constants.PREF_DEFAULT_RESOURCE_PATH, path);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultSrcPath(java.lang.String)
      */
     public void setDefaultSrcPath(String path) {
         this.store.setValue(Constants.PREF_DEFAULT_SRC_PATH, path);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getFlexSourceFolderPath()
      */
     public IPath getFlexSourceFolderPath() {
         return new Path(this.store.getString(Constants.PREF_FLEX_SRC_PATH));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setFlexSourceFolderPath(java.lang.String)
      */
     public void setFlexSourceFolderPath(String path) {
         this.store.setValue(Constants.PREF_FLEX_SRC_PATH, path);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengPreferences#getDefaultRootPackageName()
      */
     public String getDefaultRootPackageName() {
         return this.store.getString(Constants.PREF_DEFAULT_ROOT_PACKAGE);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.preferences.DoltengPreferences#setDefaultRootPackageName(java.lang.String)
      */
     public void setDefaultRootPackageName(String name) {
         this.store.setValue(Constants.PREF_DEFAULT_ROOT_PACKAGE, name);
     }
 
     public boolean isHelpRemote() {
         return this.store.getBoolean(Constants.PREF_IS_HELP_REMOTE);
     }
 
     public void setHelpRemote(boolean is) {
         this.store.setValue(Constants.PREF_IS_HELP_REMOTE, is);
     }
 
     public boolean isUsePublicField() {
         return this.store.getBoolean(Constants.PREF_IS_USE_PUBLIC_FIELD);
     }
 
     public void setUsePublicField(boolean is) {
         this.store.setValue(Constants.PREF_IS_USE_PUBLIC_FIELD, is);
     }
 }
