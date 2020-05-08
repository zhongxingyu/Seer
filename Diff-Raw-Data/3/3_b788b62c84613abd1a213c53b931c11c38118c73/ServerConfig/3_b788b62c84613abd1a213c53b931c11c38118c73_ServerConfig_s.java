 /*
  * Copyright (c) 2013 SixRQ Ltd.
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package org.freewheelschedule.freewheel.config;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.freewheelschedule.freewheel.common.dao.ExecutionDao;
 import org.freewheelschedule.freewheel.common.dao.JobDao;
 import org.freewheelschedule.freewheel.common.dao.MachineDao;
 import org.freewheelschedule.freewheel.common.dao.TriggerDao;
 import org.freewheelschedule.freewheel.common.model.Trigger;
 import org.freewheelschedule.freewheel.common.model.TriggerType;
 import org.freewheelschedule.freewheel.common.network.FreewheelClientSocket;
 import org.freewheelschedule.freewheel.common.network.FreewheelSocket;
 import org.freewheelschedule.freewheel.common.network.IServerSocketProxy;
 import org.freewheelschedule.freewheel.common.network.ServerSocketProxy;
 import org.freewheelschedule.freewheel.common.util.ApplicationContextProvider;
 import org.freewheelschedule.freewheel.common.util.QueueWrapper;
 import org.freewheelschedule.freewheel.controlserver.AcknowledgementListenerThread;
 import org.freewheelschedule.freewheel.controlserver.ControlServer;
 import org.freewheelschedule.freewheel.controlserver.ControlThread;
import org.freewheelschedule.freewheel.launcher.ScheduleLauncher;
 import org.freewheelschedule.freewheel.rest.RestServices;
 import org.freewheelschedule.freewheel.rest.WebServiceRunner;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.PriorityBlockingQueue;
 
 import static org.freewheelschedule.freewheel.common.model.TriggerType.REPEATING;
 import static org.freewheelschedule.freewheel.common.model.TriggerType.TIMED;
 
 @Configuration
 @ImportResource("classpath:applicationContext-ControlServer.xml")
 public class ServerConfig {
 
     private
     @Value("${hibernate.driverclass}")
     String driverClass = null;
     private
     @Value("${hibernate.url}")
     String hibernateUrl = null;
     private
     @Value("${hibernate.user}")
     String hibernateUser = null;
     private
     @Value("${hibernate.dialect}")
     String hibernateDialect = null;
     private
     @Value("${hibernate.showsql}")
     String hibernateShowsql = null;
     private
     @Value("${hibernate.password}")
     String hibernatePassword = null;
 
     private
     @Value("${workerPort}")
     int workerPort = 0;
     private
     @Value("${socketTimeout}")
     int socketTimeout = 0;
 
     private Class[] annotatedClasses = {org.freewheelschedule.freewheel.common.model.Job.class,
             org.freewheelschedule.freewheel.common.model.CommandJob.class,
             org.freewheelschedule.freewheel.common.model.Trigger.class,
             org.freewheelschedule.freewheel.common.model.TimedTrigger.class,
             org.freewheelschedule.freewheel.common.model.RepeatingTrigger.class,
             org.freewheelschedule.freewheel.common.model.Machine.class,
             org.freewheelschedule.freewheel.common.model.Execution.class
     };
 
     @Bean
     public BasicDataSource freewheelDataSource() {
         BasicDataSource dataSource = new BasicDataSource();
         dataSource.setDriverClassName(driverClass);
         dataSource.setUrl(hibernateUrl);
         dataSource.setUsername(hibernateUser);
         dataSource.setPassword(hibernatePassword);
         return dataSource;
     }
 
     @Bean
     public AnnotationSessionFactoryBean freewheelSessionFactory() {
         AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
         bean.setDataSource(freewheelDataSource());
         bean.setAnnotatedClasses(annotatedClasses);
         Properties hibernateProperties = new Properties();
         hibernateProperties.put("hibernate.dialect", hibernateDialect);
         hibernateProperties.put("hibernate.showsql", hibernateShowsql);
         bean.setHibernateProperties(hibernateProperties);
         return bean;
     }
 
     @Bean
     public JobDao jobDao() {
         JobDao dao = new JobDao();
         dao.setSessionFactory((SessionFactory)freewheelSessionFactory().getObject());
         return dao;
     }
 
     @Bean
     public MachineDao machineDao() {
         MachineDao dao = new MachineDao();
         dao.setSessionFactory((SessionFactory)freewheelSessionFactory().getObject());
         return dao;
     }
 
     @Bean
     public TriggerDao triggerDao() {
         TriggerDao dao = new TriggerDao();
         dao.setSessionFactory((SessionFactory)freewheelSessionFactory().getObject());
         return dao;
     }
 
     @Bean
     public ExecutionDao executionDao() {
         ExecutionDao dao = new ExecutionDao();
         dao.setSessionFactory((SessionFactory)freewheelSessionFactory().getObject());
         return dao;
     }
 
     @Bean
     public IServerSocketProxy listenerSocket() throws IOException {
         return new ServerSocketProxy((new Integer(workerPort)).intValue(), (new Integer(socketTimeout)).intValue());
     }
 
     @Bean
     public FreewheelSocket serverConnection() {
         return new FreewheelClientSocket();
     }
 
     @Bean
     public AcknowledgementListenerThread serverListener() {
         return new AcknowledgementListenerThread();
     }
 
     @Bean
     public ControlThread serverController() {
         return new ControlThread();
     }
 
     @Bean
     public QueueWrapper blockingQueueWrapper() {
         QueueWrapper queueWrapper = new QueueWrapper();
         Map<TriggerType, PriorityBlockingQueue<Trigger>> queueMap = new HashMap<TriggerType, PriorityBlockingQueue<Trigger>>();
         queueMap.put(REPEATING, repeatingQueue());
         queueMap.put(TIMED, timedQueue());
         queueWrapper.setQueueMap(queueMap);
         return queueWrapper;
     }
 
     @Bean
     public PriorityBlockingQueue<Trigger> repeatingQueue() {
         return new PriorityBlockingQueue<Trigger>();
     }
 
     @Bean
     public PriorityBlockingQueue<Trigger> timedQueue() {
         return new PriorityBlockingQueue<Trigger>();
     }
 
     @Bean
     public ControlServer controlServer() {
         ControlServer server = new ControlServer();
         server.setController(serverController());
         server.setListener(serverListener());
         return server;
     }
 
     @Bean
     public WebServiceRunner webService() {
         return new WebServiceRunner();
     }
 
     @Bean
     public RestServices restServices() {
         return new RestServices();
     }
 
     @Bean
     public ScheduleLauncher scheduleLauncher() {
         return new ScheduleLauncher();
     }
 
     @Bean
     public ApplicationContextProvider applicationContextProvider() {
         return new ApplicationContextProvider();
     }
 }
