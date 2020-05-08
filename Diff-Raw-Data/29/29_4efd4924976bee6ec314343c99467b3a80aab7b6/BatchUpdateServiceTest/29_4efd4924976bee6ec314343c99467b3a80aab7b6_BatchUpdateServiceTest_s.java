 package juitar.worker.jdbc;
 
 import org.juitar.workerq.CompletionCallback;
 import org.juitar.workerq.CompletionStatus;
 import org.juitar.workerq.Result;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * @author sha1n
  * Date: 1/20/13
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:META-INF/update-worker-context.xml", "classpath:META-INF/test-context.xml"})
 public class BatchUpdateServiceTest {
 
     @Autowired
     BatchUpdateService service;
     final Object lock = new Object();
 
     @Test
     public void testBasic() throws InterruptedException {
         final AtomicReference<String> value = new AtomicReference<>();
 
         for (int i = 0; i < 10; i++) {
            service.executeUpdate("INSERT INTO TEST VALUES (1, 'str')", new CompletionCallback() {
                 @Override
                 public void onSuccess(Result result) {
                     value.set(result.getWorkId());
                     synchronized (lock) {
                         lock.notifyAll();
                     }
                 }
 
                 @Override
                 public void onFailure(Result result, Exception e, CompletionStatus status) {
                     e.printStackTrace();
                     synchronized (lock) {
                         lock.notifyAll();
                     }
 
                 }
             });
         }
 
         synchronized (lock) {
             lock.wait(3000);
         }
 
         Assert.assertNotNull(value.get());
     }
 }
