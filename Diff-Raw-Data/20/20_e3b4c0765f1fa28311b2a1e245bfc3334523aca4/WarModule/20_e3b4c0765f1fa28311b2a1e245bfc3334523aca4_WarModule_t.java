 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.lavender.modules;
 
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Filter;
 import net.oneandone.lavender.modules.project.ProjectConfig;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.zip.ZipNode;
 import org.pustefixframework.live.LiveResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.JAXBException;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 /**
  * Iterates static resources of the Pustefix application. Valid static resource path are defined in WEB-INF/project.xml.
  * Resources can be found in the WAR or in nested JARs.
  *
  * This class is *not* called PustefixModule, because that term is also used by pustefix for it's modules.
  */
 public class WarModule extends Module {
     private static final Logger LOG = LoggerFactory.getLogger(Module.class);
 
     public static final List<String> DEFAULT_INCLUDE_EXTENSIONS = new ArrayList<>(Arrays.asList(
             "gif", "png", "jpg", "jpeg", "ico", "swf", "css", "js"));
 
    public static final String PROPERTIES = "WEB-INF/lavender.properties";
 
     public static List<Module> fromWebapp(boolean prod, Node webapp, String svnUsername, String svnPassword) throws IOException {
         Node webappSource;
         List<Module> result;
         Properties properties;
         WarModule root;
         JarModuleConfig jarConfig;
 
         LOG.trace("scanning " + webapp);
         properties = getPropertiesOpt(webapp);
         if (properties == null) {
             throw new IOException("lavender.properties not found");
         }
         result = new ArrayList<>();
         webappSource = prod ? webapp : live(webapp);
         root = create(Filter.forProperties(properties, "pustefix", DEFAULT_INCLUDE_EXTENSIONS), webappSource);
         result.add(root);
         for (Node jar : webapp.find("WEB-INF/lib/*.jar")) {
             jarConfig = loadModuleXml(root, jar);
             if (jarConfig != null) {
                 result.addAll(jarModule(prod, root, jar, jarConfig, svnUsername, svnPassword));
             }
         }
         for (SvnModuleConfig config : SvnModuleConfig.parse(properties)) {
             LOG.info("adding svn module " + config.folder);
             result.add(config.create(webapp.getWorld(), svnUsername, svnPassword));
         }
         return result;
     }
 
     public static List<Module> jarModule(boolean prod, WarModule root, Node jar, JarModuleConfig config,
                                          String svnUsername, String svnPassword) throws IOException {
         List<Module> result;
         Properties properties;
        Module jarModule;
        Resource propertiesResource;
 
         result = new ArrayList<>();
         if (jar instanceof FileNode) {
             if (!prod) {
                 jar = live(jar);
             }
             if (jar.isFile()) {
                 jar = ((FileNode) jar).openJar();
             }
            jarModule = new JarFileModule(root.getFilter(), Docroot.WEB, config, jar);
         } else {
             if (!prod) {
                 throw new UnsupportedOperationException("live mechanism not supported for jar streams");
             }
            jarModule = new JarStreamModule(root.getFilter(), Docroot.WEB, config, jar);
         }
        result.add(jarModule);
        // TODO: hack
        propertiesResource = jarModule.probe(PROPERTIES);
        if (propertiesResource != null) {
            properties = jar.getWorld().memoryNode(propertiesResource.getData()).readProperties();
            // TODO: reject unknown properties
             for (SvnModuleConfig svnConfig : SvnModuleConfig.parse(properties)) {
                 result.add(svnConfig.create(jar.getWorld(), svnUsername, svnPassword));
             }
         }
         return result;
     }
 
 
     public static Node live(Node root) throws IOException {
         File resolved;
 
         try {
             if (root instanceof FileNode) {
                 resolved = LiveResolver.getInstance().resolveLiveRoot(((FileNode) root).getAbsolute(), "/foo");
             } else if (root instanceof ZipNode) {
                 resolved = LiveResolver.getInstance().resolveLiveRoot(((ZipNode) root).getRoot().getZip().getName(), "/foo");
             } else {
                 throw new IllegalArgumentException(root.toString());
             }
         } catch (Exception e) {
             throw new IOException("cannot resolve life root", e);
         }
         return resolved == null ? root : root.getWorld().file(resolved);
     }
 
     //--
 
     private static JarModuleConfig loadModuleXml(WarModule parent, Node jar) throws IOException {
         ZipInputStream jarInputStream;
         ZipEntry jarEntry;
 
         jarInputStream = new ZipInputStream(jar.createInputStream());
         while ((jarEntry = jarInputStream.getNextEntry()) != null) {
             if (isModuleXml(jarEntry)) {
                 try {
                     return new JarModuleConfig(parent, jarInputStream);
                 } catch (JAXBException e) {
                     throw new IOException("cannot load module descriptor", e);
                 }
             }
         }
         return null;
     }
 
     private static boolean isModuleXml(ZipEntry entry) {
         return entry.getName().equals("META-INF/pustefix-module.xml");
     }
 
     public static Properties getPropertiesOpt(Node webapp) throws IOException {
         Node src;
 
         src = webapp.join(PROPERTIES);
         if (!src.exists()) {
             // TODO: dump this compatibility check as soon as I have ITs with new wars
             src = webapp.join("WEB-INF/lavendel.properties");
             if (!src.exists()) {
                 return null;
             }
         }
         return src.readProperties();
     }
 
     public static WarModule create(Filter filter, Node webapp) throws IOException {
         ProjectConfig config;
 
         try (InputStream src = webapp.join("WEB-INF/project.xml").createInputStream()) {
             config = JAXB.unmarshal(src, ProjectConfig.class);
         }
         return new WarModule(filter, config, webapp);
     }
 
     //--
 
     private final Node webapp;
     private final ProjectConfig config;
 
     public WarModule(Filter filter, ProjectConfig config, Node webapp) throws IOException {
         super(filter, Docroot.WEB, config.getProject().getName(), true, "");
 
         this.webapp = webapp;
         this.config = config;
     }
 
     public Iterator<Resource> iterator() {
         try {
             return WarResourceIterator.create(this, webapp);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public Resource probeIncluded(String path) throws IOException {
         Node node;
 
         if (!isPublicResource(path)) {
             return null;
         }
         node = webapp.join(path);
         if (!node.exists()) {
             return null;
         }
         return DefaultResource.forNode(node, path);
     }
 
     public boolean isPublicResource(String resourceName) {
         if (resourceName.startsWith("WEB-INF")) {
             return false;
         }
 
         for (String path : config.getApplication().getStatic().getPath()) {
             if (resourceName.startsWith(path)) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public void saveCaches() {
         // nothing to do
     }
 }
