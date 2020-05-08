 package com.mlesniak.homepage.config;
 
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
 import org.apache.commons.lang3.StringUtils;
 import org.quartz.*;
 import org.quartz.ee.servlet.QuartzInitializerListener;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Inject;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import static org.quartz.CronScheduleBuilder.cronSchedule;
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 /**
  * Configuration bean.
  * <p/>
  * Later configuration should be loaded from the database if possible and the configuration file is only used as the
  * initialization config.
  *
  * @author Michael Lesniak (mail@mlesniak.com)
  */
 public class Config implements ServletContextListener {
     public static final String USE_DATABASE = "useDatabase";
     public static final String RELOAD_TIME = "reloadConfiguration";
     private static Config singleton;
     // This is the only place where log is not initialized directly.
     private static Logger log = null;
     @Inject
     ConfigDao configDao;
     private String configFilename;
     private Properties properties;
     private Scheduler scheduler;
     private Thread reloadThread;
 
     public static Config getConfig() {
         if (singleton == null) {
             throw new IllegalStateException("Config not yet initialized.");
         }
 
         return singleton;
     }
 
     /**
      * To allow for an easier configuration, values for a key a.b.c.d are searched in this order:
      * <pre>
      *     d
      *     c.d
      *     b.c.d
      *     a.b.c.d
      * </pre>
      * This allows for a clearly arranged configuration file, although the user is responsible for preventing
      * duplicates.
      *
      * @param key The key to look for.
      *
      * @return The property.
      */
     public String get(String key) {
         load();
 
         String[] packageParts = key.split("\\.");
         StringBuffer sb = new StringBuffer();
         for (int i = packageParts.length - 1; i >= 0; --i) {
             sb.insert(0, packageParts[i]);
 
             String result = retrieveValue(sb.toString());
             if (result != null) {
                 return result;
             }
             sb.insert(0, '.');
         }
 
         return null;
     }
 
     private String retrieveValue(String key) {
         // Don't check for database values if configuration should be reloaded from file anyway/
         if (!properties.containsKey(USE_DATABASE)) {
             String value = configDao.get(key);
             if (value != null) {
                 return value;
             }
         }
 
         return properties.getProperty(key);
     }
 
     public List<String> getList(String key) {
         List<String> list = new ArrayList<String>();
 
         String value = get(key);
         if (value != null) {
             for (String elem : value.split(",")) {
                 list.add(elem.trim());
             }
         }
 
         return list;
     }
 
     public Scheduler getScheduler() {
         return scheduler;
     }
 
     public boolean getBoolean(String key) {
         String s = get(key);
         if (s == null) {
             return false;
         }
 
         return Boolean.parseBoolean(s);
     }
 
     public boolean isDefined(String key) {
         return get(key) != null;
     }
 
     public int getInt(String key) {
         return Integer.parseInt(get(key));
     }
 
     private void load() {
         if (properties != null && !properties.containsKey(USE_DATABASE)) {
             return;
         }
 
         properties = new Properties();
         try {
             properties.load(new FileInputStream(new File(configFilename)));
         } catch (IOException e) {
             System.out.println("Unable to load config file. name=" + configFilename);
         }
     }
 
     @Override
     public void contextInitialized(ServletContextEvent servletContextEvent) {
         try {
             if (singleton != null) {
                 return;
             }
 
             configFilename = servletContextEvent.getServletContext().getInitParameter("config-filename");
             load();
             singleton = this;
 
             handleLogging();
             handleDatabase();
             handleReloading();
 
             StdSchedulerFactory factory = (StdSchedulerFactory) servletContextEvent.getServletContext().getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
             try {
                 scheduler = factory.getScheduler();
                 startInitialThreads();
             } catch (SchedulerException e) {
                 System.out.println("Unable to get quartz scheduler.");
                 e.printStackTrace();
             }
         } catch (Exception e) {
             log.error("Unable to load configuration.", e);
         }
     }
 
     private void handleReloading() {
         if (!isDefined(RELOAD_TIME)) {
             log.info("reloadConfiguration not defined. No processing.");
             return;
         }
 
         log.debug("Initializing reloading");
         reloadThread = new Thread() {
             @Override
             public void run() {
 
                 while (!isInterrupted() && get(RELOAD_TIME) != null) {
                     log.info("Reloading configuration.");
                     load();
 
                     try {
                         Thread.sleep(1000 * getInt(RELOAD_TIME));
                     } catch (InterruptedException e) {
                         return;
                     } catch (NumberFormatException e) {
                         log.warn("No correct format for reloadConfiguration: " + get(RELOAD_TIME));
                     }
                 }
 
                 log.info("Stopping reloading.");
             }
         };
         reloadThread.start();
     }
 
    private void handleLogging() throws JoranException {
         String logback = "logback.configurationFile";
         String file = get(logback);
         if (file != null && !(new File(file).exists())) {
             System.out.println("logback configuration file does not exist: " + file);
             return;
         }
 
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(file);
         log = LoggerFactory.getLogger(Config.class);
         log.info("Logback logging initialized.");
     }
 
     private void handleDatabase() {
         String overwriteDatabase = "overwriteDatabase";
         if (!getBoolean(USE_DATABASE)) {
             log.info("Ignoring configuration file.");
             configDao.delete(overwriteDatabase);
             return;
         }
 
         log.info("Filling database from file.");
         for (Object key : properties.keySet()) {
             if (StringUtils.equals((CharSequence) key, overwriteDatabase)) {
                 continue;
             }
             String value = (String) properties.get(key);
             log.debug("Setting key=" + key + " value=" + value);
             configDao.save((String) key, value);
         }
 
     }
 
     @SuppressWarnings("unchecked")
     private void startInitialThreads() {
         List<String> jobs = getList("jobs");
         if (jobs == null) {
             log.warn("No jobs= definition found.");
             return;
         }
 
         for (String job : jobs) {
             if (StringUtils.isEmpty(job)) {
                 continue;
             }
 
             String cronExpression = get(job + ".cron");
             if (cronExpression == null) {
                 log.warn("No cron= definition for job=" + job);
                 continue;
             }
 
             try {
                 Class<? extends Job> clazz = (Class<? extends Job>) this.getClass().getClassLoader().loadClass(job);
                 JobDetail jobDetail = newJob(clazz)
                                         .withIdentity(job)
                                         .build();
                 Trigger trigger = newTrigger()
                                     .withIdentity(job + ".trigger")
                                     .withSchedule(cronSchedule(cronExpression))
                                     .build();
                 log.info("Scheduling job=" + job + " with cron=" + cronExpression);
                 getScheduler().scheduleJob(jobDetail, trigger);
             } catch (Exception e) {
                 log.warn("Unable to create job. class=" + job);
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent servletContextEvent) {
         try {
             getScheduler().shutdown();
         } catch (SchedulerException e) {
             log.error("Unable to shut down scheduler.");
             e.printStackTrace();
         }
 
         reloadThread.interrupt();
     }
 }
