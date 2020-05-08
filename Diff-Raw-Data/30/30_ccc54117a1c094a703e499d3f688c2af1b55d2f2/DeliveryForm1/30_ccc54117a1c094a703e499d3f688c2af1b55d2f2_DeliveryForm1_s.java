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
 
 /*
  * DeliveryForm1.java
  *
  * Created on 29.sep.2011, 12:24:07
  */
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
         setName("newOrderFrame"); // NOI18N
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
 
 
         backButton.setBackground(new java.awt.Color(235, 210, 210));
         backButton.setFont(new java.awt.Font("Georgia", 0, 18));
         backButton.setForeground(new java.awt.Color(115, 35, 35));
         backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/leftred_32.png"))); // NOI18N
         backButton.setText(" Tilbake");
         backButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(205, 135, 135), 2), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         backButton.setOpaque(true);
         backButton.setPreferredSize(new java.awt.Dimension(140, 50));
         backButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 backButtonMouseClicked(evt);
             }
         });
 
         updateButton.setBackground(new java.awt.Color(225, 230, 235));
         updateButton.setFont(new java.awt.Font("Georgia", 0, 18));
         updateButton.setForeground(new java.awt.Color(45, 65, 105));
         updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/clock_32.png"))); // NOI18N
         updateButton.setText(" Oppdater");
         updateButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(170, 180, 200), 2), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         updateButton.setOpaque(true);
         updateButton.setPreferredSize(new java.awt.Dimension(140, 20));
         updateButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 UpdateButtonMouseClicked(evt);
             }
         });
 
         yellowColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         yellowColorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/yellow.png"))); // NOI18N
         yellowColorLabel.setText(" Påbegynt");
 
         colorHeaderLabel.setFont(new java.awt.Font("Georgia", 0, 18));
         colorHeaderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         colorHeaderLabel.setText("Fargeforklaring:");
 
         blueColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         blueColorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/blue.png"))); // NOI18N
         blueColorLabel.setText(" Ikke påbegynt");
 
         redColorLabel.setFont(new java.awt.Font("Georgia", 0, 13));
         redColorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/red.png"))); // NOI18N
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void updateLeftPanel() {
     	ArrayList<JLabel> orderLabels = createOrderLabels();
     	
     	javax.swing.GroupLayout orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
     	orderPanel.removeAll();
     	orderPanel.setLayout(orderPanelLayout);
     	
     	defineOrderPanelHorizontalLayout(orderPanelLayout, orderLabels);
         defineOrderPanelVerticalLayout(orderPanelLayout, orderLabels);
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
         	// Create the upper half of the order Label
         	javax.swing.JLabel upperHalf = new javax.swing.JLabel();
         	// Create the lower half of the order Label
         	javax.swing.JLabel lowerHalf = new javax.swing.JLabel();
 			if (order.getStatus().equals("Under levering")) {
 				upperHalf.setBackground(new java.awt.Color(245, 245, 215));
 		        upperHalf.setForeground(new java.awt.Color(100, 70, 20));
 		        upperHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(235, 210, 155)), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 		        lowerHalf.setBackground(new java.awt.Color(245, 245, 215));
 	        	lowerHalf.setForeground(new java.awt.Color(100, 70, 20));
 	        	lowerHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, new java.awt.Color(235, 210, 155)), javax.swing.BorderFactory.createEmptyBorder(0, 10, 7, 10)));
 			}
 			else if ((order.getComment()!=null) && !order.getComment().equals("") || order.getAllergy()) {
 					upperHalf.setBackground(new java.awt.Color(235, 210, 210));
 			        upperHalf.setForeground(new java.awt.Color(115, 35, 35));
 			        upperHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(205, 135, 135)), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 			        lowerHalf.setBackground(new java.awt.Color(235, 210, 210));
 		        	lowerHalf.setForeground(new java.awt.Color(115, 35, 35));
 		        	lowerHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, new java.awt.Color(205, 135, 135)), javax.swing.BorderFactory.createEmptyBorder(0, 10, 7, 10)));
 
 			}
 			else {
 				upperHalf.setBackground(new java.awt.Color(225, 230, 235));
 		        upperHalf.setForeground(new java.awt.Color(45, 65, 105));
 		        upperHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 0, 2, new java.awt.Color(170, 180, 200)), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 		        lowerHalf.setBackground(new java.awt.Color(225, 230, 235));
 	        	lowerHalf.setForeground(new java.awt.Color(45, 65, 105));
 	        	lowerHalf.setBorder(javax.swing.BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, new java.awt.Color(170, 180, 200)), javax.swing.BorderFactory.createEmptyBorder(0, 10, 7, 10)));
 
 			}
         	upperHalf.setFont(new java.awt.Font("Georgia", 0, 18));
         	upperHalf.setText(order.getDateAndTime() + ":    " + order.getCustomer().getAddress());
         	upperHalf.setOpaque(true);
             upperHalf.setPreferredSize(new java.awt.Dimension(140, 20));
         	upperHalf.addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     orderLabelMouseClicked(evt, order);
                 }
             });
         	lowerHalf.setText(orderText);
         	lowerHalf.setOpaque(true);
             lowerHalf.setPreferredSize(new java.awt.Dimension(140, 20));
         	lowerHalf.addMouseListener(new java.awt.event.MouseAdapter() {
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                 	orderLabelMouseClicked(evt, order);
                 }
             });
 
         	labels.add(upperHalf);
         	labels.add(lowerHalf);
         }
         return labels;
     }
     
 	private void defineOrderPanelHorizontalLayout(GroupLayout orderPanelLayout, ArrayList<JLabel> orderLabels) {
         //orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
         //orderPanel.setLayout(orderPanelLayout);
         
 		javax.swing.GroupLayout.ParallelGroup tempHorizontalGroup = orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);        
         for (int i = 0; i < orderLabels.size(); i++){
         	tempHorizontalGroup.addComponent(orderLabels.get(i), javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE);
         }
 		
         orderPanelLayout.setHorizontalGroup(
             orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(orderPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tempHorizontalGroup)
                .addContainerGap(534, Short.MAX_VALUE))
         );
         
 	}
     
     private void defineOrderPanelVerticalLayout(GroupLayout orderPanelLayout, ArrayList<JLabel> orderLabels) {
     	//orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
         //orderPanel.setLayout(orderPanelLayout);
     	
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
 
 	private void backButtonMouseClicked(java.awt.event.MouseEvent evt) {                                        
     	MainMenuForm form = new MainMenuForm();         
         form.setVisible(true);
         this.setVisible(false); 
     }                                       
 
     private void UpdateButtonMouseClicked(java.awt.event.MouseEvent evt) {                                          
         // TODO add your handling code here:
     }                                         
 
     private void orderLabelMouseClicked(java.awt.event.MouseEvent evt, Order order) {                                                     
     	DeliveryForm2 form = new DeliveryForm2(order);         
         form.setVisible(true);
         this.setVisible(false);
     }                                                    
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(DeliveryForm1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(DeliveryForm1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(DeliveryForm1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(DeliveryForm1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new DeliveryForm1().setVisible(true);
             }
         });
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
