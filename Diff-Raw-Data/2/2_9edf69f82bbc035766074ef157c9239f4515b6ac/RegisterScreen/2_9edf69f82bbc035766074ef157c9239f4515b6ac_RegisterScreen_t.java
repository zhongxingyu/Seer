 package org.jsc.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.TreeMap;
 
 import com.google.gwt.core.client.GWT;
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
 public class RegisterScreen extends BaseScreen implements ValueChangeHandler {
 
     private static final String MERCHANT_ID = "339U3JVK2X4E6"; // sandbox testing merchantid
 //    private static final String MERCHANT_ID = "4STDKBE3NBV64"; // real JSC account merchantid
     
     private static final String PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr";
 //  private static final String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr";
 
     private static final String PAYPAL_HEADER_IMAGE = "http://juneauskatingclub.org/sites/all/themes/jsc/images/salamander1/jsc-header-bkg-paypal.png";
     private static final String PAYPAL_RETURN_URL = "http://reg.juneauskatingclub.org";
     private static final String PAYPAL_CANCEL_URL = "http://reg.juneauskatingclub.org";
     private static final String PAYMENT_DATA_ID = "U7fGZVYSiD6KerUAg_PhVMlmWIkK1MM2WazdncZQz_v4Dx4HIpre8iyz92e";
     private static final int EARLY_PRICE_GRACE_DAYS = 2;
     private static final double MILLISECS_PER_DAY = 24*60*60*1000;
     private static final double EARLY_PRICE = 70.00;
     private static final double STANDARD_PRICE = 80.00;
     private static final double FS_PRICE = 80.00;
     private static final double MEMBERSHIP_PRICE = 60.00;
     private static final double MEMBERSHIP_DISCOUNT = 5.00;
 
     private static final String DISCOUNT_EXPLANATION = "<p class=\"jsc-text\">Because it helps with planning our class sizes, <b>we offer a discount for those who register early</b> (more than " + EARLY_PRICE_GRACE_DAYS + " days before the session starts).</p>";
     private static final String PRICE_EXPLANATION = "<div id=\"explainstep\"><p class=\"jsc-text\">After you choose a class, you will be prompted to make payment through PayPal.</p>" + DISCOUNT_EXPLANATION + "</div>";
     private static final String PAYPAL_EXPLANATION = "<div id=\"explainstep\"><p class=\"jsc-text\">You must make your payment using PayPal by clicking on the button below.  <b>Your registration is <em>not complete</em></b> until after you have completed payment.</p><p class=\"jsc-text\">When you click \"Pay Now\" below, you will be taken to the PayPal site to make payment.  PayPal will allow you to pay by credit card or using your bank account, among other options.  Once the payment has been made, you will be returned to this site and your registration will be complete.</p></div>";
     private static final String BS_EXPLANATION = "Basic Skills is our Learn to Skate program. These group lessons are based on the United States Figure Skating's (USFSA) Basic Skills Program. This is a nationwide, skills-based, graduated series of instruction for youth and adult skaters. This program is designed to teach all skaters the fundamentals of skating.";
     private static final String FS_EXPLANATION = "Figure Skating is a one- to five-day-a-week program for skaters that have completed all of the Basic Skills Levels (8) or Adult Levels (4). Students work individually with their coach to develop their skills. Please only sign up for Figure Skating Classes if you have graduated from the Basic Skills program.";
 
     private static final String STEP_1 = "Step 1: Choose a class";
     private static final String STEP_2 = "Step 2: Process payment";
     
     private ClassListModel sessionClassList;
     // sessionClassLabels has the same data as sessionClassList but is keyed on 
     // the classid for ease of lookup of the label associated with a class
     private TreeMap<String, String> sessionClassLabels;
     
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
     private Label discountLabel;
     private NumberFormat numfmt;
     private Label bsTotalLabel;
     private Label bsDiscountLabel;
     
     /**
      * Construct the Registration view and controller used to display a form for
      * registering for skating classes.
      * @param loginSession the authenticated login session for submissions to the remote service
      * @param sessionClassList the model of skating classes
      */
     public RegisterScreen(LoginSession loginSession, HandlerManager eventBus, ClassListModel sessionClassList) {
         super(loginSession, eventBus);
         this.sessionClassList = sessionClassList;
         sessionClassLabels = new TreeMap<String, String>();
         numfmt = NumberFormat.getFormat("$#,##0.00");
         fsClassesToRegister = new HashSet<String>();
         totalFSCost = 0;
         layoutScreen();
         this.setContentPanel(screen);
         regService = GWT.create(SkaterRegistrationService.class);
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
         fsRadio = new RadioButton("BSorFSGroup", "Figure Skating Classes");
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
         bsClassChoicePanel.add(new HTMLPanel(PRICE_EXPLANATION));
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
         discountLabel = new Label();
         discountLabel.setText(numfmt.format(zero));
         figureSkatingGrid.setWidget(newRow, 4, new Label("Discount"));
         figureSkatingGrid.setWidget(newRow, 5, discountLabel);
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
         sessionClassLabels = new TreeMap<String, String>();
         
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
                 GWT.log("SessionClass: " + (new Long(curClass.getClassId()).toString()) + " " + curClass.getClassType(), null);
                 String classLabel = curClass.formatClassLabel();
                 sessionClassLabels.put(new Long(curClass.getClassId()).toString(), classLabel);    
 
                 // If it starts with "FS " it is a figure skating class
                 if (curClass.getClassType().startsWith("FS ")) {
                     FSClassCheckBox checkbox = new FSClassCheckBox();
                     checkbox.setValue(false, false);
                     checkbox.setName(Long.toString(curClass.getClassId()));
                     checkbox.addValueChangeHandler(this);
                     addToFSGrid(classLabel, checkbox);
                     
                 // Otherwise it is a Basic Skills class
                 } else {
                     classField.addItem(classLabel, new Long(curClass.getClassId()).toString());
                 }
             }
         }
         
         // Update the membership checkbox status based on the Person logged in
         if (loginSession.getPerson().isMember()) {
             memberCheckboxLabel.setText("Membership dues already paid. Discount applies.");
             memberCheckbox.setEnabled(false);
             double dues = 0;
             String duesString = numfmt.format(dues);
             memberDues.setText(duesString);
         } else {
             memberCheckboxLabel.setText("Pay membership dues");
             memberCheckbox.setEnabled(true);
             //String duesString = numfmt.format(MEMBERSHIP_PRICE);
             //memberDues.setText(duesString);
         }
         
         recalculateAndDisplayBasicSkillsTotal();
         recalculateAndDisplayTotals();
     }
 
     /**
      * Set the price and discount for the Basic Skills form by recalculating the
      * discount.
      */
     private void recalculateAndDisplayBasicSkillsTotal() {
         // Update the cost discount, and total fields on the BS Form
         feeLabel.setText(numfmt.format(STANDARD_PRICE));
         double bsDiscount = calculateDiscount();
         bsDiscountLabel.setText(numfmt.format(bsDiscount));
         double bsTotal = STANDARD_PRICE - bsDiscount;
         bsTotalLabel.setText(numfmt.format(bsTotal));
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
                 Person registrant = loginSession.getPerson();
                 RosterEntry entry = new RosterEntry();
                 entry.setClassid(new Long(selectedClassId).longValue());
                 entry.setPid(registrant.getPid());
                 entry.setPayment_amount(STANDARD_PRICE);
                 entryList.add(entry);
                 
             // Otherwise gather information from the Figure Skating form if it is selected
             } else if (fsRadio.getValue()) {
                 if (memberCheckbox.getValue() == true &! loginSession.getPerson().isMember()) {
                     createMembership = true;
                 }
                 // Loop through the checked classes, creating a RosterEntry for each
                 for (String selectedClassId : fsClassesToRegister) {
                     GWT.log("Need to register class: " + selectedClassId, null);
                     Person registrant = loginSession.getPerson();
                     RosterEntry entry = new RosterEntry();
                     entry.setClassid(new Long(selectedClassId).longValue());
                     entry.setPid(registrant.getPid());
                     entry.setPayment_amount(FS_PRICE);
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
                         // Could simply be due to duplication errors
                         // TODO: Handle case where all duplication errors occur
                         // Could still have membership created in this case
                        setMessage("Error registering... Have you are already registered for these classes? Check 'My Classes'.");
                         return;
                     } else {
                         // TODO: Handle case where some duplication errors occur on insert but not all
                         // TODO: Post error messages about entries that were not created
                         StringBuffer ppCart = new StringBuffer();
                         ppCart.append("<form id=\"wizard\" action=\""+ PAYPAL_URL + "\" method=\"post\">");
                         ppCart.append("<input type=\"hidden\" name=\"cmd\" value=\"_cart\">");
                         ppCart.append("<input type=\"hidden\" name=\"upload\" value=\"1\">");
                         ppCart.append("<input type=\"hidden\" name=\"business\" value=\"" + MERCHANT_ID + "\">");
                         ppCart.append("<input type=\"hidden\" name=\"currency_code\" value=\"USD\">");
                         ppCart.append("<input type=\"hidden\" name=\"no_note\" value=\"1\">");
                         ppCart.append("<input type=\"hidden\" name=\"no_shipping\" value=\"1\">");
                         ppCart.append("<input type=\"hidden\" name=\"cpp_header_image\" value=\""+ PAYPAL_HEADER_IMAGE + "\">");
                         ppCart.append("<input type=\"hidden\" name=\"item_name\" value=\""+ "Registration Invoice" + "\">");
                         ppCart.append("<input type=\"hidden\" name=\"invoice\" value=\""+ results.getPaymentId() + "\">");
 
                         int i = 0;
                         for (RosterEntry newEntry : newEntryList) {
                             i++;
                             ppCart.append("<input type=\"hidden\" name=\"item_name_" + i + "\" value=\"" + sessionClassLabels.get(new Long(newEntry.getClassid()).toString()) + "\">");
                             ppCart.append("<input type=\"hidden\" name=\"item_number_" + i + "\" value=\""+ newEntry.getRosterid() +"\">");
                             ppCart.append("<input type=\"hidden\" name=\"amount_" + i + "\" value=\"" + STANDARD_PRICE + "\">");
                         }
                         
                         // Handle membership payment by creating form items as needed
                         if (results.isMembershipCreated()) {
                             loginSession.getPerson().setMember(true);
                             loginSession.getPerson().setMembershipId(results.getMembershipId());
                             double dues = MEMBERSHIP_PRICE;
                             i++;
                             String season = SessionSkatingClass.calculateSeason();
                             ppCart.append("<input type=\"hidden\" name=\"item_name_" + i + "\" value=\"Membership dues for " + season + " season\">");
                             ppCart.append("<input type=\"hidden\" name=\"item_number_" + i + "\" value=\"" + results.getMembershipId() +"\">");
                             ppCart.append("<input type=\"hidden\" name=\"amount_" + i + "\" value=\"" + MEMBERSHIP_PRICE + "\">");
                         }
                         
                         ppCart.append("<input type=\"hidden\" name=\"discount_amount_cart\" value=\"" + calculateDiscount() + "\">");
                         
                         ppCart.append("<input type=\"hidden\" name=\"return\" value=\"" + PAYPAL_RETURN_URL + "\">");
                         ppCart.append("<input type=\"hidden\" name=\"cancel_return\" value=\"" + PAYPAL_CANCEL_URL + "\">");
                         ppCart.append("<input type=\"submit\" name=\"Pay Now\" value=\"Complete Payment Now\">");
 
                         //ppCart.append("<input type=\"image\" src=\"https://www.sandbox.paypal.com/en_US/i/btn/btn_paynow_LG.gif\" border=\"0\" name=\"submit\" alt=\"PayPal - The safer, easier way to pay online!\">");
                         //ppCart.append("<img alt=\"\" border=\"0\" src=\"https://www.sandbox.paypal.com/en_US/i/scr/pixel.gif\" width=\"1\" height=\"1\">");
                         ppCart.append("</form>");
                         
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
             regService.register(loginSession, loginSession.getPerson(), entryList, createMembership, callback);
             
         } else {
             GWT.log("Error: Can not register without first signing in.", null);
         }
     }
 
     /**
      * Listen for change events when the radio buttons on the registration form
      * are selected and deselected.
      */
     public void onValueChange(ValueChangeEvent event) {
         Widget sender = (Widget) event.getSource();
 
         if (sender == bsRadio) {
             GWT.log("bsRadio clicked", null);
             recalculateAndDisplayBasicSkillsTotal();
             bsPanel.setVisible(true);
             fsPanel.setVisible(false);
         } else if (sender == fsRadio) {
             GWT.log("fsRadio clicked", null);
             recalculateAndDisplayTotals();
             bsPanel.setVisible(false);
             fsPanel.setVisible(true);
         } else if (sender == memberCheckbox) {
             GWT.log("memberCheckbox clicked", null);
             double dues = 0;
             if (memberCheckbox.getValue() == true &! loginSession.getPerson().isMember()) {
                 dues = MEMBERSHIP_PRICE;
                 totalFSCost += MEMBERSHIP_PRICE;
             } else {
                 dues = 0;
                 totalFSCost -= MEMBERSHIP_PRICE;
             }
             String duesString = numfmt.format(dues);
             memberDues.setText(duesString);
             recalculateAndDisplayTotals();        
         } else if (sender instanceof FSClassCheckBox) {
             FSClassCheckBox sendercb = (FSClassCheckBox)sender;
             GWT.log( "Checked class: " + sendercb.getName(), null);
             if (sendercb.getValue() == true) {
                 totalFSCost += FS_PRICE;
                 fsClassesToRegister.add(sendercb.getName());
                 sendercb.setClassPrice(FS_PRICE);
             } else {
                 totalFSCost -= FS_PRICE;
                 fsClassesToRegister.remove(sendercb.getName());
                 sendercb.setClassPrice(0);
             }
             recalculateAndDisplayTotals();
         }
     }
 
     /**
      * Recalculate the total amount of the registration charges, and update the
      * screen to reflect the totals.
      */
     private void recalculateAndDisplayTotals() {
         double discount = calculateDiscount();
         discountLabel.setText(numfmt.format(discount));
         total = totalFSCost - discount;
         totalCostLabel.setText(numfmt.format(total));
     }
     
     /**
      * Calculate the registration discount based on the number of classes
      * for which the user registers and whether or not they are a member of
      * the club.  Hard coding this algorithm is painful, but there are so many
      * possible variants it seems that some aspect will be hardcoded.
      * @return the amount of the discount
      */
     private double calculateDiscount() {
         double multiclassDiscount = 0;        
         // TODO: determine how to externalize this algorithm for discounting
         if (fsClassesToRegister.size() == 3) {
             multiclassDiscount = 10;
         } else if (fsClassesToRegister.size() == 4) {
             multiclassDiscount = 25;
         } else if (fsClassesToRegister.size() >= 5) {
             multiclassDiscount = 50;
         }
         
         // If the person is already a member, or if they have checked the 
         // membership box, then include the membership discount
         double membershipDiscount = 0;
         if (loginSession.getPerson().isMember() || memberCheckbox.getValue()) {
             membershipDiscount = fsClassesToRegister.size()*MEMBERSHIP_DISCOUNT;
         }
         
         // Calculate the basic skills discount
         ArrayList<SessionSkatingClass> list = sessionClassList.getClassList();
         double bsDiscount = 0;
         String sessionStart = list.get(0).getStartDate();
         DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
         if (sessionStart != null) {
             Date startDate = fmt.parse(sessionStart);
             Date today = new Date(System.currentTimeMillis());
             if (today.before(startDate) && 
                (startDate.getTime() - today.getTime() > EARLY_PRICE_GRACE_DAYS*MILLISECS_PER_DAY)) {
                 bsDiscount = STANDARD_PRICE - EARLY_PRICE;
             }
         }
         
         // If the Basic Skills screen is active, return the bsDiscount
         // Otherwise, return the figure skating discount    
         if (bsRadio.getValue()) {
             return bsDiscount;            
         } else {
             return multiclassDiscount + membershipDiscount;
         }
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
