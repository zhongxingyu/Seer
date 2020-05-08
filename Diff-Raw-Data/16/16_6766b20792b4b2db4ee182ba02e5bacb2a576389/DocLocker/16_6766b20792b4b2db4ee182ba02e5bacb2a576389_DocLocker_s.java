 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
  */
 public class DocLocker implements AsyncCallback<Void> {
 
 	private Collaborator collaborator;
 
 	/**
 	 * This function creates a new DocLocker and calls it. This function
 	 * basically allows for multiple requests to be sent at the same time with
 	 * different internal state associated with them.
 	 * @param collaborator
 	 * @param key
 	 * @param side
 	 * @param ind
 	 * @return
 	 */
	public static void lockDoc(Collaborator collab, String key, String side,
			int ind) {
 		DocLocker dl = new DocLocker(collab);
		dl.lockDocument(key, side, ind);
 	}
 
 	public DocLocker(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 
 	/**
 	 * Sends a request to be added to the locked document queue
 	 * @param key
 	 * @param side
 	 * @param index
 	 */
	public void lockDocument(String key, String side, int index) {
 		// this calls the server's method addToDocQueue: essentially
 		// asks the server to enqueue this request for the lock
 		collaborator.collabService.lockDocument(collaborator.clientID, key, this);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		collaborator.statusUpdate("Error entering lock queue");
 		GWT.log("Error entering lock queue.", caught);
 	}
 
 	@Override
 	public void onSuccess(Void result) {
 		collaborator.statusUpdate("In document queue");
 	}
 
 	
 	/**
 	 * If we successfully acquired the locker - can now edit the doc.
 	 * 
 	 * @param result
 	 * @param side
 	 * @param index
 	 */
 	/*
 	protected void gotDoc(LockedDocument result, String side, int index) {
 		if (side.equals("left"))
 			collaborator.setGenericObjects(true);
 		else
 			collaborator.setGenericObjects(false);
 
 		TextBox box = collaborator.titleList.get(index);
 		TextArea area = collaborator.contentsList.get(index);
 
 		collaborator.docList.set(index, result);
 
 		// set the title and contents of this doc to be the current thing on the
 		// page
 		box.setValue(result.getTitle());
 		area.setText(result.getContents());
 
 		// the user can now edit the title and the contents of this doc
 		box.setEnabled(true);
 		area.setEnabled(true);
 
 		// we need save, removeTab, and refresh buttons
 		collaborator.hPanel.clear();
 		collaborator.hPanel.add(collaborator.saveDocButton);
 		collaborator.hPanel.add(collaborator.refresh);
 		collaborator.hPanel.add(collaborator.removeTabButton);
 
 		collaborator.saveDocButton.setEnabled(true);
 		collaborator.removeTabButton.setEnabled(true);
 		collaborator.refresh.setEnabled(false);
 	}
 	*/
 }
