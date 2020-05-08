 package gnutch.quartz;
 
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 import org.quartz.SchedulerException;
 
 public class SchedulerServiceTest extends TestCase {
 
     private class TimeoutListenerImpl implements TimeoutListener {
 
         private int invokationCount = 0;
         private boolean jobTriggered = false;
 
         @Override
         public void onTimeout(String key) {
             invokationCount++;
             jobTriggered = true;
         };
     }
 
     /**
      * Test method.
      *
      * @throws SchedulerException
      */
     public void testAddRemoveListeners() throws SchedulerException{
         SchedulerService schedulerService = new SchedulerService();
         TimeoutListenerImpl timeoutListener = new TimeoutListenerImpl();
        assertEquals(schedulerService.getTimeoutListeners().size(), 0);
         schedulerService.addTimeoutListener(timeoutListener);
        assertEquals(schedulerService.getTimeoutListeners().size(), 1);
         schedulerService.removeTimeoutListener(timeoutListener);
        assertEquals(schedulerService.getTimeoutListeners().size(), 0);
     }
 
     /**
      * Test method.
      *
      * @throws SchedulerException
      */
     public void testSingleJob() throws SchedulerException {
         SchedulerService schedulerService = new SchedulerService();
         try {
             schedulerService.getQuartzScheduler().start();
             TimeoutListenerImpl timeoutListener = new TimeoutListenerImpl();
             String key = "key-1";
             schedulerService.addTimeoutListener(timeoutListener);
             Properties jobProperties = new Properties();
             jobProperties.setProperty(key, "5 seconds");
             schedulerService // schedule a set of jobs for regular execution through given intervals
                 .setProperties(jobProperties);
             synchronized(this) {
                 try {
                     wait(5000); // note the "5 seconds" above
                 } catch (InterruptedException e) {
                 }
             }
             assertTrue("Failed to trigger a single job.", timeoutListener.jobTriggered);
         } finally {
             schedulerService.getQuartzScheduler().shutdown(false);
         }
     }
 
     /**
      * Test method.
      *
      * @throws SchedulerException
      */
     public void testMultipleSimultaneousJobs() throws SchedulerException {
         SchedulerService schedulerService = new SchedulerService();
         try {
             schedulerService.getQuartzScheduler().start();
             TimeoutListenerImpl timeoutListener1 = new TimeoutListenerImpl();
             String key1 = "key-1";
             TimeoutListenerImpl timeoutListener2 = new TimeoutListenerImpl();
             String key2 = "key-2";
             schedulerService.addTimeoutListener(timeoutListener1);
             schedulerService.addTimeoutListener(timeoutListener2);
             Properties jobProperties = new Properties();
             jobProperties.setProperty(key1, "5 seconds");
             jobProperties.setProperty(key2, "5 seconds");
             schedulerService // schedule a set of jobs for regular execution through given intervals
                 .setProperties(jobProperties);
             synchronized(this) {
                 try {
                     wait(5000); // note the "5 seconds" above
                 } catch (InterruptedException e) {
                 }
             }
             assertTrue("Failed to trigger two simultaneous jobs.", timeoutListener1.jobTriggered && timeoutListener2.jobTriggered );
         } finally {
             schedulerService.getQuartzScheduler().shutdown(false);
         }
     }
 
     /**
      * Test method.
      *
      * @throws SchedulerException
      */
     public void testSetProperties() throws SchedulerException {
         SchedulerService schedulerService = new SchedulerService();
         try {
             schedulerService.getQuartzScheduler().start();
             TimeoutListenerImpl timeoutListener = new TimeoutListenerImpl();
             String key = "testSetProperties-1";
             schedulerService.addTimeoutListener(timeoutListener);
             Properties jobProperties = new Properties();
             jobProperties.setProperty(key, "5 seconds");
             schedulerService // schedule a set of jobs for regular execution through given intervals
                 .setProperties(jobProperties);
             synchronized(this) {
                 try {
                     wait(4900); // note the "5 seconds" above
                 } catch (InterruptedException e) {
                 }
             }
             assertTrue("Failed to set the same job for a fist time. Invokation count = " + timeoutListener.invokationCount, timeoutListener.invokationCount == 1);
             // Now set the same properties for a second time
             schedulerService.setProperties(jobProperties);
             synchronized(this) {
                 try {
                     wait(4900);
                 } catch (InterruptedException e) {
                 }
             }
             assertTrue("Failed to set the same job for a second time. Invokation count = " + timeoutListener.invokationCount, timeoutListener.invokationCount == 2);
             // Wait for one more cycle to verify only one job is executed
             synchronized(this) {
                 try {
                     wait(5000);
                 } catch (InterruptedException e) {
                 }
             }
             assertTrue("Failed to set the same job for a third time. Invokation count = " + timeoutListener.invokationCount, timeoutListener.invokationCount == 3);
         } finally {
             schedulerService.getQuartzScheduler().shutdown(false);
         }
     }
 }
