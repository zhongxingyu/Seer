 package edu.caltech.cs141b.hw2.gwt.collab.client;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import java.util.ArrayList;
 
 import com.google.gwt.user.client.ui.TabBar;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * Main class for a single Collaborator widget.
  */
 public class Collaborator extends Composite implements ClickHandler, ChangeHandler {
 	
 	public static final boolean DEBUG = true;
 	
 	protected CollaboratorServiceAsync collabService;
 	
 	// Track document information.
 	protected UnlockedDocument readOnlyDoc = null;
 	protected LockedDocument lockedDoc = null;
 	
 	// Managing available documents.
 	protected ListBox documentList = new ListBox();
 	private Button refreshList = new Button("Refresh Document List");
 	private Button createNew = new Button("Create New Document");
 	
 	// For displaying document information and editing document content.
 	protected TextBox title = new TextBox();
 	protected RichTextArea contents = new RichTextArea();
 	protected Button refreshDoc = new Button("Refresh Document");
 	protected Button lockButton = new Button("Get Document Lock");
 	protected Button saveButton = new Button("Save Document");
 	
 	//annie's attempt at a close
 	protected Button closeButton = new Button("Close Current");
 	
 	// Callback objects.
 	protected DocLister lister = new DocLister(this);
 	protected DocReader reader = new DocReader(this);
 	private DocLocker locker = new DocLocker(this);
 	protected DocReleaser releaser = new DocReleaser(this);
 	private DocSaver saver = new DocSaver(this);
 	protected String waitingKey = null;
 	
 	// Status tracking.
 	private VerticalPanel statusArea = new VerticalPanel();
 	
 	private TabPanel tp = new TabPanel();
 	private TabBar tb = tp.getTabBar();
 	
 	//current tabs
 	private ArrayList<String> openTabKeys= new ArrayList<String>();
	//the title and contents for each of the tabs, indexed just like
	// openTabKeys
	private ArrayList<RichTextArea> tabContents = new ArrayList<RichTextArea>();
	private ArrayList<TextBox> tabTitles = new ArrayList<TextBox>();
 	
 	/**
 	 * UI initialization.
 	 * 
 	 * @param collabService
 	 */
 	public Collaborator(CollaboratorServiceAsync collabService) {
 		this.collabService = collabService;
 		
 		// outerHp is our horizontal panel that includes the majority of the page.
 		HorizontalPanel outerHp = new HorizontalPanel();
 		outerHp.setWidth("70%");
 		outerHp.setHeight("100%");
 		
 		// leftColVp holds our document list and console.
 		VerticalPanel leftColVp = new VerticalPanel();
 		leftColVp.setSpacing(0);
 		
 		// docsVp holds document list and relevant buttons (refresh / create new).
 		VerticalPanel docsVp = new VerticalPanel();
 		docsVp.setSpacing(10);
 		docsVp.add(new HTML("<h2>Available Documents</h2>"));
 		documentList.setWidth("100%");
 		docsVp.add(documentList);
 		
 		// docsButtonsHp holds relevant buttons (refresh / create new).
 		HorizontalPanel docsButtonsHp = new HorizontalPanel();
 		docsButtonsHp.setSpacing(10);
 		docsButtonsHp.add(refreshList);
 		docsButtonsHp.add(createNew);
 		docsVp.add(docsButtonsHp);
 		DecoratorPanel dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		docsVp.setHeight("100%");
 		dp.setHeight("100%");
 		dp.add(docsVp);
 		leftColVp.add(dp);
 		
 		// Add console to leftColVp.
 		if (DEBUG) {
 				dp = new DecoratorPanel();
 				dp.setWidth("100%");
 				statusArea.setSpacing(10);
 				statusArea.add(new HTML("<h2>Console</h2>"));
 				dp.add(statusArea);
 				leftColVp.add(statusArea);
 		}
 		
 		// We are done packing leftColVp, so add it to outerHp.
 		outerHp.add(leftColVp);
 		
 		// Now let's work on the right side of the page, which will include
 		// the tabPanel for documents (as well as some relevant buttons)
 		VerticalPanel rightColVp = new VerticalPanel();
 		rightColVp.setSpacing(20);
 
 		// Create horizontal panel that holds the document-specific buttons.
 		HorizontalPanel hp = new HorizontalPanel();
 		hp.setSpacing(10);
 		hp.add(refreshDoc);
 		hp.add(lockButton);
 		hp.add(saveButton);
 		hp.add(closeButton);
 		rightColVp.add(hp);
 		
 		// Add tab panel to rightColVp.
 		tp.setWidth("100%");
 		rightColVp.add(tp);
 		rightColVp.setWidth("100%");
 		rightColVp.setHeight("100%");
 		
 		// Add rightColVp to outerHp.
 		dp = new DecoratorPanel();
 		dp.setWidth("100%");
 		dp.setHeight("100%");
 		dp.add(rightColVp);
 		outerHp.add(dp);
 		
 		refreshList.addClickHandler(this);
 		createNew.addClickHandler(this);
 		refreshDoc.addClickHandler(this);
 		lockButton.addClickHandler(this);
 		saveButton.addClickHandler(this);
 		closeButton.addClickHandler(this);
 		
 		documentList.addChangeHandler(this);
 		documentList.setVisibleItemCount(10);
 		
 		setDefaultButtons();
 		initWidget(outerHp);
 		
 		lister.getDocumentList();
 	}
 	
 	/**
 	 * Resets the state of the buttons and edit objects to their default.
 	 * 
 	 * The state of these objects is modified by requesting or obtaining locks
 	 * and trying to or successfully saving.
 	 */
 	protected void setDefaultButtons() {
 		refreshDoc.setEnabled(true);
 		lockButton.setEnabled(true);
 		saveButton.setEnabled(false);
 		title.setEnabled(false);
 		contents.setEnabled(false);
 		closeButton.setEnabled(true);
 	}
 	
 	private void addNewTab(RichTextArea contents, TextBox title){
 		contents.setWidth("100%");
 		
 		// Add a new tab with contents and title.
 		HorizontalPanel tabHeader = new HorizontalPanel();
 		tabHeader.add(title);
 		tp.add(contents, tabHeader);
 		
 		// Select the last tab for the user.
 		tp.selectTab(tp.getTabBar().getTabCount() - 1);
 	}
 	
 	/**
 	 * Behaves similarly to locking a document, except without a key/lock obj.
 	 */
 	private void createNewDocument() {
 		discardExisting(null);
 		lockedDoc = new LockedDocument(null, null, null,
 				"Enter the document title.",
 				"Enter the document contents.");
 		locker.gotDoc(lockedDoc);
 		History.newItem("new");
 	}
 	
 	/**
 	 * Returns the currently active token.
 	 * 
 	 * @return history token which describes the current state
 	 */
 	protected String getToken() {
 		if (lockedDoc != null) {
 			if (lockedDoc.getKey() == null) {
 				return "new";
 			}
 			return lockedDoc.getKey();
 		} else if (readOnlyDoc != null) {
 			return readOnlyDoc.getKey();
 		} else {
 			return "list";
 		}
 	}
 	
 	/**
 	 * Modifies the current state to reflect the supplied token.
 	 * 
 	 * @param args history token received
 	 */
 	protected void receiveArgs(String args) {
 		if (args.equals("list")) {
 			readOnlyDoc = null;
 			lockedDoc = null;
			//title.setValue("");
			//contents.setHTML("");
 			setDefaultButtons();
 		} else if (args.equals("new")) {
 			createNewDocument();
 		} else {
 			reader.getDocument(args);
 		}
 	}
 	
 	/**
 	 * Adds status lines to the console window to enable transparency of the
 	 * underlying processes.
 	 * 
 	 * @param status the status to add to the console window
 	 */
 	protected void statusUpdate(String status) {
 		while (statusArea.getWidgetCount() > 10) {
 			statusArea.remove(1);
 		}
 		final HTML statusUpd = new HTML(status);
 		statusArea.add(statusUpd);
 	}
 
 	/* (non-Javadoc)
 	 * Receives button events.
 	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 	 */
 	@Override
 	public void onClick(ClickEvent event) {
 		if (event.getSource().equals(refreshList)) {
 			History.newItem("list");
 			lister.getDocumentList();
 		} else if (event.getSource().equals(createNew)) {
 			contents = new RichTextArea();
 			title = new TextBox();
 			createNewDocument();
 			addNewTab(contents, title);
 		} else if (event.getSource().equals(refreshDoc)) {
 			if (readOnlyDoc != null) {
 				reader.getDocument(openTabKeys.get(tb.getSelectedTab()));
				title = tabTitles.get(tb.getSelectedTab());
				contents = tabContents.get(tb.getSelectedTab());
 			}
 		} else if (event.getSource().equals(lockButton)) {
 			if (readOnlyDoc != null) {
				title = tabTitles.get(tb.getSelectedTab());
				contents = tabContents.get(tb.getSelectedTab());
 				locker.lockDocument(openTabKeys.get(tb.getSelectedTab()));
 			}
 		} else if (event.getSource().equals(saveButton)) {
 			if (lockedDoc != null) {
 				if (lockedDoc.getTitle().equals(title.getValue()) &&
 						lockedDoc.getContents().equals(contents.getHTML())) {
 					statusUpdate("No document changes; not saving.");
 				}
 				else {
 					if (!openTabKeys.contains(lockedDoc.getKey())) {
 						openTabKeys.add(lockedDoc.getKey());
						tabTitles.add(title);
						tabContents.add(contents);
 					}
 					lockedDoc.setTitle(title.getValue());
 					lockedDoc.setContents(contents.getHTML());
 					saver.saveDocument(lockedDoc);
 				}
 			}
 		} else if (event.getSource().equals(closeButton)) {
 			int removedTab = tb.getSelectedTab();
 			tp.remove(removedTab);
 			openTabKeys.remove(removedTab);
			tabTitles.remove(removedTab);
			tabContents.remove(removedTab);
 			if (tb.getTabCount() >= 1) {
 				tp.selectTab(0);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * Intercepts events from the list box.
 	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
 	 */
 	@Override
 	public void onChange(ChangeEvent event) {
 		if (event.getSource().equals(documentList)) {
 			String key = documentList.getValue(documentList.getSelectedIndex());
 			// if it's already open in a tab, save the location of the tab
			// and retrieve the title and contents from that one
 			int savedLoc = 0;
 			boolean wasOpen = false;
 			if (openTabKeys.contains(key)){
 				savedLoc = openTabKeys.indexOf(key);
 				tp.selectTab(savedLoc);
 				openTabKeys.remove(savedLoc);
 				wasOpen = true;
				title = tabTitles.get(savedLoc);
				contents = tabContents.get(savedLoc);
 			}
 			discardExisting(key);
 			if (wasOpen){
 				openTabKeys.add(savedLoc, key);
 				tp.selectTab(savedLoc);
 			} else {
 				openTabKeys.add(key);
 				contents = new RichTextArea();
 				title = new TextBox();
 				addNewTab(contents, title);
				tabTitles.add(title);
				tabContents.add(contents);
 				reader.getDocument(key);
 			}
 
 		}
 	}
 	
 	/**
 	 * Used to release existing locks when the active document changes.
 	 * 
 	 * @param key the key of the new active document or null for a new document
 	 */
 	private void discardExisting(String key) {
 		if (lockedDoc != null) {
 			if (lockedDoc.getKey() == null) {
 				statusUpdate("Discarding new document.");
 			}
 			else if (!lockedDoc.getKey().equals(key)) {
 				releaser.releaseLock(lockedDoc);
 			}
 			else {
 				// Newly active item is the currently locked item.
 				return;
 			}
 			lockedDoc = null;
 			setDefaultButtons();
 		} else if (readOnlyDoc != null) {
 			if (readOnlyDoc.getKey().equals(key)) return;
 			readOnlyDoc = null;
 		}
 	}
 }
