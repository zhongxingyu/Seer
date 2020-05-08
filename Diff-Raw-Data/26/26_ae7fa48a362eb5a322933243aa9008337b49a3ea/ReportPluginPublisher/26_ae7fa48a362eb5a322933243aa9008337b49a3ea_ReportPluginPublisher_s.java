 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Jan Rusnacko (jrusnack at redhat dot com)
  */
 
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.report.plugin.parser.Parser;
 import com.redhat.engineering.jenkins.report.plugin.results.MatrixBuildTestResults;
 import com.redhat.engineering.jenkins.report.plugin.results.MatrixRunTestResults;
 import com.redhat.engineering.jenkins.report.plugin.results.TestResults;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.matrix.MatrixProject;
 import hudson.matrix.MatrixRun;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Recorder;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.UUID;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Parts of code were reused from TestNG plugin (credits due to its authors)
  * 
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginPublisher extends Recorder{
     public final String reportLocationPattern;
     public ReportPluginProjectAction projectAction;
 
     
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
 	if(reportLocationPattern != null && project instanceof MatrixProject){
 	    projectAction = new ReportPluginProjectAction((MatrixProject)project);
 	    actions.add(projectAction);
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
 	return BuildStepMonitor.STEP;
     }
     
     /**
      * Create MatrixBuildTestResults and add them to build action if necessary
      * (only first matrix run needs to initialize parent matrix build)
      * 
      */
     @Override
     public boolean prebuild(AbstractBuild<?,?> build, BuildListener listener){
 	if(build instanceof MatrixRun){
 	    MatrixRun mrun = (MatrixRun) build;
 	    /*
 	     * If not initialized, create MatrixBuildTestResults, add them to
 	     * ReportPluginBuildAction and add it to build actions
 	     */
 	    if(! projectAction.getTestAggregator().containsKey(mrun.getParentBuild())) {
 		/*
 		 * MatrixBuildTestResults will store mapping matrix run -> test results
 		 */
		MatrixBuildTestResults bResults = new MatrixBuildTestResults(UUID.randomUUID().toString());
                
 		projectAction.getTestAggregator().addBuildResults(mrun.getParentBuild(), bResults);
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
 	
 	if(!(build instanceof MatrixRun)){
 	    return false;
 	}
 	
 	MatrixRun mrun = (MatrixRun) build;
 	
 	PrintStream logger = listener.getLogger();
 	logger.println("[Report Plugin] Report files processing: START");	
 	logger.println("[Report Plugin] Starting to process Matrix Run.");
 	logger.println("[Report Plugin] Looking for results reports in workspace"
 		+ " using pattern: " + reportLocationPattern);
 	    
 	    
 	FilePath[] paths = Parser.locateReports(mrun.getWorkspace(), reportLocationPattern);
 	if (paths.length == 0) {
 	    logger.println("Did not find any matching files.");
 	    //build can still continue
 	    return true;
 	}
 	    
 	/*
 	* filter out the reports based on timestamps. See JENKINS-12187
 	*/
 	paths = Parser.checkReports(build, paths, logger);
 
 
 	boolean filesSaved = Parser.saveReports(Parser.getReportDir(mrun), paths, logger, "test-results");
 	if (!filesSaved) {
 	    logger.println("Failed to save TestNG XML reports");
 	    return true;
 	}
 
 	TestResults rResults = new MatrixRunTestResults(UUID.randomUUID().toString());
 
 	/*
 	 * Parse results
 	 */
 	try {
 	    rResults = Parser.loadResults(mrun, logger, "test-results");
 	} catch (Throwable t) {
 	    /*
 	    * don't fail build if parser barfs, only 
 	    * print out the exception to console.
 	    */
 	    t.printStackTrace(logger);
 	} 
 
 	if (rResults.getTestList().size() > 0) {
 	    /*
 	     * Set owner
 	     */
 	    rResults.setOwner(mrun);
 	    
 	    /*
 	     * Add matrix run rResults to parent build`s bResults
 	     */ 
             MatrixBuildTestResults bResults = projectAction.getTestAggregator().getBuildResults(mrun.getParentBuild());
             bResults.addMatrixRunTestResults(mrun.toString(), mrun.getParent().getCombination(), rResults);
             rResults.setParent(bResults);
             	    
 	    if (rResults.getFailedTestCount() > 0) {
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
     
     
     
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Publisher> {
 
 	/**
 	 * Perform validation of 'reportLocationPattern' field
 	 * 
 	 * @param val	    location to be checked
 	 * @return 
 	 */
 	public FormValidation doCheckReportLocationPattern(@QueryParameter String val){
 	    return FormValidation.ok();
 	}
 	
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
