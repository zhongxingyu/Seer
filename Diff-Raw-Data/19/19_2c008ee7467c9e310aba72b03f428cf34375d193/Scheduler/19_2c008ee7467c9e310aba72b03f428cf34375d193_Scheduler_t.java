 package org.kevoree.macaw.framework.scheduler;
 
 import org.kevoree.framework.message.StdKevoreeMessage;
import org.macaw.messages.MethodResult;
 
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
 
    public MethodResult sendSyncToTester(StdKevoreeMessage msg);
 
    public MethodResult waitForResponse(List<Integer> ids);

	public void executeKevScriptStatement(String kscript);
 
 }
