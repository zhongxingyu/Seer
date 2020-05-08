 package datalogic;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionEvent;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.dialog.Dialog;
 import org.icefaces.ace.component.tabset.TabPane;
 import org.icefaces.ace.component.tabset.TabSet;
 import org.icefaces.ace.event.ExpansionChangeEvent;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.model.table.RowStateMap;
 
 import entities.Backend;
 import entities.Comment;
 import entities.Composite;
 import entities.Instance;
 import entities.Mode;
 import entities.Scheduling;
 
 @ManagedBean(name = "schedulingDataManager")
 @SessionScoped
 public class SchedulingDataManager {
 
 	@ManagedProperty(value = "#{sessionBean}")
 	private SessionBean session;
 
 	@ManagedProperty(value = "#{dataMaster}")
 	private DataMaster master;
 
 	private SchedulingBuilder builder;
 	private Comment addComment = new Comment();
 
 	private List<Scheduling> schedulings = new ArrayList<Scheduling>();
 	private RowStateMap stateMap = new RowStateMap();
 	private DataTable dataTable;
 
 	private HashMap<Integer, SchedulingBuilder> editBuffer = new HashMap<Integer, SchedulingBuilder>();
 	private HashMap<Integer, List<Comment>> commentLists = new HashMap<Integer, List<Comment>>();
 	private HashMap<Integer, Comment> editCommentList = new HashMap<Integer, Comment>();
 
 	private Collection<Mode> modes = SessionBean.MODES.values();
 	private Collection<Composite> composites = SessionBean.COMPOSITES.values();
 	private Collection<Backend> backends = SessionBean.BACKENDS.values();
 
 	private boolean showEditError;
 	private String editErrorMessage;
 
 	private boolean showAddError;
 	private String addErrorMessage;
 
 	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
 			"dd-MM-yyyy HH:mm:ss");
 
 	private static final int STATIC_TABS = 4;
 
 	private HttpConnector httpConnector = new HttpConnector();
 
 	private boolean responseDialogVisible;
 	private String runReport;
 
 	private String schedulingList;
 
 	private int counter = 0;
 
 	private List<SchedulingTab> tabs = new ArrayList<SchedulingTab>();;
 
 	private int selectedTabIndex;
 
 	private TabSet tabSet;
 
 	@PostConstruct
 	private void init() {
 		this.builder = new SchedulingBuilder();
 	}
 
 	public void listSelected() {
 		if (stateMap.getSelected().isEmpty()) {
 			this.schedulingList = "None";
 			return;
 		}
 		this.schedulingList = "";
 		for (Scheduling s : (List<Scheduling>) stateMap.getSelected()) {
 			this.schedulingList += s.getId() + "<br/>";
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when a new {@link Scheduling} is to be
 	 * constructed from the {@link SchedulingBuilder}
 	 */
 	public void addScheduling() {
 		
 		try {
 
 			/*
 			 * Build the Scheduling from values inserted to the builder. Values
 			 * are validated within the builder and an IllegalOperationException
 			 * will be thrown if the values are invalid
 			 */
 			Scheduling s = this.builder.build();
 
 			// If the database connector returns true from the persisting of
 			// Scheduling we can safely add it to the table
 			if (this.session.getConnector().addScheduling(s)) {
 				this.schedulings.add(s);
 				this.builder = new SchedulingBuilder();
 			}
 
 			/*
 			 * If there is some text in addComment, we'll add it straight away.
 			 * It will be connected by the ID of previously added Scheduling so
 			 * its important to add the Comment after the Scheduling has been
 			 * persisted and an ID has been assigned to it.
 			 */
 			if (addComment.getText() != null
 					&& !"".equals(addComment.getText())) {
 				addComment.setSchedulingID(s.getId());
 				addComment.setCreationDate(DATE_FORMAT.format(new Date()));
 				session.getConnector().addComment(addComment);
 			}
 
 			// If we've gotten this far, there were no errors and we can hide
 			// all error messages
 			showAddError = false;
 
 			// Http request, consult Hanzki for any details
 			System.out.println("HttpConnector returned: "
 					+ httpConnector.addId(
 							SessionBean.COMPOSITES.get(s.getServiceID())
 									.getDestinationURL(), s.getId()));
 		} catch (IllegalOperationException e) {
 
 			/*
 			 * An error was thrown during the validation of the Scheduling: we
 			 * set the error message to be shown in Add Scheduling section and
 			 * set the visibility to true
 			 */
 			addErrorMessage = e.getMessage();
 			showAddError = true;
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when a row is expanded or closed to
 	 * initialize or clear the needed buffers for {@link Comment} and
 	 * {@link SchedulingBuilder} instances. The {@link ExpansionChangeEvent}
 	 * passed as a parameter contains the Scheduling corresponding to the row.
 	 */
 	public void expansion(ExpansionChangeEvent e) {
 
 		// Retrieve the Scheduling corresponding to the row
 		Scheduling s = (Scheduling) e.getRowData();
 
 		if (e.isExpanded()) {
 
 			/*
 			 * Initialize the editing for this row by creating a new
 			 * SchedulingBuilder and adding it to the editBuffer with the ID of
 			 * the corresponding Scheduling as its key
 			 */
 			this.editBuffer.put(s.getId(), new SchedulingBuilder(s));
 
 			/*
 			 * Retrieve the last five Comments of this Scheduling from the
 			 * database and add them to the HashMap of lists with the ID of the
 			 * Scheduling as key
 			 */
 			this.commentLists.put(s.getId(), this.session.getConnector()
 					.getLastComments(s.getId()));
 
 			/*
 			 * Create a new empty comment, set its SchedulingID to match this
 			 * Scheduling and add it to the comment buffer
 			 */
 			Comment c = new Comment();
 			this.editCommentList.put(s.getId(), c);
 
 			/*
 			 * Since we just expanded a new row, we want to clear all error
 			 * messages that would be shown in it
 			 */
 			this.showEditError = false;
 		} else {
 			/*
 			 * When the row is closed, we remove the SchedulingBuilder from the
 			 * editBuffer so it doesn't keep the changes that were made. We also
 			 * clear the list of comments assigned to this row so it will be
 			 * refreshed when the row is expanded again
 			 */
 			this.commentLists.remove(s.getId());
 			this.editBuffer.remove(s.getId());
 
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when the edit of a {@link Scheduling} is
 	 * to be confirmed
 	 */
 	public void confirmEdit(Scheduling s) {
 
 		try {
 
 			/*
 			 * Retrieve the SchedulingBuilder corresponding to the id of
 			 * Scheduling passed as a parameter from the UI and build it.
 			 * Validation is carried out normally within the builder
 			 */
 			Scheduling n = this.editBuffer.get(s.getId()).build();
 
 			/*
 			 * In order to refresh the DataTable we need to "brute force" the
 			 * change by removing the unedited Scheduling and adding the newly
 			 * built one
 			 */
 			this.schedulings.remove(s);
 			this.schedulings.add(n);
 
 			// this.session.getConnector().updateScheduling(n);
 
 			// If we've gotten this far there were no errors and we can hide all
 			// error messages
 			showEditError = false;
 
 			// Http request, consult Hanzki for any details
 			System.out.println("HttpConnector returned: "
 					+ httpConnector.editId(
 							SessionBean.COMPOSITES.get(s.getServiceID())
 									.getDestinationURL(), s.getId()));
 		} catch (IllegalOperationException e) {
 
 			/*
 			 * An error was thrown during the validation of the edited
 			 * Scheduling: we set the error message to be shown in Edit section
 			 * and set the visibility to true
 			 */
 			editErrorMessage = e.getMessage();
 			showEditError = true;
 		}
 
 	}
 	
 	public void confirmEdit(SchedulingTab t) {
 
 		try {
 
 			/*
 			 * Retrieve the SchedulingBuilder corresponding to the id of
 			 * Scheduling passed as a parameter from the UI and build it.
 			 * Validation is carried out normally within the builder
 			 */
 			Scheduling n = t.getBuilder().build();
 
 			/*
 			 * In order to refresh the DataTable we need to "brute force" the
 			 * change by removing the unedited Scheduling and adding the newly
 			 * built one
 			 */
 			this.schedulings.remove(t.getScheduling());
 			this.schedulings.add(n);
 
 			// this.session.getConnector().updateScheduling(n);
 
 			// If we've gotten this far there were no errors and we can hide all
 			// error messages
 			showEditError = false;
 
 			// Http request, consult Hanzki for any details
 			System.out.println("HttpConnector returned: "
 					+ httpConnector.editId(
 							SessionBean.COMPOSITES.get(t.getScheduling().getServiceID())
 									.getDestinationURL(), t.getScheduling().getId()));
 		} catch (IllegalOperationException e) {
 
 			/*
 			 * An error was thrown during the validation of the edited
 			 * Scheduling: we set the error message to be shown in Edit section
 			 * and set the visibility to true
 			 */
 			editErrorMessage = e.getMessage();
 			showEditError = true;
 		}
 
 	}
 
 	/**
 	 * Method to be called from the UI when the fields in the Edit view are to
 	 * be reset to their initial state
 	 */
 	public void resetEdit(Scheduling s) {
 		/*
 		 * Reset the fields in the Edit view by replacing the SchedulingBuilder
 		 * by a new one initialized with the Scheduling passed as a parameter
 		 * from UI
 		 */
 		SchedulingBuilder b = new SchedulingBuilder(s);
 
 		this.editBuffer.put(s.getId(), b);
 	}
 
 	/**
 	 * Method to be called from the UI when the comment in the Edit view is to
 	 * be submitted to the database
 	 */
 	public void submitComment(int id) {
 		Comment c = this.editCommentList.get(id);
 
 		c.setCreationDate(DATE_FORMAT.format(new Date()));
 		c.setSchedulingID(id);
 
 		// If the database connector returns true from the persisting of the
 		// comment we can safely add it to the table
 		if (this.session.getConnector().addComment(c)) {
 			this.editCommentList.put(id, new Comment());
 			this.commentLists.get(id).add(c);
 			this.commentLists.put(id, this.session.getConnector()
 					.getLastComments(id));
 		}
 	}
 	
 	public void submitComment(SchedulingTab t) {
 		Comment c = t.getAddComment();
 
 		c.setCreationDate(DATE_FORMAT.format(new Date()));
 		c.setSchedulingID(t.getScheduling().getId());
 
 		// If the database connector returns true from the persisting of the
 		// comment we can safely add it to the table
 		if (this.session.getConnector().addComment(c)) {
 			t.setAddComment(new Comment());
 			t.setComments(this.session.getConnector()
 					.getLastComments(t.getScheduling().getId()));
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when Select All button is pressed and all
 	 * rows currently filtered (or lazily loaded) are to be selected. The [@link
 	 * DataTable} is bound to this bean so we can operate on the filtered data.
 	 */
 	public void selectFiltered() {
 
 		List<Scheduling> filteredRows = dataTable.getFilteredData();
 
 		/*
 		 * If the filtered data is empty or null, we do not currently have a
 		 * filter. To prevent a NullPointerException we use the standard method
 		 * of the stateMap for selecting all rows
 		 */
 		if (filteredRows == null || filteredRows.isEmpty()) {
 			return;
 		}
 
 		// Iterate through the filtered data and set all rows to be selected
 		for (Scheduling s : filteredRows) {
 			stateMap.get(s).setSelected(true);
 		}
 	}
 
 	/**
 	 * Method to be called from the UI when Deselect All button is pressed and
 	 * all selected rows are to be deselected.
 	 */
 	public void deselectAll() {
 		Collection<RowState> allRows = stateMap.values();
 
 		for (RowState s : allRows) {
 			s.setSelected(false);
 		}
 	}
 
 	// TODO Comments
 	public void runSelectedSchedules() {
 		String runReport = stateMap.getSelected().size()
 				+ " schedulings were run.\n";
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 			runReport += "Response for scheduling "
 					+ s.getId()
 					+ " was "
 					+ httpConnector.runId(
 							SessionBean.COMPOSITES.get(s.getServiceID())
 									.getDestinationURL(), s.getId()) + ".\n";
 		}
 		this.runReport = runReport;
 		this.responseDialogVisible = true;
 	}
 
 	// TODO comments
 	public void resumeSelected() {
 
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 
 			if (s.getStatusID() != SessionBean.REMOVED) {
 				s.setStatusID(SessionBean.ENABLED);
 
 				// this.session.getConnector().updateScheduling(s);
 				System.out.println("HttpConnector returned: "
 						+ httpConnector.editId(
 								SessionBean.COMPOSITES.get(s.getServiceID())
 										.getDestinationURL(), s.getId()));
 			}
 		}
 	}
 
 	// TODO comments
 	public void holdSelected() {
 
 		for (Object rowData : stateMap.getSelected()) {
 			Scheduling s = (Scheduling) rowData;
 
 			if (s.getStatusID() != SessionBean.REMOVED) {
 				s.setStatusID(SessionBean.DISABLED);
 
 				// this.session.getConnector().updateScheduling(s);
 				System.out.println("HttpConnector returned: "
 						+ httpConnector.editId(
 								SessionBean.COMPOSITES.get(s.getServiceID())
 										.getDestinationURL(), s.getId()));
 			}
 		}
 	}
 
 	public void addTab(Scheduling s) {
 		SchedulingTab t = new SchedulingTab();
 		t.setScheduling(s);
 
 		if (!tabs.contains(t)) {
 			counter++;
 			
 			List<Instance> temp = new ArrayList<Instance>();
 			for (Instance i : this.master.getInstances()) {
 
				if (i.getProcess() != null  && s.getName().substring(0, 4).equals(i.getProcess().substring(0,4))) {
 					temp.add(i);
 				}
 			}
 			
 			t.setComments(this.session.getConnector()
 					.getLastComments(s.getId()));
 			t.setInstances(temp);
 			tabs.add(t);
 		}
 
 		this.tabSet.setSelectedIndex(tabs.indexOf(t) + STATIC_TABS);
 	}
 
 	public void removeCurrent(SchedulingTab t) {
 		this.tabSet.setSelectedIndex(0);
 		TabPane pane = (TabPane) this.tabSet.getChildren().get(tabs.indexOf(t) + STATIC_TABS + 1);
 		pane.setInView(false);
 		tabs.remove(t);
 	}
 	
 	public void removeAllTabs() {
 		this.tabSet.setSelectedIndex(0);
 		List<UIComponent> panes =  this.tabSet.getChildren();
 		for (UIComponent pane : panes) {
 			pane.setInView(false);
 		}
 		tabs.clear();
 	}
 	
 	public void removeOtherTabs(SchedulingTab t) {
 		this.tabSet.setSelectedIndex(0);
 		List<UIComponent> panes =  this.tabSet.getChildren();
 		for (UIComponent pane : panes) {
 			if(panes.indexOf(pane) > STATIC_TABS - 1 && panes.indexOf(pane) != tabs.indexOf(t) + STATIC_TABS) {
 				pane.setInView(false);
 			}
 		}
 		tabs.clear();
 		tabs.add(t);
 		this.tabSet.setSelectedIndex(tabs.indexOf(t) + STATIC_TABS);
 	}
 
 
 	/**
 	 * Method for refreshing the contents of the data table.
 	 */
 	public void refresh() {
 		this.schedulings.clear();
 		this.schedulings = this.session.getConnector().getSchedulings();
 	}
 
 	public SessionBean getSession() {
 		return session;
 	}
 
 	public void setSession(SessionBean session) {
 		this.session = session;
 	}
 
 	public SchedulingBuilder getBuilder() {
 		return builder;
 	}
 
 	public void setBuilder(SchedulingBuilder builder) {
 		this.builder = builder;
 	}
 
 	public Comment getAddComment() {
 		return addComment;
 	}
 
 	public void setAddComment(Comment addComment) {
 		this.addComment = addComment;
 	}
 
 	public List<Scheduling> getSchedulings() {
 		if (this.schedulings == null || this.schedulings.isEmpty()) {
 			this.refresh();
 		}
 		return schedulings;
 	}
 
 	public void setSchedulings(List<Scheduling> schedulings) {
 		this.schedulings = schedulings;
 	}
 
 	public RowStateMap getStateMap() {
 		return stateMap;
 	}
 
 	public void setStateMap(RowStateMap stateMap) {
 		this.stateMap = stateMap;
 	}
 
 	public HashMap<Integer, SchedulingBuilder> getEditBuffer() {
 		return editBuffer;
 	}
 
 	public void setEditBuffer(HashMap<Integer, SchedulingBuilder> editBuffer) {
 		this.editBuffer = editBuffer;
 	}
 
 	public HashMap<Integer, List<Comment>> getCommentLists() {
 		return commentLists;
 	}
 
 	public void setCommentList(HashMap<Integer, List<Comment>> commentList) {
 		// Cannot be set from the UI but the setter has to exist
 	}
 
 	public HashMap<Integer, Comment> getEditCommentList() {
 		return editCommentList;
 	}
 
 	public void setEditCommentList(HashMap<Integer, Comment> editCommentList) {
 		this.editCommentList = editCommentList;
 	}
 
 	public boolean isShowEditError() {
 		return showEditError;
 	}
 
 	public void setEditError(boolean editError) {
 		// Cannot be set from the UI but the setter has to exist
 	}
 
 	public String getEditErrorMessage() {
 		return editErrorMessage;
 	}
 
 	public void setEditErrorMessage(String editErrorMessage) {
 		this.editErrorMessage = editErrorMessage;
 	}
 
 	public boolean isShowAddError() {
 		return showAddError;
 	}
 
 	public void setShowAddError(boolean addError) {
 		// Cannot be set from the UI but the setter has to exist
 	}
 
 	public String getAddErrorMessage() {
 		return addErrorMessage;
 	}
 
 	public void setAddErrorMessage(String addErrorMessage) {
 		this.addErrorMessage = addErrorMessage;
 	}
 
 	public DataTable getDataTable() {
 		return dataTable;
 	}
 
 	public void setDataTable(DataTable dataTable) {
 		this.dataTable = dataTable;
 	}
 
 	public Collection<Mode> getModes() {
 		return modes;
 	}
 
 	public void setModes(Collection<Mode> modes) {
 		this.modes = modes;
 	}
 
 	public Collection<Composite> getComposites() {
 		return composites;
 	}
 
 	public void setComposites(Collection<Composite> composites) {
 		this.composites = composites;
 	}
 
 	public Collection<Backend> getBackends() {
 		return backends;
 	}
 
 	public void setBackends(Collection<Backend> backends) {
 		this.backends = backends;
 	}
 
 	public boolean isResponseDialogVisible() {
 		return responseDialogVisible;
 	}
 
 	public void setResponseDialogVisible(boolean responseDialogVisible) {
 		this.responseDialogVisible = responseDialogVisible;
 	}
 
 	public String getRunReport() {
 		return runReport;
 	}
 
 	public void setRunReport(String runReport) {
 		this.runReport = runReport;
 	}
 
 	public String getSchedulingList() {
 		return schedulingList;
 	}
 
 	public void setSchedulingList(String schedulingList) {
 		this.schedulingList = schedulingList;
 	}
 
 	public List getTabs() {
 		return tabs;
 	}
 
 	public void setTabs(List tabs) {
 		this.tabs = tabs;
 	}
 
 	public int getSelectedTabIndex() {
 		return selectedTabIndex;
 	}
 
 	public void setSelectedTabIndex(int selectedTabIndex) {
 		this.selectedTabIndex = selectedTabIndex;
 	}
 
 	public TabSet getTabSet() {
 		return tabSet;
 	}
 
 	public void setTabSet(TabSet tabSet) {
 		this.tabSet = tabSet;
 	}
 
 	public DataMaster getMaster() {
 		return master;
 	}
 
 	public void setMaster(DataMaster master) {
 		this.master = master;
 	}
 }
