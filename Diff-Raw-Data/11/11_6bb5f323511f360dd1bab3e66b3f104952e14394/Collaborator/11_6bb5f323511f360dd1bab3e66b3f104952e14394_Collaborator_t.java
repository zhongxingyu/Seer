 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.AbstractDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler {
 
 	protected CollaboratorServiceAsync collabService;
 
 	// Track document information.
 	protected ArrayList<AbstractDocument> documentsLeft = new ArrayList<AbstractDocument>();
 	protected ArrayList<AbstractDocument> documentsRight = new ArrayList<AbstractDocument>();
 
 	// Managing available documents.
 	protected ListBox documentList = new ListBox();
 	private Button refreshList = new Button("Refresh Document List");
 	private Button createNew = new Button("Create New Document");
 
 	// For displaying document information and editing document content.
 	protected ArrayList<TextBox> titleL = new ArrayList<TextBox>();
 	protected ArrayList<RichTextArea> contentsL = new ArrayList<RichTextArea>();
 	protected ArrayList<TextBox> titleR = new ArrayList<TextBox>();
 	protected ArrayList<RichTextArea> contentsR = new ArrayList<RichTextArea>();
 	protected Button refreshDoc = new Button("Refresh Document");
 	protected Button lockButtonL = new Button("Get Document Lock");
 	protected Button saveButtonL = new Button("Save Document");
 	protected Button lockButtonR = new Button("Get Document Lock");
 	protected Button saveButtonR = new Button("Save Document");
 	protected TabPanel documentsL = new TabPanel();
 	protected TabPanel documentsR = new TabPanel();
 
 	// Panels
 	VerticalPanel leftPanel = new VerticalPanel();
 	VerticalPanel rightPanel = new VerticalPanel();
 	HorizontalPanel leftHPanel = new HorizontalPanel();
 	HorizontalPanel rightHPanel = new HorizontalPanel();
 	
 	// Tab panel listener
 	
 	// Callback objects.
 	protected DocReader reader = new DocReader(this);
 	protected DocLister lister = new DocLister(this);
 	protected DocReleaser releaser = new DocReleaser(this);
 	protected String waitingKey = null;
 
 	// Status tracking.
 	private VerticalPanel statusArea = new VerticalPanel();
 
 	/**
 	 * UI initialization.
 	 * 
 	 * @param collabService
 	 */
 	public Collaborator(CollaboratorServiceAsync collabService) {
 		this.collabService = collabService;
 
 		HorizontalPanel outerHp = new HorizontalPanel();
 		outerHp.setWidth("100%");
 
 		VerticalPanel outerVp = new VerticalPanel();
 		outerVp.setSpacing(20);
 
 		VerticalPanel vp = new VerticalPanel();
 		vp.setHeight("100%");
 		vp.setSpacing(10);
 		vp.add(new HTML("<h2>Available Documents</h2>"));
 		documentList.setWidth("100%");
 		vp.add(documentList);
 		HorizontalPanel hp = new HorizontalPanel();
 		hp.setSpacing(10);
 		hp.add(refreshList);
 		hp.add(createNew);
 		vp.add(hp);
 		DecoratorPanel dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		dp.add(vp);
 		outerVp.add(dp);
 
 		dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		statusArea.setSpacing(10);
 		statusArea.add(new HTML("<h2>Console</h2>"));
 		dp.add(statusArea);
 		outerVp.add(dp);
 		outerHp.add(outerVp);
 
 		outerVp = new VerticalPanel();
 		outerVp.setSpacing(20);
 		dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		VerticalPanel innerVp = new VerticalPanel();
 		innerVp.add(new HTML("<h2>Documents</h2>"));
 
 		HorizontalPanel innerHp = new HorizontalPanel();
 		innerHp.setSpacing(10);
 		leftPanel.add(documentsL);
 
 		leftHPanel.add(lockButtonL);
 		//leftHPanel.add(saveButtonL);
 		leftPanel.add(leftHPanel);
 
 		rightPanel.add(documentsR);
 
 		rightHPanel.add(lockButtonR);
 		//rightHPanel.add(saveButtonR);
 		rightPanel.add(rightHPanel);
 
 		innerHp.add(leftPanel);
 		innerHp.add(rightPanel);
 
 		innerVp.add(innerHp);
 
 		dp.add(innerVp);
 		outerVp.add(dp);
 		outerHp.add(outerVp);
 
 		refreshList.addClickHandler(this);
 		createNew.addClickHandler(this);
 		refreshDoc.addClickHandler(this);
 		lockButtonL.addClickHandler(this);
 		saveButtonL.addClickHandler(this);
 		lockButtonR.addClickHandler(this);
 		saveButtonR.addClickHandler(this);
 		
 		
 		documentsL.addSelectionHandler(new SelectionHandler<Integer>(){
 	        public void onSelection(SelectionEvent<Integer> event) {
 				int ind = documentsL.getTabBar().getSelectedTab();
 				leftHPanel.clear();
				boolean b1 = (documentsLeft.get(ind) instanceof UnlockedDocument);
				boolean b2 = (documentsLeft.get(ind) instanceof LockedDocument);
				System.out.println(b1 + " " + b2);
				if (documentsLeft.get(ind) instanceof LockedDocument) {
 					leftHPanel.add(saveButtonL);

				} else {
					leftHPanel.add(lockButtonL);
 				}
 	        }});
 		
 		documentsR.addSelectionHandler(new SelectionHandler<Integer>(){
 	        public void onSelection(SelectionEvent<Integer> event) {
 				int ind = documentsR.getTabBar().getSelectedTab();
 				rightHPanel.clear();
 				if (documentsLeft.get(ind) instanceof UnlockedDocument) {
 					rightHPanel.add(lockButtonR);
 				} else {
 					rightHPanel.add(saveButtonR);
 				}
 	        }});
 			
 		
 
 		documentList.addClickHandler(this);
 		documentList.setVisibleItemCount(20);
 
 		initWidget(outerHp);
 		System.out.println("yo");
 		lister.getDocumentList();
 	}
 
 	public void addLeftTab(String t, String c) {
 		VerticalPanel vp = new VerticalPanel();
 		vp.setSpacing(5);
 
 		TextBox titleBox = new TextBox();
 		titleBox.setValue(t);
 		vp.add(titleBox);
 		titleL.add(titleBox);
 
 		RichTextArea areaBox = new RichTextArea();
 		areaBox.setText(c);
 		vp.add(areaBox);
 		contentsL.add(areaBox);
 
 		documentsL.add(vp, t);
 
 	}
 	
 	public void openLatestTab(String side) {
 		if (side.equals("left")) {
 			int last = documentsL.getTabBar().getTabCount() -1;
 			documentsL.getTabBar().selectTab(last);
 		} else {
 			int last = documentsR.getTabBar().getTabCount() -1;
 			documentsR.getTabBar().selectTab(last);
 		}
 	}
 	
 	public void openDocument(String side) {
 		int docIndx = documentList.getSelectedIndex();
 		String title = documentList.getItemText(docIndx);
 		String key = documentList.getValue(docIndx);
 
 		if (side.equals("left")) {
 		documentsLeft.add(null);
 		addLeftTab(title, "");
 		reader = DocReader.readDoc(this, key, "left", documentsLeft.size() - 1);
 		
 		} else {
 			documentsRight.add(null);
 			addRightTab(title, "");
 			reader = DocReader.readDoc(this, key, "right", documentsLeft.size() - 1);	
 		}
 		
 		openLatestTab(side);
 
 
 	}
 
 	public void addRightTab(String t, String c) {
 		VerticalPanel vp = new VerticalPanel();
 		vp.setSpacing(5);
 
 		TextBox titleBox = new TextBox();
 		titleBox.setValue(t);
 		vp.add(titleBox);
 		titleR.add(titleBox);
 
 		RichTextArea areaBox = new RichTextArea();
 		areaBox.setText(c);
 		vp.add(areaBox);
 		contentsR.add(areaBox);
 
 		documentsR.add(vp, t);
 	}
 
 	/**
 	 * Behaves similarly to locking a document, except without a key/lock obj.
 	 */
 	private void createNewDocument(String side) {
 		LockedDocument ld = new LockedDocument(null, null, null,
 				"Enter the document title.", "Enter the document contents.");
 		if (side.equals("left")) {
 			documentsLeft.add(ld);
 			addLeftTab(ld.getTitle(), ld.getContents());
 		} else {
 			documentsRight.add(ld);
 			addRightTab(ld.getTitle(), ld.getContents());
 		}
 		openLatestTab(side);
 	}
 
 	/**
 	 * Adds status lines to the console window to enable transparency of the
 	 * underlying processes.
 	 * 
 	 * @param status
 	 *            the status to add to the console window
 	 */
 	protected void statusUpdate(String status) {
 		while (statusArea.getWidgetCount() > 10) {
 			statusArea.remove(1);
 		}
 		final HTML statusUpd = new HTML(status);
 		statusArea.add(statusUpd);
 	}
 
 	/*
 	 * (non-Javadoc) Receives button events.
 	 * 
 	 * @see
 	 * com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event
 	 * .dom.client.ClickEvent)
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		if (event.getSource().equals(refreshList)) {
 			lister.getDocumentList();
 		} else if (event.getSource().equals(createNew)) {
 			createNewDocument("left");
 		} else if (event.getSource().equals(lockButtonL)) {
 			int ind = documentsL.getTabBar()
 			.getSelectedTab();
 			AbstractDocument doc = documentsLeft.get(ind);
 			if (doc instanceof UnlockedDocument) {
 				DocLocker.lockDoc(this, doc.getKey(), "left", ind);
 				lockButtonL.removeFromParent();
 				leftHPanel.add(saveButtonL);
 			}
 		} else if (event.getSource().equals(lockButtonR)) {
 			int ind = documentsR.getTabBar()
 			.getSelectedTab();
 			AbstractDocument doc = documentsRight.get(ind);
 			if (doc instanceof UnlockedDocument) {
 				DocLocker.lockDoc(this, doc.getKey(), "right", ind);
 				lockButtonR.removeFromParent();
 				rightHPanel.add(saveButtonR);
 			}
 		} else if (event.getSource().equals(saveButtonL)) {
 			int ind = documentsL.getTabBar().getSelectedTab();
 			AbstractDocument doc = documentsLeft.get(ind);
 			if (doc instanceof LockedDocument) {
 				if (doc.getTitle().equals(titleL.get(ind).getValue())
 						&& doc.getContents()
 								.equals(contentsL.get(ind).getHTML())) {
 					statusUpdate("No document changes; not saving.");
 				} else {
 					LockedDocument ld = (LockedDocument) doc;
 					ld.setTitle(titleL.get(ind).getValue());
 					ld.setContents(contentsL.get(ind).getHTML());
 					DocSaver.saveDoc(this, ld, "left", ind);
 					saveButtonL.removeFromParent();
 					leftHPanel.add(lockButtonL);
 				}
 			}
 		} else if (event.getSource().equals(saveButtonR)) {
 			int ind = documentsR.getTabBar().getSelectedTab();
 			AbstractDocument doc = documentsRight.get(ind);
 			if (doc instanceof LockedDocument) {
 				if (doc.getTitle().equals(titleR.get(ind).getValue())
 						&& doc.getContents()
 								.equals(contentsR.get(ind).getHTML())) {
 					statusUpdate("No document changes; not saving.");
 				} else {
 					LockedDocument ld = (LockedDocument) doc;
 					ld.setTitle(titleR.get(ind).getValue());
 					ld.setContents(contentsR.get(ind).getHTML());
 					DocSaver.saveDoc(this, ld, "right", ind);
 					saveButtonR.removeFromParent();
 					rightHPanel.add(lockButtonR);
 				}
 			}
 		} else if (event.getSource().equals(documentList)) {
 			if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
 				openDocument("left");
 			} else if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
 				openDocument("right");
 			}
 		}
 	}
 
 	/**
 	 * Generalized so that it can be called elsewhere. In particular, after a
 	 * document is saved, it calls this function to simulate an initial reading
 	 * of a document.
 	 * 
 	 * @param result
 	 *            the unlocked document that should be displayed
 	 */
 	protected void setDoc(UnlockedDocument result, int index, String side) {
 		if (side.equals("left")) {
 			documentsLeft.set(index, result);
 			titleL.get(index).setValue(result.getTitle());
 			contentsL.get(index).setHTML(result.getContents());
 		} else if (side.equals("right")) {
 			documentsRight.set(index, result);
 			titleR.get(index).setValue(result.getTitle());
 			contentsR.get(index).setHTML(result.getContents());
 		}
 
 		// Set buttons back to normal
 	}
 }
