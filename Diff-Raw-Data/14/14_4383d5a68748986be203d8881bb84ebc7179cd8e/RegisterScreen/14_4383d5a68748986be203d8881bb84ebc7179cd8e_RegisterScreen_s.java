 package org.jsc.client;
 
 import java.util.ArrayList;
import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A set of panels to allow users to register for a class.  Collects the information
  * needed for registration, and then redirects the user to PayPal for payment.
  * @author Matt Jones
  */
 public class RegisterScreen extends BaseScreen implements ValueChangeHandler<Boolean>, ChangeHandler {
 
     private static final String DISCOUNT_EXPLANATION = "<p class=\"jsc-text\">Because it helps with planning our class sizes, <b>we offer a discount for those who register early</b>.</p>";
     private static final String PRICE_EXPLANATION = "<div id=\"explainstep\"><p class=\"jsc-text\">After you choose a class, you will be prompted to make payment through PayPal.</p>";
     private static final String DISCOUNT_LABEL_PREFIX = "Discount deadline: ";
     private static final String PAYPAL_EXPLANATION = "<div id=\"explainstep\"><p class=\"jsc-text\">You must make your payment using PayPal by clicking on the button below.  <b>Your registration is <em>not complete</em></b> until after you have completed payment.</p><p class=\"jsc-text\">When you click \"Pay Now\" below, you will be taken to the PayPal site to make payment.  PayPal will allow you to pay by credit card or using your bank account, among other options.  Once the payment has been made, you will be returned to this site and your registration will be complete.</p></div>";
     private static final String BS_EXPLANATION = "Basic Skills is our Learn to Skate program. These group lessons are based on the United States Figure Skating's (USFSA) Basic Skills Program. This is a nationwide, skills-based, graduated series of instruction for youth and adult skaters. This program is designed to teach all skaters the fundamentals of skating.";
     private static final String FS_EXPLANATION = "Figure Skating is a one- to five-day-a-week program for skaters that have completed all of the Basic Skills Levels (8) or Adult Levels (4). Students work individually with their coach to develop their skills. Please only sign up for Figure Skating Classes only if you have permission from the Figure Skating Coordinator.";
 
     private static final String STEP_1 = "Step 1: Choose a class";
     private static final String STEP_2 = "Step 2: Process payment";
     
     private ClassListModel sessionClassList;
     private RosterModel studentRoster;
     
     private HorizontalPanel screen;
     private VerticalPanel outerVerticalPanel;
     private HorizontalPanel outerHorizPanel;
     private RadioButton bsRadio;
     private RadioButton fsRadio;
     private VerticalPanel bsClassChoicePanel;
     private VerticalPanel ppPaymentPanel;
     private VerticalPanel bsPanel;
     private VerticalPanel fsPanel;
     private Label stepLabel;
     private Grid basicSkillsGrid;
     private Grid figureSkatingGrid;
     private Label feeLabel;
     private ListBox classField;
     private Button registerButton;
     private SkaterRegistrationServiceAsync regService;
     private CheckBox memberCheckbox;
     private Label memberDues;
     private double totalFSCost;
     private Label totalCostLabel;
     private double total;
     private Label memberCheckboxLabel;
     private HashSet<String> fsClassesToRegister;
     private Label fsDiscountLabel;
     private Label discountDateLabel;
     private NumberFormat numfmt;
     private Label bsTotalLabel;
     private Label bsDiscountLabel;
     
     /**
      * Construct the Registration view and controller used to display a form for
      * registering for skating classes.
      * @param loginSession the authenticated login session for submissions to the remote service
      * @param sessionClassList the model of skating classes
      */
     public RegisterScreen(LoginSession loginSession, HandlerManager eventBus, ClassListModel sessionClassList, RosterModel studentRoster) {
         super(loginSession, eventBus);
         this.sessionClassList = sessionClassList;
 //        sessionClassLabels = new TreeMap<String, String>();
         this.studentRoster = studentRoster;
         numfmt = NumberFormat.getFormat("$#,##0.00");
         fsClassesToRegister = new HashSet<String>();
         totalFSCost = 0;
         layoutScreen();
         this.setContentPanel(screen);
         regService = GWT.create(SkaterRegistrationService.class);
         
 //        // Register as a handler for Skating class changes, and handle those changes
 //        eventBus.addHandler(RosterChangeEvent.TYPE, new RosterChangeHandler(){
 //            public void onRosterChange(RosterChangeEvent event) {
 //                studentRoster = event.getRoster();
 //            }
 //        });
     }
     
     /**
      * Lay out the user interface widgets on the screen.
      */
     private void layoutScreen() {
         this.setScreenTitle("Register");
         this.setStyleName("jsc-onepanel-screen");
         
         screen = new HorizontalPanel();
                         
         layoutBsPanel();
         
         layoutFsPanel();
         
         ppPaymentPanel = new VerticalPanel();
         ppPaymentPanel.setVisible(false);
         
         registerButton = new Button("Continue");
         registerButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 register();
             }
         });
         registerButton.addStyleName("jsc-button-right");
         
         outerVerticalPanel = new VerticalPanel();
         stepLabel = new Label(STEP_1);
         stepLabel.addStyleName("jsc-step");
         outerVerticalPanel.add(stepLabel);
         
         bsRadio = new RadioButton("BSorFSGroup", "Basic Skills Classes");
         bsRadio.addValueChangeHandler(this);
         bsRadio.setValue(true);
         fsRadio = new RadioButton("BSorFSGroup", "Figure Skating Classes (only by permission of the Figure Skating Coordinator)");
         fsRadio.addValueChangeHandler(this);
         outerVerticalPanel.add(bsRadio);
         outerVerticalPanel.add(fsRadio);
         
         outerHorizPanel = new HorizontalPanel();
         outerHorizPanel.add(bsPanel);
         outerHorizPanel.add(fsPanel);
         outerHorizPanel.add(ppPaymentPanel);
         outerVerticalPanel.add(outerHorizPanel);
         outerVerticalPanel.add(registerButton);
         outerVerticalPanel.addStyleName("jsc-rightpanel");
         screen.add(outerVerticalPanel);
     }
 
     /**
      * Layout the user interface widgets for the basic skills screen.
      */
     private void layoutBsPanel() {
         bsPanel = new VerticalPanel();
         Label bscTitle = new Label("Basic Skills Classes");
         bscTitle.addStyleName("jsc-fieldlabel-left");
         bsPanel.add(bscTitle);
         Label bscDescription = new Label(BS_EXPLANATION);
         bscDescription.addStyleName("jsc-text");
         bsPanel.add(bscDescription);
 
         basicSkillsGrid = new Grid(0, 4);
         
         addToBSGrid(" ", new Label(" "));
         
         classField = new ListBox();
         classField.setVisibleItemCount(1);
         classField.addChangeHandler(this);
         addToBSGrid("Class:", classField);
         int currentRow = basicSkillsGrid.getRowCount() - 1;
         feeLabel = new Label("");
         basicSkillsGrid.setWidget(currentRow, 2, new Label("Cost"));
         basicSkillsGrid.setWidget(currentRow, 3, feeLabel);
         HTMLTable.CellFormatter fmt = basicSkillsGrid.getCellFormatter();
         fmt.addStyleName(currentRow, 2,  "jsc-fieldlabel");
         fmt.addStyleName(currentRow, 3,  "jsc-currencyfield");
         
         bsDiscountLabel = new Label();
         int newRow = basicSkillsGrid.insertRow(basicSkillsGrid.getRowCount());
         basicSkillsGrid.setWidget(newRow, 2, new Label("Discount"));
         basicSkillsGrid.setWidget(newRow, 3, bsDiscountLabel);
         fmt.addStyleName(newRow, 2,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 3,  "jsc-currencyfield");
         
         bsTotalLabel = new Label();
         newRow = basicSkillsGrid.insertRow(basicSkillsGrid.getRowCount());
         basicSkillsGrid.setWidget(newRow, 2, new Label("Total"));
         basicSkillsGrid.setWidget(newRow, 3, bsTotalLabel);
         fmt.addStyleName(newRow, 2,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 3,  "jsc-currencyfield");
         
         bsClassChoicePanel = new VerticalPanel();
         StringBuffer sb = new StringBuffer(PRICE_EXPLANATION);
         sb.append(DISCOUNT_EXPLANATION);
         //Date discountDate = getDiscountDate();
         bsClassChoicePanel.add(new HTMLPanel(sb.toString()));
         discountDateLabel = new Label(DISCOUNT_LABEL_PREFIX);
         bsClassChoicePanel.add(discountDateLabel);
         bsClassChoicePanel.add(basicSkillsGrid);
 
         bsPanel.add(bsClassChoicePanel);
     }
 
     /**
      * Create the widgets needed to populate the figure skating registration panel.
      */
     private void layoutFsPanel() {
         
         // Create the panel, its labels, and the contained grid for layout
         fsPanel = new VerticalPanel();
         Label fscTitle = new Label("Figure Skating Classes");
         fscTitle.addStyleName("jsc-fieldlabel-left");
         fsPanel.add(fscTitle);
         Label fscDescription = new Label(FS_EXPLANATION);
         fscDescription.addStyleName("jsc-text");
         fsPanel.add(fscDescription);
         figureSkatingGrid = new Grid(0, 6);
         
         // Insert the first row containing the membership fields
         int newRow = figureSkatingGrid.insertRow(figureSkatingGrid.getRowCount());
         memberCheckbox = new CheckBox();
         memberCheckboxLabel = new Label("Pay membership dues");
         memberDues = new Label();
         double zero = 0;
         memberDues.setText(numfmt.format(zero));
         memberCheckbox.setValue(false, false);
         memberCheckbox.addValueChangeHandler(this);
         figureSkatingGrid.setWidget(newRow, 2, memberCheckbox);
         figureSkatingGrid.setWidget(newRow, 3, memberCheckboxLabel);
         figureSkatingGrid.setWidget(newRow, 4, new Label("Dues"));
         figureSkatingGrid.setWidget(newRow, 5, memberDues);
         HTMLTable.CellFormatter fmt = figureSkatingGrid.getCellFormatter();
         fmt.addStyleName(newRow, 2,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 3,  "jsc-field");
         fmt.addStyleName(newRow, 4,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 5,  "jsc-currencyfield");
         
         // Insert the next to last row containing the discount amount
         newRow = figureSkatingGrid.insertRow(figureSkatingGrid.getRowCount());
         fsDiscountLabel = new Label();
         fsDiscountLabel.setText(numfmt.format(zero));
         figureSkatingGrid.setWidget(newRow, 4, new Label("Discount"));
         figureSkatingGrid.setWidget(newRow, 5, fsDiscountLabel);
         fmt.addStyleName(newRow, 4,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 5,  "jsc-currencyfield");
         
         // Insert the last row containing the payment total
         newRow = figureSkatingGrid.insertRow(figureSkatingGrid.getRowCount());
         totalCostLabel = new Label();
         totalCostLabel.setText(numfmt.format(zero));
         figureSkatingGrid.setWidget(newRow, 4, new Label("Total"));
         figureSkatingGrid.setWidget(newRow, 5, totalCostLabel);
         fmt.addStyleName(newRow, 4,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 5,  "jsc-currencyfield");
         
         fsPanel.add(figureSkatingGrid);
         fsPanel.setVisible(false);
     }
     
     /**
      * Add the given widget to the grid table along with a label.  The Label is
      * placed in column 1 of the grid, and the widget in column 2.
      * @param label the label to display in column 1
      * @param widget the widget to display in column 2
      */
     private void addToBSGrid(String label, Widget widget) {
         int newRow = basicSkillsGrid.insertRow(basicSkillsGrid.getRowCount());
         basicSkillsGrid.setWidget(newRow, 0, new Label(label));
         basicSkillsGrid.setWidget(newRow, 1, widget);
         HTMLTable.CellFormatter fmt = basicSkillsGrid.getCellFormatter();
         fmt.addStyleName(newRow, 0,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 1,  "jsc-field");
     }
     
     /**
      * Add the given FSClassCheckBox to the Figure Skating grid table along with a label.  
      * The Label is placed in column 2 of the grid, and the checkbox in column 1. 
      * FSClassCheckBox also contains a label that can be used to display the price
      * of a figure skating class, and this label is placed in column 5.
      *  
      * @param label the label to display in column 2
      * @param widget the FSClassCheckBox to display in column 1
      */
     private void addToFSGrid(String label, FSClassCheckBox widget) {
         int newRow = figureSkatingGrid.insertRow(figureSkatingGrid.getRowCount()-2);
         figureSkatingGrid.setWidget(newRow, 0, widget);
         figureSkatingGrid.setWidget(newRow, 1, new Label(label));
         figureSkatingGrid.setWidget(newRow, 5, widget.getClassPriceLabel());
         HTMLTable.CellFormatter fmt = figureSkatingGrid.getCellFormatter();
         fmt.addStyleName(newRow, 0,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 1,  "jsc-field");
         fmt.addStyleName(newRow, 5,  "jsc-currencyfield");
     }
 
     /**
      * Remove the current list of classes from the box and replace with the 
      * classes that are currently present in sessionClassList. Reset the 
      * registration form to the initial state of the application.
      */
     protected void updateRegistrationScreenDetails() {
         // Reset the form fields to begin registration
         ppPaymentPanel.setVisible(false);
         stepLabel.setText(STEP_1);
         bsRadio.setVisible(true);
         fsRadio.setVisible(true);
         if (bsRadio.getValue()) {
             bsPanel.setVisible(true);
             fsPanel.setVisible(false);
         } else {
             bsPanel.setVisible(false);
             fsPanel.setVisible(true);
         }
         registerButton.setVisible(true);
         
         // Clear the list of basic skills classes to reflect the new data
         classField.clear();
         
         // Clear the set of fsClasses to be registered, and the form totals
         fsClassesToRegister.clear();
         totalFSCost = 0;
         total = 0;
         
         // Remove all of the rows from the FS grid table except the first and last two rows
         int rows = figureSkatingGrid.getRowCount();
         for (int i = rows-3; i > 0; i--) {
             GWT.log("Removing FS table row: " + i, null);
             figureSkatingGrid.removeRow(i);
         }
 
         // Iterate over the new list of classes, putting each class into either
         // the dropdown for Basic Skills classes or the checkbox list for 
         // Figure Skating classes
         ArrayList<SessionSkatingClass> list = sessionClassList.getClassList();
         if (list != null) {
             for (SessionSkatingClass curClass : list) {
                 GWT.log("SessionClass: " + 
                         (new Long(curClass.getClassId()).toString()) + " " + 
                         curClass.getClassType() + " (" + curClass.getCost() + ")", null);
                 String classLabel = curClass.formatClassLabel();
                 
                 // If it starts with "FS " it is a figure skating class
                 // Only include the active session in the dropdown list
                 if (curClass.isActiveSession() && curClass.getClassType().startsWith("FS ")) {
                     FSClassCheckBox checkbox = new FSClassCheckBox();
                     checkbox.setValue(false, false);
                     checkbox.setName(Long.toString(curClass.getClassId()));
                     //checkbox.setClassPrice(curClass.getCost());
                     checkbox.addValueChangeHandler(this);
                     addToFSGrid(classLabel, checkbox);
                     // Disable checkboxes if student is already registered
                     if (studentRoster.contains(loginSession.getPerson().getPid(),
                             curClass.getClassId())) {
                         GWT.log("Disabling FS class: " + curClass.getClassId(), null);
                         checkbox.setEnabled(false);
                     }
                     
                 // Otherwise it is a Basic Skills class, but only include if its the active session
                 } else if (curClass.isActiveSession()) {
                     // Only add item to list if student is not registered
                     if (studentRoster.contains(loginSession.getPerson().getPid(),
                             curClass.getClassId())) {
                         GWT.log("Disabling BS class: " + curClass.getClassId(), null);
                     } else {
                         classField.addItem(classLabel, new Long(curClass.getClassId()).toString());
                     }
                 }
             }
         }
         
         // Update the membership checkbox status based on the Person logged in
         if (loginSession.getPerson().isMember()) {
             memberCheckboxLabel.setText("Membership dues already paid. Discount applies.");
             memberCheckbox.setValue(false);
             memberCheckbox.setEnabled(false);
             double dues = 0;
             String duesString = numfmt.format(dues);
             memberDues.setText(duesString);
         } else {
             memberCheckboxLabel.setText("Pay membership dues");
             memberCheckbox.setValue(false);
             memberCheckbox.setEnabled(true);
         }
         
         recalculateAndDisplayBasicSkillsTotal();
         recalculateAndDisplayFSTotals();
     }
 
     /**
      * Set the price and discount for the Basic Skills form by recalculating the
      * discount.
      */
     private void recalculateAndDisplayBasicSkillsTotal() {
         // Update the cost discount, and total fields on the BS Form
         String selectedClassId = classField.getValue(classField.getSelectedIndex());
         double classCost = sessionClassList.getSkatingClass(new Long(selectedClassId)).getCost();
         feeLabel.setText(numfmt.format(classCost));
         Date discountDate = getDiscountDate();
         GWT.log("Discount date is: " + discountDate, null);
         Date discountDisplay = discountDate;
         discountDisplay.setTime( discountDate.getTime() - AppConstants.MILLISECS_PER_DAY );
         String discountString = DateTimeFormat.getLongDateFormat().format(discountDisplay);
         discountDateLabel.setText(DISCOUNT_LABEL_PREFIX + discountString);
         double bsDiscount = calculateBSDiscount(discountDate);
         bsDiscountLabel.setText(numfmt.format(bsDiscount));
         double bsTotal = classCost - bsDiscount;
         bsTotalLabel.setText(numfmt.format(bsTotal));
     }
 
     /**
      * Find the discount date for the first class in the active session.
      * @return the Date which represents the Discount date
      */
     private Date getDiscountDate() {
         ArrayList<SessionSkatingClass> classes = sessionClassList.getClassList();
         String sessionDiscount = "";
         for (SessionSkatingClass curClass : classes) {
             if (curClass.isActiveSession()) {
                 sessionDiscount = curClass.getDiscountDate();
                 break;
             }
         }
         Date discountDate = null;
         if (sessionDiscount != null) {
             DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
             discountDate = fmt.parse(sessionDiscount);
         }
         return discountDate;
     }
 
     /**
      * Register for a class by creating a RosterEntry object from the form input
      * and pass it to the remote registration service.
      */
     private void register() {
         GWT.log("Registering for a class...", null);
         
         if (loginSession.isAuthenticated()) {
             // This is the array of roster entries that should be created in the db
             ArrayList<RosterEntry> entryList = new ArrayList<RosterEntry>();
             
             boolean createMembership = false;
             // Gather information from the Basic Skills form if it is selected
             if (bsRadio.getValue()) {
                 String selectedClassId = classField.getValue(classField.getSelectedIndex());
                 double classCost = sessionClassList.getSkatingClass(new Long(selectedClassId)).getCost();
                 Person registrant = loginSession.getPerson();
                 RosterEntry entry = new RosterEntry();
                 entry.setClassid(new Long(selectedClassId).longValue());
                 entry.setPid(registrant.getPid());
                 entry.setPayment_amount(classCost);
                 entryList.add(entry);
                 
             // Otherwise gather information from the Figure Skating form if it is selected
             } else if (fsRadio.getValue()) {
                 if (memberCheckbox.getValue() == true &! loginSession.getPerson().isMember()) {
                     createMembership = true;
                 }
                 // Loop through the checked classes, creating a RosterEntry for each
                 for (String selectedClassId : fsClassesToRegister) {
                     GWT.log("Need to register class: " + selectedClassId, null);
                     long classId = new Long(selectedClassId).longValue();
                     Person registrant = loginSession.getPerson();
                     RosterEntry entry = new RosterEntry();
                     entry.setClassid(classId);
                     entry.setPid(registrant.getPid());
                     double classCost = sessionClassList.getSkatingClass(classId).getCost();
                     entry.setPayment_amount(classCost);
                     entryList.add(entry);
                 }
             } else {
                 GWT.log("Neither BS nor FS form is active. This shouldn't happen!", null);
                 return;
             }
                         
             // Initialize the service proxy.
             if (regService == null) {
                 regService = GWT.create(SkaterRegistrationService.class);
             }
 
             // Set up the callback object.
             AsyncCallback<RegistrationResults> callback = new AsyncCallback<RegistrationResults>() {
 
                 public void onFailure(Throwable caught) {
                     // TODO: Do something with errors.
                     GWT.log("Failed to register the RosterEntry array.", caught);
                 }
 
                 public void onSuccess(RegistrationResults results) {
                     
                     ArrayList<RosterEntry> newEntryList = results.getEntriesCreated();
                     
                     if ((newEntryList == null || newEntryList.size() == 0) && !results.isMembershipAttempted()) {
                         // Failure on the remote end.
                         setMessage("Error registering... Have you are already registered for these classes? Check 'My Classes'.");
                         return;
                     } else {
                         studentRoster.refreshRoster();
                         if (results.isMembershipCreated()) {
                             loginSession.getPerson().setMember(true);
                             loginSession.getPerson().setMembershipId(results.getMembershipId());
                             loginSession.getPerson().setMembershipStatus(results.getMembershipStatus());
                         }
                         double discount = 0;
                         if (bsRadio.getValue()) {
                             Date discountDate = null;
                             ArrayList<SessionSkatingClass> classes = sessionClassList.getClassList();
                             String sessionDiscount = "";
                             for (SessionSkatingClass curClass : classes) {
                                 if (curClass.isActiveSession()) {
                                     sessionDiscount = curClass.getDiscountDate();
                                     break;
                                 }
                             }
                             if (sessionDiscount != null) {
                                 DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
                                 discountDate = fmt.parse(sessionDiscount);
                             }
                             GWT.log("Discount date is: " + discountDate, null);
                             discount = calculateBSDiscount(discountDate);
                         } else {
                             boolean isMember = results.isMembershipCreated() || loginSession.getPerson().isMember();
                             discount = calculateFSDiscount(results.getEntriesCreated().size(), isMember);
                         }
                         StringBuffer ppCart = createPayPalForm(results, sessionClassList, discount);
                         
                         registerButton.setVisible(false);
                         ArrayList<Long> entriesFailed = results.getEntriesNotCreated();
                         if (entriesFailed.size() > 0) {
                             setMessage("You were already registered for " + entriesFailed.size() +
                                     " classes, which were excluded.");
                         }
                         stepLabel.setText(STEP_2);
                         bsRadio.setVisible(false);
                         fsRadio.setVisible(false);
                         bsPanel.setVisible(false);
                         fsPanel.setVisible(false);
                         
                         ppPaymentPanel.clear();
                         ppPaymentPanel.add(new HTMLPanel(PAYPAL_EXPLANATION));
                         ppPaymentPanel.add(new HTMLPanel(ppCart.toString()));
                         ppPaymentPanel.setVisible(true);
                     }
                 }
             };
 
             // Make the call to the registration service.
             if (entryList.size() > 0 || createMembership) {
                 GWT.log("Sending request to register " + entryList.size() + " classes.", null);
                 regService.register(loginSession, loginSession.getPerson(), entryList, createMembership, callback);
             } else {
                 setMessage("You must select a class before clicking 'Continue'.");
             }
             
         } else {
             GWT.log("Error: Can not register without first signing in.", null);
         }
     }
 
     /**
      * Create the paypal form by iterating across the registration roster entries and
      * membership information and serializing an appropriate HTML form to request payment
      * for the items.
      * @param results the results of the registration step listing registration items
      * @return a StringBuffer containing the HTML form to be displayed
      */
     protected static StringBuffer createPayPalForm(RegistrationResults results, ClassListModel sessionClassList, double discount) {
         ArrayList<RosterEntry> newEntryList = results.getEntriesCreated();
         StringBuffer ppCart = new StringBuffer();
         ppCart.append("<form id=\"wizard\" action=\""+ ClientConstants.getString("CLIENT_PAYPAL_URL") + "\" method=\"post\">");
         ppCart.append("<input type=\"hidden\" name=\"cmd\" value=\"_cart\">");
         ppCart.append("<input type=\"hidden\" name=\"upload\" value=\"1\">");
         ppCart.append("<input type=\"hidden\" name=\"business\" value=\"" + ClientConstants.getString("CLIENT_MERCHANT_ID") + "\">");
         ppCart.append("<input type=\"hidden\" name=\"currency_code\" value=\"USD\">");
         ppCart.append("<input type=\"hidden\" name=\"no_note\" value=\"1\">");
         ppCart.append("<input type=\"hidden\" name=\"no_shipping\" value=\"1\">");
         ppCart.append("<input type=\"hidden\" name=\"cpp_header_image\" value=\""+ ClientConstants.getString("CLIENT_PAYPAL_HEADER_IMAGE") + "\">");
         ppCart.append("<input type=\"hidden\" name=\"item_name\" value=\""+ "Registration Invoice" + "\">");
         ppCart.append("<input type=\"hidden\" name=\"invoice\" value=\""+ results.getPaymentId() + "\">");
 
         int i = 0;
         for (RosterEntry newEntry : newEntryList) {
             i++;
             ppCart.append("<input type=\"hidden\" name=\"item_name_" + i + "\" value=\"" + sessionClassList.getSkatingClass(newEntry.getClassid()).formatClassLabel() + "\">");
             ppCart.append("<input type=\"hidden\" name=\"item_number_" + i + "\" value=\""+ newEntry.getRosterid() +"\">");
             ppCart.append("<input type=\"hidden\" name=\"amount_" + i + "\" value=\"" + newEntry.getPayment_amount() + "\">");
         }
         
         // Handle membership payment by creating form items as needed
         if (results.isMembershipCreated()) {
             double dues = AppConstants.MEMBERSHIP_PRICE;
             i++;
             String season = SessionSkatingClass.calculateSeason();
             ppCart.append("<input type=\"hidden\" name=\"item_name_" + i + "\" value=\"Membership dues for " + season + " season\">");
             ppCart.append("<input type=\"hidden\" name=\"item_number_" + i + "\" value=\"" + results.getMembershipId() +"\">");
             ppCart.append("<input type=\"hidden\" name=\"amount_" + i + "\" value=\"" + AppConstants.MEMBERSHIP_PRICE + "\">");
         }
         ppCart.append("<input type=\"hidden\" name=\"discount_amount_cart\" value=\"" + discount + "\">");
         
         ppCart.append("<input type=\"hidden\" name=\"return\" value=\"" + ClientConstants.getString("CLIENT_PAYPAL_RETURN_URL") + "\">");
         ppCart.append("<input type=\"hidden\" name=\"cancel_return\" value=\"" + ClientConstants.getString("CLIENT_PAYPAL_CANCEL_URL") + "\">");
         ppCart.append("<input type=\"submit\" name=\"Pay Now\" value=\"Complete Payment Now\">");
 
         //ppCart.append("<input type=\"image\" src=\"https://www.sandbox.paypal.com/en_US/i/btn/btn_paynow_LG.gif\" border=\"0\" name=\"submit\" alt=\"PayPal - The safer, easier way to pay online!\">");
         //ppCart.append("<img alt=\"\" border=\"0\" src=\"https://www.sandbox.paypal.com/en_US/i/scr/pixel.gif\" width=\"1\" height=\"1\">");
         ppCart.append("</form>");
         return ppCart;
     }
     
     /**
      * Listen for change events when the radio buttons on the registration form
      * are selected and deselected.
      */
     @Override
     public void onValueChange(ValueChangeEvent<Boolean> event) {
         Widget sender = (Widget) event.getSource();
 
         if (sender == bsRadio) {
             GWT.log("bsRadio clicked", null);
             recalculateAndDisplayBasicSkillsTotal();
             bsPanel.setVisible(true);
             fsPanel.setVisible(false);
         } else if (sender == fsRadio) {
             GWT.log("fsRadio clicked", null);
             recalculateAndDisplayFSTotals();
             bsPanel.setVisible(false);
             fsPanel.setVisible(true);
         } else if (sender == memberCheckbox) {
             GWT.log("memberCheckbox clicked", null);
             double dues = 0;
             if (memberCheckbox.getValue() == true &! loginSession.getPerson().isMember()) {
                 dues = AppConstants.MEMBERSHIP_PRICE;
                 totalFSCost += AppConstants.MEMBERSHIP_PRICE;
             } else {
                 dues = 0;
                 totalFSCost -= AppConstants.MEMBERSHIP_PRICE;
             }
             String duesString = numfmt.format(dues);
             memberDues.setText(duesString);
             recalculateAndDisplayFSTotals();        
         } else if (sender instanceof FSClassCheckBox) {
             FSClassCheckBox sendercb = (FSClassCheckBox)sender;
             long classId = new Long(sendercb.getName()).longValue();
             double classCost = sessionClassList.getSkatingClass(classId).getCost();
 
             GWT.log( "Checked class: " + sendercb.getName(), null);
             if (sendercb.getValue() == true) {
                 GWT.log( "Class cost is: " + classCost, null);
                 totalFSCost += classCost;
                 fsClassesToRegister.add(sendercb.getName());
                 sendercb.setClassPrice(classCost);
             } else {
                 GWT.log( "Class cost is: " + classCost, null);
                 totalFSCost -= classCost;
                 fsClassesToRegister.remove(sendercb.getName());
                 sendercb.setClassPrice(0);
             }
             recalculateAndDisplayFSTotals();
         }
     }
 
     @Override
     public void onChange(ChangeEvent event) {
         Widget sender = (Widget) event.getSource();
         recalculateAndDisplayBasicSkillsTotal();
     }
 
     /**
      * Recalculate the total amount of the registration charges, and update the
      * screen to reflect the totals.
      */
     private void recalculateAndDisplayFSTotals() {
         boolean isMember = loginSession.getPerson().isMember() || memberCheckbox.getValue();
         double discount = calculateFSDiscount(fsClassesToRegister.size(), isMember);
         fsDiscountLabel.setText(numfmt.format(discount));
         total = totalFSCost - discount;
         totalCostLabel.setText(numfmt.format(total));
     }
     
     /**
      * Calculate the registration discount based on the number of classes
      * for which the user registers and whether or not they are a member of
      * the club.  Hard coding this algorithm is painful, but there are so many
      * possible variants it seems that some aspect will be hardcoded.
      * @param numclasses the number of classes for which they are registering
      * @param isMember boolean set to true if they are a club member
      * @return the amount of the discount
      */
     protected static double calculateFSDiscount(int numclasses, boolean isMember) {
         double multiclassDiscount = 0;        
         // TODO: determine how to externalize this algorithm for discounting
         if (numclasses == 3) {
             multiclassDiscount = 10;
         } else if (numclasses == 4) {
             multiclassDiscount = 25;
         } else if (numclasses >= 5) {
             multiclassDiscount = 50;
         }
         
         // If the person is already a member, or if they have checked the 
         // membership box, then include the membership discount
         double membershipDiscount = 0;
         if (isMember) {
             membershipDiscount = numclasses*AppConstants.MEMBERSHIP_DISCOUNT;
         }
         
         return multiclassDiscount + membershipDiscount;
     }
     
     /**
      * Calculate the registration discount based on comparing the current date
      * to the start date of the session.  If the current date is before the session
      * start date by at least the number of grace days, then the basic skills discount applies.
      * @param discountDate the date on which discounted registration expires
      * @return the amount of the discount
      */
     protected static double calculateBSDiscount(Date discountDate) {
         double multiclassDiscount = 0;        
         // Calculate the basic skills discount
         double bsDiscount = 0;
         if (discountDate != null) {
             discountDate.setHours(23);
             discountDate.setMinutes(59);
             discountDate.setSeconds(59);
            Date now = Calendar.getInstance().getTime();

             if (now.before(discountDate)) {
                 bsDiscount = AppConstants.BS_DISCOUNT;
             }
         }
         
         return bsDiscount;            
     }
     
     /**
      * An extension of CheckBox that is used to display a figure skating class
      * checkbox on the registration panel.  Instances of FSClassCheckBox keep
      * track of the current price of the class to be displayed, and contain a
      * label that can be used to display the current price.  Whenever the price
      * of a class is updated, the label is also updated.
      * @author Matt Jones
      *
      */
     private class FSClassCheckBox extends CheckBox {
         private Label classPriceLabel;
         private double classPrice;
         private NumberFormat numfmt;
 
         /**
          * Construct the checkbox, initializing the internal label and price.
          */
         public FSClassCheckBox() {
             super();
             numfmt = NumberFormat.getFormat("$#,##0.00");
             classPriceLabel = new Label();
             setClassPrice(0);
         }
         
         /**
          * @return the classPrice
          */
         @SuppressWarnings("unused")
         public double getClassPrice() {
             return classPrice;
         }
 
         /**
          * @param classPrice the classPrice to set
          */
         public void setClassPrice(double classPrice) {
             this.classPrice = classPrice;
             
             this.classPriceLabel.setText(numfmt.format(classPrice));
         }
 
         /**
          * @return the classPriceLabel
          */
         public Label getClassPriceLabel() {
             return classPriceLabel;
         }
     }
 }
