 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external.modules;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.IteratorUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.io.Charsets;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.vfs2.*;
 import org.apache.commons.vfs2.provider.local.LocalFileName;
 import org.jahia.api.Constants;
 import org.jahia.data.templates.JahiaTemplatesPackage;
 import org.jahia.modules.external.ExternalData;
 import org.jahia.modules.external.ExternalDataSource;
 import org.jahia.modules.external.modules.osgi.ModulesSourceHttpServiceTracker;
 import org.jahia.modules.external.modules.osgi.ModulesSourceSpringInitializer;
 import org.jahia.modules.external.vfs.VFSDataSource;
 import org.jahia.services.SpringContextSingleton;
 import org.jahia.services.content.*;
 import org.jahia.services.content.decorator.JCRSiteNode;
 import org.jahia.services.content.nodetypes.*;
 import org.jahia.services.deamons.filewatcher.FileMonitor;
 import org.jahia.services.deamons.filewatcher.FileMonitorCallback;
 import org.jahia.services.deamons.filewatcher.FileMonitorJob;
 import org.jahia.services.deamons.filewatcher.FileMonitorResult;
 import org.jahia.services.preferences.user.UserPreferencesHelper;
 import org.jahia.services.templates.SourceControlManagement;
 import org.jahia.services.templates.SourceControlManagement.Status;
 import org.jahia.settings.SettingsBean;
 import org.jahia.utils.LanguageCodeConverters;
 import org.jahia.utils.i18n.Messages;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.UrlResource;
 
 import javax.annotation.Nullable;
 import javax.imageio.ImageIO;
 import javax.jcr.*;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.nodetype.NodeTypeIterator;
 import javax.jcr.query.InvalidQueryException;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryResult;
 import javax.jcr.query.qom.QueryObjectModelConstants;
 import javax.jcr.version.OnParentVersionAction;
 
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.*;
 
 /**
  * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
  *
  * @author david
  * @since 7.0
  */
 public class ModulesDataSource extends VFSDataSource implements ExternalDataSource.Initializable, ExternalDataSource.LazyProperty {
 
     private static final Predicate FILTER_OUT_FILES_WITH_STARTING_DOT = new Predicate() {
         @Override
         public boolean evaluate(Object object) {
             return !object.toString().startsWith(".");
         }
     };
     private static final List<String> JMIX_IMAGE_LIST = new ArrayList<String>();
     static {
         JMIX_IMAGE_LIST.add("jmix:image");
     }
     private static final Logger logger = LoggerFactory.getLogger(ModulesDataSource.class);
     private static final String PROPERTIES_EXTENSION = ".properties";
     protected static final String UNSTRUCTURED_PROPERTY = "__prop__";
     protected static final String UNSTRUCTURED_CHILD_NODE = "__node__";
     public static final String CND = ".cnd";
     public static final String CND_SLASH = ".cnd/";
     public static final String JNT_EDITABLE_FILE = "jnt:editableFile";
     public static final String J_AUTO_CREATED = "j:autoCreated";
     public static final String J_MANDATORY = "j:mandatory";
     public static final String J_ON_PARENT_VERSION = "j:onParentVersion";
     public static final String J_PROTECTED = "j:protected";
     public static final String J_SELECTOR_TYPE = "j:selectorType";
     public static final String J_SELECTOR_OPTIONS = "j:selectorOptions";
     public static final String J_REQUIRED_TYPE = "j:requiredType";
     public static final String J_VALUE_CONSTRAINTS = "j:valueConstraints";
     public static final String J_DEFAULT_VALUES = "j:defaultValues";
     public static final String J_MULTIPLE = "j:multiple";
     public static final String J_AVAILABLE_QUERY_OPERATORS = "j:availableQueryOperators";
     public static final String J_IS_FULL_TEXT_SEARCHABLE = "j:isFullTextSearchable";
     public static final String J_IS_QUERY_ORDERABLE = "j:isQueryOrderable";
     public static final String J_IS_FACETABLE = "j:isFacetable";
     public static final String J_IS_HIERARCHICAL = "j:isHierarchical";
     public static final String J_IS_INTERNATIONALIZED = "j:isInternationalized";
     public static final String J_IS_HIDDEN = "j:isHidden";
     public static final String J_INDEX = "j:index";
     public static final String J_SCOREBOOST = "j:scoreboost";
     public static final String J_ANALYZER = "j:analyzer";
     public static final String J_ON_CONFLICT_ACTION = "j:onConflictAction";
     public static final String J_ITEM_TYPE = "j:itemType";
     public static final String SOURCE_CODE = "sourceCode";
     public static final String JNT_NODE_TYPE = "jnt:nodeType";
     public static final String JNT_MIXIN_NODE_TYPE = "jnt:mixinNodeType";
     public static final String JNT_PRIMARY_NODE_TYPE = "jnt:primaryNodeType";
     public static final String JNT_DEFINITION_FILE = "jnt:definitionFile";
     public static final HashSet<String> NODETYPES_TYPES = Sets.newHashSet(JNT_NODE_TYPE, JNT_MIXIN_NODE_TYPE, JNT_PRIMARY_NODE_TYPE);
 
     private static final int ROOT_DEPTH_TOKEN = 0;
     private static final int TARGET_DEPTH_TOKEN = 1;
     private static final int SOURCES_DEPTH_TOKEN = 3;
     private static final int NODETYPE_FOLDER_DEPTH_TOKEN = 4;
     private static final int TEMPLATE_TYPE_FOLDER_DEPTH_TOKEN = 5;
     private static final int VIEWS_FOLDER_DEPTH_TOKEN = 5;
    private static final String SRC_MAIN_RESOURCES = "/src/main/resources/";
 
     private JahiaTemplatesPackage module;
 
     private Map<String, String> fileTypeMapping;
 
     private Map<String, String> folderTypeMapping;
 
     private Set<String> supportedNodeTypes;
 
     private Map<String, NodeTypeRegistry> nodeTypeRegistryMap = new HashMap<String, NodeTypeRegistry>();
 
     private String fileMonitorJobName;
 
     private File realRoot;
 
     private JCRStoreService jcrStoreService;
 
     private ModulesSourceSpringInitializer modulesSourceSpringInitializer;
 
     private ModulesImportExportHelper modulesImportExportHelper;
 
     public void start() {
         final String fullFolderPath = module.getSourcesFolder().getPath() + File.separator;
         final String importFilesRootFolder = fullFolderPath + "src" + File.separator + "main" + File.separator + "import" +
                 File.separator + "content" + File.separator + "modules" + File.separator + module.getId() + File.separator + "files" + File.separator;
         final String filesNodePath = "/modules/" + module.getIdWithVersion() + "/files";
 
         FileMonitor monitor = new FileMonitor(new FileMonitorCallback() {
             @Override
             public void process(FileMonitorResult result) {
                 logger.info("Detected changes in sources of the module in folder {}: {}", fullFolderPath, result);
                 if (logger.isDebugEnabled()) {
                     logger.debug(result.getInfo());
                 }
                 SourceControlManagement sourceControl = module.getSourceControl();
                 if (sourceControl != null) {
                     sourceControl.invalidateStatusCache();
                     logger.debug("Invalidating SCM status caches for module {}", module.getId());
                 }
                 boolean nodeTypeLabelsFlushed = false;
                 List<File> importFiles = new ArrayList<File>();
                 for (final File file : result.getAllAsList()) {
                     invalidateVfsParentCache(fullFolderPath, file);
                     if (file.getPath().startsWith(importFilesRootFolder)) {
                         importFiles.add(file);
                         continue;
                     }
 
                     String type = fileTypeMapping.get(FilenameUtils.getExtension(file.getName()));
                     if (type == null) {
                         continue;
                     }
                     if (StringUtils.equals(type,"jnt:propertiesFile")) {
                         // we've detected a properties file, check if its parent is of type jnt:resourceBundleFolder
                         // -> than this one gets the type jnt:resourceBundleFile; otherwise just jnt:file
                         File parent = file.getParentFile();
                         type = parent != null
                                 && StringUtils.equals(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER,
                                 folderTypeMapping.get(parent.getName())) ? Constants.JAHIANT_RESOURCEBUNDLE_FILE : type;
                     }
 
                     if (type.equals("jnt:resourceBundleFile") && !nodeTypeLabelsFlushed) {
                         NodeTypeRegistry.getInstance().flushLabels();
                         logger.debug("Flushing node type label caches");
                         for (NodeTypeRegistry registry : nodeTypeRegistryMap.values()) {
                             registry.flushLabels();
                         }
                         nodeTypeLabelsFlushed = true;
                         try {
                             JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                                 @Override
                                 public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                     JCRSiteNode site = (JCRSiteNode) session.getNode("/modules/"+module.getId());
                                     Set<String> langs = new HashSet<String>(site.getLanguages());
                                     boolean changed = false;
                                     if (file.getParentFile().listFiles() != null) {
                                         for (File f : file.getParentFile().listFiles()) {
                                             String s = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(f.getName(), "."), "_");
                                             if (!StringUtils.isEmpty(s) && !langs.contains(s)) {
                                                 langs.add(s);
                                                 changed = true;
                                             }
                                         }
                                         if (changed) {
                                             site.setLanguages(langs);
                                             session.save();
                                         }
                                     }
                                     return null;
                                 }
                             });
                         } catch (RepositoryException e) {
                             logger.error(e.getMessage(),e);
                         }
                     } else if (type.equals(JNT_DEFINITION_FILE)) {
                         registerCndFiles(file);
                     } else if (type.equals("jnt:viewFile")) {
                         ModulesSourceHttpServiceTracker httpServiceTracker = modulesSourceSpringInitializer.getHttpServiceTracker(module.getId());
                         if (result.getCreated().contains(file)) {
                             httpServiceTracker.registerJsp(file);
                         } else if (result.getDeleted().contains(file)) {
                             httpServiceTracker.unregisterJsp(file);
                         }
                         httpServiceTracker.flushJspCache(file);
                     }
                 }
                 if (!importFiles.isEmpty()) {
                     modulesImportExportHelper.updateImportFileNodes(importFiles, importFilesRootFolder, filesNodePath);
                 }
             }
         });
         monitor.setRecursive(true);
         monitor.setFilesToIgnore(".svn", ".git", "target", ".idea", ".settings", ".project", ".classpath");
         monitor.addFile(module.getSourcesFolder());
         fileMonitorJobName = "ModuleSourcesJob-" + module.getId();
         FileMonitorJob.schedule(fileMonitorJobName, 5000, monitor);
         for(String cndFilePath : module.getDefinitionsFiles()) {
             registerCndFiles(new File(fullFolderPath + "src" + File.separator + "main" + File.separator + "resources" + File.separator + cndFilePath));
         }
     }
 
     private void registerCndFiles(File file) {
         try {
             String cndPath = StringUtils.substringAfter(file.getPath(), SRC_MAIN_RESOURCES);
             List<String> definitionsFiles = module.getDefinitionsFiles();
             if (file.exists() && !definitionsFiles.contains(cndPath)) {
                 definitionsFiles.add(cndPath);
             } else if (!file.exists() && definitionsFiles.contains(cndPath)) {
                 definitionsFiles.remove(cndPath);
             }
             String systemId = module.getId();
             NodeTypeRegistry nodeTypeRegistry = NodeTypeRegistry.getInstance();
             nodeTypeRegistry.unregisterNodeTypes(systemId);
             for (String path : definitionsFiles) {
                 nodeTypeRegistry.addDefinitionsFile(getRealFile(SRC_MAIN_RESOURCES + path), systemId, module.getVersion());
             }
             if (SettingsBean.getInstance().isProcessingServer()) {
                 jcrStoreService.deployDefinitions(systemId);
             }
         } catch (IOException e) {
             logger.error("Error registering node type definition file " + file + " for bundle " + module.getBundle(), e);
         } catch (ParseException e) {
             logger.error("Error registering node type definition file " + file + " for bundle " + module.getBundle(), e);
         }
     }
 
     protected void invalidateVfsParentCache(String fullFolderPath, File file) {
         String relativePath = StringUtils.substringAfter(file.getPath(), fullFolderPath);
         if (StringUtils.isNotEmpty(relativePath)) {
             try {
                 getFile(relativePath).getParent().refresh();
             } catch (FileSystemException e) {
                 logger.warn("Unable to find parent for {}. Skipped invalidating VFS caches.", relativePath);
                 if (logger.isDebugEnabled()) {
                     logger.debug(e.getMessage(), e);
                 }
             }
         }
     }
 
     @Override
     public void stop() {
         if (fileMonitorJobName != null) {
             FileMonitorJob.unschedule(fileMonitorJobName);
         }
     }
 
     /**
      * Return the children of the specified path.
      *
      * @param path
      *            path of which we want to know the children
      * @return the children of the specified path
      */
     @Override
     public List<String> getChildren(String path) throws RepositoryException {
         String pathLowerCase = path.toLowerCase();
         if (pathLowerCase.endsWith(CND) || pathLowerCase.contains(CND_SLASH)) {
             return getCndChildren(path, pathLowerCase);
         } else {
             List<String> children = super.getChildren(path);
             if (children.size() > 0) {
                 CollectionUtils.filter(children, FILTER_OUT_FILES_WITH_STARTING_DOT);
             }
             return children;
         }
     }
 
     /**
      * Allows to know the nodetype associated to a filetype.
      * @param fileObject the file object that we want to know the associated nodetype
      * @return the associated nodetype
      * @throws FileSystemException
      */
     @Override
     public String getDataType(FileObject fileObject) throws FileSystemException {
         String relativeName = getFile("/").getName().getRelativeName(fileObject.getName());
         int relativeDepth = ".".equals(relativeName) ? 0 : StringUtils.split(relativeName, "/").length;
         String type = null;
         if (fileObject.getType().equals(FileType.FOLDER)) {
             if (relativeDepth == ROOT_DEPTH_TOKEN) {
                 // we are in root
                 type = Constants.JAHIANT_MODULEVERSIONFOLDER;
             } else {
                 if (relativeDepth == TARGET_DEPTH_TOKEN && StringUtils.equals("target",fileObject.getName().getBaseName())) {
                     type = "jnt:mavenTargetFolder";
                 } else if (StringUtils.equals("resources",fileObject.getName().getBaseName()) && relativeDepth == SOURCES_DEPTH_TOKEN) {
                     type = "jnt:folder";
                 } else if (relativeDepth == NODETYPE_FOLDER_DEPTH_TOKEN && isNodeType(fileObject.getName().getBaseName())) {
                     type = Constants.JAHIANT_NODETYPEFOLDER;
                 } else if (relativeDepth == TEMPLATE_TYPE_FOLDER_DEPTH_TOKEN) {
                     FileObject parent = fileObject.getParent();
                     if (parent != null && Constants.JAHIANT_NODETYPEFOLDER.equals(getDataType(parent))) {
                         type = Constants.JAHIANT_TEMPLATETYPEFOLDER;
                     }
                 } else if (StringUtils.split(relativeName,"/").length >= SOURCES_DEPTH_TOKEN && StringUtils.equals(StringUtils.split(relativeName,"/")[SOURCES_DEPTH_TOKEN-1],"java")) {
                     type = "jnt:javaPackageFolder";
                 }
             }
             if (type == null) {
                 type = folderTypeMapping.get(fileObject.getName().getBaseName());
             }
         } else {
             type = fileTypeMapping.get(fileObject.getName().getExtension());
         }
         if (type != null && StringUtils.equals(type,"jnt:propertiesFile")) {
             // we've detected a properties file, check if its parent is of type jnt:resourceBundleFolder
             // -> than this one gets the type jnt:resourceBundleFile; otherwise just jnt:file
             FileObject parent = fileObject.getParent();
             type = parent != null
                     && StringUtils.equals(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER,
                     getDataType(parent)) ? Constants.JAHIANT_RESOURCEBUNDLE_FILE : type;
         }
         boolean isFile = fileObject.getType() == FileType.FILE;
         if (isFile
                 && relativeDepth ==  VIEWS_FOLDER_DEPTH_TOKEN
                 && (fileObject.getParent() != null && StringUtils.equals(Constants.JAHIANT_TEMPLATETYPEFOLDER,
                 getDataType(fileObject.getParent())))) {
             if (StringUtils.endsWith(fileObject.getName().toString(), PROPERTIES_EXTENSION)) {
                 type = JNT_EDITABLE_FILE;
             } else {
                 type = Constants.JAHIANT_VIEWFILE;
             }
         }
 
         String contentType = getContentType(fileObject.getContent());
         if (type == null && isFile) {
             boolean isMedia = contentType != null && (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio") || contentType.contains("flash"));
             if (!isMedia) {
                 type = JNT_EDITABLE_FILE;
             }
         }
 
 
         return type != null ? type : super.getDataType(fileObject);
     }
 
     /**
      * Test if the name is a known node type in the system.
      * @param name
      * @return
      */
     public boolean isNodeType(String name) {
         name = StringUtils.replaceOnce(name, "_", ":");
         final Set<Map.Entry<String, NodeTypeRegistry>> entries = new HashSet<Map.Entry<String, NodeTypeRegistry>>(nodeTypeRegistryMap.entrySet());
         for (Map.Entry<String, NodeTypeRegistry> entry : entries) {
             try {
                 entry.getValue().getNodeType(name);
                 return true;
             } catch (NoSuchNodeTypeException e) {
                 // Continue with next registry
             }
         }
         // if not found, check in jahia registry
         try {
             NodeTypeRegistry.getInstance().getNodeType(name);
             return true;
         } catch (NoSuchNodeTypeException e) {
             return false;
         }
     }
 
     /**
      * Return item by identifier
      * @param identifier
      * @return
      * @throws ItemNotFoundException
      */
     @Override
     public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
         return super.getItemByIdentifier(identifier);
     }
 
     /**
      * Return item by path.
      * @param path
      * @return
      * @throws PathNotFoundException
      */
     @Override
     public ExternalData getItemByPath(String path) throws PathNotFoundException {
         if (path.toLowerCase().contains(CND_SLASH)) {
             try {
                 return getCndItemByPath(path);
             } catch (RepositoryException e) {
                 throw new PathNotFoundException(e);
             }
         }
         ExternalData data = super.getItemByPath(path);
         return enhanceData(path, data);
     }
 
     @Override
     public boolean itemExists(String path) {
         if (path.toLowerCase().contains(CND_SLASH)) {
             try {
                 getItemByPath(path);
                 return true;
             } catch (PathNotFoundException e) {
                 return false;
             }
         }
         return super.itemExists(path);
     }
 
 
     private ExternalData enhanceData(String path, ExternalData data) {
         try {
             ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());
             if (type.isNodeType("jnt:moduleVersionFolder")) {
                 String name = module.getName();
                 String v = module.getVersion().toString();
                 data.getProperties().put("j:title", new String[]{name + " (" + v + ")"});
             } else if (type.isNodeType(JNT_EDITABLE_FILE)) {
                 Set<String> lazyProperties = data.getLazyProperties();
                 if (lazyProperties == null) {
                     lazyProperties = new HashSet<String>();
                     data.setLazyProperties(lazyProperties);
                 }
                 String nodeTypeName= StringUtils.replace(StringUtils.substringBetween(path, SRC_MAIN_RESOURCES, "/"), "_", ":");
                 // add nodetype only if it is resolved
                 if (nodeTypeName != null) {
                     data.getProperties().put("nodeTypeName",new String[]{nodeTypeName});
                 }
                 lazyProperties.add(SOURCE_CODE);
                 // set Properties
                 if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
                     Properties properties = new SortedProperties();
                     InputStream is = null;
                     try {
                         is = getFile(StringUtils.substringBeforeLast(path,".") + PROPERTIES_EXTENSION).getContent().getInputStream();
                         properties.load(is);
                         Map<String, String[]> dataProperties = new HashMap<String, String[]>();
                         for (Map.Entry<?, ?> property : properties.entrySet()) {
                             ExtendedPropertyDefinition propertyDefinition = type.getPropertyDefinitionsAsMap().get(property.getKey());
                             String[] values;
                             if (propertyDefinition != null && propertyDefinition.isMultiple()) {
                                 values = StringUtils.split(((String) property.getValue()), ",");
                             } else {
                                 values = new String[] { (String) property.getValue() };
                             }
                             dataProperties.put((String) property.getKey(), values);
                         }
                         data.getProperties().putAll(dataProperties);
                     } catch (FileSystemException e) {
                         //no properties files, do nothing
                     } catch (IOException e) {
                         logger.error("Cannot read property file", e);
                     } finally {
                         IOUtils.closeQuietly(is);
                     }
                 }
             } else {
                 String ext = StringUtils.substringAfterLast(path, ".");
                 Map<?, ?> extensions = (Map<?, ?>) SpringContextSingleton.getBean("fileExtensionIcons");
                 if ("img".equals(extensions.get(ext))) {
                     InputStream is = null;
                     try {
                         is = getFile(data.getPath()).getContent().getInputStream();
                         BufferedImage bimg = ImageIO.read(is);
                         if(bimg != null){
                             data.setMixin(JMIX_IMAGE_LIST);
                             data.getProperties().put("j:height",new String[] {Integer.toString(bimg.getHeight())});
                             data.getProperties().put("j:width",new String[] {Integer.toString(bimg.getWidth())});
                         }
                     }
                     catch (FileSystemException e) {
                         //no properties files, do nothing
                     } catch (IOException e) {
                         logger.error("Cannot read property file", e);
                     } catch (Exception e) {
                         logger.error("unable to enhance image " + data.getPath(), e);
                     }
                     finally {
                         if (is != null) {
                             IOUtils.closeQuietly(is);
                         }
                     }
                 }
             }
         } catch (NoSuchNodeTypeException e) {
             logger.error("Unknown type", e);
         }
         SourceControlManagement sourceControl = module.getSourceControl();
         if (sourceControl != null) {
             try {
                 SourceControlManagement.Status status = getScmStatus(path);
                 if (status != SourceControlManagement.Status.UNMODIFIED) {
                     List<String> mixin = data.getMixin();
                     if (mixin == null) {
                         mixin = new ArrayList<String>();
                     }
                     if (!mixin.contains("jmix:sourceControl")) {
                         mixin.add("jmix:sourceControl");
                     }
                     data.setMixin(mixin);
                     data.getProperties().put("scmStatus", new String[] {status.name().toLowerCase()});
                 }
             } catch (IOException e) {
                 logger.error("Failed to get SCM status", e);
             }
         }
         if (data.getPath().startsWith("/src/main/import")) {
             if (data.getMixin() == null) {
                 data.setMixin(Arrays.asList("jmix:moduleImportFile"));
             } else {
                 List<String> mixins = new ArrayList<String>(data.getMixin());
                 mixins.add("jmix:moduleImportFile");
                 data.setMixin(mixins);
             }
         }
         return data;
     }
 
     private Status getScmStatus(String vfsPath) throws IOException {
         SourceControlManagement sourceControl = module.getSourceControl();
         return StringUtils.isEmpty(vfsPath) ? sourceControl.getStatus("/") : sourceControl.getStatus(vfsPath);
     }
 
     /**
      * Return list of supported node types.
      * @return
      */
     @Override
     public Set<String> getSupportedNodeTypes() {
         return supportedNodeTypes;
     }
 
     @Override
     public synchronized void removeItemByPath(String path) throws RepositoryException {
         SourceControlManagement sourceControl = module.getSourceControl();
         String pathLowerCase = path.toLowerCase();
         if (pathLowerCase.contains(CND_SLASH)) {
             removeCndItemByPath(path);
             if (sourceControl != null) {
                 sourceControl.invalidateStatusCache();
             }
         } else {
             if (pathLowerCase.endsWith(CND)) {
                 nodeTypeRegistryMap.remove(path);
             }
             if (sourceControl != null) {
                 try {
                     if (!SourceControlManagement.Status.UNTRACKED.equals(getScmStatus(path))) {
                         sourceControl.remove(getRealFile(path));
                         // really delete the file if the source control did not do it
                         if (getRealFile(path).exists()) {
                             super.removeItemByPath(path);
                         }
                         // refresh the parent file cache
                         getFile(path).getParent();
                     } else {
                         super.removeItemByPath(path);
                     }
                 } catch (IOException e) {
                     logger.error("Failed to mark file as removed in source control", e);
                     throw new RepositoryException("Failed to mark file as removed in source control", e);
                 }
                 sourceControl.invalidateStatusCache();
             } else {
                 super.removeItemByPath(path);
             }
         }
     }
 
     private void removeCndItemByPath(String path) throws RepositoryException {
         checkCndItemUsage(path,"modulesDataSource.errors.delete");
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String[] splitPath = StringUtils.split(subPath, "/");
         String nodeTypeName = splitPath[0];
         if (splitPath.length == 1) {
             NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
             nodeTypeRegistry.unregisterNodeType(nodeTypeName);
             writeDefinitionFile(nodeTypeRegistry, cndPath);
         } else {
             String itemDefinitionName = splitPath[1];
             try {
                 NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
                 ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
                 Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
                 if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                     propertyDefinitionsAsMap.get(itemDefinitionName).remove();
                 }
                 if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                     Integer type = Integer.valueOf(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                     nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type).remove();
                 }
                 Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                 if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                     childNodeDefinitionsAsMap.get(itemDefinitionName).remove();
                 }
                 if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                     String type = StringUtils.replace(itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length()).trim(),"@@",":");
                     nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type).remove();
                 }
                 writeDefinitionFile(nodeTypeRegistry, cndPath);
             } catch (NoSuchNodeTypeException e) {
                 throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
             }
         }
     }
 
     private String getCndPath(String path, String pathLowerCase) {
         return path.substring(0, pathLowerCase.indexOf(CND_SLASH) + CND.length());
     }
 
     private String getSubPath(String path, String pathLowerCase) {
         return path.substring(pathLowerCase.indexOf(CND_SLASH) + CND_SLASH.length());
     }
 
     /**
      * Move a file from one path to another one.
      * If file is of type CND the definitions will be first remove from the registry.
      * @param oldPath
      * @param newPath
      * @throws PathNotFoundException
      */
     @Override
     public synchronized void move(String oldPath, String newPath) throws RepositoryException {
         SourceControlManagement sourceControl = module.getSourceControl();
         final String lowerCaseOldPath = oldPath.toLowerCase();
         if (lowerCaseOldPath.contains(CND_SLASH) && newPath.toLowerCase().contains(CND_SLASH)) {
             moveCndItems(oldPath, newPath);
             if (sourceControl != null) {
                 sourceControl.invalidateStatusCache();
             }
         } else {
             if (lowerCaseOldPath.endsWith(CND)) {
                 nodeTypeRegistryMap.remove(oldPath);
             }
             if (sourceControl != null) {
                 try {
                     if (!SourceControlManagement.Status.UNTRACKED.equals(getScmStatus(oldPath))) {
                         File src = getRealFile(oldPath);
                         File dst = getRealFile(newPath);
                         sourceControl.move(src, dst);
                     }
                     // move the file if the source control do not do it (like directories for GIT)
                     if (!getFile(newPath).exists()) {
                         super.move(oldPath, newPath);
                     }
                 } catch (IOException e) {
                     logger.error("Failed to mark file as removed in source control", e);
                     throw new RepositoryException("Failed to mark file as removed in source control", e);
                 }
                 sourceControl.invalidateStatusCache();
             } else {
                 super.move(oldPath, newPath);
             }
         }
     }
 
     private void checkCndItemUsage(String path, String message) throws RepositoryException {
         try {
             ExternalData item = getCndItemByPath(path);
             if (!NODETYPES_TYPES.contains(item.getType())) {
                 item = getCndItemByPath(StringUtils.substringBeforeLast(path, "/"));
             }
             final String type = StringUtils.substringAfterLast(item.getPath(),"/");
             // Check for usage of the nodetype before moving it
             checkCndItemUsageByWorkspace(type, "default", message);
             checkCndItemUsageByWorkspace(type, "live", message);
         } catch (NoSuchNodeTypeException e) {
             // do nothing
         }
     }
 
     private void checkCndItemUsageByWorkspace(final String type, final String workspace,final String message) throws RepositoryException {
         JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
             public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                 try {
                     QueryResult result = session.getWorkspace().getQueryManager()
                             .createQuery("Select * from [" + type + "]", Query.JCR_SQL2).execute();
                     if (result.getRows().hasNext()) {
                         Locale locale = UserPreferencesHelper.getPreferredLocale(JCRSessionFactory.getInstance().getCurrentUser());
                         if (locale == null) {
                             locale = SettingsBean.getInstance().getDefaultLocale();
                         }
                         throw new ItemExistsException(Messages.get("resources.JahiaExternalProviderModules", message, locale));
                     }
                 } catch (InvalidQueryException e) {
                     // this can happen if the type just have been created and not used at all.
                 } catch (NamespaceException e) {
                     // this can happen if the namespace just have been created and the module not deployed.
                 }
                 return null;
             }
         });
     }
 
     private synchronized void moveCndItems(String oldPath, String newPath) throws RepositoryException {
         checkCndItemUsage(oldPath,"modulesDataSource.errors.move");
         if (itemExists(newPath)) {
             throw new ItemExistsException("Item " + newPath + " already exists");
         }
         String oldPathlowerCase = oldPath.toLowerCase();
         String oldCndPath = getCndPath(oldPath, oldPathlowerCase);
         String oldSubPath = getSubPath(oldPath, oldPathlowerCase);
         String[] splitOldPath = StringUtils.split(oldSubPath, "/");
 
         String newPathlowerCase = newPath.toLowerCase();
         String newCndPath = getCndPath(newPath, newPathlowerCase);
         String newSubPath = getSubPath(newPath, newPathlowerCase);
         String[] splitNewPath = StringUtils.split(newSubPath, "/");
 
         if ((splitOldPath.length == 1) && (splitNewPath.length == 1)) {
             String oldNodeTypeName = splitOldPath[0];
             String newNodeTypeName = splitNewPath[0];
 
             try {
                 NodeTypeRegistry oldNodeTypeRegistry = loadRegistry(oldCndPath);
                 ExtendedNodeType nodeType = oldNodeTypeRegistry.getNodeType(oldNodeTypeName);
 
                 NodeTypeIterator declaredSubtypes = nodeType.getDeclaredSubtypes();
                 List<ExtendedNodeType> n = new ArrayList<ExtendedNodeType>();
 
                 while (declaredSubtypes.hasNext()) {
                     n.add((ExtendedNodeType) declaredSubtypes.nextNodeType());
                 }
 
                 for (ExtendedNodeType sub : n) {
                     List<String> s = Lists.newArrayList(sub.getDeclaredSupertypeNames());
                     s.remove(oldNodeTypeName);
                     sub.setDeclaredSupertypes(s.toArray(new String[s.size()]));
                     sub.validate();
                 }
 
                 oldNodeTypeRegistry.unregisterNodeType(oldNodeTypeName);
 
                 Name name = new Name(newNodeTypeName, oldNodeTypeRegistry.getNamespaces());
                 nodeType.setName(name);
                 NodeTypeRegistry newNodeTypeRegistry = loadRegistry(newCndPath);
                 newNodeTypeRegistry.addNodeType(name, nodeType);
                 nodeType.validate();
 
                 for (ExtendedNodeType sub : n) {
                     List<String> s = Lists.newArrayList(sub.getDeclaredSupertypeNames());
                     s.add(newNodeTypeName);
                     sub.setDeclaredSupertypes(s.toArray(new String[s.size()]));
                     sub.validate();
                 }
 
                 writeDefinitionFile(oldNodeTypeRegistry, newCndPath);
                 if (!oldCndPath.equals(newCndPath)) {
                     writeDefinitionFile(newNodeTypeRegistry, oldCndPath);
                 }
             } catch (RepositoryException e) {
                 nodeTypeRegistryMap.remove(newCndPath);
                 nodeTypeRegistryMap.remove(oldCndPath);
                 throw e;
             }
         } else {
             throw new RepositoryException("Cannot move cnd items");
         }
     }
 
     /**
      * reorder children nodes according to the list passed as parameter
      * @param path
      * @param children
      * @throws PathNotFoundException
      */
     @Override
     public void order(String path, final List<String> children) throws RepositoryException {
         // Order only for nodeType
         ExternalData data = getItemByPath(path);
         if (!data.getType().equals(JNT_PRIMARY_NODE_TYPE) && !data.getType().equals(JNT_MIXIN_NODE_TYPE)) {
             throw new ConstraintViolationException("Order support only node type");
         }
 
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String splitPath = StringUtils.substringBefore(subPath, "/");
 
         NodeTypeRegistry ntr = loadRegistry(cndPath);
         ExtendedNodeType type = ntr.getNodeType(splitPath);
         Comparator<ExtendedItemDefinition> c = new Comparator<ExtendedItemDefinition>() {
             @Override
             public int compare(ExtendedItemDefinition o1, ExtendedItemDefinition o2) {
                 String s1 = o1.isUnstructured() ? computeUnstructuredItemName(o1) : o1.getName();
                 String s2 = o2.isUnstructured() ? computeUnstructuredItemName(o2) : o2.getName();
                 int i1 = children.indexOf(s1);
                 int i2 = children.indexOf(s2);
                 if (i1 == i2) {
                     return 0;
                 } else if (i1 > i2) {
                     return 1;
                 } else {
                     return -1;
                 }
             }
         };
         type.sortItems(c);
         writeDefinitionFile(ntr, cndPath);
     }
 
     private String computeUnstructuredItemName(ExtendedItemDefinition o1) {
         StringBuilder s1 = new StringBuilder();
         if (o1.isNode()) {
             s1.append(UNSTRUCTURED_CHILD_NODE);
             for (ExtendedNodeType e : ((ExtendedNodeDefinition) o1).getRequiredPrimaryTypes()) {
                 String validName = StringUtils.replace(e.getName(),":","@@");
                 s1.append(validName).append(" ");
             }
         } else {
             if (((ExtendedPropertyDefinition) o1).isMultiple()) {
                 int i = ExtendedPropertyType.MULTIPLE_OFFSET + ((ExtendedPropertyDefinition) o1).getRequiredType();
                 s1.append(UNSTRUCTURED_PROPERTY).append(i);
             } else {
                 s1.append(UNSTRUCTURED_PROPERTY).append(((ExtendedPropertyDefinition) o1).getRequiredType());
             }
         }
         return s1.toString().trim();
     }
 
     @Override
     public void saveItem(ExternalData data) throws RepositoryException {
         super.saveItem(data);
         boolean hasProperties = false;
         try {
             ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());
 
             if (type.isNodeType(JNT_DEFINITION_FILE)) {
                 checkCndFormat(data);
                 hasProperties = saveEditableFile(data, type);
             } else if (type.isNodeType(JNT_EDITABLE_FILE)) {
                 hasProperties = saveEditableFile(data, type);
             } else if (type.isNodeType(JNT_NODE_TYPE)) {
                 saveNodeType(data);
             } else if (type.isNodeType("jnt:propertyDefinition")) {
                 savePropertyDefinition(data);
             } else if (type.isNodeType("jnt:childNodeDefinition")) {
                 saveChildNodeDefinition(data);
             } else if (type.isNodeType("jnt:namespaceDefinition")) {
                 registerNamespace(data);
             }
         } catch (NoSuchNodeTypeException e) {
             logger.error("Unknown type", e);
             throw e;
         }
         SourceControlManagement sourceControl = module.getSourceControl();
         if (sourceControl != null) {
             String path = data.getPath();
             String pathLoweCase = path.toLowerCase();
             if (!pathLoweCase.contains(CND_SLASH)) {
                 if (pathLoweCase.endsWith("/" + Constants.JCR_CONTENT)) {
                     path = path.substring(0, path.indexOf("/" + Constants.JCR_CONTENT));
                 }
                 try {
                     sourceControl.add(getRealFile(path));
                     if (hasProperties) {
                         sourceControl.add(getRealFile(StringUtils.substringBeforeLast(data.getPath(),".") + PROPERTIES_EXTENSION));
                     }
                 } catch (IOException e) {
                     logger.error("Failed to add file " + path + " to source control", e);
                     throw new RepositoryException("Failed to add file " + path + " to source control", e);
                 }
             } else {
                 sourceControl.invalidateStatusCache();
             }
         }
     }
 
     private void checkCndFormat(ExternalData data) throws RepositoryException {
         if (data.getProperties().get(SOURCE_CODE) != null) {
             byte[] sourceCode = data.getProperties().get(SOURCE_CODE)[0].getBytes();
             try {
                 createRegistry().validateDefinitionsFile(new ByteArrayInputStream(sourceCode), data.getPath(), module.getId());
             } catch (ParseException e) {
                 logger.error("Error while parsing definitions file", e);
             } catch (IOException e) {
                 logger.error("Error while parsing definitions file", e);
             }
         }
     }
 
     private synchronized boolean saveEditableFile(ExternalData data, ExtendedNodeType type) throws RepositoryException {
         boolean hasProperties = false;
         // Handle source code
         OutputStream outputStream = null;
         try {
             //don't write code if file is empty
             if (data.getProperties().get(SOURCE_CODE) != null) {
                 outputStream = getFile(data.getPath()).getContent().getOutputStream();
                 byte[] sourceCode = data.getProperties().get(SOURCE_CODE)[0].getBytes(Charsets.UTF_8);
                 outputStream.write(sourceCode);
             }
         } catch (Exception e) {
             logger.error("Failed to write source code", e);
             throw new RepositoryException("Failed to write source code", e);
         } finally {
             IOUtils.closeQuietly(outputStream);
         }
 
         // Handle properties
         if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
             hasProperties = true;
             saveProperties(data);
         }
 
         if (type.isNodeType(JNT_DEFINITION_FILE)) {
             nodeTypeRegistryMap.remove(data.getPath());
         }
         return hasProperties;
     }
 
     private void saveProperties(ExternalData data) throws RepositoryException {
         OutputStream outputStream = null;
         try {
             ExtendedNodeType propertiesType = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIAMIX_VIEWPROPERTIES);
             Properties properties = new SortedProperties();
             for (Map.Entry<String, String[]> property : data.getProperties().entrySet()) {
                 Map<String, ExtendedPropertyDefinition> propertyDefinitionMap = propertiesType.getDeclaredPropertyDefinitionsAsMap();
                 if (propertyDefinitionMap.containsKey(property.getKey())) {
                     String[] v = property.getValue();
                     if (v != null) {
                         String propertyValue = StringUtils.join(v,",");
                         if (propertyDefinitionMap.get(property.getKey()).getRequiredType() != PropertyType.BOOLEAN ||
                                 !propertyValue.equals("false")) {
                             properties.put(property.getKey(), propertyValue);
                         }
                     }
                 }
             }
             FileObject file = getFile(StringUtils.substringBeforeLast(data.getPath(),".") + PROPERTIES_EXTENSION);
             if (!properties.isEmpty()) {
                 outputStream = file.getContent().getOutputStream();
                 properties.store(outputStream, data.getPath());
             } else {
                 if (file.exists()) {
                     file.delete();
                 }
             }
             ResourceBundle.clearCache();
         } catch (FileSystemException e) {
             logger.error(e.getMessage(), e);
             throw new RepositoryException("Failed to write source code", e);
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new RepositoryException("Failed to write source code", e);
         } catch (NoSuchNodeTypeException e) {
             logger.error("Unable to find type : " + data.getType() + " for node " + data.getPath(), e);
             throw e;
         } finally {
             IOUtils.closeQuietly(outputStream);
         }
     }
 
     private synchronized void saveNodeType(ExternalData data) throws RepositoryException {
         String path = data.getPath();
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String nodeTypeName = StringUtils.substringBefore(subPath, "/");
         NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
         ExtendedNodeType nodeType = null;
         try {
             nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
         } catch (NoSuchNodeTypeException e) {
             nodeType = new ExtendedNodeType(nodeTypeRegistry, module.getId());
             nodeType.setName(new Name(nodeTypeName, nodeTypeRegistry.getNamespaces()));
         }
         Map<String, String[]> properties = data.getProperties();
         List<String> declaredSupertypes = new ArrayList<String>();
         String[] values = properties.get("j:supertype");
         final HashSet<String> supertypes = Sets.newHashSet(nodeType.getDeclaredSupertypeNames());
         if (values != null && values.length > 0) {
             if (!supertypes.contains(values[0])) {
                 checkCndItemUsage(path,"modulesDataSource.errors.changeSuperType");
             }
             declaredSupertypes.add(values[0]);
         }
         values = properties.get("j:mixins");
         if (values != null) {
             for (String mixin : values) {
                 if (!supertypes.contains(mixin)) {
                     checkCndItemUsage(path,"modulesDataSource.errors.changeMixins");
                 }
                 declaredSupertypes.add(mixin);
             }
         }
         nodeType.setDeclaredSupertypes(declaredSupertypes.toArray(new String[declaredSupertypes.size()]));
         values = properties.get("j:isAbstract");
         if (values != null && values.length > 0) {
             nodeType.setAbstract(Boolean.parseBoolean(values[0]));
         } else {
             nodeType.setAbstract(false);
         }
         values = properties.get("j:isQueryable");
         if (values != null && values.length > 0) {
             nodeType.setQueryable(Boolean.parseBoolean(values[0]));
         } else {
             nodeType.setQueryable(true);
         }
         values = properties.get("j:hasOrderableChildNodes");
         if (values != null && values.length > 0) {
             nodeType.setHasOrderableChildNodes(Boolean.parseBoolean(values[0]));
         } else {
             nodeType.setHasOrderableChildNodes(false);
         }
         values = properties.get("j:itemsType");
         if (values != null && values.length > 0) {
             nodeType.setItemsType(values[0]);
         } else {
             nodeType.setItemsType(null);
         }
         values = properties.get("j:mixinExtends");
         if (values != null) {
             nodeType.setMixinExtendNames(Lists.newArrayList(values));
         } else {
             nodeType.setMixinExtendNames(new ArrayList<String>());
         }
         values = properties.get("j:primaryItemName");
         if (values != null && values.length > 0) {
             nodeType.setPrimaryItemName(values[0]);
         } else {
             nodeType.setPrimaryItemName(null);
         }
         nodeType.setMixin(JNT_MIXIN_NODE_TYPE.equals(data.getType()));
         nodeTypeRegistry.addNodeType(nodeType.getNameObject(), nodeType);
         try {
             nodeType.validate();
         } catch (NoSuchNodeTypeException e) {
             logger.error("Failed to save child node definition", e);
             nodeTypeRegistryMap.remove(cndPath);
             throw e;
         }
         writeDefinitionFile(nodeTypeRegistry, cndPath);
 
         saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName));
     }
 
     private void saveCndResourceBundle(ExternalData data, String key) throws RepositoryException {
         String resourceBundleName = module.getResourceBundleName();
         if (resourceBundleName == null) {
             resourceBundleName = "resources." + module.getId();
         }
         String rbBasePath = "/src/main/resources/resources/" + StringUtils.substringAfterLast(resourceBundleName, ".");
         Map<String, Map<String, String[]>> i18nProperties = data.getI18nProperties();
         if (i18nProperties != null) {
             List<File> newFiles = new ArrayList<File>();
             for (Map.Entry<String, Map<String, String[]>> entry : i18nProperties.entrySet()) {
                 String lang = entry.getKey();
                 Map<String, String[]> properties = entry.getValue();
 
                 String[] values = properties.get(Constants.JCR_TITLE);
                 String title = ArrayUtils.isEmpty(values) ? null : values[0];
 
                 values = properties.get(Constants.JCR_DESCRIPTION);
                 String description = ArrayUtils.isEmpty(values) ? null : values[0];
 
                 String rbPath = rbBasePath + "_" + lang + PROPERTIES_EXTENSION;
                 InputStream is = null;
                 InputStreamReader isr = null;
                 OutputStream os = null;
                 OutputStreamWriter osw = null;
                 try {
                     FileObject file = getFile(rbPath);
                     FileContent content = file.getContent();
                     Properties p = new SortedProperties();
                     if (file.exists()) {
                         is = content.getInputStream();
                         isr = new InputStreamReader(is, Charsets.ISO_8859_1);
                         p.load(isr);
                         isr.close();
                         is.close();
                     } else if (StringUtils.isBlank(title) && StringUtils.isBlank(description)) {
                         continue;
                     } else {
                         newFiles.add(new File(file.getName().getPath()));
                     }
                     if (!StringUtils.isEmpty(title)) {
                         p.setProperty(key, title);
                     }
                     if (!StringUtils.isEmpty(description)) {
                         p.setProperty(key+"_description", description);
                     }
                     os = content.getOutputStream();
                     osw = new OutputStreamWriter(os, Charsets.ISO_8859_1);
                     p.store(osw, rbPath);
                     ResourceBundle.clearCache();
                 } catch (FileSystemException e) {
                     logger.error("Failed to save resourceBundle", e);
                     throw new RepositoryException("Failed to save resourceBundle",e);
                 } catch (IOException e) {
                     logger.error("Failed to save resourceBundle", e);
                     throw new RepositoryException("Failed to save resourceBundle",e);
                 } finally {
                     IOUtils.closeQuietly(is);
                     IOUtils.closeQuietly(isr);
                     IOUtils.closeQuietly(os);
                     IOUtils.closeQuietly(osw);
                 }
             }
             SourceControlManagement sourceControl = module.getSourceControl();
             if (sourceControl != null) {
                 try {
                     sourceControl.add(newFiles);
                 } catch (IOException e) {
                     logger.error("Failed to add files to source control", e);
                     throw new RepositoryException("Failed to add new files to source control: " + newFiles, e);
                 }
             }
         }
     }
 
     private synchronized void savePropertyDefinition(ExternalData data) throws RepositoryException {
         String path = data.getPath();
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String[] splitPath = StringUtils.split(subPath, "/");
 
         NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
         String nodeTypeName = splitPath[0];
         String lastPathSegment = splitPath[1];
         try {
             ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
             boolean unstructured = "jnt:unstructuredPropertyDefinition".equals(data.getType());
             ExtendedPropertyDefinition propertyDefinition;
             if (unstructured) {
                 Integer key = Integer.valueOf(StringUtils.substringAfter(lastPathSegment, UNSTRUCTURED_PROPERTY));
                 propertyDefinition = nodeType.getDeclaredUnstructuredPropertyDefinitions().get(key);
             } else {
                 propertyDefinition = nodeType.getDeclaredPropertyDefinitionsAsMap().get(lastPathSegment);
             }
             if (propertyDefinition == null) {
                 propertyDefinition = new ExtendedPropertyDefinition(nodeTypeRegistry);
                 String qualifiedName = unstructured ? "*" : lastPathSegment;
                 Name name = new Name(qualifiedName, nodeTypeRegistry.getNamespaces());
                 propertyDefinition.setName(name);
                 propertyDefinition.setRequiredType(PropertyType.valueFromName(data.getProperties().get(J_REQUIRED_TYPE)[0]));
                 String[] isMultiple = data.getProperties().get(J_MULTIPLE);
                 if (isMultiple != null && isMultiple.length > 0) {
                     propertyDefinition.setMultiple(Boolean.parseBoolean(isMultiple[0]));
                 }
                 propertyDefinition.setDeclaringNodeType(nodeType);
             }
             Map<String, String[]> properties = data.getProperties();
             String[] values = properties.get(J_AUTO_CREATED);
             if (values != null && values.length > 0) {
                 propertyDefinition.setAutoCreated(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setAutoCreated(false);
             }
             values = properties.get(J_MANDATORY);
             if (values != null && values.length > 0) {
                 propertyDefinition.setMandatory(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setMandatory(false);
             }
             values = properties.get(J_ON_PARENT_VERSION);
             if (values != null && values.length > 0) {
                 propertyDefinition.setOnParentVersion(OnParentVersionAction.valueFromName(values[0]));
             } else {
                 propertyDefinition.setOnParentVersion(OnParentVersionAction.VERSION);
             }
             values = properties.get(J_PROTECTED);
             if (values != null && values.length > 0) {
                 propertyDefinition.setProtected(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setProtected(false);
             }
             values = properties.get(J_SELECTOR_TYPE);
             int selectorType = SelectorType.SMALLTEXT;  // Default selector type is smallText
             if (values != null && values.length > 0) {
                 selectorType = SelectorType.valueFromName(values[0]);
             } else if (propertyDefinition.getRequiredType() == PropertyType.WEAKREFERENCE) {
                 selectorType = SelectorType.CONTENTPICKER;
             }
             propertyDefinition.setSelector(selectorType);
             values = properties.get(J_SELECTOR_OPTIONS);
             Map<String, String> selectorOptions = new HashMap<String, String>();
             if (values != null) {
                 for (String option : values) {
                     String[] keyValue = StringUtils.split(option, "=", 3);
                     if (keyValue.length > 1) {
                         selectorOptions.put(keyValue[0].trim(), StringUtils.strip(keyValue[1].trim(), "'"));
                     } else {
                         selectorOptions.put(keyValue[0].trim(), "");
                     }
                 }
             }
             propertyDefinition.setSelectorOptions(selectorOptions);
             values = properties.get(J_REQUIRED_TYPE);
             int requiredType = 0;
             if (values != null && values.length > 0) {
                 requiredType = PropertyType.valueFromName(values[0]);
             }
             propertyDefinition.setRequiredType(requiredType);
             values = properties.get(J_VALUE_CONSTRAINTS);
             List<Value> valueConstraints = new ArrayList<Value>();
             if (values != null) {
                 for (String valueConstraint : values) {
                     valueConstraints.add(getValueFromString(valueConstraint, requiredType, propertyDefinition));
                 }
             }
             propertyDefinition.setValueConstraints(valueConstraints.toArray(new Value[valueConstraints.size()]));
             values = properties.get(J_DEFAULT_VALUES);
             List<Value> defaultValues = new ArrayList<Value>();
             if (values != null) {
                 for (String defaultValue : values) {
                     defaultValues.add(getValueFromString(defaultValue, requiredType, propertyDefinition));
                 }
             }
             propertyDefinition.setDefaultValues(defaultValues.toArray(new Value[defaultValues.size()]));
             values = properties.get(J_MULTIPLE);
             if (values != null && values.length > 0) {
                 propertyDefinition.setMultiple(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setMultiple(false);
             }
             values = properties.get(J_AVAILABLE_QUERY_OPERATORS);
             List<String> ops = new ArrayList<String>();
             if (values != null) {
                 for (String op : values) {
                     if (op.equals(Lexer.QUEROPS_EQUAL)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO);
                     } else if (op.equals(Lexer.QUEROPS_NOTEQUAL)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO);
                     } else if (op.equals(Lexer.QUEROPS_LESSTHAN)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN);
                     } else if (op.equals(Lexer.QUEROPS_LESSTHANOREQUAL)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
                     } else if (op.equals(Lexer.QUEROPS_GREATERTHAN)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN);
                     } else if (op.equals(Lexer.QUEROPS_GREATERTHANOREQUAL)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
                     } else if (op.equals(Lexer.QUEROPS_LIKE)) {
                         ops.add(QueryObjectModelConstants.JCR_OPERATOR_LIKE);
                     }
                 }
             }
             if (ops.isEmpty()) {
                 propertyDefinition.setAvailableQueryOperators(Lexer.ALL_OPERATORS);
             } else {
                 propertyDefinition.setAvailableQueryOperators(ops.toArray(new String[ops.size()]));
             }
             values = properties.get(J_IS_FULL_TEXT_SEARCHABLE);
             if (values != null && values.length > 0) {
                 propertyDefinition.setFullTextSearchable(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setFullTextSearchable(true);
             }
             values = properties.get(J_IS_QUERY_ORDERABLE);
             if (values != null && values.length > 0) {
                 propertyDefinition.setQueryOrderable(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setQueryOrderable(true);
             }
             values = properties.get(J_IS_FACETABLE);
             if (values != null && values.length > 0) {
                 propertyDefinition.setFacetable(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setFacetable(false);
             }
             values = properties.get(J_IS_HIERARCHICAL);
             if (values != null && values.length > 0) {
                 propertyDefinition.setHierarchical(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setHierarchical(false);
             }
             values = properties.get(J_IS_INTERNATIONALIZED);
             if (values != null && values.length > 0) {
                 propertyDefinition.setInternationalized(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setInternationalized(false);
             }
             values = properties.get(J_IS_HIDDEN);
             if (values != null && values.length > 0) {
                 propertyDefinition.setHidden(Boolean.parseBoolean(values[0]));
             } else {
                 propertyDefinition.setHidden(false);
             }
             values = properties.get(J_INDEX);
             if (values != null && values.length > 0) {
                 propertyDefinition.setIndex(IndexType.valueFromName(values[0].toLowerCase()));
             } else {
                 propertyDefinition.setIndex(IndexType.TOKENIZED);
             }
             values = properties.get(J_SCOREBOOST);
             if (values != null && values.length > 0) {
                 propertyDefinition.setScoreboost(Double.parseDouble(values[0]));
             } else {
                 propertyDefinition.setScoreboost(1.);
             }
             values = properties.get(J_ANALYZER);
             if (values != null && values.length > 0) {
                 propertyDefinition.setAnalyzer(values[0]);
             } else {
                 propertyDefinition.setAnalyzer(null);
             }
             values = properties.get(J_ON_CONFLICT_ACTION);
             if (values != null && values.length > 0) {
                 propertyDefinition.setOnConflict(OnConflictAction.valueFromName(values[0]));
             } else {
                 propertyDefinition.setOnConflict(OnConflictAction.USE_LATEST);
             }
             values = properties.get(J_ITEM_TYPE);
             if (values != null && values.length > 0) {
                 propertyDefinition.setItemType(values[0]);
             } else {
                 propertyDefinition.setItemType(null);
             }
             nodeType.validate();
             writeDefinitionFile(nodeTypeRegistry, cndPath);
 
             saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName) + "." + JCRContentUtils.replaceColon(lastPathSegment));
         } catch (NoSuchNodeTypeException e) {
             nodeTypeRegistryMap.remove(cndPath);
             throw e;
         }
     }
 
     private Value getValueFromString(String value, int requiredType, ExtendedPropertyDefinition propertyDefinition) {
         if (value.contains("(")) {
             String fn = StringUtils.substringBefore(value, "(");
             fn = fn.replace("\\\\", "\\");
             fn = fn.replace("\\'", "'");
             String[] params = StringUtils.split(StringUtils.substringBetween(value, "(", ")"), " ");
             List<String> paramList = new ArrayList<String>();
             for (String param : params) {
                 param = param.trim();
                 param = StringUtils.removeEnd(StringUtils.removeStart(param, "'"), "'");
                 if (!"".equals(param)) {
                     param = param.replace("\\\\", "\\");
                     param = param.replace("\\'", "'");
                     paramList.add(param);
                 }
             }
             return new DynamicValueImpl(fn, paramList, requiredType, false, propertyDefinition);
         } else {
             String v = StringUtils.removeEnd(StringUtils.removeStart(value, "'"), "'");
             v = v.replace("\\\\", "\\");
             v = v.replace("\\'", "'");
             return new ValueImpl(v, requiredType, false);
         }
     }
 
     private synchronized void saveChildNodeDefinition(ExternalData data) throws RepositoryException {
         String path = data.getPath();
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String[] splitPath = StringUtils.split(subPath, "/");
 
         NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
 
         String nodeTypeName = splitPath[0];
         String lastPathSegment = splitPath[1];
         try {
             ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
             boolean unstructured = "jnt:unstructuredChildNodeDefinition".equals(data.getType());
             ExtendedNodeDefinition childNodeDefinition = null;
             if (unstructured) {
                 String type = StringUtils.replace(lastPathSegment.substring(UNSTRUCTURED_CHILD_NODE.length()).trim(),"@@",":");
                 childNodeDefinition = nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type);
             } else {
                 childNodeDefinition = nodeType.getDeclaredChildNodeDefinitionsAsMap().get(lastPathSegment);
             }
             if (childNodeDefinition == null) {
                 childNodeDefinition = new ExtendedNodeDefinition(nodeTypeRegistry);
                 String qualifiedName = unstructured ? "*" : lastPathSegment;
                 Name name = new Name(qualifiedName, nodeTypeRegistry.getNamespaces());
                 childNodeDefinition.setName(name);
                 childNodeDefinition.setRequiredPrimaryTypes(data.getProperties().get("j:requiredPrimaryTypes"));
                 childNodeDefinition.setDeclaringNodeType(nodeType);
             }
             Map<String, String[]> properties = data.getProperties();
             String[] values = properties.get(J_AUTO_CREATED);
             if (values != null && values.length > 0) {
                 childNodeDefinition.setAutoCreated(Boolean.parseBoolean(values[0]));
             } else {
                 childNodeDefinition.setAutoCreated(false);
             }
             values = properties.get(J_MANDATORY);
             if (values != null && values.length > 0) {
                 childNodeDefinition.setMandatory(Boolean.parseBoolean(values[0]));
             } else {
                 childNodeDefinition.setMandatory(false);
             }
             values = properties.get(J_ON_PARENT_VERSION);
             if (values != null && values.length > 0) {
                 childNodeDefinition.setOnParentVersion(OnParentVersionAction.valueFromName(values[0]));
             } else {
                 childNodeDefinition.setOnParentVersion(OnParentVersionAction.VERSION);
             }
             values = properties.get(J_PROTECTED);
             if (values != null && values.length > 0) {
                 childNodeDefinition.setProtected(Boolean.parseBoolean(values[0]));
             } else {
                 childNodeDefinition.setProtected(false);
             }
             values = properties.get("j:requiredPrimaryTypes");
             childNodeDefinition.setRequiredPrimaryTypes(values);
             values = properties.get("j:defaultPrimaryType");
             if (values != null && values.length > 0) {
                 childNodeDefinition.setDefaultPrimaryType(values[0]);
             } else {
                 childNodeDefinition.setDefaultPrimaryType(null);
             }
             nodeType.validate();
             writeDefinitionFile(nodeTypeRegistry, cndPath);
 
             saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName) + "." + JCRContentUtils.replaceColon(lastPathSegment));
         } catch (NoSuchNodeTypeException e) {
             nodeTypeRegistryMap.remove(cndPath);
             throw e;
         }
     }
 
     private List<String> getCndChildren(String path, String pathLowerCase) throws RepositoryException {
         if (pathLowerCase.endsWith(CND)) {
             List<String> children = new ArrayList<String>();
             NodeTypeIterator nodeTypes = loadRegistry(path).getNodeTypes(module.getId());
             while (nodeTypes.hasNext()) {
                 children.add(nodeTypes.nextNodeType().getName());
             }
             return children;
         } else {
             String cndPath = getCndPath(path, pathLowerCase);
             String subPath = getSubPath(path, pathLowerCase);
             String[] splitPath = StringUtils.split(subPath, "/");
 
             List<String> children = new ArrayList<String>();
 
             if (splitPath.length == 1) {
                 String nodeTypeName = splitPath[0];
                 try {
                     ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
                     for (ExtendedItemDefinition itemDefinition : nodeType.getDeclaredItems(true)) {
                         if (itemDefinition.isUnstructured()) {
                             children.add(computeUnstructuredItemName(itemDefinition));
                         } else {
                             children.add(itemDefinition.getName());
                         }
                     }
                 } catch (NoSuchNodeTypeException e) {
                     // Ignore non-existing nodetype, no children
                 }
             }
             return children;
         }
     }
 
     private ExternalData getCndItemByPath(String path) throws RepositoryException {
         String pathLowerCase = path.toLowerCase();
         String cndPath = getCndPath(path, pathLowerCase);
         String subPath = getSubPath(path, pathLowerCase);
         String[] splitPath = StringUtils.split(subPath, "/");
         if (splitPath.length == 1) {
             String nodeTypeName = splitPath[0];
             ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
             return getNodeTypeData(path, nodeType);
         } else if (splitPath.length == 2) {
             String nodeTypeName = splitPath[0];
             String itemDefinitionName = splitPath[1];
             try {
                 ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
                 Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
                 if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                     return getPropertyDefinitionData(path, propertyDefinitionsAsMap.get(itemDefinitionName), false);
                 }
                 if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                     Integer type = Integer.valueOf(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                     if (nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type) != null)  {
                         return getPropertyDefinitionData(path, nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type), true);
                     }
                 }
                 Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                 if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                     return getChildNodeDefinitionData(path, childNodeDefinitionsAsMap.get(itemDefinitionName), false);
                 }
                 if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                     String type = StringUtils.replace(itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length()).trim(),"@@",":");
                     if (nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type) != null) {
                         return getChildNodeDefinitionData(path, nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type), true);
                     }
                 }
             } catch (NoSuchNodeTypeException e) {
                 throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
             }
         }
         throw new PathNotFoundException("Failed to get node " + path);
     }
 
     private ExternalData getNodeTypeData(String path, ExtendedNodeType nodeType) {
         Map<String, String[]> properties = new HashMap<String, String[]>();
         ExtendedNodeType[] declaredSupertypes = nodeType.getDeclaredSupertypes();
         String supertype = null;
         List<String> mixins = new ArrayList<String>();
         for (ExtendedNodeType declaredSupertype : declaredSupertypes) {
             if (declaredSupertype.isMixin()) {
                 mixins.add(declaredSupertype.getName());
             } else if (supertype == null) {
                 supertype = declaredSupertype.getName();
             }
         }
         if (supertype != null) {
             properties.put("j:supertype", new String[]{supertype});
         }
         if (!mixins.isEmpty()) {
             properties.put("j:mixins", mixins.toArray(new String[mixins.size()]));
         }
 
         properties.put("j:isAbstract", new String[]{String.valueOf(nodeType.isAbstract())});
         properties.put("j:isQueryable", new String[]{String.valueOf(nodeType.isQueryable())});
         properties.put("j:hasOrderableChildNodes", new String[]{String.valueOf(nodeType.hasOrderableChildNodes())});
         String itemsType = nodeType.getItemsType();
         if (itemsType != null) {
             properties.put("j:itemsType", new String[]{itemsType});
         }
         List<ExtendedNodeType> mixinExtends = nodeType.getMixinExtends();
         if (mixinExtends != null && !mixinExtends.isEmpty()) {
             Function<ExtendedNodeType, String> transformName = new Function<ExtendedNodeType, String>() {
                 public String apply(@Nullable ExtendedNodeType from) {
                     return from != null ? from.getName() : null;
                 }
             };
             properties.put("j:mixinExtends", Collections2.<ExtendedNodeType, String>transform(mixinExtends, transformName).toArray(new String[mixinExtends.size()]));
         }
         String primaryItemName = nodeType.getPrimaryItemName();
         if (primaryItemName != null) {
             properties.put("j:primaryItemName", new String[]{primaryItemName});
         }
         ExternalData externalData = new ExternalData(path, path, nodeType.isMixin() ? JNT_MIXIN_NODE_TYPE : JNT_PRIMARY_NODE_TYPE, properties);
         Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();
 
         for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
             Map<String, String[]> value = new HashMap<String, String[]>();
             i18nProperties.put(locale.toString(), value);
             value.put(Constants.JCR_TITLE, new String[]{nodeType.getLabel(locale)});
             value.put(Constants.JCR_DESCRIPTION, new String[]{nodeType.getDescription(locale)});
         }
 
         externalData.setI18nProperties(i18nProperties);
         return externalData;
     }
 
     private ExternalData getPropertyDefinitionData(String path, ExtendedPropertyDefinition propertyDefinition, boolean unstructured) {
         Map<String, String[]> properties = new HashMap<String, String[]>();
         properties.put(J_AUTO_CREATED, new String[]{String.valueOf(propertyDefinition.isAutoCreated())});
         properties.put(J_MANDATORY, new String[]{String.valueOf(propertyDefinition.isMandatory())});
         properties.put(J_ON_PARENT_VERSION, new String[]{OnParentVersionAction.nameFromValue(propertyDefinition.getOnParentVersion())});
         properties.put(J_PROTECTED, new String[]{String.valueOf(propertyDefinition.isProtected())});
         properties.put(J_REQUIRED_TYPE, new String[]{PropertyType.nameFromValue(propertyDefinition.getRequiredType())});
         properties.put(J_SELECTOR_TYPE, new String[]{SelectorType.nameFromValue(propertyDefinition.getSelector())});
         Map<String, String> selectorOptions = propertyDefinition.getSelectorOptions();
         List<String> selectorOptionsList = new ArrayList<String>();
         for (Map.Entry<String, String> entry : selectorOptions.entrySet()) {
             String option = entry.getKey();
             String value = entry.getValue();
             if (StringUtils.isNotBlank(value)) {
                 option += "='" + value + "'";
             }
             selectorOptionsList.add(option);
         }
         properties.put(J_SELECTOR_OPTIONS, selectorOptionsList.toArray(new String[selectorOptionsList.size()]));
         String[] valueConstraints = propertyDefinition.getValueConstraints();
         if (valueConstraints != null && valueConstraints.length > 0) {
             properties.put(J_VALUE_CONSTRAINTS, valueConstraints);
         }
         Value[] defaultValues = propertyDefinition.getDefaultValuesAsUnexpandedValue();
         if (defaultValues != null && defaultValues.length > 0) {
             try {
                 List<String> defaultValuesAsString = JahiaCndWriter.getValuesAsString(defaultValues);
                 List<String> unquotedValues = new ArrayList<String>();
                 for (String value : defaultValuesAsString) {
                     unquotedValues.add(StringUtils.removeEnd(StringUtils.removeStart(value, "'"), "'"));
                 }
                 properties.put(J_DEFAULT_VALUES, unquotedValues.toArray(new String[unquotedValues.size()]));
             } catch (IOException e) {
                 logger.error("Failed to get default values", e);
             }
         }
         properties.put(J_MULTIPLE, new String[]{String.valueOf(propertyDefinition.isMultiple())});
         String[] availableQueryOperators = propertyDefinition.getAvailableQueryOperators();
         List<String> ops = new ArrayList<String>();
         if (availableQueryOperators != null) {
             for (String op : availableQueryOperators) {
                 if (QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(op)) {
                     ops.add(Lexer.QUEROPS_EQUAL);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO.equals(op)) {
                     ops.add(Lexer.QUEROPS_NOTEQUAL);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN.equals(op)) {
                     ops.add(Lexer.QUEROPS_LESSTHAN);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO.equals(op)) {
                     ops.add(Lexer.QUEROPS_LESSTHANOREQUAL);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN.equals(op)) {
                     ops.add(Lexer.QUEROPS_GREATERTHAN);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO.equals(op)) {
                     ops.add(Lexer.QUEROPS_GREATERTHANOREQUAL);
                 } else if (QueryObjectModelConstants.JCR_OPERATOR_LIKE.equals(op)) {
                     ops.add(Lexer.QUEROPS_LIKE);
                 }
             }
             if (!ops.isEmpty()) {
                 properties.put(J_AVAILABLE_QUERY_OPERATORS, ops.toArray(new String[ops.size()]));
             }
         }
         properties.put(J_IS_FULL_TEXT_SEARCHABLE, new String[]{String.valueOf(propertyDefinition.isFullTextSearchable())});
         properties.put(J_IS_QUERY_ORDERABLE, new String[]{String.valueOf(propertyDefinition.isQueryOrderable())});
         properties.put(J_IS_FACETABLE, new String[]{String.valueOf(propertyDefinition.isFacetable())});
         properties.put(J_IS_HIERARCHICAL, new String[]{String.valueOf(propertyDefinition.isHierarchical())});
         properties.put(J_IS_INTERNATIONALIZED, new String[]{String.valueOf(propertyDefinition.isInternationalized())});
         properties.put(J_IS_HIDDEN, new String[]{String.valueOf(propertyDefinition.isHidden())});
         properties.put(J_INDEX, new String[]{IndexType.nameFromValue(propertyDefinition.getIndex())});
         properties.put(J_SCOREBOOST, new String[]{String.valueOf(propertyDefinition.getScoreboost())});
         String analyzer = propertyDefinition.getAnalyzer();
         if (analyzer != null) {
             properties.put(J_ANALYZER, new String[]{analyzer});
         }
         properties.put(J_ON_CONFLICT_ACTION, new String[]{OnConflictAction.nameFromValue(propertyDefinition.getOnConflict())});
         String itemType = propertyDefinition.getLocalItemType();
         if (itemType != null) {
             properties.put(J_ITEM_TYPE, new String[]{itemType});
         }
         ExternalData externalData = new ExternalData(path, path,
                 unstructured ? "jnt:unstructuredPropertyDefinition" : "jnt:propertyDefinition", properties);
         Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();
 
         for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
             Map<String, String[]> value = new HashMap<String, String[]>();
             i18nProperties.put(locale.toString(), value);
             value.put(Constants.JCR_TITLE, new String[]{propertyDefinition.getLabel(locale)});
         }
 
         externalData.setI18nProperties(i18nProperties);
         return externalData;
     }
 
     private ExternalData getChildNodeDefinitionData(String path, ExtendedNodeDefinition nodeDefinition, boolean unstructured) {
         Map<String, String[]> properties = new HashMap<String, String[]>();
         properties.put(J_AUTO_CREATED, new String[]{String.valueOf(nodeDefinition.isAutoCreated())});
         properties.put(J_MANDATORY, new String[]{String.valueOf(nodeDefinition.isMandatory())});
         properties.put(J_ON_PARENT_VERSION, new String[]{OnParentVersionAction.nameFromValue(nodeDefinition.getOnParentVersion())});
         properties.put(J_PROTECTED, new String[]{String.valueOf(nodeDefinition.isProtected())});
         String[] requiredPrimaryTypeNames = nodeDefinition.getRequiredPrimaryTypeNames();
         if (requiredPrimaryTypeNames != null) {
             properties.put("j:requiredPrimaryTypes", requiredPrimaryTypeNames);
         }
         String defaultPrimaryTypeName = nodeDefinition.getDefaultPrimaryTypeName();
         if (defaultPrimaryTypeName != null) {
             properties.put("j:defaultPrimaryType", new String[]{defaultPrimaryTypeName});
         }
         ExternalData externalData = new ExternalData(path, path,
                 unstructured ? "jnt:unstructuredChildNodeDefinition" : "jnt:childNodeDefinition", properties);
 
         Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();
 
         for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
             Map<String, String[]> value = new HashMap<String, String[]>();
             i18nProperties.put(locale.toString(), value);
             value.put(Constants.JCR_TITLE, new String[]{nodeDefinition.getLabel(locale)});
         }
 
         externalData.setI18nProperties(i18nProperties);
         return externalData;
     }
 
 
     private NodeTypeRegistry createRegistry() throws IOException, ParseException {
         NodeTypeRegistry ntr = new NodeTypeRegistry();
         ntr.initSystemDefinitions();
 
         List<JahiaTemplatesPackage> dependencies = module.getDependencies();
         for (JahiaTemplatesPackage depend : dependencies) {
             for (String s : depend.getDefinitionsFiles()) {
                 ntr.addDefinitionsFile(depend.getResource(s), depend.getId(), null);
             }
         }
 
         return ntr;
     }
 
     private synchronized NodeTypeRegistry loadRegistry(String path) throws RepositoryException {
         NodeTypeRegistry ntr = nodeTypeRegistryMap.get(path);
         if (ntr != null) {
             return ntr;
         } else {
             try {
                 ntr = createRegistry();
                 FileObject file = getFile(path);
                 if (file.exists()) {
                     ntr.addDefinitionsFile(new UrlResource(file.getURL()), module.getId(), null);
                     nodeTypeRegistryMap.put(path, ntr);
                 }
             } catch (IOException e) {
                 throw new RepositoryException("Failed to load node type registry", e);
             } catch (ParseException e) {
                 throw new RepositoryException("Failed to load node type registry", e);
             }
             return ntr;
         }
     }
 
     private synchronized void writeDefinitionFile(NodeTypeRegistry nodeTypeRegistry, String path) throws RepositoryException {
         try {
             Writer writer = null;
             try {
                 writer = new OutputStreamWriter(new FileOutputStream(getRealFile(path)), "UTF-8");
                 Map<String, String> namespaces = NodeTypeRegistry.getInstance().getNamespaces();
                 namespaces.remove("rep");
                 if (nodeTypeRegistryMap.containsKey(path)) {
                     nodeTypeRegistryMap.get(path).flushLabels();
                 }
                 Map<String, String> realUsedNamespaces = new TreeMap<String, String>();
                 NodeTypeIterator nodeTypes = nodeTypeRegistry.getNodeTypes(module.getId());
                 while (nodeTypes.hasNext()) {
                     ExtendedNodeType ntd = (ExtendedNodeType) nodeTypes.nextNodeType();
                     addMissingNamespace(namespaces, realUsedNamespaces, ntd);
                     ExtendedNodeType[] declaredSupertypes = ntd.getDeclaredSupertypes();
                     for (ExtendedNodeType declaredSupertype : declaredSupertypes) {
                         addMissingNamespace(namespaces, realUsedNamespaces, declaredSupertype);
                     }
                     ExtendedNodeDefinition[] childNodeDefinitions = ntd.getChildNodeDefinitions();
                     for (ExtendedNodeDefinition childNodeDefinition : childNodeDefinitions) {
                         addMissingNamespace(namespaces, realUsedNamespaces, childNodeDefinition.getDefaultPrimaryType());
                     }
                 }
 
                 new JahiaCndWriter(nodeTypeRegistry.getNodeTypes(module.getId()), realUsedNamespaces, writer);
             } finally {
                 IOUtils.closeQuietly(writer);
             }
         } catch (IOException e) {
             throw new RepositoryException("Failed to write definition file", e);
         }
     }
 
     private void addMissingNamespace(Map<String, String> namespaces, Map<String, String> realUsedNamespaces,
                                      ExtendedNodeType ntd) {
         if (ntd != null) {
             String ntdPrefix = ntd.getPrefix();
             if (!StringUtils.isEmpty(ntdPrefix) && !realUsedNamespaces.containsKey(ntdPrefix)) {
                 realUsedNamespaces.put(ntdPrefix, namespaces.get(ntdPrefix));
             }
         }
     }
 
     private synchronized void registerNamespace(ExternalData data) throws RepositoryException {
         String prefix = data.getProperties().get("j:prefix")[0];
         String uri = data.getProperties().get("j:uri")[0];
         NamespaceRegistry nsRegistry = JCRSessionFactory.getInstance().getNamespaceRegistry();
         NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
         boolean exists = false;
         try {
             nsRegistry.getURI(prefix);
             exists = true;
         } catch (NamespaceException e) {
             // the prefix is not registered yet
             try {
                 nsRegistry.getPrefix(uri);
                 exists = true;
             } catch (NamespaceException e2) {
                 // the uri is not registered yet
             }
         }
         if (exists || ntRegistry.getNamespaces().containsKey(prefix) || ntRegistry.getNamespaces().containsValue(uri)) {
             throw new NamespaceException();
         }
         nsRegistry.registerNamespace(prefix, uri);
         ntRegistry.getNamespaces().put(prefix, uri);
         nodeTypeRegistryMap.get(StringUtils.substringBeforeLast(data.getPath(), "/")).getNamespaces().put(prefix, uri);
     }
 
     /**
      * Inject mapping of file types to node types
      * @param fileTypeMapping
      */
     public void setFileTypeMapping(Map<String, String> fileTypeMapping) {
         this.fileTypeMapping = fileTypeMapping;
     }
 
     /**
      * Inject mapping of folder name (type) to node types
      * @param folderTypeMapping
      */
     public void setFolderTypeMapping(Map<String, String> folderTypeMapping) {
         this.folderTypeMapping = folderTypeMapping;
     }
 
     /**
      * Set the root folder of this module source
      * @param root
      */
     public void setRootResource(Resource root) {
         try {
             super.setRoot("file://" + root.getFile().getPath());
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     /**
      * Injection of supported node types
      * @param supportedNodeTypes
      */
     public void setSupportedNodeTypes(Set<String> supportedNodeTypes) {
         this.supportedNodeTypes = Collections.unmodifiableSet(supportedNodeTypes);
     }
 
     /**
      * Injection on runtime of the template package associated with this module source provider.
      * @param module
      */
     public void setModule(JahiaTemplatesPackage module) {
         this.module = module;
 
         try {
             for (String s : getChildren("/META-INF")) {
                 if (s.toLowerCase().endsWith(CND)) {
                     loadRegistry("/META-INF/"+s);
                 }
             }
         } catch (RepositoryException e) {
             logger.warn("Cannot read definition files for module",e);
         }
 
     }
 
     @Override
     public String[] getPropertyValues(String path, String propertyName) throws PathNotFoundException {
         if (SOURCE_CODE.equals(propertyName)) {
             InputStream is = null;
             try {
                 is = getFile(path).getContent().getInputStream();
                 java.nio.charset.Charset c = path.toLowerCase().endsWith(".properties") ? Charsets.ISO_8859_1:Charsets.UTF_8;
                 return new String[] {IOUtils.toString(is, c)};
             } catch (Exception e) {
                 logger.error("Failed to read source code", e);
             } finally {
                 IOUtils.closeQuietly(is);
             }
         }
         throw new PathNotFoundException(path + "/" + propertyName);
     }
 
     @Override
     public String[] getI18nPropertyValues(String path, String lang, String propertyName) throws PathNotFoundException {
         throw new PathNotFoundException(path + "/" + propertyName);
     }
 
     @Override
     public Binary[] getBinaryPropertyValues(String path, String propertyName) throws PathNotFoundException {
         throw new PathNotFoundException(path + "/" + propertyName);
     }
 
     protected File getRealFile(String relativePath) throws FileSystemException {
         return StringUtils.isEmpty(relativePath) || relativePath.equals("/") ? getRealRoot() : new File(getRealRoot(),
                 relativePath);
     }
 
     protected File getRealRoot() {
         if (realRoot == null) {
             try {
                 FileName name = getFile("/").getName();
                 realRoot = new File(((LocalFileName) name).getRootFile(), name.getPath());
             } catch (FileSystemException e) {
                 logger.error(e.getMessage(), e);
                 throw new IllegalArgumentException(e);
             }
         }
 
         return realRoot;
     }
 
     public void setJcrStoreService(JCRStoreService jcrStoreService) {
         this.jcrStoreService = jcrStoreService;
     }
 
     public void setModulesSourceSpringInitializer(ModulesSourceSpringInitializer modulesSourceSpringInitializer) {
         this.modulesSourceSpringInitializer = modulesSourceSpringInitializer;
     }
 
     public void setModulesImportExportHelper(ModulesImportExportHelper modulesImportExportHelper) {
         this.modulesImportExportHelper = modulesImportExportHelper;
     }
 
     static class SortedProperties extends Properties {
         private static final long serialVersionUID = -6636432847084484922L;
 
         @SuppressWarnings({ "rawtypes", "unchecked" })
         @Override
         public synchronized Enumeration<Object> keys() {
             return IteratorUtils.asEnumeration(new TreeSet(super.keySet()).iterator());
         }
     }
 }
