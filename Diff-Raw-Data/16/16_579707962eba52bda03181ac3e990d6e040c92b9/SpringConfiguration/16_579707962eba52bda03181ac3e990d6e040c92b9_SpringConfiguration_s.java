 // --------------------------------------------------------------------------------
 //
 // Copyright 2013 Gonzalo Alvarez - GonzaloAlvarez.es
 // Madrid, Spain
 //
 // This file is part of Spring Rest Starter.
 // 
 // Spring Rest Starter is free software: you can redistribute it and/or
 // modify it under the terms of the GNU General Public License 
 // as published by the Free Software Foundation, either version 3
 // of the License, or (at your option) any later version.
 // 
 // Spring Rest Starter is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 // See the GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License 
 // along with Spring Rest Starter. If not, see http://www.gnu.org/licenses/.
 //
 // --------------------------------------------------------------------------------
 //
 // System : spring-rest-starter
 // Sub-System : es.galvarez.rest.config
 // File Name : SpringConfiguration.java
 //
 // Author : Gonzalo Alvarez
 // Creation Date : 25/08/2013
 //
 // -----------------------------------------------------------------------------
 package es.galvarez.rest.config;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.config.EnableHypermediaSupport;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
 
 /**
  * @author Gonzalo Alvarez
  *
  */
 @Configuration
 @EnableJpaRepositories(basePackages = SpringConfiguration.BASE_PACKAGES)
 @EnableTransactionManagement
@EnableHypermediaSupport
 @ComponentScan(basePackages = SpringConfiguration.BASE_PACKAGES, excludeFilters = @Filter({ Service.class, Configuration.class }))
 public class SpringConfiguration  extends DelegatingWebMvcConfiguration {
 
 	public static final String BASE_PACKAGES = "es.galvarez.rest";
 	public static final String DATASOURCE_FILE = "/datasource.properties";
 
 	@Value("${dataSource.driverClassName}")
 	private String driver;
 	@Value("${dataSource.url}")
 	private String url;
 	@Value("${dataSource.username}")
 	private String username;
 	@Value("${dataSource.password}")
 	private String password;
 	@Value("${hibernate.dialect}")
 	private String dialect;
 	@Value("${hibernate.hbm2ddl.auto}")
 	private String hbm2ddlAuto;
 	
 
 	@Bean
 	public DataSource configureDataSource() {
 		DriverManagerDataSource dataSource = new DriverManagerDataSource();
 		dataSource.setDriverClassName(driver);
 		dataSource.setUrl(url);
 		dataSource.setUsername(username);
 		dataSource.setPassword(password);
 		return dataSource;
 	}
 
 	@Bean(name = "entityManagerFactory")
 	public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory() {
 		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactoryBean.setDataSource(configureDataSource());
 		entityManagerFactoryBean.setPackagesToScan(BASE_PACKAGES);
 		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
 
 		Properties jpaProperties = new Properties();
 		jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
 		jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
 		entityManagerFactoryBean.setJpaProperties(jpaProperties);
 
 		return entityManagerFactoryBean;
 	}
 
 	@Bean(name = "transactionManager")
 	public PlatformTransactionManager annotationDrivenTransactionManager() {
 		return new JpaTransactionManager();
 	}
 
 	@Bean(name = "jdbcExceptionTranslator")
 	public SQLErrorCodeSQLExceptionTranslator jdbcExceptionTranslator() {
 		SQLErrorCodeSQLExceptionTranslator exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator();
 		exceptionTranslator.setDataSource(configureDataSource());
 		return exceptionTranslator;
 	}
 
 	@Bean
 	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
 		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
 		ppc.setLocation(new ClassPathResource(DATASOURCE_FILE));
 		return ppc;
 	}
 
 	@Bean(name="eventListenerBean")
 	public DBInitializerApplicationStartupListener applicationStartupListener() {
 		return new DBInitializerApplicationStartupListener();
 	}
 }
