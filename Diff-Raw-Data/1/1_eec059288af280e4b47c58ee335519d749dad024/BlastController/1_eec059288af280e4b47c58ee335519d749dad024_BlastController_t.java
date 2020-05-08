 package org.bestgrid.control;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.frontend.model.job.JobObject;
 import grisu.jcommons.constants.Constants;
 import grisu.model.GrisuRegistryManager;
 
 import java.io.*;
 
 import org.bestgrid.model.BlastModel;
 import org.bestgrid.view.BlastView;
 
 public class BlastController {	
 	private ServiceInterface si;
 	
 	public BlastController(final BlastModel myModel, final BlastView myView, String[] aCommand) {
 		myModel.setModel(aCommand);	
 			while(true) {
 					try {
 						//getting the command from command line
 						BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 						String line = reader.readLine();
 						String[] command = line.split("\\s");
 						
 						//send commands to the model
 						myModel.setModel(command);
 						myView.setView(myModel.getModel());
 
 						if (line.equals("submit")) {
 							submit(myModel);
 							break;
 						}
 						
 						/*
 						char c = (char) System.in.read();
 						if(c != '\n' && c != '\r') {
 							myModel.setModel(c);
 							myView.setView(myModel.getModel());		
 						}*/
 					}
 					catch(Exception e) {
 						System.out.println(e.getMessage());
 						e.printStackTrace();
 					}
 				}
 		}
 
 	public ServiceInterface getServiceInterface() {
 		return this.si;
 	}
 	
 	public void setServiceInterface(ServiceInterface si) {
 		this.si = si;
 	}
 	
 	private void submit(BlastModel myModel) {
 		System.out.println("Creating job...");
 		//myModel.setServiceInterface(this.si);
 		myModel.setCommandline();
 		JobObject job = myModel.createJobObject();
 		job.setSubmissionLocation("grid_linux:ng2hpc.canterbury.ac.nz#Loadleveler");
 		
 		try {
 			System.out.println("Creating job on backend...");
 			job.createJob("/ARCS/BeSTGRID");
 		} catch (JobPropertiesException e) {
 			System.err.println("Could not create job: "
 					+ e.getLocalizedMessage());
 			System.exit(1);
 		}
     	
 		try {
 			System.out.println("Submitting job to the grid...");
 			job.submitJob();
 		} catch (JobSubmissionException e) {
 			System.err.println("Could not submit job: "
 					+ e.getLocalizedMessage());
			e.printStackTrace();
 			System.exit(1);
 		} catch (InterruptedException e) {
 			System.err.println("Jobsubmission interrupted: "
 					+ e.getLocalizedMessage());
 			System.exit(1);
 		}
 		
 		System.out.println("Job submission finished.");
 		System.out.println("Job submitted to: "
 				+ job.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 
 		System.out.println("Waiting for job to finish...");
 
 		// for a realy workflow, don't check every 5 seconds since that would
 		// put too much load on the backend/gateways
 		job.waitForJobToFinish(5);
 
 		System.out.println("Job finished with status: "
 				+ job.getStatusString(false));
 
 		System.out.println("Stdout: " + job.getStdOutContent());
 		System.out.println("Stderr: " + job.getStdErrContent());
 
 		// it's good practise to shutdown the jvm properly. There might be some
 		// executors running in the background
 		// and they need to know when to shutdown.
 		// Otherwise, your application might not exit.
 		System.exit(0);
 	}
 }
