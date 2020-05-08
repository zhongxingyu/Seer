 package com.cspinformatique.cspMarket.repository.config;
 
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 @Configuration
public class JdbcConfig {	
	public @Bean DataSource datasource() {
		try {
			Context ctx = new InitialContext();
			return (DataSource) ctx.lookup("java:comp/env/jdbc/cspMarket");
		} catch (NamingException namingException) {
			throw new RuntimeException(namingException);
		}
	}

 	public @Bean JdbcTemplate jdbcTemplate(){
		return new JdbcTemplate(this.datasource());
 	}
 }
