 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  *
  * $Id$
  */
 
 package org.nuxeo.ecm.webengine.model.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.webengine.ResourceBinding;
 import org.nuxeo.ecm.webengine.WebEngine;
 import org.nuxeo.ecm.webengine.WebException;
 import org.nuxeo.ecm.webengine.debug.ModuleTracker;
 import org.nuxeo.ecm.webengine.model.AdapterNotFoundException;
 import org.nuxeo.ecm.webengine.model.AdapterType;
 import org.nuxeo.ecm.webengine.model.LinkDescriptor;
 import org.nuxeo.ecm.webengine.model.Messages;
 import org.nuxeo.ecm.webengine.model.Module;
 import org.nuxeo.ecm.webengine.model.Resource;
 import org.nuxeo.ecm.webengine.model.ResourceType;
 import org.nuxeo.ecm.webengine.model.TypeNotFoundException;
 import org.nuxeo.ecm.webengine.model.Validator;
 import org.nuxeo.ecm.webengine.model.WebContext;
 import org.nuxeo.ecm.webengine.scripting.ScriptFile;
 
 /**
  * The default implementation for a web configuration
  *
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  */
 public class ModuleImpl implements Module {
 
     private static final Log log = LogFactory.getLog(ModuleImpl.class);
 
     protected final WebEngine engine;
     protected final Object typeLock = new Object();
     protected TypeRegistry typeReg;
     protected ModuleConfiguration configuration;
     protected final ModuleImpl superModule;
     protected LinkRegistry linkReg;
     protected final String skinPathPrefix;
     protected ResourceType rootType; 
     
     protected Messages messages;
     protected DirectoryStack dirStack;
 
     protected ModuleTracker tracker;
     
     // cache used for resolved files
     protected ConcurrentMap<String, ScriptFile> fileCache;
     
 
     public ModuleImpl(WebEngine engine, ModuleImpl superModule, ModuleConfiguration config) throws Exception {        
         this.engine = engine;
         this.superModule = superModule;
         this.configuration = config;
         skinPathPrefix = new StringBuilder()
             .append(engine.getSkinPathPrefix()).append('/').append(config.name).toString();
         fileCache = new ConcurrentHashMap<String, ScriptFile>();
         loadConfiguration();
         reloadMessages();
         loadDirectoryStack();
     }
     
     public ModuleTracker getTracker() {
         if (tracker == null) { // tracker will be installed only in debug mode
             tracker = new ModuleTracker(this);
         }
         return tracker;
     }
 
     /**
      * Whether or not this module has a GUI and should be listed in available GUI module list.
      * For example REST modules usually has no GUI 
      * @return true if headless (no GUI is provided), false otherwise
      */
     public boolean isHeadless() {
         return configuration.isHeadless;
     }
     
     /**
      * 
      * @return null if no natures was specified
      */
     public Set<String> getNatures() {
         return configuration.natures;
     }
     
     public boolean hasNature(String natureId) {
         return configuration.natures != null && configuration.natures.contains(natureId);
     }
     
     public WebEngine getEngine() {
         return engine;
     }
 
     public String getName() {
         return configuration.name;
     }
     
     public ModuleImpl getSuperModule() {
         return superModule;
     }
 
     public ModuleConfiguration getModuleConfiguration() {
         return configuration;
     }
 
     public ResourceType getRootType() {
         // force type registry creation if needed
         getTypeRegistry();
         return rootType;
     }
     
     
     public Resource getRootObject(WebContext ctx) {
         try {
             ((AbstractWebContext)ctx).setModule(this);
             Resource obj = ctx.newObject(getRootType());
             obj.setRoot(true);
             return obj;
         } catch (Exception e) {
             throw WebException.wrap("Failed to instantiate the root resource for module "+getName(),  e);
         }
     }
 
     public String getSkinPathPrefix() {
         return skinPathPrefix;
     }
 
     public TypeRegistry getTypeRegistry() {
         if (typeReg == null) { // create type registry if not already created
             synchronized (typeLock) {
                 if (typeReg == null) {
                     typeReg = createTypeRegistry();
                     rootType = typeReg.getType(configuration.rootType);
                 }
             }
         }
         return typeReg;
     }
 
 
     public Class<?> loadClass(String className) throws ClassNotFoundException {
         return engine.loadClass(className);
     }
 
     public ResourceType getType(String typeName) {
         ResourceType type = getTypeRegistry().getType(typeName);
         if (type == null) {
             throw new TypeNotFoundException(typeName);
         }
         return type;
     }
 
     public ResourceType[] getTypes() {
         return getTypeRegistry().getTypes();
     }
 
     public AdapterType[] getAdapters() {
         return getTypeRegistry().getAdapters();
     }
 
     public AdapterType getAdapter(Resource ctx, String name) {
         AdapterType type = getTypeRegistry().getAdapter(ctx, name);
         if (type == null) {
             throw new AdapterNotFoundException(ctx, name);
         }
         return type;
     }
 
     public List<String> getAdapterNames(Resource ctx) {
         return getTypeRegistry().getAdapterNames(ctx);
     }
 
     public List<AdapterType> getAdapters(Resource ctx) {
         return getTypeRegistry().getAdapters(ctx);
     }
 
     public List<String> getEnabledAdapterNames(Resource ctx) {
         return getTypeRegistry().getEnabledAdapterNames(ctx);
     }
 
     public List<AdapterType> getEnabledAdapters(Resource ctx) {
         return getTypeRegistry().getEnabledAdapters(ctx);
     }
 
     public String getMediaTypeId(MediaType mt) {
         if (configuration.mediatTypeRefs == null) {
             return null;
         }
         MediaTypeRef[] refs = configuration.mediatTypeRefs;
         for (MediaTypeRef ref : refs) {
             String id = ref.match(mt);
             if (id != null) {
                 return id;
             }
         }
         return null;
     }
 
     public List<ResourceBinding> getResourceBindings() {
         return configuration.resources;
     }
 
     public boolean isDerivedFrom(String moduleName) {
         if (configuration.name.equals(moduleName)) {
             return true;
         }
         if (superModule != null) {
             return superModule.isDerivedFrom(moduleName);
         }
         return false;
     }
 
     public Validator getValidator(String docType) {
         if (configuration.validators != null ){
             return configuration.validators.get(docType);
         }
         return null;
     }
 
     
     public void loadConfiguration() {
         linkReg = new LinkRegistry();
         if (configuration.links != null) {
             for (LinkDescriptor link : configuration.links) {
                 linkReg.registerLink(link);
             }
         }
         configuration.links = null; // avoid storing unused data
     }
     
 
     public List<LinkDescriptor> getLinks(String category) {
         return linkReg.getLinks(category);
     }
 
     public List<LinkDescriptor> getActiveLinks(Resource context, String category) {
         return linkReg.getActiveLinks(context, category);
     }
 
     public LinkRegistry getLinkRegistry() {
         return linkReg;
     }
 
 
     public String getTemplateFileExt() {
         return configuration.templateFileExt;
     }
 
     public void flushSkinCache() {
         log.info("Flushing skin cache for module: "+getName());
         fileCache = new ConcurrentHashMap<String, ScriptFile>();
     }
 
     public void flushTypeCache() {
         log.info("Flushing type cache for module: "+getName());
         synchronized (typeLock) {
             typeReg = null; // type registry will be recreated on first access
         }
     }
 
     public void flushCache() {
         //TODO: reload module configuration or recreate module
         flushSkinCache();
         flushTypeCache();
         engine.getWebLoader().flushCache();
     }
 
     public static File getSkinDir(File moduleDir) {
         return new File(moduleDir, "skin");
     }
 
     protected void loadDirectoryStack() {
         dirStack = new DirectoryStack();
         try {
             File skin = getSkinDir(configuration.directory);
             if (skin.isDirectory()) {
                 dirStack.addDirectory(skin);
             }
             if (superModule instanceof ModuleImpl) {
                 DirectoryStack ds = ((ModuleImpl)superModule).dirStack;
                 if (ds != null) {
                     dirStack.getDirectories().addAll(ds.getDirectories());
                 }
             }
         } catch (IOException e) {
             WebException.wrap("Failed to load directories stack", e);
         }
     }
 
     public ScriptFile getFile(String path) {
         int len = path.length();
         if (len == 0) {
             return null;
         }
         char c = path.charAt(0);
         if (c == '.') { // avoid getting files outside the web root
             path = new Path(path).makeAbsolute().toString();
         } else if (c != '/') {// avoid doing duplicate entries in document stack cache
             path = new StringBuilder(len+1).append("/").append(path).toString();
         }
         try {
             return findFile(new Path(path).makeAbsolute().toString());
         } catch (IOException e) {
             throw WebException.wrap(e);
         }
     }
 
     /**
      * @param path a normalized path (absolute path)
      * @return
      */
     protected ScriptFile findFile(String path) throws IOException {
         ScriptFile file = fileCache.get(path);
         if (file == null) {
             File f = dirStack.getFile(path);
             if (f != null) {
                 file = new ScriptFile(f);
                 fileCache.put(path, file);
             }
         }
         return file;
     }
 
     public ScriptFile getSkinResource(String path) throws IOException {
         File file = dirStack.getFile(path);
         if (file != null) {
             return new ScriptFile(file);
         }
         return null;
     }
 
     /**
      * TODO There are no more reasons to lazy load the type registry since module are lazy loaded.
      * Type registry must be loaded at module creation 
      * 
      * @return
      */
     public TypeRegistry createTypeRegistry() {
         //double s = System.currentTimeMillis();
         GlobalTypes gtypes = engine.getGlobalTypes();
         TypeRegistry typeReg = null;
         // install types from super modules
         if (superModule != null) { //TODO add type reg listener on super modules to update types  when needed?
             typeReg = new TypeRegistry(superModule.getTypeRegistry(), engine, this);
         } else {
             typeReg = new TypeRegistry(gtypes.getTypeRegistry(), engine, this);
         }
         if (configuration.directory.isDirectory()) {
             DefaultTypeLoader loader = new DefaultTypeLoader(this, typeReg, configuration.directory);
             loader.load();
         }
         //System.out.println(">>>>>>>>>>>>>"+((System.currentTimeMillis()-s)/1000));
         return typeReg;
     }
 
     public File getRoot() {
         return configuration.directory;
     }
 
     public void reloadMessages() {
         messages = new Messages(superModule != null
                 ? superModule.getMessages() : null, this);
     }
 
     public Messages getMessages() {
         return messages;
     }
 
     @SuppressWarnings("unchecked")
     public Map<String,String> getMessages(String language) {
         log.info("Loading i18n files for module "+configuration.name);
         File file = new File(configuration.directory,  new StringBuilder()
                     .append("/i18n/messages_")
                     .append(language)
                     .append(".properties").toString());
         InputStream in = null;
         try {
             in = new FileInputStream(file);
             Properties p = new Properties();
             p.load(in);
             return new HashMap(p); // HashMap is faster than Properties
         } catch (IOException e) {
             return null;
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ee) {
                     log.error(ee);
                 }
             }
         }
     }
     
     
     @Override
     public String toString() {
         return getName();
     }
 
 
 }
