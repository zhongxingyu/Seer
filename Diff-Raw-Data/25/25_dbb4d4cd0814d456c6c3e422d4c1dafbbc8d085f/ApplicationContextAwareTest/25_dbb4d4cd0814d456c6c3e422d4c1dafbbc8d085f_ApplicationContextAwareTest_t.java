 package com.odessa.discmanagement.server.dao.impl;
 
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * User: ToRTiK
  * Date: 13.01.13
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml","classpath:startData.xml"})
 @TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
 @Transactional
 public abstract class ApplicationContextAwareTest {
     public static final String ADDRESS = "address";
     public static final String EMAIL = "email@email.com";
     public static final String FIRST_NAME = "firstName";
     public static final String LAST_NAME = "lastName";
     public static final int STATUS_ID = 1;
     public static final String TELEPHONE = "+380000000000";
 
 }
