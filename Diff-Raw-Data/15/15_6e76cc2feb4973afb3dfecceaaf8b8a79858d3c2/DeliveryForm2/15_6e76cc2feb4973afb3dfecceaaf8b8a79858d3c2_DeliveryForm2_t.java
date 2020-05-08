 package view;
 //no.ntnu.course
 
 import java.awt.Color;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 
 import com.itextpdf.text.DocumentException;
 
 import model.DeliveryFee;
 import model.GoogleMaps;
 import model.Order;
 import model.Product;
 import model.Receipt;
 import controller.DeliveryController;
 import controller.ManageOrder;
 
 
 /**
  *
  * @author Morten Vaale Noddeland
  */
 @SuppressWarnings("serial")
 public class DeliveryForm2 extends javax.swing.JFrame {
 
 	private Order order;
     /** Creates new form DeliveryForm2 */
     public DeliveryForm2(Order order) {
         this.order = order;
     	initComponents();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      */
     private void initComponents() {
 
         topPanel = new javax.swing.JPanel();
         headerLabel = new javax.swing.JLabel();
         middlePanel = new javax.swing.JPanel();
         rightPanel = new javax.swing.JPanel();
         nameAddressHeaderLabel = new javax.swing.JLabel();
         nameHeaderLabel = new javax.swing.JLabel();
         nameContentLabel = new javax.swing.JLabel();
         addressHeaderLabel = new javax.swing.JLabel();
         addressContentLabel = new javax.swing.JLabel();
         zipCodeContentLabel = new javax.swing.JLabel();
         telephoneHeaderLabel = new javax.swing.JLabel();
         telephoneContentLabel = new javax.swing.JLabel();
         mapHeaderLabel = new javax.swing.JLabel();
         mapPanel = new javax.swing.JPanel();
         leftPanel = new javax.swing.JPanel();
         productsHeadersPanel = new javax.swing.JPanel();
         quantityHeaderLabel = new javax.swing.JLabel();
         productHeaderLabel = new javax.swing.JLabel();
         ProductsScrollPane = new javax.swing.JScrollPane();
         productsPanel = new javax.swing.JPanel();
         quantityPanel = new javax.swing.JPanel();
         productPanel = new javax.swing.JPanel();
         commentPanel = new javax.swing.JPanel();
         commentHeaderLabel = new javax.swing.JLabel();
         commentScrollPane = new javax.swing.JScrollPane();
         commentTextPane = new javax.swing.JTextPane();
         bottomPanel = new javax.swing.JPanel();
         backButton = new javax.swing.JLabel(); 
         startFinishButton = new javax.swing.JLabel();
         priceLabel = new javax.swing.JLabel();
         receiptButton = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Utkjøring - detaljer");
         setBounds(new java.awt.Rectangle(0, 0, 0, 0));
 
         topPanel.setBackground(new java.awt.Color(220, 220, 220));
         topPanel.setPreferredSize(new java.awt.Dimension(780, 100));
         topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray));
 
 
         headerLabel.setFont(new java.awt.Font("Georgia", 0, 36));
         headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         headerLabel.setText("Utkjøring - detaljer");
 
         javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
         topPanel.setLayout(topPanelLayout);
         topPanelLayout.setHorizontalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                 .addContainerGap())
         );
         topPanelLayout.setVerticalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addGap(9, 9, 9)
                 .addComponent(headerLabel)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         middlePanel.setBackground(new java.awt.Color(253, 253, 253));
         middlePanel.setPreferredSize(new java.awt.Dimension(800, 470));
 
         rightPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         nameAddressHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         nameAddressHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         nameAddressHeaderLabel.setText("Navn og adresse");
         nameAddressHeaderLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
         nameAddressHeaderLabel.setOpaque(true);
         nameAddressHeaderLabel.setPreferredSize(new java.awt.Dimension(800, 40));
 
         nameHeaderLabel.setFont(new java.awt.Font("Georgia", 1, 14));
         nameHeaderLabel.setText("Navn:");
 
         addressHeaderLabel.setFont(new java.awt.Font("Georgia", 1, 14));
         addressHeaderLabel.setText("Adresse:");
         
         telephoneHeaderLabel.setFont(new java.awt.Font("Georgia", 1, 14));
         telephoneHeaderLabel.setText("Telefon:");
         
         nameContentLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         addressContentLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         zipCodeContentLabel.setFont(new java.awt.Font("Georgia", 0, 14));
         telephoneContentLabel.setFont(new java.awt.Font("Georgia", 0, 14));
 
         mapHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         mapHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         mapHeaderLabel.setText("Kart");
         mapHeaderLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
         mapHeaderLabel.setOpaque(true);
         mapHeaderLabel.setPreferredSize(new java.awt.Dimension(800, 40));
 
         mapPanel.setBackground(new java.awt.Color(240, 240, 240));
         mapPanel.setPreferredSize(new java.awt.Dimension(329, 329));
 
         javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
         mapPanel.setLayout(mapPanelLayout);
         mapPanelLayout.setHorizontalGroup(
             mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 329, Short.MAX_VALUE)
         );
         mapPanelLayout.setVerticalGroup(
             mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 244, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
         rightPanel.setLayout(rightPanelLayout);
         rightPanelLayout.setHorizontalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(nameAddressHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
             .addGroup(rightPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(nameHeaderLabel)
                     .addComponent(addressHeaderLabel)
                     .addComponent(telephoneHeaderLabel))
                 .addGap(23, 23, 23)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(telephoneContentLabel)
                     .addComponent(zipCodeContentLabel)
                     .addComponent(addressContentLabel)
                     .addComponent(nameContentLabel))
                 .addContainerGap(108, Short.MAX_VALUE))
             .addComponent(mapHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
             .addGroup(rightPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         rightPanelLayout.setVerticalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rightPanelLayout.createSequentialGroup()
                 .addComponent(nameAddressHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nameContentLabel)
                     .addComponent(nameHeaderLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(addressContentLabel)
                     .addComponent(addressHeaderLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(zipCodeContentLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(telephoneContentLabel)
                     .addComponent(telephoneHeaderLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(mapHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                 .addContainerGap())
         );
         
         quantityHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         quantityHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         quantityHeaderLabel.setText("Antall");
         quantityHeaderLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
         quantityHeaderLabel.setOpaque(true);
         quantityHeaderLabel.setPreferredSize(new java.awt.Dimension(800, 40));
 
         productHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         productHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         productHeaderLabel.setText("Produkt");
         productHeaderLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
         productHeaderLabel.setOpaque(true);
         productHeaderLabel.setPreferredSize(new java.awt.Dimension(800, 40));
 
         javax.swing.GroupLayout productsHeadersPanelLayout = new javax.swing.GroupLayout(productsHeadersPanel);
         productsHeadersPanel.setLayout(productsHeadersPanelLayout);
         productsHeadersPanelLayout.setHorizontalGroup(
             productsHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(productsHeadersPanelLayout.createSequentialGroup()
                 .addComponent(quantityHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(productHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
         );
         productsHeadersPanelLayout.setVerticalGroup(
             productsHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(productsHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(quantityHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(productHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         ProductsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         ProductsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         ProductsScrollPane.setPreferredSize(new java.awt.Dimension(800, 240));
         ProductsScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.darkGray));
 
 
         productsPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         quantityPanel.setBackground(new java.awt.Color(253, 253, 253));
         quantityPanel.setPreferredSize(new java.awt.Dimension(80, 302));
 
         
         productPanel.setBackground(new java.awt.Color(253, 253, 253));
         productPanel.setPreferredSize(new java.awt.Dimension(300, 302));
 
         // Fetch all information that's relevant and display it
         displayOrder();
         displayMap();
 
 
         javax.swing.GroupLayout productsPanelLayout = new javax.swing.GroupLayout(productsPanel);
         productsPanel.setLayout(productsPanelLayout);
         productsPanelLayout.setHorizontalGroup(
             productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(productsPanelLayout.createSequentialGroup()
                 .addComponent(quantityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(productPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                 )
         );
         productsPanelLayout.setVerticalGroup(
             productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(productsPanelLayout.createSequentialGroup()
                 .addGroup(productsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(quantityPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                     .addComponent(productPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                 )
         );
 
         ProductsScrollPane.setViewportView(productsPanel);
 
         commentPanel.setBackground(new java.awt.Color(253, 253, 253));
         commentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.darkGray));
 
 
         commentHeaderLabel.setBackground(new java.awt.Color(240, 240, 240));
         commentHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         commentHeaderLabel.setText("Kommentar");
         commentHeaderLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5)));
         commentHeaderLabel.setOpaque(true);
         commentHeaderLabel.setPreferredSize(new java.awt.Dimension(800, 40));
 
         commentScrollPane.setBorder(null);
 
         commentTextPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         commentTextPane.setEditable(false);
         commentScrollPane.setViewportView(commentTextPane);
 
         javax.swing.GroupLayout commentPanelLayout = new javax.swing.GroupLayout(commentPanel);
         commentPanel.setLayout(commentPanelLayout);
         commentPanelLayout.setHorizontalGroup(
             commentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(commentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
             .addComponent(commentHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
         );
         commentPanelLayout.setVerticalGroup(
             commentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(commentPanelLayout.createSequentialGroup()
                 .addComponent(commentHeaderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(commentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
         leftPanel.setLayout(leftPanelLayout);
         leftPanelLayout.setHorizontalGroup(
             leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(productsHeadersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(ProductsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
             .addComponent(commentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         leftPanelLayout.setVerticalGroup(
             leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(leftPanelLayout.createSequentialGroup()
                 .addComponent(productsHeadersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(ProductsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                 .addComponent(commentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout middlePanelLayout = new javax.swing.GroupLayout(middlePanel);
         middlePanel.setLayout(middlePanelLayout);
         middlePanelLayout.setHorizontalGroup(
             middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(middlePanelLayout.createSequentialGroup()
                 .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         middlePanelLayout.setVerticalGroup(
             middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(rightPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
 
         
         if (order.getStatus().equals("Klar til levering")){
         	startFinishButton = Button.createButton("Start", "yellow", "label");
         }
         else{
         	startFinishButton = Button.createButton("Ferdig", "green", "buy");
         }
         startFinishButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 UpdateButtonMouseClicked(evt);
             }
         });
 
         priceLabel.setFont(new java.awt.Font("Georgia", 0, 30));
         priceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         
         receiptButton = Button.createButton("Kvittering", "blue", "document");
         receiptButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 receiptButtonMouseClicked(evt);
             }
         });
 
 
         javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
         bottomPanel.setLayout(bottomPanelLayout);
         bottomPanelLayout.setHorizontalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(priceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(receiptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(startFinishButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         
         bottomPanelLayout.setVerticalGroup(
                 bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(bottomPanelLayout.createSequentialGroup()
                     .addGap(10, 10, 10)
                     .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(startFinishButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(priceLabel)
                         .addComponent(receiptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             );
         
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(bottomPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(topPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                     .addComponent(middlePanel, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         ));
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(middlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
         ));
 
         pack();
     }
 
     private void displayOrder() {
     	ArrayList<ArrayList<JLabel>> labels = createProductLabels();
     	
     	setQuantityPanelLayout(labels.get(0));
     	setProductPanelLayout(labels.get(1));
     	
     	commentTextPane.setText(order.getComment());
     	nameContentLabel.setText(order.getCustomer().getName());
         addressContentLabel.setText(order.getCustomer().getAddress());
         zipCodeContentLabel.setText(order.getCustomer().getzipCode() + " " + order.getCustomer().getPostalAddress());
         telephoneContentLabel.setText(order.getCustomer().getPhone());
         priceLabel.setText(ManageOrder.formatPrice(ManageOrder.getTotalPrice(order)));
     }
 
 	private ArrayList<ArrayList<JLabel>> createProductLabels() {
     	ArrayList<Product> productsInOrder = order.getProductsInOrder();
 
         ArrayList <javax.swing.JLabel> quantityLabels = new ArrayList <javax.swing.JLabel>();
         ArrayList <javax.swing.JLabel> productLabels = new ArrayList <javax.swing.JLabel>();
 
 
         int i = 0;
         for (Product product : productsInOrder) {
 			if (product instanceof DeliveryFee){
 				continue;
 			}
         	// Create quantityLabels
 			String quantityText = "" + product.getQuantity();
         	javax.swing.JLabel quantityLabel = Labels.createBorderlessLabel(quantityText, i);
 	        quantityLabels.add(quantityLabel);
 	        
 	        // Create productLabels
 	        String productText = "" + product.getName();
         	javax.swing.JLabel productLabel = Labels.createBorderlessLabel(productText, i);
 	        productLabels.add(productLabel);
 	        
 	        i++;
         }
         ArrayList<ArrayList <javax.swing.JLabel>> labels = new ArrayList<ArrayList <javax.swing.JLabel>>();
         labels.add(quantityLabels);
         labels.add(productLabels);
         return labels;
 	}
 
 	private void setQuantityPanelLayout(ArrayList<JLabel> quantityLabels) {
 		javax.swing.GroupLayout quantityPanelLayout = new javax.swing.GroupLayout(quantityPanel);
         quantityPanel.setLayout(quantityPanelLayout);
         
         javax.swing.GroupLayout.ParallelGroup tempHorizontalGroup = quantityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);        
         for (int i = 0; i < quantityLabels.size(); i++){
         	tempHorizontalGroup.addComponent(quantityLabels.get(i), javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE);
         }
         
         javax.swing.GroupLayout.SequentialGroup tempVerticalGroup = quantityPanelLayout.createSequentialGroup();
         for (int i = 0; i < quantityLabels.size(); i++){
         	tempVerticalGroup.addComponent(quantityLabels.get(i));
         }
         
         quantityPanelLayout.setHorizontalGroup(tempHorizontalGroup);
         quantityPanelLayout.setVerticalGroup(
             quantityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tempVerticalGroup)
         );
 	}
     
     private void setProductPanelLayout(ArrayList<JLabel> productLabels) {
     	javax.swing.GroupLayout productPanelLayout = new javax.swing.GroupLayout(productPanel);
         productPanel.setLayout(productPanelLayout);
         
         javax.swing.GroupLayout.ParallelGroup tempHorizontalGroup = productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);        
         for (int i = 0; i < productLabels.size(); i++){
         	tempHorizontalGroup.addComponent(productLabels.get(i), javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE);
         }
         
         javax.swing.GroupLayout.SequentialGroup tempVerticalGroup = productPanelLayout.createSequentialGroup();
         for (int i = 0; i < productLabels.size(); i++){
         	tempVerticalGroup.addComponent(productLabels.get(i));
         }
         
         productPanelLayout.setHorizontalGroup(tempHorizontalGroup);
         productPanelLayout.setVerticalGroup(
             productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tempVerticalGroup)
         );
 	}
 
 	private void displayMap(){
     	JLabel map;
 		try {
 			map = GoogleMaps.map(order.getCustomer().getAddress() + " " + order.getCustomer().getzipCode());
 	    	
 			javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
 	        mapPanel.removeAll();
 	        mapPanel.setLayout(mapPanelLayout);
 	        mapPanelLayout.setHorizontalGroup(
 	            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 	            .addComponent(map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
 	        );
 	        mapPanelLayout.setVerticalGroup(
 	            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 	            .addComponent(map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
 	        );
 		} catch (MalformedURLException e) {
			Error.showMessage("Det oppstod en feil under lastingen av kartet");
 			e.printStackTrace();
 		} catch (IOException e) {
			Error.showMessage("Det oppstod en feil under lastingen av kartet");
 			e.printStackTrace();
 		}
 	}
 	
 	private void backButtonMouseClicked(java.awt.event.MouseEvent evt) {                                        
     	DeliveryForm1 form = new DeliveryForm1();
     	form.setVisible(true);
     	this.setVisible(false);
     }
 	
 	private void receiptButtonMouseClicked(java.awt.event.MouseEvent evt) {
         try {
 			Receipt.makeReceipe(order);
 			Receipt.openReceipt();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     private void UpdateButtonMouseClicked(java.awt.event.MouseEvent evt) {     
     	if(order.getStatus().equals("Klar til levering")){
     		DeliveryController.startOrder(order);
     		startFinishButton.setText("Ferdig");
     		Button.changeIcon(startFinishButton, "buy");
     		Button.changeColor(startFinishButton, "green");
     	}
     	else{
     		DeliveryController.setFinishedOrder(order);
     		DeliveryForm1 form = new DeliveryForm1();
     		form.setVisible(true);
     		this.setVisible(false);
     	}
     }
 
     // Variables declaration - do not modify
     private javax.swing.JScrollPane ProductsScrollPane;
     private javax.swing.JLabel addressContentLabel;
     private javax.swing.JLabel addressHeaderLabel;
     private javax.swing.JLabel backButton;
     private javax.swing.JPanel bottomPanel;
     private javax.swing.JLabel commentHeaderLabel;
     private javax.swing.JPanel commentPanel;
     private javax.swing.JScrollPane commentScrollPane;
     private javax.swing.JTextPane commentTextPane;
     private javax.swing.JLabel headerLabel;
     private javax.swing.JPanel leftPanel;
     private javax.swing.JLabel mapHeaderLabel;
     private javax.swing.JPanel mapPanel;
     private javax.swing.JPanel middlePanel;
     private javax.swing.JLabel nameAddressHeaderLabel;
     private javax.swing.JLabel nameContentLabel;
     private javax.swing.JLabel nameHeaderLabel;
     private javax.swing.JLabel priceLabel;
     private javax.swing.JLabel productHeaderLabel;
     private javax.swing.JPanel productPanel;
     private javax.swing.JPanel productsHeadersPanel;
     private javax.swing.JPanel productsPanel;
     private javax.swing.JLabel quantityHeaderLabel;
     private javax.swing.JPanel quantityPanel;
     private javax.swing.JPanel rightPanel;
     private javax.swing.JLabel startFinishButton;
     private javax.swing.JLabel telephoneContentLabel;
     private javax.swing.JLabel telephoneHeaderLabel;
     private javax.swing.JPanel topPanel;
     private javax.swing.JLabel zipCodeContentLabel;
     private javax.swing.JLabel receiptButton;
     // End of variables declaration
 }
