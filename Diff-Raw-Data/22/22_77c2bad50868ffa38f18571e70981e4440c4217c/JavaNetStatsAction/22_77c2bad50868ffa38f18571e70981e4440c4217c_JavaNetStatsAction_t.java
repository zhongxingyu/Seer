 package hudson.plugins.javanet;
 
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 import static hudson.plugins.javanet.PluginImpl.DAY;
 import hudson.security.Permission;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.QueryParameter;
 import org.apache.commons.io.FileUtils;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import java.net.URL;
 
 /**
  * UI for java.net stats. Added to the project.
  *
  * @author Kohsuke Kawaguchi
  */
 public class JavaNetStatsAction implements Action {
     /**
      * Project that owns this action.
      */
     public final AbstractProject<?,?> project;
 
     /**
      * Java.net project name.
      */
     private String projectName;
 
     private File reportDir;
 
     public JavaNetStatsAction(AbstractProject<?, ?> project, String projectName) {
         this.project = project;
         this.projectName = projectName;
         this.reportDir = getReportDirectory();
     }
 
     public String getProjectName() {
         return projectName;
     }
 
     public void scheduleGeneration() {
         new ReportGenerator(projectName,reportDir).schedule();
     }
 
     public String getIconFileName() {
         return "graph.gif";
     }
 
     public String getDisplayName() {
         return "Java.net Statistics";
     }
 
     public String getUrlName() {
         return "java.net-stats";
     }
 
     public boolean isReportReady() {
         return new File(reportDir,"index.html").exists();
     }
 
     /**
      * Schedules the re-generation of the report if the report is too old.
      */
     public void upToDateCheck() {
         File indexHtml = new File(reportDir,"index.html");
        long diff = System.currentTimeMillis() - indexHtml.lastModified();
        if(!indexHtml.exists() || (diff >7*DAY)) {
             scheduleGeneration();
         }
     }
 
     public URL getIssueStatsIndexHtml() {
         return getClass().getClassLoader().getResource("org/jvnet/its/index.html");
     }
 
     /**
      * Directory in which the report is stored.
      */
     private File getReportDirectory() {
         return new File(Hudson.getInstance().getRootDir(), "java.net/issue-tracker-stats/" + projectName);
     }
 
     /**
      * Serves static files in the report directory.
      */
     public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         String path = req.getRestOfPath().substring(1);
 
         // make sure we are not serving anything strange
         if(!PATH.matcher(path).matches()) {
             rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         rsp.serveFile(req,new File(reportDir,path).toURL());
     }
 
     public void doChangeProject(StaplerRequest req, StaplerResponse rsp,@QueryParameter("name") String name) throws IOException, ServletException {
         project.checkPermission(CONFIGURE);
 
         projectName = name.trim();
         reportDir = getReportDirectory();
         FileUtils.writeStringToFile(getOverrideFile(),projectName,"UTF-8");
 
         rsp.sendRedirect2(".");
     }
 
     /**
      * Manually trigger the regeneration.
      */
     public void doRegenerate(StaplerResponse rsp) throws IOException, ServletException {
         scheduleGeneration();
         rsp.sendRedirect2(".");
     }
 
     /**
      * Returns true if the current user has a permission to reconfigure this action.
      */
     public boolean hasConfigurePermission() {
         return project.hasPermission(CONFIGURE);
     }
 
     /**
      * File that stores the java.net project name, to manually override
      * the default project name inference.
      */
     private File getOverrideFile() {
         return new File(project.getRootDir(),"java.net.projectName");
     }
 
     /*package*/ static String readOverrideFile(AbstractProject<?,?> project) {
         try {
             return FileUtils.readFileToString(new File(project.getRootDir(),"java.net.projectName"),"UTF-8").trim();
         } catch (IOException e) {
             return null;
         }
     }
 
     private static final Pattern PATH = Pattern.compile("[A-Za-z0-9\\-.]+");
 
     /**
      * Permission to change project.
      */
     public static final Permission CONFIGURE = Permission.CONFIGURE;
 }
