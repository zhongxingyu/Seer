 package org.sukrupa.app.config;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.HandlerMapping;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.sukrupa.platform.web.StringTemplateView;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import static java.lang.String.format;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE;
 
 @Configuration
 @Import({DBConfig.class})
 public class AppConfig {
 
     private static final String ENVIRONMENT_KEY = "environment";
 
     @Bean
     public ViewResolver viewResolver() {
         InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
         viewResolver.setPrefix("/WEB-INF/templates/");
         viewResolver.setViewClass(StringTemplateView.class);
         viewResolver.setSuffix(".st");
         return viewResolver;
     }
 
     @Bean
     public HandlerMapping handlerMapping(OpenSessionInViewInterceptor interceptor) {
         DefaultAnnotationHandlerMapping handlerMapping = new DefaultAnnotationHandlerMapping();
         handlerMapping.setInterceptors(new Object[]{interceptor});
         return handlerMapping;
     }
 
     @Bean
     public OpenSessionInViewInterceptor openSessionInViewInterceptor(SessionFactory sessionFactory) {
         OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();
         interceptor.setSessionFactory(sessionFactory);
         return interceptor;
     }
 
     @Bean
     public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
         PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
         configurer.setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_OVERRIDE);
         configurer.setProperties(properties());
         return configurer;
     }
 
     @Bean
     public HandlerExceptionResolver handlerExceptionResolver() {
         SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
         resolver.setDefaultErrorView("error");
         return resolver;
     }
 
     @Bean
     public Properties properties() {
         try {
             Properties properties = new Properties();
             properties.load(defaultAppProperties());
             detectIfRunningInIntelliJ();
             if (environmentHasBeenSpecified()) {
                 properties.load(environnmentSpecificProperties());
             }
             return properties;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void detectIfRunningInIntelliJ() {
         if (isRunnintInIntelliJ()) {
             setEnvironment("intellij");
         }
     }
 
     private boolean isRunnintInIntelliJ() {
        return System.getProperty("java.class.path").toLowerCase().contains("intellij");
     }
 
     private boolean environmentHasBeenSpecified() {
         return isNotBlank(environment());
     }
 
     private String environment() {
         return System.getProperty(ENVIRONMENT_KEY);
     }
 
     private String setEnvironment(String value) {
         return System.setProperty(ENVIRONMENT_KEY, value);
     }
 
     private InputStream defaultAppProperties() throws IOException {
         return fromClassPath("app.properties");
     }
 
     private InputStream environnmentSpecificProperties() throws IOException {
         return fromClassPath(format("app.%s.properties", environment()));
     }
 
     private InputStream fromClassPath(String fileName) throws IOException {
         return new ClassPathResource(fileName).getInputStream();
     }
 }
