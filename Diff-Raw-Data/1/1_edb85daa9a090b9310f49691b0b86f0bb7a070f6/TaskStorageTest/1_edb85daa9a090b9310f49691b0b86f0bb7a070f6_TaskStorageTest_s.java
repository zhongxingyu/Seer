 package taskqueue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import task.*;
 
 
 import java.io.File;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import static java.lang.System.getProperty;
 import static org.hamcrest.CoreMatchers.is;
 import static utils.HttpTaskIsEqual.*;
 import static org.junit.Assert.*;
 
 /**
  * Created by evg.
  * Date: 01/02/12
  * Time: 00:05
  */
 public class TaskStorageTest{
 
     private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
 
     TaskStorage taskStorage;
     HttpTask aTask;
 
     @Before
     public void setup() throws Exception {
 
         taskStorage = new TaskStorage(getProperty(JAVA_IO_TMPDIR) + "/taskqueue.db");
 
         aTask = HttpTaskFactory.create("POST", "http://xxx.com", true)
                 .withParameters((new HttpParams().add("p1", "v1")).add("p2", "v2"))
                 .withIdentity("test_task")
                 .withData("datadata".getBytes(), HttpConstants.HttpContentType.JSON)
                 .withHeaders((new HttpHeaders()).addHeader("h1", "v1").addHeader("h1", "v2").addHeader("h2", "v3"))
                 .withTTL(5, TimeUnit.DAYS)
                 .withCookies(new HttpCookies().add("c1", "v1"))
                 .withMaxRetries(4)
                 .withSuccessResponseCodes(new int[]{11,12});
     }
 
     @After
     public void tearDown() {
         (new File(getProperty("java.io.tmpdir") + "/taskqueue.db")).delete();
     }
 
 
     @Test
     public void testAddTask() throws Exception {
 
         taskStorage.addTask(aTask);
 
         assertEquals(1, taskStorage.getPendingTasks().size());
     }
 
     @Test
     public void testDeleteTask() throws Exception {
 
         taskStorage.addTask(aTask);
         taskStorage.deleteTask(aTask);
 
         assertEquals(0, taskStorage.getPendingTasks().size());
     }
 
     @Test
     public void testSaveTask() throws Exception {
 
         taskStorage.addTask(aTask);
         assertEquals(0, taskStorage.getPendingTasks().get(0).getRetryCount());
 
         aTask.triedOnce();
         taskStorage.saveTask(aTask);
 
         assertEquals(1, taskStorage.getPendingTasks().get(0).getRetryCount());
     }
 
     public void testGiveUpTask() throws Exception {
 
     }
 
     @Test
     public void testLeaseTasks() throws Exception {
 
         taskStorage.addTask(aTask);
 
         assertEquals(1, taskStorage.leaseTasks().size());
         assertEquals(1, taskStorage.getActiveTasks().size());
     }
 
     @Test
     public void testPurge() throws Exception {
         taskStorage.addTask(aTask);
         taskStorage.purge();
 
         assertEquals(0, taskStorage.getPendingTasks().size());
     }
 
     @Test
     public void testTaskMarshaling() throws Exception{
 
         aTask.addResult(failedResult());
         aTask.addResult(successResult());
 
         taskStorage.addTask(aTask);
         HttpTask theTask = taskStorage.getPendingTasks().get(0);
 
         assertThat(theTask, is(aTask));
     }
 
     public void testGetActiveTasks() throws Exception {
 
     }
 
     public void testGetPendingTasks() throws Exception {
 
     }
 }
