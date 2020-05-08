 package view;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 
 import model.Customer;
 import model.Order;
 
 import controller.ManageOrder;
 import controller.Validate;
 
 /**
 *
 * @author Morten Vaale Noddeland
 */
 
 @SuppressWarnings("serial")
 public class NewOrderForm1 extends javax.swing.JFrame {
 
 private Order order;
     /** Creates new form NewOrderForm */
     public NewOrderForm1() {
         initComponents();
         this.updateLeftPanel(ManageOrder.getRelevantCustomers(""));
     }
     
     /** Creates new form NewOrderForm */
     public NewOrderForm1(Customer customer, Order order) {
         initComponents();
         this.order = order;
         this.updateLeftPanel(ManageOrder.getRelevantCustomers(""));
         this.firstNameField.setText(customer.getForename());
         this.lastNameField.setText(customer.getLastname());
         this.addressField.setText(customer.getAddress());
         this.zipCodeField.setText(customer.getzipCode());
         this.postalAddressField.setText(customer.getPostalAddress());
         this.phoneNumberField.setText(customer.getPhone());
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      */
     private void initComponents() {
 
         topPanel = new javax.swing.JPanel();
         newOrderLabel = new javax.swing.JLabel();
         leftPanel = new javax.swing.JPanel();
         searchField = new javax.swing.JTextField();
         leftScrollPane = new javax.swing.JScrollPane();
         innerLeftPanel = new javax.swing.JPanel();
         rightPanel = new javax.swing.JPanel();
         firstNameLabel = new javax.swing.JLabel();
         firstNameField = new javax.swing.JTextField();
         lastNameLabel = new javax.swing.JLabel();
         lastNameField = new javax.swing.JTextField();
         addressLabel = new javax.swing.JLabel();
         addressField = new javax.swing.JTextField();
         zipCodeLabel = new javax.swing.JLabel();
         zipCodeField = new javax.swing.JTextField();
         postalAddressField = new javax.swing.JTextField();
         postalAddressLabel = new javax.swing.JLabel();
         phoneNumberLabel = new javax.swing.JLabel();
         phoneNumberField = new javax.swing.JTextField();
         bottomPanel = new javax.swing.JPanel();
         backButton = new javax.swing.JLabel();
         nextButton = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Ny ordre - kunder");
 
         topPanel.setBackground(new java.awt.Color(220, 220, 220));
         topPanel.setPreferredSize(new java.awt.Dimension(800, 100));
         topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray));
 
 
         newOrderLabel.setFont(new java.awt.Font("Georgia", 0, 36));
         newOrderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         newOrderLabel.setText("Ny ordre - kunder");
 
         javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
         topPanel.setLayout(topPanelLayout);
         topPanelLayout.setHorizontalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addGap(229, 229, 229)
                 .addComponent(newOrderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                 .addGap(254, 254, 254))
         );
         topPanelLayout.setVerticalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addGap(11, 11, 11)
                 .addComponent(newOrderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         leftPanel.setBackground(new java.awt.Color(253, 253, 253));
         leftPanel.setPreferredSize(new java.awt.Dimension(400, 400));
 
         searchField.setFont(new java.awt.Font("Lucida Grande", 0, 18));
         searchField.setText("Søk");
         searchField.setActionCommand("<Not Set>");
         searchField.setAlignmentX(0.0F);
         searchField.setAlignmentY(0.0F);
         searchField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray), 
         		javax.swing.BorderFactory.createEmptyBorder(10,10,10,10)));
         searchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 searchFieldKeyTyped(evt);
             }
         });
        searchField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchFieldMouseClicked(evt);
             }
         });
 
        innerLeftPanel.setBackground(new java.awt.Color(253, 253, 253));
 
        leftScrollPane.setViewportView(innerLeftPanel);
        leftScrollPane.setBorder(null);
        leftScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(leftScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
        );
 
         rightPanel.setBackground(new java.awt.Color(253, 253, 253));
         rightPanel.setPreferredSize(new java.awt.Dimension(400, 400));
         rightPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.darkGray));
 
 
         firstNameLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         firstNameLabel.setText("Fornavn");
 
         firstNameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), 
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         lastNameLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         lastNameLabel.setText("Etternavn");
 
         lastNameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), 
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         addressLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         addressLabel.setText("Adresse");
 
         addressField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)),
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         zipCodeLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         zipCodeLabel.setText("Postnummer");
 
         zipCodeField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)),
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         postalAddressField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)),
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         postalAddressLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         postalAddressLabel.setText("Poststed");
 
         phoneNumberLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         phoneNumberLabel.setText("Telefon");
 
         phoneNumberField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
         		javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)),
         		javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
         rightPanel.setLayout(rightPanelLayout);
         rightPanelLayout.setHorizontalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(firstNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addComponent(addressLabel, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(phoneNumberField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addComponent(lastNameLabel, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(phoneNumberLabel, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(firstNameLabel, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lastNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addComponent(addressField, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rightPanelLayout.createSequentialGroup()
                         .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(zipCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(zipCodeLabel))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(postalAddressLabel)
                             .addComponent(postalAddressField, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE))))
                 .addContainerGap())
         );
         rightPanelLayout.setVerticalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rightPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(firstNameLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lastNameLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(addressLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(zipCodeLabel)
                     .addComponent(postalAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(zipCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(postalAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(phoneNumberLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(phoneNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(63, Short.MAX_VALUE))
         );
 
         bottomPanel.setBackground(new java.awt.Color(220, 220, 220));
         bottomPanel.setPreferredSize(new java.awt.Dimension(800, 100));
         bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.darkGray));
 
 
         backButton = Button.createButton("Tilbake", "red", "leftred");
         backButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
              backButtonMouseClicked(evt);
             }
         });
 
         nextButton = Button.createButton("Neste", "green", "right");
         nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 nextButtonMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
         bottomPanel.setLayout(bottomPanelLayout);
         bottomPanelLayout.setHorizontalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 500, Short.MAX_VALUE)
                 .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         bottomPanelLayout.setVerticalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 )
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }
     boolean searchFieldChanged;
     private void searchFieldMouseClicked(java.awt.event.MouseEvent evt) {
     	this.searchFieldChanged = true;
     	this.searchField.setText("");
     	this.updateLeftPanel(ManageOrder.getRelevantCustomers(this.searchField.getText()));
     }
     private void searchFieldKeyTyped(java.awt.event.KeyEvent evt) {
    	String letter = this.searchField.getText();
        if ((!this.searchFieldChanged) && (letter.length() == 4)) {
        	this.searchField.setText("" + letter.charAt(3));
         	this.searchFieldChanged = true;
         }
        else {
        	this.searchFieldChanged = true;
        	this.searchField.setText(letter);
        }
         this.updateLeftPanel(ManageOrder.getRelevantCustomers(this.searchField.getText()));
     }
 
     private void backButtonMouseClicked(java.awt.event.MouseEvent evt) {
     	MainMenuForm form = new MainMenuForm();
     	form.setVisible(true);
         this.setVisible(false);
     }
 
     private void nextButtonMouseClicked(java.awt.event.MouseEvent evt) {
     	String firstName = firstNameField.getText();
     	String lastName = lastNameField.getText();
     	String phone = phoneNumberField.getText();
     	String address = addressField.getText();
     	String zipCode = zipCodeField.getText();
     	String postalAddress = postalAddressField.getText();
     	boolean legalCustomer = true;
     	Color color = new Color(235, 210, 210);
     	if (!Validate.stringNonEmpty(firstName) || !Validate.isStringLegal(firstName)){
     		this.firstNameField.setBackground(color);
     		legalCustomer = false;
     	}
     	else {
     		this.firstNameField.setBackground(Color.WHITE);
     	}
     	if (!Validate.stringNonEmpty(lastName) || !Validate.isStringLegal(lastName)){
     		this.lastNameField.setBackground(color);
     		legalCustomer = false;
     	}
     	else {
     		this.lastNameField.setBackground(Color.WHITE);
     	}
     	if (!Validate.stringNonEmpty(phone) || !Validate.isStringOnlyNumbers(phone)){
     		this.phoneNumberField.setBackground(color);
     		legalCustomer = false;
     	}
     	else {
     		this.phoneNumberField.setBackground(Color.WHITE);
     	}
     	if (!Validate.isStringOnlyNumbers(zipCode)){
     		this.zipCodeField.setBackground(color);
     		legalCustomer = false;
     	}
     	if (!Validate.isStringLegal(address)){
     		this.addressField.setBackground(color);
     		legalCustomer = false;
     	}
     	if (!Validate.isStringLegal(postalAddress)){
     		this.postalAddressField.setBackground(color);
     		legalCustomer = false;
     	}
     
     	if (legalCustomer && this.order!=null){
     		NewOrderForm2 form = new NewOrderForm2(ManageOrder.addNewCustomer(firstName,
     				lastName, phone, address, zipCode, postalAddress), order);
     		form.setVisible(true);
     		this.setVisible(false);
     	}
     	else if (legalCustomer){
     		NewOrderForm2 form = new NewOrderForm2(ManageOrder.addNewCustomer(firstName,
     				lastName, phone, address, zipCode, postalAddress));
     		form.setVisible(true);
     		this.setVisible(false);
     	}
     }
     
     public void updateLeftPanel(ArrayList<Customer> customers) {
     	ArrayList<javax.swing.JLabel> customerLabels = createCustomerLabels(customers);
     	
         GroupLayout innerLeftPanelLayout = new javax.swing.GroupLayout(innerLeftPanel);
         innerLeftPanel.removeAll();
         innerLeftPanel.setLayout(innerLeftPanelLayout);
         
         setInnerLeftPanelLayout(innerLeftPanelLayout, customerLabels);
         
         pack();
         searchField.setText(searchField.getText());
     }
 
 	private ArrayList<JLabel> createCustomerLabels(ArrayList<Customer> customers){
     	ArrayList<javax.swing.JLabel> customerList = new ArrayList<javax.swing.JLabel>();
     	
     	for (int i=0; i < customers.size(); i++) {
         	final Customer customer = customers.get(i);
         	String text = customer.getForename() + " " + customer.getLastname();
         	JLabel label = Labels.createBorderlessLabel(text, i);
         	label.addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     customerLabelMouseClicked(evt, customer);
                 }
             });
             
             customerList.add(label);
         }
     	return customerList;
     }
 	
     private void setInnerLeftPanelLayout(GroupLayout innerLeftPanelLayout, ArrayList<JLabel> customerLabels) {
     	// Set the horizontal axis
     	javax.swing.GroupLayout.ParallelGroup tempGroup = innerLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
         for(int i = 0; i < customerLabels.size(); i++) {
         	tempGroup.addComponent(customerLabels.get(i), org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE);
         }
         
         innerLeftPanelLayout.setHorizontalGroup(
         	innerLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         	.addGroup(innerLeftPanelLayout.createSequentialGroup()
         		.addGroup(tempGroup)
         	)
         );
      
         // Set the vertical axis
         javax.swing.GroupLayout.SequentialGroup verticalTempGroup = innerLeftPanelLayout.createSequentialGroup();
         for(int i = 0; i < customerLabels.size(); i++) {
         	verticalTempGroup.addComponent(customerLabels.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE);
         }
         innerLeftPanelLayout.setVerticalGroup(
         	innerLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         	.addGroup(verticalTempGroup)
         );		
 	}
     
     private void customerLabelMouseClicked(java.awt.event.MouseEvent evt, Customer c) {
     	this.updateRightPanel(c);
     }
     
     public void updateRightPanel(Customer c) {
     	firstNameField.setText(c.getForename());
     	lastNameField.setText(c.getLastname());
     	phoneNumberField.setText(c.getPhone());
     	addressField.setText(c.getAddress());
     	zipCodeField.setText(c.getzipCode());
     	postalAddressField.setText(c.getPostalAddress());
     }
     
     // Variables declaration
     private javax.swing.JTextField addressField;
     private javax.swing.JLabel addressLabel;
     private javax.swing.JLabel backButton;
     private javax.swing.JPanel bottomPanel;
     private javax.swing.JTextField firstNameField;
     private javax.swing.JLabel firstNameLabel;
     private javax.swing.JTextField lastNameField;
     private javax.swing.JLabel lastNameLabel;
     private javax.swing.JPanel leftPanel;
     private javax.swing.JScrollPane leftScrollPane;
     private javax.swing.JPanel innerLeftPanel;
     private javax.swing.JLabel newOrderLabel;
     private javax.swing.JLabel nextButton;
     private javax.swing.JTextField phoneNumberField;
     private javax.swing.JLabel phoneNumberLabel;
     private javax.swing.JTextField postalAddressField;
     private javax.swing.JLabel postalAddressLabel;
     private javax.swing.JPanel rightPanel;
     private javax.swing.JTextField searchField;
     private javax.swing.JPanel topPanel;
     private javax.swing.JTextField zipCodeField;
     private javax.swing.JLabel zipCodeLabel;
     // End of variables declaration
 }
