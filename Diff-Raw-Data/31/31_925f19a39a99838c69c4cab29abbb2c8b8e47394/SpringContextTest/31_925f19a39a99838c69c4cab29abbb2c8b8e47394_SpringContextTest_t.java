 package com.selfdiagnosis.test;
 
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Base class for test which requires Spring context. It is necessary for:
  * <ul>
  * <li>beans injection</li>
  * <li>connection to database and transaction</li>
  * </ul>
  *  
  * @author pkaminski
  *
  */
 @Ignore
 @ContextConfiguration(locations = {
        "classpath:spring/root-context-test.xml"
 	})
 @RunWith(SpringJUnit4ClassRunner.class)
 public class SpringContextTest extends AbstractTransactionalJUnit4SpringContextTests {
 
 }
