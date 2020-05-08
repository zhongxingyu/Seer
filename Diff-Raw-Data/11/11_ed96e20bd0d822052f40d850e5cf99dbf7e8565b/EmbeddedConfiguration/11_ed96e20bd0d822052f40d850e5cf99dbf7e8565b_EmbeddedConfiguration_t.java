 package dk.bemyndigelsesregister.bemyndigelsesservice.config;
 
 import org.springframework.context.annotation.Profile;
 
 import javax.sql.DataSource;
 
 //@Configuration
 @Profile("embedded")
 public class EmbeddedConfiguration {
     //private final Log logger = LogFactory.getLog(getClass());
 
    //@Bean
     public DataSource dataSource() {
         //logger.info("Creating development database");
         //return new EmbeddedDatabaseBuilder().build();
         return null;
     }
 }
