 package net.pdp7.tvguide.spring;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import net.pdp7.commons.spring.context.ContextUtils;
 import net.pdp7.tvguide.dao.EpgDao;
 import net.pdp7.tvguide.dao.UserDao;
 import net.pdp7.tvguide.scraper.es.elmundo.ElMundoScraper;
 
 import org.postgresql.jdbc2.optional.PoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 
 @Configuration
 public class AppConfiguration {
 
	@Autowired
	protected AnnotationConfigWebApplicationContext webApplicationContext;
	
 	@Bean
 	public EpgDao epgDao() {
 		return new EpgDao(jdbcTemplate());
 	}
 	
 	@Bean
 	public ElMundoScraper scraper() {
 		return new ElMundoScraper(epgDao());
 	}
 	
 	@Bean
 	public ThreadPoolTaskScheduler taskScheduler() {
 		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
 		taskScheduler.initialize();
 		taskScheduler.scheduleWithFixedDelay(scraperTask(), 1000*60*60L);
 		return taskScheduler;
 	}
 
 	public ElMundoScraper.ScraperTask scraperTask() {
 		return new ElMundoScraper.ScraperTask(scraper(), "true".equals(applicationProperties().getProperty("scraper.enabled")));
 	}
 
 	@Bean
 	public SimpleJdbcTemplate jdbcTemplate() {
 		return new SimpleJdbcTemplate(dataSource());
 	}
 
 	@Bean
 	public Properties applicationProperties() {
 		return ContextUtils.propertiesFromResource(applicationPropertiesResource());
 	}
 	
 	@Bean
 	public Resource applicationPropertiesResource() {
		return new ClassPathResource(webApplicationContext.getServletContext().getContextPath() + ".properties");
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
 
 	@Bean
 	public UserDao userDao() {
 		return new UserDao(jdbcTemplate());
 	}
 }
