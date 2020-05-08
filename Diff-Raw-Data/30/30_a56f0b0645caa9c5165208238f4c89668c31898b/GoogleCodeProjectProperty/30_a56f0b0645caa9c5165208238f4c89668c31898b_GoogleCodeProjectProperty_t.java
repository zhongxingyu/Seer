 package hudson.plugins.googlecode;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Job;
 import hudson.model.JobProperty;
 import hudson.model.JobPropertyDescriptor;
 import hudson.scm.SubversionChangeLogSet.LogEntry;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Property for {@link AbstractProject} that stores the associated Google Code website URL.
  *
  * @author Kohsuke Kawaguchi
  * @author Erik Ramfelt
  */
 public final class GoogleCodeProjectProperty extends JobProperty<AbstractProject<?,?>> {
 
     /**
      * Google Code website URL that this project uses.
      *
      * This value is normalized and therefore it always ends with '/'.
      * Null if this is not configured yet.
      */
     public final String googlecodeWebsite;
     
     private transient String projectName;
 
     @DataBoundConstructor
     public GoogleCodeProjectProperty(String googlecodeWebsite) {
         // normalize
         if(googlecodeWebsite==null || googlecodeWebsite.length()==0)
             googlecodeWebsite=null;
         else {
             if(!googlecodeWebsite.endsWith("/"))
                 googlecodeWebsite += '/';
         }
         this.googlecodeWebsite = googlecodeWebsite;
     }
 
     /**
      * Returns the project name for the google code property
      * "Project's name must consist of a lowercase letter, followed by lowercase letters, digits, 
      * and dashes, with no spaces." 
      * @return the project name
      */
     public String getProjectName() {
         if (projectName == null) {
             Matcher matcher = Pattern.compile(".*\\/p\\/([\\w-]*)").matcher(googlecodeWebsite);
             matcher.find();
             projectName = matcher.group(1);
         }
         return projectName;
     }
 
     /**
      * Returns the subversion root URL for this Google Code project.
      * @return string containing a Subversion URL
      */
     public String getSubversionRootUrl() {
        return "https://" + getProjectName() + ".googlecode.com/svn/";
     }
 
     @Extension
     public static final class DescriptorImpl extends JobPropertyDescriptor {
 
         public DescriptorImpl() {
             super(GoogleCodeProjectProperty.class);
             load();
         }
 
         @Override
         public boolean isApplicable(Class<? extends Job> jobType) {
             return AbstractProject.class.isAssignableFrom(jobType);
         }
 
         @Override
         public String getDisplayName() {
             return "Associated Google Code website";
         }
 
         @Override
         public GoogleCodeProjectProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindJSON(GoogleCodeProjectProperty.class, formData);
         }
     }
 
     /**
      * Interface needed to remove the dependency of AbstractBuild and Project classes which
      * are quite difficult to mock out.
      */
     public interface PropertyRetriever {
         GoogleCodeProjectProperty getProperty(AbstractBuild<?,?> build);
         GoogleCodeProjectProperty getProperty(LogEntry entry);
     }
     
     public static class PropertyRetrieverImpl implements PropertyRetriever {
         public GoogleCodeProjectProperty getProperty(AbstractBuild<?, ?> build) {
             return build.getProject().getProperty(GoogleCodeProjectProperty.class);
         }
 
         public GoogleCodeProjectProperty getProperty(LogEntry entry) {
             return getProperty(entry.getParent().build);
         }   
     }
 }
