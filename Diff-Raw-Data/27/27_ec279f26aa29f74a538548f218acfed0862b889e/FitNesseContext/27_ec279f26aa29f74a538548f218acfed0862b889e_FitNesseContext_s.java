 // Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
 // Released under the terms of the CPL Common Public License version 1.0.
 package fitnesse;
 
 import fitnesse.authentication.Authenticator;
 import fitnesse.authentication.PromiscuousAuthenticator;
 import fitnesse.components.Logger;
 import fitnesse.testrunner.MultipleTestSystemFactory;
 import fitnesse.testsystems.TestSystemFactory;
 import fitnesse.testsystems.slim.CustomComparatorRegistry;
 import fitnesse.testsystems.slim.tables.SlimTableFactory;
 import fitnesse.wiki.RecentChanges;
 import fitnesse.html.template.PageFactory;
 import fitnesse.responders.ResponderFactory;
 import fitnesse.testrunner.RunningTestingTracker;
 import fitnesse.wiki.SystemVariableSource;
 import fitnesse.wiki.WikiPage;
 import fitnesse.wiki.fs.VersionsController;
 
 import java.io.File;
 import java.util.Properties;
 
 public class FitNesseContext {
   public final static String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
   public final static String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
   public static final String testResultsDirectoryName = "testResults";
 
   /**
    * Use the builder to create your FitNesse contexts.
    */
   public static final class Builder {
     public WikiPage root;
 
     public int port = -1;
     public String rootPath;
     public String rootDirectoryName;
 
     public Logger logger;
     public Authenticator authenticator = new PromiscuousAuthenticator();
     public VersionsController versionsController;
     public RecentChanges recentChanges;
     public TestSystemFactory testSystemFactory = new MultipleTestSystemFactory(new SlimTableFactory(), new CustomComparatorRegistry());
     public Properties properties = new Properties();
 
     public Builder() {
       super();
     }
 
     public Builder(FitNesseContext context) {
       super();
       if (context != null) {
         root = context.root;
         port = context.port;
         rootPath = context.rootPath;
         rootDirectoryName = context.rootDirectoryName;
         logger = context.logger;
         authenticator = context.authenticator;
         versionsController = context.versionsController;
         recentChanges = context.recentChanges;
         testSystemFactory = context.testSystemFactory;
         properties = context.properties;
       }
     }
 
     public final FitNesseContext createFitNesseContext() {
       FitNesseVersion version = new FitNesseVersion();
       // Those variables are defined so they can be looked up for as wiki variables.
       if (rootPath != null) {
         properties.setProperty("FITNESSE_ROOTPATH", rootPath);
       }
       properties.setProperty("FITNESSE_PORT", Integer.toString(port));
       properties.setProperty("FITNESSE_VERSION", version.toString());
       return new FitNesseContext(version,
           root,
           rootPath,
           rootDirectoryName,
           versionsController,
           recentChanges,
           port,
           authenticator,
           logger,
           testSystemFactory,
           properties);
     }
   }
 
   public final FitNesseVersion version;
   public final FitNesse fitNesse;
   public final WikiPage root;
 
   public final TestSystemFactory testSystemFactory;
   public final RunningTestingTracker runningTestingTracker;
 
   public final int port;
   private final String rootPath;
   private final String rootDirectoryName;
   public final ResponderFactory responderFactory;
   public final PageFactory pageFactory;
 
   public final VersionsController versionsController;
   public final RecentChanges recentChanges;
   public final Logger logger;
   public final Authenticator authenticator;
   private final Properties properties;
 
 
 
   private FitNesseContext(FitNesseVersion version, WikiPage root, String rootPath,
       String rootDirectoryName, VersionsController versionsController,
       RecentChanges recentChanges, int port,
       Authenticator authenticator, Logger logger,
       TestSystemFactory testSystemFactory, Properties properties) {
     super();
     this.version = version;
     this.root = root;
     this.rootPath = rootPath;
     this.rootDirectoryName = rootDirectoryName;
     this.versionsController = versionsController;
     this.recentChanges = recentChanges;
     this.port = port;
     this.authenticator = authenticator;
     this.logger = logger;
     this.testSystemFactory = testSystemFactory;
     this.properties = properties;
     runningTestingTracker = new RunningTestingTracker();
     responderFactory = new ResponderFactory(getRootPagePath());
     fitNesse = new FitNesse(this);
     pageFactory = new PageFactory(this);
   }
 
   public File getTestHistoryDirectory() {
     return new File(String.format("%s/files/%s", getRootPagePath(), testResultsDirectoryName));
   }
 
   public String getTestProgressPath() {
     return String.format("%s/files/testProgress/", getRootPagePath());
   }
 
   public String getRootPagePath() {
    return String.format("%s/%s", rootPath, rootDirectoryName);
   }
 
   public Properties getProperties() {
     return properties;
   }
 
   public String getProperty(String name) {
     return new SystemVariableSource(properties).getProperty(name);
   }
 }
