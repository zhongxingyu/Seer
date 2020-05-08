 package retailManagementSystem;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.table.DefaultTableModel;
 
 public class OrderListPanel extends JPanel implements ActionListener{
 	
 	private Database database;
 	
 	private String[] ordersID;
 	private String[] ordersDate;
 	private String[] ordersCost;
 	private String[] ordersSupplier;
 	
 	private DefaultTableModel orderTableModel;
 	
 	private JTable tableOfOrders;
 	
 	private JScrollPane orderScrollPane;
 	
 	private JLabel orderListLabel;
 	private JLabel newOrderListLabel;
 	private JLabel blankLabel;
 	
 	private JButton newOrderButton;
 	
 	private JPanel tablePanel;
 	private JPanel newOrderPanel;
 	private JPanel mainPanel;
 	
 	private CreateNewOrderUI newOrderPane;
 	
 	private JLabel productLabel, quantityLabel, priceLabel;
 	private JTextField productField1, productField2, productField3, productField4;
 	private JTextField quantityField1, quantityField2, quantityField3, quantityField4;
 	private JTextField priceField1,  priceField2,  priceField3,  priceField4;
 	
 	public OrderListPanel() {
 		System.out.println("OrderListPanel created");
 	}
 		
 	public void buildPanel(JPanel panel, final Database database) {
 		
 		this.mainPanel = panel;
 		this.database = database;
 				
 		tableOfOrders = new JTable();// JTABLE code
 		orderTableModel =  new DefaultTableModel();
 		tableOfOrders.setModel(orderTableModel);
 		orderScrollPane = new JScrollPane(tableOfOrders);
 		orderTableModel.setColumnIdentifiers(new String [] {"Order ID", "Delivery Date","Cost","Outstanding"});
 		int row = 0;
 		for(Order order : database.getOrders()){
 			orderTableModel.addRow(new String[] {
 					order.getOrderID(),
 					order.getOrderDeliveryDate(),
 					order.getOrderCost(),
 					String.valueOf(order.isOrderOutstanding())});
 			row++;
 		}
 		tableOfOrders.setVisible(true);
 		
 		tableOfOrders.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e){
 				for(Order order : database.getOrders()){
 					if(order.getOrderID().equals(tableOfOrders.getValueAt(tableOfOrders.getSelectedRow(), 0).toString())){
 						resetTextFields();
 						System.out.println(order.getProducts().size());
 						for(int productInOrder=0; productInOrder < order.getProducts().size(); productInOrder++){
 							if(productInOrder == 0){
 								productField1.setText(order.getProducts().get(productInOrder).getProductName());
 								quantityField1.setText(order.getProducts().get(productInOrder).getProductQuantity());
 								priceField1.setText(order.getProducts().get(productInOrder).getProductPrice());
 							}
 							if(productInOrder == 1){
 								productField2.setText(order.getProducts().get(productInOrder).getProductName());
 								quantityField2.setText(order.getProducts().get(productInOrder).getProductQuantity());
 								priceField2.setText(order.getProducts().get(productInOrder).getProductPrice());
 							}
 							if(productInOrder == 2){
 								productField3.setText(order.getProducts().get(productInOrder).getProductName());
 								quantityField3.setText(order.getProducts().get(productInOrder).getProductQuantity());
 								priceField3.setText(order.getProducts().get(productInOrder).getProductPrice());
 							}
 							if(productInOrder == 3){
 								productField4.setText(order.getProducts().get(productInOrder).getProductName());
 								quantityField4.setText(order.getProducts().get(productInOrder).getProductQuantity());
 								priceField4.setText(order.getProducts().get(productInOrder).getProductPrice());
 							}
 						}
 					}
 				}
 			}
 		});
 				
 		orderListLabel = new JLabel("Order Control", SwingConstants.CENTER);
 		orderListLabel.setOpaque(true);
 		orderListLabel.setBackground(new Color(0,51,102));
 		orderListLabel.setForeground(Color.WHITE);
 		orderListLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
 		
 		newOrderListLabel = new JLabel("New Order", SwingConstants.CENTER);
 		newOrderListLabel.setOpaque(true);
 		newOrderListLabel.setBackground(new Color(0,51,102));
 		newOrderListLabel.setForeground(Color.WHITE);
 		newOrderListLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
 		
 		blankLabel = new JLabel();
 		
 		newOrderButton = new JButton("Create new order");
 		newOrderButton.addActionListener(this);
 		
 		productLabel = new JLabel("Product: ");
 		quantityLabel = new JLabel("Quantity: ");
 		priceLabel = new JLabel("Price: ");
 		
 	    productField1 = new JTextField();
 	    productField1.setEditable(false);
 	    productField1.setBackground(Color.WHITE);
 	    productField2 = new JTextField();
 	    productField2.setEditable(false);
 	    productField2.setBackground(Color.WHITE);
 	    productField3 = new JTextField();
 	    productField3.setEditable(false);
 	    productField3.setBackground(Color.WHITE);
 	    productField4 = new JTextField();
 	    productField4.setEditable(false);
 	    productField4.setBackground(Color.WHITE);
 	    
 		quantityField1 = new JTextField();
 		quantityField1.setEditable(false);
 		quantityField1.setBackground(Color.WHITE);
 		quantityField2 = new JTextField();
 		quantityField2.setEditable(false);
 		quantityField2.setBackground(Color.WHITE);
 		quantityField3 = new JTextField();
 		quantityField3.setEditable(false);
 		quantityField3.setBackground(Color.WHITE);
 		quantityField4 = new JTextField();
 		quantityField4.setEditable(false);
 		quantityField4.setBackground(Color.WHITE);
 		
 		priceField1 = new JTextField();
 		priceField1.setEditable(false);
 		priceField1.setBackground(Color.WHITE);
 		priceField2 = new JTextField();
 		priceField2.setEditable(false);
 		priceField2.setBackground(Color.WHITE);
 		priceField3 = new JTextField();
 		priceField3.setEditable(false);
 		priceField3.setBackground(Color.WHITE);
 		priceField4 = new JTextField();
 		priceField4.setEditable(false);
 		priceField4.setBackground(Color.WHITE);
 		
 		tablePanel = new JPanel();
 		newOrderPanel = new JPanel();
 		
 		newOrderPane = new CreateNewOrderUI();
 		newOrderPane.buildPanel(newOrderPanel, tablePanel, database, tableOfOrders);
 		
 		tablePanel.setLayout(new GridBagLayout());
 		mainPanel.setLayout(new GridBagLayout());
 		
 		createConstraint(tablePanel, orderListLabel,	0, 0, 3, 1, 0, 10, 0, 0, 0, 0, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, newOrderButton, 	0, 1, 1, 1, 0, 0, 2, 20, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE);
 		createConstraint(tablePanel, orderScrollPane, 	0, 2, 3, 1, 0, 0, 2, 20, 2, 20, 1, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		createConstraint(tablePanel, productLabel, 		0, 3, 1, 1, 0, 0, 2, 20, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, quantityLabel, 	1, 3, 1, 1, 0, 0, 2, 2, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, priceLabel, 		2, 3, 1, 1, 0, 0, 2, 2, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		createConstraint(tablePanel, productField1, 	0, 4, 1, 1, 0, 0, 2, 20, 2, 2, 0.3, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, productField2, 	0, 5, 1, 1, 0, 0, 2, 20, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, productField3, 	0, 6, 1, 1, 0, 0, 2, 20, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, productField4, 	0, 7, 1, 1, 0, 0, 2, 20, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		createConstraint(tablePanel, quantityField1, 	1, 4, 1, 1, 0, 0, 2, 2, 2, 2, 0.3, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, quantityField2, 	1, 5, 1, 1, 0, 0, 2, 2, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, quantityField3, 	1, 6, 1, 1, 0, 0, 2, 2, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, quantityField4, 	1, 7, 1, 1, 0, 0, 2, 2, 2, 2, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		createConstraint(tablePanel, priceField1, 		2, 4, 1, 1, 0, 0, 2, 2, 2, 20, 0.3, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, priceField2, 		2, 5, 1, 1, 0, 0, 2, 2, 2, 20, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, priceField3, 		2, 6, 1, 1, 0, 0, 2, 2, 2, 20, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(tablePanel, priceField4, 		2, 7, 1, 1, 0, 0, 2, 2, 2, 20, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		createConstraint(mainPanel, tablePanel,			0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		createConstraint(mainPanel, newOrderPanel,		0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH);
 		
 		newOrderPanel.setVisible(false);
 	}
 	
 	//Create GridBagLayout constraints and add component to a panel using these constraints
 	private void createConstraint(JPanel panel, JComponent component, int gridx, int gridy, int width, int height, int ipadx, int ipady,
 			int top, int left, int bottom, int right, double weightx, double weighty, int anchor, int fill){
 			GridBagConstraints constraints = new GridBagConstraints();
 			constraints.gridx = gridx;
 			constraints.gridy = gridy;
 			constraints.gridwidth = width;
 			constraints.gridheight = height;
 			constraints.ipadx = ipadx;
 			constraints.ipady = ipady;
 			constraints.weightx = weightx;
 			constraints.weighty = weighty;
 			constraints.insets = new Insets(top, left, bottom, right);
 			constraints.anchor = anchor;
 			constraints.fill = fill;
 			panel.add(component, constraints);
 	}
 	
	public void resetTextFields() { //clear all textfields
 		productField1.setText("");
 		quantityField1.setText("");
 		priceField1.setText("");
 		productField2.setText("");
 		quantityField2.setText("");
 		priceField2.setText("");
 		productField3.setText("");
 		quantityField3.setText("");
 		priceField3.setText("");
 		productField4.setText("");
 		quantityField4.setText("");
 		priceField4.setText("");
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		
 		System.out.println(e.paramString());
 		
 		if(e.getActionCommand().equals("Create new order")) {
 			
 			//go to create order view
 			tablePanel.setVisible(false);
 			newOrderPanel.setVisible(true);
 			System.out.println("order panel invisible");
 		}
 		
 	}
 }
