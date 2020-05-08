 //GREAT JORB! (this wasn't including in commit)
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import javax.swing.JComboBox;
 //import net.miginfocom.swing.MigLayout;
 import javax.swing.JFormattedTextField;
 
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.CardLayout;
 import javax.swing.JButton;
 import javax.swing.JTextPane;
 import javax.swing.JSeparator;
 import javax.swing.JPasswordField;
 import javax.swing.JTabbedPane;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextField;
 //import net.miginfocom.swing.MigLayout;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.BevelBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JTextArea;
 
 public class MainFrame extends JFrame {
 	private AMSOracleConnection con = null;
 	private JPanel contentPane;
 	private JPasswordField passwordField;
 	private JTable table;
 	private JTextField inpq;
 	private JTextField instorecardexpd;
 	private JTextField returnReceiptId;
 	private JTextField returnUPC;
 	static JTable table_1;
 	protected JTable table_2;
 	private JTextField custUName;
 	private JTextField custPW;
 	private JTextField custRegId;
 	private JTextField custRegPW;
 	private JTextField custRegName;
 	private JTextField custRegAddr;
 	private JTextField custPNum;
 	private JTextField inpupc;
 	private JTextField ccno;
 	private String[] opColumnNames = {"UPC","Title", "Category","Leading Singer", "Price"};
 	private JTextField opCategory;
 	private JTextField optitle;
 	private JTextField opqty;
 	private JTextField opls;
 	private JTextField opcardno;
 	private JTextField opcardexpd;
 	private static ArrayList<String> savedInstoreItems;
 	private static JTextArea purchaseItems;
 	private static String cardNumber = null;
 	private static String cardExpDate = null;
 	private static Integer numberOfItems = 0;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					
 					MainFrame frame = new MainFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainFrame() {
 		final ArrayList<String> stockUpdate  = new ArrayList<String>();

 		savedInstoreItems = new ArrayList<String>();
 		con = AMSOracleConnection.getInstance();
 		setTitle("AMS Database");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
 		setContentPane(contentPane);
 		
 		JPanel login = new JPanel();
 		login.setBorder(new EmptyBorder(5, 5, 5, 5));
 		GridBagLayout gbl_login = new GridBagLayout();
 		gbl_login.columnWidths = new int[]{0, 414, 0};
 		gbl_login.rowHeights = new int[]{24, 24, 24, 24, 24, 24, 24, 0, 24, 24, 0};
 		gbl_login.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gbl_login.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		login.setLayout(gbl_login);
 		
 		JLabel lblNewLabel = new JLabel("Username:");
 		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel.gridx = 1;
 		gbc_lblNewLabel.gridy = 1;
 		login.add(lblNewLabel, gbc_lblNewLabel);
 		
 		final JTextField userName = new JTextField();
 		GridBagConstraints gbc_userName = new GridBagConstraints();
 		gbc_userName.fill = GridBagConstraints.BOTH;
 		gbc_userName.insets = new Insets(0, 0, 5, 0);
 		gbc_userName.gridx = 1;
 		gbc_userName.gridy = 2;
 		login.add(userName, gbc_userName);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblPassword.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
 		gbc_lblPassword.fill = GridBagConstraints.BOTH;
 		gbc_lblPassword.insets = new Insets(0, 0, 5, 0);
 		gbc_lblPassword.gridx = 1;
 		gbc_lblPassword.gridy = 3;
 		login.add(lblPassword, gbc_lblPassword);
 		
 		passwordField = new JPasswordField();
 		GridBagConstraints gbc_passwordField = new GridBagConstraints();
 		gbc_passwordField.fill = GridBagConstraints.BOTH;
 		gbc_passwordField.insets = new Insets(0, 0, 5, 0);
 		gbc_passwordField.gridx = 1;
 		gbc_passwordField.gridy = 4;
 		login.add(passwordField, gbc_passwordField);
 		
 		JLabel lblNewLabel_1 = new JLabel("Specify Your Access Level:");
 		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
 		gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel_1.gridx = 1;
 		gbc_lblNewLabel_1.gridy = 5;
 		login.add(lblNewLabel_1, gbc_lblNewLabel_1);
 		
 		final JComboBox AccessLevel = new JComboBox();
 		AccessLevel.setModel(new DefaultComboBoxModel(new String[] {"Customer", "Clerk", "Manager"}));
 		GridBagConstraints gbc_AccessLevel = new GridBagConstraints();
 		gbc_AccessLevel.fill = GridBagConstraints.BOTH;
 		gbc_AccessLevel.insets = new Insets(0, 0, 5, 0);
 		gbc_AccessLevel.gridx = 1;
 		gbc_AccessLevel.gridy = 6;
 		login.add(AccessLevel, gbc_AccessLevel);
 		
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (con.connect(userName.getText(), 
 					     String.valueOf(passwordField.getPassword()))){
 					String access = (String) AccessLevel.getSelectedItem();
 					switchToNextCard(access);
 					// select next card based on accesslevel
 					
 					
 				}
 			}
 		});
 		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
 		gbc_btnSubmit.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSubmit.gridx = 1;
 		gbc_btnSubmit.gridy = 7;
 		login.add(btnSubmit, gbc_btnSubmit);
 		
 		JSeparator separator_3 = new JSeparator();
 		GridBagConstraints gbc_separator_3 = new GridBagConstraints();
 		gbc_separator_3.insets = new Insets(0, 0, 0, 5);
 		gbc_separator_3.gridx = 0;
 		gbc_separator_3.gridy = 9;
 		login.add(separator_3, gbc_separator_3);
 		contentPane.setLayout(new CardLayout(0, 0));
 		contentPane.add(login, "name_1441150054636723");
 		
 		JTabbedPane clerkOperations = new JTabbedPane(JTabbedPane.TOP);
 		contentPane.add(clerkOperations, "name_1442560847488307");
 		
 		JPanel processPurchase = new JPanel();
 		processPurchase.setBorder(new EmptyBorder(10, 10, 10, 10));
 		clerkOperations.addTab("Purchase", null, processPurchase, null);
 		GridBagLayout gbl_processPurchase = new GridBagLayout();
 		gbl_processPurchase.columnWidths = new int[]{32, 82, 0, 0, 105, 0, 0};
 		gbl_processPurchase.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_processPurchase.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_processPurchase.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		processPurchase.setLayout(gbl_processPurchase);
 		
 		JLabel label = new JLabel("UPC:");
 		GridBagConstraints gbc_label = new GridBagConstraints();
 		gbc_label.anchor = GridBagConstraints.EAST;
 		gbc_label.insets = new Insets(0, 0, 5, 5);
 		gbc_label.gridx = 0;
 		gbc_label.gridy = 0;
 		processPurchase.add(label, gbc_label);
 		
 		inpupc = new JTextField();
 		GridBagConstraints gbc_inpupc = new GridBagConstraints();
 		gbc_inpupc.gridwidth = 2;
 		gbc_inpupc.insets = new Insets(0, 0, 5, 5);
 		gbc_inpupc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_inpupc.gridx = 1;
 		gbc_inpupc.gridy = 0;
 		processPurchase.add(inpupc, gbc_inpupc);
 		
 		JButton addpurchitem = new JButton("Add Item");
 		addpurchitem.addActionListener(new ActionListener() {
 			//TODO: Instore Purchase Additem Button
 			public void actionPerformed(ActionEvent arg0) {
 				String upc = inpupc.getText().trim();
 				Integer q = Integer.parseInt(inpq.getText().trim());
 				PurchaseOperations p = new PurchaseOperations();
 				if(p.isInStock(upc,q)){
 					System.out.println("Item is in stock!");
					stockUpdate.add(upc);
					stockUpdate.add(q.toString());
 					numberOfItems++;
 					//ensure items are added to table
 				}
 				
 			}
 		});
 		GridBagConstraints gbc_addpurchitem = new GridBagConstraints();
 		gbc_addpurchitem.insets = new Insets(0, 0, 5, 5);
 		gbc_addpurchitem.gridx = 4;
 		gbc_addpurchitem.gridy = 0;
 		processPurchase.add(addpurchitem, gbc_addpurchitem);
 		
 		JLabel lblQuantity_5 = new JLabel("Quantity:");
 		GridBagConstraints gbc_lblQuantity_5 = new GridBagConstraints();
 		gbc_lblQuantity_5.anchor = GridBagConstraints.EAST;
 		gbc_lblQuantity_5.insets = new Insets(0, 0, 5, 5);
 		gbc_lblQuantity_5.gridx = 0;
 		gbc_lblQuantity_5.gridy = 1;
 		processPurchase.add(lblQuantity_5, gbc_lblQuantity_5);
 		
 		inpq = new JTextField();
 		GridBagConstraints gbc_inpq = new GridBagConstraints();
 		gbc_inpq.gridwidth = 2;
 		gbc_inpq.insets = new Insets(0, 0, 5, 5);
 		gbc_inpq.fill = GridBagConstraints.HORIZONTAL;
 		gbc_inpq.gridx = 1;
 		gbc_inpq.gridy = 1;
 		processPurchase.add(inpq, gbc_inpq);
 		inpq.setColumns(10);
 		
 		JScrollPane scrollPane_4 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
 		gbc_scrollPane_4.gridheight = 2;
 		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_4.gridwidth = 6;
 		gbc_scrollPane_4.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_4.gridx = 0;
 		gbc_scrollPane_4.gridy = 2;
 		processPurchase.add(scrollPane_4, gbc_scrollPane_4);
 		
 		this.purchaseItems = new JTextArea();
 		scrollPane_4.setViewportView(purchaseItems);
 		purchaseItems.append("                    UPC               Price               Quantity\n=============================================================\n");
 		
 		JLabel lblCreditCardNumber = new JLabel("Credit Card Number:");
 		GridBagConstraints gbc_lblCreditCardNumber = new GridBagConstraints();
 		gbc_lblCreditCardNumber.gridwidth = 3;
 		gbc_lblCreditCardNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblCreditCardNumber.fill = GridBagConstraints.VERTICAL;
 		gbc_lblCreditCardNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCreditCardNumber.gridx = 1;
 		gbc_lblCreditCardNumber.gridy = 4;
 		processPurchase.add(lblCreditCardNumber, gbc_lblCreditCardNumber);
 		
 		ccno = new JTextField();
 		ccno.setColumns(10);
 		GridBagConstraints gbc_ccno = new GridBagConstraints();
 		gbc_ccno.insets = new Insets(0, 0, 5, 5);
 		gbc_ccno.fill = GridBagConstraints.HORIZONTAL;
 		gbc_ccno.gridx = 4;
 		gbc_ccno.gridy = 4;
 		processPurchase.add(ccno, gbc_ccno);
 		
 		JLabel lblCardExpiryDate = new JLabel("Card Expiry Date:");
 		GridBagConstraints gbc_lblCardExpiryDate = new GridBagConstraints();
 		gbc_lblCardExpiryDate.gridwidth = 3;
 		gbc_lblCardExpiryDate.anchor = GridBagConstraints.EAST;
 		gbc_lblCardExpiryDate.fill = GridBagConstraints.VERTICAL;
 		gbc_lblCardExpiryDate.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCardExpiryDate.gridx = 1;
 		gbc_lblCardExpiryDate.gridy = 5;
 		processPurchase.add(lblCardExpiryDate, gbc_lblCardExpiryDate);
 		
 		instorecardexpd = new JTextField();
 		instorecardexpd.setColumns(10);
 		GridBagConstraints gbc_instorecardexpd = new GridBagConstraints();
 		gbc_instorecardexpd.insets = new Insets(0, 0, 0, 5);
 		gbc_instorecardexpd.fill = GridBagConstraints.HORIZONTAL;
 		gbc_instorecardexpd.gridx = 4;
 		gbc_instorecardexpd.gridy = 5;
 		processPurchase.add(instorecardexpd, gbc_instorecardexpd);
 		
 		//TODO: INSTORE PURCHASE COMPLETE BUTTON
 		JButton btnCompletePurchase = new JButton("Complete Purchase");
 		btnCompletePurchase.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				PurchaseOperations p = new PurchaseOperations();
 				ItemOperations i = new ItemOperations();
 			
 				String expd = instorecardexpd.getText().trim();
 				String cno = ccno.getText().trim();
 				
 				if (p.completePurchase(cno, expd)){
 					System.out.println("Items Purchased!");
 				clearPurchaseList();
 				}
				if (i.reduceStockForPurchase(stockUpdate)){
 					System.out.println("Stock has been adjusted to reflect purchase");
 				}
 			}
 		});
 		GridBagConstraints gbc_btnCompletePurchase = new GridBagConstraints();
 		gbc_btnCompletePurchase.gridx = 5;
 		gbc_btnCompletePurchase.gridy = 5;
 		processPurchase.add(btnCompletePurchase, gbc_btnCompletePurchase);
 		
 		JPanel processReturn = new JPanel();
 		clerkOperations.addTab("Return", null, processReturn, null);
 		GridBagLayout gbl_processReturn = new GridBagLayout();
 		gbl_processReturn.columnWidths = new int[]{0, 0, 0, 0, 0};
 		gbl_processReturn.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_processReturn.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_processReturn.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		processReturn.setLayout(gbl_processReturn);
 		
 		JLabel lblReceiptid = new JLabel("ReceiptID:");
 		GridBagConstraints gbc_lblReceiptid = new GridBagConstraints();
 		gbc_lblReceiptid.insets = new Insets(0, 0, 5, 5);
 		gbc_lblReceiptid.anchor = GridBagConstraints.EAST;
 		gbc_lblReceiptid.gridx = 1;
 		gbc_lblReceiptid.gridy = 1;
 		processReturn.add(lblReceiptid, gbc_lblReceiptid);
 		
 		returnReceiptId = new JTextField();
 		GridBagConstraints gbc_returnReceiptId = new GridBagConstraints();
 		gbc_returnReceiptId.insets = new Insets(0, 0, 5, 5);
 		gbc_returnReceiptId.fill = GridBagConstraints.HORIZONTAL;
 		gbc_returnReceiptId.gridx = 2;
 		gbc_returnReceiptId.gridy = 1;
 		processReturn.add(returnReceiptId, gbc_returnReceiptId);
 		returnReceiptId.setColumns(10);
 		
 		JLabel lblUpc_2 = new JLabel("UPC:");
 		GridBagConstraints gbc_lblUpc_2 = new GridBagConstraints();
 		gbc_lblUpc_2.anchor = GridBagConstraints.EAST;
 		gbc_lblUpc_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUpc_2.gridx = 1;
 		gbc_lblUpc_2.gridy = 2;
 		processReturn.add(lblUpc_2, gbc_lblUpc_2);
 		
 		returnUPC = new JTextField();
 		returnUPC.setColumns(10);
 		GridBagConstraints gbc_returnUPC = new GridBagConstraints();
 		gbc_returnUPC.insets = new Insets(0, 0, 5, 5);
 		gbc_returnUPC.fill = GridBagConstraints.HORIZONTAL;
 		gbc_returnUPC.gridx = 2;
 		gbc_returnUPC.gridy = 2;
 		processReturn.add(returnUPC, gbc_returnUPC);
 		
 		JButton btnProcessReturn = new JButton("Process Return");
 		btnProcessReturn.addActionListener(new ActionListener() {
 			//TODO: RETURN BUTTON
 			public void actionPerformed(ActionEvent arg0) {
 				Integer receiptid = Integer.parseInt(returnReceiptId.getText().trim());
 				String retupc = returnUPC.getText().trim();
 				ReturnOperations r = new ReturnOperations();
 				ReturnItemOperations ri = new ReturnItemOperations();
 				Date rdate = new Date();
 				java.sql.Date sqlDate = new java.sql.Date(rdate.getTime());
 //				int paymenttype = 0;
 //				int qty = 0;
 //				if(r.validateReturn(retupc,receiptid,paymenttype,qty)){
 //					switch(paymenttype){
 //					case (0):System.out.println("Cash Return");
 //					case (1):System.out.println("Credit Return");
 //					}
 //					
 //					if(r.insert(receiptid,sqlDate)){
 //						if(ri.insert(retupc, paymenttype)){
 ////							System.out.println("Return Successful");
 //						}
 //					}
 				}
 			});
 	
 		GridBagConstraints gbc_btnProcessReturn = new GridBagConstraints();
 		gbc_btnProcessReturn.insets = new Insets(0, 0, 5, 0);
 		gbc_btnProcessReturn.gridx = 3;
 		gbc_btnProcessReturn.gridy = 2;
 		processReturn.add(btnProcessReturn, gbc_btnProcessReturn);
 		
 		JTabbedPane managerOperations = new JTabbedPane(JTabbedPane.TOP);
 		contentPane.add(managerOperations, "name_1449473464875310");
 		
 		JPanel addItem = new JPanel();
 		addItem.setBorder(new EmptyBorder(15, 15, 15, 15));
 		managerOperations.addTab("Add Item to Store", null, addItem, null);
 		GridBagLayout gbl_addItem = new GridBagLayout();
 		gbl_addItem.columnWidths = new int[] {0, 10};
 		gbl_addItem.rowHeights = new int[] {0, 10, 0, 0, 0};
 		gbl_addItem.columnWeights = new double[]{0.0, 1.0};
 		gbl_addItem.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, 0.0};
 		addItem.setLayout(gbl_addItem);
 		
 		JLabel lblUpc_1 = new JLabel("UPC:");
 		GridBagConstraints gbc_lblUpc_1 = new GridBagConstraints();
 		gbc_lblUpc_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUpc_1.gridx = 0;
 		gbc_lblUpc_1.gridy = 0;
 		addItem.add(lblUpc_1, gbc_lblUpc_1);
 		
 		final JTextField additemupc = new JTextField();
 		GridBagConstraints gbc_additemupc = new GridBagConstraints();
 		gbc_additemupc.insets = new Insets(0, 0, 5, 0);
 		gbc_additemupc.fill = GridBagConstraints.BOTH;
 		gbc_additemupc.gridx = 1;
 		gbc_additemupc.gridy = 0;
 		addItem.add(additemupc, gbc_additemupc);
 		
 		JLabel lblQuantity_1 = new JLabel("Quantity:");
 		GridBagConstraints gbc_lblQuantity_1 = new GridBagConstraints();
 		gbc_lblQuantity_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblQuantity_1.gridx = 0;
 		gbc_lblQuantity_1.gridy = 1;
 		addItem.add(lblQuantity_1, gbc_lblQuantity_1);
 		
 		final JTextField additemqty = new JTextField();
 		GridBagConstraints gbc_additemqty = new GridBagConstraints();
 		gbc_additemqty.insets = new Insets(0, 0, 5, 0);
 		gbc_additemqty.fill = GridBagConstraints.BOTH;
 		gbc_additemqty.gridx = 1;
 		gbc_additemqty.gridy = 1;
 		addItem.add(additemqty, gbc_additemqty);
 		
 		JLabel lblPrice = new JLabel("Price (Optional):");
 		GridBagConstraints gbc_lblPrice = new GridBagConstraints();
 		gbc_lblPrice.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPrice.gridx = 0;
 		gbc_lblPrice.gridy = 2;
 		addItem.add(lblPrice, gbc_lblPrice);
 		
 		final JTextField additemprice = new JTextField();
 		GridBagConstraints gbc_additemprice = new GridBagConstraints();
 		gbc_additemprice.insets = new Insets(0, 0, 5, 0);
 		gbc_additemprice.fill = GridBagConstraints.BOTH;
 		gbc_additemprice.gridx = 1;
 		gbc_additemprice.gridy = 2;
 		addItem.add(additemprice, gbc_additemprice);
 		
 		JButton btnAddItem = new JButton("Add Item");
 		//TODO: Manager AddItem button
 		btnAddItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ItemOperations i = new ItemOperations();
 				String upcString = additemupc.getText();
 				Integer qtyString = Integer.parseInt(additemqty.getText());
 				Double priceDouble = Double.parseDouble(additemprice.getText());
 				i.managerAddItem(upcString, qtyString, priceDouble);
 			}
 		});
 		GridBagConstraints gbc_btnAddItem = new GridBagConstraints();
 		gbc_btnAddItem.insets = new Insets(0, 0, 5, 0);
 		gbc_btnAddItem.gridx = 1;
 		gbc_btnAddItem.gridy = 3;
 		addItem.add(btnAddItem, gbc_btnAddItem);
 		
 		JSeparator separator_2 = new JSeparator();
 		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
 		gbc_separator_2.gridx = 1;
 		gbc_separator_2.gridy = 4;
 		addItem.add(separator_2, gbc_separator_2);
 		
 		JPanel processDelivery = new JPanel();
 		processDelivery.setBorder(new EmptyBorder(15, 15, 15, 15));
 		managerOperations.addTab("Process Delivery", null, processDelivery, null);
 
 		GridBagLayout gbl_processDelivery = new GridBagLayout();
 		gbl_processDelivery.columnWidths = new int[]{1, 0, 0, 0, 0, 0};
 		gbl_processDelivery.rowHeights = new int[]{1, 0, 0, 0, 0};
 		gbl_processDelivery.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_processDelivery.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		processDelivery.setLayout(gbl_processDelivery);
 		
 		JLabel lblNewLabel_2 = new JLabel("Enter Order ReceiptID:");
 		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
 		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel_2.gridx = 0;
 		gbc_lblNewLabel_2.gridy = 0;
 		processDelivery.add(lblNewLabel_2, gbc_lblNewLabel_2);
 		
 		final JTextField delivReceiptId = new JTextField();
 		GridBagConstraints gbc_delivReceiptId = new GridBagConstraints();
 		gbc_delivReceiptId.insets = new Insets(0, 0, 5, 5);
 		gbc_delivReceiptId.fill = GridBagConstraints.BOTH;
 		gbc_delivReceiptId.gridx = 1;
 		gbc_delivReceiptId.gridy = 0;
 		processDelivery.add(delivReceiptId, gbc_delivReceiptId);
 		
 		JLabel lblEnterDeliveryDate = new JLabel("Enter Delivery Date:");
 		GridBagConstraints gbc_lblEnterDeliveryDate = new GridBagConstraints();
 		gbc_lblEnterDeliveryDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblEnterDeliveryDate.fill = GridBagConstraints.BOTH;
 		gbc_lblEnterDeliveryDate.gridx = 0;
 		gbc_lblEnterDeliveryDate.gridy = 1;
 		processDelivery.add(lblEnterDeliveryDate, gbc_lblEnterDeliveryDate);
 		
 		final JTextField delivDate = new JTextField();
 		GridBagConstraints gbc_delivDate = new GridBagConstraints();
 		gbc_delivDate.insets = new Insets(0, 0, 5, 5);
 		gbc_delivDate.fill = GridBagConstraints.BOTH;
 		gbc_delivDate.gridx = 1;
 		gbc_delivDate.gridy = 1;
 		processDelivery.add(delivDate, gbc_delivDate);
 		
 		JButton setdelivdate = new JButton("Set Delivery Date");
 		setdelivdate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				PurchaseOperations p = new PurchaseOperations();
 				String stringdate = delivDate.getText().trim();
 				String ridstring = delivReceiptId.getText();
 				int ridint = Integer.parseInt(ridstring);
 				p.updateDeliveryDate(ridint, stringdate);
 			}
 		});
 		
 		JLabel lblUseFormatDdmmyy = new JLabel("Use Format dd/MM/yy");
 		GridBagConstraints gbc_lblUseFormatDdmmyy = new GridBagConstraints();
 		gbc_lblUseFormatDdmmyy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUseFormatDdmmyy.gridx = 2;
 		gbc_lblUseFormatDdmmyy.gridy = 1;
 		processDelivery.add(lblUseFormatDdmmyy, gbc_lblUseFormatDdmmyy);
 		GridBagConstraints gbc_setdelivdate = new GridBagConstraints();
 		gbc_setdelivdate.insets = new Insets(0, 0, 0, 5);
 		gbc_setdelivdate.fill = GridBagConstraints.BOTH;
 		gbc_setdelivdate.gridx = 1;
 		gbc_setdelivdate.gridy = 3;
 		processDelivery.add(setdelivdate, gbc_setdelivdate);
 		
 		JPanel dailySalesReport = new JPanel();
 		dailySalesReport.setBorder(new EmptyBorder(15, 15, 15, 15));
 		managerOperations.addTab("Daily Sales Report", null, dailySalesReport, null);
 		GridBagLayout gbl_dailySalesReport = new GridBagLayout();
 		gbl_dailySalesReport.columnWidths = new int[]{0, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_dailySalesReport.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_dailySalesReport.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
 		gbl_dailySalesReport.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 		dailySalesReport.setLayout(gbl_dailySalesReport);
 		
 		JLabel lblEnterDate = new JLabel("Enter Date:");
 		GridBagConstraints gbc_lblEnterDate = new GridBagConstraints();
 		gbc_lblEnterDate.gridwidth = 2;
 		gbc_lblEnterDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblEnterDate.anchor = GridBagConstraints.EAST;
 		gbc_lblEnterDate.gridx = 0;
 		gbc_lblEnterDate.gridy = 0;
 		dailySalesReport.add(lblEnterDate, gbc_lblEnterDate);
 		
 		JFormattedTextField dailysalesdate = new JFormattedTextField();
 		GridBagConstraints gbc_dailysalesdate = new GridBagConstraints();
 		gbc_dailysalesdate.gridwidth = 2;
 		gbc_dailysalesdate.insets = new Insets(0, 0, 5, 5);
 		gbc_dailysalesdate.fill = GridBagConstraints.HORIZONTAL;
 		gbc_dailysalesdate.gridx = 2;
 		gbc_dailysalesdate.gridy = 0;
 		dailySalesReport.add(dailysalesdate, gbc_dailysalesdate);
 		
 		JButton btnRetrieveSalesData = new JButton("Retrieve Sales Data");
 		GridBagConstraints gbc_btnRetrieveSalesData = new GridBagConstraints();
 		gbc_btnRetrieveSalesData.insets = new Insets(0, 0, 5, 5);
 		gbc_btnRetrieveSalesData.gridx = 2;
 		gbc_btnRetrieveSalesData.gridy = 1;
 		dailySalesReport.add(btnRetrieveSalesData, gbc_btnRetrieveSalesData);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridheight = 2;
 		gbc_scrollPane.gridwidth = 12;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 2;
 		dailySalesReport.add(scrollPane, gbc_scrollPane);
 		
 		table = new JTable();
 		scrollPane.setRowHeaderView(table);
 		
 		JLabel lblTotal = new JLabel("Total");
 		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
 		gbc_lblTotal.insets = new Insets(0, 0, 0, 5);
 		gbc_lblTotal.gridx = 1;
 		gbc_lblTotal.gridy = 5;
 		dailySalesReport.add(lblTotal, gbc_lblTotal);
 		
 		JLabel lblQuantity_2 = new JLabel("Quantity");
 		GridBagConstraints gbc_lblQuantity_2 = new GridBagConstraints();
 		gbc_lblQuantity_2.insets = new Insets(0, 0, 0, 5);
 		gbc_lblQuantity_2.gridx = 3;
 		gbc_lblQuantity_2.gridy = 5;
 		dailySalesReport.add(lblQuantity_2, gbc_lblQuantity_2);
 		
 		JTextPane textArea = new JTextPane();
 		GridBagConstraints gbc_textArea = new GridBagConstraints();
 		gbc_textArea.gridwidth = 2;
 		gbc_textArea.insets = new Insets(0, 0, 0, 5);
 		gbc_textArea.fill = GridBagConstraints.BOTH;
 		gbc_textArea.gridx = 4;
 		gbc_textArea.gridy = 5;
 		dailySalesReport.add(textArea, gbc_textArea);
 		
 		JLabel lblSales = new JLabel("Sales");
 		GridBagConstraints gbc_lblSales = new GridBagConstraints();
 		gbc_lblSales.gridwidth = 2;
 		gbc_lblSales.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSales.gridx = 6;
 		gbc_lblSales.gridy = 5;
 		dailySalesReport.add(lblSales, gbc_lblSales);
 		
 		JTextPane textPane_6 = new JTextPane();
 		GridBagConstraints gbc_textPane_6 = new GridBagConstraints();
 		gbc_textPane_6.gridwidth = 2;
 		gbc_textPane_6.insets = new Insets(0, 0, 0, 5);
 		gbc_textPane_6.fill = GridBagConstraints.BOTH;
 		gbc_textPane_6.gridx = 8;
 		gbc_textPane_6.gridy = 5;
 		dailySalesReport.add(textPane_6, gbc_textPane_6);
 		
 		JPanel topSellingItems = new JPanel();
 		managerOperations.addTab("Top Selling Items", null, topSellingItems, null);
 		
 		final JPanel customerOperations = new JPanel();
 		contentPane.add(customerOperations, "name_1452058092586835");
 		customerOperations.setLayout(new CardLayout(0, 0));
 		
 		JPanel customerLogin = new JPanel();
 		customerLogin.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		customerOperations.add(customerLogin, "name_1546903163253674");
 		GridBagLayout gbl_customerLogin = new GridBagLayout();
 		gbl_customerLogin.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_customerLogin.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_customerLogin.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_customerLogin.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		customerLogin.setLayout(gbl_customerLogin);
 		
 		JLabel lblCustomerLogin = new JLabel("Customer Login");
 		lblCustomerLogin.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblCustomerLogin = new GridBagConstraints();
 		gbc_lblCustomerLogin.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCustomerLogin.gridx = 1;
 		gbc_lblCustomerLogin.gridy = 1;
 		customerLogin.add(lblCustomerLogin, gbc_lblCustomerLogin);
 		
 		JLabel lblUsername = new JLabel("Username:");
 		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
 		gbc_lblUsername.anchor = GridBagConstraints.EAST;
 		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUsername.gridx = 2;
 		gbc_lblUsername.gridy = 4;
 		customerLogin.add(lblUsername, gbc_lblUsername);
 		
 		custUName = new JTextField();
 		GridBagConstraints gbc_custUName = new GridBagConstraints();
 		gbc_custUName.gridwidth = 2;
 		gbc_custUName.insets = new Insets(0, 0, 5, 5);
 		gbc_custUName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custUName.gridx = 3;
 		gbc_custUName.gridy = 4;
 		customerLogin.add(custUName, gbc_custUName);
 		custUName.setColumns(10);
 		
 		JLabel lblPassword_1 = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword_1 = new GridBagConstraints();
 		gbc_lblPassword_1.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword_1.gridx = 2;
 		gbc_lblPassword_1.gridy = 5;
 		customerLogin.add(lblPassword_1, gbc_lblPassword_1);
 		
 		custPW = new JTextField();
 		GridBagConstraints gbc_custPW = new GridBagConstraints();
 		gbc_custPW.gridwidth = 2;
 		gbc_custPW.insets = new Insets(0, 0, 5, 5);
 		gbc_custPW.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custPW.gridx = 3;
 		gbc_custPW.gridy = 5;
 		customerLogin.add(custPW, gbc_custPW);
 		custPW.setColumns(10);
 		
 		JButton btnSubmit_1 = new JButton("Submit");
 		
 			//TODO: submit operations to login customer
 			btnSubmit_1.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					System.out.println("Submit was pressed")	;	
 					CustomerOperations c = new CustomerOperations();
 					if (c.login(custUName.getText().trim(), custPW.getText().trim())){
 						CardLayout cl = (CardLayout) customerOperations.getLayout();
 						 cl.show(customerOperations, "name_1452390541607518");
 					}
 				}
 			});
 			GridBagConstraints gbc_btnSubmit_1 = new GridBagConstraints();
 			gbc_btnSubmit_1.insets = new Insets(0, 0, 5, 5);
 			gbc_btnSubmit_1.gridx = 3;
 			gbc_btnSubmit_1.gridy = 6;
 			customerLogin.add(btnSubmit_1, gbc_btnSubmit_1);
 		
 		JLabel lblNotRegistered = new JLabel("Not Registered?");
 		GridBagConstraints gbc_lblNotRegistered = new GridBagConstraints();
 		gbc_lblNotRegistered.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNotRegistered.gridx = 7;
 		gbc_lblNotRegistered.gridy = 7;
 		customerLogin.add(lblNotRegistered, gbc_lblNotRegistered);
 		
 		JButton btnRegisterNow = new JButton("Register Now");
 		btnRegisterNow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				CardLayout cl = (CardLayout) customerOperations.getLayout();
 				cl.show(customerOperations,"name_1452387567243092");
 			}
 		});
 		GridBagConstraints gbc_btnRegisterNow = new GridBagConstraints();
 		gbc_btnRegisterNow.insets = new Insets(0, 0, 5, 0);
 		gbc_btnRegisterNow.gridx = 7;
 		gbc_btnRegisterNow.gridy = 8;
 		customerLogin.add(btnRegisterNow, gbc_btnRegisterNow);
 		
 		JPanel registerCustomer = new JPanel();
 		registerCustomer.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
 		customerOperations.add(registerCustomer, "name_1452387567243092");
 		GridBagLayout gbl_registerCustomer = new GridBagLayout();
 		gbl_registerCustomer.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_registerCustomer.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_registerCustomer.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_registerCustomer.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		registerCustomer.setLayout(gbl_registerCustomer);
 		
 		JLabel lblCustomerRegistration = new JLabel("Customer Registration");
 		lblCustomerRegistration.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblCustomerRegistration = new GridBagConstraints();
 		gbc_lblCustomerRegistration.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCustomerRegistration.gridx = 2;
 		gbc_lblCustomerRegistration.gridy = 0;
 		registerCustomer.add(lblCustomerRegistration, gbc_lblCustomerRegistration);
 		
 		JLabel lblNewLabel_6 = new JLabel("Customer ID:");
 		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
 		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_6.gridx = 1;
 		gbc_lblNewLabel_6.gridy = 2;
 		registerCustomer.add(lblNewLabel_6, gbc_lblNewLabel_6);
 		
 		custRegId = new JTextField();
 		GridBagConstraints gbc_custRegId = new GridBagConstraints();
 		gbc_custRegId.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegId.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegId.gridx = 2;
 		gbc_custRegId.gridy = 2;
 		registerCustomer.add(custRegId, gbc_custRegId);
 		custRegId.setColumns(10);
 		
 		JLabel lblPassword_2 = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword_2 = new GridBagConstraints();
 		gbc_lblPassword_2.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword_2.gridx = 1;
 		gbc_lblPassword_2.gridy = 3;
 		registerCustomer.add(lblPassword_2, gbc_lblPassword_2);
 		
 		custRegPW = new JTextField();
 		GridBagConstraints gbc_custRegPW = new GridBagConstraints();
 		gbc_custRegPW.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegPW.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegPW.gridx = 2;
 		gbc_custRegPW.gridy = 3;
 		registerCustomer.add(custRegPW, gbc_custRegPW);
 		custRegPW.setColumns(10);
 		
 		JLabel lblNewLabel_4 = new JLabel("Name:");
 		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
 		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_4.gridx = 1;
 		gbc_lblNewLabel_4.gridy = 4;
 		registerCustomer.add(lblNewLabel_4, gbc_lblNewLabel_4);
 		
 		custRegName = new JTextField();
 		GridBagConstraints gbc_custRegName = new GridBagConstraints();
 		gbc_custRegName.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegName.gridx = 2;
 		gbc_custRegName.gridy = 4;
 		registerCustomer.add(custRegName, gbc_custRegName);
 		custRegName.setColumns(10);
 		
 		JLabel lblAddress = new JLabel("Address:");
 		GridBagConstraints gbc_lblAddress = new GridBagConstraints();
 		gbc_lblAddress.anchor = GridBagConstraints.EAST;
 		gbc_lblAddress.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAddress.gridx = 1;
 		gbc_lblAddress.gridy = 5;
 		registerCustomer.add(lblAddress, gbc_lblAddress);
 		
 		custRegAddr = new JTextField();
 		GridBagConstraints gbc_custRegAddr = new GridBagConstraints();
 		gbc_custRegAddr.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegAddr.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegAddr.gridx = 2;
 		gbc_custRegAddr.gridy = 5;
 		registerCustomer.add(custRegAddr, gbc_custRegAddr);
 		custRegAddr.setColumns(10);
 		
 		JLabel lblNewLabel_5 = new JLabel("Phone Number:");
 		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
 		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_5.gridx = 1;
 		gbc_lblNewLabel_5.gridy = 6;
 		registerCustomer.add(lblNewLabel_5, gbc_lblNewLabel_5);
 		
 		custPNum = new JTextField();
 		GridBagConstraints gbc_custPNum = new GridBagConstraints();
 		gbc_custPNum.insets = new Insets(0, 0, 5, 5);
 		gbc_custPNum.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custPNum.gridx = 2;
 		gbc_custPNum.gridy = 6;
 		registerCustomer.add(custPNum, gbc_custPNum);
 		custPNum.setColumns(10);
 		
 		JButton btnRegisterNow_1 = new JButton("Register Now");
 		btnRegisterNow_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				CustomerOperations c = new CustomerOperations();
 				if (c.insert(custRegId.getText(), custRegPW.getText(), custRegName.getText(), custRegAddr.getText(), custPNum.getText())){
 					System.out.println("Insert attempted");
 					goToOnlinePurchase();
 					System.out.println("Customer Insertion Successful");
 				}
 				else System.out.println("Customer Insertion Error");
 			}
 
 			private void goToOnlinePurchase() {
 				// TODO Auto-generated method stub
 				CardLayout cl = (CardLayout) customerOperations.getLayout();
 				 cl.show(customerOperations, "name_1452390541607518");
 			}
 		});
 		GridBagConstraints gbc_btnRegisterNow_1 = new GridBagConstraints();
 		gbc_btnRegisterNow_1.insets = new Insets(0, 0, 0, 5);
 		gbc_btnRegisterNow_1.gridx = 2;
 		gbc_btnRegisterNow_1.gridy = 8;
 		registerCustomer.add(btnRegisterNow_1, gbc_btnRegisterNow_1);
 		
 		JPanel onlinePurchase = new JPanel();
 		onlinePurchase.setBorder(new EmptyBorder(10, 10, 10, 10));
 		customerOperations.add(onlinePurchase, "name_1452390541607518");
 		GridBagLayout gbl_onlinePurchase = new GridBagLayout();
 		gbl_onlinePurchase.columnWidths = new int[] {15, 10, 10, 10, 0, 0, 10, 10, 10, 10, 10, 10};
 		gbl_onlinePurchase.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20};
 		gbl_onlinePurchase.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0};
 		gbl_onlinePurchase.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		onlinePurchase.setLayout(gbl_onlinePurchase);
 		
 		JLabel label_1 = new JLabel("Item Search");
 		label_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.insets = new Insets(0, 0, 5, 5);
 		gbc_label_1.gridx = 0;
 		gbc_label_1.gridy = 1;
 		onlinePurchase.add(label_1, gbc_label_1);
 		
 		JLabel label_3 = new JLabel("Quantity:");
 		GridBagConstraints gbc_label_3 = new GridBagConstraints();
 		gbc_label_3.insets = new Insets(0, 0, 5, 5);
 		gbc_label_3.gridx = 1;
 		gbc_label_3.gridy = 1;
 		onlinePurchase.add(label_3, gbc_label_3);
 		
 		opqty = new JTextField();
 		GridBagConstraints gbc_opqty = new GridBagConstraints();
 		gbc_opqty.gridwidth = 5;
 		gbc_opqty.insets = new Insets(0, 0, 5, 5);
 		gbc_opqty.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opqty.gridx = 2;
 		gbc_opqty.gridy = 1;
 		onlinePurchase.add(opqty, gbc_opqty);
 		opqty.setColumns(10);
 		
 		JLabel lblTitle = new JLabel("Title:");
 		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
 		gbc_lblTitle.anchor = GridBagConstraints.WEST;
 		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTitle.gridx = 8;
 		gbc_lblTitle.gridy = 1;
 		onlinePurchase.add(lblTitle, gbc_lblTitle);
 		
 		optitle = new JTextField();
 		optitle.setColumns(10);
 		GridBagConstraints gbc_optitle = new GridBagConstraints();
 		gbc_optitle.gridwidth = 3;
 		gbc_optitle.insets = new Insets(0, 0, 5, 5);
 		gbc_optitle.fill = GridBagConstraints.HORIZONTAL;
 		gbc_optitle.gridx = 9;
 		gbc_optitle.gridy = 1;
 		onlinePurchase.add(optitle, gbc_optitle);
 		
 		JLabel lblLeadingSingers = new JLabel("Leading Singers:");
 		GridBagConstraints gbc_lblLeadingSingers = new GridBagConstraints();
 		gbc_lblLeadingSingers.anchor = GridBagConstraints.WEST;
 		gbc_lblLeadingSingers.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLeadingSingers.gridx = 1;
 		gbc_lblLeadingSingers.gridy = 2;
 		onlinePurchase.add(lblLeadingSingers, gbc_lblLeadingSingers);
 		
 		opls = new JTextField();
 		GridBagConstraints gbc_opls = new GridBagConstraints();
 		gbc_opls.gridwidth = 5;
 		gbc_opls.insets = new Insets(0, 0, 5, 5);
 		gbc_opls.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opls.gridx = 2;
 		gbc_opls.gridy = 2;
 		onlinePurchase.add(opls, gbc_opls);
 		opls.setColumns(10);
 		
 		JLabel lblCategory = new JLabel("Category:");
 		GridBagConstraints gbc_lblCategory = new GridBagConstraints();
 		gbc_lblCategory.anchor = GridBagConstraints.WEST;
 		gbc_lblCategory.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCategory.gridx = 8;
 		gbc_lblCategory.gridy = 2;
 		onlinePurchase.add(lblCategory, gbc_lblCategory);
 		
 		opCategory = new JTextField();
 		opCategory.setColumns(10);
 		GridBagConstraints gbc_opCategory = new GridBagConstraints();
 		gbc_opCategory.gridwidth = 3;
 		gbc_opCategory.insets = new Insets(0, 0, 5, 5);
 		gbc_opCategory.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opCategory.gridx = 9;
 		gbc_opCategory.gridy = 2;
 		onlinePurchase.add(opCategory, gbc_opCategory);
 		
 		JLabel lblSearchItems = new JLabel("Search Items");
 		lblSearchItems.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblSearchItems = new GridBagConstraints();
 		gbc_lblSearchItems.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblSearchItems.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSearchItems.gridx = 0;
 		gbc_lblSearchItems.gridy = 3;
 		onlinePurchase.add(lblSearchItems, gbc_lblSearchItems);
 		
 		JLabel lblSearchResults = new JLabel("Shopping Cart");
 		lblSearchResults.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblSearchResults = new GridBagConstraints();
 		gbc_lblSearchResults.gridwidth = 4;
 		gbc_lblSearchResults.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSearchResults.gridx = 8;
 		gbc_lblSearchResults.gridy = 3;
 		onlinePurchase.add(lblSearchResults, gbc_lblSearchResults);
 		
 		JButton btnSearchItems = new JButton("Search Items");
 		btnSearchItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Integer qty = Integer.parseInt(opqty.getText().trim());
 				String title = optitle.getText().trim();
 				String category = opCategory.getText().trim();
 				String ls = opls.getText().trim();
 				PurchaseOperations p = new PurchaseOperations();
 				
 //				if (p.onlinePurchaseItemSearch(title,category,ls,qty)){
 //					System.out.println("Query was successful. Item should be displayed in search table.");
 //				}
 			}
 		});
 		GridBagConstraints gbc_btnSearchItems = new GridBagConstraints();
 		gbc_btnSearchItems.gridwidth = 2;
 		gbc_btnSearchItems.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSearchItems.gridx = 12;
 		gbc_btnSearchItems.gridy = 3;
 		onlinePurchase.add(btnSearchItems, gbc_btnSearchItems);
 		
 		table_1 = new JTable();
 		table_1.setModel(new DefaultTableModel(
 			new Object[][] {
 				{null, null, null, null, null},
 				{null, null, null, null, null},
 				{null, null, null, null, null},
 				{null, null, null, null, null},
 				{null, null, null, null, null},
 			},
 			new String[] {
 				"UPC", "Title", "Category", "Lead Singer", "Price"
 			}
 		));
 		JScrollPane scrollPane_2 = new JScrollPane(table_1);
 		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
 		gbc_scrollPane_2.gridheight = 5;
 		gbc_scrollPane_2.gridwidth = 7;
 		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_2.gridx = 0;
 		gbc_scrollPane_2.gridy = 4;
 		onlinePurchase.add(scrollPane_2, gbc_scrollPane_2);
 		
 		table_2 = new JTable();	
 		table_2.setModel(new DefaultTableModel(
 			new Object[][] {
 				{null, null, null},
 				{null, null, null},
 				{null, null, null},
 				{null, null, null},
 				{null, null, null},
 			},
 			new String[] {
 				"UPC", "Quantity", "Price"
 			}
 		));
 		JScrollPane scrollPane_1 = new JScrollPane(table_2);
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_1.gridheight = 5;
 		gbc_scrollPane_1.gridwidth = 7;
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.gridx = 7;
 		gbc_scrollPane_1.gridy = 4;
 		onlinePurchase.add(scrollPane_1, gbc_scrollPane_1);
 		
 		JButton btnAddItem_2 = new JButton("Add Item");
 		btnAddItem_2.addActionListener(new ActionListener() {
 			//TODO: ONLINE PURCHASE ADD ITEM BUTTON
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		GridBagConstraints gbc_btnAddItem_2 = new GridBagConstraints();
 		gbc_btnAddItem_2.insets = new Insets(0, 0, 5, 5);
 		gbc_btnAddItem_2.gridx = 0;
 		gbc_btnAddItem_2.gridy = 10;
 		onlinePurchase.add(btnAddItem_2, gbc_btnAddItem_2);
 		
 		JLabel lblCardNumber = new JLabel("Card Number");
 		GridBagConstraints gbc_lblCardNumber = new GridBagConstraints();
 		gbc_lblCardNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblCardNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCardNumber.gridx = 1;
 		gbc_lblCardNumber.gridy = 10;
 		onlinePurchase.add(lblCardNumber, gbc_lblCardNumber);
 		
 		JButton btnSubmitOrder = new JButton("Submit Order");
 		
 		//TODO: Submit Online Purchase Button
 		btnSubmitOrder.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		
 		opcardno = new JTextField();
 		GridBagConstraints gbc_opcardno = new GridBagConstraints();
 		gbc_opcardno.gridwidth = 5;
 		gbc_opcardno.insets = new Insets(0, 0, 5, 5);
 		gbc_opcardno.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opcardno.gridx = 2;
 		gbc_opcardno.gridy = 10;
 		onlinePurchase.add(opcardno, gbc_opcardno);
 		opcardno.setColumns(10);
 		
 		JLabel lblCardExpiryDate_1 = new JLabel("Card Expiry Date");
 		GridBagConstraints gbc_lblCardExpiryDate_1 = new GridBagConstraints();
 		gbc_lblCardExpiryDate_1.anchor = GridBagConstraints.EAST;
 		gbc_lblCardExpiryDate_1.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCardExpiryDate_1.gridx = 1;
 		gbc_lblCardExpiryDate_1.gridy = 11;
 		onlinePurchase.add(lblCardExpiryDate_1, gbc_lblCardExpiryDate_1);
 		
 		opcardexpd = new JTextField();
 		opcardexpd.setColumns(10);
 		GridBagConstraints gbc_opcardexpd = new GridBagConstraints();
 		gbc_opcardexpd.gridwidth = 5;
 		gbc_opcardexpd.insets = new Insets(0, 0, 0, 5);
 		gbc_opcardexpd.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opcardexpd.gridx = 2;
 		gbc_opcardexpd.gridy = 11;
 		onlinePurchase.add(opcardexpd, gbc_opcardexpd);
 		GridBagConstraints gbc_btnSubmitOrder = new GridBagConstraints();
 		gbc_btnSubmitOrder.gridwidth = 5;
 		gbc_btnSubmitOrder.gridx = 9;
 		gbc_btnSubmitOrder.gridy = 11;
 		onlinePurchase.add(btnSubmitOrder, gbc_btnSubmitOrder);
 	}
 	public void switchToNextCard(String accesslevel){
 		CardLayout cl = (CardLayout) (contentPane.getLayout());
 		switch (accesslevel){
 		case ("Clerk"):
 			cl.show(contentPane, "name_1442560847488307");
 			break;
 			
 		case ("Customer" ):
 			cl.show(contentPane, "name_1452058092586835");
 			break;
 		
 		case ("Manager"):
 			cl.show(contentPane, "name_1449473464875310");
 			break;
 			
 				
 		}
 	}
 public static void addInstoreItemToPurchase(String item){
 	purchaseItems.append(item+"\n");
 }
 public static void savePurchaseItem(String arg){
 	savedInstoreItems.add(arg);
 }
 public static void clearPurchaseList(){
 	savedInstoreItems.clear();
 }
 public static ArrayList<String> getPurchaseItems(){
 	return savedInstoreItems;
 }
 public static Integer getNumberInstorePurchaseItems(){
 	return numberOfItems;
 }
 }
