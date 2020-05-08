 package nl.surfnet.bod.config;
 
 import javax.sql.DataSource;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 @Configuration
 public class IntegrationDbConfiguration {
 
   @Bean
   public DataSource dataSource() {
     DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
     dataSource.setUrl("jdbc:postgresql://localhost/bod-integration");
    dataSource.setUsername("bod-integration_user");
    dataSource.setPassword("secret");
     return dataSource;
   }
 
 }
