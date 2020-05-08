 package bot.framework;
 
 import bot.framework.components.container.PicoContainerJobFactory;
 import bot.framework.components.container.StartupListenerStrategy;
 import bot.framework.components.groovy.PluginAutoListener;
 import bot.framework.components.groovy.PluginMaintainer;
 import bot.framework.components.groovy.PluginScheduler;
 import bot.framework.components.jira.Jira;
 import bot.framework.components.skype.Skype;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
 import org.apache.log4j.Logger;
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.MutablePicoContainer;
 import org.picocontainer.behaviors.Caching;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.impl.StdSchedulerFactory;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jaroslaw Koscinski
  * Date: 08.08.13
  * Time: 17:29
  */
 public class Oberfuhrer {
 
     static Logger logger = Logger.getLogger(Oberfuhrer.class);
 
     public static void main(String[] args) throws SchedulerException, ConfigurationException {
 
         final MutablePicoContainer container = new DefaultPicoContainer(new Caching(), new StartupListenerStrategy(), null);
         final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
         scheduler.setJobFactory(new PicoContainerJobFactory(container));
         container.addComponent(Jira.class);
         container.addComponent(Skype.class);
         container.addComponent(container);
         container.addComponent(scheduler);
         container.addComponent(PluginMaintainer.class);
         container.addComponent(PluginScheduler.class);
         container.addComponent(PluginAutoListener.class);
 
         PropertiesConfiguration configuration = new PropertiesConfiguration("bot.properties");
         configuration.setAutoSave(true);
         configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
         container.addComponent(configuration);
 
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 try {
                     scheduler.shutdown();
                 } catch (SchedulerException e) {
                     // we are shutting down, nothing terrible happened
                 }
                 container.stop();
                 container.dispose();
             }
         });
 
 
         container.start();
         scheduler.start();
 
         logger.info("Bot started");
 
     }
 
 
 
 }
