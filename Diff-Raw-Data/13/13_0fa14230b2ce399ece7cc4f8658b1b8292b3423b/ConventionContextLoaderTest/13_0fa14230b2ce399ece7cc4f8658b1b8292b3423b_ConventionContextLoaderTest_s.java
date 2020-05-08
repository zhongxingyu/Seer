 package org.rosenvold.spring.convention;
 
 import org.junit.Test;
 import org.rosenvold.spring.convention.testclasses.DefaultTestService2;
 import org.rosenvold.spring.convention.testclasses.TestService;
 import org.rosenvold.spring.convention.testclasses.TestService2;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * @author Kristian Rosenvold
  */
 public class ConventionContextLoaderTest {
     @Test
     public void testLoadContext()
             throws Exception {
         ConventionContextLoader conventionContextLoader = new ConventionContextLoader();
         final ApplicationContext applicationContext =
                 conventionContextLoader.loadContext("classpath:applicationContext-test.xml");
 
         ListableBeanFactory listableBeanFactory = applicationContext;
        for (String bean : listableBeanFactory.getBeanDefinitionNames()) {
            System.out.println("vbean = " + bean + listableBeanFactory.getBean( bean).getClass().getName());
        }
 
         TestService testService = (TestService) applicationContext.getBean("fud");
         assertNotNull(testService);
         DefaultTestService2 testService2 = (DefaultTestService2) applicationContext.getBean(TestService2.class);
         assertNotNull(testService2);
         assertNotNull(testService2.testService);
         assertNotNull(testService2.interfaceLessService);
         assertNotNull( testService2.test);
 //        assertEquals(2,testService2.test.size());
 
 
     }
 }
