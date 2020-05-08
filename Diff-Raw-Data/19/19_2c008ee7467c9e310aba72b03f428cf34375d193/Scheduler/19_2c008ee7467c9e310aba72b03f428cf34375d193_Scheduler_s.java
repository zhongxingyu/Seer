 package org.kevoree.macaw.framework.scheduler;
 
 import org.kevoree.framework.message.StdKevoreeMessage;
 
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 16/04/12
  * Time: 13:15
  */
 public interface Scheduler {
 
     public void sendToTester(StdKevoreeMessage msg);
 
    public boolean sendSyncToTester(StdKevoreeMessage msg);
 
    public void waitForResponse(List<Integer> ids);
 
 }
