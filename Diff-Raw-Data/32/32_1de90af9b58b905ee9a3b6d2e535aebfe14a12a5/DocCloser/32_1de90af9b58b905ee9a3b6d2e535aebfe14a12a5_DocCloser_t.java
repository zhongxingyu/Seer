 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Used to remove a client form the document queue
  */
 public class DocCloser implements AsyncCallback<Void> {
 
 	private Collaborator collaborator;
 
 	/**
 	 * Constructor for DocCloser. Note that there is no internal state and
 	 * thus no static constructor method is necessary.
 	 * 
 	 * @param collaborator
 	 */
 	public DocCloser(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 
 	
 	public void removeFromServerQueue(String docKey) {
 		// remove this doc from our request queue
 		collaborator.collabService.leaveLockQueue(collaborator.clientID,
 				docKey, this);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		collaborator.statusUpdate("Error removing client from queue"
 				+ "; caught exception " + caught.getClass() + " with message: "
 				+ caught.getMessage());
 		GWT.log("Error removing client from queue.", caught);
 	}
 
 	@Override
 	public void onSuccess(Void result) {
		collaborator.statusUpdate("Removed from queue");
		
 	}
 
 }
