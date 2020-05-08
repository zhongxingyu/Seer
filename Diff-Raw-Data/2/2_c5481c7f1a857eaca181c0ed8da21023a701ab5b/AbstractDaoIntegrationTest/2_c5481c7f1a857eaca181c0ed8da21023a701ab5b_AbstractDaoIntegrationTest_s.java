 package com.muni.fi.pa165.dao.gen;
 
 import org.junit.runner.RunWith;
 
 
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * This class aggregates annotations for all tests.
  *
  * @author Michal Vinkler
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:SpringContext.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager")
 @Transactional
 public abstract class AbstractDaoIntegrationTest {
 }
