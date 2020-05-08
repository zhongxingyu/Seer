 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration.CommandWrapper;
 
 import NitrateIntegration.TcmsEnvironment;
 import NitrateIntegration.TcmsProperties;
 import NitrateIntegration.TcmsReviewAction;
 import com.redhat.nitrate.TcmsCommand;
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.TcmsException;
 import com.redhat.nitrate.command.TestRun;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author jrusnack
  */
 public class SetTestRunToFinished extends CommandWrapper{
     
     public SetTestRunToFinished(TcmsCommand current, Class result_type, TcmsProperties properties, TcmsEnvironment env){
         super(current, result_type, properties, env);
     }
 
     @Override
     public Object getResultIfDuplicate(TcmsConnection connection) {
         try {
             TestRun.get get = new TestRun.get();
             get.run_id = ((TestRun.update) current).id;
             XmlRpcStruct o = (XmlRpcStruct) connection.invoke(get);
             if( o.getString("stop_date").isEmpty()) o = null;
             return o;
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsReviewAction.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     @Override
     public boolean processDependecies() {
         int run = -1;
         for (CommandWrapper deps : getDependecies()) {
 
             if (deps.current() instanceof TestRun.create) {
                 TestRun r = deps.getResult(TestRun.class);
                 run = r.run_id;
             }
         }
 
         if (run != -1) {
             ((TestRun.update) current()).id = run;
             return true;
         }
         return false;
     }
 
     @Override
     public String summary() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     @Override
     public String toString(){
         return "Set TestRun status to finished.";
     }
 }
