 package com.atlassian.refapp.ctk;
 
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
 
 import java.util.Collection;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * A mocked upgrade task.
  */
 public class MockedUpgradeTask implements PluginUpgradeTask
 {
     private AtomicInteger calledCount = new AtomicInteger(0);
 
     public int getCalledCount()
     {
         return calledCount.get();
     }
 
     public void reset()
     {
         if (!calledCount.weakCompareAndSet(1, 0))
         {
             throw new IllegalStateException(this.getClass().getName() + " doesn't get called as often as expected");
         }
     }
 
     public Collection<Message> doUpgrade() throws Exception
     {
         // avoid false positive if it gets called concurrently.
         calledCount.incrementAndGet();
         return null;
     }
 
     public int getBuildNumber()
     {
         return 1;
     }
 
     public String getShortDescription()
     {
         return "wahaha";
     }
 
     public String getPluginKey()
     {
        return "com.atlassian.sal.ctk";
     }
 }
