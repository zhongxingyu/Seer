 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 
 /**
  * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
  */
 public class DocLocker implements AsyncCallback<LockedDocument> {
 
 	private Collaborator collaborator;
 	String side;         // is the current doc on the left or the right?
 	private int index;   // which tab is the current doc on?
 
 	public static void lockDoc(Collaborator collab, String key, String side, int ind) {
 		DocLocker dl = new DocLocker(collab);
 		dl.lockDocument(key, side, ind);
 	}
 
 	public DocLocker(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 
 	public void lockDocument(String key, String side, int index) {
 		collaborator.statusUpdate("Attempting to lock document.");
 		collaborator.waitingKey = key;
 		collaborator.collabService.lockDocument(key, this);
		this.side = side;
		this.index = index;
 		//collaborator.lockButton.setEnabled(false);
 
 		// disable the 'lock doc' button for the correct side
 		if (side.equals("left"))
 			collaborator.lockButtonL.setEnabled(false);
 		else if (side.equals("right"))
 			collaborator.lockButtonR.setEnabled(false);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		if (caught instanceof LockUnavailable) {
 			collaborator.statusUpdate("LockUnavailable: " + caught.getMessage());
 		} else {
 			collaborator.statusUpdate("Error retrieving lock"
 					+ "; caught exception " + caught.getClass()
 					+ " with message: " + caught.getMessage());
 			GWT.log("Error getting document lock.", caught);
 		}
 
 		// enable lock button for the correct side
 		if (side.equals("left"))
 			collaborator.lockButtonL.setEnabled(true);
 		else if (side.equals("right"))
 			collaborator.lockButtonR.setEnabled(true);
 	}
 
 	@Override
 	public void onSuccess(LockedDocument result) {
 		if (result.getKey().equals(collaborator.waitingKey)) {
 			collaborator.statusUpdate("Lock retrieved for document.");
 			gotDoc(result, side, index);
 		} else {
 			collaborator.statusUpdate("Got lock for document which is "
 					+ "no longer active.  Releasing lock.");
 			collaborator.releaser.releaseLock(result);
 		}
 	}
 
 
 	protected void gotDoc(LockedDocument result, String side, int index) {
 		TextBox box = null;
 		RichTextArea area = null;
 
 		if (side.equals("left")) {
 			collaborator.documentsLeftList.set(index, result);
 			box = collaborator.titleL.get(index);
 			area = collaborator.contentsL.get(index);
 		} else if (side.equals("right")) {
 			collaborator.documentsRightList.set(index, result);
 			box = collaborator.titleR.get(index);
 			area = collaborator.contentsR.get(index);
 		} 			
 
 		// set the title and contents of this doc to be the current thing on the page
 		box.setValue(result.getTitle());
 		area.setHTML(result.getContents());
 
 		// the user can now edit the title and the contents of this doc
 		box.setEnabled(true);
 		area.setEnabled(true);
 
 		//collaborator.refreshDoc.setEnabled(false);
 
 		// enable buttons
 		// refresh doc is disabled for both
 		if (side.equals("left"))
 		{
 			collaborator.saveButtonL.setEnabled(true);
 			collaborator.lockButtonL.setEnabled(false);
 		}
 		else if (side.equals("right"))
 		{
 			collaborator.saveButtonR.setEnabled(true);
 			collaborator.lockButtonR.setEnabled(false);
 		}
 
 	}
 
 }
 
