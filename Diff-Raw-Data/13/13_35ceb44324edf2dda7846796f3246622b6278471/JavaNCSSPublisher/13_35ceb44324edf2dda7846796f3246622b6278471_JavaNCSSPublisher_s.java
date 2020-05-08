 package hudson.plugins.javancss;
 
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Descriptor;
 import hudson.plugins.helpers.AbstractPublisherImpl;
 import hudson.plugins.helpers.Ghostwriter;
 import hudson.tasks.Publisher;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * TODO javadoc.
  *
  * @author Stephen Connolly
  * @since 08-Jan-2008 21:24:06
  */
 public class JavaNCSSPublisher extends AbstractPublisherImpl {
 
     private String reportFilenamePattern;
 
     @DataBoundConstructor
     public JavaNCSSPublisher(String reportFilenamePattern) {
         reportFilenamePattern.getClass();
         this.reportFilenamePattern = reportFilenamePattern;
     }
 
     public String getReportFilenamePattern() {
         return reportFilenamePattern;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean needsToRunAfterFinalized() {
         return false;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * {@inheritDoc}
      */
     public Descriptor<Publisher> getDescriptor() {
         return DESCRIPTOR;
     }
 
     /**
      * {@inheritDoc}
      */
     public Action getProjectAction(AbstractProject<?, ?> project) {
         return new JavaNCSSProjectIndividualReport(project);
     }
 
     protected Ghostwriter newGhostwriter() {
         return new JavaNCSSGhostwriter(reportFilenamePattern);
     }
 
    private static final class DescriptorImpl extends Descriptor<Publisher> {
 
         /**
          * Do not instantiate DescriptorImpl.
          */
         private DescriptorImpl() {
             super(JavaNCSSPublisher.class);
         }
 
         /**
          * {@inheritDoc}
          */
         public String getDisplayName() {
             return PluginImpl.DISPLAY_NAME;
         }
 
         public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindJSON(JavaNCSSPublisher.class, formData);
         }
     }
 
 }
