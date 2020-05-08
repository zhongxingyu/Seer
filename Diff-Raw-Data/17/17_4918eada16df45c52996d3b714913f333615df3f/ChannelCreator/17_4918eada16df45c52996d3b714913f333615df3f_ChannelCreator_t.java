 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class ChannelCreator implements AsyncCallback<String> {
 	
 	private Collaborator collaborator;
 	
 	public ChannelCreator(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 	
 	public void createChannel() {
 		collaborator.statusUpdate("Attemping to set up a new channel" +
 				" with the server.");
 		collaborator.collabService.setUpChannel(this);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		collaborator.statusUpdate("Error setting up channel"
 				+ "; caught exception " + caught.getClass()
 				+ " with message: " + caught.getMessage());
 		GWT.log("Error setting up channel.", caught);
 	}
 
 	@Override
 	public void onSuccess(String token) {
		collaborator.statusUpdate(
				"Channel token: " + token.substring(0, 40) + "...");
 		collaborator.channelToken = token;
 		collaborator.setUpChannel();
 	}
 }
