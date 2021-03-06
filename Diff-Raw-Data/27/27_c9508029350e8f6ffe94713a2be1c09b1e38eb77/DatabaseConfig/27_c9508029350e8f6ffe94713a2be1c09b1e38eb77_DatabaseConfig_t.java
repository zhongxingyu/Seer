 /*
  * Copyright (C) 2012 - 2012 NHN Corporation
  * All rights reserved.
  *
  * This file is part of The nGrinder software distribution. Refer to
  * the file LICENSE which is part of The nGrinder distribution for
  * licensing details. The nGrinder distribution is available on the
  * Internet at http://nhnopensource.org/ngrinder
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.nhncorp.ngrinder.infra.config;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 
 import com.nhncorp.ngrinder.core.util.PropertiesWrapper;
 
 /**
  * Dynamic datasource bean generator.
  * 
  * @author JunHo Yoon
  * @since 3.0
  */
 @Configuration
 public class DatabaseConfig {
 	@SuppressWarnings("unused")
 	private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
 
 	@Autowired
	private Config config;
 
 	@Bean(name = "dataSource", destroyMethod = "close")
 	public BasicDataSource dataSource() {
 		BasicDataSource dataSource = new BasicDataSource();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();
 		Database database = Database.getDatabase(databaseProperties.getProperty("database", "sqlite",
 				"[FATAL] Database type is not sepecfied. In default, use sqlite."));
 		database.setup(dataSource, databaseProperties);
 		return dataSource;
 	}
 
 	@Bean(name = "emf")
 	public LocalContainerEntityManagerFactoryBean emf() {
 		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
 		emf.setDataSource(dataSource());
 		emf.setPersistenceUnitName("ngrinder");
 		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();
 		Database database = Database.getDatabase(databaseProperties.getProperty("database", "sqlite",
 				"[FATAL] Database type is not sepecfied. In default, use sqlite."));
 
 		hibernateJpaVendorAdapter.setDatabasePlatform(database.getDialect());
		hibernateJpaVendorAdapter.setShowSql(true);
 		hibernateJpaVendorAdapter.setGenerateDdl(true);
 		emf.setJpaVendorAdapter(hibernateJpaVendorAdapter);
 		emf.setPackagesToScan("com.nhncorp.ngrinder.**.model");
 		return emf;
 	}
 }
