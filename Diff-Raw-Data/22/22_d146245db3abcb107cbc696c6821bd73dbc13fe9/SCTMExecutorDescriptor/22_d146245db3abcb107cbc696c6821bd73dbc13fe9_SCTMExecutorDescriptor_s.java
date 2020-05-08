 package hudson.plugins.sctmexecutor;
 
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.plugins.sctmexecutor.validators.EmptySingleFieldValidator;
 import hudson.plugins.sctmexecutor.validators.NumberCSVSingleFieldValidator;
 import hudson.plugins.sctmexecutor.validators.NumberSingleFieldValidator;
 import hudson.plugins.sctmexecutor.validators.TestConnectionValidator;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  * 
  * @author Thomas Fuerer
  * 
  */
 public final class SCTMExecutorDescriptor extends Descriptor<Builder> {
   private String serviceURL;
   private String user;
   private String password;
 
   SCTMExecutorDescriptor() {
     super(SCTMExecutor.class);
     load();
   }
 
   @Override
   public String getDisplayName() {
    return ""; //$NON-NLS-1$
   }
 
   @Override
   public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
    String execDefIds = formData.getString("execDefId"); //$NON-NLS-1$
     String str = formData.getString("projectId"); //$NON-NLS-1$
     int projectId = Integer.parseInt(str);
     return new SCTMExecutor(projectId, execDefIds);
   }
 
   @Override
   public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
     serviceURL = json.getString("serviceURL"); //$NON-NLS-1$
     user = json.getString("user"); //$NON-NLS-1$
     password = PwdCrypt.encode(json.getString("password"), Hudson.getInstance().getSecretKey()); //$NON-NLS-1$
 
     save();
     return super.configure(req, json);
   }
 
   public void setServiceURL(String serviceURL) {
     this.serviceURL = serviceURL;
   }
 
   public String getServiceURL() {
     return serviceURL;
   }
 
   public String getUser() {
     return user;
   }
 
   public String getPassword() {
     return PwdCrypt.decode(password, Hudson.getInstance().getSecretKey());
   }
 
   public void setUser(String user) {
     this.user = user;
   }
 
   public void setPassword(String password) {
     this.password = password;
   }
 
   public void doCheckServiceURL(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("value") final String value)
       throws IOException, ServletException {
     new EmptySingleFieldValidator(value).process();
   }
 
   public void doCheckUser(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("value") final String value)
       throws IOException, ServletException {
     new EmptySingleFieldValidator(value).process();
   }
 
   public void doCheckPassword(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("value") final String value)
       throws IOException, ServletException {
     new EmptySingleFieldValidator(value).process();
   }
 
   public void doCheckExecDefIds(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("value") final String value)
       throws IOException, ServletException {
     new NumberCSVSingleFieldValidator(value).process();
   }
 
   public void doCheckProjectId(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("value") final String value)
       throws IOException, ServletException {
     new NumberSingleFieldValidator(value).process();
   }
 
   public void doTestConnection(StaplerRequest req, StaplerResponse rsp, 
       @QueryParameter("serviceURL") final String serviceURL,
       @QueryParameter("user") final String user,
       @QueryParameter("password") final String password) throws IOException, ServletException {
     new TestConnectionValidator(serviceURL, user, password).process();
   }
 }
