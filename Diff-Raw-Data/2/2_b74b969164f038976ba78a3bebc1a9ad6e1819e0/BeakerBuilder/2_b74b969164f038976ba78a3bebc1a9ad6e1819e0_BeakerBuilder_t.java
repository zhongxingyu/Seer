 package org.jenkinsci.plugins.beakerbuilder;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 
 import org.fedorahosted.beaker4j.beaker.BeakerServer;
 import org.fedorahosted.beaker4j.client.BeakerClient;
 import org.fedorahosted.beaker4j.remote_model.Identity;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class BeakerBuilder extends Builder {
 
     @DataBoundConstructor
     public BeakerBuilder(String name) {
 
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         return true;
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
 
         private String beakerURL;
         private String login;
         private String password;
 
         private transient final BeakerClient beakerClient;
 
         public DescriptorImpl() {
             load();
             beakerClient = BeakerServer.getXmlRpcClient(beakerURL);
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         public String getDisplayName() {
             return "Execute Beaker task";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             req.bindJSON(this, formData);
             save();
             return super.configure(req, formData);
         }
 
         public FormValidation doTestConnection(@QueryParameter("beakerURL") final String beakerURL,
                 @QueryParameter("login") final String login, @QueryParameter("password") final String password) {
             System.out.println("Trying to get client for " + beakerURL);
             BeakerClient bc = BeakerServer.getXmlRpcClient(beakerURL);
             Identity ident = new Identity(login, password, bc);
             try {
                 if (!ident.authenticate())
                     // TODO localization
                     return FormValidation.error("Cannot connect to " + beakerURL + " as " + login);
                 return FormValidation.ok("Connected as " + ident.whoAmI());
             } catch (Exception e) {
                 e.printStackTrace();
                return FormValidation.error("Somethign went wrong, cannot connect to " + beakerURL + ", cause: " + e.getCause());
             }
         }
 
         public String getBeakerURL() {
             return beakerURL;
         }
 
         public void setBeakerURL(String beakerURL) {
             this.beakerURL = beakerURL;
         }
 
         public String getLogin() {
             return login;
         }
 
         public void setLogin(String login) {
             this.login = login;
         }
 
         public String getPassword() {
             return password;
         }
 
         public void setPassword(String password) {
             this.password = password;
         }
 
         public BeakerClient getBeakerClient() {
             return beakerClient;
         }
 
     }
 
 }
