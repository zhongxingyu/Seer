 package juitar.worker.jdbc;
 
 import org.juitar.workerq.CompletionCallback;
 import org.juitar.workerq.CompletionStatus;
 import org.juitar.workerq.Result;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired
    JdbcTemplate jdbcTemplate;
     final Object lock = new Object();
 
     @Test
    public void testEmbeddedDB() {

        final String updatePattern = "INSERT INTO TEST VALUES (%d, %d ,'%s', '%s')";
        jdbcTemplate.update(String.format(updatePattern, 1, 2, "str1", "str2"));
        Assert.assertEquals(1, jdbcTemplate.queryForInt("SELECT count(1) from TEST"));
        jdbcTemplate.update(String.format(updatePattern, 1, 2, "str1", "str2"));
        Assert.assertEquals(2, jdbcTemplate.queryForInt("SELECT count(1) from TEST"));

    }

    @Test
     public void testBasic() throws InterruptedException {
         final AtomicReference<String> value = new AtomicReference<>();
 
         for (int i = 0; i < 10; i++) {
            service.executeUpdate("INSERT INTO TEST VALUES (1, 2, 'str1', 'str2')", new CompletionCallback() {
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
