 package org.uncertweb.et.request;
 
 import java.util.List;
 
 import org.uncertweb.et.parameter.Input;
 import org.uncertweb.et.parameter.Output;
 
 public class ScreeningRequest extends Request {
 	
 	/**
 	 * For all.
 	 * 
 	 */
 	private String serviceURL;
 	private String processIdentifier;
 	
 	private List<Input> inputs;
 	private List<Output> outputs;	
 	
 	/**
 	 * For design.
 	 * 
 	 */
 	private int numTrajectories; // R
 	private int discretisationLevel; // p
 	
 	public ScreeningRequest() {
 		
 	}
 
 	public String getServiceURL() {
 		return serviceURL;
 	}
 
 	public String getProcessIdentifier() {
 		return processIdentifier;
 	}
 
 	public List<Input> getInputs() {
 		return inputs;
 	}
 
 	public List<Output> getOutputs() {
 		return outputs;
 	}
 
 	public int getNumTrajectories() {
 		return numTrajectories;
 	}
 
 	public int getDiscretisationLevel() {
 		return discretisationLevel;
 	}
	
 }
