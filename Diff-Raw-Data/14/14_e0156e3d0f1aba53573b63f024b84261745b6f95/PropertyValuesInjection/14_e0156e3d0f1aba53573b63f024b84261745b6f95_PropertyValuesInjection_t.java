 package eu.gruchala.spring.props;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 
 /**
  * <p/>
  *
  * @author lgr
  */
 @Configuration
 @ImportResource("classpath:additional-config.xml")
 @PropertySource("classpath:organisation.properties")
 public class PropertyValuesInjection {
 
     //Environment can be used with @PropertySource instead of @Value annotation
     @Autowired
     private Environment environment;
 
     @Value("${topic}")
     private String topic;
 
    //There must be PropertySourcesPlaceholderConfigurer defined to use @Value with @PropertySource
    //can be defined in xml and imported by @ImportResource or as STATIC bean in @Configuration class
     @Value("${jug.name}")
     private String organisationName;
 
     @Bean
     protected String getOrganisationNameFromEnv() {
         return environment.getProperty("jug.name");
     }

     public String getCityOfJug() {
         return "Szczecin";
     }
 
     public String getTopic() {
         return topic;
     }
 
     public String getOrganisationName() {
         return organisationName;
     }
 }
