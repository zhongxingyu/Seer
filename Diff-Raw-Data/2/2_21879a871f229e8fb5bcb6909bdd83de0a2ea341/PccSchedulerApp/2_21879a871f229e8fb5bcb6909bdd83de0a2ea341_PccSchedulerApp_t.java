 package co.altruix.scheduler;
 
 import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.jms.Session;
 
 import org.apache.commons.io.IOUtils;
 import org.quartz.JobBuilder;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.TriggerBuilder;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.altruix.commons.api.di.PccException;
 
 import at.silverstrike.pcc.api.persistence.Persistence;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializer;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializerFactory;
 import co.altruix.pcc.api.outgoingqueuechannel.OutgoingQueueChannel;
 import co.altruix.pcc.api.outgoingqueuechannel.OutgoingQueueChannelFactory;
 import co.altruix.scheduler.api.jobdatamapcreator.JobDataMapCreator;
 import co.altruix.scheduler.api.jobdatamapcreator.JobDataMapCreatorFactory;
 import co.altruix.scheduler.impl.di.DefaultPccSchedulerInjectorFactory;
 import co.altruix.scheduler.impl.scheduledrecalculation.DefaultScheduledRecalculationJob;
 
 import com.google.inject.Injector;
 
 public final class PccSchedulerApp {
     private static final String PCC_RECALCULATION = "pcc-recalculation-job";
     private static final String CONFIG_FILE = "conf.properties";
     private static final Logger LOGGER = LoggerFactory
             .getLogger(PccSchedulerApp.class);
 
     public void run() {
         try {
             final Properties config = readConfig();
 
             final Injector injector = initDependencyInjector();
 
             final Persistence persistence =
                     injector.getInstance(Persistence.class);
             persistence.openSession(Persistence.HOST_LOCAL, null, null,
                     Persistence.DB_PRODUCTION);
 
             final String brokerUrl = config.getProperty("brokerUrl");
             final String username = config.getProperty("username");
             final String password = config.getProperty("password");
 
             final MqInfrastructureInitializer mqInitializer =
                     initMq(injector, brokerUrl, username, password);
 
             final Session session = mqInitializer.getSession();
             
             final OutgoingQueueChannelFactory factory = injector.getInstance(OutgoingQueueChannelFactory.class);
             final OutgoingQueueChannel channel = factory.create();
             
             channel.setChannelName("scheduler2workerQueueName");
            channel.setQueueName(config.getProperty("scheduler2workerQueueName"));
             channel.setSession(session);
             channel.init();
             
             final Scheduler scheduler =
                     StdSchedulerFactory.getDefaultScheduler();
             scheduler.start();
             
             
             
             final JobDetail job =
                     JobBuilder.newJob(DefaultScheduledRecalculationJob.class)
                             .withIdentity(PCC_RECALCULATION).build();
 
             final JobDataMap jobDataMap =
                     getJobDataMap(injector, session, channel);            
             
             final Trigger trigger =
                     TriggerBuilder
                             .newTrigger()
                             .withIdentity("pcc-recalculation-trigger")
                             .usingJobData(jobDataMap)
                             .startNow()
                             .withSchedule(
                                     simpleSchedule()
                                             .withIntervalInMinutes(5)
                                             .repeatForever()).build();
             scheduler.scheduleJob(job, trigger);
             
         } catch (final SchedulerException exception) {
             LOGGER.error("", exception);
         } catch (final PccException exception) {
             LOGGER.error("", exception);
         }
     }
 
     private JobDataMap getJobDataMap(final Injector injector,
             final Session session, final OutgoingQueueChannel channel)
             throws PccException {
         final JobDataMapCreatorFactory jobDataMapCreatorFactory = injector.getInstance(JobDataMapCreatorFactory.class);
         final JobDataMapCreator jobDataMapCreator = jobDataMapCreatorFactory.create();
         
         jobDataMapCreator.setChannel(channel);
         jobDataMapCreator.setInjector(injector);
         jobDataMapCreator.setSession(session);
         jobDataMapCreator.run();
         
         final JobDataMap jobDataMap = jobDataMapCreator.getJobDataMap();
         return jobDataMap;
     }
 
     private MqInfrastructureInitializer initMq(final Injector aInjector,
             final String aBrokerUrl, final String aUsername,
             final String aPassword)
             throws PccException {
         final MqInfrastructureInitializerFactory factory =
                 aInjector.getInstance(MqInfrastructureInitializerFactory.class);
         final MqInfrastructureInitializer mqInitializer = factory.create();
 
         mqInitializer.setUsername(aUsername);
         mqInitializer.setPassword(aPassword);
         mqInitializer.setBrokerUrl(aBrokerUrl);
         mqInitializer.run();
         return mqInitializer;
     }
 
     private Properties readConfig() {
         final Properties config = new Properties();
 
         FileInputStream fileInputStream = null;
         try {
             fileInputStream = new FileInputStream(new File(CONFIG_FILE));
             config.load(fileInputStream);
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         } finally {
             IOUtils.closeQuietly(fileInputStream);
         }
         return config;
     }
 
     private Injector initDependencyInjector() {
         final DefaultPccSchedulerInjectorFactory injectorFactory =
                 new DefaultPccSchedulerInjectorFactory();
         final Injector injector = injectorFactory.createInjector();
         return injector;
     }
 
     public static void main(String[] args) {
         final PccSchedulerApp app = new PccSchedulerApp();
         app.run();
     }
 }
