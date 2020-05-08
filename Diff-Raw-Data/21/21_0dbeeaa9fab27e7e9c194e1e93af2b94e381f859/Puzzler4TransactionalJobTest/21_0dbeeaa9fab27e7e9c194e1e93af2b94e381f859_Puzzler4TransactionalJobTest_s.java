 package puzzlers.puzzler4;
 
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:/puzzler4-context.xml")
 public class Puzzler4TransactionalJobTest {
 
     @Autowired
    private Puzzler4TransactionalJob job;
 
     @Test
     public void runJobRuns() {
         job.runJob();
     }
 
 }
