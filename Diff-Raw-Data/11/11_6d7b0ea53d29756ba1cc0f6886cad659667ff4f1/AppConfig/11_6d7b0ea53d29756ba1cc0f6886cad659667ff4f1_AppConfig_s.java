 package no.niths.common.config;
 
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 
 @Configuration
 public class AppConfig {
    public static final String BASE_PACKAGE = "no.niths";
    public static final String SERVICES_PACKAGE = BASE_PACKAGE + ".services";
    public static final String REST_PACKAGE = BASE_PACKAGE +
            ".application.rest";
    public static final String PERSISTENCE_PROPS = "persistence.properties";
 
     @Bean
     public static PropertyPlaceholderConfigurer properties(){
         final PropertyPlaceholderConfigurer ppc =
                 new PropertyPlaceholderConfigurer();
         final ClassPathResource[] resources = new ClassPathResource[] {
                 new ClassPathResource(PERSISTENCE_PROPS)};
         ppc.setLocations(resources);
         ppc.setIgnoreUnresolvablePlaceholders(true);
 
         return ppc;
     }
 }
