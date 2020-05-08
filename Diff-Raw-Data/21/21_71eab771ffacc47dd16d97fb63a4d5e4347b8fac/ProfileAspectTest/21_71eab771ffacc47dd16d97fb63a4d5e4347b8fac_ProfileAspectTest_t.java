 /*
  * Copyright 2008-2010 Xebia and the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.xebia.management.statistics;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectInstance;
 import javax.management.ObjectName;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import fr.xebia.management.statistics.ProfileAspect.ClassNameStyle;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:fr/xebia/management/statistics/test-spring-context.xml")
 public class ProfileAspectTest {
 
     public static class TestService {
 
         public String getCountryCode() {
             return "FR";
         }
 
         @Profiled(slowInvocationThresholdInMillis = 100, verySlowInvocationThresholdInMillis = 200)
         public void doJobWithDefaultName() {
 
         }
 
         @Profiled(name = "my-name")
         public void doJobWithStaticName() {
 
         }
         
         @Profiled(name = "test-max-active", maxActive=1)
         public void testMaxActive() {
 
         }
        
         @Profiled(name = "test-max-active-with-spel", maxActiveExpression="#{ T(java.lang.Integer).parseInt(systemProperties['tomcat.thread-pool.size']) / 2 }")
         public void testMaxActiveWithSpel() {
 
         }
         
        @Profiled(name = "test-max-active-semaphore-acquisition-default", maxActive = 1)
        public void testMaxActiveSemaphoreAcquisition_default() {
        	
        }
        
         @Profiled(name = "test-max-active-semaphore-acquisition", maxActiveSemaphoreAcquisitionMaxTimeInMillis = 1000, maxActive = 1)
         public void testMaxActiveSemaphoreAcquisition() {
         	
         }
 
         @Profiled(name = "my-name(#{args[0]}-#{args[1]}-#{invokedObject.countryCode})")
         public void doJobWithElName(String arg1, String arg2) {
 
         }
 
         @Profiled(name = "test-slow-invocation-threshold", slowInvocationThresholdInMillis = 50, verySlowInvocationThresholdInMillis = 100)
         public void testSlowInvocationThreshold() throws InterruptedException {
             Thread.sleep(75);
         }
 
         @Profiled(name = "test-very-slow-invocation-threshold", slowInvocationThresholdInMillis = 25, verySlowInvocationThresholdInMillis = 50)
         public void testVerySlowInvocationThreshold() throws InterruptedException {
             Thread.sleep(75);
         }
     }
 
     @Autowired
     protected TestService testService;
 
     @Autowired
     protected ProfileAspect profileAspect;
 
     @Autowired
     protected MBeanServer mbeanServer;
 
     @Test
     public void testProfiledAnnotationWithDefaultName() throws Exception {
 
         // test
         testService.doJobWithDefaultName();
 
         // verify
 
         // don't use getClass().getName() because the class is enhanced by CGLib
         // and its name looks like
         // "...ProfileAspectTest$TestService$$EnhancerByCGLIB$$9b64fd54"
         String name = "f.x.m.s.ProfileAspectTest$TestService.doJobWithDefaultName";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         System.out.println(profileAspect.serviceStatisticsByName);
         assertNotNull(serviceStatistics);
         assertEquals(1, serviceStatistics.getInvocationCount());
         assertEquals(0, serviceStatistics.getSlowInvocationCount());
         assertEquals(0, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(0, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(0, serviceStatistics.getTotalExceptionCount());
 
         Set<ObjectInstance> mbeansInstances = mbeanServer.queryMBeans(new ObjectName("com.mycompany:type=ServiceStatistics,name=" + name),
                 null);
         assertEquals(1, mbeansInstances.size());
         
         ObjectName serviceStatisticsObjectName = mbeansInstances.iterator().next().getObjectName();
         assertEquals(1, mbeanServer.getAttribute(serviceStatisticsObjectName, "InvocationCount"));
         
     }
 
     @Test
     public void testProfiledAnnotationWithStaticName() throws Exception {
 
         // test
         testService.doJobWithStaticName();
 
         // verify
         String name = "my-name";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(1, serviceStatistics.getInvocationCount());
         assertEquals(0, serviceStatistics.getSlowInvocationCount());
         assertEquals(0, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(0, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(0, serviceStatistics.getTotalExceptionCount());
     }
     
     
 
     @Test
     public void testProfiledAnnotationWithElName() throws Exception {
 
         // test
         testService.doJobWithElName("foo", "bar");
 
         // verify
 
         String name = "my-name(foo-bar-FR)";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(1, serviceStatistics.getInvocationCount());
         assertEquals(0, serviceStatistics.getSlowInvocationCount());
         assertEquals(0, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(0, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(0, serviceStatistics.getTotalExceptionCount());
     }
 
     @Test
     public void testSlowInvocationThreshold() throws Exception {
 
         // test
         testService.testSlowInvocationThreshold();
 
         // verify
         String name = "test-slow-invocation-threshold";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(1, serviceStatistics.getInvocationCount());
         assertEquals(1, serviceStatistics.getSlowInvocationCount());
         assertEquals(0, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(0, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(0, serviceStatistics.getTotalExceptionCount());
     }
 
     @Test
     public void testMaxActive() throws Exception {
 
         // initialize
         testService.testMaxActive();
 
         String name = "test-max-active";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         // simulate one running invocation
         serviceStatistics.getMaxActiveSemaphore().acquire();
         assertEquals(0, serviceStatistics.getMaxActiveSemaphore().availablePermits());
         
         // test
         try {
             testService.testMaxActive();
            fail("ServiceUnavailableException expected");
         } catch(ServiceUnavailableException e) {
             // expected
         }
         
         // reset
         serviceStatistics.setMaxActive(1);
         testService.testMaxActive();
 
         // verify
         assertEquals(2, serviceStatistics.getInvocationCount());
         assertEquals(0, serviceStatistics.getSlowInvocationCount());
         assertEquals(0, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(1, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(1, serviceStatistics.getTotalExceptionCount());
 
     }
 
     @Test
     public void testMaxActiveWithSpel() throws Exception {
         System.setProperty("tomcat.thread-pool.size", "20");
 
         // initialize
         testService.testMaxActiveWithSpel();
 
         String name = "test-max-active-with-spel";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         
         int availablePermits = serviceStatistics.getMaxActiveSemaphore().availablePermits();
         assertEquals(10, availablePermits);
         }
 
 
     @Test
     public void testMaxActive_defaultSemaphoreAcquisition() throws Exception {
         // initialize
        testService.testMaxActiveSemaphoreAcquisition_default();
 
        String name = "test-max-active-semaphore-acquisition-default";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(TimeUnit.NANOSECONDS.convert(100, TimeUnit.MILLISECONDS), serviceStatistics.getMaxActiveSemaphoreAcquisitionMaxTimeInNanos());
     }
     
     @Test(timeout = 5000)
     public void testMaxActiveSemaphoreAcquisition() throws Exception {
         // initialize
         testService.testMaxActiveSemaphoreAcquisition();
 
         String name = "test-max-active-semaphore-acquisition";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(TimeUnit.NANOSECONDS.convert(1000, TimeUnit.MILLISECONDS), serviceStatistics.getMaxActiveSemaphoreAcquisitionMaxTimeInNanos());
 
         serviceStatistics.getMaxActiveSemaphore().acquire();
         assertEquals(0, serviceStatistics.getMaxActiveSemaphore().availablePermits());
         
         long start = System.currentTimeMillis();
         try {
         	testService.testMaxActiveSemaphoreAcquisition();
         } catch(ServiceUnavailableException ex) {
         	// Expected
         }
         long waited = System.currentTimeMillis() - start;
         
         // reset
         serviceStatistics.setMaxActive(1);
        testService.testMaxActiveSemaphoreAcquisition();
         
         assertTrue(waited >= 900L);
     }
     
     @Test
     public void testVerySlowInvocationThreshold() throws Exception {
 
         // test
         testService.testVerySlowInvocationThreshold();
 
         // verify
         String name = "test-very-slow-invocation-threshold";
         ServiceStatistics serviceStatistics = profileAspect.serviceStatisticsByName.get(name);
         assertNotNull(serviceStatistics);
         assertEquals(1, serviceStatistics.getInvocationCount());
         assertEquals(0, serviceStatistics.getSlowInvocationCount());
         assertEquals(1, serviceStatistics.getVerySlowInvocationCount());
         assertEquals(0, serviceStatistics.getBusinessExceptionCount());
         assertEquals(0, serviceStatistics.getServiceUnavailableExceptionCount());
         assertEquals(0, serviceStatistics.getCommunicationExceptionCount());
         assertEquals(0, serviceStatistics.getOtherExceptionCount());
         assertEquals(0, serviceStatistics.getTotalExceptionCount());
     }
 
     @Test
     public void testGetClassNameCompactFullyQualifiedName() {
         String actual = ProfileAspect
                 .getFullyQualifiedMethodName("java.lang.String", "length", ClassNameStyle.COMPACT_FULLY_QUALIFIED_NAME);
         assertEquals("j.l.String.length", actual);
     }
 
     @Test
     public void testGetClassNameFullyQualifiedName() {
         String actual = ProfileAspect.getFullyQualifiedMethodName("java.lang.String", "length", ClassNameStyle.FULLY_QUALIFIED_NAME);
         assertEquals("java.lang.String.length", actual);
     }
 
     @Test
     public void testGetClassNameShortName() {
         String actual = ProfileAspect.getFullyQualifiedMethodName("java.lang.String", "length", ClassNameStyle.SHORT_NAME);
         assertEquals("String.length", actual);
     }
 
     @Autowired
     ServiceStatistics serviceStatistics;
 
     @Test
     public void testServiceStatistics() throws Exception {
         System.out.println(serviceStatistics);
     }
 }
