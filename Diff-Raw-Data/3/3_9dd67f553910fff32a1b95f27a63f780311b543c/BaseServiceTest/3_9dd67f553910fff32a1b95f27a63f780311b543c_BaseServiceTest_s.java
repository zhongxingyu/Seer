 package com.worthsoln.test.service;
 
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.sql.DataSource;
 
 /**
  *  All service tests should extend.
  *
  *  Sets up everything required for hibernate, persistence.
  *
  *  NOTE: these tests are not transaction driven, the transactions are create new in the service layer,
  *  hence the need to manually roll back the database in between each test.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring-context.xml", "classpath:test-context.xml"})
 public abstract class BaseServiceTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceTest.class);
 
     private JdbcTemplate jdbcTemplate;
 
     @Inject
     private DataSource dataSource;
 
     @PostConstruct
     public void init() {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Before
     @Transactional
     public void clearDownDb() {
         LOGGER.info("STARTING CLEAR DOWN DB");
 
         // this should have all the tables in - extend when necessary
 
         // importer tables
         jdbcTemplate.execute("delete from centre");
         jdbcTemplate.execute("delete from patient");
         jdbcTemplate.execute("delete from testresult");
         jdbcTemplate.execute("delete from letter");
         jdbcTemplate.execute("delete from diagnosis");
         jdbcTemplate.execute("delete from medicine");
 
         // user tables
         jdbcTemplate.execute("delete from usermapping");
        jdbcTemplate.execute("delete from user");
         jdbcTemplate.execute("delete from tenancyuserrole");
         jdbcTemplate.execute("delete from tenancy");
 
 
 
     }
 }
