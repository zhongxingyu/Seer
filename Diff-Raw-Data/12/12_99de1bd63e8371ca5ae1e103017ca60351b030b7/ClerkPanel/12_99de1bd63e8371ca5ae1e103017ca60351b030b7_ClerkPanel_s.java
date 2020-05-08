 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.sql.Date;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import java.sql.Connection;
 
 import main.Clerk;
 
 public class ClerkPanel {
 
 	// Add Borrower form fields
 	private JTextField passwordField;
 	private JTextField nameField;
 	private JTextField addressField;
 	private JTextField phoneField;
 	private JTextField emailField;
 	private JTextField sinOrStNoField;
 	private JFormattedTextField expiryDateField;
 	private JComboBox typeComboBox;
 	
 	// Checkout form fields
 	private JTextField bidField;
 	private JTextField callNumberField;
 	
 	// Return form fields;
 	private JTextField copyNoField;
 	
 	private JPanel mainPanel;
 
 	private Connection con;
 	
 	public ClerkPanel(Connection con) {
 		this.con = con;
 	}
 
 	private void openAddBorrowerForm(){
 		// Add borrower Form
 		JPanel addBorrowerForm = new JPanel();
 		// Set form layout
 		addBorrowerForm.setLayout(new GridLayout(0, 2, 10, 10));
 		addBorrowerForm.setBorder(new EmptyBorder(10, 10, 10, 10) );
 
 		Font bItalic = new Font("Arial", Font.ITALIC, 15);
 //		loginButton.setFont(bItalic);
 		
 		// Field Labels
 		JLabel passwordLabel = new JLabel("Password: ");
 		JLabel nameLabel = new JLabel("Name: ");
 		JLabel addressLabel = new JLabel("Address: ");
 		JLabel phoneLabel = new JLabel("Phone: ");
 		JLabel emailLabel = new JLabel("Email Address: ");
 		JLabel sinOrStNoLabel = new JLabel("SIN/Student #: ");
 		JLabel expiryDateLabel = new JLabel("Expiry Date(dd/mm/yyyy): ");
 		JLabel typeLabel = new JLabel("Type: ");
 
 		// Fields
 		passwordField = new JTextField(10);
 		nameField = new JTextField(10);
 		addressField = new JTextField(10);
 		phoneField = new JTextField(10);
 		emailField = new JTextField(10);
 		sinOrStNoField = new JTextField(10);
 		expiryDateField = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
 		
 		//typeField = new JTextField(10);
 		String[] types = {"", "Student", "Faculty", "Staff"};
 		typeComboBox = new JComboBox(types);
 		
 		// Buttons
 		JButton addButton = new JButton("Add");
 		JButton cancelButton = new JButton("Cancel");
 		
 		passwordLabel.setFont(bItalic);
 		nameLabel.setFont(bItalic);
 		addressLabel.setFont(bItalic);
 		phoneLabel.setFont(bItalic);
 		emailLabel.setFont(bItalic);
 		sinOrStNoLabel.setFont(bItalic);
 		expiryDateLabel.setFont(bItalic);
 		typeLabel.setFont(bItalic);
 		addButton.setFont(bItalic);
 		cancelButton.setFont(bItalic);
 
 		// Add components to panel
 		addBorrowerForm.add(nameLabel);
 		addBorrowerForm.add(nameField);
 		addBorrowerForm.add(passwordLabel);
 		addBorrowerForm.add(passwordField);
 		addBorrowerForm.add(addressLabel);
 		addBorrowerForm.add(addressField);
 		addBorrowerForm.add(phoneLabel);
 		addBorrowerForm.add(phoneField);
 		addBorrowerForm.add(emailLabel);
 		addBorrowerForm.add(emailField);
 		addBorrowerForm.add(sinOrStNoLabel);
 		addBorrowerForm.add(sinOrStNoField);
 		addBorrowerForm.add(expiryDateLabel);
 		addBorrowerForm.add(expiryDateField);
 		addBorrowerForm.add(typeLabel);
 		addBorrowerForm.add(typeComboBox);
 
 
 		addBorrowerForm.add(cancelButton);
 		addBorrowerForm.add(addButton);
 		
 		// Window
 		final JFrame frame = new JFrame("Add Borrower");
 		// Window Properties
 		frame.pack();
 		frame.setVisible(true);
 
 		
 		//Add content to the window.
 		frame.add(addBorrowerForm, BorderLayout.CENTER);
 		
 		frame.setVisible(true);
 		frame.setResizable(true);
 		frame.setSize(640,400);
 		frame.setLocation( 50, 50 );
 
 		// Button Listeners
 		addButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{	
 				String password = passwordField.getText();
 				if (password.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in a password.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				String name = nameField.getText();
 				if (name.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in a name.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				String address = addressField.getText();
 				if (address.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in an address.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				int phone = 0;
 				try{
 					phone = Integer.parseInt(phoneField.getText());
 				}
 				catch(NumberFormatException numExcept){
 					JOptionPane.showMessageDialog(null,
 							"Invalid phone number.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				};
 
 				String email = emailField.getText();
 				if (email.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in an email address.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				int sinOrStNo = 0;
 				try{
 					sinOrStNo = Integer.parseInt(sinOrStNoField.getText());
 				}
 				catch(NumberFormatException numExcept){
 					JOptionPane.showMessageDialog(null,
 							"Invalid SIN/Student number.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				};
 
 				Date expiryDate = (Date) expiryDateField.getValue();
 				if (expiryDate.equals(null)) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in an expiry date.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				String type = (String)typeComboBox.getSelectedItem();
 				if (type.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please select borrower type.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				Clerk.addBorrower(password, name, address, phone, email, sinOrStNo, expiryDate, type);
 				frame.setVisible(false);
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				frame.setVisible(false);
 			}
 		});
 	}
 	
 	private void openCheckoutForm(){
 		// Add checkout Form
 		JPanel checkoutForm = new JPanel();
 		// Set form layout
 		checkoutForm.setLayout(new GridLayout(0, 2, 10, 10));
 		checkoutForm.setBorder(new EmptyBorder(10, 10, 10, 10) );
 
 		Font bItalic = new Font("Arial", Font.ITALIC, 15);
 //		loginButton.setFont(bItalic);
 		
 		
 		// Field Labels
 		JLabel bidLabel = new JLabel("Bid: ");
 		JLabel callNumberLabel = new JLabel("Call Numbers(separated by ;): ");
 		// Fields
 		bidField = new JTextField(10); 
 		callNumberField = new JTextField(10);
 		
 		// Buttons
 		JButton checkoutButton = new JButton("Checkout");
 		JButton cancelButton = new JButton("Cancel");
 
 		bidLabel.setFont(bItalic);
 		callNumberLabel.setFont(bItalic);
 		checkoutButton.setFont(bItalic);
 		cancelButton.setFont(bItalic);
 		
 		// Add components to panel
 		checkoutForm.add(bidLabel);
 		checkoutForm.add(bidField);
 		checkoutForm.add(callNumberLabel);
 		checkoutForm.add(callNumberField);		
 		checkoutForm.add(cancelButton);		
 		checkoutForm.add(checkoutButton);
 
 
 		// Window
 		final JFrame frame = new JFrame("Checkout");
 		// Window Properties
 		frame.pack();
 		frame.setVisible(true);
 
 		//Add content to the window.
 		frame.add(checkoutForm, BorderLayout.CENTER);
 
 		
 		frame.setVisible(true);
 		frame.setResizable(true);
 		frame.setSize(550,360/2);
 		frame.setLocation( 100, 140 );
 
 		// Button Listeners
 		checkoutButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				int bid = 0;
 				try{
 					bid = Integer.parseInt(bidField.getText());
 				}
 				catch(NumberFormatException numExcept){
 					JOptionPane.showMessageDialog(null,
 							"Invalid bid.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				};
 				
 				String callNumber = callNumberField.getText();
				ArrayList<String> callNumbers;
 				if (callNumber.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in call numbers.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				Clerk.checkoutItems(bid, callNumbers);
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				frame.setVisible(false);
 			}
 		});
 	}
 	
 	private void openReturnForm(){
 		// Add checkout Form
 		JPanel returnForm = new JPanel();
 		// Set form layout
 		returnForm.setLayout(new GridLayout(0, 2, 10, 10));
 		returnForm.setBorder(new EmptyBorder(10, 10, 10, 10) );
 
 		Font bItalic = new Font("Arial", Font.ITALIC, 15);
 //		loginButton.setFont(bItalic);
 		
 		// Field Labels
 		JLabel callNumberLabel = new JLabel("Call Number: ");
 		JLabel copyNoLabel = new JLabel("Copy Number: ");
 		// Fields
 		callNumberField = new JTextField(10);
 		copyNoField = new JTextField(10);
 		
 		// Buttons
 		JButton returnButton = new JButton("Return");
 		JButton cancelButton = new JButton("Cancel");
 		
 		callNumberLabel.setFont(bItalic);
 		copyNoLabel.setFont(bItalic);
 		returnButton.setFont(bItalic);
 		cancelButton.setFont(bItalic);
 		
 		
 		
 
 		// Add components to panel
 		returnForm.add(callNumberLabel);
 		returnForm.add(callNumberField);
 		returnForm.add(copyNoLabel);
 		returnForm.add(copyNoField);
 		
 		returnForm.add(cancelButton);
 		returnForm.add(returnButton);
 
 		// Window
 		final JFrame frame = new JFrame("Checkout");
 		// Window Properties
 		frame.pack();
 		frame.setVisible(true);
 
 		//Add content to the window.
 		frame.add(returnForm, BorderLayout.CENTER);
 
 		frame.setVisible(true);
 		frame.setResizable(true);
 		frame.setSize(640/2,360/2);
 		frame.setLocation( 180, 140 );
 
 		// Button Listeners
 		returnButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{				
 				String callNumber = callNumberField.getText();
 				if (callNumber.equals("")) {
 					JOptionPane.showMessageDialog(null,
 							"Please fill in call number.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				int copyNo = 0;
 				try{
 					copyNo = Integer.parseInt(bidField.getText());
 				}
 				catch(NumberFormatException numExcept){
 					JOptionPane.showMessageDialog(null,
 							"Invalid copy number.",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				};
 				
 				Clerk.processReturn(callNumber, copyNo);
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				frame.setVisible(false);
 			}
 		});
 	}
 	
 	private void openOverdueForm(){
 
 		
 		JPanel overdueForm = new JPanel();
 
 		overdueForm.setLayout(new GridLayout(0, 1, 10, 10));
 		overdueForm.setBorder(new EmptyBorder(10, 10, 10, 10) );
 		
 		final String[] columnNames = {"Call Number", "Copy #", "Title", "Bid", "Name", "Select"};
 		Object[][] data = {};
 
 		final DefaultTableModel model = new DefaultTableModel(data,columnNames);
 
 	
 		// Add table to view items
 		JTable overdueTable = new JTable(model);
 		
 		TableColumn tc = overdueTable.getColumnModel().getColumn(5);  
         tc.setCellEditor(overdueTable.getDefaultEditor(Boolean.class));  
         tc.setCellRenderer(overdueTable.getDefaultRenderer(Boolean.class));
 
 		model.insertRow(overdueTable.getRowCount(),new Object[]{"Call1", "1", "Book1", "11", "Mark", new Boolean(false)});
 		model.insertRow(overdueTable.getRowCount(),new Object[]{"Call2", "2", "Book2", "22", "John", new Boolean(false)});
 		model.insertRow(overdueTable.getRowCount(),new Object[]{"Call3", "3", "Book3", "33", "Sam", new Boolean(false)});
 		model.insertRow(overdueTable.getRowCount(),new Object[]{"Call4", "4", "Book4", "44", "Bill", new Boolean(false)});
 		
 		
 		Font bItalic = new Font("Arial", Font.ITALIC, 15);
 //		loginButton.setFont(bItalic);
 		
 		// Buttons
 		JButton sendSeleButton = new JButton("Send to selected");
 		JButton sendAllButton = new JButton("Send to All");
 		
 		sendSeleButton.setFont(bItalic);
 		sendAllButton.setFont(bItalic);
 		
 
 		// Button panel
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(sendSeleButton);
 		buttonPanel.add(sendAllButton);
 		
 		
 		// Add table to view items
 		JScrollPane scrollPane = new JScrollPane(overdueTable);
 
 		// Add components to panel
 		scrollPane.setPreferredSize(new Dimension(480, 200));
 		
 			overdueForm.add(scrollPane, BorderLayout.CENTER);
 			overdueForm.add(buttonPanel, BorderLayout.CENTER);	
 
 		// Window
 		final JFrame frame = new JFrame("Overdue Items");
 		// Window Properties
 		frame.pack();
 		frame.setVisible(true);
 
 		//Add content to the window.
 		frame.add(overdueForm, BorderLayout.CENTER);
 
 		frame.setVisible(true);
 		frame.setResizable(true);
 		frame.setSize(750,300);
 		frame.setLocation( 50, 50 );
 		
 
 		// Button Listeners
 		sendSeleButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{			
 				ArrayList<String> bids = new ArrayList<String>();
 				for(int x=0; x<model.getRowCount(); x++){
 					if(model.getValueAt(x, 5).equals(true)){
 						bids.add((String) model.getValueAt(x, 3));
 					}
 				}
 				for(String bid : bids){
 					System.out.println(bid);
 				}
 			}
 		});
 		
 		sendAllButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{			
 				ArrayList<String> bids = new ArrayList<String>();
 				for(int x=0; x<model.getRowCount(); x++){
 						bids.add((String) model.getValueAt(x, 3));
 				}
 				for(String bid : bids){
 					System.out.println(bid);
 				}
 			}
 		});
 
 
 	}
 
 
 	public JComponent getClerkPanel(){
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new GridLayout(0, 1, 10, 10));
 		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10) );
 		
 		Font bItalic = new Font("Arial", Font.ITALIC, 30);
 		
 		
 		
 		JButton addBorrowerButton = new JButton("Add Borrower");
 		addBorrowerButton.setFont(bItalic);
 		
 		JButton checkoutButton = new JButton("Checkout");
 		checkoutButton.setFont(bItalic);
 		
 		JButton processReturnButton = new JButton("Process Return");
 		processReturnButton.setFont(bItalic);
 		
 		JButton checkOverdueButton = new JButton("Check Overdue");
 		checkOverdueButton.setFont(bItalic);
 
 		addBorrowerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				openAddBorrowerForm();
 			}
 		});  
 
 		checkoutButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				openCheckoutForm();
 			}
 		});  
 
 		processReturnButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				openReturnForm();
 			}
 		});
 		checkOverdueButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				openOverdueForm();
 			}
 		});  
 
 		mainPanel.add(addBorrowerButton);
 		mainPanel.add(checkoutButton);
 		mainPanel.add(processReturnButton);
 		mainPanel.add(checkOverdueButton);
 
 		return mainPanel;
 
 	}
 }
