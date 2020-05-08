 package org.knetwork.webapp.configuration;
 
 import java.io.FileNotFoundException;
 import java.util.Properties;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.PropertiesFactoryBean;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.util.Log4jConfigurer;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.springframework.web.servlet.view.JstlView;
 
 @Configuration
 public class ServletConfig {
     
     @Bean(name = "log4jInitializer")
    public static boolean initLog4j() throws FileNotFoundException {
         Log4jConfigurer.initLogging("classpath:log4j.xml");
         return true;
     }
     
     @Bean(name = "messageSource")
    public static ReloadableResourceBundleMessageSource makeMessageSource() {
         final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
 
         messageSource.setBasename("/WEB-INF/messages/messages");
         messageSource.setCacheSeconds(0);
 
         return messageSource;
     }
     
     @Bean(name = "knetworkConfig")
    public static PropertiesFactoryBean makeAppProperties() {
         PropertiesFactoryBean bean = new PropertiesFactoryBean();
         bean.setLocation(new ClassPathResource("knetwork.properties"));
         return bean;
     }
 
     @Bean(name = "propertyConfigurer")
    public static PropertyPlaceholderConfigurer makePropertyConfigurer(@Value("#{knetworkConfig}") Properties properties) {
         final PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
 
         propertyConfigurer.setProperties(properties);
         propertyConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
 
         return propertyConfigurer;
     }
 
     @Bean(name = "viewResolver")
    public static InternalResourceViewResolver makeViewResolver() {
         final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
 
         viewResolver.setViewClass(JstlView.class);
         viewResolver.setPrefix("/WEB-INF/views/");
         viewResolver.setSuffix(".jsp");
         viewResolver.setExposedContextBeanNames(new String[] { "knetworkConfig" });
 
         return viewResolver;
     }
 
 }
