 package mhcs.dan;
 
 import mhcs.blaed.ModuleSerializer;
 import mhcs.danielle.MinimumConfiguration;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Daniel Hammond
  */
 public class Logging {
 
     /* properties */
 
     private boolean minConfig;
 
     // delete panel widgets
     private static Label locationInfoLabel;
     private static Label damageInfoLabel;
     private static Label orientationInfo;
     private static Label typeInfoLabel;
     private static ListBox moduleListBox;
     private static VerticalPanel deletePanel;
     private static VerticalPanel deleteLeftPanel;
     private static HorizontalPanel deleteButtonPanel;
     private static Button confirmDelButton;
     private static Button closeDeleteButton;
 
     // Logging panel widgets
     private static Grid grid;
     private static TextBox codeTextBox;
     private static ListBox damageListBox;
     private static TextBox xCoorBox;
     private static TextBox yCoorBox;
     private static ListBox turnsListBox;
     private static Button enterButton;
     private static Button deleteButton;
 
     // Meta widgets
     private static HorizontalPanel backgroundPanel;
     private static DecoratorPanel loggingPanel;
     private static AbsolutePanel mainPanel;
     private static Histogram histogram;
     private static VerticalPanel rightPanel;
 
     // GPS widgets
     private static DecoratorPanel loadingWrapper;
     private static HorizontalPanel loadingPanel;
     private static ListBox testCaseList;
     private static Button addTestButton;
     private static Button saveButton;
     private static Button loadButton;
 
     /**
      * constructor for logging page.
      */
     public Logging() {
 
         makeEverything();
 
         enterButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // try adding module to the list
                 if (!ModuleList.addModule(new Module(codeTextBox.getText(),
                         damageListBox.getItemText(damageListBox.getSelectedIndex()),
                         xCoorBox.getText(), yCoorBox.getText(),
                         turnsListBox.getItemText(turnsListBox.getSelectedIndex())))) {
                     // announce if error on adding
                     Window.alert("Invalid input");
                 }
                 // update histrogram to new state of list
                 histogram.update();
                 // check if minimum already alerted
                 if (!minConfig) {
                     // if not alerted, check for possible minimum configuration
                     if (MinimumConfiguration.testMinConfig()) {
                         // alert that minimum configuration possible
                         Window.alert("Minimum configuration possible");
                         // set that alert was displayed
                         minConfig = true;
                     }
                 }
                 // reset input boxes to empty and first choice
                 codeTextBox.setText("");
                 damageListBox.setSelectedIndex(0);
                 xCoorBox.setText("");
                 yCoorBox.setText("");
                 turnsListBox.setSelectedIndex(0);
             }
         });
 
         // button to open delete interface
         deleteButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // fill list box of modules in list
                 populateModuleListBox();
                 // display delete interface
                 deletePanel.setVisible(true);
             }
         });
 
         // button to delete selected module
         confirmDelButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // remove module from list
                 ModuleList.removeModule(
                         ModuleList.getModuleByCode(moduleListBox.getItemText(moduleListBox.getSelectedIndex())));
                 // refresh list box to current list
                 populateModuleListBox();
                // set module information to empty
                 locationInfoLabel.setText("Location:");
                 damageInfoLabel.setText("Damage:");
                 orientationInfo.setText("Orientation:");
                 typeInfoLabel.setText("Type:");
                 // update histogram to current state of module list
                 histogram.update();
                 // check if minimum configuration is no longer possible
                 if (!MinimumConfiguration.testMinConfig()) {
                     // alert that minimum configuration is not possible
                     Window.alert("Minimum configuration no longer possible.");
                     // set that alert should be displayed when minimum
                     // configuration is possible
                     minConfig = false;
                 }
             }
         });
 
         // button to close delete interface
         closeDeleteButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // reset module information to empty
                 locationInfoLabel.setText("Location:");
                 damageInfoLabel.setText("Damage:");
                 orientationInfo.setText("Orientation:");
                 typeInfoLabel.setText("Type:");
                 // hide delete interface
                 deletePanel.setVisible(false);
             }
         });
 
         // module information on change of selection
         moduleListBox.addChangeHandler(new ChangeHandler() {
             public void onChange(final ChangeEvent event) {
                 // check if list is not empty
                 if (!ModuleList.isEmptyList()) {
                     // retrieve module from that of which is selected
                     Module mod =  ModuleList.getModuleByCode(moduleListBox.getItemText(moduleListBox.getSelectedIndex()));
                     // update module information panel
                     locationInfoLabel.setText("Location: (" + mod.getXCoor() + ", " + mod.getYCoor() + ")");
                     damageInfoLabel.setText("Damage: " + mod.getDamage());
                     orientationInfo.setText("Orientation: " + mod.getTurns() + " flips");
                     typeInfoLabel.setText("Type: " + mod.getType().toString());
                 }
             }
         });
 
         // button to save current module list to storage
         saveButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // create saver
                 ModuleSerializer saver = new ModuleSerializer();
                 // save list under "saveList"
                 saver.saveToLocal("saveList");
             }
         });
 
         // button to load modules from previously saved list
         loadButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // create module loader
                 ModuleSerializer loader = new ModuleSerializer();
                 // try retrieving list
                 if (!loader.retrieveModuleList("saveList")) {
                     // alert if load was unsuccessful
                     Window.alert("Failed to load modules");
                 }
                 if (MinimumConfiguration.testMinConfig()) {
                     // alert that minimum configuration possible
                     Window.alert("Minimum configuration possible");
                     // set that alert was displayed
                     minConfig = true;
                 }
                 // update histogram to current state of module list
                 histogram.update();
             }
         });
 
         // button to load data from "gps"
         addTestButton.addClickHandler(new ClickHandler() {
             public void onClick(final ClickEvent event) {
                 // load data for given test case
                 GPSDataReceiver.loadDataByNumber(testCaseList.getSelectedIndex() + 1);
                 if (MinimumConfiguration.testMinConfig()) {
                     // alert that minimum configuration possible
                     Window.alert("Minimum configuration possible");
                     // set that alert was displayed
                     minConfig = true;
                 }
                 // update histogram to current state of module list
                 histogram.update();
             }
         });
 
         // assemble the interface
         assemblePanels();
     }
 
     /**
      *
      * @return mainPanel the panel containing everything
      * for module logging
      */
     public final Widget getLoggingPage() {
         return mainPanel;
     }
 
     /**
      *
      */
     private void makeEverything() {
         minConfig = false;
         locationInfoLabel = new Label("Location:");
         damageInfoLabel = new Label("Damage:");
         orientationInfo = new Label("Orientation:");
         typeInfoLabel = new Label("Type:");
         moduleListBox = new ListBox();
         deleteLeftPanel = new VerticalPanel();
         deleteButtonPanel = new HorizontalPanel();
         deletePanel = new VerticalPanel();
         backgroundPanel = new HorizontalPanel();
         loggingPanel = new DecoratorPanel();
         mainPanel = new AbsolutePanel();
         grid = new Grid(12, 1);
         codeTextBox = new TextBox();
         damageListBox = new ListBox();
         xCoorBox = new TextBox();
         yCoorBox = new TextBox();
         turnsListBox = new ListBox();
         enterButton = new Button("Enter Module");
         deleteButton = new Button("Delete Entry");
         confirmDelButton = new Button("Delete Module");
         closeDeleteButton = new Button("Close");
         histogram = new Histogram();
         testCaseList = new ListBox();
         addTestButton = new Button("Get GPS Data");
         saveButton = new Button("Save Modules");
         loadButton = new Button("Load Modules");
         rightPanel = new VerticalPanel();
         loadingPanel = new HorizontalPanel();
         loadingWrapper = new DecoratorPanel();
     }
 
     /**
      * Places panels within each other for overall structure.
      */
     private void assemblePanels() {
         // set properties for list box in delete panel
         moduleListBox.setWidth("200px");
         moduleListBox.setVisibleItemCount(10);
 
         // add list box of modules to panel
         deleteLeftPanel.add(moduleListBox);
         // add module details to panel
         deleteLeftPanel.add(locationInfoLabel);
         deleteLeftPanel.add(damageInfoLabel);
         deleteLeftPanel.add(orientationInfo);
         deleteLeftPanel.add(typeInfoLabel);
 
         // add buttons to panel
         deleteButtonPanel.add(confirmDelButton);
         deleteButtonPanel.add(closeDeleteButton);
 
         // create delete interface
         deletePanel.addStyleName("documentClass-delete");
         deletePanel.add(new Label("Delete Module"));
         deletePanel.add(deleteLeftPanel);
         deletePanel.add(deleteButtonPanel);
         deletePanel.setVisible(false);
 
         damageListBox.addItem("undamaged");
         damageListBox.addItem("damaged");
         damageListBox.addItem("unceratin");
 
         turnsListBox.addItem("0");
         turnsListBox.addItem("1");
         turnsListBox.addItem("2");
 
         for (int i = 0; i < 10; i++) {
             testCaseList.addItem(String.valueOf(i + 1));
         }
 
         grid.setWidget(0, 0, new Label("Module Code"));
         grid.setWidget(1, 0, codeTextBox);
         grid.setWidget(2, 0, new Label("Module Damage"));
         grid.setWidget(3, 0, damageListBox);
         grid.setWidget(4, 0, new Label("X Coordinate"));
         grid.setWidget(5, 0, xCoorBox);
         grid.setWidget(6, 0, new Label("Y Coordinate"));
         grid.setWidget(7, 0, yCoorBox);
         grid.setWidget(8, 0, new Label("Turns to Upright"));
         grid.setWidget(9, 0, turnsListBox);
         grid.setWidget(10, 0, enterButton);
         grid.setWidget(11, 0, deleteButton);
 
         loadingPanel.add(testCaseList);
         loadingPanel.add(addTestButton);
         loadingPanel.add(saveButton);
         loadingPanel.add(loadButton);
 
         loggingPanel.add(grid);
 
         loadingWrapper.add(loadingPanel);
 
         rightPanel.add(histogram.get());
         rightPanel.add(loadingWrapper);
 
         backgroundPanel.add(loggingPanel);
         backgroundPanel.add(rightPanel);
 
         mainPanel.setPixelSize(800, 400);
         mainPanel.add(backgroundPanel);
         mainPanel.add(deletePanel, 50, 50);
     }
 
     /**
      * Add the modules to the list box.
      */
     private void populateModuleListBox() {
         moduleListBox.clear();
         for (Module mod : ModuleList.get()) {
             moduleListBox.addItem(mod.getCode());
         }
         for (Module mod : ModuleList.getDamaged()) {
             moduleListBox.addItem(mod.getCode());
         }
     }
 }
