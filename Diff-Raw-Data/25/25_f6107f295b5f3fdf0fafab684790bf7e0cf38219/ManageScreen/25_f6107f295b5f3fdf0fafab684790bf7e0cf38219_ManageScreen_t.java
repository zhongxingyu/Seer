 package org.jsc.client;
 
 import java.util.ArrayList;
 
 import org.jsc.client.event.SkatingClassChangeEvent;
 import org.jsc.client.event.SkatingClassChangeHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A screen that is used by coaches and administrators to manage class rosters, 
  * and to mark student level passed, and sections.
  * 
  * @author Matt Jones
  */
 public class ManageScreen extends BaseScreen implements SkatingClassChangeHandler, ClickHandler {
 
     protected static final String DOWNLOAD_SERVLET = "/download?key=";
     protected HorizontalPanel screen;
     private VerticalPanel classesPanel;
     protected VerticalPanel rosterPanel;
     private ClassListModel sessionClassList;
     private ArrayList<SessionSkatingClass> currentSeasonClasses;
     private Grid classesGrid;
     private TextBox seasonToList;
     private Anchor downloadAnchor;
     private SkaterRegistrationServiceAsync regService;
     private int selectedClassRowIndex;
     private Grid rosterGrid;
     private Label classLabel;
     private Grid classInfoGrid;
     protected ArrayList<RosterEntry> currentRoster;
     private RosterScreen roster;
     private boolean layoutForPrinting;
     private static int SAVE = 1;
     private static int ADD = 2;
     private static int DELETE = 3;
 
     /**
      * Create the Management screen for managing class rosters and levels 
      * passed. This screen is only accessed by coaches and administrators.
      * @param loginSession
      * @param eventBus
      * @param sessionClassList
      */
     public ManageScreen(LoginSession loginSession, HandlerManager eventBus, 
             ClassListModel sessionClassList, RosterScreen roster) {
         super(loginSession, eventBus);
         this.sessionClassList = sessionClassList;
         this.roster = roster;
         this.layoutForPrinting = false;
         selectedClassRowIndex = 0;
         
         this.setScreenTitle("Manage Classes");
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
     public ManageScreen(LoginSession loginSession, HandlerManager eventBus) {
         super(loginSession, eventBus);
         this.layoutForPrinting = true;
     }
     
     /**
      * Lay out the user interface widgets on the screen.
      */
     protected void layoutScreen() {
         this.setScreenTitle("Manage Classes");
         this.setStyleName("jsc-twopanel-screen");
         
         screen = new HorizontalPanel();
         
         createClassListPanel();
         Label spacer = new Label("");
         spacer.addStyleName("jsc-spacer");
         createRosterPanel();
         screen.add(classesPanel);
         screen.add(spacer);
         screen.add(rosterPanel);
     }
     
     /**
      * Fill in the GUI for the Login screen
      */
     private void createClassListPanel() {
         classesPanel = new VerticalPanel();
         classesPanel.addStyleName("jsc-leftpanel");
                 
         Label seasonLabel = new Label("Show season: ");
         seasonToList = new TextBox();
         String season = SessionSkatingClass.calculateSeason();
         ((TextBox)seasonToList).setText(season);
         seasonToList.addStyleName("jsc-text-box-medium");
         Button seasonButton = new Button("Show Season");
         seasonButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 setSeason();
             }
         });
         seasonButton.addStyleName("jsc-button-right");
         HorizontalPanel seasonPanel = new HorizontalPanel();
         seasonPanel.add(seasonLabel);
         seasonPanel.add(seasonToList);   
         seasonPanel.add(seasonButton);
         classesPanel.add(seasonPanel);
         
         Label spacer = new Label(" ");
         spacer.addStyleName("jsc-spacer");
         classesPanel.add(spacer);
 
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
      * Change the season that is being displayed in the class list, and update
      * the class list to show only that season classes.
      */
     private void setSeason() {
         clearClassInfoTable();
         clearRosterTable();
         updateClassListTable();
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
         
         // Create a new list of just the classes from the current season
         // This will be used to find the right class to refresh the roster display
         currentSeasonClasses = new ArrayList<SessionSkatingClass>();
         
         // Reset the selected class row marker
         selectedClassRowIndex = 0;
                 
         // Add all of the new class entries into the table
         for (SessionSkatingClass curClass : sessionClassList.getClassList()) {
             
             if (curClass == null) {
                 GWT.log("Expected SessionSkatingClass was not found in model", null);
                 break;
             }
             String classSeason = curClass.getSeason();
             
             if (classSeason.equals(seasonToList.getText())) {
                 
                 currentSeasonClasses.add(curClass);
                 
                 Label sessionLabel = new Label(classSeason + " (" +
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
     }
 
     public void onClassChange(SkatingClassChangeEvent event) {
         updateClassListTable();
     }
     
     /**
      * Set up the roster panel to display students in a selected class.
      */
     protected void createRosterPanel() {
         rosterPanel = new VerticalPanel();
         rosterPanel.addStyleName("jsc-rightpanel");
         
         classLabel = new Label("Select a class from the list to see the class roster.");
         classLabel.addStyleName("jsc-step");
         rosterPanel.add(classLabel);
         
        downloadAnchor = new Anchor();

         if (!layoutForPrinting) {
             int classInfoColumns = 10;
             Label seasonLabel = new Label("Season");
             Label sessionLabel = new Label("Session");
             Label classTypeLabel = new Label("Class");
             Label dayLabel = new Label("Day");
             Label timeLabel = new Label("Time");
             Label instructorLabel = new Label("Instructor");
             Label priceLabel = new Label("Price");
             Label saveButtonLabel = new Label(" ");
             Label addButtonLabel = new Label(" ");
             Label deleteButtonLabel = new Label(" ");
 
             classInfoGrid = new Grid(0, classInfoColumns);
             Widget[] labels = {seasonLabel, sessionLabel, classTypeLabel,  
                     dayLabel, timeLabel, instructorLabel, priceLabel,
                     saveButtonLabel, addButtonLabel, deleteButtonLabel};
             addRowToGrid(classInfoGrid, labels);
             
             rosterPanel.add(classInfoGrid);
         }
         
         Button printButton = new Button("Print Roster");
         printButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 printRoster();
             }
         });
         printButton.addStyleName("jsc-button-right");
         Button downloadButton = new Button("Prepare Download...");
         downloadButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 downloadRoster();
             }
         });
         downloadButton.addStyleName("jsc-button-right");
         if (!layoutForPrinting) {
             Label spacer = new Label(" ");
             spacer.addStyleName("jsc-spacer");
             rosterPanel.add(spacer);
             HorizontalPanel buttonPanel = new HorizontalPanel();
             Label spacer2 = new Label(" ");
             spacer2.addStyleName("jsc-spacer");
             Label spacer3 = new Label(" ");
             spacer3.addStyleName("jsc-spacer");
             buttonPanel.add(printButton);
             buttonPanel.add(spacer2);
             buttonPanel.add(downloadButton);
             buttonPanel.add(spacer3);
             buttonPanel.add(downloadAnchor);
 
             rosterPanel.add(buttonPanel);
             Label spacer4 = new Label(" ");
             spacer4.addStyleName("jsc-spacer");
             rosterPanel.add(spacer4);
         }
         
         int columns = 9;
         if (layoutForPrinting) {
             columns += 1;
         }
         rosterGrid = new Grid(0, columns);
         
         // Add a header row to the table
         Label sectionLabel = new Label("Section");
         Label skaterNameLabel = new Label("Skater");
         Label statusLabel = new Label("Payment");
         Widget w1, w2, w3, w4, w5, w6;
         Label moveLabel =new Label("Move to:");
         if (layoutForPrinting) {
             w1 = new Label("Wk 1");
             w2 = new Label("Wk 2");
             w3 = new Label("Wk 3");
             w4 = new Label("Wk 4");
             w5 = new Label("Wk 5");
             w6 = new Label("Wk 6");
         } else {
             w1 = new Hidden("rosterid");
             w2 = new Hidden("paymentid");
             w3 = new Hidden(" ");
             w4 = new Hidden(" ");
             w5 = new Hidden(" ");
             w6 = new Hidden(" ");
         }
    
         Label levelPassedLabel = null;
         if (layoutForPrinting) {
             levelPassedLabel = new Label("Max Level");
         } else {
             levelPassedLabel = new Label("Level Passed");
         }
         Label saveLabel = new Label(" ");
         Label deleteLabel = new Label(" ");
         
         if (layoutForPrinting) {
             Widget[] widgets = {sectionLabel, skaterNameLabel, statusLabel,  
                     w1, w2, w3, w4, w5, w6, levelPassedLabel};
             addRowToGrid(rosterGrid, widgets);
         } else {
             Widget[] widgets = {sectionLabel, skaterNameLabel, statusLabel,  
                     w1, w2, levelPassedLabel, saveLabel, deleteLabel, moveLabel};
             addRowToGrid(rosterGrid, widgets);
         }
         rosterPanel.add(rosterGrid);
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
         final SessionSkatingClass curClass = currentSeasonClasses.get(row-1);
         
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
     protected void updateRosterTable(ArrayList<RosterEntry> newRoster, 
             SessionSkatingClass curClass) {
         if (layoutForPrinting) {
             // Update the class label
             classLabel.setText(curClass.formatClassLabel());
             downloadAnchor.setText("");
             downloadAnchor.setHref("");
         } else {
 
             classLabel.setText("Class and Roster Information");
             downloadAnchor.setText("");
             downloadAnchor.setHref("");
 
             // Remove all of the rows from the classInfoGrid, except the header row
             clearClassInfoTable();
 
             Widget seasonWidget = null;
             Widget sessionWidget = null;
             Widget classTypeWidget = null;
             Widget dayWidget = null;
             Widget timeWidget = null;
             Widget instructorWidget = null;
             Widget priceWidget = null;
             Widget saveButtonWidget = null;
             
             if (loginSession.getPerson().getRole() >= Person.ADMIN) {
                 seasonWidget = new TextBox();
                 ((TextBox)seasonWidget).setText(curClass.getSeason());
                 seasonWidget.addStyleName("jsc-text-box-medium");
                 
                 sessionWidget = new TextBox();
                 ((TextBox)sessionWidget).setText(Long.toString(curClass.getSessionNum()));
                 sessionWidget.addStyleName("jsc-text-box-medium");
                 
                 classTypeWidget = new TextBox();
                 ((TextBox)classTypeWidget).setText(curClass.getClassType());
                 classTypeWidget.addStyleName("jsc-text-box-medium");
                 
                 dayWidget = new TextBox();
                 ((TextBox)dayWidget).setText(curClass.getDay());
                 dayWidget.addStyleName("jsc-text-box-medium");
                 
                 timeWidget = new TextBox();
                 ((TextBox)timeWidget).setText(curClass.getTimeslot());
                 timeWidget.addStyleName("jsc-text-box-medium");
                 
                 instructorWidget = new TextBox();
                 ((TextBox)instructorWidget).setText(curClass.getInstructorGivenName() + " " + curClass.getInstructorSurName());
                 instructorWidget.addStyleName("jsc-text-box-medium");
                 
                 priceWidget = new TextBox();
                 NumberFormat numfmt = NumberFormat.getFormat("###0.00");
                 ((TextBox)priceWidget).setText(numfmt.format(curClass.getCost()));
                 priceWidget.addStyleName("jsc-text-box-medium");
                 
                 Button saveClassButton = new Button("Save");
                 final long currentClassId = curClass.getClassId();
                 saveClassButton.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         GWT.log("Save class button clicked.");
                         requestClassChange(currentClassId, event);
                     }
                 });
                 saveClassButton.addStyleName("jsc-button-right");
                                 
                 Button addClassButton = new Button("Add");
                 addClassButton.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         GWT.log("Add class button clicked.");
                         requestClassChange(currentClassId, event);
                     }
                 });
                 addClassButton.addStyleName("jsc-button-right");
                 
                 Button deleteClassButton = new Button("Delete");
                 deleteClassButton.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         GWT.log("Delete class button clicked.");
                         requestClassChange(currentClassId, event);
                     }
                 });
                 deleteClassButton.addStyleName("jsc-button-right");
                 
                 Widget[] labels = {seasonWidget, sessionWidget, classTypeWidget,  
                         dayWidget, timeWidget, instructorWidget, priceWidget, saveClassButton,
                         addClassButton, deleteClassButton};
                 addRowToGrid(classInfoGrid, labels);
                 
             } else {
                 seasonWidget = new Label(curClass.getSeason());
                 sessionWidget = new Label(Long.toString(curClass.getSessionNum()));
                 classTypeWidget = new Label(curClass.getClassType());
                 dayWidget = new Label(curClass.getDay());
                 timeWidget = new Label(curClass.getTimeslot());
                 instructorWidget = new Label(curClass.getInstructorGivenName() + " " + curClass.getInstructorSurName());
                 NumberFormat numfmt = NumberFormat.getFormat("$#,##0.00");
                 priceWidget = new Label(numfmt.format(curClass.getCost()));
                 Widget[] labels = {seasonWidget, sessionWidget, classTypeWidget,  
                         dayWidget, timeWidget, instructorWidget, priceWidget};
                 addRowToGrid(classInfoGrid, labels);
             }
         }
         
         clearRosterTable();
         
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
     }
 
     /**
      * Remove the rows from the class information display in preparation to 
      * update the information.
      */
     private void clearClassInfoTable() {
         // Remove all of the rows from the classInfoGrid, except the header row
         int rows = classInfoGrid.getRowCount();
         for (int i = rows-1; i > 0; i--) {
             GWT.log("Removing table row: " + i, null);
             classInfoGrid.removeRow(i);
         }
     }
 
     /**
      * Remove rows from the roster table before updating it.
      */
     private void clearRosterTable() {
         // Remove all of the rows from the table, except the header row
         int rows = rosterGrid.getRowCount();
         for (int i = rows-1; i > 0; i--) {
             GWT.log("Removing table row: " + i, null);
             rosterGrid.removeRow(i);
         }
     }
     
     /**
      * Handle the request to save changes to a class by calling the remote
      * registration service and passing off the request data and login information.
      * @param currentClassId the class to be changed
      * @param event the event that was clicked, from which form field data can be retrieved
      */
     private void requestClassChange(long currentClassId, ClickEvent event) {
         Button source = (Button)event.getSource();
         Grid rosterGrid = (Grid)source.getParent();
         ArrayList<String> newClassValues = new ArrayList<String>();
         int row = rosterGrid.getCellForEvent(event).getRowIndex();
         // Loop through the text boxes and put the values in an array
         // season, session, class, day, time, instructor
         for (int i=0; i<7; i++) {
             TextBox tb = (TextBox)rosterGrid.getWidget(row, i);
             newClassValues.add(tb.getValue());
         }
 
         final int operation;
         if (source.getText().equals("Save")) {
             GWT.log("Got save button", null);
             operation = SAVE;
         } else if (source.getText().equals("Add")) {
             GWT.log("Got add button", null);
             operation = ADD;
         } else if (source.getText().equals("Delete")) {
             GWT.log("Got delete button", null);
             operation = DELETE;
         } else {
             GWT.log("Invalid button choice", null);
             return;
         }
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
 
         // Set up the callback object.
         AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
 
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to save the class changes.", caught);
             }
 
             public void onSuccess(Boolean resultFlag) {
                 GWT.log("Save class returned: " + resultFlag, null);
                 if (resultFlag) {
                     sessionClassList.refreshClassList();
 //                    if (operation==DELETE) {
 //                        SessionSkatingClass curClass = currentSeasonClasses.get(0);
 //                        roster.updateRosterTable(currentRoster, curClass);
 //                    }
                     setMessage("Class changes saved.");
                 } else {
                     setMessage("Error saving class changes. Check your entries and try again.");
                 }
             }
         };
         
         // Make the call to the registration service.
         regService.saveSkatingClass(loginSession, currentClassId, newClassValues, operation, callback);
     }
     
     /**
      * Handle the request to save changes to a roster entry by calling the remote
      * registration service and passing off the request data and login information.
      * @param currentRosterId the roster entry to be changed
      * @param event the event that was clicked, from which form field data can be retrieved
      */
     private void requestSaveRoster(long currentRosterId, ClickEvent event) {
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
                 refreshClassRoster(selectedClassRowIndex);
                 if (resultFlag) {
                     setMessage("Changes saved.");
                 } else {
                     setMessage("Error saving changes. Please check that you provided a valid level code (e.g., BS1, BS2).");
                 }
             }
         };
 
         // Make the call to the registration service.
         regService.saveRoster(loginSession, currentRosterId, newLevel, newSection, selectedClassId, callback);
     }
     
     /**
      * Put up a confirmation dialog when a request to cancel a registration
      * entry is made.
      * @param paymentid the payment to be canceled
      */
     private void requestCancelInvoice(long paymentid) {
         GWT.log("CANCEL requested for invoice: " + paymentid, null);
         String prompt = "Are you sure you want to delete " +
                 "all registration entries for invoice " +
                 paymentid + "?";
         ConfirmDialog confirm = new ConfirmDialog(prompt, this);
         confirm.setIdentifier(paymentid);
         confirm.setModal(true);
         confirm.center();
         confirm.show();
     }
     
     /**
      * This handler method is a callback that is registered with the confirmation
      * dialog and is called when one of the two buttons is pressed -- Yes or No.
      */
     public void onClick(ClickEvent event) {
         Button source = (Button)event.getSource();
         ConfirmDialog confirm = (ConfirmDialog)source.getParent().getParent().getParent().getParent();
         if (source.getText().equals("Yes")) {
             long paymentid = confirm.getIdentifier();
             finishCancelInvoice(paymentid);
         }
         confirm.hide();
     }
     
     /**
      * After the user has confirmed that an invoice should be deleted, connect to
      * the remote service and delete the entries from the membership, roster,
      * and payment tables.
      * @param paymentid the identifier of the invoice to be deleted
      */
     private void finishCancelInvoice(long paymentid) {
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
     }
 
     /**
      * Switch to the screen for printing rosters after updating it by passing in
      * the currently selected roster and class.
      */
     private void printRoster() {
         GWT.log("Print roster rows: " + currentRoster.size(), null);
         // Switch to the screen for printing rosters, but before doing so
         // update it by passing in the currentRoster to be displayed along with 
         // a reference to the current selected class to be displayed
         SessionSkatingClass curClass = currentSeasonClasses.get(selectedClassRowIndex-1);
         roster.updateRosterTable(currentRoster, curClass);
         History.newItem("roster");
     }
     
     private void downloadRoster() {
         GWT.log("Download roster rows: " + currentRoster.size(), null);
         // Switch to the screen for printing rosters, but before doing so
         // update it by passing in the currentRoster to be displayed along with 
         // a reference to the current selected class to be displayed
         SessionSkatingClass curClass = currentSeasonClasses.get(selectedClassRowIndex-1);
         long classId = curClass.getClassId();
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
     
         // Set up the callback object.
         AsyncCallback<Long> callback = new AsyncCallback<Long>() {
     
             public void onFailure(Throwable caught) {
                 // TODO: Do something with errors.
                 GWT.log("Failed to download the roster.", caught);
             }
     
             public void onSuccess(Long downloadKey) {
                 GWT.log("Download roster returned: " + downloadKey, null);
                // Create an Anchor link with this URL
                 String href = DOWNLOAD_SERVLET + downloadKey;
                 downloadAnchor.setHref(href);
                 downloadAnchor.setText("Download file...");
             }
         };
         
         // Make the call to the registration service.
         regService.downloadRoster(loginSession, classId, callback);
     }
     
     /**
      * Construct a list box of the classes to be listed.
      * @param classid the class that they are already registered for
      * @return ListBox for use in the GUI
      */
     private ListBox createClassListBox(long classid) {
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
     }
 }
