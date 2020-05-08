 package taskqueue;
 
 import httptestserver.HttpTestServer;
 import org.junit.Before;
 import org.junit.After;
 
 import task.HttpConstants.HttpContentType;
 import task.HttpConstants;
 import task.HttpHeaders;
 import task.HttpParams;
 import utils.DefaultTaskQueueResultListener;
 
 import java.util.Arrays;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.junit.Assert.*;
 
 /**
  * User: evg
  * Date: 23/11/11
  * Time: 17:59
  */
 
 
public class HTTPTaskQueueTest
{
     HttpTaskQueue taskQueue;
     HttpTestServer testServer;
 
     @Before
     public void setup() {
 
         Logger logger = Logger.getLogger("com.wixpress.aqueduct");
         logger.setLevel(Level.FINEST);
 
         ConsoleHandler handler = new ConsoleHandler();
         logger.addHandler(handler);
 
         testServer = new HttpTestServer();
         testServer.start();
 
         taskQueue = new HttpTaskQueue("test");
         taskQueue.purgeTasks();
     }
 
     @After
     public void tearDown(){
         taskQueue.shutdown();
         testServer.stop();
     }
 
     @org.junit.Test
     public void testAddGetTaskSuccess() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
         taskQueue.addListener(resultListener, true);
 
         taskQueue.queue(
                 taskQueue.createGetTask(testServer.getLocalUrl())
         );
 
         assertEquals(200, resultListener.getTask().lastResult().getStatus());
         assertEquals(0, taskQueue.getPendingTasks().size());
     }
 
     @org.junit.Test
     public void testAddGetTaskFailedServerBadPort() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
         taskQueue.addListener(resultListener, true);
 
         // Set your own Identity (limited string, remember it is marshalled/unmarshaled)
         taskQueue.queue(
                 taskQueue.createGetTask("http://localhost:1")
                         .withIdentity("my_task")
         );
 
         assertEquals(0, resultListener.getTask().lastResult().getStatus());
         assertEquals("my_task", taskQueue.getPendingTasks().get(0).getIdentity());
     }
 
     @org.junit.Test
     public void testAddGetTaskFailedRetry() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener(30, TimeUnit.SECONDS, 3);
         taskQueue.addListener(resultListener, true);
 
         // Task with custom Http headers
         taskQueue.queue(
                 taskQueue.createGetTask(testServer.getLocalUrl())
                 .withHeaders(
                         (new HttpHeaders()).addHeader("Test-Status", "503"))
         );
 
         assertEquals(503, resultListener.getTask().lastResult().getStatus());
         assertEquals(3, resultListener.getTask().getRetryCount());
     }
 
     @org.junit.Test
     public void testPostTaskBigParams() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
         taskQueue.addListener(resultListener, true);
 
         byte [] payload = new byte[4096];
         Arrays.fill(payload, (byte) '1');
 
         // Big POST with custom success response code
         taskQueue.queue(
                 taskQueue.createPostTask(testServer.getLocalUrl())
                         .withIdentity("my_unique_guid")
                         .withParameters(new HttpParams().add("data", new String(payload)))
                         .withHeaders((new HttpHeaders()).addHeader("Test-Status", "409"))
                         .withSuccessResponseCodes(new int[]{409})
         );
 
 
         assertTrue(resultListener.getTask().isSuccess());
     }
 
     @org.junit.Test
     public void testPutTaskWithData() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
         taskQueue.addListener(resultListener, true);
 
         byte [] payload = new byte[4096];
         Arrays.fill(payload, (byte) '1');
 
         // Big PUT with custom success response code
         taskQueue.queue(
                 taskQueue.createPutTask(testServer.getLocalUrl())
                         .withIdentity("my_unique_guid")
                         .withParameters(new HttpParams().add("param1", "value1"))
                         .withHeaders((new HttpHeaders()).addHeader("Test-Status", "409"))
                         .withSuccessResponseCodes(new int[]{409})
                         .withData(payload, HttpContentType.JSON)
         );
 
         assertTrue(resultListener.getTask().isSuccess());
     }
 
 
     @org.junit.Test(expected = IllegalArgumentException.class)
     public void testDeleteTaskURLTooLong() throws Exception {
 
         DefaultTaskQueueResultListener resultListener = new DefaultTaskQueueResultListener();
         taskQueue.addListener(resultListener, true);
 
         byte [] payload = new byte[4096];
         Arrays.fill(payload, (byte) '1');
 
         // Big Parameter
         taskQueue.queue(
                 taskQueue.createDeleteTask(testServer.getLocalUrl())
                         .withParameters(new HttpParams().add("data", new String(payload)))
         );
 
         assertFalse(resultListener.getTask().isSuccess());
     }
 }
