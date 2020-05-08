 package dk.nsi.minlog.web;
 
 import static org.junit.Assert.assertFalse;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 
 import dk.nsi.minlog.server.dao.RegistreringDao;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MinLogCleanupJobTest {
 	
 	@Mock
 	RegistreringDao registreringDao;
 	
 	@InjectMocks
 	MinLogCleanupJob job;
 		
 	@Test
 	public void removeExecution() {
 		job.cleanup();
 		verify(registreringDao).removeRegistreringBefore((DateTime)any());
 	}
 	
 	@Test
 	public void rerun(){
 		//Delay the answers so we make sure we only run job one at a time
 		doAnswer(new Answer<Object>() {
 		     public Object answer(InvocationOnMock invocation) {
 		    	 try{ Thread.sleep(100); } catch(Exception e){}
 		         return null;
 		     }
 		})
 		.when(registreringDao).removeRegistreringBefore((DateTime)any());		
 
 		//Start first cleanup in a seperate thread, so we can run second cleanup async.
 		new Thread(){
 			public void run() {
 				job.cleanup();				
 			};
 		}.start();
 		
 		job.cleanup();
 		verify(registreringDao).removeRegistreringBefore((DateTime)any());
 	}
 	
 	@Test
 	public void assumeNotRunningOnException(){
 		doThrow(new RuntimeException()).when(registreringDao).removeRegistreringBefore((DateTime)any());
		assertFalse(job.isRunning()); 
 	}
 }
