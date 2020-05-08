 package org.pillarone.riskanalytics.application;
 
 import grails.util.GrailsUtil;
 import groovy.lang.ExpandoMetaClass;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.groovy.grails.commons.BootstrapArtefactHandler;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
 import org.codehaus.groovy.grails.commons.GrailsClass;
 import org.pillarone.riskanalytics.application.initialization.DatabaseManagingSessionStateListener;
 import org.pillarone.riskanalytics.application.initialization.IExternalDatabaseSupport;
 import org.pillarone.riskanalytics.application.initialization.StandaloneConfigLoader;
 import org.pillarone.riskanalytics.application.ui.P1RATStandaloneLauncher;
 import org.springframework.context.ApplicationContext;
 
 /**
  * Starting point of the standalone application.
  * Initializes log4j and starts grails, then executes all BootStraps and starts the UI.
  */
 public class Main {
 
     private static Log LOG = LogFactory.getLog(Main.class);
 
     public static void main(String args[]) {
         launchApplication(null);
     }
 
     public static void launchApplication(IExternalDatabaseSupport databaseSupport) {
         try {
             String environment = System.getProperty("grails.env");
             if (environment == null) {
                 environment = "development";
                 System.setProperty("grails.env", environment);
 
             }
             StandaloneConfigLoader.loadLog4JConfig(environment);
             LOG.info("Starting RiskAnalytics with environment " + environment);
 
             if(databaseSupport != null) {
                 LOG.info("Starting external database for environment " + environment);
                 databaseSupport.startDatabase();
             }
 
 
             LOG.info("Loading grails..");
             ExpandoMetaClass.enableGlobally();
             ApplicationContext ctx = GrailsUtil.bootstrapGrailsFromClassPath();
             GrailsApplication app = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID);
 
             LOG.info("Executing bootstraps..");
             GrailsClass[] bootstraps = app.getArtefacts(BootstrapArtefactHandler.TYPE);
             for (GrailsClass bootstrap : bootstraps) {
                 final GrailsBootstrapClass bootstrapClass = (GrailsBootstrapClass) bootstrap;
                 //Quartz bootstrap needs a servlet context
                 if (!bootstrapClass.getClazz().getSimpleName().startsWith("Quartz")) {
                     bootstrapClass.callInit(null);
                 }
             }
 
             LOG.info("Loading user interface..");
             if (databaseSupport == null) {
                 P1RATStandaloneLauncher.start();
             } else {
                 P1RATStandaloneLauncher.start(new DatabaseManagingSessionStateListener(databaseSupport));
             }
         } catch (Exception e) {
             LOG.fatal("Startup failed", e);
            if (databaseSupport != null) {
                databaseSupport.stopDatabase();
            }
         }
     }
 }
