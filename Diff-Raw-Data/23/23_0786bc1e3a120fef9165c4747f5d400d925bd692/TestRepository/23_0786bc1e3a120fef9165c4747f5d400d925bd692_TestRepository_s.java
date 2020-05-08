 package com.forum.repository;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.sql.DataSource;
 
 @Repository
 public class TestRepository {
 
    @Autowired
    @Qualifier("dataSource")
    BasicDataSource dataSource;
 
     private JdbcTemplate jdbcTemplate;
 
     @Autowired
    public TestRepository() {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Transactional
     public boolean getNumber(){
         int answer = jdbcTemplate.queryForInt("Select * from forum");
         return answer == 1;
     }
 }
