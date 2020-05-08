 package edu.northwestern.bioinformatics.studycalendar.testing;
 
 import edu.nwu.bioinformatics.commons.testing.DbTestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
 
 import javax.sql.DataSource;
 
 /**
  * @author Rhett Sutphin
  */
 public abstract class DaoTestCase extends DbTestCase {
     private static ApplicationContext applicationContext = null;
     protected final Log log = LogFactory.getLog(getClass());
 
     protected MockHttpServletRequest request = new MockHttpServletRequest();
     protected MockHttpServletResponse response = new MockHttpServletResponse();
     private boolean shouldFlush = true;
 
     protected void setUp() throws Exception {
         super.setUp();
         beginSession();
     }
 
     protected void tearDown() throws Exception {
         endSession();
        findOpenSessionInViewInterceptor().setFlushModeName("FLUSH_NEVER");
         super.tearDown();
     }
 
     public void runBare() throws Throwable {
         setUp();
         try {
             runTest();
         } catch (Throwable throwable) {
             shouldFlush = false;
             throw throwable;
         } finally {
             tearDown();
         }
     }
 
     private void beginSession() {
         log.info("-- beginning DaoTestCase interceptor session --");
         findOpenSessionInViewInterceptor().preHandle(request, response, null);
     }
 
     private void endSession() {
         log.info("--    ending DaoTestCase interceptor session --");
         OpenSessionInViewInterceptor interceptor = findOpenSessionInViewInterceptor();
         if (shouldFlush) {
             interceptor.postHandle(request, response, null, null);
         }
         interceptor.afterCompletion(request, response, null, null);
     }
 
     protected void interruptSession() {
         endSession();
         log.info("-- interrupted DaoTestCase session --");
         beginSession();
     }
 
     private OpenSessionInViewInterceptor findOpenSessionInViewInterceptor() {
         return (OpenSessionInViewInterceptor) getApplicationContext().getBean("openSessionInViewInterceptor");
     }
 
 
     protected DataSource getDataSource() {
         return (DataSource) getApplicationContext().getBean("dataSource");
     }
 
     public ApplicationContext getApplicationContext() {
         synchronized (DaoTestCase.class) {
             if (applicationContext == null) {
                 applicationContext = ContextTools.createDeployedApplicationContext();
             }
             return applicationContext;
         }
     }
 }
