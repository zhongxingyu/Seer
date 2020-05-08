 package org.jenkinsci.plugins.jenkins_force;
 import hudson.*;
 import hudson.model.*;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.util.ListBoxModel;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.tasks.Builder;
 import hudson.tasks.Ant;
 import hudson.tasks.BuildStepDescriptor;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import com.cloudbees.plugins.credentials.CredentialsProvider;
 import org.jenkinsci.plugins.jenkins_force.ForceDotComUserPassword;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author Kohsuke Kawaguchi
  */
 public class ForceDotComBuilder extends Builder {
 
     private final String username;
     private final String task;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public ForceDotComBuilder(String task, String username) {
         this.username = username;
         this.task = task;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getUsername() {
         return username;
     }
     public String getTask() {
         return task;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
         // This is where you 'build' the project.
         // Since this is a dummy, we just say 'hello world' and call that a build.
 
         // This also shows how you can consult the global configuration of the builder
 
         List<ForceDotComUserPassword> creds = CredentialsProvider.lookupCredentials( ForceDotComUserPassword.class );
 
         String un = "";
         String pw = "";
         String env = "login";
 
         for ( ForceDotComUserPassword cred : creds )
         {
             if ( !cred.getId().equals( username ) )
             {
                 continue;
             }
 
             un = cred.getUsername();
             pw = cred.getPassword().getPlainText();
 //            env = cred.getEnvironment();
         }
 
 	String message = "Login to " + un + " for " + task;
         listener.getLogger().println(message);
 
         String properties = "sf.username=" + un + "\n" +
                             "sf.password=" + pw + "\n" +
                             "sf.serverurl=https://" + env + ".salesforce.com";
 
         /*
         String packageContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         packageContents += "<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">";
         packageContents += "<types>";
         packageContents += "<members>*</members>";
         packageContents += "<name>ApexClass</name>";
         packageContents += "</types>";
         packageContents += "</Package>";
 
         FilePath srcDir = build.getModuleRoot().createTempDir( "fdc", "src" );
         FilePath packageFile = srcDir.createTextTempFile( "package", ".xml", packageContents );
         String packageXml = srcDir.getName() + '/' + packageFile.getName();
         */
 
         FilePath propertyFile = build.getModuleRoot().createTextTempFile( "build", ".properties", properties );
 
         String checkOnly = task.equals("validate") ? "true" : "false";
 
         String buildScript = "<project basedir=\".\" xmlns:sf=\"antlib:com.salesforce\">";
         buildScript += "<property file=\"" + propertyFile.getName() + "\"/>";
         buildScript += "<target name=\"" + task + "\">";
         buildScript += "<sf:deploy username=\"${sf.username}\" password=\"${sf.password}\" serverurl=\"${sf.serverurl}\" ";
         buildScript += "pollWaitMillis=\"60000\" maxPoll=\"30\" ";
         buildScript += "deployRoot=\"src\" runAllTests=\"true\" checkOnly=\"" + checkOnly + "\"/>";
 //        buildScript += "retrieveTarget=\"" + srcDir.getName() + "\"  unpackaged=\"" + packageXml + "\"/>";
         buildScript += "</target>";
         buildScript += "</project>";
         FilePath buildFile = build.getModuleRoot().createTextTempFile( "fdcbuild", ".xml", buildScript );
 
         Ant antTask = new Ant(task, "", "", buildFile.getName(), "");
         boolean success = antTask.perform(build, launcher, listener);
 
         propertyFile.delete();
 
         return success;
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String sfmtLocation;
 

         public ListBoxModel doFillUsernameItems() {
             ListBoxModel items = new ListBoxModel();
             items.add( "--Select username--", "" );
 
             List<ForceDotComUser> creds = CredentialsProvider.lookupCredentials( ForceDotComUser.class );
 
             for ( ForceDotComUser cred : creds )
             {
                 items.add( cred.getUsername(), cred.getId() );
             }
 
             return items;
         }
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckUsername(@QueryParameter String value)
                 throws IOException, ServletException {
             if (value.length() == 0)
                 return FormValidation.error("Please set a name");
             if (value.length() < 4)
                 return FormValidation.warning("Isn't the name too short?");
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Build on Force.com";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             sfmtLocation = formData.getString("sfmtLocation");
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         public String getSfmtLocation() {
             return sfmtLocation;
         }
     }
 }
 
