 package view;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import controller.AdminController;
 import controller.Validate;
 
 import model.Product;
 import model.Properties;
 
 
 /**
  *
  * @author Morten Vaale Noddeland
  */
 @SuppressWarnings("serial")
 public class AdminForm extends JFrame {
 
 	private Product currentProduct;
 	
     /** Creates new form adminForm */
     public AdminForm() {
         initComponents();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      */
     private void initComponents() {
 
         topPanel = new javax.swing.JPanel();
         newOrderLabel = new javax.swing.JLabel();
         middlePanel = new javax.swing.JPanel();
         tabPane = new javax.swing.JTabbedPane();
         productPanel = new javax.swing.JPanel();
         rightProductPanel = new javax.swing.JPanel();
         addEditButton = new javax.swing.JLabel(); 
         deleteButton = new javax.swing.JLabel(); 
         productNameLabel = new javax.swing.JLabel();
         productNameField = new javax.swing.JTextField();
         priceLabel = new javax.swing.JLabel();
         priceField = new javax.swing.JTextField();
         commentLabel = new javax.swing.JLabel();
         commentField = new javax.swing.JTextField();
         leftProductPanel = new javax.swing.JPanel();
         productScrollPane = new javax.swing.JScrollPane();
         innerLeftProductPanel = new javax.swing.JPanel();
         searchField = new javax.swing.JTextField();
         restaurantPanel = new javax.swing.JPanel();
         leftRestaurantPanel = new javax.swing.JPanel();
         restaurantNameLabel = new javax.swing.JLabel();
         addressLabel = new javax.swing.JLabel();
         zipCodeLabel = new javax.swing.JLabel();
         postalAddressLabel = new javax.swing.JLabel();
         telephoneLabel = new javax.swing.JLabel();
         zipCodeField = new javax.swing.JTextField();
         telephoneField = new javax.swing.JTextField();
         postalAddressField = new javax.swing.JTextField();
         addressField = new javax.swing.JTextField();
         restaurantNameField = new javax.swing.JTextField();
         nameAddressHeaderLabel = new javax.swing.JLabel();
         rightRestaurantPanel = new javax.swing.JPanel();
         editButton = new javax.swing.JLabel(); 
         freeDeliveryLimitLabel = new javax.swing.JLabel();
         freeDeliveryLimitField = new javax.swing.JTextField();
         otherInfoHeaderLabel = new javax.swing.JLabel();
         bottomPanel = new javax.swing.JPanel();
         backButton = new javax.swing.JLabel(); 
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Administrer");
 
         topPanel.setBackground(new java.awt.Color(220, 220, 220));
         topPanel.setPreferredSize(new java.awt.Dimension(800, 100));
         topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray));
 
         newOrderLabel.setFont(new java.awt.Font("Georgia", 0, 36));
         newOrderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         newOrderLabel.setText("Administrer");
 
         javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
         topPanel.setLayout(topPanelLayout);
         topPanelLayout.setHorizontalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(newOrderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                 .addContainerGap())
         );
         topPanelLayout.setVerticalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addGap(11, 11, 11)
                 .addComponent(newOrderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         middlePanel.setBackground(new java.awt.Color(253, 253, 253));
         middlePanel.setPreferredSize(new java.awt.Dimension(400, 400));
 
         rightProductPanel.setBackground(new java.awt.Color(253, 253, 253));
         
         addEditButton = Button.createButton("Legg til", "green", "tick");
         addEditButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 addEditButtonMouseClicked(evt);
             }
         });
 
         deleteButton = Button.createButton("Slett", "red", "delete");
         deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 deleteButtonMouseClicked(evt);
             }
         });
 
         productNameLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         productNameLabel.setText("Produktnavn");
 
         productNameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         priceLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         priceLabel.setText("Pris");
 
         priceField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         commentLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         commentLabel.setText("Kommentar");
 
         commentField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         javax.swing.GroupLayout rightProductPanelLayout = new javax.swing.GroupLayout(rightProductPanel);
         rightProductPanel.setLayout(rightProductPanelLayout);
         rightProductPanelLayout.setHorizontalGroup(
             rightProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rightProductPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(rightProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(productNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightProductPanelLayout.createSequentialGroup()
                         .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                         .addComponent(addEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(productNameLabel)
                     .addComponent(priceLabel)
                     .addComponent(priceField, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                     .addComponent(commentLabel)
                     .addComponent(commentField, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE))
                 .addContainerGap())
         );
         rightProductPanelLayout.setVerticalGroup(
             rightProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightProductPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(productNameLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(productNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(priceLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(priceField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(commentLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(commentField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                 .addGroup(rightProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(addEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         leftProductPanel.setBackground(new java.awt.Color(253, 253, 253));
         leftProductPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.darkGray));
 
         innerLeftProductPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         productScrollPane.setBorder(null);
         productScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         productScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         productScrollPane.setViewportView(innerLeftProductPanel);
         
         searchField.setFont(new java.awt.Font("Lucida Grande", 0, 18));
         searchField.setText("Søk");
         searchField.setActionCommand("<Not Set>");
         searchField.setAlignmentX(0.0F);
         searchField.setAlignmentY(0.0F);
         searchField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(10,10,10,10)));
         searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                 searchFieldKeyTyped(evt);
             }
         });
        searchField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchFieldMouseClicked(evt);
             }
         });
        javax.swing.GroupLayout leftProductPanelLayout = new javax.swing.GroupLayout(leftProductPanel);
        leftProductPanel.setLayout(leftProductPanelLayout);
        leftProductPanelLayout.setHorizontalGroup(
            leftProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftProductPanelLayout.createSequentialGroup()
                .addGroup(leftProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(productScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
        ));
        leftProductPanelLayout.setVerticalGroup(
            leftProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftProductPanelLayout.createSequentialGroup()
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(productScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
        );
         
         displayAdminPanel("");
 
         javax.swing.GroupLayout productPanelLayout = new javax.swing.GroupLayout(productPanel);
         productPanel.setLayout(productPanelLayout);
         productPanelLayout.setHorizontalGroup(
             productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(productPanelLayout.createSequentialGroup()
                 .addComponent(leftProductPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(rightProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         productPanelLayout.setVerticalGroup(
             productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(leftProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(rightProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         tabPane.addTab("Produkter", productPanel);
 
         restaurantPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         leftRestaurantPanel.setBackground(new java.awt.Color(253, 253, 253));
         leftRestaurantPanel.setPreferredSize(new java.awt.Dimension(375, 354));
 
         restaurantNameLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         restaurantNameLabel.setText("Navn");
         
         restaurantNameField.setText(Properties.getName());
 
         addressLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         addressLabel.setText("Adresse");
 
         addressField.setText(Properties.getAddress());
         
         zipCodeLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         zipCodeLabel.setText("Postnummer");
         
         zipCodeField.setText(Properties.getZipCode());
 
         postalAddressLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         postalAddressLabel.setText("Poststed");
         
         postalAddressField.setText(Properties.getPostalAddress());
 
         telephoneLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         telephoneLabel.setText("Telefonnummer");
         
         telephoneField.setText(Properties.getPhone());
 
         zipCodeField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         telephoneField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         postalAddressField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         addressField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         restaurantNameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         nameAddressHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         nameAddressHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         nameAddressHeaderLabel.setText("Restaurantopplysninger");
         nameAddressHeaderLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5));
         nameAddressHeaderLabel.setOpaque(true);
         nameAddressHeaderLabel.setPreferredSize(new java.awt.Dimension(375, 40));
 
         javax.swing.GroupLayout leftRestaurantPanelLayout = new javax.swing.GroupLayout(leftRestaurantPanel);
         leftRestaurantPanel.setLayout(leftRestaurantPanelLayout);
         leftRestaurantPanelLayout.setHorizontalGroup(
             leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                         .addComponent(addressField, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                         .addComponent(telephoneField, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                         .addComponent(telephoneLabel)
                         .addContainerGap(254, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftRestaurantPanelLayout.createSequentialGroup()
                         .addGroup(leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(zipCodeField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                             .addComponent(zipCodeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                                 .addComponent(postalAddressLabel)
                                 .addGap(210, 210, 210))
                             .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                                 .addComponent(postalAddressField, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                                 .addContainerGap())))
                     .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                         .addComponent(addressLabel)
                         .addContainerGap(306, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftRestaurantPanelLayout.createSequentialGroup()
                         .addComponent(restaurantNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                         .addContainerGap())))
             .addComponent(nameAddressHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
             .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(restaurantNameLabel)
                 .addContainerGap(322, Short.MAX_VALUE))
         );
         leftRestaurantPanelLayout.setVerticalGroup(
             leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(leftRestaurantPanelLayout.createSequentialGroup()
                 .addComponent(nameAddressHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(restaurantNameLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(restaurantNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(addressLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(zipCodeLabel)
                     .addComponent(postalAddressLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(leftRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(zipCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(postalAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(telephoneLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(telephoneField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         rightRestaurantPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         editButton = Button.createButton("Endre", "yellow", "warning");
         editButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 editButtonMouseClicked(evt);
             }
         });
 
         freeDeliveryLimitLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         freeDeliveryLimitLabel.setText("Grense for gratis levering");
 
         freeDeliveryLimitField.setText(""+Properties.getLimitFreeDelivery());
         freeDeliveryLimitField.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
 
         otherInfoHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         otherInfoHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         otherInfoHeaderLabel.setText("Andre innstillinger");
         otherInfoHeaderLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5));
         otherInfoHeaderLabel.setOpaque(true);
         otherInfoHeaderLabel.setPreferredSize(new java.awt.Dimension(378, 40));
 
         javax.swing.GroupLayout rightRestaurantPanelLayout = new javax.swing.GroupLayout(rightRestaurantPanel);
         rightRestaurantPanel.setLayout(rightRestaurantPanelLayout);
         rightRestaurantPanelLayout.setHorizontalGroup(
             rightRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(otherInfoHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
             .addGroup(rightRestaurantPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(freeDeliveryLimitLabel)
                 .addContainerGap(201, Short.MAX_VALUE))
             .addGroup(rightRestaurantPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(freeDeliveryLimitField, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(rightRestaurantPanelLayout.createSequentialGroup()
             	.addContainerGap(228, Short.MAX_VALUE)
                 .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         rightRestaurantPanelLayout.setVerticalGroup(
             rightRestaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rightRestaurantPanelLayout.createSequentialGroup()
                 .addComponent(otherInfoHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(freeDeliveryLimitLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(freeDeliveryLimitField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 199, Short.MAX_VALUE)
                 .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout restaurantPanelLayout = new javax.swing.GroupLayout(restaurantPanel);
         restaurantPanel.setLayout(restaurantPanelLayout);
         restaurantPanelLayout.setHorizontalGroup(
             restaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(restaurantPanelLayout.createSequentialGroup()
                 .addComponent(leftRestaurantPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(rightRestaurantPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         restaurantPanelLayout.setVerticalGroup(
             restaurantPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(leftRestaurantPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
             .addComponent(rightRestaurantPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         tabPane.addTab("Restaurant", restaurantPanel);
 
         javax.swing.GroupLayout middlePanelLayout = new javax.swing.GroupLayout(middlePanel);
         middlePanel.setLayout(middlePanelLayout);
         middlePanelLayout.setHorizontalGroup(
             middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(middlePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
                 .addContainerGap())
         );
         middlePanelLayout.setVerticalGroup(
             middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(middlePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                 .addContainerGap())
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
 
         javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
         bottomPanel.setLayout(bottomPanelLayout);
         bottomPanelLayout.setHorizontalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(650, Short.MAX_VALUE))
         );
         bottomPanelLayout.setVerticalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(middlePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
                )
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(middlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
         ));
 
         pack();
     }
 
     /**
      * Fills in the dynamic parts of the admin Panel
      */
     private void displayAdminPanel(String query) {
     	
     	ArrayList<javax.swing.JLabel> products = createProductLabels(query);
     	setInnerLeftProductPanelLayout(products);
     	
     	searchField.grabFocus();
 		searchField.setText(searchField.getText());
     	
     }
 
 	private ArrayList<JLabel> createProductLabels(String query) {
     	ArrayList<javax.swing.JLabel> productLabels = new ArrayList<javax.swing.JLabel>();
     	ArrayList<Product> products = AdminController.getRelevantProducts(query);
     	
     	for (int i=0; i < products.size(); i++) {
     		final Product product = products.get(i);
         	// Create a new JLabel
         	javax.swing.JLabel label = Labels.createBorderlessLabel(product.getName(), i);
             label.addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     productLabelMouseClicked(product);
                 }
             });
             // Add the JLabel to the array of JLabels
             productLabels.add(label);
     	}
     	return productLabels;
 	}
 
     private void setInnerLeftProductPanelLayout(ArrayList<JLabel> products) {
     	javax.swing.GroupLayout innerLeftProductPanelLayout = new javax.swing.GroupLayout(innerLeftProductPanel);
         innerLeftProductPanel.removeAll();
     	innerLeftProductPanel.setLayout(innerLeftProductPanelLayout);
         
         javax.swing.GroupLayout.ParallelGroup tempHorizontalGroup = innerLeftProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);        
         for (int i = 0; i < products.size(); i++){
         	tempHorizontalGroup.addComponent(products.get(i), javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE);
         }
         
         javax.swing.GroupLayout.SequentialGroup tempVerticalGroup = innerLeftProductPanelLayout.createSequentialGroup();
         for (int i = 0; i < products.size(); i++){
         	tempVerticalGroup.addComponent(products.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE);
         }
         
         innerLeftProductPanelLayout.setHorizontalGroup(tempHorizontalGroup);
         innerLeftProductPanelLayout.setVerticalGroup(
             innerLeftProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tempVerticalGroup)
         );
     }
     
     private void searchFieldKeyTyped(KeyEvent evt) {
 		displayAdminPanel(searchField.getText());
 	}
 
 	private void searchFieldMouseClicked(MouseEvent evt) {
 		searchField.setText("");
 		currentProduct = null;
 		clearProductFields();
 		
 		addEditButton.setText("Legg til");
 		Button.changeColor(addEditButton, "green");
 		Button.changeIcon(addEditButton, "tick");
 		
 		displayAdminPanel("");
 	}
     
     
 	private void productLabelMouseClicked(Product product) {
 		currentProduct = product;
 		productNameField.setText(product.getName());
 		priceField.setText(""+product.getPrice());
 		commentField.setText(product.getComment());
 		//Make the addEditButton yellow and the delete button red.
 		addEditButton.setText("Endre");
 		Button.changeColor(addEditButton, "yellow");
 		Button.changeIcon(addEditButton, "warning");
 		
 		deleteButton.setText("Slett");
 		Button.changeColor(deleteButton, "red");
 		Button.changeIcon(deleteButton, "delete");
 
 	}
     	
 	private void backButtonMouseClicked(java.awt.event.MouseEvent evt) {                                        
     	MainMenuForm form = new MainMenuForm();
     	form.setVisible(true);
     	this.setVisible(false);
     }
     private void addEditButtonMouseClicked(java.awt.event.MouseEvent evt) {   
     	boolean legalValues = true;
     	String name = this.productNameField.getText();
     	String comment = this.commentField.getText();
     	Color color = new Color(235, 210, 210);
     	double price = -1;
     	try {
     		price = Double.parseDouble(this.priceField.getText());
 		} catch (Exception e) {
 			legalValues = false;
 			this.priceField.setBackground(color);
 			
 		}
     	if (!Validate.stringNonEmpty(name) || !Validate.isStringLegal(name)){
     		this.productNameField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.productNameField.setBackground(Color.WHITE);
     	}
     	if (!Validate.isStringLegal(comment)){
     		this.telephoneField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.telephoneField.setBackground(Color.WHITE);
     	}
     	
     	if (legalValues && currentProduct==null && price>=0){
     		if (AdminController.addProduct(name, price, comment)){
     			addEditButton.setText("Lagt til");
     			this.priceField.setText("");
     			this.productNameField.setText("");
     			this.commentField.setText("");
     			currentProduct=null;
     			displayAdminPanel("");
     		}
     	}
     	else if (legalValues && price >= 0){
     		if (AdminController.updateProduct(currentProduct, name, price, comment)){
     			addEditButton.setText("Endret");
     			Button.changeColor(addEditButton, "green");
     			Button.changeIcon(addEditButton, "tick");
     			
     			this.priceField.setText("");
     			this.productNameField.setText("");
     			this.commentField.setText("");
     			currentProduct=null;
     			displayAdminPanel("");
     		}
     	}
     	
     	
     }                                       
 
     private void deleteButtonMouseClicked(java.awt.event.MouseEvent evt) {
     	if (currentProduct != null && AdminController.deleteProduct(currentProduct)){
     		AdminController.deleteProduct(currentProduct);
     		displayAdminPanel("");
     		
     		deleteButton.setText("Slettet");
     		Button.changeColor(deleteButton, "green");
     		Button.changeIcon(deleteButton, "tick");
     		
     		addEditButton.setText("Legg til");
     		Button.changeColor(addEditButton, "green");
     		Button.changeIcon(addEditButton, "tick");
     		
 			clearProductFields();
     	}
     }
 
     private void editButtonMouseClicked(java.awt.event.MouseEvent evt) {
     	String name = restaurantNameField.getText();
     	String phone = telephoneField.getText();
     	String address = addressField.getText();
     	String zipCode = zipCodeField.getText();
     	String postalAddress = postalAddressField.getText();
     	boolean legalValues = true;
     	Color color = new Color(235, 210, 210);
     	double limitFreeDelivery = -1;
     	
     	try {
     		limitFreeDelivery = Double.parseDouble(freeDeliveryLimitField.getText());
 		} catch (Exception e) {
 			legalValues = false;
 			this.priceField.setBackground(color);
 			this.freeDeliveryLimitField.setBackground(color);
 		}
     	if (!Validate.stringNonEmpty(name) || !Validate.isStringLegal(name)){
     		this.productNameField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.productNameField.setBackground(Color.WHITE);
     	}
     	if (!Validate.stringNonEmpty(phone) || !Validate.isStringLegal(phone)){
     		this.telephoneField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.telephoneField.setBackground(Color.WHITE);
     	}
     	if (!Validate.stringNonEmpty(address) || !Validate.isStringLegal(address)){
     		this.addressField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.addressField.setBackground(Color.WHITE);
     	}
     	if (!Validate.isStringOnlyNumbers(zipCode) || !Validate.isStringLegal(zipCode)){
     		this.zipCodeField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.zipCodeField.setBackground(Color.WHITE);
     	}
     	if (!Validate.stringNonEmpty(postalAddress) || !Validate.isStringLegal(postalAddress)){
     		this.postalAddressField.setBackground(color);
     		legalValues = false;
     	}
     	else {
     		this.postalAddressField.setBackground(Color.WHITE);
     	}
     	
     	if (legalValues){
     			AdminController.setRestaurantProperties(name, address, zipCode, postalAddress, phone, limitFreeDelivery);
     			editButton.setText("Endret");
         		Button.changeColor(addEditButton, "green");
         		Button.changeIcon(addEditButton, "tick");
     	}
     }
     
     private void clearProductFields(){
     	currentProduct = null;
     	this.productNameLabel.setBackground(Color.WHITE);
     	this.priceField.setBackground(Color.WHITE);
     	this.commentLabel.setBackground(Color.WHITE);
     	this.productNameField.setText("");
     	this.priceField.setText("");
     	this.commentField.setText("");
     }
     
 
     // Variables declaration
     private javax.swing.JLabel addEditButton;
     private javax.swing.JTextField addressField;
     private javax.swing.JLabel addressLabel;
     private javax.swing.JLabel backButton;
     private javax.swing.JPanel bottomPanel;
     private javax.swing.JTextField commentField;
     private javax.swing.JLabel commentLabel;
     private javax.swing.JLabel deleteButton;
     private javax.swing.JTextField freeDeliveryLimitField;
     private javax.swing.JLabel freeDeliveryLimitLabel;
     private javax.swing.JPanel leftProductPanel;
     private javax.swing.JScrollPane productScrollPane;
     private javax.swing.JPanel innerLeftProductPanel;
     private javax.swing.JTextField searchField;
     private javax.swing.JPanel leftRestaurantPanel;
     private javax.swing.JTextField productNameField;
     private javax.swing.JPanel middlePanel;
     private javax.swing.JLabel nameAddressHeaderLabel;
     private javax.swing.JLabel newOrderLabel;
     private javax.swing.JLabel otherInfoHeaderLabel;
     private javax.swing.JTextField postalAddressField;
     private javax.swing.JLabel postalAddressLabel;
     private javax.swing.JTextField priceField;
     private javax.swing.JLabel priceLabel;
     private javax.swing.JLabel productNameLabel;
     private javax.swing.JPanel productPanel;
     private javax.swing.JTextField restaurantNameField;
     private javax.swing.JLabel restaurantNameLabel;
     private javax.swing.JPanel restaurantPanel;
     private javax.swing.JPanel rightProductPanel;
     private javax.swing.JPanel rightRestaurantPanel;
     private javax.swing.JTabbedPane tabPane;
     private javax.swing.JTextField telephoneField;
     private javax.swing.JLabel telephoneLabel;
     private javax.swing.JPanel topPanel;
     private javax.swing.JLabel editButton;
     private javax.swing.JTextField zipCodeField;
     private javax.swing.JLabel zipCodeLabel;
     // End of variables declaration
 }
