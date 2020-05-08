 package org.jenkinsci.plugins.customartifactbuilder;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Notifier;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 /*
  * A custom artifact deployer.
  * 
  * This class is in charge of the building.
  * 
  * @author Brian Cain
  */
 public class CustomArtifactDeployerBuilder extends Builder {
 
     private final String file;
     private final String filedir;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public CustomArtifactDeployerBuilder(String file, String filedir) {
         this.file = file;
         this.filedir = filedir;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
    /*public String getFile() {
         return file;
     }
     
     public String getFiledir(){
     	return filedir;
    }*/
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
         // This is where you 'build' the project.
         // Since this is a dummy, we just say 'hello world' and call that a build.
 
         // This also shows how you can consult the global configuration of the builder
 //        if (getDescriptor().getUseFrench())
 //            listener.getLogger().println("Bonjour, "+name+"!");
 //        else
 //            listener.getLogger().println("Hello, "+name+"!");
         	
         listener.getLogger().println("[CustomArtifactDeployer] - Welcome to the custom artifact deployer plugin.");
         listener.getLogger().println("[CustomArtifactDeployer] - The file you have picked is: " + file + ".");
         listener.getLogger().println("[CustomArtifactDeployer] - The file directory you have picked is: " + filedir + ".");
         
         final FilePath workspace = build.getWorkspace();
         boolean deploy = processDeployment(build, launcher, listener, workspace);
         
         if (!deploy){
         	listener.getLogger().println("[CustomArtifactDeployer] - Deployment failed.");
         	return false;
         }
         else{
         	listener.getLogger().println("[CustomArtifactDeployer] - Deployment worked!");
         }
        
         return true;
     }
     
     private boolean processDeployment(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, FilePath workspace) throws InterruptedException {
     	//Creating the remote directory
         //listener.getLogger().println("[CustomArtifactDeployer] - The given path is:\n" + workspace);
     	
     	if (filedir == null){
     		listener.getLogger().println("[CustomArtifactDeployer] - A directory must be set.");
     		return false;
     	}
         
     	//Creating the remote directory
     	listener.getLogger().println("[CustomArtifactDeployer] - Begin file directory creation.");
         final FilePath outputFilePath = new FilePath(workspace.getChannel(), filedir);
         try {
             outputFilePath.mkdirs();
         } catch (IOException ioe) {
         	listener.getLogger().println("[CustomArtifactDeployer] - Making dir fails.");
         	return false;
         }
         
         //Deleting files to remote directory if necessary
         /*boolean deletedPreviously = entry.isDeleteRemote();
         if (deletedPreviously) {
             try {
                 outputFilePath.deleteContents();
             } catch (IOException ioe) {
                 throw new ArtifactDeployerException(String.format("Can't delete contents of '%s'", outputPath), ioe);
             }
         }*/
         
     	return true;
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
         private boolean useFrench;
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckName(@QueryParameter String value)
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
             return "Custom Artifact Deployer Builder";
         }
 
        /*@Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             useFrench = formData.getBoolean("useFrench");
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
        }*/
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         public boolean getUseFrench() {
             return useFrench;
         }
     }
 }
