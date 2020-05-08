 package com.cspinformatique.cspMarket.repository.config;
 
 import javax.sql.DataSource;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 @Configuration
@ImportResource("classpath:persistence/applicationContext-datasource.xml")
public class JdbcConfig {
	@Autowired private DataSource datasource;
	
 	public @Bean JdbcTemplate jdbcTemplate(){
		return new JdbcTemplate(this.datasource);
 	}
 }
