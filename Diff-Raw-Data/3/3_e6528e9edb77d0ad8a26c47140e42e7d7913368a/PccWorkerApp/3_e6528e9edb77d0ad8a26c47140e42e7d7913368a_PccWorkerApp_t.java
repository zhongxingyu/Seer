 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.pcc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.jms.Connection;
 import javax.jms.Session;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.silverstrike.pcc.api.persistence.Persistence;
 
 import com.google.inject.Injector;
 
 import ru.altruix.commons.api.di.PccException;
 import co.altruix.pcc.api.dispatcher.Dispatcher;
 import co.altruix.pcc.api.dispatcher.DispatcherFactory;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializer;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializerFactory;
 import co.altruix.pcc.api.queuechannel.QueueChannel;
 import co.altruix.pcc.api.queuechannel.QueueChannelFactory;
 import co.altruix.pcc.api.shutdownhook.ShutdownHook;
 import co.altruix.pcc.api.shutdownhook.ShutdownHookFactory;
 import co.altruix.pcc.impl.di.DefaultPccWorkerInjectorFactory;
 
 public final class PccWorkerApp {
     private static final int HALF_SECOND = 500;
     private static final String CONFIG_FILE = "conf.properties";
     private static final Logger LOGGER = LoggerFactory
             .getLogger(PccWorkerApp.class);
 
     private PccWorkerApp() {
 
     }
 
     private void run() throws PccException {
         final Properties config = readConfig();
 
         LOGGER.info("tj3Path: {}", config.getProperty("tj3Path"));
 
         final Injector injector = initDependencyInjector(config);
 
         final Persistence persistence = injector.getInstance(Persistence.class);
         persistence.openSession();
 
         final String brokerUrl = config.getProperty("brokerUrl");
         final String username = config.getProperty("username");
         final String password = config.getProperty("password");
 
         final MqInfrastructureInitializer mqInitializer =
                 initMq(injector, brokerUrl, username, password);
 
         final Session session = mqInitializer.getSession();
 
         setupShutdownHook(injector, session,
                 mqInitializer.getConnection());
 
         final QueueChannelFactory channelFactory =
                 injector.getInstance(QueueChannelFactory.class);
         final QueueChannel web2workerQueue = channelFactory.create();
 
         final String web2workerQueueName =
                 config.getProperty("web2workerQueueName");
         web2workerQueue.setQueueName(web2workerQueueName);
         web2workerQueue.setSession(session);
         web2workerQueue.init();
 
         final Dispatcher dispatcher = getDispatcher(injector, config);
 
         dispatcher.addChannel(web2workerQueue);
 
         while (true) {
             try {
                 dispatcher.run();
             } catch (final PccException exception) {
                 LOGGER.error("", exception);
             }
 
             try {
                 Thread.sleep(HALF_SECOND);
             } catch (final InterruptedException exception) {
                 LOGGER.error("", exception);
             }
         }
     }
 
     private Injector initDependencyInjector(final Properties aConfiguration) {
         final DefaultPccWorkerInjectorFactory injectorFactory =
                 new DefaultPccWorkerInjectorFactory();
         injectorFactory.setConfiguration(aConfiguration);
         final Injector injector = injectorFactory.createInjector();
         return injector;
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
 
     private Dispatcher getDispatcher(final Injector aInjector,
             final Properties aConfiguration) {
         final DispatcherFactory factory =
                 aInjector.getInstance(DispatcherFactory.class);
         final Dispatcher dispatcher = factory.create();
         dispatcher.setInjector(aInjector);
         return dispatcher;
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
 
     private void setupShutdownHook(final Injector aInjector,
             final Session aSession, final Connection aConnection) {
         final ShutdownHookFactory factory =
                 aInjector.getInstance(ShutdownHookFactory.class);
         final ShutdownHook hook = factory.create();
 
         hook.setSession(aSession);
         hook.setConnection(aConnection);
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                 try {
                     hook.run();
                 } catch (final PccException exception) {
                     LOGGER.error("", exception);
                 }
             }
         });
     }
 
     public static void main(final String[] aArgs) throws PccException {
         final PccWorkerApp app = new PccWorkerApp();
 
         app.run();
     }
 }
