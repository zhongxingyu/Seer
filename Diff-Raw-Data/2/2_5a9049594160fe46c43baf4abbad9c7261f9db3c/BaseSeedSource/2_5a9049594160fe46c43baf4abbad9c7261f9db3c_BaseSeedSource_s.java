 package org.motechproject.ghana.national.tools.seed.data.source;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import javax.sql.DataSource;
 
 public class BaseSeedSource {
     protected JdbcTemplate jdbcTemplate;
 
     @Autowired
    public void init(@Qualifier("dataSource") DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
 }
