 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.installer;
 
 import org.apache.commons.io.FileUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import sorcer.core.SorcerEnv;
 import sorcer.resolver.Resolver;
 import sorcer.util.Artifact;
 import sorcer.util.ArtifactCoordinates;
 import sorcer.util.PropertiesLoader;
 import sorcer.util.Zip;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.*;
 import java.io.*;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * User: prubach
  * Date: 14.05.13
  * Time: 10:18
  */
 public class Installer {
     protected Map<String, String> groupDirMap = new HashMap<String, String>();
     protected Map<String, String> versionsMap = new HashMap<String, String>();
 
     private static String MARKER_FILENAME="sorcer_jars_installed.tmp";
 
     private static String COMMONS_LIBS="commons";
 
     private static String VERSIONS_PROPS_FILE=SorcerEnv.getHomeDir() + File.separator + "configs" +
             File.separator + "groupversions.properties";
 
     private static String REPOLAYOUT_PROPS_FILE=SorcerEnv.getExtDir() + File.separator + "configs" +
             File.separator + "repolayout.properties";
 
     private static String repoDir;
     int errorCount = 0;
 
     protected static final Logger logger = Logger.getLogger(Installer.class.getName());
 
     {
         try {
             String fileName;
             File tempDir = new File(System.getProperty("java.io.tmpdir"));
             if (SorcerEnv.getHomeDir()!=null)
                 fileName = SorcerEnv.getHomeDir() + File.separator + "logs" + File.separator + "sos_jar_install.log";
             else
                 fileName = tempDir.getAbsolutePath() + File.separator + "logs" + File.separator + "sos_jar_install.log";
             FileHandler fh = new FileHandler();
             fh.setFormatter(new SimpleFormatter());
             logger.addHandler(fh);
             logger.setLevel(Level.ALL);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         try {
             repoDir = Resolver.getRepoDir();
             if (repoDir == null)                          {
                 throw new IOException("Repo Dir is null");
             }
             else
                 FileUtils.forceMkdir(new File(repoDir));
         } catch (IOException io) {
             logger.severe("Problem installing jars to local maven repository - repository directory does not exist! " + io.getMessage());
             System.exit(-1);
         }
         String resourceName = "META-INF/maven/groupversions.properties";
         URL resourceVersions = Thread.currentThread().getContextClassLoader().getResource(resourceName);
         if (resourceVersions == null) {
             throw new RuntimeException("Could not find versions.properties");
         }
 
         resourceName = "META-INF/maven/repolayout.properties";
         URL resourceRepo = Thread.currentThread().getContextClassLoader().getResource(resourceName);
         if (resourceRepo == null) {
             throw new RuntimeException("Could not find repolayout.properties");
         }
         Properties propertiesRepo = new Properties();
         Properties propertiesVer = new Properties();
         InputStream inputStream = null;
         try {
             inputStream = resourceRepo.openStream();
             propertiesRepo.load(inputStream);
             // properties is a Map<Object, Object> but it contains only Strings
             @SuppressWarnings("unchecked")
             Map<String, String> propertyMap = (Map) propertiesRepo;
             groupDirMap.putAll(propertyMap);
 
             File repoFile = new File(REPOLAYOUT_PROPS_FILE);
             if (repoFile.exists()) {
                 Properties props = new Properties();
                 props.load(new FileInputStream(repoFile));
                 propertyMap = (Map) props;
                  groupDirMap.putAll(propertyMap);
             }
 
             inputStream = resourceVersions.openStream();
             propertiesVer.load(inputStream);
             // properties is a Map<Object, Object> but it contains only Strings
             @SuppressWarnings("unchecked")
             Map<String, String> propertyMapVer = (Map) propertiesVer;
             versionsMap.putAll(propertyMapVer);
 
             File versionsFile = new File(VERSIONS_PROPS_FILE);
             if (versionsFile.exists()) {
                 Properties props = new Properties();
                 props.load(new FileInputStream(versionsFile));
                 propertyMapVer = (Map) props;
                 versionsMap.putAll(propertyMapVer);
             }
         } catch (IOException e) {
             throw new RuntimeException("Could not load repolayout.properties", e);
         } finally {
             close(inputStream);
         }
     }
 
     public void install() {
         for (String group : groupDirMap.keySet()) {
             String dir = groupDirMap.get(group);
             String version = versionsMap.get(group);
 
             if (dir == null || version == null || !new File(Resolver.getRootDir() + "/" + dir).exists()) {
                 logger.severe("Problem installing jars for groupId: " + group + " directory or version not specified: " + dir + " " + version);
                 errorCount++;
                 continue;
             }
 
             // unzip Sigar
             if (group.equals("org.sorcersoft.sigar")) {
                 File[] jars = new File(Resolver.getRootDir() + "/" + dir).listFiles(new FileFilter() {
                     @Override
                     public boolean accept(File pathname) {
                         if (pathname.getName().endsWith("native.zip"))
                             return true;
                         return false;
                     }
                 });
                 for (File zipFile : jars) {
                     try {
                     File sigarDir = new File(Resolver.getRepoDir() + "/" + group.replace(".", "/") + "/sigar/" + version);
                     File libraryDir = new File(sigarDir, "lib");
                     if (!libraryDir.exists()) {
                         Zip.unzip(zipFile, sigarDir);
                        FileUtils.copyFile(zipFile, new File(sigarDir, "sigar-native.zip"));
                     }
                     } catch (IOException io) {
                         logger.severe("Problem unzipping sigar-native.zip to repo: " + io.getMessage());
                     }
                 }
             }
 
             File[] jars = new File(Resolver.getRootDir() + "/" + dir).listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File pathname) {
                     if (pathname.getName().endsWith("jar"))
                         return true;
                     return false;
                 }
             });
 
             for (File jar : jars) {
                 String fileNoExt = jar.getName().replace(".jar", "");
                 String artifactDir = Resolver.getRepoDir() + "/" + group.replace(".", "/") + "/" + fileNoExt + "/" + version;
                 try {
                     FileUtils.forceMkdir(new File(artifactDir));
                     extractZipFile(jar, "META-INF/maven/" + group + "/" + fileNoExt + "/pom.xml",
                             artifactDir + "/" + fileNoExt + "-" + version + ".pom");
                     FileUtils.copyFile(jar, new File(artifactDir, fileNoExt + "-" + version + ".jar"));
                 } catch (IOException io) {
                     errorCount++;
                     logger.severe("Problem installing jar: " + fileNoExt + " to: " + artifactDir);
                 }
             }
         }
         // install commons
         File[] jars = new File(Resolver.getRootDir() + "/" + COMMONS_LIBS).listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 if (pathname.getName().endsWith("jar"))
                     return true;
                 return false;
             }
         });
 
         for (File jar : jars) {
             ArtifactCoordinates ac = getArtifactCoordinatesFromJar(jar);
             if (ac!=null && ac.getGroupId()!=null && ac.getArtifactId()!=null && ac.getVersion()!=null) {
                 String artifactDir = Resolver.getRepoDir() + "/" + ac.getGroupId().replace(".", "/") + "/" + ac.getArtifactId() + "/" + ac.getVersion();
                 try {
                     FileUtils.forceMkdir(new File(artifactDir));
                     extractZipFile(jar, "META-INF/maven/" + ac.getGroupId() + "/" + ac.getArtifactId() + "/pom.xml",
                             artifactDir + "/" + ac.getArtifactId() + "-" + ac.getVersion() + ".pom");
                     FileUtils.copyFile(jar, new File(artifactDir, ac.getArtifactId() + "-" + ac.getVersion() + ".jar"));
                     logger.info("Installed jar and pom file: " + artifactDir + File.separator + ac.getArtifactId() + "-" + ac.getVersion() + ".jar");
                 } catch (IOException io) {
                     errorCount++;
                     logger.severe("Problem installing jar: " + ac.getArtifactId() + " to: " + artifactDir + "\n" + io.getMessage());
                 }
             }
         }
     }
 
 
     public ArtifactCoordinates getArtifactCoordinatesFromJar(File jar) {
         File tempDir = new File(System.getProperty("java.io.tmpdir"));
         String fileName = tempDir.getAbsolutePath() + File.separator+ "pom-" + System.currentTimeMillis() + ".properties";
         extractPomPropFile(jar, fileName);
         Properties properties = new Properties();
         ArtifactCoordinates ac = null;
         try {
             properties.load(new FileInputStream(fileName));
             ac = new ArtifactCoordinates(properties.getProperty("groupId"), properties.getProperty("artifactId"), "jar", properties.getProperty("version"), null);
             new File(fileName).deleteOnExit();
         } catch (FileNotFoundException e) {
             logger.fine("Could not find pom.properties in file: " + jar.toString() + "\n" + e.getMessage());
         } catch (IOException e) {
             logger.fine("Could not find pom.properties in file: " + jar.toString() + "\n" + e.getMessage());
         }
         return ac;
     }
 
 
     public void installPoms() {
 
         String pomDir = SorcerEnv.getHomeDir() + "/configs/poms/";
 
         File[] jars = new File(pomDir).listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 if (pathname.getName().endsWith("pom"))
                     return true;
                 return false;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
         String group = "org.sorcersoft.sorcer";
         for (File jar : jars) {
                 ArtifactCoordinates ac = getArtifactCoordsFromPom(jar.getAbsolutePath());
                 if (ac!=null) {
                     String artifactDir = Resolver.getRepoDir() + "/" + ac.getGroupId().replace(".", "/") + "/" + ac.getArtifactId() + "/" + ac.getVersion();
                     try {
                         FileUtils.forceMkdir(new File(artifactDir));
                         FileUtils.copyFile(jar, new File(artifactDir, jar.getName()));
                         logger.info("Installed pom file: " + artifactDir + File.separator + jar.getName());
                     } catch (IOException io) {
                         errorCount++;
                         logger.severe("Problem installing pom file: " + jar.getAbsolutePath() + " to: " + artifactDir);
                     }
                 } else
                     errorCount++;
         }
     }
 
     public static void main(String[] args) {
         Installer installer = new Installer();
         installer.install();
         installer.installPoms();
         installer.createMarker();
     }
 
     public ArtifactCoordinates getArtifactCoordsFromPom(String fileName) {
         DocumentBuilderFactory domFactory =
                 DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true);
         DocumentBuilder builder = null;
         String groupId = null;
         String artifactId = null;
         String version = null;
         String packaging = null;
         try {
             builder = domFactory.newDocumentBuilder();
             Document doc = builder.parse(fileName);
             XPath xpath = XPathFactory.newInstance().newXPath();
             Map<String, String> namespaces = new HashMap<String, String>();
             namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
             xpath.setNamespaceContext(
                     new NamespaceContextImpl("http://maven.apache.org/POM/4.0.0",
                             namespaces));
             XPathExpression expr = xpath.compile("/pom:project/pom:groupId");
             Object result = expr.evaluate(doc, XPathConstants.NODESET);
             NodeList nodes = (NodeList) result;
             if (nodes.getLength()>0)
                 groupId = nodes.item(0).getTextContent();
             else {
                 expr = xpath.compile("/pom:project/pom:parent/pom:groupId");
                 result = expr.evaluate(doc, XPathConstants.NODESET);
                 nodes = (NodeList) result;
                 if (nodes.getLength()>0)
                     groupId = nodes.item(0).getTextContent();
             }
 
             expr = xpath.compile("/pom:project/pom:artifactId");
             result = expr.evaluate(doc, XPathConstants.NODESET);
             nodes = (NodeList) result;
             artifactId = nodes.item(0).getTextContent();
 
             expr = xpath.compile("/pom:project/pom:version");
             result = expr.evaluate(doc, XPathConstants.NODESET);
             nodes = (NodeList) result;
             if (nodes.getLength()>0)
                 version = nodes.item(0).getTextContent();
             else {
                 expr = xpath.compile("/pom:project/pom:parent/pom:version");
                 result = expr.evaluate(doc, XPathConstants.NODESET);
                 nodes = (NodeList) result;
                 if (nodes.getLength()>0)
                     version = nodes.item(0).getTextContent();
             }
             expr = xpath.compile("/pom:project/pom:packaging");
             result = expr.evaluate(doc, XPathConstants.NODESET);
             nodes = (NodeList) result;
             if (nodes.getLength()>0)
                 packaging = nodes.item(0).getTextContent();
 
         } catch (ParserConfigurationException e) {
             logger.severe("Problem installing file: " + fileName + "\n" + e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         } catch (XPathExpressionException e) {
             logger.severe("Problem installing file: " + fileName + "\n" + e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         } catch (SAXException e) {
             logger.severe("Problem installing file: " + fileName + "\n" + e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         } catch (IOException e) {
             logger.severe("Problem installing file: " + fileName + "\n" + e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         } catch (Exception e) {
             logger.severe("Problem installing file: " + fileName + "\n" + e.getMessage());
             e.printStackTrace();
             return null;
         }
 
         return new ArtifactCoordinates(groupId, artifactId, packaging, version, null);
     }
 
     private void createMarker() {
         if (errorCount==0) {
             String markerFile = SorcerEnv.getHomeDir() + "/logs/" + MARKER_FILENAME;
             File f = new File(markerFile);
             try {
                 f.createNewFile();
             } catch (IOException e) {
             }
         }
         logger.info("Installer finished with " + errorCount + " errors");
     }
 
 
     protected void close(Closeable inputStream) {
         if (inputStream != null) {
             try {
                 inputStream.close();
             } catch (IOException e) {
                 // ignore
             }
         }
     }
 
     public static void extractZipFile(File zipFileSrc, String relativeFilePath, String targetFilePath) {
         try {
             ZipFile zipFile = new ZipFile(zipFileSrc);
             Enumeration<? extends ZipEntry> e = zipFile.entries();
 
             while (e.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) e.nextElement();
                 // if the entry is not directory and matches relative file then extract it
                 if (!entry.isDirectory() && entry.getName().equals(relativeFilePath)) {
                     InputStream bis = new BufferedInputStream(
                             zipFile.getInputStream(entry));
 
                     // write the inputStream to a FileOutputStream
                     OutputStream outputStream =
                             new FileOutputStream(new File(targetFilePath));
 
                     int read = 0;
                     byte[] bytes = new byte[1024];
 
                     while ((read = bis.read(bytes)) != -1) {
                         outputStream.write(bytes, 0, read);
                     }
                     bis.close();
                     outputStream.close();
                 } else {
                     continue;
                 }
             }
         } catch (IOException e) {
             logger.severe("IOError :" + e);
             e.printStackTrace();
         }
     }
 
     public static void extractPomPropFile(File zipFileSrc, String targetFilePath) {
         try {
             ZipFile zipFile = new ZipFile(zipFileSrc);
             Enumeration<? extends ZipEntry> e = zipFile.entries();
 
             while (e.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) e.nextElement();
                 // if the entry is not directory and matches relative file then extract it
                 if (!entry.isDirectory() && entry.getName().contains("META-INF/maven/") && entry.getName().contains("pom.properties")) {
                     InputStream bis = new BufferedInputStream(
                             zipFile.getInputStream(entry));
 
                     // write the inputStream to a FileOutputStream
                     OutputStream outputStream =
                             new FileOutputStream(new File(targetFilePath));
 
                     int read = 0;
                     byte[] bytes = new byte[1024];
 
                     while ((read = bis.read(bytes)) != -1) {
                         outputStream.write(bytes, 0, read);
                     }
                     bis.close();
                     outputStream.close();
                 } else {
                     continue;
                 }
             }
         } catch (IOException e) {
             logger.severe("IOError :" + e);
             e.printStackTrace();
         }
     }
 
 }
