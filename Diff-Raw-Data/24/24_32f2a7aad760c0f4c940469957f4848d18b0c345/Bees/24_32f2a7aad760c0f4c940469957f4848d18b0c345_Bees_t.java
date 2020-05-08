 package com.cloudbees.sdk;
 
 import com.cloudbees.api.BeesClientException;
 import com.cloudbees.sdk.cli.CommandScope;
 import com.cloudbees.sdk.cli.CommandService;
 import com.cloudbees.sdk.cli.ICommand;
 import com.cloudbees.sdk.extensibility.AnnotationLiteral;
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
     /**
      * Version of the bees CLI.
      */
     public static VersionNumber version = loadVersion();
 
     private final static String app_template_xml_url = "http://cloudbees-downloads.s3.amazonaws.com/";
     private final static String app_template_xml_name = "sdk/cloudbees-sdk-config-2.xml";
     private final static String app_template_xml_desc = "CloudBees SDK configuration";
     private static final long CHECK_INTERVAL = 1000 * 60 * 60 * 12;  // 12 hours
    private static final String SDK_PLUGIN_INSTALL = "sdk:plugin:install";
 
     @Inject
     private CommandService commandService;
 
     @Inject
     private PluginsToInstallList pluginsToInstallList;
 
     /**
      * Entry point to all the components.
      */
     private final Injector injector;
 
     private final ClassLoader extLoader;
 
     private long time(String msg, long start) {
         long end = System.currentTimeMillis();
         System.out.println(msg + " : " + (end-start) + " ms");
         return end;
     }
 
     public Bees() throws Exception {
         long start = System.currentTimeMillis();
 
         extLoader = getClass().getClassLoader();
         start = time("S1", start);
         //  container that includes all the things that make a bees CLI.
         Injector injector = Guice.createInjector(
                 new AbstractModule() {
                     @Override
                     protected void configure() {
                         bind(CommandService.class).to(CommandServiceImpl.class);
                         bind(ClassLoader.class).annotatedWith(AnnotationLiteral.of(ExtensionClassLoader.class)).toInstance(extLoader);
                         bindScope(CommandScope.class,new CommandScopeImpl());
                     }
                 }
         );
         start = time("S2", start);
 
         this.injector = injector;
         this.injector.injectMembers(this);
         start = time("S3", start);
         initialize(false);
         start = time("S4", start);
     }
 
     public int run(String[] args) throws Exception {
         // Load command definitions
         long start = System.currentTimeMillis();
         start = time("R1", start);
         commandService.loadCommandProperties();
         if (commandService.getCount() == 0) {
             throw new RuntimeException("Cannot find bees commands");
         }
         start = time("R2", start);
 
         if (args.length==0) args = new String[]{"help"};
 
         Object context = CommandScopeImpl.begin();
         try {
             // Install plugins
             installPlugins();
             start = time("R3", start);
 
             ICommand command = commandService.getCommand(args[0]);
             if (command==null) {
                 // no such command. print help
                 System.err.println("No such command: "+args[0]);
                 command = commandService.getCommand("help");
                 if (command==null)
                     throw new Error("Panic: command "+args[0]+" was not found, and even the help command was not found");
             }
             start = time("R4", start);
 
             int r = command.run(Arrays.asList(args));
             if (r == 99) {
                 initialize(true);
             }
             start = time("R5", start);
             return r;
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
         File lastCheckFile = new File(beesRepoPath, "sdk/check.dat");
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
             File sdkConfig = getURLAsFile(localRepository,app_template_xml_url + app_template_xml_name,
                     app_template_xml_name, app_template_xml_desc);
             Document doc = XmlHelper.readXMLFromFile(sdkConfig.getCanonicalPath());
             Element e = doc.getDocumentElement();
             String availVersion = e.getAttribute("version");
             String minVersion = e.getAttribute("min-version");
 
             VersionNumber currentVersion = version;
             VersionNumber availableVersion = new VersionNumber(availVersion);
             VersionNumber minimunVersion = new VersionNumber(minVersion);
 
             if (currentVersion.compareTo(availableVersion) < 0) {
                 System.out.println();
                 if (currentVersion.compareTo(minimunVersion) < 0) {
                     throw new AbortException("Error - This version of the CloudBees SDK is no longer supported," + "" +
                             " please install the latest version (" + availVersion + ").");
                 } else if (currentVersion.compareTo(availableVersion) < 0) {
                     System.out.println("A new version of the CloudBees SDK is available, please install the latest version (" + availVersion + ").");
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
                 System.out.println("  SDK home:     " + homeRef);
                 System.out.println("  SDK download: " + hRef);
                 System.out.println();
             }
 
             // Update last check
             p.setProperty("last", ""+System.currentTimeMillis());
             lastCheckFile.getParentFile().mkdirs();
             FileOutputStream fos = new FileOutputStream(lastCheckFile);
             p.store(fos, "CloudBees SDK check");
             fos.close();
         }
     }
 
     private void installPlugins() throws Exception {
         Set<Map.Entry<String, GAV>> set = pluginsToInstallList.entrySet();
         if (set.size() > 0) {
            ICommand installPluginCmd = commandService.getCommand(SDK_PLUGIN_INSTALL);
             Iterator<Map.Entry<String, GAV>> it = set.iterator();
             while (it.hasNext()) {
                 Map.Entry<String, GAV> entry = it.next();
                 System.out.println("Installing plugin: " + entry.getValue());
                installPluginCmd.run(Arrays.asList(SDK_PLUGIN_INSTALL, entry.getValue().toString()));
                 pluginsToInstallList.remove(entry.getKey());
             }
         }
     }
 
     private File getURLAsFile(LocalRepository localRepository,String urlStr, String localCachePath, String description) throws IOException {
         try {
             return localRepository.getURLAsFile(urlStr,localCachePath,description);
         } catch (Exception e) {
             throw (IOException)new IOException("Failed to retrieve "+urlStr).initCause(e);
         }
     }
 
     public static void main(String[] args) {
         System.out.println("# CloudBees SDK version: " + version);
         try {
             new Bees().run(args);
         } catch (BeesClientException e) {
             System.err.println();
             String errCode = e.getError().getErrorCode();
             if (errCode != null && errCode.equals("AuthFailure"))
                 System.err.println("ERROR: Authentication failure, please check credentials!");
             else
                 System.err.println("ERROR: " +  e.getMessage());
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
      *
      * To support running this from IDE and elsewhere, work gracefully if the version
      * is not available or not filtered.
      */
     private static VersionNumber loadVersion() {
         Properties props = new Properties();
         InputStream in = Bees.class.getResourceAsStream("version.properties");
         if (in!=null) {
             try {
                 props.load(in);
             } catch (IOException e) {
                 throw new Error(e);
             } finally {
                 IOUtils.closeQuietly(in);
             }
         }
         Object v = props.get("version");
         if (v!=null)
             try {
                 return new VersionNumber(v.toString());
             } catch (Exception e) {
                 // fall through
             }
 
         return new VersionNumber("0");
     }
 
     private static boolean isVerbose(String[] args) {
         for (String arg: args) {
             if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--verbose"))
                 return true;
         }
         return false;
     }
 }
