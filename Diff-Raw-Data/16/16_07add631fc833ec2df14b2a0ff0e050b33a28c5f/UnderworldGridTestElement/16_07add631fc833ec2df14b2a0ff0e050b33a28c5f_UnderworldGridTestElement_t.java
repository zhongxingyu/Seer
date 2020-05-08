 package org.vpac.grisu.client.gridTests;
 
 import java.io.File;
 import java.util.Arrays;
 
 import org.vpac.grisu.client.model.JobObject;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.MdsInformationException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 
 public class UnderworldGridTestElement extends GridTestElement {
 
 	public UnderworldGridTestElement(ServiceInterface si, String version,
 			String submissionLocation) throws MdsInformationException {
 		super(si, version, submissionLocation);
 	}
 
 	
 	@Override
 	protected boolean checkJobSuccess() {
 
 //		if ( JobConstants.DONE == this.jobObject.getStatus(true) ) {
 //			addMessage("Status checked. Equals \"Done\". Good");
 //			return true;
 //		} else {
 //			addMessage("Status checked. Status is \""+jobObject.getStatus(false)+". Not good.");
 //			return false;
 //		}
 		
 		String jobDir = null;
 		try {
 			jobDir = serviceInterface.getJobProperty(jobObject.getJobname(), ServiceInterface.JOBDIRECTORY_KEY);
 		} catch (NoSuchJobException e) {
 			addMessage("Could not find job. This is most likely a globus/grisu problem...");
 			setPossibleExceptionForCurrentStage(e);
 			return false;
 		}
 		
 		String[] children = null;
 		try {
 			children = serviceInterface.getChildrenFiles(jobDir+"/output", false);
 			addMessage("Listing output directory: ");
//			StringBuffer listing = new StringBuffer();
//			for ( String child : children ) {
//				listing.append(child+"\n");
//			}
//			addMessage(listing.toString());
 		} catch (Exception e) {
 			addMessage("Could not get children of output directory.");
 			setPossibleExceptionForCurrentStage(e);
 			return false;
 		}
 		
 		if ( Arrays.binarySearch(children, jobDir+"/output/FrequentOutput.dat") >= 0 ) {
 			addMessage("\"FrequentOutput.dat\" file found. Good. Means job ran successful.");
 			return true;
 		} else {
 			addMessage("\"FrequentOutput.dat\" file not found. Means job didn't ran successful.");
 			
 			return false;
 		}
 		
 	}
 
 	@Override
 	protected JobObject createJobObject() throws MdsInformationException {
 
 		JobObject jo = new JobObject(serviceInterface);
 		
 		jo.setApplication(this.application);
 		jo.setApplicationVersion(this.version);
 		jo.setWalltimeInSeconds(60);
 		
 		jo.setCommandline("Underworld ./RayleighTaylorBenchmark_1.2.0.xml");
 		jo.addInputFileUrl(GridTestController.GridTestDirectory+File.separator+"RayleighTaylorBenchmark_1.2.0.xml");
 		
 		return jo;
 		
 	}
 
 	@Override
 	protected String getApplicationSupported() {
 		return "Underworld";
 	}
 
 }
