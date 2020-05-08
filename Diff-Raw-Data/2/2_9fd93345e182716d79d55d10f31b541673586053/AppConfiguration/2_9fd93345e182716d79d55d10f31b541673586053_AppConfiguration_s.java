 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod;
 
 import java.beans.PropertyVetoException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.service.EmailSender;
 
 import org.jasypt.spring31.properties.EncryptablePropertyPlaceholderConfigurer;
 import org.jasypt.util.text.StrongTextEncryptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.*;
 import org.springframework.context.support.MessageSourceSupport;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.scheduling.annotation.EnableAsync;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.scheduling.annotation.SchedulingConfigurer;
 import org.springframework.scheduling.config.ScheduledTaskRegistrar;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.ObjectArrays;
 import com.googlecode.flyway.core.Flyway;
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 @Configuration
 @ComponentScan(basePackages = "nl.surfnet.bod")
 @ImportResource({
   "classpath:spring/appCtx-security.xml",
   "classpath:spring/appCtx-ws.xml" })
 @EnableTransactionManagement
 @EnableAspectJAutoProxy(proxyTargetClass = true)
 @EnableJpaRepositories(basePackages = "nl.surfnet.bod")
 @EnableScheduling
 @EnableAsync
 public class AppConfiguration implements SchedulingConfigurer {
 
   private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
 
   @Value("${jdbc.jdbcUrl}") private String jdbcUrl;
   @Value("${jdbc.driverClass}") private String driverClass;
   @Value("${jdbc.user}") private String jdbcUser;
   @Value("${jdbc.password}") private String jdbcPassword;
   @Value("${jdbc.initialPoolSize}") private int initialPoolSize;
   @Value("${jdbc.maxPoolSize}") private int maxPoolSize;
   @Value("${jdbc.minPoolSize}") private int minPoolSize;
   @Value("${jdbc.acquireIncrement}") private int acquireIncrement;
   @Value("${jdbc.acquireRetryAttempts}") private int acquireRetryAttempts;
   @Value("${jdbc.idleConnectionTestPeriod}") private int idleConnectionTestPeriod;
 
   @Bean
   public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
     StrongTextEncryptor encryptor = new StrongTextEncryptor();
     encryptor.setPassword(System.getenv("BOD_ENCRYPTION_PASSWORD"));
 
     EncryptablePropertyPlaceholderConfigurer configurer = new EncryptablePropertyPlaceholderConfigurer(encryptor);
     configurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
 
     Resource[] resources = addEnvPropertyResource(new Resource[] {
       new ClassPathResource("bod-default.properties")
     });
 
     logger.info("Using property files: {}", Joiner.on(",").join(resources));
 
     configurer.setLocations(resources);
 
     return configurer;
   }
 
   private static Resource[] addEnvPropertyResource(Resource[] resources) {
     String env = System.getProperties().getProperty("bod.env");
 
     if (env == null || env.isEmpty()) {
       Resource devProperties = new ClassPathResource(getPropertyEnvName("dev"));
 
       return devProperties.exists() ? ObjectArrays.concat(resources, devProperties) : resources;
     }
     else {
       return ObjectArrays.concat(resources, new ClassPathResource(getPropertyEnvName(env)));
     }
   }
 
   private static String getPropertyEnvName(String env) {
     return String.format("env-properties/bod-%s.properties", env);
   }
 
   @Bean
   public MessageSourceSupport messageSource() {
     ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
     messageSource.setBasename("WEB-INF/i18n/messages,WEB-INF/i18n/application");
     messageSource.setFallbackToSystemLocale(false);
     messageSource.setUseCodeAsDefaultMessage(true);
     return messageSource;
   }
 
   @Bean
   public JavaMailSender mailSender(
       @Value("${mail.host}") String host, @Value("${mail.port}") int port,
       @Value("${mail.protocol}") String protocol, @Value("${mail.debug}") boolean debug) {
     JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
     mailSender.setDefaultEncoding("UTF-8");
     mailSender.setHost(host);
     mailSender.setPort(port);
     mailSender.setProtocol(protocol);
 
     Properties properties = new Properties();
     properties.put("mail.debug", debug);
     mailSender.setJavaMailProperties(properties);
 
     return mailSender;
   }
 
   @Bean
   public EmailSender emailSender(@Value("${mail.sender.class}") String emailSenderClass)
       throws InstantiationException, IllegalAccessException, ClassNotFoundException {
     return (EmailSender) Class.forName(emailSenderClass).newInstance();
   }
 
   @Bean
   public NbiClient nbiClient(@Value("${nbi.client.class}") String nbiClientClass)
       throws InstantiationException, IllegalAccessException, ClassNotFoundException {
     return (NbiClient) Class.forName(nbiClientClass).newInstance();
   }
 
   @Bean
   public IddClient iddclient(
     @Value("${idd.client.class}") String iddClientClass,
     @Value("${idd.user}") String username,
     @Value("${idd.password}") String password,
     @Value("${idd.url}") String endPoint) {
 
     try {
       return (IddClient) Class.forName(iddClientClass)
           .getConstructor(String.class, String.class, String.class)
           .newInstance(username, password, endPoint);
     }
     catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   @Bean(initMethod = "migrate")
   public Flyway flyway() throws PropertyVetoException {
     Flyway flyway = new Flyway();
     flyway.setDataSource(dataSource());
     flyway.setLocations("nl.surfnet.bod.db.migration");
     return flyway;
   }
 
   @Bean(destroyMethod = "close")
   public DataSource dataSource() throws PropertyVetoException {
     ComboPooledDataSource dataSource = new ComboPooledDataSource();
     dataSource.setJdbcUrl(jdbcUrl);
     dataSource.setDriverClass(driverClass);
     dataSource.setUser(jdbcUser);
     dataSource.setPassword(jdbcPassword);
     dataSource.setInitialPoolSize(initialPoolSize);
     dataSource.setMaxPoolSize(maxPoolSize);
     dataSource.setMinPoolSize(minPoolSize);
     dataSource.setAcquireIncrement(acquireIncrement);
     dataSource.setAcquireRetryAttempts(acquireRetryAttempts);
     dataSource.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
 
     return dataSource;
   }
 
   @Bean
   @DependsOn("flyway")
   public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws PropertyVetoException {
     LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
     emfBean.setPersistenceUnitName("bod");
     emfBean.setDataSource(dataSource());
 
     return emfBean;
   }
 
   @Bean
   public JpaTransactionManager transactionManager() {
     return new JpaTransactionManager();
   }
 
   @Bean
   public List<String> nsaProviderUrns() {
     return Lists.newArrayList("urn:ogf:network:nsa:surfnet.nl");
   }
 
   @Bean
   public Map<String, Map<String, String>> reportToVersMap() {
     return ImmutableMap.<String, Map<String, String>>builder()
       .put("amountRunningReservationsSucceeded",
           ImmutableMap.of("TRUE", "Succeeded", "FALSE", "Failed"))
       .put("amountRunningReservationsFailed",
           ImmutableMap.of("TRUE", "Failed", "FALSE", "N.A."))
       .put("amountRunningReservationsStillRunning",
           ImmutableMap.of("TRUE", "Running", "FALSE", "N.A."))
       .put("amountRunningReservationsStillScheduled",
           ImmutableMap.of("TRUE", "Still Scheduled", "FALSE", "N.A."))
       .put("amountRunningReservationsNeverProvisioned",
           ImmutableMap.of("TRUE", "Timed Out", "FALSE", "N.A."))
       .put("amountRequestsModifiedSucceeded",
           ImmutableMap.of("TRUE", "Succeeded", "FALSE", "Failed"))
       .put("amountRequestsCancelSucceeded",
           ImmutableMap.of("TRUE", "Succeeded", "FALSE", "Failed"))
       .put("amountReservationsProtected",
           ImmutableMap.of("TRUE", "Protected Scheduled", "FALSE", "Unprotected"))
       .put("amountReservationsRedundant",
           ImmutableMap.of("TRUE", "Succeeded", "FALSE", "Failed"))
       .put("amountRequestsThroughGUI",
          ImmutableMap.of("TRUE", "NSI", "FALSE", "GUI"))
       .build();
   }
 
   @Override
   public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
   }
 
 }
