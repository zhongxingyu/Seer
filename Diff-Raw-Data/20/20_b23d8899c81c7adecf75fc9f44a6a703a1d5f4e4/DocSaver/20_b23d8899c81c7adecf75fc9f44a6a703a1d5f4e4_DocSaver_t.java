 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 public class DocSaver implements AsyncCallback<UnlockedDocument> {
 
 	private Collaborator collaborator;
 	private LockedDocument lockedDocument;
 	private String side; // is the current doc on the left or the right?
 	private int index; // which tab is the current doc on?
 	private final static int maxStrLen = 25;
 
 	/**
 	 * This function creates a new DocSaver and calls it. This function
 	 * basically allows for multiple requests to be sent at the same time with
 	 * different internal state associated with them.
 	 * 
 	 * @param col
 	 *            The collaborator
 	 * @param ld
 	 *            The doc we want to save
 	 * @param side
 	 *            Which side the doc is on
 	 * @param ind
 	 *            Which index the doc is on
 	 */
 	public static void saveDoc(Collaborator col, LockedDocument ld,
 			String side, int ind) {
 		DocSaver ds = new DocSaver(col);
 		ds.saveDocument(ld, side, ind);
 	}
 
 	/**
 	 * Constructs a new doc saver
 	 */
 	public DocSaver(Collaborator collaborator) {
 		this.collaborator = collaborator;
 	}
 
 	/**
 	 * Starts the save request
 	 * 
 	 * @param lockedDoc
 	 *            The document to lock
 	 * @param side
 	 *            The panel in which it is sitting
 	 * @param ind
 	 *            The index of the doc
 	 */
 	public void saveDocument(LockedDocument lockedDoc, String side, int ind) {
 		this.lockedDocument = lockedDoc;
 		this.side = side;
 		this.index = ind;
 
 		collaborator.collabService.saveDocument(collaborator.clientID,
 				lockedDoc, this);
 
 		if (side.equals("left"))
 			collaborator.setGenericObjects(true);
 		else
 			collaborator.setGenericObjects(false);
 
 		// the user cannot edit the title and the contents of this doc
 		TextBox box = collaborator.titleList.get(index);
 		TextArea area = collaborator.contentsList.get(index);
 		box.setEnabled(false);
 		area.setEnabled(false);
 
 		// disable the save doc button for this side (until this doc is saved)
 		collaborator.disableButton(collaborator.saveDocButton);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		if (caught instanceof LockExpired)
 			collaborator.statusUpdate("Lock had already expired; save failed.");
 		else {
 			collaborator.statusUpdate("Error saving document"
 					+ "; caught exception " + caught.getClass()
 					+ " with message: " + caught.getMessage());
 			GWT.log("Error saving document.", caught);
 			collaborator.releaser.releaseLock(lockedDocument);
 		}
 
 		if (lockedDocument != null) {
 			collaborator.setDoc(lockedDocument.unlock(), index, side);
 
 			// is simulation enabled? then keep going.
 			// if we are stopping, then finish.
 			if (collaborator.simulation || collaborator.simulationStopping)
 				collaborator.simulationDone();
 		}
 	}
 
 	@Override
 	public void onSuccess(UnlockedDocument result) {
 		collaborator.statusUpdate("Document '"
 				+ Collaborator.shortenText(result.getTitle(), maxStrLen)
 				+ "' successfully saved.");
 
 		collaborator.setDoc(result, index, side);
 
 		// is simulation enabled? then keep going.
 		// if we are stopping, then finish.
 		if (collaborator.simulation || collaborator.simulationStopping)
 			collaborator.simulationDone();
 		// Refresh list in case title was changed.
 		collaborator.lister.getDocumentList();
 
 	}
 }
