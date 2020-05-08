 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration;
 
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.*;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
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
     
     private String serverUrl;
     private TcmsAccessCredentials credentials;
     
     public TcmsProperties properties;
     public final TcmsEnvironment environment;
     private Hashtable<String, Hashtable<String, String>> env_status;
     private boolean wrongProperty;
     private HashSet<String> propertyWWrongValue;
     boolean change_axis = false;
 
     public List<String> update_problems = new LinkedList<String>();
     public HashSet<String> env_check_problems = new HashSet<String>();
 
     /* Used to store exception, if occurs, and print it in reasonable format, not
      * ugly long exception
      */
     private String exception;
     
     public boolean isChange_axis() {
         return change_axis;
     }
 
     public String getServerUrl() {
         return serverUrl;
     }
 
     public List<String> getUpdate_problems() {
         return update_problems;
     }
 
     public HashSet<String> getEnv_check_problems() {
         return env_check_problems;
     }
 
     
     
     public String getUsername() {
         return credentials.getUsername();
     }
 
     public String getPassword() {
         return credentials.getPassword();
     }
 
     
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
 
     public Hashtable<String, Hashtable<String, String>> getEnv_status() {
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
     
     public boolean exceptionOccured(){
         return !exception.isEmpty();
     }
     
     public String getException(){
         return exception;
     }
 
     public TcmsReviewAction(AbstractBuild build, String serverUrl, 
             String plan,
             String product,
             String product_v,
             String category,
             String priority,
             String manager,
             String env,
             String testPath) {
 
         this.properties = new TcmsProperties(plan, product, product_v, category, priority, manager);
         this.credentials =  new TcmsAccessCredentials();
        boolean a = credentials.isEmpty();
         this.serverUrl = serverUrl;
         this.environment = new TcmsEnvironment(env);
         this.build = build;
         gatherer = new TcmsGatherer(properties, environment);
         env_status = new Hashtable<String, Hashtable<String, String>>();
         propertyWWrongValue = new HashSet<String>();
         wrongProperty = false;
     }
 
     public void doGather(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException, XmlRpcFault {
 
         gatherer.clear();
        
        credentials.setUsername(req.getParameter("_.username"));
        credentials.setPassword(req.getParameter("_.password"));
        
         connection = new TcmsConnection(serverUrl);
         connection.setUsernameAndPassword(credentials.getUsername(), credentials.getPassword());
 
         try{
             boolean test = connection.testTcmsConnection();
             if (test == false) {
                 throw new IOException("Couln't connect to tcms server");
             }
         } catch(IOException e){
             exception = e.toString();
             rsp.sendRedirect("../" + Definitions.__URL_NAME);
             return;
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
 
 
         gatherer.setProperties(properties);
         
         for (GatherFiles gatherfile : gatherFiles) {
             gatherer.gather(gatherfile.results, build, gatherfile.build, gatherfile.variables);
         }
 
         rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
     public static class GatherFiles {
 
         public TestResults results;
         public AbstractBuild build;
         public Map<String, String> variables;
 
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
 
     public void addGatherPath(TestResults results, AbstractBuild build, Map<String, String> variables) {
         GatherFiles f = new GatherFiles(results, build, variables);
         if (f != null) {
             gatherFiles.add(f);
         }
     }
 
      public void doUpdateSettings(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
                    List<String> problems=new LinkedList<String>();
 
          String serverUrl = req.getParameter("_.serverUrl");
          String username = req.getParameter("_.username");
          String password = req.getParameter("_.password");
          
          if(this.serverUrl.contentEquals(serverUrl) &&
              credentials.getUsername().contentEquals(username)  &&
              credentials.getPassword().contentEquals(password)){
              //do nothing
          }else{
              TcmsConnection c = new TcmsConnection(serverUrl);
              c.setUsernameAndPassword(username, password);
              if(c.testTcmsConnection()){
                  connection = c;
              }else{
                  problems.add("Conection with new url,username and password failed.");
              }
          }
          if(problems.isEmpty()){
          String plan = req.getParameter("_.plan");
          String product = req.getParameter("_.product");
          String product_v = req.getParameter("_.product_v");
          String category = req.getParameter("_.category");
          String priority = req.getParameter("_.priority");
          String manager = req.getParameter("_.manager");
          
          TcmsProperties properties = new TcmsProperties(plan, product, product_v, category, priority, manager);
             String session;
             Auth.login_krbv auth = new Auth.login_krbv();
 
            try {
                 session = auth.invoke(connection);
                 if (session.length() > 0) {
                     connection.setSession(session);
                 }
                 properties.setConnection(connection);
                 properties.reload();
             } catch (XmlRpcFault ex) {
                 problems.add("Possibly wrong username/password");
             }
             if (properties.getPlanID() == null) {
                problems.add(properties.plan + " is possibly wrong plan id");
             }
             if (properties.getProductID() == null) {
                 problems.add(properties.product + " is possibly wrong product name (couldn't check product version and category)");
             }else{
                 if (properties.getProduct_vID() == null) {
                 problems.add(properties.product_v + " is possibly wrong product version");
                 }
                 if (properties.getCategoryID() == null) {
                     problems.add(properties.category + " is possibly wrong category name");
                 }
             }
             
             if (properties.getPriorityID() == null) {
                 problems.add(properties.priority + " is possibly wrong priority name");
             }
             if (properties.getManagerId() == null) {
                 problems.add(properties.manager + " is possibly wrong manager's username");
             }
              if(problems.isEmpty()){
                 this.properties=properties;
             }
          }
            
          this.update_problems = problems;
          rsp.sendRedirect("../" + Definitions.__URL_NAME);
      }
     public void doCheckSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
         HashSet<String> problems=new HashSet<String>();
 
         
         change_axis=false;
         if(req.getParameter("Submit").equals("Change")){
             change_axis=true;
         }
         
         Map params = req.getParameterMap();
       
         /*update values first*/
         for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
             Map.Entry<String,Object> entry =(Map.Entry<String,Object>) it.next();
             if(entry.getKey().startsWith("value-")){
                 String value = ((String[]) entry.getValue())[0];
                 
                 String property_name = entry.getKey().replaceFirst("value-", "");
                 String value_name = property_name.split("=>")[1];
                 property_name = property_name.split("=>")[0];
                 
                 for (GatherFiles env : gatherFiles) {
                     if(env.variables.containsKey(property_name)){
                         if(env.variables.get(property_name).equals(value_name)){
                             env.variables.remove(property_name);
                             env.variables.put(property_name, value);
                         }
                     }
                 }
             }
         }
         
         
         /*change property-names second*/
         for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
             Map.Entry<String,Object> entry =(Map.Entry<String,Object>) it.next();
             if(entry.getKey().startsWith("property-")){
                 String new_property_name = ((String[]) entry.getValue())[0];
                 
                 String property_name = entry.getKey().replaceFirst("property-", "");
                 
                 for (GatherFiles env : gatherFiles) {
                     if(env.variables.containsKey(property_name)){
                         if(env.variables.containsKey(new_property_name)){
                             problems.add("Duplicit property name error."); 
                         }else{
                             String val = env.variables.get(property_name);
                             env.variables.remove(property_name);
                             env.variables.put(new_property_name, val);
                         }
 
                     }
                 }
             }
         }
         
         /*test*/
         try {
             connection = new TcmsConnection(serverUrl);
             connection.setUsernameAndPassword(credentials.getUsername(), credentials.getPassword());
 
             boolean test = connection.testTcmsConnection();
             if (test == false) {
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
             for (Map.Entry<String, String> prop : env.variables.entrySet()) {
                 // check value
                 String name = prop.getKey();
                 String val = prop.getValue();
 
                 String result = "UNKNOWN";
 
                 if (environment.containsProperty(name)) {
                     if (environment.containsValue(name, val)) {
                         result = "CHECKED";
                     } else {
                         result = "VALUE";
                         propertyWWrongValue.add(name);
                     }
                 } else {
                     result = "PROPERTY";
                     wrongProperty = true;
                 }
 
                 if (env_status.containsKey(name) == false) {
                     env_status.put(name, new Hashtable<String, String>());
                 }
                 env_status.get(name).put(val, result);
 
 
             }
         }
 
         this.env_check_problems = problems;
 
         rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
     public void doReportSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
         
         if (req.getParameter("Submit").equals("update")) {
             // update build name
             Build.create buildCreate = (Build.create) gatherer.getCommandList("Build.create").getFirst().current;
             buildCreate.name = req.getParameter("buildName");
 
             // update testRun summary
             TestRun.create testRunCreate = (TestRun.create) gatherer.getCommandList("TestRun.create").getFirst().current;
             testRunCreate.summary = req.getParameter("testRunSummary");
 
             rsp.sendRedirect("../" + Definitions.__URL_NAME);
         } else {
             try {
                 connection = new TcmsConnection(serverUrl);
                 connection.setUsernameAndPassword(credentials.getUsername(),credentials.getPassword());
 
                 boolean test = connection.testTcmsConnection();
                 if (test == false) {
                     throw new IOException("Couln't connect to tcms server");
                 }
 
                 // parse 
                 String input;
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
 
     }
 
     public void upload(TcmsGatherer gathered, TcmsConnection connection) /*
      * throws XmlRpcFault
      */ {
         boolean at_least_one;
         boolean at_least_one_not_duplicate;
         do {
             at_least_one = false;
             at_least_one_not_duplicate = false;
             for (CommandWrapper command : gathered) {
                 if (command.isExecutable()) {
                     if (command.resolved()) { //If dependecnies are satisfied
                         if (command.completed() == false) { // not to run command again
                             if (command.performed() == false) { // this command had satisfied dependecies but failed for some reason, so dont loop o it
                                 boolean tmp = command.perform(connection);
                                 if (tmp) {
                                     at_least_one = true;
                                 }
                                 if (command.duplicate() == false) {
                                     at_least_one_not_duplicate = true;
                                 }
                             }
                         }
                     } else { // dependencies we not met
                         command.setUnmetDependencies();
                     }
                 }
 
             }
         } while (at_least_one && at_least_one_not_duplicate);
     }
 
   
 }
