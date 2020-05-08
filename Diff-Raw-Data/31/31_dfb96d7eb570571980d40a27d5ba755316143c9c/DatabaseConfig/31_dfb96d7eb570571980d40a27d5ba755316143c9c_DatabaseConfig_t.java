 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.minlog.export.config;
 
 import java.util.ArrayList;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.transaction.annotation.TransactionManagementConfigurer;
 
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
 import com.avaje.ebean.springsupport.txn.SpringAwareJdbcTransactionManager;
 
 import dk.nsi.minlog.export.domain.LogEntry;
 
 @Configuration
 @EnableTransactionManagement
@ComponentScan({"dk.nsi.minlog.export.dao.ebean"})
 public class DatabaseConfig implements TransactionManagementConfigurer{
 	static final Logger logger = Logger.getLogger(DatabaseConfig.class);
 
     @Value("${jdbc.url}") String url;
     @Value("${jdbc.username}") String username;
     @Value("${jdbc.password}") String password;
 
     @Bean
     public DataSource dataSource() {
         final DriverManagerDataSource dataSource = new DriverManagerDataSource(
                 url,
                 username,
                 password
         );
         dataSource.setDriverClassName("com.mysql.jdbc.Driver");
         return dataSource;
     }
     
     @Bean
     public PlatformTransactionManager txManager() {
         return new DataSourceTransactionManager(dataSource());
     }
 
     @Override
     public PlatformTransactionManager annotationDrivenTransactionManager() {
         return txManager();
     }
 
     @Bean
     public EbeanServerFactoryBean ebeanServer(DataSource dataSource) throws Exception {
     	logger.debug("Creating ebeanServer with url=" + url + " username=" + username);    		
     	
         final EbeanServerFactoryBean factoryBean = new EbeanServerFactoryBean();
         final ServerConfig serverConfig = new ServerConfig();        
         final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
         classes.add(LogEntry.class);
         
         serverConfig.setName("localhostConfig");
         serverConfig.setClasses(classes);
         serverConfig.setDataSource(dataSource);
         serverConfig.setExternalTransactionManager(new SpringAwareJdbcTransactionManager());
         serverConfig.setNamingConvention(new com.avaje.ebean.config.MatchingNamingConvention());
         factoryBean.setServerConfig(serverConfig);
         return factoryBean;
     }
 }
