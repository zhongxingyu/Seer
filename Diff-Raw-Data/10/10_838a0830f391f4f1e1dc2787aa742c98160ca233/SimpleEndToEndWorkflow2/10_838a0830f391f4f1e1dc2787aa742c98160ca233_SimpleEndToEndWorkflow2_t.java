 package org.vpac.grisu.frontend.examples;
 
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.frontend.control.login.LoginException;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.view.cli.CliHelpers;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.info.ApplicationInformation;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public class SimpleEndToEndWorkflow2 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		System.out.println("Logging in...");
 		ServiceInterface si = null;
 		try {
 			si = LoginManager.loginCommandline();
 		} catch (LoginException e) {
 			System.err.println("Could not login: "+e.getLocalizedMessage());
 			System.exit(1);
 		}
 		
 		String file1url = args[0];
 		String file1Name = FileManager.getFilename(file1url);
 		String file2url = args[1];
 		String file2Name = FileManager.getFilename(file2url);
 
 		System.out.println("Creating job...");
 		JobObject job = new JobObject(si);
 		job.setApplication("UnixCommands");
 		job.setTimestampJobname("MyFirstDiffJob");
 		System.out.println("Set jobname to be: "+job.getJobname());
 		job.setCommandline("diff "+file1Name+" "+file2Name);
 		
 		// now we need to add the input files to the job
 		job.addInputFileUrl(file1url);
 		job.addInputFileUrl(file2url);
 
 		job.setWalltimeInSeconds(60);
 		
 		// now ask the user where to submit the job to
 		// for that we rely on the mds inforamation that is published for the application we are using (UnixCommands)
 		// to be correct and up-to-date
 		
 		// let's get an object that contains all the information about the application on the grid
 		ApplicationInformation appInfo = GrisuRegistryManager.getDefault(si).getApplicationInformation("UnixCommands");
 		// we don't care about the version here. it's possible to get that kind of information for a specific version too...
		Set<String> allSubmissionLocations = appInfo.getAvailableSubmissionLocationsForFqan("/ARCS/StartUp");
		appInfo.getAvailableSubmissionLocationsForVersionAndFqan("1.6.0", "/ARCS/StartUp");
 		
 		// now we ask the user (on the commandline) which submission location to use
 		// we give him the option of not specifing one, in which case we rely on grisu to figure out the best one
 		String subLoc = CliHelpers.getUserChoice(allSubmissionLocations, "Auto-select the best one");
 		
 		if ( StringUtils.isNotBlank(subLoc) ) {
 			job.setSubmissionLocation(subLoc);
 		}
 
 		try {
 			System.out.println("Creating job on backend...");
 			job.createJob("/ARCS/StartUp");
 		} catch (JobPropertiesException e) {
 			System.err.println("Could not create job: "+e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		try {
 			System.out.println("Submitting job to the grid...");
 			job.submitJob();
 		} catch (JobSubmissionException e) {
 			System.err.println("Could not submit job: "+e.getLocalizedMessage());
 			System.exit(1);
 		} catch (InterruptedException e) {
 			System.err.println("Jobsubmission interrupted: "+e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		System.out.println("Job submission finished.");
 		System.out.println("Job submitted to: "+job.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 
 		System.out.println("Waiting for job to finish...");
 
 		// for a real workflow, don't check every 5 seconds since that would put too much load on the backend/gateways
 		job.waitForJobToFinish(5);
 
 		System.out.println("Job finished with status: "+job.getStatusString(false));
 
 		System.out.println("Stdout: "+job.getStdOutContent());
 		System.out.println("Stderr: " +job.getStdErrContent());
 
 		// it's pretty important to shutdown the jvm properly. There might be some executors running in the background
 		// and they need to know when to shutdown.
 		// Otherwise, your application might not exit.
 		System.exit(0);
 		
 	}
 
 }
