 package view;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 
 import model.DeliveryFee;
 import model.GoogleMaps;
 import model.Order;
 import model.Product;
 import controller.DeliveryController;
 
 
 /**
  *
  * @author Morten Vaale Noddeland
  */
 @SuppressWarnings("serial")
 public class DeliveryForm1 extends javax.swing.JFrame {
 
     /** Creates new form DeliveryForm1 */
     public DeliveryForm1() {
         initComponents();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      */
     private void initComponents() {
 
         topPanel = new javax.swing.JPanel();
         headerLabel = new javax.swing.JLabel();
         leftPanel = new javax.swing.JPanel();
         leftScrollPane = new javax.swing.JScrollPane();
         orderPanel = new javax.swing.JPanel();
         rightPanel = new javax.swing.JPanel();
         mapPanel = new javax.swing.JPanel();
         bottomPanel = new javax.swing.JPanel();
         backButton = new javax.swing.JLabel();
         updateButton = new javax.swing.JLabel();
         yellowColorLabel = new javax.swing.JLabel();
         colorHeaderLabel = new javax.swing.JLabel();
         blueColorLabel = new javax.swing.JLabel();
         redColorLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Utkjøring - oversikt");
         setBounds(new java.awt.Rectangle(0, 0, 0, 0));
         setResizable(false);
 
         topPanel.setBackground(new java.awt.Color(220, 220, 220));
         topPanel.setPreferredSize(new java.awt.Dimension(780, 100));
         topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray));
 
 
         headerLabel.setFont(new java.awt.Font("Georgia", 0, 36));
         headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         headerLabel.setText("Utkjøring - oversikt");
 
         javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
         topPanel.setLayout(topPanelLayout);
         topPanelLayout.setHorizontalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         topPanelLayout.setVerticalGroup(
             topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(topPanelLayout.createSequentialGroup()
                 .addGap(9, 9, 9)
                 .addComponent(headerLabel)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         leftPanel.setBackground(new java.awt.Color(253, 253, 253));
         leftPanel.setPreferredSize(new java.awt.Dimension(400, 370));
         leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.darkGray));
 
 
         leftScrollPane.setBorder(null);
         leftScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         leftScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         leftScrollPane.setPreferredSize(new java.awt.Dimension(800, 470));
 
         orderPanel.setBackground(new java.awt.Color(253, 253, 253));
         
         // Displays all orders
         updateLeftPanel();        
 
         leftScrollPane.setViewportView(orderPanel);
 
         javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
         leftPanel.setLayout(leftPanelLayout);
         leftPanelLayout.setHorizontalGroup(
             leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(leftScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
         );
         leftPanelLayout.setVerticalGroup(
             leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(leftScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
         );
 
         rightPanel.setBackground(new java.awt.Color(253, 253, 253));
 
         mapPanel.setPreferredSize(new java.awt.Dimension(349, 455));
         
         // Displays the map
         updateRightPanel();
 
         javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
         rightPanel.setLayout(rightPanelLayout);
         rightPanelLayout.setHorizontalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         rightPanelLayout.setVerticalGroup(
             rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rightPanelLayout.createSequentialGroup()
             .addGap(7, 7, 7)
             .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         ));
 
         bottomPanel.setBackground(new java.awt.Color(220, 220, 220));
         bottomPanel.setPreferredSize(new java.awt.Dimension(800, 100));
         bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.darkGray));
 
         backButton = Button.createButton("Tilbake", "red", "leftred");
         backButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 backButtonMouseClicked(evt);
             }
         });
         
         updateButton = Button.createButton("Oppdater", "blue", "clock");
         updateButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 UpdateButtonMouseClicked(evt);
             }
         });
 
         yellowColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         yellowColorLabel.setIcon(new javax.swing.ImageIcon("icons/yellow_square.png"));
         yellowColorLabel.setText(" Påbegynt");
 
         colorHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         colorHeaderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         colorHeaderLabel.setText("Fargeforklaring:");
 
         blueColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         blueColorLabel.setIcon(new javax.swing.ImageIcon("icons/blue_square.png"));
         blueColorLabel.setText(" Ikke påbegynt");
 
         redColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         redColorLabel.setIcon(new javax.swing.ImageIcon("icons/red_square.png"));
         redColorLabel.setText(" Se kommentar");
 
         javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
         bottomPanel.setLayout(bottomPanelLayout);
         bottomPanelLayout.setHorizontalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(bottomPanelLayout.createSequentialGroup()
                         .addGap(18, 18, 18)
                         .addComponent(colorHeaderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))
                     .addGroup(bottomPanelLayout.createSequentialGroup()
                         .addGap(108, 108, 108)
                         .addComponent(blueColorLabel)
                         .addGap(18, 18, 18)
                         .addComponent(yellowColorLabel)
                         .addGap(18, 18, 18)
                         .addComponent(redColorLabel)))
                 .addGap(18, 18, 18)
                 .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         bottomPanelLayout.setVerticalGroup(
             bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(bottomPanelLayout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(bottomPanelLayout.createSequentialGroup()
                         .addComponent(colorHeaderLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(yellowColorLabel)
                             .addComponent(blueColorLabel)
                             .addComponent(redColorLabel)))
                     .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(bottomPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(topPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                         .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         ));
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                 .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }
 
 	private void updateLeftPanel() {
     	ArrayList<JLabel> orderLabels = createOrderLabels();
     	
     	javax.swing.GroupLayout orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
     	orderPanel.removeAll();
     	orderPanel.setLayout(orderPanelLayout);
     	
     	setOrderPanelLayout(orderPanelLayout, orderLabels);
     }
     
 	private ArrayList<JLabel> createOrderLabels() {
 		ArrayList<Order> orders = DeliveryController.getFreshOrders();
         ArrayList <javax.swing.JLabel> labels = new ArrayList <javax.swing.JLabel>();
 
         for (int i=0; i<orders.size(); i++){	
         	String orderText = "";
     		final Order order = orders.get(i);
         	for (Product product : orders.get(i).getProductsInOrder()) {
     			if (product instanceof DeliveryFee){
     				continue;
     			}
 			String productName = ((Product) product).getName();
 			int quantity = product.getQuantity();
 			// Set text for the JLabel
 			orderText += quantity + " stk: ";
 			orderText += productName + ", ";
     		}
         	//Remove the last two characters from orderText
         	if (orderText.length() > 0)
         		orderText = orderText.substring(0, (orderText.length() - 2));
 
         	// Create the Label
         	ArrayList<JLabel> label;
 			if (order.getStatus().equals("Under levering")) {
 				label = Labels.createTwoLineLabel(order.getDateAndTime() + ":    " + order.getCustomer().getAddress(), orderText, "yellow");
 			}
 			else if ((order.getComment()!=null) && !order.getComment().equals("") || order.getAllergy()) {
 				label = Labels.createTwoLineLabel(order.getDateAndTime() + ":    " + order.getCustomer().getAddress(), orderText, "red");
 			}
 			else {
 				label = Labels.createTwoLineLabel(order.getDateAndTime() + ":    " + order.getCustomer().getAddress(), orderText, "blue");
 			}
         	
 			label.get(0).addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     orderLabelMouseClicked(evt, order);
                 }
             });
 			label.get(1).addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                 	orderLabelMouseClicked(evt, order);
                 }
             });
 
         	labels.add(label.get(0));
         	labels.add(label.get(1));
         }
         return labels;
     }
     
 	private void setOrderPanelLayout(GroupLayout orderPanelLayout, ArrayList<JLabel> orderLabels) {
         
         // Set horizontal axis
 		javax.swing.GroupLayout.ParallelGroup tempHorizontalGroup = orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);        
         for (int i = 0; i < orderLabels.size(); i++){
         	tempHorizontalGroup.addComponent(orderLabels.get(i), javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE);
         }
 		
         orderPanelLayout.setHorizontalGroup(
             orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(orderPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tempHorizontalGroup)
             )
         );
         
         // Set vertical axis
     	javax.swing.GroupLayout.SequentialGroup tempVerticalGroup = orderPanelLayout.createSequentialGroup();
         tempVerticalGroup.addContainerGap();
     	for (int i = 0; i < orderLabels.size(); i++){
         	tempVerticalGroup.addComponent(orderLabels.get(i), javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE);
             if (i % 2 == 1) {
             	tempVerticalGroup.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
             }
         }
     	
     	orderPanelLayout.setVerticalGroup(
                 orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(tempVerticalGroup)
         );
         
 	}
 
     private void updateRightPanel() {
         
     	JLabel map;
 		try {
 			map = GoogleMaps.mainMap();
 	    	
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
			e.printStackTrace();
 		} catch (IOException e) {
			e.printStackTrace();
 		}
 	}
     
 	private void backButtonMouseClicked(java.awt.event.MouseEvent evt) {                                        
     	MainMenuForm form = new MainMenuForm();         
         form.setVisible(true);
         this.setVisible(false); 
     }                                       
 
     private void UpdateButtonMouseClicked(java.awt.event.MouseEvent evt) {                                          
         updateLeftPanel();
         updateRightPanel();
     }                                         
 
     private void orderLabelMouseClicked(java.awt.event.MouseEvent evt, Order order) {                                                     
     	DeliveryForm2 form = new DeliveryForm2(order);         
         form.setVisible(true);
         this.setVisible(false);
     }                                                    
 
     // Variables declaration - do not modify
     private javax.swing.JLabel backButton;
     private javax.swing.JLabel blueColorLabel;
     private javax.swing.JPanel bottomPanel;
     private javax.swing.JLabel colorHeaderLabel;
     private javax.swing.JLabel headerLabel;
     private javax.swing.JPanel leftPanel;
     private javax.swing.JScrollPane leftScrollPane;
     private javax.swing.JPanel mapPanel;
     private javax.swing.JPanel orderPanel;
     private javax.swing.JLabel redColorLabel;
     private javax.swing.JPanel rightPanel;
     private javax.swing.JPanel topPanel;
     private javax.swing.JLabel updateButton;
     private javax.swing.JLabel yellowColorLabel;
     // End of variables declaration
 }
