 package eu.stratosphere.meteor.client;
 
 import eu.stratosphere.meteor.common.DSCLJob;
 import eu.stratosphere.meteor.common.JobState;
 import eu.stratosphere.meteor.common.JobStateListener;
 import org.junit.Test;
 
 public class DOPAClientTest {
 	
	/**
	 * PLATZHALTER
	 */
 
 
     @Test
     public void testQuerySubmission () {
         DOPAClient client = DOPAClient.createNewClient("testID");
         client.connect();
         JobStateListener listener = new JobStateListener() {
             @Override
             public void stateChanged(DSCLJob job, JobState newStatus) {
                 System.out.println("Changed JobState " + newStatus.toString());
             }
         };
 
         DSCLJob job = client.createNewJob("PROVOKE SYNTAX ERROR", listener);
 
 
         while (true) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 	
 }
