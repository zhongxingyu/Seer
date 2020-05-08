 package hudson.plugins.jswidgets;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * This class implements the JS widgets pages for a job.
  * 
  * @author mfriedenhagen
  */
 public class JsJobAction extends JsBaseAction {
 
     /** The Logger. */
     private static final Logger LOG = Logger.getLogger(JsJobAction.class.getName());
 
     /** The project. */
     private final transient AbstractProject<?, ?> project;
 
     /**
      * @param project
      *            the job for which the health report will be generated.
      */
     public JsJobAction(AbstractProject<?, ?> project) {
         this.project = project;
         // add the JsBuildAction to all run builds, JsRunListener will append this to the others.
         final List<?> builds = (List<?>) project.getBuilds();
         for (Object object : builds) {
             final AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) object;
             final List<JsBuildAction> jsBuildActions = build.getActions(JsBuildAction.class);
             if (jsBuildActions.size() == 0) {                
                 final JsBuildAction jsBuildAction = new JsBuildAction(build);
                 build.addAction(jsBuildAction);
                 LOG.fine("Adding " + jsBuildAction + " to " + build);
             } else {
                 LOG.fine(build + " already has " + jsBuildActions);
             }
             LOG.fine(build + ":" + build.getActions());
         }
 
     }
 
     /**
      * Returns the job for which the health report will be generated.
      * 
      * @return job
      */
     public AbstractProject<?, ?> getProject() {
         return project;
     }
 
     /**
     * Returns the description of the job without line feeds as this will break the Javascript output.
      * 
      * @return the description in one line.
      */
    public String getJobDescription() {
        return project.getDescription().replace("\n", "").replace("\r", "");
     }
 
 }
