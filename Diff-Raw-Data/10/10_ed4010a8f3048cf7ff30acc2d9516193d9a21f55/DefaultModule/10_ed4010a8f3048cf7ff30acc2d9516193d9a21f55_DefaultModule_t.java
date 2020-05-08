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
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.filter.Action;
 import net.oneandone.sushi.fs.filter.Filter;
 import net.oneandone.sushi.fs.filter.Predicate;
 import net.oneandone.sushi.fs.zip.ZipNode;
 import net.oneandone.sushi.util.Separator;
 import net.oneandone.sushi.xml.Selector;
 import net.oneandone.sushi.xml.XmlException;
 import org.pustefixframework.live.LiveResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 public class DefaultModule extends Module {
     //--
 
     public static final List<String> DEFAULT_INCLUDES = new ArrayList<>(Arrays.asList(
             "**/*.gif", "**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.ico", "**/*.swf", "**/*.css", "**/*.js"));
 
     public static Filter filterForProperties(Properties properties, String prefix, List<String> defaultIncludes) {
         Filter result;
 
         result = new Filter();
         result.include(get(properties, prefix + ".includes", defaultIncludes));
         result.exclude(get(properties, prefix + ".excludes", Collections.EMPTY_LIST));
         return result;
     }
 
     private static List<String> get(Properties properties, String key, List<String> dflt) {
         String str;
 
         str = properties.getProperty(key);
        return str == null ? dflt : Separator.COMMA.split(str);
     }
 
     //--
 
     private static final Logger LOG = LoggerFactory.getLogger(Module.class);
 
     public static final String PROPERTIES = "PUSTEFIX-INF/lavender.properties";
 
     public static List<Module> fromWebapp(boolean prod, Node webapp, String svnUsername, String svnPassword) throws IOException {
         Node webappSource;
         List<Module> result;
         Properties properties;
         WarConfig rootConfig;
         DefaultModule root;
         JarConfig jarConfig;
         Filter filter;
 
         LOG.trace("scanning " + webapp);
         properties = getPropertiesOpt(webapp);
         if (properties == null) {
             throw new IOException("lavender.properties not found");
         }
         result = new ArrayList<>();
         webappSource = prod ? webapp : live(webapp);
         rootConfig = WarConfig.fromXml(webapp);
         filter = filterForProperties(properties, "pustefix", DEFAULT_INCLUDES);
         root = jarModule(rootConfig, filter, webappSource);
         result.add(root);
         for (Node jar : webapp.find("WEB-INF/lib/*.jar")) {
             jarConfig = loadJarModuleConfig(rootConfig, jar);
             if (jarConfig != null) {
                 result.addAll(jarModule(prod, jar, filter, jarConfig, svnUsername, svnPassword));
             }
         }
         for (SvnModuleConfig config : SvnModuleConfig.parse(properties, DEFAULT_INCLUDES)) {
             LOG.info("adding svn module " + config.folder);
             result.add(config.create(webapp.getWorld(), svnUsername, svnPassword));
         }
         return result;
     }
 
     public static List<Module> jarModule(boolean prod, Node jarOrig, Filter filter, JarConfig config,
                                          String svnUsername, String svnPassword) throws IOException {
         List<Module> result;
         Node jarLive;
         Properties properties;
         Module jarModule;
         Node propertiesNode;
         Object[] tmp;
 
         result = new ArrayList<>();
         if (jarOrig instanceof FileNode) {
             if (prod) {
                 jarLive = jarOrig;
             } else {
                 jarLive = live(jarOrig);
             }
             if (jarLive.isFile()) {
                 jarLive = ((FileNode) jarLive).openJar();
                 propertiesNode = jarLive.join(PROPERTIES);
             } else {
                 propertiesNode = ((FileNode) jarOrig).openJar().join(PROPERTIES);
             }
             jarModule = new DefaultModule(Docroot.WEB, config.getModuleName(), files(filter, config, jarLive));
         } else {
             if (!prod) {
                 throw new UnsupportedOperationException("live mechanism not supported for jar streams");
             }
             tmp = DefaultModule.fromJar(filter, Docroot.WEB, config, jarOrig);
             jarModule = (Module) tmp[0];
             propertiesNode = (Node) tmp[1];
         }
         result.add(jarModule);
 
         if (propertiesNode != null && propertiesNode.exists()) {
             properties = propertiesNode.readProperties();
             // TODO: reject unknown properties
             for (SvnModuleConfig svnConfig : SvnModuleConfig.parse(properties, DEFAULT_INCLUDES)) {
                 result.add(svnConfig.create(propertiesNode.getWorld(), svnUsername, svnPassword));
             }
         }
         return result;
     }
 
     private static Map<String, Node> files(final Filter filter, final JarConfig config, final Node exploded) throws IOException {
         Filter f;
         final Map<String, Node> result;
 
         result = new HashMap<>();
         f = exploded.getWorld().filter().predicate(Predicate.FILE).includeAll();
         f.invoke(exploded, new Action() {
             public void enter(Node node, boolean isLink) {
             }
 
             public void enterFailed(Node node, boolean isLink, IOException e) throws IOException {
                 throw e;
             }
 
             public void leave(Node node, boolean isLink) {
             }
 
             public void select(Node node, boolean isLink) {
                 String path;
                 String resourcePath;
 
                 path = node.getRelative(exploded);
                 if (filter.matches(path)) {
                     resourcePath = config.getPath(path);
                     if (resourcePath != null) {
                         result.put(resourcePath, node);
                     }
                 }
             }
         });
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
 
     private static JarConfig loadJarModuleConfig(WarConfig parent, Node jar) throws IOException {
         ZipEntry entry;
 
         // TODO: expensive
         try (ZipInputStream jarInputStream = new ZipInputStream(jar.createInputStream())) {
             while ((entry = jarInputStream.getNextEntry()) != null) {
                 if (entry.getName().equals("META-INF/pustefix-module.xml")) {
                     try {
                         return JarConfig.load(jar.getWorld().getXml(), parent, jarInputStream);
                     } catch (SAXException | XmlException e) {
                         throw new IOException(jar + ": cannot load module descriptor:" + e.getMessage(), e);
                     }
                 }
             }
         }
         return null;
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
 
     public static DefaultModule jarModule(WarConfig config, Filter filter, Node webapp) throws IOException {
         Element root;
         Selector selector;
         String name;
 
         try {
             root = webapp.join("WEB-INF/project.xml").readXml().getDocumentElement();
             selector = webapp.getWorld().getXml().getSelector();
             name = selector.string(root, "project/name");
             return new DefaultModule(Docroot.WEB, name, scanJar(config, filter, webapp));
         } catch (SAXException | XmlException e) {
             throw new IOException("cannot load project descriptor: " + e);
         }
     }
 
     private static Map<String, Node> scanJar(final WarConfig global, final Filter filter, final Node exploded) throws IOException {
         Filter f;
         final Map<String, Node> result;
 
         result = new HashMap<>();
         f = exploded.getWorld().filter().predicate(Predicate.FILE).includeAll();
         f.invoke(exploded, new Action() {
             public void enter(Node node, boolean isLink) {
             }
 
             public void enterFailed(Node node, boolean isLink, IOException e) throws IOException {
                 throw e;
             }
 
             public void leave(Node node, boolean isLink) {
             }
 
             public void select(Node node, boolean isLink) {
                 String path;
 
                 path = node.getRelative(exploded);
                 if (filter.matches(path) && global.isPublicResource(path)) {
                     result.put(path, node);
                 }
             }
         });
         return result;
     }
 
     //--
 
     /** To properly make jars available as a module, I have to load them into memory when the jar is itself packaged into a war. */
     public static Object[] fromJar(Filter filter, String type, JarConfig config, Node jar) throws IOException {
         World world;
         ZipEntry entry;
         String path;
         ZipInputStream src;
         Node root;
         Node child;
         boolean isProperty;
         Node propertyNode;
         Map<String, Node> files;
         String resourcePath;
 
         world = jar.getWorld();
         root = world.getMemoryFilesystem().root().node(UUID.randomUUID().toString(), null).mkdir();
         src = new ZipInputStream(jar.createInputStream());
         propertyNode = null;
         files = new HashMap<>();
         resourcePath = null; // definite assignment
         while ((entry = src.getNextEntry()) != null) {
             path = entry.getName();
             if (!entry.isDirectory()) {
                 isProperty = PROPERTIES.equals(path);
                 if (isProperty || ((resourcePath = config.getPath(path)) != null && filter.matches(path))) {
                     child = root.join(path);
                     child.getParent().mkdirsOpt();
                     world.getBuffer().copy(src, child);
                     if (isProperty) {
                         propertyNode = child;
                     } else {
                         files.put(resourcePath, child);
                     }
                 }
             }
         }
         return new Object[] { new DefaultModule(type, config.getModuleName(), files), propertyNode };
     }
 
     //--
 
     private final Map<String, Node> files;
 
     public DefaultModule(String type, String name, Map<String, Node> files) throws IOException {
         super(type, name, true, "");
         this.files = files;
     }
 
     public Iterator<Resource> iterator() {
         return new ResourceIterator(files.entrySet().iterator());
     }
 
     public Resource probe(String path) throws IOException {
         Node file;
 
         file = files.get(path);
         return file == null ? null : DefaultResource.forNode(file, path);
     }
 
     @Override
     public void saveCaches() {
         // nothing to do
     }
 }
