 package dk.statsbiblioteket.broadcasttranscoder.ws.config;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.BroadcastTranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
 import org.apache.commons.pool.BasePoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.io.File;
 import java.io.IOException;
 
 /**
  *
  */
 public class ConfigurationLoader implements ServletContextListener {
 
     public static Logger logger;
 
     private static GenericObjectPool thePool;
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         ServletContext context = sce.getServletContext();
         String logbackFilepath = context.getInitParameter("bta.logback.xml");
         LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
         JoranConfigurator configurator = new JoranConfigurator();
         configurator.setContext(lc);
         lc.reset();
         try {
             configurator.doConfigure(new File(logbackFilepath));
         } catch (JoranException e) {
             System.out.println("Could not initialise logging. " + e.getMessage());
             System.out.println("File path to logging configuration was " + logbackFilepath);
             throw new RuntimeException(e);
         }
         logger = LoggerFactory.getLogger(ConfigurationLoader.class);
         String behaviourFilepath = context.getInitParameter("bta.behaviour.properties");
         String infrastructureFilepath = context.getInitParameter("bta.infrastructure.properties");
         logger.info("Loading behaviour properties from " + behaviourFilepath);
         logger.info("Loading infrastructure properties from " + infrastructureFilepath);
         File behaviourFile = new File(behaviourFilepath);
         if (!behaviourFile.exists()) {
             final String message = "No such file: " + behaviourFile.getAbsolutePath();
             logger.error(message);
             throw new RuntimeException(message);
         }
         File infrastructureFile = new File(infrastructureFilepath);
         if (!infrastructureFile.exists()) {
             final String message = "No such file: " + infrastructureFilepath;
             logger.error(message);
             throw new RuntimeException(message);
         }
         SingleTranscodingContext<BroadcastTranscodingRecord> transcodingContext = new SingleTranscodingContext<BroadcastTranscodingRecord>();
         transcodingContext.setBehaviourConfigFile(behaviourFile);
         transcodingContext.setInfrastructuralConfigFile(infrastructureFile);
         try {
             new WebserviceTranscodingOptionsParser(transcodingContext);
         } catch (Exception e) {
             throw new RuntimeException("Error parsing properties", e);
         }
         sce.getServletContext().setAttribute("transcodingContext", transcodingContext);
         FileUtils.cleanupAllTempDirs(transcodingContext);
        int maxActiveProcesses = Integer.parseInt(sce.getServletContext().getInitParameter("bta.maxActive"));
         thePool = new GenericObjectPool(new BasePoolableObjectFactory() {
             @Override
             public Object makeObject() throws Exception {
                 return new Object();    }
         }, maxActiveProcesses);
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
         SingleTranscodingContext<BroadcastTranscodingRecord> transcodingContext = (SingleTranscodingContext<BroadcastTranscodingRecord>) sce.getServletContext().getAttribute("transcodingContext");
         FileUtils.cleanupAllTempDirs(transcodingContext);
     }
 
     public static GenericObjectPool getThePool() {
         return thePool;
     }
 
 }
