 package nl.surfnet.bod.config;
 
 import javax.sql.DataSource;
 
import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 @Configuration
 public class IntegrationDbConfiguration {
 
  @Value("${jdbc.jdbcUrl}") private String jdbcUrl;
  @Value("${jdbc.driverClass}") private String driverClass;
  @Value("${jdbc.user}") private String user;
  @Value("${jdbc.password") private String password;

   @Bean
   public DataSource dataSource() {
     DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(driverClass);
     dataSource.setUrl("jdbc:postgresql://localhost/bod-integration");
    dataSource.setUsername(user);
    dataSource.setPassword(password);
     return dataSource;
   }
 
 }
