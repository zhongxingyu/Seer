 package com.meltmedia.cadmium.cli;
 
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 @Parameters(commandDescription = "Toggles on and off maintenance page")
 public class MaintenanceCommand {
 	
 	@Parameter(names="state", description="The state of the maintenance page", required=true)
 	private String state;
 
 	@Parameter(names="site", description="Site whos maintenance page is being Toggled", required=true)
 	private String site;
 
 	@Parameter(names="comment", description="Comment", required=true)
 	private String comment;
 
	public void execute() throws Exception {
 		
		HttpClient client = new HttpClient( );
 		String url = site + "/maintenance";
 		
 		if(state.trim().equalsIgnoreCase("on") || state.trim().equalsIgnoreCase("off")) {
			PostMethod method = new PostMethod(url);
			method.addParameter("state", state.trim());
			method.addParameter("comment", comment);
			client.executeMethod(method);
			String response = method.getResponseBodyAsString();
			System.out.println(response);
			method.releaseConnection();
 		}
 		System.err.println("Invalid State. Please use 'on' or 'off'.");
 	}
 
 }
