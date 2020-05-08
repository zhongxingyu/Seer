 package com.meltmedia.cadmium.cli;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 @Parameters(commandDescription = "Toggles on and off maintenance page", separators="=")
 public class MaintenanceCommand {
 	
 	@Parameter(names="--state", description="The state of the maintenance page", required=true)
 	private String state;
 
 	@Parameter(names="--site", description="Site whos maintenance page is being Toggled", required=true)
 	private String site;
 
 	@Parameter(names="--comment", description="Comment", required=true)
 	private String comment;
 	
	private final String JERSEY_ENDPOINT = "/system/maintenance";
 	
 	public void execute() throws ClientProtocolException, IOException {
 		
 		DefaultHttpClient client = new DefaultHttpClient();
		String url = site + JERSEY_ENDPOINT;
 		
 		if(state.trim().equalsIgnoreCase("on") || state.trim().equalsIgnoreCase("off")) {
 			HttpPost post = new HttpPost(url);
 			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
       nvps.add(new BasicNameValuePair("state", state.trim()));
       nvps.add(new BasicNameValuePair("comment", comment.trim()));
 
       post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
 			HttpResponse response = client.execute(post);
 			System.out.println(response.toString());		
 		} else {
 			System.err.println("Invalid State. Please use 'on' or 'off'.");
 		}
 	}
 
 }
