 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration;
 
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.Auth;
 import com.redhat.nitrate.TcmsConnection;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import org.apache.commons.io.IOExceptionWithCause;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import redstone.xmlrpc.XmlRpcFault;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsReviewAction implements Action {
 
     public AbstractBuild<?, ?> build;
     private TcmsGatherer gatherer;
     private TcmsConnection connection;
     String serverUrl;
     String username;
     String password;
     public final TcmsProperties properties;
     public final TcmsEnvironment environment;
     private LinkedList<EnvStatus> env_status;
     private boolean wrongProperty;
     private HashSet<String> propertyWWrongValue;
 
     public String getIconFileName() {
         return Definitions.__ICON_FILE_NAME;
     }
 
     public String getDisplayName() {
         return Definitions.__DISPLAY_NAME;
     }
 
     public String getUrlName() {
         return Definitions.__URL_NAME;
     }
 
     public String getPrefix() {
         return Definitions.__PREFIX;
     }
 
     public TcmsGatherer getGatherer() {
         return gatherer;
     }
 
     public LinkedList<EnvStatus> getEnv_status() {
         return env_status;
     }
 
     public boolean existsWrongProperty() {
         return wrongProperty;
     }
 
     public HashSet<String> getPropertyWWrongValue() {
         return propertyWWrongValue;
     }
     
 
     public TcmsEnvironment getEnvironment() {
         return environment;
     }
 
     public AbstractBuild getBuild() {
         return build;
     }
 
     public TcmsReviewAction(AbstractBuild build, String serverUrl, String username, String password,
             String plan,
             String product,
             String product_v,
             String category,
             String priority,
             String manager,
             String env,
             String testPath) {
 
         this.properties = new TcmsProperties(plan, product, product_v, category, priority, manager);
         this.username = username;
         this.password = password;
         this.serverUrl = serverUrl;
         this.environment = new TcmsEnvironment(env);
         this.build = build;
         gatherer = new TcmsGatherer(properties,environment);
         env_status = new LinkedList<EnvStatus>();
         propertyWWrongValue = new HashSet<String>();
         wrongProperty =false;
     }
 
     public void doGather(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException, XmlRpcFault {
         
                 gatherer.clear();
 
                 connection = new TcmsConnection(serverUrl);
                 connection.setUsernameAndPassword(username, password);
                 
                 boolean test = connection.testTcmsConnection();
                 if(test == false){
                     throw new IOException("Couln't connect to tcms server");
                 }
                 
                 
                 Auth.login_krbv auth = new Auth.login_krbv();
                 String session;
                 session = auth.invoke(connection);
                 if (session.length() > 0) {
                     connection.setSession(session);
                 }
                 environment.setConnection(connection);
                 environment.reloadEnvId();
                 
                 properties.setConnection(connection);
                 properties.reload();
 
         
         for (GatherFiles gatherfile : gatherFiles) {
             gatherer.gather(gatherfile.results, build, gatherfile.build,gatherfile.variables);
         }
         
         rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
     public static class GatherFiles {
         public TestResults results;
         public AbstractBuild build;
         public Map<String,String> variables;
 
         public GatherFiles(TestResults results, AbstractBuild build, Map<String, String> variables) {
             this.results = results;
             this.build = build;
             this.variables = variables;
         }
    
     }
     LinkedList<GatherFiles> gatherFiles = new LinkedList<GatherFiles>();
 
     public void clearGatherPaths() {
         gatherFiles.clear();
     }
 
     public void addGatherPath(TestResults results, AbstractBuild build,Map<String,String> variables) {
         GatherFiles f = new GatherFiles(results, build,variables);
         if(f!=null){
             gatherFiles.add(f);
         }
     }
 
     public void doCheckSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
        
 
             try {
                 connection = new TcmsConnection(serverUrl);
                 connection.setUsernameAndPassword(username, password);
                 
                 boolean test = connection.testTcmsConnection();
                 if(test == false){
                     throw new IOException("Couln't connect to tcms server");
                 }
                 
                 Auth.login_krbv auth = new Auth.login_krbv();
                 String session;
                 session = auth.invoke(connection);
                 if (session.length() > 0) {
                     connection.setSession(session);
                 }
                 environment.setConnection(connection);
                 environment.reloadEnvId();
 
             } catch (XmlRpcFault ex) {
                 Logger.getLogger(TcmsReviewAction.class.getName()).log(Level.SEVERE, null, ex);
             } catch (MalformedURLException ex) {
                 Logger.getLogger(TcmsPublisher.class.getName()).log(Level.SEVERE, null, ex);
             }
             env_status.clear();
             propertyWWrongValue.clear();
             wrongProperty = false;
             for (GatherFiles env : gatherFiles) {
                 for (Map.Entry<String,String> prop : env.variables.entrySet()) {
                     /*
                      * check value
                      */
                     String name =prop.getKey();
                     String val=prop.getValue();
                     if (environment.containsProperty(name)) {
                         if (environment.containsValue(name, val)) {
                             env_status.add(new EnvStatus(name, val, "CHECKED"));
                         } else {
                             env_status.add(new EnvStatus(name, val, "VALUE"));
                             propertyWWrongValue.add(name);
                         }
                     } else {
                         env_status.add(new EnvStatus(name, val, "PROPERTY"));
                         wrongProperty = true;
                     }
                 }
             }
         
         rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
    
    
     public void doReportSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
         
         
         
       
         try {
             connection = new TcmsConnection(serverUrl);
             connection.setUsernameAndPassword(username, password);
 
             boolean test = connection.testTcmsConnection();
             if(test == false){
                 throw new IOException("Couln't connect to tcms server");
             }
             
             // parse 
             String input = null;
             for (CommandWrapper c : gatherer) {
                 String a = new Integer(c.hashCode()).toString();
                 input = req.getParameter(a);
                 if (input != null) {
                     c.setExecutable(true);
                     c.setChecked(true);
                 } else {
                     c.setExecutable(false);
                     c.setChecked(false);
                 }
             }
 
             
             Auth.login_krbv auth = new Auth.login_krbv();
             String session;
             session = auth.invoke(connection);
             if (session.length() > 0) {
                 connection.setSession(session);
             }
             properties.setConnection(connection);
             properties.reload();
 
             
             upload(gatherer, connection);
         } catch (XmlRpcFault ex) {
             Logger.getLogger(TcmsReviewAction.class.getName()).log(Level.SEVERE, null, ex);
         } catch (MalformedURLException ex) {
             Logger.getLogger(TcmsPublisher.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         
         rsp.sendRedirect("../" + Definitions.__URL_NAME);
 
 
     }
 
     public void upload(TcmsGatherer gathered, TcmsConnection connection) /*throws XmlRpcFault*/ {
         boolean at_least_one;
         boolean at_least_one_not_duplicate;
         do {
             at_least_one = false;
             at_least_one_not_duplicate = false;
             for (CommandWrapper command : gathered) {
                 if (command.isExecutable()) {
                     if (command.resolved()) { //If dependecnies are satisfied
                         if(command.completed()==false){ // not to run command again
                             if(command.performed()==false){ // this command had satisfied dependecies but failed for some reason, so dont loop o it
                                 boolean tmp = command.perform(connection);
                                 if (tmp) {
                                     at_least_one = true;
                                 }
                                 if (command.duplicate() == false) {
                                     at_least_one_not_duplicate = true;
                                 }
                             }
                         } 
                     }else{ // dependencies we not met
                         command.setUnmetDependencies();
                     }
                 }
                 
             }
         } while (at_least_one && at_least_one_not_duplicate);
     }
 
     public static class EnvStatus {
 
         private final String property;
         private final String value;
         private final String status;
 
         public EnvStatus(String property, String value, String status) {
             this.property = property;
             this.value = value;
             this.status = status;
         }
 
         public String getProperty() {
             return property;
         }
 
         public String getStatus() {
             return status;
         }
 
         public String getValue() {
             return value;
         }
     }
 }
