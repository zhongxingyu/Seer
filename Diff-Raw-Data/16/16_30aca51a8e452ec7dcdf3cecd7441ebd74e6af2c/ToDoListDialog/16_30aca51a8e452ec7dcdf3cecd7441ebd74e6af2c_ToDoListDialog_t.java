 package ptm.client.todolist;
 
 import java.util.Collections;
 
 import ptm.client.connection.Action;
 import ptm.client.datamodel.ObjectListElement;
 import ptm.client.datamodel.Task;
 import ptm.client.datamodel.ToDoList;
 
 
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.datepicker.client.DateBox;
 
 /**
  * This is ToDoList Dialog class. it the dialog that is shown on the screen.
  * it also stores ToDoList objects which stores TaskObjects.Every Task method is in this class.
  * @author huseyin
  */
 public class ToDoListDialog extends DialogBox {
 	// main list screen widgets
 	private DeckPanel mainPanel = new DeckPanel();
 	private DockPanel listPanel= new DockPanel();
 	private FlexTable taskTable = new FlexTable();
 	private Button closeButton = new Button("Close");
 	private Button newButton = new Button("New");
 	private Button editButton = new Button("Edit");
 	private Button deleteButton = new Button("Delete");
 
 	// edit/create task widgets
 	private VerticalPanel infoPanel= new VerticalPanel();
	private DisclosurePanel alertPanel = new DisclosurePanel("E-mail Options");
 	private RichTextArea textArea = new RichTextArea();
 	private CheckBox completedCheckBox= new CheckBox("Task Completed");
 	private Button okButton = new Button("OK");
 	private Button cancelButton = new Button("Cancel");
 	
 	//Alert widgets
 	private CheckBox alertCheckbox= new CheckBox("E-mail me about this task");
 	private ListBox askBeforeListbox = new ListBox();
 	private DateBox  dueDateDateBox= new DateBox();
 	
 	// manager that manages this todolist
 	private ToDoListManager toDoListManager;
 	
 	private TaskClickHandler taskClickHandler = new TaskClickHandler(this);
 	
 	//ToDoList object that this dialog represents on screen.
 	private ToDoList toDoList;
 
 	/* 
 	 * this is used when we edit or create a new task. after we press ok/cancel button.
 	 * we use this task to apply changes.
 	 */
 	private Task task;
 	
 	/*
 	 * after pressing ok/cancel method we use this variable to determine the purpose of
 	 * info screen (edit or create)
 	 */ 
 	private boolean isInEditMode;
 	
 	
 	/*
 	 * holds current row
 	 */
 	private int currentRow = -1;
 	
 
 	
 	private enum Panels{
 		listPanel,infoPanel
 	}
 	
 	//Constructors
 	
 	
 	/**
 	 * Creates a new Dialog which use given todoList and managed by given Manager
 	 * @param todoList list that this dialog will be representing
 	 * @param toDoListManager manager that this dialog will be managed by.
 	 */
 	public ToDoListDialog(ToDoList todoList ,ToDoListManager toDoListManager){
 		super(false,false);
 		
 		this.toDoListManager = toDoListManager;
 		this.toDoList = todoList;
 		
 		setAnimationEnabled(true);
 		//setPopupPosition(150, 150);
 		setText(this.toDoList.getName());
 		//Create ButtonPanel
 	    HorizontalPanel buttonPanel = new HorizontalPanel();
 	    buttonPanel.setSpacing(10);
 	    buttonPanel.add(newButton);
 	    buttonPanel.add(editButton);
 	    buttonPanel.add(deleteButton);
 	    buttonPanel.add(closeButton);
 	    
 	    newButton.addClickHandler(toDoListManager.getEventManager());
 	    editButton.addClickHandler(toDoListManager.getEventManager());
 	    deleteButton.addClickHandler(toDoListManager.getEventManager());
 	    closeButton.addClickHandler(toDoListManager.getEventManager());
 	    
 	    
 	    //Create Edit Panel
 	    //dueDateDateBox.setStyleName("ToDoListDialog_dateBox");
 	    //textArea.setStyleName("ToDoListDialog_textArea");
 	    
 	    //Alert disclosure panel
 	    alertPanel.setAnimationEnabled(true);
 	    VerticalPanel verticalAlertPanel = new VerticalPanel();
 	    alertPanel.add(verticalAlertPanel);
 	    //email
 	    verticalAlertPanel.add(alertCheckbox);
 	    
 	    //duedate panel
 	    HorizontalPanel tempPanel = new HorizontalPanel();
 	    tempPanel.setSpacing(10);
 	    tempPanel.add(new Label("Due Date "));
 	    tempPanel.add(dueDateDateBox);
 	    verticalAlertPanel.add(tempPanel);
 
 	    //alert before panel
 	    tempPanel = new HorizontalPanel();
 	    tempPanel.setSpacing(10);
 	    tempPanel.add(new Label("Alert me "));
 	    tempPanel.add(askBeforeListbox);
 	    askBeforeListbox.addItem("same day", "0");
 	    askBeforeListbox.addItem("1 day before", "1");
 	    askBeforeListbox.addItem("3 days before", "3");
 	    askBeforeListbox.addItem("5 days before", "5");
 	    askBeforeListbox.addItem("one week before", "7");
 	    askBeforeListbox.addItem("one month before", "30");
 	    askBeforeListbox.setSelectedIndex(2);
 	    verticalAlertPanel.add(tempPanel);
 	    
 	    
 	    
 	    //completedCheckBox.addStyleName("ToDoListDialog_checkBox");
 	    infoPanel.setSpacing(10);
 	    infoPanel.setSize("400", "400");
 	    infoPanel.add(completedCheckBox);
 	    infoPanel.add(alertPanel);
 	    infoPanel.add(new Label("Value"));
 	   
 	    textArea.setSize("380", "200");
 	    infoPanel.add(textArea);
 	    
 	    tempPanel = new HorizontalPanel();
 	    tempPanel.setSpacing(10);
 	    tempPanel.add(okButton);
 	    tempPanel.add(cancelButton);
 	    
 	    okButton.addClickHandler(toDoListManager.getEventManager());
 	    cancelButton.addClickHandler(toDoListManager.getEventManager());
 	    
 	    infoPanel.add(tempPanel);
 	    infoPanel.setCellHorizontalAlignment(tempPanel,VerticalPanel.ALIGN_CENTER);
 
 	    taskTable.setCellPadding(6);
 	    
 	    //set centerPanel
 	    mainPanel.add(listPanel);
 	    mainPanel.add(infoPanel);
 	    mainPanel.showWidget(Panels.listPanel.ordinal());
 	    //create main panel
 	    listPanel.setSize("400", "400");
 	    ScrollPanel scrollPanel = new ScrollPanel();
 	    scrollPanel.setSize("400", "350");
 	    scrollPanel.add(taskTable);
 	    listPanel.add(scrollPanel,DockPanel.CENTER);
 	    listPanel.add(buttonPanel,DockPanel.SOUTH);
 	    listPanel.setCellHeight(buttonPanel, "30px");
 	    listPanel.setCellHorizontalAlignment(buttonPanel, DockPanel.ALIGN_CENTER);
 	    setWidget(mainPanel);
 	    
 	    taskTable.addClickHandler(toDoListManager.getEventManager());
 	    //Fill Task table
 	    Collections.sort(toDoList.getList());
 	    for (int i=0;i<toDoList.getList().size();i++){
 	    	addTaskTableRow(i, toDoList.getList().get(i));
 	    }
 	    setCurrentRow(-1);
 	    evaluateButtonStatus();
 	}
 
 	
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see com.google.gwt.user.client.ui.DialogBox#endDragging(com.google.gwt.event.dom.client.MouseUpEvent)
 	 */
 	@Override
 	protected void endDragging(MouseUpEvent event) {
 		super.endDragging(event);
 		ObjectListElement e = toDoListManager.getApplicationManager().getSession().getAllToDoLists().get(toDoListManager.getApplicationManager().getSession().getToDoIndex(toDoList.getId()));
 		if (e.getX() != getAbsoluteLeft() || e.getY() != getAbsoluteTop()){
 			e.setX(getPopupLeft());
 			e.setY(getPopupTop());
 			
 			Action action = new Action();
 			action.setActionType(Action.ActionType.TODO_MOVE);
 			action.setObject(e);
 			
 			toDoListManager.getApplicationManager().getConnectionManager().addAction(action);
 		}
 		
 	}
 
 
 
 
 
 	/**
 	 * set name of the todoList. this dialog change name of the todoList that this
 	 * dialog is representing and change title of the dialog to given name
 	 * @param name new name of todoList
 	 */
 	public void setName(String name){
 		toDoList.setName(name);
 		this.setText(name);
 	}
 	
 	/**
 	 * Creates a new Task on to-do list that this dialog is representing 
 	 * and opens edit screen for new task. if cancel button is pressed on edit screen
 	 * new task will be deleted.
 	 */
 	public void createPressed(){
 		isInEditMode = false;
 		
 		task = new Task(toDoListManager.getApplicationManager().getNextTempOId(),null);
 		toDoList.addElement(task);
 		openEditMode(task);
 		mainPanel.showWidget(Panels.infoPanel.ordinal());
 	}
 	
 	/**
 	 * This method is called after task is created on the server side.
 	 */
 	public void postCreate(Task task,long newObjectId){
 		task.setId(newObjectId);
 	}
 	
 	/**
 	 * Edits selected task. it opens edit screen with information of selected task.
 	 * if cancel button is pressed on edit screen no changes will be made on selected task.
 	 */
 	public void editPressed(){
 		isInEditMode = true;
 		
 		task = toDoList.getList().get(currentRow);
 		openEditMode(task);
 		mainPanel.showWidget(Panels.infoPanel.ordinal());
 	}
 	
 	/**
 	 * Deletes selected task. First it will asks confirmation for delete process if OK button is pressed
 	 * on confirmation task will be deleted.
 	 */
 	public void deletePressed(){
 		if (Window.confirm("Are you sure you want to delete current task ?")){
 			Task task = toDoList.getList().get(currentRow);
 			toDoList.getList().remove(currentRow);
 			taskTable.removeRow(currentRow);
 			
 			//create action
 			Action action = new Action();
 			action.setObjectId(toDoList.getId());
 			action.setObject(task);
 			action.setActionType(Action.ActionType.TASK_DELETE);
 			toDoListManager.getApplicationManager().getConnectionManager().addAction(action);
 			//reset Current row
 			setCurrentRow(-1);
 			evaluateButtonStatus();
 		}
 	
 	}
 	
 	
 	/**
 	 * Closes Dialog and deletes everything about this dialog.
 	 */
 	public void closePressed(){
 		
 		/* if dialog is closed in create mode, we have to
 		 * remove task information.
 		 */
 		/*if (task != null && !isInEditMode/*in create mode* /){
 			toDoList.removeElement(task);
 			task = null;
 		}*/
 		cancelPressed();
 		
 		Action action = new Action();
 		action.setActionType(Action.ActionType.TODO_CLOSE);
 		action.setObjectId(getId());
 		
 		toDoListManager.getApplicationManager().getConnectionManager().addAction(action);
 		
 		this.hide();
 		toDoListManager.getToDoListDialogs().remove(this);
 		
 		//update session
 		toDoListManager.getApplicationManager().getSession().getToDo(toDoList.getId()).setOpen(false);
 		toDoListManager.getApplicationManager().getToolbarManager().evaluateToDoButtonStatus();
 	}
 	
 	
 	/**
 	 * Saves changes on edit Task screen.
 	 */
 	public void okPressed(){
 		int taskLoc=0;
 		//we need original task location only if we are in editmode.
 		if ( isInEditMode )
 			taskLoc = toDoList.getList().indexOf(task);
 		
 		//create Task Object
 		task.setIsDone(completedCheckBox.getValue());
 		task.setContent(textArea.getHTML());
 		task.setDueDate(dueDateDateBox.getValue());
 		int index = askBeforeListbox.getSelectedIndex();
 		int askBeforeValue = Integer.parseInt(askBeforeListbox.getValue(index));
 		task.setAlertBefore(askBeforeValue);
 		task.setIsAlerOn(alertCheckbox.getValue());
 		
 		Collections.sort(toDoList.getList());
 		int newLoc = toDoList.getList().indexOf(task);
 		//create action
 		Action action = new Action();
 		if(isInEditMode)
 			action.setActionType(Action.ActionType.TASK_EDIT);
 		else
 			action.setActionType(Action.ActionType.TASK_CREATE);
 		action.setObject(task);
 		action.setObjectId(toDoList.getId());
 		toDoListManager.getApplicationManager().getConnectionManager().addAction(action);
 		
 		//update task list.
 		if(isInEditMode){
 			if ( newLoc == taskLoc ){
 				setTaskTableRow(taskLoc, task);
 			}else{
 				taskTable.removeRow(taskLoc);
 				addTaskTableRow(newLoc, task);
 			}
 			
 		}else{
 			addTaskTableRow(newLoc, task);
 		}
 		
 		// We do this because if we close dialog on create mode we have to clear new task object 
 		task = null;
 		
 		//open list mode
 		mainPanel.showWidget(Panels.listPanel.ordinal());
 		evaluateButtonStatus();
 	}
 	
 	
 	/**
 	 * Discards changes on edit Task screen.
 	 */
 	public void cancelPressed(){
 		//if dialog was opened in create mode remove newly created task object
 		if (!isInEditMode)
 			toDoList.removeElement(task);
 
 		// We do this because if we close dialog on create mode we have to clear new task object 
 		task = null;
 		
 		//open list mode
 		mainPanel.showWidget(Panels.listPanel.ordinal());
 	}
 	
 	
 	/**
 	 * Opens edit mode with given task.
 	 * @param task Task that will be edited.
 	 */
 	public void openEditMode(Task task){
		//close alert disclosure panel if it is open.
		alertPanel.setAnimationEnabled(false);
		alertPanel.setOpen(false);
		alertPanel.setAnimationEnabled(true);
		
 		textArea.setHTML(task.getContent()==null?"":task.getContent());
 		completedCheckBox.setValue(task.getIsDone());
 		
 		dueDateDateBox.setValue(task.getDueDate());
 		alertCheckbox.setValue(task.getIsAlerOn());
 		
 		//setalertbefore
 		int index = 0;
 		switch(task.getAlertBefore()){
 		case 0:index=0;break;
 		case 1:index=1;break;
 		case 3:index=2;break;
 		case 5:index=3;break;
 		case 7:index=4;break;
 		case 30:index=5;break;
 		}
 		askBeforeListbox.setSelectedIndex(index);
 		
 	}
 	
 	/**
 	 * Set Current row to given value. -1 means there is no current.
 	 * @param currentRow New current tow value
 	 */
 	public void setCurrentRow(int currentRow){
 		if (this.currentRow == currentRow)
 			return;
 		if (this.currentRow!=-1)
 			taskTable.getRowFormatter().setStyleName(this.currentRow, "ToDoListDialog_row");
 		if (currentRow!=-1)
 			taskTable.getRowFormatter().setStyleName(currentRow, "ToDoListDialog_currentRow");
 		this.currentRow = currentRow;
 	}
 	
 	
 	/**
 	 * Adds a new row to given row index of task table with the given task values
 	 * @param rowIndex index of row that needs to be added
 	 * @param task Task that will be represented by given table row.
 	 */
 	public void addTaskTableRow(int rowIndex,Task task){
 		CheckBox checkBox = new CheckBox();
 		checkBox.setValue(task.getIsDone());
 		checkBox.addClickHandler(taskClickHandler);
 		//clear currentrow first
 		setCurrentRow(-1);
 		taskTable.insertRow(rowIndex);
 		taskTable.setWidget(rowIndex, 0,checkBox);
 		taskTable.setHTML(rowIndex, 1, task.getContent());
 		setCurrentRow(rowIndex);
 	}
 	
 	/**
 	 * check if there is a selected task if there is not it disables edit and delete task buttons.
 	 */
 	public void evaluateButtonStatus(){
 		boolean editButtonEnable = false;
 		boolean deleteButtonEnable = false;
 		if(currentRow != -1){
 			editButtonEnable = true;
 			deleteButtonEnable = true;
 		}
 		editButton.setEnabled(editButtonEnable);
 		deleteButton.setEnabled(deleteButtonEnable);
 	}
 	
 	/**
 	 * Sets given given task values to given row of task table.
 	 * @param rowIndex index of row that needs to be changed
 	 * @param task Task that is represented by given table row.
 	 */
 	public void setTaskTableRow(int rowIndex,Task task){
 		((CheckBox)(taskTable.getWidget(rowIndex, 0))).setValue(task.getIsDone());
 		taskTable.setHTML(rowIndex, 1, task.getContent());
 	}
 	
 	//Getters and Setters
 	public void setElementId(long oldId,long newId){
 		toDoList.setElementId(oldId,newId);
 	}
 	
 	public long getId(){
 		return toDoList.getId();
 	}
 
 	public Button getCloseButton() {
 		return closeButton;
 	}
 
 	public Button getNewButton() {
 		return newButton;
 	}
 
 	public Button getEditButton() {
 		return editButton;
 	}
 
 	public Button getDeleteButton() {
 		return deleteButton;
 	}
 
 	public ToDoList getToDolist() {
 		return toDoList;
 	}
 
 	public Button getOkButton() {
 		return okButton;
 	}
 
 	public Button getCancelButton() {
 		return cancelButton;
 	}
 
 
 	public FlexTable getTaskTable() {
 		return taskTable;
 	}
 
 
 	public void setTaskTable(FlexTable taskTable) {
 		this.taskTable = taskTable;
 	}
 
 
 	public ToDoListManager getToDoListManager() {
 		return toDoListManager;
 	}
 	
 
 }
