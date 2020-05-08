 /*
  * Copyright 2010-2013, CloudBees Inc.
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
 
 package com.cloudbees.sdk;
 
 import com.cloudbees.api.BeesClientException;
 import com.cloudbees.sdk.cli.ACommand;
 import com.cloudbees.sdk.cli.CommandScope;
 import com.cloudbees.sdk.cli.CommandService;
 import com.cloudbees.sdk.cli.DirectoryStructure;
 import com.cloudbees.sdk.extensibility.AnnotationLiteral;
import com.cloudbees.sdk.maven.MavenRepositorySystemSessionDecorator;
 import com.cloudbees.sdk.maven.RemoteRepositoryDecorator;
 import com.cloudbees.sdk.maven.RepositorySystemModule;
 import com.cloudbees.sdk.utils.Helper;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.staxnet.appserver.utils.XmlHelper;
 import com.staxnet.repository.LocalRepository;
 import hudson.util.VersionNumber;
 import org.apache.commons.cli.UnrecognizedOptionException;
 import org.apache.commons.io.IOUtils;
 import org.w3c.dom.*;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * @author Fabian Donze
  * @author Kohsuke Kawaguchi
  */
 public class Bees {
     public static String BOOTSTRAP_VERSION = "1.3.2";
     /**
      * Version of the bees CLI.
      */
     public static VersionNumber version = loadVersion();
 
     private final static String app_template_xml_url = "http://cloudbees-downloads.s3.amazonaws.com/";
     private final static String app_template_xml_name = "sdk/cloudbees-sdk-config-5.xml";
     private final static String app_template_xml_desc = "CloudBees SDK configuration";
     private static final long CHECK_INTERVAL = 1000 * 60 * 60 * 12;  // 12 hours
     public final static String CHECK_FILE = "sdk/check-5.dat";
     public static final String SDK_PLUGIN_INSTALL = "plugin:install";
 
     @Inject
     private CommandService commandService;
 
     @Inject
     private PluginsToInstallList pluginsToInstallList;
 
     /**
      * Entry point to all the components.
      */
     private final Injector injector;
 
     private final ClassLoader extLoader;
 
     public Bees() throws Exception {
         extLoader = getClass().getClassLoader();
         //  container that includes all the things that make a bees CLI.
         Injector injector = Guice.createInjector(
                 new AbstractModule() {
                     @Override
                     protected void configure() {
                         bind(CommandService.class).to(CommandServiceImpl.class);
                         bind(ClassLoader.class).annotatedWith(AnnotationLiteral.of(ExtensionClassLoader.class)).toInstance(extLoader);
                         bindScope(CommandScope.class, new CommandScopeImpl());
                         bind(RemoteRepositoryDecorator.class).to(RemoteRepositoryDecoratorImpl.class);
                        bind(MavenRepositorySystemSessionDecorator.class).to(CliMavenRepositorySystemSessionDecorator.class);
                     }
                 },
                 new RepositorySystemModule()
         );
 
         this.injector = injector;
         this.injector.injectMembers(this);
         CommandServiceImpl service = (CommandServiceImpl) commandService;
         service.loadCommandProperties();
         if (service.getCount() == 0) {
             throw new RuntimeException("Cannot find bees commands");
         }
     }
 
     public int run(String[] args) throws Exception {
         reportTime("Bees.run");
 
         // Load command definitions
         if (args.length == 0) args = new String[]{"help"};
 
         Object context = CommandScopeImpl.begin();
         try {
             // If it is the init command, execute before anything else
             if (args[0].equalsIgnoreCase("init")) {
                 // Setup the configuration file
                 ACommand setupCommand = commandService.getCommand(args[0]);
                 if (setupCommand == null)
                     throw new Error("Panic: init error");
                 setupCommand.run(Arrays.asList(args));
 
                 // Initialize the SDK
                 initialize(false);
 
                 // Install plugins
                 installPlugins(args);
 
                 return 0;
             } else {
                 // Initialize the SDK
                 initialize(false);
 
                 // Install plugins
                 installPlugins(args);
 
                 ACommand command = commandService.getCommand(args[0]);
                 if (command == null) {
                     // no such command. print help
                     System.err.println("No such command: " + args[0]);
                     command = commandService.getCommand("help");
                     if (command == null)
                         throw new Error("Panic: command " + args[0] + " was not found, and even the help command was not found");
                 }
 
                 int r = command.run(Arrays.asList(args));
                 if (r == 99) {
                     initialize(true);
                 }
                 return r;
             }
 
         } finally {
             CommandScopeImpl.end(context);
         }
     }
 
     private String getHome() {
         return System.getProperty("bees.home");
     }
 
     private void initialize(boolean force) throws Exception {
         LocalRepository localRepository = new LocalRepository();
 
         String beesRepoPath = localRepository.getRepositoryPath();
         File lastCheckFile = new File(beesRepoPath, CHECK_FILE);
         boolean checkVersion = true;
         Properties p = new Properties();
         if (!force && Helper.loadProperties(lastCheckFile, p)) {
             String str = p.getProperty("last");
             if (str != null) {
                 long interval = System.currentTimeMillis() - Long.parseLong(str);
                 if (interval < CHECK_INTERVAL)
                     checkVersion = false;
             }
         }
 
         if (checkVersion) {
             // Check SDK version
             File sdkConfig = getURLAsFile(localRepository, app_template_xml_url + app_template_xml_name,
                     app_template_xml_name, app_template_xml_desc);
             Document doc = XmlHelper.readXMLFromFile(sdkConfig.getCanonicalPath());
             Element e = doc.getDocumentElement();
             String availVersion = e.getAttribute("version");
             String minVersion = e.getAttribute("min-version");
 
             VersionNumber currentVersion = new VersionNumber(BOOTSTRAP_VERSION);
             VersionNumber availableVersion = new VersionNumber(availVersion);
             VersionNumber minimumVersion = new VersionNumber(minVersion);
 
             if (currentVersion.compareTo(availableVersion) < 0) {
                 System.out.println();
                 if (currentVersion.compareTo(minimumVersion) < 0) {
                     throw new AbortException("ERROR - This version of the CloudBees SDK is no longer supported," + "" +
                             " please install the latest version (" + availVersion + ").");
                 } else if (currentVersion.compareTo(availableVersion) < 0) {
                     System.out.println("WARNING - A new version of the CloudBees SDK is available, please install the latest version (" + availVersion + ").");
                 }
 
                 String hRef = e.getAttribute("href");
 
                 String homeRef = "www.cloudbees.com";
                 NodeList nodeList = e.getElementsByTagName("link");
                 for (int i = 0; i < nodeList.getLength(); i++) {
                     Node node = nodeList.item(i);
                     NamedNodeMap nodeMap = node.getAttributes();
                     Node rel = nodeMap.getNamedItem("rel");
                     Node href = nodeMap.getNamedItem("href");
                     if (rel != null && rel.getTextContent().trim().equalsIgnoreCase("alternate") && href != null) {
                         homeRef = href.getTextContent();
                     }
                 }
 
                 NodeList libsNL = e.getElementsByTagName("libraries");
                 Node libsNode = null;
                 if (libsNL.getLength() > 0) {
                     libsNode = libsNL.item(0);
                 }
                 if (libsNode != null) {
                     NodeList libNL = e.getElementsByTagName("library");
                     for (int i = 0; i < libNL.getLength(); i++) {
                         Node node = libNL.item(i);
                         NamedNodeMap nodeMap = node.getAttributes();
                         Node nameNode = nodeMap.getNamedItem("name");
                         Node refNode = nodeMap.getNamedItem("href");
                         if (nameNode != null && refNode != null) {
                             String libName = nameNode.getTextContent();
                             String libUrlString = refNode.getTextContent().trim();
                             int idx = libUrlString.lastIndexOf('/');
                             String libFileName = libUrlString.substring(idx);
                             localRepository.getURLAsFile(libUrlString, "lib1" + libFileName, libName);
                         }
                     }
                 }
 
                 System.out.println("  SDK home:     " + homeRef);
                 System.out.println("  SDK download: " + hRef);
                 System.out.println();
             }
 
             // Check plugins version
             NodeList pluginsNL = e.getElementsByTagName("plugins");
             Node pluginsNode = null;
             if (pluginsNL.getLength() > 0) {
                 pluginsNode = pluginsNL.item(0);
             }
             if (pluginsNode != null) {
                 NodeList pluginNL = e.getElementsByTagName("plugin");
                 CommandServiceImpl service = (CommandServiceImpl) commandService;
                 for (int i = 0; i < pluginNL.getLength(); i++) {
                     Node node = pluginNL.item(i);
                     NamedNodeMap nodeMap = node.getAttributes();
                     Node nameNode = nodeMap.getNamedItem("artifact");
                     if (nameNode != null) {
                         Node n = nodeMap.getNamedItem("required");
                         boolean forceInstall = (n != null && Boolean.parseBoolean(n.getTextContent()));
 
                         String pluginArtifact = nameNode.getTextContent();
                         GAV gav = new GAV(pluginArtifact);
                         VersionNumber pluginVersion = new VersionNumber(gav.version);
                         Plugin plugin = service.getPlugin(gav.artifactId);
                         if (plugin != null) {
                             forceInstall = false;
                             GAV pgav = new GAV(plugin.getArtifact());
                             VersionNumber currentPluginVersion = new VersionNumber(pgav.version);
                             if (currentPluginVersion.compareTo(pluginVersion) < 0) {
                                 Node nf = nodeMap.getNamedItem("force-upgrade");
                                 boolean forced = (nf != null && Boolean.parseBoolean(nf.getTextContent()));
                                 if (forced) {
                                     forceInstall = true;
                                 } else {
                                     System.out.println();
                                     System.out.println("WARNING - A newer version of the [" + gav.artifactId + "] plugin is available, please update with:");
                                     System.out.println(" > bees plugin:update " + gav.artifactId);
                                     System.out.println();
                                 }
                             }
                         }
                         if (forceInstall)
                             pluginsToInstallList.put(gav.artifactId, gav);
 
                     }
                 }
             }
 
             // Update last check
             p.setProperty("last", "" + System.currentTimeMillis());
             lastCheckFile.getParentFile().mkdirs();
             FileOutputStream fos = new FileOutputStream(lastCheckFile);
             p.store(fos, "CloudBees SDK check");
             fos.close();
         }
     }
 
     private void installPlugins(String[] args) throws Exception {
         Set<Map.Entry<String, GAV>> set = pluginsToInstallList.entrySet();
         if (set.size() > 0) {
             ACommand installPluginCmd = commandService.getCommand(SDK_PLUGIN_INSTALL);
             Iterator<Map.Entry<String, GAV>> it = set.iterator();
             while (it.hasNext()) {
                 Map.Entry<String, GAV> entry = it.next();
                 System.out.println("Installing plugin: " + entry.getValue());
                 List<String> piArgs;
                 if (isVerbose(args))
                     piArgs = Arrays.asList(SDK_PLUGIN_INSTALL, entry.getValue().toString(), "-f", "-v");
                 else
                     piArgs = Arrays.asList(SDK_PLUGIN_INSTALL, entry.getValue().toString(), "-f");
                 installPluginCmd.run(piArgs);
                 pluginsToInstallList.remove(entry.getKey());
             }
             // Reload the plugins commands
             CommandServiceImpl service = (CommandServiceImpl) commandService;
             service.loadCommandProperties();
         }
     }
 
     private File getURLAsFile(LocalRepository localRepository, String urlStr, String localCachePath, String description) throws IOException {
         try {
             DirectoryStructure ds = new DirectoryStructure();
             Properties properties = new Properties();
             if (Helper.loadProperties(new File(ds.localRepository, "bees.config"), properties)) {
                 Helper.setJVMProxySettings(properties);
             }
             return localRepository.getURLAsFile(urlStr, localCachePath, description);
         } catch (Exception e) {
             throw (IOException) new IOException("Failed to retrieve " + urlStr).initCause(e);
         }
     }
 
     public static void main(String[] args) {
         reportTime("Bees");
 
         boolean verbose = isVerbose(args);
         if (verbose || isHelp(args) || isPluginCmd(args)) {
             System.out.println("# CloudBees SDK version: " + BOOTSTRAP_VERSION);
             System.out.println("# CloudBees Driver version: " + version);
             if (verbose) System.out.println(System.getProperties());
         }
         try {
             new Bees().run(args);
         } catch (BeesClientException e) {
             System.err.println();
             String errCode = e.getError().getErrorCode();
             if (errCode != null && errCode.equals("AuthFailure")) {
                 if (e.getError().getMessage() != null)
                     System.err.println("ERROR: " + e.getError().getMessage());
                 else
                     System.err.println("ERROR: Authentication failure, please check credentials!");
             } else
                 System.err.println("ERROR: " + e.getMessage());
 //            e.printStackTrace();
             System.exit(2);
         } catch (UnrecognizedOptionException e) {
             System.err.println();
             System.err.println("ERROR: " + e.getMessage());
             System.exit(2);
         } catch (IllegalArgumentException e) {
             System.err.println();
             System.err.println("ERROR: " + e.getMessage());
             System.exit(2);
         } catch (BeesSecurityException e) {
             System.err.println();
             System.err.println("ERROR: " + e.getMessage());
             System.exit(2);
         } catch (Throwable e) {
             System.err.println();
             System.err.println("ERROR: " + e.getMessage());
             if (isVerbose(args))
                 e.printStackTrace();
             System.exit(2);
         }
     }
 
     /**
      * Parses the version number of SDK from the resource file that Maven produces.
      * <p/>
      * To support running this from IDE and elsewhere, work gracefully if the version
      * is not available or not filtered.
      */
     private static VersionNumber loadVersion() {
         Properties props = new Properties();
         InputStream in = Bees.class.getResourceAsStream("version.properties");
         if (in != null) {
             try {
                 props.load(in);
             } catch (IOException e) {
                 throw new Error(e);
             } finally {
                 IOUtils.closeQuietly(in);
             }
         }
         Object v = props.get("version");
         if (v != null)
             try {
                 return new VersionNumber(v.toString());
             } catch (Exception e) {
                 // fall through
             }
 
         return new VersionNumber("0");
     }
 
     private static boolean isVerbose(String[] args) {
         for (String arg : args) {
             if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--verbose"))
                 return true;
         }
         return false;
     }
 
     private static boolean isHelp(String[] args) {
         if (args.length == 0) return true;
         for (String arg : args) {
             if (arg.equalsIgnoreCase("help"))
                 return true;
         }
         return false;
     }
 
     private static boolean isPluginCmd(String[] args) {
         if (args.length == 0) return false;
         for (String arg : args) {
             if (arg.startsWith("plugin:"))
                 return true;
         }
         return false;
     }
 
     private static void reportTime(String caption) {
         String profile = System.getProperty("profile");
         if (profile !=null) {
             System.out.println(caption+": "+(System.nanoTime()-Long.valueOf(profile))/1000000L+"ms");
         }
     }
 }
