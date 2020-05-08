 package net.chrisrichardson.async.futures;
 
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 import net.chrisrichardson.asyncpojos.futures.EnhancedFutureImpl;
 
 import org.junit.Test;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
 
 
 public class EnhancedFutureImplTest {
   
   private static final TaskExecutor taskExecutor = new ConcurrentTaskExecutor();
 
 @Test
   public void futuresShouldNotNest() throws Exception {
     EnhancedFutureImpl<Integer> f1 = new EnhancedFutureImpl<Integer>();
     EnhancedFutureImpl<Integer> f2 = new EnhancedFutureImpl<Integer>();
     f1.supplyResult(f2, taskExecutor);
     f2.supplyResult(3, taskExecutor);
     
     Assert.assertEquals(new Integer(3), f1.get(5, TimeUnit.MILLISECONDS));
   }
   
   @Test
   public void futuresShouldNotNest2() throws Exception {
     EnhancedFutureImpl<Integer> f1 = new EnhancedFutureImpl<Integer>();
     EnhancedFutureImpl<Integer> f2 = new EnhancedFutureImpl<Integer>();
     f2.supplyResult(3, taskExecutor);
     f1.supplyResult(f2, taskExecutor);
     
     Assert.assertEquals(new Integer(3), f1.get(5, TimeUnit.MILLISECONDS));
   }
 
  @Test
  public void futureShouldAllowNullResult() throws Exception {
    EnhancedFutureImpl<Integer> f1 = new EnhancedFutureImpl<Integer>();
    f1.supplyResult(null, taskExecutor);

    Assert.assertNull(f1.get(5, TimeUnit.MILLISECONDS));

  }

 }
