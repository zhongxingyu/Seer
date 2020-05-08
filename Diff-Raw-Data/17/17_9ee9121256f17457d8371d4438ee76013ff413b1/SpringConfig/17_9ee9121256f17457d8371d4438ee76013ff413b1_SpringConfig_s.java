 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.quartz.Scheduler;
 import org.quartz.simpl.RAMJobStore;
 import org.quartz.simpl.SimpleThreadPool;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.i18n.FixedLocaleResolver;
 import org.springframework.web.servlet.view.JstlView;
 import org.springframework.web.servlet.view.UrlBasedViewResolver;
 
 import com.github.dbourdette.otto.quartz.OttoJobFactory;
 import com.github.dbourdette.otto.service.user.User;
 import com.github.dbourdette.otto.source.MailReports;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.ServerAddress;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.config.CacheConfiguration;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 @Configuration
 @EnableWebMvc
 public class SpringConfig {
 
     public static final String DEFAULT_DB_URL = "localhost";
 
     public static final String DEFAULT_DB_NAME = "otto";
 
     public static final String DEFAULT_SECURITY_USERNAME = "letme";
 
     public static final String DEFAULT_SECURITY_PASSWORD = "in";
 
     public static final String APPLICATION_CONTEXT_KEY = "applicationContext";
 
     /**
      * Static reference to spring context used by jsp functions.
      */
     public static ApplicationContext staticContext;
 
     @Inject
     private ServletContext servletContext;
 
     @Inject
     private ApplicationContext context;
 
     @Inject
     private MailReports mailReports;
 
     @PostConstruct
    public void init() throws UnknownHostException {
         SpringConfig.staticContext = context;
 
         Registry.mongoDb = mongoDb();
         Registry.mailReports = mailReports;
     }
 
     @Bean
     public FixedLocaleResolver fixedLocaleResolver() {
         FixedLocaleResolver resolver = new FixedLocaleResolver();
         resolver.setDefaultLocale(Locale.FRANCE);
         return resolver;
     }
 
     @Bean
     public UrlBasedViewResolver urlBasedViewResolver() {
         UrlBasedViewResolver resolver = new UrlBasedViewResolver();
         resolver.setViewClass(JstlView.class);
         resolver.setPrefix("/WEB-INF/jsp/");
         resolver.setSuffix(".jsp");
         return resolver;
     }
 
     @Bean
     public ReloadableResourceBundleMessageSource messageSource() {
         ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
         messageSource.setBasename("classpath:messages");
         messageSource.setDefaultEncoding("UTF-8");
         return messageSource;
     }
 
     @Bean
     public Scheduler quartzScheduler(ApplicationContext context) throws Exception {
         SchedulerFactoryBean factory = new SchedulerFactoryBean();
 
         factory.setApplicationContext(context);
         factory.setExposeSchedulerInRepository(true);
         factory.setApplicationContextSchedulerContextKey(APPLICATION_CONTEXT_KEY);
         factory.setJobFactory(new OttoJobFactory());
 
         Properties properties = new Properties();
         properties.setProperty("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
         properties.setProperty("org.quartz.threadPool.threadCount", "5");
         properties.setProperty("org.quartz.threadPool.threadPriority", "4");
 
         properties.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());
 
         factory.setQuartzProperties(properties);
 
         factory.afterPropertiesSet();
 
         Scheduler scheduler = factory.getObject();
 
         scheduler.start();
 
         return scheduler;
     }
 
     @Bean
     public Mongo mongo() throws MongoException, UnknownHostException {
         Mongo mongo = new Mongo(getMongoServerAdresses());
 
         mongo.slaveOk();
 
         return mongo;
     }
 
     @Bean
     public DB mongoDb() throws MongoException, UnknownHostException {
         String username = getMongoUsername();
 
         DB db = mongo().getDB(getMongoDbName());
 
         if (StringUtils.isNotEmpty(username)) {
             db.authenticate(username, getMongoPassword().toCharArray());
         }
 
         return db;
     }
 
     @Bean
     public Datastore dataStore() throws MongoException, UnknownHostException {
         return morphia().createDatastore(mongo(), getMongoDbName());
     }
 
     @Bean
     public Morphia morphia() throws MongoException, UnknownHostException {
         Morphia morphia = new Morphia();
 
         morphia.map(User.class);
 
         return morphia;
     }
 
     public List<ServerAddress> getMongoServerAdresses() throws UnknownHostException {
         List<ServerAddress> addresses = new ArrayList<ServerAddress>();
 
         for (String url : StringUtils.split(getMongoUrl(), ",")) {
             addresses.add(new ServerAddress(url));
         }
 
         return addresses;
     }
 
     public String getMongoUrl() {
         return getInitParameter("mongo/url", DEFAULT_DB_URL);
     }
 
     public String getMongoDbName() {
         return getInitParameter("mongo/dbName", DEFAULT_DB_NAME);
     }
 
     public String getMongoUsername() {
         return getInitParameter("mongo/username", "");
     }
 
     public String getMongoPassword() {
         return getInitParameter("mongo/password", "");
     }
 
     public String getSecurityDefaultUsername() {
         return getInitParameter("security/default.username", DEFAULT_SECURITY_USERNAME);
     }
 
     public String getSecurityDefaultPassword() {
         return getInitParameter("security/default.password", DEFAULT_SECURITY_PASSWORD);
     }
 
     public String getSecurityLdapProviderUrl() {
         return getInitParameter("security/ldap.providerUrl", "");
     }
 
     public String getSecurityLdapUserDn() {
         return getInitParameter("security/ldap.userDn", "");
     }
 
     public String getSecurityLdapPassword() {
         return getInitParameter("security/ldap.password", "");
     }
 
     public String getSecurityLdapSearchBase() {
         return getInitParameter("security/ldap.searchBase", "");
     }
 
     public String getSecurityLdapSearchFilter() {
         return getInitParameter("security/ldap.searchFilter", "");
     }
 
     public String getSecurityRadiusHost() {
         return getInitParameter("security/radius.host", "");
     }
 
     public String getSecurityRadiusPort() {
         return getInitParameter("security/radius.port", "1812");
     }
 
     public String getSecurityRadiusSecret() {
         return getInitParameter("security/radius.secret", "");
     }
 
     public String getInitParameter(String name, String defaultValue) {
         String value = servletContext.getInitParameter(name);
 
         if (StringUtils.isEmpty(value)) {
             return defaultValue;
         } else {
             return value;
         }
     }
 
 }
