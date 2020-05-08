 package org.rosenvold.spring.convention;
 
 import org.junit.Test;
 import org.rosenvold.spring.convention.testclasses.DefaultTestService2;
 import org.rosenvold.spring.convention.testclasses.TestService;
 import org.rosenvold.spring.convention.testclasses.TestService2;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.support.GenericXmlContextLoader;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * @author Kristian Rosenvold
  */
 public class StandardContextLoaderTest {
     @Test
     public void testLoadContext()
             throws Exception {
         GenericXmlContextLoader conventionContextLoader = new GenericXmlContextLoader();
         final ApplicationContext applicationContext =
                 conventionContextLoader.loadContext("classpath:applicationContext-test2.xml");
         TestService testService = (TestService) applicationContext.getBean("fud");
         assertNotNull(testService);
         DefaultTestService2 testService2 = (DefaultTestService2) applicationContext.getBean(TestService2.class);
         assertNotNull(testService2);
         assertNotNull(testService2.testService);
         assertNotNull(testService2.applicationContext);
         assertNotNull( testService2.test);
         assertEquals(2,testService2.test.size());
 
 
         ListableBeanFactory listableBeanFactory = applicationContext;
         for (String bean : listableBeanFactory.getBeanDefinitionNames()) {
        //    System.out.println("bean = " + bean + listableBeanFactory.getBean( bean).getClass().getName());
         }
 
     }
 }
