 package com.meltmedia.cadmium.cli;
 
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
 
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
 
	public void execute() throws ClientProtocolException, IOException {
 		
		DefaultHttpClient client = new DefaultHttpClient();
 		String url = site + "/maintenance";
 		
 		if(state.trim().equalsIgnoreCase("on") || state.trim().equalsIgnoreCase("off")) {
			HttpPost post = new HttpPost(url);
			HttpParams params = new BasicHttpParams(); 
			params.setParameter("state", state.trim());
			params.setParameter("comment", comment.trim());
			HttpResponse response = client.execute(post);
			System.out.println(response.toString());		
 		}
 		System.err.println("Invalid State. Please use 'on' or 'off'.");
 	}
 
 }
