 package net.pdp7.tvguide.spring;
 
import java.io.IOException;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import net.pdp7.commons.spring.context.ContextUtils;
 import net.pdp7.tvguide.dao.EpgDao;
 
 import org.postgresql.jdbc2.optional.PoolingDataSource;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 @Configuration
 public class AppConfiguration {
 
 	@Bean
 	public EpgDao epgDao() {
 		return new EpgDao(jdbcTemplate());
 	}
 
 	@Bean
 	public SimpleJdbcTemplate jdbcTemplate() {
 		return new SimpleJdbcTemplate(dataSource());
 	}
 
 	@Bean
 	public Properties applicationProperties() {
 		return ContextUtils.propertiesFromResource(applicationPropertiesResource());
 	}


 	public Resource applicationPropertiesResource() {
 		return new ClassPathResource("/net/pdp7/tvguide/application.properties");
 	}
 
 	@Bean
 	public DataSource dataSource() {
 		PoolingDataSource poolingDataSource = new PoolingDataSource();
 		poolingDataSource.setServerName(applicationProperties().getProperty("jdbc.server.name"));
 		poolingDataSource.setDatabaseName(applicationProperties().getProperty("jdbc.database.name"));
 		poolingDataSource.setUser(applicationProperties().getProperty("jdbc.user"));
 		poolingDataSource.setPassword(applicationProperties().getProperty("jdbc.password"));
 		return poolingDataSource;	
 	}
 }
