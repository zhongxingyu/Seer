 package com.selfdiagnosis.test;
 
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.webflow.config.FlowDefinitionResource;
 import org.springframework.webflow.config.FlowDefinitionResourceFactory;
 import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;
 
 /**
  * Base class for test which requires Spring context web context. It is necessary for:
  * <ul>
  * <li>beans injection</li>
  * <li>connection to database and transaction</li>
  * <li>webflow tests</li>
  * </ul>
  *  
  * @author mmieszkowski
  *
  */
 @Ignore
 @ContextConfiguration(locations = {
	    "classpath:spring/root-context-test.xml",
	    "file:src/main/webapp/WEB-INF/spring/web-root-context.xml"
 	})
 @RunWith(SpringJUnit4ClassRunner.class)
 public abstract class SpringWebContextTest extends AbstractXmlFlowExecutionTests {
 
     @Override
     protected abstract FlowDefinitionResource getResource(FlowDefinitionResourceFactory arg0);
 
 }
