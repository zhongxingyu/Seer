 package com.orangeleap.tangerine.test.dao.ibatis;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 
@ContextConfiguration(locations = { "classpath:/test-ibatis-application-context.xml" })
 public abstract class AbstractIBatisTest extends AbstractTestNGSpringContextTests  {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
 }
