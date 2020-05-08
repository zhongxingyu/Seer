 package org.jsc.client;
 
 import java.util.ArrayList;
 
 import org.jsc.client.event.SkatingClassChangeEvent;
 import org.jsc.client.event.SkatingClassChangeHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A screen that is used to administer the skating data, including creating modifying
  * sessions and classes.
  * 
  * @author Matt Jones
  */
 public class AdminScreen extends BaseScreen implements SkatingClassChangeHandler, ClickHandler {
 
     protected HorizontalPanel screen;
     private VerticalPanel classesPanel;
     protected VerticalPanel adminPanel;
     private ClassListModel sessionClassList;
     private Grid classesGrid;
     private SkaterRegistrationServiceAsync regService;
     private int selectedClassRowIndex;
 //    private Grid rosterGrid;
     private Label classLabel;
 //    private Grid classInfoGrid;
     protected ArrayList<RosterEntry> currentRoster;
     private RosterScreen roster;
 //    private boolean layoutForPrinting;
     private TextBox oldSeasonField;
     private TextBox oldSessionField;
     private TextBox newSeasonField;
     private TextBox newSessionField;
 
     /**
      * Create the Management screen for managing class rosters and levels 
      * passed. This screen is only accessed by coaches and administrators.
      * @param loginSession
      * @param eventBus
      * @param sessionClassList
      */
     public AdminScreen(LoginSession loginSession, HandlerManager eventBus, 
             ClassListModel sessionClassList, RosterScreen roster) {
         super(loginSession, eventBus);
         this.sessionClassList = sessionClassList;
         this.roster = roster;
         //this.layoutForPrinting = false;
         selectedClassRowIndex = 0;
         
         this.setScreenTitle("Administer Database");
         layoutScreen();
         this.setContentPanel(screen);
         
         // Register as a handler for Skating class changes, and handle those changes
         eventBus.addHandler(SkatingClassChangeEvent.TYPE, this);
     }
     
     /**
      * Constructor that is used when building the RosterScreen.
      * @param loginSession
      * @param eventBus
      */
     public AdminScreen(LoginSession loginSession, HandlerManager eventBus) {
         super(loginSession, eventBus);
 //        this.layoutForPrinting = true;
     }
     
     /**
      * Lay out the user interface widgets on the screen.
      */
     protected void layoutScreen() {
         this.setScreenTitle("Administer Database");
         this.setStyleName("jsc-twopanel-screen");
         
         screen = new HorizontalPanel();
         
         createClassListPanel();
         Label spacer = new Label("");
         spacer.addStyleName("jsc-spacer");
         createAdminPanel();
         screen.add(classesPanel);
         screen.add(spacer);
         screen.add(adminPanel);
     }
     
     /**
      * Fill in the GUI for the Login screen
      */
     private void createClassListPanel() {
         classesPanel = new VerticalPanel();
         classesPanel.addStyleName("jsc-leftpanel");
                 
         classesGrid = new Grid(0, 3);
         classesGrid.addStyleName("jsc-pointer-cursor");
         
         // Add a header row to the table
         Label sessionLabel = new Label("Session");
         Label classLabel = new Label("Class");
         Label dayLabel = new Label("Day");
         Widget[] labels= {sessionLabel, classLabel, dayLabel};
         addRowToGrid(classesGrid, labels);
         
         ScrollPanel scrollPanel = new ScrollPanel();
         scrollPanel.addStyleName("jsc-classes-scroll");
         scrollPanel.add(classesGrid);
         classesPanel.add(scrollPanel);
         
         addClickHandler(classesGrid);
     }
     
     /**
      * A handler that monitors the classesGrid table and responds when a new selection is made.
      * @param classesGrid the grid of classes to monitor
      */
     private void addClickHandler(Grid classesGrid) {
         classesGrid.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 HTMLTable table = (HTMLTable)event.getSource();
                 int row = table.getCellForEvent(event).getRowIndex();
                 RowFormatter rf = table.getRowFormatter();
                 rf.removeStyleName(selectedClassRowIndex, "jsc-row-selected");
                 selectedClassRowIndex = row;
                 if (row > 0) {
                     rf.addStyleName(row, "jsc-row-selected");
                 }
                 refreshClassRoster(row);
                 GWT.log("Table was clicked on row: " + row, null);
             }
         });
     }
 
     /**
      * Remove the previous list of classes and add the new list to the table whenever
      * the classListModel is changed.
      */
     private void updateClassListTable() {
         // Remove all of the rows from the table, except the header row
         int rows = classesGrid.getRowCount();
         for (int i = rows-1; i > 0; i--) {
             GWT.log("Removing table row: " + i, null);
             classesGrid.removeRow(i);
         }
         
         // Reset the selected class row marker
         selectedClassRowIndex = 0;
         
         // Add all of the new roster entries into the table
         for (SessionSkatingClass curClass : sessionClassList.getClassList()) {
             
             if (curClass == null) {
                 GWT.log("Expected SessionSkatingClass was not found in model", null);
                 break;
             }
             Label sessionLabel = new Label(curClass.getSeason() + " (" +
                     curClass.getSessionNum() + ")");
             Label classLabel = new Label(curClass.getClassType());
            String day = curClass.getDay();
            Label dayLabel = new Label(day);
            if (day.length() >= 3) {
                dayLabel = new Label(day.substring(0, 3));
            }
             Widget[] labels= {sessionLabel, classLabel, dayLabel};
             addRowToGrid(classesGrid, labels);
         }
     }
 
     public void onClassChange(SkatingClassChangeEvent event) {
         updateClassListTable();
     }
     
     /**
      * Set up the roster panel to display students in a selected class.
      */
     private void createAdminPanel() {
         adminPanel = new VerticalPanel();
         adminPanel.addStyleName("jsc-rightpanel");
         
         classLabel = new Label("Select a function from the choices below.");
         classLabel.addStyleName("jsc-step");
         adminPanel.add(classLabel);
                
         Button duplicateButton = new Button("Duplicate");
         duplicateButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 duplicateSessionClassList();
             }
         });
         duplicateButton.addStyleName("jsc-button-right");
 
         Label duplicateLabel = new Label("Duplicate all classes from a previous session into a new session.");
         duplicateLabel.addStyleName("jsc-text");
         
         // Create a table to lay out the form for duplicating the classes in a session
         Grid duplicateGrid = new Grid(0, 5);
         Label oldSeasonLabel = new Label("Old Season");
         Label oldSessionLabel = new Label("Old Session");
         Label newSeasonLabel = new Label("New Season");
         Label newSessionLabel = new Label("New Session");
         Widget[] labels= {oldSeasonLabel, oldSessionLabel, newSeasonLabel, newSessionLabel};
         addRowToGrid(duplicateGrid, labels);
         oldSeasonField = new TextBox();
         oldSeasonField.addStyleName("jsc-text-box-medium");    
         oldSessionField = new TextBox();
         oldSessionField.addStyleName("jsc-text-box-medium");    
         newSeasonField = new TextBox();
         newSeasonField.addStyleName("jsc-text-box-medium");    
         newSessionField = new TextBox();
         newSessionField.addStyleName("jsc-text-box-medium"); 
         Widget[] boxes= {oldSeasonField, oldSessionField, newSeasonField, newSessionField, duplicateButton};
         addRowToGrid(duplicateGrid, boxes);
 
         Label spacer = new Label(" ");
         spacer.addStyleName("jsc-spacer");
         adminPanel.add(spacer);
         adminPanel.add(duplicateGrid);
         Label spacer2 = new Label(" ");
         spacer2.addStyleName("jsc-spacer");
         adminPanel.add(spacer2);
     }
 
     /**
      * Add the given set of widgets to the grid table. One column is set up for
      * each widget, which is assigned to the column and assigned a CSS style.
      * 
      * @param grid the grid to which a row should be added
      * @param widgets the set of widgets to be added, one per grid column
      */
     private void addRowToGrid(Grid grid, Widget[] widgets) {
         int newRow = grid.insertRow(grid.getRowCount());
         int column = 0;
         for (Widget widget : widgets) {
             grid.setWidget(newRow, column, widget);
             column++;
         }
 
         HTMLTable.CellFormatter fmt = grid.getCellFormatter();
         for (int i = 0; i < grid.getColumnCount(); i++) {
             if (newRow == 0) {
                 // The header row has its own style
                 fmt.addStyleName(newRow, i,  "jsc-tablehead");
             } else {
                 fmt.addStyleName(newRow, i,  "jsc-tablecell");
             }
         }
     }
     
     /**
      * Update the class roster display whenever a new row is selected. The row
      * represents the row of the class listing table, which starts at 1.  
      * @param row the index in the classesGrid indicating which roster to display
      */
     private void refreshClassRoster(int row) {
         clearMessage();
         ArrayList<SessionSkatingClass> classes = sessionClassList.getClassList(); 
         final SessionSkatingClass curClass = classes.get(row-1);
         //classLabel.setText(curClass.formatClassLabel());
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
 
         // Set up the callback object.
         AsyncCallback<ArrayList<RosterEntry>> callback = new AsyncCallback<ArrayList<RosterEntry>>() {
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to get roster.", caught);
             }
 
             public void onSuccess(ArrayList<RosterEntry> newRoster) {
                 if (newRoster == null) {
                     // Failure on the remote end.
                     GWT.log("Error finding the roster.", null);
                     return;
                 } else {
                     currentRoster = newRoster;
                     updateRosterTable(newRoster, curClass);
                 }
             }
         };
 
         // Make the call to the registration service.
         regService.getClassRoster(loginSession, curClass.getClassId(), callback);
     }
     
     /**
      * Update the roster table to reflect a new class selection.  Whenever a new
      * roster needs to be displayed (usually from the user clicking
      * on a new class in the classes list), clear all of the roster rows of the 
      * table, update the class label, and then populate the table with rows from
      * the new roster.
      * @param newRoster the array of RosterEntrys to be displayed
      * @param curClass the SkatingSessionClass that is being displayed
      */
     private void updateRosterTable(ArrayList<RosterEntry> newRoster, 
             SessionSkatingClass curClass) {
         /*
         if (layoutForPrinting) {
             // Update the class label
             classLabel.setText(curClass.formatClassLabel());
         } else {
 
             classLabel.setText("Class and Roster Information");
             // Remove all of the rows from the classInfoGrid, except the header row
             int rows = classInfoGrid.getRowCount();
             for (int i = rows-1; i > 0; i--) {
                 GWT.log("Removing table row: " + i, null);
                 classInfoGrid.removeRow(i);
             }
             Label seasonLabel = new Label(curClass.getSeason());
             Label sessionLabel = new Label(Long.toString(curClass.getSessionNum()));
             Label classTypeLabel = new Label(curClass.getClassType());
             Label dayLabel = new Label(curClass.getDay());
             Label timeLabel = new Label(curClass.getTimeslot());
             Label instructorLabel = new Label(curClass.getInstructorSurName());
 
             Widget[] labels = {seasonLabel, sessionLabel, classTypeLabel,  
                     dayLabel, timeLabel, instructorLabel};
             addRowToGrid(classInfoGrid, labels);
         }
         
         // Remove all of the rows from the table, except the header row
         int rows = rosterGrid.getRowCount();
         for (int i = rows-1; i > 0; i--) {
             GWT.log("Removing table row: " + i, null);
             rosterGrid.removeRow(i);
         }
         
         // Add all of the new roster entries into the table
         for (RosterEntry entry : newRoster) {
             Widget sectionBox = null;
             if (layoutForPrinting) {
                 sectionBox = new Label(entry.getSection());
             } else {
                 sectionBox = new TextBox();
                 ((TextBox)sectionBox).setText(entry.getSection());
                 sectionBox.addStyleName("jsc-text-box-short");
             }
             
             Label skaterNameLabel = new Label(entry.getGivenname() + " " + entry.getSurname());
             Label paymentStatusLabel = new Label(entry.getPaypal_status());
 
             Label week1Label = new Label(" ");
             Label week2Label = new Label(" ");
             Label week3Label = new Label(" ");
             Label week4Label = new Label(" ");
             Label week5Label = new Label(" ");
             Label week6Label = new Label(" ");
             
             Widget levelPassedBox = null;
             if (layoutForPrinting) {
                 levelPassedBox = new Label(entry.getMaxLevel());
             } else {
                 levelPassedBox = new TextBox();
                 ((TextBox)levelPassedBox).setText(entry.getLevelpassed());
                 ((TextBox)levelPassedBox).setMaxLength(3);
                 levelPassedBox.addStyleName("jsc-text-box-short");    
             }
             
             Widget saveWidget = null;
             if (!layoutForPrinting) {
                 Button saveButton = new Button("Save");
                 final long currentRosterId = entry.getRosterid();
                 saveButton.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         requestSaveRoster(currentRosterId, event);
                     }
                 });
                 saveButton.addStyleName("jsc-button-right");
                 saveWidget = saveButton;
             } else {
                 saveWidget = new Label(" ");
             }
             
             Widget deleteWidget;
             if (!layoutForPrinting && entry.getPaypal_status().equals("Pending")) {
                 Button cancelButton = new Button("Delete");
                 final long currentPaymentId = entry.getPaymentid();
                 cancelButton.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         requestCancelInvoice(currentPaymentId);
                     }
                 });
                 cancelButton.addStyleName("jsc-button-right");
                 deleteWidget = cancelButton;
             } else {
                 deleteWidget = new Label(" ");
             }
             
             Widget rosterIdWidget = null;
             Widget paymentIdWidget = null;
             if (layoutForPrinting) {
                 rosterIdWidget = new Label(" ");
                 paymentIdWidget = new Label(" ");
             } else {
                 Hidden rosterIdHidden = new Hidden(new Long(entry.getRosterid()).toString());
                 Hidden paymentIdHidden = new Hidden(Long.toString(entry.getPaymentid()));
                 rosterIdWidget = rosterIdHidden;
                 paymentIdWidget = paymentIdHidden;
             }
             
             if (layoutForPrinting) {
                 Widget[] widgets = {sectionBox, skaterNameLabel, paymentStatusLabel, 
                         rosterIdWidget, paymentIdWidget, week3Label, week4Label, week5Label, 
                         week6Label, levelPassedBox};
                 addRowToGrid(rosterGrid, widgets);
             } else {
                 ListBox moveMenu = createClassListBox(entry.getClassid());  
                 Widget[] widgets = {sectionBox, skaterNameLabel, paymentStatusLabel, 
                         rosterIdWidget, paymentIdWidget, levelPassedBox, saveWidget, deleteWidget, moveMenu};
                 addRowToGrid(rosterGrid, widgets);
 
             }
         }
         */
     }
     
     /**
      * Handle the request to save changes to a roster entry by calling the remote
      * registration service and passing off the request data and login information.
      * @param currentRosterId the roster entry to be changed
      * @param event the event that was clicked, from which form field data can be retrieved
      */
 /*    private void requestSaveRoster(long currentRosterId, ClickEvent event) {
         Button source = (Button)event.getSource();
         Grid rosterGrid = (Grid)source.getParent();
         int row = rosterGrid.getCellForEvent(event).getRowIndex();
         TextBox tb = (TextBox)rosterGrid.getWidget(row, 5);
         String newLevel = tb.getValue();
         tb = (TextBox)rosterGrid.getWidget(row, 0);
         String newSection = tb.getValue();
         GWT.log("Section value is: " + newLevel + " " + newSection, null);
         ListBox lb = (ListBox)rosterGrid.getWidget(row, 8);
         String selectedClassId = lb.getValue(lb.getSelectedIndex());
 
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
 
         // Set up the callback object.
         AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
 
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to save the roster changes.", caught);
             }
 
             public void onSuccess(Boolean resultFlag) {
                 GWT.log("Save roster returned: " + resultFlag, null);
                 if (resultFlag) {
                     refreshClassRoster(selectedClassRowIndex);
                     setMessage("Changes saved.");
                 } else {
                     refreshClassRoster(selectedClassRowIndex);
                     setMessage("Error saving changes. Please check that you provided a valid level code (e.g., BS1, BS2).");
                 }
             }
         };
 
         // Make the call to the registration service.
         regService.saveRoster(loginSession, currentRosterId, newLevel, newSection, selectedClassId, callback);
     }*/
     
     /**
      * Put up a confirmation dialog when a request to cancel a registration
      * entry is made.
      * @param paymentid the payment to be canceled
      */
 /*    private void requestCancelInvoice(long paymentid) {
         GWT.log("CANCEL requested for invoice: " + paymentid, null);
         String prompt = "Are you sure you want to delete " +
                 "all registration entries for invoice " +
                 paymentid + "?";
         ConfirmDialog confirm = new ConfirmDialog(prompt, this);
         confirm.setIdentifier(paymentid);
         confirm.setModal(true);
         confirm.center();
         confirm.show();
     }*/
     
     /**
      * This handler method is a callback that is registered with the confirmation
      * dialog and is called when one of the two buttons is pressed -- Yes or No.
      */
     public void onClick(ClickEvent event) {
         Button source = (Button)event.getSource();
         /*ConfirmDialog confirm = (ConfirmDialog)source.getParent().getParent().getParent().getParent();
         if (source.getText().equals("Yes")) {
             long paymentid = confirm.getIdentifier();
             finishCancelInvoice(paymentid);
         }
         confirm.hide();*/
     }
     
     /**
      * After the user has confirmed that an invoice should be deleted, connect to
      * the remote service and delete the entries from the membership, roster,
      * and payment tables.
      * @param paymentid the identifier of the invoice to be deleted
      */
     /*private void finishCancelInvoice(long paymentid) {
         GWT.log("Cancel confirmed for invoice: " + paymentid, null);
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
     
         // Set up the callback object.
         AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
     
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to cancel the invoice.", caught);
             }
     
             public void onSuccess(Boolean resultFlag) {
                 GWT.log("Cancel invoice returned: " + resultFlag, null);
                 if (resultFlag) {
                     refreshClassRoster(selectedClassRowIndex);
                 }
             }
         };
     
         // Make the call to the registration service.
         regService.cancelInvoice(loginSession, paymentid, callback);
     }*/
 
     /**
      * Copy all of the classes from one session and assign them to a new session.
      * For now, the new session must already exist.
      */
     private void duplicateSessionClassList() {
         GWT.log("Duplicating class list...", null);
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
     
         // Set up the callback object.
         AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
     
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to duplicate the class list.", caught);
             }
     
             public void onSuccess(Boolean resultFlag) {
                 GWT.log("DuplicateClassList returned: " + resultFlag, null);
                 if (resultFlag) {
                     setMessage("Class duplication completed.");
                     // Update the skating class list
                     sessionClassList.refreshClassList();
                 } else {
                     setMessage("Error duplicating classes.");
                 }
             }
         };
     
         // Get values from the duplicate form text boxes
         String oldSeason = oldSeasonField.getText();
         String oldSession = oldSessionField.getText();
         String newSeason = newSeasonField.getText();
         String newSession = newSessionField.getText();
         
         // Make the call to the registration service.
         regService.duplicateSessionClassList(loginSession, oldSeason, oldSession, newSeason, newSession, callback);
     }
     
 /*    private ListBox createClassListBox(long classid) {
         ListBox classField = new ListBox();
         classField.addItem("Select new class", "0");
         
         ArrayList<SessionSkatingClass> list = sessionClassList.getClassList();
         
         if (list != null) {
             for (SessionSkatingClass curClass : list) {
                 // Only include the active session in the dropdown list
                 if (curClass.isCurrentSeason()) {
                     // Only add item to list if student is not registered
                     if (classid == curClass.getClassId()) {
                         GWT.log("Skipping class in menu: " + curClass.getClassId(), null);
                     } else {
                         String classLabel = curClass.formatShortClassLabel();
                         classField.addItem(classLabel, new Long(curClass.getClassId()).toString());
                     }
                 }
             }
         }
         return classField;
     }*/
 }
