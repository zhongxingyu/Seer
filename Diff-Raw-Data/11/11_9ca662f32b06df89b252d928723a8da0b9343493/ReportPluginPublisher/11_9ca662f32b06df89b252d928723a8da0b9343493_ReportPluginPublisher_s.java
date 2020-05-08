 
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.report.plugin.results.MatrixBuildTestResults;
 import com.redhat.engineering.jenkins.report.plugin.results.MatrixRunTestResults;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.matrix.MatrixProject;
 import hudson.matrix.MatrixRun;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Recorder;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  *
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginPublisher extends Recorder{
     public final String reportLocationPattern;
 
     
    /**
     * Get location of reports from project configuration page
     */
     @DataBoundConstructor
     public ReportPluginPublisher(String reportLocationPattern){
 	this.reportLocationPattern = reportLocationPattern;
 	
     }
     
     /**
      * Add ReportPluginProjectAction to actions of project if plugin is configured
      * (path to reports is set)
      */
     @Override
     public Collection<? extends Action> getProjectActions(AbstractProject<?,?> project){
 	Collection<Action> actions = new ArrayList<Action>();
 	if(reportLocationPattern != null){
 	    actions.add(new ReportPluginProjectAction(project));
 	}
 	return actions;
     }
     
     /**
      * Declares the scope of the synchronization monitor we expect from outside.
      * 
      * STEP = Executed only after the same step in the previous build is completed.
      * NONE = No external synchronization is performed on this build step.
      */
     public BuildStepMonitor getRequiredMonitorService() {
 	//TODO: check if STEP is really necessary, or if NONE suffices
 	return BuildStepMonitor.STEP;
     }
     
     /**
      * Create MatrixBuildTestResults and add them to build action if necessary
      * (only first matrix run needs to initialize parent matrix build)
      * 
     * @param build
     * @param listener
     * @return 
      */
     @Override
     public boolean prebuild(AbstractBuild<?,?> build, BuildListener listener){
 	/*
 	 * TODO: enhance for other types than multiconf projects
 	 */
 	if(build instanceof MatrixRun){
 	    MatrixRun mrun = (MatrixRun) build;
 	    /*
 	     * If not initialized, create MatrixBuildTestResults, add then to
 	     * ReportPluginBuildAction and add it to build actions
 	     */
	    // FIXME: doesn`t work
	    if((mrun.getParentBuild().getActions(ReportPluginBuildAction.class)) == null) {
 		/*
 		 * MatrixBuildTestResults will store mapping matrix run -> test results
 		 */
 		MatrixBuildTestResults bResults = new MatrixBuildTestResults("");
 		ReportPluginBuildAction action = new 
 			ReportPluginBuildAction(mrun.getParentBuild(), bResults);
 		mrun.getParentBuild().getActions().add(action);
 	    }
 	    return true;
 	}
 	/*
 	 * Publisher should be enabled only for multiconf. projects, so fail
 	 */
 	return false;
     }
     
     /**
      * Locates, checks and saves reports after build finishes, initializes 
      * BuildAction with results and adds it to build actions
      * 
      * @param build	    
      * @param launcher	    
      * @param listener	    
      * @return		    true if build can continue
      * @throws InterruptedException
      * @throws IOException 
      */    
     @Override	    
     public boolean perform(AbstractBuild<?,?> build, Launcher launcher,
 	BuildListener listener) throws InterruptedException, IOException{
 	
 	/* Only for matrix projects now
 	 * TODO: add also for other types of build (like free style projects, 
          * even though already implemented in TestNG plugin and JUnit publish)
 	 */
 	if(!(build instanceof MatrixRun)){
 	    return false;
 	}
 	
 	MatrixRun mrun = (MatrixRun) build;
 	
 	PrintStream logger = listener.getLogger();
 	logger.println("[Report Plugin] Report files processing: START");	
 	logger.println("[Report Plugin] Starting to process Matrix Run.");
 	logger.println("[Report Plugin] Looking for results reports in workspace"
 		+ " using pattern: " + reportLocationPattern);
 	    
 	    
 	FilePath[] paths = locateReports(mrun.getWorkspace(), reportLocationPattern);
 	if (paths.length == 0) {
 	    logger.println("Did not find any matching files.");
 	    //build can still continue
 	    return true;
 	}
 	    
 	/*
 	* filter out the reports based on timestamps. See JENKINS-12187
 	*/
 	paths = checkReports(build, paths, logger);
 
 
 	boolean filesSaved = saveReports(getReportDir(mrun), paths, logger);
 	if (!filesSaved) {
 	    logger.println("Failed to save TestNG XML reports");
 	    return true;
 	}
 
 	MatrixRunTestResults rResults = new MatrixRunTestResults("");
 
 	/*
 	 * Parse results
 	 */
 	try {
 	    rResults = ReportPluginRunAction.loadResults(mrun, logger);
 	} catch (Throwable t) {
 	    /*
 	    * don't fail build if parser barfs, only 
 	    * print out the exception to console.
 	    */
 	    t.printStackTrace(logger);
 	} 
 
 	if (rResults.getTestList().size() > 0) {
 	    
 	    /*
 	    * Add matrix run rResults to parent build`s bResults
 	    */ 
 	    ReportPluginBuildAction action = mrun.getParentBuild().getAction(ReportPluginBuildAction.class);
 	    action.getBuildResults().addMatrixTestResults(mrun, rResults);
 	    if (rResults.getFailedConfigCount() > 0 || rResults.getFailedTestCount() > 0) {
 		mrun.setResult(Result.UNSTABLE);
 	    }
 	} else {
 	    logger.println("Found matching files but did not find any test results.");
 	    return true;
 	} 
 
 	logger.println("[Report Plugin] Finished processing Matrix Run.");	
 	logger.println("[Report Plugin] Report Processing: FINISH");
 	
 	return true;
     }
 
 
     public static FilePath[] locateReports(FilePath workspace, String reportLocationPattern)
     throws IOException, InterruptedException{
 	// First use ant-style pattern
       try {
          FilePath[] ret = workspace.list(reportLocationPattern);
          if (ret.length > 0) {
             return ret;
          }
       } catch (Exception e) {}
 
       // If it fails, do a legacy search
       List<FilePath> files = new ArrayList<FilePath>();
       String parts[] = reportLocationPattern.split("\\s*[;:,]+\\s*");
       for (String path : parts) {
          FilePath src = workspace.child(path);
          if (src.exists()) {
             if (src.isDirectory()) {
                files.addAll(Arrays.asList(src.list("**/testng*.xml")));
             } else {
                files.add(src);
             }
          }
       }
       return files.toArray(new FilePath[files.size()]);
     }
     
     /**
      * Filter out the reports based on timestamps. Those with timestamp earlier 
      * than start of build are to be ignored. See JENKINS-12187
      */
     public static FilePath[] checkReports(AbstractBuild<?,?> build, FilePath[] paths,
             PrintStream logger){
 	List<FilePath> filePathList = new ArrayList<FilePath>(paths.length);
 
 	for (FilePath report : paths) {
 	    /*
 	    * Check that the file was created as part of this build and is not
 	    * something left over from before.
 	    *
 	    * Checks that the last modified time of file is greater than the
 	    * start time of the build
 	    *
 	    */
 	    try {
 		/*
 		* dividing by 1000 and comparing because we want to compare secs
 		* and not milliseconds
 		*/
 		if (build.getTimestamp().getTimeInMillis() / 1000 <= report.lastModified() / 1000) {
 		filePathList.add(report);
 		} else {
 		logger.println(report.getName() + " was last modified before "
 			    + "this build started. Ignoring it.");
 		}
 	    } catch (IOException e) {
 		// just log the exception
 		e.printStackTrace(logger);
 	    } catch (InterruptedException e) {
 		// just log the exception
 		e.printStackTrace(logger);
 	    }
 	}
 	return filePathList.toArray(new FilePath[]{});
     }
     
     /**
     * Gets the directory to store report files
     */
     static FilePath getReportDir(AbstractBuild<?,?> build) {
 	return new FilePath(new File(build.getRootDir(), "report-plugin"));
     }
 
     public static boolean saveReports(FilePath reportDir, FilePath[] paths, PrintStream logger) {
 	logger.println("Saving reports...");
 	try {
 	    reportDir.mkdirs();
 	    int i = 0;
 	    for (FilePath report : paths) {
 		String name = "test-results" + (i > 0 ? "-" + i : "") + ".xml";
 		i++;
 		FilePath dst = reportDir.child(name);
 		report.copyTo(dst);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace(logger);
 	    return false;
 	}
 	return true;
     }
     
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Publisher> {
 
 	
 	// TODO: enable for other types than multiconf projects
 	@Override
 	public boolean isApplicable(Class<? extends AbstractProject> project) {
 	    if(project == MatrixProject.class){
 		return true;
 	    }	
 	    return false;
 	}
 
 	@Override
 	public String getDisplayName() {
 	    return "Publish " + Definitions.__DISPLAY_NAME + " results";
 	}
        
 	// Invoked when global configuration page is submitted
 	@Override
 	public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
 	    save();
 	    return super.configure(req, formData);
 	}
    }
 }
