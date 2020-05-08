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
 import com.redhat.nitrate.command.Env;
 import com.redhat.nitrate.command.TestRun;
 import java.util.Hashtable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import redstone.xmlrpc.XmlRpcArray;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 public class LinkRunToVarCreate extends CommandWrapper {
 
      static{
         CommandWrapper.enlistWrapper(TestRun.link_env_value.class, new WrapperConstructor() {
 
             public CommandWrapper create(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env) {
                 return new LinkRunToVarCreate(current, result_type, properties, env);
             }
         });
     }
 
     public LinkRunToVarCreate(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env) {
         super(current, result_type, properties, env);
     }
 
     @Override
     public Object getResultIfDuplicate(TcmsConnection connection) {
         try {
             TestRun.get_env_values f = new TestRun.get_env_values();
             TestRun.link_env_value link_val = ((TestRun.link_env_value) current);
             f.run_id = link_val.run_id;
             XmlRpcArray a = (XmlRpcArray) connection.invoke(f);
             for (Object o : a) {
                 Env.Value v = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Env.Value.class);
                 if (v.id.compareTo(link_val.env_value_id) == 0) {
                     return o;
                 }
             }
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
             ((TestRun.link_env_value) current()).run_id = run;
             return true;
         }
         return false;
     }
 
     public void setResult(Object o) {
         if (this.result == null) {
             if (o instanceof XmlRpcStruct) {
                 XmlRpcStruct struct = (XmlRpcStruct) o;
                 result = null;
                 unexpected = o;
                 status = CommandWrapper.Status.EXCEPTION;
                 return;
 
             } else if (o instanceof XmlRpcArray) {
                 XmlRpcArray array = (XmlRpcArray) o;
                 result = null;
                 unexpected = o;
                 status = CommandWrapper.Status.EXCEPTION;
                 return;
             }
 
         }
     }
 
     public Hashtable<String, String> description() {
         Hashtable<String, String> map = current.descriptionMap();
         TestRun.link_env_value f = (TestRun.link_env_value) current;
         Env.Value v = env.getValueById(f.env_value_id);
         if (v != null) {
             map.put("env_value_id", v.value + " (" + map.get("env_value_id") + ")");
         }
         return map;
     }
 
     public String summary() {
         return description().get("env_value_id");
     }
 
     public String toString() {
        return "Link Run to Environmental Variable";
     }
 }
